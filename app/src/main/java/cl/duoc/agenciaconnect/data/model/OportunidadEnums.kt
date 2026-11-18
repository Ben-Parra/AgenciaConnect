package cl.duoc.agenciaconnect.data.model

import com.google.gson.annotations.SerializedName

// Con estos enums tipamos los atributos de las oportunidades comerciales.

enum class TipoOportunidad(val etiqueta: String) {
    @SerializedName(value = "Evento corporativo", alternate = ["EVENTO_CORPORATIVO"])
    EVENTO_CORPORATIVO("Evento corporativo"),

    @SerializedName(value = "Feria empresarial", alternate = ["FERIA_EMPRESARIAL"])
    FERIA_EMPRESARIAL("Feria empresarial"),

    @SerializedName(value = "Actividad municipal", alternate = ["ACTIVIDAD_MUNICIPAL"])
    ACTIVIDAD_MUNICIPAL("Actividad municipal"),

    @SerializedName(value = "Jornada de beneficios", alternate = ["JORNADA_BENEFICIOS"])
    JORNADA_BENEFICIOS("Jornada de beneficios"),

    @SerializedName(value = "Feria institucional", alternate = ["FERIA_INSTITUCIONAL"])
    FERIA_INSTITUCIONAL("Feria institucional"),

    @SerializedName(value = "Actividad de trabajadores", alternate = ["ACTIVIDAD_TRABAJADORES"])
    ACTIVIDAD_TRABAJADORES("Actividad de trabajadores"),

    @SerializedName(value = "Activación comercial", alternate = ["ACTIVACION_COMERCIAL", "Activacion comercial"])
    ACTIVACION_COMERCIAL("Activación comercial"),

    @SerializedName(value = "Otra instancia", alternate = ["OTRA_INSTANCIA"])
    OTRA_INSTANCIA("Otra instancia");

    companion object {
        fun deEtiqueta(valor: String): TipoOportunidad {
            val v = valor.trim()
            return entries.find { it.etiqueta.equals(v, ignoreCase = true) || it.name.equals(v, ignoreCase = true) }
                ?: OTRA_INSTANCIA
        }
    }
}

enum class PrioridadOportunidad(val etiqueta: String) {
    @SerializedName(value = "Alta", alternate = ["ALTA"])
    ALTA("Alta"),

    @SerializedName(value = "Media", alternate = ["MEDIA"])
    MEDIA("Media"),

    @SerializedName(value = "Baja", alternate = ["BAJA"])
    BAJA("Baja");

    companion object {
        fun deEtiqueta(valor: String): PrioridadOportunidad {
            val v = valor.trim()
            return entries.find { it.etiqueta.equals(v, ignoreCase = true) || it.name.equals(v, ignoreCase = true) }
                ?: MEDIA
        }
    }
}

enum class PotencialOportunidad(val etiqueta: String) {
    @SerializedName(value = "Alto", alternate = ["ALTO"])
    ALTO("Alto"),

    @SerializedName(value = "Medio", alternate = ["MEDIO"])
    MEDIO("Medio"),

    @SerializedName(value = "Bajo", alternate = ["BAJO"])
    BAJO("Bajo");

    companion object {
        fun deEtiqueta(valor: String): PotencialOportunidad {
            val v = valor.trim()
            return entries.find { it.etiqueta.equals(v, ignoreCase = true) || it.name.equals(v, ignoreCase = true) }
                ?: MEDIO
        }
    }
}

enum class EstadoOportunidad(val etiqueta: String) {
    @SerializedName(value = "Evento identificado", alternate = ["EVENTO_IDENTIFICADO"])
    EVENTO_IDENTIFICADO("Evento identificado"),

    @SerializedName(value = "Contacto pendiente", alternate = ["CONTACTO_PENDIENTE"])
    CONTACTO_PENDIENTE("Contacto pendiente"),

    @SerializedName(value = "Contacto realizado", alternate = ["CONTACTO_REALIZADO"])
    CONTACTO_REALIZADO("Contacto realizado"),

    @SerializedName(value = "Propuesta enviada", alternate = ["PROPUESTA_ENVIADA"])
    PROPUESTA_ENVIADA("Propuesta enviada"),

    @SerializedName(value = "En espera de respuesta", alternate = ["EN_ESPERA_RESPUESTA"])
    EN_ESPERA_RESPUESTA("En espera de respuesta"),

    @SerializedName(value = "Participación confirmada", alternate = ["PARTICIPACION_CONFIRMADA", "Participacion confirmada"])
    PARTICIPACION_CONFIRMADA("Participación confirmada"),

    @SerializedName(value = "Oportunidad descartada", alternate = ["OPORTUNIDAD_DESCARTADA"])
    OPORTUNIDAD_DESCARTADA("Oportunidad descartada");

    companion object {
        fun deEtiqueta(valor: String): EstadoOportunidad {
            val v = valor.trim()
            return entries.find { it.etiqueta.equals(v, ignoreCase = true) || it.name.equals(v, ignoreCase = true) }
                ?: EVENTO_IDENTIFICADO
        }
    }
}
