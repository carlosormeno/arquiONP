# LIN-API-REST-001 — Estándar de Servicios Web y APIs REST ONP

| | |
|---|---|
| **Código** | LIN-API-REST-001 |
| **Versión** | 0.1.8 |
| **Fecha** | 2026-08-09 |
| **Estado** | En revisión |
| **Clasificación** | Uso Interno (Técnico) |
| **Área responsable** | OTI — Innovación y Desarrollo |
| **Dirigido a** | Equipos de desarrollo, arquitectura y QA de la OTI |
| **Marco rector** | LIN-ARQ-001 — Marco Rector de Arquitectura de Software |

---

## Historial de versiones

| Versión | Fecha | Autor | Descripción |
|---|---|---|---|
| 0.1.0 | 2026-05-22 | Arquitectura OTI | Versión inicial |
| 0.1.1 | 2026-05-28 | Arquitectura OTI | Aclara el carácter normativo de `codDetRespuesta` y alinea el checklist de observabilidad con `LIN-OBS-001` y `LIN-K8S-001` |
| 0.1.2 | 2026-05-28 | Arquitectura OTI | Define la gobernanza operativa de `codDetRespuesta`, su dueño y el proceso de alta/cambio de códigos |
| 0.1.3 | 2026-07-10 | Arquitectura OTI | Migra Marco rector de `LIN-ARQ-000` (congelado) a `LIN-ARQ-001` (vigente). Corrige 5 citas a `LIN-DEV-JAVA-001 sección 11.4[.x]` → `sección 13.4[.x]`/`14` (renumeración interna nunca reflejada aquí) y 2 citas a la sección fantasma `LIN-ARQ-000 sección 9.5` → `LIN-ARQ-001 §5.3` (Four Golden Signals) |
| 0.1.4 | 2026-07-14 | Arquitectura OTI | Corrige el Apéndice A.3: la cita a Arquitectura Hexagonal decía `LIN-DIS-001 sección 3.2` (introducido por error en la corrección de v0.1.3) — el número real es `sección 2.3` |
| 0.1.5 | 2026-08-05 | Arquitectura OTI | **§8.3 deja de publicar valores propios de timeout** (Connection 5s / Read 10s), que divergían de la matriz por criticidad de `LIN-DIS-001 §6.1` (documento dueño) y del rango de `LIN-ARQ-001 §4.3` — tres fuentes distintas para el mismo control. Ahora referencia al dueño y conserva solo lo propio del contrato REST: la respuesta `504` / `codDetRespuesta 402` ante vencimiento (`GOB-CHK-001` H3) |
| 0.1.6 | 2026-08-09 | Arquitectura OTI | Revisión de fondo (`GOB-CHK-001` H24). **Seguridad:** `§7.2` proponía un RBAC propio con `hasRole('ROL_…')` —contrario a `LIN-SEC-APP-001 §5.3` numeral 4 y además inoperante, porque Spring antepone `ROLE_`— ahora usa permisos SAA con `hasAuthority`; `§7.4` daba los headers de seguridad como «recomendados» y los delegaba en el gateway en PoC, siendo **obligatorios** para el servicio según `LIN-SEC-APP-001 §7.3`; `§8.4` eximía a los servicios de rate limiting atribuyéndolo a ese mismo gateway, dejando a toda API sin control alguno. **Coherencia interna:** `§2.2` prohíbe `http://` y `§2.5` daba por sentado tráfico intra-cluster sobre HTTP; `§9.6` ponía las probes en `8080` mientras `§9.5` exige separar el puerto de gestión; `§3.1` y `§2.5` publicaban dos formas de URL canónica distintas. **Contrato:** `§3.3.2` clasificaba como «versión menor» —inexpresable en `/api/v{N}`— la incorporación de un campo obligatorio al request, que rompe a todo consumidor; la tabla se alinea con `LIN-VER-001 §17.2`–`§17.3`. Se añade `codDetRespuesta 302` / `429` para el límite de peticiones. **Gobernanza:** `§10.2` hacía de WSO2 la única vía a producción pese al PoC — se define vía transitoria sin eximir del gate; `§10.3` no pedía la **prueba de contrato** que `LIN-TEST-001 §6` declara obligatoria. **RFC 7807** figuraba como normativa de errores sin serlo: se explicita que la ONP no lo adopta y por qué |
| 0.1.7 | 2026-08-09 | Arquitectura OTI | Cierre de las dos decisiones que la revisión H24 elevó a Arquitectura. (1) **`codDetRespuesta 302` (HTTP 429) queda ratificado** e incorporado al catálogo normativo conforme al proceso de `§4.2.1(c)`. (2) El tráfico intra-cluster sobre HTTP se resuelve en **`ADR-TLS-INTERNO-001`** como excepción acotada a `LIN-SEC-APP-001 §7.1`, con `NetworkPolicy` obligatoria como control sustitutivo; `§2.2` y `§2.5` retiran la reserva y el gate de `§10.3` verifica la `NetworkPolicy` antes de autorizar producción |
| 0.1.8 | 2026-08-18 | Arquitectura OTI | El apartado de excepción titulaba «Proceso de excepción a este estándar» y no definía identificador: una desviación de este lineamiento se registraba como «un ADR», instrumento que `GOB-MAT-001` reserva a las decisiones **institucionales** del Comité. Pasa a **`EXC-API-NNN`**, con vigencia acotada y fecha de revisión obligatoria (`GOB-CHK-001` H38) |

---

## Tabla de contenidos

1. [Introducción](#1-introducción)
2. [Principios generales](#2-principios-generales)
3. [Diseño de la API](#3-diseño-de-la-api)
4. [Contrato de respuesta estándar](#4-contrato-de-respuesta-estándar)
5. [Manejo de errores](#5-manejo-de-errores)
6. [Documentación OpenAPI / Swagger](#6-documentación-openapi--swagger)
7. [Seguridad](#7-seguridad)
8. [Rendimiento y escalabilidad](#8-rendimiento-y-escalabilidad)
9. [Observabilidad](#9-observabilidad)
   - 9.1 Correlación de peticiones — X-Request-ID
   - 9.2 Trazas distribuidas
   - 9.3 Logging estructurado
   - 9.4 Log canónico de request
   - 9.5 Métricas
   - 9.6 Health checks
   - 9.7 Checklist mínimo de observabilidad
10. [Gobernanza](#10-gobernanza)

---

## 1. Introducción

### 1.1 Objetivo

Establecer las reglas, convenciones y buenas prácticas que deben aplicarse al diseño, implementación y documentación de todos los servicios web REST desarrollados en la Oficina de Tecnologías de la Información (OTI) de la ONP.

Este lineamiento busca garantizar consistencia, interoperabilidad, mantenibilidad y seguridad en las APIs que exponen los sistemas de la institución, tanto para consumo interno como externo.

### 1.2 Alcance

Aplica a:

- Todos los servicios web REST nuevos desarrollados por la OTI o por proveedores bajo supervisión de la OTI.
- Servicios existentes cuando sean sometidos a refactorización mayor o a incorporación de nuevas versiones.
- Integraciones entre sistemas internos de la ONP que utilicen servicios web.

No aplica a:

- Servicios SOAP legacy existentes en producción que no estén en proceso de renovación.
- Software adquirido a terceros sin acceso al código fuente.

### 1.3 Normativa de referencia

| Documento | Descripción |
|---|---|
| LIN-ARQ-001 — Marco Rector de Arquitectura de Software | Estilos arquitectónicos y principios |
| LIN-DEV-JAVA-001 — Estándar de Desarrollo Java ONP | Implementación Spring Boot, estructura Maven, Swagger/OpenAPI |
| LIN-OBS-001 — Log Centralizado, Trazabilidad y Observabilidad | Trazas OTEL, logging estructurado ECS, métricas, No PII |
| INFORME-0000XX-2024-OTI.ID — Lineamientos de Servicios Web v2.0 | Antecedente institucional |
| OpenAPI Specification 3.0 | Especificación de documentación de APIs |
| RFC 7807 — Problem Details for HTTP APIs | Referencia de diseño. **La ONP no lo adopta como formato de error**: el contrato institucional es `ApiResponseWrapper` (ver [sección 4.1](#41-estructura-apiresponsewrapper)) |
| Ley N.° 29733 — Protección de Datos Personales | Marco legal de privacidad |

### 1.4 Términos y definiciones

| Término | Definición |
|---|---|
| **API** | Application Programming Interface — contrato que define cómo dos sistemas se comunican |
| **REST** | Representational State Transfer — estilo arquitectónico para diseñar APIs sobre HTTP |
| **Endpoint** | URL específica que expone una operación sobre un recurso |
| **Recurso** | Entidad o dato accesible y manipulable a través de la API (ej. afiliado, pensión) |
| **Idempotencia** | Propiedad de una operación que produce el mismo resultado al ejecutarse una o varias veces |
| **ApiResponseWrapper** | Estructura de respuesta estándar ONP — envuelve todo resultado de una API REST |
| **codDetRespuesta** | Código de resultado institucional ONP de 3 dígitos, complementario al código HTTP |
| **X-Request-ID** | Header HTTP de correlación que permite rastrear una petición a través del sistema |
| **API Gateway** | Componente técnico de entrada que gestiona acceso, seguridad, enrutamiento y rate limiting de APIs |
| **API Manager** | Plataforma institucional de gestión del ciclo de vida de APIs: registro, publicación, versiones, suscripciones, portal de desarrolladores, analytics y gobierno |
| **WSO2 API Manager** | Plataforma de API Management adoptada por la ONP. Incluye gateway, publisher, dev portal, key manager y analytics |

---

## 2. Principios generales

### 2.1 REST como estilo arquitectónico obligatorio

Todos los servicios web nuevos de la ONP deben implementarse siguiendo el estilo REST. Los principios fundamentales que deben cumplirse son:

- **Interfaz uniforme:** uso explícito de métodos HTTP (GET, POST, PUT, PATCH, DELETE).
- **Sin estado (stateless):** cada petición debe contener toda la información necesaria para ser procesada; el servidor no almacena estado de sesión entre peticiones.
- **Recursos identificados por URI:** cada recurso tiene una URI única e intuitiva.
- **JSON como formato de intercambio:** salvo excepciones justificadas y aprobadas por el Arquitecto de Soluciones, el formato de datos es JSON (RFC 8259).

> **Servicios SOAP:** Los servicios SOAP legados existentes se mantienen sin cambios. Para nuevos desarrollos que necesiten consumir servicios SOAP externos, ver [sección 10.4](#104-consumo-de-servicios-soap-legacy).

### 2.2 HTTPS obligatorio

Toda comunicación con APIs de la ONP debe realizarse a través de HTTPS con certificado SSL/TLS válido. Las URLs que inicien con `http://` quedan prohibidas en entornos QA y PROD, conforme a `LIN-SEC-APP-001 §7.1`, que solo admite excepción para desarrollo local en la máquina del desarrollador.

> **Tráfico intra-cluster.** El tramo comprendido entre el punto de terminación TLS y el pod destino, dentro del mismo cluster, puede viajar sobre HTTP: es la excepción acotada de `LIN-SEC-APP-001 §7.1`, decidida en **`ADR-TLS-INTERNO-001`**. Se sostiene sobre un control sustitutivo, no sobre una dispensa: exige `NetworkPolicy` obligatoria en el servicio (`LIN-K8S-001 §9.1`), terminación TLS en el Ingress o en el gateway, y migración a mTLS cuando exista malla de servicios. **Un servicio sin `NetworkPolicy` no puede acogerse a ella.** El tráfico hacia servicios externos al cluster —SAA, RENIEC, SUNAT, Oracle— exige HTTPS sin excepción.

### 2.3 Codificación de caracteres

Usar UTF-8 en todos los intercambios. El header `Content-Type` debe incluir el charset explícitamente:

```
Content-Type: application/json; charset=UTF-8
```

### 2.4 Idioma

Los recursos, parámetros y mensajes se nombran en **español** siguiendo el lenguaje ubicuo del dominio de negocio de la ONP. Excepción: términos técnicos sin traducción establecida (ej. `token`, `endpoint`).

### 2.5 Gestión de APIs — API Gateway y API Manager

#### Plataforma institucional

> **Estado de implementación:** WSO2 API Manager se encuentra en **fase PoC** — no está operativo como gateway centralizado en producción. SAA con `SaaTokenValidationFilter` es el mecanismo institucional vigente. Las reglas de esta sección describen el **estándar objetivo**; la transición está gobernada por **ADR-WSO2-001**. Toda referencia a WSO2 como obligación operativa debe leerse como objetivo de diseño hasta que Arquitectura y Plataforma emitan comunicación formal de graduación del PoC.

La ONP adopta **WSO2 API Manager** como plataforma institucional objetivo de gestión de APIs. Todo servicio REST nuevo debe diseñarse asumiendo que será publicado y gestionado a través de esta plataforma una vez operativa.

WSO2 API Manager integra dos niveles de responsabilidad que deben distinguirse claramente:

| Nivel | Componente WSO2 | Responsabilidad |
|---|---|---|
| **Gateway técnico** | WSO2 Gateway | Entrada de tráfico, enrutamiento, validación de tokens, rate limiting, políticas de seguridad, logs de acceso |
| **API Management** | Publisher + Dev Portal + Key Manager + Analytics | Ciclo de vida de la API: registro, publicación, versionado, documentación, suscripción de consumidores, aprobación de acceso, métricas de consumo y retiro/deprecación |

#### Reglas obligatorias (estándar objetivo)

Estas reglas son de cumplimiento obligatorio **una vez que WSO2 esté operativo en producción**. Mientras permanezca en PoC, aplican como guía de diseño para que los servicios nuevos sean compatibles con la plataforma cuando se active la transición (ADR-WSO2-001).

**Toda API REST institucional debe publicarse a través de WSO2 API Manager.** No se permite exponer servicios directamente a consumidores externos sin pasar por la plataforma. Excepción: comunicación interna entre servicios dentro del mismo cluster K8s, con justificación en ADR.

**No se crea un gateway por aplicación.** La exposición de APIs se centraliza en WSO2. Crear instancias de gateway propias por proyecto requiere ADR aprobado por Arquitectura.

#### Responsabilidades del gateway técnico (WSO2 Gateway)

El gateway es responsable de:

- Aplicación de rate limiting por cliente (IP o token) — los servicios no implementan rate limiting internamente
- Inyección de headers de correlación (`X-Request-ID`, `X-Forwarded-For`)
- Logging de acceso (quién llamó, cuándo, qué endpoint, status de respuesta)
- Enrutamiento al backend según la versión de la API (`/v1/`, `/v2/`)
- Terminación TLS en el perímetro. Los backend reciben tráfico interno sobre HTTP dentro del cluster, conforme a la excepción acotada de `LIN-SEC-APP-001 §7.1` y `ADR-TLS-INTERNO-001`, que exige `NetworkPolicy` en el servicio como control sustitutivo (ver [sección 2.2](#22-https-obligatorio))

#### Validación del token SAA — estado actual y objetivo futuro

> **Estado actual (WSO2 en fase PoC — no operativo como gateway centralizado):**
> Cada servicio Spring Boot implementa `SaaTokenValidationFilter` (`@Order(3)`), que realiza una llamada síncrona al endpoint de SAA para validar el token opaco en cada petición entrante. Este filtro es **obligatorio** en todos los servicios mientras WSO2 no esté operativo como gateway. Ver [sección 7.1](#71-autenticacion-saa-token) y LIN-SEC-APP-001 sección 8.3.
>
> **Objetivo futuro (cuando WSO2 sea el gateway centralizado operativo):**
> WSO2 Gateway validará el token SAA en el perímetro e inyectará headers de contexto de usuario sanitizados (`X-User-Id`, `X-User-Roles`) hacia el backend. En ese escenario, `SaaTokenValidationFilter` podrá eliminarse de los servicios individuales y el backend confiará en los headers del gateway. Esta transición requerirá ADR y confirmación de Plataforma.

#### Responsabilidades de la plataforma de API Management

La plataforma es responsable de:

- **Registro y publicación:** toda API nueva se registra en WSO2 Publisher con su especificación OpenAPI antes de exponerse
- **Versionado del ciclo de vida:** la API pasa por estados `CREATED → PUBLISHED → DEPRECATED → RETIRED`
- **Portal de desarrolladores:** los consumidores descubren, prueban y suscriben APIs desde el Dev Portal
- **Planes y suscripciones:** el acceso a una API requiere suscripción aprobada con un plan (ej. `ILIMITADO`, `10 req/min`, `100 req/día`)
- **Analytics:** métricas de consumo por API, por suscriptor, por plan — visibles en el dashboard de WSO2

#### Relación con el ciclo de desarrollo

```
Desarrollador                 OTI Arquitectura             WSO2 API Manager
──────────────               ──────────────────           ─────────────────
Desarrolla servicio    →     Revisa spec OpenAPI    →     Publica en Publisher
Genera spec OpenAPI          Aprueba publicación          Configura políticas
Expone /api/v1/...           Define plan de acceso        Habilita en Dev Portal
                                                          Consumidor suscribe y recibe key
```

#### Antipatrón prohibido

> Este antipatrón aplica plenamente cuando WSO2 esté operativo. Hoy, mientras el PoC no haya graduado, el patrón "CORRECTO" es el diseño objetivo que los servicios deben anticipar — no un requisito operativo inmediato.

```
# PROHIBIDO — el servicio expuesto directamente sin gateway
Consumidor → https://mi-servicio.onp.gob.pe/api/v1/expedientes

# CORRECTO (objetivo) — toda exposición pasa por WSO2
Consumidor → https://apis.onp.gob.pe/pensiones/v1/expedientes  (WSO2 Gateway)
                                                    ↓
                                    mi-servicio.onp.internal:8080
```

> **Para publicar una API en WSO2:** ver **[sección 10.3](#103-gate-de-publicacion-en-wso2)** — gate de publicación con los requisitos técnicos y de gobernanza que el equipo de desarrollo debe completar antes de que Arquitectura active el estado `PUBLISHED`.

---

## 3. Diseño de la API

### 3.1 Estructura de la URL

La estructura estándar de una URL en APIs ONP es:

```
https://<host>/api/v{N}/{recurso-en-plural}/{id}/{sub-recurso}
```

> **Esta es la ruta que expone el servicio**, la que el equipo implementa en sus `@RequestMapping`. La URL que ve el consumidor externo puede diferir: cuando WSO2 esté operativo, el gateway publica un *context path* propio por dominio y enruta hacia el backend —`https://apis.onp.gob.pe/pensiones/v1/...` → `mi-servicio.onp.internal:8080/api/v1/...` (ver [sección 2.5](#25-gestion-de-apis-api-gateway-y-api-manager))—. El backend no cambia su ruta para acomodar al gateway.

Ejemplos:

```
GET  https://api.onp.gob.pe/api/v1/afiliados
GET  https://api.onp.gob.pe/api/v1/afiliados/12345
GET  https://api.onp.gob.pe/api/v1/afiliados/12345/periodos-aporte
POST https://api.onp.gob.pe/api/v1/pensiones
```

#### 3.1.1 Sustantivos en plural para nombrar recursos

Los segmentos de ruta deben ser **sustantivos en plural** que representen la entidad. Nunca verbos.

| Correcto | Incorrecto |
|---|---|
| `/afiliados` | `/afiliado` |
| `/periodos-aporte` | `/obtenerPeriodosAporte` |
| `/liquidaciones` | `/calcularLiquidacion` |
| `/documentos` | `/getDocumentos` |

#### 3.1.2 Formato de los segmentos: kebab-case

Los segmentos de ruta con más de una palabra usan **kebab-case** (palabras separadas con guión). Los identificadores de recursos en path y los parámetros de query usan **camelCase**.

```
/periodos-aporte          ← segmento de recurso: kebab-case
/afiliados/{afiliadoId}   ← path parameter: camelCase
?fechaInicio=2025-01-01   ← query parameter: camelCase
```

#### 3.1.3 Jerarquía limitada

La profundidad de anidamiento no debe superar **dos recursos**. Jerarquías más profundas dificultan la legibilidad y el mantenimiento.

| Correcto | Incorrecto |
|---|---|
| `/afiliados/{id}/periodos-aporte` | `/afiliados/{id}/empleadores/{empId}/aportes/{aId}/detalle` |

Cuando se necesite filtrar por múltiples criterios relacionados, usar **query parameters** en lugar de URI anidada:

```
GET /liquidaciones?afiliadoId=123&empleadorId=456
```

#### 3.1.4 Sin verbos en la URI

La acción la define el método HTTP, no la URI.

| Incorrecto | Correcto |
|---|---|
| `POST /afiliados/crear` | `POST /afiliados` |
| `GET /afiliados/obtener/123` | `GET /afiliados/123` |
| `PUT /afiliados/actualizar/123` | `PUT /afiliados/123` |
| `DELETE /afiliados/eliminar/123` | `DELETE /afiliados/123` |

**Excepción permitida:** operaciones que no corresponden a CRUD directo pueden usar un verbo descriptivo como segmento final cuando no existe un recurso natural que las represente.

```
GET  /afiliados/buscar?nombre=Garcia&regimen=SPP
GET  /liquidaciones/calcular?afiliadoId=123
```

### 3.2 Métodos HTTP

| Método | Operación | Idempotente | Seguro | Body en request | Body en response |
|---|---|---|---|---|---|
| GET | Lectura | Sí | Sí | No | Sí |
| POST | Creación | No | No | Sí | Sí |
| PUT | Actualización/Reemplazo completo | Sí | No | Sí | Sí |
| PATCH | Actualización parcial | No | No | Sí | Sí |
| DELETE | Eliminación | Sí | No | No | No (204) |
| HEAD | Metadatos sin cuerpo | Sí | Sí | No | No |

**Reglas de uso:**

- `GET` nunca debe modificar el estado del servidor.
- `POST /recurso` crea un nuevo registro; el servidor asigna el ID.
- `PUT /recurso/{id}` reemplaza el recurso completo. Si el recurso no existe, puede crearlo (upsert), siempre que esté documentado.
- `PATCH /recurso/{id}` modifica solo los campos enviados en el body.
- `DELETE /recurso/{id}` elimina el recurso. Devuelve `204 No Content` sin cuerpo.

### 3.3 Versionado

#### 3.3.1 Versión en el path

La versión se incluye como primer segmento del path después de `/api/`:

```
/api/v1/afiliados
/api/v2/afiliados
```

#### 3.3.2 Cuándo incrementar la versión

El path solo expresa la versión **mayor** (`/api/v1`, `/api/v2`). Por tanto, la única distinción operativa es si el cambio es compatible —no toca el path— o incompatible —obliga a publicar `/api/v{N+1}`—. La clasificación se alinea con `LIN-VER-001 §17.2`–`§17.3`, documento dueño del control de cambios:

| Tipo de cambio | Compatibilidad | Acción |
|---|---|---|
| Agregar campo opcional a la respuesta | Compatible | Sin cambio de versión |
| Agregar endpoint nuevo | Compatible | Sin cambio de versión |
| Ampliar descripción o documentar un código de respuesta existente | Compatible | Sin cambio de versión |
| Agregar campo **obligatorio** al request | **Incompatible** | Nueva versión mayor |
| Cambiar tipo de dato de un campo | **Incompatible** | Nueva versión mayor |
| Eliminar o renombrar campo | **Incompatible** | Nueva versión mayor |
| Cambiar comportamiento o semántica de error de un endpoint | **Incompatible** | Nueva versión mayor |
| Cambiar ruta o método HTTP | **Incompatible** | Nueva versión mayor |
| Cambiar reglas de autenticación o autorización | **Incompatible** | Nueva versión mayor |

> Un campo obligatorio nuevo en el request **rompe a todo consumidor existente**, que dejará de recibir `200`. La versión anterior de esta tabla lo clasificaba como «nueva versión menor», categoría que además el esquema `/api/v{N}` no puede representar.
>
> **Todo cambio incompatible exige ADR**, evaluación de versionamiento, comunicación a los consumidores y actualización del OpenAPI (`LIN-VER-001 §17.3`).

#### 3.3.3 Coexistencia de versiones

Cuando se lanza `v2`, la versión `v1` debe mantenerse operativa durante un período de **deprecación mínimo de 6 meses**, comunicado a los consumidores con antelación. Durante ese período, `v1` devuelve el header:

```
Deprecation: true
Sunset: Sat, 22 Nov 2026 00:00:00 GMT
```

### 3.4 Parámetros HTTP

#### 3.4.1 Headers obligatorios en toda petición

| Header | Tipo | Descripción |
|---|---|---|
| `Authorization` | Request | `Bearer <token>` — token SAA de autenticación (ver [sección 7.1](#71-autenticacion-saa-token)) |
| `Content-Type` | Request | `application/json; charset=UTF-8` (cuando hay body) |
| `X-Request-ID` | Request/Response | ID de correlación. El servicio lo genera si no llega en el request |

#### 3.4.2 Path parameters

Para identificar un recurso específico. Deben usar el tipo de dato más restrictivo posible (Long en lugar de String cuando corresponda).

```
GET /afiliados/{afiliadoId}    ← Long
GET /documentos/{codigoDoc}    ← String cuando es un código alfanumérico
```

#### 3.4.3 Query parameters — filtrado, ordenamiento y paginación

**Filtrado:**

```
GET /afiliados?regimen=SPP&estado=ACTIVO&fechaAfiliacionDesde=2020-01-01
```

**Ordenamiento:**

```
GET /afiliados?ordenarPor=apellidos&orden=asc
```

Valores permitidos para `orden`: `asc`, `desc`.

**Paginación — obligatoria para consultas que puedan retornar más de 500 registros:**

| Parámetro | Tipo | Default | Descripción |
|---|---|---|---|
| `pagina` | Integer | 0 | Número de página (base 0) |
| `tamanio` | Integer | 20 | Registros por página |

La respuesta con paginación incluye los datos de paginación en el campo `meta`:

```json
{
  "codHttp": 200,
  "codDetRespuesta": "000",
  "menDetRespuesta": "Operacion completada correctamente.",
  "data": [ ... ],
  "errors": null,
  "meta": {
    "timestamp": "2026-05-22T10:30:00Z",
    "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "version": "1.0.0",
    "pagina": 0,
    "tamanio": 20,
    "totalElementos": 1543,
    "totalPaginas": 78
  }
}
```

---

## 4. Contrato de respuesta estándar

### 4.1 Estructura ApiResponseWrapper

Todos los servicios REST de la ONP deben retornar sus respuestas usando `ApiResponseWrapper`. Esta estructura es el contrato institucional que garantiza consistencia entre servicios y facilita la correlación con logs y trazas.

> **`codHttp` es informativo; la fuente de verdad es el status line HTTP.** El campo duplica en el body el código que ya viaja en la línea de estado, para comodidad de clientes que solo inspeccionan el JSON. Ambos valores **deben coincidir siempre**: responder `HTTP 200` con `"codHttp": 500` es un defecto —los proxys, el gateway, las métricas `http.server.requests` y las alertas de Kibana leen el status line, no el body, de modo que el error quedaría invisible en toda la cadena de observabilidad—. Un servicio nunca devuelve `200` para señalar un fallo.

> **Relación con RFC 7807 (`application/problem+json`).** La ONP **no** adopta Problem Details como formato de error. Se optó por `ApiResponseWrapper` porque unifica respuestas de éxito y de error en una sola estructura, transporta el catálogo institucional `codDetRespuesta` —que RFC 7807 no contempla— y da continuidad al contrato ya usado por los consumidores existentes. Un servicio no debe emitir `Content-Type: application/problem+json`. Si un consumidor externo lo exige contractualmente, la traducción se resuelve en el gateway o en un adaptador dedicado, con ADR.

| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `codHttp` | Integer | Sí | Réplica informativa del status HTTP de la respuesta (ver nota abajo) |
| `codDetRespuesta` | String | Sí | Código de resultado institucional ONP (ver [sección 4.2](#42-tabla-de-coddetrespuesta)) |
| `menDetRespuesta` | String | Sí | Mensaje descriptivo del resultado |
| `data` | Object / Array / null | Sí | Resultado de la operación. `null` en caso de error |
| `errors` | Array / null | Solo en validación | Lista de errores de validación por campo |
| `meta.timestamp` | String (ISO 8601) | Sí | Fecha y hora de la respuesta en UTC |
| `meta.requestId` | String (UUID) | Sí | ID de correlación (X-Request-ID) |
| `meta.version` | String | Sí | Versión del servicio que respondió |
| `meta.pagina` | Integer | Solo paginado | Página actual (base 0) |
| `meta.tamanio` | Integer | Solo paginado | Registros por página |
| `meta.totalElementos` | Long | Solo paginado | Total de registros que coinciden con el filtro |
| `meta.totalPaginas` | Integer | Solo paginado | Total de páginas |

> Para la implementación Spring Boot de `ApiResponseWrapper<T>` (clase completa, factory methods, `CampoError`), ver **LIN-DEV-JAVA-001 sección 13.4.4**.

### 4.2 Tabla de codDetRespuesta

`codDetRespuesta` es un **catálogo normativo institucional** definido en este lineamiento para el contrato de respuesta API. Este documento no obliga a que el catálogo exista como tabla Oracle física; si un sistema requiere persistirlo o parametrizarlo, esa decisión debe documentarse explícitamente en diseño y mantenerse alineada con esta tabla normativa.

| Código | Categoría | Descripción | codHttp asociado |
|---|---|---|---|
| `000` | Éxito | Operación completada correctamente | 200 |
| `001` | Éxito parcial | Operación completada con advertencias | 200 |
| `100` | Validación | Error de validación en los datos enviados | 400 |
| `101` | Validación | Campo obligatorio ausente | 400 |
| `102` | Validación | Formato de dato incorrecto | 400 |
| `103` | Validación | Valor fuera del rango permitido | 400 |
| `200` | Negocio | Regla de negocio no cumplida | 422 |
| `201` | Negocio | Recurso no encontrado | 404 |
| `202` | Negocio | Recurso ya existe (duplicado) | 409 |
| `203` | Negocio | Estado inválido para la operación | 422 |
| `300` | Autorización | No autenticado | 401 |
| `301` | Autorización | No autorizado para esta operación | 403 |
| `302` | Autorización | Límite de peticiones excedido | 429 | 
| `400` | Integración | Servicio externo no disponible | 503 |
| `401` | Integración | Servicio externo respondió con error | 502 |
| `402` | Integración | Timeout en llamada a servicio externo | 504 |
| `500` | Sistema | Error interno del servidor | 500 |
| `501` | Sistema | Error de conexión a base de datos | 500 |
| `502` | Sistema | Error de configuración del servicio | 500 |

### 4.2.1 Gobernanza de `codDetRespuesta`

`codDetRespuesta` tiene tres niveles de gobierno que deben distinguirse explícitamente:

| Nivel | Regla |
|---|---|
| Normativo | La tabla de este lineamiento es la fuente institucional oficial del catálogo |
| Implementación | Cada servicio debe usar únicamente códigos aprobados en este lineamiento |
| Persistencia | La existencia de tabla, archivo paramétrico o repositorio de configuración es opcional y depende del diseño del sistema |

#### a) Dueño del catálogo

El dueño normativo de `codDetRespuesta` es **Arquitectura OTI**. Ningún equipo de proyecto puede crear, reutilizar o reinterpretar códigos fuera de este catálogo sin aprobación explícita.

#### b) Regla de persistencia

La persistencia física del catálogo **no es obligatoria por defecto**. Se admiten tres escenarios:

- catálogo solo documental, cuando el servicio codifica los valores aprobados directamente en su capa de aplicación;
- catálogo parametrizado en configuración o repositorio interno, cuando varios componentes del mismo sistema deben reutilizarlo;
- catálogo persistido en base de datos, cuando existe una necesidad operativa real de administración centralizada o consulta compartida.

Si un sistema decide persistir `codDetRespuesta`, debe cumplir estas reglas:

- la tabla o estructura persistida no puede contradecir el catálogo normativo de este lineamiento;
- el diseño físico y su mantenimiento deben documentarse en arquitectura y, si aplica, en `LIN-BD-ORA-001`;
- una actualización del catálogo persistido no equivale a aprobar nuevos códigos por sí sola.

#### c) Alta, cambio y retiro de códigos

Todo nuevo código o cambio de significado debe seguir este proceso mínimo:

1. El equipo solicitante documenta la necesidad funcional o técnica del nuevo código.
2. Arquitectura OTI evalúa si el caso puede resolverse con un código existente.
3. Si no existe equivalencia, Arquitectura aprueba la incorporación o ajuste del código en este lineamiento.
4. El servicio consumidor actualiza su implementación, OpenAPI y pruebas de contrato si aplica.
5. Si existe catálogo persistido, el equipo responsable sincroniza la estructura física con la versión aprobada del lineamiento.

#### d) Restricciones obligatorias

- No se permite crear códigos locales por sistema sin aprobación de Arquitectura.
- No se permite reutilizar un código existente con significado distinto.
- No se permite que OpenAPI, `ApiResponseWrapper`, documentación funcional y catálogo persistido diverjan entre sí.
- Todo cambio aprobado en `codDetRespuesta` debe reflejarse en el historial de versiones de este lineamiento.

### 4.3 Ejemplos de respuesta

**Respuesta exitosa — GET:**

```json
{
  "codHttp": 200,
  "codDetRespuesta": "000",
  "menDetRespuesta": "Operacion completada correctamente.",
  "data": {
    "afiliadoId": 12345,
    "apellidos": "García Pérez",
    "nombres": "Juan Carlos",
    "regimen": "SPP",
    "estado": "ACTIVO"
  },
  "errors": null,
  "meta": {
    "timestamp": "2026-05-22T10:30:00Z",
    "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "version": "1.0.0"
  }
}
```

**Error de validación — POST con datos inválidos:**

```json
{
  "codHttp": 400,
  "codDetRespuesta": "100",
  "menDetRespuesta": "Error de validacion en los datos enviados.",
  "data": null,
  "errors": [
    { "campo": "regimen", "mensaje": "El campo es obligatorio." },
    { "campo": "fechaInicioLaboral", "mensaje": "El formato debe ser ISO 8601 (yyyy-MM-dd)." }
  ],
  "meta": {
    "timestamp": "2026-05-22T10:30:01Z",
    "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "version": "1.0.0"
  }
}
```

**Error de negocio — recurso no encontrado:**

```json
{
  "codHttp": 404,
  "codDetRespuesta": "201",
  "menDetRespuesta": "El afiliado solicitado no existe.",
  "data": null,
  "errors": null,
  "meta": {
    "timestamp": "2026-05-22T10:30:02Z",
    "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "version": "1.0.0"
  }
}
```

**Error de sistema — excepción no controlada:**

```json
{
  "codHttp": 500,
  "codDetRespuesta": "500",
  "menDetRespuesta": "Error interno del servidor. Referencie el requestId al equipo de soporte.",
  "data": null,
  "errors": null,
  "meta": {
    "timestamp": "2026-05-22T10:30:03Z",
    "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "version": "1.0.0"
  }
}
```

> **Seguridad:** El mensaje de error de sistema nunca debe exponer detalles técnicos internos (stack trace, nombre de clase, query SQL) en la respuesta al cliente. Esos detalles se registran únicamente en los logs del servidor.

---

## 5. Manejo de errores

### 5.1 Mapeo de código HTTP a codDetRespuesta

| Situación | Método HTTP | codHttp | codDetRespuesta |
|---|---|---|---|
| Operación exitosa (no creación) | GET / PUT / PATCH | 200 | 000 |
| Recurso creado exitosamente | POST | **201** | 000 |
| Eliminación exitosa | DELETE | 204 | — (sin cuerpo) |
| Datos de entrada inválidos | POST/PUT/PATCH | 400 | 100–103 |
| No autenticado | Cualquiera | 401 | 300 |
| No autorizado | Cualquiera | 403 | 301 |
| Límite de peticiones excedido | Cualquiera | 429 | 302 |
| Recurso no encontrado | GET/PUT/DELETE | 404 | 201 |
| Recurso duplicado | POST | 409 | 202 |
| Regla de negocio | POST/PUT/PATCH | 422 | 200, 203 |
| Servicio externo caído | Cualquiera | 503 | 400 |
| Error en servicio externo | Cualquiera | 502 | 401 |
| Timeout servicio externo | Cualquiera | 504 | 402 |
| Error interno | Cualquiera | 500 | 500–502 |

### 5.2 GlobalExceptionHandler

Todo servicio REST debe implementar un `@ControllerAdvice` que capture las excepciones y las transforme en `ApiResponseWrapper`. Esto garantiza que **ninguna excepción no controlada llegue al cliente con un stack trace en texto plano**.

El handler debe cubrir como mínimo:

| Excepción | Respuesta |
|---|---|
| `MethodArgumentNotValidException` (Bean Validation) | 400 / `100` con lista de errores por campo |
| `ConstraintViolationException` | 400 / `100` |
| Excepción de negocio personalizada (recurso no encontrado) | 404 / `201` |
| Excepción de negocio personalizada (regla de negocio) | 422 / `200` |
| `HttpMessageNotReadableException` (JSON malformado) | 400 / `102` |
| `Exception` (genérico — fallback) | 500 / `500` |

> La excepción genérica debe loggearse con nivel `ERROR` incluyendo el stack trace, pero la respuesta al cliente solo debe contener el `requestId` para referencia al equipo de soporte.

---

## 6. Documentación OpenAPI / Swagger

### 6.1 Obligatoriedad

La documentación Swagger es un **requisito de entrega, no opcional**. Todo servicio REST que exponga endpoints HTTP debe documentarlos con OpenAPI 3.0 antes de pasar a revisión de código. Un servicio sin documentación Swagger no se considera completo.

### 6.2 Configuración

La herramienta de documentación es **SpringDoc OpenAPI** (`springdoc-openapi-starter-webmvc-ui`).

**Habilitación por entorno:**

| Entorno | Swagger UI | Observación |
|---|---|---|
| DEV | Habilitado | Siempre activo |
| QA | Habilitado | Siempre activo |
| PROD | Deshabilitado por defecto | Activar puntualmente vía variable de entorno `SWAGGER_ENABLED=true` y deshabilitar al terminar |

> Para la configuración completa de SpringDoc (dependencia Maven, `OpenApiConfig.java`, YAML por entorno y activación temporal en PROD), ver **LIN-DEV-JAVA-001 sección 13.4.1–13.4.3**.

### 6.3 Anotaciones obligatorias

Cada `@RestController` debe tener:

- `@Tag(name, description)` — agrupa los endpoints en Swagger UI bajo el nombre del dominio.
- `@Operation(summary, description)` — describe cada endpoint.
- `@ApiResponses` con todos los códigos HTTP posibles — mínimo: el código de éxito, 400 (si hay body), 404 (si busca por ID), 500.
- `@Schema` en las clases DTO de request y response — describe cada campo con descripción y ejemplo.

Todos los endpoints deben retornar `ApiResponseWrapper` como tipo de respuesta en las anotaciones Swagger.

**Reglas de nomenclatura en anotaciones:**

| Atributo | Regla |
|---|---|
| `@Tag name` | Nombre del dominio en español, singular, con mayúscula inicial. Sin "Controller" ni "API". Ej: `Afiliaciones` |
| `@Operation summary` | Verbo en infinitivo + objeto. Máx 80 chars. Sin punto. Ej: `Obtener afiliado por ID` |
| `@ApiResponse description` | Cuándo ocurre esa respuesta. Terminar con punto. Ej: `Afiliado no encontrado.` |

> Para los ejemplos de código completos de cada método HTTP (GET, POST, PUT, DELETE) con `@Operation`, `@ApiResponses` y `@Schema`, ver **LIN-DEV-JAVA-001 sección 13.4.6–13.4.7**.

---

## 7. Seguridad

### 7.1 Autenticación — SAA Token

Todos los endpoints de la ONP requieren el token emitido por **SAA** (Sistema de Autenticación y Autorización institucional) en el header `Authorization`:

```
Authorization: Bearer <token-saa>
```

El token SAA es **opaco** (no es JWT — no es autocontenido ni verificable localmente). El servicio no puede decodificarlo por su cuenta; la validación la realiza el `SaaTokenValidationFilter` mediante una llamada al endpoint de validación de SAA en cada request. Ver **LIN-SEC-APP-001 sección 8.3** para la implementación del filtro.

> **Objetivo futuro:** reemplazar SAA por OAuth2/OIDC mediante **WSO2 API Manager** (actualmente en PoC, no operacional en producción). Cuando esté operativo, el token pasará a ser JWT verificable localmente y este lineamiento se actualizará. Ver LIN-SEC-APP-001 para el modelo objetivo con WSO2.

**Manejo de fallos de autenticación:**

| Situación | codHttp | codDetRespuesta |
|---|---|---|
| Token ausente | 401 | 300 |
| Token expirado | 401 | 300 |
| Token inválido | 401 | 300 |
| Sin permisos para el recurso | 403 | 301 |

### 7.2 Autorización — permisos SAA

> **Documento dueño: `LIN-SEC-APP-001 §5.3`–`§5.4`.**

El control de acceso se basa en los **permisos que SAA devuelve para el usuario**, no en un modelo de roles propio de la aplicación: `LIN-SEC-APP-001 §5.3` numeral 4 prohíbe implementar RBAC propio cuando el permiso ya existe en SAA. Cada endpoint declara explícitamente qué permiso requiere, y la verificación se realiza a nivel del servicio, no solo en el Gateway.

```java
@PreAuthorize("hasAuthority('REGISTRAR_AFILIACION')")
@PostMapping
public ResponseEntity<ApiResponseWrapper> registrar(...) { ... }
```

> **Por qué `hasAuthority` y no `hasRole`.** En Spring Security `hasRole('X')` comprueba en realidad el authority `ROLE_X`: antepone el prefijo automáticamente. Los permisos SAA se cargan en el `SecurityContext` con su nombre tal cual (`LIN-SEC-APP-001 §5.4`), sin prefijo `ROLE_`, de modo que un `hasRole('ROL_GESTOR_AFILIACIONES')` buscaría `ROLE_ROL_GESTOR_AFILIACIONES` y **nunca coincidiría** — el endpoint quedaría inaccesible para todos. Usar siempre `hasAuthority` con el nombre exacto del permiso SAA.

### 7.3 Validación de inputs

Todo campo del request body debe ser validado con **Bean Validation (Jakarta Validation)** antes de procesarse en la capa de negocio. Las anotaciones mínimas a usar:

| Anotación | Uso |
|---|---|
| `@NotNull` | Campo no puede ser null |
| `@NotBlank` | String no puede ser null, vacío ni solo espacios |
| `@Size(min, max)` | Longitud de String o colección |
| `@Min` / `@Max` | Rango numérico |
| `@Pattern` | Formato con expresión regular |
| `@Email` | Formato de correo electrónico |
| `@Past` / `@Future` | Fechas en el pasado/futuro |

El Controller debe anotar el `@RequestBody` con `@Valid` para activar la validación automática. El `GlobalExceptionHandler` captura el `MethodArgumentNotValidException` y lo transforma en respuesta `100`.

### 7.4 Headers de seguridad

> **Documento dueño: `LIN-SEC-APP-001 §7.3`.** Estos headers son **obligatorios**, no recomendados.

Mientras WSO2 no esté operativo como gateway centralizado, **es el propio servicio Spring Boot quien debe emitirlos** (`LIN-SEC-APP-001 §7.1`: los controles se configuran en la aplicación cuando no hay gateway). Delegarlos en una plataforma que aún está en PoC equivale a no emitirlos.

| Header | Valor obligatorio |
|---|---|
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains` |
| `X-Content-Type-Options` | `nosniff` |
| `X-Frame-Options` | `DENY` |
| `Cache-Control` | `no-store` |

Ver la configuración `SecurityConfig` completa en `LIN-SEC-APP-001 §7.3`. Cuando WSO2 sea el gateway operativo, la emisión podrá centralizarse en el perímetro; la transición requiere ADR.

### 7.5 CORS

Los servicios REST configuran CORS según el entorno. Solo deben aceptar peticiones de los orígenes autorizados por el equipo de Arquitectura.

```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
            "https://*.onp.gob.pe",
            "http://localhost:4200"  // solo en DEV
        ));
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization","Content-Type","X-Request-ID"));
        config.setExposedHeaders(List.of("X-Request-ID"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
```

### 7.6 Protección contra inyecciones

- Usar **consultas parametrizadas** (JPA/Hibernate o PreparedStatement). Prohibido construir queries SQL concatenando strings con datos del usuario.
- Validar y sanitizar todos los inputs con Bean Validation antes de procesarlos.
- Prohibido usar `eval`, `exec` o equivalentes con datos del usuario en cualquier contexto.

---

## 8. Rendimiento y escalabilidad

### 8.1 Paginación

Las consultas que puedan retornar más de 500 registros **deben implementar paginación**. No existe justificación técnica para retornar colecciones sin paginar de ese tamaño.

La paginación usa los parámetros `pagina` y `tamanio` (ver [sección 3.4.3](#343-query-parameters-filtrado-ordenamiento-y-paginacion)) y devuelve los campos de paginación en `meta` (ver [sección 4.1](#41-estructura-apiresponsewrapper)).

El tamaño máximo de página es **200 registros**. Si el cliente solicita un `tamanio` mayor, el servicio lo limita a 200 sin error.

### 8.2 Caché

Los datos que se consultan con alta frecuencia y cambian poco (catálogos, tablas de referencia) deben implementar caché en memoria mediante **Redis**, configurado con TTL apropiado al período de actualización del dato.

```java
@Cacheable(value = "regimenes", key = "#tipo")
public List<Regimen> obtenerRegimenes(String tipo) {
    return regimenRepository.findByTipo(tipo);
}
```

### 8.3 Timeout

Todo cliente HTTP que llame a servicios externos debe configurar timeouts explícitos. Queda prohibido dejar los valores por defecto del cliente (infinitos o excesivos).

> **Valores normativos — documento dueño `LIN-DIS-001 §6.1`.** Los umbrales se definen mediante una matriz por **criticidad y demanda** del servicio consumido (ruta crítica interactiva / consulta de negocio / proceso diferido o batch), no con un par de valores único: RENIEC en ventanilla virtual exige `fail-fast` más agresivo que una conciliación SUNAT por lotes. Este lineamiento **no publica valores propios** para evitar divergencia entre documentos. Ver también `LIN-ARQ-001 §4.3` para integraciones con entidades del Estado.

Lo que este lineamiento sí norma es la **respuesta del contrato REST ante el vencimiento del timeout**: si el servicio externo no responde en el plazo configurado, retornar `504` / `codDetRespuesta: 402` (ver [sección 5.1](#51-mapeo-de-codigo-http-a-coddetrespuesta)).

### 8.4 Rate limiting

**Modelo objetivo (WSO2 operativo):** el API Gateway aplica rate limiting por cliente (IP o token) según el plan de suscripción, y los servicios no lo implementan internamente.

**Estado actual (WSO2 en PoC):** dado que no existe gateway centralizado en producción, aplica la regla del documento dueño `LIN-SEC-APP-001 §7.1` — *«Rate limiting básico: configurado en la aplicación si no hay gateway»*. Todo servicio expuesto a consumidores externos (ciudadano o entidad pública) debe implementar un límite básico por cliente hasta que WSO2 gradúe.

Al superarse el límite, el servicio responde `429 Too Many Requests` con `codDetRespuesta: 302` (ver [sección 5.1](#51-mapeo-de-codigo-http-a-coddetrespuesta)).

> La versión anterior de esta sección asignaba el control exclusivamente al gateway. Como el gateway no está operativo, el efecto real era que **ninguna API de la ONP tenía rate limiting** (`GOB-CHK-001` H24).

---

## 9. Observabilidad

Los servicios REST de la ONP deben estar instrumentados con los **cuatro pilares de observabilidad** definidos en **LIN-ARQ-001 sección 5.3** (Four Golden Signals): trazas, logs estructurados, métricas y health checks. **No hay excepciones:** un servicio sin observabilidad no está listo para producción. Este lineamiento define los requisitos de cada pilar desde la perspectiva REST; la implementación técnica completa se encuentra en **LIN-OBS-001**.

### 9.1 Correlación de peticiones — X-Request-ID

Cada petición debe generar o propagar un ID de correlación:

- **Entrada:** el cliente puede enviar `X-Request-ID` en el header. Si no lo envía, el servicio genera un UUID propio.
- **Propagación:** el `X-Request-ID` se propaga en el MDC de todos los logs de esa petición y se devuelve en el header de la respuesta.
- **Respuesta:** el `requestId` aparece en el campo `meta.requestId` de todo `ApiResponseWrapper`.

> **Implementación:** `RequestIdFilter` — ver **LIN-OBS-001 sección 4.10**.

### 9.2 Trazas distribuidas

Todo servicio REST debe emitir trazas al OTEL Collector del entorno correspondiente. Las trazas permiten al equipo de operaciones seguir el recorrido de una petición a través de múltiples servicios y detectar cuellos de botella.

> **Implementación completa:** ver **LIN-OBS-001 sección 5** (dependencias, configuración por entorno, `@NewSpan`, `@ContinueSpan`, `@Scheduled`, verificación en Jaeger).

### 9.3 Logging estructurado

Los logs se emiten en formato JSON (ECS — Elastic Common Schema) y se exportan al OTEL Collector para su visualización en Kibana. Cada línea de log incluye automáticamente `trace.id`, `span.id`, `http.request.id` y `user.id` para correlación cruzada.

> **Implementación completa:** ver **LIN-OBS-001 sección 6** (`logback-spring.xml`, campos ECS, política No PII, anti-patrones, verificación en Kibana).

### 9.4 Log canónico de request

Cada petición HTTP emite al finalizar una única línea de log estructurado con: método, ruta, status HTTP, duración total y usuario autenticado. Este log permite consultas operacionales en Kibana como:

```
¿Qué endpoints están fallando?    → message:"REQUEST" AND http.response.status_code:"500"
¿Requests lentos?                 → message:"REQUEST" AND duration_ms > 1000
¿Qué hizo el usuario X?           → message:"REQUEST" AND user.id:"jperez"
```

> **Implementación:** `CanonicalRequestLogFilter` — ver **LIN-OBS-001 sección 4.9**.

### 9.5 Métricas

Todo servicio REST debe exponer métricas de JVM, HTTP y negocio a través de **Spring Boot Actuator + Micrometer**. Prometheus las recolecta periódicamente; Grafana las visualiza.

**Métricas obligatorias que todo servicio debe exponer:**

| Categoría | Métricas | Descripción |
|---|---|---|
| **JVM** | `jvm.memory.used`, `jvm.gc.pause`, `jvm.threads.live` | Estado del runtime Java |
| **HTTP** | `http.server.requests` (count, sum, max por ruta y status) | Latencia y tasa de errores por endpoint |
| **Negocio** | Al menos un contador por operación principal del dominio | Ej: `onp.afiliaciones.registradas.total` |

**Configuración mínima en `application.yml`:**

```yaml
management:
  # Exponer endpoint de métricas para Prometheus
  endpoints:
    web:
      exposure:
        include: "health,metrics,prometheus"
  endpoint:
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    # Etiquetas comunes en todas las métricas del servicio
    tags:
      application: "${spring.application.name}"
      environment: "${deployment.environment}"
```

**Dependencia Maven para el registro Prometheus:**

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

El endpoint `/actuator/prometheus` queda expuesto en el puerto de gestión y es scrapeado por Prometheus según la configuración del OTEL Collector. **Este endpoint no debe quedar expuesto en el puerto público de la API** — configurar `management.server.port` distinto al puerto de la aplicación, o restringir acceso a nivel de red.

> **Implementación completa** (Micrometer Tracing, `MeterRegistry`, convención de nombres `onp.<s>.<m>.<metrica>`, dashboards Grafana mínimos): ver **LIN-OBS-001 secciones 5 y 8**.

### 9.6 Health checks

Todo servicio REST debe implementar endpoints de **liveness** y **readiness** para integración con Kubernetes. Estos endpoints son consumidos por las probes del manifiesto de despliegue.

| Endpoint | Propósito | Qué verifica |
|---|---|---|
| `/actuator/health/liveness` | El proceso está vivo | JVM corriendo, sin estado de deadlock |
| `/actuator/health/readiness` | El servicio puede recibir tráfico | Conexión a BD, Redis y servicios críticos operativos |

**Configuración en `application.yml`:**

```yaml
management:
  endpoint:
    health:
      probes:
        enabled: true
      show-details: "when-authorized"
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
```

**Configuración en el manifiesto K8s (`Deployment`):**

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080          # debe coincidir con el puerto donde responde Actuator
  initialDelaySeconds: 30
  periodSeconds: 10
  failureThreshold: 3

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 20
  periodSeconds: 5
  failureThreshold: 3
```

> **El puerto de la probe debe ser aquel en el que responde Actuator.** La [sección 9.5](#95-metricas) ofrece dos formas de evitar que `/actuator/prometheus` quede publicado en el puerto de la API: separar `management.server.port`, o mantenerlo en el mismo puerto y restringir el acceso a nivel de red. **Los ejemplos de este corpus asumen la segunda opción** (Actuator en `8080`, protegido por red), y por eso las probes apuntan a `8080`.
>
> Si un proyecto opta por separar el puerto de gestión, **debe actualizar también las probes**: dejarlas en `8080` mientras Actuator escucha en otro puerto hace que Kubernetes reciba `404`, marque el pod como no listo y el despliegue nunca converja. El valor de `application.yml` y el del `Deployment` se declaran juntos, en el mismo cambio (`LIN-K8S-001`).

Un servicio sin probes declaradas en el Deployment **no puede ser aprobado** para producción.

### 9.7 Checklist mínimo de observabilidad antes de producción

Equivalente del checklist de **LIN-ARQ-001 sección 5.3**, aplicado a servicios REST:

- [ ] `spring-boot-starter-actuator` incluido en `pom.xml`
- [ ] `micrometer-registry-prometheus` incluido en `pom.xml`
- [ ] Endpoints `health`, `metrics` y `prometheus` expuestos en `management.endpoints.web.exposure.include`
- [ ] `management.server.port` distinto del puerto de la aplicación (o restricción de red equivalente)
- [ ] `logback-spring.xml` con `LogstashEncoder` y `OpenTelemetryAppender`
- [ ] Perfil Spring activo configurado (`SPRING_PROFILES_ACTIVE`) y configuración OTEL resuelta según `LIN-OBS-001`; si existen overrides por variables OTEL, son administrados por Plataforma según `LIN-K8S-001`
- [ ] Liveness y readiness probe declarados en el Deployment
- [ ] Al menos un dashboard de Grafana con `http.server.requests` del servicio
- [ ] Traza de prueba visible en Jaeger antes del go-live
- [ ] `RequestIdFilter` activo — verificar que `X-Request-ID` aparece en `meta.requestId` de la respuesta

---

## 10. Gobernanza

### 10.1 Catálogo de servicios

La OTI.ID administra un catálogo centralizado de todos los servicios web REST desplegados en producción. Antes de desarrollar un nuevo servicio, el equipo debe **consultar el catálogo** para determinar si ya existe un servicio equivalente que pueda reutilizarse.

**Responsabilidades:**

| Actor | Responsabilidad |
|---|---|
| OTI.DE (Desarrollo) | Remitir la información técnica del servicio a OTI.ID cuando sea desplegado en PROD |
| OTI.ID (Arquitectura) | Mantener el catálogo actualizado y accesible a todos los equipos |
| Cualquier servidor OTI | Informar a OTI.ID si detecta un servicio no registrado en el catálogo |

### 10.2 Ciclo de vida de una API

Los estados de ciclo de vida se gestionan en WSO2 API Manager (Publisher). Solo OTI Arquitectura puede avanzar o retroceder un estado.

> **Vía transitoria mientras WSO2 esté en PoC.** Los estados `CREATED`/`PUBLISHED` son estados **de la plataforma**, no del servicio. Como WSO2 no está operativo en producción (ver [sección 2.5](#25-gestion-de-apis-api-gateway-y-api-manager) y ADR-WSO2-001), exigirlos como condición literal dejaría a toda API nueva sin camino legítimo a producción. Mientras dure el PoC:
>
> - los **requisitos** del gate de la [sección 10.3](#103-gate-de-publicacion-en-wso2) se cumplen y se evidencian igual, y OTI Arquitectura los aprueba formalmente;
> - la evidencia se conserva en el repositorio del servicio y en el catálogo de la [sección 10.1](#101-catalogo-de-servicios), en lugar de en WSO2 Publisher;
> - los ítems que solo puede satisfacer la plataforma —URL base en Publisher, plan de suscripción, publicación en Dev Portal— quedan **diferidos y registrados como pendientes de migración**, no omitidos;
> - al graduar el PoC, las APIs ya en producción se registran en WSO2 conservando el estado equivalente.
>
> Ningún servicio se exime del gate por esta vía: se exime únicamente del registro en una herramienta que aún no existe.

| Fase | Estado WSO2 | Descripción | Quién activa |
|---|---|---|---|
| **Diseño** | — | Definición del contrato OpenAPI y revisión por Arquitectura. La API no está en WSO2 aún | Equipo de desarrollo |
| **Desarrollo** | — | Implementación siguiendo este lineamiento, LIN-DEV-JAVA-001 y LIN-OBS-001 | Equipo de desarrollo |
| **QA** | `CREATED` | API registrada en WSO2 Publisher, visible solo para administradores. Gate [sección 10.3](#103-gate-de-publicacion-en-wso2) completado para QA | OTI Arquitectura |
| **Producción** | `PUBLISHED` | Visible en Dev Portal, consumidores pueden suscribirse. Gate [sección 10.3](#103-gate-de-publicacion-en-wso2) completado para PROD | OTI Arquitectura |
| **Deprecación** | `DEPRECATED` | Sigue funcionando; no acepta nuevas suscripciones. Header `Deprecation: true` en todas las respuestas. Período mínimo: 6 meses | OTI Arquitectura |
| **Retiro** | `RETIRED` | Eliminada del gateway; consumidores bloqueados. Solo tras período mínimo en `DEPRECATED` | OTI Arquitectura |

> **Regla de retiro:** antes de pasar a `RETIRED`, OTI Arquitectura debe verificar que todos los consumidores activos confirmaron la migración o fueron notificados con al menos 30 días de anticipación.

### 10.3 Gate de publicación en WSO2

Antes de que OTI Arquitectura active el estado `PUBLISHED` en WSO2, el equipo de desarrollo debe completar la parte técnica y Arquitectura debe validar la parte de gobernanza. Ninguna API puede pasar a `PUBLISHED` sin ambas partes aprobadas.

#### Parte A — Requisitos técnicos (equipo de desarrollo)

- [ ] Especificación OpenAPI 3.0 generada, válida y sin errores (verificable en Swagger Editor o Stoplight)
- [ ] **Prueba de contrato ejecutada y en verde**, conforme a `LIN-TEST-001 §6` — documento dueño. El mínimo aceptable es validar la respuesta contra un JSON Schema derivado del OpenAPI; para APIs publicadas en WSO2 o con consumidores externos, `LIN-TEST-001 §6.2` exige validar request y response contra el `openapi.yml` completo con un OpenAPI validator
- [ ] Todos los endpoints documentados con `@Operation`, `@ApiResponse` y `@Schema` (ver LIN-DEV-JAVA-001 sección 13.4.6–13.4.7)
- [ ] Backend desplegado y smoke-tested en el ambiente de destino (QA o PROD)
- [ ] Endpoint `/actuator/health` responde `{"status":"UP"}` en el ambiente de destino
- [ ] Instrumentación de observabilidad completa: trazas visibles en Jaeger, logs en Kibana (ver LIN-OBS-001 sección 11)
- [ ] Esquema de autenticación SAA declarado en la especificación OpenAPI (`securitySchemes: bearerAuth`) — cuando WSO2/OAuth2 esté operacional, actualizar a `oauth2`
- [ ] Swagger deshabilitado en PROD (`SWAGGER_ENABLED=false`) si el ambiente es producción (ver [sección 6.2](#62-configuracion))
- [ ] URL base interna del backend configurada en WSO2 Publisher apuntando al servicio K8s interno
- [ ] **`NetworkPolicy` declarada** en los manifiestos del servicio (`LIN-K8S-001 §9.1`, Anexo E). Es la condición que sostiene la excepción de tráfico intra-cluster sobre HTTP de `ADR-TLS-INTERNO-001`: sin ella, el servicio debe servir HTTPS extremo a extremo

#### Parte B — Requisitos de gobernanza (OTI Arquitectura verifica)

- [ ] **Tipo de consumidor declarado:**

  | Tipo | Descripción | Implicación |
  |---|---|---|
  | Interno | Otros servicios ONP dentro del cluster K8s | Solo acceso por red interna, sin Dev Portal |
  | Externo — entidad pública | Entidades del Estado (RENIEC, SUNAT, MEF, SBS…) | Acuerdo de intercambio, IP whitelist |
  | Externo — ciudadano | Portal web o app de ciudadanos ONP | Requiere autenticación reforzada, rate limit estricto |

- [ ] **Responsable funcional** nombrado (nombre, área, correo) — quien aprueba cambios de negocio en la API
- [ ] **Responsable técnico** nombrado (nombre, área, correo) — quien atiende incidentes técnicos
- [ ] **Política de rate limit** definida — seleccionar uno:
  - `ILIMITADO` (solo para APIs internas de alto volumen con justificación)
  - `100 req/min por suscriptor`
  - `1 000 req/día por suscriptor`
  - Política personalizada (requiere ADR)
- [ ] **Clasificación de datos** declarada:
  - ¿La API expone datos personales (DNI, nombre, domicilio)? → activa política CORS estricta y auditoría de acceso
  - ¿La API expone datos previsionales (montos, periodos, beneficios)? → requiere autenticación obligatoria, sin caché en gateway
- [ ] API registrada en el catálogo institucional ([sección 10.1](#101-catalogo-de-servicios)) antes de pasar a `PUBLISHED`
- [ ] Confirmado que no duplica un servicio ya existente en el catálogo ([sección 10.1](#101-catalogo-de-servicios))

#### Entregables mínimos al solicitar publicación

El equipo de desarrollo entrega a OTI Arquitectura:

```
1. Archivo openapi.yml del servicio (generado desde el código)
2. URL del servicio desplegado en el ambiente de destino
3. Formulario de gobernanza completado (Partes A y B)
```

> **Referencia:** los estados del ciclo de vida resultantes de este gate están documentados en **[sección 10.2](#102-ciclo-de-vida-de-una-api)**.

### 10.4 Consumo de servicios SOAP legacy

Algunos sistemas externos con los que la ONP se integra (RENIEC, SUNAT, sistemas previsionales heredados) exponen servicios SOAP. Los servicios REST nuevos que necesiten consumir estas APIs SOAP deben hacerlo desde la **capa de infraestructura** del servicio (paquete `client/`), encapsulando el acceso SOAP detrás de una interfaz de puerto limpia.

**Stack recomendado para consumo SOAP desde Spring Boot:**

```xml
<dependency>
    <groupId>org.springframework.ws</groupId>
    <artifactId>spring-ws-core</artifactId>
</dependency>
```

El cliente SOAP se implementa como `WebServiceGatewaySupport` y nunca se expone directamente al resto de la aplicación — siempre a través de una interfaz de dominio.

---

## Apéndice A — Referencia rápida

### A.1 Estructura de URL estándar ONP

```
https://<host>/api/v{N}/{recurso-plural}/{id}/{sub-recurso}?param=valor
```

### A.2 Métodos HTTP y status codes por operación

| Operación | Método | Status éxito | Status error frecuente |
|---|---|---|---|
| Listar recursos | GET | 200 | 400, 500 |
| Obtener por ID | GET | 200 | 404, 500 |
| Crear recurso | POST | **201** | 400, 409, 500 |
| Reemplazar recurso | PUT | 200 | 400, 404, 500 |
| Actualizar parcial | PATCH | 200 | 400, 404, 500 |
| Eliminar recurso | DELETE | 204 | 404, 500 |

### A.3 Documentos relacionados

| Documento | Propósito |
|---|---|
| LIN-OBS-001 sección 5 | Implementación de trazas OTEL en Spring Boot (`@NewSpan`, Jaeger) |
| LIN-OBS-001 sección 6 | Implementación de logging estructurado (`logback-spring.xml`, ECS, No PII) |
| LIN-OBS-001 secciones 4.9–4.10 | `CanonicalRequestLogFilter`, `RequestIdFilter` |
| LIN-DEV-JAVA-001 sección 13.4 | Implementación completa de OpenAPI/Swagger y `ApiResponseWrapper<T>` |
| LIN-DEV-JAVA-001 sección 14 | Estructura de proyecto Maven y convenciones de nombrado |
| LIN-ARQ-001 sección 2 | Estilos arquitectónicos macro (monolito, monolito modular, microservicios) |
| LIN-DIS-001 sección 2.3 | Arquitectura Hexagonal (Ports & Adapters) |

---

## Proceso de excepción a este estándar (`EXC-API-NNN`)

> **Instrumento correcto: `EXC-API-NNN`, no un ADR.** Conforme a `GOB-MAT-001` (Registro de decisiones y excepciones), la desviación de un lineamiento **en un proyecto concreto** se registra como excepción con vigencia acotada y **fecha de revisión**, nunca indefinida. El `ADR-NNN` queda reservado a decisiones **institucionales** del Comité de Arquitectura, que obligan a todo el corpus; llevar allí cada desviación de cada sistema vaciaría de valor ese registro. La excepción se aprueba por Arquitectura OTI y se registra en el documento de arquitectura del sistema (`GOB-PLA-001`, Anexo E, criterio 14).


Toda desviación de las reglas establecidas en este documento requiere un ADR (Architecture Decision Record) aprobado formalmente por el equipo de Arquitectura de la OTI antes de implementarse.

El ADR debe incluir: contexto, decisión, alternativas evaluadas, consecuencias, vigencia de la excepción, responsable y fecha de revisión.

**No se acepta la urgencia como justificación para omitir este proceso.**

---

*LIN-API-REST-001 — Estándar de Servicios Web y APIs REST ONP*  
*OTI — Oficina de Tecnologías de la Información*
