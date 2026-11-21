package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.OrganizacionDao
import cl.duoc.agenciaconnect.data.model.Organizacion
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
        // Organizamos: preparamos mocks y el repositorio.
        api = mockk(relaxed = true)
        dao = mockk(relaxed = true)
        coEvery { dao.obtenerOrganizaciones() } returns flowOf(emptyList())
        repositorio = OrganizacionRepository(organizacionDao = dao, api = api)
    }

    // ── sincronizarDesdeApi ───────────────────────────────────────────────

    @Test
    fun sincronizarDesdeApi_organizacionNueva_insertaEnDao() = runTest {
        // Organizamos: organizacion nueva.
        val org = crearOrganizacion(id = 10L)
        coEvery { api.obtenerOrganizaciones() } returns listOf(org)
        coEvery { dao.obtenerOrganizacionPorId(10L) } returns null

        // Actuamos.
        repositorio.sincronizarDesdeApi()

        // Afirmamos.
        coVerify { dao.insertarOrganizacion(org) }
    }

    @Test
    fun sincronizarDesdeApi_organizacionExistente_actualizaEnDao() = runTest {
        // Organizamos: organización ya registrada.
        val org = crearOrganizacion(id = 10L)
        coEvery { api.obtenerOrganizaciones() } returns listOf(org)
        coEvery { dao.obtenerOrganizacionPorId(10L) } returns org

        // Actuamos.
        repositorio.sincronizarDesdeApi()

        // Afirmamos.
        coVerify { dao.actualizarOrganizacion(org) }
    }

    @Test
    fun sincronizarDesdeApi_errorDeRed_noLanzaExcepcion() = runTest {
        // Organizamos: API no responde.
        coEvery { api.obtenerOrganizaciones() } throws java.io.IOException("Red caída")
        // Actuamos y afirmamos: sin excepción no controlada.
        repositorio.sincronizarDesdeApi()
    }

    // ── insertarOrganizacion ──────────────────────────────────────────────

    @Test
    fun insertarOrganizacion_apiExitosa_insertaConIdDelServidor() = runTest {
        // Organizamos.
        val enviada = crearOrganizacion(id = 0L)
        val creada = crearOrganizacion(id = 77L)
        coEvery { api.crearOrganizacion(enviada) } returns Response.success(creada)
        coEvery { dao.obtenerOrganizacionPorId(77L) } returns null

        // Actuamos.
        repositorio.insertarOrganizacion(enviada)

        // Afirmamos.
        coVerify { dao.insertarOrganizacion(creada) }
    }

    @Test
    fun insertarOrganizacion_apiFalla_guardaLocalmenteEnDao() = runTest {
        // Organizamos.
        val org = crearOrganizacion(id = 0L)
        coEvery { api.crearOrganizacion(org) } throws RuntimeException("Sin conexión")

        // Actuamos.
        repositorio.insertarOrganizacion(org)

        // Afirmamos: respaldo local.
        coVerify { dao.insertarOrganizacion(org) }
    }

    // ── actualizarOrganizacion ────────────────────────────────────────────

    @Test
    fun actualizarOrganizacion_idPositivo_llamaApiYActualizaDao() = runTest {
        // Organizamos.
        val org = crearOrganizacion(id = 3L)

        // Actuamos.
        repositorio.actualizarOrganizacion(org)

        // Afirmamos.
        coVerify { api.actualizarOrganizacion(3L, org) }
        coVerify { dao.actualizarOrganizacion(org) }
    }

    @Test
    fun actualizarOrganizacion_errorApi_sigueActualizandoDao() = runTest {
        // Organizamos.
        val org = crearOrganizacion(id = 3L)
        coEvery { api.actualizarOrganizacion(any(), any()) } throws RuntimeException("Error")

        // Actuamos.
        repositorio.actualizarOrganizacion(org)

        // Afirmamos: el DAO igualmente se actualiza.
        coVerify { dao.actualizarOrganizacion(org) }
    }

    // ── eliminarOrganizacion ──────────────────────────────────────────────

    @Test
    fun eliminarOrganizacion_idPositivo_llamaApiYEliminaDao() = runTest {
        // Organizamos.
        val org = crearOrganizacion(id = 6L)

        // Actuamos.
        repositorio.eliminarOrganizacion(org)

        // Afirmamos.
        coVerify { api.eliminarOrganizacion(6L) }
        coVerify { dao.eliminarOrganizacion(org) }
    }

    @Test
    fun eliminarOrganizacion_idCero_noLlamaApi() = runTest {
        // Organizamos.
        val org = crearOrganizacion(id = 0L)

        // Actuamos.
        repositorio.eliminarOrganizacion(org)

        // Afirmamos: no llama a la API.
        coVerify(exactly = 0) { api.eliminarOrganizacion(any()) }
        coVerify { dao.eliminarOrganizacion(org) }
    }
}
