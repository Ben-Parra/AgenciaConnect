package cl.duoc.agenciaconnect.data.repository

import cl.duoc.agenciaconnect.data.local.OportunidadDao
import cl.duoc.agenciaconnect.data.model.EstadoOportunidad
import cl.duoc.agenciaconnect.data.model.Oportunidad
import cl.duoc.agenciaconnect.data.model.PotencialOportunidad
import cl.duoc.agenciaconnect.data.model.PrioridadOportunidad
import cl.duoc.agenciaconnect.data.model.TipoOportunidad
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
 * Pruebas unitarias para OportunidadRepository.
 * Mockeamos AgenciaApi y OportunidadDao para ejecutar en JVM sin Android.
 */
class OportunidadRepositoryTest {

    private lateinit var api: AgenciaApi
    private lateinit var dao: OportunidadDao
    private lateinit var repositorio: OportunidadRepository

    private fun crearOportunidad(id: Long = 1L) = Oportunidad(
        id = id,
        nombre = "Activación Dental",
        tipo = TipoOportunidad.FERIA_EMPRESARIAL,
        organizacionId = 1L,
        prioridad = PrioridadOportunidad.ALTA,
        potencial = PotencialOportunidad.ALTO,
        estado = EstadoOportunidad.PROPUESTA_ENVIADA,
        responsable = "Carlos",
        observaciones = "Observación",
        fechaDeteccion = "10082026"
    )

    @Before
    fun setUp() {
        // Organizamos: creamos mocks relajados y el repositorio con ellos inyectados.
        api = mockk(relaxed = true)
        dao = mockk(relaxed = true)
        // Configuramos el flow reactivo del DAO.
        coEvery { dao.obtenerOportunidades() } returns flowOf(emptyList())
        repositorio = OportunidadRepository(oportunidadDao = dao, api = api)
    }

    // ── sincronizarDesdeApi ───────────────────────────────────────────────

    @Test
    fun sincronizarDesdeApi_apiRetornaLista_insertaEnDao() = runTest {
        // Organizamos: la API devuelve una oportunidad nueva (no existe en DAO).
        val op = crearOportunidad(id = 10L)
        coEvery { api.obtenerOportunidades() } returns listOf(op)
        coEvery { dao.obtenerOportunidadPorId(10L) } returns null

        // Actuamos.
        repositorio.sincronizarDesdeApi()

        // Afirmamos: se insertó en Room.
        coVerify { dao.insertarOportunidad(op) }
    }

    @Test
    fun sincronizarDesdeApi_oportunidadExistente_actualizaEnDao() = runTest {
        // Organizamos: la API devuelve una oportunidad que ya existe localmente.
        val op = crearOportunidad(id = 10L)
        coEvery { api.obtenerOportunidades() } returns listOf(op)
        coEvery { dao.obtenerOportunidadPorId(10L) } returns op

        // Actuamos.
        repositorio.sincronizarDesdeApi()

        // Afirmamos: se actualizó (no duplicó).
        coVerify { dao.actualizarOportunidad(op) }
    }

    @Test
    fun sincronizarDesdeApi_errorDeRed_noLanzaExcepcion() = runTest {
        // Organizamos: la API lanza RuntimeException.
        coEvery { api.obtenerOportunidades() } throws RuntimeException("Sin conexión")

        // Actuamos y afirmamos: no se propaga ninguna excepción no controlada.
        repositorio.sincronizarDesdeApi()
        // Si llegamos aquí, el repositorio capturó el error correctamente.
    }

    // ── insertarOportunidad ───────────────────────────────────────────────

    @Test
    fun insertarOportunidad_apiExitosa_insertaOportunidadConIdDelServidor() = runTest {
        // Organizamos: la API responde con la oportunidad con ID asignado por MySQL.
        val enviada = crearOportunidad(id = 0L)
        val creada = crearOportunidad(id = 99L)
        coEvery { api.crearOportunidad(enviada) } returns Response.success(creada)
        coEvery { dao.obtenerOportunidadPorId(99L) } returns null

        // Actuamos.
        repositorio.insertarOportunidad(enviada)

        // Afirmamos: se guarda la versión con el ID oficial.
        coVerify { dao.insertarOportunidad(creada) }
    }

    @Test
    fun insertarOportunidad_apiFalla_guardaLocalmenteEnDao() = runTest {
        // Organizamos: la API lanza IOException.
        val op = crearOportunidad(id = 0L)
        coEvery { api.crearOportunidad(op) } throws java.io.IOException("Timeout")

        // Actuamos.
        repositorio.insertarOportunidad(op)

        // Afirmamos: se hace respaldo local.
        coVerify { dao.insertarOportunidad(op) }
    }

    @Test
    fun insertarOportunidad_apiRespuestaNoExitosa_guardaLocalmenteEnDao() = runTest {
        // Organizamos: API devuelve 400 sin body.
        val op = crearOportunidad(id = 0L)
        coEvery { api.crearOportunidad(op) } returns Response.error(
            400, okhttp3.ResponseBody.create(null, "")
        )

        // Actuamos.
        repositorio.insertarOportunidad(op)

        // Afirmamos: se guarda localmente.
        coVerify { dao.insertarOportunidad(op) }
    }

    // ── actualizarOportunidad ─────────────────────────────────────────────

    @Test
    fun actualizarOportunidad_llamaApiYActualizaDao() = runTest {
        // Organizamos: oportunidad a modificar.
        val op = crearOportunidad(id = 5L)

        // Actuamos.
        repositorio.actualizarOportunidad(op)

        // Afirmamos: se llama a la API y al DAO.
        coVerify { api.actualizarOportunidad(5L, op) }
        coVerify { dao.actualizarOportunidad(op) }
    }

    @Test
    fun actualizarOportunidad_errorApi_sigueActualizandoDao() = runTest {
        // Organizamos: la API falla pero el DAO debe actualizarse igual.
        val op = crearOportunidad(id = 5L)
        coEvery { api.actualizarOportunidad(any(), any()) } throws RuntimeException("Error red")

        // Actuamos.
        repositorio.actualizarOportunidad(op)

        // Afirmamos: el DAO se actualiza igual.
        coVerify { dao.actualizarOportunidad(op) }
    }

    // ── eliminarOportunidad ───────────────────────────────────────────────

    @Test
    fun eliminarOportunidad_idPositivo_llamaApiYEliminaEnDao() = runTest {
        // Organizamos: oportunidad con ID real del servidor.
        val op = crearOportunidad(id = 7L)

        // Actuamos.
        repositorio.eliminarOportunidad(op)

        // Afirmamos: DELETE en API y en DAO.
        coVerify { api.eliminarOportunidad(7L) }
        coVerify { dao.eliminarOportunidad(op) }
    }

    @Test
    fun eliminarOportunidad_idCero_noLlamaApiPeroEliminaEnDao() = runTest {
        // Organizamos: oportunidad sin ID (nunca sincronizada con servidor).
        val op = crearOportunidad(id = 0L)

        // Actuamos.
        repositorio.eliminarOportunidad(op)

        // Afirmamos: no llama a la API pero sí borra localmente.
        coVerify(exactly = 0) { api.eliminarOportunidad(any()) }
        coVerify { dao.eliminarOportunidad(op) }
    }

    @Test
    fun eliminarOportunidad_errorApi_sigueEliminandoEnDao() = runTest {
        // Organizamos: la API lanza error.
        val op = crearOportunidad(id = 3L)
        coEvery { api.eliminarOportunidad(3L) } throws RuntimeException("Error red")

        // Actuamos.
        repositorio.eliminarOportunidad(op)

        // Afirmamos: el DAO elimina igual.
        coVerify { dao.eliminarOportunidad(op) }
    }
}
