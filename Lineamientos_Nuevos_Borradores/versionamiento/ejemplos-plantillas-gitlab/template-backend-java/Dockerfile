# EJEMPLO DE REFERENCIA — LIN-VER-001 Anexo F / LIN-K8S-001 §5.2
# Dockerfile multi-stage para Spring Boot — imagen institucional ONP
# Plataforma/Infraestructura es responsable de mantener este archivo en el proyecto GitLab real.

# ── Etapa 1: Build ─────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
# -DskipTests correcto aquí: el pipeline CI ejecuta las pruebas antes de construir
# la imagen. El Dockerfile es solo la etapa de empaquetado.
RUN mvn package -DskipTests -B

# ── Etapa 2: Runtime ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre

# Reemplazar con el nombre real del sistema
LABEL org.opencontainers.image.title="api-nombre-sistema" \
      org.opencontainers.image.vendor="ONP" \
      org.opencontainers.image.base.name="eclipse-temurin:21-jre"

# Usuario no privilegiado — obligatorio por LIN-K8S-001
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
