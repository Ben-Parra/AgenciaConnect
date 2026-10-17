package cl.duoc.agenciaconnect.ui.organizaciones

import cl.duoc.agenciaconnect.data.model.Organizacion

// Con esta clase centralizamos los datos del formulario, sus errores y las organizaciones registradas.
data class OrganizacionUiState(

    // Con estos campos conservamos los valores que el usuario escribe en el formulario.
    val nombre: String = "",
    val tipo: String = "",
    val rubro: String = "",
    val comuna: String = "",
    val region: String = "",
    val sitioWeb: String = "",
    val publicoAsociado: String = "",
    val observaciones: String = "",

    // Con estos indicadores mostramos errores específicos en los campos que necesitan corrección.
    val errorNombre: Boolean = false,
    val errorTipo: Boolean = false,
    val errorRubro: Boolean = false,
    val errorComuna: Boolean = false,
    val errorRegion: Boolean = false,
    val errorPublicoAsociado: Boolean = false,

    // Con esta lista mantenemos los datos de las organizaciones que observamos desde Room.
    val organizaciones: List<Organizacion> = emptyList(),

    // Con este identificador distinguimos cuándo estamos editando una organización existente.
    val idEnEdicion: Long? = null,

    // Con esta propiedad guardamos temporalmente la organización que se desea eliminar.
    val organizacionPorEliminar: Organizacion? = null,

    // Con este indicador informamos si la operación de guardado está en curso.
    val guardando: Boolean = false,

    // Con este mensaje comunicamos el resultado de una operación al usuario.
    val mensaje: String? = null
)
