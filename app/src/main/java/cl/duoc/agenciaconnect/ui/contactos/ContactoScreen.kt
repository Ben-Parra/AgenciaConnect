package cl.duoc.agenciaconnect.ui.contactos

import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// Con esta pantalla registramos contactos y modificamos sus datos existentes.
@Composable
fun ContactoScreen(
    onVolver: () -> Unit,
    onVerContactos: () -> Unit,
    viewModel: ContactosViewModel = viewModel()
) {
    // Con este estado observamos los datos del formulario y los registros disponibles.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Con este efecto reiniciamos el formulario cuando abandonamos la pantalla.
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.reiniciarFormularioAlSalir()
        }
    }


    // Con esta variable controlamos la apertura del selector de organizaciones.
    var menuOrganizacionesAbierto by remember { mutableStateOf(false) }

    // Con esta variable mostramos el nombre de la organización actualmente seleccionada.
    val organizacionSeleccionada = uiState.organizaciones.find {
        it.id == uiState.organizacionId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Con este título diferenciamos el registro de la edición de un contacto.
        Text(
            text = if (uiState.idEnEdicion == null) {
                "Registrar contacto"
            } else {
                "Editar contacto"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text("Ingresa los datos de la persona de contacto.")

        Spacer(modifier = Modifier.height(8.dp))

        // Con este campo ingresamos el nombre completo del contacto.
        OutlinedTextField(
            value = uiState.nombreCompleto,
            onValueChange = viewModel::actualizarNombreCompleto,
            label = { Text("Nombre completo") },
            isError = uiState.errorNombreCompleto,
            supportingText = {
                if (uiState.errorNombreCompleto) {
                    Text("El nombre completo es obligatorio.")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Con este selector asociamos el contacto a una organización registrada.
        Box(modifier = Modifier.fillMaxWidth()) {

            OutlinedTextField(
                value = organizacionSeleccionada?.nombre ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Organización") },
                placeholder = { Text("Selecciona una organización") },
                isError = uiState.errorOrganizacion,
                supportingText = {
                    if (uiState.errorOrganizacion) {
                        Text("Debes seleccionar una organización registrada.")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    Text(if (menuOrganizacionesAbierto) "▲" else "▼")
                }
            )

            // Con esta capa permitimos abrir el menú al pulsar el campo.
            Box(
                Modifier
                    .matchParentSize()
                    .padding(end = 48.dp)
                    .clickable(
                        interactionSource = remember {
                            androidx.compose.foundation.interaction.MutableInteractionSource()
                        },
                        indication = null,
                        onClick = {
                            menuOrganizacionesAbierto = true
                        }
                    )
            )

            // Con este menú mostramos las organizaciones junto al campo que lo activa.
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
                                viewModel.actualizarOrganizacion(organizacion.id)
                                menuOrganizacionesAbierto = false
                            }
                        )
                    }
                }
            }
        }

        // Con estos campos registramos el cargo y el área del contacto.
        OutlinedTextField(
            value = uiState.cargo,
            onValueChange = viewModel::actualizarCargo,
            label = { Text("Cargo") },
            placeholder = { Text("Ej.: Encargado de beneficios") },
            isError = uiState.errorCargo,
            supportingText = {
                if (uiState.errorCargo) {
                    Text("El cargo es obligatorio.")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.area,
            onValueChange = viewModel::actualizarArea,
            label = { Text("Área de trabajo") },
            placeholder = { Text("Ej.: Recursos Humanos") },
            isError = uiState.errorArea,
            supportingText = {
                if (uiState.errorArea) {
                    Text("El área de trabajo es obligatoria.")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Con este campo ingresamos el correo y mostramos su error de formato.
        OutlinedTextField(
            value = uiState.correo,
            onValueChange = viewModel::actualizarCorreo,
            label = { Text("Correo electrónico") },
            placeholder = { Text("contacto@ejemplo.cl") },
            isError = uiState.errorCorreo,
            supportingText = {
                if (uiState.errorCorreo) {
                    Text("Ingresa un correo electrónico válido.")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Con este campo registramos un teléfono opcional.
        OutlinedTextField(
            value = uiState.telefono,
            onValueChange = viewModel::actualizarTelefono,
            label = { Text("Teléfono (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Con este campo registramos el medio de comunicación preferido.
        OutlinedTextField(
            value = uiState.medioPreferido,
            onValueChange = viewModel::actualizarMedioPreferido,
            label = { Text("Medio de contacto preferido") },
            placeholder = { Text("Ej.: Correo, teléfono") },
            isError = uiState.errorMedioPreferido,
            supportingText = {
                if (uiState.errorMedioPreferido) {
                    Text("El medio de contacto preferido es obligatorio.")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Con este campo ingresamos observaciones opcionales sin ocupar espacio excesivo.
        OutlinedTextField(
            value = uiState.observaciones,
            onValueChange = viewModel::actualizarObservaciones,
            label = { Text("Observaciones (opcional)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            minLines = 3,
            maxLines = 4
        )

        // Con este mensaje informamos el resultado de las operaciones.
        uiState.mensaje?.let { mensaje ->
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = if (
                    mensaje.startsWith("No se pudo") ||
                    mensaje.startsWith("Revisa") ||
                    mensaje.startsWith("Selecciona")
                ) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }

        // Con este botón registramos el contacto o guardamos los cambios realizados.
        Button(
            onClick = { viewModel.guardarContacto() },
            enabled = !uiState.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                when {
                    uiState.guardando -> "Guardando..."
                    uiState.idEnEdicion == null -> "Guardar contacto"
                    else -> "Guardar cambios"
                }
            )
        }

        // Con este botón cancelamos la edición sin guardar los cambios pendientes.
        if (uiState.idEnEdicion != null) {
            OutlinedButton(
                onClick = { viewModel.cancelarEdicion() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar edición")
            }
        }

        // Con este botón abrimos la pantalla del listado de contactos.
        OutlinedButton(
            onClick = onVerContactos,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver contactos (${uiState.contactos.size})")
        }

        // Con este botón regresamos al panel administrativo sin ocupar espacio adicional.
        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Volver al panel")
        }
    } // <--- AQUÍ SE CIERRA CORRECTAMENTE LA COLUMN PRINCIPAL
}