package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.EventoDao
import cl.duoc.agenciaconnect.data.model.Evento
import cl.duoc.agenciaconnect.data.remote.AgenciaApi
import cl.duoc.agenciaconnect.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// Repositorio híbrido para administrar los Eventos corporativos.
// Integramos el consumo REST de Retrofit con el almacenamiento SQLite de Room.
class EventoRepository(
    private val eventoDao: EventoDao,
    private val api: AgenciaApi = RetrofitClient.retrofit.create(AgenciaApi::class.java)
) {

    // Mantenemos la lista reactiva de eventos escuchando directamente las mutaciones de Room.
    val eventos: Flow<List<Evento>> = eventoDao.obtenerEventos()

    // Traemos los eventos remotos al iniciar el módulo y los consolidamos en el DAO local.
    suspend fun sincronizarDesdeApi() = withContext(Dispatchers.IO) {
        try {
            val listaApi = api.obtenerEventos()
            listaApi.forEach { evento ->
                try {
                    val existente = eventoDao.obtenerEventoPorId(evento.id)
                    if (existente == null) {
                        eventoDao.insertarEvento(evento)
                    } else {
                        eventoDao.actualizarEvento(evento)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Registramos un nuevo evento solicitando la creación remota e insertando la respuesta en Room.
    suspend fun insertarEvento(evento: Evento): Long = withContext(Dispatchers.IO) {
        try {
            val response = api.crearEvento(evento)
            val creado = response.body()
            if (response.isSuccessful && creado != null) {
                val existente = eventoDao.obtenerEventoPorId(creado.id)
                if (existente == null) {
                    eventoDao.insertarEvento(creado)
                } else {
                    eventoDao.actualizarEvento(creado)
                }
                creado.id
            } else {
                eventoDao.insertarEvento(evento)
            }
        } catch (e: Exception) {
            // Ofrecemos tolerancia a fallos registrando el evento localmente si el backend no responde.
            try {
                eventoDao.insertarEvento(evento)
            } catch (ex: Exception) {
                0L
            }
        }
    }

    // Sincronizamos los cambios del evento mediante PUT remoto y actualización de Room.
    suspend fun actualizarEvento(evento: Evento) = withContext(Dispatchers.IO) {
        try {
            if (evento.id > 0) {
                api.actualizarEvento(evento.id, evento)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            eventoDao.actualizarEvento(evento)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Removemos el evento en la API remota y en la base de datos interna.
    suspend fun eliminarEvento(evento: Evento) = withContext(Dispatchers.IO) {
        try {
            if (evento.id > 0) {
                api.eliminarEvento(evento.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            eventoDao.eliminarEvento(evento)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
