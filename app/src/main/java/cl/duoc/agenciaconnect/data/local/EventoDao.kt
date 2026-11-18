package cl.duoc.agenciaconnect.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.duoc.agenciaconnect.data.model.Evento
import kotlinx.coroutines.flow.Flow

// Con esta interfaz definimos las operaciones que utilizaremos
// para consultar, registrar, actualizar y eliminar eventos.
@Dao
interface EventoDao {

    // Con esta función observamos los eventos almacenados
    // y recibimos automáticamente los cambios de la tabla.
    @Query("SELECT * FROM eventos ORDER BY id DESC")
    fun obtenerEventos(): Flow<List<Evento>>

    // Con esta consulta obtenemos un evento por su identificador.
    @Query("SELECT * FROM eventos WHERE id = :id")
    suspend fun obtenerEventoPorId(id: Long): Evento?

    // Con esta función guardamos un evento nuevo.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEvento(evento: Evento): Long

    // Con esta función actualizamos los datos de un evento existente.
    @Update
    suspend fun actualizarEvento(evento: Evento)

    // Con esta función eliminamos un evento de la base de datos.
    @Delete
    suspend fun eliminarEvento(evento: Evento)
}
