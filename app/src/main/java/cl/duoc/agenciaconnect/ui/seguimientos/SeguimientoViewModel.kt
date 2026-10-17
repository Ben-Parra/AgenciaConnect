package cl.duoc.agenciaconnect.ui.seguimientos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaconnect.data.local.AgenciaDatabase
import cl.duoc.agenciaconnect.data.model.Seguimiento
import cl.duoc.agenciaconnect.data.repository.SeguimientoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Con este estado representamos los datos del formulario de gestiones comerciales y el historial.
data class SeguimientoUiState(
    val tipoAccion: String = "Primer contacto",
    val resultado: String = "",
    val proximaAccion: String = "",
    val observaciones: String = "",
    val errorResultado: Boolean = false,
    val errorProximaAccion: Boolean = false,
    val seguimientos: List<Seguimiento> = emptyList(),
    val guardando: Boolean = false,
    val mensaje: String? = null
)

// Con este ViewModel coordinamos la bitácora de acciones comerciales realizadas sobre una oportunidad.
class SeguimientoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SeguimientoRepository(
        AgenciaDatabase.obtenerInstancia(application).seguimientoDao()
    )

    private val _uiState = MutableStateFlow(SeguimientoUiState())
    val uiState: StateFlow<SeguimientoUiState> = _uiState.asStateFlow()

    // Con esta función observamos las acciones registradas para una oportunidad específica.
    fun cargarSeguimientos(oportunidadId: Long) {
        viewModelScope.launch {
            repository.obtenerSeguimientosPorOportunidad(oportunidadId).collect { lista ->
                _uiState.update { it.copy(seguimientos = lista) }
            }
        }
    }

    fun cambiarTipoAccion(valor: String) {
        _uiState.update { it.copy(tipoAccion = valor) }
    }

    fun cambiarResultado(valor: String) {
        _uiState.update { it.copy(resultado = valor, errorResultado = false) }
    }

    fun cambiarProximaAccion(valor: String) {
        _uiState.update { it.copy(proximaAccion = valor, errorProximaAccion = false) }
    }

    fun cambiarObservaciones(valor: String) {
        _uiState.update { it.copy(observaciones = valor) }
    }

    // Con esta función guardamos la nueva gestión comercial realizada por el colaborador.
    fun guardarSeguimiento(oportunidadId: Long, usuarioNombre: String, onExito: () -> Unit) {
        val estado = _uiState.value

        val resultadoValido = estado.resultado.trim().isNotEmpty()
        val proximaValida = estado.proximaAccion.trim().isNotEmpty()

        if (!resultadoValido || !proximaValida) {
            _uiState.update {
                it.copy(
                    errorResultado = !resultadoValido,
                    errorProximaAccion = !proximaValida,
                    mensaje = "Completa los campos obligatorios del seguimiento."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensaje = null) }
            try {
                val fechaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                val seguimiento = Seguimiento(
                    oportunidadId = oportunidadId,
                    usuarioNombre = usuarioNombre,
                    tipoAccion = estado.tipoAccion,
                    fechaHora = fechaActual,
                    resultado = estado.resultado.trim(),
                    proximaAccion = estado.proximaAccion.trim(),
                    observaciones = estado.observaciones.trim()
                )

                repository.insertarSeguimiento(seguimiento)
                _uiState.update {
                    it.copy(
                        tipoAccion = "Llamada telefónica",
                        resultado = "",
                        proximaAccion = "",
                        observaciones = "",
                        guardando = false,
                        mensaje = "Gestión registrada correctamente."
                    )
                }
                onExito()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(guardando = false, mensaje = "No se pudo guardar el seguimiento.")
                }
            }
        }
    }
}