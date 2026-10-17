package cl.duoc.agenciaconnect.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Con esta función lanzamos el calendario nativo de Android para agendar una reunión o evento corporativo.
fun agendarEnCalendario(
    context: Context,
    titulo: String,
    descripcion: String,
    ubicacion: String,
    fechaTexto: String
) {
    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, titulo)
        putExtra(CalendarContract.Events.DESCRIPTION, descripcion)
        putExtra(CalendarContract.Events.EVENT_LOCATION, ubicacion)

        // Si la fecha tiene formato DDMMYYYY o contiene barras, calculamos los milisegundos iniciales.
        val fechaLimpia = fechaTexto.filter { it.isDigit() }
        if (fechaLimpia.length == 8) {
            try {
                val formato = DateTimeFormatter.ofPattern("ddMMuuuu")
                val fecha = LocalDate.parse(fechaLimpia, formato)
                val millis = fecha.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, millis)
            } catch (e: Exception) {
                // Si la fecha no se puede parsear, el calendario se abre con la fecha actual.
            }
        }
    }

    // Comprobamos que el dispositivo cuente con una aplicación capaz de responder al intent.
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // En caso de que un emulador mínimo carezca de app de calendario, evitamos el cierre forzado.
    }
}