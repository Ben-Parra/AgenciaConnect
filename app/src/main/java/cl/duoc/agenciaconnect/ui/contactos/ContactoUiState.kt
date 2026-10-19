package cl.duoc.agenciaconnect.ui.contactos

import cl.duoc.agenciaconnect.data.model.Contacto
import cl.duoc.agenciaconnect.data.model.Organizacion

// Con esta clase centralizamos los datos, errores y mensajes del módulo de contactos.
data class ContactoUiState(

    // Con estos campos almacenamos los datos ingresados en el formulario.
    val nombreCompleto: String = "",
    val organizacionId: Long? = null,
    val cargo: String = "",
    val area: String = "",
    val correo: String = "",
    val telefono: String = "+56",
    val medioPreferido: String = "",
    val observaciones: String = "",

    // Con estos indicadores controlamos los errores de validación.
    val errorNombreCompleto: Boolean = false,
    val errorOrganizacion: Boolean = false,
    val errorCargo: Boolean = false,
    val errorArea: Boolean = false,
    val errorCorreo: Boolean = false,
    val errorTelefono: Boolean = false,
    val errorMedioPreferido: Boolean = false,

    // Con estas listas mostramos los contactos y organizaciones registrados.
    val contactos: List<Contacto> = emptyList(),
    val organizaciones: List<Organizacion> = emptyList(),

    // Con estos campos controlamos la edición y eliminación de contactos.
    val idEnEdicion: Long? = null,
    val contactoPorEliminar: Contacto? = null,

    // Con estos campos indicamos el estado del guardado y los mensajes.
    val guardando: Boolean = false,
    val mensaje: String? = null
)
