package cl.duoc.agenciaconnect.ui.seguimientos

import android.app.Application
import cl.duoc.agenciaconnect.MainDispatcherRule
import cl.duoc.agenciaconnect.data.local.AgenciaDatabase
import cl.duoc.agenciaconnect.data.local.SeguimientoDao
import cl.duoc.agenciaconnect.data.repository.SeguimientoRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SeguimientoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var application: Application
    private lateinit var mockDatabase: AgenciaDatabase
    private lateinit var mockSeguimientoDao: SeguimientoDao
    private lateinit var viewModel: SeguimientoViewModel

    @Before
    fun setUp() {
        // Organizamos: preparamos mocks.
        application = mockk(relaxed = true)
        mockSeguimientoDao = mockk(relaxed = true)
        mockDatabase = mockk(relaxed = true)

        every { mockDatabase.seguimientoDao() } returns mockSeguimientoDao
        every { mockSeguimientoDao.obtenerTodosLosSeguimientos() } returns flowOf(emptyList())
        every { mockSeguimientoDao.obtenerSeguimientosPorOportunidad(any()) } returns flowOf(emptyList())

        mockkObject(AgenciaDatabase)
        every { AgenciaDatabase.obtenerInstancia(any()) } returns mockDatabase
        mockkConstructor(SeguimientoRepository::class)
        coEvery { anyConstructed<SeguimientoRepository>().sincronizarDesdeApi() } returns Unit
        coEvery { anyConstructed<SeguimientoRepository>().insertarSeguimiento(any()) } returns 1L

        viewModel = SeguimientoViewModel(application)
    }

    // ── Estado inicial ────────────────────────────────────────────────────

    @Test
    fun estadoInicial_camposVaciosYSinErrores() {
        // Actuamos.
        val estado = viewModel.uiState.value
        // Afirmamos.
        assertEquals("", estado.resultado)
        assertEquals("", estado.proximaAccion)
        assertFalse(estado.errorResultado)
        assertFalse(estado.errorProximaAccion)
    }

    // ── cambiarTipoAccion ─────────────────────────────────────────────────

    @Test
    fun cambiarTipoAccion_actualizaValor() {
        // Actuamos.
        viewModel.cambiarTipoAccion("Reunión presencial")
        // Afirmamos.
        assertEquals("Reunión presencial", viewModel.uiState.value.tipoAccion)
    }

    // ── cambiarResultado ──────────────────────────────────────────────────

    @Test
    fun cambiarResultado_actualizaYLimpiaError() {
        // Actuamos.
        viewModel.cambiarResultado("Cliente interesado")
        // Afirmamos.
        assertEquals("Cliente interesado", viewModel.uiState.value.resultado)
        assertFalse(viewModel.uiState.value.errorResultado)
    }

    // ── cambiarProximaAccion ──────────────────────────────────────────────

    @Test
    fun cambiarProximaAccion_actualizaYLimpiaError() {
        // Actuamos.
        viewModel.cambiarProximaAccion("Enviar propuesta formal")
        // Afirmamos.
        assertEquals("Enviar propuesta formal", viewModel.uiState.value.proximaAccion)
        assertFalse(viewModel.uiState.value.errorProximaAccion)
    }

    // ── cambiarObservaciones ──────────────────────────────────────────────

    @Test
    fun cambiarObservaciones_actualizaValor() {
        // Actuamos.
        viewModel.cambiarObservaciones("Notas extra del ejecutivo")
        // Afirmamos.
        assertEquals("Notas extra del ejecutivo", viewModel.uiState.value.observaciones)
    }

    // ── guardarSeguimiento campos vacíos ──────────────────────────────────

    @Test
    fun guardarSeguimiento_camposVacios_marcaErroresYNoContinua() = runTest {
        // Organizamos: estado inicial sin datos.
        var callbackLlamado = false

        // Actuamos.
        viewModel.guardarSeguimiento(1L, "Carlos") { callbackLlamado = true }
        advanceUntilIdle()

        // Afirmamos: se marcaron errores y el callback no fue invocado.
        assertTrue(viewModel.uiState.value.errorResultado)
        assertTrue(viewModel.uiState.value.errorProximaAccion)
        assertFalse(callbackLlamado)
    }

    @Test
    fun guardarSeguimiento_soloResultado_marcaErrorProximaAccion() = runTest {
        // Organizamos: solo resultado, sin próxima acción.
        viewModel.cambiarResultado("Interesado")
        var callbackLlamado = false

        // Actuamos.
        viewModel.guardarSeguimiento(1L, "Carlos") { callbackLlamado = true }
        advanceUntilIdle()

        // Afirmamos.
        assertFalse(viewModel.uiState.value.errorResultado)
        assertTrue(viewModel.uiState.value.errorProximaAccion)
        assertFalse(callbackLlamado)
    }

    // ── guardarSeguimiento exitoso ────────────────────────────────────────

    @Test
    fun guardarSeguimiento_datosCompletos_llamamaCallbackYLimpiaCampos() = runTest {
        // Organizamos: llenamos los campos obligatorios.
        viewModel.cambiarResultado("Cliente muy interesado")
        viewModel.cambiarProximaAccion("Enviar propuesta formal")
        viewModel.cambiarObservaciones("Reunión positiva")
        var callbackLlamado = false

        // Actuamos.
        viewModel.guardarSeguimiento(1L, "Carlos Muñoz") { callbackLlamado = true }
        advanceUntilIdle()

        // Afirmamos: callback ejecutado y campos limpios.
        assertTrue(callbackLlamado)
        assertEquals("", viewModel.uiState.value.resultado)
        assertEquals("", viewModel.uiState.value.proximaAccion)
    }

    // ── cargarSeguimientos ────────────────────────────────────────────────

    @Test
    fun cargarSeguimientos_iniciaObservacion() = runTest {
        // Actuamos: cargamos seguimientos para la oportunidad 1.
        viewModel.cargarSeguimientos(1L)
        advanceUntilIdle()
        // Afirmamos: lista vacía (mock retorna emptyList) sin excepción.
        assertEquals(0, viewModel.uiState.value.seguimientos.size)
    }
}
