package cl.duoc.agenciaconnect.ui.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Con esta clase gestionamos los datos, las validaciones
// y el inicio de sesión del administrador.
class LoginViewModel : ViewModel() {

    // Con estas constantes definimos las credenciales
    // genéricas que utilizaremos durante la demostración académica.
    private val usuarioAdmin = "admin"
    private val claveAdmin = "admin123"

    // Con esta variable mantenemos el estado privado del formulario.
    private val _uiState = MutableStateFlow(LoginUiState())

    // Con esta variable exponemos el estado para que la pantalla lo observe.
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Con esta función actualizamos el usuario y limpiamos sus errores.
    fun cambiarUsuario(usuario: String) {
        _uiState.update { estado ->
            estado.copy(
                usuario = usuario,
                errorUsuario = false,
                mensajeError = ""
            )
        }
    }

    // Con esta función actualizamos la contraseña y limpiamos sus errores.
    fun cambiarContrasena(contrasena: String) {
        _uiState.update { estado ->
            estado.copy(
                contrasena = contrasena,
                errorContrasena = false,
                mensajeError = ""
            )
        }
    }

    // Con esta función validamos los campos y comprobamos las credenciales.
    // Retornamos true si el acceso es correcto y false en caso contrario.
    fun iniciarSesion(): Boolean {
        val estadoActual = _uiState.value

        val usuarioValido = validarUsuarioLogin(estadoActual.usuario)
        val contrasenaValida = validarContrasenaLogin(estadoActual.contrasena)

        // Con estas condiciones identificamos los campos vacíos.
        if (!usuarioValido || !contrasenaValida) {
            _uiState.update { estado ->
                estado.copy(
                    errorUsuario = !usuarioValido,
                    errorContrasena = !contrasenaValida,
                    mensajeError = "Completa los campos indicados."
                )
            }

            return false
        }

        // Con esta condición comprobamos las credenciales de demostración.
        if (
            estadoActual.usuario.trim() == usuarioAdmin &&
            estadoActual.contrasena == claveAdmin
        ) {
            _uiState.update { estado ->
                estado.copy(
                    errorUsuario = false,
                    errorContrasena = false,
                    mensajeError = ""
                )
            }

            return true
        }

        // Con este mensaje informamos cuando las credenciales son incorrectas.
        _uiState.update { estado ->
            estado.copy(
                errorUsuario = false,
                errorContrasena = false,
                mensajeError = "El usuario o la contraseña son incorrectos."
            )
        }

        return false
    }

    // Con esta función limpiamos los datos al salir del inicio de sesión.
    fun limpiarFormulario() {
        _uiState.value = LoginUiState()
    }
}