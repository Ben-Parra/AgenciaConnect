package cl.duoc.agenciaconnect.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Configuración del cliente singleton de Retrofit.
// Definimos la conexión hacia el backend de Spring Boot mediante la IP loopback del emulador.
object RetrofitClient {
    // Definimos la dirección base para consultar el microservicio local desde el emulador de Android (10.0.2.2)
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Inicializamos mediante instanciación perezosa (lazy) la configuración de Retrofit con convertidor Gson.
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
