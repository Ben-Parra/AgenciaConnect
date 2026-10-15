package cl.duoc.agenciaconnect.data.model

// Con esta clase definimos los datos que necesitamos
// para registrar y administrar cada evento corporativo.
data class Evento(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val fecha: String,
    val ubicacion: String,
    val organizacion: String
)
