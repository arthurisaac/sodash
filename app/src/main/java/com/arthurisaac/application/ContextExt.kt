package com.arthurisaac.application

import android.content.Context
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager

fun Context.hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}