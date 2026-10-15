
package cl.duoc.agenciaconnect.ui.eventos

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import cl.duoc.agenciaconnect.data.local.AgenciaDatabase
import cl.duoc.agenciaconnect.data.local.EventoRepository
import cl.duoc.agenciaconnect.data.model.Evento
import cl.duoc.agenciaconnect.util.validarNombreEvento
import cl.duoc.agenciaconnect.util.validarDescripcionEvento
import cl.duoc.agenciaconnect.util.validarFechaEvento
import cl.duoc.agenciaconnect.util.validarUbicacionEvento
import cl.duoc.agenciaconnect.util.validarOrganizacionEvento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Con esta clase coordinamos el formulario, las validaciones
// y las operaciones CRUD utilizando el repositorio de Room.
class EventoViewModel(application: Application) : AndroidViewModel(application) {

    // Con esta propiedad obtenemos el acceso a la base de datos local.
    private val repositorio = EventoRepository(
        AgenciaDatabase.obtenerInstancia(application).eventoDao()
    )

    // Con este estado privado administramos los cambios internos.
    private val _uiState = MutableStateFlow(EventoUiState())

    // Con este estado público permitimos que la interfaz observe los cambios.
    val uiState: StateFlow<EventoUiState> = _uiState.asStateFlow()

    init {
        // Con esta corrutina observamos los eventos guardados en Room
        // y actualizamos automáticamente la lista de la interfaz.
        viewModelScope.launch {
            repositorio.eventos.collect { eventosGuardados ->
                _uiState.update {
                    it.copy(eventos = eventosGuardados)
                }
            }
        }
    }

    // Con esta función actualizamos el nombre y limpiamos su error anterior.
    fun cambiarNombre(nuevoNombre: String) {
        _uiState.update {
            it.copy(nombre = nuevoNombre, errorNombre = false)
        }
    }

    // Con esta función actualizamos la descripción y limpiamos su error.
    fun cambiarDescripcion(nuevaDescripcion: String) {
        _uiState.update {
            it.copy(
                descripcion = nuevaDescripcion,
                errorDescripcion = false
            )
        }
    }

    // Con esta función conservamos solo los dígitos de la fecha
    // y limitamos su longitud a ocho caracteres.
    fun cambiarFecha(nuevaFecha: String) {
        val fechaLimpia = nuevaFecha
            .filter { it.isDigit() }
            .take(8)

        _uiState.update {
            it.copy(fecha = fechaLimpia, errorFecha = false)
        }
    }

    // Con esta función actualizamos la ubicación del evento.
    fun cambiarUbicacion(nuevaUbicacion: String) {
        _uiState.update {
            it.copy(
                ubicacion = nuevaUbicacion,
                errorUbicacion = false
            )
        }
    }

    // Con esta función actualizamos la organización asociada al evento.
    fun cambiarOrganizacion(nuevaOrganizacion: String) {
        _uiState.update {
            it.copy(
                organizacion = nuevaOrganizacion,
                errorOrganizacion = false
            )
        }
    }

    // Con esta función validamos los campos y guardamos sus errores
    // para que la interfaz pueda mostrarlos individualmente.
    fun validarFormulario(): Boolean {
        val estado = _uiState.value

        val errorNombre = !validarNombreEvento(estado.nombre)
        val errorDescripcion =
            !validarDescripcionEvento(estado.descripcion)
        val errorFecha = !validarFechaEvento(estado.fecha)
        val errorUbicacion =
            !validarUbicacionEvento(estado.ubicacion)
        val errorOrganizacion =
            !validarOrganizacionEvento(estado.organizacion)

        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorDescripcion = errorDescripcion,
                errorFecha = errorFecha,
                errorUbicacion = errorUbicacion,
                errorOrganizacion = errorOrganizacion
            )
        }

        return !errorNombre &&
                !errorDescripcion &&
                !errorFecha &&
                !errorUbicacion &&
                !errorOrganizacion
    }

    // Con esta función creamos o actualizamos un evento en Room
    // después de comprobar que todos los campos sean válidos.
    fun guardarEvento() {
        if (!validarFormulario()) return

        val estado = _uiState.value

        val evento = Evento(
            id = estado.idEnEdicion ?: 0L,
            nombre = estado.nombre.trim(),
            descripcion = estado.descripcion.trim(),
            fecha = estado.fecha,
            ubicacion = estado.ubicacion.trim(),
            organizacion = estado.organizacion.trim()
        )

        // Con esta corrutina ejecutamos la operación correspondiente
        // sin bloquear la interfaz mientras Room guarda los datos.
        viewModelScope.launch {
            if (estado.idEnEdicion == null) {
                repositorio.insertarEvento(evento)
            } else {
                repositorio.actualizarEvento(evento)
            }
        }

        // Con esta actualización limpiamos el formulario después de guardar.
        // La lista se actualizará cuando Room emita los nuevos registros.
        limpiarFormulario()
    }

    // Con esta función cargamos los datos de un evento
    // para modificarlos desde el formulario.
    fun cargarEventoParaEditar(evento: Evento) {
        _uiState.update {
            it.copy(
                nombre = evento.nombre,
                descripcion = evento.descripcion,
                fecha = evento.fecha,
                ubicacion = evento.ubicacion,
                organizacion = evento.organizacion,
                errorNombre = false,
                errorDescripcion = false,
                errorFecha = false,
                errorUbicacion = false,
                errorOrganizacion = false,
                idEnEdicion = evento.id
            )
        }
    }

    // Con esta función cancelamos la edición y limpiamos el formulario.
    fun cancelarEdicion() {
        limpiarFormulario()
    }

    // Con esta función seleccionamos el evento que queremos eliminar
    // para que la interfaz pueda solicitar una confirmación.
    fun solicitarEliminar(evento: Evento) {
        _uiState.update {
            it.copy(eventoPorEliminar = evento)
        }
    }

    // Con esta función cerramos la confirmación sin eliminar el evento.
    fun cancelarEliminar() {
        _uiState.update {
            it.copy(eventoPorEliminar = null)
        }
    }

    // Con esta función eliminamos de Room el evento confirmado
    // y limpiamos el formulario si estábamos editando ese registro.
    fun confirmarEliminar() {
        val estado = _uiState.value
        val evento = estado.eventoPorEliminar ?: return
        val estabaEditando = estado.idEnEdicion == evento.id

        // Con esta corrutina eliminamos el registro de forma asíncrona.
        viewModelScope.launch {
            repositorio.eliminarEvento(evento)
        }

        _uiState.update {
            it.copy(
                eventoPorEliminar = null,
                nombre = if (estabaEditando) "" else it.nombre,
                descripcion = if (estabaEditando) "" else it.descripcion,
                fecha = if (estabaEditando) "" else it.fecha,
                ubicacion = if (estabaEditando) "" else it.ubicacion,
                organizacion = if (estabaEditando) "" else it.organizacion,
                errorNombre = if (estabaEditando) false else it.errorNombre,
                errorDescripcion = if (estabaEditando) false else it.errorDescripcion,
                errorFecha = if (estabaEditando) false else it.errorFecha,
                errorUbicacion = if (estabaEditando) false else it.errorUbicacion,
                errorOrganizacion = if (estabaEditando) false else it.errorOrganizacion,
                idEnEdicion = if (estabaEditando) null else it.idEnEdicion
            )
        }
    }

    // Con esta función limpiamos los campos, sus errores
    // y el identificador del evento que estábamos editando.
    private fun limpiarFormulario() {
        _uiState.update {
            it.copy(
                nombre = "",
                descripcion = "",
                fecha = "",
                ubicacion = "",
                organizacion = "",
                errorNombre = false,
                errorDescripcion = false,
                errorFecha = false,
                errorUbicacion = false,
                errorOrganizacion = false,
                idEnEdicion = null
            )
        }
    }
}
