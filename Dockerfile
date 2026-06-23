# Usa la imagen oficial de Eclipse Temurin (OpenJDK) desde Docker Hub
FROM openjdk:17-slim-bullseye

# Establece el directorio de trabajo
WORKDIR /app


# Copia el JAR desde la raíz del proyecto
COPY sircc_backend-0.0.1-SNAPSHOT.jar app.jar


# Expone el puerto 8080
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]