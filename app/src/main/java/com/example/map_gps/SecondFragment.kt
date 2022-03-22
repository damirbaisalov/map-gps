package com.example.map_gps

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PointF
import android.location.LocationManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.ui_view.ViewProvider

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SecondFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SecondFragment : Fragment(), UserLocationObjectListener, CameraListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mapView: MapView
    private lateinit var rootView: View
    private lateinit var pLauncher: ActivityResultLauncher<String>

    private var permissionLocation = false
    private var followUserLocation = false
    private lateinit var userLocationLayer: UserLocationLayer
    private var routeStartLocation = Point(52.27401,77.00438)

    private lateinit var userLocationImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        rootView = inflater.inflate(R.layout.fragment_second, container, false)

        MapKitFactory.initialize(rootView.context)

        mapView = rootView.findViewById(R.id.mapview)
        mapView.map.move(
            CameraPosition(routeStartLocation, 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 0f),
            null
        )

        registerPermissionListener()
        checkPermission()
        userInterface()

        return rootView
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                rootView.context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ->{
                Toast.makeText(rootView.context, "ACCESS_FINE_LOCATION run", Toast.LENGTH_LONG).show()
                onMapReady()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(rootView.context, "we need your permission", Toast.LENGTH_LONG).show()
            }

            else -> {
                pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

            }
        }

    }

    private fun registerPermissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()){
            if (it){
                onMapReady()
            } else {
                Toast.makeText(rootView.context, "Permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun userInterface() {
        userLocationImageView = rootView.findViewById(R.id.user_location_map_image_view)
        userLocationImageView.setOnClickListener {
            if (permissionLocation) {

                cameraUserPosition()
                followUserLocation = true

            } else {
                checkPermission()
            }
        }

    }

    private fun onMapReady(){
        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true
        userLocationLayer.setObjectListener(this)

        mapView.map.addCameraListener(this)

        cameraUserPosition()

        permissionLocation = true
    }

    private fun cameraUserPosition() {
        if (userLocationLayer.cameraPosition() != null){
            routeStartLocation = userLocationLayer.cameraPosition()!!.target
            mapView.map.move(
                CameraPosition(routeStartLocation,16f,0f,0f),
                Animation(Animation.Type.SMOOTH,1F),
                null
            )
        } else {
            mapView.map.move(CameraPosition(routeStartLocation,11f, 0f,0f))
        }
    }

    private fun setAnchor() {
        userLocationLayer.setAnchor(
            PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.5).toFloat()),
            PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.83).toFloat())
        )

        followUserLocation = false
    }

    private fun noAnchor() {
        userLocationLayer.resetAnchor()

    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onObjectAdded(p0: UserLocationView) {
        setAnchor()

        val view = View(rootView.context).apply {
            background = rootView.context.getDrawable(R.drawable.ic_user_location)
        }

        p0.pin.setView(ViewProvider(view))
        p0.arrow.setView(ViewProvider(view))
//        p0.pin.setIcon(fromResource(this,R.drawable.marker_logo))
//        p0.arrow.setIcon(fromResource(this,R.drawable.marker_logo))
    }

    override fun onObjectRemoved(p0: UserLocationView) {

    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {

    }

    override fun onCameraPositionChanged(
        p0: Map,
        p1: CameraPosition,
        p2: CameraUpdateReason,
        p3: Boolean
    ) {
        if (p3){
            if (followUserLocation){
                setAnchor()
            }
        } else {
            if (!followUserLocation) {
                noAnchor()
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SecondFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SecondFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}