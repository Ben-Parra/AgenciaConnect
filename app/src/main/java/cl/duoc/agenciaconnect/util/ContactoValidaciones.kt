package cl.duoc.agenciaconnect.util

// Con esta función comprobamos que el nombre completo del contacto no esté vacío.
fun validarNombreContacto(nombre: String): Boolean {
    return nombre.trim().isNotEmpty()
}

// Con esta función comprobamos que se haya seleccionado una organización.
fun validarOrganizacionContacto(organizacionId: Long?): Boolean {
    return organizacionId != null && organizacionId > 0L
}

// Con esta función comprobamos que el cargo del contacto esté ingresado.
fun validarCargoContacto(cargo: String): Boolean {
    return cargo.trim().isNotEmpty()
}

// Con esta función comprobamos que el área de trabajo esté ingresada.
fun validarAreaContacto(area: String): Boolean {
    return area.trim().isNotEmpty()
}

// Con esta función comprobamos que el correo tenga un formato básico válido.
fun validarCorreoContacto(correo: String): Boolean {
    val correoLimpio = correo.trim()
    return correoLimpio.isNotEmpty() &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()
}

// Con esta función comprobamos que el medio preferido esté seleccionado.
fun validarMedioPreferidoContacto(medioPreferido: String): Boolean {
    return medioPreferido.trim().isNotEmpty()
}
