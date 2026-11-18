package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.SeguimientoDao
import cl.duoc.agenciaconnect.data.model.Seguimiento
import cl.duoc.agenciaconnect.data.remote.AgenciaApi
import cl.duoc.agenciaconnect.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// Repositorio híbrido para la bitácora de Seguimientos comerciales.
// Sincronizamos las acciones y gestiones vinculadas a cada oportunidad con el servidor.
class SeguimientoRepository(
    private val seguimientoDao: SeguimientoDao,
    private val api: AgenciaApi = RetrofitClient.retrofit.create(AgenciaApi::class.java)
) {

    // Obtenemos las gestiones reactivas asociadas a una oportunidad específica.
    fun obtenerSeguimientosPorOportunidad(oportunidadId: Long): Flow<List<Seguimiento>> {
        return seguimientoDao.obtenerSeguimientosPorOportunidad(oportunidadId)
    }

    // Exponemos el historial completo de gestiones para vistas globales.
    val todosLosSeguimientos: Flow<List<Seguimiento>> = seguimientoDao.obtenerTodosLosSeguimientos()

    // Sincronizamos la bitácora histórica desde la API remota guardando registros sin duplicar.
    suspend fun sincronizarDesdeApi() = withContext(Dispatchers.IO) {
        try {
            val listaApi = api.obtenerSeguimientos()
            listaApi.forEach { seg ->
                try {
                    val existente = seguimientoDao.obtenerSeguimientoPorId(seg.id)
                    if (existente == null) {
                        seguimientoDao.insertarSeguimiento(seg)
                    } else {
                        seguimientoDao.actualizarSeguimiento(seg)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Registramos la gestión comercial enviando POST a Retrofit e insertando la respuesta en Room.
    suspend fun insertarSeguimiento(seguimiento: Seguimiento): Long = withContext(Dispatchers.IO) {
        try {
            val response = api.crearSeguimiento(seguimiento)
            val creado = response.body()
            if (response.isSuccessful && creado != null) {
                val existente = seguimientoDao.obtenerSeguimientoPorId(creado.id)
                if (existente == null) {
                    seguimientoDao.insertarSeguimiento(creado)
                } else {
                    seguimientoDao.actualizarSeguimiento(creado)
                }
                creado.id
            } else {
                seguimientoDao.insertarSeguimiento(seguimiento)
            }
        } catch (e: Exception) {
            // Guardamos localmente el seguimiento en Room ante falta de red.
            try {
                seguimientoDao.insertarSeguimiento(seguimiento)
            } catch (ex: Exception) {
                0L
            }
        }
    }

    // Actualizamos un registro de seguimiento en la API y en la base local.
    suspend fun actualizarSeguimiento(seguimiento: Seguimiento) = withContext(Dispatchers.IO) {
        try {
            if (seguimiento.id > 0) {
                api.actualizarSeguimiento(seguimiento.id, seguimiento)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            seguimientoDao.actualizarSeguimiento(seguimiento)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Eliminamos una entrada de seguimiento en la API y en Room.
    suspend fun eliminarSeguimiento(seguimiento: Seguimiento) = withContext(Dispatchers.IO) {
        try {
            if (seguimiento.id > 0) {
                api.eliminarSeguimiento(seguimiento.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            seguimientoDao.eliminarSeguimiento(seguimiento)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
