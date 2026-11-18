package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.OrganizacionDao
import cl.duoc.agenciaconnect.data.model.Organizacion
import cl.duoc.agenciaconnect.data.remote.AgenciaApi
import cl.duoc.agenciaconnect.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class OrganizacionRepository(
    private val organizacionDao: OrganizacionDao,
    private val api: AgenciaApi = RetrofitClient.retrofit.create(AgenciaApi::class.java)
) {

    val organizaciones: Flow<List<Organizacion>> = organizacionDao.obtenerOrganizaciones()

    suspend fun sincronizarDesdeApi() = withContext(Dispatchers.IO) {
        try {
            val listaApi = api.obtenerOrganizaciones()
            listaApi.forEach { org ->
                try {
                    val existente = organizacionDao.obtenerOrganizacionPorId(org.id)
                    if (existente == null) {
                        organizacionDao.insertarOrganizacion(org)
                    } else {
                        organizacionDao.actualizarOrganizacion(org)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun insertarOrganizacion(organizacion: Organizacion): Long = withContext(Dispatchers.IO) {
        try {
            val response = api.crearOrganizacion(organizacion)
            val creada = response.body()
            if (response.isSuccessful && creada != null) {
                val existente = organizacionDao.obtenerOrganizacionPorId(creada.id)
                if (existente == null) {
                    organizacionDao.insertarOrganizacion(creada)
                } else {
                    organizacionDao.actualizarOrganizacion(creada)
                }
                creada.id
            } else {
                organizacionDao.insertarOrganizacion(organizacion)
            }
        } catch (e: Exception) {
            try {
                organizacionDao.insertarOrganizacion(organizacion)
            } catch (ex: Exception) {
                0L
            }
        }
    }

    suspend fun actualizarOrganizacion(organizacion: Organizacion) = withContext(Dispatchers.IO) {
        try {
            if (organizacion.id > 0) {
                api.actualizarOrganizacion(organizacion.id, organizacion)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            organizacionDao.actualizarOrganizacion(organizacion)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun eliminarOrganizacion(organizacion: Organizacion) = withContext(Dispatchers.IO) {
        try {
            if (organizacion.id > 0) {
                api.eliminarOrganizacion(organizacion.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            organizacionDao.eliminarOrganizacion(organizacion)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
