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
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
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
        medioPreferido = "Correo", observaciones = "Notas"
    )

    @Before
    fun setUp() {
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

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun mockearValidacionesContacto() {
        mockkStatic("cl.duoc.agenciaconnect.util.ContactoValidacionesKt")
        every { cl.duoc.agenciaconnect.util.validarNombreContacto(any()) } returns true
        every { cl.duoc.agenciaconnect.util.validarOrganizacionContacto(any()) } returns true
        every { cl.duoc.agenciaconnect.util.validarCargoContacto(any()) } returns true
        every { cl.duoc.agenciaconnect.util.validarAreaContacto(any()) } returns true
        every { cl.duoc.agenciaconnect.util.validarCorreoContacto(any()) } returns true
        every { cl.duoc.agenciaconnect.util.validarMedioPreferidoContacto(any()) } returns true
    }

    // ── Estado inicial ────────────────────────────────────────────────────

    @Test
    fun estadoInicial_camposVaciosYSinErrores() {
        val estado = viewModel.uiState.value
        assertEquals("", estado.nombreCompleto)
        assertFalse(estado.errorNombreCompleto)
        assertNull(estado.idEnEdicion)
    }

    // ── actualizarCampos ──────────────────────────────────────────────────

    @Test
    fun actualizarCampos_actualizanValoresEnEstado() {
        viewModel.actualizarNombreCompleto("Camila Pérez")
        viewModel.actualizarOrganizacion(1L)
        viewModel.actualizarCargo("Jefa RRHH")
        viewModel.actualizarArea("RRHH")
        viewModel.actualizarCorreo("cperez@empresa.cl")
        viewModel.actualizarObservaciones("Observación importante")

        val estado = viewModel.uiState.value
        assertEquals("Camila Pérez", estado.nombreCompleto)
        assertEquals(1L, estado.organizacionId)
        assertEquals("Jefa RRHH", estado.cargo)
        assertEquals("RRHH", estado.area)
        assertEquals("cperez@empresa.cl", estado.correo)
        assertEquals("Observación importante", estado.observaciones)
    }

    // ── actualizarTelefono ────────────────────────────────────────────────

    @Test
    fun actualizarTelefono_sinPrefijo_agregaPrefijo56() {
        viewModel.actualizarTelefono("911223344")
        assertTrue(viewModel.uiState.value.telefono.startsWith("+56"))
    }

    @Test
    fun actualizarTelefono_prefijoSolo_esValido() {
        viewModel.actualizarTelefono("+56")
        assertFalse(viewModel.uiState.value.errorTelefono)
    }

    @Test
    fun actualizarTelefono_numeroCompleto_esValido() {
        viewModel.actualizarTelefono("+56911223344")
        assertFalse(viewModel.uiState.value.errorTelefono)
    }

    @Test
    fun actualizarTelefono_numeroIncompleto_esInvalido() {
        viewModel.actualizarTelefono("+56911")
        assertTrue(viewModel.uiState.value.errorTelefono)
    }

    // ── guardarContacto con validaciones ─────────────────────────────────

    @Test
    fun guardarContacto_camposVacios_marcaErrores() {
        viewModel.guardarContacto()
        val estado = viewModel.uiState.value
        assertTrue(estado.errorNombreCompleto)
        assertTrue(estado.errorOrganizacion)
        assertTrue(estado.errorCargo)
        assertTrue(estado.errorArea)
        assertTrue(estado.errorCorreo)
        assertTrue(estado.errorMedioPreferido)
    }

    @Test
    fun guardarContacto_organizacionInexistente_marcaErrorOrganizacion() {
        mockearValidacionesContacto()

        viewModel.actualizarNombreCompleto("Camila Pérez")
        viewModel.actualizarOrganizacion(999L) // Org que no existe en el estado.
        viewModel.actualizarCargo("Jefa RRHH")
        viewModel.actualizarArea("RRHH")
        viewModel.actualizarCorreo("cperez@empresa.cl")
        viewModel.actualizarMedioPreferido("Correo")

        viewModel.guardarContacto()

        assertTrue(viewModel.uiState.value.errorOrganizacion)
        assertEquals("Selecciona una organización registrada.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun guardarContacto_camposValidosNuevo_llamaInsertarContacto() = runTest {
        mockearValidacionesContacto()
        coEvery { anyConstructed<ContactoRepository>().insertarContacto(any()) } returns 1L

        viewModel.actualizarNombreCompleto("Camila Pérez")
        viewModel.actualizarOrganizacion(1L)
        viewModel.actualizarCargo("Jefa RRHH")
        viewModel.actualizarArea("RRHH")
        viewModel.actualizarCorreo("cperez@empresa.cl")
        viewModel.actualizarMedioPreferido("Correo")

        viewModel.guardarContacto()
        advanceUntilIdle()

        coVerify { anyConstructed<ContactoRepository>().insertarContacto(any()) }
        assertEquals("Contacto registrado correctamente.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun guardarContacto_camposValidosEdicion_llamaActualizarContacto() = runTest {
        mockearValidacionesContacto()
        coEvery { anyConstructed<ContactoRepository>().actualizarContacto(any()) } returns Unit

        viewModel.editarContacto(contacto(id = 10L))
        viewModel.actualizarNombreCompleto("Camila Pérez Actualizada")

        viewModel.guardarContacto()
        advanceUntilIdle()

        coVerify { anyConstructed<ContactoRepository>().actualizarContacto(any()) }
        assertEquals("Contacto actualizado correctamente.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun guardarContacto_excepcionRepositorio_muestraMensajeError() = runTest {
        mockearValidacionesContacto()
        coEvery { anyConstructed<ContactoRepository>().insertarContacto(any()) } throws RuntimeException("Error BD")

        viewModel.actualizarNombreCompleto("Camila Pérez")
        viewModel.actualizarOrganizacion(1L)
        viewModel.actualizarCargo("Jefa RRHH")
        viewModel.actualizarArea("RRHH")
        viewModel.actualizarCorreo("cperez@empresa.cl")
        viewModel.actualizarMedioPreferido("Correo")

        viewModel.guardarContacto()
        advanceUntilIdle()

        assertEquals("No se pudo guardar el contacto. Inténtalo nuevamente.", viewModel.uiState.value.mensaje)
    }

    // ── editarContacto / cancelarEdicion ─────────────────────────────────

    @Test
    fun editarContacto_cargaDatosEnFormulario() {
        val c = contacto(id = 8L)
        viewModel.editarContacto(c)
        val estado = viewModel.uiState.value
        assertEquals("Camila Pérez", estado.nombreCompleto)
        assertEquals(8L, estado.idEnEdicion)
    }

    @Test
    fun cancelarEdicion_limpiaCampos() {
        viewModel.editarContacto(contacto(id = 3L))
        viewModel.cancelarEdicion()
        assertEquals("", viewModel.uiState.value.nombreCompleto)
        assertNull(viewModel.uiState.value.idEnEdicion)
    }

    // ── eliminarContacto ──────────────────────────────────────────────────

    @Test
    fun solicitarEliminarContacto_estableceContactoPendiente() {
        val c = contacto(id = 5L)
        viewModel.solicitarEliminarContacto(c)
        assertEquals(c, viewModel.uiState.value.contactoPorEliminar)
    }

    @Test
    fun cancelarEliminarContacto_limpiaPendiente() {
        viewModel.solicitarEliminarContacto(contacto(id = 5L))
        viewModel.cancelarEliminarContacto()
        assertNull(viewModel.uiState.value.contactoPorEliminar)
    }

    @Test
    fun confirmarEliminarContacto_conPendiente_eliminaContacto() = runTest {
        val c = contacto(id = 6L)
        viewModel.solicitarEliminarContacto(c)
        coEvery { anyConstructed<ContactoRepository>().eliminarContacto(c) } returns Unit

        viewModel.confirmarEliminarContacto()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.contactoPorEliminar)
    }

    @Test
    fun confirmarEliminarContacto_excepcion_muestraMensajeError() = runTest {
        val c = contacto(id = 6L)
        viewModel.solicitarEliminarContacto(c)
        coEvery { anyConstructed<ContactoRepository>().eliminarContacto(c) } throws RuntimeException("Error")

        viewModel.confirmarEliminarContacto()
        advanceUntilIdle()

        assertEquals("No se pudo eliminar el contacto.", viewModel.uiState.value.mensaje)
    }

    // ── métodos auxiliares de limpieza ────────────────────────────────────

    @Test
    fun limpiarMensajes_reseteaMensajeYErrores() {
        viewModel.guardarContacto() // genera errores
        viewModel.limpiarMensajes()

        val estado = viewModel.uiState.value
        assertNull(estado.mensaje)
        assertFalse(estado.errorNombreCompleto)
    }

    @Test
    fun reiniciarFormularioAlSalir_reseteaFormularioYMensaje() {
        viewModel.actualizarNombreCompleto("Camila")
        viewModel.reiniciarFormularioAlSalir()

        val estado = viewModel.uiState.value
        assertEquals("", estado.nombreCompleto)
        assertNull(estado.mensaje)
    }

    // ── Pruebas adicionales de ramas ───────────────────────────────────────

    @Test
    fun editarContacto_cargaTodosLosCamposCorrectamente() {
        val c = contacto(id = 14L)

        viewModel.editarContacto(c)

        val estado = viewModel.uiState.value
        assertEquals("Camila Pérez", estado.nombreCompleto)
        assertEquals(1L, estado.organizacionId)
        assertEquals("Jefa RRHH", estado.cargo)
        assertEquals("Recursos Humanos", estado.area)
        assertEquals("cperez@empresa.cl", estado.correo)
        assertEquals("+56911223344", estado.telefono)
        assertEquals("Correo", estado.medioPreferido)
        assertEquals("Notas", estado.observaciones)
        assertEquals(14L, estado.idEnEdicion)
    }

    @Test
    fun confirmarEliminarContacto_sinPendiente_noHaceNada() = runTest {
        assertNull(viewModel.uiState.value.contactoPorEliminar)

        viewModel.confirmarEliminarContacto()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.contactoPorEliminar)
    }

    @Test
    fun guardarContacto_cargoVacio_fallaCargo() {
        mockearValidacionesContacto()
        every { cl.duoc.agenciaconnect.util.validarCargoContacto("") } returns false

        viewModel.actualizarNombreCompleto("Camila Pérez")
        viewModel.actualizarOrganizacion(1L)
        viewModel.actualizarCargo("") // Cargo vacío
        viewModel.actualizarArea("RRHH")
        viewModel.actualizarCorreo("cperez@empresa.cl")
        viewModel.actualizarMedioPreferido("Correo")

        viewModel.guardarContacto()

        assertTrue(viewModel.uiState.value.errorCargo)
        assertEquals("Revisa los campos obligatorios y el formato del teléfono.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun guardarContacto_areaVacia_fallaArea() {
        mockearValidacionesContacto()
        every { cl.duoc.agenciaconnect.util.validarAreaContacto("") } returns false

        viewModel.actualizarNombreCompleto("Camila Pérez")
        viewModel.actualizarOrganizacion(1L)
        viewModel.actualizarCargo("Jefa RRHH")
        viewModel.actualizarArea("") // Área vacía
        viewModel.actualizarCorreo("cperez@empresa.cl")
        viewModel.actualizarMedioPreferido("Correo")

        viewModel.guardarContacto()

        assertTrue(viewModel.uiState.value.errorArea)
    }

    @Test
    fun guardarContacto_correoInvalido_fallaCorreo() {
        mockearValidacionesContacto()
        every { cl.duoc.agenciaconnect.util.validarCorreoContacto("") } returns false

        viewModel.actualizarNombreCompleto("Camila Pérez")
        viewModel.actualizarOrganizacion(1L)
        viewModel.actualizarCargo("Jefa RRHH")
        viewModel.actualizarArea("RRHH")
        viewModel.actualizarCorreo("") // Correo vacío/inválido
        viewModel.actualizarMedioPreferido("Correo")

        viewModel.guardarContacto()

        assertTrue(viewModel.uiState.value.errorCorreo)
    }

    @Test
    fun guardarContacto_medioPreferidoVacio_fallaMedioPreferido() {
        mockearValidacionesContacto()
        every { cl.duoc.agenciaconnect.util.validarMedioPreferidoContacto("") } returns false

        viewModel.actualizarNombreCompleto("Camila Pérez")
        viewModel.actualizarOrganizacion(1L)
        viewModel.actualizarCargo("Jefa RRHH")
        viewModel.actualizarArea("RRHH")
        viewModel.actualizarCorreo("cperez@empresa.cl")
        viewModel.actualizarMedioPreferido("") // Medio preferido vacío

        viewModel.guardarContacto()

        assertTrue(viewModel.uiState.value.errorMedioPreferido)
    }

    @Test
    fun guardarContacto_organizacionIdCero_fallaOrganizacion() {
        mockearValidacionesContacto()
        every { cl.duoc.agenciaconnect.util.validarOrganizacionContacto(0L) } returns false

        viewModel.actualizarNombreCompleto("Camila Pérez")
        viewModel.actualizarOrganizacion(0L) // id Cero
        viewModel.actualizarCargo("Jefa RRHH")
        viewModel.actualizarArea("RRHH")
        viewModel.actualizarCorreo("cperez@empresa.cl")
        viewModel.actualizarMedioPreferido("Correo")

        viewModel.guardarContacto()

        assertTrue(viewModel.uiState.value.errorOrganizacion)
    }
}
