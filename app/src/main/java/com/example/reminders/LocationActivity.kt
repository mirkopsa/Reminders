package com.example.reminders

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.material.Text
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locSharedPref = getSharedPreferences("virtualLocation", MODE_PRIVATE)
        val locEditor = locSharedPref.edit()

        val virtualLat = Double.fromBits(locSharedPref.getLong("lat", 0))
        val virtualLng = Double.fromBits(locSharedPref.getLong("lng", 0))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if (location == null)
                    println("location null")
                else {
                    var lat = location.latitude
                    var lng = location.longitude
                    setContent {

                        Column {
                            TopAppBar(
                                title = { Text(text = "Location") },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        val intent = Intent(this@LocationActivity, MainActivity::class.java)
                                        startActivity(intent)
                                    }) {
                                        Icon(Icons.Filled.ArrowBack, "")
                                    }
                                }
                            )
                            Column {
                                var markerPos by remember { mutableStateOf(LatLng(lat, lng)) }
                                val cameraPositionState = rememberCameraPositionState {
                                    position = CameraPosition.fromLatLngZoom(markerPos, 10f)
                                }
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState,
                                    properties = MapProperties(isMyLocationEnabled = true),
                                    onMapClick = {
                                        markerPos = it
                                        println("latlong")
                                        println(markerPos)
                                        locEditor.apply {
                                            putLong("lat", markerPos.latitude.toRawBits())
                                            putLong("lng", markerPos.longitude.toRawBits())
                                            apply()
                                        }
                                    }
                                ) {
                                    Marker(
                                        state = MarkerState(position = markerPos),
                                        draggable = false,
                                        title = "Reminders in this area"
                                    )
                                }
                            }
                        }
                    }
                }
            }
    }

}