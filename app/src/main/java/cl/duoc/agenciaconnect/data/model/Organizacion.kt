package cl.duoc.agenciaconnect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Con esta entidad definimos la información de cada organización que guardaremos en Room.
@Entity(tableName = "organizaciones")
data class Organizacion(

    // Con este identificador distinguimos cada organización y permitimos que Room lo genere automáticamente.
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Con este campo identificamos el nombre de la empresa o institución.
    val nombre: String,

    // Con este campo clasificamos la organización, por ejemplo, empresa o municipalidad.
    val tipo: String,

    // Con este campo registramos el sector al que pertenece la organización.
    val rubro: String,

    // Con este campo indicamos la comuna donde se encuentra la organización.
    val comuna: String,

    // Con este campo registramos la región correspondiente.
    val region: String,

    // Con este campo guardamos el sitio web ficticio o público, si corresponde.
    val sitioWeb: String,

    // Con este campo describimos el público relacionado con la organización.
    val publicoAsociado: String,

    // Con este campo almacenamos observaciones adicionales.
    val observaciones: String
)
