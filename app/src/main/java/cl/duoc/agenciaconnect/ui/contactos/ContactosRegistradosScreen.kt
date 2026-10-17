package cl.duoc.agenciaconnect.ui.contactos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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

// Con esta pantalla mostramos los contactos registrados y permitimos administrarlos.
@Composable
fun ContactosRegistradosScreen(
    onVolverAlFormulario: () -> Unit,
    onVolver: () -> Unit,
    onEditarContacto: () -> Unit,
    viewModel: ContactosViewModel = viewModel()
) {
    // Con este estado observamos los contactos y las organizaciones disponibles.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Con este título identificamos la pantalla de contactos registrados.
        Text(
            text = "Contactos registrados",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Total de contactos: ${uiState.contactos.size}",
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
        )

        // Con esta lista presentamos cada contacto en una tarjeta independiente.
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = uiState.contactos,
                key = { contacto -> contacto.id }
            ) { contacto ->

                // Con esta búsqueda obtenemos el nombre de la organización asociada.
                val organizacion = uiState.organizaciones.find {
                    it.id == contacto.organizacionId
                }

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = contacto.nombreCompleto,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Organización: ${organizacion?.nombre ?: "No disponible"}"
                        )
                        Text(text = "Cargo: ${contacto.cargo}")
                        Text(text = "Área: ${contacto.area}")
                        Text(text = "Correo: ${contacto.correo}")

                        if (contacto.telefono.isNotBlank()) {
                            Text(text = "Teléfono: ${contacto.telefono}")
                        }

                        Text(text = "Medio preferido: ${contacto.medioPreferido}")

                        if (contacto.observaciones.isNotBlank()) {
                            Text(text = "Observaciones: ${contacto.observaciones}")
                        }

                        // Con estos botones permitimos editar o solicitar la eliminación.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.editarContacto(contacto)
                                    onEditarContacto()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Editar")
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.solicitarEliminarContacto(contacto)
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

        // Con este botón regresamos al formulario para registrar otro contacto.
        OutlinedButton(
            onClick = onVolverAlFormulario,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text("Volver al formulario")
        }

        // Con este botón regresamos al panel administrativo.
        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al panel")
        }
    }

    // Con este diálogo pedimos confirmación antes de eliminar un contacto.
    uiState.contactoPorEliminar?.let { contacto ->
        AlertDialog(
            onDismissRequest = {
                viewModel.cancelarEliminarContacto()
            },
            title = {
                Text("Eliminar contacto")
            },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar a ${contacto.nombreCompleto}?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmarEliminarContacto()
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelarEliminarContacto()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
