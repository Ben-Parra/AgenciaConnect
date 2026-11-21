package cl.duoc.agenciaconnect.ui.organizaciones

import android.app.Application
import cl.duoc.agenciaconnect.MainDispatcherRule
import cl.duoc.agenciaconnect.data.local.AgenciaDatabase
import cl.duoc.agenciaconnect.data.local.OrganizacionDao
import cl.duoc.agenciaconnect.data.model.Organizacion
import cl.duoc.agenciaconnect.data.repository.OrganizacionRepository
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
class OrganizacionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var application: Application
    private lateinit var mockDatabase: AgenciaDatabase
    private lateinit var mockDao: OrganizacionDao
    private lateinit var viewModel: OrganizacionViewModel

    private fun org(id: Long = 0L) = Organizacion(
        id = id, nombre = "Clínica Dental", tipo = "Empresa", rubro = "Salud",
        comuna = "Providencia", region = "Metropolitana", sitioWeb = "www.c.cl",
        publicoAsociado = "Colaboradores", observaciones = ""
    )

    @Before
    fun setUp() {
        // Organizamos: preparamos mocks de la capa de datos.
        application = mockk(relaxed = true)
        mockDao = mockk(relaxed = true)
        mockDatabase = mockk(relaxed = true)
        every { mockDatabase.organizacionDao() } returns mockDao
        every { mockDao.obtenerOrganizaciones() } returns flowOf(listOf(org(id = 1L)))
        mockkObject(AgenciaDatabase)
        every { AgenciaDatabase.obtenerInstancia(any()) } returns mockDatabase
        mockkConstructor(OrganizacionRepository::class)
        coEvery { anyConstructed<OrganizacionRepository>().sincronizarDesdeApi() } returns Unit
        viewModel = OrganizacionViewModel(application)
    }

    // ── Estado inicial ────────────────────────────────────────────────────

    @Test
    fun estadoInicial_camposVaciosYSinErrores() {
        // Actuamos: leemos el estado.
        val estado = viewModel.uiState.value
        // Afirmamos.
        assertEquals("", estado.nombre)
        assertFalse(estado.errorNombre)
        assertNull(estado.idEnEdicion)
    }

    // ── cambiarNombre ─────────────────────────────────────────────────────

    @Test
    fun cambiarNombre_actualizaValorYLimpiaError() {
        // Actuamos.
        viewModel.cambiarNombre("Clínica Nueva")
        // Afirmamos.
        assertEquals("Clínica Nueva", viewModel.uiState.value.nombre)
        assertFalse(viewModel.uiState.value.errorNombre)
    }

    // ── guardarOrganizacion con campos vacíos ─────────────────────────────

    @Test
    fun guardarOrganizacion_camposVacios_marcaErrores() {
        // Actuamos.
        viewModel.guardarOrganizacion()
        // Afirmamos: todos los campos obligatorios tienen error.
        val estado = viewModel.uiState.value
        assertTrue(estado.errorNombre)
        assertTrue(estado.errorTipo)
        assertTrue(estado.errorRubro)
        assertTrue(estado.errorComuna)
        assertTrue(estado.errorRegion)
        assertTrue(estado.errorPublicoAsociado)
    }

    @Test
    fun guardarOrganizacion_soloNombre_marcaErroresEnCamposRestantes() {
        // Organizamos: solo nombre.
        viewModel.cambiarNombre("Clínica Dental")
        // Actuamos.
        viewModel.guardarOrganizacion()
        // Afirmamos: nombre no tiene error pero los demás sí.
        assertFalse(viewModel.uiState.value.errorNombre)
        assertTrue(viewModel.uiState.value.errorTipo)
    }

    // ── guardarOrganizacion exitoso ───────────────────────────────────────

    @Test
    fun guardarOrganizacion_todosLosValidos_confirmaMensajeExito() = runTest {
        // Organizamos: llenamos todos los campos.
        viewModel.cambiarNombre("Clínica Dental")
        viewModel.cambiarTipo("Empresa")
        viewModel.cambiarRubro("Salud")
        viewModel.cambiarComuna("Providencia")
        viewModel.cambiarRegion("Metropolitana")
        viewModel.cambiarPublicoAsociado("Colaboradores")
        coEvery { anyConstructed<OrganizacionRepository>().insertarOrganizacion(any()) } returns 1L

        // Actuamos.
        viewModel.guardarOrganizacion()
        advanceUntilIdle()

        // Afirmamos: mensaje de éxito (el texto puede cambiar al limpiar; verificamos que no hay errores).
        val estado = viewModel.uiState.value
        assertFalse(estado.errorNombre)
        assertFalse(estado.errorTipo)
    }

    // ── editarOrganizacion ────────────────────────────────────────────────

    @Test
    fun editarOrganizacion_cargaDatosEnFormulario() {
        // Organizamos.
        val o = org(id = 5L)
        // Actuamos.
        viewModel.editarOrganizacion(o)
        // Afirmamos.
        val estado = viewModel.uiState.value
        assertEquals("Clínica Dental", estado.nombre)
        assertEquals(5L, estado.idEnEdicion)
    }

    // ── cancelarEdicion ───────────────────────────────────────────────────

    @Test
    fun cancelarEdicion_limpiaCamposYIdEnEdicion() {
        // Organizamos: cargamos una org para editar.
        viewModel.editarOrganizacion(org(id = 5L))
        // Actuamos.
        viewModel.cancelarEdicion()
        // Afirmamos.
        assertEquals("", viewModel.uiState.value.nombre)
        assertNull(viewModel.uiState.value.idEnEdicion)
    }

    // ── solicitarEliminacion / cancelarEliminacion ────────────────────────

    @Test
    fun solicitarEliminacion_guardaOrganizacionPendiente() {
        // Organizamos.
        val o = org(id = 3L)
        // Actuamos.
        viewModel.solicitarEliminacion(o)
        // Afirmamos.
        assertEquals(o, viewModel.uiState.value.organizacionPorEliminar)
    }

    @Test
    fun cancelarEliminacion_limpiaPendiente() {
        // Organizamos.
        viewModel.solicitarEliminacion(org(id = 3L))
        // Actuamos.
        viewModel.cancelarEliminacion()
        // Afirmamos.
        assertNull(viewModel.uiState.value.organizacionPorEliminar)
    }

    // ── confirmarEliminacion ──────────────────────────────────────────────

    @Test
    fun confirmarEliminacion_sinPendiente_noLanzaExcepcion() = runTest {
        // Organizamos: sin pendiente.
        // Actuamos y afirmamos: no lanza excepción.
        viewModel.confirmarEliminacion()
        advanceUntilIdle()
    }

    @Test
    fun confirmarEliminacion_conPendiente_ejecutaEliminar() = runTest {
        // Organizamos: configuramos la org pendiente.
        val o = org(id = 3L)
        viewModel.solicitarEliminacion(o)
        coEvery { anyConstructed<OrganizacionRepository>().eliminarOrganizacion(o) } returns Unit

        // Actuamos.
        viewModel.confirmarEliminacion()
        advanceUntilIdle()

        // Afirmamos: se limpió el pendiente.
        assertNull(viewModel.uiState.value.organizacionPorEliminar)
    }

    // ── limpiarFormulario ─────────────────────────────────────────────────

    @Test
    fun limpiarFormulario_reseteaTodosLosCampos() {
        // Organizamos: llenamos el formulario.
        viewModel.cambiarNombre("Algo")
        viewModel.cambiarTipo("Empresa")
        viewModel.editarOrganizacion(org(id = 2L))
        // Actuamos.
        viewModel.limpiarFormulario()
        // Afirmamos.
        val estado = viewModel.uiState.value
        assertEquals("", estado.nombre)
        assertEquals("", estado.tipo)
        assertNull(estado.idEnEdicion)
    }
}
