
package cl.duoc.agenciaconnect.ui.eventos

import cl.duoc.agenciaconnect.data.model.Evento
import cl.duoc.agenciaconnect.data.model.Organizacion

// Con este estado centralizamos los datos del formulario,
// las organizaciones disponibles y los eventos registrados.
data class EventoUiState(
    val nombre: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val ubicacion: String = "",
    val organizacion: String = "",

    // Con esta lista mostramos únicamente las organizaciones
    // que obtenemos desde nuestra base de datos.
    val organizaciones: List<Organizacion> = emptyList(),

    val errorNombre: Boolean = false,
    val errorDescripcion: Boolean = false,
    val errorFecha: Boolean = false,
    val errorUbicacion: Boolean = false,
    val errorOrganizacion: Boolean = false,

    val eventos: List<Evento> = emptyList(),
    val idEnEdicion: Long? = null,
    val eventoPorEliminar: Evento? = null,
    val mensaje: String? = null
)
