# Usa la imagen oficial de Eclipse Temurin (OpenJDK) desde Docker Hub
FROM docker.io/eclipse-temurin:17-jdk-slim

# Establece el directorio de trabajo
WORKDIR /app

# Copia el JAR generado al contenedor
COPY target/sircc_backend-0.0.1-SNAPSHOT.jar app.jar

# Expone el puerto 8080
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]