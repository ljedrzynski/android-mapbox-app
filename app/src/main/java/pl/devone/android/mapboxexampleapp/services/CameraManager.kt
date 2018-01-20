package pl.devone.android.mapboxexampleapp.services

import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import java.util.*

/**
 * Created by ljedrzynski on 20.01.2018.
 */
object CameraManager {

    fun easeCamera(map: MapboxMap, latLng: LatLng, zoom: Double, bearing: Double, tilt: Double, duration: Int) {
        map.easeCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                        .target(latLng)
                        .zoom(zoom)
                        .bearing(bearing)
                        .tilt(tilt)
                        .build()), duration)
    }

    fun cameraIncludePositions(mapboxMap: MapboxMap, padding: Int, duration: Int, positions: List<LatLng>) {
        cameraIncludePositions(mapboxMap, padding, duration, HashSet(positions))
    }

    fun cameraIncludePositions(mapboxMap: MapboxMap?, padding: Int, duration: Int, positions: Set<LatLng>?) {
        if (positions == null) {
            throw RuntimeException("Positions cannot be null! Fatal error!")
        }
        if (mapboxMap == null) {
            throw RuntimeException("MapBoxMap cannot be null. Fatal error!")
        }
        if (positions.size == 1) {
            easeCamera(mapboxMap, positions.iterator().next(), 16.0, mapboxMap.cameraPosition.bearing, 45.0, 1000)
            return
        }

        val builder = LatLngBounds.Builder()
        for (position in positions) {
            builder.include(position)
        }

        mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding), duration)
    }

    fun cameraIncludePositions(mapboxMap: MapboxMap, padding: Int, duration: Int, vararg positions: LatLng) {
        cameraIncludePositions(mapboxMap, padding, duration, HashSet(Arrays.asList(*positions)))
    }
}