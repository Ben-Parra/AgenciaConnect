package cl.duoc.agenciaconnect.ui.contactos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaconnect.data.local.AgenciaDatabase
import cl.duoc.agenciaconnect.data.model.Contacto
import cl.duoc.agenciaconnect.data.repository.ContactoRepository
import cl.duoc.agenciaconnect.data.local.OrganizacionDao
import cl.duoc.agenciaconnect.util.validarAreaContacto
import cl.duoc.agenciaconnect.util.validarCargoContacto
import cl.duoc.agenciaconnect.util.validarCorreoContacto
import cl.duoc.agenciaconnect.util.validarMedioPreferidoContacto
import cl.duoc.agenciaconnect.util.validarNombreContacto
import cl.duoc.agenciaconnect.util.validarOrganizacionContacto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Con este ViewModel gestionamos los datos y las operaciones del módulo de contactos.
class ContactosViewModel(application: Application) : AndroidViewModel(application) {

    // Con estas dependencias accedemos a los contactos y las organizaciones registradas.
    private val database = AgenciaDatabase.obtenerInstancia(application)
    private val contactoRepository = ContactoRepository(database.contactoDao())
    private val organizacionDao: OrganizacionDao = database.organizacionDao()

    // Con este estado mantenemos sincronizada la información de la pantalla.
    private val _uiState = MutableStateFlow(ContactoUiState())
    val uiState: StateFlow<ContactoUiState> = _uiState.asStateFlow()

    init {
        // Con este bloque observamos los contactos y organizaciones guardados.
        viewModelScope.launch {
            contactoRepository.contactos.collect { contactos ->
                _uiState.update { it.copy(contactos = contactos) }
            }
        }

        viewModelScope.launch {
            organizacionDao.obtenerOrganizaciones().collect { organizaciones ->
                _uiState.update { it.copy(organizaciones = organizaciones) }
            }
        }
    }

    // Con estas funciones actualizamos los campos del formulario.
    fun actualizarNombreCompleto(valor: String) {
        _uiState.update { it.copy(nombreCompleto = valor, errorNombreCompleto = false) }
    }

    fun actualizarOrganizacion(organizacionId: Long?) {
        _uiState.update { it.copy(organizacionId = organizacionId, errorOrganizacion = false) }
    }

    fun actualizarCargo(valor: String) {
        _uiState.update { it.copy(cargo = valor, errorCargo = false) }
    }

    fun actualizarArea(valor: String) {
        _uiState.update { it.copy(area = valor, errorArea = false) }
    }

    fun actualizarCorreo(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = false) }
    }

    // Con esta función validamos y formateamos el teléfono chileno automáticamente (+56 obligatorio)
    fun actualizarTelefono(valor: String) {
        // Aseguramos que comience con "+56"
        val textoLimpio = if (!valor.startsWith("+56")) {
            "+56" + valor.replace("+", "").replace("56", "")
        } else {
            valor
        }

        // Permitimos el "+56" al inicio y solo dígitos numéricos en el resto
        val soloDigitosDespues = textoLimpio.substring(3).filter { it.isDigit() }
        val telefonoFinal = "+56" + soloDigitosDespues

        // Como el teléfono es opcional, es válido si está vacío ("+56") o si completa los 9 dígitos de número (+56 + 9 dígitos = 12 caracteres)
        val esValido = telefonoFinal.length == 3 || telefonoFinal.length == 12

        _uiState.update {
            it.copy(
                telefono = telefonoFinal,
                errorTelefono = !esValido
            )
        }
    }

    fun actualizarMedioPreferido(valor: String) {
        _uiState.update { it.copy(medioPreferido = valor, errorMedioPreferido = false) }
    }

    fun actualizarObservaciones(valor: String) {
        _uiState.update { it.copy(observaciones = valor) }
    }

    // Con esta función preparamos el formulario para registrar un contacto nuevo.
    fun nuevoContacto() {
        _uiState.update {
            ContactoUiState(
                contactos = it.contactos,
                organizaciones = it.organizaciones,
                telefono = "+56" // Precargamos "+56" por defecto al crear uno nuevo
            )
        }
    }

    // Con esta función cargamos los datos de un contacto para editarlo.
    fun editarContacto(contacto: Contacto) {
        val telefonoCargado = if (contacto.telefono.isBlank() || !contacto.telefono.startsWith("+56")) {
            "+56" + contacto.telefono.replace("+", "")
        } else {
            contacto.telefono
        }

        _uiState.update {
            it.copy(
                nombreCompleto = contacto.nombreCompleto,
                organizacionId = contacto.organizacionId,
                cargo = contacto.cargo,
                area = contacto.area,
                correo = contacto.correo,
                telefono = telefonoCargado,
                medioPreferido = contacto.medioPreferido,
                observaciones = contacto.observaciones,
                idEnEdicion = contacto.id,
                contactoPorEliminar = null,
                mensaje = null,
                errorTelefono = false
            )
        }
    }

    // Con esta función cancelamos la edición y limpiamos el formulario.
    fun cancelarEdicion() {
        nuevoContacto()
    }

    // Con esta función validamos y guardamos un contacto nuevo o sus modificaciones.
    fun guardarContacto() {
        val estado = _uiState.value

        val nombreValido = validarNombreContacto(estado.nombreCompleto)
        val organizacionValida = validarOrganizacionContacto(estado.organizacionId)
        val cargoValido = validarCargoContacto(estado.cargo)
        val areaValida = validarAreaContacto(estado.area)
        val correoValido = validarCorreoContacto(estado.correo)
        val medioValido = validarMedioPreferidoContacto(estado.medioPreferido)

        // El teléfono es válido si está vacío (solo "+56") o si tiene el formato completo de 12 caracteres
        val telefonoValido = estado.telefono == "+56" || estado.telefono.length == 12

        _uiState.update {
            it.copy(
                errorNombreCompleto = !nombreValido,
                errorOrganizacion = !organizacionValida,
                errorCargo = !cargoValido,
                errorArea = !areaValida,
                errorCorreo = !correoValido,
                errorMedioPreferido = !medioValido,
                errorTelefono = !telefonoValido,
                mensaje = null
            )
        }

        if (!nombreValido || !organizacionValida || !cargoValido ||
            !areaValida || !correoValido || !medioValido || !telefonoValido
        ) {
            _uiState.update { it.copy(mensaje = "Revisa los campos obligatorios y el formato del teléfono.") }
            return
        }

        val organizacionExiste = estado.organizaciones.any {
            it.id == estado.organizacionId
        }

        if (!organizacionExiste) {
            _uiState.update {
                it.copy(
                    errorOrganizacion = true,
                    mensaje = "Selecciona una organización registrada."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensaje = null) }

            try {
                // Si el usuario dejó solo el "+56", guardamos el campo vacío para mantenerlo opcional limpio en la BD
                val telefonoAGuardar = if (estado.telefono == "+56") "" else estado.telefono.trim()

                val contacto = Contacto(
                    id = estado.idEnEdicion ?: 0L,
                    nombreCompleto = estado.nombreCompleto.trim(),
                    organizacionId = estado.organizacionId!!,
                    cargo = estado.cargo.trim(),
                    area = estado.area.trim(),
                    correo = estado.correo.trim(),
                    telefono = telefonoAGuardar,
                    medioPreferido = estado.medioPreferido.trim(),
                    observaciones = estado.observaciones.trim()
                )

                if (estado.idEnEdicion == null) {
                    contactoRepository.insertarContacto(contacto)
                    _uiState.update { it.copy(mensaje = "Contacto registrado correctamente.") }
                } else {
                    contactoRepository.actualizarContacto(contacto)
                    _uiState.update { it.copy(mensaje = "Contacto actualizado correctamente.") }
                }

                val mensajeExito = _uiState.value.mensaje
                nuevoContacto()
                _uiState.update { it.copy(mensaje = mensajeExito) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(mensaje = "No se pudo guardar el contacto. Inténtalo nuevamente.")
                }
            } finally {
                _uiState.update { it.copy(guardando = false) }
            }
        }
    }

    // Con esta función preparamos la confirmación antes de eliminar un contacto.
    fun solicitarEliminarContacto(contacto: Contacto) {
        _uiState.update { it.copy(contactoPorEliminar = contacto) }
    }

    // Con esta función eliminamos el contacto confirmado por el usuario.
    fun confirmarEliminarContacto() {
        val contacto = _uiState.value.contactoPorEliminar ?: return

        viewModelScope.launch {
            try {
                contactoRepository.eliminarContacto(contacto)
                _uiState.update {
                    it.copy(
                        contactoPorEliminar = null,
                        mensaje = "Contacto eliminado correctamente."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        contactoPorEliminar = null,
                        mensaje = "No se pudo eliminar el contacto."
                    )
                }
            }
        }
    }

    // Con esta función cerramos el diálogo de eliminación sin borrar el contacto.
    fun cancelarEliminarContacto() {
        _uiState.update { it.copy(contactoPorEliminar = null) }
    }

    // Con esta función limpiamos los mensajes y errores al salir del formulario.
    fun limpiarMensajes() {
        _uiState.update {
            it.copy(
                mensaje = null,
                errorNombreCompleto = false,
                errorOrganizacion = false,
                errorCargo = false,
                errorArea = false,
                errorCorreo = false,
                errorMedioPreferido = false,
                errorTelefono = false
            )
        }
    }

    // Con esta función limpiamos los campos y restablecemos el teléfono con "+56".
    private fun limpiarFormulario() {
        _uiState.update {
            it.copy(
                nombreCompleto = "",
                organizacionId = null,
                cargo = "",
                area = "",
                correo = "",
                telefono = "+56",
                medioPreferido = "",
                observaciones = "",
                errorNombreCompleto = false,
                errorOrganizacion = false,
                errorCargo = false,
                errorArea = false,
                errorCorreo = false,
                errorMedioPreferido = false,
                errorTelefono = false,
                idEnEdicion = null
            )
        }
    }

    // Con esta función reiniciamos el formulario al abandonarlo.
    fun reiniciarFormularioAlSalir() {
        limpiarFormulario()

        _uiState.update {
            it.copy(mensaje = null)
        }
    }
}