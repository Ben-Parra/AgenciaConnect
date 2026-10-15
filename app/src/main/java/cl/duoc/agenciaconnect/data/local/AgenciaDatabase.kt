
package cl.duoc.agenciaconnect.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cl.duoc.agenciaconnect.data.model.Evento

// Con esta clase definimos la base de datos local
// y registramos las entidades que utilizaremos en la aplicación.
@Database(
    entities = [Evento::class],
    version = 1,
    exportSchema = false
)
abstract class AgenciaDatabase : RoomDatabase() {

    // Con esta función proporcionamos acceso a las operaciones
    // que definimos en EventoDao.
    abstract fun eventoDao(): EventoDao

    companion object {

        @Volatile
        private var INSTANCE: AgenciaDatabase? = null

        // Con esta función obtenemos una única instancia de la base
        // de datos para reutilizarla durante la ejecución de la aplicación.
        fun obtenerInstancia(context: Context): AgenciaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AgenciaDatabase::class.java,
                    "agencia_connect.db"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
