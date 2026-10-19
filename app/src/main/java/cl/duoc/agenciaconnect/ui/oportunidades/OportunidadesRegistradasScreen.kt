package cl.duoc.agenciaconnect.ui.oportunidades

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.agenciaconnect.data.model.Oportunidad
import cl.duoc.agenciaconnect.data.model.Usuario
import cl.duoc.agenciaconnect.ui.theme.coloresOportunidad
import cl.duoc.agenciaconnect.util.FechaVisualTransformation

// Función auxiliar para formatear la fecha guardada (DDMMYYYY) a visual (DD/MM/AAAA) en las tarjetas
private fun formatearFechaVisual(fechaCrud: String): String {
    val soloDigitos = fechaCrud.filter { it.isDigit() }
    return buildString {
        soloDigitos.forEachIndexed { index, char ->
            if (index == 2 || index == 4) append("/")
            append(char)
        }
    }
}

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

    // Estados de búsqueda y filtros
    var textoBusqueda by remember { mutableStateOf("") }
    var filtroEstado by remember { mutableStateOf("Todos") }
    var menuFiltroEstadoAbierto by remember { mutableStateOf(false) }

    var filtroPrioridad by remember { mutableStateOf("Todas") }
    var menuFiltroPrioridadAbierto by remember { mutableStateOf(false) }

    var filtroTipo by remember { mutableStateOf("Todos") }
    var menuFiltroTipoAbierto by remember { mutableStateOf(false) }

    var filtroOrganizacion by remember { mutableStateOf("Todas") }
    var menuFiltroOrgAbierto by remember { mutableStateOf(false) }

    // Estado para el Filtro por Fecha de Detección (Criterio 21)
    var filtroFecha by remember { mutableStateOf("") }

    val estadosFiltro = listOf(
        "Todos", "Evento identificado", "Contacto pendiente", "Contacto realizado",
        "Propuesta enviada", "En espera de respuesta", "Participación confirmada", "Oportunidad descartada"
    )
    val prioridadesFiltro = listOf("Todas", "Alta", "Media", "Baja")
    val tiposFiltro = listOf(
        "Todos", "Evento corporativo", "Feria empresarial", "Actividad municipal",
        "Jornada de beneficios", "Feria institucional", "Actividad de trabajadores", "Activación comercial", "Otra instancia"
    )
    val organizacionesFiltro = listOf("Todas") + uiState.organizaciones.map { it.nombre }

    // Filtrado por Rol
    val oportunidadesVisibles = uiState.oportunidades.filter { op ->
        if (esSupervisor) true else op.responsable.trim().equals(usuarioActual?.nombre.orEmpty().trim(), ignoreCase = true)
    }

    // Filtrado completo incluyendo texto, estado, prioridad, tipo, organización y fecha
    val oportunidadesFiltradas = oportunidadesVisibles.filter { oportunidad ->
        val coincideTexto = oportunidad.nombre.contains(textoBusqueda, ignoreCase = true) ||
                oportunidad.responsable.contains(textoBusqueda, ignoreCase = true)

        val coincideEstado = if (filtroEstado == "Todos") true else oportunidad.estado.equals(filtroEstado, ignoreCase = true)
        val coincidePrioridad = if (filtroPrioridad == "Todas") true else oportunidad.prioridad.equals(filtroPrioridad, ignoreCase = true)
        val coincideTipo = if (filtroTipo == "Todos") true else oportunidad.tipo.equals(filtroTipo, ignoreCase = true)

        val orgAsociada = uiState.organizaciones.find { it.id == oportunidad.organizacionId }?.nombre.orEmpty()
        val coincideOrg = if (filtroOrganizacion == "Todas") true else orgAsociada.equals(filtroOrganizacion, ignoreCase = true)

        // Limpiamos de barras el filtro para buscar contra los dígitos guardados en la BD
        val fechaFiltroLimpia = filtroFecha.filter { it.isDigit() }
        val coincideFecha = fechaFiltroLimpia.isBlank() || oportunidad.fechaDeteccion.contains(fechaFiltroLimpia, ignoreCase = true)

        coincideTexto && coincideEstado && coincidePrioridad && coincideTipo && coincideOrg && coincideFecha
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

        // Fila 1: Estado y Prioridad
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(value = filtroEstado, onValueChange = {}, readOnly = true, label = { Text("Estado") }, modifier = Modifier.fillMaxWidth(), singleLine = true, trailingIcon = { Text(if (menuFiltroEstadoAbierto) "▲" else "▼", fontSize = 12.sp) })
                Box(Modifier.matchParentSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { menuFiltroEstadoAbierto = true }))
                DropdownMenu(expanded = menuFiltroEstadoAbierto, onDismissRequest = { menuFiltroEstadoAbierto = false }) {
                    estadosFiltro.forEach { estado -> DropdownMenuItem(text = { Text(estado) }, onClick = { filtroEstado = estado; menuFiltroEstadoAbierto = false }) }
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(value = filtroPrioridad, onValueChange = {}, readOnly = true, label = { Text("Prioridad") }, modifier = Modifier.fillMaxWidth(), singleLine = true, trailingIcon = { Text(if (menuFiltroPrioridadAbierto) "▲" else "▼", fontSize = 12.sp) })
                Box(Modifier.matchParentSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { menuFiltroPrioridadAbierto = true }))
                DropdownMenu(expanded = menuFiltroPrioridadAbierto, onDismissRequest = { menuFiltroPrioridadAbierto = false }) {
                    prioridadesFiltro.forEach { prioridad -> DropdownMenuItem(text = { Text(prioridad) }, onClick = { filtroPrioridad = prioridad; menuFiltroPrioridadAbierto = false }) }
                }
            }
        }

        // Fila 2: Tipo y Organización
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(value = filtroTipo, onValueChange = {}, readOnly = true, label = { Text("Tipo") }, modifier = Modifier.fillMaxWidth(), singleLine = true, trailingIcon = { Text(if (menuFiltroTipoAbierto) "▲" else "▼", fontSize = 12.sp) })
                Box(Modifier.matchParentSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { menuFiltroTipoAbierto = true }))
                DropdownMenu(expanded = menuFiltroTipoAbierto, onDismissRequest = { menuFiltroTipoAbierto = false }) {
                    tiposFiltro.forEach { tipo -> DropdownMenuItem(text = { Text(tipo) }, onClick = { filtroTipo = tipo; menuFiltroTipoAbierto = false }) }
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(value = filtroOrganizacion, onValueChange = {}, readOnly = true, label = { Text("Organización") }, modifier = Modifier.fillMaxWidth(), singleLine = true, trailingIcon = { Text(if (menuFiltroOrgAbierto) "▲" else "▼", fontSize = 12.sp) })
                Box(Modifier.matchParentSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { menuFiltroOrgAbierto = true }))
                DropdownMenu(expanded = menuFiltroOrgAbierto, onDismissRequest = { menuFiltroOrgAbierto = false }) {
                    organizacionesFiltro.forEach { org -> DropdownMenuItem(text = { Text(org) }, onClick = { filtroOrganizacion = org; menuFiltroOrgAbierto = false }) }
                }
            }
        }

        // Fila 3: Filtro por Fecha de Detección (Criterio 21) con formato automático DD/MM/AAAA
        OutlinedTextField(
            value = filtroFecha,
            onValueChange = { nuevoTexto ->
                filtroFecha = nuevoTexto.filter { it.isDigit() }.take(8)
            },
            label = { Text("Filtrar por Fecha (DD/MM/AAAA)") },
            placeholder = { Text("Ej: 15102026") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = FechaVisualTransformation(),
            trailingIcon = {
                if (filtroFecha.isNotBlank()) {
                    TextButton(onClick = { filtroFecha = "" }) {
                        Text("Limpiar", fontSize = 12.sp)
                    }
                }
            }
        )

        Text(text = "Oportunidades encontradas: ${oportunidadesFiltradas.size}", style = MaterialTheme.typography.titleSmall)

        if (oportunidadesFiltradas.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No hay oportunidades que coincidan con el criterio.")
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items = oportunidadesFiltradas, key = { it.id }) { oportunidad ->
                    TarjetaOportunidad(
                        oportunidad = oportunidad,
                        esSupervisor = esSupervisor,
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(300), placementSpec = tween(300), fadeOutSpec = tween(300)
                        ),
                        onEditar = {
                            viewModel.cargarOportunidadParaEditar(oportunidad)
                            onEditarOportunidad(oportunidad)
                        },
                        onVerSeguimiento = { onVerSeguimiento(oportunidad) },
                        onEliminar = { viewModel.solicitarEliminacion(oportunidad) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedButton(onClick = onVolverFormulario, modifier = Modifier.fillMaxWidth()) {
            Text("Crear nueva oportunidad")
        }

        OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al panel")
        }
    }

    uiState.oportunidadPorEliminar?.let { oportunidad ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelarEliminacion() },
            title = { Text("Eliminar oportunidad") },
            text = { Text("¿Deseas eliminar definitivamente la oportunidad \"${oportunidad.nombre}\"?") },
            confirmButton = { TextButton(onClick = { viewModel.confirmarEliminacion() }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { viewModel.cancelarEliminacion() }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun TarjetaOportunidad(
    oportunidad: Oportunidad,
    esSupervisor: Boolean,
    modifier: Modifier = Modifier,
    onEditar: () -> Unit,
    onVerSeguimiento: () -> Unit,
    onEliminar: () -> Unit
) {
    val colores = MaterialTheme.coloresOportunidad

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = oportunidad.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(text = oportunidad.prioridad, fontWeight = FontWeight.Bold, color = when (oportunidad.prioridad) {
                    "Alta" -> colores.prioridadAlta
                    "Media" -> colores.prioridadMedia
                    else -> colores.prioridadBaja
                })
            }

            Text("Tipo: ${oportunidad.tipo}", style = MaterialTheme.typography.bodySmall)

            // Mostramos la fecha formateada visualmente con barras (DD/MM/AAAA)
            if (oportunidad.fechaDeteccion.isNotBlank()) {
                Text("Detectada el: ${formatearFechaVisual(oportunidad.fechaDeteccion)}", style = MaterialTheme.typography.bodySmall)
            }

            Text(text = "Estado: ${oportunidad.estado}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, color = when (oportunidad.estado) {
                "Participación confirmada" -> colores.estadoConfirmado
                "Oportunidad descartada" -> colores.estadoDescartado
                "En espera de respuesta" -> colores.estadoEnEspera
                else -> MaterialTheme.colorScheme.onSurface
            })
            Text("Potencial: ${oportunidad.potencial}", style = MaterialTheme.typography.bodySmall)
            Text("Responsable: ${oportunidad.responsable}", style = MaterialTheme.typography.bodySmall)

            if (oportunidad.observaciones.isNotBlank()) {
                Text("Obs: ${oportunidad.observaciones}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onVerSeguimiento, modifier = Modifier.fillMaxWidth()) {
                Text("Seguimiento y Calendario", fontSize = 13.sp)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEditar, modifier = Modifier.weight(1f)) { Text("Editar", fontSize = 12.sp) }
                if (esSupervisor) {
                    OutlinedButton(onClick = onEliminar, modifier = Modifier.weight(1f)) { Text("Eliminar", fontSize = 12.sp) }
                }
            }
        }
    }
}