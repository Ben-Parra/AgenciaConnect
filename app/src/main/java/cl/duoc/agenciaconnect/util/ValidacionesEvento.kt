package cl.duoc.agenciaconnect.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

// Con esta función comprobamos que el nombre del evento
// no esté vacío ni contenga solamente espacios.
fun validarNombreEvento(nombre: String): Boolean {
    return nombre.isNotBlank()
}

// Con esta función comprobamos que la descripción
// contenga información y no esté vacía.
fun validarDescripcionEvento(descripcion: String): Boolean {
    return descripcion.isNotBlank()
}

// Con esta función comprobamos que la ubicación
// del evento esté ingresada correctamente.
fun validarUbicacionEvento(ubicacion: String): Boolean {
    return ubicacion.isNotBlank()
}

// Con esta función comprobamos que la organización
// asociada al evento no esté vacía.
fun validarOrganizacionEvento(organizacion: String): Boolean {
    return organizacion.isNotBlank()
}

// Con esta función comprobamos que la fecha exista
// y sea posterior a la fecha actual del dispositivo.
// Recibimos la fecha en formato DDMMYYYY, sin barras.
fun validarFechaEvento(fecha: String): Boolean {
    if (fecha.length != 8 || !fecha.all { it.isDigit() }) {
        return false
    }

    return try {
        val formato = DateTimeFormatter
            .ofPattern("ddMMuuuu")
            .withResolverStyle(ResolverStyle.STRICT)

        val fechaEvento = LocalDate.parse(fecha, formato)

        fechaEvento.isAfter(LocalDate.now())
    } catch (e: java.time.DateTimeException) {
        false
    }
}
