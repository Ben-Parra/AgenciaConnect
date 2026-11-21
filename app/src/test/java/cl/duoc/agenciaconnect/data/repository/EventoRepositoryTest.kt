package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.EventoDao
import cl.duoc.agenciaconnect.data.model.Evento
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
 * Pruebas unitarias para EventoRepository.
 */
class EventoRepositoryTest {

    private lateinit var api: AgenciaApi
    private lateinit var dao: EventoDao
    private lateinit var repositorio: EventoRepository

    private fun crearEvento(id: Long = 1L) = Evento(
        id = id,
        nombre = "Feria de Salud",
        descripcion = "Exposición dental",
        fecha = "15062027",
        ubicacion = "Espacio Riesco",
        organizacion = "Clínica Dental",
        imagenUri = "content://img/1"
    )

    @Before
    fun setUp() {
        api = mockk(relaxed = true)
        dao = mockk(relaxed = true)
        coEvery { dao.obtenerEventos() } returns flowOf(emptyList())
        repositorio = EventoRepository(eventoDao = dao, api = api)
    }

    // ── sincronizarDesdeApi ───────────────────────────────────────────────

    @Test
    fun sincronizarDesdeApi_apiRetornaLista_insertaEventoNuevo() = runTest {
        val ev = crearEvento(id = 20L)
        coEvery { api.obtenerEventos() } returns listOf(ev)
        coEvery { dao.obtenerEventoPorId(20L) } returns null

        repositorio.sincronizarDesdeApi()

        coVerify { dao.insertarEvento(ev) }
    }

    @Test
    fun sincronizarDesdeApi_eventoExistente_actualiza() = runTest {
        val ev = crearEvento(id = 20L)
        coEvery { api.obtenerEventos() } returns listOf(ev)
        coEvery { dao.obtenerEventoPorId(20L) } returns ev

        repositorio.sincronizarDesdeApi()

        coVerify { dao.actualizarEvento(ev) }
    }

    @Test
    fun sincronizarDesdeApi_errorDeRed_noLanzaExcepcion() = runTest {
        coEvery { api.obtenerEventos() } throws RuntimeException("Timeout")
        repositorio.sincronizarDesdeApi()
    }

    // ── insertarEvento ────────────────────────────────────────────────────

    @Test
    fun insertarEvento_apiExitosa_insertaConIdDelServidor() = runTest {
        val enviado = crearEvento(id = 0L)
        val creado = crearEvento(id = 55L)
        coEvery { api.crearEvento(enviado) } returns Response.success(creado)
        coEvery { dao.obtenerEventoPorId(55L) } returns null

        repositorio.insertarEvento(enviado)

        coVerify { dao.insertarEvento(creado) }
    }

    @Test
    fun insertarEvento_apiFalla_guardaLocalmenteEnDao() = runTest {
        val ev = crearEvento(id = 0L)
        coEvery { api.crearEvento(ev) } throws java.io.IOException("Sin red")

        repositorio.insertarEvento(ev)

        coVerify { dao.insertarEvento(ev) }
    }

    @Test
    fun insertarEvento_apiRespuestaNoExitosa_guardaLocalmenteEnDao() = runTest {
        val ev = crearEvento(id = 0L)
        coEvery { api.crearEvento(ev) } returns Response.error(
            400, ResponseBody.create(null, "")
        )

        repositorio.insertarEvento(ev)

        coVerify { dao.insertarEvento(ev) }
    }

    // ── actualizarEvento ──────────────────────────────────────────────────

    @Test
    fun actualizarEvento_idPositivo_llamaApiYActualizaDao() = runTest {
        val ev = crearEvento(id = 4L)

        repositorio.actualizarEvento(ev)

        coVerify { api.actualizarEvento(4L, ev) }
        coVerify { dao.actualizarEvento(ev) }
    }

    @Test
    fun actualizarEvento_idCero_noLlamaApi() = runTest {
        val ev = crearEvento(id = 0L)

        repositorio.actualizarEvento(ev)

        coVerify(exactly = 0) { api.actualizarEvento(any(), any()) }
        coVerify { dao.actualizarEvento(ev) }
    }

    // ── eliminarEvento ────────────────────────────────────────────────────

    @Test
    fun eliminarEvento_idPositivo_llamaApiYEliminaDao() = runTest {
        val ev = crearEvento(id = 8L)

        repositorio.eliminarEvento(ev)

        coVerify { api.eliminarEvento(8L) }
        coVerify { dao.eliminarEvento(ev) }
    }

    @Test
    fun eliminarEvento_idCero_noLlamaApiPeroEliminaEnDao() = runTest {
        val ev = crearEvento(id = 0L)

        repositorio.eliminarEvento(ev)

        coVerify(exactly = 0) { api.eliminarEvento(any()) }
        coVerify { dao.eliminarEvento(ev) }
    }

    @Test
    fun eliminarEvento_errorApi_sigueEliminandoEnDao() = runTest {
        val ev = crearEvento(id = 2L)
        coEvery { api.eliminarEvento(2L) } throws RuntimeException("Error")

        repositorio.eliminarEvento(ev)

        coVerify { dao.eliminarEvento(ev) }
    }
}
