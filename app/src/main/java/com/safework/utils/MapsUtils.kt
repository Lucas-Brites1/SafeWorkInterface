package com.safework.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

object MapsUtils {
    const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    // Função para configurar o Google Map
    fun configureMap(googleMap: GoogleMap) {
        // Habilitar o zoom
        googleMap.uiSettings.isZoomControlsEnabled = true
        // Habilitar o movimento da câmera
        googleMap.uiSettings.isScrollGesturesEnabled = true
    }

    // Função para adicionar um marcador no mapa
    fun addMarker(googleMap: GoogleMap, latLng: LatLng, title: String) {
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(title)
        googleMap.addMarker(markerOptions)
    }

    fun handleMapReady(
        googleMap: GoogleMap,
        context: Context,
        onLocationReady: (LatLng) -> Unit,
        onError: () -> Unit
    ) {
        configureMap(googleMap)
        setMapType(googleMap, GoogleMap.MAP_TYPE_NORMAL)

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true

            getUserLocation(
                context = context,
                onLocationFound = onLocationReady,
                onError = onError
            )
        } else {
            ActivityCompat.requestPermissions(
                context as AppCompatActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun getUserLocation(
        context: Context,
        onLocationFound: (LatLng) -> Unit,
        onError: (() -> Unit)? = null
    ) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        onLocationFound(userLatLng)
                    } else {
                        onError?.invoke()
                    }
                }
                .addOnFailureListener {
                    onError?.invoke()
                }
        } catch (e: SecurityException) {
            onError?.invoke()
        }
    }

    // Função para mover a câmera para uma posição no mapa
    fun moveCamera(googleMap: GoogleMap, latLng: LatLng, zoomLevel: Float = 15f) {
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel)
        googleMap.animateCamera(cameraUpdate)
    }

    // Função para desenhar uma rota (Polyline)
    fun drawRoute(googleMap: GoogleMap, points: List<LatLng>) {
        val polylineOptions = PolylineOptions()
            .addAll(points)
            .width(5f)
            .color(android.graphics.Color.BLUE) // Cor da linha da rota
        googleMap.addPolyline(polylineOptions)
    }

    // Função para habilitar a localização do usuário
    fun enableUserLocation(googleMap: GoogleMap, context: Context) {
        if (googleMap.isMyLocationEnabled) {
            googleMap.isMyLocationEnabled = true
        } else {
            // Solicitar permissão se necessário
        }
    }

    // Função para limpar todos os marcadores ou polylines
    fun clearMap(googleMap: GoogleMap) {
        googleMap.clear() // Isso vai remover todos os marcadores e polylines
    }

    // Função para configurar o tipo de mapa (Exemplo: Normal, Satélite, etc.)
    fun setMapType(googleMap: GoogleMap, type: Int) {
        googleMap.mapType = type
    }
}
