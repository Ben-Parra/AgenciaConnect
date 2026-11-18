package cl.duoc.agenciaconnect.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.duoc.agenciaconnect.data.model.Usuario
import kotlinx.coroutines.flow.Flow

// Con esta interfaz administramos las consultas y accesos de los usuarios en Room.
@Dao
interface UsuarioDao {

    // Con esta consulta obtenemos todos los usuarios registrados.
    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    fun obtenerUsuarios(): Flow<List<Usuario>>

    // Con esta consulta obtenemos un usuario por su identificador.
    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtenerUsuarioPorId(id: Long): Usuario?

    // Con esta consulta obtenemos únicamente los ejecutivos para asignación de oportunidades.
    @Query("SELECT * FROM usuarios WHERE rol = 'EJECUTIVO' ORDER BY nombre ASC")
    fun obtenerEjecutivos(): Flow<List<Usuario>>

    // Con esta función validamos las credenciales de acceso al iniciar sesión.
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND contrasena = :contrasena LIMIT 1")
    suspend fun autenticar(correo: String, contrasena: String): Usuario?

    // Con esta función insertamos usuarios de prueba o sincronizados desde la API.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuarios(usuarios: List<Usuario>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: Usuario): Long

    @Update
    suspend fun actualizarUsuario(usuario: Usuario)

    @Delete
    suspend fun eliminarUsuario(usuario: Usuario)

    // Con esta consulta contamos cuántos usuarios existen para determinar la precarga.
    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contarUsuarios(): Int
}
