package cl.duoc.agenciaconnect.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.duoc.agenciaconnect.data.model.Seguimiento
import kotlinx.coroutines.flow.Flow

// Con esta interfaz gestionamos el historial de gestiones comerciales en Room.
@Dao
interface SeguimientoDao {

    // Con esta consulta obtenemos las gestiones de una oportunidad ordenadas cronológicamente.
    @Query("SELECT * FROM seguimientos WHERE oportunidadId = :oportunidadId ORDER BY id DESC")
    fun obtenerSeguimientosPorOportunidad(oportunidadId: Long): Flow<List<Seguimiento>>

    // Con esta consulta obtenemos todas las gestiones globales para el panel del supervisor.
    @Query("SELECT * FROM seguimientos ORDER BY id DESC")
    fun obtenerTodosLosSeguimientos(): Flow<List<Seguimiento>>

    // Con esta consulta obtenemos un seguimiento por su identificador.
    @Query("SELECT * FROM seguimientos WHERE id = :id")
    suspend fun obtenerSeguimientoPorId(id: Long): Seguimiento?

    // Con esta operación guardamos una nueva gestión comercial.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSeguimiento(seguimiento: Seguimiento): Long

    // Con esta operación actualizamos los datos de un seguimiento existente.
    @Update
    suspend fun actualizarSeguimiento(seguimiento: Seguimiento)

    // Con esta operación eliminamos una gestión si fuese necesario.
    @Delete
    suspend fun eliminarSeguimiento(seguimiento: Seguimiento)
}
