package cl.duoc.agenciaconnect.ui.oportunidades

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

// Con esta pantalla mostramos las oportunidades comerciales registradas y permitimos gestionarlas.
@Composable
fun OportunidadesRegistradasScreen(
    onVolverFormulario: () -> Unit,
    onVolver: () -> Unit,
    onEditarOportunidad: () -> Unit,
    viewModel: OportunidadViewModel = viewModel()
) {
    // Con este estado observamos las oportunidades, organizaciones y eventos almacenados en Room.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Con este título identificamos la vista del listado de oportunidades comerciales.
        Text(
            text = "Oportunidades registradas",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Total de oportunidades: ${uiState.oportunidades.size}",
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
            style = MaterialTheme.typography.titleMedium
        )

        // Con este mensaje informamos cuando todavía no se han registrado oportunidades.
        if (uiState.oportunidades.isEmpty()) {
            Text("Aún no hay oportunidades registradas.")
            Spacer(modifier = Modifier.weight(1f))
        } else {
            // Con esta lista optimizada presentamos cada oportunidad comercial en una tarjeta independiente.
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.oportunidades,
                    key = { oportunidad -> oportunidad.id }
                ) { oportunidad ->

                    // Con estas búsquedas obtenemos el nombre de la organización y del evento vinculado.
                    val organizacion = uiState.organizaciones.find { it.id == oportunidad.organizacionId }
                    val evento = uiState.eventos.find { it.id == oportunidad.eventoId }

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = oportunidad.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(text = "Tipo: ${oportunidad.tipo}")
                            Text(text = "Organización: ${organizacion?.nombre ?: "No encontrada"}")

                            if (evento != null) {
                                Text(text = "Evento vinculado: ${evento.nombre}")
                            }

                            Text(
                                text = "Prioridad: ${oportunidad.prioridad}  |  Potencial: ${oportunidad.potencial}",
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "Estado: ${oportunidad.estado}",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(text = "Responsable: ${oportunidad.responsable}")

                            if (oportunidad.observaciones.isNotBlank()) {
                                Text(text = "Observaciones: ${oportunidad.observaciones}")
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Con estos botones permitimos editar los datos o solicitar la eliminación.
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.editarOportunidad(oportunidad)
                                        onEditarOportunidad()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Editar")
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.solicitarEliminacion(oportunidad)
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

        // Con este botón regresamos al formulario para registrar una nueva oportunidad.
        OutlinedButton(
            onClick = onVolverFormulario,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text("Volver al formulario")
        }

        // Con este botón regresamos al panel de administración general.
        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al panel")
        }
    }

    // Con este diálogo solicitamos confirmación antes de borrar una oportunidad de Room.
    uiState.oportunidadPorEliminar?.let { oportunidad ->
        AlertDialog(
            onDismissRequest = {
                viewModel.cancelarEliminacion()
            },
            title = {
                Text("Eliminar oportunidad")
            },
            text = {
                Text("¿Estás seguro de que deseas eliminar la oportunidad \"${oportunidad.nombre}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmarEliminacion()
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelarEliminacion()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}