package pl.devone.android.mapboxexampleapp.services

import android.app.*
import android.content.Intent
import android.util.Log
import com.google.android.gms.common.GooglePlayServicesUtil.getErrorString
import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import pl.devone.android.mapboxexampleapp.R
import android.app.PendingIntent
import android.os.Build
import android.support.annotation.RequiresApi
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import java.util.*
import android.app.NotificationChannel
import android.app.NotificationManager


/**
 * Created by ljedrzynski on 25.01.2018.
 */
class GeofenceTransitionService : IntentService("GTC") {
    private val TAG = GeofenceTransitionService::class.java.getSimpleName()
    private val GEOFENCE_NOTIFICATION_ID = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onHandleIntent(intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMsg = getErrorString(geofencingEvent.errorCode)
            Log.e(TAG, errorMsg)
            return
        }
        val geoFenceTransition = geofencingEvent.geofenceTransition
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            sendNotification(getGeofenceTrasitionDetails(geoFenceTransition, geofencingEvent.triggeringGeofences))
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(msg: String) {
        Log.i(TAG, "sendNotification: " + msg)
        val resultIntent = Intent(this, GeofenceTransitionService::class.java)
        val resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = getString(R.string.geofence_notification_channel)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(channelId) == null) {
            notificationManager.createNotificationChannel(NotificationChannel(channelId, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW).apply {
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            })
        }

        notificationManager.notify(
                GEOFENCE_NOTIFICATION_ID,
                createNotification(msg, channelId, resultPendingIntent))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(msg: String, channelId: String, notificationPendingIntent: PendingIntent): Notification =
            Notification.Builder(this, channelId)
                    .setSmallIcon(R.drawable.mapbox_compass_icon)
                    .setColor(Color.RED)
                    .setContentTitle(msg)
                    .setContentText(getString(R.string.geofence_notification))
                    .setContentIntent(notificationPendingIntent).build()

}