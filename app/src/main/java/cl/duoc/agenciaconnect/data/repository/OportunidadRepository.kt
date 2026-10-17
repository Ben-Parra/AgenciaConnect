package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.OportunidadDao
import cl.duoc.agenciaconnect.data.model.Oportunidad
import kotlinx.coroutines.flow.Flow

// Con esta clase conectamos el ViewModel con las operaciones de oportunidades de la base de datos.
class OportunidadRepository(
    private val oportunidadDao: OportunidadDao
) {

    // Con este flujo observamos las oportunidades registradas y sus cambios en tiempo real.
    val oportunidades: Flow<List<Oportunidad>> = oportunidadDao.obtenerOportunidades()

    // Con esta función consultamos una oportunidad específica por su identificador.
    suspend fun obtenerOportunidadPorId(id: Long): Oportunidad? {
        return oportunidadDao.obtenerOportunidadPorId(id)
    }

    // Con esta función registramos una nueva oportunidad comercial.
    suspend fun insertarOportunidad(oportunidad: Oportunidad): Long {
        return oportunidadDao.insertarOportunidad(oportunidad)
    }

    // Con esta función guardamos las modificaciones de una oportunidad existente.
    suspend fun actualizarOportunidad(oportunidad: Oportunidad) {
        oportunidadDao.actualizarOportunidad(oportunidad)
    }

    // Con esta función eliminamos la oportunidad seleccionada de la base de datos.
    suspend fun eliminarOportunidad(oportunidad: Oportunidad) {
        oportunidadDao.eliminarOportunidad(oportunidad)
    }

}