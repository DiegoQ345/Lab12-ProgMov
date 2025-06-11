package com.quispe.lab12

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import kotlinx.coroutines.launch

@Composable
fun rememberSmallMountainIcon(): BitmapDescriptor {
    val context = LocalContext.current
    return remember {
        val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable._1mountain)
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 80, 80, false)
        BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }
}


@Composable
fun MapScreen() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState()

    var expanded by remember { mutableStateOf(false) }
    var selectedMapType by remember { mutableStateOf(MapType.NORMAL) }

    val mapTypes = listOf(
        "Normal" to MapType.NORMAL,
        "Satélite" to MapType.SATELLITE,
        "Terreno" to MapType.TERRAIN,
        "Híbrido" to MapType.HYBRID
    )

    // Necesitamos un CoroutineScope para llamar a funciones suspendidas en eventos de UI (como onClick)
    val coroutineScope = rememberCoroutineScope() // <--- ¡Añade esta línea!

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    userLocation = latLng
                    // Usamos coroutineScope.launch para la animación inicial si es necesario aquí
                    // Aunque el LaunchedEffect ya maneja el primer movimiento, es buena práctica.
                    coroutineScope.launch {
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    }
                }
            }
        } else {
            // Permiso denegado. Puedes mostrar un mensaje al usuario aquí.
        }
    }

    // Solicitar ubicación cuando el Composable se lanza por primera vez
    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        userLocation = latLng
                        // La animación inicial se maneja aquí directamente
                        coroutineScope.launch { // <--- ¡Importante!
                            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        }
                    }
                }
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { expanded = true }) {
                Text("Tipo de Mapa: ${mapTypes.first { it.second == selectedMapType }.first}")
            }

            Button(
                onClick = {
                    userLocation?.let {
                        // ¡Aquí es donde usaremos coroutineScope.launch!
                        coroutineScope.launch { // <--- ¡Esta es la clave para que funcione el botón!
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newLatLngZoom(it, 17f),
                                durationMs = 1500
                            )
                        }
                    }
                }
            ) {
                Text("Ir a mi ubicación")
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(mapType = selectedMapType)
            ) {
                userLocation?.let {
                    Marker(
                        state = rememberMarkerState(position = it),
                        title = "Tu ubicación"
                    )
                }
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                mapTypes.forEach { (label, type) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            selectedMapType = type
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
