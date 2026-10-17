
package cl.duoc.agenciaconnect.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.agenciaconnect.data.model.Organizacion
import kotlinx.coroutines.flow.Flow

// Con esta interfaz definimos las operaciones para administrar las organizaciones en Room.
@Dao
interface OrganizacionDao {

    // Con esta consulta obtenemos las organizaciones y las ordenamos desde la más reciente.
    @Query("SELECT * FROM organizaciones ORDER BY id DESC")
    fun obtenerOrganizaciones(): Flow<List<Organizacion>>

    // Con esta operación guardamos una organización y recibimos el identificador generado.
    @Insert
    suspend fun insertarOrganizacion(organizacion: Organizacion): Long

    // Con esta operación actualizamos los datos de una organización existente.
    @Update
    suspend fun actualizarOrganizacion(organizacion: Organizacion)

    // Con esta operación eliminamos una organización seleccionada.
    @Delete
    suspend fun eliminarOrganizacion(organizacion: Organizacion)
}
