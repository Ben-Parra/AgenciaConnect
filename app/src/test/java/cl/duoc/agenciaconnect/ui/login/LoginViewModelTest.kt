package cl.duoc.agenciaconnect.ui.login

import android.app.Application
import cl.duoc.agenciaconnect.MainDispatcherRule
import cl.duoc.agenciaconnect.data.local.AgenciaDatabase
import cl.duoc.agenciaconnect.data.local.UsuarioDao
import cl.duoc.agenciaconnect.data.model.Usuario
import cl.duoc.agenciaconnect.data.repository.UsuarioRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var application: Application
    private lateinit var mockDatabase: AgenciaDatabase
    private lateinit var mockUsuarioDao: UsuarioDao
    private lateinit var viewModel: LoginViewModel

    private val supervisor = Usuario(
        id = 1L, nombre = "Patricia Vega", correo = "supervisor@agencia.cl",
        contrasena = "super123", rol = "SUPERVISOR"
    )
    private val ejecutivo = Usuario(
        id = 2L, nombre = "Carlos Muñoz", correo = "carlos.comercial@agencia.cl",
        contrasena = "eje123", rol = "EJECUTIVO"
    )

    @Before
    fun setUp() {
        application = mockk(relaxed = true)
        mockUsuarioDao = mockk(relaxed = true)
        mockDatabase = mockk(relaxed = true)

        every { mockDatabase.usuarioDao() } returns mockUsuarioDao
        every { mockUsuarioDao.obtenerUsuarios() } returns flowOf(emptyList())
        every { mockUsuarioDao.obtenerEjecutivos() } returns flowOf(emptyList())
        coEvery { mockUsuarioDao.autenticar(any(), any()) } returns null

        mockkObject(AgenciaDatabase)
        every { AgenciaDatabase.obtenerInstancia(any()) } returns mockDatabase

        mockkConstructor(UsuarioRepository::class)
        coEvery { anyConstructed<UsuarioRepository>().sincronizarDesdeApi() } returns Unit

        viewModel = LoginViewModel(application)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun mockearValidacionesLogin() {
        mockkStatic("cl.duoc.agenciaconnect.util.LoginValidacionesKt")
        every { cl.duoc.agenciaconnect.util.validarCorreoLogin(any()) } returns true
        every { cl.duoc.agenciaconnect.util.validarContrasenaLogin(any()) } returns true
    }

    // ── Estado inicial ────────────────────────────────────────────────────

    @Test
    fun estadoInicial_camposVaciosYSinErrores() {
        val estado = viewModel.uiState.value
        assertEquals("", estado.correo)
        assertEquals("", estado.contrasena)
        assertFalse(estado.errorCorreo)
        assertFalse(estado.errorContrasena)
        assertNull(estado.usuarioAutenticado)
    }

    // ── cambiarCorreo / cambiarContrasena ─────────────────────────────────

    @Test
    fun cambiarCorreo_actualizaCorreoYLimpiaError() {
        viewModel.cambiarCorreo("supervisor@agencia.cl")
        assertEquals("supervisor@agencia.cl", viewModel.uiState.value.correo)
        assertFalse(viewModel.uiState.value.errorCorreo)
    }

    @Test
    fun cambiarContrasena_actualizaContrasenaYLimpiaError() {
        viewModel.cambiarContrasena("super123")
        assertEquals("super123", viewModel.uiState.value.contrasena)
        assertFalse(viewModel.uiState.value.errorContrasena)
    }

    // ── cargarCredencialPrueba ────────────────────────────────────────────

    @Test
    fun cargarCredencialPrueba_estableceAmbosValores() {
        viewModel.cargarCredencialPrueba("supervisor@agencia.cl", "super123")
        val estado = viewModel.uiState.value
        assertEquals("supervisor@agencia.cl", estado.correo)
        assertEquals("super123", estado.contrasena)
    }

    // ── autenticar con validaciones ──────────────────────────────────────

    @Test
    fun autenticar_camposVacios_marcaErroresYNoContinua() {
        viewModel.autenticar {}
        val estado = viewModel.uiState.value
        assertTrue(estado.errorCorreo)
        assertTrue(estado.errorContrasena)
        assertNull(estado.usuarioAutenticado)
    }

    @Test
    fun autenticar_supervisorExitoso_llamaCallbackYEstableceUsuario() = runTest {
        mockearValidacionesLogin()
        coEvery { anyConstructed<UsuarioRepository>().autenticar("supervisor@agencia.cl", "super123") } returns supervisor

        viewModel.cambiarCorreo("supervisor@agencia.cl")
        viewModel.cambiarContrasena("super123")
        var exitoLlamado = false

        viewModel.autenticar { usuario ->
            exitoLlamado = true
            assertEquals(supervisor, usuario)
        }
        advanceUntilIdle()

        assertTrue(exitoLlamado)
        assertEquals(supervisor, viewModel.uiState.value.usuarioAutenticado)
        assertEquals("", viewModel.uiState.value.mensajeError)
        assertFalse(viewModel.uiState.value.cargando)
    }

    @Test
    fun autenticar_credencialesIncorrectasEnServidor_estableceMensajeError() = runTest {
        mockearValidacionesLogin()
        coEvery { anyConstructed<UsuarioRepository>().autenticar(any(), any()) } returns null

        viewModel.cambiarCorreo("invalido@agencia.cl")
        viewModel.cambiarContrasena("bad123")

        viewModel.autenticar {}
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.usuarioAutenticado)
        assertEquals("Correo o contraseña incorrectos. Revisa las cuentas de prueba.", viewModel.uiState.value.mensajeError)
        assertFalse(viewModel.uiState.value.cargando)
    }

    @Test
    fun autenticar_excepcionDeRed_muestraMensajeErrorConexion() = runTest {
        mockearValidacionesLogin()
        coEvery { anyConstructed<UsuarioRepository>().autenticar(any(), any()) } throws RuntimeException("Error BD")

        viewModel.cambiarCorreo("supervisor@agencia.cl")
        viewModel.cambiarContrasena("super123")

        viewModel.autenticar {}
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.usuarioAutenticado)
        assertEquals("Error al conectar con la base de datos.", viewModel.uiState.value.mensajeError)
        assertFalse(viewModel.uiState.value.cargando)
    }

    @Test
    fun autenticar_rolSupervisor_esCorrecto() {
        assertEquals("SUPERVISOR", supervisor.rol)
    }

    @Test
    fun autenticar_rolEjecutivo_esCorrecto() {
        assertEquals("EJECUTIVO", ejecutivo.rol)
    }

    // ── cerrarSesion ──────────────────────────────────────────────────────

    @Test
    fun cerrarSesion_limpiaTodoElEstado() {
        viewModel.cambiarCorreo("supervisor@agencia.cl")
        viewModel.cambiarContrasena("super123")

        viewModel.cerrarSesion()

        val estado = viewModel.uiState.value
        assertEquals("", estado.correo)
        assertEquals("", estado.contrasena)
        assertNull(estado.usuarioAutenticado)
        assertEquals("", estado.mensajeError)
    }
}
