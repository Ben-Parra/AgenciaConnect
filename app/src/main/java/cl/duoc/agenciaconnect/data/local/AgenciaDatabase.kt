package cl.duoc.agenciaconnect.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.agenciaconnect.data.model.Contacto
import cl.duoc.agenciaconnect.data.model.Evento
import cl.duoc.agenciaconnect.data.model.Organizacion

// Con esta base de datos centralizamos las entidades que utiliza AgenciaConnect.
@Database(
    entities = [Evento::class, Organizacion::class, Contacto::class],
    version = 3,
    exportSchema = false
)
abstract class AgenciaDatabase : RoomDatabase() {

    // Con estos métodos permitimos acceder a las operaciones de cada entidad.
    abstract fun eventoDao(): EventoDao
    abstract fun organizacionDao(): OrganizacionDao
    abstract fun contactoDao(): ContactoDao

    companion object {

        // Con esta migración agregamos organizaciones sin eliminar los eventos existentes.
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS organizaciones (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombre TEXT NOT NULL,
                        tipo TEXT NOT NULL,
                        rubro TEXT NOT NULL,
                        comuna TEXT NOT NULL,
                        region TEXT NOT NULL,
                        sitioWeb TEXT NOT NULL,
                        publicoAsociado TEXT NOT NULL,
                        observaciones TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        // Con esta migración incorporamos contactos y conservamos las tablas anteriores.
        private val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS contactos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombreCompleto TEXT NOT NULL,
                        organizacionId INTEGER NOT NULL,
                        cargo TEXT NOT NULL,
                        area TEXT NOT NULL,
                        correo TEXT NOT NULL,
                        telefono TEXT NOT NULL,
                        medioPreferido TEXT NOT NULL,
                        observaciones TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var INSTANCIA: AgenciaDatabase? = null

        // Con esta función reutilizamos una única instancia de la base de datos.
        fun obtenerInstancia(context: Context): AgenciaDatabase {
            return INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    AgenciaDatabase::class.java,
                    "agencia_connect.db"
                )
                    .addMigrations(MIGRACION_1_2, MIGRACION_2_3)
                    .build()
                    .also { INSTANCIA = it }
            }
        }
    }
}
