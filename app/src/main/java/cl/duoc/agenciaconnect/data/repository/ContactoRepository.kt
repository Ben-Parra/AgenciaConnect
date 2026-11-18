package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.ContactoDao
import cl.duoc.agenciaconnect.data.model.Contacto
import cl.duoc.agenciaconnect.data.remote.AgenciaApi
import cl.duoc.agenciaconnect.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// Repositorio híbrido para la gestión de Contactos comerciales.
// Sincronizamos las operaciones locales de Room con los endpoints remotos de Spring Boot.
class ContactoRepository(
    private val contactoDao: ContactoDao,
    private val api: AgenciaApi = RetrofitClient.retrofit.create(AgenciaApi::class.java)
) {

    // Exponemos el flujo reactivo de contactos para que los ViewModels y pantallas observen los cambios automáticamente.
    val contactos: Flow<List<Contacto>> = contactoDao.obtenerContactos()

    // Buscamos un contacto específico por su identificador local.
    suspend fun obtenerContactoPorId(id: Long): Contacto? = withContext(Dispatchers.IO) {
        contactoDao.obtenerContactoPorId(id)
    }

    // Consultamos los contactos en el microservicio en un hilo de E/S y persistimos las novedades en Room.
    suspend fun sincronizarDesdeApi() = withContext(Dispatchers.IO) {
        try {
            val listaApi = api.obtenerContactos()
            listaApi.forEach { contacto ->
                try {
                    val existente = contactoDao.obtenerContactoPorId(contacto.id)
                    if (existente == null) {
                        contactoDao.insertarContacto(contacto)
                    } else {
                        contactoDao.actualizarContacto(contacto)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Persistimos el contacto primero en el servidor vía POST y guardamos la entidad con su ID oficial en Room.
    suspend fun insertarContacto(contacto: Contacto): Long = withContext(Dispatchers.IO) {
        try {
            val response = api.crearContacto(contacto)
            val creado = response.body()
            if (response.isSuccessful && creado != null) {
                val existente = contactoDao.obtenerContactoPorId(creado.id)
                if (existente == null) {
                    contactoDao.insertarContacto(creado)
                } else {
                    contactoDao.actualizarContacto(creado)
                }
                creado.id
            } else {
                contactoDao.insertarContacto(contacto)
            }
        } catch (e: Exception) {
            // Respaldamos el contacto en Room en caso de falla de conectividad de red.
            try {
                contactoDao.insertarContacto(contacto)
            } catch (ex: Exception) {
                0L
            }
        }
    }

    // Actualizamos los datos del contacto en la API y refrescamos el almacenamiento local.
    suspend fun actualizarContacto(contacto: Contacto) = withContext(Dispatchers.IO) {
        try {
            if (contacto.id > 0) {
                api.actualizarContacto(contacto.id, contacto)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            contactoDao.actualizarContacto(contacto)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Borramos la entidad tanto en la base de datos remota mediante DELETE como en la BD local.
    suspend fun eliminarContacto(contacto: Contacto) = withContext(Dispatchers.IO) {
        try {
            if (contacto.id > 0) {
                api.eliminarContacto(contacto.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            contactoDao.eliminarContacto(contacto)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
