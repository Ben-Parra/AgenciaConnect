package cl.duoc.agenciaconnect.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

// Con esta función creamos una URI temporal y segura mediante FileProvider
// para que la aplicación de la cámara pueda almacenar la fotografía capturada.
fun crearUriFotografia(context: Context): Uri {
    val carpeta = File(context.cacheDir, "fotos")
    carpeta.mkdirs()
    val archivo = File.createTempFile("foto_", ".jpg", carpeta)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        archivo
    )
}