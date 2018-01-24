package pl.devone.android.mapboxexampleapp.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.app.Fragment
import android.content.*
import android.location.Location
import android.os.IBinder
import android.support.design.widget.FloatingActionButton
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds

import pl.devone.android.mapboxexampleapp.R
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import pl.devone.ipark.services.location.LocationProvider
import com.mapbox.services.android.telemetry.permissions.PermissionsManager
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.services.android.telemetry.permissions.PermissionsListener
import pl.devone.android.mapboxexampleapp.models.PastLocation
import kotlin.collections.ArrayList

class MapFragment : Fragment(), LocationProvider.LocationServiceListener, PermissionsListener {
    private val TAG = MapFragment::class.java.canonicalName

    private var mMap: MapboxMap? = null
    private var mMapView: MapView? = null
    private var mLocationProvider: LocationProvider? = null
    private var mLocationPlugin: LocationLayerPlugin? = null
    private var mPermissionsManager: PermissionsManager? = null
    private var mListenerMap: OnMapFragmentInteractionListener? = null
    private var mLastLocation: Location? = null
    private val mPastLocations: ArrayList<PastLocation> = ArrayList()

    private var locationServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            mLocationProvider = (service as LocationProvider.LocationBinder).getService().apply {
                registerListener(this@MapFragment)
                mLastLocation = getLastLocation()
            }
            mMapView!!.getMapAsync({ map ->
                mMap = map
                mMap!!.addOnMapClickListener { cameraIncludeAll(300, 1000 ) }
                enableLocationPlugin()
            })
        }
    }

    private fun init() {
        Mapbox.getInstance(context, resources.getString(R.string.mapbox_token))
        Log.i(TAG, "Initialization -> binding LocationProvider")
        context.bindService(Intent(context, LocationProvider::class.java),
                locationServiceConnection,
                if (PermissionsManager.areLocationPermissionsGranted(context)) Context.BIND_AUTO_CREATE else 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_map, container, false).apply {
            if (this != null) {
                mMapView = this.findViewById(R.id.map_view)
                mMapView!!.onCreate(savedInstanceState)
                val button: FloatingActionButton = this.findViewById(R.id.fab_add_location)
                button.setOnClickListener { createAddLocationDialog().show() }
            }

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

    @SuppressLint("InflateParams")
    private fun createAddLocationDialog(): AlertDialog {
        return AlertDialog.Builder(activity).apply {
            val view = activity.layoutInflater.inflate(R.layout.add_location_dialog, null)
            val etName = view.findViewById<EditText>(R.id.et_name)
            val etDescription = view.findViewById<EditText>(R.id.et_description)
            val npRadius = view.findViewById<NumberPicker>(R.id.np_radius)
            npRadius.minValue = 1
            npRadius.maxValue = 20
            setTitle(getString(R.string.add_new_location))
            setPositiveButton(getString(R.string.add)) { dialog, _ ->
                addLocation(PastLocation(mLastLocation!!, etName.text.toString(), etDescription.text.toString(), npRadius.value))
                cameraIncludeAll(300, 1000)
                dialog?.dismiss()
            }
            setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog?.dismiss() }
            setView(view)
        }.create()
    }

    private fun addLocation(location: PastLocation) {
        mMap?.addMarker(MarkerOptions().position(location)
                .title(location.name)
                .snippet(String.format("%s (%s)", location.description, location.radius)))
        mPastLocations.add(location)
        setCameraPosition(location.location)
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
        mMap?.animateCamera(CameraUpdateFactory
                .newLatLngZoom(LatLng(location.latitude, location.longitude), 13.0))
    }

    private fun cameraIncludeAll(padding: Int, duration: Int) {
        LatLngBounds.Builder().apply {
            mPastLocations.forEach { position -> this.include(position) }
            this.include(LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude))
            mMap!!.easeCamera(CameraUpdateFactory.newLatLngBounds(this.build(), padding), duration)
        }
    }

    override fun onLocationChanged(location: Location) {
        setCameraPosition(location)
        mLastLocation = location
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
