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
    version = 7, // <--- Actualizado a versión 7 para soportar fechaDeteccion
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

        // Con esta migración incorporamos usuarios y seguimientos en la versión 6.
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

        // Con esta migración incorporamos fechaDeteccion en oportunidades en la versión 7.
        private val MIGRACION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE oportunidades ADD COLUMN fechaDeteccion TEXT NOT NULL DEFAULT ''"
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
                        MIGRACION_5_6,
                        MIGRACION_6_7
                    )
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Con esta corrutina garantizamos la precarga inicial de datos de prueba coherentes.
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCIA?.let { base ->

                                    // 1. Precarga de Usuarios (Roles)
                                    val conteoUsuarios = base.usuarioDao().contarUsuarios()
                                    if (conteoUsuarios == 0) {
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

                                    // 2. Precarga de Organizaciones Base
                                    val cursorOrgs = db.query("SELECT COUNT(*) FROM organizaciones")
                                    var totalOrgs = 0
                                    if (cursorOrgs.moveToFirst()) {
                                        totalOrgs = cursorOrgs.getInt(0)
                                    }
                                    cursorOrgs.close()

                                    if (totalOrgs == 0) {
                                        val idOrg1 = db.compileStatement("INSERT INTO organizaciones (nombre, tipo, rubro, comuna, region, sitioWeb, publicoAsociado, observaciones) VALUES ('Clínica Dental Sonrisa Austral', 'Empresa', 'Salud / Dental', 'Providencia', 'Metropolitana', 'www.sonrisasaustral.cl', 'Colaboradores y familias', 'Convenio corporativo dental prioritario')").executeInsert()
                                        val idOrg2 = db.compileStatement("INSERT INTO organizaciones (nombre, tipo, rubro, comuna, region, sitioWeb, publicoAsociado, observaciones) VALUES ('Automotora Central de Servicios', 'Empresa', 'Automotriz', 'Ñuñoa', 'Metropolitana', 'www.automotoracentral.cl', 'Trabajadores y conductores', 'Servicios de mantención y flota')").executeInsert()
                                        val idOrg3 = db.compileStatement("INSERT INTO organizaciones (nombre, tipo, rubro, comuna, region, sitioWeb, publicoAsociado, observaciones) VALUES ('Centro Veterinario Patitas Felices', 'Empresa', 'Veterinario', 'La Florida', 'Metropolitana', 'www.patitasfelices.cl', 'Amantes de mascotas y dueños', 'Beneficios veterinarios corporativos')").executeInsert()
                                        val idOrg4 = db.compileStatement("INSERT INTO organizaciones (nombre, tipo, rubro, comuna, region, sitioWeb, publicoAsociado, observaciones) VALUES ('Municipalidad de Puente Alto', 'Municipalidad', 'Institución Pública', 'Puente Alto', 'Metropolitana', 'www.puentealto.cl', 'Vecinos y funcionarios municipales', 'Ferias vecinales de alta afluencia')").executeInsert()
                                        val idOrg5 = db.compileStatement("INSERT INTO organizaciones (nombre, tipo, rubro, comuna, region, sitioWeb, publicoAsociado, observaciones) VALUES ('Corporación de Bienestar Bancario', 'Asociación', 'Financiero / Bienestar', 'Santiago', 'Metropolitana', 'www.bienestarbancario.cl', 'Asociados y cargas familiares', 'Jornadas de beneficios institucionales')").executeInsert()

                                        // 3. Precarga de Contactos vinculados
                                        db.execSQL("INSERT INTO contactos (nombreCompleto, organizacionId, cargo, area, correo, telefono, medioPreferido, observaciones) VALUES ('Camara Pérez', $idOrg1, 'Jefa de Bienestar', 'Recursos Humanos', 'cperez@sonrisas.cl', '+56911223344', 'Correo', 'Muy interesada en activaciones dentales')")
                                        db.execSQL("INSERT INTO contactos (nombreCompleto, organizacionId, cargo, area, correo, telefono, medioPreferido, observaciones) VALUES ('Esteban Gatica', $idOrg2, 'Encargado de Beneficios', 'Administración', 'egatica@central.cl', '+56922334455', 'Teléfono', 'Coordina espacios en edificio corporativo')")
                                        db.execSQL("INSERT INTO contactos (nombreCompleto, organizacionId, cargo, area, correo, telefono, medioPreferido, observaciones) VALUES ('Daniela Soto', $idOrg3, 'Coordinadora de Eventos', 'Marketing', 'dsoto@patitas.cl', '+56933445566', 'Correo', 'Apoya ferias temáticas de mascotas')")
                                        db.execSQL("INSERT INTO contactos (nombreCompleto, organizacionId, cargo, area, correo, telefono, medioPreferido, observaciones) VALUES ('Rodrigo Valenzuela', $idOrg4, 'Director Dideco', 'Comunicaciones', 'rvalenzuela@puentealto.cl', '+56944556677', 'Teléfono', 'Gestiona permisos para plazas y espacios públicos')")
                                        db.execSQL("INSERT INTO contactos (nombreCompleto, organizacionId, cargo, area, correo, telefono, medioPreferido, observaciones) VALUES ('Andrea Fuentes', $idOrg5, 'Supervisora de Convenios', 'Bienestar', 'afuentes@bienestarbancario.cl', '+56955667788', 'Correo', 'Busca alianzas para socios bancarios')")

                                        // 4. Precarga de Eventos de prueba
                                        db.execSQL("INSERT INTO eventos (nombre, descripcion, fecha, ubicacion, organizacion, imagenUri) VALUES ('Feria de Salud y Sonrisas', 'Exposición de servicios dentales preventivos', '25082026', 'Espacio Riesco', 'Clínica Dental Sonrisa Austral', 'android.resource://cl.duoc.agenciaconnect/drawable/imagen5')")
                                        db.execSQL("INSERT INTO eventos (nombre, descripcion, fecha, ubicacion, organizacion, imagenUri) VALUES ('Jornada de Beneficios Automotrices', 'Revisión técnica y convenios de mantención', '10092026', 'Edificio Corporativo Ñuñoa', 'Automotora Central de Servicios', 'android.resource://cl.duoc.agenciaconnect/drawable/imagen4')")
                                        db.execSQL("INSERT INTO eventos (nombre, descripcion, fecha, ubicacion, organizacion, imagenUri) VALUES ('Expo Mascotas y Bienestar', 'Feria de salud animal y emprendedores', '15102026', 'Parque Bicentenario', 'Centro Veterinario Patitas Felices', 'android.resource://cl.duoc.agenciaconnect/drawable/imagen3')")
                                        db.execSQL("INSERT INTO eventos (nombre, descripcion, fecha, ubicacion, organizacion, imagenUri) VALUES ('Feria Vecinal y Servicios', 'Activación municipal abierta a la comunidad', '05112026', 'Plaza de Armas Puente Alto', 'Municipalidad de Puente Alto', 'android.resource://cl.duoc.agenciaconnect/drawable/imagen2')")
                                        db.execSQL("INSERT INTO eventos (nombre, descripcion, fecha, ubicacion, organizacion, imagenUri) VALUES ('Encuentro Anual de Bienestar', 'Jornada corporativa para asociados y familias', '20112026', 'Hotel Sheraton Santiago', 'Corporación de Bienestar Bancario', 'android.resource://cl.duoc.agenciaconnect/drawable/imagen1')")

                                        // 5. Precarga de Oportunidades comerciales (con fechaDeteccion incluida)
                                        db.execSQL("INSERT INTO oportunidades (nombre, tipo, organizacionId, eventoId, prioridad, potencial, estado, responsable, observaciones, fechaDeteccion) VALUES ('Activación Módulo Dental en Feria', 'Feria empresarial', $idOrg1, 1, 'Alta', 'Alto', 'Propuesta enviada', 'Carlos Muñoz', 'Se envió presentación formal con tarifas preferenciales', '10082026')")
                                        db.execSQL("INSERT INTO oportunidades (nombre, tipo, organizacionId, eventoId, prioridad, potencial, estado, responsable, observaciones, fechaDeteccion) VALUES ('Convenio Mantención Flota Colaboradores', 'Evento corporativo', $idOrg2, 2, 'Media', 'Alto', 'Contacto realizado', 'Marcela Rojas', 'Reunión inicial agendada para la próxima semana', '15082026')")
                                        db.execSQL("INSERT INTO oportunidades (nombre, tipo, organizacionId, eventoId, prioridad, potencial, estado, responsable, observaciones, fechaDeteccion) VALUES ('Jornada de Vacunación y Cuidados', 'Jornada de beneficios', $idOrg3, 3, 'Alta', 'Medio', 'Contacto pendiente', 'Felipe Soto', 'Esperando validación de fechas por parte del centro', '01092026')")
                                        db.execSQL("INSERT INTO oportunidades (nombre, tipo, organizacionId, eventoId, prioridad, potencial, estado, responsable, observaciones, fechaDeteccion) VALUES ('Stand de Beneficios en Feria Municipal', 'Actividad municipal', $idOrg4, 4, 'Alta', 'Alto', 'Evento identificado', 'Carlos Muñoz', 'Detectado aviso oficial en sitio municipal', '10092026')")
                                        db.execSQL("INSERT INTO oportunidades (nombre, tipo, organizacionId, eventoId, prioridad, potencial, estado, responsable, observaciones, fechaDeteccion) VALUES ('Alianza de Convenios Exclusivos Socios', 'Feria institucional', $idOrg5, 5, 'Media', 'Alto', 'En espera de respuesta', 'Marcela Rojas', 'Propuesta enviada a comité de bienestar', '01102026')")
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