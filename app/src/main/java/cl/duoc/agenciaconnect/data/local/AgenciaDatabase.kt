package cl.duoc.agenciaconnect.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.agenciaconnect.data.model.Contacto
import cl.duoc.agenciaconnect.data.model.Evento
import cl.duoc.agenciaconnect.data.model.Oportunidad
import cl.duoc.agenciaconnect.data.model.Organizacion
import cl.duoc.agenciaconnect.data.model.Seguimiento
import cl.duoc.agenciaconnect.data.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Con esta base de datos centralizamos todas las entidades relacionales de AgenciaConnect.
@Database(
    entities = [
        Evento::class,
        Organizacion::class,
        Contacto::class,
        Oportunidad::class,
        Usuario::class,
        Seguimiento::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AgenciaDatabase : RoomDatabase() {

    // Con estos métodos permitimos acceder a las operaciones de cada entidad.
    abstract fun eventoDao(): EventoDao
    abstract fun organizacionDao(): OrganizacionDao
    abstract fun contactoDao(): ContactoDao
    abstract fun oportunidadDao(): OportunidadDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun seguimientoDao(): SeguimientoDao

    companion object {

        // Con esta migración agregamos organizaciones en la versión 2.
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

        // Con esta migración incorporamos contactos en la versión 3.
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

        // Con esta migración incorporamos oportunidades comerciales en la versión 4.
        private val MIGRACION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS oportunidades (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombre TEXT NOT NULL,
                        tipo TEXT NOT NULL,
                        organizacionId INTEGER NOT NULL,
                        eventoId INTEGER NOT NULL,
                        prioridad TEXT NOT NULL,
                        potencial TEXT NOT NULL,
                        estado TEXT NOT NULL,
                        responsable TEXT NOT NULL,
                        observaciones TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        // Con esta migración agregamos imagenUri a eventos en la versión 5.
        private val MIGRACION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE eventos ADD COLUMN imagenUri TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        // Con esta migración incorporamos usuarios con roles y la bitácora de seguimientos en la versión 6.
        private val MIGRACION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS usuarios (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombre TEXT NOT NULL,
                        correo TEXT NOT NULL,
                        contrasena TEXT NOT NULL,
                        rol TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS seguimientos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        oportunidadId INTEGER NOT NULL,
                        usuarioNombre TEXT NOT NULL,
                        tipoAccion TEXT NOT NULL,
                        fechaHora TEXT NOT NULL,
                        resultado TEXT NOT NULL,
                        proximaAccion TEXT NOT NULL,
                        observaciones TEXT NOT NULL,
                        FOREIGN KEY (oportunidadId) REFERENCES oportunidades(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_seguimientos_oportunidadId ON seguimientos(oportunidadId)"
                )
            }
        }

        @Volatile
        private var INSTANCIA: AgenciaDatabase? = null

        // Con esta función reutilizamos una única instancia de la base de datos con precarga de usuarios.
        fun obtenerInstancia(context: Context): AgenciaDatabase {
            return INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    AgenciaDatabase::class.java,
                    "agencia_connect.db"
                )
                    .addMigrations(
                        MIGRACION_1_2,
                        MIGRACION_2_3,
                        MIGRACION_3_4,
                        MIGRACION_4_5,
                        MIGRACION_5_6
                    )
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Con esta corrutina garantizamos que existan las 4 cuentas iniciales requeridas.
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCIA?.let { base ->
                                    val conteo = base.usuarioDao().contarUsuarios()
                                    if (conteo == 0) {
                                        val cuentasIniciales = listOf(
                                            Usuario(
                                                nombre = "Patricia Vega (Supervisor)",
                                                correo = "supervisor@agencia.cl",
                                                contrasena = "super123",
                                                rol = "SUPERVISOR"
                                            ),
                                            Usuario(
                                                nombre = "Carlos Muñoz (Ejecutivo)",
                                                correo = "carlos.comercial@agencia.cl",
                                                contrasena = "eje123",
                                                rol = "EJECUTIVO"
                                            ),
                                            Usuario(
                                                nombre = "Marcela Rojas (Ejecutiva)",
                                                correo = "marcela.ventas@agencia.cl",
                                                contrasena = "eje123",
                                                rol = "EJECUTIVO"
                                            ),
                                            Usuario(
                                                nombre = "Felipe Soto (Ejecutivo)",
                                                correo = "felipe.convenios@agencia.cl",
                                                contrasena = "eje123",
                                                rol = "EJECUTIVO"
                                            )
                                        )
                                        base.usuarioDao().insertarUsuarios(cuentasIniciales)
                                    }
                                }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCIA = it }
            }
        }
    }
}