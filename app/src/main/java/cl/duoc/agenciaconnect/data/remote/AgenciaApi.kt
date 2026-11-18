package cl.duoc.agenciaconnect.data.remote

import cl.duoc.agenciaconnect.data.model.Contacto
import cl.duoc.agenciaconnect.data.model.Evento
import cl.duoc.agenciaconnect.data.model.Oportunidad
import cl.duoc.agenciaconnect.data.model.Organizacion
import cl.duoc.agenciaconnect.data.model.Seguimiento
import cl.duoc.agenciaconnect.data.model.Usuario
import retrofit2.Response
import retrofit2.http.*

// Contrato de la API remota.
// Mapeamos las operaciones HTTP REST (@GET, @POST, @PUT, @DELETE) hacia el servidor Spring Boot.
interface AgenciaApi {

    // ── OPORTUNIDADES ──
    // Consultamos la lista completa de oportunidades desde el servidor.
    @GET("oportunidades")
    suspend fun obtenerOportunidades(): List<Oportunidad>

    // Enviamos una nueva oportunidad al backend para asignar el ID autogenerado en MySQL.
    @POST("oportunidades")
    suspend fun crearOportunidad(@Body oportunidad: Oportunidad): Response<Oportunidad>

    // Actualizamos una oportunidad existente en la base de datos remota mediante su ID.
    @PUT("oportunidades/{id}")
    suspend fun actualizarOportunidad(@Path("id") id: Long, @Body oportunidad: Oportunidad): Response<Oportunidad>

    // Eliminamos físicamente la oportunidad en el backend por su ID.
    @DELETE("oportunidades/{id}")
    suspend fun eliminarOportunidad(@Path("id") id: Long): Response<Void>

    // ── ORGANIZACIONES ──
    // Obtenemos la colección de organizaciones registradas en la API.
    @GET("organizaciones")
    suspend fun obtenerOrganizaciones(): List<Organizacion>

    // Registramos una nueva organización en la base de datos remota.
    @POST("organizaciones")
    suspend fun crearOrganizacion(@Body organizacion: Organizacion): Response<Organizacion>

    // Modificamos los datos de una organización existente por su ID.
    @PUT("organizaciones/{id}")
    suspend fun actualizarOrganizacion(@Path("id") id: Long, @Body organizacion: Organizacion): Response<Organizacion>

    // Eliminamos una organización por su identificador único.
    @DELETE("organizaciones/{id}")
    suspend fun eliminarOrganizacion(@Path("id") id: Long): Response<Void>

    // ── EVENTOS ──
    // Obtenemos todos los eventos corporativos sincronizados en el servidor.
    @GET("eventos")
    suspend fun obtenerEventos(): List<Evento>

    // Enviamos la entidad evento para persistencia remota.
    @POST("eventos")
    suspend fun crearEvento(@Body evento: Evento): Response<Evento>

    // Actualizamos la información de un evento seleccionado.
    @PUT("eventos/{id}")
    suspend fun actualizarEvento(@Path("id") id: Long, @Body evento: Evento): Response<Evento>

    // Eliminamos un evento registrado en la API.
    @DELETE("eventos/{id}")
    suspend fun eliminarEvento(@Path("id") id: Long): Response<Void>

    // ── CONTACTOS ──
    // Consultamos el listado de contactos registrados en la base de datos remota.
    @GET("contactos")
    suspend fun obtenerContactos(): List<Contacto>

    // Creamos un nuevo contacto comercial en el servidor.
    @POST("contactos")
    suspend fun crearContacto(@Body contacto: Contacto): Response<Contacto>

    // Actualizamos los datos de contacto según su ID.
    @PUT("contactos/{id}")
    suspend fun actualizarContacto(@Path("id") id: Long, @Body contacto: Contacto): Response<Contacto>

    // Eliminamos el registro de un contacto en el backend.
    @DELETE("contactos/{id}")
    suspend fun eliminarContacto(@Path("id") id: Long): Response<Void>

    // ── USUARIOS ──
    // Consultamos los usuarios del sistema para sincronización de credenciales y roles.
    @GET("usuarios")
    suspend fun obtenerUsuarios(): List<Usuario>

    // Registramos un nuevo colaborador en la base de datos remota.
    @POST("usuarios")
    suspend fun crearUsuario(@Body usuario: Usuario): Response<Usuario>

    // Actualizamos los datos de un usuario existente.
    @PUT("usuarios/{id}")
    suspend fun actualizarUsuario(@Path("id") id: Long, @Body usuario: Usuario): Response<Usuario>

    // Eliminamos un usuario del sistema.
    @DELETE("usuarios/{id}")
    suspend fun eliminarUsuario(@Path("id") id: Long): Response<Void>

    // ── SEGUIMIENTOS ──
    // Consultamos el historial global de gestiones comerciales.
    @GET("seguimientos")
    suspend fun obtenerSeguimientos(): List<Seguimiento>

    // Registramos una nueva acción de seguimiento asociada a una oportunidad.
    @POST("seguimientos")
    suspend fun crearSeguimiento(@Body seguimiento: Seguimiento): Response<Seguimiento>

    // Actualizamos los datos de una gestión de seguimiento.
    @PUT("seguimientos/{id}")
    suspend fun actualizarSeguimiento(@Path("id") id: Long, @Body seguimiento: Seguimiento): Response<Seguimiento>

    // Eliminamos un registro de seguimiento en la API.
    @DELETE("seguimientos/{id}")
    suspend fun eliminarSeguimiento(@Path("id") id: Long): Response<Void>
}
