package cl.duoc.agenciaconnect

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.BackHandler
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.agenciaconnect.ui.contactos.ContactoScreen
import cl.duoc.agenciaconnect.ui.contactos.ContactosRegistradosScreen
import cl.duoc.agenciaconnect.ui.eventos.EventosRegistradosScreen
import cl.duoc.agenciaconnect.ui.eventos.EventosScreen
import cl.duoc.agenciaconnect.ui.login.LoginViewModel
import cl.duoc.agenciaconnect.ui.oportunidades.OportunidadesRegistradasScreen
import cl.duoc.agenciaconnect.ui.oportunidades.OportunidadesScreen
import cl.duoc.agenciaconnect.ui.organizaciones.OrganizacionesRegistradasScreen
import cl.duoc.agenciaconnect.ui.organizaciones.OrganizacionesScreen
import cl.duoc.agenciaconnect.ui.theme.AgenciaConnectTheme

// Con esta actividad preparamos la aplicación y coordinamos
// la navegación entre el inicio, el acceso y la administración.
class MainActivity : ComponentActivity() {

    // Con esta función iniciamos la interfaz principal de la aplicación.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AgenciaConnectTheme {
                // Usamos una pila (stack) para recordar el historial de navegación
                val historialPantallas = remember { mutableStateOf(listOf("inicio")) }
                val pantallaActual = historialPantallas.value.last()

                var usuarioActual by remember { mutableStateOf<cl.duoc.agenciaconnect.data.model.Usuario?>(null) }
                var oportunidadSeleccionada by remember { mutableStateOf<cl.duoc.agenciaconnect.data.model.Oportunidad?>(null) }

                // ── MANEJO DEL BOTÓN ATRÁS FÍSICO ──────────────────────────────
                BackHandler(enabled = historialPantallas.value.size > 1) {
                    // Si hay más de una pantalla en el historial, al presionar atrás sacamos la última
                    historialPantallas.value = historialPantallas.value.dropLast(1)
                }

                // Función auxiliar para navegar hacia adelante agregando a la pila
                fun navegarA(nuevaPantalla: String) {
                    historialPantallas.value = historialPantallas.value + nuevaPantalla
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = pantallaActual,
                            transitionSpec = {
                                (slideInHorizontally(animationSpec = tween(300)) { ancho -> ancho } +
                                        fadeIn(animationSpec = tween(300))) togetherWith
                                        (slideOutHorizontally(animationSpec = tween(300)) { ancho -> -ancho } +
                                                fadeOut(animationSpec = tween(300)))
                            },
                            label = "Transición entre pantallas"
                        ) { pantalla ->
                            when (pantalla) {
                                "inicio" -> PantallaInicio(
                                    onAcceso = {
                                        navegarA("login")
                                    }
                                )

                                "login" -> PantallaLoginAdmin(
                                    onAccesoCorrecto = { usuario ->
                                        usuarioActual = usuario
                                        navegarA("admin")
                                    },
                                    onVolver = {
                                        // Si vuelve al inicio, limpiamos el historial y dejamos solo "inicio"
                                        historialPantallas.value = listOf("inicio")
                                    }
                                )

                                "admin" -> PantallaAdmin(
                                    usuario = usuarioActual,
                                    onGestionarEventos = { navegarA("eventos") },
                                    onGestionarOrganizaciones = { navegarA("organizaciones") },
                                    onGestionarContactos = { navegarA("contactos") },
                                    onGestionarOportunidades = { navegarA("oportunidades") },
                                    onCerrarSesion = {
                                        usuarioActual = null
                                        historialPantallas.value = listOf("inicio")
                                    }
                                )

                                "eventos" -> EventosScreen(
                                    onVolver = { historialPantallas.value = historialPantallas.value.dropLast(1) },
                                    onVerEventos = { navegarA("eventos_registrados") }
                                )

                                "eventos_registrados" -> EventosRegistradosScreen(
                                    onVolverFormulario = { historialPantallas.value = historialPantallas.value.dropLast(1) },
                                    onVolver = { historialPantallas.value = historialPantallas.value.dropLast(2) }, // Salta directo al admin
                                    onEditarEvento = { navegarA("eventos") }
                                )

                                "organizaciones" -> OrganizacionesScreen(
                                    onVolver = { historialPantallas.value = historialPantallas.value.dropLast(1) },
                                    onVerOrganizaciones = { navegarA("organizaciones_registradas") }
                                )

                                "organizaciones_registradas" -> OrganizacionesRegistradasScreen(
                                    onVolverFormulario = { historialPantallas.value = historialPantallas.value.dropLast(1) },
                                    onVolver = { historialPantallas.value = historialPantallas.value.dropLast(2) },
                                    onEditarOrganizacion = { navegarA("organizaciones") }
                                )

                                "contactos" -> ContactoScreen(
                                    onVolver = { historialPantallas.value = historialPantallas.value.dropLast(1) },
                                    onVerContactos = { navegarA("contactos_registrados") }
                                )

                                "contactos_registrados" -> ContactosRegistradosScreen(
                                    onVolverAlFormulario = { historialPantallas.value = historialPantallas.value.dropLast(1) },
                                    onVolver = { historialPantallas.value = historialPantallas.value.dropLast(2) },
                                    onEditarContacto = { navegarA("contactos") }
                                )

                                "oportunidades" -> OportunidadesScreen(
                                    usuarioActual = usuarioActual,
                                    onVolver = { historialPantallas.value = historialPantallas.value.dropLast(1) },
                                    onVerOportunidades = { navegarA("oportunidades_registradas") }
                                )

                                "oportunidades_registradas" -> OportunidadesRegistradasScreen(
                                    usuarioActual = usuarioActual,
                                    onVolverFormulario = { navegarA("oportunidades") },
                                    onVolver = { historialPantallas.value = historialPantallas.value.dropLast(2) },
                                    onEditarOportunidad = { navegarA("oportunidades") },
                                    onVerSeguimiento = { oportunidad ->
                                        oportunidadSeleccionada = oportunidad
                                        navegarA("seguimiento")
                                    }
                                )

                                "seguimiento" -> {
                                    oportunidadSeleccionada?.let { op ->
                                        cl.duoc.agenciaconnect.ui.seguimientos.SeguimientoScreen(
                                            oportunidad = op,
                                            usuarioActual = usuarioActual,
                                            onVolver = { historialPantallas.value = historialPantallas.value.dropLast(1) }
                                        )
                                    } ?: run {
                                        historialPantallas.value = historialPantallas.value.dropLast(1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Con esta función presentamos la aplicación y ofrecemos el acceso principal al sistema,
// incluyendo el logo corporativo integrado elegantemente en la cabecera.
@Composable
fun PantallaInicio(
    onAcceso: () -> Unit
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
            // ── LOGO CORPORATIVO EN LA CABECERA ──────────────────────────
            // Mostramos el logo de la app con un tamaño controlado (80.dp)
            // utilizando un contenedor circular o esquinas redondeadas.
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_logo),
                contentDescription = "Logo de AgenciaConnect",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AgenciaConnect",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Gestión de oportunidades y eventos corporativos",
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Centraliza tus eventos, oportunidades y seguimientos en un solo lugar.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onAcceso,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar sesión")
            }
        }
    }
}

// Con esta función mostramos el formulario de acceso, permitimos ingreso manual
// y ofrecemos una tarjeta informativa con botones de autocompletado rápido.
@Composable
fun PantallaLoginAdmin(
    onAccesoCorrecto: (cl.duoc.agenciaconnect.data.model.Usuario) -> Unit,
    onVolver: () -> Unit,
    loginViewModel: LoginViewModel = viewModel()
) {
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Acceso al sistema",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Con este campo ingresamos el correo institucional manualmente.
        OutlinedTextField(
            value = uiState.correo,
            onValueChange = loginViewModel::cambiarCorreo,
            label = { Text("Correo electrónico") },
            placeholder = { Text("ejemplo@agencia.cl") },
            singleLine = true,
            isError = uiState.errorCorreo,
            supportingText = {
                if (uiState.errorCorreo) {
                    Text("Ingresa un correo válido.")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Con este campo ingresamos la contraseña manualmente.
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

        // Con este bloque animado mostramos el mensaje de error de acceso
        // de forma fluida con expand/shrink en lugar de aparecer de golpe.
        AnimatedVisibility(
            visible = uiState.mensajeError.isNotBlank(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.mensajeError,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Con este botón ejecutamos la autenticación y pasamos el usuario autenticado sin desfase.
        // Aplicamos una microinteracción de escala con animateFloatAsState al pulsar.
        val interactionSourceIngresar = remember { MutableInteractionSource() }
        val presionadoIngresar by interactionSourceIngresar.collectIsPressedAsState()
        val escalaIngresar by animateFloatAsState(
            targetValue = if (presionadoIngresar) 0.96f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "Escala del botón ingresar"
        )

        Button(
            onClick = {
                loginViewModel.autenticar { usuarioLogueado ->
                    onAccesoCorrecto(usuarioLogueado)
                }
            },
            enabled = !uiState.cargando,
            interactionSource = interactionSourceIngresar,
            modifier = Modifier
                .fillMaxWidth()
                .scale(escalaIngresar)
        ) {
            Text(if (uiState.cargando) "Verificando..." else "Ingresar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                loginViewModel.cerrarSesion()
                onVolver()
            }
        ) {
            Text("Volver al inicio")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Con esta tarjeta mostramos las 4 credenciales de prueba exigidas por la evaluación.
        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Cuentas de prueba (4 cuentas exigidas)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "• Sup: supervisor@agencia.cl | super123\n" +
                            "• Eje 1: carlos.comercial@agencia.cl | eje123\n" +
                            "• Eje 2: marcela.ventas@agencia.cl | eje123\n" +
                            "• Eje 3: felipe.convenios@agencia.cl | eje123",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Autocompletar con un clic:",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            loginViewModel.cargarCredencialPrueba("supervisor@agencia.cl", "super123")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Supervisor", fontSize = 10.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            loginViewModel.cargarCredencialPrueba("carlos.comercial@agencia.cl", "eje123")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Eje 1", fontSize = 10.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            loginViewModel.cargarCredencialPrueba("marcela.ventas@agencia.cl", "eje123")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Eje 2", fontSize = 10.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            loginViewModel.cargarCredencialPrueba("felipe.convenios@agencia.cl", "eje123")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Eje 3", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

// Con esta función mostramos el panel principal, un dashboard de resumen y reflejamos el usuario autenticado.
@Composable
fun PantallaAdmin(
    usuario: cl.duoc.agenciaconnect.data.model.Usuario?,
    onGestionarEventos: () -> Unit,
    onGestionarOrganizaciones: () -> Unit,
    onGestionarContactos: () -> Unit,
    onGestionarOportunidades: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Panel de control",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Con este texto identificamos al colaborador y su perfil asignado.
        Text(
            text = "Usuario: ${usuario?.nombre ?: "Sin sesión"}",
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Rol activo: ${usuario?.rol ?: "INVITADO"}",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Tarjeta Dashboard / Resumen de Estado del Sistema
        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Resumen de Gestión (Dashboard)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = if (usuario?.rol == "SUPERVISOR") {
                        "• Visibilidad global del pipeline habilitada.\n• Gestión completa de eventos y asignaciones."
                    } else {
                        "• Vista filtrada a tus oportunidades asignadas.\n• Registro de seguimientos y calendario activo."
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onGestionarEventos,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gestionar eventos")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onGestionarOrganizaciones,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gestionar organizaciones")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onGestionarContactos,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gestionar contactos")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onGestionarOportunidades,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gestionar oportunidades")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}

// Con esta función previsualizamos la pantalla inicial desde las herramientas de diseño.
@Preview(showBackground = true)
@Composable
fun VistaPreviaInicio() {
    AgenciaConnectTheme {
        PantallaInicio(onAcceso = {})
    }
}