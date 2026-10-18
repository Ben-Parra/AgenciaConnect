# AgenciaConnect 

Aplicación móvil desarrollada en **Android Studio** utilizando **Jetpack Compose**, **Material Design 3**, arquitectura **MVVM** y persistencia local con **Room (SQLite)**.

Este proyecto forma parte de la **Evaluación Parcial 2** de la asignatura **DSY1105 - Desarrollo de Aplicaciones Móviles** (Duoc UC).

---

##  Integrantes del Equipo
* **Carla Urrutia**
* **Benjamin Parra**

---

##  Funcionalidades Principales

* **Módulo de Autenticación y Roles:** Inicio de sesión local con 4 cuentas predefinidas (1 Supervisor y 3 Ejecutivos), control estricto de permisos y visibilidad de datos según el rol activo.
* **Gestión de Oportunidades Comerciales:** Registro, edición, eliminación, búsqueda en tiempo real y filtros avanzados por Estado y Prioridad.
* **Gestión de Eventos y Organizaciones:** Administración completa de empresas, municipalidades, instituciones y eventos corporativos vinculados.
* **Integración de Recursos Nativos:**
    * *Cámara y Photo Picker:* Selección o captura fotográfica en vivo para asociarla a eventos corporativos de manera segura mediante `FileProvider`.
    * *Calendario Nativo:* Enlace directo con el calendario del dispositivo para programar recordatorios de seguimientos comerciales.
* **Bitácora de Seguimiento:** Historial cronológico de acciones comerciales por oportunidad.
* **Diseño UI/UX y Animaciones:** Interfaz construida con Material Design 3, colores semánticos centralizados, transiciones fluidas entre pantallas (`AnimatedContent`), animaciones de aparición (`AnimatedVisibility`) y microinteracciones en botones.

---

##  Arquitectura del Software (MVVM)

El proyecto sigue estrictamente el patrón arquitectónico **MVVM** separado por responsabilidades:
* **`data/`**: Modelos de datos (`@Entity`), interfaces de acceso a datos (`@Dao`), base de datos unificada con Room (`AppDatabase`) y repositorios de enlace.
* **`ui/`**: Capas visuales desacopladas organizadas por módulos (`login`, `oportunidades`, `eventos`, `organizaciones`, `contactos`, `seguimientos`) junto a sus respectivos `ViewModels` y estados (`UiState`).
* **`util/`**: Lógica de negocio y validaciones centralizadas independientes de los componentes visuales (*Composables*).

---

##  Instrucciones de Ejecución y Pruebas

1. **Requisitos previos:** Tener instalado Android Studio (versión recomendada QUAIL 4 o superior), emulador o dispositivo físico configurado con API 26 o superior.
2. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/Ben-Parra/AgenciaConnect.git](https://github.com/Ben-Parra/AgenciaConnect.git)


Abrir en Android Studio: Selecciona Open an Existing Project y apunta a la carpeta raíz de AgenciaConnect.

Sincronizar Gradle: Haz clic en Sync Project with Gradle Files para descargar las dependencias de Compose, Room y Coil.

Ejecutar la App: Selecciona un dispositivo virtual (ej. Pixel 8, API 36) y presiona Run (Shift + F10).

🔑 Cuentas de Prueba Precargadas (Seeder Automático)
La aplicación incluye precarga automática de datos de prueba coherentes al iniciar por primera vez:

Supervisor: supervisor@agencia.cl / Contraseña: super123

Ejecutivo 1: carlos.comercial@agencia.cl / Contraseña: eje123

Ejecutivo 2: marcela.ventas@agencia.cl / Contraseña: eje123

Ejecutivo 3: felipe.convenios@agencia.cl / Contraseña: eje123