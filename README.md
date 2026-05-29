# 🔥 Valle del Sol — Plataforma Inteligente de Gestión de Incendios

Sistema distribuido basado en microservicios orientado a la gestión de incendios forestales y urbanos para municipalidades y organismos de emergencia.

La plataforma permite:

- Reportar focos de incendio en tiempo real.
- Visualizar incidentes en mapas interactivos.
- Administrar brigadas y zonas operacionales.
- Gestionar rutas de evacuación.
- Generar alertas automáticas.
- Centralizar la autenticación mediante JWT.
- Orquestar servicios mediante API Gateway y BFF.
- Desplegar toda la arquitectura mediante Docker Compose.
- Coordinar recursos de emergencia en tiempo real.

---

# 📌 Tabla de Contenidos

- Arquitectura General
- Microservicios
- Tecnologías Utilizadas
- Arquitectura de Comunicación
- Estructura del Proyecto
- Puertos del Sistema
- Variables de Entorno
- Ejecución con Docker
- Flujo de Autenticación JWT
- Patrones Arquitectónicos Aplicados
- Frontend
- Base de Datos
- Endpoints Principales
- Escalabilidad
- Estado Actual del Proyecto
- Equipo de Desarrollo

---

# 🏗 Arquitectura General

La plataforma utiliza una arquitectura basada en microservicios, donde cada servicio posee responsabilidades claramente separadas y persistencia desacoplada.

```text
Frontend Web / Android
            │
            ▼
      API Gateway
            │
            ▼
             BFF
            │
 ┌──────────┼──────────┬──────────┬──────────┬──────────┬──────────┐
 ▼          ▼          ▼          ▼          ▼          ▼          ▼

Auth      User      Report     Alert      Geo       Zone     Brigade
Service   Service   Service    Service    Service   Service  Service
```

---

# 🧩 Microservicios

## 🔐 auth-service

Responsable de:

- Login.
- Validación JWT.
- Seguridad.
- Autenticación.

### Funcionalidades

- Generación de tokens JWT.
- Validación de credenciales.
- Protección de endpoints.
- Integración REST con user-service.

---

## 👤 user-service

Responsable de:

- Registro de usuarios.
- Gestión de usuarios.
- Roles.
- Estados de usuario.

### Funcionalidades

- Registro.
- Consulta de usuarios.
- Usuarios notificables.
- Endpoint interno para autenticación.

---

## 🚨 report-service

Responsable de:

- Gestión de reportes de incendios.
- Historial de incidentes.
- Estados de reportes.

### Funcionalidades

- Crear reportes.
- Listar reportes.
- Actualizar estados.
- Estadísticas operacionales.
- Integración con alertas.

---

## 🌍 geo-service

Responsable de:

- Reportes georreferenciados.
- Coordenadas.
- Información para visualización cartográfica.

### Funcionalidades

- Gestión de Mapped Reports.
- Integración con report-service.
- Información geográfica para mapas.

---

## 🗺 zone-service

Responsable de:

- Administración de zonas operativas.
- Gestión de polígonos GeoJSON.
- Rutas de evacuación.

### Funcionalidades

- Creación de zonas MAIN.
- Creación de zonas OPERATIONAL.
- Validación espacial.
- Administración de rutas de evacuación.
- Relaciones internas con EvacuationRoute.

---

## 🚒 brigade-service

Responsable de:

- Administración de brigadas.
- Asignación territorial.

### Funcionalidades

- Crear brigadas.
- Actualizar estado operativo.
- Asociar brigadas a zonas.
- Control operacional de recursos.

---

## 📢 alert-service

Responsable de:

- Alertas automáticas.
- Notificaciones.
- Registro histórico.

### Funcionalidades

- Generación de alertas.
- Asociación a reportes.
- Historial de eventos.

---

## 🧠 bff-service

Backend For Frontend encargado de consolidar información para el cliente.

### Funcionalidades

- Dashboard centralizado.
- Consolidación de datos.
- MapData.
- Orquestación de llamadas.

### Ejemplo

```text
MapData
→ consulta zone-service
→ consulta brigade-service
→ consulta geo-service
→ retorna respuesta unificada
```

---

## 🚪 api-gateway

Punto único de entrada del sistema.

### Funcionalidades

- Routing.
- CORS.
- Seguridad.
- Integración con BFF.
- Escalabilidad futura.

---

# ⚙ Tecnologías Utilizadas

| Tecnología | Uso |
|------------|-----|
| Java 21 | Backend |
| Spring Boot 3 | Microservicios |
| Spring Security | Seguridad |
| JWT | Autenticación |
| Spring Data JPA | Persistencia |
| Hibernate | ORM |
| RestClient | Comunicación entre servicios |
| MySQL 8 | Base de datos |
| Docker | Contenedores |
| Docker Compose | Orquestación |
| Angular 17 | Frontend |
| Ionic 7 | Frontend móvil |
| Leaflet | Mapas |
| Nginx | Hosting frontend |
| Git/GitHub | Versionamiento |

---

# 🔄 Arquitectura de Comunicación

## Flujo General

```text
Frontend
→ API Gateway
→ BFF
→ Microservicios
```

## Login

```text
Frontend
→ Gateway
→ BFF
→ Auth Service
→ User Service
→ JWT
→ Respuesta
```

## Registro

```text
Frontend
→ Gateway
→ BFF
→ User Service
→ MySQL
```

## Mapa

```text
Frontend
→ Gateway
→ BFF
→ Zone Service
→ Brigade Service
→ Geo Service
→ Respuesta consolidada
```

---

# 📁 Estructura del Proyecto

```text
valledelsol-platform/
│
├── api-gateway/
├── auth-service/
├── bff-service/
├── brigade-service/
├── geo-service/
├── report-service/
├── alert-service/
├── user-service/
├── zone-service/
│
├── frontend-web/
├── frontend-android/
│
├── docker-compose.yml
├── init-db.sql
└── README.md
```

---

# 🌐 Puertos del Sistema

| Servicio | Puerto |
|-----------|---------|
| API Gateway | 8000 |
| BFF | 8001 |
| Auth Service | 8080 |
| Report Service | 8081 |
| Geo Service | 8082 |
| Alert Service | 8083 |
| User Service | 8084 |
| Zone Service | 8085 |
| Brigade Service | 8086 |
| MySQL | 3307 |
| Frontend Web | 8100 |
| Frontend Android | 8101 |

---

# 🔑 Variables de Entorno

```properties
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD

MS_AUTH_URL
MS_USER_URL
MS_REPORTES_URL
MS_ALERTAS_URL
MS_GEO_URL
MS_ZONE_URL
MS_BRIGADE_URL
```

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

## Reconstruir servicios individuales

```bash
docker compose up --build auth-app
docker compose up --build user-app
docker compose up --build report-app
docker compose up --build geo-app
docker compose up --build zone-app
docker compose up --build brigade-app
docker compose up --build alertas-app
docker compose up --build bff-app
docker compose up --build gateway-app
```

---

# 🔐 Flujo JWT

## Login

```http
POST /api/auth/login
```

### Respuesta

```json
{
  "token": "jwt-token",
  "type": "Bearer"
}
```

### Uso

```http
Authorization: Bearer <token>
```

---

# 🧱 Patrones Arquitectónicos Aplicados

## Repository Pattern

Persistencia desacoplada mediante JpaRepository.

## Database per Service

Cada microservicio mantiene su propio esquema de persistencia.

## Backend For Frontend (BFF)

Optimización de respuestas para Frontend.

## API Gateway Pattern

Punto único de entrada para el sistema.

## Microservices Architecture

Separación de dominios funcionales independientes.

---

# 📱 Frontend

Desarrollado con:

- Ionic Framework 7.
- Angular 17.

### Funcionalidades

- Login.
- Registro.
- Creación de reportes.
- Visualización de incendios.
- Mapa interactivo.
- Dashboard administrativo.
- Administración de brigadas.
- Administración de alertas.
- Visualización de zonas operativas.

---

# 🗄 Base de Datos

## Esquemas

| Base de Datos | Servicio |
|---------------|-----------|
| auth_vallesol_db | auth-service |
| user_db | user-service |
| report_db | report-service |
| geo_db | geo-service |
| zone_db | zone-service |
| brigade_db | brigade-service |
| alert_db | alert-service |

---

# 📡 Endpoints Principales

## Auth

```http
POST /api/auth/login
POST /api/auth/register
```

## Usuarios

```http
GET /api/users
GET /api/users/notificables
GET /api/users/internal
```

## Reportes

```http
GET /api/reportes
POST /api/reportes
```

## Geo

```http
GET /api/geo/reports/mapped
```

## Zonas

```http
GET /api/zones
POST /api/zones
```

## Brigadas

```http
GET /api/brigades
POST /api/brigades
```

## Dashboard

```http
GET /api/dashboard/stats
```

## Mapa Consolidado

```http
GET /api/map/data
```

---

# 📈 Escalabilidad

La arquitectura permite:

- Escalado independiente por servicio.
- Tolerancia parcial a fallos.
- Despliegues independientes.
- Mantenibilidad.
- Separación de dominios.
- Evolución independiente de cada componente.
- Incorporación de nuevos microservicios sin afectar los existentes.

---

# 🚧 Estado Actual del Proyecto

## ✅ Implementado

- Arquitectura de microservicios.
- API Gateway.
- BFF.
- JWT Authentication.
- Docker Compose.
- Frontend Ionic/Angular.
- Comunicación REST entre microservicios.
- Separación auth-service ↔ user-service.
- Separación geo-service ↔ zone-service ↔ brigade-service.
- Dashboard centralizado.
- Consolidación MapData.
- Gestión de zonas operativas.
- Gestión de brigadas.
- Gestión de rutas de evacuación.

## 🚧 En Desarrollo

- Asignación automática de brigadas.
- Alertas avanzadas.
- Dashboard táctico avanzado.
- Observabilidad.
- Métricas operacionales.
- Escalamiento horizontal.
- Integración con servicios externos de emergencia.

---

# 👨‍💻 Equipo de Desarrollo

- Felipe Bravo
- Valentina Pino Norambuena
- Wilfred Vinet

---

# 📚 Referencia Académica

Proyecto desarrollado para:

```text
Desarrollo FullStack III — DSY1106
Duoc UC
```