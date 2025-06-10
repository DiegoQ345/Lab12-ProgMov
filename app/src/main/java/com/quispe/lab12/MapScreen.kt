package com.quispe.lab12

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker

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
    val ArequipaLocation = LatLng(-16.4040102, -71.559611) // Arequipa, Perú
    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(ArequipaLocation, 12f)
    }


    Box(modifier = Modifier.fillMaxSize()) {
        // Añadir GoogleMap al layout
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            val mountainIcon = rememberSmallMountainIcon()
            // Añadir marcador en Arequipa Perú
            Marker(
                state = rememberMarkerState(position = ArequipaLocation),
                icon = mountainIcon,
                title = "Arequipa, Perú"
            )

        }
    }
}
