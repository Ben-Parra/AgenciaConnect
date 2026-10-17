package cl.duoc.agenciaconnect.ui.oportunidades

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.agenciaconnect.data.model.Oportunidad
import cl.duoc.agenciaconnect.data.model.Usuario

// Con esta pantalla listamos las oportunidades, permitimos búsqueda, filtros por estado/prioridad
// y aplicamos restricciones según si el usuario es Supervisor o Ejecutivo.
@Composable
fun OportunidadesRegistradasScreen(
    usuarioActual: Usuario?,
    onVolverFormulario: () -> Unit,
    onVolver: () -> Unit,
    onEditarOportunidad: (Oportunidad) -> Unit,
    onVerSeguimiento: (Oportunidad) -> Unit,
    viewModel: OportunidadViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val esSupervisor = usuarioActual?.rol == "SUPERVISOR"

    // Estados para búsqueda y filtrado
    var textoBusqueda by remember { mutableStateOf("") }
    var filtroEstado by remember { mutableStateOf("Todos") }
    var menuFiltroEstadoAbierto by remember { mutableStateOf(false) }

    // Estados reales que coinciden 100% con el formulario
    val estadosFiltro = listOf(
        "Todos",
        "Evento identificado",
        "Contacto pendiente",
        "Contacto realizado",
        "Propuesta enviada",
        "En espera de respuesta",
        "Participación confirmada",
        "Oportunidad descartada"
    )

    // Filtrado reactivo:
    // Si es Supervisor ve todas las oportunidades.
    // Si es Ejecutivo ve solo las asignadas a su nombre (o las que estén sin responsable asignado).
    // Filtrado reactivo de visibilidad por rol:
    val oportunidadesVisibles = uiState.oportunidades.filter { op ->
        if (esSupervisor) {
            true // El supervisor tiene visibilidad total del pipeline
        } else {
            // El ejecutivo ve las oportunidades que tengan asignado su nombre exacto
            val nombreEjecutivo = usuarioActual?.nombre.orEmpty().trim()
            op.responsable.trim().equals(nombreEjecutivo, ignoreCase = true)
        }
    }

    val oportunidadesFiltradas = oportunidadesVisibles.filter { oportunidad ->
        val coincideTexto = oportunidad.nombre.contains(textoBusqueda, ignoreCase = true) ||
                oportunidad.responsable.contains(textoBusqueda, ignoreCase = true)
        val coincideEstado = if (filtroEstado == "Todos") true else oportunidad.estado.equals(filtroEstado, ignoreCase = true)
        coincideTexto && coincideEstado
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Oportunidades comerciales",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Perfil: ${usuarioActual?.rol ?: "INVITADO"} - ${usuarioActual?.nombre ?: ""}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        // Barra de búsqueda por texto
        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it },
            label = { Text("Buscar por nombre o responsable") },
            placeholder = { Text("Ej.: Clínica dental, Carlos...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Filtro por Estado
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = "Filtro estado: $filtroEstado",
                onValueChange = {},
                readOnly = true,
                label = { Text("Filtrar por estado") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Text(if (menuFiltroEstadoAbierto) "▲" else "▼") }
            )

            Box(
                Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { menuFiltroEstadoAbierto = true }
                    )
            )

            DropdownMenu(
                expanded = menuFiltroEstadoAbierto,
                onDismissRequest = { menuFiltroEstadoAbierto = false }
            ) {
                estadosFiltro.forEach { estado ->
                    DropdownMenuItem(
                        text = { Text(estado) },
                        onClick = {
                            filtroEstado = estado
                            menuFiltroEstadoAbierto = false
                        }
                    )
                }
            }
        }

        Text(
            text = "Oportunidades encontradas: ${oportunidadesFiltradas.size}",
            style = MaterialTheme.typography.titleSmall
        )

        if (oportunidadesFiltradas.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No hay oportunidades que coincidan con el criterio.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = oportunidadesFiltradas,
                    key = { it.id }
                ) { oportunidad ->
                    TarjetaOportunidad(
                        oportunidad = oportunidad,
                        esSupervisor = esSupervisor,
                        onEditar = {
                            viewModel.cargarOportunidadParaEditar(oportunidad)
                            onEditarOportunidad(oportunidad)
                        },
                        onVerSeguimiento = {
                            onVerSeguimiento(oportunidad)
                        },
                        onEliminar = {
                            viewModel.solicitarEliminar(oportunidad)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedButton(
            onClick = onVolverFormulario,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear nueva oportunidad")
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al panel")
        }
    }

    // Diálogo de confirmación para eliminar (restringido a supervisor)
    uiState.oportunidadPorEliminar?.let { oportunidad ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelarEliminar() },
            title = { Text("Eliminar oportunidad") },
            text = { Text("¿Deseas eliminar definitivamente la oportunidad \"${oportunidad.nombre}\"?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmarEliminar() }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelarEliminar() }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Con esta tarjeta representamos los datos completos de una oportunidad y sus acciones según rol.
@Composable
private fun TarjetaOportunidad(
    oportunidad: Oportunidad,
    esSupervisor: Boolean,
    onEditar: () -> Unit,
    onVerSeguimiento: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = oportunidad.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = oportunidad.prioridad,
                    fontWeight = FontWeight.Bold,
                    color = when (oportunidad.prioridad) {
                        "Alta" -> MaterialTheme.colorScheme.error
                        "Media" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                )
            }

            Text("Tipo: ${oportunidad.tipo}", style = MaterialTheme.typography.bodySmall)
            Text("Estado: ${oportunidad.estado}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
            Text("Potencial: ${oportunidad.potencial}", style = MaterialTheme.typography.bodySmall)
            Text("Responsable: ${oportunidad.responsable}", style = MaterialTheme.typography.bodySmall)

            if (oportunidad.observaciones.isNotBlank()) {
                Text("Obs: ${oportunidad.observaciones}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón principal de seguimiento (Bitácora + Calendario)
            Button(
                onClick = onVerSeguimiento,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Seguimiento y Calendario", fontSize = 13.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEditar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Editar", fontSize = 12.sp)
                }

                // La eliminación solo se muestra si el usuario es SUPERVISOR
                if (esSupervisor) {
                    OutlinedButton(
                        onClick = onEliminar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Eliminar", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}