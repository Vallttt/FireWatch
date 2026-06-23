# 🔥 FireWatch - Sistema de Alertas Comunitario

> **Municipalidad Valle del Sol** - Sistema integral de gestión de emergencias y alertas comunitarias

![FireWatch Logo](https://img.shields.io/badge/FireWatch-v1.0-red?style=flat-square)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📋 Descripción

FireWatch es una plataforma integral de **gestión de emergencias y alertas comunitarias** desarrollada como solución para la Municipalidad Valle del Sol. El sistema permite:

- 📍 **Reportar emergencias** con ubicación GPS, tipo de incendio y severidad
- 🚨 **Enviar alertas comunitarias** con protocolo de actuación
- 📊 **Dashboard operacional** con estadísticas en tiempo real
- 🗺️ **Mapa interactivo** con zonas operativas y rutas de evacuación
- 📬 **Notificaciones por email** y push a brigadas y administradores
- 👥 **Gestión de brigadas** y asignaciones operacionales
- 🔐 **Autenticación segura** con JWT y recuperación de contraseña

---

## 🏗️ Arquitectura

### Microservicios (Backend - Java Spring Boot)

```
┌─────────────────────────────────────────────────────────┐
│                  Spring Cloud Gateway                   │
│            (Puerto 8765 - API Gateway Principal)        │
└──────────────────────┬──────────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
   ┌────▼─────┐  ┌───▼────┐  ┌──────▼────┐
   │ Auth Svc │  │BFF Svc │  │Eureka Svc │
   │ (8080)   │  │(8082)  │  │ (8761)    │
   └────┬─────┘  └───┬────┘  └──────┬────┘
        │            │              │
   ┌────┴─────────────┼──────────────┴─────────────┐
   │                  │                            │
   ├─ Report Service (8081)                        │
   ├─ Incident Service (8086)                      │
   ├─ Zone Service (8083)                          │
   ├─ User Service (8084)                          │
   ├─ Brigade Service (8087)                       │
   ├─ Geo Service (8085)                           │
   ├─ Evidence Service (8088)                      │
   ├─ Alert Service (8089)                         │
   ├─ Notification Service (8090)                  │
   └─ MySQL Database (Puerto 3307)                 │
```

### Frontend (Ionic Angular)

- **Web**: `localhost:8100` (Angular + Ionic)
- **Android**: APK buildable desde `frontend-android/`

---

## 🚀 Inicio Rápido

### Requisitos

- **Docker & Docker Compose** v20.10+
- **Node.js** v18+ (para desarrollo frontend)
- **Java** 21+ (para desarrollo backend)
- **Maven** 3.8+ (para compilar servicios)
- **Android SDK** (opcional, para compilar APK)

### Instalación & Ejecución

```bash
# 1. Clonar el repositorio
git clone https://github.com/Vallttt/FireWatch.git
cd valledelsol-app

# 2. Iniciar todos los servicios con Docker
docker-compose up -d

# 3. Verificar que todos los contenedores estén corriendo
docker-compose ps

# 4. Acceder a la aplicación
# Web: http://localhost:8100
# Gateway: http://localhost:8765
# Eureka: http://localhost:8761
```

### Credenciales Iniciales

```
Email: admin@valledelsol.cl
Contraseña: 123456
Rol: ADMIN
```

---

## 📱 Funcionalidades Principales

### 1️⃣ Reportar Emergencias
- Reportar incendios (Forestal, Urbano, Estructural)
- Indicar severidad (Baja, Media, Alta, Crítica)
- Capturar ubicación GPS
- Protocolo automático según severidad
- Distancias de seguridad dinámicas por tipo

### 2️⃣ Alertas Comunitarias
- Enviar a todos los usuarios o grupos específicos
- Tipos de protocolo: Evacuación, Incendio, Prevención, Controlado
- Distribución multicanal (Email + Push)
- Historial operacional con remitente

### 3️⃣ Dashboard Operacional
- Estadísticas de focos activos
- Alertas enviadas (conteo único)
- Brigadas activas en terreno
- Estado global del sistema

### 4️⃣ Mapa Interactivo
- Zonas operativas clickeables
- Rutas de evacuación dinámicas
- Centrado en zona al hacer clic
- Marcadores de incendios activos

### 5️⃣ Gestión de Brigadas
- Crear y asignar brigadas
- Vincular a zonas operativas
- Seguimiento operacional
- Estados: DEPLOYED, STANDBY, INACTIVE

---

## ✅ Características Implementadas

### 🔐 Autenticación & Seguridad
- ✅ JWT con refresh tokens (5 min de expiración)
- ✅ Alerta de sesión expirando (5 min antes)
- ✅ Recuperación de contraseña por email
- ✅ HttpInterceptor con validación automática
- ✅ Guards de autenticación en rutas
- ✅ JWT tokens con expiración automática
- ✅ CORS configurado para APIs
- ✅ SQL Injection prevention (Prepared statements)
- ✅ XSS protection (Angular sanitization)
- ✅ CSRF tokens en formularios sensibles

### 📊 Data Management & Geocercas
- ✅ Point-in-polygon (geo-cercas) con ray casting
- ✅ Auto-eliminación de rutas cuando incidente inactiva
- ✅ Mapeo dinámico de severidad (EN ↔ ES)
- ✅ Sincronización cross-device (web + Android)
- ✅ Conteo correcto de alertas (únicas en dashboard)
- ✅ Historial operacional con email del remitente
- ✅ Persistencia en MySQL con transacciones

### 📬 Notificaciones Multicanal
- ✅ **Envío real de correos** via Brevo SMTP
- ✅ **Push notifications reales** via Firebase Cloud Messaging (FCM)
- ✅ Alertas de reporte con detalles de emergencia
- ✅ Alertas comunitarias sin datos sensibles
- ✅ Colores diferenciados por severidad
- ✅ Plantillas HTML dinámicas por tipo de alerta
- ✅ Notificación inmediata a brigadas asignadas

### 👥 Gestión de Brigadas & Asignación
- ✅ Asignación automática de brigadas a zonas críticas
- ✅ Vinculación de brigadas a zonas operativas
- ✅ Seguimiento operacional en tiempo real
- ✅ Estados: DEPLOYED, STANDBY, INACTIVE
- ✅ Notificación inmediata cuando se asigna

### 🗺️ Mapa & Zonas Operativas
- ✅ Zonas operativas clickeables en mapa
- ✅ Rutas de evacuación dinámicas
- ✅ Auto-generación de rutas según severidad
- ✅ Centrado automático en zona al hacer clic
- ✅ Marcadores de incendios activos
- ✅ Visualización de distancias de seguridad

### 🚨 Protocolos & Procedimientos
- ✅ Protocolo automático según severidad del incendio
- ✅ Tipos de protocolo: Evacuación, Incendio, Prevención, Controlado
- ✅ Distancias de seguridad dinámicas por tipo:
  - Forestal: 300m / 1000m / 3000m / 5000m
  - Urbano: 100m / 300m / 500m / 1000m
  - Estructural: 50m / 100m / 200m / 500m

### 🔗 Integración Externa
- ✅ **Brevo SMTP** para envío de emails en producción
- ✅ **Firebase Cloud Messaging** (FCM) para push
- ✅ **MySQL** para persistencia de datos
- ✅ **Leaflet** para mapas con GeoJSON
- ✅ **Eureka** para service discovery
- ✅ **Spring Cloud Gateway** para enrutamiento central

---

## 📂 Estructura del Proyecto

```
valledelsol-app/
├── frontend-web/                 # Ionic Angular Web
│   ├── src/app/
│   │   ├── Alertas/             # Gestión de alertas comunitarias
│   │   ├── Mapa/                # Mapa interactivo con zonas
│   │   ├── Reportar/            # Formulario de emergencias
│   │   ├── Dashboard/           # Panel operacional
│   │   ├── services/            # HTTP clients y lógica
│   │   └── guards/              # Protección de rutas
│   └── docker-compose.yml
│
├── frontend-android/             # APK Android (mismo código)
│   ├── src/app/                 # (Sincronizado con web)
│   └── android/                 # Gradle build config
│
├── [backend services]/           # Java Spring Boot
│   ├── auth-service/            # JWT, login, recuperación
│   ├── alert-service/           # Gestión de alertas
│   ├── notification-service/    # Envío de emails/push
│   ├── report-service/          # Crear reportes
│   ├── incident-service/        # Estados de incendios
│   ├── zone-service/            # Zonas y rutas evacuación
│   ├── brigade-service/         # Brigadas operacionales
│   └── api-gateway/             # Enrutamiento central
│
└── docker-compose.yml            # Orquestación de servicios
```

---

## 🛠️ Desarrollo

### Frontend (Web + Android)

```bash
# Desarrollar en tiempo real
cd frontend-web
npm install
npm start  # Abre http://localhost:4200

# Compilar para producción
npm run build

# Compilar APK Android
cd ../frontend-android
npm run build
cd android
./gradlew build
```

### Backend (Microservicios)

```bash
# Compilar un servicio
cd alert-service
mvn clean package -DskipTests

# Ejecutar localmente (sin Docker)
mvn spring-boot:run
```

### Docker (Recomendado)

```bash
# Build y start todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f service-name

# Detener todo
docker-compose down
```

---

## 🔄 Flujo de Datos Principal

```
Ciudadano/Admin                 Frontend (Web/Android)              Backend (Microservicios)
    │                                  │                                    │
    ├─ Report Emergencia               │                                    │
    │                          ──────► │                                    │
    │                                  ├─ POST /api/reportes ──────────────┤
    │                                  │                          Report Service
    │                                  │                          ├─ Mapeo severidad
    │                                  │                          ├─ Crear Alert
    │                                  │                          └─ Enviar a Alert Svc
    │                                  │                                    │
    │                                  │                          Alert Service
    │                                  │                          ├─ Clasificación
    │                                  │                          ├─ Persistir
    │                                  │                          └─ Enviar a Notification
    │                                  │                                    │
    │                                  │                    Notification Service
    │◄─ Email Alerta ───────────────────────── SMTP (Brevo)
    │                                  │
    │◄─ Push Notification ───────────────────── FCM
```

---

## 📊 Mapeo de Severidad

| English  | Spanish | Color | Distancia (Forestal) | Email Template |
|----------|---------|-------|----------------------|----------------|
| LOW      | BAJO    | Verde | 300m                 | Preventiva     |
| MEDIUM   | MEDIO   | Amarillo | 1000m             | General        |
| HIGH     | ALTO    | Rojo  | 3000m                | Crítica        |
| CRITICAL | CRÍTICO | Rojo Oscuro | 5000m         | Máxima urgencia|

---

## 🚧 En Desarrollo

Las siguientes funcionalidades están en desarrollo activo:

### Backend
- 📊 **Dashboard táctico avanzado** - Timeline completo de eventos, análisis predictivo, heatmaps
- 📈 **Observabilidad y métricas** - Prometheus, Grafana, distributed tracing con Jaeger
- 🔄 **Escalamiento horizontal** - Replicación automática de servicios críticos bajo carga
- 🏥 **Health checks avanzados** - Registro dinámico completo en Eureka con validaciones
- 🔐 **Autorización granular por roles** - RBAC con políticas dinámicas en Spring Security + Gateway
- 🎯 **API Rate Limiting** - Control de velocidad por usuario/IP en API Gateway

### Frontend
- 🗺️ **Administración de zonas desde GUI** - Crear, editar, eliminar zonas sin recompilación
- 🖌️ **Creación interactiva de polígonos** - Dibuja zonas directamente sobre el mapa Leaflet
- 📱 **PWA (Progressive Web App)** - Funciona offline, instalable como app nativa
- 📋 **Gestión de eventos operacionales** - CRUD completo con filtros y búsqueda
- 🔍 **Análisis de patrones** - Detección automática de zonas de riesgo recurrente

---

## 📞 Contacto & Soporte

**Municipalidad Valle del Sol**
- Email: contacto@valledelsol.cl
- Teléfono: +56 9 XXXX XXXX
- Web: www.valledelsol.cl

---

## 📄 Licencia

Este proyecto está bajo la licencia **MIT**. Ver [LICENSE](LICENSE) para más detalles.

---

## 👥 Contribuyentes

- **Valentina Norambuena** - Desarrollo Principal
- **Equipo FireWatch** - Testing y Operaciones

---

**Última actualización**: Junio 2026  
**Versión**: 1.0.0
