package cl.duoc.agenciaconnect.ui.contactos

import android.app.Application
import cl.duoc.agenciaconnect.MainDispatcherRule
import cl.duoc.agenciaconnect.data.local.AgenciaDatabase
import cl.duoc.agenciaconnect.data.local.ContactoDao
import cl.duoc.agenciaconnect.data.local.OrganizacionDao
import cl.duoc.agenciaconnect.data.model.Contacto
import cl.duoc.agenciaconnect.data.model.Organizacion
import cl.duoc.agenciaconnect.data.repository.ContactoRepository
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
class ContactosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var application: Application
    private lateinit var mockDatabase: AgenciaDatabase
    private lateinit var mockContactoDao: ContactoDao
    private lateinit var mockOrganizacionDao: OrganizacionDao
    private lateinit var viewModel: ContactosViewModel

    private val orgRegistrada = Organizacion(
        id = 1L, nombre = "Clínica Dental", tipo = "Empresa", rubro = "Salud",
        comuna = "Providencia", region = "Metropolitana", sitioWeb = "",
        publicoAsociado = "Colaboradores", observaciones = ""
    )

    private fun contacto(id: Long = 0L) = Contacto(
        id = id, nombreCompleto = "Camila Pérez", organizacionId = 1L,
        cargo = "Jefa RRHH", area = "Recursos Humanos",
        correo = "cperez@empresa.cl", telefono = "+56911223344",
        medioPreferido = "Correo", observaciones = ""
    )

    @Before
    fun setUp() {
        // Organizamos: configuramos mocks para la capa de datos.
        application = mockk(relaxed = true)
        mockContactoDao = mockk(relaxed = true)
        mockOrganizacionDao = mockk(relaxed = true)
        mockDatabase = mockk(relaxed = true)

        every { mockDatabase.contactoDao() } returns mockContactoDao
        every { mockDatabase.organizacionDao() } returns mockOrganizacionDao
        every { mockContactoDao.obtenerContactos() } returns flowOf(emptyList())
        every { mockOrganizacionDao.obtenerOrganizaciones() } returns flowOf(listOf(orgRegistrada))

        mockkObject(AgenciaDatabase)
        every { AgenciaDatabase.obtenerInstancia(any()) } returns mockDatabase
        mockkConstructor(ContactoRepository::class)
        coEvery { anyConstructed<ContactoRepository>().sincronizarDesdeApi() } returns Unit

        viewModel = ContactosViewModel(application)
    }

    // ── Estado inicial ────────────────────────────────────────────────────

    @Test
    fun estadoInicial_camposVaciosYSinErrores() {
        // Actuamos.
        val estado = viewModel.uiState.value
        // Afirmamos.
        assertEquals("", estado.nombreCompleto)
        assertFalse(estado.errorNombreCompleto)
        assertNull(estado.idEnEdicion)
    }

    // ── actualizarNombreCompleto ──────────────────────────────────────────

    @Test
    fun actualizarNombreCompleto_actualizaYLimpiaError() {
        // Actuamos.
        viewModel.actualizarNombreCompleto("Camila Pérez")
        // Afirmamos.
        assertEquals("Camila Pérez", viewModel.uiState.value.nombreCompleto)
        assertFalse(viewModel.uiState.value.errorNombreCompleto)
    }

    // ── actualizarTelefono ────────────────────────────────────────────────

    @Test
    fun actualizarTelefono_sinPrefijo_agregaPrefijo56() {
        // Organizamos: valor sin "+56".
        viewModel.actualizarTelefono("911223344")
        // Afirmamos: el ViewModel antepone "+56".
        assertTrue(viewModel.uiState.value.telefono.startsWith("+56"))
    }

    @Test
    fun actualizarTelefono_prefijoSolo_esValido() {
        // Organizamos: solo el prefijo (campo vacío).
        viewModel.actualizarTelefono("+56")
        // Afirmamos: se acepta como vacío válido.
        assertFalse(viewModel.uiState.value.errorTelefono)
    }

    @Test
    fun actualizarTelefono_numeroCompleto_esValido() {
        // Organizamos: número completo chileno.
        viewModel.actualizarTelefono("+56911223344")
        // Afirmamos: válido (12 caracteres).
        assertFalse(viewModel.uiState.value.errorTelefono)
    }

    @Test
    fun actualizarTelefono_numeroIncompleto_esInvalido() {
        // Organizamos: número parcial.
        viewModel.actualizarTelefono("+56911")
        // Afirmamos: inválido.
        assertTrue(viewModel.uiState.value.errorTelefono)
    }

    // ── guardarContacto con campos vacíos ─────────────────────────────────

    @Test
    fun guardarContacto_camposVacios_marcaErrores() {
        // Actuamos.
        viewModel.guardarContacto()
        // Afirmamos.
        val estado = viewModel.uiState.value
        assertTrue(estado.errorNombreCompleto)
        assertTrue(estado.errorOrganizacion)
        assertTrue(estado.errorCargo)
        assertTrue(estado.errorArea)
        assertTrue(estado.errorCorreo)
        assertTrue(estado.errorMedioPreferido)
    }

    // ── guardarContacto correo vacío ──────────────────────────────────────

    @Test
    fun guardarContacto_correoVacio_marcaErrorCorreo() {
        // Organizamos: llenamos todo menos el correo.
        viewModel.actualizarNombreCompleto("Camila Pérez")
        viewModel.actualizarOrganizacion(1L)
        viewModel.actualizarCargo("Jefa RRHH")
        viewModel.actualizarArea("Recursos Humanos")
        viewModel.actualizarCorreo("")
        viewModel.actualizarMedioPreferido("Correo")
        // Actuamos.
        viewModel.guardarContacto()
        // Afirmamos: error en correo vacío.
        assertTrue(viewModel.uiState.value.errorCorreo)
    }

    // ── editarContacto ────────────────────────────────────────────────────

    @Test
    fun editarContacto_cargaDatosEnFormulario() {
        // Organizamos.
        val c = contacto(id = 8L)
        // Actuamos.
        viewModel.editarContacto(c)
        // Afirmamos.
        val estado = viewModel.uiState.value
        assertEquals("Camila Pérez", estado.nombreCompleto)
        assertEquals(8L, estado.idEnEdicion)
    }

    // ── cancelarEdicion ───────────────────────────────────────────────────

    @Test
    fun cancelarEdicion_limpiaCampos() {
        // Organizamos.
        viewModel.editarContacto(contacto(id = 3L))
        // Actuamos.
        viewModel.cancelarEdicion()
        // Afirmamos.
        assertEquals("", viewModel.uiState.value.nombreCompleto)
        assertNull(viewModel.uiState.value.idEnEdicion)
    }

    // ── solicitarEliminarContacto / cancelarEliminarContacto ──────────────

    @Test
    fun solicitarEliminarContacto_estableceContactoPendiente() {
        // Organizamos.
        val c = contacto(id = 5L)
        // Actuamos.
        viewModel.solicitarEliminarContacto(c)
        // Afirmamos.
        assertEquals(c, viewModel.uiState.value.contactoPorEliminar)
    }

    @Test
    fun cancelarEliminarContacto_limpiaPendiente() {
        // Organizamos.
        viewModel.solicitarEliminarContacto(contacto(id = 5L))
        // Actuamos.
        viewModel.cancelarEliminarContacto()
        // Afirmamos.
        assertNull(viewModel.uiState.value.contactoPorEliminar)
    }

    // ── confirmarEliminarContacto ─────────────────────────────────────────

    @Test
    fun confirmarEliminarContacto_conPendiente_eliminaContacto() = runTest {
        // Organizamos.
        val c = contacto(id = 6L)
        viewModel.solicitarEliminarContacto(c)
        coEvery { anyConstructed<ContactoRepository>().eliminarContacto(c) } returns Unit

        // Actuamos.
        viewModel.confirmarEliminarContacto()
        advanceUntilIdle()

        // Afirmamos: se limpió el pendiente.
        assertNull(viewModel.uiState.value.contactoPorEliminar)
    }

    // ── nuevoContacto ─────────────────────────────────────────────────────

    @Test
    fun nuevoContacto_limpiaCamposYPreCargaPrefijo() {
        // Organizamos: estado con datos.
        viewModel.actualizarNombreCompleto("Alguien")
        viewModel.editarContacto(contacto(id = 1L))
        // Actuamos.
        viewModel.nuevoContacto()
        // Afirmamos.
        assertEquals("", viewModel.uiState.value.nombreCompleto)
        assertTrue(viewModel.uiState.value.telefono.startsWith("+56"))
    }
}
