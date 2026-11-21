package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.SeguimientoDao
import cl.duoc.agenciaconnect.data.model.Seguimiento
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
 * Pruebas unitarias para SeguimientoRepository.
 */
class SeguimientoRepositoryTest {

    private lateinit var api: AgenciaApi
    private lateinit var dao: SeguimientoDao
    private lateinit var repositorio: SeguimientoRepository

    private fun crearSeguimiento(id: Long = 1L) = Seguimiento(
        id = id,
        oportunidadId = 1L,
        usuarioNombre = "Carlos",
        tipoAccion = "Llamada telefónica",
        fechaHora = "18/11/2026 10:00",
        resultado = "Interesado",
        proximaAccion = "Enviar propuesta",
        observaciones = "Sin notas"
    )

    @Before
    fun setUp() {
        api = mockk(relaxed = true)
        dao = mockk(relaxed = true)
        coEvery { dao.obtenerTodosLosSeguimientos() } returns flowOf(emptyList())
        coEvery { dao.obtenerSeguimientosPorOportunidad(any()) } returns flowOf(emptyList())
        repositorio = SeguimientoRepository(seguimientoDao = dao, api = api)
    }

    // ── sincronizarDesdeApi ───────────────────────────────────────────────

    @Test
    fun sincronizarDesdeApi_seguimientoNuevo_insertaEnDao() = runTest {
        val seg = crearSeguimiento(id = 20L)
        coEvery { api.obtenerSeguimientos() } returns listOf(seg)
        coEvery { dao.obtenerSeguimientoPorId(20L) } returns null

        repositorio.sincronizarDesdeApi()

        coVerify { dao.insertarSeguimiento(seg) }
    }

    @Test
    fun sincronizarDesdeApi_seguimientoExistente_actualiza() = runTest {
        val seg = crearSeguimiento(id = 20L)
        coEvery { api.obtenerSeguimientos() } returns listOf(seg)
        coEvery { dao.obtenerSeguimientoPorId(20L) } returns seg

        repositorio.sincronizarDesdeApi()

        coVerify { dao.actualizarSeguimiento(seg) }
    }

    @Test
    fun sincronizarDesdeApi_errorDeRed_noLanzaExcepcion() = runTest {
        coEvery { api.obtenerSeguimientos() } throws RuntimeException("Error")
        repositorio.sincronizarDesdeApi()
    }

    // ── insertarSeguimiento ───────────────────────────────────────────────

    @Test
    fun insertarSeguimiento_apiExitosa_insertaConIdDelServidor() = runTest {
        val enviado = crearSeguimiento(id = 0L)
        val creado = crearSeguimiento(id = 88L)
        coEvery { api.crearSeguimiento(enviado) } returns Response.success(creado)
        coEvery { dao.obtenerSeguimientoPorId(88L) } returns null

        repositorio.insertarSeguimiento(enviado)

        coVerify { dao.insertarSeguimiento(creado) }
    }

    @Test
    fun insertarSeguimiento_apiFalla_guardaLocalmenteEnDao() = runTest {
        val seg = crearSeguimiento(id = 0L)
        coEvery { api.crearSeguimiento(seg) } throws java.io.IOException("Sin red")

        repositorio.insertarSeguimiento(seg)

        coVerify { dao.insertarSeguimiento(seg) }
    }

    @Test
    fun insertarSeguimiento_apiRespuestaNoExitosa_guardaLocalmenteEnDao() = runTest {
        val seg = crearSeguimiento(id = 0L)
        coEvery { api.crearSeguimiento(seg) } returns Response.error(
            400, ResponseBody.create(null, "")
        )

        repositorio.insertarSeguimiento(seg)

        coVerify { dao.insertarSeguimiento(seg) }
    }

    // ── actualizarSeguimiento ─────────────────────────────────────────────

    @Test
    fun actualizarSeguimiento_idPositivo_llamaApiYActualizaDao() = runTest {
        val seg = crearSeguimiento(id = 5L)

        repositorio.actualizarSeguimiento(seg)

        coVerify { api.actualizarSeguimiento(5L, seg) }
        coVerify { dao.actualizarSeguimiento(seg) }
    }

    @Test
    fun actualizarSeguimiento_idCero_noLlamaApi() = runTest {
        val seg = crearSeguimiento(id = 0L)

        repositorio.actualizarSeguimiento(seg)

        coVerify(exactly = 0) { api.actualizarSeguimiento(any(), any()) }
        coVerify { dao.actualizarSeguimiento(seg) }
    }

    // ── eliminarSeguimiento ───────────────────────────────────────────────

    @Test
    fun eliminarSeguimiento_idPositivo_llamaApiYEliminaDao() = runTest {
        val seg = crearSeguimiento(id = 7L)

        repositorio.eliminarSeguimiento(seg)

        coVerify { api.eliminarSeguimiento(7L) }
        coVerify { dao.eliminarSeguimiento(seg) }
    }

    @Test
    fun eliminarSeguimiento_idCero_noLlamaApi() = runTest {
        val seg = crearSeguimiento(id = 0L)

        repositorio.eliminarSeguimiento(seg)

        coVerify(exactly = 0) { api.eliminarSeguimiento(any()) }
        coVerify { dao.eliminarSeguimiento(seg) }
    }
}
