package cl.duoc.agenciaconnect.data.model

// Con estos enums tipamos los atributos de las oportunidades comerciales.

enum class TipoOportunidad(val etiqueta: String) {
    EVENTO_CORPORATIVO("Evento corporativo"),
    FERIA_EMPRESARIAL("Feria empresarial"),
    ACTIVIDAD_MUNICIPAL("Actividad municipal"),
    JORNADA_BENEFICIOS("Jornada de beneficios"),
    FERIA_INSTITUCIONAL("Feria institucional"),
    ACTIVIDAD_TRABAJADORES("Actividad de trabajadores"),
    ACTIVACION_COMERCIAL("Activación comercial"),
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
    ALTA("Alta"),
    MEDIA("Media"),
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
    ALTO("Alto"),
    MEDIO("Medio"),
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
    EVENTO_IDENTIFICADO("Evento identificado"),
    CONTACTO_PENDIENTE("Contacto pendiente"),
    CONTACTO_REALIZADO("Contacto realizado"),
    PROPUESTA_ENVIADA("Propuesta enviada"),
    EN_ESPERA_RESPUESTA("En espera de respuesta"),
    PARTICIPACION_CONFIRMADA("Participación confirmada"),
    OPORTUNIDAD_DESCARTADA("Oportunidad descartada");

    companion object {
        fun deEtiqueta(valor: String): EstadoOportunidad {
            val v = valor.trim()
            return entries.find { it.etiqueta.equals(v, ignoreCase = true) || it.name.equals(v, ignoreCase = true) }
                ?: EVENTO_IDENTIFICADO
        }
    }
}
