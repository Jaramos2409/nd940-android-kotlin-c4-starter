package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Resources
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import androidx.core.content.getSystemService
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.LocationReminderLocationFeatureDirectionsAlertDialog
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.base.SaveBaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.PermissionUtils
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

class SelectLocationFragment : SaveBaseFragment(), OnMapReadyCallback {

    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var supportMapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private lateinit var locationReminderLocationFeatureDirectionsAlertDialog: LocationReminderLocationFeatureDirectionsAlertDialog

    override fun onStart() {
        super.onStart()
        if (!checkIfAllPermissionsApproved()) {
            requestForegroundLocationAndNotificationPermissions()
        }
        checkLocationSettings()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        locationReminderLocationFeatureDirectionsAlertDialog =
            LocationReminderLocationFeatureDirectionsAlertDialog(requireContext())

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.map_options, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.normal_map -> {
                        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                        true
                    }
                    R.id.hybrid_map -> {
                        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                        true
                    }
                    R.id.satellite_map -> {
                        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                        true
                    }
                    R.id.terrain_map -> {
                        googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setDisplayHomeAsUpEnabled(true)

        binding.locationReminderSaveButton.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        supportMapFragment =
            childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
        supportMapFragment.onCreate(savedInstanceState)
    }

    private fun onLocationSelected() {
        _viewModel.navigationCommand.value =
            NavigationCommand.To(SelectLocationFragmentDirections.actionSelectLocationFragmentToSaveReminderFragment())
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap.setContentDescription(getString(R.string.map_has_loaded))
        setMapStyle(this.googleMap)
        setPoiClick(this.googleMap)
        setMapLongClick(this.googleMap)

        _viewModel.getSelectedPoi()?.let {
            val poi = _viewModel.getSelectedPoi()

            poi?.let {
                this.googleMap.addMarker(
                    MarkerOptions()
                        .position(poi.latLng)
                        .title(poi.name)
                )?.let { newMarker ->
                    {
                        newMarker.showInfoWindow()
                        _viewModel.setReminderLocationData(
                            newMarker,
                            poi,
                            poi.name,
                            poi.latLng.latitude,
                            poi.latLng.longitude
                        )
                    }
                }
            }
        }

        setLocationOnMap()

        locationReminderLocationFeatureDirectionsAlertDialog.showIfNecessary()
    }

    @SuppressLint("MissingPermission")
    private fun setLocationOnMap() = ifLocationPermissionsAreGranted {
        this.googleMap.isMyLocationEnabled = true

        val locationManager = requireContext().getSystemService<LocationManager>()
        val location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        location?.let {
            this.googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        location.latitude,
                        location.longitude
                    ), 15f
                )
            )
        }
    }

    private fun ifLocationPermissionsAreGranted(function: () -> Unit) {
        if (PermissionUtils.hasPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            && PermissionUtils.hasPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            function()
        }
    }

    override fun onResume() {
        super.onResume()
        supportMapFragment.onResume()
    }

    override fun onPause() {
        super.onPause()
        supportMapFragment.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        supportMapFragment.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        supportMapFragment.onLowMemory()
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Timber.e("Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Timber.e(e, "Can't find style. Error: ")
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val newLocationReminderMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )

            if (newLocationReminderMarker != null) {
                _viewModel.getSelectedMarker()?.remove()
                _viewModel.setReminderLocationData(
                    newLocationReminderMarker,
                    poi,
                    poi.name,
                    poi.latLng.latitude,
                    poi.latLng.longitude
                )
                _viewModel.getSelectedMarker()?.showInfoWindow()
            }
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val locationTitle = "${truncateCoordinate(latLng.latitude)}, ${
                truncateCoordinate(
                    latLng.longitude
                )
            }"

            val newLocationReminderMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(locationTitle)
            )

            if (newLocationReminderMarker != null) {
                _viewModel.getSelectedMarker()?.remove()
                _viewModel.setReminderLocationData(
                    selectedMarker = newLocationReminderMarker,
                    selectedLocationName = locationTitle,
                    selectedLatitude = latLng.latitude,
                    selectedLongitude = latLng.longitude
                )
                _viewModel.getSelectedMarker()?.showInfoWindow()
            }
        }
    }

    private fun truncateCoordinate(value: Double): Double {
        val factor = 10.0.pow(4.0)
        return (value.times(factor)).roundToInt() / factor
    }

    override fun onLocationSettingsEnabled() {
        super.onLocationSettingsEnabled()
        setLocationOnMap()
    }

    override fun onPermissionGranted() {
        super.onPermissionGranted()
        setLocationOnMap()
    }

}
