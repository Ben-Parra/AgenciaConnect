package cl.duoc.agenciaconnect.ui.eventos

// ────────────────────────────────────────────────────────────────────────────
// IMPORTS GENERALES
// ────────────────────────────────────────────────────────────────────────────
import android.net.Uri
import android.os.Build

// Imports para el selector de imágenes del sistema (Photo Picker).
// ActivityResultContracts.PickVisualMedia is the recommended mechanism
// by Android to select images without requesting gallery permissions.
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest

// ────────────────────────────────────────────────────────────────────────────
// IMPORTS DE ANIMACIONES
// ────────────────────────────────────────────────────────────────────────────
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.compose.runtime.saveable.rememberSaveable
import cl.duoc.agenciaconnect.util.crearUriFotografia

// ────────────────────────────────────────────────────────────────────────────
// TRANSFORMACIÓN VISUAL DE FECHA
// ────────────────────────────────────────────────────────────────────────────

// Con esta clase mostramos las barras separadoras de la fecha (DD/MM/AAAA)
// en el campo de texto, sin modificar el valor real que se almacena.
private class FechaVisualTransformation : VisualTransformation {

    // Con esta función presentamos la fecha con el formato DD/MM/AAAA.
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text

        val formateado = buildString {
            original.forEachIndexed { indice, caracter ->
                if (indice == 2 || indice == 4) append("/")
                append(caracter)
            }
        }

        val desplazamiento = object : OffsetMapping {

            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 4 -> offset + 1
                    else -> offset + 2
                }.coerceAtMost(formateado.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    else -> offset - 2
                }.coerceIn(0, original.length)
            }
        }

        return TransformedText(
            AnnotatedString(formateado),
            desplazamiento
        )
    }
}

// ────────────────────────────────────────────────────────────────────────────
// FUNCIÓN AUXILIAR PARA CARGAR IMAGEN
// ────────────────────────────────────────────────────────────────────────────

// Con esta función convertimos la URI de una imagen en un Bitmap
// que Compose puede mostrar directamente en pantalla.
private fun cargarBitmapDesdeUri(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val fuente = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(fuente)
        } else {
            @Suppress("DEPRECATION")
            android.graphics.BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(uri)
            )
        }
    } catch (e: Exception) {
        null
    }
}

// ────────────────────────────────────────────────────────────────────────────
// PANTALLA PRINCIPAL: FORMULARIO DE EVENTOS
// ────────────────────────────────────────────────────────────────────────────

// Con esta pantalla presentamos el formulario para registrar o editar eventos.
// Aquí aplicamos las tres animaciones:
//   1. AnimatedVisibility → para mostrar u ocultar la vista previa de la imagen.
//   2. animateColorAsState → para animar el color del botón de selección.
//   3. Crossfade → para transicionar el texto de estado debajo del título.
@Composable
fun EventosScreen(
    onVolver: () -> Unit,
    onVerEventos: () -> Unit,
    viewModel: EventoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Con este efecto reiniciamos el formulario cuando abandonamos la pantalla.
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.reiniciarFormularioAlSalir()
        }
    }

    val context = LocalContext.current

    // Con esta variable determinamos si ya hay una imagen seleccionada.
    val hayImagen = uiState.imagenUri.isNotBlank()

    // ────────────────────────────────────────────────────────────────────
    // ANIMACIÓN 2: animateColorAsState
    // ────────────────────────────────────────────────────────────────────
    // Con animateColorAsState animamos el color del botón de imagen.
    val colorBotonImagen by animateColorAsState(
        targetValue = if (hayImagen) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondary
        },
        animationSpec = tween(durationMillis = 600),
        label = "Color del botón de imagen"
    )

    // ────────────────────────────────────────────────────────────────────
    // SELECTOR DE IMÁGENES (Photo Picker)
    // ────────────────────────────────────────────────────────────────────
    val selectorImagenes = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Algunos sistemas no admiten permisos persistentes sobre
                // todas las URIs. Capturamos el error sin interrumpir el flujo.
            }
            viewModel.cambiarImagenUri(uri.toString())
        }
    }

    // ────────────────────────────────────────────────────────────────────
    // LANZADOR DE CÁMARA (TakePicture)
    // ────────────────────────────────────────────────────────────────────
    // Con este launcher abrimos la cámara del dispositivo para capturar una fotografía.
    var uriPendiente by rememberSaveable { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { fotoTomada ->
        if (fotoTomada && uriPendiente != null) {
            viewModel.cambiarImagenUri(uriPendiente.toString())
        }
        uriPendiente = null
    }

    // ────────────────────────────────────────────────────────────────────
    // CARGA DEL BITMAP PARA VISTA PREVIA
    // ────────────────────────────────────────────────────────────────────
    val bitmapVistaPrevia = remember(uiState.imagenUri) {
        if (uiState.imagenUri.isNotBlank()) {
            // Soportamos tanto URI normal como recursos drawable locales
            if (uiState.imagenUri.all { it.isDigit() }) {
                val resId = uiState.imagenUri.toInt()
                android.graphics.BitmapFactory.decodeResource(context.resources, resId)
            } else {
                cargarBitmapDesdeUri(context, Uri.parse(uiState.imagenUri))
            }
        } else {
            null
        }
    }

    // Con este estado controlamos cuándo mostramos las organizaciones disponibles.
    var menuOrganizacionesAbierto by remember {
        mutableStateOf(false)
    }

    // Estado para controlar la apertura del menú de imágenes precargadas
    var menuImagenesAbierto by remember { mutableStateOf(false) }

    // Lista de las 5 imágenes precargadas en res/drawable
    val imagenesPrecargadas = listOf(
        "imagen1" to "Imagen Precargada 1",
        "imagen2" to "Imagen Precargada 2",
        "imagen3" to "Imagen Precargada 3",
        "imagen4" to "Imagen Precargada 4",
        "imagen5" to "Imagen Precargada 5"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (uiState.idEnEdicion == null) {
                "Registrar evento"
            } else {
                "Editar evento"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // ────────────────────────────────────────────────────────────────
        // ANIMACIÓN 3: Crossfade
        // ────────────────────────────────────────────────────────────────
        // Con Crossfade hacemos que el texto de instrucción debajo del
        // título cambie de forma progresiva cuando el usuario selecciona
        // o quita una imagen.
        Crossfade(
            targetState = hayImagen,
            animationSpec = tween(durationMillis = 500),
            label = "Mensaje de estado de imagen"
        ) { tieneImagen ->
            Text(
                text = if (tieneImagen) {
                    "¡Imagen seleccionada para el evento!"
                } else {
                    "Ingresa los datos del evento corporativo."
                },
                color = if (tieneImagen) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Campos del formulario ────────────────────────────────────────
        OutlinedTextField(
            value = uiState.nombre,
            onValueChange = viewModel::cambiarNombre,
            label = { Text("Nombre del evento") },
            isError = uiState.errorNombre,
            supportingText = {
                if (uiState.errorNombre) {
                    Text("El nombre del evento es obligatorio")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.descripcion,
            onValueChange = viewModel::cambiarDescripcion,
            label = { Text("Descripción") },
            isError = uiState.errorDescripcion,
            supportingText = {
                if (uiState.errorDescripcion) {
                    Text("La descripción es obligatoria")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.fecha,
            onValueChange = viewModel::cambiarFecha,
            label = { Text("Fecha (DD/MM/AAAA)") },
            placeholder = { Text("Ejemplo: 25/12/2027") },
            isError = uiState.errorFecha,
            supportingText = {
                if (uiState.errorFecha) {
                    Text("Ingresa una fecha válida posterior a hoy")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            visualTransformation = FechaVisualTransformation()
        )

        OutlinedTextField(
            value = uiState.ubicacion,
            onValueChange = viewModel::cambiarUbicacion,
            label = { Text("Ubicación") },
            isError = uiState.errorUbicacion,
            supportingText = {
                if (uiState.errorUbicacion) {
                    Text("La ubicación es obligatoria")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Con este selector permitimos elegir una organización registrada en Room.
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = uiState.organizacion,
                onValueChange = {},
                readOnly = true,
                label = { Text("Organización") },
                placeholder = {
                    Text("Selecciona una organización")
                },
                isError = uiState.errorOrganizacion,
                supportingText = {
                    if (uiState.errorOrganizacion) {
                        Text("Selecciona una organización registrada")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    Text(
                        if (menuOrganizacionesAbierto) "▲" else "▼"
                    )
                }
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(end = 48.dp)
                    .clickable(
                        interactionSource = remember {
                            androidx.compose.foundation.interaction
                                .MutableInteractionSource()
                        },
                        indication = null,
                        onClick = {
                            menuOrganizacionesAbierto = true
                        }
                    )
            )

            DropdownMenu(
                expanded = menuOrganizacionesAbierto,
                onDismissRequest = {
                    menuOrganizacionesAbierto = false
                }
            ) {
                if (uiState.organizaciones.isEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Text("No hay organizaciones registradas")
                        },
                        onClick = {
                            menuOrganizacionesAbierto = false
                        }
                    )
                } else {
                    uiState.organizaciones.forEach { organizacion ->
                        DropdownMenuItem(
                            text = {
                                Text(organizacion.nombre)
                            },
                            onClick = {
                                viewModel.cambiarOrganizacion(organizacion.nombre)
                                menuOrganizacionesAbierto = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ────────────────────────────────────────────────────────────────
        // ANIMACIÓN 1: AnimatedVisibility — vista previa de la imagen
        // ────────────────────────────────────────────────────────────────
        // Con AnimatedVisibility mostramos la imagen seleccionada de forma
        // progresiva en lugar de que aparezca o desaparezca de golpe.
        AnimatedVisibility(
            visible = bitmapVistaPrevia != null,
            enter = fadeIn(animationSpec = tween(400)) + expandVertically(),
            exit = fadeOut(animationSpec = tween(300)) + shrinkVertically()
        ) {
            if (bitmapVistaPrevia != null) {
                Image(
                    bitmap = bitmapVistaPrevia.asImageBitmap(),
                    contentDescription = "Vista previa de la imagen del evento",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Con esta segunda AnimatedVisibility mostramos el recuadro indicador
        // solo cuando NO hay imagen seleccionada.
        AnimatedVisibility(
            visible = bitmapVistaPrevia == null,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(200)) + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = if (uiState.errorImagenUri) {
                            MaterialTheme.colorScheme.error
                        } else {
                            Color.Gray
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sin imagen seleccionada",
                    color = if (uiState.errorImagenUri) {
                        MaterialTheme.colorScheme.error
                    } else {
                        Color.Gray
                    }
                )
            }
        }

        // Con esta tercera AnimatedVisibility mostramos el mensaje de error
        // de imagen de forma animada.
        AnimatedVisibility(
            visible = uiState.errorImagenUri,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = "Debes seleccionar una imagen para el evento",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // ── Botón de selección de imagen desde Galería ───────────────────
        // Su color cambia animadamente gracias a animateColorAsState.
        Button(
            onClick = {
                selectorImagenes.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = colorBotonImagen)
        ) {
            Text(
                text = if (hayImagen) "Cambiar imagen de galería" else "Seleccionar de galería"
            )
        }

        // ── Botón desplegable para elegir imágenes precargadas de la app ──
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { menuImagenesAbierto = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Elegir imagen precargada de la app ▼")
            }

            DropdownMenu(
                expanded = menuImagenesAbierto,
                onDismissRequest = { menuImagenesAbierto = false }
            ) {
                imagenesPrecargadas.forEach { (nombreDrawable, etiqueta) ->
                    DropdownMenuItem(
                        text = { Text(etiqueta) },
                        onClick = {
                            val resId = context.resources.getIdentifier(nombreDrawable, "drawable", context.packageName)
                            if (resId != 0) {
                                viewModel.cambiarImagenUri(resId.toString())
                            }
                            menuImagenesAbierto = false
                        }
                    )
                }
            }
        }

        // ── Botón de cámara (recurso nativo) ─────────────────────────────
        // Con este botón permitimos capturar una fotografía directamente con la cámara.
        OutlinedButton(
            onClick = {
                try {
                    val nuevaUri = crearUriFotografia(context)
                    uriPendiente = nuevaUri
                    cameraLauncher.launch(nuevaUri)
                } catch (e: Exception) {
                    uriPendiente = null
                    e.printStackTrace()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tomar fotografía con cámara")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Con este mensaje informamos el resultado de la validación o del guardado del evento.
        // Lo envolvemos en AnimatedVisibility con expand/shrink para que entre y salga
        // de forma fluida, empujando el contenido inferior.
        AnimatedVisibility(
            visible = uiState.mensaje != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            uiState.mensaje?.let { mensaje ->
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (
                        mensaje.startsWith("No se pudo") ||
                        mensaje.startsWith("Completa") ||
                        mensaje.startsWith("Registra") ||
                        mensaje.startsWith("Selecciona")
                    ) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Con este botón guardamos un evento nuevo
        // o actualizamos los datos del evento que estamos editando.
        // Aplicamos una microinteracción de escala con animateFloatAsState al pulsar.
        val interactionSourceGuardarEvento = remember { MutableInteractionSource() }
        val presionadoGuardarEvento by interactionSourceGuardarEvento.collectIsPressedAsState()
        val escalaGuardarEvento by animateFloatAsState(
            targetValue = if (presionadoGuardarEvento) 0.96f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "Escala del botón guardar evento"
        )

        Button(
            onClick = { viewModel.guardarEvento() },
            interactionSource = interactionSourceGuardarEvento,
            modifier = Modifier
                .fillMaxWidth()
                .scale(escalaGuardarEvento)
        ) {
            Text(
                if (uiState.idEnEdicion == null) {
                    "Guardar evento"
                } else {
                    "Guardar cambios"
                }
            )
        }

        // Con este botón cancelamos una edición sin guardar cambios.
        if (uiState.idEnEdicion != null) {
            OutlinedButton(
                onClick = { viewModel.cancelarEdicion() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar edición")
            }
        }

        // Con este botón abrimos la pantalla independiente
        // donde consultamos y administramos los eventos registrados.
        OutlinedButton(
            onClick = onVerEventos,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver eventos registrados (${uiState.eventos.size})")
        }

        // Con este botón regresamos al panel administrativo.
        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al panel")
        }
    }
}