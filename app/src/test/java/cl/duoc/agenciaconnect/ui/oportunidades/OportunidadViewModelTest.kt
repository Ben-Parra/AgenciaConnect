package cl.duoc.agenciaconnect.ui.oportunidades

import android.app.Application
import cl.duoc.agenciaconnect.MainDispatcherRule
import cl.duoc.agenciaconnect.data.local.AgenciaDatabase
import cl.duoc.agenciaconnect.data.local.EventoDao
import cl.duoc.agenciaconnect.data.local.OportunidadDao
import cl.duoc.agenciaconnect.data.local.OrganizacionDao
import cl.duoc.agenciaconnect.data.local.UsuarioDao
import cl.duoc.agenciaconnect.data.model.EstadoOportunidad
import cl.duoc.agenciaconnect.data.model.Oportunidad
import cl.duoc.agenciaconnect.data.model.Organizacion
import cl.duoc.agenciaconnect.data.model.PotencialOportunidad
import cl.duoc.agenciaconnect.data.model.PrioridadOportunidad
import cl.duoc.agenciaconnect.data.model.TipoOportunidad
import cl.duoc.agenciaconnect.data.model.Usuario
import cl.duoc.agenciaconnect.data.repository.EventoRepository
import cl.duoc.agenciaconnect.data.repository.OportunidadRepository
import cl.duoc.agenciaconnect.data.repository.OrganizacionRepository
import cl.duoc.agenciaconnect.data.repository.UsuarioRepository
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
class OportunidadViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var application: Application
    private lateinit var mockDatabase: AgenciaDatabase
    private lateinit var mockOportunidadDao: OportunidadDao
    private lateinit var mockOrganizacionDao: OrganizacionDao
    private lateinit var mockEventoDao: EventoDao
    private lateinit var mockUsuarioDao: UsuarioDao
    private lateinit var viewModel: OportunidadViewModel

    private val orgRegistrada = Organizacion(
        id = 1L, nombre = "Clínica Dental", tipo = "Empresa", rubro = "Salud",
        comuna = "Providencia", region = "Metropolitana", sitioWeb = "",
        publicoAsociado = "Colaboradores", observaciones = ""
    )
    private val supervisor = Usuario(
        id = 1L, nombre = "Patricia Vega", correo = "supervisor@agencia.cl",
        contrasena = "super123", rol = "SUPERVISOR"
    )
    private val ejecutivo = Usuario(
        id = 2L, nombre = "Carlos Muñoz", correo = "carlos.comercial@agencia.cl",
        contrasena = "eje123", rol = "EJECUTIVO"
    )

    private fun oportunidad(id: Long = 1L, responsable: String = "Carlos Muñoz") = Oportunidad(
        id = id, nombre = "Activación Dental",
        tipo = TipoOportunidad.FERIA_EMPRESARIAL,
        organizacionId = 1L,
        prioridad = PrioridadOportunidad.ALTA,
        potencial = PotencialOportunidad.ALTO,
        estado = EstadoOportunidad.PROPUESTA_ENVIADA,
        responsable = responsable,
        observaciones = "Sin notas",
        fechaDeteccion = "10082024"
    )

    @Before
    fun setUp() {
        application = mockk(relaxed = true)
        mockOportunidadDao = mockk(relaxed = true)
        mockOrganizacionDao = mockk(relaxed = true)
        mockEventoDao = mockk(relaxed = true)
        mockUsuarioDao = mockk(relaxed = true)
        mockDatabase = mockk(relaxed = true)

        every { mockDatabase.oportunidadDao() } returns mockOportunidadDao
        every { mockDatabase.organizacionDao() } returns mockOrganizacionDao
        every { mockDatabase.eventoDao() } returns mockEventoDao
        every { mockDatabase.usuarioDao() } returns mockUsuarioDao
        every { mockOportunidadDao.obtenerOportunidades() } returns flowOf(emptyList())
        every { mockOrganizacionDao.obtenerOrganizaciones() } returns flowOf(listOf(orgRegistrada))
        every { mockEventoDao.obtenerEventos() } returns flowOf(emptyList())
        every { mockUsuarioDao.obtenerUsuarios() } returns flowOf(listOf(supervisor, ejecutivo))
        every { mockUsuarioDao.obtenerEjecutivos() } returns flowOf(listOf(ejecutivo))

        mockkObject(AgenciaDatabase)
        every { AgenciaDatabase.obtenerInstancia(any()) } returns mockDatabase

        mockkConstructor(OportunidadRepository::class)
        mockkConstructor(OrganizacionRepository::class)
        mockkConstructor(EventoRepository::class)
        mockkConstructor(UsuarioRepository::class)
        coEvery { anyConstructed<OportunidadRepository>().sincronizarDesdeApi() } returns Unit
        coEvery { anyConstructed<OrganizacionRepository>().sincronizarDesdeApi() } returns Unit
        coEvery { anyConstructed<EventoRepository>().sincronizarDesdeApi() } returns Unit
        coEvery { anyConstructed<UsuarioRepository>().sincronizarDesdeApi() } returns Unit

        viewModel = OportunidadViewModel(application)
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
    fun cambiarCampos_actualizaValoresEnEstado() {
        viewModel.cambiarNombre("Activación Dental")
        viewModel.cambiarEvento(5L)
        viewModel.cambiarObservaciones("Notas adicionales")
        viewModel.cambiarFechaDeteccion("10082024")

        val estado = viewModel.uiState.value
        assertEquals("Activación Dental", estado.nombre)
        assertEquals(5L, estado.eventoId)
        assertEquals("Notas adicionales", estado.observaciones)
        assertEquals("10082024", estado.fechaDeteccion)
    }

    // ── guardarOportunidad ────────────────────────────────────────────────

    @Test
    fun guardarOportunidad_camposVacios_marcaErrores() {
        viewModel.guardarOportunidad()
        val estado = viewModel.uiState.value
        assertTrue(estado.errorNombre)
        assertTrue(estado.errorTipo)
        assertTrue(estado.errorOrganizacion)
        assertTrue(estado.errorPrioridad)
        assertTrue(estado.errorPotencial)
        assertTrue(estado.errorResponsable)
    }

    @Test
    fun guardarOportunidad_excepcionRepositorio_muestraMensajeError() = runTest {
        viewModel.cambiarNombre("Activación Dental")
        viewModel.cambiarTipo(TipoOportunidad.FERIA_EMPRESARIAL)
        viewModel.cambiarOrganizacion(1L)
        viewModel.cambiarPrioridad(PrioridadOportunidad.ALTA)
        viewModel.cambiarPotencial(PotencialOportunidad.ALTO)
        viewModel.cambiarEstado(EstadoOportunidad.PROPUESTA_ENVIADA)
        viewModel.cambiarResponsable("Carlos Muñoz")
        viewModel.cambiarFechaDeteccion("10082024")
        coEvery { anyConstructed<OportunidadRepository>().insertarOportunidad(any()) } throws RuntimeException("Error BD")

        viewModel.guardarOportunidad()
        advanceUntilIdle()

        assertEquals("No se pudo guardar la oportunidad. Inténtalo nuevamente.", viewModel.uiState.value.mensaje)
    }

    // ── cargarOportunidadParaEditar / reiniciarFormularioAlSalir ──────────

    @Test
    fun cargarOportunidadParaEditar_estableceDatosYEdicion() {
        val op = oportunidad(id = 8L)
        viewModel.cargarOportunidadParaEditar(op)

        val estado = viewModel.uiState.value
        assertEquals("Activación Dental", estado.nombre)
        assertEquals(8L, estado.idEnEdicion)
    }

    @Test
    fun reiniciarFormularioAlSalir_reseteaFormularioYMensaje() {
        viewModel.cambiarNombre("Prueba")
        viewModel.reiniciarFormularioAlSalir()

        val estado = viewModel.uiState.value
        assertEquals("", estado.nombre)
        assertNull(estado.mensaje)
    }

    // ── eliminarOportunidad excepcion ──────────────────────────────────────

    @Test
    fun eliminarOportunidad_excepcionRepositorio_muestraMensajeError() = runTest {
        val op = oportunidad(id = 9L)
        coEvery { anyConstructed<OportunidadRepository>().eliminarOportunidad(op) } throws RuntimeException("Error")

        viewModel.eliminarOportunidad(op)
        advanceUntilIdle()

        assertEquals("No se pudo eliminar la oportunidad.", viewModel.uiState.value.mensaje)
    }

    // ── Pruebas adicionales de ramas ───────────────────────────────────────

    @Test
    fun flujoEliminacionOportunidad_solicitaYConfirmaConDialogo() = runTest {
        val op = oportunidad(id = 25L)
        viewModel.solicitarEliminacion(op)
        assertEquals(op, viewModel.uiState.value.oportunidadPorEliminar)

        coEvery { anyConstructed<OportunidadRepository>().eliminarOportunidad(op) } returns Unit

        viewModel.confirmarEliminacion()
        advanceUntilIdle()

        coVerify { anyConstructed<OportunidadRepository>().eliminarOportunidad(op) }
        assertNull(viewModel.uiState.value.oportunidadPorEliminar)
    }

    @Test
    fun flujoEliminacionOportunidad_cancelarEliminacionLimpiaPendiente() {
        val op = oportunidad(id = 30L)
        viewModel.solicitarEliminacion(op)

        viewModel.cancelarEliminar()

        assertNull(viewModel.uiState.value.oportunidadPorEliminar)
    }

    @Test
    fun edicionOportunidad_conIdPositivo_guardaYLlamaActualizarOportunidad() = runTest {
        val op = oportunidad(id = 40L)
        viewModel.editarOportunidad(op)
        viewModel.cambiarNombre("Activación Dental Modificada")
        coEvery { anyConstructed<OportunidadRepository>().actualizarOportunidad(any()) } returns Unit

        viewModel.guardarOportunidad()
        advanceUntilIdle()

        coVerify { anyConstructed<OportunidadRepository>().actualizarOportunidad(any()) }
        assertEquals("Oportunidad actualizada correctamente.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun cancelarEdicionOportunidad_limpiaCampos() {
        val op = oportunidad(id = 50L)
        viewModel.editarOportunidad(op)

        viewModel.cancelarEdicion()

        assertEquals("", viewModel.uiState.value.nombre)
        assertNull(viewModel.uiState.value.idEnEdicion)
    }

    @Test
    fun guardarOportunidad_fallaFechaDeteccion() {
        viewModel.cambiarNombre("Activación Dental")
        viewModel.cambiarTipo(TipoOportunidad.FERIA_EMPRESARIAL)
        viewModel.cambiarOrganizacion(1L)
        viewModel.cambiarPrioridad(PrioridadOportunidad.ALTA)
        viewModel.cambiarPotencial(PotencialOportunidad.ALTO)
        viewModel.cambiarEstado(EstadoOportunidad.PROPUESTA_ENVIADA)
        viewModel.cambiarResponsable("Carlos Muñoz")
        viewModel.cambiarFechaDeteccion("") // Fecha vacía/inválida

        viewModel.guardarOportunidad()

        assertTrue(viewModel.uiState.value.errorFechaDeteccion)
        assertEquals("Revisa los campos obligatorios y la fecha de detección.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun guardarOportunidad_fallaTipoNulo() {
        viewModel.cambiarNombre("Activación Dental")
        viewModel.cambiarTipo(null) // Tipo nulo
        viewModel.cambiarOrganizacion(1L)
        viewModel.cambiarPrioridad(PrioridadOportunidad.ALTA)
        viewModel.cambiarPotencial(PotencialOportunidad.ALTO)
        viewModel.cambiarEstado(EstadoOportunidad.PROPUESTA_ENVIADA)
        viewModel.cambiarResponsable("Carlos Muñoz")
        viewModel.cambiarFechaDeteccion("10082024")

        viewModel.guardarOportunidad()

        assertTrue(viewModel.uiState.value.errorTipo)
    }

    @Test
    fun guardarOportunidad_fallaPrioridadNula() {
        viewModel.cambiarNombre("Activación Dental")
        viewModel.cambiarTipo(TipoOportunidad.FERIA_EMPRESARIAL)
        viewModel.cambiarOrganizacion(1L)
        viewModel.cambiarPrioridad(null) // Prioridad nula
        viewModel.cambiarPotencial(PotencialOportunidad.ALTO)
        viewModel.cambiarEstado(EstadoOportunidad.PROPUESTA_ENVIADA)
        viewModel.cambiarResponsable("Carlos Muñoz")
        viewModel.cambiarFechaDeteccion("10082024")

        viewModel.guardarOportunidad()

        assertTrue(viewModel.uiState.value.errorPrioridad)
    }

    @Test
    fun guardarOportunidad_fallaPotencialNulo() {
        viewModel.cambiarNombre("Activación Dental")
        viewModel.cambiarTipo(TipoOportunidad.FERIA_EMPRESARIAL)
        viewModel.cambiarOrganizacion(1L)
        viewModel.cambiarPrioridad(PrioridadOportunidad.ALTA)
        viewModel.cambiarPotencial(null) // Potencial nulo
        viewModel.cambiarEstado(EstadoOportunidad.PROPUESTA_ENVIADA)
        viewModel.cambiarResponsable("Carlos Muñoz")
        viewModel.cambiarFechaDeteccion("10082024")

        viewModel.guardarOportunidad()

        assertTrue(viewModel.uiState.value.errorPotencial)
    }

    @Test
    fun guardarOportunidad_fallaResponsableVacio() {
        viewModel.cambiarNombre("Activación Dental")
        viewModel.cambiarTipo(TipoOportunidad.FERIA_EMPRESARIAL)
        viewModel.cambiarOrganizacion(1L)
        viewModel.cambiarPrioridad(PrioridadOportunidad.ALTA)
        viewModel.cambiarPotencial(PotencialOportunidad.ALTO)
        viewModel.cambiarEstado(EstadoOportunidad.PROPUESTA_ENVIADA)
        viewModel.cambiarResponsable("") // Responsable vacío
        viewModel.cambiarFechaDeteccion("10082024")

        viewModel.guardarOportunidad()

        assertTrue(viewModel.uiState.value.errorResponsable)
    }

    @Test
    fun solicitarEliminarYCancelarEliminarAuxiliares() {
        val op = oportunidad(id = 60L)
        viewModel.solicitarEliminar(op)
        assertEquals(op, viewModel.uiState.value.oportunidadPorEliminar)

        viewModel.cancelarEliminar()
        assertNull(viewModel.uiState.value.oportunidadPorEliminar)
    }

    @Test
    fun confirmarEliminacion_sinOportunidadPendiente_noHaceNada() = runTest {
        assertNull(viewModel.uiState.value.oportunidadPorEliminar)
        viewModel.confirmarEliminacion()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.oportunidadPorEliminar)
    }

    @Test
    fun propiedadesFiltrosYOportunidadSeleccionada_lecturaYEscritura_actualizanEstado() {
        val op = oportunidad(id = 70L)

        viewModel.filtroTexto = "Feria"
        viewModel.filtroTipo = "Feria"
        viewModel.filtroOrganizacion = "Duoc"
        viewModel.filtroUbicacion = "Santiago"
        viewModel.oportunidadSeleccionada = op

        assertEquals("Feria", viewModel.filtroTexto)
        assertEquals("Feria", viewModel.filtroTipo)
        assertEquals("Duoc", viewModel.filtroOrganizacion)
        assertEquals("Santiago", viewModel.filtroUbicacion)
        assertEquals(op, viewModel.oportunidadSeleccionada)
    }

    @Test
    fun guardarOportunidad_nombreVacioYOrganizacionNula_activanErroresDeValidacion() {
        viewModel.guardarOportunidad()
        assertTrue(viewModel.uiState.value.errorNombre)
        assertTrue(viewModel.uiState.value.errorOrganizacion)

        viewModel.cambiarNombre("Nombre Válido")
        viewModel.guardarOportunidad()
        assertTrue(viewModel.uiState.value.errorOrganizacion)
    }

    @Test
    fun guardarOportunidad_organizacionNoRegistrada_activaErrorOrganizacion() {
        viewModel.cambiarNombre("Activación Dental")
        viewModel.cambiarTipo(TipoOportunidad.FERIA_EMPRESARIAL)
        viewModel.cambiarOrganizacion(999L)
        viewModel.cambiarPrioridad(PrioridadOportunidad.ALTA)
        viewModel.cambiarPotencial(PotencialOportunidad.ALTO)
        viewModel.cambiarEstado(EstadoOportunidad.PROPUESTA_ENVIADA)
        viewModel.cambiarResponsable("Carlos Muñoz")
        viewModel.cambiarFechaDeteccion("10082024")

        viewModel.guardarOportunidad()

        assertTrue(viewModel.uiState.value.errorOrganizacion)
        assertEquals("Debes seleccionar una organización válida registrada.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun guardarOportunidad_datosValidosNueva_insertaYLimpiaFormulario() = runTest {
        viewModel.cambiarNombre("Activación Dental")
        viewModel.cambiarTipo(TipoOportunidad.FERIA_EMPRESARIAL)
        viewModel.cambiarOrganizacion(1L)
        viewModel.cambiarPrioridad(PrioridadOportunidad.ALTA)
        viewModel.cambiarPotencial(PotencialOportunidad.ALTO)
        viewModel.cambiarEstado(EstadoOportunidad.PROPUESTA_ENVIADA)
        viewModel.cambiarResponsable("Carlos Muñoz")
        viewModel.cambiarFechaDeteccion("10082024")
        coEvery { anyConstructed<OportunidadRepository>().insertarOportunidad(any()) } returns 1L

        viewModel.guardarOportunidad()
        advanceUntilIdle()

        coVerify { anyConstructed<OportunidadRepository>().insertarOportunidad(any()) }
        assertEquals("Oportunidad registrada correctamente.", viewModel.uiState.value.mensaje)
        assertEquals("", viewModel.uiState.value.nombre)
    }

    @Test
    fun guardarOportunidad_sinOrganizacionesRegistradas_activaErrorOrganizacion() {
        every { mockOrganizacionDao.obtenerOrganizaciones() } returns flowOf(emptyList())
        val vmSinOrgs = OportunidadViewModel(application)

        vmSinOrgs.cambiarNombre("Activación Dental")
        vmSinOrgs.cambiarTipo(TipoOportunidad.FERIA_EMPRESARIAL)
        vmSinOrgs.cambiarOrganizacion(1L)
        vmSinOrgs.cambiarPrioridad(PrioridadOportunidad.ALTA)
        vmSinOrgs.cambiarPotencial(PotencialOportunidad.ALTO)
        vmSinOrgs.cambiarEstado(EstadoOportunidad.PROPUESTA_ENVIADA)
        vmSinOrgs.cambiarResponsable("Carlos Muñoz")
        vmSinOrgs.cambiarFechaDeteccion("10082024")

        vmSinOrgs.guardarOportunidad()

        assertTrue(vmSinOrgs.uiState.value.errorOrganizacion)
    }
}
