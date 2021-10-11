package hu.hm.icguide.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import co.zsmb.rainbowcake.navigation.navigator
import com.example.icguide.R
import com.example.icguide.databinding.FragmentMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.ui.add.AddFragment
import hu.hm.icguide.ui.login.LoginFragment

@AndroidEntryPoint
class MapFragment : RainbowCakeFragment<MapViewState, MapViewModel>(),
    ActivityCompat.OnRequestPermissionsResultCallback {

    override fun provideViewModel() = getViewModelFromFactory()
    override fun getViewResource() = R.layout.fragment_maps

    private lateinit var map: GoogleMap
    private var isPermissionGranted = false
    private lateinit var permReqLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMapsBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMapsBinding.bind(view)
        setupToolbar()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        // TODO Setup views
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            navigator?.pop()
        }
        binding.toolbar.inflateMenu(R.menu.menu_list)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permReqLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                Toast.makeText(context, getString(R.string.permission_granted), Toast.LENGTH_SHORT)
                    .show()
                isPermissionGranted = true
                enableMyLocation()

            } else {
                Toast.makeText(context, getString(R.string.permission_denied), Toast.LENGTH_SHORT)
                    .show()
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
        googleMap.uiSettings.setAllGesturesEnabled(true)

        googleMap.isMyLocationEnabled = isPermissionGranted

        googleMap.setOnInfoWindowLongClickListener {
            navigator?.replace(AddFragment(it.position))
        }

        val budapest = LatLng(47.4979, 19.0402)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(budapest, 10F))
        googleMap.setOnMapLongClickListener {
            googleMap.addMarker(
                MarkerOptions().position(it).title(getString(R.string.add_new_shop_here))
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(it))
        }
        googleMap.setOnPoiClickListener {
            Toast.makeText(context, " ${it.name}", Toast.LENGTH_SHORT).show()
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

}
