package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.OrganizacionDao
import cl.duoc.agenciaconnect.data.model.Organizacion
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
 * Pruebas unitarias para OrganizacionRepository.
 */
class OrganizacionRepositoryTest {

    private lateinit var api: AgenciaApi
    private lateinit var dao: OrganizacionDao
    private lateinit var repositorio: OrganizacionRepository

    private fun crearOrganizacion(id: Long = 1L) = Organizacion(
        id = id,
        nombre = "Clínica Dental",
        tipo = "Empresa",
        rubro = "Salud",
        comuna = "Providencia",
        region = "Metropolitana",
        sitioWeb = "www.clinica.cl",
        publicoAsociado = "Colaboradores",
        observaciones = "Sin observaciones"
    )

    @Before
    fun setUp() {
        api = mockk(relaxed = true)
        dao = mockk(relaxed = true)
        coEvery { dao.obtenerOrganizaciones() } returns flowOf(emptyList())
        repositorio = OrganizacionRepository(organizacionDao = dao, api = api)
    }

    // ── sincronizarDesdeApi ───────────────────────────────────────────────

    @Test
    fun sincronizarDesdeApi_organizacionNueva_insertaEnDao() = runTest {
        val org = crearOrganizacion(id = 10L)
        coEvery { api.obtenerOrganizaciones() } returns listOf(org)
        coEvery { dao.obtenerOrganizacionPorId(10L) } returns null

        repositorio.sincronizarDesdeApi()

        coVerify { dao.insertarOrganizacion(org) }
    }

    @Test
    fun sincronizarDesdeApi_organizacionExistente_actualizaEnDao() = runTest {
        val org = crearOrganizacion(id = 10L)
        coEvery { api.obtenerOrganizaciones() } returns listOf(org)
        coEvery { dao.obtenerOrganizacionPorId(10L) } returns org

        repositorio.sincronizarDesdeApi()

        coVerify { dao.actualizarOrganizacion(org) }
    }

    @Test
    fun sincronizarDesdeApi_errorDeRed_noLanzaExcepcion() = runTest {
        coEvery { api.obtenerOrganizaciones() } throws java.io.IOException("Red caída")
        repositorio.sincronizarDesdeApi()
    }

    // ── insertarOrganizacion ──────────────────────────────────────────────

    @Test
    fun insertarOrganizacion_apiExitosa_insertaConIdDelServidor() = runTest {
        val enviada = crearOrganizacion(id = 0L)
        val creada = crearOrganizacion(id = 77L)
        coEvery { api.crearOrganizacion(enviada) } returns Response.success(creada)
        coEvery { dao.obtenerOrganizacionPorId(77L) } returns null

        repositorio.insertarOrganizacion(enviada)

        coVerify { dao.insertarOrganizacion(creada) }
    }

    @Test
    fun insertarOrganizacion_apiFalla_guardaLocalmenteEnDao() = runTest {
        val org = crearOrganizacion(id = 0L)
        coEvery { api.crearOrganizacion(org) } throws RuntimeException("Sin conexión")

        repositorio.insertarOrganizacion(org)

        coVerify { dao.insertarOrganizacion(org) }
    }

    @Test
    fun insertarOrganizacion_apiRespuestaNoExitosa_guardaLocalmenteEnDao() = runTest {
        val org = crearOrganizacion(id = 0L)
        coEvery { api.crearOrganizacion(org) } returns Response.error(
            400, ResponseBody.create(null, "")
        )

        repositorio.insertarOrganizacion(org)

        coVerify { dao.insertarOrganizacion(org) }
    }

    // ── actualizarOrganizacion ────────────────────────────────────────────

    @Test
    fun actualizarOrganizacion_idPositivo_llamaApiYActualizaDao() = runTest {
        val org = crearOrganizacion(id = 3L)

        repositorio.actualizarOrganizacion(org)

        coVerify { api.actualizarOrganizacion(3L, org) }
        coVerify { dao.actualizarOrganizacion(org) }
    }

    @Test
    fun actualizarOrganizacion_errorApi_sigueActualizandoDao() = runTest {
        val org = crearOrganizacion(id = 3L)
        coEvery { api.actualizarOrganizacion(any(), any()) } throws RuntimeException("Error")

        repositorio.actualizarOrganizacion(org)

        coVerify { dao.actualizarOrganizacion(org) }
    }

    // ── eliminarOrganizacion ──────────────────────────────────────────────

    @Test
    fun eliminarOrganizacion_idPositivo_llamaApiYEliminaDao() = runTest {
        val org = crearOrganizacion(id = 6L)

        repositorio.eliminarOrganizacion(org)

        coVerify { api.eliminarOrganizacion(6L) }
        coVerify { dao.eliminarOrganizacion(org) }
    }

    @Test
    fun eliminarOrganizacion_idCero_noLlamaApi() = runTest {
        val org = crearOrganizacion(id = 0L)

        repositorio.eliminarOrganizacion(org)

        coVerify(exactly = 0) { api.eliminarOrganizacion(any()) }
        coVerify { dao.eliminarOrganizacion(org) }
    }
}
