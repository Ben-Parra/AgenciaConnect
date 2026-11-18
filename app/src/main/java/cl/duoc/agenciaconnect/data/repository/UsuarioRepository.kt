package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.UsuarioDao
import cl.duoc.agenciaconnect.data.model.Usuario
import cl.duoc.agenciaconnect.data.remote.AgenciaApi
import cl.duoc.agenciaconnect.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// Repositorio híbrido para la administración de Usuarios y Roles del sistema.
// Gestionamos las credenciales locales y la sincronización con el servidor.
class UsuarioRepository(
    private val usuarioDao: UsuarioDao,
    private val api: AgenciaApi = RetrofitClient.retrofit.create(AgenciaApi::class.java)
) {

    // Exponemos el flujo de usuarios completos para el sistema de autenticación y asignación.
    val usuarios: Flow<List<Usuario>> = usuarioDao.obtenerUsuarios()
    val ejecutivos: Flow<List<Usuario>> = usuarioDao.obtenerEjecutivos()

    // Autenticamos las credenciales del usuario ejecutando la consulta en un hilo de E/S.
    suspend fun autenticar(correo: String, contrasena: String): Usuario? = withContext(Dispatchers.IO) {
        usuarioDao.autenticar(correo.trim(), contrasena)
    }

    // Traemos los usuarios registrados desde la API remota e insertamos o reemplazamos en Room.
    suspend fun sincronizarDesdeApi() = withContext(Dispatchers.IO) {
        try {
            val listaApi = api.obtenerUsuarios()
            if (listaApi.isNotEmpty()) {
                usuarioDao.insertarUsuarios(listaApi)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Registramos un nuevo usuario vía POST remoto y lo almacenamos en la base de datos interna.
    suspend fun insertarUsuario(usuario: Usuario): Long = withContext(Dispatchers.IO) {
        try {
            val response = api.crearUsuario(usuario)
            val creado = response.body()
            if (response.isSuccessful && creado != null) {
                usuarioDao.insertarUsuario(creado)
                creado.id
            } else {
                usuarioDao.insertarUsuario(usuario)
            }
        } catch (e: Exception) {
            try {
                usuarioDao.insertarUsuario(usuario)
            } catch (ex: Exception) {
                0L
            }
        }
    }

    // Actualizamos las credenciales o el rol del usuario en la API y en la BD local.
    suspend fun actualizarUsuario(usuario: Usuario) = withContext(Dispatchers.IO) {
        try {
            if (usuario.id > 0) {
                api.actualizarUsuario(usuario.id, usuario)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            usuarioDao.actualizarUsuario(usuario)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Borramos un usuario del sistema tanto en el servidor como en Room.
    suspend fun eliminarUsuario(usuario: Usuario) = withContext(Dispatchers.IO) {
        try {
            if (usuario.id > 0) {
                api.eliminarUsuario(usuario.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            usuarioDao.eliminarUsuario(usuario)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
