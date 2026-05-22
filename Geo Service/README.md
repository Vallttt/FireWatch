# Microservicio GEO - Sistema de Gestión de Emergencias

Este microservicio forma parte de una arquitectura basada en microservicios orientada a la gestión de emergencias. Su responsabilidad principal es manejar la información geográfica del sistema, incluyendo zonas, brigadas y rutas de evacuación.

---

## Objetivo

Centralizar y exponer datos geoespaciales necesarios para la operación del sistema, permitiendo la visualización en mapas y la coordinación con otros microservicios como alertas y reportes.

---

## Funcionalidades principales

- Gestión de zonas (Zones)
- Gestión de brigadas (Brigades)
- Gestión de rutas de evacuación (Evacuation Routes)
- Asociación entre entidades geográficas
- Exposición de endpoints REST para consumo externo

---

## Arquitectura

El microservicio sigue una arquitectura en capas:

- **Controller** → Manejo de endpoints REST
- **Service** → Lógica de negocio
- **Repository** → Acceso a datos
- **Model / Entity** → Representación de la base de datos
- **DTOs** → Transferencia de datos

---

## Entidades principales

### Zone
Representa una zona geográfica dentro del sistema.

- id
- name
- type (SAFE, DANGER, etc.)
- latitude
- longitude

---

### Brigade
Representa equipos de respuesta en terreno.

- id
- name
- institution
- latitude
- longitude
- status (AVAILABLE, DEPLOYED, etc.)
- zoneId

---

### EvacuationRoute
Define rutas de evacuación asociadas a zonas.

- id
- name
- startLatitude
- startLongitude
- endLatitude
- endLongitude
- zoneId

---

## Flujo del microservicio

1. Se registran zonas geográficas
2. Se asignan brigadas a zonas
3. Se definen rutas de evacuación
4. Otros microservicios consumen esta información (ej: alertas)
5. Frontend visualiza los datos en un mapa

---

## Endpoints (ejemplo)

### Zones
- GET /zones
- POST /zones
- GET /zones/{id}
- PUT /zones/{id}
- DELETE /zones/{id}

### Brigades
- GET /brigades
- POST /brigades
- GET /brigades/{id}

---

## Tecnologías utilizadas

- Java 17+
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Lombok
- Maven

---

## Ejecución local

1. Clonar el repositorio:
```bash
git clone <url-del-repo>
```

2. Configurar base de datos en `application.properties`

3. Ejecutar el proyecto:
```bash
mvn spring-boot:run
```

---

## 🧪 Pruebas

Se pueden probar los endpoints usando:

- Postman
- Insomnia

---

## 📌 Estado del proyecto

🚧 En desarrollo (MVP en construcción)

---

## 👥 Equipo

Proyecto académico - Desarrollo de Software  
Arquitectura basada en microservicios
