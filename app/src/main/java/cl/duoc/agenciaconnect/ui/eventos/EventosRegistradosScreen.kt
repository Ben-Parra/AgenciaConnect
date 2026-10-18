package cl.duoc.agenciaconnect.ui.eventos

// ────────────────────────────────────────────────────────────────────────────
// IMPORTS GENERALES
// ────────────────────────────────────────────────────────────────────────────
import android.net.Uri
import android.os.Build

// ────────────────────────────────────────────────────────────────────────────
// IMPORTS DE ANIMACIONES
// ────────────────────────────────────────────────────────────────────────────
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.agenciaconnect.data.model.Evento
import android.graphics.Bitmap
import android.graphics.ImageDecoder

// ────────────────────────────────────────────────────────────────────────────
// FUNCIÓN AUXILIAR PARA CARGAR IMAGEN EN LA TARJETA
// ────────────────────────────────────────────────────────────────────────────

// Con esta función adaptada convertimos la URI del evento en un Bitmap,
// soportando tanto fotos de galería/cámara como recursos locales de drawable.
private fun cargarBitmapEvento(context: android.content.Context, uriTexto: String): Bitmap? {
    if (uriTexto.isBlank()) return null
    return try {
        // Si la URI empieza con número (guardamos el ID del drawable directamente)
        if (uriTexto.all { it.isDigit() }) {
            val resId = uriTexto.toInt()
            android.graphics.BitmapFactory.decodeResource(context.resources, resId)
        } else if (uriTexto.startsWith("android.resource://")) {
            // Si viene con formato resource, extraemos el nombre o ID
            val partes = uriTexto.split("/")
            val nombreDrawable = partes.last()
            val resId = context.resources.getIdentifier(nombreDrawable, "drawable", context.packageName)
            if (resId != 0) {
                android.graphics.BitmapFactory.decodeResource(context.resources, resId)
            } else {
                null
            }
        } else {
            // Carga normal para URI de galería o cámara (ContentResolver / ImageDecoder)
            val uri = Uri.parse(uriTexto)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val fuente = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(fuente)
            } else {
                @Suppress("DEPRECATION")
                android.graphics.BitmapFactory.decodeStream(
                    context.contentResolver.openInputStream(uri)
                )
            }
        }
    } catch (e: Exception) {
        null
    }
}

// ────────────────────────────────────────────────────────────────────────────
// PANTALLA DE EVENTOS REGISTRADOS
// ────────────────────────────────────────────────────────────────────────────

// Con esta pantalla mostramos la lista de eventos guardados en Room.
// Cada evento se presenta en una tarjeta con su imagen a la izquierda.
// Aplicamos AnimatedVisibility para que cada tarjeta entre con
// animación al aparecer en la lista por primera vez.
@Composable
fun EventosRegistradosScreen(
    onVolverFormulario: () -> Unit,
    onVolver: () -> Unit,
    onEditarEvento: () -> Unit,
    viewModel: EventoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Eventos registrados",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Total de eventos: ${uiState.eventos.size}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.eventos.isEmpty()) {
            Text("Aún no hay eventos registrados.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.eventos,
                    key = { evento -> evento.id }
                ) { evento ->
                    TarjetaEvento(
                        evento = evento,
                        modifier = Modifier.animateItem(
                            // Con null delegamos la entrada/salida al AnimatedVisibility interno
                            // y dejamos que animateItem solo anime el reordenamiento (placement).
                            fadeInSpec = null,
                            fadeOutSpec = null,
                            placementSpec = tween(durationMillis = 300)
                        ),
                        onEditar = {
                            viewModel.cargarEventoParaEditar(evento)
                            onEditarEvento()
                        },
                        onEliminar = {
                            viewModel.solicitarEliminar(evento)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onVolverFormulario,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al formulario")
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al panel")
        }
    }

    // ── Diálogo de confirmación de eliminación ───────────────────────────
    uiState.eventoPorEliminar?.let { evento ->
        AlertDialog(
            onDismissRequest = {
                viewModel.cancelarEliminar()
            },
            title = {
                Text("Eliminar evento")
            },
            text = {
                Text("¿Deseas eliminar el evento \"${evento.nombre}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmarEliminar()
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelarEliminar()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ────────────────────────────────────────────────────────────────────────────
// COMPOSABLE DE TARJETA INDIVIDUAL
// ────────────────────────────────────────────────────────────────────────────

// Con esta función presentamos la información de un evento dentro de una
// tarjeta Card de Material 3 con la imagen a la izquierda.
// Aplicamos AnimatedVisibility para que la tarjeta entre con animación
// cuando aparece en la lista por primera vez.
@Composable
private fun TarjetaEvento(
    evento: Evento,
    modifier: Modifier = Modifier,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val context = LocalContext.current

    // ────────────────────────────────────────────────────────────────────
    // ANIMACIÓN: AnimatedVisibility en la entrada de la tarjeta
    // ────────────────────────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(evento.id) {
        visible = true
    }

    val bitmap = remember(evento.imagenUri) {
        cargarBitmapEvento(context, evento.imagenUri)
    }

    val fechaVisible = if (evento.fecha.length == 8) {
        "${evento.fecha.substring(0, 2)}/" +
                "${evento.fecha.substring(2, 4)}/" +
                evento.fecha.substring(4, 8)
    } else {
        evento.fecha
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(durationMillis = 400)) +
                expandVertically(animationSpec = tween(durationMillis = 400))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // ── LADO IZQUIERDO: imagen del evento ────────────────────
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Imagen del evento ${evento.nombre}",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sin\nimagen",
                            color = Color.DarkGray,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // ── LADO DERECHO: datos y acciones del evento ────────────
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = evento.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text("Descripción: ${evento.descripcion}")
                    Text("Fecha: $fechaVisible")
                    Text("Ubicación: ${evento.ubicacion}")
                    Text("Organización: ${evento.organizacion}")

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onEditar,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Editar")
                        }

                        Button(
                            onClick = onEliminar,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Eliminar")
                        }
                    }
                }
            }
        }
    }
}
