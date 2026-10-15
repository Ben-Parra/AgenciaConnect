package cl.duoc.agenciaconnect.ui.eventos

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import cl.duoc.agenciaconnect.data.model.Evento
import cl.duoc.agenciaconnect.util.validarNombreEvento
import cl.duoc.agenciaconnect.util.validarDescripcionEvento
import cl.duoc.agenciaconnect.util.validarFechaEvento
import cl.duoc.agenciaconnect.util.validarUbicacionEvento
import cl.duoc.agenciaconnect.util.validarOrganizacionEvento

// Con esta clase coordinamos los datos del formulario,
// las validaciones y las operaciones CRUD de los eventos.
class EventoViewModel : ViewModel() {

    // Con este estado privado administramos los cambios internos.
    private val _uiState = MutableStateFlow(EventoUiState())

    // Con este estado público permitimos que la interfaz observe los cambios.
    val uiState: StateFlow<EventoUiState> = _uiState.asStateFlow()

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

    // Con esta función guardamos únicamente los dígitos de la fecha,
    // limitamos su longitud y limpiamos el error anterior.
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

    // Con esta función validamos cada campo y guardamos sus errores
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

    // Con esta función creamos un evento nuevo o actualizamos el que editamos,
    // siempre que todos los campos sean válidos.
    fun guardarEvento() {
        if (!validarFormulario()) return

        val estado = _uiState.value

        val evento = Evento(
            id = estado.idEnEdicion ?: estado.siguienteId,
            nombre = estado.nombre.trim(),
            descripcion = estado.descripcion.trim(),
            fecha = estado.fecha,
            ubicacion = estado.ubicacion.trim(),
            organizacion = estado.organizacion.trim()
        )

        val listaActualizada = if (estado.idEnEdicion == null) {
            estado.eventos + evento
        } else {
            estado.eventos.map {
                if (it.id == evento.id) evento else it
            }
        }

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
                eventos = listaActualizada,
                idEnEdicion = null,
                siguienteId = if (estado.idEnEdicion == null) {
                    estado.siguienteId + 1
                } else {
                    estado.siguienteId
                }
            )
        }
    }

    // Con esta función cargamos los datos del evento seleccionado
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

    // Con esta función cancelamos la edición y limpiamos el formulario,
    // conservando los eventos que ya están registrados.
    fun cancelarEdicion() {
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

    // Con esta función seleccionamos el evento que queremos eliminar
    // para que la interfaz pueda mostrar una confirmación.
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

    // Con esta función eliminamos el evento confirmado y mantenemos
    // el resto de los registros y el siguiente identificador.
    fun confirmarEliminar() {
        val estado = _uiState.value
        val evento = estado.eventoPorEliminar ?: return

        val listaActualizada = estado.eventos.filterNot {
            it.id == evento.id
        }

        val estabaEditando = estado.idEnEdicion == evento.id

        _uiState.update {
            it.copy(
                eventos = listaActualizada,
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
}
