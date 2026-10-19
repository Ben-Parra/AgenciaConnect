package cl.duoc.agenciaconnect.ui.oportunidades

import cl.duoc.agenciaconnect.util.FechaVisualTransformation
import cl.duoc.agenciaconnect.data.model.Usuario
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// Con esta pantalla mostramos el formulario completo para registrar y editar oportunidades comerciales.
@Composable
fun OportunidadesScreen(
    usuarioActual: Usuario? = null,
    onVolver: () -> Unit,
    onVerOportunidades: () -> Unit,
    viewModel: OportunidadViewModel = viewModel()
) {
    // Con este estado observamos los campos del formulario, listas auxiliares y errores de validación.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Con esta variable identificamos si el usuario activo tiene rol de supervisión.
    val esSupervisor = usuarioActual?.rol == "SUPERVISOR"

    // Si el usuario es un ejecutivo y el responsable aún está en blanco, se asigna automáticamente en el estado.
    androidx.compose.runtime.LaunchedEffect(usuarioActual) {
        if (!esSupervisor && uiState.responsable.isBlank() && usuarioActual != null) {
            viewModel.cambiarResponsable(usuarioActual.nombre)
        }
    }

    // Con este efecto reiniciamos el formulario al salir de la pantalla para evitar datos residuales.
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.reiniciarFormularioAlSalir()
        }
    }

    // Con estas variables controlamos la apertura de cada selector desplegable.
    var menuOrganizacionesAbierto by remember { mutableStateOf(false) }
    var menuEventosAbierto by remember { mutableStateOf(false) }
    var menuTiposAbierto by remember { mutableStateOf(false) }
    var menuPrioridadAbierto by remember { mutableStateOf(false) }
    var menuPotencialAbierto by remember { mutableStateOf(false) }
    var menuEstadoAbierto by remember { mutableStateOf(false) }
    var menuResponsableAbierto by remember { mutableStateOf(false) }

    // Con estas listas definimos las opciones estandarizadas según el caso de estudio.
    val tiposOportunidad = listOf(
        "Evento corporativo",
        "Feria empresarial",
        "Actividad municipal",
        "Jornada de beneficios",
        "Feria institucional",
        "Activación comercial",
        "Otra instancia"
    )

    val prioridades = listOf("Alta", "Media", "Baja")
    val potenciales = listOf("Alto", "Medio", "Bajo")

    // REGLA DE NEGOCIO: El ejecutivo no puede confirmar ni descartar definitivamente oportunidades.
    val estados = if (esSupervisor) {
        listOf(
            "Evento identificado",
            "Contacto pendiente",
            "Contacto realizado",
            "Propuesta enviada",
            "En espera de respuesta",
            "Participación confirmada",
            "Oportunidad descartada"
        )
    } else {
        listOf(
            "Evento identificado",
            "Contacto pendiente",
            "Contacto realizado",
            "Propuesta enviada",
            "En espera de respuesta"
        )
    }

    // Con estas variables identificamos los nombres asociados a las entidades seleccionadas.
    val organizacionSeleccionada = uiState.organizaciones.find { it.id == uiState.organizacionId }
    val eventoSeleccionado = uiState.eventos.find { it.id == uiState.eventoId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Con este título indicamos visualmente si estamos registrando o modificando.
        Text(
            text = if (uiState.idEnEdicion == null) "Registrar oportunidad" else "Editar oportunidad",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text("Ingresa los antecedentes de la oportunidad comercial y asigna su seguimiento.")

        Spacer(modifier = Modifier.height(8.dp))

        // Con este campo ingresamos el nombre descriptivo de la oportunidad.
        OutlinedTextField(
            value = uiState.nombre,
            onValueChange = viewModel::cambiarNombre,
            label = { Text("Nombre de la oportunidad") },
            placeholder = { Text("Ej.: Activación Feria Minera 2026") },
            isError = uiState.errorNombre,
            supportingText = {
                if (uiState.errorNombre) Text("El nombre de la oportunidad es obligatorio.")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // ── NUEVO CAMPO: Fecha de Detección (Validación: Hoy o anterior) ──
        OutlinedTextField(
            value = uiState.fechaDeteccion,
            onValueChange = viewModel::cambiarFechaDeteccion,
            label = { Text("Fecha de detección (DD/MM/AAAA)") },
            placeholder = { Text("Ej: 15102026") },
            isError = uiState.errorFechaDeteccion,
            supportingText = {
                if (uiState.errorFechaDeteccion) {
                    Text("Debe ser una fecha válida de hoy o anterior")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            visualTransformation = FechaVisualTransformation() // <--- ¡Esta es la magia que pone las barras automáticamente!
        )

        // Con este combobox seleccionamos la organización registrada en Room.
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = organizacionSeleccionada?.nombre ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Organización responsable") },
                placeholder = { Text("Selecciona una organización") },
                isError = uiState.errorOrganizacion,
                supportingText = {
                    if (uiState.errorOrganizacion) Text("Debes seleccionar una organización registrada.")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = { Text(if (menuOrganizacionesAbierto) "▲" else "▼") }
            )

            Box(
                Modifier
                    .matchParentSize()
                    .padding(end = 48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { menuOrganizacionesAbierto = true }
                    )
            )

            DropdownMenu(
                expanded = menuOrganizacionesAbierto,
                onDismissRequest = { menuOrganizacionesAbierto = false }
            ) {
                if (uiState.organizaciones.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No hay organizaciones registradas") },
                        onClick = { menuOrganizacionesAbierto = false }
                    )
                } else {
                    uiState.organizaciones.forEach { org ->
                        DropdownMenuItem(
                            text = { Text(org.nombre) },
                            onClick = {
                                viewModel.cambiarOrganizacion(org.id)
                                menuOrganizacionesAbierto = false
                            }
                        )
                    }
                }
            }
        }

        // Con este combobox seleccionamos el tipo de oportunidad predefinido.
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.tipo,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de oportunidad") },
                placeholder = { Text("Selecciona un tipo") },
                isError = uiState.errorTipo,
                supportingText = {
                    if (uiState.errorTipo) Text("El tipo de oportunidad es obligatorio.")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = { Text(if (menuTiposAbierto) "▲" else "▼") }
            )

            Box(
                Modifier
                    .matchParentSize()
                    .padding(end = 48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { menuTiposAbierto = true }
                    )
            )

            DropdownMenu(
                expanded = menuTiposAbierto,
                onDismissRequest = { menuTiposAbierto = false }
            ) {
                tiposOportunidad.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo) },
                        onClick = {
                            viewModel.cambiarTipo(tipo)
                            menuTiposAbierto = false
                        }
                    )
                }
            }
        }

        // Con este combobox vinculamos opcionalmente un evento registrado en Room.
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = eventoSeleccionado?.nombre ?: if (uiState.eventoId == 0L) "Sin evento específico" else "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Evento asociado (opcional)") },
                placeholder = { Text("Selecciona un evento si aplica") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = { Text(if (menuEventosAbierto) "▲" else "▼") }
            )

            Box(
                Modifier
                    .matchParentSize()
                    .padding(end = 48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { menuEventosAbierto = true }
                    )
            )

            DropdownMenu(
                expanded = menuEventosAbierto,
                onDismissRequest = { menuEventosAbierto = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Sin evento específico") },
                    onClick = {
                        viewModel.cambiarEvento(0L)
                        menuEventosAbierto = false
                    }
                )
                uiState.eventos.forEach { evento ->
                    DropdownMenuItem(
                        text = { Text(evento.nombre) },
                        onClick = {
                            viewModel.cambiarEvento(evento.id)
                            menuEventosAbierto = false
                        }
                    )
                }
            }
        }

        // Con este combobox seleccionamos el nivel de prioridad.
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.prioridad,
                onValueChange = {},
                readOnly = true,
                label = { Text("Prioridad") },
                placeholder = { Text("Selecciona prioridad") },
                isError = uiState.errorPrioridad,
                supportingText = {
                    if (uiState.errorPrioridad) Text("La prioridad es obligatoria.")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = { Text(if (menuPrioridadAbierto) "▲" else "▼") }
            )

            Box(
                Modifier
                    .matchParentSize()
                    .padding(end = 48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { menuPrioridadAbierto = true }
                    )
            )

            DropdownMenu(
                expanded = menuPrioridadAbierto,
                onDismissRequest = { menuPrioridadAbierto = false }
            ) {
                prioridades.forEach { p ->
                    DropdownMenuItem(
                        text = { Text(p) },
                        onClick = {
                            viewModel.cambiarPrioridad(p)
                            menuPrioridadAbierto = false
                        }
                    )
                }
            }
        }

        // Con este combobox definimos el potencial comercial.
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.potencial,
                onValueChange = {},
                readOnly = true,
                label = { Text("Potencial comercial estimado") },
                placeholder = { Text("Selecciona potencial") },
                isError = uiState.errorPotencial,
                supportingText = {
                    if (uiState.errorPotencial) Text("El potencial comercial es obligatorio.")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = { Text(if (menuPotencialAbierto) "▲" else "▼") }
            )

            Box(
                Modifier
                    .matchParentSize()
                    .padding(end = 48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { menuPotencialAbierto = true }
                    )
            )

            DropdownMenu(
                expanded = menuPotencialAbierto,
                onDismissRequest = { menuPotencialAbierto = false }
            ) {
                potenciales.forEach { pot ->
                    DropdownMenuItem(
                        text = { Text(pot) },
                        onClick = {
                            viewModel.cambiarPotencial(pot)
                            menuPotencialAbierto = false
                        }
                    )
                }
            }
        }

        // Con este combobox asignamos la etapa o estado de la gestión.
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.estado,
                onValueChange = {},
                readOnly = true,
                label = { Text("Estado de la gestión") },
                isError = uiState.errorEstado,
                supportingText = {
                    if (uiState.errorEstado) Text("El estado de la gestión es obligatorio.")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = { Text(if (menuEstadoAbierto) "▲" else "▼") }
            )

            Box(
                Modifier
                    .matchParentSize()
                    .padding(end = 48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { menuEstadoAbierto = true }
                    )
            )

            DropdownMenu(
                expanded = menuEstadoAbierto,
                onDismissRequest = { menuEstadoAbierto = false }
            ) {
                estados.forEach { est ->
                    DropdownMenuItem(
                        text = { Text(est) },
                        onClick = {
                            viewModel.cambiarEstado(est)
                            menuEstadoAbierto = false
                        }
                    )
                }
            }
        }

        // Si es Supervisor, mostramos un ComboBox con los ejecutivos registrados.
        // Si es Ejecutivo, mostramos el campo bloqueado con su propio nombre.
        if (esSupervisor) {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.responsable.ifBlank { "Selecciona un ejecutivo responsable" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Asignar a Ejecutivo Responsable") },
                    isError = uiState.errorResponsable,
                    supportingText = {
                        if (uiState.errorResponsable) {
                            Text("Debes asignar un ejecutivo a la oportunidad.")
                        } else {
                            Text("Selecciona el ejecutivo comercial a cargo.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = { Text(if (menuResponsableAbierto) "▲" else "▼") }
                )

                Box(
                    Modifier
                        .matchParentSize()
                        .padding(end = 48.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { menuResponsableAbierto = true }
                        )
                )

                DropdownMenu(
                    expanded = menuResponsableAbierto,
                    onDismissRequest = { menuResponsableAbierto = false }
                ) {
                    if (uiState.ejecutivos.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay ejecutivos registrados") },
                            onClick = { menuResponsableAbierto = false }
                        )
                    } else {
                        val ejecutivosOrdenados = uiState.ejecutivos.sortedBy { ejecutivo ->
                            when (ejecutivo.correo) {
                                "carlos.comercial@agencia.cl" -> 1
                                "marcela.ventas@agencia.cl" -> 2
                                "felipe.convenios@agencia.cl" -> 3
                                else -> 4
                            }
                        }

                        ejecutivosOrdenados.forEach { ejecutivo ->
                            val rolEtiqueta = when (ejecutivo.correo) {
                                "carlos.comercial@agencia.cl" -> "Ejecutivo 1"
                                "marcela.ventas@agencia.cl" -> "Ejecutivo 2"
                                "felipe.convenios@agencia.cl" -> "Ejecutivo 3"
                                else -> "Ejecutivo"
                            }
                            val etiqueta = "${ejecutivo.nombre} - $rolEtiqueta"

                            DropdownMenuItem(
                                text = { Text(etiqueta) },
                                onClick = {
                                    viewModel.cambiarResponsable(ejecutivo.nombre)
                                    menuResponsableAbierto = false
                                }
                            )
                        }
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = uiState.responsable,
                onValueChange = {},
                readOnly = true,
                label = { Text("Responsable asignado") },
                supportingText = {
                    Text("Asignado automáticamente al ejecutivo en sesión.")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // Con este campo agregamos notas adicionales u observaciones.
        OutlinedTextField(
            value = uiState.observaciones,
            onValueChange = viewModel::cambiarObservaciones,
            label = { Text("Observaciones (opcional)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            minLines = 3,
            maxLines = 4
        )

        // Con este bloque animado mostramos mensajes informativos de éxito o error.
        AnimatedVisibility(
            visible = uiState.mensaje != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            uiState.mensaje?.let { mensaje ->
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (
                        mensaje.startsWith("No se pudo") ||
                        mensaje.startsWith("Revisa") ||
                        mensaje.startsWith("Debes")
                    ) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Con este botón guardamos la nueva oportunidad o actualizamos los cambios.
        val interactionSourceGuardar = remember { MutableInteractionSource() }
        val presionadoGuardar by interactionSourceGuardar.collectIsPressedAsState()
        val escalaGuardar by animateFloatAsState(
            targetValue = if (presionadoGuardar) 0.96f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "Escala del botón guardar oportunidad"
        )

        Button(
            onClick = { viewModel.guardarOportunidad() },
            enabled = !uiState.guardando,
            interactionSource = interactionSourceGuardar,
            modifier = Modifier
                .fillMaxWidth()
                .scale(escalaGuardar)
        ) {
            Text(
                when {
                    uiState.guardando -> "Guardando..."
                    uiState.idEnEdicion == null -> "Guardar oportunidad"
                    else -> "Guardar cambios"
                }
            )
        }

        // Con este botón cancelamos la edición sin afectar la base de datos.
        if (uiState.idEnEdicion != null) {
            OutlinedButton(
                onClick = { viewModel.cancelarEdicion() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar edición")
            }
        }

        // Con este botón navegamos a la pantalla de listado de oportunidades registradas.
        OutlinedButton(
            onClick = onVerOportunidades,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver oportunidades (${uiState.oportunidades.size})")
        }

        // Con este botón volvemos al panel administrativo principal.
        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Volver al panel")
        }
    }
}