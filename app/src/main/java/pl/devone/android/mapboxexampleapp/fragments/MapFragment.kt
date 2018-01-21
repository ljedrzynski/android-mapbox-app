package pl.devone.android.mapboxexampleapp.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.app.Fragment
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng

import pl.devone.android.mapboxexampleapp.R
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import pl.devone.ipark.services.location.LocationProvider
import com.mapbox.services.android.telemetry.permissions.PermissionsManager
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.services.android.telemetry.permissions.PermissionsListener


class MapFragment : Fragment(), LocationProvider.LocationServiceListener, PermissionsListener {
    private val TAG = MapFragment::class.java.canonicalName

    private var mMap: MapboxMap? = null
    private var mMapView: MapView? = null
    private var mLocationProvider: LocationProvider? = null
    private var mLocationPlugin: LocationLayerPlugin? = null
    private var mPermissionsManager: PermissionsManager? = null
    private var mListenerMap: OnMapFragmentInteractionListener? = null
    private var mLastLocation: Location? = null

    private var locationServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            mLocationProvider = (service as LocationProvider.LocationBinder).getService().apply {
                registerListener(this@MapFragment)
                mLastLocation = getLastLocation()
            }

            mMapView!!.getMapAsync({ mapboxMap ->
                mMap = mapboxMap
                enableLocationPlugin()
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        Mapbox.getInstance(context, resources.getString(R.string.mapbox_token))
        Log.i(TAG, "Initialization -> binding LocationProvider")
        context.bindService(Intent(context, LocationProvider::class.java),
                locationServiceConnection,
                if (PermissionsManager.areLocationPermissionsGranted(context)) Context.BIND_AUTO_CREATE else 0)
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationPlugin() {
        if (PermissionsManager.areLocationPermissionsGranted(this.context)) {
            mLocationPlugin = LocationLayerPlugin(mMapView!!, mMap!!, mLocationProvider!!.getLocationEngine())
            mLocationPlugin!!.setLocationLayerEnabled(LocationLayerMode.TRACKING)
        } else {
            mPermissionsManager = PermissionsManager(this)
            mPermissionsManager!!.requestLocationPermissions(this.activity)
        }
    }

    private fun setCameraPosition(location: Location) {
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(location.latitude, location.longitude), 13.0))
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_map, container, false).apply {
            mMapView = this!!.findViewById(R.id.map_view)
            mMapView!!.onCreate(savedInstanceState)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        mLocationPlugin?.onStart()
        mMapView?.onStart()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnMapFragmentInteractionListener) {
            mListenerMap = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnMapFragmentInteractionListener")
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.d(TAG, String.format("New location -> %s", location.toString()))
        setCameraPosition(location)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStop() {
        super.onStop()
        mLocationPlugin?.onStop()
        mMapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocationProvider?.onDestroy()
        mMapView?.onDestroy()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationPlugin()
        } else {
            this.activity.finish()
        }
    }

    interface OnMapFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }
}
