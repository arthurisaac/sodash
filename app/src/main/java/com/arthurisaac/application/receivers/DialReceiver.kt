package com.arthurisaac.application.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.arthurisaac.application.MainActivity


class DialReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val p: PackageManager = context.packageManager
        val componentName = ComponentName(context, MainActivity::class.java)
        p.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        val i : Intent = Intent(context, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        context.startActivity(i);
    }
}