# ==========================================
# ETAPA 1: COMPILACIÓN (BUILDER)
<<<<<<< HEAD
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
# Copiamos todo tu código fuente desde tu PC hacia adentro de la caja virtual
COPY pom.xml .
COPY src ./src
# Obligamos a Docker a compilar el proyecto por nosotros adentro de su barriga
=======
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
>>>>>>> 2008e67 (separacion de SolAlertas en alert-service y notification-service con Eureka)
RUN mvn clean package -DskipTests

# ==========================================
# ETAPA 2: EJECUCIÓN (RUNNER)
# ==========================================
<<<<<<< HEAD
# Ahora descargamos una computadora súper liviana que SOLO tiene Java para correr el proyecto
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
# Extraemos el archivo .jar que la Etapa 1 compiló y lo pegamos en esta etapa final
COPY --from=builder /app/target/*.jar app.jar
# Lo encendemos
ENTRYPOINT ["java", "-jar", "app.jar"]
=======
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
>>>>>>> 2008e67 (separacion de SolAlertas en alert-service y notification-service con Eureka)
