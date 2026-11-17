package cl.duoc.agenciaconnect.ui.theme

import androidx.compose.ui.graphics.Color

// ────────────────────────────────────────────────────────────────────────────
// PALETA CORPORATIVA BASE (INSPIRADA EN EL LOGO)
// ────────────────────────────────────────────────────────────────────────────

// --- MODO CLARO ---
val BluePrimaryLight = Color(0xFF1565C0)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFDCEEFE)
val OnPrimaryContainerLight = Color(0xFF001D36)

val OrangeSecondaryLight = Color(0xFFEF6C00)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFFFDCC3)
val OnSecondaryContainerLight = Color(0xFF301400)

val TealTertiaryLight = Color(0xFF00897B)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFB2EBF2)
val OnTertiaryContainerLight = Color(0xFF00201D)

val BackgroundLight = Color(0xFFF5F9FC)
val OnBackgroundLight = Color(0xFF191C1E)

val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF191C1E)

val SurfaceVariantLight = Color(0xFFEBF3FA)
val OnSurfaceVariantLight = Color(0xFF41474D)

// --- MODO OSCURO ---
val BluePrimaryDark = Color(0xFF90CAF9)
val OnPrimaryDark = Color(0xFF00325B)
val PrimaryContainerDark = Color(0xFF004881)
val OnPrimaryContainerDark = Color(0xFFDCEEFE)

val OrangeSecondaryDark = Color(0xFFFFB74D)
val OnSecondaryDark = Color(0xFF4B2500)
val SecondaryContainerDark = Color(0xFF6D3800)
val OnSecondaryContainerDark = Color(0xFFFFDCC3)

val TealTertiaryDark = Color(0xFF80CBC4)
val OnTertiaryDark = Color(0xFF003731)
val TertiaryContainerDark = Color(0xFF004F47)
val OnTertiaryContainerDark = Color(0xFFB2EBF2)

val BackgroundDark = Color(0xFF0F172A)
val OnBackgroundDark = Color(0xFFE2E8F0)

val SurfaceDark = Color(0xFF1E293B)
val OnSurfaceDark = Color(0xFFE2E8F0)

val SurfaceVariantDark = Color(0xFF334155)
val OnSurfaceVariantDark = Color(0xFFCBD5E1)

// ────────────────────────────────────────────────────────────────────────────
// COLORES SEMÁNTICOS CENTRALIZADOS
// ────────────────────────────────────────────────────────────────────────────
// Con estos valores centralizamos los colores de los estados y prioridades
// de las oportunidades para evitar hexadecimales sueltos en las pantallas.
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
