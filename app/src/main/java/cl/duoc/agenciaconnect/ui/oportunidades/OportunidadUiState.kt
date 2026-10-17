package cl.duoc.agenciaconnect.ui.oportunidades

import cl.duoc.agenciaconnect.data.model.Evento
import cl.duoc.agenciaconnect.data.model.Oportunidad
import cl.duoc.agenciaconnect.data.model.Organizacion

// Con esta clase centralizamos los datos del formulario, errores, listas y mensajes de oportunidades.
data class OportunidadUiState(

    // Con estos campos almacenamos los datos ingresados o seleccionados en el formulario.
    val nombre: String = "",
    val tipo: String = "",
    val organizacionId: Long? = null,
    val eventoId: Long = 0L,
    val prioridad: String = "",
    val potencial: String = "",
    val estado: String = "Evento identificado",
    val responsable: String = "",
    val observaciones: String = "",

    // Con estos indicadores controlamos los mensajes de error visuales por cada campo.
    val errorNombre: Boolean = false,
    val errorTipo: Boolean = false,
    val errorOrganizacion: Boolean = false,
    val errorPrioridad: Boolean = false,
    val errorPotencial: Boolean = false,
    val errorEstado: Boolean = false,
    val errorResponsable: Boolean = false,

    // Con estas listas cargamos las entidades desde Room para usarlas en los selectores y tarjetas.
    val oportunidades: List<Oportunidad> = emptyList(),
    val organizaciones: List<Organizacion> = emptyList(),
    val eventos: List<Evento> = emptyList(),

    // Con estos campos controlamos el modo de edición y la solicitud de eliminación.
    val idEnEdicion: Long? = null,
    val oportunidadPorEliminar: Oportunidad? = null,

    // Con estos indicadores informamos el estado de las operaciones y el resultado visual.
    val guardando: Boolean = false,
    val mensaje: String? = null
)