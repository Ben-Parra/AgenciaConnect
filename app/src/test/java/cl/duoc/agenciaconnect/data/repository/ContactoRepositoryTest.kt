package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.ContactoDao
import cl.duoc.agenciaconnect.data.model.Contacto
import cl.duoc.agenciaconnect.data.remote.AgenciaApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/**
 * Pruebas unitarias para ContactoRepository.
 */
class ContactoRepositoryTest {

    private lateinit var api: AgenciaApi
    private lateinit var dao: ContactoDao
    private lateinit var repositorio: ContactoRepository

    private fun crearContacto(id: Long = 1L) = Contacto(
        id = id,
        nombreCompleto = "Camila Pérez",
        organizacionId = 1L,
        cargo = "Jefa de Bienestar",
        area = "RRHH",
        correo = "cperez@empresa.cl",
        telefono = "+56911223344",
        medioPreferido = "Correo",
        observaciones = "Sin notas"
    )

    @Before
    fun setUp() {
        // Organizamos: preparamos mocks.
        api = mockk(relaxed = true)
        dao = mockk(relaxed = true)
        coEvery { dao.obtenerContactos() } returns flowOf(emptyList())
        repositorio = ContactoRepository(contactoDao = dao, api = api)
    }

    // ── sincronizarDesdeApi ───────────────────────────────────────────────

    @Test
    fun sincronizarDesdeApi_contactoNuevo_insertaEnDao() = runTest {
        // Organizamos.
        val c = crearContacto(id = 15L)
        coEvery { api.obtenerContactos() } returns listOf(c)
        coEvery { dao.obtenerContactoPorId(15L) } returns null

        // Actuamos.
        repositorio.sincronizarDesdeApi()

        // Afirmamos.
        coVerify { dao.insertarContacto(c) }
    }

    @Test
    fun sincronizarDesdeApi_contactoExistente_actualizaEnDao() = runTest {
        // Organizamos.
        val c = crearContacto(id = 15L)
        coEvery { api.obtenerContactos() } returns listOf(c)
        coEvery { dao.obtenerContactoPorId(15L) } returns c

        // Actuamos.
        repositorio.sincronizarDesdeApi()

        // Afirmamos.
        coVerify { dao.actualizarContacto(c) }
    }

    @Test
    fun sincronizarDesdeApi_errorDeRed_noLanzaExcepcion() = runTest {
        // Organizamos.
        coEvery { api.obtenerContactos() } throws RuntimeException("Error de red")
        // Actuamos y afirmamos: sin excepción no controlada.
        repositorio.sincronizarDesdeApi()
    }

    // ── insertarContacto ──────────────────────────────────────────────────

    @Test
    fun insertarContacto_apiExitosa_insertaConIdDelServidor() = runTest {
        // Organizamos.
        val enviado = crearContacto(id = 0L)
        val creado = crearContacto(id = 42L)
        coEvery { api.crearContacto(enviado) } returns Response.success(creado)
        coEvery { dao.obtenerContactoPorId(42L) } returns null

        // Actuamos.
        repositorio.insertarContacto(enviado)

        // Afirmamos.
        coVerify { dao.insertarContacto(creado) }
    }

    @Test
    fun insertarContacto_apiFalla_guardaLocalmenteEnDao() = runTest {
        // Organizamos.
        val c = crearContacto(id = 0L)
        coEvery { api.crearContacto(c) } throws java.io.IOException("Sin red")

        // Actuamos.
        repositorio.insertarContacto(c)

        // Afirmamos.
        coVerify { dao.insertarContacto(c) }
    }

    // ── actualizarContacto ────────────────────────────────────────────────

    @Test
    fun actualizarContacto_idPositivo_llamaApiYActualizaDao() = runTest {
        // Organizamos.
        val c = crearContacto(id = 9L)

        // Actuamos.
        repositorio.actualizarContacto(c)

        // Afirmamos.
        coVerify { api.actualizarContacto(9L, c) }
        coVerify { dao.actualizarContacto(c) }
    }

    @Test
    fun actualizarContacto_idCero_noLlamaApi() = runTest {
        // Organizamos: contacto sin ID del servidor.
        val c = crearContacto(id = 0L)

        // Actuamos.
        repositorio.actualizarContacto(c)

        // Afirmamos: no llama a PUT.
        coVerify(exactly = 0) { api.actualizarContacto(any(), any()) }
        coVerify { dao.actualizarContacto(c) }
    }

    // ── eliminarContacto ──────────────────────────────────────────────────

    @Test
    fun eliminarContacto_idPositivo_llamaApiYEliminaDao() = runTest {
        // Organizamos.
        val c = crearContacto(id = 11L)

        // Actuamos.
        repositorio.eliminarContacto(c)

        // Afirmamos.
        coVerify { api.eliminarContacto(11L) }
        coVerify { dao.eliminarContacto(c) }
    }

    @Test
    fun eliminarContacto_idCero_noLlamaApi() = runTest {
        // Organizamos.
        val c = crearContacto(id = 0L)

        // Actuamos.
        repositorio.eliminarContacto(c)

        // Afirmamos.
        coVerify(exactly = 0) { api.eliminarContacto(any()) }
        coVerify { dao.eliminarContacto(c) }
    }

    @Test
    fun eliminarContacto_errorApi_sigueEliminandoEnDao() = runTest {
        // Organizamos.
        val c = crearContacto(id = 5L)
        coEvery { api.eliminarContacto(5L) } throws RuntimeException("Error")

        // Actuamos.
        repositorio.eliminarContacto(c)

        // Afirmamos.
        coVerify { dao.eliminarContacto(c) }
    }
}
