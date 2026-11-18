package cl.duoc.agenciaconnect.data.remote

import cl.duoc.agenciaconnect.data.model.Contacto
import cl.duoc.agenciaconnect.data.model.Evento
import cl.duoc.agenciaconnect.data.model.Oportunidad
import cl.duoc.agenciaconnect.data.model.Organizacion
import cl.duoc.agenciaconnect.data.model.Seguimiento
import cl.duoc.agenciaconnect.data.model.Usuario
import retrofit2.Response
import retrofit2.http.*

interface AgenciaApi {

    // ── OPORTUNIDADES ──
    @GET("oportunidades")
    suspend fun obtenerOportunidades(): List<Oportunidad>

    @POST("oportunidades")
    suspend fun crearOportunidad(@Body oportunidad: Oportunidad): Response<Oportunidad>

    @PUT("oportunidades/{id}")
    suspend fun actualizarOportunidad(@Path("id") id: Long, @Body oportunidad: Oportunidad): Response<Oportunidad>

    @DELETE("oportunidades/{id}")
    suspend fun eliminarOportunidad(@Path("id") id: Long): Response<Void>

    // ── ORGANIZACIONES ──
    @GET("organizaciones")
    suspend fun obtenerOrganizaciones(): List<Organizacion>

    @POST("organizaciones")
    suspend fun crearOrganizacion(@Body organizacion: Organizacion): Response<Organizacion>

    @PUT("organizaciones/{id}")
    suspend fun actualizarOrganizacion(@Path("id") id: Long, @Body organizacion: Organizacion): Response<Organizacion>

    @DELETE("organizaciones/{id}")
    suspend fun eliminarOrganizacion(@Path("id") id: Long): Response<Void>

    // ── EVENTOS ──
    @GET("eventos")
    suspend fun obtenerEventos(): List<Evento>

    @POST("eventos")
    suspend fun crearEvento(@Body evento: Evento): Response<Evento>

    @PUT("eventos/{id}")
    suspend fun actualizarEvento(@Path("id") id: Long, @Body evento: Evento): Response<Evento>

    @DELETE("eventos/{id}")
    suspend fun eliminarEvento(@Path("id") id: Long): Response<Void>

    // ── CONTACTOS ──
    @GET("contactos")
    suspend fun obtenerContactos(): List<Contacto>

    @POST("contactos")
    suspend fun crearContacto(@Body contacto: Contacto): Response<Contacto>

    @PUT("contactos/{id}")
    suspend fun actualizarContacto(@Path("id") id: Long, @Body contacto: Contacto): Response<Contacto>

    @DELETE("contactos/{id}")
    suspend fun eliminarContacto(@Path("id") id: Long): Response<Void>

    // ── USUARIOS ──
    @GET("usuarios")
    suspend fun obtenerUsuarios(): List<Usuario>

    @POST("usuarios")
    suspend fun crearUsuario(@Body usuario: Usuario): Response<Usuario>

    @PUT("usuarios/{id}")
    suspend fun actualizarUsuario(@Path("id") id: Long, @Body usuario: Usuario): Response<Usuario>

    @DELETE("usuarios/{id}")
    suspend fun eliminarUsuario(@Path("id") id: Long): Response<Void>

    // ── SEGUIMIENTOS ──
    @GET("seguimientos")
    suspend fun obtenerSeguimientos(): List<Seguimiento>

    @POST("seguimientos")
    suspend fun crearSeguimiento(@Body seguimiento: Seguimiento): Response<Seguimiento>

    @PUT("seguimientos/{id}")
    suspend fun actualizarSeguimiento(@Path("id") id: Long, @Body seguimiento: Seguimiento): Response<Seguimiento>

    @DELETE("seguimientos/{id}")
    suspend fun eliminarSeguimiento(@Path("id") id: Long): Response<Void>
}
