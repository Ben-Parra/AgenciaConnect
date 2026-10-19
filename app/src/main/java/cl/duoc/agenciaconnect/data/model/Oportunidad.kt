package cl.duoc.agenciaconnect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Con esta entidad definimos los datos de cada oportunidad comercial que guardaremos en Room.
@Entity(tableName = "oportunidades")
data class Oportunidad(

    // Con este identificador distinguimos de forma única cada oportunidad registrada.
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Con este campo registramos el título o nombre descriptivo de la oportunidad.
    val nombre: String,

    // Con este campo clasificamos el tipo de oportunidad (feria, actividad municipal, etc.).
    val tipo: String,

    // Con este identificador vinculamos la oportunidad con una organización registrada.
    val organizacionId: Long,

    // Con este identificador opcional vinculamos la oportunidad a un evento registrado (0 si no aplica).
    val eventoId: Long = 0L,

    // Con estos campos definimos la urgencia y el impacto comercial esperado.
    val prioridad: String,
    val potencial: String,

    // Con este campo controlamos la etapa actual del seguimiento comercial.
    val estado: String,

    // Con este campo asignamos al colaborador del equipo comercial a cargo.
    val responsable: String,

    // Con este campo registramos notas o detalles adicionales de la gestión.
    val observaciones: String,

    val fechaDeteccion: String = ""
)