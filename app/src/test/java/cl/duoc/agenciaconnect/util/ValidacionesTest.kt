package cl.duoc.agenciaconnect.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Suite de pruebas unitarias para todos los validadores de lógica de negocio.
 * Seguimos el patrón Arrange - Act - Assert con comentarios en español.
 *
 * NOTA sobre android.util.Patterns: LoginValidaciones y ContactoValidaciones usan
 * android.util.Patterns.EMAIL_ADDRESS. En la JVM local ese campo es null por defecto,
 * pero con testOptions.unitTests.isReturnDefaultValues = true el matcher devuelve
 * false para cualquier entrada, por lo que las pruebas de correo verifican el
 * comportamiento del stub de Android (correo inválido = false, correo válido = false).
 * Para aislar la lógica propia probamos las funciones de ruta que NO dependen de
 * Patterns (nombre, contraseña, campos de texto) y marcamos las de correo separadas.
 */
class LoginValidacionesTest {

    // ── validarContrasenaLogin ──────────────────────────────────────────────

    @Test
    fun validarContrasenaLogin_contrasenaConTexto_retornaTrue() {
        // Organizamos: una contraseña con contenido real.
        val contrasena = "super123"
        // Actuamos: invocamos la función.
        val resultado = validarContrasenaLogin(contrasena)
        // Afirmamos: debe ser válida.
        assertTrue(resultado)
    }

    @Test
    fun validarContrasenaLogin_contrasenaVacia_retornaFalse() {
        // Organizamos: contraseña completamente vacía.
        val contrasena = ""
        // Actuamos.
        val resultado = validarContrasenaLogin(contrasena)
        // Afirmamos: debe ser inválida.
        assertFalse(resultado)
    }

    @Test
    fun validarContrasenaLogin_contrasenaSoloEspacios_retornaFalse() {
        // Organizamos: contraseña con solo espacios en blanco.
        val contrasena = "   "
        // Actuamos.
        val resultado = validarContrasenaLogin(contrasena)
        // Afirmamos: trim() elimina los espacios, debe ser inválida.
        assertFalse(resultado)
    }

    @Test
    fun validarContrasenaLogin_contrasenaSoloUnCaracter_retornaTrue() {
        // Organizamos: contraseña mínima de un carácter.
        val contrasena = "a"
        // Actuamos.
        val resultado = validarContrasenaLogin(contrasena)
        // Afirmamos: cualquier carácter distinto de espacio es válido.
        assertTrue(resultado)
    }

    // ── validarCorreoLogin (Patterns stub devuelve false en JVM) ───────────

    @Test
    fun validarCorreoLogin_correoVacio_retornaFalse() {
        // Organizamos: correo vacío.
        val correo = ""
        // Actuamos.
        val resultado = validarCorreoLogin(correo)
        // Afirmamos: vacío siempre es inválido (la primera condición isNotEmpty falla).
        assertFalse(resultado)
    }

    @Test
    fun validarCorreoLogin_correoSoloEspacios_retornaFalse() {
        // Organizamos: correo de solo espacios.
        val correo = "   "
        // Actuamos.
        val resultado = validarCorreoLogin(correo)
        // Afirmamos: trim() queda vacío, inválido.
        assertFalse(resultado)
    }
}

// ─────────────────────────────────────────────────────────────────────────────

class OrganizacionValidacionesTest {

    @Test
    fun validarNombreOrganizacion_nombreValido_retornaTrue() {
        // Organizamos: nombre real de empresa.
        val nombre = "Clínica Dental Sonrisa"
        // Actuamos.
        val resultado = validarNombreOrganizacion(nombre)
        // Afirmamos: válido.
        assertTrue(resultado)
    }

    @Test
    fun validarNombreOrganizacion_nombreVacio_retornaFalse() {
        // Organizamos: nombre vacío.
        val resultado = validarNombreOrganizacion("")
        // Afirmamos.
        assertFalse(resultado)
    }

    @Test
    fun validarNombreOrganizacion_nombreSoloEspacios_retornaFalse() {
        // Organizamos: solo espacios.
        val resultado = validarNombreOrganizacion("   ")
        // Afirmamos: trim queda vacío.
        assertFalse(resultado)
    }

    @Test
    fun validarTipoOrganizacion_tipoValido_retornaTrue() {
        assertTrue(validarTipoOrganizacion("Empresa"))
    }

    @Test
    fun validarTipoOrganizacion_tipoVacio_retornaFalse() {
        assertFalse(validarTipoOrganizacion(""))
    }

    @Test
    fun validarTipoOrganizacion_tipoEspacios_retornaFalse() {
        assertFalse(validarTipoOrganizacion("  "))
    }

    @Test
    fun validarRubroOrganizacion_rubroValido_retornaTrue() {
        assertTrue(validarRubroOrganizacion("Salud"))
    }

    @Test
    fun validarRubroOrganizacion_rubroVacio_retornaFalse() {
        assertFalse(validarRubroOrganizacion(""))
    }

    @Test
    fun validarComunaOrganizacion_comunaValida_retornaTrue() {
        assertTrue(validarComunaOrganizacion("Providencia"))
    }

    @Test
    fun validarComunaOrganizacion_comunaVacia_retornaFalse() {
        assertFalse(validarComunaOrganizacion(""))
    }

    @Test
    fun validarRegionOrganizacion_regionValida_retornaTrue() {
        assertTrue(validarRegionOrganizacion("Metropolitana"))
    }

    @Test
    fun validarRegionOrganizacion_regionVacia_retornaFalse() {
        assertFalse(validarRegionOrganizacion(""))
    }

    @Test
    fun validarPublicoAsociadoOrganizacion_publicoValido_retornaTrue() {
        assertTrue(validarPublicoAsociadoOrganizacion("Colaboradores"))
    }

    @Test
    fun validarPublicoAsociadoOrganizacion_publicoVacio_retornaFalse() {
        assertFalse(validarPublicoAsociadoOrganizacion(""))
    }

    @Test
    fun validarPublicoAsociadoOrganizacion_publicoEspacios_retornaFalse() {
        assertFalse(validarPublicoAsociadoOrganizacion("   "))
    }
}

// ─────────────────────────────────────────────────────────────────────────────

class EventoValidacionesTest {

    @Test
    fun validarNombreEvento_nombreValido_retornaTrue() {
        assertTrue(validarNombreEvento("Feria de Salud 2026"))
    }

    @Test
    fun validarNombreEvento_nombreVacio_retornaFalse() {
        assertFalse(validarNombreEvento(""))
    }

    @Test
    fun validarNombreEvento_nombreEspacios_retornaFalse() {
        assertFalse(validarNombreEvento("   "))
    }

    @Test
    fun validarDescripcionEvento_descripcionValida_retornaTrue() {
        assertTrue(validarDescripcionEvento("Exposición de servicios dentales"))
    }

    @Test
    fun validarDescripcionEvento_descripcionVacia_retornaFalse() {
        assertFalse(validarDescripcionEvento(""))
    }

    @Test
    fun validarUbicacionEvento_ubicacionValida_retornaTrue() {
        assertTrue(validarUbicacionEvento("Espacio Riesco, Santiago"))
    }

    @Test
    fun validarUbicacionEvento_ubicacionVacia_retornaFalse() {
        assertFalse(validarUbicacionEvento(""))
    }

    @Test
    fun validarOrganizacionEvento_organizacionValida_retornaTrue() {
        assertTrue(validarOrganizacionEvento("Clínica Dental"))
    }

    @Test
    fun validarOrganizacionEvento_organizacionVacia_retornaFalse() {
        assertFalse(validarOrganizacionEvento(""))
    }

    @Test
    fun validarFechaEvento_fechaFuturaValida_retornaTrue() {
        // Organizamos: fecha futura en formato DDMMYYYY (dentro del año 2027).
        val fecha = "15062027"
        // Actuamos.
        val resultado = validarFechaEvento(fecha)
        // Afirmamos: fecha futura es válida.
        assertTrue(resultado)
    }

    @Test
    fun validarFechaEvento_fechaPasada_retornaFalse() {
        // Organizamos: fecha en 2020 (claramente pasada).
        val fecha = "10062020"
        // Actuamos.
        val resultado = validarFechaEvento(fecha)
        // Afirmamos: fecha pasada es inválida.
        assertFalse(resultado)
    }

    @Test
    fun validarFechaEvento_fechaMenosDeOchoDigitos_retornaFalse() {
        // Organizamos: fecha incompleta.
        val fecha = "1506"
        // Actuamos.
        val resultado = validarFechaEvento(fecha)
        // Afirmamos: longitud incorrecta es inválida.
        assertFalse(resultado)
    }

    @Test
    fun validarFechaEvento_fechaConLetras_retornaFalse() {
        // Organizamos: texto no numérico.
        val fecha = "DDMMYYYY"
        // Actuamos.
        val resultado = validarFechaEvento(fecha)
        // Afirmamos: no es numérico.
        assertFalse(resultado)
    }

    @Test
    fun validarFechaEvento_fechaInexistente_retornaFalse() {
        // Organizamos: 32 de enero — día inexistente.
        val fecha = "32012027"
        // Actuamos.
        val resultado = validarFechaEvento(fecha)
        // Afirmamos: fecha inválida en el calendario.
        assertFalse(resultado)
    }

    @Test
    fun validarImagenUri_uriValida_retornaTrue() {
        assertTrue(validarImagenUri("content://media/external/images/1234"))
    }

    @Test
    fun validarImagenUri_uriVacia_retornaFalse() {
        assertFalse(validarImagenUri(""))
    }

    @Test
    fun validarImagenUri_uriEspacios_retornaFalse() {
        assertFalse(validarImagenUri("   "))
    }
}

// ─────────────────────────────────────────────────────────────────────────────

class ContactoValidacionesTest {

    @Test
    fun validarNombreContacto_nombreValido_retornaTrue() {
        assertTrue(validarNombreContacto("Camila Pérez"))
    }

    @Test
    fun validarNombreContacto_nombreVacio_retornaFalse() {
        assertFalse(validarNombreContacto(""))
    }

    @Test
    fun validarNombreContacto_nombreSoloEspacios_retornaFalse() {
        assertFalse(validarNombreContacto("   "))
    }

    @Test
    fun validarOrganizacionContacto_idPositivo_retornaTrue() {
        // Organizamos: ID positivo válido.
        val resultado = validarOrganizacionContacto(5L)
        assertTrue(resultado)
    }

    @Test
    fun validarOrganizacionContacto_idNulo_retornaFalse() {
        // Organizamos: sin organización seleccionada.
        val resultado = validarOrganizacionContacto(null)
        assertFalse(resultado)
    }

    @Test
    fun validarOrganizacionContacto_idCero_retornaFalse() {
        // Organizamos: ID 0 no corresponde a ningún registro.
        val resultado = validarOrganizacionContacto(0L)
        assertFalse(resultado)
    }

    @Test
    fun validarOrganizacionContacto_idNegativo_retornaFalse() {
        // Organizamos: ID negativo no es válido.
        val resultado = validarOrganizacionContacto(-1L)
        assertFalse(resultado)
    }

    @Test
    fun validarCargoContacto_cargoValido_retornaTrue() {
        assertTrue(validarCargoContacto("Jefa de Bienestar"))
    }

    @Test
    fun validarCargoContacto_cargoVacio_retornaFalse() {
        assertFalse(validarCargoContacto(""))
    }

    @Test
    fun validarAreaContacto_areaValida_retornaTrue() {
        assertTrue(validarAreaContacto("Recursos Humanos"))
    }

    @Test
    fun validarAreaContacto_areaVacia_retornaFalse() {
        assertFalse(validarAreaContacto(""))
    }

    @Test
    fun validarAreaContacto_areaSoloEspacios_retornaFalse() {
        assertFalse(validarAreaContacto("   "))
    }

    @Test
    fun validarMedioPreferidoContacto_medioValido_retornaTrue() {
        assertTrue(validarMedioPreferidoContacto("Correo"))
    }

    @Test
    fun validarMedioPreferidoContacto_medioVacio_retornaFalse() {
        assertFalse(validarMedioPreferidoContacto(""))
    }

    // ── validarCorreoContacto también usa Patterns (stub en JVM) ─────────

    @Test
    fun validarCorreoContacto_correoVacio_retornaFalse() {
        // Organizamos: correo vacío.
        val resultado = validarCorreoContacto("")
        // Afirmamos: inválido por isEmpty.
        assertFalse(resultado)
    }

    @Test
    fun validarCorreoContacto_correoEspacios_retornaFalse() {
        // Organizamos: solo espacios.
        val resultado = validarCorreoContacto("   ")
        // Afirmamos: trim queda vacío.
        assertFalse(resultado)
    }
}

// ─────────────────────────────────────────────────────────────────────────────

class ValidacionesOportunidadTest {

    @Test
    fun validarNombreOportunidad_nombreValido_retornaTrue() {
        // Organizamos: nombre descriptivo de la oportunidad.
        val resultado = validarNombreOportunidad("Activación Módulo Dental")
        // Afirmamos: válido.
        assertTrue(resultado)
    }

    @Test
    fun validarNombreOportunidad_nombreVacio_retornaFalse() {
        // Organizamos: vacío.
        val resultado = validarNombreOportunidad("")
        // Afirmamos: inválido.
        assertFalse(resultado)
    }

    @Test
    fun validarNombreOportunidad_nombreSoloEspacios_retornaFalse() {
        // Organizamos: solo espacios.
        val resultado = validarNombreOportunidad("   ")
        // Afirmamos: inválido.
        assertFalse(resultado)
    }

    @Test
    fun validarTipoOportunidad_tipoPresente_retornaTrue() {
        // Organizamos: tipo de oportunidad seleccionado.
        val tipo = cl.duoc.agenciaconnect.data.model.TipoOportunidad.FERIA_EMPRESARIAL
        // Actuamos.
        val resultado = validarTipoOportunidad(tipo)
        // Afirmamos: válido.
        assertTrue(resultado)
    }

    @Test
    fun validarTipoOportunidad_tipoNulo_retornaFalse() {
        // Organizamos: sin tipo seleccionado.
        val resultado = validarTipoOportunidad(null)
        // Afirmamos: inválido.
        assertFalse(resultado)
    }

    @Test
    fun validarOrganizacionOportunidad_idPositivo_retornaTrue() {
        assertTrue(validarOrganizacionOportunidad(3L))
    }

    @Test
    fun validarOrganizacionOportunidad_idNulo_retornaFalse() {
        assertFalse(validarOrganizacionOportunidad(null))
    }

    @Test
    fun validarOrganizacionOportunidad_idCero_retornaFalse() {
        assertFalse(validarOrganizacionOportunidad(0L))
    }

    @Test
    fun validarOrganizacionOportunidad_idNegativo_retornaFalse() {
        assertFalse(validarOrganizacionOportunidad(-5L))
    }

    @Test
    fun validarPrioridadOportunidad_prioridadPresente_retornaTrue() {
        assertTrue(validarPrioridadOportunidad(cl.duoc.agenciaconnect.data.model.PrioridadOportunidad.ALTA))
    }

    @Test
    fun validarPrioridadOportunidad_prioridadNula_retornaFalse() {
        assertFalse(validarPrioridadOportunidad(null))
    }

    @Test
    fun validarPotencialOportunidad_potencialPresente_retornaTrue() {
        assertTrue(validarPotencialOportunidad(cl.duoc.agenciaconnect.data.model.PotencialOportunidad.ALTO))
    }

    @Test
    fun validarPotencialOportunidad_potencialNulo_retornaFalse() {
        assertFalse(validarPotencialOportunidad(null))
    }

    @Test
    fun validarEstadoOportunidad_estadoPresente_retornaTrue() {
        assertTrue(validarEstadoOportunidad(cl.duoc.agenciaconnect.data.model.EstadoOportunidad.PROPUESTA_ENVIADA))
    }

    @Test
    fun validarEstadoOportunidad_estadoNulo_retornaFalse() {
        assertFalse(validarEstadoOportunidad(null))
    }

    @Test
    fun validarResponsableOportunidad_responsableValido_retornaTrue() {
        assertTrue(validarResponsableOportunidad("Carlos Muñoz"))
    }

    @Test
    fun validarResponsableOportunidad_responsableVacio_retornaFalse() {
        assertFalse(validarResponsableOportunidad(""))
    }

    @Test
    fun validarResponsableOportunidad_responsableSoloEspacios_retornaFalse() {
        assertFalse(validarResponsableOportunidad("   "))
    }

    // ── validarFechaDeteccion ─────────────────────────────────────────────

    @Test
    fun validarFechaDeteccion_fechaValidaEnPasado_retornaTrue() {
        // Organizamos: fecha en el pasado pero real (formato DDMMYYYY sin barras).
        val fecha = "10082024"
        // Actuamos.
        val resultado = validarFechaDeteccion(fecha)
        // Afirmamos: fecha pasada es válida para detección.
        assertTrue(resultado)
    }

    @Test
    fun validarFechaDeteccion_fechaFutura_retornaFalse() {
        // Organizamos: año 2099, claramente futuro.
        val fecha = "01012099"
        // Actuamos.
        val resultado = validarFechaDeteccion(fecha)
        // Afirmamos: no puede ser posterior a hoy.
        assertFalse(resultado)
    }

    @Test
    fun validarFechaDeteccion_fechaBlanca_retornaFalse() {
        // Organizamos: cadena vacía.
        val resultado = validarFechaDeteccion("")
        // Afirmamos: inválido.
        assertFalse(resultado)
    }

    @Test
    fun validarFechaDeteccion_fechaIncompleta_retornaFalse() {
        // Organizamos: solo 4 dígitos.
        val resultado = validarFechaDeteccion("0108")
        // Afirmamos: longitud incorrecta.
        assertFalse(resultado)
    }

    @Test
    fun validarFechaDeteccion_fechaConLetras_retornaFalse() {
        // Organizamos: texto con letras mezcladas.
        val resultado = validarFechaDeteccion("01AB2024")
        // Afirmamos: soloDigitos filtra, se quedan menos de 8 dígitos.
        assertFalse(resultado)
    }

    @Test
    fun validarFechaDeteccion_fechaConDiaInvalido_retornaFalse() {
        // Organizamos: día 32, inexistente.
        val resultado = validarFechaDeteccion("32012020")
        // Afirmamos: leniencia false rechaza.
        assertFalse(resultado)
    }
}
