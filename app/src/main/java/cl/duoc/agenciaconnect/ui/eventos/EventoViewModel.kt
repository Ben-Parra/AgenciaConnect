
package cl.duoc.agenciaconnect.ui.eventos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaconnect.data.local.AgenciaDatabase
import cl.duoc.agenciaconnect.data.model.Evento
import cl.duoc.agenciaconnect.data.repository.EventoRepository
import cl.duoc.agenciaconnect.util.validarDescripcionEvento
import cl.duoc.agenciaconnect.util.validarFechaEvento
import cl.duoc.agenciaconnect.util.validarNombreEvento
import cl.duoc.agenciaconnect.util.validarUbicacionEvento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Con esta clase coordinamos el formulario, las validaciones,
// las organizaciones disponibles y las operaciones CRUD con Room.
class EventoViewModel(application: Application) : AndroidViewModel(application) {

    // Con esta propiedad reutilizamos la instancia de nuestra base de datos.
    private val baseDatos = AgenciaDatabase.obtenerInstancia(application)

    // Con estos repositorios y DAO accedemos a los eventos y organizaciones.
    private val repositorio = EventoRepository(baseDatos.eventoDao())
    private val organizacionDao = baseDatos.organizacionDao()

    // Con este estado privado administramos los cambios internos.
    private val _uiState = MutableStateFlow(EventoUiState())

    // Con este estado público permitimos que la interfaz observe los cambios.
    val uiState: StateFlow<EventoUiState> = _uiState.asStateFlow()

    init {
        // Con esta corrutina observamos los eventos guardados en Room.
        viewModelScope.launch {
            repositorio.eventos.collect { eventosGuardados ->
                _uiState.update {
                    it.copy(eventos = eventosGuardados)
                }
            }
        }

        // Con esta corrutina cargamos las organizaciones registradas
        // para ofrecerlas como opciones en el formulario de eventos.
        viewModelScope.launch {
            organizacionDao.obtenerOrganizaciones().collect { organizacionesGuardadas ->
                _uiState.update {
                    it.copy(organizaciones = organizacionesGuardadas)
                }
            }
        }
    }

    // Con esta función actualizamos el nombre y limpiamos su error anterior.
    fun cambiarNombre(nuevoNombre: String) {
        _uiState.update {
            it.copy(nombre = nuevoNombre, errorNombre = false)
        }
    }

    // Con esta función actualizamos la descripción y limpiamos su error.
    fun cambiarDescripcion(nuevaDescripcion: String) {
        _uiState.update {
            it.copy(
                descripcion = nuevaDescripcion,
                errorDescripcion = false
            )
        }
    }

    // Con esta función conservamos solo los dígitos de la fecha
    // y limitamos su longitud a ocho caracteres.
    fun cambiarFecha(nuevaFecha: String) {
        val fechaLimpia = nuevaFecha
            .filter { it.isDigit() }
            .take(8)

        _uiState.update {
            it.copy(fecha = fechaLimpia, errorFecha = false)
        }
    }

    // Con esta función actualizamos la ubicación del evento.
    fun cambiarUbicacion(nuevaUbicacion: String) {
        _uiState.update {
            it.copy(
                ubicacion = nuevaUbicacion,
                errorUbicacion = false
            )
        }
    }

    // Con esta función guardamos el nombre de la organización elegida
    // desde las opciones disponibles en el formulario.
    fun cambiarOrganizacion(nuevaOrganizacion: String) {
        _uiState.update {
            it.copy(
                organizacion = nuevaOrganizacion,
                errorOrganizacion = false,
                mensaje = null
            )
        }
    }

    // Con esta función validamos los campos y comprobamos que la organización
    // seleccionada corresponda a una organización que siga registrada en Room.
    fun validarFormulario(): Boolean {
        val estado = _uiState.value

        val errorNombre = !validarNombreEvento(estado.nombre)
        val errorDescripcion = !validarDescripcionEvento(estado.descripcion)
        val errorFecha = !validarFechaEvento(estado.fecha)
        val errorUbicacion = !validarUbicacionEvento(estado.ubicacion)

        val organizacionExiste = estado.organizaciones.any {
            it.nombre == estado.organizacion
        }

        val errorOrganizacion = !organizacionExiste

        val hayErrores =
            errorNombre ||
                    errorDescripcion ||
                    errorFecha ||
                    errorUbicacion ||
                    errorOrganizacion

        // Con esta actualización mostramos los errores y explicamos
        // qué debemos corregir antes de guardar el evento.
        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorDescripcion = errorDescripcion,
                errorFecha = errorFecha,
                errorUbicacion = errorUbicacion,
                errorOrganizacion = errorOrganizacion,
                mensaje = when {
                    estado.organizaciones.isEmpty() ->
                        "Registra una organización antes de crear un evento."

                    hayErrores ->
                        "Completa los campos obligatorios y verifica la fecha."

                    else -> null
                }

            )
        }

        return !hayErrores
    }

    // Con esta función validamos los datos y registramos o actualizamos
    // el evento en Room solo si todos los campos son correctos.
    fun guardarEvento() {
        if (!validarFormulario()) return

        val estado = _uiState.value

        // Con este objeto reunimos los datos validados que guardaremos.
        val evento = Evento(
            id = estado.idEnEdicion ?: 0L,
            nombre = estado.nombre.trim(),
            descripcion = estado.descripcion.trim(),
            fecha = estado.fecha,
            ubicacion = estado.ubicacion.trim(),
            organizacion = estado.organizacion
        )

        // Con esta corrutina guardamos el evento y comunicamos el resultado.
        viewModelScope.launch {
            try {
                if (estado.idEnEdicion == null) {
                    repositorio.insertarEvento(evento)
                } else {
                    repositorio.actualizarEvento(evento)
                }

                // Con esta función limpiamos el formulario después de guardar.
                limpiarFormulario()

                // Con este mensaje confirmamos el resultado de la operación.
                _uiState.update {
                    it.copy(
                        mensaje = if (estado.idEnEdicion == null) {
                            "Evento registrado correctamente."
                        } else {
                            "Evento actualizado correctamente."
                        }
                    )
                }
            } catch (e: Exception) {
                // Con este mensaje informamos si ocurrió un problema al guardar.
                _uiState.update {
                    it.copy(
                        mensaje = "No se pudo guardar el evento. Inténtalo nuevamente."
                    )
                }
            }
        }
    }

    // Con esta función cargamos los datos de un evento para editarlo.
    fun cargarEventoParaEditar(evento: Evento) {
        _uiState.update {
            it.copy(
                nombre = evento.nombre,
                descripcion = evento.descripcion,
                fecha = evento.fecha,
                ubicacion = evento.ubicacion,
                organizacion = evento.organizacion,
                errorNombre = false,
                errorDescripcion = false,
                errorFecha = false,
                errorUbicacion = false,
                errorOrganizacion = false,
                mensaje = null,
                idEnEdicion = evento.id
            )
        }
    }

    // Con esta función cancelamos la edición y limpiamos el formulario.
    fun cancelarEdicion() {
        limpiarFormulario()
    }

    // Con esta función seleccionamos el evento que queremos eliminar
    // para que la interfaz pueda solicitar una confirmación.
    fun solicitarEliminar(evento: Evento) {
        _uiState.update {
            it.copy(eventoPorEliminar = evento)
        }
    }

    // Con esta función cerramos la confirmación sin eliminar el evento.
    fun cancelarEliminar() {
        _uiState.update {
            it.copy(eventoPorEliminar = null)
        }
    }

    // Con esta función eliminamos el evento confirmado y limpiamos
    // el formulario si estábamos editando ese mismo registro.
    fun confirmarEliminar() {
        val estado = _uiState.value
        val evento = estado.eventoPorEliminar ?: return
        val estabaEditando = estado.idEnEdicion == evento.id

        // Con esta corrutina eliminamos el registro de forma asíncrona.
        viewModelScope.launch {
            try {
                repositorio.eliminarEvento(evento)

                _uiState.update {
                    it.copy(
                        eventoPorEliminar = null,
                        nombre = if (estabaEditando) "" else it.nombre,
                        descripcion = if (estabaEditando) "" else it.descripcion,
                        fecha = if (estabaEditando) "" else it.fecha,
                        ubicacion = if (estabaEditando) "" else it.ubicacion,
                        organizacion = if (estabaEditando) "" else it.organizacion,
                        errorNombre = if (estabaEditando) false else it.errorNombre,
                        errorDescripcion = if (estabaEditando) false else it.errorDescripcion,
                        errorFecha = if (estabaEditando) false else it.errorFecha,
                        errorUbicacion = if (estabaEditando) false else it.errorUbicacion,
                        errorOrganizacion = if (estabaEditando) false else it.errorOrganizacion,
                        idEnEdicion = if (estabaEditando) null else it.idEnEdicion
                    )
                }
            } catch (e: Exception) {
                // Con este mensaje informamos si no fue posible eliminar el evento.
                _uiState.update {
                    it.copy(
                        eventoPorEliminar = null,
                        mensaje = "No se pudo eliminar el evento. Inténtalo nuevamente."
                    )
                }
            }
        }
    }

    // Con esta función limpiamos los campos, sus errores
    // y el identificador del evento que estábamos editando.
    private fun limpiarFormulario() {
        _uiState.update {
            it.copy(
                nombre = "",
                descripcion = "",
                fecha = "",
                ubicacion = "",
                organizacion = "",
                errorNombre = false,
                errorDescripcion = false,
                errorFecha = false,
                errorUbicacion = false,
                errorOrganizacion = false,
                idEnEdicion = null
            )
        }
    }


    // Con esta función reiniciamos el formulario al abandonar la pantalla,
    // limpiando los campos, los errores y el modo de edición sin borrar registros.
    fun reiniciarFormularioAlSalir() {
        limpiarFormulario()

        _uiState.update {
            it.copy(mensaje = null)
        }
    }


}
