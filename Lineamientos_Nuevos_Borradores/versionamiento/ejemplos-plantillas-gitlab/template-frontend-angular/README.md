# onp-template-frontend — Plantilla Frontend Angular ONP

> **PLANTILLA DE REFERENCIA — `LIN-VER-001` Anexo F / `LIN-FE-ANG-001`**
> Este es un proyecto Angular real y funcional (no un stub): compila, corre pruebas
> unitarias y e2e, y produce una imagen de contenedor válida. Al usarlo como base de
> un sistema real, renombrar el proyecto (`angular.json`, `package.json`, selectores
> `onp-*` si se desea otro prefijo) y reemplazar los contenidos de ejemplo del
> dominio "usuarios" por los dominios funcionales reales.
> Plataforma/Infraestructura es responsable de mantener esta plantilla en GitLab.

## Descripción

Scaffold de referencia que materializa `LIN-FE-ANG-001 — Estándar de Diseño Web
Frontend ONP`: estructura de carpetas (`core/`, `shared/`, `features/`, `layout/`),
layout de cuatro zonas, integración con `ApiResponseWrapper`, interceptores HTTP
(correlación, autenticación, errores), manejo de errores y feedback, design tokens
institucionales, configuración por entorno, pruebas unitarias y e2e, observabilidad
(Core Web Vitals, captura de errores) y contenedorización no-root.

Incluye un feature de ejemplo completo (`features/usuarios/`) con una Vista de
Listado (§7.1) y una Vista de Formulario (§7.2) consumiendo el contrato
`ApiResponseWrapper`.

**Código lineamiento:** `LIN-FE-ANG-001`
**Tipo:** Frontend Angular SPA (plantilla)
**Propietario técnico:** Arquitectura OTI

## Stack (LIN-FE-ANG-001 §3)

| Capa | Tecnología | Versión en esta plantilla |
|---|---|---|
| Framework | Angular | 22.1.x (standalone, sin NgModules) |
| Lenguaje | TypeScript | 6.0.x |
| Estilos | Tailwind CSS | 3.4.x (`corePlugins.preflight: false`) |
| Componentes UI | Angular Material | 22.1.x |
| Estado | Angular Signals | nativo |
| Testing unitario | Karma + Jasmine | — |
| Testing e2e | Playwright | — |
| Node.js (build) | Node.js | **22.x** (ver nota de Dockerfile más abajo) |

## Prerrequisitos

| Herramienta | Versión mínima | Notas |
|---|---|---|
| Node.js | **22.22.3** | Angular CLI 22.x lo exige en tiempo de build; Node 20 falla |
| npm | 10+ | |
| Docker o Podman | 24+ | Para build de imagen |
| kubectl + kustomize | 1.27+ / 5+ | Para despliegue en K8s (`kubectl kustomize` incluye kustomize) |

## Ejecución local

```bash
# Instalar dependencias (usa el lockfile versionado)
npm ci

# Ejecutar en modo desarrollo
npm start            # equivalente a: ng serve

# Compilar por entorno
ng build --configuration development   # default
ng build --configuration qa
ng build --configuration production

# Ejecutar pruebas unitarias (Karma/Jasmine, con cobertura)
ng test --watch=false
# En un entorno sin Chrome instalado, definir CHROME_BIN antes de ejecutar
# (ver "Nota sobre navegador headless" más abajo).

# Ejecutar pruebas e2e (Playwright) — levanta `ng serve` automáticamente
npx playwright install chromium   # una sola vez, descarga el navegador
npx playwright test
```

### Nota sobre navegador headless

Este proyecto no incluye Chrome/Chromium: ni `ng test` (Karma) ni `npx playwright
test` encuentran un navegador si la máquina no lo tiene instalado. Para CI o un
entorno sin navegador preinstalado:

```bash
# Para Karma
export CHROME_BIN=/ruta/a/chromium   # o instalar Google Chrome / chromium del SO

# Para Playwright
npx playwright install chromium      # descarga un binario propio, no requiere root
```

## Estructura del proyecto

```
src/
├── app/
│   ├── core/                  # Interceptores, guards, servicios singleton
│   │   ├── interceptors/      # correlation, auth, error
│   │   ├── guards/            # auth.guard
│   │   └── services/          # api-base, auth, notification, global-error-handler
│   ├── shared/                # Componentes, pipes, directivas y modelos reutilizables
│   │   ├── components/        # error-page, not-found, skeleton-loader
│   │   ├── directives/        # highlight
│   │   ├── pipes/             # date-format
│   │   └── models/            # api-response, menu-item, utilidades de HttpParams/codDetRespuesta
│   ├── features/              # Dominios de negocio
│   │   ├── auth/login/        # Vista de Login
│   │   ├── home/              # Vista de Bienvenida
│   │   └── usuarios/          # Ejemplo: Listado (§7.1) + Formulario (§7.2) + Detalle
│   ├── layout/                # Shell, header, sidebar, footer (layout de 4 zonas, §6)
│   ├── app.component.ts
│   ├── app.config.ts          # Bootstrap standalone: interceptores, Router, ErrorHandler
│   └── app.routes.ts
├── assets/{images,icons,fonts}/
├── environments/{environment,environment.qa,environment.prod}.ts
├── styles/{_tokens,_mat-theme,styles}.scss
├── index.html
└── main.ts
e2e/                            # Pruebas e2e Playwright (mockean la API — no requieren backend real)
├── auth/login.spec.ts
├── usuarios/{listado,formulario}.spec.ts
└── support/mock-api.ts
docs/adr/                       # Architecture Decision Records del proyecto real
k8s/
├── base/                       # Manifiestos base (Kustomize)
└── overlays/{dev,qa,prod}/     # Overlays por ambiente
lighthouserc.js                 # Gate de Core Web Vitals (§15.2)
tailwind.config.js               # Design tokens (§8.5)
```

## Registro de imagen

```
registry.gitlab.onp.gob.pe/aplicaciones/nombre-sistema/frontend-nombre-sistema:<version>
```

Ver `LIN-K8S-001` para el proceso completo de construcción y promoción de imágenes.

## Contenedorización (LIN-FE-ANG-001 §16)

- Imagen builder: `node:22-alpine` (ver nota abajo).
- Imagen runtime: `nginxinc/nginx-unprivileged:1.27-alpine`, puerto **8080** (no root, uid 101).
- `securityContext` en `k8s/base/deployment.yaml`: `runAsNonRoot`, `readOnlyRootFilesystem: true`
  con `emptyDir` en `/tmp`, `capabilities.drop: [ALL]` — conforme a `LIN-K8S-001 §14.1`.

```bash
docker build -t onp-template-frontend:local .
docker run --rm -p 8080:8080 onp-template-frontend:local
curl http://localhost:8080/
```

> **Nota de versión de Node en el Dockerfile:** `LIN-FE-ANG-001 §16.3` fija
> `FROM node:20-alpine`. Verificado con un build real: Angular CLI 22.x (la LTS
> vigente instalada por `ng new` al construir esta plantilla, conforme exige
> `§3` "≥18") requiere Node.js `^22.22.3 || ^24.15.0 || >=26.0.0` — con
> `node:20-alpine` el build falla antes de compilar. Este `Dockerfile` usa
> `node:22-alpine`; es un hallazgo para actualizar `§16.3` (ver reporte de
> entrega del scaffold).

## Pruebas (LIN-FE-ANG-001 §14)

- **Unitarias:** Karma + Jasmine. Cobertura exigida sobre `core/` y `shared/`
  (`LIN-TEST-001 §5.2`: statements ≥70%, branches ≥65%, functions ≥70%), configurada
  como gate en `angular.json` (`architect.test.options.coverageThresholds`).
- **E2E:** Playwright. Los specs interceptan las llamadas a la API
  (`e2e/support/mock-api.ts`) para no depender de un backend real desplegado;
  cubren los flujos obligatorios de `§14.2` (login exitoso/fallido, listado con
  filtros, alta exitosa/inválida, acceso a ruta protegida sin sesión).

## Configuración nginx

El contenedor de producción usa nginx no-root con `try_files` para el enrutamiento
SPA, compresión gzip y cache agresivo de assets con hash. Ver `nginx.conf` en la
raíz y `LIN-FE-ANG-001 §16.2`.

## Contacto

- **Líder técnico:** [nombre]
- **Arquitectura OTI:** arquitectura@onp.gob.pe
- **Plataforma/Infraestructura:** [canal interno]
