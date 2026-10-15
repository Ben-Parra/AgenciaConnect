package cl.duoc.agenciaconnect.ui.login

// Con esta clase centralizamos los datos del acceso,
// los errores individuales y el resultado del inicio de sesión.
data class LoginUiState(

    // Con estas variables conservamos lo que escribe el usuario.
    val usuario: String = "",
    val contrasena: String = "",

    // Con estas variables indicamos si cada campo tiene un error.
    val errorUsuario: Boolean = false,
    val errorContrasena: Boolean = false,

    // Con este mensaje informamos el resultado del inicio de sesión.
    val mensajeError: String = ""
)