package hu.hm.icguide.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import co.zsmb.rainbowcake.navigation.navigator
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.R
import hu.hm.icguide.databinding.FragmentMapsBinding
import hu.hm.icguide.extensions.toast
import hu.hm.icguide.models.Shop
import hu.hm.icguide.ui.add.AddDialog
import hu.hm.icguide.ui.detail.DetailFragment
import timber.log.Timber

@AndroidEntryPoint
class MapFragment(private val targetShop: Shop? = null) :
    RainbowCakeFragment<MapViewState, MapViewModel>(),
    ActivityCompat.OnRequestPermissionsResultCallback {

    override fun provideViewModel() = getViewModelFromFactory()
    override fun getViewResource() = R.layout.fragment_maps

    private lateinit var map: GoogleMap
    private var isPermissionGranted = false
    private lateinit var permReqLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMapsBinding

    override fun onStart() {
        super.onStart()
        viewModel.load()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMapsBinding.bind(view)
        setupToolbar()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            navigator?.pop()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permReqLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                toast(getString(R.string.permission_granted))
                isPermissionGranted = true
                enableMyLocation()

            } else {
                toast(getString(R.string.permission_denied))
            }
        }
        handleLocationPermission()
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        map.isMyLocationEnabled = isPermissionGranted
    }

    override fun render(viewState: MapViewState) {
        if (!::map.isInitialized) return
        Timber.d("Received ${viewState.markers.size} markers to display in map")
        viewState.markers.forEach {
            val marker = map.addMarker(
                MarkerOptions().position(LatLng(it.geoPoint.latitude, it.geoPoint.longitude))
                    .title(it.name)
            )
            marker?.tag = it.id
            if(it.id == targetShop?.id){
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            targetShop.geoPoint.latitude,
                            targetShop.geoPoint.longitude
                        ), 15F
                    )
                )
                marker?.showInfoWindow()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->

        map = googleMap
        googleMap.uiSettings.setAllGesturesEnabled(true)

        googleMap.isMyLocationEnabled = isPermissionGranted
        val sp = PreferenceManager.getDefaultSharedPreferences(requireActivity().applicationContext)
        val darkMode = sp.getBoolean("darkTheme", false)
        if (darkMode) googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.mapstyle_dark
            )
        )

        val budapest = LatLng(47.4979, 19.0402)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(budapest, 10F))
        googleMap.setOnMapLongClickListener {
            Timber.d("Long clicked on map at $it, open addDialog")
            AddDialog(it).show(childFragmentManager, null)
        }
        googleMap.setOnInfoWindowClickListener {
            Timber.d("Clicked on ${it.id} marker's info window")
            it.tag ?: return@setOnInfoWindowClickListener
            Timber.d("Navigate to DetailFragment id: ${it.tag}")
            navigator?.add(DetailFragment(it.tag!! as String))
        }
        googleMap.setOnMarkerClickListener {
            var distance: String
            distance = if(!isPermissionGranted){
                getString(R.string.distance_no_permission)
            } else " ${getString(R.string.distance)} ${getDistance(it.position)} km"
            it.snippet = distance
            false
        }
    }

    private fun showRationaleDialog(
        @StringRes title: Int = R.string.rationale_dialog_title,
        @StringRes explanation: Int,
        onPositiveButton: () -> Unit,
        onNegativeButton: () -> Boolean? = { navigator?.pop() }
    ) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(explanation)
            .setCancelable(false)
            .setPositiveButton(R.string.proceed) { dialog, _ ->
                dialog.cancel()
                onPositiveButton()
            }
            .setNegativeButton(R.string.exit) { _, _ -> onNegativeButton() }
            .create()
        alertDialog.show()
    }

    private fun handleLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                showRationaleDialog(
                    explanation = R.string.location_permission_explanation,
                    onPositiveButton = this::requestLocationPermission
                )
            } else {
                requestLocationPermission()
            }
        } else {
            isPermissionGranted = true
        }
    }

    private fun requestLocationPermission() {
        permReqLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    private fun getDistance(position: LatLng): Float {
        if(!isPermissionGranted) return 0F
        val location = Location(LocationManager.GPS_PROVIDER)
        location.latitude = position.latitude
        location.longitude = position.longitude
        return location.distanceTo(map.myLocation) / 1000
    }

}
