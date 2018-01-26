package pl.devone.android.mapboxexampleapp.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import com.mapbox.services.android.telemetry.location.LocationEngine
import com.mapbox.services.android.telemetry.location.LocationEngineListener
import com.mapbox.services.android.telemetry.location.LocationEnginePriority
import com.mapbox.services.android.telemetry.location.LostLocationEngine
import com.mapbox.services.android.telemetry.permissions.PermissionsManager


/**
 * Created by ljedrzynski on 02.06.2017.
 */

class LocationService : Service(), LocationEngineListener {

    private lateinit var mLocationEngine: LocationEngine
    private var mOriginLocation: Location? = null
    private val mLocationServiceListeners = ArrayList<LocationServiceListener>()

    inner class LocationBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        if (!PermissionsManager.areLocationPermissionsGranted(applicationContext)) {
            stopSelf()
        }
        mLocationEngine = LostLocationEngine(this).apply {
            priority = LocationEnginePriority.HIGH_ACCURACY
            activate()
            requestLocationUpdates()
        }

        val lastLocation = mLocationEngine.lastLocation
        if (lastLocation != null) {
            mOriginLocation = lastLocation
        } else {
            mLocationEngine.addLocationEngineListener(this)
        }
    }

    override fun onBind(intent: Intent): IBinder? = LocationBinder()

    fun registerListener(locationServiceListener: LocationServiceListener) {
        if (!mLocationServiceListeners.contains(locationServiceListener)) {
            mLocationServiceListeners.add(locationServiceListener)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onConnected() = mLocationEngine.requestLocationUpdates()

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            mOriginLocation = location
            mLocationServiceListeners.forEach { listener -> listener.onLocationChanged(location) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocationEngine.deactivate()
    }

    fun getLastLocation(): Location? = mOriginLocation

    fun getLocationEngine(): LocationEngine? = mLocationEngine

    interface LocationServiceListener {
        fun onLocationChanged(location: Location)
    }
}
