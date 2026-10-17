package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.OrganizacionDao
import cl.duoc.agenciaconnect.data.model.Organizacion
import kotlinx.coroutines.flow.Flow

// Con esta clase centralizamos el acceso a los datos de las organizaciones.
class OrganizacionRepository(
    private val organizacionDao: OrganizacionDao
) {

    // Con esta propiedad exponemos las organizaciones para observar sus cambios desde el ViewModel.
    val organizaciones: Flow<List<Organizacion>> =
        organizacionDao.obtenerOrganizaciones()

    // Con esta función registramos una nueva organización mediante el DAO.
    suspend fun insertarOrganizacion(organizacion: Organizacion): Long {
        return organizacionDao.insertarOrganizacion(organizacion)
    }

    // Con esta función actualizamos una organización existente mediante el DAO.
    suspend fun actualizarOrganizacion(organizacion: Organizacion) {
        organizacionDao.actualizarOrganizacion(organizacion)
    }

    // Con esta función eliminamos la organización seleccionada mediante el DAO.
    suspend fun eliminarOrganizacion(organizacion: Organizacion) {
        organizacionDao.eliminarOrganizacion(organizacion)
    }
}
