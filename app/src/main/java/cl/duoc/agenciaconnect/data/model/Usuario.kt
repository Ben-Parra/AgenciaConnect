package cl.duoc.agenciaconnect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Con esta entidad representamos a los colaboradores y sus roles en el sistema comercial.
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    // Con este campo distinguimos si el usuario es SUPERVISOR o EJECUTIVO.
    val rol: String
)