# LIN-FE-ANG-001 — Estándar de Diseño Web Frontend ONP
**Código:** LIN-FE-ANG-001  
**Versión:** 0.1.0  
**Estado:** Borrador  
**Fecha:** 2026-05-22  
**Área responsable:** OTI — Oficina de Tecnologías de la Información  
**Marco rector:** LIN-ARQ-000 — Marco Rector de Diseño y Arquitectura de Software  

---

## Tabla de contenido

1. [Introducción](#1-introducción)
2. [Alcance y aplicabilidad](#2-alcance-y-aplicabilidad)
3. [Stack tecnológico estándar](#3-stack-tecnológico-estándar)
4. [Estructura del proyecto Angular](#4-estructura-del-proyecto-angular)
5. [Convenciones de nomenclatura](#5-convenciones-de-nomenclatura)
6. [Layout estándar ONP](#6-layout-estándar-onp)
7. [Tipos de vista estándar](#7-tipos-de-vista-estándar)
8. [Diseño visual — Design Tokens ONP](#8-diseño-visual--design-tokens-onp)
9. [Integración con APIs REST](#9-integración-con-apis-rest)
10. [Manejo de errores y feedback](#10-manejo-de-errores-y-feedback)
11. [Reportes imprimibles](#11-reportes-imprimibles)
12. [Accesibilidad](#12-accesibilidad)
13. [Configuración por entorno](#13-configuración-por-entorno)
14. [Pruebas](#14-pruebas)
15. [Observabilidad y performance](#15-observabilidad-y-performance)
16. [Contenedorización](#16-contenedorización)

---

## 1. Introducción

Este lineamiento establece los estándares de diseño y desarrollo para aplicaciones web frontend en ONP. Define el stack tecnológico adoptado, las convenciones de estructura y nomenclatura, los patrones de integración con APIs REST, y los criterios de calidad que todo sistema web debe cumplir.

Este documento es complementario al **LIN-DEV-JAVA-001 — Estándar de Desarrollo Java ONP**, que define el lado backend (APIs REST, `ApiResponseWrapper`, autenticación). Juntos conforman el estándar completo para sistemas web de ONP.

Los principios de estructura visual (layout de cuatro zonas, tipos de vista, estructura de reportes) se derivan de la **Guía de Diseño y Programación Web ONP (2019)**, actualizados al stack tecnológico vigente.

## 2. Alcance y aplicabilidad

Aplica a todo proyecto de desarrollo web frontend que:
- Sea desarrollado o mantenido por la OTI o proveedores bajo contrato con ONP.
- Consuma APIs REST expuestas por servicios Spring Boot internos.
- Sea desplegado en la infraestructura de ONP.

**No aplica a:**
- Portales o micrositios estáticos de contenido informativo.
- Dashboards de monitoreo (Grafana, Kibana) administrados como herramientas de plataforma.
- Aplicaciones móviles nativas.

## 3. Stack tecnológico estándar

| Capa | Tecnología | Versión mínima |
|---|---|---|
| Framework | Angular | LTS vigente (≥ 18) |
| Lenguaje | TypeScript | 5.x |
| Estilos (utilidades) | Tailwind CSS | 3.x |
| Componentes UI | Angular Material | LTS vigente (≥ 18) |
| Gestión de estado | Angular Signals | nativo (≥ 16) |
| HTTP | Angular HttpClient | nativo |
| Router | Angular Router | nativo |
| Testing unitario | Jest o Karma/Jasmine | LTS vigente |
| Testing e2e | Playwright | LTS vigente |
| Node.js (build) | Node.js | LTS vigente |

### 3.1 Justificación del stack

**Angular** es el framework elegido porque:
- Provee una estructura opinada que reduce las decisiones de arquitectura en cada proyecto.
- TypeScript nativo elimina una categoría entera de errores en runtime.
- Angular Material entrega componentes accesibles (ARIA) sin configuración adicional.
- Tailwind CSS permite expresar estilos de layout y utilidades directamente en el template sin escribir CSS ad-hoc por cada componente.

**Tailwind CSS y Angular Material son complementarios, no excluyentes:**
- Angular Material provee los *componentes* (botones, formularios, tablas, diálogos, datepickers).
- Tailwind provee *utilidades de layout* (flex, grid, spacing, responsive breakpoints).

### 3.2 Compatibilidad de navegadores

| Navegador | Soporte |
|---|---|
| Google Chrome | Últimas 2 versiones |
| Mozilla Firefox | Últimas 2 versiones |
| Microsoft Edge | Últimas 2 versiones |
| Safari | Últimas 2 versiones |

Internet Explorer no es un objetivo de compatibilidad.

## 4. Estructura del proyecto Angular

Todo proyecto Angular ONP debe seguir la siguiente organización de carpetas:

```
src/
├── app/
│   ├── core/                  # Singleton: interceptores, guards, servicios globales
│   │   ├── interceptors/
│   │   ├── guards/
│   │   └── services/
│   ├── shared/                # Componentes, pipes y directivas reutilizables
│   │   ├── components/
│   │   ├── directives/
│   │   └── pipes/
│   ├── features/              # Módulos de negocio (uno por dominio)
│   │   ├── auth/
│   │   ├── <dominio-a>/
│   │   └── <dominio-b>/
│   ├── layout/                # Componentes de shell (cabecera, menú, footer)
│   │   ├── header/
│   │   ├── sidebar/
│   │   └── footer/
│   ├── app.component.ts
│   ├── app.config.ts          # Standalone bootstrap config
│   └── app.routes.ts
├── assets/
│   ├── images/
│   ├── icons/
│   └── fonts/
├── environments/
│   ├── environment.ts         # Desarrollo (default)
│   ├── environment.qa.ts
│   └── environment.prod.ts
├── styles/
│   ├── _tokens.scss           # Design tokens ONP (colores, tipografía, espaciado)
│   ├── _mat-theme.scss        # Tema Angular Material personalizado ONP
│   └── styles.scss            # Entry point de estilos globales
├── index.html
└── main.ts
```

### 4.1 Standalone Components

Todo componente nuevo debe ser **Standalone** (introducido como default en Angular 17):

```typescript
@Component({
  selector: 'onp-user-list',
  standalone: true,
  imports: [CommonModule, MatTableModule, RouterLink],
  templateUrl: './user-list.component.html',
})
export class UserListComponent { }
```

No se crean `NgModule` para features nuevas. Los módulos existentes en proyectos legacy se migran de forma incremental.

### 4.2 Estructura interna de un feature

Cada feature dentro de `features/` sigue esta estructura:

```
features/<dominio>/
├── components/           # Componentes presentacionales del dominio
├── pages/                # Componentes de ruta (routed components)
│   ├── list/
│   └── detail/
├── services/             # Servicios HTTP del dominio
├── models/               # Interfaces TypeScript (DTOs)
└── <dominio>.routes.ts   # Rutas lazy del feature
```

## 5. Convenciones de nomenclatura

### 5.1 Archivos

| Tipo | Sufijo | Ejemplo |
|---|---|---|
| Componente | `.component.ts` | `user-list.component.ts` |
| Servicio | `.service.ts` | `user.service.ts` |
| Guard | `.guard.ts` | `auth.guard.ts` |
| Interceptor | `.interceptor.ts` | `auth.interceptor.ts` |
| Pipe | `.pipe.ts` | `date-format.pipe.ts` |
| Directiva | `.directive.ts` | `highlight.directive.ts` |
| Modelo/DTO | `.model.ts` | `user.model.ts` |
| Rutas | `.routes.ts` | `user.routes.ts` |

**Reglas generales:**
- Nombres en `kebab-case` para archivos y carpetas.
- Nombres en `PascalCase` para clases TypeScript.
- Un componente o servicio por archivo.

### 5.2 Selectores de componentes

Todos los componentes usan el prefijo `onp-`:

```typescript
selector: 'onp-user-list'
selector: 'onp-search-bar'
selector: 'onp-error-page'
```

### 5.3 Prefijos de elementos de interfaz

Para variables de template y referencias a controles, se aplican los prefijos del estándar ONP:

| Prefijo | Tipo de control | Ejemplo |
|---|---|---|
| `btn` | Botón | `btnGuardar`, `btnCancelar` |
| `txt` | Campo de texto | `txtNombre`, `txtDni` |
| `lbl` | Etiqueta | `lblTitulo` |
| `lst` | Lista / select | `lstEstado`, `lstTipoDoc` |
| `chk` | Checkbox | `chkActivo` |
| `rad` | Radio button | `radTipoPersona` |
| `tar` | Área de texto | `tarObservacion` |
| `frm` | Formulario | `frmRegistro` |
| `tbl` | Tabla | `tblUsuarios` |
| `dlg` | Diálogo / modal | `dlgConfirmacion` |

### 5.4 Interfaces y modelos

| Sufijo | Uso | Ejemplo |
|---|---|---|
| `Response` | DTO de respuesta API | `UserResponse` |
| `Request` | DTO de petición API | `CreateUserRequest` |
| Sin sufijo | Modelo de dominio interno | `User` |

### 5.5 Signals

Las propiedades reactivas basadas en Signals usan nombres descriptivos sin sufijo especial:

```typescript
users        = signal<User[]>([]);
isLoading    = signal(false);
selectedUser = signal<User | null>(null);

// computed: nombre que exprese la derivación
activeUsers = computed(() => this.users().filter(u => u.active));
```

## 6. Layout estándar ONP

Todo sistema web ONP implementa un layout de cuatro zonas:

```
┌─────────────────────────────────────────────────────────────┐
│  CABECERA: Logo ONP | Nombre del sistema | Usuario | Fecha  │
│            Breadcrumb                    | Cerrar sesión     │
├──────────────┬──────────────────────────────────────────────┤
│              │                                              │
│  MENÚ        │  CONTENIDO                                   │
│  GENERAL     │  (cambia por ruta)                           │
│  (lateral    │                                              │
│   izquierdo) │                                              │
│              │                                              │
├──────────────┴──────────────────────────────────────────────┤
│  PIE DE PÁGINA: ONP — Oficina de Normalización Previsional  │
└─────────────────────────────────────────────────────────────┘
```

### 6.1 Implementación en Angular

El layout se implementa como shell component en `layout/`. La ruta de login no usa el shell (no tiene menú ni cabecera completa):

```typescript
// app.routes.ts
export const routes: Routes = [
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: 'home',     loadComponent: () => import('./features/home/...') },
      { path: 'usuarios', loadChildren: () => import('./features/usuarios/usuario.routes') },
    ],
  },
  { path: 'login',  loadComponent: () => import('./features/auth/login/...') },
  { path: 'error/:code', loadComponent: () => import('./shared/components/error-page/...') },
  { path: '**',     loadComponent: () => import('./shared/components/not-found/...') },
];
```

### 6.2 Cabecera

Elementos obligatorios:
- Logo ONP.
- Nombre del sistema (cargado desde `environment.systemName`).
- Nombre y rol del usuario autenticado.
- Fecha y hora actual (actualización cada minuto).
- Enlace "Cerrar sesión".
- Breadcrumb dinámico generado desde el router.

### 6.3 Menú general

- Menú lateral izquierdo jerárquico, dos niveles máximo.
- Implementado con `MatSidenav` + `MatNavList` (Angular Material).
- Items definidos en configuración, filtrados por roles del usuario.
- Estado colapsado/expandido persiste en `localStorage`.

### 6.4 Pie de página

Contenido fijo mínimo:

```
Oficina de Normalización Previsional — OTI  |  v{version}  |  {año}
```

## 7. Tipos de vista estándar

ONP define nueve tipos de vista. Todo componente de ruta debe encuadrarse en uno de ellos:

| Tipo | Ruta típica | Descripción |
|---|---|---|
| **Login** | `/login` | Formulario de autenticación. Sin menú lateral ni footer |
| **Bienvenida** | `/home` | Pantalla inicial post-login. Logo del sistema, accesos directos |
| **Listado** | `/<dominio>/list` | Tabla paginada con filtros de búsqueda |
| **Formulario** | `/<dominio>/new`, `/<dominio>/:id/edit` | Alta o edición de entidad |
| **Detalle** | `/<dominio>/:id` | Vista de solo lectura de una entidad |
| **Error** | `/error/:code` | Pantalla de error HTTP (404, 500, 403, etc.) |
| **Notificación** | Modal/dialog | Confirmaciones, alertas, resultados de operación |
| **Acordeón/Tab** | Sub-secciones dentro de formulario o detalle | Contenido extenso en secciones colapsables |
| **Reporte** | `/<dominio>/report` | Vista optimizada para impresión (ver §11) |

### 7.1 Vista de Listado

Estructura estándar:

```
┌─ Título de la sección ─────────────────────────────────────┐
│  [Filtros de búsqueda]  [Buscar]  [Limpiar]                │
├────────────────────────────────────────────────────────────┤
│  [Nuevo]                                                   │
│  Tabla: columnas | acciones (Ver / Editar / Eliminar)      │
│  Paginación: elementos por página | navegación de páginas  │
└────────────────────────────────────────────────────────────┘
```

Componentes Angular Material: `MatTable`, `MatPaginator`, `MatSort`, `MatFormField`.

### 7.2 Vista de Formulario

- Usar `ReactiveFormsModule` con `FormBuilder`. No usar template-driven forms.
- Validaciones declaradas en el grupo de formulario; mensajes de error bajo cada campo.
- Botones: **Guardar** (acción principal) + **Cancelar** (navega al listado).
- Campos requeridos marcados con asterisco (*) en el label.

```typescript
form = this.fb.group({
  nombre: ['', [Validators.required, Validators.maxLength(100)]],
  dni:    ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
});
```

### 7.3 Vista de Error

Estructura mínima por código HTTP:

| Código | Título | Mensaje |
|---|---|---|
| 400 | Solicitud incorrecta | Los datos enviados no son válidos |
| 401 | No autenticado | Su sesión ha expirado. Inicie sesión nuevamente |
| 403 | Acceso denegado | No tiene permiso para acceder a este recurso |
| 404 | No encontrado | El recurso solicitado no existe |
| 500 | Error del servidor | Se produjo un error interno. Contacte a soporte |
| 503 | Servicio no disponible | El servicio está temporalmente fuera de línea |

Toda vista de error incluye un botón **Regresar** que navega al historial anterior o a `/home`.

## 8. Diseño visual — Design Tokens ONP

Los design tokens centralizan los valores visuales. Se definen en `styles/_tokens.scss` como custom properties CSS para que sean accesibles globalmente.

### 8.1 Colores institucionales

```scss
// styles/_tokens.scss
:root {
  // Colores primarios ONP
  --onp-primary:        [PLACEHOLDER — color primario institucional];
  --onp-primary-dark:   [PLACEHOLDER — variante oscura del primario];
  --onp-primary-light:  [PLACEHOLDER — variante clara del primario];

  // Color de acento
  --onp-accent:         [PLACEHOLDER — color de acento/secundario];

  // Semánticos (estos no cambian por branding)
  --onp-success:        #2e7d32;
  --onp-warning:        #f57c00;
  --onp-error:          #c62828;
  --onp-info:           #1565c0;

  // Neutros
  --onp-bg-page:        #f5f5f5;
  --onp-bg-surface:     #ffffff;
  --onp-text-primary:   #212121;
  --onp-text-secondary: #757575;
  --onp-divider:        #e0e0e0;
}
```

### 8.2 Tipografía

```scss
:root {
  --onp-font-family:    [PLACEHOLDER — familia tipográfica ONP], sans-serif;
  --onp-font-size-base: 14px;
  --onp-font-size-sm:   12px;
  --onp-font-size-lg:   16px;
  --onp-font-size-xl:   20px;
  --onp-font-size-h1:   24px;
  --onp-font-size-h2:   20px;
  --onp-font-size-h3:   16px;
  --onp-line-height:    1.5;
}
```

### 8.3 Espaciado y breakpoints

```scss
// Escala de espaciado (múltiplos de 4px — alineada con Tailwind spacing scale)
$spacing-1: 4px;
$spacing-2: 8px;
$spacing-3: 12px;
$spacing-4: 16px;
$spacing-6: 24px;
$spacing-8: 32px;

// Breakpoints (compatibles con Tailwind CSS defaults)
$bp-sm:  640px;    // móvil horizontal
$bp-md:  768px;    // tablet
$bp-lg:  1024px;   // desktop
$bp-xl:  1280px;   // desktop wide
```

### 8.4 Tema Angular Material

```scss
// styles/_mat-theme.scss
@use '@angular/material' as mat;

$onp-primary-palette: mat.define-palette(mat.$[PLACEHOLDER]-palette);
$onp-accent-palette:  mat.define-palette(mat.$[PLACEHOLDER]-palette);

$onp-theme: mat.define-light-theme((
  color: (
    primary: $onp-primary-palette,
    accent:  $onp-accent-palette,
  ),
  typography: mat.define-typography-config(
    $font-family: var(--onp-font-family),
  ),
  density: 0,
));

@include mat.all-component-themes($onp-theme);
```

### 8.5 Tailwind CSS — configuración

```javascript
// tailwind.config.js
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        'onp-primary': 'var(--onp-primary)',
        'onp-accent':  'var(--onp-accent)',
        'onp-success': 'var(--onp-success)',
        'onp-error':   'var(--onp-error)',
      },
      fontFamily: {
        onp: 'var(--onp-font-family)',
      },
    },
  },
  // Evitar conflictos con Angular Material (preflight resetea estilos base)
  corePlugins: {
    preflight: false,
  },
};
```

> **Importante:** `preflight: false` es obligatorio cuando se usa Tailwind junto con Angular Material. El CSS reset de Tailwind (preflight) interfiere con los estilos base de Angular Material.

## 9. Integración con APIs REST

### 9.1 Modelo de respuesta estándar

Todas las APIs REST de ONP responden con `ApiResponseWrapper` (definido en LIN-DEV-JAVA-001 §11.4.4). El frontend lo tipifica así:

```typescript
// shared/models/api-response.model.ts
export interface ApiResponse<T> {
  codHttp:         number;
  codDetRespuesta: string;
  menDetRespuesta: string;
  data:            T | null;
  errors:          ApiError[] | null;
  meta:            ApiMeta | null;
}

export interface ApiError {
  campo:   string;
  mensaje: string;
}

export interface ApiMeta {
  timestamp:      string;
  requestId:      string;
  version:        string;
  pagina?:        number;
  tamanio?:       number;
  totalElementos?: number;
  totalPaginas?:  number;
}
```

### 9.2 Servicio base HTTP

Todos los servicios de dominio delegan en un servicio base que encapsula el `HttpClient` y estandariza los tipos de retorno:

```typescript
// core/services/api-base.service.ts
@Injectable({ providedIn: 'root' })
export class ApiBaseService {
  private http = inject(HttpClient);

  get<T>(path: string, params?: HttpParams): Observable<ApiResponse<T>> {
    return this.http.get<ApiResponse<T>>(path, { params });
  }

  post<T>(path: string, body: unknown): Observable<ApiResponse<T>> {
    return this.http.post<ApiResponse<T>>(path, body);
  }

  put<T>(path: string, body: unknown): Observable<ApiResponse<T>> {
    return this.http.put<ApiResponse<T>>(path, body);
  }

  delete<T>(path: string): Observable<ApiResponse<T>> {
    return this.http.delete<ApiResponse<T>>(path);
  }
}
```

Ejemplo de servicio de dominio:

```typescript
// features/usuarios/services/usuario.service.ts
@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private api    = inject(ApiBaseService);
  private base   = `${environment.apiUrl}/usuarios`;

  listar(filtros: FiltroUsuarioRequest): Observable<ApiResponse<Usuario[]>> {
    return this.api.get<Usuario[]>(this.base, toHttpParams(filtros));
  }

  crear(request: CreateUsuarioRequest): Observable<ApiResponse<Usuario>> {
    return this.api.post<Usuario>(this.base, request);
  }

  actualizar(id: number, request: UpdateUsuarioRequest): Observable<ApiResponse<Usuario>> {
    return this.api.put<Usuario>(`${this.base}/${id}`, request);
  }

  eliminar(id: number): Observable<ApiResponse<void>> {
    return this.api.delete<void>(`${this.base}/${id}`);
  }
}
```

### 9.3 Interceptor de autenticación

```typescript
// core/interceptors/auth.interceptor.ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken(); // token opaco de SAA — no es JWT, no decodificar en cliente

  if (token) {
    req = req.clone({
      // Esquema Bearer requerido por LIN-API-REST-001 §7.1.
      // El backend valida el token contra el endpoint SAA (SaaTokenValidationFilter).
      setHeaders: { Authorization: `Bearer ${token}` },
    });
  }
  return next(req);
};
```

### 9.4 Interceptor de errores HTTP

```typescript
// core/interceptors/error.interceptor.ts
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router   = inject(Router);
  const notifier = inject(NotificationService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      switch (error.status) {
        case 401:
          router.navigate(['/login']);
          break;
        case 403:
          router.navigate(['/error/403']);
          break;
        case 500:
        case 503:
          notifier.showError('Error del servidor. Intente nuevamente más tarde.');
          break;
      }
      return throwError(() => error);
    })
  );
};
```

### 9.5 Registro de interceptores

```typescript
// app.config.ts
export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withInterceptors([correlationInterceptor, authInterceptor, errorInterceptor])
    ),
    provideRouter(routes),
    provideAnimations(),
  ],
};
```

> El orden importa: `correlationInterceptor` debe ir primero para que el header `X-Request-ID` esté presente incluso en requests rechazados por `authInterceptor`. Ver §15.1 para la implementación.

### 9.6 Interpretación de codDetRespuesta

El componente que consume un servicio es responsable de interpretar `codDetRespuesta` para determinar el feedback al usuario:

| Rango `codDetRespuesta` | Significado | Acción en frontend |
|---|---|---|
| `000`, `200` | Éxito | Snackbar de éxito o navegar al listado |
| `100`–`103` | Error de validación (400) | Mostrar errores por campo en el formulario |
| `200`–`203` | Error de negocio (422/409/404) | Mostrar mensaje descriptivo al usuario |
| `300`–`301` | Error de autenticación (401/403) | Redirigir a login o página de error 403 |
| `400`–`402` | Error de integración (503/502/504) | Snackbar de error de servicio externo |
| `500`–`502` | Error de sistema (500) | Snackbar de error genérico |

## 10. Manejo de errores y feedback

### 10.1 NotificationService

Centraliza el feedback al usuario. Usa `MatSnackBar` para mensajes transitorios:

```typescript
// core/services/notification.service.ts
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private snackBar = inject(MatSnackBar);

  showSuccess(message: string): void {
    this.snackBar.open(message, 'Cerrar', {
      duration: 4000,
      panelClass: ['snack-success'],
    });
  }

  showError(message: string): void {
    this.snackBar.open(message, 'Cerrar', {
      duration: 6000,
      panelClass: ['snack-error'],
    });
  }

  showWarning(message: string): void {
    this.snackBar.open(message, 'Cerrar', {
      duration: 5000,
      panelClass: ['snack-warning'],
    });
  }
}
```

### 10.2 Errores de validación en formularios

Los errores de validación retornados por la API (`codDetRespuesta` 100-103) se mapean a los campos del formulario:

```typescript
private mapApiErrors(errors: ApiError[], form: FormGroup): void {
  errors.forEach(error => {
    const control = form.get(error.campo);
    if (control) {
      control.setErrors({ apiError: error.mensaje });
    }
  });
}
```

Template del campo con error de API:

```html
<mat-form-field>
  <mat-label>DNI</mat-label>
  <input matInput formControlName="dni" />
  <mat-error *ngIf="form.get('dni')?.hasError('required')">
    Campo requerido
  </mat-error>
  <mat-error *ngIf="form.get('dni')?.hasError('apiError')">
    {{ form.get('dni')?.getError('apiError') }}
  </mat-error>
</mat-form-field>
```

### 10.3 Indicador de carga

Todo llamado HTTP muestra un indicador de carga mediante Signal local:

```typescript
isLoading = signal(false);

cargarDatos(): void {
  this.isLoading.set(true);
  this.usuarioService.listar(this.filtros()).pipe(
    finalize(() => this.isLoading.set(false))
  ).subscribe(response => {
    this.usuarios.set(response.data ?? []);
  });
}
```

```html
<mat-progress-bar *ngIf="isLoading()" mode="indeterminate" />
```

## 11. Reportes imprimibles

Los reportes son vistas Angular optimizadas para impresión mediante `@media print`. No se requiere librería de PDF del lado cliente; el navegador imprime la vista directamente.

### 11.1 Estructura del reporte

```
CABECERA
  Nombre de la institución: Oficina de Normalización Previsional
  Unidad organizacional / proceso
  Nombre del sistema
  Fecha y hora de generación
  Número de página

CONTENIDO
  Título del reporte
  Filtros aplicados (columna izquierda) | Datos de contexto (columna derecha)
  Tabla de datos:
    - <thead> con <th> para encabezados
    - <tbody> con filas alternadas (odd/even)
  Total de registros encontrados
  Marca de fin: *** FIN DE REPORTE : <NOMBRE DEL REPORTE> ***

PIE DE PÁGINA (datos de impresión)
  Usuario que generó el reporte
  Dirección IP
  Ambiente (DEV / QA / PROD)
```

### 11.2 CSS de impresión

```scss
// En el componente de reporte
@media print {
  .no-print     { display: none !important; }  // oculta menú, cabecera principal, botones
  
  table         { width: 100%; border-collapse: collapse; }
  thead         { display: table-header-group; }  // repite encabezado en cada página
  th            { background-color: #e0e0e0; font-weight: bold; }
  tr:nth-child(even) { background-color: #f9f9f9; }
  td, th        { padding: 4px 8px; font-size: 10px; border: 1px solid #ccc; }

  @page {
    size: A4 landscape;
    margin: 1.5cm;
  }
}
```

### 11.3 Ruta de reporte

La vista de reporte se abre en ventana separada para facilitar la impresión:

```typescript
abrirReporte(): void {
  const params = new URLSearchParams(this.filtrosActivos()).toString();
  window.open(`/reportes/usuarios?${params}`, '_blank');
}
```

La ruta de reporte está fuera del `ShellComponent` (sin menú ni cabecera principal) para que la impresión solo incluya el contenido del reporte.

## 12. Accesibilidad

Todo componente desarrollado para ONP debe cumplir **WCAG 2.1 nivel AA** como mínimo.

### 12.1 Reglas obligatorias

| Criterio | Implementación |
|---|---|
| Contraste de color | Relación mínima 4.5:1 para texto normal, 3:1 para texto grande |
| Navegación por teclado | Todos los controles interactivos accesibles con Tab / Enter / Space |
| Etiquetas ARIA | `aria-label` en iconos sin texto visible; `aria-describedby` en campos con texto de ayuda |
| Roles semánticos | Usar `<button>` para acciones, `<a>` para navegación. No usar `<div>` o `<span>` como botón |
| Mensajes de error | Anunciados por lectores de pantalla (`role="alert"` o `aria-live="polite"`) |
| Imágenes | `alt` descriptivo en imágenes de contenido; `alt=""` en imágenes decorativas |
| Formularios | Todo `<input>` vinculado a su `<label>` mediante `MatFormField` o atributos `for`/`id` |

Angular Material implementa ARIA por defecto. Usar siempre los componentes Material en lugar de HTML nativo para controles complejos (datepicker, select, dialog, chips, etc.).

### 12.2 Validación

Antes de entregar un sistema a producción, ejecutar al menos una auditoría con:
- **axe DevTools** (extensión Chrome) sobre las páginas principales.
- **Lighthouse** (Chrome DevTools → pestaña Accessibility) con puntaje objetivo ≥ 90.

## 13. Configuración por entorno

### 13.1 Archivos de entorno

```typescript
// environments/environment.ts (Desarrollo — default)
export const environment = {
  production:  false,
  apiUrl:      'http://localhost:8080/api/v1',
  systemName:  'Nombre del Sistema',
  version:     '0.1.0',
};

// environments/environment.qa.ts
export const environment = {
  production:  false,
  apiUrl:      'https://qa.onp.gob.pe/api/v1',
  systemName:  'Nombre del Sistema',
  version:     '0.1.0',
};

// environments/environment.prod.ts
export const environment = {
  production:  true,
  apiUrl:      'https://sistemas.onp.gob.pe/api/v1',
  systemName:  'Nombre del Sistema',
  version:     '0.1.0',
};
```

### 13.2 Reglas de configuración

- Nunca incluir credenciales, tokens o secrets en los archivos `environment.ts`.
- La URL base de la API (`apiUrl`) nunca se hardcodea en los servicios; siempre se lee desde `environment`.
- En un despliegue Kubernetes, la `apiUrl` puede inyectarse mediante variable de entorno en el `ConfigMap` del Nginx que sirve la SPA (reemplazo en tiempo de inicio de contenedor).

### 13.3 Construcción por entorno

```bash
ng build --configuration=qa
ng build --configuration=production
```

## 14. Pruebas

### 14.1 Pruebas unitarias

Mínimo requerido por tipo de artefacto:

| Elemento | Qué probar |
|---|---|
| Componente de listado | Renderiza filas de datos; filtros activan búsqueda |
| Componente de formulario | Validaciones requeridas; submit llama al servicio con datos correctos |
| Servicio HTTP | Construye la URL correcta; mapea la respuesta al tipo esperado |
| Interceptor de errores | Redirige a `/login` ante 401; llama a `NotificationService` ante 500 |
| Guard de autenticación | Bloquea la ruta sin token; permite con token válido |

Cobertura mínima: **70% de líneas** en `core/` y `shared/`. Las páginas de features se validan con pruebas e2e.

### 14.2 Pruebas e2e

La herramienta institucional preferente para pruebas E2E es **Playwright** (ver `LIN-TEST-001 §4.4`). Cypress solo se permite en proyectos donde ya existe; proyectos nuevos deben usar Playwright.

Estructura de carpetas con Playwright:

```
e2e/
├── auth/
│   └── login.spec.ts          # login exitoso, credenciales incorrectas
├── <dominio>/
│   ├── listado.spec.ts        # filtros, paginación, navegación a detalle
│   └── formulario.spec.ts     # alta exitosa, errores de validación
```

Flujos obligatorios a cubrir por cada feature:

- Login exitoso → redirección a Home.
- Login fallido → mensaje de error visible.
- Listado con filtros → resultados correctos.
- Alta exitosa → snackbar de confirmación + regreso al listado.
- Alta con datos inválidos → errores visibles por campo.
- Acceso a ruta protegida sin sesión → redirección a `/login`.

---

## 15. Observabilidad y performance

El **LIN-ARQ-000 §9.5** establece que todo sistema que llega a producción en ONP debe implementar los cuatro pilares de observabilidad sin excepción. Para una SPA Angular, estos pilares se traducen de la siguiente manera:

| Pilar (Architecture §9.5) | Aplicabilidad en SPA Angular |
|---|---|
| **Trazas distribuidas** | El SPA no genera spans propios, pero **debe propagar `X-Request-ID`** en cada request HTTP para que los traces del backend sean correlacionables |
| **Logs estructurados** | No aplica al browser directamente; los errores JS se capturan y envían al backend via API |
| **Métricas** | Core Web Vitals medidos con Lighthouse — gate obligatorio en CI/CD |
| **Health checks** | No aplica (no hay servidor propio en el SPA) |

### 15.1 Interceptor de correlación (X-Request-ID)

Cada request HTTP saliente **debe incluir el header `X-Request-ID`**. Si el request viene de una acción iniciada por el usuario, se genera un UUID nuevo; si se encadena desde una respuesta previa, se reutiliza el ID recibido. Esto permite correlacionar la acción del usuario con el trace distribuido en Jaeger.

```typescript
// core/interceptors/correlation.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { tap } from 'rxjs/operators';

export const correlationInterceptor: HttpInterceptorFn = (req, next) => {
  const requestId = crypto.randomUUID();

  const cloned = req.clone({
    setHeaders: { 'X-Request-ID': requestId },
  });

  return next(cloned).pipe(
    tap({
      error: (err) => {
        // El requestId ya viaja en el header — el backend lo devuelve en meta.requestId
        // para facilitar búsquedas en Kibana/Jaeger si el usuario reporta un error.
      },
    })
  );
};
```

El interceptor se registra **antes** de `authInterceptor` y `errorInterceptor`:

```typescript
// app.config.ts
provideHttpClient(
  withInterceptors([correlationInterceptor, authInterceptor, errorInterceptor])
),
```

> El backend devuelve el mismo `X-Request-ID` en el campo `meta.requestId` de `ApiResponseWrapper`. Cuando un usuario reporta un error, ese valor es suficiente para localizar el trace en Jaeger y el log en Kibana.

### 15.2 Core Web Vitals

ONP adopta Core Web Vitals como framework de medición de performance frontend. Los umbrales son **obligatorios** y se miden con Lighthouse. Un build que no los cumple **no pasa a producción**.

| Métrica | Qué mide | Umbral ONP |
|---|---|---|
| **LCP** (Largest Contentful Paint) | Tiempo hasta que el contenido principal es visible | < 2.5 s |
| **INP** (Interaction to Next Paint) | Tiempo de respuesta a interacciones del usuario | < 200 ms |
| **CLS** (Cumulative Layout Shift) | Estabilidad visual — elementos que no saltan | < 0.1 |
| **FCP** (First Contentful Paint) | Primer elemento visible en pantalla | < 1.8 s |
| **TTI** (Time to Interactive) | Cuando la página responde completamente | < 3.5 s |
| **TBT** (Total Blocking Time) | Tiempo que el hilo principal está bloqueado | < 200 ms |
| **FPS en animaciones** | Fluidez de transiciones | ≥ 60 fps |

#### Técnicas obligatorias para cumplir los umbrales

- **Lazy loading de rutas:** cada feature module se carga solo cuando el usuario navega a él (`loadComponent` / `loadChildren`).
- **Code splitting:** no se genera un bundle único — Angular CLI divide por ruta de manera automática con la configuración por defecto.
- **Tree shaking:** importar únicamente lo que se usa; prohibido `import * from 'librería'`.
- **Imágenes:** formato WebP, atributo `loading="lazy"`, dimensiones explícitas para evitar CLS.
- **Compresión:** gzip o brotli habilitado en el servidor nginx que sirve los assets estáticos.
- **Cache de assets:** los nombres de archivo generados por `ng build` incluyen hash — no configurar `max-age=0` en el servidor.

#### Optimización nativa del LCP

LCP es la métrica más exigente en Angular porque el framework renderiza en el cliente por defecto: el navegador descarga, parsea y ejecuta JavaScript antes de mostrar contenido. Las técnicas base reducen el bundle, pero no eliminan ese tiempo de arranque. Para atacar el LCP directamente:

| Técnica | Qué hace | Cuándo aplicar en ONP |
|---|---|---|
| `<link rel="preload">` | Le indica al navegador que descargue un recurso crítico (imagen hero, fuente) antes de que el parser lo encuentre | Siempre que el elemento LCP sea una imagen o fuente externa |
| `<link rel="preconnect">` | Establece conexión TCP anticipada con dominios externos | Cuando se consumen APIs o recursos de dominios distintos al propio |
| `loading="eager"` en imagen LCP | Evita que la imagen principal quede en lazy load accidentalmente | El elemento LCP **nunca** debe tener `loading="lazy"` |
| SSR con Angular Universal | Entrega HTML prerenderizado desde el servidor — el navegador muestra contenido sin esperar JavaScript | Solo para portales públicos ONP. No aplica a sistemas internos: agrega un servidor Node que mantener y requiere madurez en el equipo |

#### Estabilidad del CLS con bloques deferibles (`@defer`)

Angular 17+ introduce `@defer`, que permite diferir la carga de bloques de template hasta que se cumpla una condición (`on viewport`, `on interaction`, `on idle`). Usado correctamente mejora el CLS y el TBT; usado incorrectamente los empeora.

**El riesgo directo con CLS — uso incorrecto:**

```html
@defer {
  <componente-pesado />    <!-- tamaño real: 400px de alto -->
}
@placeholder {
  <div>Cargando...</div>   <!-- tamaño: 20px de alto -->
}
```

Cuando el componente real reemplaza al placeholder, el layout salta 380px → CLS alto. El `@defer` sin reserva de espacio explícita genera exactamente el problema que se quiere evitar.

**Uso correcto — reservar el espacio del contenido real:**

```html
@defer (on viewport) {
  <componente-pesado />
} @placeholder (minimum 300ms) {
  <div style="height: 400px; width: 100%;">
    <app-skeleton-loader />
  </div>
}
```

**Reglas obligatorias para usar `@defer` sin romper el CLS:**

| Regla | Razón |
|---|---|
| El `@placeholder` debe reservar exactamente el mismo espacio que el componente real | Si el tamaño difiere, el reemplazo causa layout shift |
| Usar `skeleton loaders` con dimensiones fijas en el placeholder | Son visualmente consistentes y mantienen el espacio reservado |
| No usar `@defer` en elementos above-the-fold sin SSR | El contenido visible al cargar nunca debe deferirse — es el candidato LCP |
| Preferir `on viewport` sobre `on idle` para contenido visible | `on idle` puede cargar cuando el usuario ya está viendo el área, causando shift inesperado |

#### Configuración de Lighthouse CI

El archivo `lighthouserc.js` en la raíz del proyecto define los thresholds como gate de calidad:

```javascript
// lighthouserc.js
module.exports = {
  ci: {
    collect: {
      url: ['http://localhost:4200'],
      startServerCommand: 'npx http-server dist/onp-<sistema> -p 4200',
      numberOfRuns: 3,
    },
    assert: {
      assertions: {
        'categories:performance':       ['error', { minScore: 0.85 }],
        'categories:accessibility':     ['error', { minScore: 0.90 }],
        'first-contentful-paint':       ['error', { maxNumericValue: 1800 }],
        'largest-contentful-paint':     ['error', { maxNumericValue: 2500 }],
        'total-blocking-time':          ['error', { maxNumericValue: 200  }],
        'cumulative-layout-shift':      ['error', { maxNumericValue: 0.1  }],
        'interactive':                  ['error', { maxNumericValue: 3500 }],
      },
    },
    upload: {
      target: 'temporary-public-storage',
    },
  },
};
```

La integración de Lighthouse en el pipeline CI/CD se define en **LIN-CICD-001** (en borrador).

### 15.3 Captura de errores JavaScript no manejados

Los errores JS en runtime que no son capturados por `errorInterceptor` (errores de lógica, excepciones en componentes) deben registrarse para diagnóstico. Se implementa un `ErrorHandler` global que envía el error al backend:

```typescript
// core/services/global-error-handler.service.ts
import { ErrorHandler, Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  private http = inject(HttpClient);

  handleError(error: unknown): void {
    console.error(error);

    if (environment.production) {
      this.http.post(`${environment.apiBase}/v1/logs/frontend-error`, {
        message: error instanceof Error ? error.message : String(error),
        stack:   error instanceof Error ? error.stack   : null,
        url:     window.location.href,
        userAgent: navigator.userAgent,
      }).subscribe({ error: () => {} });
    }
  }
}
```

Registro en `app.config.ts`:

```typescript
{ provide: ErrorHandler, useClass: GlobalErrorHandler },
```

> En entornos DEV y QA el error se muestra en consola pero **no** se envía al backend para evitar ruido. Solo en `production: true` se activa el envío.

### 15.4 Checklist mínimo antes de pasar a producción

Equivalente del checklist de Architecture §9.5, aplicado al SPA Angular:

- [ ] `correlationInterceptor` registrado como primer interceptor en `app.config.ts`
- [ ] `GlobalErrorHandler` registrado en `app.config.ts`
- [ ] `lighthouserc.js` presente en la raíz del proyecto con los umbrales ONP
- [ ] `ng build --configuration production` sin errores ni warnings de bundle size
- [ ] Lighthouse ejecutado contra el build de producción — todos los assertions en verde
- [ ] Lazy loading verificado: el bundle inicial (`main.js`) no supera **200 KB** (gzip)
- [ ] Imágenes en formato WebP con `loading="lazy"`
- [ ] Header `X-Request-ID` visible en DevTools → Network para al menos un request de prueba

---

## 16. Contenedorización

Todo frontend Angular desplegado en Kubernetes debe ejecutarse como contenedor no root, usando un puerto no privilegiado. Ejecutar Nginx en el puerto 80 dentro de un contenedor requiere privilegios de root, lo que es incompatible con las directivas de seguridad del clúster corporativo (PodSecurityPolicy / SecurityContext con `runAsNonRoot: true`).

### 16.1 Imagen base

Usar `nginxinc/nginx-unprivileged` en lugar de `nginx` oficial.

| Imagen | Puerto por defecto | Usuario | Compatible K8s corporativo |
|---|---|---|---|
| `nginx:alpine` | 80 | root | No — falla con `runAsNonRoot` |
| `nginxinc/nginx-unprivileged:alpine` | 8080 | uid 101 (no root) | Sí |

### 16.2 nginx.conf

Crear `nginx.conf` en la raíz del proyecto con configuración para puerto 8080 y directorios temporales accesibles por usuario no root:

```nginx
worker_processes auto;
error_log  /var/log/nginx/error.log warn;
pid        /tmp/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include      /etc/nginx/mime.types;
    default_type application/octet-stream;

    # Directorios temporales fuera de /var/run — accesibles por uid 101
    client_body_temp_path /tmp/client_temp;
    proxy_temp_path       /tmp/proxy_temp_path;
    fastcgi_temp_path     /tmp/fastcgi_temp;
    uwsgi_temp_path       /tmp/uwsgi_temp;
    scgi_temp_path        /tmp/scgi_temp;

    sendfile       on;
    keepalive_timeout 65;

    gzip on;
    gzip_types text/plain text/css application/json application/javascript
               text/xml application/xml text/javascript;

    server {
        listen      8080;
        server_name _;

        root  /usr/share/nginx/html;
        index index.html;

        # SPA routing — redirige rutas Angular al index
        location / {
            try_files $uri $uri/ /index.html;
        }

        # Cache agresivo para assets con hash de contenido
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
}
```

### 16.3 Dockerfile

Multi-stage build: stage de compilación con Node y stage de servicio con Nginx no root.

```dockerfile
# ── Stage 1: compilación ──────────────────────────────────────────────────────
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration production

# ── Stage 2: servicio ─────────────────────────────────────────────────────────
FROM nginxinc/nginx-unprivileged:1.27-alpine
COPY --from=builder /app/dist/<nombre-proyecto>/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 8080
```

> Reemplazar `<nombre-proyecto>` por el nombre real del proyecto Angular (`outputPath` en `angular.json`).

La imagen `nginx-unprivileged` ya ejecuta Nginx como uid 101. No es necesario declarar `USER` explícitamente.

### 16.4 Manifiestos Kubernetes

El Service y el Deployment deben referenciar el puerto 8080:

```yaml
# Service
spec:
  ports:
    - port: 80
      targetPort: 8080

# Deployment — SecurityContext recomendado
securityContext:
  runAsNonRoot: true
  runAsUser: 101
  allowPrivilegeEscalation: false
  readOnlyRootFilesystem: false   # nginx necesita escribir en /tmp
```

### 16.5 Anti-patrón

| Anti-patrón | Riesgo |
|---|---|
| `FROM nginx:alpine` sin usuario no root | Falla en clústeres con `runAsNonRoot: true` (OpenShift, K8s corporativo) |
| `listen 80` en nginx.conf con imagen unprivileged | El proceso no puede bindear puertos < 1024 — el contenedor no arranca |
| Omitir `client_body_temp_path` y similares en `/tmp` | nginx intenta escribir en `/var/cache/nginx` sin permisos — error al iniciar |

---

## Proceso de excepción a este estándar

Toda desviación de las reglas establecidas en este documento requiere un ADR (Architecture Decision Record) aprobado formalmente por el equipo de Arquitectura de la OTI antes de implementarse.

El ADR debe incluir: contexto, decisión, alternativas evaluadas, consecuencias, vigencia de la excepción, responsable y fecha de revisión.

Casos que siempre requieren ADR en este estándar:

- Uso de React o Vue en lugar de Angular
- Uso de una librería de componentes distinta a Angular Material
- Omisión de lazy loading por feature
- Incumplimiento de umbrales Core Web Vitals con justificación documentada

**No se acepta la urgencia como justificación para omitir este proceso.**

---

*LIN-FE-ANG-001 — Estándar de Diseño Web Frontend ONP v0.1.0*  
*OTI — Oficina de Tecnologías de la Información*
