# 🔥 Valle del Sol — Plataforma Inteligente de Gestión de Incendios

Sistema distribuido basado en microservicios orientado a la gestión de incendios forestales y urbanos para municipalidades y organismos de emergencia.

La plataforma permite:

- Reportar focos de incendio en tiempo real
- Visualizar incidentes en mapas interactivos
- Administrar brigadas y zonas operacionales
- Generar alertas automáticas
- Centralizar la autenticación mediante JWT
- Orquestar servicios mediante API Gateway y BFF
- Desplegar toda la arquitectura mediante Docker Compose

---

# 📌 Tabla de Contenidos

- [Arquitectura General](#-arquitectura-general)
- [Microservicios](#-microservicios)
- [Tecnologías Utilizadas](#-tecnologías-utilizadas)
- [Arquitectura de Comunicación](#-arquitectura-de-comunicación)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Puertos del Sistema](#-puertos-del-sistema)
- [Variables de Entorno](#-variables-de-entorno)
- [Ejecución con Docker](#-ejecución-con-docker)
- [Flujo de Autenticación JWT](#-flujo-de-autenticación-jwt)
- [Patrones Arquitectónicos Aplicados](#-patrones-arquitectónicos-aplicados)
- [Frontend](#-frontend)
- [Base de Datos](#-base-de-datos)
- [Endpoints Principales](#-endpoints-principales)
- [Escalabilidad](#-escalabilidad)
- [Estado Actual del Proyecto](#-estado-actual-del-proyecto)
- [Equipo de Desarrollo](#-equipo-de-desarrollo)

---

# 🏗 Arquitectura General

La plataforma utiliza una arquitectura basada en microservicios, donde cada servicio posee responsabilidades claramente separadas y su propia base de datos.

```text
Frontend (Angular/Ionic)
        │
        ▼
API Gateway (Spring Cloud Gateway)
        │
        ▼
BFF - Backend For Frontend
        │
 ┌──────┼───────────────┬──────────────┬──────────────┐
 ▼      ▼               ▼              ▼              ▼
Auth  User           Report         Geo           Alert
Svc   Svc            Svc            Svc            Svc
```

---

# 🧩 Microservicios

## 🔐 auth-service

Responsable de:

- Login
- Validación JWT
- Seguridad
- Autenticación

### Funcionalidades

- Generación de tokens JWT
- Validación de credenciales
- Protección de endpoints
- Integración REST con `user-service`

---

## 👤 user-service

Responsable de:

- Registro de usuarios
- Gestión de usuarios
- Estados de usuario
- Roles

### Funcionalidades

- Registro
- Consulta de usuarios
- Usuarios notificables
- Endpoint interno para autenticación

---

## 🚨 report-service

Responsable de:

- Gestión de reportes de incendio
- Historial de incidentes
- Estados de reportes

### Funcionalidades

- Crear reportes
- Listar reportes
- Actualizar estado
- Relación geográfica con zonas

---

## 🌍 geo-service

Responsable de:

- Coordenadas
- Reportes geolocalizados
- Lógica geográfica base

---

## 🗺 zone-service

Responsable de:

- Zonas MAIN y OPERATIONAL
- Polígonos GeoJSON
- Rutas de evacuación
- Validación espacial

---

## 🚒 brigade-service

Responsable de:

- Brigadas
- Asignación operacional
- Estado de brigadas
- Relación brigada ↔ zona

---

## 📢 alert-service

Responsable de:

- Alertas automáticas
- Notificaciones
- Registro histórico
- Mensajería de emergencia

---

## 🧠 BFF - Backend For Frontend

Responsable de:

- Orquestación de respuestas
- Comunicación optimizada con frontend
- Consolidación de información

### Ejemplo

```text
Dashboard
→ consulta report-service
→ consulta geo-service
→ consulta alert-service
→ retorna respuesta unificada
```

---

## 🚪 API Gateway

Punto único de entrada del sistema.

Responsable de:

- Routing
- CORS
- Seguridad
- Validación JWT
- Logging

---

# ⚙ Tecnologías Utilizadas

| Tecnología | Uso |
|---|---|
| Java 21 | Backend |
| Spring Boot 3 | Microservicios |
| Spring Cloud Gateway | API Gateway |
| Spring Security | Seguridad |
| JWT | Autenticación |
| Spring Data JPA | Persistencia |
| Hibernate | ORM |
| MySQL 8 | Base de datos |
| Docker | Contenedores |
| Docker Compose | Orquestación |
| Angular 17 | Frontend |
| Ionic 7 | Frontend móvil/web |
| Leaflet.js | Mapas |
| Nginx | Hosting frontend |
| Git/GitHub | Versionamiento |

---

# 🔄 Arquitectura de Comunicación

## Flujo General

```text
Frontend
→ API Gateway
→ BFF
→ Microservicios internos
```

## Ejemplo Login

```text
Frontend
→ Gateway
→ BFF
→ auth-service
→ user-service
→ JWT
→ respuesta
```

## Ejemplo Registro

```text
Frontend
→ Gateway
→ BFF
→ user-service
→ MySQL
```

---

# 📁 Estructura del Proyecto

```text
ms-alertas-valledelsol-main/
│
├── authservice-valleSol/
├── userservice/
├── reportservice/
├── geoservice/
├── zoneservice/
├── brigadeservice/
├── alertservice/
├── BFF-ValleSol/
├── gatewayservice/
├── frontend/
├── android-app/
├── docker-compose.yml
└── README.md
```

---

# 🌐 Puertos del Sistema

| Servicio | Puerto |
|---|---|
| API Gateway | 8000 |
| BFF | 8001 |
| report-service | 8081 |
| geo-service | 8082 |
| auth-service | 8080 |
| user-service | 8084 |
| zone-service | 8085 |
| brigade-service | 8086 |
| MySQL | 3307 |
| Frontend | 8100 |
| Android Frontend | 8101 |

---

# 🔑 Variables de Entorno

## Ejemplo

```properties
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD

MS_AUTH_URL
MS_USER_URL
MS_REPORTES_URL
MS_GEO_URL
MS_ALERTAS_URL
```

---

# 🐳 Ejecución con Docker

## Construcción

```bash
docker compose up --build
```

## Levantar servicios específicos

```bash
docker compose up --build auth-app
```

```bash
docker compose up --build user-app
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

## Uso del Token

```http
Authorization: Bearer <token>
```

---

# 🧱 Patrones Arquitectónicos Aplicados

## Repository Pattern

Separación de lógica de persistencia mediante `JpaRepository`.

---

## Database per Service

Cada microservicio posee:

- Base de datos independiente
- Persistencia independiente
- Escalabilidad independiente

---

## Factory Method

Utilizado para:

- Tipos de alerta
- Tipos de incendio

---

## Backend For Frontend (BFF)

Optimización de respuestas hacia frontend.

---

# 📱 Frontend

Frontend desarrollado con:

- Ionic Framework 7
- Angular 17

## Funcionalidades

- Login
- Registro
- Reporte de incendios
- Mapa interactivo
- Dashboard administrativo
- Visualización de brigadas
- Alertas activas

---

# 🗄 Base de Datos

## Esquemas

| Base de Datos | Servicio |
|---|---|
| auth_vallesol_db | auth-service |
| user_db | user-service |
| report_db | report-service |
| geo_db | geo-service |
| zone_db | zone-service |
| brigade_db | brigade-service |
| alertas_db | alert-service |

---

# 📡 Endpoints Principales

## Auth

```http
POST /api/auth/login
```

---

## Usuarios

```http
POST /api/users/register
GET /api/users/notificables
```

---

## Reportes

```http
POST /api/reportes
GET /api/reportes
```

---

## Geo

```http
GET /api/geo/mapa
```

---

## Zonas

```http
GET /api/zones
POST /api/zones
```

---

## Brigadas

```http
GET /api/brigades
POST /api/brigades
```

---

# 📈 Escalabilidad

La arquitectura permite:

- Escalar servicios individualmente
- Tolerancia parcial a fallos
- Separación de dominios
- Mantenibilidad
- Despliegue independiente

---

# 🚧 Estado Actual del Proyecto

## ✅ Implementado

- Arquitectura base
- API Gateway
- BFF
- JWT
- Docker Compose
- Frontend Ionic/Angular
- Comunicación REST entre microservicios
- Separación `auth-service` ↔ `user-service`

## 🚧 En desarrollo

- División completa de `geo-service`
- Alertas automáticas avanzadas
- Dashboard táctico
- Métricas y monitoreo
- Escalamiento horizontal

---

# 👨‍💻 Equipo de Desarrollo

- Felipe Bravo
- Valentina Pino Norambuena
- Willfred Vinet

---

# 📚 Referencia Académica

Proyecto desarrollado para la asignatura:

```text
Desarrollo FullStack III — DSY1106
```