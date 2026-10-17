package cl.duoc.agenciaconnect.util

// Con esta función comprobamos que el nombre de la organización no esté vacío.
fun validarNombreOrganizacion(nombre: String): Boolean {
    return nombre.trim().isNotEmpty()
}

// Con esta función comprobamos que se haya seleccionado un tipo de organización.
fun validarTipoOrganizacion(tipo: String): Boolean {
    return tipo.trim().isNotEmpty()
}

// Con esta función comprobamos que el rubro de la organización esté informado.
fun validarRubroOrganizacion(rubro: String): Boolean {
    return rubro.trim().isNotEmpty()
}

// Con esta función comprobamos que la comuna no esté vacía.
fun validarComunaOrganizacion(comuna: String): Boolean {
    return comuna.trim().isNotEmpty()
}

// Con esta función comprobamos que la región no esté vacía.
fun validarRegionOrganizacion(region: String): Boolean {
    return region.trim().isNotEmpty()
}

// Con esta función comprobamos que se indique el público asociado a la organización.
fun validarPublicoAsociadoOrganizacion(publicoAsociado: String): Boolean {
    return publicoAsociado.trim().isNotEmpty()
}
