package cl.duoc.agenciaconnect.ui.organizaciones

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

// Con esta pantalla consultamos las organizaciones y administramos sus registros.
@Composable
fun OrganizacionesRegistradasScreen(
    onVolverFormulario: () -> Unit,
    onEditarOrganizacion: () -> Unit,
    viewModel: OrganizacionViewModel = viewModel()
) {
    // Con este estado observamos la lista actualizada desde Room.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Organizaciones registradas",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Total de organizaciones: ${uiState.organizaciones.size}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Con este bloque informamos cuando todavía no existen organizaciones guardadas.
        if (uiState.organizaciones.isEmpty()) {
            Text("Aún no hay organizaciones registradas.")
        } else {
            // Con esta lista mostramos cada organización y sus opciones de administración.
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.organizaciones,
                    key = { organizacion -> organizacion.id }
                ) { organizacion ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = organizacion.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text("Tipo: ${organizacion.tipo}")
                            Text("Rubro: ${organizacion.rubro}")
                            Text("Comuna: ${organizacion.comuna}")
                            Text("Región: ${organizacion.region}")
                            Text("Público asociado: ${organizacion.publicoAsociado}")

                            // Con este bloque mostramos solo los datos opcionales que fueron ingresados.
                            if (organizacion.sitioWeb.isNotBlank()) {
                                Text("Sitio web: ${organizacion.sitioWeb}")
                            }

                            if (organizacion.observaciones.isNotBlank()) {
                                Text("Observaciones: ${organizacion.observaciones}")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Con estos botones permitimos editar o solicitar la eliminación del registro.
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.editarOrganizacion(organizacion)
                                        onEditarOrganizacion()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Editar")
                                }

                                Button(
                                    onClick = {
                                        viewModel.solicitarEliminacion(organizacion)
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

        // Con este botón regresamos al formulario de organizaciones.
        OutlinedButton(
            onClick = onVolverFormulario,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al formulario")
        }
    }

    // Con este diálogo pedimos confirmación antes de eliminar una organización.
    uiState.organizacionPorEliminar?.let { organizacion ->
        AlertDialog(
            onDismissRequest = {
                viewModel.cancelarEliminacion()
            },
            title = {
                Text("Eliminar organización")
            },
            text = {
                Text(
                    "¿Deseas eliminar la organización \"${organizacion.nombre}\"?"
                )
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
