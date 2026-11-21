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
    fun cambiarNombre_actualizaYLimpiaError() {
        viewModel.cambiarNombre("Feria de Salud")
        assertEquals("Feria de Salud", viewModel.uiState.value.nombre)
        assertFalse(viewModel.uiState.value.errorNombre)
    }

    @Test
    fun cambiarFecha_filtrarLetrasYLimitarOcho() {
        viewModel.cambiarFecha("01ab06202799")
        assertEquals("01062027", viewModel.uiState.value.fecha)
    }

    // ── validarFormulario fallos individuales ─────────────────────────────

    @Test
    fun validarFormulario_camposVacios_retornaFalseYMarcaErrores() {
        val resultado = viewModel.validarFormulario()
        assertFalse(resultado)
        assertTrue(viewModel.uiState.value.errorNombre)
        assertTrue(viewModel.uiState.value.errorDescripcion)
        assertTrue(viewModel.uiState.value.errorFecha)
        assertTrue(viewModel.uiState.value.errorUbicacion)
    }

    @Test
    fun validarFormulario_nombreVacio_fallaNombre() {
        viewModel.cambiarNombre("")
        viewModel.cambiarDescripcion("Expo dental")
        viewModel.cambiarFecha("15062027")
        viewModel.cambiarUbicacion("Espacio Riesco")
        viewModel.cambiarOrganizacion("Clínica Dental")
        viewModel.cambiarImagenUri("content://img/1")

        assertFalse(viewModel.validarFormulario())
        assertTrue(viewModel.uiState.value.errorNombre)
    }

    @Test
    fun validarFormulario_descripcionVacia_fallaDescripcion() {
        viewModel.cambiarNombre("Feria Dental")
        viewModel.cambiarDescripcion("")
        viewModel.cambiarFecha("15062027")
        viewModel.cambiarUbicacion("Espacio Riesco")
        viewModel.cambiarOrganizacion("Clínica Dental")
        viewModel.cambiarImagenUri("content://img/1")

        assertFalse(viewModel.validarFormulario())
        assertTrue(viewModel.uiState.value.errorDescripcion)
    }

    @Test
    fun validarFormulario_fechaInvalida_fallaFecha() {
        viewModel.cambiarNombre("Feria Dental")
        viewModel.cambiarDescripcion("Expo dental")
        viewModel.cambiarFecha("123") // menos de 8 digitos
        viewModel.cambiarUbicacion("Espacio Riesco")
        viewModel.cambiarOrganizacion("Clínica Dental")
        viewModel.cambiarImagenUri("content://img/1")

        assertFalse(viewModel.validarFormulario())
        assertTrue(viewModel.uiState.value.errorFecha)
    }

    @Test
    fun validarFormulario_ubicacionVacia_fallaUbicacion() {
        viewModel.cambiarNombre("Feria Dental")
        viewModel.cambiarDescripcion("Expo dental")
        viewModel.cambiarFecha("15062027")
        viewModel.cambiarUbicacion("")
        viewModel.cambiarOrganizacion("Clínica Dental")
        viewModel.cambiarImagenUri("content://img/1")

        assertFalse(viewModel.validarFormulario())
        assertTrue(viewModel.uiState.value.errorUbicacion)
    }

    @Test
    fun validarFormulario_organizacionVacia_fallaOrganizacion() {
        viewModel.cambiarNombre("Feria Dental")
        viewModel.cambiarDescripcion("Expo dental")
        viewModel.cambiarFecha("15062027")
        viewModel.cambiarUbicacion("Espacio Riesco")
        viewModel.cambiarOrganizacion("")
        viewModel.cambiarImagenUri("content://img/1")

        assertFalse(viewModel.validarFormulario())
        assertTrue(viewModel.uiState.value.errorOrganizacion)
    }

    @Test
    fun validarFormulario_todosLosValidos_retornaTrue() {
        viewModel.cambiarNombre("Feria Dental")
        viewModel.cambiarDescripcion("Expo dental")
        viewModel.cambiarFecha("15062027")
        viewModel.cambiarUbicacion("Espacio Riesco")
        viewModel.cambiarOrganizacion("Clínica Dental")
        viewModel.cambiarImagenUri("content://img/1")

        assertTrue(viewModel.validarFormulario())
    }

    // ── guardarEvento ─────────────────────────────────────────────────────

    @Test
    fun guardarEvento_conDatosValidos_llamaInsertarEvento() = runTest {
        viewModel.cambiarNombre("Feria Dental")
        viewModel.cambiarDescripcion("Expo dental")
        viewModel.cambiarFecha("15062027")
        viewModel.cambiarUbicacion("Espacio Riesco")
        viewModel.cambiarOrganizacion("Clínica Dental")
        viewModel.cambiarImagenUri("content://img/1")
        coEvery { anyConstructed<EventoRepository>().insertarEvento(any()) } returns 1L

        viewModel.guardarEvento()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.errorNombre)
    }

    @Test
    fun guardarEvento_excepcionEnRepositorio_muestraMensajeError() = runTest {
        viewModel.cambiarNombre("Feria Dental")
        viewModel.cambiarDescripcion("Expo dental")
        viewModel.cambiarFecha("15062027")
        viewModel.cambiarUbicacion("Espacio Riesco")
        viewModel.cambiarOrganizacion("Clínica Dental")
        viewModel.cambiarImagenUri("content://img/1")
        coEvery { anyConstructed<EventoRepository>().insertarEvento(any()) } throws RuntimeException("Error BD")

        viewModel.guardarEvento()
        advanceUntilIdle()

        assertEquals("No se pudo guardar el evento. Inténtalo nuevamente.", viewModel.uiState.value.mensaje)
    }

    // ── eliminarEvento ────────────────────────────────────────────────────

    @Test
    fun confirmarEliminar_excepcionRepositorio_muestraMensajeError() = runTest {
        val ev = evento(id = 5L)
        viewModel.solicitarEliminar(ev)
        coEvery { anyConstructed<EventoRepository>().eliminarEvento(ev) } throws RuntimeException("Error")

        viewModel.confirmarEliminar()
        advanceUntilIdle()

        assertEquals("No se pudo eliminar el evento. Inténtalo nuevamente.", viewModel.uiState.value.mensaje)
    }

    // ── métodos auxiliares ────────────────────────────────────────────────

    @Test
    fun reiniciarFormularioAlSalir_reseteaFormularioYMensaje() {
        viewModel.cambiarNombre("Feria Dental")
        viewModel.reiniciarFormularioAlSalir()

        val estado = viewModel.uiState.value
        assertEquals("", estado.nombre)
        assertNull(estado.mensaje)
    }

    // ── Pruebas adicionales de ramas ───────────────────────────────────────

    @Test
    fun flujoEliminacionExitosa_solicitaYConfirmaEliminacion() = runTest {
        val ev = evento(id = 10L)
        viewModel.solicitarEliminar(ev)
        coEvery { anyConstructed<EventoRepository>().eliminarEvento(ev) } returns Unit

        viewModel.confirmarEliminar()
        advanceUntilIdle()

        coVerify { anyConstructed<EventoRepository>().eliminarEvento(ev) }
        assertNull(viewModel.uiState.value.eventoPorEliminar)
    }

    @Test
    fun cancelarEliminacion_limpiaEstadoDeDialogo() {
        val ev = evento(id = 12L)
        viewModel.solicitarEliminar(ev)

        viewModel.cancelarEliminar()

        assertNull(viewModel.uiState.value.eventoPorEliminar)
    }

    @Test
    fun edicionEvento_modificaCamposYGuardaLlamandoActualizarEvento() = runTest {
        val ev = evento(id = 15L)
        viewModel.cargarEventoParaEditar(ev)
        viewModel.cambiarNombre("Feria Dental Modificada")
        coEvery { anyConstructed<EventoRepository>().actualizarEvento(any()) } returns Unit

        viewModel.guardarEvento()
        advanceUntilIdle()

        coVerify { anyConstructed<EventoRepository>().actualizarEvento(any()) }
        assertEquals("Evento actualizado correctamente.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun edicionEvento_cancelarEdicionLimpiaFormularioYSetIdNull() {
        val ev = evento(id = 20L)
        viewModel.cargarEventoParaEditar(ev)

        viewModel.cancelarEdicion()

        assertEquals("", viewModel.uiState.value.nombre)
        assertNull(viewModel.uiState.value.idEnEdicion)
    }

    @Test
    fun confirmarEliminar_estabaEditandoMismoEvento_limpiaFormularioCompleto() = runTest {
        val ev = evento(id = 30L)
        viewModel.cargarEventoParaEditar(ev) // idEnEdicion = 30L
        viewModel.solicitarEliminar(ev)     // eventoPorEliminar = ev
        coEvery { anyConstructed<EventoRepository>().eliminarEvento(ev) } returns Unit

        viewModel.confirmarEliminar()
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertEquals("", estado.nombre)
        assertEquals("", estado.descripcion)
        assertNull(estado.idEnEdicion)
        assertNull(estado.eventoPorEliminar)
    }

    @Test
    fun confirmarEliminar_sinEventoPendiente_noHaceNada() = runTest {
        assertNull(viewModel.uiState.value.eventoPorEliminar)
        viewModel.confirmarEliminar()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.eventoPorEliminar)
    }
}
