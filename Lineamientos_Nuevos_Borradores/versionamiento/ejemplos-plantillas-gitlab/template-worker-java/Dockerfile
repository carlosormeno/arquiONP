# EJEMPLO DE REFERENCIA — LIN-VER-001 Anexo F / LIN-K8S-001 sección 5.2
# Dockerfile multi-stage para Spring Boot — imagen institucional ONP
# Plataforma/Infraestructura es responsable de mantener este archivo en el proyecto GitLab real.
#
# Una única imagen sirve las tres variantes de workload (Worker/Deployment, Job, CronJob,
# ver LIN-K8S-001 §4.1): la variante y el ambiente los decide SPRING_PROFILES_ACTIVE en el
# manifiesto K8s que la invoca (ej. "prod,worker" o "prod,job"), no un build de imagen distinto
# por variante. A diferencia de un servidor que corre indefinidamente, con el perfil "job" este
# mismo ENTRYPOINT termina el proceso al finalizar la tarea (System.exit, ver
# ActualizacionMasivaJobRunner) — el contenedor no necesita nada especial para soportar eso.

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
LABEL org.opencontainers.image.title="worker-nombre-sistema" \
      org.opencontainers.image.vendor="ONP" \
      org.opencontainers.image.base.name="eclipse-temurin:21-jre"

# Usuario no privilegiado — obligatorio por LIN-K8S-001
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

# Solo se enlaza en pods con el perfil "worker" (Actuator para probes, LIN-K8S-001 §11.1/§11.2).
# Los pods "job"/"cronjob" no escuchan aquí — declararlo igual no tiene costo ni riesgo.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
