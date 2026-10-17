package cl.duoc.agenciaconnect.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Con esta entidad registramos cada gestión comercial asociada a una oportunidad.
@Entity(
    tableName = "seguimientos",
    foreignKeys = [
        ForeignKey(
            entity = Oportunidad::class,
            parentColumns = ["id"],
            childColumns = ["oportunidadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["oportunidadId"])]
)
data class Seguimiento(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val oportunidadId: Long,
    val usuarioNombre: String,
    val tipoAccion: String, // Llamada, Envío de propuesta, Reunión, etc.
    val fechaHora: String,
    val resultado: String,
    val proximaAccion: String,
    val observaciones: String
)