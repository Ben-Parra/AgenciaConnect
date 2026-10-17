package cl.duoc.agenciaconnect.ui.eventos

import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// Con esta clase mostramos las barras de la fecha sin modificar
// los dígitos originales ni desordenar la posición del cursor.
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

            // Con esta función ajustamos el cursor al mostrar las barras.
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 4 -> offset + 1
                    else -> offset + 2
                }.coerceAtMost(formateado.length)
            }

            // Con esta función relacionamos la posición visual del cursor
            // con la posición real del texto que estamos escribiendo.
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

// Con esta pantalla presentamos únicamente el formulario
// para registrar eventos o modificar los que ya existen.
@Composable
fun EventosScreen(
    onVolver: () -> Unit,
    onVerEventos: () -> Unit,
    viewModel: EventoViewModel = viewModel()
) {
    // Con este estado observamos los datos y errores del formulario.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Con este efecto reiniciamos el formulario cuando abandonamos la pantalla.
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.reiniciarFormularioAlSalir()
        }
    }


    // Con este estado controlamos cuándo mostramos las organizaciones disponibles.
    var menuOrganizacionesAbierto by remember {
        mutableStateOf(false)
    }

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

        Text(
            text = "Ingresa los datos del evento corporativo."
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Con estos campos ingresamos los datos del evento
        // y mostramos los errores específicos de cada campo.
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

            // Con esta superficie habilitamos la apertura del menú
            // sin permitir que el usuario escriba texto libre.
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

            // Con este menú presentamos las organizaciones registradas
            // justo debajo del campo correspondiente.
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
                                // Con esta acción guardamos la organización elegida.
                                viewModel.cambiarOrganizacion(organizacion.nombre)
                                menuOrganizacionesAbierto = false
                            }
                        )
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(8.dp))

        // Con este mensaje informamos el resultado de la validación o del guardado del evento.
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

        Spacer(modifier = Modifier.height(8.dp))

        // Con este botón guardamos un evento nuevo
        // o actualizamos los datos del evento que estamos editando.
        Button(
            onClick = { viewModel.guardarEvento() },
            modifier = Modifier.fillMaxWidth()
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
