package cl.duoc.agenciaconnect.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.agenciaconnect.data.model.Contacto
import kotlinx.coroutines.flow.Flow

// Con esta interfaz definimos las operaciones para administrar los contactos en la base de datos.
@Dao
interface ContactoDao {

    // Con esta consulta obtenemos los contactos ordenados alfabéticamente.
    @Query("SELECT * FROM contactos ORDER BY nombreCompleto ASC")
    fun obtenerContactos(): Flow<List<Contacto>>

    // Con esta consulta buscamos un contacto por su identificador.
    @Query("SELECT * FROM contactos WHERE id = :id")
    suspend fun obtenerContactoPorId(id: Long): Contacto?

    // Con esta operación guardamos un contacto nuevo y obtenemos su identificador.
    @Insert
    suspend fun insertarContacto(contacto: Contacto): Long

    // Con esta operación actualizamos los datos de un contacto existente.
    @Update
    suspend fun actualizarContacto(contacto: Contacto)

    // Con esta operación eliminamos un contacto seleccionado.
    @Delete
    suspend fun eliminarContacto(contacto: Contacto)
}
