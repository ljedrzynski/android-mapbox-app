package pl.devone.android.mapboxexampleapp.services

import android.app.*
import android.content.Intent
import android.util.Log
import com.google.android.gms.common.GooglePlayServicesUtil.getErrorString
import com.mapzen.android.lost.api.Geofence
import com.mapzen.android.lost.api.GeofencingEvent
import android.content.Context
import android.graphics.Color
import android.support.v4.app.NotificationCompat
import android.text.TextUtils
import pl.devone.android.mapboxexampleapp.R
import android.app.PendingIntent

/**
 * Created by ljedrzynski on 25.01.2018.
 */
class GeofenceTransitionService : IntentService("GTC") {
    private val TAG = GeofenceTransitionService::class.java.getSimpleName()
    private val GEOFENCE_NOTIFICATION_ID = 0

    override fun onHandleIntent(intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMsg = getErrorString(geofencingEvent.errorCode)
            Log.e(TAG, errorMsg)
            return
        }
        val geoFenceTransition = geofencingEvent.geofenceTransition
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            val geofenceTransitionDetails = getGeofenceTrasitionDetails(geoFenceTransition, triggeringGeofences)
            sendNotification(geofenceTransitionDetails)
        }
    }

    private fun getGeofenceTrasitionDetails(geoFenceTransition: Int, triggeringGeofences: List<Geofence>): String {
        val triggeringGeofencesList = ArrayList<String>()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesList.add(geofence.requestId)
        }

        var status: String? = null
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
            status = "Entering "
        else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
            status = "Exiting "
        return status!! + TextUtils.join(", ", triggeringGeofencesList)
    }

    private fun sendNotification(msg: String) {
        Log.i(TAG, "sendNotification: " + msg)
        val resultIntent = Intent(this, GeofenceTransitionService::class.java)
        val resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
                GEOFENCE_NOTIFICATION_ID,
                createNotification(msg, resultPendingIntent))
    }

    private fun createNotification(msg: String, notificationPendingIntent: PendingIntent): Notification {
        val notificationBuilder = NotificationCompat.Builder(this)
        notificationBuilder
                .setSmallIcon(R.drawable.mapbox_compass_icon)
                .setColor(Color.RED)
                .setContentTitle(msg)
                .setContentText("Geofence Notification!")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
        return notificationBuilder.build()
    }

}