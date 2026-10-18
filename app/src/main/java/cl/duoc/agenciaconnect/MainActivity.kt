package cl.duoc.agenciaconnect

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
                // Con esta variable controlamos la pantalla visible en cada momento.
                var pantalla by remember { mutableStateOf("inicio") }

                // Con esta variable recordamos al usuario que inició sesión para controlar sus permisos y rol.
                var usuarioActual by remember { mutableStateOf<cl.duoc.agenciaconnect.data.model.Usuario?>(null) }
                // Con esta variable guardamos la oportunidad seleccionada para revisar su bitácora de seguimiento.
                var oportunidadSeleccionada by remember { mutableStateOf<cl.duoc.agenciaconnect.data.model.Oportunidad?>(null) }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Con este contenedor animado aplicamos transiciones fluidas
                        // entre las pantallas del sistema (slide + fade), cumpliendo el
                        // requisito de transición animada entre pantallas del enrutador.
                        AnimatedContent(
                            targetState = pantalla,
                            transitionSpec = {
                                (slideInHorizontally(animationSpec = tween(300)) { ancho -> ancho } +
                                    fadeIn(animationSpec = tween(300))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(300)) { ancho -> -ancho } +
                                    fadeOut(animationSpec = tween(300)))
                            },
                            label = "Transición entre pantallas"
                        ) { pantallaActual ->
                        when (pantallaActual) {
                            "inicio" -> PantallaInicio(
                                onAcceso = {
                                    pantalla = "login"
                                }
                            )

                            "login" -> PantallaLoginAdmin(
                                onAccesoCorrecto = { usuario ->
                                    usuarioActual = usuario
                                    pantalla = "admin"
                                },
                                onVolver = {
                                    pantalla = "inicio"
                                }
                            )

                            "admin" -> PantallaAdmin(
                                usuario = usuarioActual,
                                onGestionarEventos = {
                                    pantalla = "eventos"
                                },
                                onGestionarOrganizaciones = {
                                    pantalla = "organizaciones"
                                },
                                onGestionarContactos = {
                                    pantalla = "contactos"
                                },
                                onGestionarOportunidades = {
                                    pantalla = "oportunidades"
                                },
                                onCerrarSesion = {
                                    usuarioActual = null
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
                                onVolverFormulario = { pantalla = "eventos" },
                                onVolver = { pantalla = "admin" },
                                onEditarEvento = { pantalla = "eventos" }
                            )

                            "organizaciones" -> OrganizacionesScreen(
                                onVolver = {
                                    pantalla = "admin"
                                },
                                onVerOrganizaciones = {
                                    pantalla = "organizaciones_registradas"
                                }
                            )

                            "organizaciones_registradas" -> OrganizacionesRegistradasScreen(
                                onVolverFormulario = { pantalla = "organizaciones" },
                                onVolver = { pantalla = "admin" },
                                onEditarOrganizacion = { pantalla = "organizaciones" }
                            )

                            // Con esta pantalla registramos contactos y editamos sus datos.
                            "contactos" -> ContactoScreen(
                                onVolver = {
                                    pantalla = "admin"
                                },
                                onVerContactos = {
                                    pantalla = "contactos_registrados"
                                }
                            )

                            // Con esta pantalla consultamos, editamos y eliminamos contactos.
                            "contactos_registrados" -> ContactosRegistradosScreen(
                                onVolverAlFormulario = {
                                    pantalla = "contactos"
                                },
                                onVolver = {
                                    pantalla = "admin"
                                },
                                onEditarContacto = {
                                    pantalla = "contactos"
                                }
                            )

                            // Con esta pantalla registramos oportunidades comerciales respetando los permisos del rol.
                            "oportunidades" -> OportunidadesScreen(
                                usuarioActual = usuarioActual,
                                onVolver = {
                                    pantalla = "admin"
                                },
                                onVerOportunidades = {
                                    pantalla = "oportunidades_registradas"
                                }
                            )

                            // Con esta pantalla consultamos, filtramos y gestionamos oportunidades según el rol.
                            "oportunidades_registradas" -> OportunidadesRegistradasScreen(
                                usuarioActual = usuarioActual,
                                onVolverFormulario = {
                                    pantalla = "oportunidades"
                                },
                                onVolver = {
                                    pantalla = "admin"
                                },
                                onEditarOportunidad = {
                                    pantalla = "oportunidades"
                                },
                                onVerSeguimiento = { oportunidad ->
                                    oportunidadSeleccionada = oportunidad
                                    pantalla = "seguimiento"
                                }
                            )

                            // Con esta pantalla revisamos la bitácora de seguimiento y agendamos en el calendario nativo.
                            "seguimiento" -> {
                                oportunidadSeleccionada?.let { op ->
                                    cl.duoc.agenciaconnect.ui.seguimientos.SeguimientoScreen(
                                        oportunidad = op,
                                        usuarioActual = usuarioActual,
                                        onVolver = {
                                            pantalla = "oportunidades_registradas"
                                        }
                                    )
                                } ?: run {
                                    pantalla = "oportunidades_registradas"
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

// Con esta función presentamos la aplicación y ofrecemos el acceso principal al sistema.
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

// Con esta función mostramos el panel principal y reflejamos el usuario autenticado.
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

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(28.dp))

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