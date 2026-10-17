package cl.duoc.agenciaconnect.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaconnect.data.local.AgenciaDatabase
import cl.duoc.agenciaconnect.data.repository.UsuarioRepository
import cl.duoc.agenciaconnect.util.validarContrasenaLogin
import cl.duoc.agenciaconnect.util.validarCorreoLogin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Con este ViewModel gestionamos el inicio de sesión y la verificación de roles contra Room.
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // Con esta dependencia accedemos al repositorio de usuarios y autenticación.
    private val usuarioRepository = UsuarioRepository(
        AgenciaDatabase.obtenerInstancia(application).usuarioDao()
    )

    // Con este flujo mantenemos el estado privado de la pantalla de acceso.
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Con esta función actualizamos el correo ingresado y limpiamos errores previos.
    fun cambiarCorreo(nuevoCorreo: String) {
        _uiState.update {
            it.copy(
                correo = nuevoCorreo,
                errorCorreo = false,
                mensajeError = ""
            )
        }
    }

    // Con esta función actualizamos la contraseña y limpiamos su error.
    fun cambiarContrasena(nuevaContrasena: String) {
        _uiState.update {
            it.copy(
                contrasena = nuevaContrasena,
                errorContrasena = false,
                mensajeError = ""
            )
        }
    }

    // Con esta función ayudamos a cargar credenciales rápidas de prueba durante la defensa.
    fun cargarCredencialPrueba(correo: String, contrasena: String) {
        _uiState.update {
            it.copy(
                correo = correo,
                contrasena = contrasena,
                errorCorreo = false,
                errorContrasena = false,
                mensajeError = ""
            )
        }
    }

    // Con esta función validamos los campos y autenticamos al usuario contra la base de datos,
    // entregando directamente el usuario encontrado en el callback de éxito.
    fun autenticar(onExito: (cl.duoc.agenciaconnect.data.model.Usuario) -> Unit) {
        val estadoActual = _uiState.value

        val correoValido = validarCorreoLogin(estadoActual.correo)
        val contrasenaValida = validarContrasenaLogin(estadoActual.contrasena)

        if (!correoValido || !contrasenaValida) {
            _uiState.update {
                it.copy(
                    errorCorreo = !correoValido,
                    errorContrasena = !contrasenaValida,
                    mensajeError = "Ingresa un correo válido y tu contraseña."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, mensajeError = "") }

            try {
                val usuario = usuarioRepository.autenticar(
                    estadoActual.correo,
                    estadoActual.contrasena
                )

                if (usuario != null) {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            usuarioAutenticado = usuario,
                            mensajeError = ""
                        )
                    }
                    // Entregamos directamente el usuario recién verificado
                    onExito(usuario)
                } else {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            usuarioAutenticado = null,
                            mensajeError = "Correo o contraseña incorrectos. Revisa las cuentas de prueba."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeError = "Error al conectar con la base de datos."
                    )
                }
            }
        }
    }

    // Con esta función cerramos la sesión y limpiamos los datos del estado.
    fun cerrarSesion() {
        _uiState.value = LoginUiState()
    }
}