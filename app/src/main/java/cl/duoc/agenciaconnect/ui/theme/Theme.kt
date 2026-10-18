package cl.duoc.agenciaconnect.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

// ────────────────────────────────────────────────────────────────────────────
// COLORES SEMÁNTICOS DE NEGOCIO
// ────────────────────────────────────────────────────────────────────────────
// Con esta clase centralizamos los colores asociados a estados y prioridades
// de las oportunidades, de modo que las pantallas nunca usen hexadecimales sueltos.
data class ColoresOportunidad(
    val prioridadAlta: Color = PrioridadAlta,
    val prioridadMedia: Color = PrioridadMedia,
    val prioridadBaja: Color = PrioridadBaja,
    val estadoConfirmado: Color = EstadoConfirmado,
    val estadoDescartado: Color = EstadoDescartado,
    val estadoEnEspera: Color = EstadoEnEspera,
    val potencialAlto: Color = PotencialAlto,
    val potencialMedio: Color = PotencialMedio,
    val potencialBajo: Color = PotencialBajo
)

// Con este CompositionLocal exponemos la paleta semántica a toda la app.
val LocalColoresOportunidad = staticCompositionLocalOf { ColoresOportunidad() }

// Con esta propiedad de extensión accedemos a los colores semánticos
// igual que MaterialTheme.colorScheme, manteniendo el código legible.
val MaterialTheme.coloresOportunidad: ColoresOportunidad
    @Composable
    get() = LocalColoresOportunidad.current

@Composable
fun AgenciaConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}