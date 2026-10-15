
package cl.duoc.agenciaconnect.ui.eventos

import cl.duoc.agenciaconnect.data.model.Evento

// Con esta clase centralizamos el estado del formulario,
// los errores y los eventos obtenidos desde la base de datos.
data class EventoUiState(

    // Con estas variables almacenamos los datos del formulario.
    val nombre: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val ubicacion: String = "",
    val organizacion: String = "",

    // Con estos estados indicamos los errores de cada campo.
    val errorNombre: Boolean = false,
    val errorDescripcion: Boolean = false,
    val errorFecha: Boolean = false,
    val errorUbicacion: Boolean = false,
    val errorOrganizacion: Boolean = false,

    // Con esta lista mostramos los eventos guardados en Room.
    val eventos: List<Evento> = emptyList(),

    // Con esta variable identificamos el evento que estamos editando.
    val idEnEdicion: Long? = null,

    // Con esta variable identificamos el evento pendiente de eliminación.
    val eventoPorEliminar: Evento? = null
)
