package cl.duoc.agenciaconnect.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// ────────────────────────────────────────────────────────────────────────────
// COLORES SEMÁNTICOS CENTRALIZADOS
// ────────────────────────────────────────────────────────────────────────────
// Con estos valores centralizamos los colores de los estados y prioridades
// de las oportunidades para evitar hexadecimal sueltos en las pantallas.
// Cualquier cambio visual de la marca se realiza únicamente desde aquí.

// Prioridad comercial
val PrioridadAlta = Color(0xFFD32F2F)   // rojo intenso para prioridad alta
val PrioridadMedia = Color(0xFFF57C00) // ámbar para prioridad media
val PrioridadBaja = Color(0xFF388E3C)  // verde para prioridad baja

// Estado de la gestión comercial
val EstadoConfirmado = Color(0xFF2E7D32) // verde para participación confirmada
val EstadoDescartado = Color(0xFF9E9E9E) // gris para oportunidad descartada
val EstadoEnEspera = Color(0xFF1565C0)    // azul para en espera de respuesta

// Potencial comercial (mismo patrón semántico que la prioridad)
val PotencialAlto = Color(0xFF2E7D32)
val PotencialMedio = Color(0xFFF57C00)
val PotencialBajo = Color(0xFF9E9E9E)