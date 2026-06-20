# 🔥 Valle del Sol — Plataforma Inteligente de Gestión de Incendios

Sistema distribuido basado en microservicios orientado a la gestión de incendios forestales y urbanos para municipalidades y organismos de emergencia.

La plataforma permite:

- Reportar focos de incendio en tiempo real.
- Visualizar incidentes en mapas interactivos.
- Administrar brigadas y zonas operacionales.
- Gestionar rutas de evacuación.
- Generar y clasificar alertas automáticas por nivel de emergencia.
- Despachar notificaciones segmentadas por rol (ciudadanos, brigadas, administradores).
- Centralizar la autenticación y validación JWT en el API Gateway.
- Orquestar servicios mediante API Gateway y BFF.
- Descubrir servicios dinámicamente mediante Eureka.
- Desplegar toda la arquitectura mediante Docker Compose.
- Coordinar recursos de emergencia en tiempo real.

---

# 📌 Tabla de Contenidos

- Arquitectura General
- Microservicios
- Tecnologías Utilizadas
- Arquitectura de Comunicación
- Service Discovery con Eureka
- Estructura del Proyecto
- Puertos del Sistema
- Variables de Entorno
- Bases de Datos
- Ejecución con Docker
- Flujo de Autenticación JWT
- Recuperación de Contraseña
- Endpoints Principales
- Patrones Arquitectónicos Aplicados
- Frontend
- Estado Actual del Proyecto
- Usuarios de Prueba
- Equipo de Desarrollo

---

# 🏗 Arquitectura General

La plataforma utiliza una arquitectura basada en microservicios, donde cada servicio posee responsabilidades claramente separadas y persistencia desacoplada. El descubrimiento dinámico de servicios se gestiona mediante un servidor Eureka.

```text
Frontend Web / Android
            │
            ▼
      API Gateway (:8000)
       [JWT Validation]
            │
            ▼
          BFF (:8001)
            │
 ┌──────────┼──────────┬──────────┬──────────┬──────────┬──────────┐
 ▼          ▼          ▼          ▼          ▼          ▼          ▼

Auth      User      Report    Alert     Notif.    Geo       Zone    Brigade
Service   Service   Service   Service   Service   Service   Service Service
(:8080)  (:8084)   (:8081)   (:8083)   (:8090)   (:8082)  (:8085)  (:8086)

                       │
              ┌────────┴────────┐
              ▼                 ▼
          Incident          Evidence
          Service           Service
          (:8087)           (:8088)

                                  │           ▲
                                  └───────────┘
                               Eureka Discovery

                        Eureka Server (:8761)
                     [alert-service registrado]
                   [notification-service registrado]
```

---

# 🧩 Microservicios

## 🔐 auth-service

Responsable de:

- Login.
- Generación de tokens JWT.
- Validación de credenciales.
- Integración REST con user-service.

---

## 👤 user-service

Responsable de:

- Registro de usuarios.
- Gestión de usuarios y roles.
- Recuperación de contraseña.
- Usuarios notificables.

### Funcionalidades

- Registro.
- Consulta de usuarios.
- Endpoint interno para autenticación.
- Recuperación de contraseña con código temporal.
- Envío de correos HTML mediante Brevo SMTP.

---

## 🚨 report-service

Responsable de:

- Gestión del núcleo del reporte de incendios (datos base, ubicación, usuario reportante).
- Coordinación con incident-service y evidence-service.
- Integración con alert-service y geo-service.

### Funcionalidades

- Crear, listar, consultar y eliminar reportes.
- Delegar el estado y la severidad del incidente a incident-service.
- Delegar los archivos multimedia a evidence-service.
- Integración con alert-service y geo-service.

originalmente report-service también manejaba los estados/severidad y los adjuntos multimedia. Esa responsabilidad se separó en dos microservicios dedicados (incident-service y evidence-service) para desacoplar. El reporte completo se reconstruye en el BFF mediante el endpoint  `/api/reportes/{id}/completo`.

---

## 🔥 incident-service

Responsable de:

- Estado operativo del incidente (ACTIVE / INACTIVE).
- Nivel de severidad del incidente.
- Vínculo con su reporte de origen mediante `reporteId`.

### Funcionalidades

- Listar incidentes y filtrar focos activos.
- Consultar incidente por su `id` o por `reporteId`.
- Actualizar el estado del incidente.
- Eliminar el incidente asociado a un reporte.

### Endpoints

```http
GET    /api/incidentes
POST   /api/incidentes
GET    /api/incidentes/focos-activos
GET    /api/incidentes/{id}
GET    /api/incidentes/reporte/{reporteId}
PATCH  /api/incidentes/{id}/estado
DELETE /api/incidentes/reporte/{reporteId}
```

---

## 📎 evidence-service

Responsable de:

- Almacenamiento de evidencias multimedia (foto/video) asociadas a un reporte.
- Persistencia binaria en `evidence_db` (LONGBLOB).
- Entrega de la multimedia como data-URL en base64.

### Funcionalidades

- Subir una o varias evidencias a un reporte (multipart).
- Listar las evidencias de un reporte.
- Contar evidencias por reporte.
- Eliminar las evidencias de un reporte.

### Endpoints

```http
GET    /api/evidencias/{reporteId}
POST   /api/evidencias/{reporteId}
GET    /api/evidencias/{reporteId}/count
DELETE /api/evidencias/{reporteId}
```

---

## 🌍 geo-service

Responsable de:

- Consolidación de datos georreferenciados.
- Reportes mapeados.
- Integración con zone-service.
- Integración con brigade-service.
- Construcción de respuestas MapData para el frontend.

### Estado actual

- Funcionalidad operativa.
- Pendiente optimización y consolidación final del modelo cartográfico.

---

## 🗺 zone-service

Responsable de:

- CRUD completo de zonas.
- Soporte GeoJSON para polígonos.
- Zona principal (MAIN) única.
- Zonas operacionales (OPERATIONAL).
- Validación geoespacial de pertenencia.
- CRUD completo de rutas de evacuación.
- Validación de rutas dentro de zonas operacionales.
- Soft delete.

```http
GET    /api/zones/main
GET    /api/zones/operational
GET    /api/zones/active
GET    /api/zones/{id}
POST   /api/zones
PUT    /api/zones/{id}
DELETE /api/zones/{id}
```
```http
GET    /api/evacuation-routes
GET    /api/evacuation-routes/{id}
GET    /api/evacuation-routes/zone/{zoneId}
POST   /api/evacuation-routes
PUT    /api/evacuation-routes/{id}
DELETE /api/evacuation-routes/{id}
```

---

## 🚒 brigade-service

Responsable de:

- CRUD completo de brigadas.
- Asociación a zonas operacionales.
- Validación geoespacial de ubicación.
- Impide asignar brigadas fuera de una zona.
- Impide asignar brigadas a zonas MAIN.
- Soft delete.
- Estado operativo de brigadas.

```http
GET    /api/brigades
GET    /api/brigades/{id}
POST   /api/brigades
PUT    /api/brigades/{id}
DELETE /api/brigades/{id}
```

---

## 🚨 alert-service  *(nuevo)*

Responsable de:

- Generación de alertas.
- Alertas críticas de zona.
- Alertas automáticas desde reportes.
- Clasificación de emergencias.

### Funcionalidades

- **ClasificadorEmergencia** — deriva automáticamente el nivel (CRITICO / ALTO / MEDIO / BAJO) y el público objetivo (TODOS / BRIGADAS / ADMINISTRADORES / BRIGADAS_Y_ADMINISTRADORES) a partir del tipo y severidad de la alerta.
- **AlertaCriticaService** — emite alertas de zona crítica y emergencia máxima con prioridad máxima y ambos canales activos.
- **AlertaAutomaticaService** — genera alertas sin intervención del operador cuando report-service crea un nuevo reporte.
- Persiste cada alerta en `alert_db` con trazabilidad mediante `despachoId`.
- Delega el despacho de notificaciones a `notification-service` vía Eureka.

### Tipos de alerta

| Tipo | Nivel | Destinatarios |
|---|---|---|
| INCENDIO | ALTO | Brigadas + Admins |
| ZONA_CRITICA | CRITICO | Brigadas + Admins |
| AUTOMATICA | MEDIO | Administradores |
| MANUAL | BAJO | Todos |
| SISTEMA | BAJO | Administradores |

### Endpoints

```http
POST /api/alertas/enviar           — alerta general / manual
POST /api/alertas/critica          — alerta de zona crítica
POST /api/alertas/critica/maxima   — emergencia máxima
POST /api/alertas/automatica       — auto-alerta desde reporte
POST /api/alertas/sistema          — aviso de sistema
GET  /api/alertas                  — historial de alertas
GET  /api/alertas/conteo           — total de alertas
GET  /api/alertas/nivel/{nivel}    — filtrar por nivel de emergencia
```

---

## 🔔 notification-service  *(nuevo)*

Responsable de:

- Correos electrónicos.
- Notificaciones push.
- Avisos específicos a brigadas.
- Avisos específicos a administradores.
- Comunicación del sistema.

### Funcionalidades

- **NotificacionStrategy** — enruta cada notificación según el rol del usuario y los canales activos:
  - `BRIGADA` → `GeneradorBrigada` (formato operacional de despacho, ambos canales).
  - `ADMIN` → `GeneradorAdmin` (formato administrativo, ambos canales).
  - `CIUDADANO` → `GeneradorEmail` y/o `GeneradorPush` según los flags del evento.
- Filtra la audiencia por `destinatarios` (TODOS / BRIGADAS / ADMINISTRADORES / BRIGADAS_Y_ADMINISTRADORES).
- Persiste una `Notificacion` por cada (usuario × generador) en `notification_db`.
- Expone el historial agrupado por despacho al BFF.

### Generadores disponibles

| Generador | Formato | Canal por defecto |
|---|---|---|
| GeneradorEmail | `Asunto: [Municipalidad Valle del Sol] - {mensaje}` | Email |
| GeneradorPush | `[🔔 ALERTA] {mensaje}` | Push |
| GeneradorBrigada | `🚒 BRIGADAS — DESPACHO INMEDIATO` | Ambos |
| GeneradorAdmin | `📋 [DIRECCIÓN EJECUTIVA]` | Ambos |

### Endpoints

```http
POST /api/notificaciones/enviar          — procesar evento de alerta (interno)
GET  /api/notificaciones                 — historial (uno por despacho)
GET  /api/notificaciones/conteo          — total de notificaciones
DEL  /api/notificaciones/{id}            — eliminar grupo de despacho
GET  /api/notificaciones/brigadas        — notificaciones a brigadas
GET  /api/notificaciones/administradores — notificaciones a admins
GET  /api/notificaciones/nivel/{nivel}   — filtrar por nivel de emergencia
GET  /api/notificaciones/test-auth       — debug: verificar conexión auth-service
```

---

## 🧠 bff-service

Backend For Frontend encargado de consolidar información para el cliente.

### Funcionalidades

- Dashboard centralizado con datos en tiempo real.
- Consolidación MapData (zonas, brigadas, reportes georreferenciados).
- Reconstrucción del reporte completo (`/api/reportes/{id}/completo`): combina el reporte base, 
  su incidente (estado/severidad) y sus evidencias en una sola respuesta.
- Proxy hacia incident-service (`/api/incidentes`) y evidence-service (`/api/evidencias`).
- Descubrimiento de `alert-service` y `notification-service` vía Eureka.
- Enrutamiento transparente de alertas e historial de notificaciones.

### Ejemplo MapData

```text
GET /api/map/data
→ consulta zone-service
→ consulta brigade-service
→ consulta geo-service
→ retorna respuesta unificada
```

---

## 🌐 eureka-server  *(nuevo)*

Registro centralizado de servicios basado en Spring Cloud Netflix Eureka.

### Funcionalidades

- Registro automático de `alert-service` y `notification-service`.
- Descubrimiento por nombre lógico desde el BFF y entre servicios.
- Dashboard de monitoreo en `http://localhost:8761`.

### Servicios registrados

| Nombre lógico | Servicio |
|---|---|
| `alert-service` | alert-service (:8083) |
| `notification-service` | notification-service (:8090) |

---

## 🚪 api-gateway

Punto único de entrada del sistema con validación JWT integrada.

### Funcionalidades

- **Validación JWT** — verifica tokens HMAC-SHA256 localmente (sin llamadas a auth-service).
- Rutas públicas sin token: `/auth/**`, `/api/users/register`, `/api/auth/password/**`, `OPTIONS`.
- Cualquier otra ruta requiere `Authorization: Bearer <token>` válido.
- CORS global (acepta web, Capacitor Android, emulador AVD).
- Enrutamiento de todo el tráfico al BFF, incluidas las rutas `/api/incidentes/**` y `/api/evidencias/**`.

---

# ⚙ Tecnologías Utilizadas

| Tecnología | Uso |
|------------|-----|
| Java 17 / 21 | Backend |
| Spring Boot 3 / 4 | Microservicios |
| Spring Security 7 | Seguridad y JWT |
| Spring Cloud Gateway | API Gateway reactivo |
| Spring Cloud Netflix Eureka | Service Discovery |
| Spring Cloud OpenFeign | Clientes HTTP declarativos |
| JWT (HMAC-SHA256) | Autenticación stateless |
| Spring Data JPA | Persistencia |
| Hibernate | ORM |
| RestTemplate | Comunicación entre servicios |
| MySQL 8 | Base de datos |
| Docker | Contenedores |
| Docker Compose | Orquestación |
| Angular 17 | Frontend |
| Ionic 7 | Frontend móvil |
| Capacitor | APK Android |
| Leaflet | Mapas |
| Nginx | Hosting frontend |
| Git / GitHub | Versionamiento |
| Brevo SMTP | Correos transaccionales |
| Jakarta Mail | Envío de correos HTML |

---

# 🔄 Arquitectura de Comunicación

## Flujo General

```text
Frontend
→ API Gateway  [valida JWT]
→ BFF
→ Microservicios
```

## Alerta desde Reporte

```text
report-service crea reporte
→ POST /api/alertas/enviar  (alert-service)
→ ClasificadorEmergencia determina nivel y audiencia
→ Persiste Alerta en alert_db
→ POST /api/notificaciones/enviar  (notification-service via Eureka)
→ NotificacionStrategy filtra usuarios y selecciona generadores
→ Persiste Notificacion por usuario en notification_db
```

## Alerta Manual desde Dashboard

```text
Operador selecciona canal y destinatarios
→ BFF → alert-service
→ alert-service → notification-service (Eureka)
→ Notificaciones despachadas por rol
```

## Login

```text
Frontend → Gateway → BFF → Auth Service → User Service → JWT
```

## Registro

```text
Frontend → Gateway → BFF → User Service → MySQL
```

## Mapa Consolidado

```text
Frontend → Gateway → BFF → Zone Service + Brigade Service + Geo Service
```

---

# 🔍 Service Discovery con Eureka

`alert-service` y `notification-service` se registran automáticamente en Eureka al iniciar.

El BFF y `alert-service` los descubren por nombre lógico sin necesidad de IPs ni puertos hardcodeados.

```text
alert-service (registrado como "alert-service")
    └→ http://notification-service/api/notificaciones/enviar
         ↑ Eureka resuelve "notification-service" → notification-app:8090

BFF (Feign + Eureka)
    ├→ @FeignClient(name = "alert-service")       → alertas-app:8083
    └→ @FeignClient(name = "notification-service") → notification-app:8090
```

Dashboard Eureka: `http://localhost:8761`

---

# 📁 Estructura del Proyecto

```text
valledelsol-platform/
│
├── api-gateway/            — Gateway + JWT validation
├── eureka-server/          — Service Registry
├── auth-service/           — Autenticación JWT
├── user-service/           — Usuarios y roles
├── bff-service/            — Backend For Frontend
├── report-service/         — Núcleo de reportes de incendios
├── incident-service/       — Estado y severidad de incidentes
├── evidence-service/       — Evidencias multimedia
├── alert-service/          — Generación y clasificación de alertas
├── notification-service/   — Despacho de notificaciones por canal y rol
├── geo-service/            — Reportes georreferenciados
├── zone-service/           — Zonas operativas y evacuación
├── brigade-service/        — Brigadas de emergencia
│
├── frontend-web/           — App web Ionic/Angular
├── frontend-android/       — App Android (Capacitor APK)
│
├── valledelsol-app/        — Código fuente web
├── valledelsol-android/    — Código fuente Android
│
├── docker-compose.yml
├── init-db.sql
└── README.md
```

---

# 🌐 Puertos del Sistema

| Servicio | Puerto | Descripción |
|---|---|---|
| API Gateway | 8000 | Única puerta de entrada |
| BFF | 8001 | Orquestador |
| Auth Service | 8080 | Autenticación JWT |
| Report Service | 8081 | Reportes de incendios |
| Incident Service | 8087 | Estado y severidad de incidentes |
| Evidence Service | 8088 | Evidencias multimedia |
| Geo Service | 8082 | Datos georreferenciados |
| Alert Service | 8083 | Generación de alertas |
| User Service | 8084 | Gestión de usuarios |
| Zone Service | 8085 | Zonas operativas |
| Brigade Service | 8086 | Brigadas |
| Notification Service | 8090 | Despacho de notificaciones |
| Eureka Server | 8761 | Service Registry |
| MySQL | 3306 | Base de datos |
| Frontend Web | 8100 | Aplicación web |
| Frontend Android | 8101 | Vista web móvil |

---

# 🔑 Variables de Entorno

```properties
# Base de datos
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD

# JWT (compartido entre auth-service y api-gateway)
JWT_SECRET_KEY

# Eureka (alert-service, notification-service, bff-service)
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE

# URLs directas (servicios sin Eureka)
MS_AUTH_URL
MS_REPORTES_URL
MS_GEO_URL
MS_USER_URL
MS_ZONE_URL
MS_BRIGADE_URL

# Auth-service interno
AUTH_SERVICE_URL

# Correo
BREVO_SMTP_USER
BREVO_SMTP_KEY
```

---

# 🗄 Base de Datos

| Base de Datos | Servicio | Tablas principales |
|---|---|---|
| auth_vallesol_db | auth-service | — |
| user_db | user-service | users, password_reset_codes |
| report_db | report-service | reportes |
| incident_db | incident-service | incidentes |
| evidence_db | evidence-service | evidencias |
| geo_db | geo-service | mapped_reports |
| zone_db | zone-service | zones, evacuation_routes |
| brigade_db | brigade-service | brigades |
| alert_db | alert-service | alertas |
| notification_db | notification-service | notificaciones |

---

# 🐳 Ejecución con Docker

## Levantar entorno completo

```bash
docker compose up --build
```

## Detener entorno

```bash
docker compose down
```

## Orden de arranque

```text
mysql-db → eureka-server → alert-service + notification-service → bff-app → gateway-app → frontends
```

## Reconstruir servicios individuales

```bash
docker compose build eureka-server --no-cache
docker compose build alertas-app --no-cache
docker compose build notification-app --no-cache
docker compose build bff-app --no-cache
docker compose build gateway-app --no-cache
docker compose build auth-app --no-cache
docker compose build report-app --no-cache
```

---

# 🔐 Flujo de Autenticación JWT

## Login

```http
POST /auth/login
Content-Type: application/json

{
  "email": "admin@valledelsol.cl",
  "password": "Admin123*"
}
```

### Respuesta

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer"
}
```

### Uso en peticiones protegidas

```http
Authorization: Bearer <token>
```

## Validación en el Gateway

El API Gateway valida el JWT localmente usando el secreto HMAC-SHA256 compartido con auth-service. No se realiza ninguna llamada de red para validar — la verificación es instantánea.

```text
Request → Gateway
    ├── Ruta pública → pasa directo
    └── Ruta protegida → verifica firma JWT
            ✅ válido  → reenvía con header Authorization al BFF
            ❌ inválido → 401 Unauthorized
```

---

# 🔑 Recuperación de Contraseña

## Flujo

```text
POST /api/auth/password/forgot  → user-service → Brevo SMTP → correo al usuario
POST /api/auth/password/reset   → user-service valida código → actualiza contraseña
```

## Endpoints

```http
POST /api/auth/password/forgot
Body: { "email": "usuario@correo.com" }

POST /api/auth/password/reset
Body: { "email": "usuario@correo.com", "code": "123456", "newPassword": "NuevaPass123*" }
```

---

# 📡 Endpoints Principales

## Autenticación

```http
POST /auth/login
POST /api/users/register
POST /api/auth/password/forgot
POST /api/auth/password/reset
```

## Alertas

```http
POST /api/alertas/enviar
POST /api/alertas/critica
POST /api/alertas/critica/maxima
POST /api/alertas/automatica
POST /api/alertas/sistema
GET  /api/alertas
GET  /api/alertas/nivel/{nivel}
```

## Notificaciones (expuestas por el BFF como /api/alertas)

```http
GET  /api/alertas                  — historial de despachos
DEL  /api/alertas/{id}             — eliminar despacho
GET  /api/dashboard/stats          — totalIncendios, alertasEmitidas, brigadasActivas
```

## Reportes

```http
GET    /api/reportes
POST   /api/reportes
GET    /api/reportes/{id}
DELETE /api/reportes/{id}
GET    /api/reportes/{id}/completo
```

## Incidentes

```http
GET    /api/incidentes
POST   /api/incidentes
GET    /api/incidentes/focos-activos
GET    /api/incidentes/{id}
GET    /api/incidentes/reporte/{reporteId}
PATCH  /api/incidentes/{id}/estado
DELETE /api/incidentes/reporte/{reporteId}
```

## Evidencias

```http
GET    /api/evidencias/{reporteId}
POST   /api/evidencias/{reporteId}
GET    /api/evidencias/{reporteId}/count
DELETE /api/evidencias/{reporteId}
```

## Zonas y Brigadas

```http
GET  /api/zones
POST /api/zones
GET  /api/brigades
POST /api/brigades
```

## Mapa Consolidado

```http
GET /api/map/data
```

---

# 🧱 Patrones Arquitectónicos Aplicados

## Repository Pattern
Persistencia desacoplada mediante JpaRepository.

## Database per Service
Cada microservicio mantiene su propio esquema. 10 bases de datos independientes.

## Backend For Frontend (BFF)
Optimización y consolidación de respuestas para el frontend.

## API Gateway Pattern
Punto único de entrada con validación JWT integrada.

## Service Discovery (Eureka)
Registro y descubrimiento dinámico de servicios. alert-service y notification-service se registran en Eureka; el BFF los descubre por nombre lógico.

## Strategy Pattern
`NotificacionStrategy` selecciona el generador correcto por rol de usuario y canales activos.

## Factory Pattern
`AlertaFactory` en notification-service y `ClasificadorEmergencia` en alert-service.

## Microservices Architecture
Separación de dominios funcionales independientes con responsabilidades claras.

---

# 📱 Frontend

Desarrollado con Ionic 7 + Angular 17.

### Versión Web (`:8100`)
- Login y registro.
- Dashboard administrativo con estadísticas en tiempo real.
- Creación y gestión de reportes con adjuntos multimedia.
- Mapa interactivo con zonas, brigadas e incidentes.
- Gestión de alertas con selección de canal y destinatarios.
- Historial operativo de notificaciones.

### Versión Android (APK Capacitor)
- Mismas funcionalidades adaptadas para móvil.
- Cámara nativa mediante `@capacitor/camera`.
- Subida de fotos y videos a reportes.

---

# 👥 Usuarios de Prueba

| Rol | Email | Contraseña |
|---|---|---|
| ADMIN | admin@valledelsol.cl | Admin123* |
| USER | w.vinet.h@gmail.com | User123* |

---

# 🚧 Estado Actual del Proyecto

## ✅ Implementado

- Arquitectura de microservicios completa.
- API Gateway con validación JWT (HMAC-SHA256).
- BFF con orquestación y consolidación de datos.
- Eureka Server para service discovery.
- alert-service: generación, clasificación y tipos de alerta.
- notification-service: correo, push, brigadas, admins, sistema.
- Comunicación alert-service → notification-service vía Eureka.
- Descubrimiento BFF → alert-service y notification-service vía Feign + Eureka.
- Dashboard centralizado (totalIncendios, alertasEmitidas, brigadasActivas).
- Separación auth-service ↔ user-service.
- Separación geo-service ↔ zone-service ↔ brigade-service.
- Reportes multimedia (foto/video).
- Migración de report-service en tres microservicios: report (núcleo), incident (estado/severidad) y evidence (multimedia), expuestos por el BFF y ruteados por el Gateway.
- Endpoint agregador de reporte completo en el BFF (`/api/reportes/{id}/completo`).
- Gestión de zonas operativas y rutas de evacuación.
- Gestión de brigadas y estado operativo.
- Recuperación de contraseña con Brevo SMTP.
- Correos HTML personalizados.
- Usuarios semilla para demostración.
- Frontend web y Android (APK Capacitor).
- Docker Compose con orden de arranque correcto.
- CRUD completo de zonas.
- CRUD completo de rutas de evacuación.
- CRUD completo de brigadas.
- Validaciones geoespaciales mediante JTS.
- Separación geo-service / zone-service / brigade-service.
- Integración BFF ↔ Zone Service.
- Integración BFF ↔ Brigade Service.

## 🚧 En Desarrollo

- Envío real de correos desde notification-service (actualmente simula el despacho).
- Integración con Firebase Cloud Messaging para push real.
- Asignación automática de brigadas a zonas críticas.
- Dashboard táctico avanzado con timeline de eventos.
- Observabilidad y métricas operacionales.
- Escalamiento horizontal de servicios críticos.
- Registro dinámico completo mediante Eureka.
- Autorización por roles en API Gateway.
- Administración de zonas desde interfaz gráfica.
- Creación interactiva de polígonos sobre mapa.
- Dashboard táctico avanzado.
- Asignación automática de brigadas.

---

# 👨‍💻 Equipo de Desarrollo

- Felipe Bravo
- Valentina Pino
- Wilfred Vinet

---

# 📚 Referencia Académica

```text
Desarrollo FullStack III — DSY1106
Duoc UC
```
