package cl.duoc.agenciaconnect.ui.eventos

import cl.duoc.agenciaconnect.data.model.Evento

// Con esta clase centralizamos el estado del formulario,
// los errores y las operaciones disponibles sobre los eventos.
data class EventoUiState(

    // Variables que utilizamos para registrar un evento.
    val nombre: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val ubicacion: String = "",
    val organizacion: String = "",

    // Estados que indican si cada campo contiene un error.
    val errorNombre: Boolean = false,
    val errorDescripcion: Boolean = false,
    val errorFecha: Boolean = false,
    val errorUbicacion: Boolean = false,
    val errorOrganizacion: Boolean = false,

    // Con esta lista mantenemos los eventos registrados.
    val eventos: List<Evento> = emptyList(),

    // Con esta variable identificamos el evento que estamos editando.
    val idEnEdicion: Long? = null,

    // Con esta variable controlamos la generación de identificadores.
    val siguienteId: Long = 1L,

    // Con esta variable identificamos el evento pendiente de eliminación.
    val eventoPorEliminar: Evento? = null
)
