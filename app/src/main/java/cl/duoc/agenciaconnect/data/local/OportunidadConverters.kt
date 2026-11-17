package cl.duoc.agenciaconnect.data.local

import androidx.room.TypeConverter
import cl.duoc.agenciaconnect.data.model.EstadoOportunidad
import cl.duoc.agenciaconnect.data.model.PotencialOportunidad
import cl.duoc.agenciaconnect.data.model.PrioridadOportunidad
import cl.duoc.agenciaconnect.data.model.TipoOportunidad

// Con esta clase proveemos los convertidores de tipo para que Room persista los Enums de Oportunidad.
class OportunidadConverters {

    @TypeConverter
    fun fromTipoOportunidad(tipo: TipoOportunidad?): String {
        return tipo?.etiqueta ?: TipoOportunidad.OTRA_INSTANCIA.etiqueta
    }

    @TypeConverter
    fun toTipoOportunidad(valor: String?): TipoOportunidad {
        return TipoOportunidad.deEtiqueta(valor ?: "")
    }

    @TypeConverter
    fun fromPrioridadOportunidad(prioridad: PrioridadOportunidad?): String {
        return prioridad?.etiqueta ?: PrioridadOportunidad.MEDIA.etiqueta
    }

    @TypeConverter
    fun toPrioridadOportunidad(valor: String?): PrioridadOportunidad {
        return PrioridadOportunidad.deEtiqueta(valor ?: "")
    }

    @TypeConverter
    fun fromPotencialOportunidad(potencial: PotencialOportunidad?): String {
        return potencial?.etiqueta ?: PotencialOportunidad.MEDIO.etiqueta
    }

    @TypeConverter
    fun toPotencialOportunidad(valor: String?): PotencialOportunidad {
        return PotencialOportunidad.deEtiqueta(valor ?: "")
    }

    @TypeConverter
    fun fromEstadoOportunidad(estado: EstadoOportunidad?): String {
        return estado?.etiqueta ?: EstadoOportunidad.EVENTO_IDENTIFICADO.etiqueta
    }

    @TypeConverter
    fun toEstadoOportunidad(valor: String?): EstadoOportunidad {
        return EstadoOportunidad.deEtiqueta(valor ?: "")
    }
}
