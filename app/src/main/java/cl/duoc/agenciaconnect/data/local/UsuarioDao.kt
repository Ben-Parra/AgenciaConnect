package cl.duoc.agenciaconnect.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duoc.agenciaconnect.data.model.Usuario
import kotlinx.coroutines.flow.Flow

// Con esta interfaz administramos las consultas y accesos de los usuarios en Room.
@Dao
interface UsuarioDao {

    // Con esta consulta obtenemos todos los usuarios registrados.
    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    fun obtenerUsuarios(): Flow<List<Usuario>>

    // Con esta consulta obtenemos únicamente los ejecutivos para asignación de oportunidades.
    @Query("SELECT * FROM usuarios WHERE rol = 'EJECUTIVO' ORDER BY nombre ASC")
    fun obtenerEjecutivos(): Flow<List<Usuario>>

    // Con esta función validamos las credenciales de acceso al iniciar sesión.
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND contrasena = :contrasena LIMIT 1")
    suspend fun autenticar(correo: String, contrasena: String): Usuario?

    // Con esta función insertamos usuarios de prueba si la tabla está vacía.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuarios(usuarios: List<Usuario>)

    // Con esta consulta contamos cuántos usuarios existen para determinar la precarga.
    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contarUsuarios(): Int
}