package cl.duoc.agenciaconnect.ui.organizaciones

import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// Con esta pantalla registramos organizaciones y modificamos sus datos existentes.
@Composable
fun OrganizacionesScreen(
    onVolver: () -> Unit,
    onVerOrganizaciones: () -> Unit,
    viewModel: OrganizacionViewModel = viewModel()
) {
    // Con este estado observamos los datos del formulario y las organizaciones registradas.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Con este efecto reiniciamos el formulario cuando abandonamos la pantalla.
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.reiniciarFormularioAlSalir()
        }
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
                "Registrar organización"
            } else {
                "Editar organización"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text("Ingresa los datos de la empresa o institución.")

        Spacer(modifier = Modifier.height(8.dp))

        // Con este campo ingresamos el nombre y mostramos su error si está vacío.
        OutlinedTextField(
            value = uiState.nombre,
            onValueChange = viewModel::cambiarNombre,
            label = { Text("Nombre de la organización") },
            isError = uiState.errorNombre,
            supportingText = {
                if (uiState.errorNombre) {
                    Text("El nombre es obligatorio.")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Con este campo indicamos el tipo de organización.
        OutlinedTextField(
            value = uiState.tipo,
            onValueChange = viewModel::cambiarTipo,
            label = { Text("Tipo de organización") },
            placeholder = {
                Text("Ej.: Empresa, Municipalidad")
            },
            isError = uiState.errorTipo,
            supportingText = {
                if (uiState.errorTipo) {
                    Text("El tipo de organización es obligatorio.")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Con este campo registramos el rubro o sector de la organización.
        OutlinedTextField(
            value = uiState.rubro,
            onValueChange = viewModel::cambiarRubro,
            label = { Text("Rubro") },
            placeholder = { Text("Ej.: Marketing, Comercio") },
            isError = uiState.errorRubro,
            supportingText = {
                if (uiState.errorRubro) {
                    Text("El rubro es obligatorio.")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Con este campo registramos la comuna de la organización.
        OutlinedTextField(
            value = uiState.comuna,
            onValueChange = viewModel::cambiarComuna,
            label = { Text("Comuna") },
            isError = uiState.errorComuna,
            supportingText = {
                if (uiState.errorComuna) {
                    Text("La comuna es obligatoria.")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Con este campo registramos la región correspondiente.
        OutlinedTextField(
            value = uiState.region,
            onValueChange = viewModel::cambiarRegion,
            label = { Text("Región") },
            isError = uiState.errorRegion,
            supportingText = {
                if (uiState.errorRegion) {
                    Text("La región es obligatoria.")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Con este campo permitimos registrar un sitio web opcional.
        OutlinedTextField(
            value = uiState.sitioWeb,
            onValueChange = viewModel::cambiarSitioWeb,
            label = { Text("Sitio web (opcional)") },
            placeholder = { Text("https://ejemplo.cl") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Con este campo describimos el público relacionado con la organización.
        OutlinedTextField(
            value = uiState.publicoAsociado,
            onValueChange = viewModel::cambiarPublicoAsociado,
            label = { Text("Público asociado") },
            placeholder = { Text("Ej.: Familias, trabajadores") },
            isError = uiState.errorPublicoAsociado,
            supportingText = {
                if (uiState.errorPublicoAsociado) {
                    Text("El público asociado es obligatorio.")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Con este campo añadimos observaciones que no son obligatorias.
        OutlinedTextField(
            value = uiState.observaciones,
            onValueChange = viewModel::cambiarObservaciones,
            label = { Text("Observaciones (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Con este mensaje informamos el resultado de las operaciones del formulario.
        uiState.mensaje?.let { mensaje ->
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = if (mensaje.startsWith("No se pudo") ||
                    mensaje.startsWith("Completa")
                ) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }

        // Con este botón registramos una organización o guardamos los cambios de edición.
        Button(
            onClick = { viewModel.guardarOrganizacion() },
            enabled = !uiState.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (uiState.guardando) {
                    "Guardando..."
                } else if (uiState.idEnEdicion == null) {
                    "Guardar organización"
                } else {
                    "Guardar cambios"
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

        // Con este botón abrimos la pantalla independiente del listado de organizaciones.
        OutlinedButton(
            onClick = onVerOrganizaciones,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver organizaciones (${uiState.organizaciones.size})")
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
