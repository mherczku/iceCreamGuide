package hu.hm.icguide.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import co.zsmb.rainbowcake.navigation.navigator
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.R
import hu.hm.icguide.databinding.FragmentMapsBinding
import hu.hm.icguide.extensions.toast
import hu.hm.icguide.ui.add.AddDialog
import hu.hm.icguide.ui.detail.DetailFragment

@AndroidEntryPoint
class MapFragment : RainbowCakeFragment<MapViewState, MapViewModel>(),
    ActivityCompat.OnRequestPermissionsResultCallback, OnSuccessListener<QuerySnapshot> {

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

    override fun onStart() {
        super.onStart()
        viewModel.load()
    }

    override fun render(viewState: MapViewState) {
        if (!::map.isInitialized) return
        viewState.markers.forEach {
            val marker = map.addMarker(
                MarkerOptions().position(LatLng(it.geoPoint.latitude, it.geoPoint.longitude))
                    .title(it.name)
            )
            marker?.tag = it.id
        }
        // TODO Render state
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->

        map = googleMap
        googleMap.uiSettings.setAllGesturesEnabled(true)

        googleMap.isMyLocationEnabled = isPermissionGranted

        val budapest = LatLng(47.4979, 19.0402)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(budapest, 10F))
        googleMap.setOnMapLongClickListener {
            AddDialog(it).show(childFragmentManager, null)
        }
        googleMap.setOnInfoWindowLongClickListener {
            it.tag ?: return@setOnInfoWindowLongClickListener
            navigator?.add(DetailFragment(it.tag!! as String))
        }
        viewModel.getData(this)
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

    override fun onSuccess(p0: QuerySnapshot?) {
        p0 ?: return
        viewModel.getMarkers(p0)
    }

}
