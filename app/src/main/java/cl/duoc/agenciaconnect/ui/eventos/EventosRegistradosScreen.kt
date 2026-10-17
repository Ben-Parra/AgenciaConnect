package cl.duoc.agenciaconnect.ui.eventos

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// Con esta pantalla mostramos los eventos guardados
// y ofrecemos las opciones para editarlos o eliminarlos.
@Composable
fun EventosRegistradosScreen(
    onVolverFormulario: () -> Unit,
    onVolver: () -> Unit,
    onEditarEvento: () -> Unit,
    viewModel: EventoViewModel = viewModel()
) {
    // Con este estado observamos los eventos guardados en Room.
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

        // Con este mensaje informamos cuando todavía no hay eventos.
        if (uiState.eventos.isEmpty()) {
            Text("Aún no hay eventos registrados.")
        } else {
            // Con esta lista mostramos cada evento y sus acciones.
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

                            // Con este bloque presentamos la fecha
                            // con barras para facilitar su lectura.
                            val fechaVisible =
                                if (evento.fecha.length == 8) {
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
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                // Con este botón cargamos el evento
                                // y regresamos al formulario para editarlo.
                                OutlinedButton(
                                    onClick = {
                                        viewModel.cargarEventoParaEditar(evento)
                                        onEditarEvento()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Editar")
                                }

                                // Con este botón solicitamos confirmación
                                // antes de eliminar el evento seleccionado.
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
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Con este botón regresamos al formulario de eventos.
        OutlinedButton(
            onClick = onVolverFormulario,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al formulario")
        }
        // Con este botón regresamos directamente al panel administrativo.
        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al panel")
        }
    }

    // Con este diálogo confirmamos si realmente queremos eliminar
    // el evento seleccionado antes de borrar sus datos.
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
