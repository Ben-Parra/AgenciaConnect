package cl.duoc.agenciaconnect.data.local

import cl.duoc.agenciaconnect.data.model.EstadoOportunidad
import cl.duoc.agenciaconnect.data.model.PotencialOportunidad
import cl.duoc.agenciaconnect.data.model.PrioridadOportunidad
import cl.duoc.agenciaconnect.data.model.TipoOportunidad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias para OportunidadConverters y los enums con @SerializedName.
 * Verificamos conversión ida y vuelta (enum → String y String → enum) para todos
 * los valores de TipoOportunidad, PrioridadOportunidad, PotencialOportunidad
 * y EstadoOportunidad, incluyendo etiquetas legibles al estilo Spring Boot.
 */
class OportunidadConvertersTest {

    private lateinit var converters: OportunidadConverters

    @Before
    fun setUp() {
        // Organizamos: instanciamos los convertidores antes de cada test.
        converters = OportunidadConverters()
    }

    // ── TipoOportunidad ───────────────────────────────────────────────────

    @Test
    fun fromTipoOportunidad_feriaEmpresarial_retornaEtiqueta() {
        // Organizamos: enum conocido.
        val tipo = TipoOportunidad.FERIA_EMPRESARIAL
        // Actuamos.
        val resultado = converters.fromTipoOportunidad(tipo)
        // Afirmamos: la etiqueta coincide con la esperada por Spring Boot.
        assertEquals("Feria empresarial", resultado)
    }

    @Test
    fun toTipoOportunidad_etiquetaFeriaEmpresarial_retornaEnum() {
        // Organizamos: texto que llegaría del JSON.
        val etiqueta = "Feria empresarial"
        // Actuamos.
        val resultado = converters.toTipoOportunidad(etiqueta)
        // Afirmamos: recupera el enum correcto.
        assertEquals(TipoOportunidad.FERIA_EMPRESARIAL, resultado)
    }

    @Test
    fun tipoOportunidad_idaYVuelta_todosLosValores() {
        // Organizamos: todos los valores del enum.
        TipoOportunidad.entries.forEach { tipo ->
            // Actuamos: convertimos a String y de vuelta.
            val etiqueta = converters.fromTipoOportunidad(tipo)
            val recuperado = converters.toTipoOportunidad(etiqueta)
            // Afirmamos: round-trip sin pérdida.
            assertEquals("Fallo en round-trip para $tipo", tipo, recuperado)
        }
    }

    @Test
    fun toTipoOportunidad_etiquetaInvalida_retornaOtraInstancia() {
        // Organizamos: valor desconocido.
        val etiqueta = "Valor Inexistente"
        // Actuamos.
        val resultado = converters.toTipoOportunidad(etiqueta)
        // Afirmamos: fallback es OTRA_INSTANCIA.
        assertEquals(TipoOportunidad.OTRA_INSTANCIA, resultado)
    }

    @Test
    fun toTipoOportunidad_etiquetaNula_retornaOtraInstancia() {
        // Organizamos: valor null (posible en base de datos con columna nula).
        val resultado = converters.toTipoOportunidad(null)
        // Afirmamos: no lanza excepción y devuelve fallback.
        assertEquals(TipoOportunidad.OTRA_INSTANCIA, resultado)
    }

    @Test
    fun fromTipoOportunidad_null_retornaEtiquetaOtraInstancia() {
        // Organizamos: enum null (campo opcional no seleccionado).
        val resultado = converters.fromTipoOportunidad(null)
        // Afirmamos: usa fallback legible.
        assertEquals(TipoOportunidad.OTRA_INSTANCIA.etiqueta, resultado)
    }

    @Test
    fun toTipoOportunidad_etiquetaEventoCorporativo_retornaEnum() {
        assertEquals(TipoOportunidad.EVENTO_CORPORATIVO, converters.toTipoOportunidad("Evento corporativo"))
    }

    @Test
    fun toTipoOportunidad_etiquetaActividadMunicipal_retornaEnum() {
        assertEquals(TipoOportunidad.ACTIVIDAD_MUNICIPAL, converters.toTipoOportunidad("Actividad municipal"))
    }

    @Test
    fun toTipoOportunidad_etiquetaJornadaBeneficios_retornaEnum() {
        assertEquals(TipoOportunidad.JORNADA_BENEFICIOS, converters.toTipoOportunidad("Jornada de beneficios"))
    }

    @Test
    fun toTipoOportunidad_etiquetaActivacionComercial_retornaEnum() {
        assertEquals(TipoOportunidad.ACTIVACION_COMERCIAL, converters.toTipoOportunidad("Activación comercial"))
    }

    // ── PrioridadOportunidad ──────────────────────────────────────────────

    @Test
    fun fromPrioridadOportunidad_alta_retornaEtiqueta() {
        val resultado = converters.fromPrioridadOportunidad(PrioridadOportunidad.ALTA)
        assertEquals("Alta", resultado)
    }

    @Test
    fun toPrioridadOportunidad_etiquetaAlta_retornaEnum() {
        assertEquals(PrioridadOportunidad.ALTA, converters.toPrioridadOportunidad("Alta"))
    }

    @Test
    fun prioridadOportunidad_idaYVuelta_todosLosValores() {
        // Organizamos: todos los valores de prioridad.
        PrioridadOportunidad.entries.forEach { prioridad ->
            // Actuamos.
            val etiqueta = converters.fromPrioridadOportunidad(prioridad)
            val recuperado = converters.toPrioridadOportunidad(etiqueta)
            // Afirmamos.
            assertEquals("Fallo en $prioridad", prioridad, recuperado)
        }
    }

    @Test
    fun toPrioridadOportunidad_etiquetaInvalida_retornaMedia() {
        // Organizamos: texto desconocido.
        val resultado = converters.toPrioridadOportunidad("Maxima")
        // Afirmamos: fallback es MEDIA.
        assertEquals(PrioridadOportunidad.MEDIA, resultado)
    }

    @Test
    fun toPrioridadOportunidad_null_retornaMedia() {
        assertEquals(PrioridadOportunidad.MEDIA, converters.toPrioridadOportunidad(null))
    }

    @Test
    fun fromPrioridadOportunidad_null_retornaEtiquetaMedia() {
        assertEquals(PrioridadOportunidad.MEDIA.etiqueta, converters.fromPrioridadOportunidad(null))
    }

    @Test
    fun toPrioridadOportunidad_etiquetaBaja_retornaEnum() {
        assertEquals(PrioridadOportunidad.BAJA, converters.toPrioridadOportunidad("Baja"))
    }

    // ── PotencialOportunidad ──────────────────────────────────────────────

    @Test
    fun fromPotencialOportunidad_alto_retornaEtiqueta() {
        assertEquals("Alto", converters.fromPotencialOportunidad(PotencialOportunidad.ALTO))
    }

    @Test
    fun toPotencialOportunidad_etiquetaAlto_retornaEnum() {
        assertEquals(PotencialOportunidad.ALTO, converters.toPotencialOportunidad("Alto"))
    }

    @Test
    fun potencialOportunidad_idaYVuelta_todosLosValores() {
        // Organizamos: todos los potenciales.
        PotencialOportunidad.entries.forEach { potencial ->
            // Actuamos.
            val etiqueta = converters.fromPotencialOportunidad(potencial)
            val recuperado = converters.toPotencialOportunidad(etiqueta)
            // Afirmamos.
            assertEquals("Fallo en $potencial", potencial, recuperado)
        }
    }

    @Test
    fun toPotencialOportunidad_etiquetaInvalida_retornaMedio() {
        assertEquals(PotencialOportunidad.MEDIO, converters.toPotencialOportunidad("Enorme"))
    }

    @Test
    fun toPotencialOportunidad_null_retornaMedio() {
        assertEquals(PotencialOportunidad.MEDIO, converters.toPotencialOportunidad(null))
    }

    @Test
    fun fromPotencialOportunidad_null_retornaEtiquetaMedio() {
        assertEquals(PotencialOportunidad.MEDIO.etiqueta, converters.fromPotencialOportunidad(null))
    }

    // ── EstadoOportunidad ─────────────────────────────────────────────────

    @Test
    fun fromEstadoOportunidad_propuestaEnviada_retornaEtiqueta() {
        val resultado = converters.fromEstadoOportunidad(EstadoOportunidad.PROPUESTA_ENVIADA)
        assertEquals("Propuesta enviada", resultado)
    }

    @Test
    fun toEstadoOportunidad_etiquetaPropuestaEnviada_retornaEnum() {
        assertEquals(EstadoOportunidad.PROPUESTA_ENVIADA, converters.toEstadoOportunidad("Propuesta enviada"))
    }

    @Test
    fun estadoOportunidad_idaYVuelta_todosLosValores() {
        // Organizamos: todos los estados del flujo comercial.
        EstadoOportunidad.entries.forEach { estado ->
            // Actuamos.
            val etiqueta = converters.fromEstadoOportunidad(estado)
            val recuperado = converters.toEstadoOportunidad(etiqueta)
            // Afirmamos.
            assertEquals("Fallo en $estado", estado, recuperado)
        }
    }

    @Test
    fun toEstadoOportunidad_etiquetaInvalida_retornaEventoIdentificado() {
        // Organizamos: estado desconocido.
        val resultado = converters.toEstadoOportunidad("Estado Inexistente")
        // Afirmamos: fallback es EVENTO_IDENTIFICADO.
        assertEquals(EstadoOportunidad.EVENTO_IDENTIFICADO, resultado)
    }

    @Test
    fun toEstadoOportunidad_null_retornaEventoIdentificado() {
        assertEquals(EstadoOportunidad.EVENTO_IDENTIFICADO, converters.toEstadoOportunidad(null))
    }

    @Test
    fun fromEstadoOportunidad_null_retornaEtiquetaEventoIdentificado() {
        assertEquals(EstadoOportunidad.EVENTO_IDENTIFICADO.etiqueta, converters.fromEstadoOportunidad(null))
    }

    @Test
    fun toEstadoOportunidad_etiquetaContactoPendiente_retornaEnum() {
        assertEquals(EstadoOportunidad.CONTACTO_PENDIENTE, converters.toEstadoOportunidad("Contacto pendiente"))
    }

    @Test
    fun toEstadoOportunidad_etiquetaParticipacionConfirmada_retornaEnum() {
        assertEquals(EstadoOportunidad.PARTICIPACION_CONFIRMADA, converters.toEstadoOportunidad("Participación confirmada"))
    }

    @Test
    fun toEstadoOportunidad_etiquetaEnEsperaRespuesta_retornaEnum() {
        assertEquals(EstadoOportunidad.EN_ESPERA_RESPUESTA, converters.toEstadoOportunidad("En espera de respuesta"))
    }

    @Test
    fun toEstadoOportunidad_etiquetaOportunidadDescartada_retornaEnum() {
        assertEquals(EstadoOportunidad.OPORTUNIDAD_DESCARTADA, converters.toEstadoOportunidad("Oportunidad descartada"))
    }

    // ── Cobertura de deEtiqueta con nombre de constante en MAYÚSCULAS ─────

    @Test
    fun deEtiquetaTipo_nombreConstante_retornaEnum() {
        // Organizamos: nombre de constante en MAYÚSCULAS (alternativo en @SerializedName).
        val resultado = TipoOportunidad.deEtiqueta("FERIA_EMPRESARIAL")
        // Afirmamos: se resuelve correctamente.
        assertEquals(TipoOportunidad.FERIA_EMPRESARIAL, resultado)
    }

    @Test
    fun deEtiquetaPrioridad_nombreConstanteMayusculas_retornaEnum() {
        assertEquals(PrioridadOportunidad.ALTA, PrioridadOportunidad.deEtiqueta("ALTA"))
    }

    @Test
    fun deEtiquetaPotencial_nombreConstanteMayusculas_retornaEnum() {
        assertEquals(PotencialOportunidad.ALTO, PotencialOportunidad.deEtiqueta("ALTO"))
    }

    @Test
    fun deEtiquetaEstado_nombreConstanteMayusculas_retornaEnum() {
        assertEquals(EstadoOportunidad.PROPUESTA_ENVIADA, EstadoOportunidad.deEtiqueta("PROPUESTA_ENVIADA"))
    }

    @Test
    fun todosLosEnums_etiquetasNoNulas() {
        // Verificamos que ningún enum produzca una etiqueta null al serializar.
        TipoOportunidad.entries.forEach { assertNotNull(it.etiqueta) }
        PrioridadOportunidad.entries.forEach { assertNotNull(it.etiqueta) }
        PotencialOportunidad.entries.forEach { assertNotNull(it.etiqueta) }
        EstadoOportunidad.entries.forEach { assertNotNull(it.etiqueta) }
    }
}
