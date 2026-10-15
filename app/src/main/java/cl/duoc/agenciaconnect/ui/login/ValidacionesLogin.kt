package cl.duoc.agenciaconnect.ui.login

// Con esta función comprobamos que se haya ingresado un usuario.
fun validarUsuarioLogin(usuario: String): Boolean {
    return usuario.isNotBlank()
}

// Con esta función comprobamos que se haya ingresado una contraseña.
fun validarContrasenaLogin(contrasena: String): Boolean {
    return contrasena.isNotBlank()
}