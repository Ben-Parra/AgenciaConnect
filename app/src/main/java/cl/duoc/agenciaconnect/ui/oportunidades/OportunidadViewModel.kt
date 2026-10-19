package cl.duoc.agenciaconnect.ui.oportunidades

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaconnect.data.local.AgenciaDatabase
import cl.duoc.agenciaconnect.data.model.Oportunidad
import cl.duoc.agenciaconnect.data.repository.EventoRepository
import cl.duoc.agenciaconnect.data.repository.OportunidadRepository
import cl.duoc.agenciaconnect.data.repository.OrganizacionRepository
import cl.duoc.agenciaconnect.util.validarEstadoOportunidad
import cl.duoc.agenciaconnect.util.validarFechaDeteccion
import cl.duoc.agenciaconnect.util.validarNombreOportunidad
import cl.duoc.agenciaconnect.util.validarOrganizacionOportunidad
import cl.duoc.agenciaconnect.util.validarPotencialOportunidad
import cl.duoc.agenciaconnect.util.validarPrioridadOportunidad
import cl.duoc.agenciaconnect.util.validarResponsableOportunidad
import cl.duoc.agenciaconnect.util.validarTipoOportunidad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Con este ViewModel coordinamos el formulario, las validaciones y las operaciones CRUD de oportunidades.
class OportunidadViewModel(application: Application) : AndroidViewModel(application) {

    // Con estas dependencias accedemos a las bases de datos de Room y a sus repositorios.
    private val database = AgenciaDatabase.obtenerInstancia(application)
    private val oportunidadRepository = OportunidadRepository(database.oportunidadDao())
    private val organizacionRepository = OrganizacionRepository(database.organizacionDao())
    private val eventoRepository = EventoRepository(database.eventoDao())

    private val usuarioRepository = cl.duoc.agenciaconnect.data.repository.UsuarioRepository(database.usuarioDao())

    // Con este flujo mantenemos el estado privado y observable de la interfaz.
    private val _uiState = MutableStateFlow(OportunidadUiState())
    val uiState: StateFlow<OportunidadUiState> = _uiState.asStateFlow()

    // ── VARIABLES DE FILTROS AVANZADOS (Criterios 21, 22, 23, 24) ──
    var filtroTexto by mutableStateOf("")
    var filtroTipo by mutableStateOf("")
    var filtroOrganizacion by mutableStateOf("")
    var filtroUbicacion by mutableStateOf("")

    init {
        // Con esta corrutina observamos las oportunidades registradas en la base de datos.
        viewModelScope.launch {
            oportunidadRepository.oportunidades.collect { listaOportunidades ->
                _uiState.update { it.copy(oportunidades = listaOportunidades) }
            }
        }

        // Con esta corrutina cargamos las organizaciones para alimentar el selector desplegable.
        viewModelScope.launch {
            organizacionRepository.organizaciones.collect { listaOrganizaciones ->
                _uiState.update { it.copy(organizaciones = listaOrganizaciones) }
            }
        }

        // Con esta corrutina cargamos los eventos para vincularlos opcionalmente a la oportunidad.
        viewModelScope.launch {
            eventoRepository.eventos.collect { listaEventos ->
                _uiState.update { it.copy(eventos = listaEventos) }
            }
        }

        // Con esta corrutina cargamos los usuarios con rol EJECUTIVO para el combobox del supervisor.
        viewModelScope.launch {
            usuarioRepository.usuarios.collect { listaUsuarios ->
                val soloEjecutivos = listaUsuarios.filter { it.rol == "EJECUTIVO" }
                _uiState.update { it.copy(ejecutivos = soloEjecutivos) }
            }
        }
    }

    // Con estas funciones actualizamos cada valor ingresado o seleccionado en el formulario.
    fun cambiarNombre(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = false) }
    }

    fun cambiarTipo(valor: String) {
        _uiState.update { it.copy(tipo = valor, errorTipo = false) }
    }

    fun cambiarOrganizacion(organizacionId: Long?) {
        _uiState.update { it.copy(organizacionId = organizacionId, errorOrganizacion = false) }
    }

    fun cambiarEvento(eventoId: Long) {
        _uiState.update { it.copy(eventoId = eventoId) }
    }

    fun cambiarPrioridad(valor: String) {
        _uiState.update { it.copy(prioridad = valor, errorPrioridad = false) }
    }

    fun cambiarPotencial(valor: String) {
        _uiState.update { it.copy(potencial = valor, errorPotencial = false) }
    }

    fun cambiarEstado(valor: String) {
        _uiState.update { it.copy(estado = valor, errorEstado = false) }
    }

    fun cambiarResponsable(valor: String) {
        _uiState.update { it.copy(responsable = valor, errorResponsable = false) }
    }

    fun cambiarObservaciones(valor: String) {
        _uiState.update { it.copy(observaciones = valor) }
    }

    // Con esta función validamos los campos obligatorios y guardamos o actualizamos la oportunidad.
    fun guardarOportunidad() {
        val estado = _uiState.value

        val nombreValido = validarNombreOportunidad(estado.nombre)
        val tipoValido = validarTipoOportunidad(estado.tipo)
        val organizacionValida = validarOrganizacionOportunidad(estado.organizacionId)
        val prioridadValida = validarPrioridadOportunidad(estado.prioridad)
        val potencialValido = validarPotencialOportunidad(estado.potencial)
        val estadoValido = validarEstadoOportunidad(estado.estado)
        val responsableValido = validarResponsableOportunidad(estado.responsable)
        val fechaValida = validarFechaDeteccion(estado.fechaDeteccion)

        _uiState.update {
            it.copy(
                errorNombre = !nombreValido,
                errorTipo = !tipoValido,
                errorOrganizacion = !organizacionValida,
                errorPrioridad = !prioridadValida,
                errorPotencial = !potencialValido,
                errorEstado = !estadoValido,
                errorResponsable = !responsableValido,
                mensaje = null,
                errorFechaDeteccion = !fechaValida,
            )
        }

        if (!nombreValido || !tipoValido || !organizacionValida ||
            !prioridadValida || !potencialValido || !estadoValido || !responsableValido || !fechaValida
        ) {
            _uiState.update { it.copy(mensaje = "Revisa los campos obligatorios y la fecha de detección.") }
            return
        }

        val organizacionExiste = estado.organizaciones.any { it.id == estado.organizacionId }
        if (!organizacionExiste) {
            _uiState.update {
                it.copy(
                    errorOrganizacion = true,
                    mensaje = "Debes seleccionar una organización válida registrada."
                )
            }
            return
        }

        // Con este bloque construimos el objeto y ejecutamos la inserción o actualización asíncrona.
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensaje = null) }

            try {
                val oportunidad = Oportunidad(
                    id = estado.idEnEdicion ?: 0L,
                    nombre = estado.nombre.trim(),
                    tipo = estado.tipo.trim(),
                    organizacionId = estado.organizacionId!!,
                    eventoId = estado.eventoId,
                    prioridad = estado.prioridad.trim(),
                    potencial = estado.potencial.trim(),
                    estado = estado.estado.trim(),
                    responsable = estado.responsable.trim(),
                    observaciones = estado.observaciones.trim(),
                    fechaDeteccion = estado.fechaDeteccion.trim()
                )

                if (estado.idEnEdicion == null) {
                    oportunidadRepository.insertarOportunidad(oportunidad)
                    _uiState.update { it.copy(mensaje = "Oportunidad registrada correctamente.") }
                } else {
                    oportunidadRepository.actualizarOportunidad(oportunidad)
                    _uiState.update { it.copy(mensaje = "Oportunidad actualizada correctamente.") }
                }

                val mensajeExito = _uiState.value.mensaje
                limpiarFormulario()
                _uiState.update { it.copy(mensaje = mensajeExito) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(mensaje = "No se pudo guardar la oportunidad. Inténtalo nuevamente.")
                }
            } finally {
                _uiState.update { it.copy(guardando = false) }
            }
        }
    }

    // Con esta función preparamos los datos de una oportunidad existente para su edición.
    fun editarOportunidad(oportunidad: Oportunidad) {
        _uiState.update {
            it.copy(
                nombre = oportunidad.nombre,
                tipo = oportunidad.tipo,
                organizacionId = oportunidad.organizacionId,
                eventoId = oportunidad.eventoId,
                prioridad = oportunidad.prioridad,
                potencial = oportunidad.potencial,
                estado = oportunidad.estado,
                responsable = oportunidad.responsable,
                observaciones = oportunidad.observaciones,
                idEnEdicion = oportunidad.id,
                errorNombre = false,
                errorTipo = false,
                errorOrganizacion = false,
                errorPrioridad = false,
                errorPotencial = false,
                errorEstado = false,
                errorResponsable = false,
                oportunidadPorEliminar = null,
                mensaje = null,
                fechaDeteccion = oportunidad.fechaDeteccion,
                errorFechaDeteccion = false,
            )
        }
    }

    // Con esta función cancelamos la edición en curso y limpiamos los campos.
    fun cancelarEdicion() {
        limpiarFormulario()
    }

    // Con esta función marcamos la oportunidad que se desea eliminar para mostrar el diálogo.
    fun solicitarEliminacion(oportunidad: Oportunidad) {
        _uiState.update { it.copy(oportunidadPorEliminar = oportunidad, mensaje = null) }
    }

    // Con esta función cancelamos el diálogo sin borrar la oportunidad.
    fun cancelarEliminacion() {
        _uiState.update { it.copy(oportunidadPorEliminar = null) }
    }

    // Con esta función eliminamos definitivamente la oportunidad confirmada por el usuario.
    fun confirmarEliminacion() {
        val oportunidad = _uiState.value.oportunidadPorEliminar ?: return

        viewModelScope.launch {
            try {
                oportunidadRepository.eliminarOportunidad(oportunidad)
                _uiState.update {
                    it.copy(
                        oportunidadPorEliminar = null,
                        mensaje = "Oportunidad eliminada correctamente."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        oportunidadPorEliminar = null,
                        mensaje = "No se pudo eliminar la oportunidad."
                    )
                }
            }
        }
    }

    // Con esta función restablecemos los campos del formulario y el modo de edición.
    private fun limpiarFormulario() {
        _uiState.update {
            it.copy(
                nombre = "",
                tipo = "",
                organizacionId = null,
                eventoId = 0L,
                prioridad = "",
                potencial = "",
                estado = "Evento identificado",
                responsable = "",
                observaciones = "",
                errorNombre = false,
                errorTipo = false,
                errorOrganizacion = false,
                errorPrioridad = false,
                errorPotencial = false,
                errorEstado = false,
                errorResponsable = false,
                idEnEdicion = null,
                fechaDeteccion = "",
                errorFechaDeteccion = false,
            )
        }
    }

    // Con esta función reiniciamos el formulario cuando el usuario abandona la pantalla.
    fun reiniciarFormularioAlSalir() {
        limpiarFormulario()
        _uiState.update { it.copy(mensaje = null) }
    }

    // Con esta función preparamos los datos de la oportunidad para editarla en el formulario.
    fun cargarOportunidadParaEditar(oportunidad: Oportunidad) {
        editarOportunidad(oportunidad)
    }

    // Con esta función seleccionamos la oportunidad sobre la que se pide confirmación de eliminación.
    fun solicitarEliminar(oportunidad: Oportunidad) {
        solicitarEliminacion(oportunidad)
    }

    // Con esta función cancelamos el diálogo de confirmación.
    fun cancelarEliminar() {
        cancelarEliminacion()
    }

    // Con esta función conservamos solo los dígitos de la fecha de detección
    // y limitamos su longitud a ocho caracteres.
    fun cambiarFechaDeteccion(nuevaFecha: String) {
        val fechaLimpia = nuevaFecha
            .filter { it.isDigit() }
            .take(8)

        _uiState.update {
            it.copy(
                fechaDeteccion = fechaLimpia,
                errorFechaDeteccion = false
            )
        }
    }

}