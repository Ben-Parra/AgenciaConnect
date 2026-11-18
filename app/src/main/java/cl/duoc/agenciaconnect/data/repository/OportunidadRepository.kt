package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.OportunidadDao
import cl.duoc.agenciaconnect.data.model.Oportunidad
import cl.duoc.agenciaconnect.data.remote.AgenciaApi
import cl.duoc.agenciaconnect.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class OportunidadRepository(
    private val oportunidadDao: OportunidadDao,
    private val api: AgenciaApi = RetrofitClient.retrofit.create(AgenciaApi::class.java)
) {

    val oportunidades: Flow<List<Oportunidad>> = oportunidadDao.obtenerOportunidades()

    suspend fun sincronizarDesdeApi() = withContext(Dispatchers.IO) {
        try {
            val listaApi = api.obtenerOportunidades()
            listaApi.forEach { op ->
                try {
                    val existente = oportunidadDao.obtenerOportunidadPorId(op.id)
                    if (existente == null) {
                        oportunidadDao.insertarOportunidad(op)
                    } else {
                        oportunidadDao.actualizarOportunidad(op)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun insertarOportunidad(oportunidad: Oportunidad) = withContext(Dispatchers.IO) {
        try {
            val response = api.crearOportunidad(oportunidad)
            val creada = response.body()
            if (response.isSuccessful && creada != null) {
                val existente = oportunidadDao.obtenerOportunidadPorId(creada.id)
                if (existente == null) {
                    oportunidadDao.insertarOportunidad(creada)
                } else {
                    oportunidadDao.actualizarOportunidad(creada)
                }
            } else {
                oportunidadDao.insertarOportunidad(oportunidad)
            }
        } catch (e: Exception) {
            try {
                oportunidadDao.insertarOportunidad(oportunidad)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    suspend fun actualizarOportunidad(oportunidad: Oportunidad) = withContext(Dispatchers.IO) {
        try {
            api.actualizarOportunidad(oportunidad.id, oportunidad)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            oportunidadDao.actualizarOportunidad(oportunidad)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun eliminarOportunidad(oportunidad: Oportunidad) = withContext(Dispatchers.IO) {
        try {
            api.eliminarOportunidad(oportunidad.id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            oportunidadDao.eliminarOportunidad(oportunidad)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
