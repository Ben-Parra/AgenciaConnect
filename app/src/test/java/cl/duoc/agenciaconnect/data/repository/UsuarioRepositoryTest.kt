package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.UsuarioDao
import cl.duoc.agenciaconnect.data.model.Usuario
import cl.duoc.agenciaconnect.data.remote.AgenciaApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/**
 * Pruebas unitarias para UsuarioRepository, incluyendo autenticación.
 */
class UsuarioRepositoryTest {

    private lateinit var api: AgenciaApi
    private lateinit var dao: UsuarioDao
    private lateinit var repositorio: UsuarioRepository

    private val usuarioSupervisor = Usuario(
        id = 1L, nombre = "Patricia Vega", correo = "supervisor@agencia.cl",
        contrasena = "super123", rol = "SUPERVISOR"
    )
    private val usuarioEjecutivo = Usuario(
        id = 2L, nombre = "Carlos Muñoz", correo = "carlos.comercial@agencia.cl",
        contrasena = "eje123", rol = "EJECUTIVO"
    )

    @Before
    fun setUp() {
        // Organizamos: preparamos mocks.
        api = mockk(relaxed = true)
        dao = mockk(relaxed = true)
        coEvery { dao.obtenerUsuarios() } returns flowOf(emptyList())
        coEvery { dao.obtenerEjecutivos() } returns flowOf(emptyList())
        repositorio = UsuarioRepository(usuarioDao = dao, api = api)
    }

    // ── autenticar ────────────────────────────────────────────────────────

    @Test
    fun autenticar_credencialesCorrectas_retornaUsuario() = runTest {
        // Organizamos: DAO devuelve el usuario supervisor.
        coEvery { dao.autenticar("supervisor@agencia.cl", "super123") } returns usuarioSupervisor

        // Actuamos.
        val resultado = repositorio.autenticar("supervisor@agencia.cl", "super123")

        // Afirmamos: retorna el usuario correcto.
        assertEquals(usuarioSupervisor, resultado)
    }

    @Test
    fun autenticar_credencialesEjecutivo_retornaEjecutivo() = runTest {
        // Organizamos.
        coEvery { dao.autenticar("carlos.comercial@agencia.cl", "eje123") } returns usuarioEjecutivo

        // Actuamos.
        val resultado = repositorio.autenticar("carlos.comercial@agencia.cl", "eje123")

        // Afirmamos.
        assertEquals(usuarioEjecutivo, resultado)
    }

    @Test
    fun autenticar_credencialesIncorrectas_retornaNull() = runTest {
        // Organizamos: DAO no encuentra coincidencia.
        coEvery { dao.autenticar("x@x.cl", "mala") } returns null

        // Actuamos.
        val resultado = repositorio.autenticar("x@x.cl", "mala")

        // Afirmamos: retorna null sin lanzar excepción.
        assertNull(resultado)
    }

    @Test
    fun autenticar_correoConEspacios_trimConservado() = runTest {
        // Organizamos: correo con espacios al inicio; el repositorio hace trim.
        coEvery { dao.autenticar("supervisor@agencia.cl", "super123") } returns usuarioSupervisor

        // Actuamos: pasamos correo con espacio inicial.
        val resultado = repositorio.autenticar("  supervisor@agencia.cl", "super123")

        // Afirmamos: el repositorio hizo trim y encontró el usuario.
        assertEquals(usuarioSupervisor, resultado)
    }

    // ── sincronizarDesdeApi ───────────────────────────────────────────────

    @Test
    fun sincronizarDesdeApi_apiRetornaUsuarios_insertaEnDao() = runTest {
        // Organizamos.
        coEvery { api.obtenerUsuarios() } returns listOf(usuarioSupervisor, usuarioEjecutivo)

        // Actuamos.
        repositorio.sincronizarDesdeApi()

        // Afirmamos.
        coVerify { dao.insertarUsuarios(listOf(usuarioSupervisor, usuarioEjecutivo)) }
    }

    @Test
    fun sincronizarDesdeApi_listaVacia_noInserta() = runTest {
        // Organizamos: API devuelve lista vacía.
        coEvery { api.obtenerUsuarios() } returns emptyList()

        // Actuamos.
        repositorio.sincronizarDesdeApi()

        // Afirmamos: no llama a insertarUsuarios con lista vacía.
        coVerify(exactly = 0) { dao.insertarUsuarios(any()) }
    }

    @Test
    fun sincronizarDesdeApi_errorDeRed_noLanzaExcepcion() = runTest {
        // Organizamos.
        coEvery { api.obtenerUsuarios() } throws RuntimeException("Sin red")
        // Actuamos y afirmamos.
        repositorio.sincronizarDesdeApi()
    }

    // ── insertarUsuario ───────────────────────────────────────────────────

    @Test
    fun insertarUsuario_apiExitosa_insertaConIdDelServidor() = runTest {
        // Organizamos.
        val enviado = usuarioSupervisor.copy(id = 0L)
        val creado = usuarioSupervisor.copy(id = 50L)
        coEvery { api.crearUsuario(enviado) } returns Response.success(creado)

        // Actuamos.
        repositorio.insertarUsuario(enviado)

        // Afirmamos.
        coVerify { dao.insertarUsuario(creado) }
    }

    @Test
    fun insertarUsuario_apiFalla_guardaLocalmenteEnDao() = runTest {
        // Organizamos.
        val u = usuarioSupervisor.copy(id = 0L)
        coEvery { api.crearUsuario(u) } throws java.io.IOException("Sin red")

        // Actuamos.
        repositorio.insertarUsuario(u)

        // Afirmamos.
        coVerify { dao.insertarUsuario(u) }
    }

    // ── actualizarUsuario ─────────────────────────────────────────────────

    @Test
    fun actualizarUsuario_llamaApiYActualizaDao() = runTest {
        // Organizamos.
        val u = usuarioSupervisor

        // Actuamos.
        repositorio.actualizarUsuario(u)

        // Afirmamos.
        coVerify { api.actualizarUsuario(1L, u) }
        coVerify { dao.actualizarUsuario(u) }
    }

    // ── eliminarUsuario ───────────────────────────────────────────────────

    @Test
    fun eliminarUsuario_idPositivo_llamaApiYEliminaDao() = runTest {
        // Organizamos.
        val u = usuarioSupervisor

        // Actuamos.
        repositorio.eliminarUsuario(u)

        // Afirmamos.
        coVerify { api.eliminarUsuario(1L) }
        coVerify { dao.eliminarUsuario(u) }
    }

    @Test
    fun eliminarUsuario_idCero_noLlamaApi() = runTest {
        // Organizamos.
        val u = usuarioSupervisor.copy(id = 0L)

        // Actuamos.
        repositorio.eliminarUsuario(u)

        // Afirmamos.
        coVerify(exactly = 0) { api.eliminarUsuario(any()) }
        coVerify { dao.eliminarUsuario(u) }
    }
}
