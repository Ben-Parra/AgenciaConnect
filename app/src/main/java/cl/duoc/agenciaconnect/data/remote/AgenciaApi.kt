package cl.duoc.agenciaconnect.data.remote

import cl.duoc.agenciaconnect.data.model.Oportunidad
import cl.duoc.agenciaconnect.data.model.Organizacion
import cl.duoc.agenciaconnect.data.model.Evento
import cl.duoc.agenciaconnect.data.model.Contacto
import cl.duoc.agenciaconnect.data.model.Usuario
import retrofit2.Response
import retrofit2.http.*

interface AgenciaApi {

    @GET("oportunidades")
    suspend fun obtenerOportunidades(): List<Oportunidad>

    @POST("oportunidades")
    suspend fun crearOportunidad(@Body oportunidad: Oportunidad): Response<Oportunidad>

    @PUT("oportunidades/{id}")
    suspend fun actualizarOportunidad(@Path("id") id: Long, @Body oportunidad: Oportunidad): Response<Oportunidad>

    @DELETE("oportunidades/{id}")
    suspend fun eliminarOportunidad(@Path("id") id: Long): Response<Void>

    @GET("organizaciones")
    suspend fun obtenerOrganizaciones(): List<Organizacion>

    @GET("eventos")
    suspend fun obtenerEventos(): List<Evento>

    @GET("contactos")
    suspend fun obtenerContactos(): List<Contacto>

    @GET("usuarios")
    suspend fun obtenerUsuarios(): List<Usuario>
}