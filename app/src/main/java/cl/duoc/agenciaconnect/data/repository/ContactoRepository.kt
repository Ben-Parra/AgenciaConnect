package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.ContactoDao
import cl.duoc.agenciaconnect.data.model.Contacto
import kotlinx.coroutines.flow.Flow

// Con esta clase conectamos el ViewModel con las operaciones de contactos de la base de datos.
class ContactoRepository(
    private val contactoDao: ContactoDao
) {

    // Con este flujo observamos los contactos registrados y sus cambios.
    val contactos: Flow<List<Contacto>> = contactoDao.obtenerContactos()

    // Con esta función buscamos un contacto por su identificador.
    suspend fun obtenerContactoPorId(id: Long): Contacto? {
        return contactoDao.obtenerContactoPorId(id)
    }

    // Con esta función registramos un contacto nuevo.
    suspend fun insertarContacto(contacto: Contacto): Long {
        return contactoDao.insertarContacto(contacto)
    }

    // Con esta función actualizamos un contacto existente.
    suspend fun actualizarContacto(contacto: Contacto) {
        contactoDao.actualizarContacto(contacto)
    }

    // Con esta función eliminamos el contacto seleccionado.
    suspend fun eliminarContacto(contacto: Contacto) {
        contactoDao.eliminarContacto(contacto)
    }
}
