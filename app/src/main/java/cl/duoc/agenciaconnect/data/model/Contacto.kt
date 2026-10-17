package cl.duoc.agenciaconnect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Con esta entidad definimos los datos de cada contacto que guardaremos en Room.
@Entity(tableName = "contactos")
data class Contacto(

    // Con este identificador distinguimos cada contacto y permitimos que Room lo genere.
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Con este campo identificamos a la persona de contacto.
    val nombreCompleto: String,

    // Con este identificador relacionamos el contacto con una organización registrada.
    val organizacionId: Long,

    // Con estos campos describimos el cargo y el área de trabajo del contacto.
    val cargo: String,
    val area: String,

    // Con estos campos registramos sus medios de contacto.
    val correo: String,
    val telefono: String,

    // Con este campo indicamos el medio de comunicación preferido.
    val medioPreferido: String,

    // Con este campo almacenamos observaciones adicionales.
    val observaciones: String
)
