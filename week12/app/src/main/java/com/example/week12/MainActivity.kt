package com.example.week12

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var latLng: LatLng
    private lateinit var markerOptions: MarkerOptions
    private lateinit var cameraUpdate: CameraUpdateFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        findViewById<Button>(R.id.btnOpenMaps).setOnClickListener {
            findViewById<View>(R.id.map).visibility = View.VISIBLE
            mapFragment.getMapAsync(this)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val torontoCnTower = LatLng(43.6532, -79.3832)
        markerOptions = MarkerOptions().position(torontoCnTower).title("Toronto CN Tower")
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(torontoCnTower, 15f))
        map.addMarker(markerOptions)
        // 20f building itself
        // 15f building, street
        // 10f city
        // 5f country
        // 1f world
    }
}