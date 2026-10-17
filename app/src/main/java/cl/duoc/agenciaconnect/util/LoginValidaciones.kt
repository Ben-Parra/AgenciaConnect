package cl.duoc.agenciaconnect.util

import android.util.Patterns

// Con esta función comprobamos que el correo no esté vacío y cumpla con el formato estándar.
fun validarCorreoLogin(correo: String): Boolean {
    val correoLimpio = correo.trim()
    return correoLimpio.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()
}

// Con esta función comprobamos que la contraseña ingresada no esté vacía.
fun validarContrasenaLogin(contrasena: String): Boolean {
    return contrasena.trim().isNotEmpty()
}