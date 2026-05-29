# 🌍 Reporte GEO – AuthService

Microservicio de autenticación para el sistema de **mapas y reportes georreferenciados**, encargado de gestionar la identidad de los usuarios que desean interactuar con la plataforma.

Este servicio permite a los usuarios registrarse e iniciar sesión para acceder a funcionalidades personalizadas, como la visualización de sus reportes.

---

## 📌 Responsabilidad del servicio

El AuthService centraliza toda la lógica relacionada a autenticación:

* Registro de usuarios
* Inicio de sesión (login)
* Encriptación de contraseñas
* Generación y validación de tokens JWT
* Identificación del usuario autenticado (`/auth/me`)

⚠️ **Importante:**
El sistema permite la creación de reportes de forma **anónima**, por lo que el uso de cuentas es completamente **opcional**.

Este servicio es utilizado por otros microservicios (como Report Service) para validar la identidad de los usuarios mediante JWT.

---

## 🎯 Objetivo

Proveer un sistema de autenticación:

* Simple
* Seguro
* Desacoplado

Que permita identificar usuarios y proteger endpoints que requieren autenticación, sin afectar la experiencia de usuarios anónimos.

---

## 🛠️ Tecnologías utilizadas

* Java 17
* Spring Boot
* Spring Security
* JWT (JSON Web Token)
* PostgreSQL
* JPA / Hibernate
* Maven
* Lombok
* Docker (pendiente)

---

## 🧱 Estructura del proyecto

El proyecto sigue una arquitectura por capas para mantener separación de responsabilidades:

```
authservice/
│
├── config/        # Configuración de seguridad y beans
├── controller/    # Endpoints REST (/auth)
├── dto/           # Objetos de transferencia de datos
├── enums/         # Enumeraciones (UserRole, UserStatus)
├── model/         # Entidades (User, SecurityUser)
├── repository/    # Acceso a base de datos
├── security/      # JWT, filtros, UserDetailsService
├── service/       # Lógica de negocio (AuthService)
```

---

## 🔐 Endpoints principales

### 📍 Registro

```
POST /auth/register
```

Permite crear una nueva cuenta de usuario.

---

### 📍 Login

```
POST /auth/login
```

Autentica al usuario y retorna un token JWT.

---

### 📍 Usuario autenticado

```
GET /auth/me
```

Retorna la información del usuario autenticado a partir del token JWT.

---

## 🔑 Autenticación con JWT

El sistema utiliza JWT para autenticar usuarios.

### 📌 Flujo:

1. El usuario inicia sesión
2. El AuthService genera un token JWT
3. El cliente envía el token en cada request:

```
Authorization: Bearer <token>
```

4. Los microservicios validan el token para autorizar acceso

---

## 🗄️ Base de datos

Tabla principal:

### `users`

Contiene la información de los usuarios registrados:

* id (UUID)
* first_name
* last_name
* email
* password_hash
* phone
* status
* role
* last_login_at
* created_at
* updated_at

---

## 🔄 Integración con otros microservicios

Este servicio se integra principalmente con:

* **Report Service** → para asociar reportes a usuarios autenticados
* **Geo Service** → opcional, para personalización futura

Los microservicios consumen el JWT generado para identificar al usuario.

---

## 🚀 Estado del proyecto

✔ Registro de usuarios
✔ Login con JWT
✔ Seguridad stateless
✔ Protección de endpoints
✔ Estructura base lista para microservicios

🔜 Endpoint `/auth/me`
🔜 Integración completa con Report Service
🔜 Dockerización

---

## 📌 Notas finales

* El sistema está diseñado para permitir tanto usuarios autenticados como anónimos
* La autenticación no es obligatoria para generar reportes
* Se prioriza simplicidad y escalabilidad para evolución futura

---
