package pl.devone.android.mapboxexampleapp.models

import android.location.Location
import com.mapbox.mapboxsdk.geometry.LatLng

/**
 * Created by ljedrzynski on 22.01.2018.
 */
data class PastLocation(val location: Location, val name: String, val description: String, val radius: Int) : LatLng(location.latitude, location.longitude)
