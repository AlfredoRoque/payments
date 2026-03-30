# ─────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copia solo el pom.xml primero para aprovechar la caché de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia el resto del código fuente y compila
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─────────────────────────────────────────
# Stage 2: Runtime
# ─────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crea un usuario no-root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copia el JAR generado desde la etapa de build
COPY --from=builder /app/target/payments-1.0.0.jar app.jar

# Cambia al usuario no-root
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

