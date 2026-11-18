package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.SeguimientoDao
import cl.duoc.agenciaconnect.data.model.Seguimiento
import cl.duoc.agenciaconnect.data.remote.AgenciaApi
import cl.duoc.agenciaconnect.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SeguimientoRepository(
    private val seguimientoDao: SeguimientoDao,
    private val api: AgenciaApi = RetrofitClient.retrofit.create(AgenciaApi::class.java)
) {

    fun obtenerSeguimientosPorOportunidad(oportunidadId: Long): Flow<List<Seguimiento>> {
        return seguimientoDao.obtenerSeguimientosPorOportunidad(oportunidadId)
    }

    val todosLosSeguimientos: Flow<List<Seguimiento>> = seguimientoDao.obtenerTodosLosSeguimientos()

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
            try {
                seguimientoDao.insertarSeguimiento(seguimiento)
            } catch (ex: Exception) {
                0L
            }
        }
    }

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
