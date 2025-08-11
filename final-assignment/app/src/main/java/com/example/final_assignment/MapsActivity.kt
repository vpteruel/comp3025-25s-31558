package com.example.final_assignment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.final_assignment.databinding.ActivityMapsBinding
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding

    private lateinit var map: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private var pendingAction: (() -> Unit)? = null

    companion object {
        private val MARK_LOCATION = LatLng(43.769560, -79.275280)
        private const val DEFAULT_ZOOM = 15f
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                Log.d("Location", "Location permissions granted.")

                pendingAction?.invoke()
                pendingAction = null
                updateLocationUI()
            } else {
                Log.w("Location", "Location permissions denied by the user.")
                Toast.makeText(this, "Location permission denied. Cannot show current location.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragmentContainer) as SupportMapFragment

        mapFragment.getMapAsync(this)

        binding.markLocationButton.setOnClickListener {
            binding.mapFragmentContainer.visibility = View.VISIBLE
            if (::map.isInitialized) {
                showIlacDowntownCampus()
            } else {
                Toast.makeText(this, "Map not ready, please wait...", Toast.LENGTH_SHORT).show()
            }
        }

        binding.myLocationButton.setOnClickListener {
            binding.mapFragmentContainer.visibility = View.VISIBLE
            if (::map.isInitialized) {
                pendingAction = {
                    getDeviceLocationAndShowOnMap()
                }
                getPermissions()
            } else {
                Toast.makeText(this, "Map not ready, please wait...", Toast.LENGTH_SHORT).show()
            }
        }

        binding.connectMarkAndMyLocationButton.setOnClickListener {
            binding.mapFragmentContainer.visibility = View.VISIBLE
            if (::map.isInitialized) {
                pendingAction = {
                    connectIlacAndMyLocation()
                }
                getPermissions()
            } else {
                Toast.makeText(this, "Map not ready, please wait...", Toast.LENGTH_SHORT).show()
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.maps)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        Log.d("Map", "Google Map is ready.")

        updateLocationUI()
    }

    private fun getPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Location", "Permissions already granted.")

            pendingAction?.invoke()
            pendingAction = null
        } else {
            Log.d("Location", "Requesting location permissions.")
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun showIlacDowntownCampus() {
        map.clear()
        map.addMarker(MarkerOptions().position(MARK_LOCATION).title("ILAC Downtown Campus"))
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(MARK_LOCATION, DEFAULT_ZOOM))
    }

    private fun getDeviceLocationAndShowOnMap() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        lastKnownLocation = location
                        Log.d("Location", "Found last known location: ${lastKnownLocation?.latitude}, ${lastKnownLocation?.longitude}")
                        val currentLocationLatLng = LatLng(location.latitude, location.longitude)
                        map.clear()
                        map.addMarker(MarkerOptions().position(currentLocationLatLng).title("Your Current Location"))
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, DEFAULT_ZOOM))
                    } else {
                        Log.w("Location", "Last known location is null. Trying to get current location with getCurrentLocation.")
                        getCurrentLocationUpdatesAndShowOnMap()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Location", "Failed to get last known location: ${e.message}", e)
                    Toast.makeText(this, "Error getting location. Trying live update.", Toast.LENGTH_SHORT).show()
                    getCurrentLocationUpdatesAndShowOnMap()
                }
        } else {
            Toast.makeText(this, "Location permissions not granted.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentLocationUpdatesAndShowOnMap() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            val currentLocationRequest = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setDurationMillis(5000)
                .setMaxUpdateAgeMillis(0)
                .build()

            fusedLocationClient.getCurrentLocation(currentLocationRequest, null)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        lastKnownLocation = location
                        Log.d("Location", "Found current location from getCurrentLocation: ${lastKnownLocation?.latitude}, ${lastKnownLocation?.longitude}")
                        val currentLocationLatLng = LatLng(location.latitude, location.longitude)
                        map.clear()
                        map.addMarker(MarkerOptions().position(currentLocationLatLng).title("Your Current Location (Live)"))
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, DEFAULT_ZOOM))
                    } else {
                        Log.w("Location", "getCurrentLocation returned null.")
                        Toast.makeText(this, "Could not get current location.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Location", "Failed to get current location with getCurrentLocation: ${e.message}", e)
                    Toast.makeText(this, "Error getting current location.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun connectIlacAndMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        lastKnownLocation = location
                        val currentLocationLatLng = LatLng(location.latitude, location.longitude)
                        drawConnectionLine(currentLocationLatLng, MARK_LOCATION)
                    } else {
                        Log.w("Location", "Last known location is null for drawing line. Trying live update.")

                        val currentLocationRequest = CurrentLocationRequest.Builder()
                            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                            .setDurationMillis(5000)
                            .setMaxUpdateAgeMillis(0)
                            .build()

                        fusedLocationClient.getCurrentLocation(currentLocationRequest, null)
                            .addOnSuccessListener { liveLocation: Location? ->
                                if (liveLocation != null) {
                                    lastKnownLocation = liveLocation
                                    val liveCurrentLocationLatLng = LatLng(liveLocation.latitude, liveLocation.longitude)
                                    drawConnectionLine(liveCurrentLocationLatLng, MARK_LOCATION)
                                } else {
                                    Toast.makeText(this, "Could not get your current location to draw line.", Toast.LENGTH_LONG).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error getting live location for line: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Location", "Failed to get location for drawing line: ${e.message}", e)
                    Toast.makeText(this, "Error getting your location to draw line.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Location permissions not granted to draw line.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLocationUI() {
        if (::map.isInitialized) {
            try {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ) {
                    map.isMyLocationEnabled = true
                    map.uiSettings.isMyLocationButtonEnabled = true
                    map.uiSettings.isZoomControlsEnabled = true
                    map.uiSettings.isCompassEnabled = true
                    map.uiSettings.isIndoorLevelPickerEnabled = false
                    map.uiSettings.isMapToolbarEnabled = true
                } else {
                    map.isMyLocationEnabled = false
                    map.uiSettings.isMyLocationButtonEnabled = false
                    map.uiSettings.isZoomControlsEnabled = false
                    map.uiSettings.isCompassEnabled = false
                    map.uiSettings.isMapToolbarEnabled = false
                    lastKnownLocation = null
                }
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message, e)
            }
        }
    }

    private fun drawConnectionLine(start: LatLng, end: LatLng) {
        map.clear()

        map.addMarker(MarkerOptions().position(start).title("My Location"))
        map.addMarker(MarkerOptions().position(end).title("ILAC Downtown Campus"))

        val dash = Dash(20f)
        val gap = Gap(20f)
        val pattern = listOf(dash, gap)

        val dashedPolylineOptions = PolylineOptions()
            .add(start)
            .add(end)
            .width(7f)
            .color(Color.BLUE)
            .geodesic(true)
            .pattern(pattern)

        map.addPolyline(dashedPolylineOptions)

        val builder = LatLngBounds.Builder()
        builder.include(start)
        builder.include(end)
        val bounds = builder.build()
        val padding = 100
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)

        try {
            map.animateCamera(cameraUpdate)
        } catch (e: IllegalStateException) {
            Log.e("Map", "LatLngBounds are too small: ${e.message}")
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(start, DEFAULT_ZOOM))
        }
    }
}