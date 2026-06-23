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

## 🚧 Estado Actual del Proyecto

### ✅ Implementado

**Arquitectura & Comunicación**
- Arquitectura de microservicios completa.
- API Gateway con validación JWT (HMAC-SHA256).
- Autorización por roles (ADMIN / USER) en el API Gateway.
- BFF con orquestación y consolidación de datos.
- Eureka Server para service discovery.
- Comunicación alert-service → notification-service vía Eureka.
- Descubrimiento BFF → alert-service y notification-service vía Feign + Eureka.
- Integración BFF ↔ Zone Service y BFF ↔ Brigade Service.
- Docker Compose con orden de arranque correcto.

**Reportes & Incidentes**
- Migración de report-service en tres microservicios: report (núcleo), incident (estado/severidad) y evidence (multimedia), expuestos por el BFF y ruteados por el Gateway.
- Endpoint agregador de reporte completo en el BFF (`/api/reportes/{id}/completo`).
- Reportes multimedia (foto/video).
- Mapeo dinámico de severidad (EN ↔ ES) y protocolos de actuación.
- Distancias de seguridad dinámicas por tipo de incendio.

**Alertas & Notificaciones**
- alert-service: generación, clasificación y tipos de alerta.
- notification-service: correo, push, brigadas, admins, sistema.
- Envío real de correos vía Brevo SMTP.
- Push notifications.
- Correos HTML personalizados por tipo de alerta.
- Dashboard centralizado (totalIncendios, alertasEmitidas, brigadasActivas).
- Historial operacional con email del remitente.

**Zonas, Rutas & Brigadas**
- Gestión de zonas operativas y rutas de evacuación.
- CRUD completo de zonas, rutas de evacuación y brigadas.
- Gestión de brigadas y estado operativo (asignación manual a zonas).
- Auto-generación y auto-eliminación de rutas según el estado del incidente.
- Validaciones geoespaciales mediante JTS (point-in-polygon).
- Administración de zonas desde interfaz gráfica.
- Creación interactiva de polígonos sobre el mapa.

**Usuarios & Seguridad**
- Separación auth-service ↔ user-service.
- Separación geo-service ↔ zone-service ↔ brigade-service.
- JWT con expiración automática y aviso de sesión por expirar.
- Recuperación de contraseña con Brevo SMTP.
- Usuarios semilla para demostración.

**Frontend**
- Frontend web y Android (APK Capacitor) sincronizados.

### 🚧 En Desarrollo
- Dashboard táctico avanzado con timeline de eventos.
- Asignación automática de brigadas a zonas críticas.
- Observabilidad y métricas operacionales.
- Escalamiento horizontal de servicios críticos.

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
