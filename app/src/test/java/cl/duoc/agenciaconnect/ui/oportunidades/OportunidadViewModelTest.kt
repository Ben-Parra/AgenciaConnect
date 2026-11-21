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

    // Datos de prueba compartidos.
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
        // Organizamos: preparamos mocks de todos los DAOs.
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

    // ── Estado inicial ────────────────────────────────────────────────────

    @Test
    fun estadoInicial_camposVaciosYSinErrores() {
        // Afirmamos.
        val estado = viewModel.uiState.value
        assertEquals("", estado.nombre)
        assertFalse(estado.errorNombre)
        assertNull(estado.idEnEdicion)
    }

    // ── cambiarNombre ─────────────────────────────────────────────────────

    @Test
    fun cambiarNombre_actualizaYLimpiaError() {
        // Actuamos.
        viewModel.cambiarNombre("Activación Dental")
        // Afirmamos.
        assertEquals("Activación Dental", viewModel.uiState.value.nombre)
        assertFalse(viewModel.uiState.value.errorNombre)
    }

    // ── cambiarFechaDeteccion ─────────────────────────────────────────────

    @Test
    fun cambiarFechaDeteccion_filtrarLetrasYLimitarOcho() {
        // Organizamos: texto mixto con más de 8 dígitos.
        viewModel.cambiarFechaDeteccion("01ab082024999")
        // Afirmamos: solo los primeros 8 dígitos.
        assertEquals("01082024", viewModel.uiState.value.fechaDeteccion)
    }

    // ── guardarOportunidad con campos vacíos ──────────────────────────────

    @Test
    fun guardarOportunidad_camposVacios_marcaErrores() {
        // Actuamos.
        viewModel.guardarOportunidad()
        // Afirmamos.
        val estado = viewModel.uiState.value
        assertTrue(estado.errorNombre)
        assertTrue(estado.errorTipo)
        assertTrue(estado.errorOrganizacion)
        assertTrue(estado.errorPrioridad)
        assertTrue(estado.errorPotencial)
        assertTrue(estado.errorResponsable)
    }

    // ── guardarOportunidad sin organización válida ────────────────────────

    @Test
    fun guardarOportunidad_organizacionNoRegistrada_marcaErrorOrganizacion() {
        // Organizamos: llenamos todos los campos pero con organización inexistente.
        viewModel.cambiarNombre("Activación")
        viewModel.cambiarTipo(TipoOportunidad.FERIA_EMPRESARIAL)
        viewModel.cambiarOrganizacion(99L) // No existe en el estado.
        viewModel.cambiarPrioridad(PrioridadOportunidad.ALTA)
        viewModel.cambiarPotencial(PotencialOportunidad.ALTO)
        viewModel.cambiarEstado(EstadoOportunidad.PROPUESTA_ENVIADA)
        viewModel.cambiarResponsable("Carlos")
        viewModel.cambiarFechaDeteccion("10082024")

        // Actuamos.
        viewModel.guardarOportunidad()

        // Afirmamos: error por organización no encontrada en la lista.
        assertTrue(viewModel.uiState.value.errorOrganizacion)
    }

    // ── guardarOportunidad exitoso (nueva) ────────────────────────────────

    @Test
    fun guardarOportunidad_datosValidos_insertaOportunidad() = runTest {
        // Organizamos: llenamos todos los campos con datos válidos.
        viewModel.cambiarNombre("Activación Dental")
        viewModel.cambiarTipo(TipoOportunidad.FERIA_EMPRESARIAL)
        viewModel.cambiarOrganizacion(1L)   // Org registrada en el mock.
        viewModel.cambiarPrioridad(PrioridadOportunidad.ALTA)
        viewModel.cambiarPotencial(PotencialOportunidad.ALTO)
        viewModel.cambiarEstado(EstadoOportunidad.PROPUESTA_ENVIADA)
        viewModel.cambiarResponsable("Carlos Muñoz")
        viewModel.cambiarFechaDeteccion("10082024")
        coEvery { anyConstructed<OportunidadRepository>().insertarOportunidad(any()) } returns Unit

        // Actuamos.
        viewModel.guardarOportunidad()
        advanceUntilIdle()

        // Afirmamos: sin errores de validación.
        assertFalse(viewModel.uiState.value.errorNombre)
        assertFalse(viewModel.uiState.value.errorTipo)
    }

    // ── Filtrado por rol ──────────────────────────────────────────────────

    @Test
    fun oportunidades_supervisorVeTodas_sinFiltro() = runTest {
        // Organizamos: lista con oportunidades de distintos responsables.
        val opCarlos = oportunidad(id = 1L, responsable = "Carlos Muñoz")
        val opMarcela = oportunidad(id = 2L, responsable = "Marcela Rojas")
        every { mockOportunidadDao.obtenerOportunidades() } returns flowOf(listOf(opCarlos, opMarcela))

        // Recreamos el ViewModel con el nuevo mock.
        viewModel = OportunidadViewModel(application)
        advanceUntilIdle()

        // Actuamos: el Supervisor ve todas (sin filtro de responsable).
        val todasOportunidades = viewModel.uiState.value.oportunidades

        // Afirmamos: el flujo entrega ambas oportunidades.
        assertEquals(2, todasOportunidades.size)
    }

    @Test
    fun oportunidades_ejecutivoFiltraPoResponsable() = runTest {
        // Organizamos: solo oportunidades asignadas a Carlos.
        val opCarlos = oportunidad(id = 1L, responsable = "Carlos Muñoz")
        val opMarcela = oportunidad(id = 2L, responsable = "Marcela Rojas")
        every { mockOportunidadDao.obtenerOportunidades() } returns flowOf(listOf(opCarlos, opMarcela))
        viewModel = OportunidadViewModel(application)
        advanceUntilIdle()

        // Actuamos: filtramos manualmente como lo haría la pantalla del Ejecutivo.
        val opsFiltradas = viewModel.uiState.value.oportunidades.filter {
            it.responsable.contains("Carlos")
        }

        // Afirmamos: solo la oportunidad de Carlos.
        assertEquals(1, opsFiltradas.size)
        assertEquals("Carlos Muñoz", opsFiltradas.first().responsable)
    }

    // ── editarOportunidad ─────────────────────────────────────────────────

    @Test
    fun editarOportunidad_cargaDatosYSetIdEnEdicion() {
        // Organizamos.
        val op = oportunidad(id = 10L)
        // Actuamos.
        viewModel.editarOportunidad(op)
        // Afirmamos.
        val estado = viewModel.uiState.value
        assertEquals("Activación Dental", estado.nombre)
        assertEquals(10L, estado.idEnEdicion)
        assertEquals(TipoOportunidad.FERIA_EMPRESARIAL, estado.tipo)
    }

    // ── cancelarEdicion ───────────────────────────────────────────────────

    @Test
    fun cancelarEdicion_limpiaCampos() {
        // Organizamos.
        viewModel.editarOportunidad(oportunidad(id = 5L))
        // Actuamos.
        viewModel.cancelarEdicion()
        // Afirmamos.
        assertEquals("", viewModel.uiState.value.nombre)
        assertNull(viewModel.uiState.value.idEnEdicion)
    }

    // ── solicitarEliminacion / cancelarEliminacion ────────────────────────

    @Test
    fun solicitarEliminar_estableceOportunidadPendiente() {
        // Organizamos.
        val op = oportunidad(id = 7L)
        // Actuamos.
        viewModel.solicitarEliminar(op)
        // Afirmamos.
        assertEquals(op, viewModel.uiState.value.oportunidadPorEliminar)
    }

    @Test
    fun cancelarEliminar_limpiaPendiente() {
        // Organizamos.
        viewModel.solicitarEliminar(oportunidad(id = 7L))
        // Actuamos.
        viewModel.cancelarEliminar()
        // Afirmamos.
        assertNull(viewModel.uiState.value.oportunidadPorEliminar)
    }

    // ── eliminarOportunidad ───────────────────────────────────────────────

    @Test
    fun eliminarOportunidad_conId_ejecutaEliminar() = runTest {
        // Organizamos.
        val op = oportunidad(id = 9L)
        coEvery { anyConstructed<OportunidadRepository>().eliminarOportunidad(op) } returns Unit

        // Actuamos.
        viewModel.eliminarOportunidad(op)
        advanceUntilIdle()

        // Afirmamos: sin pendiente de eliminación.
        assertNull(viewModel.uiState.value.oportunidadPorEliminar)
    }

    // ── confirmarEliminacion ──────────────────────────────────────────────

    @Test
    fun confirmarEliminacion_conPendiente_eliminaOportunidad() = runTest {
        // Organizamos.
        val op = oportunidad(id = 11L)
        viewModel.solicitarEliminar(op)
        coEvery { anyConstructed<OportunidadRepository>().eliminarOportunidad(op) } returns Unit

        // Actuamos.
        viewModel.confirmarEliminacion()
        advanceUntilIdle()

        // Afirmamos.
        assertNull(viewModel.uiState.value.oportunidadPorEliminar)
    }

    // ── limpiarFormulario ─────────────────────────────────────────────────

    @Test
    fun limpiarFormulario_reseteaTodosLosCampos() {
        // Organizamos.
        viewModel.cambiarNombre("Algo")
        viewModel.cambiarTipo(TipoOportunidad.FERIA_EMPRESARIAL)
        viewModel.editarOportunidad(oportunidad(id = 2L))
        // Actuamos.
        viewModel.limpiarFormulario()
        // Afirmamos.
        val estado = viewModel.uiState.value
        assertEquals("", estado.nombre)
        assertNull(estado.tipo)
        assertNull(estado.idEnEdicion)
    }
}
