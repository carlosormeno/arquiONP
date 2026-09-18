# DOCUMENTO DE DISEÑO

**Oficina:** Oficina de Tecnologías de la Información (OTI)
**Documento / Entregable:** Documento de Diseño
**Requerimiento:** [código del requerimiento, ej. INI-XXXX]
**Proyecto / Aplicación:** [Nombre del Sistema]
**Fecha:** [XX/XX/AAAA]

> 📋 **Qué es este documento, y cómo se relaciona con los otros dos**
> El corpus de arquitectura tiene tres documentos que se leen en cascada, cada uno más detallado que el anterior:
>
> | Documento | Responde | Plantilla |
> |---|---|---|
> | **Documento de Arquitectura de TI** | ¿Qué sistemas, qué estilo macro, qué integraciones a alto nivel? | `Plantilla_Arquitectura_TI_ONP_v1.3.md` |
> | **Documento de Diseño** (este) | ¿Cómo se construye internamente? Base de datos, interfaces, objetos de software, patrones tácticos por componente | Este archivo |
> | **`LIN-DIS-001`** (norma) | Qué reglas debe cumplir el diseño de cada componente (no es una plantilla, es el estándar que este documento verifica) | — |
>
> Este documento **no repite** el contenido del Documento de Arquitectura — lo referencia y profundiza. Si tu proyecto ya tiene un Documento de Arquitectura de TI aprobado, complétalo primero; varias secciones de aquí (`§3.1`, `§3.2`) son un resumen orientado a diseño, no una repetición completa.
>
> *Una vez completada esta sección, elimina este bloque de orientación.*

---

## TABLA DE CONTENIDO

1. [ALCANCE](#1-alcance)
2. [RESTRICCIONES DE DISEÑO](#2-restricciones-de-diseño)
3. [ARQUITECTURA DEL SISTEMA](#3-arquitectura-del-sistema)
   3.1 Diagrama de Arquitectura de TI · 3.2 Componentes Principales · 3.3 Tecnologías Utilizadas · 3.4 Patrones de Diseño y Diseño Táctico
4. [DISEÑO DE BASE DE DATOS](#4-diseño-de-base-de-datos)
   4.1 Diagrama Entidad-Relación · 4.2 Diccionario de Datos
5. [DISEÑO DE INTERFACES](#5-diseño-de-interfaces)
   5.1 APIs / Servicios Internos · 5.2 APIs / Servicios Externos
6. [LISTADO DE OBJETOS](#6-listado-de-objetos)
   6.1 Objetos Frontend · 6.2 Objetos Backend · 6.3 Objetos Base de Datos
7. [ANEXOS](#7-anexos)

---

## HISTORIAL DE CAMBIOS

| Ítem | Versión | Fecha | Descripción |
|---|---|---|---|
| 1 | 0.1 | [DD/MM/AAAA] | Versión inicial de la plantilla |

---

## 1. ALCANCE

> 📋 **Orientación para el diseñador**
> Describe qué construye este documento de diseño, en qué requerimiento/iniciativa se enmarca, y qué procesos de negocio cubre — en una frase por proceso, no la especificación funcional completa (esa vive en el documento de requisitos). Declara explícitamente qué queda **excluido**: es tan importante como lo incluido, porque delimita responsabilidad.
>
> **Pregunta de validación:** ¿un desarrollador que se une al proyecto hoy podría, leyendo solo esta sección, saber qué construye y qué no construye este diseño?
>
> *Una vez completada esta sección, elimina este bloque de orientación.*

Este documento describe el diseño técnico para **[nombre del módulo/proceso]** dentro de **[nombre del sistema]**.

El [módulo/proceso] soporta [N] procesos:
- **[P1 — nombre]:** [una frase]
- **[P2 — nombre]:** [una frase]

El alcance técnico cubre:
- [ej. Diseño de la base de datos relacional (modelo lógico y físico)]
- [ej. Arquitectura de componentes para procesamiento concurrente y asíncrono]
- [ej. Definición de contratos de APIs internas y externas]
- [ej. Identificación de todos los objetos de software del sistema]

Excluye:
- [ej. Infraestructura de red]
- [ej. Gestión de identidades corporativas y sistemas de terceros]
- [lo que quede fuera de este entregable específico]

---

## 2. RESTRICCIONES DE DISEÑO

> 📋 **Orientación para el diseñador**
> Lista las condicionantes técnicas, de seguridad, de auditoría o de negocio que **no son decisión de este proyecto** — vienen dadas por el corpus normativo, por un sistema existente, o por una exigencia regulatoria, y todo el diseño posterior debe respetarlas. No confundas restricción con decisión de diseño: "usar Oracle 19c" es restricción si el corpus ya lo exige (`LIN-ARQ-001 §6.1`); "usar Redis para el read model" es una decisión que se justifica en `§3.4`, no una restricción.
>
> **Categorías típicas en ONP:** motor de base de datos, cifrado en tránsito, autenticación obligatoria, inmutabilidad de logs de auditoría, control de concurrencia, gestión documental, alcance de diseño responsivo.
>
> *Una vez completada esta sección, elimina este bloque de orientación.*

| # | Restricción | Tipo | Descripción |
|---|---|---|---|
| RD-01 | [nombre] | Tecnológica / Seguridad / Auditoría / Diseño-Alcance | [descripción y de dónde viene — cita el lineamiento si aplica] |

---

## 3. ARQUITECTURA DEL SISTEMA

### 3.1 Diagrama de Arquitectura de TI

> 📋 **Orientación**
> Si el sistema ya tiene un Documento de Arquitectura de TI aprobado, **no dupliques el diagrama** — referéncialo: *"El detalle de la arquitectura se encuentra en el Documento de Arquitectura de TI v[X.Y]"*. Si este Documento de Diseño es el primer artefacto formal del sistema, incluye aquí el mismo diagrama de capas que usa `Plantilla_Arquitectura_TI_ONP_v1.3.md §3`.

[Insertar Diagrama de Arquitectura de TI, o referenciar el Documento de Arquitectura de TI vigente.]

### 3.2 Componentes Principales

> 📋 **Orientación**
> Resume las 5 capas del sistema (Usuarios, Seguridad, Aplicaciones, Datos, Servicios Transversales) al nivel que un diseñador necesita para ubicar dónde encaja cada objeto que va a construir — no repitas el detalle completo del Documento de Arquitectura si ya existe, solo lo que este documento necesita para dar contexto a `§3.4` en adelante.

#### 3.2.1 Capa de Usuarios
[Tipos de usuario y canal de acceso.]

#### 3.2.2 Capa de Seguridad
- **Seguridad interna:** [componentes y función — SAA/token institucional, mTLS interno]
- **Seguridad externa:** [WAF, protección anti-bots, autenticación de terceros]

#### 3.2.3 Capa de Aplicaciones
- **Frontend:** [tipo de aplicación, framework]
- **Backend:** [Monolito Modular / Microservicios — lista de componentes, ver `§6.2`]
- **Integración:** [API Gateway / BFF / Facade que gestiona la entrada]

#### 3.2.4 Capa de Datos
[Motor(es) de base de datos y qué tipo de datos gestiona cada uno.]

#### 3.2.5 Servicios Transversales

> 📋 **Orientación**
> La conexión a observabilidad **no es opcional ni una decisión de este proyecto**: `LIN-ARQ-001 §5.3` (`ARQ-R-005`) exige que ningún sistema pase a producción sin exponer las Cuatro Señales Doradas (Latencia, Tráfico, Errores, Saturación), y `LIN-OBS-001` norma el mecanismo. Esta subsección **declara cómo** este sistema cumple ese mandato — no lo redefine.

- **Observabilidad (obligatorio, `LIN-ARQ-001 §5.3` / `LIN-OBS-001`):**
  - Estándar de instrumentación: OpenTelemetry (trazas + métricas), exportación OTLP hacia el OTel Collector institucional.
  - Métricas mínimas expuestas: latencia p95/p99, throughput (RPS), tasa de error (4xx/5xx), salud del proceso (heap/GC/threads en JVM).
  - Trazas: propagación distribuida end-to-end (HTTP y dependencias DB/HTTP client), header W3C `traceparent`.
  - Endpoints estándar: `/actuator/health` (liveness/readiness) y `/actuator/metrics` o equivalente vía Collector.
  - Logs estructurados JSON con `trace_id`/`span_id` correlacionados, sin PII en el payload (enmascarado).
- **Notificaciones:** [SMS, SMTP — servicio y proveedor]
- **Gestor documental:** [si aplica]
- **Otros servicios transversales compartidos que el sistema consume:** [listar]

### 3.3 Tecnologías Utilizadas

> 📋 **Orientación**
> Tabla de stack técnico real, con versión exacta — no rangos ("3.x") salvo que el propio lineamiento lo permita. Sirve para que Plataforma valide compatibilidad y para que un desarrollador nuevo sepa qué instalar. Separa Frontend y Backend.

**Frontend**

| Categoría | Tecnología | Versión | Uso en el sistema |
|---|---|---|---|
| Framework base | [ej. Angular] | [ej. 17+] | |

**Backend**

| Categoría | Tecnología | Versión | Uso en el sistema |
|---|---|---|---|
| Lenguaje | [ej. Java] | [ej. 21 LTS] | |
| Framework base | [ej. Spring Boot] | [ej. 3.x] | |

### 3.4 Patrones de Diseño y Diseño Táctico

> 📋 **Orientación**
> Esta es la sección que verifica `LIN-DIS-001`. Tiene dos niveles, no uno: **`3.4.1` es el inventario** — qué patrones están en uso, en una tabla simple, a modo de vistazo rápido. **`3.4.2` en adelante es el gobierno** — no basta con nombrar "Hexagonal" o "CQRS", hay que declarar cómo se aplicó cada uno y con qué evidencia, que es lo que un revisor de Arquitectura necesita para aprobar el diseño. No optimices por brevedad aquí — es la sección que más se audita.

#### A. Patrones de Diseño (inventario)

##### 3.4.1 Inventario de patrones adoptados

| Patrón | Descripción técnica | Beneficio en el proyecto |
|---|---|---|
| [ej. API Gateway] | [qué hace] | [por qué se necesita aquí] |
| [ej. Arquitectura Hexagonal] | Domain / Application / Infrastructure — Puertos y Adaptadores (`LIN-DIS-001 §2.3`) | Desacopla la lógica de negocio de dependencias externas |

#### B. Diseño Táctico (`LIN-DIS-001`)

##### 3.4.2 Clasificación táctica por componente (`LIN-DIS-001 §2-6`)

> 📋 **Orientación**
> Una fila por cada componente/microservicio listado en `§6.2`. Esta tabla es la que un revisor de Arquitectura usa para verificar `DIS-R-001` a `DIS-R-009` — cada columna corresponde a una decisión que el lineamiento exige declarar.

| Componente (`§6.2`) | Estilo (`§2.2`/`§2.3`) | ¿DDD? (`§3.0`) | Estrategia de lógica (`§4.1`) | ¿CQRS? (`§4.2`) | Categoría resiliencia (`§6.1`) | ¿ACL? (`§5.4`) |
|---|---|---|---|---|---|---|
| [nombre del componente] | Hexagonal / Capas | Sí / No | Transaction Script / Active Record / Table Module / Domain Model | No / Outbox+Kafka / CDC | Alta / Media / Baja demanda | Sí ([contra qué]) / No |

##### 3.4.3 Detalle DDD — solo componentes con DDD = Sí

> 📋 **Orientación**
> Repite este bloque por cada componente marcado DDD = Sí en `3.4.2`. Si ningún componente aplica DDD, elimina esta subsección completa — no la dejes con la tabla vacía como si faltara llenar.

**Componente: [nombre]**

| # | Criterio (`LIN-DIS-001 §3.0`) | ¿Cumple? | Evidencia |
|---|---|---|---|
| 1 | Sistema core de la ONP | Sí/No | |
| 2 | Reglas de negocio complejas y cambiantes | Sí/No | |
| 3 | Experto de dominio disponible | Sí/No | |
| 4 | Equipo con experiencia previa en DDD | Sí/No | |
| 5 | Vida útil larga (+5 años) | Sí/No | |
| 6 | Bounded Context delimitado | Sí/No | ver `3.4.4` |

**Building Blocks (`§3.2`):**

| Aggregate Root | Entidades internas | Value Objects | Domain Services |
|---|---|---|---|
| | | | |

##### 3.4.4 Context Map — relaciones entre componentes (`§3.1`)

> 📋 **Orientación**
> Solo relaciones **entre componentes de este mismo sistema**. Las dependencias hacia el Estado u otras entidades externas van en `§5.2`, no aquí.

| Componente origen | Componente relacionado | Tipo de relación | Regla táctica aplicada |
|---|---|---|---|
| | | Cliente-Proveedor / ACL / Conformista / Caminos Separados | |

##### 3.4.5 Resiliencia táctica — dependencias externas por red (`§6`)

> 📋 **Orientación**
> Una fila por cada dependencia externa por red de cualquier componente. Si `§7` (Anexos) ya describe una estrategia de reintentos a nivel funcional (ej. un *Scheduler* que reprocesa registros en estado `ERROR`), el máximo de intentos y el tiempo de espera que declares ahí y los valores técnicos de esta tabla **deben ser el mismo número** — no dos definiciones independientes de lo mismo.

| Componente | Dependencia externa | Categoría (`§6.1`) | Connection / Read Timeout | Circuit Breaker | Reintentos (`§6.4`) |
|---|---|---|---|---|---|
| | | Alta / Media / Baja demanda | | Sí/No | |

##### 3.4.6 Conformidad con LIN-DIS-001

| Regla | Tema | Componentes que aplican | Cumple |
|---|---|---|---|
| `DIS-R-001` (`§2.3`) | Arquitectura Hexagonal obligatoria | | Sí/No |
| `DIS-R-002` (`§3`) | Límites de componente = Bounded Context | | Sí/No |
| `DIS-R-003` (`§3.4`) | Gobierno del Shared Kernel | | Sí/No |
| `DIS-R-004` (`§4.2`) | CQRS | | Sí/No |
| `DIS-R-005` (`§5.1`) | BFF | | Sí/No |
| `DIS-R-006` (`§5.4`) | Anti-Corruption Layer | | Sí/No |
| `DIS-R-007`–`009` (`§6`) | Resiliencia táctica | Los que tengan dependencia externa por red | Sí/No |

---

## 4. DISEÑO DE BASE DE DATOS

### 4.1 Diagrama Entidad-Relación

> 📋 **Orientación**
> Modelo lógico y/o físico según el nivel de madurez del diseño. Notación estándar (crow's foot o UML). Si el motor es Oracle (estándar institucional, `LIN-ARQ-001 §6.1`), aclara explícitamente cualquier desviación con su ADR.

[Insertar diagrama entidad-relación.]

### 4.2 Diccionario de Datos

> 📋 **Orientación**
> Una tabla por entidad física, con sus columnas, tipo de dato, nulabilidad y descripción de negocio — no solo el nombre técnico. Incluye las tablas de auditoría/logs si el sistema las requiere (ver restricciones de `§2`).

| Tabla | Columna | Tipo | Nulo | Descripción |
|---|---|---|---|---|
| | | | | |

---

## 5. DISEÑO DE INTERFACES

> 📋 **Orientación general de la sección**
> Dos mandatos institucionales rigen todo lo que se declara aquí — este documento no los redefine, solo declara cómo se cumplen:
> - **Contrato OpenAPI/Swagger obligatorio:** toda API REST debe estar formalmente especificada en OpenAPI 3.0+ y publicada en el Portal institucional (`ARQ-R-004`, `LIN-ARQ-001 §4.1`). El contrato completo (el YAML/JSON de Swagger) va en `§7` Anexos; esta sección es el **inventario**, no el contrato.
> - **Todo servicio pasa por el API Manager:** prohibido exponer un endpoint REST sin publicarlo a través de WSO2 API Manager — no se accede a un backend directamente saltándose el Gateway institucional (`LIN-ARQ-001 §4.1`, `LIN-DIS-001 §5.1`/`§5.2`). El catálogo de abajo es donde se evidencia ese registro (columnas `Estado`, `Disponibilidad / Acceso`, URLs por ambiente).

### 5.1 APIs / Servicios Internos

> 📋 **Orientación**
> Catálogo de servicios que este sistema **publica** o **consume** de otros sistemas de la ONP — mismo formato que el catálogo institucional de servicios, para que se pueda copiar/pegar directamente desde o hacia él. Una fila por servicio; usa la columna `Dirección` para distinguir lo que este sistema expone de lo que consume.

| ID_Servicio | ID_APP (vínculo sugerido) | Aplicación relacionada | Nombre del Servicio | Tipo de Servicio | Dirección (Publica/Consume) | Descripción | Estado | Disponibilidad / Acceso | URL PRD | Servidor / Grupo PRD | URL QA | Servidor / Grupo QA | URL DEV | Servidor / Grupo DEV | Gestor Responsable | Consumidores / Apps Cliente | Repositorio de Fuentes | Nombre del Proyecto | Demanda | Operaciones (detalle) | Fuente | Validación de URL |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| | | | | | | | | | | | | | | | | | | | | | | |

### 5.2 APIs / Servicios Externos

> 📋 **Orientación**
> Mismo catálogo, para servicios de entidades externas al Estado (RENIEC, SUNAT, SBS, PIDE) o de terceros — `Dirección = Consume` en casi todos los casos. Además de las columnas del catálogo, indica el patrón de integración usado (Facade/ACL, `LIN-DIS-001 §5.3`/`§5.4`); debe ser coherente con lo declarado en `§3.4.2`.

| ID_Servicio | Nombre del Servicio | Entidad | Dirección | Protocolo | Patrón de integración (`§3.4`) | URL PRD | Gestor Responsable | Estado |
|---|---|---|---|---|---|---|---|---|
| | | | Consume | | Facade / ACL | | | |

---

## 6. LISTADO DE OBJETOS

### 6.1 Objetos Frontend

| Nro | Tipo | Detalle | Nombre | Estado |
|---|---|---|---|---|

### 6.2 Objetos Backend

> 📋 **Orientación**
> Este es el inventario que `§3.4.2` referencia por nombre — mantén los nombres idénticos entre ambas secciones.

| Nro | Tipo | Detalle | Nombre | Estado |
|---|---|---|---|---|

### 6.3 Objetos Base de Datos

| Paquete / Objeto | Descripción |
|---|---|

---

## 7. ANEXOS

> 📋 **Orientación**
> Los anexos son específicos de cada proyecto — no hay una lista fija. Los más comunes en diseños de la ONP:
> - Estimación de transacciones / volumetría
> - Estándares de desarrollo y codificación (si difieren de `LIN-DEV-JAVA-001`, deben justificarse con ADR)
> - Contratos de servicios API (OpenAPI completo)
> - Procesos de integración y flujos funcionales detallados, paso a paso, por cada proceso de negocio complejo
> - Reglas de validación por archivo/interfaz de carga masiva
>
> **Regla de coherencia:** cualquier valor técnico que un anexo declare (timeouts, reintentos, umbrales) debe coincidir con lo declarado en `§3.4.5` — un anexo no es el lugar para definir un umbral de resiliencia que `§3.4` no registra.

[Listar y desarrollar los anexos aplicables al proyecto.]

---

*Documento de Diseño — ONP · profundiza el Documento de Arquitectura de TI (`Plantilla_Arquitectura_TI_ONP_v1.3.md`) y verifica `LIN-DIS-001`*
