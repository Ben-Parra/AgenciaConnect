package cl.duoc.agenciaconnect.ui.organizaciones

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaconnect.data.local.AgenciaDatabase
import cl.duoc.agenciaconnect.data.repository.OrganizacionRepository
import cl.duoc.agenciaconnect.data.model.Organizacion
import cl.duoc.agenciaconnect.util.validarNombreOrganizacion
import cl.duoc.agenciaconnect.util.validarTipoOrganizacion
import cl.duoc.agenciaconnect.util.validarRubroOrganizacion
import cl.duoc.agenciaconnect.util.validarComunaOrganizacion
import cl.duoc.agenciaconnect.util.validarRegionOrganizacion
import cl.duoc.agenciaconnect.util.validarPublicoAsociadoOrganizacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Con este ViewModel coordinamos el formulario, las validaciones y las operaciones de organizaciones.
class OrganizacionViewModel(application: Application) : AndroidViewModel(application) {

    // Con este repositorio conectamos el ViewModel con las operaciones de Room.
    private val repository = OrganizacionRepository(
        AgenciaDatabase.obtenerInstancia(application).organizacionDao()
    )

    // Con este estado privado actualizamos los datos que necesita la interfaz.
    private val _uiState = MutableStateFlow(OrganizacionUiState())

    // Con esta propiedad permitimos que la interfaz observe el estado sin modificarlo directamente.
    val uiState: StateFlow<OrganizacionUiState> = _uiState.asStateFlow()

    init {
        // Con esta observación mantenemos la lista sincronizada con los datos guardados en Room.
        viewModelScope.launch {
            repository.organizaciones.collect { organizaciones ->
                _uiState.value = _uiState.value.copy(
                    organizaciones = organizaciones
                )
            }
        }
    }

    // Con esta función actualizamos el nombre escrito en el formulario.
    fun cambiarNombre(valor: String) {
        _uiState.value = _uiState.value.copy(nombre = valor, errorNombre = false)
    }

    // Con esta función actualizamos el tipo de organización seleccionado.
    fun cambiarTipo(valor: String) {
        _uiState.value = _uiState.value.copy(tipo = valor, errorTipo = false)
    }

    // Con esta función actualizamos el rubro de la organización.
    fun cambiarRubro(valor: String) {
        _uiState.value = _uiState.value.copy(rubro = valor, errorRubro = false)
    }

    // Con esta función actualizamos la comuna ingresada.
    fun cambiarComuna(valor: String) {
        _uiState.value = _uiState.value.copy(comuna = valor, errorComuna = false)
    }

    // Con esta función actualizamos la región ingresada.
    fun cambiarRegion(valor: String) {
        _uiState.value = _uiState.value.copy(region = valor, errorRegion = false)
    }

    // Con esta función actualizamos el sitio web opcional.
    fun cambiarSitioWeb(valor: String) {
        _uiState.value = _uiState.value.copy(sitioWeb = valor)
    }

    // Con esta función actualizamos el público asociado.
    fun cambiarPublicoAsociado(valor: String) {
        _uiState.value = _uiState.value.copy(
            publicoAsociado = valor,
            errorPublicoAsociado = false
        )
    }

    // Con esta función actualizamos las observaciones opcionales.
    fun cambiarObservaciones(valor: String) {
        _uiState.value = _uiState.value.copy(observaciones = valor)
    }

    // Con esta función validamos el formulario y guardamos o actualizamos la organización.
    fun guardarOrganizacion() {
        val estado = _uiState.value

        val nombreValido = validarNombreOrganizacion(estado.nombre)
        val tipoValido = validarTipoOrganizacion(estado.tipo)
        val rubroValido = validarRubroOrganizacion(estado.rubro)
        val comunaValida = validarComunaOrganizacion(estado.comuna)
        val regionValida = validarRegionOrganizacion(estado.region)
        val publicoValido = validarPublicoAsociadoOrganizacion(estado.publicoAsociado)

        _uiState.value = estado.copy(
            errorNombre = !nombreValido,
            errorTipo = !tipoValido,
            errorRubro = !rubroValido,
            errorComuna = !comunaValida,
            errorRegion = !regionValida,
            errorPublicoAsociado = !publicoValido,
            mensaje = null
        )

        if (!nombreValido || !tipoValido || !rubroValido ||
            !comunaValida || !regionValida || !publicoValido
        ) {
            _uiState.value = _uiState.value.copy(
                mensaje = "Completa los campos obligatorios."
            )
            return
        }

        // Con este objeto reunimos los valores validados antes de guardarlos en Room.
        val organizacion = Organizacion(
            id = estado.idEnEdicion ?: 0L,
            nombre = estado.nombre.trim(),
            tipo = estado.tipo.trim(),
            rubro = estado.rubro.trim(),
            comuna = estado.comuna.trim(),
            region = estado.region.trim(),
            sitioWeb = estado.sitioWeb.trim(),
            publicoAsociado = estado.publicoAsociado.trim(),
            observaciones = estado.observaciones.trim()
        )

        // Con esta corrutina ejecutamos la operación de base de datos fuera del hilo principal.
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true)

            try {
                if (estado.idEnEdicion == null) {
                    repository.insertarOrganizacion(organizacion)
                    _uiState.value = _uiState.value.copy(
                        mensaje = "Organización registrada correctamente."
                    )
                } else {
                    repository.actualizarOrganizacion(organizacion)
                    _uiState.value = _uiState.value.copy(
                        mensaje = "Organización actualizada correctamente."
                    )
                }

                limpiarFormulario()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    mensaje = "No se pudo guardar la organización. Inténtalo nuevamente."
                )
            } finally {
                _uiState.value = _uiState.value.copy(guardando = false)
            }
        }
    }

    // Con esta función cargamos una organización para modificar sus datos.
    fun editarOrganizacion(organizacion: Organizacion) {
        _uiState.value = _uiState.value.copy(
            nombre = organizacion.nombre,
            tipo = organizacion.tipo,
            rubro = organizacion.rubro,
            comuna = organizacion.comuna,
            region = organizacion.region,
            sitioWeb = organizacion.sitioWeb,
            publicoAsociado = organizacion.publicoAsociado,
            observaciones = organizacion.observaciones,
            idEnEdicion = organizacion.id,
            errorNombre = false,
            errorTipo = false,
            errorRubro = false,
            errorComuna = false,
            errorRegion = false,
            errorPublicoAsociado = false,
            mensaje = null
        )
    }

    // Con esta función cancelamos la edición y dejamos el formulario vacío.
    fun cancelarEdicion() {
        limpiarFormulario()
    }

    // Con esta función solicitamos confirmación antes de eliminar una organización.
    fun solicitarEliminacion(organizacion: Organizacion) {
        _uiState.value = _uiState.value.copy(
            organizacionPorEliminar = organizacion,
            mensaje = null
        )
    }

    // Con esta función cerramos el diálogo sin eliminar la organización.
    fun cancelarEliminacion() {
        _uiState.value = _uiState.value.copy(
            organizacionPorEliminar = null
        )
    }

    // Con esta función eliminamos únicamente la organización confirmada por el usuario.
    fun confirmarEliminacion() {
        val organizacion = _uiState.value.organizacionPorEliminar ?: return

        viewModelScope.launch {
            try {
                repository.eliminarOrganizacion(organizacion)
                _uiState.value = _uiState.value.copy(
                    organizacionPorEliminar = null,
                    mensaje = "Organización eliminada correctamente."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    organizacionPorEliminar = null,
                    mensaje = "No se pudo eliminar la organización."
                )
            }
        }
    }

    // Con esta función reiniciamos el formulario y salimos del modo de edición.
    fun limpiarFormulario() {
        _uiState.value = _uiState.value.copy(
            nombre = "",
            tipo = "",
            rubro = "",
            comuna = "",
            region = "",
            sitioWeb = "",
            publicoAsociado = "",
            observaciones = "",
            errorNombre = false,
            errorTipo = false,
            errorRubro = false,
            errorComuna = false,
            errorRegion = false,
            errorPublicoAsociado = false,
            idEnEdicion = null
        )
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
