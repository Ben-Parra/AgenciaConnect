package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.EventoDao
import cl.duoc.agenciaconnect.data.model.Evento
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
        // Organizamos: preparamos los mocks.
        api = mockk(relaxed = true)
        dao = mockk(relaxed = true)
        coEvery { dao.obtenerEventos() } returns flowOf(emptyList())
        repositorio = EventoRepository(eventoDao = dao, api = api)
    }

    // ── sincronizarDesdeApi ───────────────────────────────────────────────

    @Test
    fun sincronizarDesdeApi_apiRetornaLista_insertaEventoNuevo() = runTest {
        // Organizamos: evento nuevo sin registro local.
        val ev = crearEvento(id = 20L)
        coEvery { api.obtenerEventos() } returns listOf(ev)
        coEvery { dao.obtenerEventoPorId(20L) } returns null

        // Actuamos.
        repositorio.sincronizarDesdeApi()

        // Afirmamos: se insertó.
        coVerify { dao.insertarEvento(ev) }
    }

    @Test
    fun sincronizarDesdeApi_eventoExistente_actualiza() = runTest {
        // Organizamos: evento que ya existe localmente.
        val ev = crearEvento(id = 20L)
        coEvery { api.obtenerEventos() } returns listOf(ev)
        coEvery { dao.obtenerEventoPorId(20L) } returns ev

        // Actuamos.
        repositorio.sincronizarDesdeApi()

        // Afirmamos: se actualiza.
        coVerify { dao.actualizarEvento(ev) }
    }

    @Test
    fun sincronizarDesdeApi_errorDeRed_noLanzaExcepcion() = runTest {
        // Organizamos: API no disponible.
        coEvery { api.obtenerEventos() } throws RuntimeException("Timeout")
        // Actuamos y afirmamos: sin excepción no controlada.
        repositorio.sincronizarDesdeApi()
    }

    // ── insertarEvento ────────────────────────────────────────────────────

    @Test
    fun insertarEvento_apiExitosa_insertaConIdDelServidor() = runTest {
        // Organizamos: API responde con evento con ID del servidor.
        val enviado = crearEvento(id = 0L)
        val creado = crearEvento(id = 55L)
        coEvery { api.crearEvento(enviado) } returns Response.success(creado)
        coEvery { dao.obtenerEventoPorId(55L) } returns null

        // Actuamos.
        repositorio.insertarEvento(enviado)

        // Afirmamos.
        coVerify { dao.insertarEvento(creado) }
    }

    @Test
    fun insertarEvento_apiFalla_guardaLocalmenteEnDao() = runTest {
        // Organizamos: falla de red.
        val ev = crearEvento(id = 0L)
        coEvery { api.crearEvento(ev) } throws java.io.IOException("Sin red")

        // Actuamos.
        repositorio.insertarEvento(ev)

        // Afirmamos: respaldo local.
        coVerify { dao.insertarEvento(ev) }
    }

    // ── actualizarEvento ──────────────────────────────────────────────────

    @Test
    fun actualizarEvento_idPositivo_llamaApiYActualizaDao() = runTest {
        // Organizamos: evento con ID.
        val ev = crearEvento(id = 4L)

        // Actuamos.
        repositorio.actualizarEvento(ev)

        // Afirmamos.
        coVerify { api.actualizarEvento(4L, ev) }
        coVerify { dao.actualizarEvento(ev) }
    }

    @Test
    fun actualizarEvento_idCero_noLlamaApi() = runTest {
        // Organizamos: evento sin ID del servidor.
        val ev = crearEvento(id = 0L)

        // Actuamos.
        repositorio.actualizarEvento(ev)

        // Afirmamos: no llama a la API.
        coVerify(exactly = 0) { api.actualizarEvento(any(), any()) }
        coVerify { dao.actualizarEvento(ev) }
    }

    // ── eliminarEvento ────────────────────────────────────────────────────

    @Test
    fun eliminarEvento_idPositivo_llamaApiYEliminaDao() = runTest {
        // Organizamos: evento a eliminar.
        val ev = crearEvento(id = 8L)

        // Actuamos.
        repositorio.eliminarEvento(ev)

        // Afirmamos.
        coVerify { api.eliminarEvento(8L) }
        coVerify { dao.eliminarEvento(ev) }
    }

    @Test
    fun eliminarEvento_idCero_noLlamaApiPeroEliminaEnDao() = runTest {
        // Organizamos: evento sin ID.
        val ev = crearEvento(id = 0L)

        // Actuamos.
        repositorio.eliminarEvento(ev)

        // Afirmamos.
        coVerify(exactly = 0) { api.eliminarEvento(any()) }
        coVerify { dao.eliminarEvento(ev) }
    }

    @Test
    fun eliminarEvento_errorApi_sigueEliminandoEnDao() = runTest {
        // Organizamos: falla de API.
        val ev = crearEvento(id = 2L)
        coEvery { api.eliminarEvento(2L) } throws RuntimeException("Error")

        // Actuamos.
        repositorio.eliminarEvento(ev)

        // Afirmamos: el DAO elimina de todos modos.
        coVerify { dao.eliminarEvento(ev) }
    }
}
