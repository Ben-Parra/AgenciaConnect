package cl.duoc.agenciaconnect.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // La IP mágica del emulador de Android para apuntar al localhost de tu PC (Spring Boot)
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}