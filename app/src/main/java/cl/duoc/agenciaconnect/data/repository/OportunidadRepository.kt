package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.OportunidadDao
import cl.duoc.agenciaconnect.data.model.Oportunidad
import cl.duoc.agenciaconnect.data.remote.AgenciaApi
import cl.duoc.agenciaconnect.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// Repositorio híbrido para la gestión de Oportunidades comerciales.
// Coordinamos la persistencia reactiva en la base de datos local Room con el microservicio en Spring Boot.
class OportunidadRepository(
    private val oportunidadDao: OportunidadDao,
    private val api: AgenciaApi = RetrofitClient.retrofit.create(AgenciaApi::class.java)
) {

    // Exponemos la fuente de datos reactiva mediante Flow para observar cambios locales e impulsar la interfaz.
    val oportunidades: Flow<List<Oportunidad>> = oportunidadDao.obtenerOportunidades()

    // Sincronizamos las oportunidades desde el backend en un hilo secundario (Dispatchers.IO) actualizando o insertando en Room.
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

    // Registramos una oportunidad enviando la petición a la API; si responde exitosamente, guardamos en Room la versión con el ID real de MySQL.
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
            // Capturamos fallos de red para mantener la disponibilidad sin perder la inserción local en Room.
            try {
                oportunidadDao.insertarOportunidad(oportunidad)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    // Enviamos las modificaciones mediante un verbo PUT al backend y refrescamos los datos locales en Room.
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

    // Ejecutamos el borrado físico tanto en la API remota como en la base de datos Room.
    suspend fun eliminarOportunidad(oportunidad: Oportunidad) = withContext(Dispatchers.IO) {
        try {
            if (oportunidad.id > 0) {
                api.eliminarOportunidad(oportunidad.id)
            }
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
