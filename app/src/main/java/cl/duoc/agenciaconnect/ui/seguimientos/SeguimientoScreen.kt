package cl.duoc.agenciaconnect.ui.seguimientos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.agenciaconnect.data.model.Oportunidad
import cl.duoc.agenciaconnect.data.model.Usuario
import cl.duoc.agenciaconnect.util.agendarEnCalendario

// Con esta pantalla mostramos el historial de gestiones y permitimos registrar nuevas acciones comerciales.
@Composable
fun SeguimientoScreen(
    oportunidad: Oportunidad,
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    viewModel: SeguimientoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(oportunidad.id) {
        viewModel.cargarSeguimientos(oportunidad.id)
    }

    var menuAccionesAbierto by remember { mutableStateOf(false) }
    val tiposAccion = listOf(
        "Primer contacto",
        "Llamada telefónica",
        "Envío de presentación",
        "Envío de propuesta",
        "Reunión de coordinación",
        "Recepción de respuesta",
        "Cierre y confirmación"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Historial de seguimientos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Oportunidad: ${oportunidad.nombre}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Con este botón integramos el recurso nativo del calendario para agendar un recordatorio en el teléfono.
        OutlinedButton(
            onClick = {
                agendarEnCalendario(
                    context = context,
                    titulo = "Seguimiento: ${oportunidad.nombre}",
                    descripcion = "Gestión comercial para la oportunidad ${oportunidad.nombre}",
                    ubicacion = "Agencia Atenas / Remoto",
                    fechaTexto = ""
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agendar recordatorio en el Calendario")
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Registrar nueva gestión",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall
        )

        // Selector desplegable del tipo de acción
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.tipoAccion,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de acción") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Text(if (menuAccionesAbierto) "▲" else "▼") }
            )

            Box(
                Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { menuAccionesAbierto = true }
                    )
            )

            DropdownMenu(
                expanded = menuAccionesAbierto,
                onDismissRequest = { menuAccionesAbierto = false }
            ) {
                tiposAccion.forEach { accion ->
                    DropdownMenuItem(
                        text = { Text(accion) },
                        onClick = {
                            viewModel.cambiarTipoAccion(accion)
                            menuAccionesAbierto = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.resultado,
            onValueChange = viewModel::cambiarResultado,
            label = { Text("Resultado obtenido") },
            placeholder = { Text("Ej.: Mostraron interés en propuesta dental") },
            isError = uiState.errorResultado,
            supportingText = {
                if (uiState.errorResultado) Text("El resultado es obligatorio.")
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.proximaAccion,
            onValueChange = viewModel::cambiarProximaAccion,
            label = { Text("Próxima acción o compromiso") },
            placeholder = { Text("Ej.: Llamar el próximo martes") },
            isError = uiState.errorProximaAccion,
            supportingText = {
                if (uiState.errorProximaAccion) Text("La próxima acción es obligatoria.")
            },
            modifier = Modifier.fillMaxWidth()
        )

        uiState.mensaje?.let {
            Text(
                text = it,
                color = if (it.startsWith("No") || it.startsWith("Completa")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = {
                viewModel.guardarSeguimiento(
                    oportunidadId = oportunidad.id,
                    usuarioNombre = usuarioActual?.nombre ?: "Ejecutivo",
                    onExito = {}
                )
            },
            enabled = !uiState.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (uiState.guardando) "Guardando..." else "Registrar gestión")
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Bitácora histórica (${uiState.seguimientos.size})",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (uiState.seguimientos.isEmpty()) {
                item {
                    Text("Aún no se han registrado gestiones para esta oportunidad.")
                }
            } else {
                items(uiState.seguimientos, key = { it.id }) { seg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(
                                fadeInSpec = tween(durationMillis = 300),
                                placementSpec = tween(durationMillis = 300),
                                fadeOutSpec = tween(durationMillis = 300)
                            )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${seg.tipoAccion} - ${seg.fechaHora}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(text = "Por: ${seg.usuarioNombre}", style = MaterialTheme.typography.labelMedium)
                            Text(text = "Resultado: ${seg.resultado}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Próximo paso: ${seg.proximaAccion}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver a oportunidades")
        }
    }
}