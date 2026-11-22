# AgenciaConnect

Aplicación móvil nativa desarrollada en **Kotlin** y **Jetpack Compose** bajo arquitectura **MVVM**, integrada con **microservicios REST en Spring Boot**, base de datos relacional **MySQL**, persistencia local con **Room (SQLite)** y consumo de **API externa mediante Retrofit**.

Este proyecto corresponde a la **Evaluación Parcial 4** de la asignatura **DSY1105 - Desarrollo de Aplicaciones Móviles** (**Duoc UC**), enmarcado en el caso *Gestión móvil de oportunidades y activaciones corporativas* para una agencia de marketing directo y gestión de convenios corporativos (clínicas dentales, servicios automotrices y veterinarios).

---

## Integrantes del Equipo
* **Carla Urrutia**
* **Benjamin Parra**

---

## Planificación y Gestión Colaborativa
* **Tablero Trello:** [https://trello.com/b/AjHESmxV/tareas](https://trello.com/b/AjHESmxV/tareas)
* **Repositorio GitHub:** [https://github.com/Ben-Parra/AgenciaConnect](https://github.com/Ben-Parra/AgenciaConnect)
* **Backend Spring Boot:** [https://github.com/CarlaUrrutia/AgenciaConnectBackend.git](https://github.com/CarlaUrrutia/AgenciaConnectBackend.git)

---

## Funcionalidades Principales

* **Módulo de Autenticación y Roles:** Inicio de sesión con cuentas ficticias de demostración (1 Supervisor y 3 Ejecutivos comerciales), control de permisos y visibilidad diferenciada según el perfil activo.
* **Gestión de Oportunidades Comerciales (CRUD Remoto y Local):** Registro, edición, eliminación, búsqueda en tiempo real y filtros avanzados por texto, tipo, organización y ubicación, sincronizados en tiempo real con Spring Boot y respaldados en Room.
* **Gestión de Eventos y Organizaciones:** Administración completa de empresas, municipalidades e instituciones responsables junto a sus eventos corporativos vinculados.
* **Gestión de Contactos Corporativos:** Registro y vinculación de contactos ficticios por organización, cargo, área y medio de contacto preferido.
* **Bitácora de Seguimiento:** Historial cronológico de gestiones comerciales, resultados y programación de próximas acciones por oportunidad.
* **Consumo de API Externa:** Integración vía Retrofit para consultar datos externos de apoyo en tiempo real dentro del flujo visual de la aplicación, sin interferir con la persistencia local ni los microservicios propios.
* **Integración de Recursos Nativos:**
  * **Cámara y Photo Picker:** Captura fotográfica en vivo o selección desde galería para asociar imágenes a eventos corporativos mediante `FileProvider`.
  * **Calendario Nativo:** Enlace directo con el calendario del dispositivo para agendar recordatorios de seguimientos comerciales.
* **Diseño UI/UX (Material Design 3):** Interfaz moderna con paleta semántica centralizada, validaciones en tiempo real en todos los formularios, retroalimentación visual de estados (carga, éxito y error) y animaciones (`AnimatedContent` y `AnimatedVisibility`).

---

## Arquitectura del Software (MVVM)

El proyecto está estructurado siguiendo el patrón **Model - View - ViewModel** con separación estricta de responsabilidades:

* **`data/local/`**: Entidades de Room (`@Entity`), objetos de acceso a datos (`@Dao`), conversores de tipos (`OportunidadConverters`) y configuración de base de datos local (`AgenciaDatabase`).
* **`data/remote/`**: Interfaces de **Retrofit** (`AgenciaApi` y cliente para API externa) junto con los DTOs/modelos de red.
* **`data/repository/`**: Repositorios (`OportunidadRepository`, `EventoRepository`, `OrganizacionRepository`, `ContactoRepository`, `SeguimientoRepository`, `UsuarioRepository`) que coordinan la sincronización entre los microservicios Spring Boot y la caché local en Room.
* **`ui/`**: Pantallas en Jetpack Compose (`Screen`), estados inmutables (`UiState`) y controladores (`ViewModel`) organizados por módulo (`login`, `oportunidades`, `eventos`, `organizaciones`, `contactos`, `seguimientos`).
* **`util/`**: Reglas de negocio y funciones puras de validación independientes de la interfaz gráfica.

---

## Endpoints Utilizados

### 1. Microservicios Propios (Spring Boot REST API)
Base URL en emulador Android: `http://10.0.2.2:8080/api/`

| Módulo | Método HTTP | Endpoint | Descripción |
| :--- | :---: | :--- | :--- |
| **Usuarios** | `GET` / `POST` | `/usuarios` | Lista usuarios y registra cuentas ficticias. |
| **Organizaciones** | `GET` / `POST` / `PUT` / `DELETE` | `/organizaciones` (`/{id}`) | Operaciones CRUD completas de organizaciones. |
| **Eventos** | `GET` / `POST` / `PUT` / `DELETE` | `/eventos` (`/{id}`) | Operaciones CRUD completas de eventos corporativos. |
| **Oportunidades** | `GET` / `POST` / `PUT` / `DELETE` | `/oportunidades` (`/{id}`) | Operaciones CRUD y actualización de estados comerciales. |
| **Contactos** | `GET` / `POST` / `PUT` / `DELETE` | `/contactos` (`/{id}`) | Operaciones CRUD de contactos asociados a organizaciones. |
| **Seguimientos** | `GET` / `POST` / `PUT` / `DELETE` | `/seguimientos` (`/{id}`) | Registro y consulta de historial de gestiones. |

---

## Instrucciones de Ejecución

### 1. Base de Datos y Backend (Spring Boot)
1. Iniciar el servicio de **MySQL** (por ejemplo, desde **XAMPP Control Panel** en el puerto `3306`) y verificar la configuración en `src/main/resources/application.properties`.
2. Abrir el proyecto del backend (`agenciaconnectapi`) en **IntelliJ IDEA** o ejecutar `./mvnw spring-boot:run`.
3. Verificar que el servidor Spring Boot levante sin errores en `http://localhost:8080`.

### 2. Aplicación Móvil (Android Studio)
1. **Requisitos previos:** Android Studio (Ladybug / Meerkat / Quail o superior), JDK 17/21 y emulador o dispositivo físico con **Android API 26 o superior**.
2. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/Ben-Parra/AgenciaConnect.git](https://github.com/Ben-Parra/AgenciaConnect.git)
   ```
3. **Abrir y sincronizar:** Abrir la carpeta raíz `AgenciaConnect` en Android Studio y ejecutar **Sync Project with Gradle Files**.
4. **Ejecutar:** Seleccionar un dispositivo virtual (ej. *Pixel 8, API 36*) y presionar **Run (`Shift + F10`)**.

---

## Cuentas de Prueba Precargadas (Datos Ficticios)
Al iniciar la aplicación por primera vez se cargan credenciales ficticias de demostración académica:

| Rol | Correo Electrónico | Contraseña |
| :--- | :--- | :--- |
| **Supervisor** | `supervisor@agencia.cl` | `super123` |
| **Ejecutivo 1** | `carlos.comercial@agencia.cl` | `eje123` |
| **Ejecutivo 2** | `marcela.ventas@agencia.cl` | `eje123` |
| **Ejecutivo 3** | `felipe.convenios@agencia.cl` | `eje123` |

---

## Pruebas Unitarias y Cobertura de Código (Kover)

La solución cuenta con una suite de pruebas unitarias implementada con **JUnit 4**, **MockK** y **kotlinx-coroutines-test** sobre los módulos de `ViewModel`, `Repository` y `util` (validaciones de negocio), medida mediante el plugin **Kover**:

* **Comando de ejecución y reporte HTML:**
  ```bash
  ./gradlew :app:koverHtmlReportDebug
  ```
* **Resultados de cobertura alcanzados (superando el 80% exigido):**
  * **Cobertura de Líneas (`Line %`):** `95,79%`
  * **Cobertura de Ramas (`Branch %`):** `88,41%`
  * **Cobertura de Instrucciones (`Instruction %`):** `96,19%`
  * **Cobertura de Métodos (`Method %`):** `99,09%`

---

## Generación de APK Firmado (Modo Release)

El ejecutable instalable en modo `release` fue generado y firmado digitalmente desde Android Studio (**Build > Generate Signed App Bundle / APK... > APK**) utilizando un almacén de claves de demostración académica (`.jks`, sin contraseñas productivas):

* **Ubicación del APK firmado:** `entregable_apk/app-release.apk`
* **Ubicación del Keystore (`.jks`):** `entregable_apk/agenciaconnect-demo.jks`
* **Alias de demostración:** `agenciaconnect-key`

### Evidencia del Proceso de Firma y APK Generado
![Evidencia APK Firmado](docs/evidencia.docs)
