package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.UsuarioDao
import cl.duoc.agenciaconnect.data.model.Usuario
import cl.duoc.agenciaconnect.data.remote.AgenciaApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
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
        api = mockk(relaxed = true)
        dao = mockk(relaxed = true)
        coEvery { dao.obtenerUsuarios() } returns flowOf(emptyList())
        coEvery { dao.obtenerEjecutivos() } returns flowOf(emptyList())
        repositorio = UsuarioRepository(usuarioDao = dao, api = api)
    }

    // ── autenticar ────────────────────────────────────────────────────────

    @Test
    fun autenticar_credencialesCorrectas_retornaUsuario() = runTest {
        coEvery { dao.autenticar("supervisor@agencia.cl", "super123") } returns usuarioSupervisor

        val resultado = repositorio.autenticar("supervisor@agencia.cl", "super123")

        assertEquals(usuarioSupervisor, resultado)
    }

    @Test
    fun autenticar_credencialesEjecutivo_retornaEjecutivo() = runTest {
        coEvery { dao.autenticar("carlos.comercial@agencia.cl", "eje123") } returns usuarioEjecutivo

        val resultado = repositorio.autenticar("carlos.comercial@agencia.cl", "eje123")

        assertEquals(usuarioEjecutivo, resultado)
    }

    @Test
    fun autenticar_credencialesIncorrectas_retornaNull() = runTest {
        coEvery { dao.autenticar("x@x.cl", "mala") } returns null

        val resultado = repositorio.autenticar("x@x.cl", "mala")

        assertNull(resultado)
    }

    @Test
    fun autenticar_correoConEspacios_trimConservado() = runTest {
        coEvery { dao.autenticar("supervisor@agencia.cl", "super123") } returns usuarioSupervisor

        val resultado = repositorio.autenticar("  supervisor@agencia.cl", "super123")

        assertEquals(usuarioSupervisor, resultado)
    }

    // ── sincronizarDesdeApi ───────────────────────────────────────────────

    @Test
    fun sincronizarDesdeApi_apiRetornaUsuarios_insertaEnDao() = runTest {
        coEvery { api.obtenerUsuarios() } returns listOf(usuarioSupervisor, usuarioEjecutivo)

        repositorio.sincronizarDesdeApi()

        coVerify { dao.insertarUsuarios(listOf(usuarioSupervisor, usuarioEjecutivo)) }
    }

    @Test
    fun sincronizarDesdeApi_listaVacia_noInserta() = runTest {
        coEvery { api.obtenerUsuarios() } returns emptyList()

        repositorio.sincronizarDesdeApi()

        coVerify(exactly = 0) { dao.insertarUsuarios(any()) }
    }

    @Test
    fun sincronizarDesdeApi_errorDeRed_noLanzaExcepcion() = runTest {
        coEvery { api.obtenerUsuarios() } throws RuntimeException("Sin red")
        repositorio.sincronizarDesdeApi()
    }

    // ── insertarUsuario ───────────────────────────────────────────────────

    @Test
    fun insertarUsuario_apiExitosa_insertaConIdDelServidor() = runTest {
        val enviado = usuarioSupervisor.copy(id = 0L)
        val creado = usuarioSupervisor.copy(id = 50L)
        coEvery { api.crearUsuario(enviado) } returns Response.success(creado)

        repositorio.insertarUsuario(enviado)

        coVerify { dao.insertarUsuario(creado) }
    }

    @Test
    fun insertarUsuario_apiFalla_guardaLocalmenteEnDao() = runTest {
        val u = usuarioSupervisor.copy(id = 0L)
        coEvery { api.crearUsuario(u) } throws java.io.IOException("Sin red")

        repositorio.insertarUsuario(u)

        coVerify { dao.insertarUsuario(u) }
    }

    @Test
    fun insertarUsuario_apiRespuestaNoExitosa_guardaLocalmenteEnDao() = runTest {
        val u = usuarioSupervisor.copy(id = 0L)
        coEvery { api.crearUsuario(u) } returns Response.error(
            400, ResponseBody.create(null, "")
        )

        repositorio.insertarUsuario(u)

        coVerify { dao.insertarUsuario(u) }
    }

    // ── actualizarUsuario ─────────────────────────────────────────────────

    @Test
    fun actualizarUsuario_llamaApiYActualizaDao() = runTest {
        val u = usuarioSupervisor

        repositorio.actualizarUsuario(u)

        coVerify { api.actualizarUsuario(1L, u) }
        coVerify { dao.actualizarUsuario(u) }
    }

    // ── eliminarUsuario ───────────────────────────────────────────────────

    @Test
    fun eliminarUsuario_idPositivo_llamaApiYEliminaDao() = runTest {
        val u = usuarioSupervisor

        repositorio.eliminarUsuario(u)

        coVerify { api.eliminarUsuario(1L) }
        coVerify { dao.eliminarUsuario(u) }
    }

    @Test
    fun eliminarUsuario_idCero_noLlamaApi() = runTest {
        val u = usuarioSupervisor.copy(id = 0L)

        repositorio.eliminarUsuario(u)

        coVerify(exactly = 0) { api.eliminarUsuario(any()) }
        coVerify { dao.eliminarUsuario(u) }
    }
}
