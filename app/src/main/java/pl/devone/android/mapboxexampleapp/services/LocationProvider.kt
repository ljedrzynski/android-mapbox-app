package pl.devone.ipark.services.location

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

class LocationProvider : Service(), LocationEngineListener {

    private lateinit var locationEngine: LocationEngine
    private var originLocation: Location? = null
    private val mLocationServiceListeners = ArrayList<LocationServiceListener>()

    inner class LocationBinder : Binder() {
        fun getService(): LocationProvider = this@LocationProvider
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        if (!PermissionsManager.areLocationPermissionsGranted(applicationContext)) {
            stopSelf()
        }
        locationEngine = LostLocationEngine(this).apply {
            priority = LocationEnginePriority.HIGH_ACCURACY
            activate()
            requestLocationUpdates()
        }

        val lastLocation = locationEngine.lastLocation
        if (lastLocation != null) {
            originLocation = lastLocation
        } else {
            locationEngine.addLocationEngineListener(this)
        }
    }

    override fun onBind(intent: Intent): IBinder? = LocationBinder()

    fun registerListener(locationServiceListener: LocationServiceListener) {
        if (!mLocationServiceListeners.contains(locationServiceListener)) {
            mLocationServiceListeners.add(locationServiceListener)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onConnected() = locationEngine.requestLocationUpdates()

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            originLocation = location
            mLocationServiceListeners.forEach { listener -> listener.onLocationChanged(location) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationEngine.deactivate()
    }

    fun getLastLocation(): Location? = originLocation

    fun getLocationEngine(): LocationEngine? = locationEngine

    interface LocationServiceListener {
        fun onLocationChanged(location: Location)
    }
}
