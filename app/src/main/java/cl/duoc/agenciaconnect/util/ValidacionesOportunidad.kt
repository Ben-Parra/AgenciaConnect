package cl.duoc.agenciaconnect.util

// Con esta función comprobamos que el nombre descriptivo de la oportunidad no esté vacío.
fun validarNombreOportunidad(nombre: String): Boolean {
    return nombre.trim().isNotEmpty()
}

// Con esta función comprobamos que se haya seleccionado un tipo de oportunidad válido.
fun validarTipoOportunidad(tipo: String): Boolean {
    return tipo.trim().isNotEmpty()
}

// Con esta función comprobamos que se haya seleccionado una organización registrada.
fun validarOrganizacionOportunidad(organizacionId: Long?): Boolean {
    return organizacionId != null && organizacionId > 0L
}

// Con esta función comprobamos que se haya asignado una prioridad para la gestión.
fun validarPrioridadOportunidad(prioridad: String): Boolean {
    return prioridad.trim().isNotEmpty()
}

// Con esta función comprobamos que se haya definido el potencial comercial de la oportunidad.
fun validarPotencialOportunidad(potencial: String): Boolean {
    return potencial.trim().isNotEmpty()
}

// Con esta función comprobamos que se haya seleccionado el estado inicial o actual de la gestión.
fun validarEstadoOportunidad(estado: String): Boolean {
    return estado.trim().isNotEmpty()
}

// Con esta función comprobamos que se indique el colaborador o responsable comercial a cargo.
fun validarResponsableOportunidad(responsable: String): Boolean {
    return responsable.trim().isNotEmpty()
}