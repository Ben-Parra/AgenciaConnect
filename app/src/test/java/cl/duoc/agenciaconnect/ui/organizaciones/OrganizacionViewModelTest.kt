package cl.duoc.agenciaconnect.ui.organizaciones

import android.app.Application
import cl.duoc.agenciaconnect.MainDispatcherRule
import cl.duoc.agenciaconnect.data.local.AgenciaDatabase
import cl.duoc.agenciaconnect.data.local.OrganizacionDao
import cl.duoc.agenciaconnect.data.model.Organizacion
import cl.duoc.agenciaconnect.data.repository.OrganizacionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
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

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ── Estado inicial ────────────────────────────────────────────────────

    @Test
    fun estadoInicial_camposVaciosYSinErrores() {
        val estado = viewModel.uiState.value
        assertEquals("", estado.nombre)
        assertFalse(estado.errorNombre)
        assertNull(estado.idEnEdicion)
    }

    // ── cambiarCampos ─────────────────────────────────────────────────────

    @Test
    fun cambiarCampos_actualizanEstado() {
        viewModel.cambiarNombre("Clínica Nueva")
        viewModel.cambiarSitioWeb("www.clinicanueva.cl")
        viewModel.cambiarObservaciones("Nota de la empresa")

        val estado = viewModel.uiState.value
        assertEquals("Clínica Nueva", estado.nombre)
        assertEquals("www.clinicanueva.cl", estado.sitioWeb)
        assertEquals("Nota de la empresa", estado.observaciones)
    }

    // ── guardarOrganizacion ───────────────────────────────────────────────

    @Test
    fun guardarOrganizacion_excepcionRepositorio_muestraMensajeError() = runTest {
        viewModel.cambiarNombre("Clínica Dental")
        viewModel.cambiarTipo("Empresa")
        viewModel.cambiarRubro("Salud")
        viewModel.cambiarComuna("Providencia")
        viewModel.cambiarRegion("Metropolitana")
        viewModel.cambiarPublicoAsociado("Colaboradores")
        coEvery { anyConstructed<OrganizacionRepository>().insertarOrganizacion(any()) } throws RuntimeException("Error BD")

        viewModel.guardarOrganizacion()
        advanceUntilIdle()

        assertEquals("No se pudo guardar la organización. Inténtalo nuevamente.", viewModel.uiState.value.mensaje)
    }

    // ── confirmarEliminacion excepcion ─────────────────────────────────────

    @Test
    fun confirmarEliminacion_excepcionRepositorio_muestraMensajeError() = runTest {
        val o = org(id = 3L)
        viewModel.solicitarEliminacion(o)
        coEvery { anyConstructed<OrganizacionRepository>().eliminarOrganizacion(o) } throws RuntimeException("Error")

        viewModel.confirmarEliminacion()
        advanceUntilIdle()

        assertEquals("No se pudo eliminar la organización.", viewModel.uiState.value.mensaje)
    }

    // ── reiniciarFormularioAlSalir ────────────────────────────────────────

    @Test
    fun reiniciarFormularioAlSalir_reseteaFormularioYMensaje() {
        viewModel.cambiarNombre("Clínica Dental")
        viewModel.reiniciarFormularioAlSalir()

        val estado = viewModel.uiState.value
        assertEquals("", estado.nombre)
        assertNull(estado.mensaje)
    }

    // ── Pruebas adicionales de ramas ───────────────────────────────────────

    @Test
    fun edicionOrganizacion_conIdPositivo_guardaYLlamaActualizarOrganizacion() = runTest {
        val o = org(id = 15L)
        viewModel.editarOrganizacion(o)
        viewModel.cambiarNombre("Clínica Dental Modificada")
        coEvery { anyConstructed<OrganizacionRepository>().actualizarOrganizacion(any()) } returns Unit

        viewModel.guardarOrganizacion()
        advanceUntilIdle()

        coVerify { anyConstructed<OrganizacionRepository>().actualizarOrganizacion(any()) }
    }

    @Test
    fun edicionOrganizacion_cancelarEdicionLimpiaFormulario() {
        viewModel.editarOrganizacion(org(id = 18L))

        viewModel.cancelarEdicion()

        assertEquals("", viewModel.uiState.value.nombre)
        assertNull(viewModel.uiState.value.idEnEdicion)
    }

    @Test
    fun cancelarEliminacionOrganizacion_limpiaEstadoPendiente() {
        val o = org(id = 22L)
        viewModel.solicitarEliminacion(o)

        viewModel.cancelarEliminacion()

        assertNull(viewModel.uiState.value.organizacionPorEliminar)
    }

    @Test
    fun guardarOrganizacion_tipoVacio_fallaTipo() {
        viewModel.cambiarNombre("Clínica Dental")
        viewModel.cambiarTipo("") // Tipo vacío
        viewModel.cambiarRubro("Salud")
        viewModel.cambiarComuna("Providencia")
        viewModel.cambiarRegion("Metropolitana")
        viewModel.cambiarPublicoAsociado("Colaboradores")

        viewModel.guardarOrganizacion()

        assertTrue(viewModel.uiState.value.errorTipo)
        assertEquals("Completa los campos obligatorios.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun guardarOrganizacion_rubroVacio_fallaRubro() {
        viewModel.cambiarNombre("Clínica Dental")
        viewModel.cambiarTipo("Empresa")
        viewModel.cambiarRubro("") // Rubro vacío
        viewModel.cambiarComuna("Providencia")
        viewModel.cambiarRegion("Metropolitana")
        viewModel.cambiarPublicoAsociado("Colaboradores")

        viewModel.guardarOrganizacion()

        assertTrue(viewModel.uiState.value.errorRubro)
    }

    @Test
    fun guardarOrganizacion_comunaVacia_fallaComuna() {
        viewModel.cambiarNombre("Clínica Dental")
        viewModel.cambiarTipo("Empresa")
        viewModel.cambiarRubro("Salud")
        viewModel.cambiarComuna("") // Comuna vacía
        viewModel.cambiarRegion("Metropolitana")
        viewModel.cambiarPublicoAsociado("Colaboradores")

        viewModel.guardarOrganizacion()

        assertTrue(viewModel.uiState.value.errorComuna)
    }

    @Test
    fun guardarOrganizacion_regionVacia_fallaRegion() {
        viewModel.cambiarNombre("Clínica Dental")
        viewModel.cambiarTipo("Empresa")
        viewModel.cambiarRubro("Salud")
        viewModel.cambiarComuna("Providencia")
        viewModel.cambiarRegion("") // Región vacía
        viewModel.cambiarPublicoAsociado("Colaboradores")

        viewModel.guardarOrganizacion()

        assertTrue(viewModel.uiState.value.errorRegion)
    }

    @Test
    fun guardarOrganizacion_publicoAsociadoVacio_fallaPublicoAsociado() {
        viewModel.cambiarNombre("Clínica Dental")
        viewModel.cambiarTipo("Empresa")
        viewModel.cambiarRubro("Salud")
        viewModel.cambiarComuna("Providencia")
        viewModel.cambiarRegion("Metropolitana")
        viewModel.cambiarPublicoAsociado("") // Público vacío

        viewModel.guardarOrganizacion()

        assertTrue(viewModel.uiState.value.errorPublicoAsociado)
    }
}
