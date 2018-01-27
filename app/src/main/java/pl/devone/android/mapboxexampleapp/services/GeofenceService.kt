package pl.devone.android.mapboxexampleapp.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import android.app.PendingIntent
import android.os.Binder
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.services.android.telemetry.permissions.PermissionsManager
import pl.devone.android.mapboxexampleapp.models.PastLocation
import java.util.stream.Collectors


/**
 * Created by ljedrzynski on 25.01.2018.
 */
class GeofenceService : Service(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private val TAG: String = GeofenceService::class.java.canonicalName
    private var mGeofencePendingIntent: PendingIntent? = null
    private var mGeofencingClient: GeofencingClient? = null
    private val GEOFENCE_REQ_ID = String.format("gf_%s", System.currentTimeMillis() % 1000)

    inner class GeofenceBinder : Binder() {
        fun getService(): GeofenceService = this@GeofenceService
    }

    override fun onCreate() {
        super.onCreate()
        if (!PermissionsManager.areLocationPermissionsGranted(applicationContext)) {
            stopSelf()
        }
        mGeofencingClient = LocationServices.getGeofencingClient(this)
    }

    override fun onBind(intent: Intent?): Binder = GeofenceBinder()

    @SuppressLint("MissingPermission")
    private fun addGeofences(request: GeofencingRequest) {
        Log.i(TAG, String.format("addGeofences: Request->{%s}", request.toString()))
        mGeofencingClient!!.addGeofences(request, getGeofencePendingIntent())
                .addOnSuccessListener { Log.i(TAG, "addGeofences:success") }
                .addOnFailureListener { exception -> Log.e(TAG, String.format("addGeofences: Failure->{%s}", exception)) }
    }

    fun createGeofences(locations: List<PastLocation>) =
            addGeofences(buildGeofenceRequest(locations.stream()
                    .map { t -> buildGeofence(t) }
                    .collect(Collectors.toList()) as List<Geofence>))

    fun createGeofence(location: PastLocation) =
            addGeofences(buildGeofenceRequest(buildGeofence(location)))


    private fun buildGeofence(location: PastLocation): Geofence =
            Geofence.Builder()
                    .setRequestId(location.name)
                    .setCircularRegion(location.latitude, location.longitude, location.radius!! * 1000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .build()


    private fun buildGeofenceRequest(geofence: Geofence): GeofencingRequest =
            GeofencingRequest.Builder()
                    .addGeofence(geofence)
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT)
                    .build()


    private fun buildGeofenceRequest(geofences: List<Geofence>): GeofencingRequest =
            GeofencingRequest.Builder()
                    .addGeofences(geofences)
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT)
                    .build()

    private fun getGeofencePendingIntent(): PendingIntent? {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent
        }
        val intent = Intent(this, GeofenceTransitionService::class.java)
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return mGeofencePendingIntent
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.e(TAG, String.format("onConnectionFailed->{%s}", p0))
    }

    override fun onConnected(p0: Bundle?) {
        Log.i(TAG, String.format("onConnected->{%s}", p0))
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.i(TAG, String.format("onConnectionSuspended->{%s}", p0))
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}