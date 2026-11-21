package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.ContactoDao
import cl.duoc.agenciaconnect.data.model.Contacto
import cl.duoc.agenciaconnect.data.remote.AgenciaApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
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
        api = mockk(relaxed = true)
        dao = mockk(relaxed = true)
        coEvery { dao.obtenerContactos() } returns flowOf(emptyList())
        repositorio = ContactoRepository(contactoDao = dao, api = api)
    }

    // ── sincronizarDesdeApi ───────────────────────────────────────────────

    @Test
    fun sincronizarDesdeApi_contactoNuevo_insertaEnDao() = runTest {
        val c = crearContacto(id = 15L)
        coEvery { api.obtenerContactos() } returns listOf(c)
        coEvery { dao.obtenerContactoPorId(15L) } returns null

        repositorio.sincronizarDesdeApi()

        coVerify { dao.insertarContacto(c) }
    }

    @Test
    fun sincronizarDesdeApi_contactoExistente_actualizaEnDao() = runTest {
        val c = crearContacto(id = 15L)
        coEvery { api.obtenerContactos() } returns listOf(c)
        coEvery { dao.obtenerContactoPorId(15L) } returns c

        repositorio.sincronizarDesdeApi()

        coVerify { dao.actualizarContacto(c) }
    }

    @Test
    fun sincronizarDesdeApi_errorDeRed_noLanzaExcepcion() = runTest {
        coEvery { api.obtenerContactos() } throws RuntimeException("Error de red")
        repositorio.sincronizarDesdeApi()
    }

    // ── insertarContacto ──────────────────────────────────────────────────

    @Test
    fun insertarContacto_apiExitosa_insertaConIdDelServidor() = runTest {
        val enviado = crearContacto(id = 0L)
        val creado = crearContacto(id = 42L)
        coEvery { api.crearContacto(enviado) } returns Response.success(creado)
        coEvery { dao.obtenerContactoPorId(42L) } returns null

        repositorio.insertarContacto(enviado)

        coVerify { dao.insertarContacto(creado) }
    }

    @Test
    fun insertarContacto_apiFalla_guardaLocalmenteEnDao() = runTest {
        val c = crearContacto(id = 0L)
        coEvery { api.crearContacto(c) } throws java.io.IOException("Sin red")

        repositorio.insertarContacto(c)

        coVerify { dao.insertarContacto(c) }
    }

    @Test
    fun insertarContacto_apiRespuestaNoExitosa_guardaLocalmenteEnDao() = runTest {
        val c = crearContacto(id = 0L)
        coEvery { api.crearContacto(c) } returns Response.error(
            400, ResponseBody.create(null, "")
        )

        repositorio.insertarContacto(c)

        coVerify { dao.insertarContacto(c) }
    }

    // ── actualizarContacto ────────────────────────────────────────────────

    @Test
    fun actualizarContacto_idPositivo_llamaApiYActualizaDao() = runTest {
        val c = crearContacto(id = 9L)

        repositorio.actualizarContacto(c)

        coVerify { api.actualizarContacto(9L, c) }
        coVerify { dao.actualizarContacto(c) }
    }

    @Test
    fun actualizarContacto_idCero_noLlamaApi() = runTest {
        val c = crearContacto(id = 0L)

        repositorio.actualizarContacto(c)

        coVerify(exactly = 0) { api.actualizarContacto(any(), any()) }
        coVerify { dao.actualizarContacto(c) }
    }

    // ── eliminarContacto ──────────────────────────────────────────────────

    @Test
    fun eliminarContacto_idPositivo_llamaApiYEliminaDao() = runTest {
        val c = crearContacto(id = 11L)

        repositorio.eliminarContacto(c)

        coVerify { api.eliminarContacto(11L) }
        coVerify { dao.eliminarContacto(c) }
    }

    @Test
    fun eliminarContacto_idCero_noLlamaApi() = runTest {
        val c = crearContacto(id = 0L)

        repositorio.eliminarContacto(c)

        coVerify(exactly = 0) { api.eliminarContacto(any()) }
        coVerify { dao.eliminarContacto(c) }
    }

    @Test
    fun eliminarContacto_errorApi_sigueEliminandoEnDao() = runTest {
        val c = crearContacto(id = 5L)
        coEvery { api.eliminarContacto(5L) } throws RuntimeException("Error")

        repositorio.eliminarContacto(c)

        coVerify { dao.eliminarContacto(c) }
    }
}
