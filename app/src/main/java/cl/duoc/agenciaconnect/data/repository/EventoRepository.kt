package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.EventoDao
import cl.duoc.agenciaconnect.data.model.Evento
import kotlinx.coroutines.flow.Flow

// Con esta clase centralizamos el acceso a los datos de los eventos
// y conectamos el ViewModel con las operaciones de Room.
class EventoRepository(
    private val eventoDao: EventoDao
) {

    // Con esta propiedad observamos la lista de eventos almacenados.
    val eventos: Flow<List<Evento>> = eventoDao.obtenerEventos()

    // Con esta función registramos un evento y recibimos su identificador.
    suspend fun insertarEvento(evento: Evento): Long {
        return eventoDao.insertarEvento(evento)
    }

    // Con esta función guardamos los cambios de un evento existente.
    suspend fun actualizarEvento(evento: Evento) {
        eventoDao.actualizarEvento(evento)
    }

    // Con esta función eliminamos un evento de la base de datos.
    suspend fun eliminarEvento(evento: Evento) {
        eventoDao.eliminarEvento(evento)
    }
}
