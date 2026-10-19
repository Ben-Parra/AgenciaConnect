package cl.duoc.agenciaconnect.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

// Valida que la fecha de detección sea una fecha válida y que NO sea posterior a hoy
fun validarFechaDeteccion(fechaStr: String): Boolean {
    if (fechaStr.isBlank()) return false

    val soloDigitos = fechaStr.filter { it.isDigit() }
    if (soloDigitos.length != 8) return false

    try {
        val formato = SimpleDateFormat("ddMMyyyy", Locale.getDefault()).apply {
            isLenient = false
        }
        val fechaIngresada = formato.parse(soloDigitos) ?: return false
        val hoy = Date()

        return !fechaIngresada.after(hoy)
    } catch (e: Exception) {
        return false
    }
}

// ────────────────────────────────────────────────────────────────────────────
// TRANSFORMACIÓN VISUAL DE FECHA (Fuera de las funciones, a nivel de archivo)
// ────────────────────────────────────────────────────────────────────────────
class FechaVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text

        val formateado = buildString {
            original.forEachIndexed { indice, caracter ->
                if (indice == 2 || indice == 4) append("/")
                append(caracter)
            }
        }

        val desplazamiento = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 4 -> offset + 1
                    else -> offset + 2
                }.coerceAtMost(formateado.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    else -> offset - 2
                }.coerceIn(0, original.length)
            }
        }

        return TransformedText(
            AnnotatedString(formateado),
            desplazamiento
        )
    }
}