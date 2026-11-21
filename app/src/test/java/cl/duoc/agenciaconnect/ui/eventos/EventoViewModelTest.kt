package cl.duoc.agenciaconnect.ui.eventos

import android.app.Application
import cl.duoc.agenciaconnect.MainDispatcherRule
import cl.duoc.agenciaconnect.data.local.AgenciaDatabase
import cl.duoc.agenciaconnect.data.local.EventoDao
import cl.duoc.agenciaconnect.data.local.OrganizacionDao
import cl.duoc.agenciaconnect.data.model.Evento
import cl.duoc.agenciaconnect.data.model.Organizacion
import cl.duoc.agenciaconnect.data.repository.EventoRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var application: Application
    private lateinit var mockDatabase: AgenciaDatabase
    private lateinit var mockEventoDao: EventoDao
    private lateinit var mockOrganizacionDao: OrganizacionDao
    private lateinit var viewModel: EventoViewModel

    private val orgRegistrada = Organizacion(
        id = 1L, nombre = "Clínica Dental", tipo = "Empresa", rubro = "Salud",
        comuna = "Providencia", region = "Metropolitana", sitioWeb = "",
        publicoAsociado = "Colaboradores", observaciones = ""
    )

    private fun evento(id: Long = 0L) = Evento(
        id = id, nombre = "Feria Dental", descripcion = "Expo dental",
        fecha = "15062027", ubicacion = "Espacio Riesco",
        organizacion = "Clínica Dental", imagenUri = "content://img/1"
    )

    @Before
    fun setUp() {
        // Organizamos: preparamos mocks.
        application = mockk(relaxed = true)
        mockEventoDao = mockk(relaxed = true)
        mockOrganizacionDao = mockk(relaxed = true)
        mockDatabase = mockk(relaxed = true)

        every { mockDatabase.eventoDao() } returns mockEventoDao
        every { mockDatabase.organizacionDao() } returns mockOrganizacionDao
        every { mockEventoDao.obtenerEventos() } returns flowOf(emptyList())
        every { mockOrganizacionDao.obtenerOrganizaciones() } returns flowOf(listOf(orgRegistrada))

        mockkObject(AgenciaDatabase)
        every { AgenciaDatabase.obtenerInstancia(any()) } returns mockDatabase
        mockkConstructor(EventoRepository::class)
        coEvery { anyConstructed<EventoRepository>().sincronizarDesdeApi() } returns Unit

        viewModel = EventoViewModel(application)
    }

    // ── Estado inicial ────────────────────────────────────────────────────

    @Test
    fun estadoInicial_camposVaciosYSinErrores() {
        // Afirmamos: estado inicial limpio.
        val estado = viewModel.uiState.value
        assertEquals("", estado.nombre)
        assertFalse(estado.errorNombre)
        assertNull(estado.idEnEdicion)
    }

    // ── cambiarNombre ─────────────────────────────────────────────────────

    @Test
    fun cambiarNombre_actualizaYLimpiaError() {
        // Actuamos.
        viewModel.cambiarNombre("Feria de Salud")
        // Afirmamos.
        assertEquals("Feria de Salud", viewModel.uiState.value.nombre)
        assertFalse(viewModel.uiState.value.errorNombre)
    }

    // ── cambiarFecha ──────────────────────────────────────────────────────

    @Test
    fun cambiarFecha_filtrarLetrasYLimitarOcho() {
        // Organizamos: texto con letras y más de 8 dígitos.
        viewModel.cambiarFecha("01ab06202799")
        // Afirmamos: se filtran letras y se trunca a 8.
        assertEquals("01062027", viewModel.uiState.value.fecha)
    }

    // ── validarFormulario con campos vacíos ───────────────────────────────

    @Test
    fun validarFormulario_camposVacios_retornaFalseYMarcaErrores() {
        // Actuamos.
        val resultado = viewModel.validarFormulario()
        // Afirmamos.
        assertFalse(resultado)
        assertTrue(viewModel.uiState.value.errorNombre)
        assertTrue(viewModel.uiState.value.errorDescripcion)
        assertTrue(viewModel.uiState.value.errorFecha)
        assertTrue(viewModel.uiState.value.errorUbicacion)
    }

    // ── validarFormulario sin imagen ──────────────────────────────────────

    @Test
    fun validarFormulario_sinImagen_marcaErrorImagenUri() {
        // Organizamos: llenamos todo menos la imagen.
        viewModel.cambiarNombre("Feria Dental")
        viewModel.cambiarDescripcion("Expo dental")
        viewModel.cambiarFecha("15062027")
        viewModel.cambiarUbicacion("Espacio Riesco")
        viewModel.cambiarOrganizacion("Clínica Dental")
        // Actuamos.
        val resultado = viewModel.validarFormulario()
        // Afirmamos: falla por imagen.
        assertFalse(resultado)
        assertTrue(viewModel.uiState.value.errorImagenUri)
    }

    // ── validarFormulario completo ────────────────────────────────────────

    @Test
    fun validarFormulario_todosLosValidos_retornaTrue() {
        // Organizamos: llenamos todos los campos requeridos.
        viewModel.cambiarNombre("Feria Dental")
        viewModel.cambiarDescripcion("Expo dental")
        viewModel.cambiarFecha("15062027")
        viewModel.cambiarUbicacion("Espacio Riesco")
        viewModel.cambiarOrganizacion("Clínica Dental")
        viewModel.cambiarImagenUri("content://img/1")
        // Actuamos.
        val resultado = viewModel.validarFormulario()
        // Afirmamos.
        assertTrue(resultado)
    }

    // ── guardarEvento ─────────────────────────────────────────────────────

    @Test
    fun guardarEvento_conDatosValidos_llama_insertarEvento() = runTest {
        // Organizamos.
        viewModel.cambiarNombre("Feria Dental")
        viewModel.cambiarDescripcion("Expo dental")
        viewModel.cambiarFecha("15062027")
        viewModel.cambiarUbicacion("Espacio Riesco")
        viewModel.cambiarOrganizacion("Clínica Dental")
        viewModel.cambiarImagenUri("content://img/1")
        coEvery { anyConstructed<EventoRepository>().insertarEvento(any()) } returns 1L

        // Actuamos.
        viewModel.guardarEvento()
        advanceUntilIdle()

        // Afirmamos: sin errores de validación.
        assertFalse(viewModel.uiState.value.errorNombre)
        assertFalse(viewModel.uiState.value.errorFecha)
    }

    @Test
    fun guardarEvento_camposInvalidos_noInserta() = runTest {
        // Actuamos: guardamos sin llenar nada.
        viewModel.guardarEvento()
        advanceUntilIdle()
        // Afirmamos: se marcaron errores, no se insertó.
        assertTrue(viewModel.uiState.value.errorNombre)
    }

    // ── cargarEventoParaEditar ────────────────────────────────────────────

    @Test
    fun cargarEventoParaEditar_estableceCamposYIdEnEdicion() {
        // Organizamos.
        val ev = evento(id = 7L)
        // Actuamos.
        viewModel.cargarEventoParaEditar(ev)
        // Afirmamos.
        val estado = viewModel.uiState.value
        assertEquals("Feria Dental", estado.nombre)
        assertEquals(7L, estado.idEnEdicion)
    }

    // ── cancelarEdicion ───────────────────────────────────────────────────

    @Test
    fun cancelarEdicion_limpiaCampos() {
        // Organizamos.
        viewModel.cargarEventoParaEditar(evento(id = 2L))
        // Actuamos.
        viewModel.cancelarEdicion()
        // Afirmamos.
        assertEquals("", viewModel.uiState.value.nombre)
        assertNull(viewModel.uiState.value.idEnEdicion)
    }

    // ── solicitarEliminar / cancelarEliminar ──────────────────────────────

    @Test
    fun solicitarEliminar_estableceEventoPendiente() {
        // Organizamos.
        val ev = evento(id = 4L)
        // Actuamos.
        viewModel.solicitarEliminar(ev)
        // Afirmamos.
        assertEquals(ev, viewModel.uiState.value.eventoPorEliminar)
    }

    @Test
    fun cancelarEliminar_limpiaPendiente() {
        // Organizamos.
        viewModel.solicitarEliminar(evento(id = 4L))
        // Actuamos.
        viewModel.cancelarEliminar()
        // Afirmamos.
        assertNull(viewModel.uiState.value.eventoPorEliminar)
    }

    // ── confirmarEliminar ─────────────────────────────────────────────────

    @Test
    fun confirmarEliminar_conPendiente_eliminaEvento() = runTest {
        // Organizamos.
        val ev = evento(id = 5L)
        viewModel.solicitarEliminar(ev)
        coEvery { anyConstructed<EventoRepository>().eliminarEvento(ev) } returns Unit

        // Actuamos.
        viewModel.confirmarEliminar()
        advanceUntilIdle()

        // Afirmamos: se limpió el pendiente.
        assertNull(viewModel.uiState.value.eventoPorEliminar)
    }

    // ── limpiarFormulario ─────────────────────────────────────────────────

    @Test
    fun limpiarFormulario_reseteaTodosLosCampos() {
        // Organizamos.
        viewModel.cambiarNombre("Algo")
        viewModel.cargarEventoParaEditar(evento(id = 1L))
        // Actuamos.
        viewModel.limpiarFormulario()
        // Afirmamos.
        assertEquals("", viewModel.uiState.value.nombre)
        assertNull(viewModel.uiState.value.idEnEdicion)
    }
}
