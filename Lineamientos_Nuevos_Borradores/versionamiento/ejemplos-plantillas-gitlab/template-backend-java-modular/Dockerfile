# EJEMPLO DE REFERENCIA — LIN-VER-001 Anexo F / LIN-K8S-001 sección 5.2
# Dockerfile multi-stage para Monolito Modular Maven — imagen institucional ONP
# Plataforma/Infraestructura es responsable de mantener este archivo en el proyecto GitLab real.

# ── Etapa 1: Build ─────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copia todos los pom.xml del reactor primero (cache de capas Docker: solo se
# reinstalan dependencias si algún pom.xml cambia, no en cada cambio de código).
COPY pom.xml .
COPY comun/onp-common-domain/pom.xml comun/onp-common-domain/pom.xml
COPY comun/onp-common-web/pom.xml comun/onp-common-web/pom.xml
COPY componentes/afiliacion/onp-afiliacion-domain/pom.xml componentes/afiliacion/onp-afiliacion-domain/pom.xml
COPY componentes/afiliacion/onp-afiliacion-application/pom.xml componentes/afiliacion/onp-afiliacion-application/pom.xml
COPY componentes/afiliacion/onp-afiliacion-infrastructure/pom.xml componentes/afiliacion/onp-afiliacion-infrastructure/pom.xml
COPY componentes/afiliacion/onp-afiliacion-api/pom.xml componentes/afiliacion/onp-afiliacion-api/pom.xml
COPY onp-template-boot/pom.xml onp-template-boot/pom.xml
RUN mvn dependency:go-offline -B

COPY comun ./comun
COPY componentes ./componentes
COPY onp-template-boot ./onp-template-boot
# -DskipTests correcto aquí: el pipeline CI ejecuta las pruebas de todo el reactor
# antes de construir la imagen. El Dockerfile es solo la etapa de empaquetado.
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

# El .jar ejecutable solo lo produce el módulo -boot (único con spring-boot-maven-plugin)
COPY --from=build /workspace/onp-template-boot/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
