package cl.duoc.agenciaconnect.ui.login

import cl.duoc.agenciaconnect.data.model.Usuario

// Con esta clase centralizamos el estado de la pantalla de acceso y el usuario autenticado.
data class LoginUiState(

    // Con estos campos conservamos los datos escritos en el formulario.
    val correo: String = "",
    val contrasena: String = "",

    // Con estos indicadores controlamos los mensajes de error por campo.
    val errorCorreo: Boolean = false,
    val errorContrasena: Boolean = false,

    // Con este indicador informamos si la autenticación está en proceso.
    val cargando: Boolean = false,

    // Con este mensaje informamos fallos de credenciales o errores generales.
    val mensajeError: String = "",

    // Con este campo conservamos los datos del usuario que inició sesión correctamente.
    val usuarioAutenticado: Usuario? = null
)