package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.UsuarioDao
import cl.duoc.agenciaconnect.data.model.Usuario
import kotlinx.coroutines.flow.Flow

// Con esta clase conectamos las operaciones de cuentas y roles de usuario con la base de datos.
class UsuarioRepository(
    private val usuarioDao: UsuarioDao
) {
    // Con este flujo observamos todos los usuarios registrados en el sistema.
    val usuarios: Flow<List<Usuario>> = usuarioDao.obtenerUsuarios()

    // Con este flujo observamos únicamente los ejecutivos disponibles para asignación.
    val ejecutivos: Flow<List<Usuario>> = usuarioDao.obtenerEjecutivos()

    // Con esta función validamos las credenciales ingresadas en la pantalla de inicio de sesión.
    suspend fun autenticar(correo: String, contrasena: String): Usuario? {
        return usuarioDao.autenticar(correo.trim(), contrasena)
    }
}