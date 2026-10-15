package cl.duoc.agenciaconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.agenciaconnect.ui.eventos.EventosScreen
import cl.duoc.agenciaconnect.ui.login.LoginViewModel
import cl.duoc.agenciaconnect.ui.theme.AgenciaConnectTheme
import cl.duoc.agenciaconnect.ui.eventos.EventosRegistradosScreen

// Con esta actividad preparamos la aplicación y coordinamos
// la navegación entre el inicio, el acceso y la administración.
class MainActivity : ComponentActivity() {

    // Con esta función iniciamos la interfaz principal de la aplicación.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AgenciaConnectTheme {
                var pantalla by remember { mutableStateOf("inicio") }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (pantalla) {
                            "inicio" -> PantallaInicio(
                                onAccesoAdmin = {
                                    pantalla = "login"
                                }
                            )

                            "login" -> PantallaLoginAdmin(
                                onAccesoCorrecto = {
                                    pantalla = "admin"
                                },
                                onVolver = {
                                    pantalla = "inicio"
                                }
                            )

                            "admin" -> PantallaAdmin(
                                onGestionarEventos = {
                                    pantalla = "eventos"
                                },
                                onCerrarSesion = {
                                    pantalla = "inicio"
                                }
                            )

                            "eventos" -> EventosScreen(
                                onVolver = {
                                    pantalla = "admin"
                                },
                                onVerEventos = {
                                    pantalla = "eventos_registrados"
                                }
                            )

                            "eventos_registrados" -> EventosRegistradosScreen(
                                onVolverFormulario = {
                                    pantalla = "eventos"
                                },
                                onEditarEvento = {
                                    pantalla = "eventos"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Con esta función presentamos la aplicación y ofrecemos
// el acceso a la sección administrativa.
@Composable
fun PantallaInicio(
    onAccesoAdmin: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "AgenciaConnect",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Gestión de oportunidades y eventos corporativos",
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Centraliza tus eventos, oportunidades y seguimientos en un solo lugar."
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onAccesoAdmin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Acceso administrador")
            }
        }
    }
}

// Con esta función mostramos el formulario de acceso y delegamos
// la validación de las credenciales al LoginViewModel.
@Composable
fun PantallaLoginAdmin(
    onAccesoCorrecto: () -> Unit,
    onVolver: () -> Unit,
    loginViewModel: LoginViewModel = viewModel()
) {
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Acceso administrador",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.usuario,
            onValueChange = loginViewModel::cambiarUsuario,
            label = { Text("Usuario") },
            singleLine = true,
            isError = uiState.errorUsuario,
            supportingText = {
                if (uiState.errorUsuario) {
                    Text("Ingresa tu usuario de administrador.")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.contrasena,
            onValueChange = loginViewModel::cambiarContrasena,
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = uiState.errorContrasena,
            supportingText = {
                if (uiState.errorContrasena) {
                    Text("Ingresa tu contraseña.")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.mensajeError.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = uiState.mensajeError,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (loginViewModel.iniciarSesion()) {
                    loginViewModel.limpiarFormulario()
                    onAccesoCorrecto()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                loginViewModel.limpiarFormulario()
                onVolver()
            }
        ) {
            Text("Volver al inicio")
        }
    }
}

// Con esta función mostramos el panel administrativo y
// ofrecemos el acceso a la gestión de eventos.
@Composable
fun PantallaAdmin(
    onGestionarEventos: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Panel de administración",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Sesión de administrador iniciada correctamente.")

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onGestionarEventos,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gestionar eventos")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}

// Con esta función previsualizamos la pantalla inicial
// desde las herramientas de diseño de Android Studio.
@Preview(showBackground = true)
@Composable
fun VistaPreviaInicio() {
    AgenciaConnectTheme {
        PantallaInicio(onAccesoAdmin = {})
    }
}