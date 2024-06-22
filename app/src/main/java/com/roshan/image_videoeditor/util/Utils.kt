package com.roshan.image_videoeditor.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.app.ActivityCompat

class Utils {
    companion object {
        private fun hasPermissions(context: Activity?, permissions: Array<String>): Boolean {
            if (context != null) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            permission
                        ) != PackageManager.PERMISSION_GRANTED
                    ) return false
                }
            }
            return true
        }



        fun isInternetAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        }




        private val PERMISSION_ALL = 1

        private var PERMISSIONS_ANDROID_7_10 = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE
        )

        private var PERMISSIONS_ANDROID_11_12 = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.ACCESS_NETWORK_STATE
        )

        private var PERMISSIONS_ANDROID_13_ABOVE = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.ACCESS_NETWORK_STATE
        )


        fun askPermissions(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!hasPermissions(activity, PERMISSIONS_ANDROID_13_ABOVE)) {
                    ActivityCompat.requestPermissions(activity,
                        PERMISSIONS_ANDROID_13_ABOVE, PERMISSION_ALL)
                }
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                if (!hasPermissions(activity, PERMISSIONS_ANDROID_11_12)) {
                    ActivityCompat.requestPermissions(activity,
                        PERMISSIONS_ANDROID_11_12, PERMISSION_ALL)
                }
            } else {
                if (!hasPermissions(activity, PERMISSIONS_ANDROID_7_10)) {
                    ActivityCompat.requestPermissions(activity,
                        PERMISSIONS_ANDROID_7_10, PERMISSION_ALL)
                }
            }
        }


        fun checkPermissions(activity: Activity) : Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!hasPermissions(activity, PERMISSIONS_ANDROID_13_ABOVE)) {
                    return false
                }
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                if (!hasPermissions(activity, PERMISSIONS_ANDROID_11_12)) {
                    return false
                }
            } else {
                if (!hasPermissions(activity, PERMISSIONS_ANDROID_7_10)) {
                    return false
                }
            }

            return true
        }




        fun checkCameraPermission(activity: Activity) : Boolean {
            return hasPermissions(activity, arrayOf(Manifest.permission.CAMERA))
        }
    }
}