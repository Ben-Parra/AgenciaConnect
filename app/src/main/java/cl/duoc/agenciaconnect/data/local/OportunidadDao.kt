package cl.duoc.agenciaconnect.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.duoc.agenciaconnect.data.model.Oportunidad
import kotlinx.coroutines.flow.Flow

// Con esta interfaz definimos las operaciones de base de datos para el módulo de oportunidades.
@Dao
interface OportunidadDao {

    // Con esta consulta obtenemos todas las oportunidades ordenadas desde la más reciente.
    @Query("SELECT * FROM oportunidades ORDER BY id DESC")
    fun obtenerOportunidades(): Flow<List<Oportunidad>>

    // Con esta consulta buscamos una oportunidad específica mediante su identificador.
    @Query("SELECT * FROM oportunidades WHERE id = :id")
    suspend fun obtenerOportunidadPorId(id: Long): Oportunidad?

    // Con esta operación guardamos una nueva oportunidad y obtenemos su identificador generado.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarOportunidad(oportunidad: Oportunidad): Long

    // Con esta operación guardamos las modificaciones realizadas en una oportunidad existente.
    @Update
    suspend fun actualizarOportunidad(oportunidad: Oportunidad)

    // Con esta operación eliminamos una oportunidad seleccionada de la base de datos.
    @Delete
    suspend fun eliminarOportunidad(oportunidad: Oportunidad)
}