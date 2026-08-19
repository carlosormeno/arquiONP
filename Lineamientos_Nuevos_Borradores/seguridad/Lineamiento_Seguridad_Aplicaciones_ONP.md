# Lineamiento de Seguridad en Aplicaciones ONP
**Código:** LIN-SEC-APP-001
**Versión:** v0.1.8
**Estado:** En revisión — revisión de contenido cerrada (`GOB-CHK-001` H15); pendiente de graduación
**Fecha:** 2026-08-09
**Propietario:** Arquitectura de Software — OTI
**Revisores:** Seguridad de la Información, Arquitectura, Desarrollo
**Marco rector:** LIN-ARQ-001 — Marco Rector de Arquitectura de Software

---

## Control de cambios

| Versión | Fecha | Autor | Descripción |
|---|---|---|---|
| v0.1.0 | 2026-05-25 | Arquitectura OTI | Borrador inicial |
| v0.1.1 | 2026-05-28 | Arquitectura OTI | Alinea la configuración a YAML, documenta el comportamiento del filtro SAA ante fallos y referencia la transición gobernada hacia WSO2 |
| v0.1.2 | 2026-07-06 | Arquitectura OTI | Declara a LIN-ARQ-000 como marco rector (encabezado y §2), único lineamiento de Nivel 3 que no lo citaba. Corrige mención de Circuit Breaker: el patrón oficial es PT06, no PT07 (PT07 es Bulkhead); actualiza la referencia obsoleta "pendiente LIN-BUS-001" a LIN-ARQ-000 §3.7.3, donde Circuit Breaker ya está normado |
| v0.1.3 | 2026-07-10 | Arquitectura OTI | Revierte la corrección de v0.1.2: el código correcto de Circuit Breaker es **PT07**, no PT06 — PT06 es Retry y PT08 es Bulkhead, según el catálogo autoritativo `LIN-PAT-001` (fichas PAT-RES-01/02) y `Matriz_Propiedad_Documental_ONP`. Actualiza §8.7 y el glosario. Redirige además la referencia de Circuit Breaker desde el documento congelado `LIN-ARQ-000 §3.7.3` hacia `LIN-DIS-001 §6.2`, donde vive el contenido vigente |
| v0.1.4 | 2026-07-10 | Arquitectura OTI | Migra Marco rector de `LIN-ARQ-000` (congelado) a `LIN-ARQ-001` (vigente) en encabezado y §2; corrige la clasificación de "Nivel 2" a "Nivel 1" para el marco rector |
| v0.1.7 | 2026-08-09 | Arquitectura OTI | `§7.1` incorpora la **excepción acotada de tráfico intra-cluster** decidida en `ADR-TLS-INTERNO-001`, con sus tres condiciones (terminación TLS en el perímetro, `NetworkPolicy` obligatoria como control sustitutivo, migración a mTLS al habilitarse la malla) y sus límites explícitos. Resuelve la contradicción que `LIN-API-REST-001 §2.5` mantenía con este documento (`GOB-CHK-001` H24.4) |
| v0.1.8 | 2026-08-18 | Arquitectura OTI | El apartado de excepción titulaba «Proceso ADR para desviaciones» y no definía identificador: una desviación de este lineamiento se registraba como «un ADR», instrumento que `GOB-MAT-001` reserva a las decisiones **institucionales** del Comité. Pasa a **`EXC-SEC-NNN`**, con vigencia acotada y fecha de revisión obligatoria (`GOB-CHK-001` H38) |
| v0.1.6 | 2026-08-08 | Arquitectura OTI | `SaaTokenValidationFilter` pasa a `@Order(3)`, por dentro de `CanonicalRequestLogFilter` (`@Order(2)`), para que sus rechazos queden registrados en el log canónico (`GOB-CHK-001` H19.1). Publica la identidad también como atributo de request `onp.user.id`, porque el MDC se limpia al desapilarse el filtro. Actualizada la tabla de `§8.7.1`: los 401 y 503 **sí** producen log canónico |
| v0.1.5 | 2026-08-08 | Arquitectura OTI | Revisión de contenido (`GOB-CHK-001` H15). **(1) §9.3** publicaba un contrato de error paralelo (`ErrorResponse` con códigos `ERR-INTERNAL`/`ERR-FORBIDDEN`) que contradecía el contrato institucional y el catálogo `codDetRespuesta` — reescrito con `ApiResponseWrapper` y códigos `500`/`301`. **(2) §8.4** el cliente SAA usaba `RestTemplate` **sin timeouts**, en la ruta crítica de cada petición y contra el servicio que el propio §8.7 declara dependencia crítica — reescrito con `RestClient` sobre Apache HttpClient 5, con los umbrales de `LIN-DIS-001 §6.1` y Bulkhead de `§6.3`. **(3) §8.3** se documenta que `response.sendError()` no produce `ApiResponseWrapper` y cuál es la forma conforme. **(4) §8.5** el ejemplo DEV publicaba la URL del SAA en claro sobre **HTTP** contra un servidor compartido, violando su propio §7.1 (HTTPS obligatorio fuera de localhost) y §12.1 (esa URL es un secreto) — sustituida por variable de entorno. **(5)** Corrige la cita de PMD `LIN-DEV-JAVA-001 10.3` → `§12.3` |

---

## Tabla de contenido

1. [Objetivo y alcance](#1-objetivo-y-alcance)
2. [Normativa y documentos relacionados](#2-normativa-y-documentos-relacionados)
3. [Modelo de seguridad institucional](#3-modelo-de-seguridad-institucional)
4. [Autenticación](#4-autenticación)
5. [Autorización](#5-autorización)
6. [Tokens y sesiones](#6-tokens-y-sesiones)
7. [Seguridad de APIs](#7-seguridad-de-apis)
8. [Integración SAA para aplicaciones nuevas](#8-integración-saa-para-aplicaciones-nuevas)
9. [Seguridad en backend Java](#9-seguridad-en-backend-java)
10. [Seguridad en frontend Angular](#10-seguridad-en-frontend-angular)
11. [Seguridad en base de datos](#11-seguridad-en-base-de-datos)
12. [Gestión de secretos](#12-gestión-de-secretos)
13. [Dependencias y escaneo de vulnerabilidades](#13-dependencias-y-escaneo-de-vulnerabilidades)
14. [Controles para sistemas legacy](#14-controles-para-sistemas-legacy)
15. [Responsabilidades](#15-responsabilidades)
16. [Checklist de seguridad](#16-checklist-de-seguridad)
17. [Anti-patrones](#17-anti-patrones)
18. [Proceso ADR para desviaciones](#18-proceso-adr-para-desviaciones)
19. [Glosario](#19-glosario)

---

## 1. Objetivo y alcance

### 1.1 Objetivo

Este lineamiento define los controles mínimos de seguridad que deben aplicarse en el desarrollo, integración y despliegue de aplicaciones en la Oficina de Tecnologías de la Información (OTI) de la ONP. Su propósito es gobernar la convivencia entre el modelo de seguridad actual (basado en SAA y Active Directory) y el modelo objetivo (OAuth2/OIDC mediante WSO2 API Manager), evitando la creación de mecanismos paralelos no gobernados.

Este lineamiento **no reemplaza** las políticas institucionales de seguridad de la información de la ONP. Las aterriza en decisiones concretas de diseño y desarrollo de software.

### 1.2 Alcance

Aplica a:

| Componente | Ejemplos |
|---|---|
| Backend Java/Spring Boot | APIs REST, servicios internos, jobs, adaptadores |
| Frontend Angular | SPAs institucionales |
| Integraciones | Consumidores y proveedores de servicios |
| Sistemas legacy modernizados | Cuando son intervenidos funcionalmente |
| Nuevos desarrollos | Cualquier sistema que inicie desarrollo desde cero |

### 1.3 Fuera de alcance

| Tema | Responsable |
|---|---|
| Seguridad de infraestructura de red, firewalls, VPN | Plataforma / Seguridad |
| Seguridad física de servidores y centros de datos | Infraestructura |
| Políticas de contraseñas para usuarios finales | Seguridad de la Información |
| Auditoría interna de cumplimiento normativo | Órgano de Control |
| Pentest y evaluación de vulnerabilidades institucionales | Seguridad de la Información |

---

## 2. Normativa y documentos relacionados

| Documento | Código | Relevancia |
|---|---|---|
| Marco Rector de Arquitectura de Software | LIN-ARQ-001 | Documento rector de Nivel 1 — define Zero Trust y Defensa en Profundidad (§5.1), Observabilidad (§5.3) y el marco general que este lineamiento opera en materia de seguridad. Circuit Breaker/Resilience4j vive en `LIN-DIS-001 §6.2` |
| Lineamiento de Estándar APIs REST | LIN-API-REST-001 | HTTPS, headers, CORS, WSO2 |
| Lineamiento Estándar Desarrollo Java | LIN-DEV-JAVA-001 | Estructura de proyecto, secrets |
| Lineamiento Log, Trazabilidad y Observabilidad | LIN-OBS-001 | No PII en logs, propagación user.id |
| Lineamiento Estándar Base de Datos | LIN-BD-ORA-001 | Privilegios mínimos, acceso a esquemas |
| Lineamiento de Contenedores y Orquestación | LIN-K8S-001 | K8s Secrets, imágenes seguras |
| Lineamiento de CI/CD | LIN-CICD-001 | SAST, SCA, gates de seguridad |
| Ley N.° 29733 | — | Protección de Datos Personales |
| OWASP Top 10 | — | Referencia de vulnerabilidades web |
| Documento de Arquitectura SAA v2.0 | — | Sistema de Administración de Accesos |

---

## 3. Modelo de seguridad institucional

### 3.1 Estado actual

En el modelo vigente, el SAA actúa como referente institucional para autenticación, autorización y administración de accesos. Esta condición corresponde al estado actual de la ONP, no a un diseño permanente. En el modelo objetivo, estas capacidades deberán evolucionar progresivamente hacia un esquema gobernado por WSO2/OAuth2/OIDC, manteniendo interoperabilidad con SAA durante la transición.

El modelo vigente se compone de:

- **SAA (Sistema de Administración de Accesos):** Gestiona usuarios, roles, perfiles, permisos, sistemas y sesiones en el modelo actual. Su autoridad como sistema de identidad aplica mientras no exista un esquema sucesor aprobado por Arquitectura mediante ADR.
- **Active Directory:** Directorio de usuarios internos, accedido via LDAP. SAA lo consume para autenticar empleados de la ONP.
- **Token propietario SAA:** Sistema de tokens basado en semilla (`SAA/token/*`). No es JWT estándar ni OAuth2.
- **SOAP/EJB legacy:** Los servicios de autenticación históricos (`WSAutenticarUsuario`, `WSAutenticarAutorizarUsuario`) son de tipo SOAP.

### 3.2 Modelo objetivo

El modelo objetivo incorpora:

- **WSO2 API Manager** como gateway institucional: validación de tokens, rate limiting, scopes por operación, catálogo de APIs.
- **OAuth2/OIDC** como protocolo de autenticación y autorización para nuevos sistemas.
- **Federación con Active Directory** vía WSO2 Identity Server o equivalente aprobado.
- **Coexistencia gobernada** con SAA durante el período de transición.

> **Estado actual de WSO2:** Prueba de concepto. Hasta que esté operativo en producción, las reglas de validación de token SAA son responsabilidad de cada servicio backend.

### 3.3 Tipos de usuario y mecanismo de autenticación

| Tipo de usuario | Mecanismo actual | Mecanismo objetivo |
|---|---|---|
| Usuario interno (empleado ONP) | SAA → LDAP/Active Directory | WSO2 OIDC → AD federado |
| Usuario externo (asesores, ciudadanos) | SAA → credenciales + CAPTCHA | WSO2 OIDC → SAA federado |
| Sistema consumidor (máquina a máquina) | Token SAA de servicio | OAuth2 Client Credentials via WSO2 |
| Servicio interno sin usuario | Token de servicio SAA | OAuth2 Client Credentials |

### 3.4 Principios de seguridad

1. **Centralización:** La autenticación no es responsabilidad de las aplicaciones individuales. SAA es el proveedor institucional vigente.
2. **Separación de autenticación y autorización:** Token válido no implica permiso para operar. La autorización se aplica explícitamente en cada servicio.
3. **Privilegio mínimo:** Cada componente accede únicamente a lo que necesita para su función.
4. **Defensa en profundidad:** No se asume que la red interna es segura. Cada servicio valida sus propias entradas y tokens.
5. **No repudio:** Las acciones significativas generan trazas con `user.id` y `trace.id` (ver LIN-OBS-001 7).
6. **Sin secretos en código:** Ninguna credencial, clave ni token de servicio puede estar en el repositorio.

---

## 4. Autenticación

### 4.1 Usuarios internos

Los empleados de la ONP se autentican a través del SAA, que verifica sus credenciales contra Active Directory vía LDAP. Las aplicaciones nuevas no implementan autenticación propia — delegan al SAA.

### 4.2 Usuarios externos

Los usuarios externos (asesores legales, ciudadanos, entidades) se autentican mediante el formulario del SAA con CAPTCHA. Las aplicaciones no deben crear formularios de login propios.

### 4.3 Prohibiciones de autenticación

Queda **estrictamente prohibido**:

| Prohibición | Motivo |
|---|---|
| Implementar autenticación propia por aplicación | Fragmenta el modelo; crea vectores no gobernados |
| Validar credenciales directamente contra tablas de `SEGURITYSYS` | Acceso no autorizado al esquema SAA |
| Almacenar contraseñas en cualquier forma, incluso hasheadas | La aplicación no es el IdP |
| Hardcodear credenciales de usuarios de prueba | Riesgo de exposición en repositorios |
| Reusar tokens SAA entre sistemas distintos | Cada sistema debe obtener su propio token |

### 4.4 Cambios futuros

Cuando WSO2 API Manager esté operativo, los flujos de autenticación para nuevos sistemas migrarán a OAuth2/OIDC (Authorization Code Flow para usuarios, Client Credentials para M2M). Esta migración requerirá ADR documentado.

---

## 5. Autorización

### 5.1 Principio base

> La autenticación responde **quién es** el usuario. La autorización responde **qué puede hacer**. Ningún servicio debe asumir permisos solo porque el token es válido.

### 5.2 Modelo de autorización SAA

El SAA administra permisos a nivel de sistema, perfil y servicio web. Al validar un token, el SAA puede devolver los permisos y servicios web asignados al usuario para el sistema consultante.

Los servicios relevantes son:
- `WSAutenticarAutorizarUsuario` (SOAP): autenticación + lista de permisos
- `SAA/token/semilla/permisos/generar`: token con perfiles y permisos embebidos
- `SAA/token/usuario/permisos/generar`: equivalente basado en credenciales

### 5.3 Reglas de autorización en aplicaciones nuevas

1. Cada endpoint o recurso protegido define explícitamente qué permisos SAA son requeridos.
2. La validación de autorización ocurre **después** de validar el token, no en lugar de.
3. Los permisos se resuelven desde el response de SAA — no se asumen desde el perfil genérico.
4. No se implementa RBAC propio en la aplicación si el permiso ya existe en SAA.

### 5.4 Anotaciones Spring Security (modelo objetivo)

```java
@PreAuthorize("hasAuthority('CONSULTAR_PENSION')")
@GetMapping("/pensiones/{id}")
public ResponseEntity<PensionDto> getPension(@PathVariable Long id) { ... }
```

Los authorities se cargan en el `SecurityContext` durante la validación del token SAA (ver 8).

---

## 6. Tokens y sesiones

### 6.1 Token SAA — características

El token SAA es un token propietario (no JWT estándar). Sus características operativas:

| Característica | Descripción |
|---|---|
| Emisor | SAA — `appComponente` o `APP_WSSAA` |
| Validación | `POST SAA/token/validar` |
| Renovación | `POST SAA/token/renovar` |
| Cierre de sesión | `WSAdministrarSesion/cerrarSesion` |
| Tiempo de vida | Definido por configuración del SAA |
| Contenido | Perfiles, permisos, sistemas asignados al usuario |

### 6.2 Reglas para manejo de tokens

1. **Solo el SAA emite tokens.** Ninguna aplicación nueva puede crear sus propios tokens de sesión.
2. **La validación siempre va contra SAA.** No se valida localmente extrayendo la clave del token.
3. **Un token es válido para el sistema que lo generó.** No se reutiliza entre sistemas distintos.
4. **El token no se guarda en base de datos de la aplicación.** Solo en memoria/sesión del cliente.
5. **El token no se registra en logs.** Ver LIN-OBS-001 6 — prohibición de PII y secretos en logs.

### 6.3 Prohibición crítica — exposición de claves

> **Ningún servicio nuevo puede exponer endpoints que devuelvan claves, secretos, algoritmos criptográficos o material de firma de tokens.**

Este principio surge directamente del análisis del SAA actual, donde `ServicioToken GET /token/key` devuelve el algoritmo y clave del token a cualquier consumidor. Ese patrón queda **prohibido** para cualquier nuevo servicio.

Cualquier necesidad de distribución de claves debe resolverse mediante:
- Bóveda de secretos (Vault, K8s Secrets)
- JWKS interno bajo autenticación estricta
- Configuración gestionada por Plataforma

### 6.4 Servicio sin autenticación — prohibición

> **Todo servicio expuesto a través de red debe contar con mecanismo de autenticación o control de acceso.**

Este principio surge del `ServicioNotificacion` del SAA actual, que explícitamente "no cuenta con mecanismos de seguridad". Ese patrón queda **prohibido** para nuevos servicios. Si un servicio de integración interna no puede tener autenticación de usuario, debe tener al menos autenticación de sistema (token de servicio, API key de plataforma, o mTLS).

---

## 7. Seguridad de APIs

### 7.1 Reglas vigentes (sin WSO2 operativo)

Mientras WSO2 API Manager no esté operativo en producción, aplican las siguientes reglas:

| Regla | Descripción |
|---|---|
| HTTPS obligatorio | Todos los ambientes compartidos — DEV-servidor, QA y PROD. Dos excepciones, y ninguna más: desarrollo local en la máquina del desarrollador (`localhost`), y el tráfico **intra-cluster** en Kubernetes bajo las condiciones de `ADR-TLS-INTERNO-001` (ver nota abajo). |
| Validación de token en cada servicio | Cada API REST valida el token SAA en su propio filtro (ver 8) |
| CORS explícito | Configurado en backend, nunca `*` en producción |
| Headers de seguridad | Ver tabla en 7.3 |
| No exponer APIs internas directamente | Los servicios internos no tienen endpoint público sin gateway |
| Rate limiting básico | Configurado en la aplicación si no hay gateway |

> **Excepción acotada — tráfico intra-cluster (`ADR-TLS-INTERNO-001`).** Se admite que el tramo comprendido entre el punto de terminación TLS y el pod destino, dentro del mismo cluster, viaje sobre HTTP. La excepción **sustituye un control por otro, no lo retira**, y exige las tres condiciones del ADR:
>
> 1. **TLS termina en el perímetro** —Ingress Controller o gateway WSO2—. Ningún `Service` de tipo `NodePort` o `LoadBalancer` expone un backend sin pasar por él.
> 2. **`NetworkPolicy` obligatoria** en todo servicio que reciba tráfico interno, con la política mínima del Anexo E de `LIN-K8S-001`: aceptar solo del Ingress Controller y de los pods del mismo sistema, denegar el resto. **Un servicio sin `NetworkPolicy` no puede acogerse a esta excepción** y debe servir HTTPS extremo a extremo.
> 3. **Migración a mTLS** cuando Plataforma habilite la malla de servicios; en ese momento la excepción queda sin efecto.
>
> Lo que **no** cubre: el tráfico hacia servicios fuera del cluster (SAA, RENIEC, SUNAT, Oracle) exige HTTPS sin excepción, igual que cualquier tramo que atraviese el borde del cluster; y no autoriza HTTP en ambientes compartidos fuera de Kubernetes —una aplicación en máquina virtual sigue sujeta a la regla sin matices—.

### 7.2 Modelo objetivo (WSO2 operativo)

Cuando WSO2 API Manager esté en producción:

- Validación de token delegada al gateway
- Scopes OAuth2 por operación (`GET /pensiones` → scope `pension:read`)
- Rate limiting gestionado en WSO2
- APIs publicadas únicamente vía gate de publicación (LIN-API-REST-001 10.3)
- Client Credentials para integraciones M2M

La transición de validación interna a validación en gateway requiere ADR.

### 7.3 Headers de seguridad obligatorios

Los headers requeridos difieren según el tipo de aplicación:

**APIs REST (respuestas `application/json`) — obligatorios:**

```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
Strict-Transport-Security: max-age=31536000; includeSubDomains
Cache-Control: no-store
```

**Aplicaciones web / frontend (respuestas HTML) — adicionalmente obligatorio:**

```
Content-Security-Policy: default-src 'self'
```

> `Content-Security-Policy` protege contextos de renderizado HTML. En APIs REST que retornan exclusivamente JSON no aporta protección adicional. El header crítico para APIs es `Cache-Control: no-store` — evita que proxies o navegadores almacenen respuestas que pueden contener datos sensibles.

Configuración en Spring Boot para APIs REST:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(Customizer.withDefaults())
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true))
                .cacheControl(Customizer.withDefaults())
                // CSP: no configurar en APIs REST puras
                // Para aplicaciones web que sirven HTML, agregar:
                // .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
            )
            .csrf(csrf -> csrf.disable()) // APIs REST stateless — usar token SAA
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
```

### 7.4 CORS

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    // DEV/QA: dominio de pruebas; PROD: dominio institucional
    config.setAllowedOrigins(List.of("${app.cors.allowed-origins}"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Request-ID"));
    config.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

Valor de `app.cors.allowed-origins` proviene de variable de entorno — nunca hardcodeado, nunca `*` en producción.

---

## 8. Integración SAA para aplicaciones nuevas

### 8.1 Principio

Las aplicaciones nuevas **no reemplazan el SAA ni crean mecanismos paralelos**. Se integran con él dentro de sus capacidades actuales: validación de token via REST, consulta de permisos, propagación de identidad al contexto de la aplicación.

### 8.2 Flujo de integración

```
┌─────────────┐   1. Login en SAA    ┌──────────────┐
│   Frontend   │ ──────────────────► │     SAA      │
│  (Angular)   │ ◄────────────────── │ (appComp./   │
└─────────────┘   2. Token SAA       │  APP_WSSAA)  │
       │                             └──────────────┘
       │ 3. Request + Bearer token
       ▼
┌─────────────┐   4. POST /validar   ┌──────────────┐
│  Spring Boot │ ──────────────────► │     SAA      │
│     API      │ ◄────────────────── │  SAA/token/  │
└─────────────┘   5. userId+permisos │    validar   │
       │                             └──────────────┘
       │ 6. Propaga user.id al MDC
       │    Aplica autorización local
       ▼
   Lógica de negocio
```

### 8.3 Filtro de validación de token SAA

> **Posición en la cadena: `@Order(3)`, por dentro del filtro canónico.** `CanonicalRequestLogFilter` (`@Order(2)`) envuelve a este filtro para que **los rechazos de autenticación queden registrados**: si el filtro de seguridad corriera por fuera, ningún 401 ni 503 aparecería en el log canónico y la tasa de fallos de autenticación sería inmedible — el escenario *OWASP A09* que §13.1 identifica como relevante. Por eso este filtro publica la identidad **también** como atributo de la request: el MDC se limpia al desapilarse, el atributo no. Cadena completa en `LIN-OBS-001 §4.11`.

```java
@Component
@Order(3) // Dentro de CanonicalRequestLogFilter (@Order 2) para que sus rechazos queden registrados
public class SaaTokenValidationFilter extends OncePerRequestFilter {

    /** Identidad publicada para el log canónico; sobrevive a la limpieza del MDC (LIN-OBS-001 §4.9). */
    public static final String ATTR_USER_ID = "onp.user.id";

    private static final Logger log = LoggerFactory.getLogger(SaaTokenValidationFilter.class);

    private final SaaTokenClient saaTokenClient;

    public SaaTokenValidationFilter(SaaTokenClient saaTokenClient) {
        this.saaTokenClient = saaTokenClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String token = extractBearerToken(request);

        if (token == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token requerido");
            return;
        }

        SaaValidationResult result;
        try {
            result = saaTokenClient.validar(token);
        } catch (SaaUnavailableException e) {
            log.error("SAA no disponible para validar token");
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Servicio de autenticación no disponible");
            return;
        }

        if (!result.isValid()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado");
            return;
        }

        MDC.put("user.id", result.getUsuarioId());              // LIN-OBS-001 §7 — logs de negocio
        request.setAttribute(ATTR_USER_ID, result.getUsuarioId()); // LIN-OBS-001 §4.9 — log canónico

        SecurityContextHolder.getContext().setAuthentication(
            new SaaAuthentication(result.getUsuarioId(), result.getPermisos())
        );

        try {
            chain.doFilter(request, response);
        } finally {
            // El MDC se limpia aquí para no filtrar identidad al siguiente uso del hilo.
            // El atributo de la request NO se limpia: CanonicalRequestLogFilter, que envuelve
            // a este filtro, lo lee después para emitir el log canónico (LIN-OBS-001 §4.9).
            MDC.remove("user.id");
            SecurityContextHolder.clearContext();
        }
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Excluir endpoints públicos: actuator/health, actuator/info, /public/**
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health")
            || path.startsWith("/actuator/info")
            || path.startsWith("/public/");
    }
}
```

> **Respuestas del filtro y contrato institucional.** `response.sendError()` delega en el manejador de errores por defecto del contenedor, que **no** produce `ApiResponseWrapper`: un 401 emitido aquí no cumple el contrato de `LIN-API-REST-001 §4`. Dado que el filtro se ejecuta antes del `@RestControllerAdvice`, la implementación conforme debe **serializar el wrapper directamente en la respuesta** —escribiendo `ApiResponseWrapper.error(401, "300", ...)` sobre `response.getWriter()` con `Content-Type: application/json`— o delegar en un `AuthenticationEntryPoint` de Spring Security que haga lo mismo. Los códigos aplicables son `401`/`300` (token ausente, inválido o expirado) y `503`/`400` (SAA no disponible), según `LIN-API-REST-001 §4.2` y `§7.1`. El ejemplo de arriba usa `sendError` por brevedad; **no es la forma conforme**.

### 8.4 Cliente SAA

```java
@Component
public class SaaTokenClient {

    private static final Logger log = LoggerFactory.getLogger(SaaTokenClient.class);

    private final RestClient restClient;

    @Value("${saa.token.validar.url}")
    private String validarUrl;

    // El SAA está en la ruta crítica de CADA petición: sus timeouts son obligatorios
    // y siguen la categoría «Alta demanda / ruta crítica interactiva» de LIN-DIS-001 §6.1
    // (connect ≤1.5-2s, read 2-3s), con el pool acotado por proveedor (Bulkhead, §6.3).
    public SaaTokenClient(RestClient.Builder builder,
                          @Value("${saa.token.connect-timeout-ms:1500}") int connectTimeoutMs,
                          @Value("${saa.token.read-timeout-ms:3000}") int readTimeoutMs) {
        var connConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(connectTimeoutMs))
                .setSocketTimeout(Timeout.ofMilliseconds(readTimeoutMs))
                .build();
        var connManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connConfig)
                .setMaxConnPerRoute(15)   // Bulkhead — LIN-DIS-001 §6.3
                .build();
        var httpClient = HttpClients.custom().setConnectionManager(connManager).build();
        this.restClient = builder
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                .build();
    }

    public SaaValidationResult validar(String token) {
        try {
            return restClient.post()
                    .uri(validarUrl)
                    .headers(h -> h.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(SaaValidationResult.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            return SaaValidationResult.invalid();
        } catch (RestClientException e) {
            log.error("Error de comunicación con SAA: {}", e.getMessage());
            throw new SaaUnavailableException("SAA no disponible", e);
        }
    }
}
```

### 8.5 Configuración de propiedades

> **Convención institucional:** la configuración base de proyectos Spring Boot ONP se documenta en archivos YAML (`application.yml`, `application-dev.yml`, `application-qa.yml`, `application-prod.yml`).

```yaml
# application.yml (valor por ambiente via variable de entorno)
saa:
  token:
    validar:
      url: "${SAA_TOKEN_VALIDAR_URL}"
```

```yaml
# application-dev.yml — la URL también viene de variable de entorno
saa:
  token:
    validar:
      url: "${SAA_TOKEN_VALIDAR_URL}"
    connect-timeout-ms: 1500   # LIN-DIS-001 §6.1 — ruta crítica interactiva
    read-timeout-ms: 3000
```

**En ningún ambiente se escribe la URL literal en el repositorio**, tampoco en DEV: `§12.1` clasifica la URL de validación de token SAA como **secreto** por contener el host interno. El valor de cada ambiente lo inyecta Plataforma como variable de entorno o K8s Secret.

Todas las URLs de ambientes compartidos (DEV-servidor, QA, PROD) usan **HTTPS**; `§7.1` solo admite HTTP en `localhost` del desarrollador.

### 8.6 Prohibiciones para la integración SAA

| Prohibición | Motivo |
|---|---|
| Consultar directamente `SEGURITYSYS.usuarios` para verificar identidad | Acoplamiento no gobernado al esquema SAA |
| Llamar a `ServicioToken GET /token/key` para validar localmente | Exposición de material criptográfico |
| Crear un token de sesión propio adicional al token SAA | Mecanismo paralelo no gobernado |
| Cachear el resultado de validación por más de 60 segundos sin invalidación | Riesgo de sesión revocada en SAA que sigue activa en la app |
| Propagar el token SAA completo en logs | Secreto — ver LIN-OBS-001 6 |

### 8.7 Manejo de indisponibilidad del SAA

El SAA es una dependencia crítica. Si no responde:

- Retornar `503 Service Unavailable` con mensaje genérico (no técnico)
- Registrar el evento como `log.error` con contexto de traza
- No cachear el último resultado como sustituto de validación
- Aplicar Circuit Breaker si el servicio tiene alta concurrencia (ver patrón PT07 — normado en LIN-DIS-001 §6.2)

#### 8.7.1 Comportamiento del filtro ante error

| Escenario | HTTP esperado | `user.id` en MDC | ¿Se ejecuta `CanonicalRequestLogFilter`? | Observación |
|---|---|---|---|---|
| No llega header `Authorization` | 401 | No | **Sí** | El log canónico registra el 401 con `user.id: anonymous` |
| Token inválido o expirado | 401 | No | **Sí** | Ídem — la tasa de rechazos es medible en Kibana |
| SAA no disponible | 503 | No | **Sí** | El 503 queda registrado con nivel `ERROR` |
| Token válido | Continúa | Sí | Sí | El log canónico lee la identidad del atributo `onp.user.id` |

---

## 9. Seguridad en backend Java

### 9.1 Spring Security

Toda aplicación Spring Boot que expone endpoints protegidos **debe configurar Spring Security explícitamente**. No se acepta la configuración default ni la deshabilitación total.

Reglas mínimas:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize
public class SecurityConfig {

    private final SaaTokenValidationFilter saaTokenFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(saaTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

### 9.2 Validación de entrada

| Regla | Implementación |
|---|---|
| Validar todos los parámetros de entrada de usuario | Bean Validation (`@Valid`, `@NotNull`, `@Size`, `@Pattern`) |
| Nunca construir consultas con concatenación de strings | JPA/JPQL con parámetros nombrados, o `JdbcTemplate` con `?` |
| Sanitizar HTML en datos que se mostrarán en frontend | `HtmlUtils.htmlEscape()` antes de persistir si aplica |
| Rechazar entradas que excedan tamaño máximo definido | `@Size(max = N)` en DTOs |
| Validar Content-Type en endpoints que aceptan upload | `@RequestPart` + validación de tipo MIME |

### 9.3 Manejo seguro de errores

> **Contrato de respuesta:** el manejador usa `ApiResponseWrapper` y los códigos `codDetRespuesta` del catálogo institucional (`LIN-API-REST-001 §4` y `§4.2`), que es el dueño de ese contrato. **No se crean códigos de error propios por sistema** (`LIN-API-REST-001 §4.2.1.d`). La implementación de referencia completa está en `LIN-DEV-JAVA-001 §11.1`; aquí solo se muestran los aspectos de seguridad.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Value("${info.app.version:1.0.0}")
    private String version;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleGeneral(Exception ex, HttpServletRequest req) {
        // Log completo interno con trace.id para correlación
        log.error("Error no controlado en {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        // Respuesta al cliente: genérica, sin stack trace, sin detalles técnicos
        return ResponseEntity.status(500).body(ApiResponseWrapper.error(
            500, "500",
            "Error interno del servidor. Referencie el requestId al equipo de soporte.",
            null, MDC.get("http.request.id"), version
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(ApiResponseWrapper.error(
            403, "301",
            "No tiene permisos para realizar esta operación.",
            null, MDC.get("http.request.id"), version
        ));
    }
}
```

Reglas:

- Stack traces **nunca** en respuestas HTTP
- Mensajes de error no revelan estructura interna (nombres de tabla, rutas de clase, versiones)
- El `X-Request-ID` se incluye en el body para que el usuario pueda reportar el incidente
- Los detalles técnicos van al log interno, no al cliente

### 9.4 Protección contra inyección

**SQL — correcto:**
```java
// JPA con parámetros nombrados
@Query("SELECT p FROM Pension p WHERE p.dni = :dni AND p.estado = :estado")
List<Pension> findByDniAndEstado(@Param("dni") String dni, @Param("estado") String estado);

// JdbcTemplate con PreparedStatement
jdbcTemplate.query(
    "SELECT * FROM pensiones WHERE dni = ? AND estado = ?",
    new Object[]{dni, estado},
    rowMapper
);
```

**SQL — prohibido:**
```java
// NUNCA construir queries con concatenación
String query = "SELECT * FROM pensiones WHERE dni = '" + dni + "'"; // INYECCIÓN SQL
```

**JPQL — prohibido:**
```java
// NUNCA JPQL dinámico con concatenación
String jpql = "FROM Pension WHERE dni = '" + dni + "'"; // VULNERABILIDAD
```

### 9.5 Datos sensibles en código

Ver LIN-OBS-001 6 y 4.8 (Mask.java). Aplica también a:

- Mensajes de error que podrían contener DNI, nombres completos, CUS
- Logs de debugging que muestran entidades completas con campos personales
- Respuestas de API que exponen más campos de los necesarios (over-fetching)

---

## 10. Seguridad en frontend Angular

### 10.1 Almacenamiento de tokens

| Opción | Uso recomendado | Riesgo |
|---|---|---|
| `sessionStorage` | **Preferido** — token SAA de sesión activa | Persiste solo en la pestaña; se pierde al cerrar |
| `localStorage` | Evitar para tokens de sesión | Accesible por cualquier script de la página (XSS) |
| Cookie `HttpOnly + Secure` | Alternativa robusta si el backend la emite | No accesible por JS; requiere configuración backend |
| Variable en memoria (servicio Angular) | Válido para tokens de vida corta | Se pierde en recarga de página |

**Regla:** El token SAA no se almacena en `localStorage`. Si el refresco de página es requerido, evaluar cookie `HttpOnly` con coordinación del equipo de backend.

### 10.2 Guards y control de acceso

```typescript
// auth.guard.ts
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return false;
    }
    const requiredPermission = route.data['permission'];
    if (requiredPermission && !this.authService.hasPermission(requiredPermission)) {
      this.router.navigate(['/acceso-denegado']);
      return false;
    }
    return true;
  }
}
```

```typescript
// app-routing.module.ts
{ path: 'pensiones', component: PensionesComponent,
  canActivate: [AuthGuard], data: { permission: 'CONSULTAR_PENSION' } }
```

### 10.3 Protección contra XSS

| Regla | Implementación |
|---|---|
| Confiar en el escape automático de Angular | No reemplazar con `[innerHTML]` sin justificación |
| Sanitizar con `DomSanitizer` si `[innerHTML]` es obligatorio | `sanitizer.sanitize(SecurityContext.HTML, value)` |
| No ejecutar strings dinámicos como código | Evitar `eval()`, `Function()`, `setTimeout(string)` |
| Validar datos antes de renderizar | Nunca mostrar directamente parámetros de URL sin parsear |

### 10.4 Secrets en frontend

> **Ningún secreto puede estar en el frontend Angular.** Los archivos `environment.ts` y `environment.prod.ts` son compilados en el bundle y son legibles por cualquier usuario.

Prohibido en `environment.ts`:

```typescript
// PROHIBIDO
export const environment = {
  apiKey: 'clave-secreta-123',         // NUNCA
  saaAdminUser: 'admin',               // NUNCA
  dbConnectionString: '...',           // NUNCA
  internalServiceUrl: 'http://...'     // Evitar si es interno
};
```

Los endpoints de API backend son el único valor de configuración aceptable en `environment.ts`.

### 10.5 Manejo de expiración de sesión

```typescript
// session.service.ts
@Injectable({ providedIn: 'root' })
export class SessionService {

  private sessionTimer: any;

  startSessionTimer(expiresInMs: number): void {
    this.sessionTimer = setTimeout(() => {
      this.handleSessionExpired();
    }, expiresInMs - 30_000); // Avisa 30 segundos antes
  }

  private handleSessionExpired(): void {
    sessionStorage.removeItem('saa_token');
    this.router.navigate(['/login'], { queryParams: { reason: 'session_expired' } });
  }
}
```

---

## 11. Seguridad en base de datos

Ver LIN-BD-ORA-001 para el lineamiento completo de base de datos. Las reglas de seguridad específicas:

### 11.1 Privilegios mínimos

| Regla | Descripción |
|---|---|
| Usuario de aplicación ≠ dueño de esquema | El usuario de conexión no tiene `DBA` ni `CONNECT` con privilegios elevados |
| Solo permisos necesarios | `GRANT SELECT, INSERT, UPDATE, DELETE` en tablas específicas; no `GRANT ALL` |
| No DDL en runtime | El usuario de aplicación no puede crear ni modificar tablas en producción |
| Sin acceso a esquemas de otros sistemas | Cada app solo accede a su esquema propio |

### 11.2 Prohibición de acceso directo a esquemas SAA

> **Las aplicaciones nuevas no pueden acceder directamente a los esquemas del SAA** (`SEGURITYSYS`, `GENERALSYS`, `SAEMPLSYS`).

La integración con SAA se realiza exclusivamente a través de sus servicios publicados (ver 8). Cualquier necesidad de datos del SAA que no esté cubierta por sus servicios debe resolverse mediante solicitud formal al equipo propietario del SAA.

### 11.3 Datos personales en base de datos

Aplica Ley N.° 29733:

| Clasificación | Tratamiento |
|---|---|
| DNI, CUS, número de expediente | Pueden almacenarse; no exponer en logs (LIN-OBS-001 6) |
| Datos de salud, situación previsional detallada | Acceso restringido; auditoría de consultas obligatoria |
| Contraseñas de usuarios | **Nunca** las almacena la aplicación — son del SAA/AD |
| Tokens de sesión | No persistir en base de datos de la aplicación |

### 11.4 Auditoría de acceso

Para tablas que contienen datos sensibles, activar auditoría de Oracle:

```sql
-- Auditar acceso a tablas de datos personales
AUDIT SELECT, INSERT, UPDATE, DELETE ON esquema.tabla_sensible BY ACCESS;
```

El equipo de Seguridad de la Información define qué tablas requieren auditoría. Arquitectura la registra en el documento de diseño del sistema.

---

## 12. Gestión de secretos

### 12.1 Qué es un secreto

| Tipo | Ejemplos |
|---|---|
| Credenciales de base de datos | Usuario y contraseña de conexión Oracle |
| URLs de servicios críticos | URL de validación de token SAA (contiene host interno) |
| Claves de cifrado | Claves de AES, certificados TLS |
| Tokens de integración | Tokens de APIs externas, webhooks |
| Claves de firma | Si se implementa HMAC en algún flujo propio |

### 12.2 Prohibiciones absolutas

Queda **terminantemente prohibido**:

1. Secretos en cualquier archivo bajo control de versiones (Git, Harvest)
2. Secretos en `application.yml`, `application-*.yml`, `logback-spring.xml`
3. Secretos en `environment.ts` o cualquier archivo Angular compilado
4. Secretos en correos electrónicos, Slack, documentación wiki
5. Secretos compartidos entre ambientes (DEV, QA, PROD usan secretos distintos)
6. Secretos en comentarios de código
7. Secretos en variables de entorno de Dockerfile (quedan en capa de imagen)

### 12.3 Mecanismos de inyección por contexto

Los secretos se inyectan de forma distinta según el contexto de uso:

| Contexto | Mecanismo | Documento dueño |
|---|---|---|
| Runtime de aplicación en Kubernetes | Kubernetes Secrets montados en el pod | **LIN-K8S-001 8** |
| Variables del pipeline CI/CD | GitLab CI/CD Variables (protegidas y enmascaradas) | **LIN-CICD-001 13.4** |
| Infraestructura como Código (Terraform) | Variables `TF_VAR_*` en GitLab CI/CD | **LIN-CICD-001 13.4** |
| Evolución futura | HashiCorp Vault o mecanismo aprobado por Arquitectura y Seguridad | Pendiente — capacidad objetivo |

> Este lineamiento define qué es un secreto y qué está prohibido. La mecánica de inyección en cada contexto es responsabilidad del lineamiento correspondiente indicado arriba.

### 12.4 Rotación de secretos

| Tipo | Frecuencia mínima |
|---|---|
| Contraseñas de base de datos | Anual o ante sospecha de exposición |
| Tokens de integración | Semestral o ante rotación del sistema remoto |
| Claves de cifrado | Según política de Seguridad de la Información |
| Ante incidente de exposición | Inmediata — dentro de 24 horas del descubrimiento |

La rotación aplica tanto a Kubernetes Secrets (ver LIN-K8S-001 8) como a variables de pipeline en GitLab (ver LIN-CICD-001 13.4).

---

## 13. Dependencias y escaneo de vulnerabilidades

### 13.1 OWASP Top 10

Todo desarrollador que trabaje en sistemas ONP debe conocer el OWASP Top 10 vigente. Las categorías más relevantes en el contexto ONP:

| # | Categoría | Relevancia ONP |
|---|---|---|
| A01 | Broken Access Control | Control de permisos SAA en cada endpoint |
| A02 | Cryptographic Failures | Tokens SAA, datos personales, secrets |
| A03 | Injection | SQL en Oracle, JPQL, LDAP queries |
| A04 | Insecure Design | Diseño de flujos de autenticación |
| A05 | Security Misconfiguration | Spring Security defaults, CORS, headers |
| A06 | Vulnerable Components | Struts 1.x, JDK 1.7, iBatis 2 en legacy |
| A07 | Authentication Failures | Integración SAA, validación de token |
| A09 | Security Logging Failures | LIN-OBS-001 — logs sin PII, con trace.id |

### 13.2 Herramientas por fase

> **Separación de responsabilidades:** Este lineamiento define **qué debe cumplirse** — herramientas aprobadas, severidades bloqueantes y criterios de aceptación. LIN-CICD-001 (en borrador) definirá **cómo se valida automáticamente** — cuándo se ejecuta cada herramienta, qué hace fallar el pipeline y cómo se gestiona la deuda técnica en el repositorio.

| Fase | Herramienta | Qué detecta | Propietario del gate |
|---|---|---|---|
| Desarrollo | IDE plugins (SonarLint, PMD) | Problemas en tiempo real | Desarrollador |
| CI/CD | SonarQube / PMD (SAST) | Vulnerabilidades en código fuente | LIN-CICD-001 |
| CI/CD | OWASP Dependency-Check (SCA) | CVEs en dependencias Maven | LIN-CICD-001 |
| CI/CD | Trivy | CVEs en imagen de contenedor | LIN-CICD-001 |
| Pre-producción | DAST (herramienta a definir por Seguridad) | Vulnerabilidades en runtime | Seguridad de la Información |

**Nota sobre PMD (SAST):** Adicionalmente a SonarQube, para proyectos Java se requiere la validación local y en pipeline de las reglas de seguridad de PMD (`category/java/security.xml`), bloqueando el build si se detectan credenciales hardcodeadas (`HardCodedCredential`) o criptografía insegura. Ver **`LIN-DEV-JAVA-001 §12.3`** para la configuración de PMD.

### 13.3 Criterios de aceptación de seguridad

Este lineamiento define los umbrales. LIN-CICD-001 definirá cómo se aplican automáticamente en el pipeline.

Un artefacto **no es apto para QA ni para producción** si presenta:

- Vulnerabilidades SonarQube de severidad **Critical** o **Blocker** sin resolución documentada
- CVEs con CVSS ≥ 9.0 en dependencias directas (SCA) sin plan de mitigación aprobado
- Vulnerabilidades **CRITICAL** en la imagen de contenedor (Trivy) sin justificación

La aceptación temporal de una vulnerabilidad conocida requiere ADR firmado por Arquitectura que documente el riesgo aceptado y la fecha de revisión.

### 13.4 Tecnologías legacy de riesgo clasificadas

Las siguientes tecnologías presentes en sistemas ONP están clasificadas como **legacy de riesgo** y requieren plan de contención o modernización al ser intervenidas:

| Tecnología | Riesgo principal | Sistemas conocidos |
|---|---|---|
| Struts 1.1/1.2 | CVEs críticos conocidos, EOL | appAccesoExternoEAR, appAccesoInternoSAAEAR, appSAA |
| JDK 1.6 / 1.7 | EOL, sin parches de seguridad | Mayoría de apps SAA |
| iBatis 2 | EOL, sin mantenimiento | appComponente, appSAA |
| Oracle 11g | EOL | BDPR11G2 (SAA) |
| WAS 8.5 | Versión antigua | Todos los EARs del SAA |

No se exige migración inmediata de sistemas estables en producción. Sí se exige que al intervenir cualquiera de estos sistemas (cambio funcional, nuevo servicio, refactorización), se apliquen los controles mínimos de este lineamiento.

---

## 14. Controles para sistemas legacy

### 14.1 Clasificación de escenarios

| Escenario | Obligación de seguridad |
|---|---|
| **Legacy sin cambios** — sistema estable en producción, sin intervención | Se inventaría y evalúa riesgo. No se obliga migración. Requiere registro en Matriz de Propiedad Documental. |
| **Legacy con mantenimiento correctivo** — bugfix sin cambio funcional | No puede introducir nuevas vulnerabilidades. No puede debilitar controles existentes. |
| **Legacy con cambio funcional** — nueva funcionalidad o modificación de negocio | Debe aplicar controles mínimos de 4 (autenticación), 6 (tokens) y 9 (validación de entrada) sobre los componentes modificados. |
| **Legacy que expone API nueva** | Debe pasar por API Manager (o mecanismo aprobado por Arquitectura). No se expone directamente sin revisión. |
| **Legacy con tokens/credenciales propias** | Revisión obligatoria de seguridad antes de cualquier cambio. Si expone material criptográfico (como `/token/key`), debe evaluarse con ADR. |
| **Legacy sin autenticación** | Requiere control compensatorio documentado (restricción de red, IP whitelist) o plan de cierre con fecha. No puede quedar indefinidamente sin control. |

### 14.2 El SAA como capacidad legacy crítica

El SAA constituye una **capacidad institucional existente** para autenticación, autorización, administración de usuarios, roles, perfiles, servicios y sesiones. Por su criticidad:

- Todo nuevo desarrollo que requiera integrarse con SAA lo hace mediante sus servicios publicados, sin accesos directos no gobernados
- No se crean mecanismos paralelos de token o autenticación
- No se reutilizan informalmente claves o semillas del SAA
- No se exponen endpoints sin seguridad basándose en que el SAA "ya lo maneja"

La evolución del SAA hacia el modelo objetivo de seguridad institucional (OAuth2/OIDC) se gestiona mediante arquitectura de transición. Cualquier coexistencia entre SAA, WSO2, OAuth2/OIDC y servicios legacy debe documentarse mediante ADR antes de implementarse. La decisión institucional de transición debe formalizarse en `ADR-WSO2-001`.

---

## 15. Responsabilidades

### 15.1 Equipo de desarrollo

| Responsabilidad | Detalle |
|---|---|
| Implementar `SaaTokenValidationFilter` | Según 8.3 — en cada nuevo servicio Spring Boot |
| Configurar Spring Security explícitamente | No defaults, no deshabilitación total |
| No incluir secrets en repositorio | Usar variables de entorno o K8s Secrets |
| Validar entradas en cada endpoint | Bean Validation + protección contra inyección |
| No exponer stack traces al cliente | GlobalExceptionHandler según 9.3 |
| No registrar datos PII en logs | LIN-OBS-001 6 y Mask.java |
| Corregir vulnerabilidades bloqueantes en CI/CD | Antes de merge a rama de QA o producción |

### 15.2 Arquitectura de Software

| Responsabilidad | Detalle |
|---|---|
| Validar diseño de seguridad en revisión de arquitectura | Antes de iniciar desarrollo de sistemas nuevos |
| Aprobar desviaciones (ADR) | Firmar ADR cuando se acepta excepción al lineamiento |
| Mantener este lineamiento actualizado | Ante cambios en SAA, WSO2 o modelo institucional |
| Clasificar nuevos sistemas legacy | Actualizar tabla 13.4 cuando se incorporan nuevas apps |
| Gobernar transición SAA → WSO2/OIDC | Definir hoja de ruta con Seguridad e Infraestructura |

### 15.3 Plataforma / Infraestructura

| Responsabilidad | Detalle |
|---|---|
| Gestionar K8s Secrets por ambiente | DEV, QA, PROD con secretos distintos |
| Configurar TLS en todos los ambientes | HTTPS desde DEV |
| Proveer URLs de SAA por ambiente | Variables de entorno inyectadas en deployments |
| Gestionar certificados | Renovación antes de expiración |
| Avanzar PoC WSO2 → producción | Según hoja de ruta aprobada |

### 15.4 Seguridad de la Información

| Responsabilidad | Detalle |
|---|---|
| Definir qué tablas requieren auditoría Oracle | Notificar a Arquitectura para inclusión en diseño |
| Gestionar incidentes de seguridad | Coordinación con equipos técnicos |
| Definir política de rotación de secretos | Frecuencias mínimas obligatorias |
| Aprobar herramientas de escaneo | SonarQube, OWASP Dependency-Check, Trivy — licencias y configuración |

---

## 16. Checklist de seguridad

### Para nuevos servicios Spring Boot

```
AUTENTICACIÓN Y AUTORIZACIÓN
[ ] SaaTokenValidationFilter implementado y registrado como @Component
[ ] Endpoints públicos declarados explícitamente en shouldNotFilter() y en SecurityConfig
[ ] Autorización por endpoint con @PreAuthorize o authorizeHttpRequests configurado
[ ] user.id propagado al MDC después de validación de token

SECRETS Y CONFIGURACIÓN
[ ] Ningún secreto en src/main/resources ni en repositorio
[ ] URLs de SAA y BD provienen de variables de entorno
[ ] K8s Secrets creados por Plataforma para cada ambiente

SEGURIDAD HTTP
[ ] HTTPS configurado — sin HTTP en producción
[ ] CORS configurado explícitamente — sin * en producción
[ ] Headers de seguridad presentes en todas las respuestas (7.3)
[ ] CSRF deshabilitado correctamente para API REST stateless

CÓDIGO SEGURO
[ ] Bean Validation en todos los DTOs de entrada
[ ] No concatenación de strings en queries SQL/JPQL
[ ] GlobalExceptionHandler implementado — sin stack traces al cliente
[ ] No PII en logs (Mask.java para campos sensibles)
[ ] No secretos en comentarios ni en logs de debugging

DEPENDENCIAS
[ ] No dependencias con CVEs críticos conocidos sin justificación
[ ] SonarQube sin vulnerabilidades bloqueantes
[ ] Imagen de contenedor escaneada con Trivy
```

### Para sistemas legacy intervenidos

```
[ ] Caso de intervención clasificado según tabla 14.1
[ ] Si expone API nueva: revisión de Arquitectura antes de exposición
[ ] Si tiene tokens/credenciales propias: revisión de seguridad completada
[ ] Si no tiene autenticación: control compensatorio documentado o plan de cierre con fecha
[ ] Nuevas funcionalidades aplican controles mínimos de 4, 6 y 9
```

---

## 17. Anti-patrones

| Anti-patrón | Riesgo | Regla |
|---|---|---|
| **Autenticación propia** — la app crea su propio login y tokens | Fragmenta el modelo; doble vector de ataque | 4 — solo SAA autentica |
| **Endpoint que expone clave criptográfica** — `/token/key` en servicio nuevo | Permite fabricar tokens | 6.3 — prohibido exponer material criptográfico |
| **Servicio sin autenticación** — endpoint REST sin ningún control | Acceso no controlado desde red interna | 6.4 — todo servicio requiere autenticación o control compensatorio |
| **Query directo a SEGURITYSYS** — la app consulta tablas SAA directamente | Acoplamiento no gobernado; viola propiedad documental | 8.6 y 11.2 |
| **Token en localStorage** — frontend guarda token SAA en localStorage | Robo de token via XSS | 10.1 — usar sessionStorage o cookie HttpOnly |
| **Secret en environment.ts** — claves en archivos Angular compilados | Secret expuesto en bundle público | 10.4 — prohibido |
| **CORS con `*`** — cualquier origen puede consumir la API | Permite CSRF y lectura de respuestas desde dominios maliciosos | 7.4 — CORS explícito |
| **Stack trace al cliente** — respuesta 500 con mensaje técnico completo | Revela estructura interna, rutas, versiones | 9.3 — respuestas genéricas |
| **Cachear validación SAA indefinidamente** — la app guarda el resultado positivo sin TTL | Sesión revocada en SAA sigue activa | 8.6 — TTL máximo 60 segundos |
| **Mismo secret en todos los ambientes** — DEV/QA/PROD comparten credenciales | Credencial de DEV compromete producción | 12.3 — secretos separados por ambiente |

---

## 18. Proceso de excepción (`EXC-SEC-NNN`)

> **Instrumento correcto: `EXC-SEC-NNN`, no un ADR.** Conforme a `GOB-MAT-001` (Registro de decisiones y excepciones), la desviación de un lineamiento **en un proyecto concreto** se registra como excepción con vigencia acotada y **fecha de revisión**, nunca indefinida. El `ADR-NNN` queda reservado a decisiones **institucionales** del Comité de Arquitectura, que obligan a todo el corpus; llevar allí cada desviación de cada sistema vaciaría de valor ese registro. La excepción se aprueba por Arquitectura OTI y **validación de Seguridad Digital**, por tratarse de controles de seguridad y se registra en el documento de arquitectura del sistema (`GOB-PLA-001`, Anexo E, criterio 14).


Cualquier desviación a las reglas de este lineamiento requiere un ADR (Architecture Decision Record) aprobado por Arquitectura de Software antes de implementarse.

### Casos que típicamente requieren ADR

- Sistema que no puede integrarse con SAA por restricciones técnicas documentadas
- Adopción de OAuth2/OIDC en un sistema antes de que WSO2 esté operativo
- Sistema legacy que no puede aplicar controles mínimos sin riesgo de regresión
- Aceptación temporal de CVE conocido en dependencia legacy
- Modelo de autorización alternativo al propuesto en 5
- Coexistencia de SAA + nuevo IdP en el mismo sistema

### Formato mínimo del ADR de seguridad

```markdown
# ADR-SEC-NNN — [Título de la decisión]

## Contexto
[Sistema, componente, restricción técnica o de negocio que origina la desviación]

## Decisión
[Qué se decide y por qué]

## Controles compensatorios
[Qué medidas mitigan el riesgo que genera la desviación]

## Riesgos aceptados
[Qué riesgo queda sin mitigar completamente]

## Fecha de revisión
[Cuándo se revisará si la excepción sigue siendo válida]

## Aprobado por
[Arquitectura de Software — firma o registro]
```

---

## 19. Glosario

| Término | Definición |
|---|---|
| **SAA** | Sistema de Administración de Accesos — IAM institucional de ONP |
| **AD** | Active Directory — directorio de usuarios internos ONP via LDAP |
| **Token SAA** | Token propietario emitido por SAA (`appComponente` o `APP_WSSAA`) |
| **OAuth2** | Protocolo de autorización delegada estándar (RFC 6749) |
| **OIDC** | OpenID Connect — capa de identidad sobre OAuth2 |
| **WSO2 API Manager** | Gateway institucional objetivo para APIs ONP |
| **M2M** | Machine-to-Machine — comunicación entre sistemas sin usuario humano |
| **SAST** | Static Application Security Testing — análisis estático de código |
| **SCA** | Software Composition Analysis — análisis de dependencias |
| **DAST** | Dynamic Application Security Testing — análisis en runtime |
| **CVE** | Common Vulnerabilities and Exposures — identificador de vulnerabilidad |
| **CVSS** | Common Vulnerability Scoring System — escala 0–10 de severidad |
| **MDC** | Mapped Diagnostic Context — contexto de log por hilo (SLF4J) |
| **PII** | Personally Identifiable Information — datos personales identificables |
| **IAM** | Identity and Access Management |
| **IdP** | Identity Provider — proveedor de identidad |
| **CAPTCHA** | Prueba de verificación humana para acceso externo |
| **SEGURITYSYS** | Esquema Oracle del SAA que contiene usuarios, roles y permisos |
| **LDAP** | Lightweight Directory Access Protocol — protocolo de directorio |
| **Circuit Breaker** | Patrón de resiliencia que corta llamadas a servicio no disponible (PT07) |
| **ADR** | Architecture Decision Record — registro de decisión de arquitectura |

---

## Apéndice A — Relación con otros lineamientos

| Lineamiento | Relación con LIN-SEC-APP-001 |
|---|---|
| LIN-API-REST-001 | Este lineamiento define el **qué** de seguridad en APIs; LIN-API-REST-001 define el **cómo** se publican en WSO2 y el gate de publicación |
| LIN-DEV-JAVA-001 | LIN-DEV-JAVA-001 8 es fuente autoritativa de observabilidad; este lineamiento es fuente autoritativa de seguridad en código Java |
| LIN-OBS-001 | Propietario de No PII en logs, Mask.java, propagación de user.id — este lineamiento referencia esas reglas sin redefinirlas |
| LIN-BD-ORA-001 | Propietario de reglas de BD; este lineamiento agrega la capa de privilegios mínimos y prohibición de acceso a esquemas SAA |
| LIN-K8S-001 | Propietario de K8s Secrets, imágenes seguras, Trivy en contenedores |
| LIN-CICD-001 | Propietario de gates de seguridad en pipeline (SonarQube, OWASP Dependency-Check, Trivy en CI) |
| LIN-FE-ANG-001 *(pendiente seguridad)* | Propietario de seguridad específica Angular — este lineamiento provee las reglas base |
