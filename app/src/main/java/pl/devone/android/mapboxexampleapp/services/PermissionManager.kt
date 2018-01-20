//package pl.devone.android.mapboxexampleapp.services
//
//import android.Manifest
//import android.app.AlertDialog
//import android.app.Fragment
//import android.content.Context
//import android.content.pm.PackageManager
//import android.support.v4.app.ActivityCompat
//import pl.devone.android.mapboxexampleapp.R
//
///**
// * Created by ljedrzynski on 20.01.2018.
// */
//object PermissionManager {
//    val PERMISSION_REQUEST_LOCATION = 99
//
//    fun requestLocationPermission(fragment: Fragment) {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(fragment.activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
//            AlertDialog.Builder(fragment.context)
//                    .setTitle(fragment.getString(R.string.title_location_permission))
//                    .setMessage(fragment.getString(R.string.info_location_permission))
//                    .setPositiveButton("OK") { dialogInterface, i ->
//                        fragment.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                                PERMISSION_REQUEST_LOCATION)
//                    }
//                    .create()
//                    .show()
//        } else {
//            fragment.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                    PERMISSION_REQUEST_LOCATION)
//        }
//    }
//
//    fun hasLocationPermission(context: Context): Boolean {
//        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
//    }
//}
