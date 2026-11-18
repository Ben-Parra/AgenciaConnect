package cl.duoc.agenciaconnect.ui.eventos

// ────────────────────────────────────────────────────────────────────────────
// IMPORTS GENERALES
// ────────────────────────────────────────────────────────────────────────────
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build

// ────────────────────────────────────────────────────────────────────────────
// IMPORTS DE ANIMACIONES Y COMPOSABLES
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.agenciaconnect.data.model.Evento

// ────────────────────────────────────────────────────────────────────────────
// FUNCIÓN AUXILIAR PARA CARGAR IMAGEN EN LA TARJETA
// ────────────────────────────────────────────────────────────────────────────

private fun cargarBitmapEvento(context: android.content.Context, uriTexto: String): Bitmap? {
    if (uriTexto.isBlank()) return null
    return try {
        if (uriTexto.all { it.isDigit() }) {
            val resId = uriTexto.toInt()
            android.graphics.BitmapFactory.decodeResource(context.resources, resId)
        } else if (uriTexto.startsWith("android.resource://")) {
            val partes = uriTexto.split("/")
            val nombreDrawable = partes.last()
            val resId = context.resources.getIdentifier(nombreDrawable, "drawable", context.packageName)
            if (resId != 0) {
                android.graphics.BitmapFactory.decodeResource(context.resources, resId)
            } else {
                null
            }
        } else {
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = uiState.eventos,
                    key = { evento -> evento.id }
                ) { evento ->
                    TarjetaEvento(
                        evento = evento,
                        modifier = Modifier.animateItem(
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
// COMPOSABLE DE TARJETA INDIVIDUAL REDISEÑADA
// ────────────────────────────────────────────────────────────────────────────

@Composable
private fun TarjetaEvento(
    evento: Evento,
    modifier: Modifier = Modifier,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val context = LocalContext.current

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
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📷\nSin imagen",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // ── LADO DERECHO: datos y acciones del evento ────────────
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Encabezado con título principal
                    Text(
                        text = evento.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Píldora de Organización asociada
                    if (evento.organizacion.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "🏢 ${evento.organizacion}",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Datos compactos
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🗓 ", fontSize = 13.sp)
                        Text("Fecha: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        Text(fechaVisible, style = MaterialTheme.typography.bodySmall)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📌 ", fontSize = 13.sp)
                        Text("Ubicación: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = evento.ubicacion,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Bloque sutil de descripción
                    if (evento.descripcion.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = evento.descripcion,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }

                    // Botones de acción limpios
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onEditar,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("✏ Editar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        TextButton(
                            onClick = onEliminar,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🗑 Eliminar",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
