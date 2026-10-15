package cl.duoc.agenciaconnect.ui.eventos

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

// Con esta clase mostramos las barras de la fecha sin modificar
// los dígitos originales ni desordenar la posición del cursor.
private class FechaVisualTransformation : VisualTransformation {
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

// Con esta pantalla mostramos el formulario y la lista de eventos,
// utilizando el ViewModel para centralizar los datos y las operaciones.
@Composable
fun EventosScreen(
    onVolver: () -> Unit,
    viewModel: EventoViewModel = viewModel()
) {
    // Con este estado observamos los cambios realizados en el ViewModel.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Gestión de eventos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        OutlinedTextField(
            value = uiState.organizacion,
            onValueChange = viewModel::cambiarOrganizacion,
            label = { Text("Organización") },
            isError = uiState.errorOrganizacion,
            supportingText = {
                if (uiState.errorOrganizacion) {
                    Text("La organización es obligatoria")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Con este botón registramos el evento o guardamos sus cambios.
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

        // Con este botón cancelamos una edición sin modificar el registro.
        if (uiState.idEnEdicion != null) {
            OutlinedButton(
                onClick = { viewModel.cancelarEdicion() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar edición")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Eventos registrados: ${uiState.eventos.size}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Con esta lista mostramos los eventos registrados y sus acciones.
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = uiState.eventos,
                key = { evento -> evento.id }
            ) { evento ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = evento.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text("Descripción: ${evento.descripcion}")

                        val fechaVisible = if (evento.fecha.length == 8) {
                            "${evento.fecha.substring(0, 2)}/" +
                                    "${evento.fecha.substring(2, 4)}/" +
                                    evento.fecha.substring(4, 8)
                        } else {
                            evento.fecha
                        }

                        Text("Fecha: $fechaVisible")
                        Text("Ubicación: ${evento.ubicacion}")
                        Text("Organización: ${evento.organizacion}")

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.cargarEventoParaEditar(evento)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Editar")
                            }

                            Button(
                                onClick = {
                                    viewModel.solicitarEliminar(evento)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Con este botón regresamos a la pantalla anterior.
        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al panel")
        }
    }

    // Con este diálogo solicitamos confirmación antes de eliminar un evento.
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
