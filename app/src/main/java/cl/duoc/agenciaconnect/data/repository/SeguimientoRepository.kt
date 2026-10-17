package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.SeguimientoDao
import cl.duoc.agenciaconnect.data.model.Seguimiento
import kotlinx.coroutines.flow.Flow

// Con esta clase coordinamos las consultas y registros del historial de gestiones de oportunidades.
class SeguimientoRepository(
    private val seguimientoDao: SeguimientoDao
) {
    // Con esta función observamos las gestiones asociadas a una oportunidad específica.
    fun obtenerSeguimientosPorOportunidad(oportunidadId: Long): Flow<List<Seguimiento>> {
        return seguimientoDao.obtenerSeguimientosPorOportunidad(oportunidadId)
    }

    // Con esta función observamos todas las gestiones globales registradas en el sistema.
    val todosLosSeguimientos: Flow<List<Seguimiento>> = seguimientoDao.obtenerTodosLosSeguimientos()

    // Con esta función registramos una nueva gestión comercial.
    suspend fun insertarSeguimiento(seguimiento: Seguimiento): Long {
        return seguimientoDao.insertarSeguimiento(seguimiento)
    }

    // Con esta función eliminamos un seguimiento si fuese necesario.
    suspend fun eliminarSeguimiento(seguimiento: Seguimiento) {
        seguimientoDao.eliminarSeguimiento(seguimiento)
    }
}