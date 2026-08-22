# DOCUMENTO DE ARQUITECTURA DE TI
**OFICINA DE TECNOLOGÍAS DE LA INFORMACIÓN**

> **Identidad de esta plantilla — no copiar al documento derivado.**
> **Código:** GOB-PLA-001 · **Versión:** v2.8 · **Estado:** Vigente · **Propietario:** Arquitectura OTI
>
> Estos datos identifican a la **plantilla institucional**. El documento de arquitectura que se produzca a partir de ella lleva su propia identidad en la tabla de abajo, y su versión evoluciona de forma independiente de la versión de la plantilla. Al completar el documento, elimina este bloque.

---

| Campo | Valor |
|---|---|
| **Proyecto / Aplicación** | [Nombre del Sistema] |
| **Versión** | [vX.Y — versión del documento de arquitectura, no de la plantilla] |
| **Fecha** | [DD/MM/AAAA] |
| **Estado** | Borrador / En revisión / Aprobado |
| **Elaborado por** | [Nombre del Arquitecto] |
| **Revisado por** | [Nombre del Revisor] |
| **Aprobado por** | [Nombre del Aprobador] |
| **Línea base normativa** | [Corpus según `GOB-MAT-001` vX.Y.Z, consultada el DD/MM/AAAA] |
| **Próxima revisión normativa** | [DD/MM/AAAA — máximo 12 meses, o antes si se activa un disparador de `§1.5`] |

---

## HISTORIAL DE CAMBIOS

| Versión | Descripción del Cambio | Autor | Fecha |
|---|---|---|---|
| v0.1 – v1.2 | Versiones iniciales y refinamientos pedagógicos de la plantilla | OTI | |
| v2.0 | Alineación normativa total con los 3 Niveles de Arquitectura ONP (`LIN-ARQ-001`, `LIN-DIS-001`, `LIN-DEV-JAVA-001`), mandatos K8s/containerd, SRE Four Golden Signals y Política de Deuda Técnica Cero / Excepciones ADR | OTI / Arquitectura | [DD/MM/AAAA] |
| v2.1 | Corrige la escala de Estadios a la oficial de `LIN-ARQ-001 §2.1` (1 = Legacy, 2 = Monolito Modular, 3 = Microservicios) en las guías de las vistas A.1, A.2 y A.3 — antes usaba una numeración 0/1/2 inexistente en el marco rector — y redirige las citas de ACL (`LIN-DIS-001 §5.4`) y Strangler Fig (`LIN-ARQ-001 §2.2`) | OTI / Arquitectura | 2026-08-05 |
| v2.2 | Corrige la cita de Kubernetes/`containerd`: decía `LIN-ARQ-001 §7` (que es *Estrategia Macro de Frontend*) — el destino real es `§5.2` (`GOB-CHK-001` H10.2) | OTI / Arquitectura | 2026-08-08 |
| v2.3 | Revisión de fondo (`GOB-CHK-001` H31). **(1)** El ejemplo de Seguridad de `C.1` describía el token SAA como **JWT**, cuando `LIN-API-REST-001 §7.1` afirma expresamente que es **opaco** y no verificable localmente — un arquitecto que lo copiara diseñaría validación local de un token que no la admite. **(2)** El mismo cuadro trataba `codDetRespuesta` como *header*, siendo un campo del cuerpo de `ApiResponseWrapper`. **(3)** La fila de **Recuperabilidad** exige RTO/RPO sin que exista lineamiento al que remitirse (`H11.2`): se explicita cómo proceder mientras la brecha siga abierta. **(4)** `D.2` citaba «Feature Toggles (**PA14**)», código del tablero de brechas y no del catálogo normativo — redirigido a `LIN-ARQ-001 §2.3` y `ADR-014`. **(5)** `B.1` no distinguía `AD-XXX` (proyecto) de `ADR-XXX` (institucional), de modo que un proyecto podía creer que se autodispensa de un lineamiento con una decisión propia | OTI / Arquitectura | 2026-08-17 |
| v2.4 | El atributo **Recuperabilidad** deja de ser una exigencia huérfana: remite a `LIN-ARQ-001 §5.4`, que fija las bandas de criticidad y sus objetivos de RTO/RPO. Se precisa qué debe declararse —criticidad, RTO/RPO con el validador del área usuaria, verificación de dependencias y procedimiento de recuperación— y que un documento de criticidad **Alta** no puede aprobarse sin ello (`GOB-CHK-001` H11.2) | OTI / Arquitectura | 2026-08-17 |
| v2.5 | Evaluación de la plantilla como instrumento normativo (`GOB-CHK-001` H34). **(1)** Se incorpora el **Anexo E — Conformidad y Criterios de Aprobación**: la plantilla enseñaba a redactar pero no definía cuándo un documento está listo para aprobarse, de modo que la aprobación dependía del criterio de quien firmara. Es el único anexo que **no se elimina** del entregable. **(2)** Nueva `§1.4` con las cuatro **declaraciones obligatorias** que el corpus exige y la plantilla no pedía —Estadio, criterios de microservicio, DDD y **CAP**— más la Declaración de Conformidad del `README.md`, cuya ausencia **bloquea el pipeline** (`LIN-CICD-001 §12.5`) sin que el arquitecto tuviera forma de enterarse. **(3)** `§5.2` listaba 6 de 19 documentos del corpus, omitiendo `LIN-K8S-001`, `LIN-TEST-001` y `LIN-VER-001`; ahora lista el corpus completo con su estado y exige declarar cuáles no aplican y por qué | OTI / Arquitectura | 2026-08-17 |
| v2.6 | Cierra los dos hallazgos de diseño de la evaluación (`GOB-CHK-001` H34.4 y H34.5). **(1) Regla de precedencia entre `§3` y el Anexo A:** eran dos representaciones del mismo sistema mantenidas por separado y sin regla de cuál prevalece. El **Anexo A pasa a ser la fuente autoritativa** —es el modelo ArchiMate versionable— y `§3` queda como lectura derivada para las audiencias que no leen ArchiMate, con prohibición de introducir elementos ausentes del modelo y obligación de actualizarse después de él. Se resuelve además el caso de la Capa de Seguridad, única sin vista propia. **(2) Vigencia frente al corpus:** el documento declaraba conformidad con lineamientos que cambian sin que nada indicara cuándo revisarlo. La tabla de identidad incorpora **línea base normativa** y **próxima revisión**; la nueva `§1.5` define cinco disparadores de revisión obligatoria —el principal, que un documento del que el sistema depende **gradúe a `Vigente`**— y qué explícitamente *no* obliga, para que la regla no degenere en revisar por cada errata | OTI / Arquitectura | 2026-08-18 |
| v2.7 | El Anexo A advierte que el modelo **será contrastado contra la arquitectura observada** (`LIN-ARQ-001 §5.5`) mediante el grafo de servicios, y que una dependencia real no modelada se resuelve corrigiendo el documento o el código, no normalizando la desviación. Nuevo criterio 13c en el Anexo E para criticidad Alta y Media (`GOB-CHK-001` H35) | OTI / Arquitectura | 2026-08-18 |
| v2.8 | La elección de ArchiMate/Archi remite a su documento dueño `DOC-R-002` (LIN-DOC-001 §7), que norma además la notación de los diagramas embebidos y la regla de versionar la fuente de todo diagrama (`GOB-CHK-001` H42) | OTI / Arquitectura | 2026-08-21 |

---

## TABLA DE CONTENIDO

1. [Alcance del Documento](#1-alcance-del-documento)
2. [Glosario Técnico](#2-glosario-técnico)
3. [Diagrama de Arquitectura de TI](#3-diagrama-de-arquitectura-de-ti)
4. [Supuestos y Restricciones](#4-supuestos-y-restricciones)
5. [Documentos Adjuntos](#5-documentos-adjuntos)
- [Anexo A: Vistas de Arquitectura (ArchiMate)](#anexo-a-vistas-de-arquitectura-archimate)
- [Anexo B: Decisiones Arquitectónicas (ADRs)](#anexo-b-decisiones-arquitectónicas-adrs)
- [Anexo C: Atributos de Calidad](#anexo-c-atributos-de-calidad)
- [Anexo D: Riesgos y Deuda Técnica](#anexo-d-riesgos-y-deuda-técnica)
- [Anexo E: Conformidad y Criterios de Aprobación](#anexo-e-conformidad-y-criterios-de-aprobación) — *no se elimina del entregable*

---

## 1. ALCANCE DEL DOCUMENTO

El presente documento describe la arquitectura de TI propuesta para **[Nombre del Sistema]**. Está dirigido a diferentes audiencias con distintos niveles de detalle, facilitando la comprensión del sistema desde una perspectiva estratégica hasta una perspectiva técnica.

> 📋 **Orientación para el arquitecto — Sección 1 completa**
>
> Esta sección responde a tres preguntas fundamentales: para qué existe el sistema, qué cubre este documento y quién debe leerlo. Es la sección que más se confunde en la práctica: muchos arquitectos escriben el objetivo del sistema cuando deberían escribir el objetivo del documento, o describen el alcance funcional cuando deberían describir el alcance arquitectónico.
>
> **Error frecuente:** copiar el objetivo del documento de análisis. El objetivo aquí no es el del sistema en sí, sino el propósito de este documento de arquitectura como entregable: qué decisiones captura, a qué nivel de detalle y para qué audiencias.
>
> Una vez completada esta sección, elimina este bloque de orientación.

### 1.1 Objetivo

> 📋 **Orientación para el arquitecto**
>
> Escribe dos párrafos cortos. El primero describe qué hace el sistema y qué valor aporta a la ONP y a sus usuarios finales, en lenguaje comprensible para cualquier audiencia. El segundo describe qué captura este documento: qué decisiones arquitectónicas contiene, a qué versión del sistema corresponde y cuál es su propósito como entregable institucional.
>
> **Lo que NO va aquí:** requisitos funcionales, casos de uso, reglas de negocio, ni descripción de procesos. Si te encuentras escribiendo "el sistema permitirá que el usuario pueda...", estás en el documento equivocado.

[Describir el objetivo principal del sistema desde la perspectiva de negocio y TI. Qué problema resuelve y qué valor aporta a la ONP y a sus usuarios. Luego, indicar el propósito de este documento como entregable arquitectónico.]

### 1.2 Alcance del sistema

> 📋 **Orientación para el arquitecto**
>
> Esta subsección es crítica porque define los límites del sistema. Lo que está dentro del alcance es lo que el documento describe y lo que el equipo debe construir. Lo que está fuera es igualmente importante: evita malentendidos sobre qué no es responsabilidad de este sistema.
>
> **Dentro del alcance** debe listar los módulos o componentes del sistema que se están documentando, las integraciones que el sistema debe implementar (aunque el otro extremo sea responsabilidad de otra entidad), y los procesos de negocio que el sistema soporta.
>
> **Fuera del alcance** debe listar explícitamente los sistemas adyacentes que no son parte de este proyecto aunque se integren con él, las funcionalidades que se postergaron para fases futuras, y los aspectos de diseño e implementación que se documentan en otros artefactos.
>
> **Pregunta de validación:** ¿Un revisor externo podría determinar con claridad si un componente o funcionalidad específica está o no está en el alcance de este documento? Si hay ambigüedad, necesitas ser más preciso.

**Dentro del alcance:**

- [Sistema o módulo principal]
- [Integraciones con sistemas internos que el sistema debe implementar]
- [Integraciones con entidades externas que el sistema debe consumir]

**Fuera del alcance:**

- [Sistemas adyacentes que se integran pero no son parte de este proyecto]
- [Funcionalidades postergadas para fases futuras]
- [Aspectos de diseño detallado e implementación — documentados en otros artefactos]

### 1.3 Audiencia objetivo

> 📋 **Orientación para el arquitecto**
>
> Esta tabla orienta a cada lector hacia las secciones que le son más relevantes. Ajústala según las audiencias reales del proyecto. Si el sistema no tiene usuarios internos, elimina esa fila. Si hay un equipo de seguridad que revisa formalmente, agrégalo. El propósito es que cada persona que reciba el documento sepa exactamente dónde encontrar lo que necesita sin tener que leerlo completo.

| Audiencia | Propósito | Secciones de interés |
|---|---|---|
| Gerencia / Stakeholders | Comprensión del valor y alcance del sistema | Sección 1, Anexo A - Vista de Contexto |
| Equipo de Plataforma / Seguridad | Comprensión de los componentes y su despliegue | Secciones 3, 4; Anexo A - Vista de Aplicación y Vista de Infraestructura |
| Desarrolladores | Comprensión de los componentes internos y decisiones técnicas | Secciones 3, 4; Anexo A - Vista de Componentes; Anexo B |
| Equipo de Soporte | Comprensión del entorno de ejecución | Sección 3; Anexo A - Vista de Infraestructura |

### 1.4 Declaraciones arquitectónicas obligatorias

> 📋 **Orientación para el arquitecto**
>
> Estas cuatro declaraciones **no son opcionales ni derivables del resto del documento**: el corpus las exige de forma expresa y un revisor las busca antes que nada. Complétalas al inicio, no al final — condicionan todo el diseño posterior.
>
> Una vez completada esta sección, elimina este bloque de orientación.

| Declaración | Valor | Fundamento |
|---|---|---|
| **Estadio de topología** | [1 — Legacy / 2 — Monolito Modular / 3 — Microservicios] | `ARQ-R-001` (LIN-ARQ-001 §2.1). El Estadio 2 es el **por defecto** para todo sistema nuevo |
| **Criterios de extracción a microservicio** | [No aplica (Estadio 1 o 2) / Cumplidos los 6, ver AD-00X] | `ARQ-R-001` (LIN-ARQ-001 §2.1) — los seis criterios se cumplen **simultáneamente** o el sistema no es candidato |
| **Adopción de DDD táctico** | [Sí, ver AD-00X / No] | `LIN-DIS-001 §3.0` — seis criterios propios, **independientes** de los de microservicio: cumplir unos no implica cumplir los otros |
| **Declaración CAP** | [CP / AP / No aplica (no distribuido)] | `LIN-ARQ-001 §3.1` — **obligatoria** para todo microservicio o módulo distribuido, con su sustento |
| **Criticidad y objetivos de recuperación** | [Alta / Media / Baja + RTO y RPO] | `LIN-ARQ-001 §5.4.1`. Se detalla en el Anexo C — Recuperabilidad |

#### Declaración de Conformidad en el `README.md` del repositorio

Además de este documento, `ARQ-R-008` (LIN-ARQ-001 §8.3) numeral 4 exige una **declaración jurada técnica firmada por el Tech Lead** en el `README.md` del repositorio. **`LIN-CICD-001 §12.5` la verifica en el pipeline y bloquea el pase si falta** — un documento de arquitectura impecable no evita ese bloqueo.

```markdown
## Declaración de Conformidad con LIN-ARQ-001

- **Tech Lead responsable:** <nombre completo>
- **Fecha:** <YYYY-MM-DD>
- **Declaro que** el presente repositorio no contiene importaciones entre fronteras
  prohibidas del Monolito Modular según LIN-DIS-001 §3.4, y que la arquitectura
  implementada es conforme con LIN-ARQ-001.
```

> El pipeline verifica que la declaración **exista y esté firmada**, no que sea cierta. La veracidad sigue siendo responsabilidad del Tech Lead.

### 1.5 Vigencia del documento frente al corpus normativo

> 📋 **Orientación para el arquitecto**
>
> Este documento declara conformidad con el corpus **en la versión y fecha registradas en la tabla de identidad**, no con el corpus perpetuo. Los lineamientos evolucionan, y un documento aprobado hace un año puede estar declarando conformidad con reglas que cambiaron.
>
> Registra en la tabla de identidad la versión de `GOB-MAT-001` que consultaste: su catálogo es el índice del corpus con el estado de cada documento en esa fecha.
>
> Una vez completada esta sección, elimina este bloque de orientación.

#### Disparadores de revisión obligatoria

El documento **debe revisarse y volver a aprobarse** cuando ocurra cualquiera de estos hechos, sin esperar a la revisión programada:

| Disparador | Por qué obliga |
|---|---|
| Un documento del que este sistema depende **gradúa a `Vigente`** | Cambia lo que es exigible: lo que era criterio técnico pasa a ser exigible contractualmente (`GOB-MAT-001`, regla de exigibilidad) |
| Cambia una regla que este documento **cita como sustento** de una decisión | El fundamento de un `AD-NNN` deja de existir o dice otra cosa |
| El sistema **cambia de Estadio** o de **criticidad** | Se alteran las declaraciones obligatorias de `§1.4` y, con la criticidad, los objetivos de RTO/RPO |
| **Vence una excepción `EXC-`** registrada en este documento | Toda excepción tiene fecha de revisión; al vencer, o se subsana o se renueva con justificación |
| Intervención mayor sobre el sistema | Modernización, migración de estadio o cambio de topología de despliegue |

**No obligan a revisión:** correcciones editoriales o de erratas en un lineamiento, cambios en documentos que `§5.2` declara no aplicables, y cambios de versión que no alteren una regla que este documento invoque.

#### Revisión programada

Con independencia de los disparadores, el documento se revisa **al menos cada 12 meses**. La revisión puede concluir «sin cambios», pero debe quedar registrada en el historial con la nueva línea base consultada.

#### Responsabilidad

Detectar los disparadores es responsabilidad del **arquitecto responsable del sistema**, apoyándose en el historial de versiones de `GOB-MAT-001`. Arquitectura OTI comunica las graduaciones a `Vigente`, que es el disparador de mayor impacto.

---

## 2. GLOSARIO TÉCNICO

> 📋 **Orientación para el arquitecto**
>
> El glosario tiene un propósito concreto: eliminar la ambigüedad en la lectura del documento. No es un diccionario enciclopédico ni una lista de definiciones genéricas copiadas de internet. Cada término debe estar definido en el contexto específico del sistema que se documenta.
>
> **Criterio para incluir un término:** incluye el término si un lector de otra área (negocio, soporte, seguridad) podría interpretarlo de forma distinta a como se usa en este documento. Si el significado es obvio para todas las audiencias, no lo incluyas.
>
> **Qué debe incluir obligatoriamente:**
> - Todas las siglas que aparecen en el documento, tanto institucionales (SAA, NSP, OTI) como del proyecto (PAST, EAF) y tecnológicas (WAF, JWT, REST)
> - Términos de negocio propios del dominio de la ONP que pueden no ser conocidos por el equipo técnico
> - Términos técnicos que pueden ser conocidos por el equipo técnico pero no por los stakeholders de negocio
>
> **Lo que NO debe incluir:**
> - Definiciones genéricas de términos universalmente conocidos como "base de datos", "servidor" o "usuario"
> - Definiciones copiadas textualmente de Wikipedia o manuales sin adaptación al contexto
>
> **Consejo práctico:** elabora el glosario al final, una vez que el documento esté completo. Revisa cada sección e identifica los términos que pueden generar confusión. Ordenar alfabéticamente facilita la consulta.
>
> Una vez completado el glosario, elimina este bloque de orientación.

| # | Término | Descripción |
|---|---|---|
| 1 | [Sigla o término] | [Definición clara y concisa en el contexto del sistema] |
| 2 | [Sigla o término] | [Definición clara y concisa en el contexto del sistema] |
| 3 | [Sigla o término] | [Definición clara y concisa en el contexto del sistema] |

---

## 3. DIAGRAMA DE ARQUITECTURA DE TI

A continuación se presenta la Vista General de la arquitectura de **[Nombre del Sistema]**, que muestra los principales componentes del sistema, sus relaciones, las integraciones con sistemas internos y externos, y los orígenes de datos.

> 📋 **Orientación para el arquitecto — Sección 3 completa**
>
> Esta sección contiene la vista de referencia rápida del sistema, pensada para ser leída junto al diagrama. Es la evolución del diagrama que el equipo ya conoce, pero con criterios más claros sobre qué nivel de detalle corresponde aquí y qué va en los Anexos.
>
> **Regla fundamental de esta sección:** describe QUÉ es cada componente y PARA QUÉ sirve. No describas CÓMO funciona internamente ni CÓMO se configura. Si te encuentras escribiendo puertos, URLs, campos de datos, parámetros de configuración o pasos de un proceso, ese contenido no pertenece aquí.
>
> **Sobre el diagrama:** usa el mismo diagrama por capas que el equipo ya maneja. Asegúrate de incluir una leyenda de colores que diferencie visualmente los componentes nuevos a implementar de los componentes existentes. El diagrama debe ser autoexplicativo junto con la narrativa de esta sección.
>
> Una vez completada esta sección, elimina este bloque de orientación.

> ### 📐 Regla de precedencia — esta sección es **derivada**, no autónoma
>
> El **Anexo A es la fuente autoritativa** de la arquitectura: es el modelo ArchiMate mantenido en Archi, versionable y con niveles de abstracción explícitos. Esta sección `§3` es una **lectura derivada de ese modelo**, dirigida a las audiencias que no leen ArchiMate (Gerencia, Stakeholders, Soporte).
>
> De ello se siguen tres reglas de obligado cumplimiento:
>
> 1. **Ante cualquier divergencia entre `§3` y el Anexo A, prevalece el Anexo A.** Esta sección no crea arquitectura: la describe.
> 2. **`§3` no puede contener ningún elemento que no exista en el Anexo A.** Si al redactarla aparece un componente, una integración o un origen de datos que no está modelado, el error está en el modelo — se corrige allí primero, no aquí. Es la vía por la que las dos representaciones divergen.
> 3. **`§3` se actualiza siempre después del Anexo A**, nunca antes ni en paralelo. Un cambio arquitectónico se modela y luego se narra.
>
> El Anexo E verifica la correspondencia (ítem 18).
>
> **Sobre la Capa de Seguridad (`B`):** es la única que no tiene vista propia en el Anexo A — sus elementos aparecen distribuidos entre la Vista de Contexto (actores y perímetro de acceso) y la Vista de Infraestructura (zonas de seguridad y red). Esta capa **agrupa esa lectura**; tampoco puede introducir componentes ausentes del modelo.

---

*[Insertar Diagrama de Arquitectura de TI — Ilustración 01]*

**Ilustración 01 — Vista General de Arquitectura de TI**

---

### A. CAPA DE USUARIOS

> 📋 **Orientación para el arquitecto**
>
> Describe quiénes son los actores que usan el sistema. Diferencia entre usuario interno (accede desde la red de la ONP) y usuario externo (accede desde internet). Si el sistema solo tiene un tipo de usuario, elimina la subdivisión. Para cada tipo de usuario indica: quién es en términos de rol o perfil, y cómo accede al sistema (canal web, aplicación móvil, servicio automatizado). No describas los permisos ni los flujos de proceso.

[Describir los tipos de usuario que interactúan con el sistema.]

- **Usuario interno:** [Rol o perfil y canal de acceso]
- **Usuario externo:** [Rol o perfil y canal de acceso]

### B. CAPA DE SEGURIDAD

> 📋 **Orientación para el arquitecto**
>
> Describe los componentes de seguridad que protegen el acceso al sistema. Agrupa en seguridad interna (para acceso desde la red ONP) y seguridad externa (para acceso desde internet) si aplican ambas. Para cada componente indica: qué es, si es existente o nuevo, y cuál es su función de seguridad específica en este sistema. No describas configuraciones, reglas de firewall, políticas de contraseñas ni detalles de implementación de tokens.
>
> **Componentes típicos a considerar:** WAF, protección anti-bots, autenticación de terceros, SAA/token institucional, VPN para integraciones con otras entidades.

[Describir los componentes de seguridad diferenciando seguridad interna y externa.]

- **Seguridad interna:** [Componentes y su función de seguridad]
- **Seguridad externa:** [Componentes y su función de seguridad]

### C. CAPA DE APLICACIONES

> 📋 **Orientación para el arquitecto**
>
> Describe los componentes de aplicación del sistema alineados a **LIN-DIS-001 (Patrones Tácticos de Diseño)**.
> - **Estilo Táctico Seleccionado:** Declara explícitamente si el sistema adopta *Monolito Modular* o *Arquitectura Hexagonal / Limpia* (por defecto en nuevos proyectos, según `ARQ-R-001` (LIN-ARQ-001 §2.1) y `LIN-DIS-001 §2`) o *Capas Clásica* (solo mantenimiento/legacy).
> - **Frontend:** Indica el tipo de aplicación (SPA, MPA, aplicación móvil) y la estrategia de despliegue.
> - **Backend / Contextos Delimitados (Bounded Contexts):** Lista cada servicio o módulo táctico de dominio con su nombre oficial y responsabilidad principal en una línea. Si el sistema expone un **API Gateway**, **BFF (Backend for Frontend)** o **Facade Arquitectónico**, descríbelo como punto de entrada perimetral.
> - **Integraciones con Legados:** Si el Backend interactúa con sistemas core legacy de la institución (Estadio 1), declara si implementa el patrón **Anti-Corruption Layer (ACL)** (`DIS-R-006` (LIN-DIS-001 §5.4)) o **Strangler Fig** (`LIN-ARQ-001 §2.2`).
>
> **Lo que NO va aquí:** protocolos de comunicación en detalle, contratos de API, formatos de mensajes, lógica interna de métodos.

[Describir el estilo arquitectónico seleccionado, los componentes del Frontend y Backend con sus responsabilidades y Bounded Contexts.]

- **Estilo Arquitectónico Táctico:** [Monolito Modular / Arquitectura Hexagonal / Capas Clásica]
- **Frontend:** [Tipo de aplicación, responsabilidad principal y estrategia de despliegue]
- **Backend (Bounded Contexts / Servicios):** [Lista de módulos o microservicios con su responsabilidad de dominio]
- **Perímetro de Exposición:** [API Gateway / BFF / Facade que gestiona la entrada al sistema]
- **Integraciones internas (y ACL si aplica):** [Sistemas de la ONP con los que interactúa y dirección de datos]
- **Integraciones externas:** [Servicio de fachada de Entidades Externas y entidades que encapsula]

### D. CAPA DE SERVICIOS

> 📋 **Orientación para el arquitecto**
>
> Lista los servicios transversales que el sistema consume o que son compartidos con otros sistemas de la ONP. Para cada uno indica: nombre del servicio, si es existente o nuevo a implementar, y cuál es su función dentro de este sistema específicamente.
>
> **Servicios transversales obligatorios o comunes en la ONP (según LIN-ARQ-001 y LIN-OBS-001):**
> - **Observabilidad y Logs:** Centralización de logs estructurados con `trace_id` y telemetría (OpenTelemetry / Elastic ECS).
> - **Seguridad y Autenticación:** SAA / Token institucional para APIs internas, WAF y VPN para externos.
> - **Comunes y Notificaciones:** Gestor documental, servicio SMTP de correos, servicio de SMS.
> - **Patrones de Integración Asíncrona (si aplica):** Eventos de dominio vía **CloudEvents over Apache Kafka** o conectores **CDC Debezium / Transactional Outbox** (`LIN-DIS-001 §5`).

- **Servicios transversales a implementar:** [Nombre y función específica en este sistema]
- **Servicios transversales existentes a consumir:** [Nombre y función específica en este sistema]

### E. CAPA DE DATOS

> 📋 **Orientación para el arquitecto**
>
> Lista todas las bases de datos que el sistema usa, tanto las propias como las de sistemas con los que se integra. Para cada una indica el tipo de acceso usando la convención CRUD. Incluir las bases de datos de los sistemas legados es importante porque permite evaluar el impacto de cambios futuros.
>
> **Convención de acceso:** C = Creación, R = Lectura, U = Actualización, D = Eliminación.
>
> **No incluyas:** esquemas de tablas, modelos de datos, diccionarios de datos. Eso pertenece a la documentación técnica de base de datos.

| Base de datos | Tipo | Sistema propietario | Acceso |
|---|---|---|---|
| [Nombre BD propia] | [Oracle 19c / PostgreSQL / etc.] | [Este sistema] | CRU |
| [Nombre BD legado] | [Oracle 11g / etc.] | [Sistema legado] | R |

---

## 4. SUPUESTOS Y RESTRICCIONES

> 📋 **Orientación para el arquitecto — Sección 4 completa**
>
> Esta es la sección más descuidada en los documentos de arquitectura y una de las más valiosas. Protege al arquitecto frente a cambios de alcance y deja evidencia de las condiciones bajo las cuales se tomaron las decisiones. Si en el futuro algo cambia y la arquitectura debe revisarse, esta sección es la que explica por qué se diseñó de esa manera.
>
> **Diferencia clave entre supuesto y restricción:**
> - Un **supuesto** es algo que se asume como verdadero porque no se tiene certeza en el momento del diseño. Si el supuesto resulta falso, la arquitectura puede necesitar revisarse. Ejemplo: "Se asume que el servicio de autenticación de la SBS estará disponible antes del inicio del desarrollo."
> - Una **restricción** es algo que está dado y no puede cambiarse. No es una decisión del arquitecto, es una condición externa o institucional. Ejemplo: "El sistema debe desplegarse en la infraestructura existente de la ONP."
>
> Una vez completada esta sección, elimina este bloque de orientación.

### 4.1 Supuestos

> 📋 **Orientación para el arquitecto**
>
> Lista los supuestos que sustentan las decisiones arquitectónicas. Para cada supuesto, indica qué se está asumiendo y cuál sería el impacto en la arquitectura si ese supuesto resulta incorrecto. Esto es especialmente importante en proyectos con integraciones externas cuya disponibilidad o especificación aún no está confirmada.
>
> **Categorías comunes de supuestos en proyectos ONP:**
> - Disponibilidad de servicios de entidades externas (RENIEC, SBS, SUNAT)
> - Información de infraestructura que aún no ha sido confirmada por AD
> - Requisitos funcionales que están en proceso de definición
> - Capacidad de los equipos que deben exponer servicios para la integración
>
> **Error frecuente:** dejar esta sección con una sola frase genérica como "el documento se elaboró con la información disponible a la fecha". Eso no es un supuesto, es una advertencia. Los supuestos deben ser específicos y trazables.

El presente documento ha sido elaborado en base a la información disponible a la fecha de su elaboración. Los supuestos considerados son los siguientes:

- [Supuesto 1: qué se asume como verdadero e impacto si resulta incorrecto]
- [Supuesto 2: qué se asume como verdadero e impacto si resulta incorrecto]

Cualquier modificación en los requisitos funcionales o no funcionales podrá requerir la revisión y actualización del presente documento.

### 4.2 Restricciones

> 📋 **Orientación para el arquitecto**
>
> Lista las restricciones que condicionaron las decisiones arquitectónicas. Organízalas por categoría para facilitar su lectura. Recuerda que una restricción es una condición dada, no una decisión: no escribas "se decidió usar Oracle" sino "el sistema debe usar la infraestructura de base de datos Oracle institucional existente".
>
> **Categorías a considerar:**
> - **Técnicas:** tecnologías obligatorias, estándares aprobados institucionalmente (como ArchiMate), plataformas existentes que deben usarse
> - **Normativas:** leyes, directivas del MEF, políticas de seguridad de la OTI, Ley de Protección de Datos Personales
> - **Operativas:** plazos del proyecto, disponibilidad de ambientes, capacidad de infraestructura, recursos humanos disponibles

- **Restricciones técnicas:** [Tecnologías o estándares de uso obligatorio, plataformas existentes que deben usarse]
- **Restricciones normativas:** [Marcos legales, directivas institucionales, políticas de seguridad aplicables]
- **Restricciones operativas:** [Plazos, disponibilidad de ambientes, recursos]

---

## 5. DOCUMENTOS ADJUNTOS

### 5.1 Documentos de análisis

- [Documento de Análisis — Nombre y versión]
- [Documento de Análisis — Nombre y versión]

### 5.2 Corpus normativo de Arquitectura aplicable

> 📋 **Orientación para el arquitecto**
>
> Esta tabla es el corpus completo, no una selección. Marca en la última columna qué documentos aplican a tu sistema y por qué los que no aplican quedan fuera — un sistema sin frontend no necesita `LIN-FE-ANG-001`, pero **debe decirlo**, no omitirlo en silencio.
>
> **Lee la columna de estado.** Un documento `Vigente` es **exigible contractualmente** y puede invocarse en un TDR. Uno `En revisión` obliga como criterio técnico pero, conforme a la regla de exigibilidad de `GOB-MAT-001`, **no puede usarse como criterio de aceptación formal** mientras no gradúe. Consulta `GOB-MAT-001` para el estado del día: esta tabla refleja el corpus a la fecha de la plantilla y los documentos evolucionan.

| Código | Documento | Estado a la fecha de esta plantilla | ¿Aplica al sistema? |
|---|---|---|---|
| `LIN-ARQ-001` | Marco Rector de Arquitectura (Nivel 1) | En revisión | **Siempre** |
| `LIN-DIS-001` | Diseño de Software y Patrones Tácticos (Nivel 2) | En revisión | **Siempre** |
| `LIN-PAT-001` | Catálogo Oficial de Patrones y Fichas de Decisión | En revisión | **Siempre** |
| `LIN-VER-001` | Versionamiento, Control de Cambios y Revisión de Código | En revisión | **Siempre** |
| `LIN-TEST-001` | Estándar de Pruebas | **Vigente** | **Siempre** |
| `LIN-OBS-001` | Log, Trazabilidad y Observabilidad | **Vigente** | **Siempre** |
| `LIN-SEC-APP-001` | Seguridad en Aplicaciones | En revisión | **Siempre** |
| `LIN-K8S-001` | Contenedores y Orquestación | En revisión | Sí, salvo excepción de despliegue en VM (`LIN-ARQ-001 §5.2`) |
| `LIN-CICD-001` | Integración y Entrega Continua | En revisión | **Siempre** |
| `LIN-DEV-JAVA-001` | Estándar de Desarrollo Java | En revisión | Si hay backend Java |
| `LIN-API-REST-001` | Servicios Web y APIs REST | En revisión | Si expone o consume APIs REST |
| `LIN-FE-ANG-001` | Diseño Web Frontend Angular | En revisión | Si hay frontend web |
| `LIN-BD-ORA-001` | Base de Datos Oracle | En revisión | Si persiste en Oracle |
| `LIN-BUS-001` | Mensajería y Bus de Eventos | En revisión | Si publica o consume eventos Kafka |
| `LIN-BI-001` | Explotación y Analítica de Datos (BI) | En revisión | Si alimenta o consume el Lakehouse |
| `LIN-PERF-001` | Pruebas de Rendimiento, Carga y Estrés | En revisión | Según criticidad (`LIN-PERF-001 §6.1`) |
| `LIN-IAC-001` | Infraestructura como Código | En revisión | Si aprovisiona infraestructura con Terraform |
| `GOB-MAT-001` | Matriz de Propiedad Documental | **Vigente** | Referencia — resuelve qué documento es dueño de cada tema |
| `GLOSARIO-ONP` | Glosario transversal | Vigente / Operativo | Referencia |

> **`LIN-ARQ-000`** es cantera histórica **congelada**: no se cita como norma vigente. **`LIN-DOC-001`** (Documentación y Modelado) está pendiente de elaboración.

---
---

# ANEXO A: VISTAS DE ARQUITECTURA (ArchiMate)

Este anexo presenta la arquitectura de **[Nombre del Sistema]** mediante cuatro vistas modeladas en ArchiMate, cada una orientada a una audiencia y propósito específico. El uso de múltiples vistas permite comunicar la arquitectura de forma apropiada según el nivel de abstracción requerido.

> **Herramienta de modelado:** Archi (ArchiMate 3.x) — estándar aprobado en la ONP, normado en `DOC-R-002` (LIN-DOC-001 §7).

> 🔍 **Este modelo será contrastado contra la arquitectura observada.** `LIN-ARQ-001 §5.5` establece que, para sistemas de criticidad **Alta o Media**, Arquitectura OTI compara semestralmente estas vistas contra el **grafo de servicios** derivado de las trazas en producción (`LIN-OBS-001 §5.8`). Una dependencia que exista en ejecución y no esté modelada aquí es una divergencia que se resuelve **corrigiendo este documento o el código** — nunca dando por buena la desviación por el hecho de estar en producción. Modela las integraciones reales, incluidas las de baja frecuencia.

> **Para el arquitecto que elabora este documento:** cada vista tiene una sección de orientación marcada con 📋 que explica qué se espera modelar, qué elementos incluir y qué evitar. Una vez completada la vista, esa orientación puede eliminarse del documento final.

---

## A.1 Vista de Contexto

**Tipo de vista ArchiMate:** Vista de Motivación / Contexto
**Audiencia:** Gerencia, stakeholders, cualquier persona interesada en comprender el rol del sistema en la organización.

### Propósito

Mostrar el sistema como una unidad y su relación con los actores externos (usuarios, sistemas y entidades) que interactúan con él. No expone detalle interno. Responde a la pregunta: **¿qué es el sistema y con quién se relaciona?**

---

> 📋 **Orientación para el arquitecto**
>
> **¿Qué debes modelar en Archi?**
> Esta es la vista más simple y la primera que debe elaborarse. El sistema completo se representa como un único bloque (Application Component). Alrededor de él se ubican todos los actores que interactúan con él: tipos de usuarios (ciudadanos, funcionarios, representantes de empresas) y sistemas externos (otras entidades del Estado, servicios de terceros). Las relaciones deben ser simples, mostrando solo que existe una interacción, no cómo funciona técnicamente.
>
> **Elementos que debes incluir:**
> - El sistema como un solo bloque con su nombre oficial
> - Los tipos de usuario que lo usan (no personas específicas, sino roles)
> - Los sistemas externos e internos con los que se integra, declarando explícitamente a qué estadio de la topología institucional pertenecen (`Estadio 1 Monolito Tradicional/Legacy`, `Estadio 2 Monolito Modular` o `Estadio 3 Microservicios Selectivos`, según `ARQ-R-001` (LIN-ARQ-001 §2.1))
> - Una relación por cada interacción relevante, con una etiqueta que indique qué hace (ej. "consulta datos", "recibe notificación")
>
> **Lo que NO debe aparecer en esta vista:**
> - Componentes internos del sistema (frontend, backend, base de datos)
> - Protocolos de comunicación ni tecnologías específicas
> - Flujos de datos o secuencias de pasos
> - Más de dos niveles de detalle
>
> **Pregunta de validación antes de cerrar la vista:** ¿Podría un gerente sin conocimiento técnico entender quiénes usan el sistema y con qué otros sistemas se relaciona? Si la respuesta es sí, la vista está bien.

---

*[Insertar Vista de Contexto — Ilustración A.1]*

**Ilustración A.1 — Vista de Contexto (ArchiMate)**

---

### Notación utilizada

| Elemento ArchiMate | Representación | Descripción |
|---|---|---|
| Business Actor | Actor (persona o entidad) | Usuario o sistema externo que interactúa con el sistema |
| Application Component | Componente de aplicación | El sistema modelado como unidad |
| Association | Asociación | Relación general de interacción entre actores y el sistema |
| Serving | Relación de servicio | El sistema provee un servicio a un actor |

### Narrativa

[Describir en prosa quiénes son los actores que interactúan con el sistema, qué rol cumple cada uno y cuál es la naturaleza de esa relación. No describir cómo funciona internamente el sistema. Ejemplo: "El sistema es accedido por dos tipos de usuarios externos: el Ciudadano, que utiliza la plataforma para realizar sus trámites de manera autónoma, y el representante de la EAF, que actúa en nombre de una empresa administradora de fondos. A su vez, el sistema se integra con entidades del Estado como RENIEC para la validación de identidad y con la SBS para la autenticación de representantes institucionales."]

---

## A.2 Vista de Aplicación

**Tipo de vista ArchiMate:** Vista de Capa de Aplicación con elementos de Tecnología
**Audiencia:** Equipos de plataforma, seguridad, DevOps y arquitectos.

### Propósito

Mostrar los principales componentes de aplicación que componen el sistema, sus interfaces, sus dependencias internas y sus integraciones con sistemas externos. Responde a la pregunta: **¿cuáles son los componentes del sistema y cómo se comunican entre sí?**

---

> 📋 **Orientación para el arquitecto**
>
> **¿Qué debes modelar en Archi?**
> Esta es la vista central del documento. Aquí se abre el bloque del sistema de la Vista de Contexto y se muestran sus partes principales: el frontend, el backend (con sus servicios si aplica), el API Gateway, los servicios transversales y las bases de datos propias. También se mantienen visibles los sistemas externos con los que se integra, para mostrar cómo se conectan con los componentes internos.
>
> **Elementos que debes incluir:**
> - Todos los componentes desplegables del sistema (frontend, servicios de backend, gateway, servicios de integración)
> - Declaración visual y conceptual de los **Bounded Contexts** (Contextos Delimitados de DDD) del sistema (`DIS-R-002` (LIN-DIS-001 §3))
> - Las interfaces o APIs expuestas por cada componente (indicando si cuentan con contrato OpenAPI 3.0 Code-First según `LIN-API-REST-001`)
> - Las bases de datos propias del sistema (Oracle 19c / PostgreSQL institucional)
> - Los sistemas internos de la ONP con los que se integra (declarando si se intermedia con una Capa Anticorrupción **ACL** para legados Estadio 1)
> - Los sistemas externos agrupados por entidad y comunicados vía Facade perimetral
> - Las relaciones de comunicación entre componentes, indicando el protocolo si es relevante (REST sincrónico o CloudEvents asíncrono sobre Apache Kafka)
> - Diferenciación visual entre componentes nuevos a implementar y componentes ya existentes (usar colores según leyenda)
> - Leyenda de colores explicando el código visual utilizado
>
> **Lo que NO debe aparecer en esta vista:**
> - Lógica interna de cada componente (eso va en la Vista de Componentes)
> - Configuraciones técnicas específicas (puertos, URLs, versiones)
> - Detalles de infraestructura como nodos o servidores (eso va en la Vista de Infraestructura)
>
> **Pregunta de validación antes de cerrar la vista:** ¿Puede un especialista de plataforma identificar todos los componentes que debe provisionar y cómo se conectan entre sí? Si la respuesta es sí, la vista está bien.

---

*[Insertar Vista de Aplicación — Ilustración A.2]*

**Ilustración A.2 — Vista de Aplicación (ArchiMate)**

---

### Notación utilizada

| Elemento ArchiMate | Representación | Descripción |
|---|---|---|
| Application Component | Componente de aplicación | Unidad desplegable del sistema (frontend, backend, servicio) |
| Application Interface | Interfaz de aplicación | Punto de exposición de un servicio o API |
| Application Service | Servicio de aplicación | Funcionalidad expuesta por un componente hacia otros |
| Serving | Relación de servicio | Un componente provee funcionalidad a otro |
| Association | Asociación | Relación general entre elementos |
| System Software | Software de sistema | Middleware, bus de eventos, API Gateway |
| Data Store | Almacén de datos | Base de datos propia del sistema |

### Narrativa

[Describir en prosa los componentes principales del sistema, sus responsabilidades y el flujo de comunicación más relevante. Mencionar qué componentes son nuevos y cuáles son existentes. Ejemplo: "Las peticiones del usuario ingresan a través del Frontend (SPA responsiva a implementar), que se comunica con el Backend vía el API Manager / Gateway. El Backend está compuesto por tres servicios independientes: Elección, Afiliación y Traslado. Para las integraciones con entidades externas, se utiliza un servicio de fachada que encapsula las llamadas hacia RENIEC, SBS, SUNAT y otras entidades. Los datos del sistema se persisten en una base de datos Oracle 19c propia."]

---

## A.3 Vista de Componentes

**Tipo de vista ArchiMate:** Vista de Aplicación detallada (por componente)
**Audiencia:** Desarrolladores y arquitectos de software.

### Propósito

Mostrar la estructura interna de los componentes más relevantes del sistema: sus módulos internos, las funciones que exponen y cómo colaboran entre sí. Responde a la pregunta: **¿qué hay dentro de cada componente y cómo está organizado internamente?**

---

> 📋 **Orientación para el arquitecto**
>
> **¿Qué debes modelar en Archi?**
> Esta vista se elabora una vez por cada componente que tenga suficiente complejidad interna como para requerir explicación. No es obligatorio hacerla para todos los componentes: prioriza los que tienen mayor criticidad, mayor cantidad de responsabilidades o que han generado más preguntas durante la revisión. Un buen criterio es: si el equipo de desarrollo necesita más contexto para implementarlo correctamente, necesita esta vista.
>
> **Elementos que debes incluir:**
> - Los módulos o sub-componentes internos del componente que se está detallando
> - Las funciones o capacidades de cada módulo
> - Las interfaces internas por donde se comunican los módulos entre sí
> - Los objetos de datos relevantes que fluyen entre módulos
> - Las relaciones con componentes externos al que se está detallando (para mostrar los puntos de entrada y salida)
>
> **Lo que NO debe aparecer en esta vista:**
> - Clases, métodos o código fuente (eso es diseño de detalle)
> - Tablas de base de datos o esquemas de datos
> - Configuraciones de librerías o frameworks específicos
>
> **¿Cuántas vistas de componentes elaborar?**
> Elabora una vista por cada componente complejo. En un sistema típico de la ONP esto suele ser entre 2 y 4 vistas. Si un componente es simple y su responsabilidad queda clara en la Vista de Aplicación, no necesita su propia Vista de Componentes.
>
> **Pregunta de validación antes de cerrar la vista:** ¿Puede un desarrollador nuevo entender qué debe construir dentro de este componente y cómo se relaciona con el resto del sistema? Si la respuesta es sí, la vista está bien.

---

*[Insertar Vista de Componentes de [Nombre del Componente] — Ilustración A.3]*

**Ilustración A.3 — Vista de Componentes: [Nombre del Componente] (ArchiMate)**

---

### Notación utilizada

| Elemento ArchiMate | Representación | Descripción |
|---|---|---|
| Application Component | Componente interno | Módulo o sub-servicio dentro del componente principal |
| Application Function | Función de aplicación | Capacidad o comportamiento interno del módulo |
| Application Interface | Interfaz interna | Punto de comunicación entre módulos internos |
| Serving | Relación de servicio | Un módulo provee funcionalidad a otro |
| Triggering | Relación de disparo | Un módulo inicia la ejecución de otro |
| Data Object | Objeto de datos | Entidad de datos relevante manipulada por los módulos |

### Narrativa

[Describir en prosa los módulos internos del componente, sus responsabilidades individuales y cómo colaboran para cumplir la función del componente. Indicar los puntos de entrada (cómo llegan las peticiones) y los puntos de salida (qué devuelve o con qué interactúa hacia afuera). Ejemplo: "El servicio de Afiliación está compuesto por tres módulos internos: el módulo de Validación, que verifica la identidad del solicitante consultando al servicio de Entidades Externas; el módulo de Proceso de Afiliación, que ejecuta las reglas de negocio del trámite; y el módulo de Persistencia, que registra el resultado en la base de datos del PAST. Las peticiones ingresan desde el API Gateway y las respuestas se devuelven al Frontend a través del mismo canal."]

---

## A.4 Vista de Infraestructura

**Tipo de vista ArchiMate:** Vista de Capa de Tecnología e Infraestructura
**Audiencia:** Equipos de infraestructura, plataforma y soporte.

### Propósito

Mostrar cómo los componentes del sistema son desplegados en la infraestructura tecnológica: nodos de cómputo, orquestadores, redes, zonas de seguridad y ambientes. Responde a la pregunta: **¿dónde y cómo se despliega el sistema?**

---

> 📋 **Orientación para el arquitecto**
>
> **¿Qué debes modelar en Archi?**
> Esta vista muestra la capa física y lógica donde vive el sistema. Toma los componentes de la Vista de Aplicación y los ubica sobre la infraestructura que los soporta. Debe mostrar todos los ambientes relevantes del proyecto (como mínimo DEV, QA y PRD; si existe UAT también debe incluirse). Para cada ambiente se muestra cómo están distribuidos los componentes en los nodos disponibles.
>
> **Elementos que debes incluir:**
> - Los ambientes del sistema (DEV, QA, UAT, PRD) como agrupadores
> - Los nodos de cómputo: servidores virtuales y nodos workers del cluster institucional
> - El orquestador de contenedores institucional obligatorio: **Kubernetes (K8s) con motor de runtime CRI `containerd`** y contenedores inmutables (`LIN-ARQ-001 §5.2`)
> - Los artefactos desplegables: imágenes de contenedor por cada componente o Bounded Context, asignadas a sus respectivos *Namespaces* (ej. `past-frontend`, `past-backend`)
> - Las redes y zonas de seguridad de la OTI: DMZ (acceso público), Red Interna de Aplicaciones (K8s pods), y Red de Datos (Oracle/BD)
> - Los mecanismos de acceso perimetral externo: WAF institucional, Balanceador de Carga e Ingress Controller
> - Las relaciones de despliegue: qué artefacto se despliega en qué nodo o pod
> - La estrategia de escalamiento horizontal y resiliencia: número mínimo y máximo de réplicas por servicio (HPA) y sondas de salud (*Liveness / Readiness / Startup Probes*)
>
> **Lo que NO debe aparecer en esta vista:**
> - Configuraciones internas de los contenedores (variables de entorno, puertos específicos)
> - Scripts de despliegue o archivos de configuración (Helm charts, docker-compose)
> - Lógica de negocio o flujos funcionales
>
> **Coordinación necesaria antes de elaborar esta vista:**
> Antes de diagramar, debes confirmar con el equipo de plataforma / AD: qué orquestador está disponible y aprobado, si se usará un cluster existente o uno nuevo, cuántos nodos hay disponibles por ambiente, y cuál es la topología de red institucional.
>
> **Pregunta de validación antes de cerrar la vista:** ¿Puede el equipo de infraestructura provisionar los ambientes y desplegar el sistema usando solo esta vista como referencia, sin necesitar preguntar al arquitecto? Si la respuesta es sí, la vista está bien.

---

*[Insertar Vista de Infraestructura — Ilustración A.4]*

**Ilustración A.4 — Vista de Infraestructura (ArchiMate)**

---

### Notación utilizada

| Elemento ArchiMate | Representación | Descripción |
|---|---|---|
| Node | Nodo | Servidor físico o virtual, nodo del orquestador |
| System Software | Software de sistema | Sistema operativo, orquestador de contenedores, middleware |
| Artifact | Artefacto | Imagen de contenedor o paquete desplegable |
| Communication Network | Red de comunicación | Red que conecta nodos (interna, externa, DMZ) |
| Deployment Relationship | Relación de despliegue | Artefacto desplegado sobre un nodo específico |
| Assignment | Asignación | Componente de aplicación asignado a su nodo de ejecución |

### Narrativa

[Describir en prosa los ambientes del sistema, los nodos principales, el orquestador utilizado, las zonas de red y la estrategia de despliegue. Mencionar explícitamente si se usa infraestructura existente o nueva. Ejemplo: "El sistema se despliega en un cluster Kubernetes existente gestionado por el equipo de AD, en tres ambientes: DEV, QA y PRD. El Frontend se despliega como un contenedor en el namespace past-frontend, con una réplica en DEV/QA y dos réplicas en PRD. Los servicios del Backend se despliegan en el namespace past-backend. El acceso externo se canaliza a través del WAF institucional y un Ingress Controller, que enruta el tráfico hacia los servicios correspondientes dentro del cluster. La base de datos Oracle 19c es provista por el equipo de BD sobre infraestructura existente."]

---
---

# ANEXO B: DECISIONES ARQUITECTÓNICAS (ADRs)

Este anexo registra las decisiones arquitectónicas significativas tomadas durante el diseño del sistema. Su propósito es preservar la memoria institucional, facilitar la trazabilidad de las decisiones y evitar que se repitan análisis ya realizados.

> 📋 **Orientación para el arquitecto — Anexo B completo**
>
> Los ADRs (Architecture Decision Records) son el registro de las decisiones importantes que tomaste durante el diseño y por qué las tomaste. Son la diferencia entre un documento de arquitectura que solo describe qué se construyó y uno que explica por qué se construyó así.
>
> **¿Cuándo registrar un ADR?** Registra una decisión cuando cumpla al menos una de estas condiciones:
> - Tuviste que elegir entre dos o más alternativas válidas
> - La decisión tiene impacto en la estructura del sistema o en cómo los componentes se relacionan
> - La decisión podría ser cuestionada en el futuro por alguien que no estuvo en la discusión
> - La decisión tiene consecuencias negativas o compromisos que el equipo debe conocer
>
> **¿Qué NO registrar como ADR?**
> - Decisiones de implementación o configuración (qué puerto usar, qué nombre ponerle a una tabla)
> - Decisiones obvias que no requirieron evaluación de alternativas
> - Detalles de diseño interno de un componente
>
> **Ejemplos de decisiones que sí merecen un ADR en un proyecto típico de la ONP:**
> - Elección del patrón de arquitectura (microservicios vs monolito modular)
> - Elección del orquestador de contenedores y si se usa cluster existente o nuevo
> - Elección del patrón de integración con legados (sincrónico vs asincrónico, directo vs cola)
> - Elección de la estrategia de autenticación externa con entidades como SBS
> - Adopción de OpenTelemetry como estándar de telemetría
>
> **¿Cuántos ADRs elaborar?** No hay un número mínimo ni máximo. En un proyecto de mediana complejidad como el PAST, entre 5 y 10 ADRs es un rango razonable. Si tienes menos de 3, probablemente estás subregistrando decisiones importantes.
>
> Una vez completado este anexo, elimina este bloque de orientación.

---

## B.1 Resumen de decisiones

> 📋 **Orientación para el arquitecto**
>
> Esta tabla es el índice de todos los ADRs del documento. Complétala una vez que hayas elaborado todos los ADRs en la sección B.2. El estado refleja en qué punto está cada decisión: una decisión puede estar en revisión si aún no ha sido validada con los stakeholders relevantes.

| ID | Título | Estado | Fecha |
|---|---|---|---|
| AD-001 | [Título de la decisión] | Aprobado | [DD/MM/AAAA] |
| AD-002 | [Título de la decisión] | En revisión | [DD/MM/AAAA] |

**Estados posibles:** Propuesto / En revisión / Aprobado / Descartado / Reemplazado por [AD-XXX]

> **`AD-XXX` es del proyecto; `ADR-XXX` es institucional — no se mezclan.** Las decisiones que registras aquí llevan el prefijo **`AD-`** y su alcance es este sistema. Las decisiones institucionales, que obligan a todo el corpus, llevan **`ADR-`** y no se crean desde un documento de proyecto: viven en la **Matriz de Decisiones Arquitectónicas de `LIN-ARQ-001` (Apéndice A)** con numeración correlativa (`ADR-001`…`ADR-014`), o como documento propio con identificador temático (`ADR-WSO2-001`, `ADR-CLOUDEVENTS-001`, `ADR-TLS-INTERNO-001`) cuando la decisión requiere desarrollo extenso.
>
> Si tu proyecto necesita una **excepción a un lineamiento institucional**, no basta con un `AD-XXX`: se eleva al Comité de Arquitectura y, de aprobarse, se registra como `ADR-` en la matriz del marco rector. Un `AD-XXX` no puede, por sí solo, dispensar del cumplimiento de un lineamiento.

---

## B.2 Detalle de decisiones

> 📋 **Orientación para el arquitecto**
>
> Completa una ficha por cada decisión listada en B.1. La clave de un buen ADR está en el campo "Contexto": debe explicar la situación real que te llevó a tomar la decisión, no solo enunciar la decisión en sí. Un lector que no estuvo en las reuniones debe poder entender por qué era necesario decidir algo y qué estaba en juego.
>
> El campo "Alternativas evaluadas" debe incluir al menos dos opciones reales que fueron consideradas. Si solo hubo una opción posible, probablemente no necesita ser un ADR.
>
> Duplica el bloque de ficha para cada decisión adicional.

### AD-001: [Título de la decisión]

| Campo | Detalle |
|---|---|
| **ID** | AD-001 |
| **Título** | [Título descriptivo que identifique claramente la decisión] |
| **Estado** | Propuesto / En revisión / Aprobado / Descartado |
| **Fecha** | [DD/MM/AAAA] |
| **Alineación Normativa ONP / ¿Es Excepción?** | [Declara explícitamente: **"Alineado a LIN-ARQ-001 / LIN-DIS-001"** o **"EXCEPCIÓN a LIN-XXX"**. Si es una excepción que contraviene un lineamiento institucional (ej. no usar Java 21 o no desplegar en K8s), requiere justificación de fuerza mayor y la firma expresa de la **Dirección de Arquitectura de la OTI** para su validez técnica.] |
| **Contexto** | [Describir la situación concreta que motivó esta decisión: qué problema existía, qué necesidad había que cubrir, qué restricciones o fuerzas estaban en juego. Debe ser comprensible para alguien que no estuvo en las reuniones.] |
| **Alternativas evaluadas** | **Alternativa 1:** [Descripción y razón por la que fue descartada] / **Alternativa 2:** [Descripción y razón por la que fue descartada] |
| **Decisión adoptada** | [La opción elegida, expresada en una o dos oraciones claras y concretas.] |
| **Justificación** | [Por qué esta opción es mejor que las alternativas para este contexto específico, en términos de atributos de calidad, restricciones institucionales o criterios técnicos.] |
| **Consecuencias positivas** | [Beneficios concretos que esta decisión aporta al sistema] |
| **Consecuencias negativas / riesgos asumidos** | [Compromisos, limitaciones o riesgos que se asumen conscientemente con esta decisión] |

---
---

# ANEXO C: ATRIBUTOS DE CALIDAD

Este anexo describe los atributos de calidad relevantes para el sistema y cómo la arquitectura propuesta los aborda. Sirve como vínculo entre los requisitos no funcionales y las decisiones arquitectónicas.

> 📋 **Orientación para el arquitecto**
>
> Los atributos de calidad son las características del sistema que no se refieren a qué hace (funcionalidad) sino a cómo lo hace: qué tan disponible es, qué tan seguro, qué tan rápido, qué tan fácil de mantener. Son los requisitos no funcionales elevados al nivel arquitectónico.
>
> **¿Cómo completar esta tabla?**
> - La columna "Atributo" lista la característica de calidad. Usa los atributos relevantes para el sistema; no tienes que incluir todos los que aparecen en el ejemplo si no aplican.
> - La columna "Requisito / Expectativa" expresa el nivel esperado de ese atributo: debe ser concreto y medible si es posible (ej. "99.5% de disponibilidad en horario hábil" es mejor que "alta disponibilidad").
> - La columna "Decisión arquitectónica que lo aborda" vincula el atributo con la decisión concreta que lo satisface. Si hay un ADR relacionado, referenciarlo (ej. "Ver AD-003").
>
> **Diferencia con los ADRs:** el ADR explica POR QUÉ se tomó una decisión. Esta tabla explica QUÉ atributo de calidad satisface cada decisión. Son complementarios.
>
> **Error frecuente:** confundir atributos de calidad con requisitos funcionales. "El sistema debe registrar la afiliación" es un requisito funcional. "El sistema debe registrar la afiliación en menos de 3 segundos el 95% de las veces" es un atributo de calidad (rendimiento).
>
> **Atributos comunes a considerar:** Disponibilidad, Seguridad, Rendimiento, Escalabilidad, Observabilidad, Mantenibilidad, Interoperabilidad, Recuperabilidad (RTO/RPO).
>
> Una vez completada esta sección, elimina este bloque de orientación.

---

## C.1 Atributos de calidad y su cobertura arquitectónica

| Atributo | Requisito / Expectativa | Decisión arquitectónica que lo aborda |
|---|---|---|
| **Disponibilidad y Resiliencia** | [ej. 99.5% uptime en horario hábil y tolerancia a fallos transaccionales] | [ej. Despliegue en K8s con réplicas/probes; aislamiento de fallos en llamadas externas según la matriz por criticidad de `DIS-R-007` (LIN-DIS-001 §6) — timeout estricto y Bulkhead siempre, Circuit Breaker con Resilience4j solo en Microservicios o bajo ADR (`§6.2`) — Ver AD-00X] |
| **Seguridad** | [ej. Autenticación obligatoria en todas las APIs públicas y Zero Trust] | [ej. Validación del **token opaco de SAA** en cada servicio mediante `SaaTokenValidationFilter` (`SEC-R-002` (LIN-SEC-APP-001 §8.3)) — **el token SAA no es JWT**: no es autocontenido ni verificable localmente (`LIN-API-REST-001 §7.1`); autorización por permisos SAA con `hasAuthority` (`LIN-SEC-APP-001 §5.4`) — Ver AD-00X] |
| **Escalabilidad** | [ej. Soporte para N usuarios concurrentes en pico electoral] | [ej. Contenedorización inmutable en K8s con autoescalado horizontal (HPA) — Ver AD-00X] |
| **Observabilidad (Google SRE 4 Golden Signals)** | [ej. Monitoreo obligatorio de las 4 Señales Doradas: Latencia, Tráfico, Errores y Saturación (`ARQ-R-005` (LIN-ARQ-001 §5.3))] | [ej. OpenTelemetry + centralización de logs ECS con `trace.id`, propagación del header `X-Request-ID` (`LIN-OBS-001 §4.10`) y `codDetRespuesta` en el cuerpo de `ApiResponseWrapper` — **es un campo del body, no un header** (`API-R-002` (LIN-API-REST-001 §4.1)) — Ver AD-00X] |
| **Mantenibilidad** | [ej. Capacidad de actualizar o reemplazar un servicio sin afectar los demás] | [ej. Bounded Contexts independientes con contratos OpenAPI 3.0 Code-First (`LIN-API-REST-001`)] |
| **Interoperabilidad** | [ej. Integración con 10+ entidades externas del Estado y legados internos] | [ej. Servicio de fachada para Entidades Externas y Capa Anticorrupción (**ACL**) para legados ONP] |
| **Recuperabilidad** | [Criticidad asignada + RTO y RPO de su banda (`LIN-ARQ-001 §5.4.1`), con el nombre de quien los validó por el área usuaria] | [ej. Respaldo RMAN según `BD-R-002` (LIN-BD-ORA-001 §11.2) con frecuencia coherente al RPO; procedimiento de recuperación con orden de dependencias (`LIN-ARQ-001 §5.4.3`); prueba de restauración semestral — Ver AD-00X] |

> 📌 **Recuperabilidad — documento dueño: `ARQ-R-006` (LIN-ARQ-001 §5.4).** Los valores no se inventan por proyecto: se derivan de la **banda de criticidad** asignada al sistema (`§5.4.1`). Este atributo debe declarar: la criticidad asignada; el RTO y RPO comprometidos y **quién los validó por el área usuaria**; la verificación de que ninguna dependencia tiene un RTO/RPO peor que el declarado (regla 2 de `§5.4.1`); y, para criticidad **Alta o Media**, el procedimiento de recuperación de `§5.4.3`.
>
> **Un documento de arquitectura de criticidad Alta no puede aprobarse sin estos elementos.**

---
---

# ANEXO D: RIESGOS Y DEUDA TÉCNICA

Este anexo registra los riesgos arquitectónicos identificados y la deuda técnica conocida. Su propósito es hacer explícitas las decisiones de compromiso tomadas durante el diseño y establecer un plan de seguimiento.

> 📋 **Orientación para el arquitecto — Anexo D completo**
>
> Este anexo tiene dos partes con propósitos distintos pero complementarios.
>
> **Riesgos:** son situaciones que podrían ocurrir y afectar negativamente la arquitectura o el proyecto. Se registran para que el equipo los tenga presentes y pueda mitigarlos proactivamente. Un riesgo arquitectónico es diferente de un riesgo de proyecto: no es "el proveedor puede no entregar a tiempo" sino "si la latencia del servicio de RENIEC supera X ms, el proceso de afiliación fallará y no hay mecanismo de reintento".
>
> **Deuda técnica:** son decisiones de compromiso que se tomaron conscientemente por razones de tiempo, recursos o información incompleta, sabiendo que no son la solución ideal a largo plazo. Registrarla es importante para que no se pierda el conocimiento de que existe y que en algún momento debe resolverse.
>
> **Diferencia clave entre riesgo y deuda técnica:**
> - El **riesgo** es algo que puede pasar y que debemos evitar o mitigar.
> - La **deuda técnica** es algo que ya pasó (una decisión subóptima que ya tomamos) y que debemos resolver en el futuro.
>
> Una vez completado este anexo, elimina este bloque de orientación.

---

## D.1 Riesgos arquitectónicos

> 📋 **Orientación para el arquitecto**
>
> Lista los riesgos que identificaste durante el diseño. Para cada riesgo, evalúa su probabilidad de ocurrencia y su impacto en el sistema si ocurre, y propón una acción de mitigación concreta. La mitigación no tiene que eliminar el riesgo por completo; puede ser una acción para reducir su probabilidad o su impacto.
>
> **Fuentes comunes de riesgos en proyectos ONP:**
> - Dependencia de servicios de entidades externas que no están bajo control de la ONP (RENIEC, SBS, SUNAT)
> - Disponibilidad y capacidad de la infraestructura institucional
> - Integración con sistemas legados cuya documentación es incompleta
> - Cambios de alcance en requisitos que impacten la arquitectura definida

| ID | Descripción del Riesgo | Probabilidad | Impacto | Mitigación |
|---|---|---|---|---|
| R-001 | [Descripción concreta del riesgo: qué podría ocurrir y cuál sería su consecuencia arquitectónica] | Alta / Media / Baja | Alto / Medio / Bajo | [Acción concreta para reducir la probabilidad o el impacto] |
| R-002 | [Descripción concreta del riesgo] | Alta / Media / Baja | Alto / Medio / Bajo | [Acción de mitigación] |

---

## D.2 Deuda técnica conocida

> 📋 **Orientación para el arquitecto**
>
> Lista las decisiones subóptimas que se tomaron conscientemente. Para cada una indica qué se hizo, por qué no se hizo de la forma ideal (tiempo, información incompleta, dependencia de otro equipo) y cuándo o cómo se planea resolver.
>
> **MANDATO INSTITUCIONAL DE DEUDA TÉCNICA CERO (`ARQ-R-002` (LIN-ARQ-001 §2.3) / `LIN-DEV-JAVA-001 §16.6`):**
> Toda deuda técnica admitida por compromisos de cronograma o dependencias externas debe:
> 1. Estar asociada obligatoriamente a un **Ticket de Refactorización registrado en el Backlog** oficial de GitLab / Jira del proyecto.
> 2. Contar con una **estrategia de mitigación de bajo riesgo**, como el uso de **Feature Toggles** (`ARQ-R-002` (LIN-ARQ-001 §2.3) y `ADR-014` — Unleash; cuatro categorías, de las cuales solo *Release* y *Experiment* caducan obligatoriamente) para encender/apagar el comportamiento temporal sin re-despliegues complejos.
> 3. Tener un **horizonte de remediación acotado en Sprints** (prioridad alta/media) pactado formalmente antes de obtener la conformidad de paso a Producción.

| ID | Descripción | Prioridad | Plan de resolución (y Ticket en Backlog) |
|---|---|---|---|
| DT-001 | [Qué se hizo de forma subóptima, por qué se tomó esa decisión y cuál es el impacto de no resolverlo] | Alta / Media / Baja | [Acción concreta, Ticket en Jira/GitLab (`ONP-XXXX`) y horizonte de tiempo en Sprints para resolverlo] |
| DT-002 | [Descripción de la deuda técnica] | Alta / Media / Baja | [Plan de resolución con Ticket e Hito de remediación] |

---
---

# ANEXO E: CONFORMIDAD Y CRITERIOS DE APROBACIÓN

Este anexo es la lista de verificación del **revisor y del aprobador**, no del autor. Su propósito es que la aprobación de un documento de arquitectura dependa de criterios verificables y no del criterio individual de quien firma.

> 📋 **Cómo se usa**
>
> El arquitecto lo completa antes de someter el documento a revisión; el revisor lo verifica; el aprobador exige que esté completo antes de firmar. **A diferencia de los demás bloques de orientación, este anexo NO se elimina**: forma parte del entregable y queda como evidencia de la revisión.
>
> Un ítem no aplicable se marca `N/A` **con la razón** — nunca se deja en blanco ni se borra la fila.

## E.1 Completitud del documento

| # | Criterio | Estado |
|---|---|---|
| 1 | Todos los bloques de orientación 📋 fueron eliminados del documento final (salvo este anexo) | ☐ |
| 2 | No quedan *placeholders* sin reemplazar (`[Nombre del Sistema]`, `[DD/MM/AAAA]`, `AD-00X`) | ☐ |
| 3 | El bloque de identidad de la plantilla fue eliminado y la tabla de identidad del documento está completa | ☐ |
| 4 | El glosario define todas las siglas que aparecen en el documento | ☐ |
| 5 | El alcance declara explícitamente qué queda **fuera**, no solo qué queda dentro | ☐ |

## E.2 Declaraciones obligatorias (`§1.4`)

| # | Criterio | Fundamento | Estado |
|---|---|---|---|
| 6 | **Estadio de topología** declarado | `ARQ-R-001` (LIN-ARQ-001 §2.1) | ☐ |
| 7 | Si declara Estadio 3: los **6 criterios de extracción** se sustentan uno a uno en un ADR | `ARQ-R-001` (LIN-ARQ-001 §2.1) | ☐ |
| 8 | **Adopción de DDD** declarada, evaluada de forma independiente de la anterior | `LIN-DIS-001 §3.0` | ☐ |
| 9 | **Declaración CAP** (CP/AP) presente y sustentada, o justificado que el sistema no es distribuido | `LIN-ARQ-001 §3.1` | ☐ |
| 10 | **Criticidad** asignada, con RTO y RPO de su banda y **nombre de quien los validó por el área usuaria** | `ARQ-R-006` (LIN-ARQ-001 §5.4) | ☐ |
| 11 | Verificado que **ninguna dependencia tiene un RTO/RPO peor** que el declarado | `LIN-ARQ-001 §5.4.1` regla 2 | ☐ |
| 12 | Si la criticidad es **Alta o Media**: procedimiento de recuperación con orden de dependencias | `LIN-ARQ-001 §5.4.3` | ☐ |

## E.3 Conformidad normativa

| # | Criterio | Fundamento | Estado |
|---|---|---|---|
| 13 | `§5.2` indica, para **cada** documento del corpus, si aplica o por qué no | — | ☐ |
| 13b | **Línea base normativa** registrada en la tabla de identidad (versión de `GOB-MAT-001` y fecha) y **próxima revisión** fijada a 12 meses o menos | `GOB-PLA-001 §1.5` | ☐ |
| 13c | Si la criticidad es **Alta o Media**: las vistas del Anexo A incluyen **todas** las dependencias externas, incluidas las de baja frecuencia (batches, integraciones periódicas), porque serán contrastadas contra el grafo observado | `LIN-ARQ-001 §5.5` | ☐ |
| 14 | Toda desviación de un lineamiento está registrada como **`EXC-<CÓDIGO>-NNN`** con riesgo aceptado, control compensatorio y **fecha de revisión** | `GOB-MAT-001` | ☐ |
| 15 | Ningún `AD-NNN` de este documento pretende dispensar del cumplimiento de un lineamiento institucional | `GOB-MAT-001` | ☐ |
| 16 | Ningún criterio de aceptación del proyecto se apoya en un documento que **no esté `Vigente`** | `GOB-MAT-001`, regla de exigibilidad | ☐ |
| 17 | La **Declaración de Conformidad** existe y está firmada en el `README.md` del repositorio | `ARQ-R-008` (LIN-ARQ-001 §8.3); verificada por `LIN-CICD-001 §12.5` | ☐ |

## E.4 Consistencia interna

| # | Criterio | Estado |
|---|---|---|
| 18 | **`§3` no contiene ningún elemento ausente del Anexo A.** La verificación es en un solo sentido: el Anexo A puede tener detalle que `§3` no narre, pero nunca al revés (regla de precedencia de `§3`) | ☐ |
| 19 | Todo ADR listado en `B.1` tiene su ficha completa en `B.2`, y ninguna ficha existe sin estar en el índice | ☐ |
| 20 | Cada atributo de calidad de `C.1` referencia el `AD-NNN` que lo aborda | ☐ |
| 21 | Cada ADR tiene al menos **dos alternativas reales** evaluadas | ☐ |
| 22 | Toda deuda técnica de `D.2` tiene ticket en el backlog y horizonte de remediación acotado | `ARQ-R-002` (LIN-ARQ-001 §2.3) | ☐ |
| 23 | Los riesgos de `D.1` son **arquitectónicos**, no de gestión de proyecto | ☐ |

## E.5 Registro de la revisión

| Campo | Valor |
|---|---|
| **Revisado por** | [Nombre, área, fecha] |
| **Ítems marcados `N/A`** | [Número de ítem y razón, uno por línea] |
| **Observaciones que no bloquean** | [Lista, o «ninguna»] |
| **Resultado** | ☐ Aprobado ☐ Aprobado con observaciones ☐ Devuelto |

> **Regla de aprobación.** Un documento de arquitectura de un sistema de criticidad **Alta** no puede aprobarse con ítems de `E.2` o `E.3` sin marcar. Para criticidad Media y Baja, un ítem pendiente puede admitirse como observación con fecha de subsanación, salvo los ítems **9, 14 y 17**, que bloquean en toda criticidad — el primero porque una decisión CAP no declarada se descubre en producción, y los otros dos porque son verificados por el pipeline.

---

*Documento elaborado por la Oficina de Tecnologías de la Información — ONP*
