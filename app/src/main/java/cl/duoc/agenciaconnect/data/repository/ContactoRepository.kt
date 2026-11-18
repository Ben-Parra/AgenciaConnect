package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.ContactoDao
import cl.duoc.agenciaconnect.data.model.Contacto
import cl.duoc.agenciaconnect.data.remote.AgenciaApi
import cl.duoc.agenciaconnect.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ContactoRepository(
    private val contactoDao: ContactoDao,
    private val api: AgenciaApi = RetrofitClient.retrofit.create(AgenciaApi::class.java)
) {

    val contactos: Flow<List<Contacto>> = contactoDao.obtenerContactos()

    suspend fun obtenerContactoPorId(id: Long): Contacto? = withContext(Dispatchers.IO) {
        contactoDao.obtenerContactoPorId(id)
    }

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
            try {
                contactoDao.insertarContacto(contacto)
            } catch (ex: Exception) {
                0L
            }
        }
    }

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
