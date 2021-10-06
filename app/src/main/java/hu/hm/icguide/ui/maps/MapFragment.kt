package hu.hm.icguide.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import co.zsmb.rainbowcake.navigation.navigator
import com.example.icguide.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.ui.add.AddFragment

@AndroidEntryPoint
class MapFragment : RainbowCakeFragment<MapViewState, MapViewModel>(),
    ActivityCompat.OnRequestPermissionsResultCallback {

    override fun provideViewModel() = getViewModelFromFactory()
    override fun getViewResource() = R.layout.fragment_maps

    private lateinit var map: GoogleMap
    private var isPermissionGranted = false
    private lateinit var permReqLauncher: ActivityResultLauncher<String>


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        // TODO Setup views
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permReqLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                Toast.makeText(context, "Engedély megadva", Toast.LENGTH_SHORT).show()
                isPermissionGranted = true
                enableMyLocation()

            } else {
                Toast.makeText(context, "Engedély megtagadva", Toast.LENGTH_SHORT).show()
            }
        }
        handleLocationPermission()

    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        map.isMyLocationEnabled = isPermissionGranted
    }

    override fun onStart() {
        super.onStart()

        viewModel.load()
    }

    override fun render(viewState: MapViewState) {
        // TODO Render state
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->

        map = googleMap

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isTiltGesturesEnabled = true
        googleMap.uiSettings.isRotateGesturesEnabled = true
        googleMap.uiSettings.isScrollGesturesEnabled = true


        googleMap.setOnMyLocationButtonClickListener {
            Toast.makeText(context, "buttonClick", Toast.LENGTH_SHORT).show()
            false
        }

        /**
         * If you want to do sth when user clicks on his exact location (blue dot)*
         * googleMap.setOnMyLocationClickListener {}
         **/

        googleMap.isMyLocationEnabled = isPermissionGranted

        googleMap.setOnInfoWindowLongClickListener {
            navigator?.replace(AddFragment(it.position))
        }

        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        googleMap.setOnMapClickListener {
            googleMap.addMarker(MarkerOptions().position(it).title("Fagylaltozó felvétele ide"))
        }

        //TODO every shop a marker
        //TODO current position, click --> new marker --> Add new shop
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
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                showRationaleDialog(
                    explanation = R.string.location_permission_explanation,
                    onPositiveButton = this::requestLocationPermission
                )

            } else {
                // No explanation needed, we can request the permission.
                requestLocationPermission()
            }
        } else {
            isPermissionGranted = true
        }
    }

    private fun requestLocationPermission() {
        permReqLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }


}
