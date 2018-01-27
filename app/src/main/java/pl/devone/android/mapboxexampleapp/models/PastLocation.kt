package pl.devone.android.mapboxexampleapp.models

import android.location.Location
import com.mapbox.mapboxsdk.geometry.LatLng

/**
 * Created by ljedrzynski on 22.01.2018.
 */

class PastLocation : LatLng {
    var name: String? = null
    var description: String? = null
    var radius: Float? = null

    constructor() : super()

    constructor(location: Location, name: String, description: String, radius: Float) : super(location.latitude, location.longitude) {
        this.name = name
        this.description = description
        this.radius = radius
    }


}

