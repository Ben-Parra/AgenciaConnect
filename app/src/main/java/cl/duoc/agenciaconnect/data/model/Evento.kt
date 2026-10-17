package cl.duoc.agenciaconnect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Con esta clase definimos los datos que necesitamos
// para registrar y administrar cada evento corporativo.
// Además, indicamos que Room guardará estos datos en una tabla.
@Entity(tableName = "eventos")
data class Evento(
    // Con esta propiedad identificamos de manera única cada evento.
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Con estas propiedades almacenamos la información del evento.
    val nombre: String,
    val descripcion: String,
    val fecha: String,
    val ubicacion: String,
    val organizacion: String,

    // Con esta propiedad guardamos la referencia a la imagen del evento.

    val imagenUri: String = ""
)
