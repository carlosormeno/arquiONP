# Dockerfile multi-stage para Angular SPA — imagen institucional ONP
# Ver LIN-FE-ANG-001 sección 16.3.
#
# CORRECCIÓN DE GOBIERNO DOCUMENTAL (2): la versión anterior de este archivo
# (stub DevOps sin código Angular real) construía con
# `--output-path=dist/app` y copiaba `/app/dist/app` al runtime. Verificado
# con un build real: el builder de aplicación de Angular (`@angular/build:
# application`, activo desde Angular 17) SIEMPRE anida la salida bajo un
# subdirectorio `browser/`, incluso con `--output-path` personalizado
# (`dist/app/browser/index.html`, no `dist/app/index.html`). Copiar
# `/app/dist/app` sin el sufijo `/browser` deja `index.html` un nivel más
# profundo de lo que nginx espera — el contenedor arranca pero sirve 403/404
# en `/`. Este Dockerfile sigue el path exacto de LIN-FE-ANG-001 §16.3.
#
# CORRECCIÓN DE GOBIERNO DOCUMENTAL (2): §16.3 fija `FROM node:20-alpine`.
# Verificado con `docker build` real: Angular CLI 22.1.8 (la LTS vigente que
# instala hoy `ng new`, conforme exige §3 "≥18") exige Node.js
# ^22.22.3 || ^24.15.0 || >=26.0.0 — node:20 hace fallar el build
# (`ng build` aborta antes de compilar). Se usa `node:22-alpine` aquí; ver
# hallazgo en el reporte de entrega para actualizar §16.3.

# ── Etapa 1: Build Angular ─────────────────────────────────────────────────────
FROM node:22-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci --prefer-offline

COPY . .
# El argumento de configuración selecciona el environment Angular correcto
# (angular.json define development/qa/production). El pipeline CI pasa
# --build-arg CONFIGURATION=qa|production según rama/entorno de destino.
ARG CONFIGURATION=production
RUN npm run build -- --configuration=${CONFIGURATION}

# ── Etapa 2: Runtime nginx ─────────────────────────────────────────────────────
FROM nginxinc/nginx-unprivileged:1.27-alpine

LABEL org.opencontainers.image.title="onp-template-frontend" \
      org.opencontainers.image.vendor="ONP" \
      org.opencontainers.image.base.name="nginxinc/nginx-unprivileged:1.27-alpine"

# Configuración nginx institucional (SPA routing + gzip + seguridad).
# Ver LIN-FE-ANG-001 sección 16.2.
COPY nginx.conf /etc/nginx/nginx.conf

# Reemplazar "onp-template-frontend" por el outputPath real del proyecto
# (angular.json → projects.<nombre>.architect.build) si se renombra la
# plantilla para un sistema concreto.
COPY --from=builder /app/dist/onp-template-frontend/browser /usr/share/nginx/html

EXPOSE 8080
