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
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds

import pl.devone.android.mapboxexampleapp.R
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.services.android.telemetry.permissions.PermissionsManager
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.services.android.telemetry.permissions.PermissionsListener
import pl.devone.android.mapboxexampleapp.models.PastLocation
import pl.devone.android.mapboxexampleapp.services.GeofenceService
import pl.devone.android.mapboxexampleapp.services.LocationService
import java.util.HashMap
import kotlin.collections.ArrayList

class MapFragment : Fragment(), LocationService.LocationServiceListener, PermissionsListener {
    private val TAG = MapFragment::class.java.canonicalName
    private var mMap: MapboxMap? = null
    private var mMapView: MapView? = null
    private var mLocationService: LocationService? = null
    private var mGeofenceService: GeofenceService? = null
    private var mLocationPlugin: LocationLayerPlugin? = null
    private var mPermissionsManager: PermissionsManager? = null
    private var mListenerMap: OnMapFragmentInteractionListener? = null
    private var mLastLocation: Location? = null
    private val mPastLocations: ArrayList<PastLocation> = ArrayList()
    private var mFirebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()

    private var locationServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            mLocationService = (service as LocationService.LocationBinder).getService().apply {
                mLastLocation = getLastLocation()
                registerListener(this@MapFragment)
            }
            mMapView!!.getMapAsync({ map ->
                mMap = map
                mMap!!.addOnMapClickListener { cameraIncludeAll(400, 1000) }
                if (mPastLocations.size > 0) {
                    addMarkers(mPastLocations)
                }
                enableLocationPlugin()
            })
        }
    }

    private var geofenceServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            mGeofenceService = (service as GeofenceService.GeofenceBinder).getService().apply {
                if (mPastLocations.size > 0) {
                    createGeofences(mPastLocations)
                }
            }
        }
    }

    private fun init() {
        Mapbox.getInstance(context, resources.getString(R.string.mapbox_token))
        if (PermissionsManager.areLocationPermissionsGranted(this@MapFragment.context)) {
            mFirebaseDatabase.getReference("pastLocations")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            mPastLocations.clear()
                            dataSnapshot.children.forEach({ snapshot ->
                                mPastLocations.add(snapshot.getValue<PastLocation>(PastLocation::class.java)!!)
                            })
                            context.apply {
                                bindService(Intent(context, LocationService::class.java), locationServiceConnection, Context.BIND_AUTO_CREATE)
                                bindService(Intent(context, GeofenceService::class.java), geofenceServiceConnection, Context.BIND_AUTO_CREATE)
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.i(TAG, "The read failed " + databaseError.code)
                        }
                    })
        } else {
            mPermissionsManager = PermissionsManager(this@MapFragment)
            mPermissionsManager!!.requestLocationPermissions(this@MapFragment.activity)

        }
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
                button.setOnClickListener { buildAddLocationDialog().show() }
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
    private fun buildAddLocationDialog(): AlertDialog {
        return AlertDialog.Builder(activity).apply {
            val view = activity.layoutInflater.inflate(R.layout.add_location_dialog, null)
            val etName = view.findViewById<EditText>(R.id.et_name)
            val etDescription = view.findViewById<EditText>(R.id.et_description)
            val npRadius = view.findViewById<NumberPicker>(R.id.np_radius)
            npRadius.minValue = 1
            npRadius.maxValue = 20
            setTitle(getString(R.string.add_new_location))
            setPositiveButton(getString(R.string.add)) { dialog, _ ->
                val pastLocation = PastLocation(mLastLocation!!, etName.text.toString(), etDescription.text.toString(), npRadius.value.toFloat())
                saveLocation(pastLocation, OnSuccessListener {
                    addNewLocation(pastLocation)
                    cameraIncludeAll(400, 500)
                    dialog?.dismiss()
                })
            }
            setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog?.dismiss() }
            setView(view)
        }.create()
    }

    private fun addMarkers(locations: ArrayList<PastLocation>) {
        locations.forEach { location -> addMarker(location) }
    }


    private fun addMarker(location: PastLocation) {
        mMap!!.addMarker(MarkerOptions().position(location)
                .title(location.name)
                .snippet(String.format("%s (%s)", location.description, location.radius)))
    }

    private fun addNewLocation(location: PastLocation) {
        addMarker(location)
        mGeofenceService!!.createGeofence(location)
        mPastLocations.add(location)
    }

    private fun saveLocation(location: PastLocation, onSuccessListener: OnSuccessListener<Void>) {
        val databaseReference = mFirebaseDatabase.getReference("pastLocations")
        val updateMap = HashMap<String, Any>()
        updateMap.put(location.hashCode().toString(), location)
        databaseReference.updateChildren(updateMap).addOnSuccessListener(onSuccessListener)
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationPlugin() {
        mLocationPlugin = LocationLayerPlugin(mMapView!!, mMap!!, mLocationService!!.getLocationEngine())
        mLocationPlugin!!.setLocationLayerEnabled(LocationLayerMode.TRACKING)
    }

    private fun setCameraPosition(location: Location) {
        setCameraPosition(location.latitude, location.longitude)
    }

    private fun setCameraPosition(latitude: Double, longitude: Double) {
        mMap?.animateCamera(CameraUpdateFactory
                .newLatLngZoom(LatLng(latitude, longitude), 8.0))
    }

    private fun cameraIncludeAll(padding: Int, duration: Int) {
        if (mMap != null) {
            if (mPastLocations.size == 0) {
                setCameraPosition(mLastLocation!!)
            } else {
                LatLngBounds.Builder().apply {
                    this.include(LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude))
                    mPastLocations.forEach { position -> this.include(position) }
                    mMap!!.easeCamera(CameraUpdateFactory.newLatLngBounds(this.build(), padding), duration)
                }
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        setCameraPosition(location)
        mLastLocation = location
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
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
        mLocationService?.onDestroy()
        mMapView?.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mPermissionsManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            init()
        } else {
            this.activity.finish()
        }
    }

    interface OnMapFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }
}
