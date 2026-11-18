package cl.duoc.agenciaconnect

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.agenciaconnect.ui.contactos.ContactoScreen
import cl.duoc.agenciaconnect.ui.contactos.ContactosRegistradosScreen
import cl.duoc.agenciaconnect.ui.contactos.ContactosViewModel
import cl.duoc.agenciaconnect.ui.eventos.EventoViewModel
import cl.duoc.agenciaconnect.ui.eventos.EventosRegistradosScreen
import cl.duoc.agenciaconnect.ui.eventos.EventosScreen
import cl.duoc.agenciaconnect.ui.login.LoginViewModel
import cl.duoc.agenciaconnect.ui.oportunidades.OportunidadViewModel
import cl.duoc.agenciaconnect.ui.oportunidades.OportunidadesRegistradasScreen
import cl.duoc.agenciaconnect.ui.oportunidades.OportunidadesScreen
import cl.duoc.agenciaconnect.ui.organizaciones.OrganizacionViewModel
import cl.duoc.agenciaconnect.ui.organizaciones.OrganizacionesRegistradasScreen
import cl.duoc.agenciaconnect.ui.organizaciones.OrganizacionesScreen
import cl.duoc.agenciaconnect.ui.seguimientos.SeguimientoScreen
import cl.duoc.agenciaconnect.ui.theme.AgenciaConnectTheme

// Con esta actividad preparamos la aplicación y coordinamos
// la navegación entre el inicio, el acceso y la administración.
class MainActivity : ComponentActivity() {

    // Con esta función iniciamos la interfaz principal de la aplicación.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AgenciaConnectTheme {
                // Usamos NavController para gestionar la navegación y mantener el estado al rotar la pantalla
                val navController = rememberNavController()

                val loginViewModel: LoginViewModel = viewModel()
                val loginUiState by loginViewModel.uiState.collectAsStateWithLifecycle()
                val usuarioActual = loginUiState.usuarioAutenticado

                val eventoViewModel: EventoViewModel = viewModel()
                val eventoUiState by eventoViewModel.uiState.collectAsStateWithLifecycle()

                val organizacionViewModel: OrganizacionViewModel = viewModel()
                val organizacionUiState by organizacionViewModel.uiState.collectAsStateWithLifecycle()

                val contactosViewModel: ContactosViewModel = viewModel()
                val contactosUiState by contactosViewModel.uiState.collectAsStateWithLifecycle()

                val oportunidadViewModel: OportunidadViewModel = viewModel()
                val oportunidadUiState by oportunidadViewModel.uiState.collectAsStateWithLifecycle()

                // Criterio de conteo reactivo de Oportunidades según el Rol del usuario
                val esSupervisor = usuarioActual?.rol == "SUPERVISOR"
                val conteoOportunidades = if (esSupervisor) {
                    oportunidadUiState.oportunidades.size
                } else {
                    val normUser = usuarioActual?.nombre.orEmpty().replace(Regex("\\s*\\(.*?\\)"), "").trim()
                    oportunidadUiState.oportunidades.count { op ->
                        val normOp = op.responsable.replace(Regex("\\s*\\(.*?\\)"), "").trim()
                        normOp.equals(normUser, ignoreCase = true) ||
                                op.responsable.trim().equals(usuarioActual?.nombre.orEmpty().trim(), ignoreCase = true)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "inicio",
                            enterTransition = { slideInHorizontally(animationSpec = tween(300)) { ancho -> ancho } + fadeIn(animationSpec = tween(300)) },
                            exitTransition = { slideOutHorizontally(animationSpec = tween(300)) { ancho -> -ancho } + fadeOut(animationSpec = tween(300)) },
                            popEnterTransition = { slideInHorizontally(animationSpec = tween(300)) { ancho -> -ancho } + fadeIn(animationSpec = tween(300)) },
                            popExitTransition = { slideOutHorizontally(animationSpec = tween(300)) { ancho -> ancho } + fadeOut(animationSpec = tween(300)) }
                        ) {
                            composable("inicio") {
                                PantallaInicio(
                                    onAcceso = {
                                        navController.navigate("login")
                                    }
                                )
                            }

                            composable("login") {
                                PantallaLoginAdmin(
                                    onAccesoCorrecto = { _ ->
                                        navController.navigate("admin") {
                                            popUpTo("inicio") { inclusive = false }
                                        }
                                    },
                                    onVolver = {
                                        navController.navigate("inicio") {
                                            popUpTo("inicio") { inclusive = true }
                                        }
                                    },
                                    loginViewModel = loginViewModel
                                )
                            }

                            composable("admin") {
                                PantallaAdmin(
                                    usuario = usuarioActual,
                                    conteoOportunidades = conteoOportunidades,
                                    conteoEventos = eventoUiState.eventos.size,
                                    conteoOrganizaciones = organizacionUiState.organizaciones.size,
                                    conteoContactos = contactosUiState.contactos.size,
                                    onGestionarEventos = {
                                        eventoViewModel.limpiarFormulario()
                                        navController.navigate("eventos")
                                    },
                                    onGestionarOrganizaciones = {
                                        organizacionViewModel.limpiarFormulario()
                                        navController.navigate("organizaciones")
                                    },
                                    onGestionarContactos = {
                                        contactosViewModel.reiniciarFormularioAlSalir()
                                        navController.navigate("contactos")
                                    },
                                    onGestionarOportunidades = {
                                        oportunidadViewModel.limpiarFormulario()
                                        navController.navigate("oportunidades")
                                    },
                                    onCerrarSesion = {
                                        loginViewModel.cerrarSesion()
                                        navController.navigate("inicio") {
                                            popUpTo("inicio") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable("eventos") {
                                EventosScreen(
                                    onVolver = { navController.popBackStack("admin", inclusive = false) },
                                    onVerEventos = { navController.navigate("eventos_registrados") },
                                    viewModel = eventoViewModel
                                )
                            }

                            composable("eventos_registrados") {
                                EventosRegistradosScreen(
                                    onVolverFormulario = {
                                        eventoViewModel.limpiarFormulario()
                                        navController.navigate("eventos")
                                    },
                                    onVolver = { navController.popBackStack("admin", inclusive = false) },
                                    onEditarEvento = { navController.navigate("eventos") },
                                    viewModel = eventoViewModel
                                )
                            }

                            composable("organizaciones") {
                                OrganizacionesScreen(
                                    onVolver = { navController.popBackStack("admin", inclusive = false) },
                                    onVerOrganizaciones = { navController.navigate("organizaciones_registradas") },
                                    viewModel = organizacionViewModel
                                )
                            }

                            composable("organizaciones_registradas") {
                                OrganizacionesRegistradasScreen(
                                    onVolverFormulario = {
                                        organizacionViewModel.limpiarFormulario()
                                        navController.navigate("organizaciones")
                                    },
                                    onVolver = { navController.popBackStack("admin", inclusive = false) },
                                    onEditarOrganizacion = { navController.navigate("organizaciones") },
                                    viewModel = organizacionViewModel
                                )
                            }

                            composable("contactos") {
                                ContactoScreen(
                                    onVolver = { navController.popBackStack("admin", inclusive = false) },
                                    onVerContactos = { navController.navigate("contactos_registrados") },
                                    viewModel = contactosViewModel
                                )
                            }

                            composable("contactos_registrados") {
                                ContactosRegistradosScreen(
                                    onVolverAlFormulario = {
                                        contactosViewModel.reiniciarFormularioAlSalir()
                                        navController.navigate("contactos")
                                    },
                                    onVolver = { navController.popBackStack("admin", inclusive = false) },
                                    onEditarContacto = { navController.navigate("contactos") },
                                    viewModel = contactosViewModel
                                )
                            }

                            composable("oportunidades") {
                                OportunidadesScreen(
                                    usuarioActual = usuarioActual,
                                    onVolver = { navController.popBackStack("admin", inclusive = false) },
                                    onVerOportunidades = { navController.navigate("oportunidades_registradas") },
                                    viewModel = oportunidadViewModel
                                )
                            }

                            composable("oportunidades_registradas") {
                                OportunidadesRegistradasScreen(
                                    usuarioActual = usuarioActual,
                                    onVolverFormulario = {
                                        oportunidadViewModel.limpiarFormulario()
                                        navController.navigate("oportunidades")
                                    },
                                    onVolver = { navController.popBackStack("admin", inclusive = false) },
                                    onEditarOportunidad = { navController.navigate("oportunidades") },
                                    onVerSeguimiento = { oportunidad ->
                                        oportunidadViewModel.oportunidadSeleccionada = oportunidad
                                        navController.navigate("seguimiento")
                                    },
                                    viewModel = oportunidadViewModel
                                )
                            }

                            composable("seguimiento") {
                                oportunidadViewModel.oportunidadSeleccionada?.let { op ->
                                    SeguimientoScreen(
                                        oportunidad = op,
                                        usuarioActual = usuarioActual,
                                        onVolver = { navController.popBackStack() }
                                    )
                                } ?: run {
                                    navController.popBackStack()
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

// Renderizamos la pantalla del panel administrativo principal (Dashboard).
// Evaluamos el rol del usuario autenticado (Supervisor vs Ejecutivo) y distribuimos las métricas reactivas en tiempo real.
@Composable
fun PantallaAdmin(
    usuario: cl.duoc.agenciaconnect.data.model.Usuario?,
    conteoOportunidades: Int,
    conteoEventos: Int,
    conteoOrganizaciones: Int,
    conteoContactos: Int,
    onGestionarEventos: () -> Unit,
    onGestionarOrganizaciones: () -> Unit,
    onGestionarContactos: () -> Unit,
    onGestionarOportunidades: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val esSupervisor = usuario?.rol == "SUPERVISOR"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Panel de control",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Con este texto identificamos al colaborador y su perfil asignado.
        Text(
            text = "Usuario: ${usuario?.nombre ?: "Sin sesión"}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Rol activo: ${usuario?.rol ?: "INVITADO"}",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta Dashboard / Resumen de Estado del Sistema según el Rol
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (esSupervisor) "📊 Vista General del Supervisor" else "📋 Tu Resumen Ejecutivo",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (esSupervisor) {
                        "• Visibilidad global del pipeline habilitada.\n• Gestión completa de eventos y asignaciones."
                    } else {
                        "• Vista filtrada a tus oportunidades asignadas.\n• Registro de seguimientos y calendario activo."
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Módulos principales",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Cuadrícula 2x2 con Tarjetas Dashboard Interactivas
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Fila 1: Oportunidades & Eventos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TarjetaDashboardModulo(
                    titulo = "Oportunidades",
                    conteo = conteoOportunidades,
                    etiquetaContexto = if (esSupervisor) "registradas" else "asignadas a ti",
                    iconoEmoji = "🎯",
                    colorContenedor = MaterialTheme.colorScheme.primaryContainer,
                    onClick = onGestionarOportunidades,
                    modifier = Modifier.weight(1f)
                )

                TarjetaDashboardModulo(
                    titulo = "Eventos",
                    conteo = conteoEventos,
                    etiquetaContexto = "registrados",
                    iconoEmoji = "🗓",
                    colorContenedor = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = onGestionarEventos,
                    modifier = Modifier.weight(1f)
                )
            }

            // Fila 2: Organizaciones & Contactos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TarjetaDashboardModulo(
                    titulo = "Organizaciones",
                    conteo = conteoOrganizaciones,
                    etiquetaContexto = "registradas",
                    iconoEmoji = "🏢",
                    colorContenedor = MaterialTheme.colorScheme.tertiaryContainer,
                    onClick = onGestionarOrganizaciones,
                    modifier = Modifier.weight(1f)
                )

                TarjetaDashboardModulo(
                    titulo = "Contactos",
                    conteo = conteoContactos,
                    etiquetaContexto = "registrados",
                    iconoEmoji = "👥",
                    colorContenedor = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = onGestionarContactos,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// Construimos cada tarjeta interactiva del Dashboard con su contenedor circular, conteo en tiempo real e indicador de navegación.
@Composable
private fun TarjetaDashboardModulo(
    titulo: String,
    conteo: Int,
    etiquetaContexto: String,
    iconoEmoji: String,
    colorContenedor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = colorContenedor,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = iconoEmoji, fontSize = 20.sp)
                    }
                }

                Text(
                    text = "➔",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Text(
                text = conteo.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = etiquetaContexto,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
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