# DOCUMENTO DE ARQUITECTURA DE TI

**Oficina:** Oficina de Tecnologías de la Información (OTI)
**Documento / Entregable:** Documento de Arquitectura de TI
**Proyecto / Aplicación:** [YYYYYY]
**Fecha:** [XX/XX/AAAA]

## TABLA DE CONTENIDO

1. [ALCANCE DEL DOCUMENTO](#1-alcance-del-documento)
   1.1 Contexto · 1.2 Objetivo · 1.3 Alcance del sistema · 1.4 Audiencia objetivo
2. [GLOSARIO TÉCNICO](#2-glosario-técnico)
3. [DIAGRAMA DE ARQUITECTURA DE TI](#3-diagrama-de-arquitectura-de-ti)
   A. Capa de usuarios · B. Capa de seguridad · C. Capa de aplicaciones · D. Capa de servicios · E. Capa de datos
4. [SUPUESTOS Y RESTRICCIONES](#4-supuestos-y-restricciones)
   4.1 Supuestos · 4.2 Restricciones
5. [DOCUMENTOS ADJUNTOS](#5-documentos-adjuntos)

**ANEXOS**
- [ANEXO A: VISTAS DE ARQUITECTURA](#anexo-a-vistas-de-arquitectura) — A.1 Contexto · A.2 Aplicación · A.3 Componentes · A.4 Integraciones · A.5 Infraestructura
- [ANEXO B: MATRIZ DE TRAZABILIDAD ARQUITECTÓNICA](#anexo-b-matriz-de-trazabilidad-arquitectónica)
- [ANEXO C: DECISIONES ARQUITECTÓNICAS (ADRs)](#anexo-c-decisiones-arquitectónicas-adrs) — C.1 Resumen de decisiones · C.2 Detalle de decisiones
- [ANEXO D: ATRIBUTOS DE CALIDAD](#anexo-d-atributos-de-calidad) — D.1 Atributos de calidad y su cobertura arquitectónica
- [ANEXO E: RIESGOS, DEUDA TÉCNICA Y OPORTUNIDADES DE MEJORA](#anexo-e-riesgos-deuda-técnica-y-oportunidades-de-mejora) — E.1 Riesgos · E.2 Deuda técnica · E.3 Oportunidades de mejora · E.4 Registro de excepciones

---

## HISTORIAL DE CAMBIOS

| Ítem | Versión | Fecha | Descripción |
|---|---|---|---|
| 1 | X.X.X | DD/MM/YYYY |   |
| 2 | 1.3 | 08/09/2026 | Se incorpora la Vista de Integraciones (Anexo A.4); se corrige la numeración de anexos (C: ADRs, D: Atributos de Calidad, E: Riesgos, Deuda Técnica y Oportunidades de Mejora); se añaden las secciones E.3 Oportunidades de mejora arquitectónica y E.4 Registro de excepciones; se homogeniza el nivel de encabezados del documento. |

## 1. ALCANCE DEL DOCUMENTO

> 📋 **Orientación para el arquitecto**

*Describe los límites del diseño arquitectónico que se está entregando en este documento.*

*Una vez completada esta sección, elimina este bloque de orientación.*

*Ejemplo:*

El presente documento describe la arquitectura de TI propuesta para la PAST**, **correspondiente únicamente al Módulo 1 (Traslado), el cual será construido durante el Sprint 1 y 2 de la fase de desarrollo del proyecto. Está dirigido a diferentes audiencias con distintos niveles de detalle, facilitando la comprensión del sistema desde una perspectiva estratégica hasta una perspectiva técnica.

Fuera del alcance:

### 1.1 Contexto

> 📋 **Orientación para el arquitecto**
> Describe para qué existe el sistema, que procesos soporta y qué valor aporta a la ONP y a sus usuarios finales, en lenguaje comprensible para cualquier audiencia.
> Una vez completada esta sección, elimina este bloque de orientación.

*Ejemplo:*

La PAST será la plataforma digital mediante la cual la ONP atenderá los procesos de elección, afiliación y traslado entre el Sistema Nacional de Pensiones (SNP) y el Sistema Privado de Pensiones (SPP), en el marco de la Ley N.° 32123 “Ley de Modernización del Sistema Previsional Peruano” y su Reglamento. La plataforma permitirá que el ciudadano gestione su traslado de forma autónoma desde Internet y que las Empresas Administradoras de Fondos (EAF) registren traslados en representación de sus afiliados, individualmente o por lotes. Su valor para la institución es doble, puesto que reducirá el tiempo y el error operativo de un trámite que hoy exige la intervención coordinada de más de diez entidades del Estado, y dejará trazabilidad inmutable de cada paso, lo que será el sustento probatorio de un acto administrativo con efectos previsionales sobre el ciudadano.

### 1.2 Objetivo

> 📋 **Orientación para el arquitecto**
> Describe qué cubre este documento: qué decisiones arquitectónicas contiene, a qué versión del sistema corresponde y cuál es su propósito como entregable institucional.
> **Error frecuente:** copiar el objetivo del documento de análisis. El objetivo aquí no es el del sistema en sí, sino el propósito de este documento de arquitectura como entregable.
> **Lo que NO va aquí:** requisitos funcionales, casos de uso, reglas de negocio, ni descripción de procesos. Si te encuentras escribiendo "el sistema permitirá que el usuario pueda...", estás en el documento equivocado.

*Una vez completada esta sección, elimina este bloque de orientación.*

> Ejemplo:
> El propósito de este documento es formalizar la línea base arquitectónica para la versión 1.4.2 del sistema PAST, consolidando las decisiones clave de diseño basadas en microservicios, eventos, persistencia en caché  y despliegue automatizado en un orquestador de contenedores On-Premise. Como entregable institucional, este artefacto actúa como el contrato técnico oficial que valida el cumplimiento de los estándares tecnológicos exigidos por la institución para la transición del proyecto hacia la fase de certificación.
> Este documento cumple ese propósito mediante:

- La representación de los componentes del sistema y sus relaciones (Anexo A).
- La trazabilidad entre requerimientos funcionales/no funcionales y los elementos de arquitectura que les dan soporte (Anexo B).
- El registro formal de las decisiones arquitectónicas adoptadas y su alineación normativa (Anexo C).
- La cobertura de los atributos de calidad exigidos al sistema (Anexo D).
- Los riesgos arquitectónicos, la deuda técnica conocida, las oportunidades de mejora y las excepciones identificadas sobre la arquitectura (Anexo E).

### 1.3 Alcance del sistema

> 📋 **Orientación para el arquitecto**
> Esta sección es crítica porque define los límites del sistema. Lo que está dentro del alcance es lo que el equipo debe construir. Lo que está fuera es igualmente importante: evita malentendidos sobre qué no es responsabilidad de este sistema.
> **Dentro del alcance** debe listar todos los módulos o componentes del sistema, así  como las integraciones que el sistema debe implementar (aunque el otro extremo sea responsabilidad de otra entidad).
> **Fuera del alcance** debe listar explícitamente los sistemas adyacentes que no son parte de este sistema, aunque se integran con él, las funcionalidades que se postergaron para fases futuras, y los aspectos de diseño e implementación que se documentan en otros artefactos.
> **Pregunta de validación:** ¿Un revisor externo podría determinar con claridad si un componente o funcionalidad específica está o no está en el alcance de este documento? Si hay ambigüedad, necesitas ser más preciso.

*Una vez completada esta sección, elimina este bloque de orientación.*

*Ejemplo:*

El alcance funcional detallado del sistema se encuentra formalizado en el Documento de Alcance del Proyecto (Código: ONP-PAST-SRS-v1.0), por ello en esta sección el alcance se presenta de forma resumida.

**Dentro del alcance:**

- Implementación de los módulos de Elección, Afiliación y Traslado.
- Integraciones con los sistemas internos NSTD, PUA, Clave Virtual, VIC, SIGA, NSP, SISREC y NOE.
- Integraciones con las entidades externas MEF, RREE, MTPE, MININTER, MINDEF, SBS, SINADEF,MIGRACIONES, RENIEC y SUNAT.
- Desde la perspectiva arquitectónica, el sistema se delimita mediante el Diagrama de Contexto (Ver Anexo A), el cual define las fronteras técnicas del sistema, los usuarios que interactúan con ella , así como los sistemas internos y externos con los que se comunicará.
**Fuera del alcance:**

- Implementación de las Apis que deben ser expuestas por las aplicaciones internas y entidades externas.
- Las Apis de los sistemas internos serán implementadas mediante requerimientos de mantenimiento.
- Las APIs de las entidades externas serán provistas por las entidades externas mediante convenios.
- La Reportería Analítica ha sido postergada para una fase posterior del proyecto. Desde la perspectiva de TI, esto implica posponer la siguiente implementación arquitectónica:
- Clúster de Base de Datos de Lectura (Read Replicas): La arquitectura de datos actual soporta un Redis de infraestructura para caché transaccional, pero pospone la implementación de réplicas de lectura y herramientas de BI (Business Intelligence) para la fase analítica futura.
- Con el fin de evitar la duplicidad de información técnica, los siguientes componentes de diseño detallado quedan fuera de este documento y se delegan a sus respectivos repositorios:
- Modelos y diccionarios de datos de los sistemas internos.
- Contratos de las APIs ya existentes de RENIEC y Migraciones.

### 1.4 Audiencia objetivo

> 📋 **Orientación para el arquitecto**
> Esta tabla orienta a cada lector hacia las secciones que le son más relevantes. Ajústala según las audiencias reales del proyecto. Si el sistema no tiene usuarios internos, elimina esa fila. Si hay un equipo de seguridad que revisa formalmente, agrégalo. El propósito es que cada persona que reciba el documento sepa exactamente dónde encontrar lo que necesita sin tener que leerlo completo.

*Una vez completada esta sección, elimina este bloque de orientación.*

| Audiencia | Propósito | Secciones de interés |
|---|---|---|
| Gerencia / Stakeholders | Comprensión del valor y alcance del sistema | Sección 1; Anexo A – A.1 Vista de Contexto |
| Equipo de Plataforma / Seguridad | Comprensión de los componentes y su despliegue | Secciones 3, 4; Anexo A – A.2 Vista de Aplicación y A.5 Vista de Infraestructura |
| Desarrolladores | Comprensión de los componentes internos y decisiones técnicas | Secciones 3, 4; Anexo A – A.3 Vista de Componentes y A.4 Vista de Integraciones; Anexo C – ADRs |
| Equipo de Soporte | Comprensión del entorno de ejecución | Sección 3; Anexo A – A.5 Vista de Infraestructura |
| Arquitectura OTI | Validación de la alineación normativa, cobertura de requerimientos y decisiones pendientes de aprobación | Anexo B – Matriz de Trazabilidad; Anexo C – ADRs; Anexo E |
| Equipo de Calidad / Pruebas | Planificación de pruebas y análisis de impacto de cambios funcionales sobre la arquitectura | Anexo B – Matriz de Trazabilidad |

## 2. GLOSARIO TÉCNICO

> 📋 **Orientación para el arquitecto**
> El glosario tiene un propósito concreto: eliminar la ambigüedad en la lectura del documento. No es un diccionario enciclopédico ni una lista de definiciones genéricas copiadas de internet. Cada término debe estar definido en el contexto específico del sistema que se documenta.
> **Criterio para incluir un término:** incluye el término si es mencionado en el documento y un lector de otra área (negocio, soporte, seguridad) podría interpretarlo de forma distinta a como se usa en este documento. Si el significado es obvio para todas las audiencias, no lo incluyas.
> **Qué debe incluir obligatoriamente:**
> - Todas las siglas que aparecen en el documento, tanto institucionales (SAA, NSP, OTI) como del proyecto (PAST, EAF) y tecnológicas (WAF, JWT, REST).
> - Términos de negocio propios del dominio de la ONP que pueden no ser conocidos por el equipo técnico.
> - Términos técnicos que pueden ser conocidos por el equipo técnico, pero no por los stakeholders de negocio.
> **Lo que NO debe incluir:**
> - Definiciones genéricas de términos universalmente conocidos como "base de datos", "servidor" o "usuario".
> - Definiciones copiadas textualmente de Wikipedia o manuales sin adaptación al contexto
> **Consejo práctico:** elabora el glosario al final, una vez que el documento esté completo. Revisa cada sección e identifica los términos que pueden generar confusión. Ordena alfabéticamente los términos para facilitar la consulta.
> Una vez completada esta sección, elimina este bloque de orientación.

| # | Término | Descripción |
|---|---|---|
| 1 | [Sigla o término] | [Definición clara y concisa en el contexto del sistema] |
| 2 | [Sigla o término] | [Definición clara y concisa en el contexto del sistema] |
| 3 | [Sigla o término] | [Definición clara y concisa en el contexto del sistema] |

## 3. DIAGRAMA DE ARQUITECTURA DE TI

A continuación, se presenta la Vista General de la arquitectura de TI del **[Nombre del Sistema]**, que muestra los principales componentes del sistema, sus relaciones, las integraciones con sistemas internos y externos, y los orígenes de datos.

> 📋 **Orientación para el arquitecto**
> Esta sección contiene la vista de referencia rápida del sistema, pensada para ser leída junto al diagrama. Es la evolución del diagrama que el equipo ya conoce, pero con criterios más claros sobre qué nivel de detalle corresponde aquí y qué va en los Anexos.
> **Regla fundamental de esta sección:** describe QUÉ es cada componente y PARA QUÉ sirve. No describas CÓMO funciona internamente ni CÓMO se configura. Si te encuentras escribiendo puertos, URLs, campos de datos, parámetros de configuración o pasos de un proceso, ese contenido no pertenece aquí.
> **Sobre el diagrama:** usa el mismo diagrama por capas que el equipo ya maneja. Asegúrate de incluir una leyenda de colores que diferencie visualmente los componentes nuevos a implementar de los componentes existentes. El diagrama debe ser auto explicativo junto con la narrativa de esta sección.
> **Nota:** Esta es una vista de referencia rápida. Para vistas con distintos niveles de abstracción, consultar el **Anexo A**.

*[Insertar Diagrama de Arquitectura de TI — Ilustración 01]*

*Una vez completada esta sección, elimina este bloque de orientación.*

*Ejemplo:*

**Ilustración 01 — Vista General de Arquitectura de TI**

### A. CAPA DE USUARIOS

> 📋 **Orientación para el arquitecto**
> Describe quiénes son los actores que usan el sistema. Diferencia entre usuario interno (accede desde la red de la ONP) y usuario externo (accede desde internet). Si el sistema solo tiene un tipo de usuario, elimina la subdivisión. Para cada tipo de usuario indica: quién es en términos de rol o perfil, y cómo accede al sistema (canal web, aplicación móvil, servicio automatizado). No describas los permisos ni los flujos de proceso.

*Una vez completada esta sección, elimina este bloque de orientación.*

*Ejemplo:*

Como se puede visualizar en la ilustración 01, el Módulo de Notificaciones Electrónicas será utilizado por dos (2) tipos de usuarios, los cuales accederán a través de un navegador web.

### B. CAPA DE SEGURIDAD

> 📋 **Orientación para el arquitecto**
> Describe los componentes de seguridad que protegen el acceso al sistema. Agrupa en seguridad interna (para acceso desde la red ONP) y seguridad externa (para acceso desde internet) si aplican ambas. Para cada componente indica: qué es, si es existente o nuevo, y cuál es su función de seguridad específica en este sistema. No describas configuraciones, reglas de firewall, políticas de contraseñas ni detalles de implementación de tokens.
> **Componentes típicos a considerar:** WAF, protección anti-bots, autenticación de terceros, SAA/token institucional, VPN para integraciones con otras entidades.

*Una vez completada esta sección, elimina este bloque de orientación.*

[Describir los componentes de seguridad diferenciando seguridad interna y externa.]

- **Seguridad interna:** [Componentes y su función de seguridad]
- **Seguridad externa:** [Componentes y su función de seguridad]

### C. CAPA DE APLICACIONES

> 📋 **Orientación para el arquitecto**
> Describe los componentes de aplicación del sistema.
> - **Estilo arquitectónico seleccionado:** Declara explícitamente si el sistema adopta Monolito Modular o Arquitectura Hexagonal / Limpia o Capas Clásica (solo mantenimiento/legacy).
> - **Frontend:** Especifica la ingeniería del lado del cliente. Indica el tipo de aplicación (SPA, MPA, aplicación móvil), el enfoque (monolito modular o Microfrontends), como consumir las APIs del backend, como gestionar los tokens, uso de interceptores, compatibilidad de navegadores, accesibilidad, diseño responsivo; esto es, especificar los componentes de ingeniería de la capa de presentación que interactuará con el usuario. NO describas el diseño visual (mockups o colores).
> - **Backend / Contextos Delimitados (Bounded Contexts):** Especifica la ingeniería del lado del servidor. Lista cada servicio o módulo táctico de dominio con su nombre oficial y responsabilidad principal en una línea. Si el sistema expone un **API Gateway**, **BFF (Backend for Frontend)** o **Facade Arquitectónico**, descríbelo como punto de entrada perimetral.
> - **Integraciones con Legados:** Si el Backend interactúa con sistemas core legacy de la institución (Estadio 1), declara si implementa el patrón **Anti-Corruption Layer (ACL)** o **Strangler Fig**.
> **Lo que NO va aquí:** protocolos de comunicación en detalle, contratos de API, formatos de mensajes, lógica interna de métodos.

*Una vez completada esta sección, elimina este bloque de orientación.*

[Describir el estilo arquitectónico seleccionado, los componentes del Frontend y Backend con sus responsabilidades y Bounded Contexts.]

- **Estilo Arquitectónico:** [Monolito Modular / Arquitectura Hexagonal / Capas Clásica]
- **Frontend:** [Tipo de aplicación, responsabilidad principal y estrategia de despliegue]
- **Backend (Bounded Contexts / Servicios):** [Lista de módulos o microservicios con su responsabilidad de dominio]
- **Perímetro de Exposición:** [API Gateway / BFF / Facade que gestiona la entrada al sistema]
- **Integraciones internas (y ACL si aplica):** [Sistemas de la ONP con los que interactúa y dirección de datos]
- **Integraciones externas:** [Servicio de fachada de Entidades Externas y entidades que encapsula]

### D. CAPA DE SERVICIOS

> 📋 **Orientación para el arquitecto**
> Lista los servicios transversales que el sistema creará o reutilizará y que son o pueden ser compartidos con otros sistemas de la ONP. Para cada uno indica: nombre del servicio, si es existente o nuevo a implementar, su entorno de ejecución (tipo de servidor) y cuál es su función dentro de este sistema específicamente.

*Una vez completada esta sección, elimina este bloque de orientación.*

> **Servicios transversales obligatorios o comunes en la ONP:**
> - **Observabilidad y Logs:** Centralización de logs estructurados con trace_id y telemetría (OpenTelemetry / Elastic ECS).
> - **Seguridad y Autenticación:** SAA / Token institucional para APIs internas, WAF y VPN para externos.
> - **Comunes y Notificaciones:** Gestor documental, servicio SMTP de correos, servicio de SMS.
> - **Patrones de Integración Asíncrona (si aplica):** Eventos de dominio vía **CloudEvents over Apache Kafka** o conectores **CDC Debezium / Transactional Outbox**.

- **Servicios transversales a implementar:** [Nombre y función específica en este sistema]
- **Servicios transversales existentes a consumir:** [Nombre y función específica en este sistema]

### E. CAPA DE DATOS

> 📋 **Orientación para el arquitecto**
> Lista todas las fuentes de datos (Bases de Datos, Archivos planos, etc) que el sistema usa, tanto las propias como las de sistemas con los que se integra. Para cada una indica su esquema/origen y también el tipo de acceso usando la convención CRUD. Incluir las fuentes de datos de los sistemas legados es importante porque permite evaluar el impacto de cambios futuros.
> **Convención de acceso:** C = Creación, R = Lectura, U = Actualización, D = Eliminación.
> **No incluyas:** estructura de tablas, modelos de datos, diccionarios de datos. Eso pertenece a la documentación técnica de base de datos.

| Fuente de datos | Tipo | Nombre | Origen | Acceso |
|---|---|---|---|---|
| [Nombre de la fuente de datos] | [Oracle 19c / PostgreSQL / etc.] | [Nombre BD propia] | [Este sistema] | CRU |
| [Nombre de la fuente de datos del legado] | [Oracle 11g / etc.] | [Nombre BD legado] | [Sistema legado] | R |
| [Nombre de la fuente] | [texto, json] | [Nombre del archivo] |   | R |

Ejemplo:

| Fuente de datos | Tipo | Nombre | Origen | Acceso |
|---|---|---|---|---|
| Base de Datos | Oracle 19c | BDPRDG2 | PAST | CRUD |
| Base de Datos | Oracle 11g | NSP18 | NSP | R |
| Archivo | Texto | fallecidos.csv | RENIEC | R |

## 4. SUPUESTOS Y RESTRICCIONES

> 📋 **Orientación para el arquitecto**
> Esta es la sección más descuidada en documentos de arquitectura y una de las más valiosas. Actúa como un mecanismo de salvaguarda técnica ante modificaciones en el alcance del proyecto, dejando constancia formal de los criterios y restricciones que justificaron la arquitectura propuesta. Si en el futuro algo cambia y la arquitectura debe revisarse, esta sección es la que explica por qué se diseñó de esa manera.
> **Diferencia clave entre supuesto y restricción:**
> - Un **supuesto** es algo que se asume como verdadero porque no se tiene certeza en el momento del diseño. Si el supuesto resulta falso, la arquitectura puede necesitar revisarse. Ejemplo: "Se asume que el servicio de autenticación de la SBS estará disponible antes del inicio del desarrollo."
> - Una **restricción** es algo que está dado y no puede cambiarse. No es una decisión del arquitecto, es una condición externa o institucional. Ejemplo: "El sistema debe desplegarse en la infraestructura existente de la ONP."
> Una vez completada esta sección, elimina este bloque de orientación.

### 4.1 Supuestos

> 📋 **Orientación para el arquitecto**
> Lista los supuestos que sustentan las decisiones arquitectónicas. Para cada supuesto, indica qué se está asumiendo y cuál sería el impacto en la arquitectura si ese supuesto resulta incorrecto. Esto es especialmente importante en proyectos con integraciones externas cuya disponibilidad o especificación aún no está confirmada.
> **Categorías comunes de supuestos en proyectos ONP:**
> - Disponibilidad de servicios que deben ser provistos por entidades externas (RENIEC, SBS, SUNAT).
> - Información de infraestructura que aún no ha sido confirmada por OTI.UI.T
> - Requisitos funcionales y no funcionales que están en proceso de definición.
> - Capacidad técnica de los equipos para consumir y exponer los servicios de integración.
> **Error frecuente:** dejar esta sección con una sola frase genérica como "el documento se elaboró con la información disponible a la fecha". Eso no es un supuesto, es una advertencia. Los supuestos deben ser específicos y trazables.

*Una vez completada esta sección, elimina este bloque de orientación.*

La arquitectura propuesta en el presente documento ha sido diseñada en base a la información disponible a la fecha de su elaboración. Los supuestos considerados son los siguientes:

- [Supuesto 1: qué se asume como verdadero e impacto si resulta incorrecto]
- [Supuesto 2: qué se asume como verdadero e impacto si resulta incorrecto]
Cualquier modificación posterior en los requisitos funcionales o no funcionales del sistema podrá requerir la revisión y actualización del presente documento.

### 4.2 Restricciones

> 📋 **Orientación para el arquitecto**
> Lista las restricciones que condicionaron las decisiones arquitectónicas. Organízalas por categoría para facilitar su lectura. Recuerda que una restricción es una condición dada, no una decisión: no escribas "se decidió usar Oracle" sino "el sistema debe usar la infraestructura de base de datos Oracle institucional existente".
> **Categorías:**
> - **Técnicas:** tecnologías obligatorias, estándares aprobados institucionalmente, plataformas existentes que deben usarse.
> - **Normativas:** leyes, directivas del MEF, políticas de seguridad de la OTI, Ley de Protección de Datos Personales.
> - **Operativas:** plazos del proyecto, disponibilidad de ambientes, capacidad de infraestructura, recursos humanos disponibles.

*Una vez completada esta sección, elimina este bloque de orientación.*

- **Restricciones técnicas:** [Tecnologías o estándares de uso obligatorio, plataformas existentes que deben usarse]
- **Restricciones normativas:** [Marcos legales, directivas institucionales, políticas de seguridad aplicables]
- **Restricciones operativas:** [Plazos, disponibilidad de ambientes, recursos]

## 5. DOCUMENTOS ADJUNTOS

> 📋 **Orientación para el arquitecto**

Lista todos los documentos que fueron revisados y que sirvieron de base para realizar el diseño de la arquitectura (Project chárter, documento de alcance, documento de análisis, manuales, etc.).

*Una vez completada esta sección, elimina este bloque de orientación.*

- [Documento de Alcance — Nombre y versión]
- [Documento de Análisis — Nombre y versión]

## ANEXOS

## ANEXO A: VISTAS DE ARQUITECTURA

Este anexo presenta la arquitectura de [Nombre del Sistema] mediante cinco vistas modeladas en C4, cada una orientada a una audiencia y propósito específico. El uso de múltiples vistas permite comunicar la arquitectura de forma apropiada según el nivel de abstracción requerido.

> Nota: si este documento describe una arquitectura ya implementada, cada vista puede indicarlo en su título (ej. "A.3 Vista de Componentes - Arquitectura As-Built") y la narrativa debe declarar la fecha de corte de la verificación contra el código fuente.

### A.1 Vista de Contexto

**Tipo de vista:** Vista de Motivación / Contexto

**Audiencia:** Gerencia, stakeholders, cualquier persona interesada en comprender el rol del sistema en la organización.

#### Propósito

Mostrar el sistema como una unidad y su relación con los actores externos (usuarios, sistemas y entidades) que interactúan con él. No expone detalle interno. Responde a la pregunta: **¿qué es el sistema y con quién se relaciona?**

> 📋 **Orientación para el arquitecto**
> **¿Qué debes modelar?**
> Esta es la vista más simple y la primera que debe elaborarse. El sistema completo se representa como un único bloque (Application Component). Alrededor de él se ubican todos los actores que interactúan con él: tipos de usuarios (ciudadanos, funcionarios, representantes de empresas) y sistemas externos (otras entidades del Estado, servicios de terceros). Las relaciones deben ser simples, mostrando solo que existe una interacción, no cómo funciona técnicamente.
> **Elementos que debes incluir:**
> - El sistema como un solo bloque con su nombre oficial
> - Los tipos de usuario que lo usan (no personas específicas, sino roles)
> - Los sistemas externos e internos con los que se integra, declarando explícitamente a qué estadio de la topología institucional pertenecen (Estadio 1 Monolito Tradicional/Legacy, Estadio 2 Monolito Modular o Estadio 3 Microservicios Selectivos)
> - Una relación por cada interacción relevante, con una etiqueta que indique qué hace (ej. "consulta datos", "recibe notificación")
> **Lo que NO debe aparecer en esta vista:**
> - Componentes internos del sistema (frontend, backend, base de datos)
> - Protocolos de comunicación ni tecnologías específicas
> - Flujos de datos o secuencias de pasos
> - Más de dos niveles de detalle
> **Pregunta de validación antes de cerrar la vista:** ¿Podría un director sin conocimiento técnico entender quiénes usan el sistema y con qué otros sistemas existen interacciones? Si la respuesta es sí, la vista está bien.

*Una vez completada esta sección, elimina este bloque de orientación.*

*Ejemplo:*

**Ilustración A.1 — Vista de Contexto**

#### Narrativa

[Describir en prosa quiénes son los actores que interactúan con el sistema, qué rol cumple cada uno y cuál es la naturaleza de esa relación. No describir cómo funciona internamente el sistema.]

*Una vez completada esta sección, elimina este bloque de orientación.*

*Ejemplo:*

El sistema es accedido por dos tipos de usuarios externos: el Ciudadano, que utiliza la plataforma para realizar sus trámites de manera autónoma, y el Representante de la EAF, que actúa en nombre de una empresa administradora de fondos. A su vez, el sistema se integra con entidades del Estado como RENIEC para la validación de identidad y con la SBS para la autenticación de representantes institucionales.

### A.2 Vista de Aplicación

**Tipo de vista:** Vista de Capa de Aplicación con elementos de Tecnología

**Audiencia:** Equipos de plataforma, seguridad, DevOps y arquitectos.

#### Propósito

Mostrar los principales componentes de aplicación que conforman el sistema, sus interfaces, sus dependencias internas y sus integraciones con sistemas externos. Responde a la pregunta: **¿cuáles son los componentes del sistema y cómo se comunican entre sí?**

> 📋 **Orientación para el arquitecto**
> **¿Qué debes modelar?**
> Esta es la vista central del documento. Aquí se abre el bloque del sistema de la Vista de Contexto y se muestran sus partes principales: el frontend, el backend (con sus servicios si aplica), el API Gateway, los servicios transversales y las bases de datos propias. También se mantienen visibles los sistemas externos con los que se integra, para mostrar cómo se conectan con los componentes internos.
> **Elementos que debes incluir:**
> - Todos los componentes desplegables del sistema (frontend, servicios de backend, gateway, servicios de integración).
> - Declaración visual y conceptual de los **Bounded Contexts** (Contextos Delimitados de DDD) del sistema.
> - Las interfaces o APIs expuestas por cada componente (indicando si cuentan con contrato OpenAPI 3.0 Code-First).
> - Las bases de datos propias del sistema (Oracle 19c / PostgreSQL institucional)
> - Los sistemas internos de la ONP con los que se integra (declarando si se intermedia con una Capa Anticorrupción **ACL** para legados Estadio 1)
> - Los sistemas externos agrupados por entidad y comunicados vía Facade perimetral.
> - Las relaciones de comunicación entre componentes, indicando el protocolo si es relevante (REST sincrónico o CloudEvents asíncrono sobre Apache Kafka).
> - Diferenciación visual entre componentes nuevos a implementar y componentes ya existentes (usar colores según leyenda).
> - Leyenda de colores explicando el código visual utilizado.
> **Lo que NO debe aparecer en esta vista:**
> - Lógica interna de cada componente (eso va en la Vista de Componentes).
> - Configuraciones técnicas específicas (puertos, URLs, versiones).
> - Detalles de infraestructura como nodos o servidores (eso va en la Vista de Infraestructura).
> **Pregunta de validación antes de cerrar la vista:** ¿Puede un especialista de plataforma identificar todos los componentes que debe provisionar y cómo se conectan entre sí? Si la respuesta es sí, la vista está bien.

*Una vez completada esta sección, elimina este bloque de orientación.*

**Ilustración A.2 — Vista de Aplicación**

#### Narrativa

*[Describir en prosa los componentes principales del sistema, sus responsabilidades y el flujo de comunicación más relevante. Mencionar qué componentes son nuevos y cuáles son existentes.]*

*Una vez completada esta sección, elimina este bloque de orientación.*

*Ejemplo:*

Las peticiones del usuario ingresan a través del Frontend (SPA responsiva a implementar), que se comunica con el Backend vía el API Manager / Gateway. El Backend está compuesto por tres servicios independientes: Elección, Afiliación y Traslado. Para las integraciones con entidades externas, se utiliza un servicio de fachada que encapsula las llamadas hacia RENIEC, SBS, SUNAT y otras entidades. Los datos del sistema se persisten en una base de datos Oracle 19c propia.

### A.3 Vista de Componentes

**Tipo de vista:** Vista de Aplicación detallada (por componente)

**Audiencia:** Desarrolladores y arquitectos de software.

#### Propósito

Mostrar la estructura interna de los componentes más relevantes del sistema: sus módulos internos, las funciones que exponen y cómo colaboran entre sí. Responde a la pregunta: **¿qué hay dentro de cada componente y cómo está organizado internamente?**

> 📋 **Orientación para el arquitecto**
> **¿Qué debes modelar?**
> Esta vista se elabora una vez por cada componente que tenga suficiente complejidad interna como para requerir explicación. No es obligatorio hacerla para todos los componentes: prioriza los que tienen mayor criticidad, mayor cantidad de responsabilidades o que han generado más preguntas durante la revisión. Un buen criterio es: si el equipo de desarrollo necesita más contexto para implementarlo correctamente, necesita esta vista.
> **Elementos que debes incluir:**
> - Los módulos o sub-componentes internos del componente que se está detallando
> - Las funciones o capacidades de cada módulo
> - Las interfaces internas por donde se comunican los módulos entre sí
> - Los objetos de datos relevantes que fluyen entre módulos
> - Las relaciones con componentes externos al que se está detallando (para mostrar los puntos de entrada y salida)
> **Lo que NO debe aparecer en esta vista:**
> - Clases, métodos o código fuente (eso es diseño de detalle)
> - Tablas de base de datos o esquemas de datos
> - Configuraciones de librerías o frameworks específicos
> **¿Cuántas vistas de componentes elaborar?**
> Elabora una vista por cada componente complejo. En un sistema típico de la ONP esto suele ser entre 2 y 4 vistas. Si un componente es simple y su responsabilidad queda clara en la Vista de Aplicación, no necesita su propia Vista de Componentes.
> **Pregunta de validación antes de cerrar la vista:** ¿Puede un desarrollador nuevo entender qué debe construir dentro de este componente y cómo se relaciona con el resto del sistema? Si la respuesta es sí, la vista está bien.

*Una vez completada esta sección, elimina este bloque de orientación.*

**Ilustración A.3 — Vista de Componentes: [Nombre del Componente]**

#### Narrativa

*[Describir en prosa los módulos internos del componente, sus responsabilidades individuales y cómo colaboran para cumplir la función del componente. Indicar los puntos de entrada (cómo llegan las peticiones) y los puntos de salida (qué devuelve o con qué interactúa hacia afuera).]*

*Una vez completada esta sección, elimina este bloque de orientación.*

*Ejemplo: *

El servicio de Afiliación está compuesto por tres módulos internos: el módulo de Validación, que verifica la identidad del solicitante consultando al servicio de Entidades Externas; el módulo de Proceso de Afiliación, que ejecuta las reglas de negocio del trámite; y el módulo de Persistencia, que registra el resultado en la base de datos del PAST. Las peticiones ingresan desde el API Gateway y las respuestas se devuelven al Frontend a través del mismo canal.

### A.4 Vista de Integraciones

**Tipo de vista: Vista de Integraciones (diagrama de contenedores con foco en comunicaciones)**

**Audiencia: Desarrolladores y arquitectos de software.**

#### Propósito

Mostrar las integraciones del sistema con servicios internos y externos: qué componente origina cada comunicación, con qué sistema o servicio se conecta, y mediante qué mecanismo. Responde a la pregunta: ¿cómo se comunican los componentes del sistema entre sí y con terceros, y mediante qué mecanismos?

> 📋 **Orientación para el arquitecto**
> **¿Qué debes modelar?**
> Esta vista se centra en las comunicaciones: qué componente del sistema inicia cada integración, con qué sistema o servicio se conecta (interno, legado o externo), y mediante qué mecanismo (síncrono vía REST/SOAP, o asíncrono vía mensajería). Es un complemento de la Vista de Aplicación, que muestra los componentes; esta vista muestra explícitamente cómo se conectan entre sí.
> **Elementos que debes incluir:**
> - El componente del sistema que origina cada integración.
> - El sistema o servicio destino de cada integración (interno, legado o entidad externa).
> - El mecanismo utilizado (REST síncrono, SOAP, mensajería asíncrona, archivo, etc.).
> - La dirección del intercambio de información (consulta, registro, actualización).
> - La diferenciación entre integraciones síncronas y asíncronas, si el sistema usa ambas.
> **Lo que NO debe aparecer en esta vista:**
> - El detalle de payloads, esquemas de mensaje o contratos de API.
> - La configuración de colas, tópicos o brokers de mensajería.
> - La lógica de negocio de cada integración.
> **¿Cuándo es necesaria esta vista?**
> Esta vista es especialmente útil cuando el sistema tiene múltiples integraciones con sistemas internos, legados y entidades externas, como suele ocurrir en proyectos de la ONP. Si el sistema tiene pocas integraciones y ya quedan claras en la Vista de Aplicación, esta vista puede omitirse.
> **Pregunta de validación antes de cerrar la vista: ¿Puede un desarrollador identificar, sin ambigüedad, qué componente llama a qué sistema y con qué mecanismo? Si la respuesta es sí, la vista está bien.**

*Una vez completada esta sección, elimina este bloque de orientación.*

**Ilustración A.4 — Vista de Integraciones**

#### Narrativa

*[Describir en prosa las integraciones del sistema: qué componentes se comunican con sistemas internos, legados y externos, mediante qué mecanismos (síncronos o asíncronos) y con qué propósito. No repetir el detalle ya cubierto en la Vista de Aplicación.]*

*Una vez completada esta sección, elimina este bloque de orientación.*

*Ejemplo: *

El Backend se integra de forma síncrona con las entidades externas mediante el servicio de fachada, que expone operaciones REST consumidas por los distintos módulos de dominio. Las notificaciones al ciudadano, en cambio, se procesan de forma asíncrona mediante un mecanismo de mensajería, desacoplando su envío del flujo interactivo del trámite.

### A.5 Vista de Infraestructura

**Tipo de vista:** Vista de Capa de Tecnología e Infraestructura

**Audiencia:** Equipos de infraestructura, plataforma y soporte.

#### Propósito

Mostrar cómo los componentes del sistema son desplegados en la infraestructura tecnológica: nodos de cómputo, orquestadores, redes, zonas de seguridad y ambientes. Responde a la pregunta: **¿dónde y cómo se despliega el sistema?**

> 📋 **Orientación para el arquitecto**
> **¿Qué debes modelar?**
> Esta vista muestra la capa física y lógica donde vive el sistema. Toma los componentes de la Vista de Aplicación y los ubica sobre la infraestructura que los soporta. Debe mostrar todos los ambientes relevantes del proyecto (como mínimo DEV, QA y PRD; si existe UAT también debe incluirse). Para cada ambiente se muestra cómo están distribuidos los componentes en los nodos disponibles.
> **Elementos que debes incluir:**
> - Los ambientes del sistema (DEV, QA, UAT, PRD) como agrupadores
> - Los nodos de cómputo: servidores virtuales y nodos workers del cluster institucional
> - El orquestador de contenedores institucional obligatorio: **Kubernetes (K8s) con motor de runtime CRI `containerd`** y contenedores inmutables
> - Los artefactos desplegables: imágenes de contenedor por cada componente o Bounded Context, asignadas a sus respectivos Namespaces (ej. past-frontend, past-backend)
> - Las redes y zonas de seguridad de la OTI: DMZ (acceso público), Red Interna de Aplicaciones (K8s pods), y Red de Datos (Oracle/BD)
> - Los mecanismos de acceso perimetral externo: WAF institucional, Balanceador de Carga e Ingress Controller
> - Las relaciones de despliegue: qué artefacto se despliega en qué nodo o pod
> - La estrategia de escalamiento horizontal y resiliencia: número mínimo y máximo de réplicas por servicio (HPA) y sondas de salud (Liveness / Readiness / Startup Probes)
> **Lo que NO debe aparecer en esta vista:**
> - Configuraciones internas de los contenedores (variables de entorno, puertos específicos)
> - Scripts de despliegue o archivos de configuración (Helm charts, docker-compose)
> - Lógica de negocio o flujos funcionales
> **Coordinación necesaria antes de elaborar esta vista:**
> Antes de diagramar, debes confirmar con el equipo de plataforma / AD: qué orquestador está disponible y aprobado, si se usará un cluster existente o uno nuevo, cuántos nodos hay disponibles por ambiente, y cuál es la topología de red institucional.
> **Pregunta de validación antes de cerrar la vista:** ¿Puede el equipo de infraestructura provisionar los ambientes y desplegar el sistema usando solo esta vista como referencia, sin necesitar preguntar al arquitecto? Si la respuesta es sí, la vista está bien.

*Una vez completada esta sección, elimina este bloque de orientación.*

**Ilustración A.5 — Vista de Infraestructura**

#### Narrativa

*[Describir en prosa los ambientes del sistema, los nodos principales, el orquestador utilizado, las zonas de red y la estrategia de despliegue. Mencionar explícitamente si se usa infraestructura existente o nueva.]*

*Una vez completada esta sección, elimina este bloque de orientación.*

*Ejemplo:*

El sistema se despliega dentro de la red interna, en un cluster Kubernetes existente gestionado por el equipo de Plataforma, en tres ambientes: DEV, QA y PRD. El Frontend se despliega como un contenedor en el namespace past-frontend, con una réplica en DEV/QA y dos réplicas en PRD. Los servicios del Backend se despliegan en el namespace past-backend. El acceso externo se canaliza a través del WAF institucional y un Ingress Controller, que enruta el tráfico hacia los servicios correspondientes dentro del cluster. La base de datos Oracle 19c es provista por el equipo de BD sobre infraestructura existente.

## ANEXO B: MATRIZ DE TRAZABILIDAD ARQUITECTÓNICA

*[Presentar una matriz que establezca la trazabilidad entre los requerimientos funcionales y no funcionales de la solución y los elementos de la arquitectura que les dan soporte, tales como componentes, integraciones, fuentes de datos, mecanismos de seguridad, procesos asíncronos, vistas arquitectónicas y demás entregables técnicos asociados. Esta matriz tiene como finalidad facilitar la validación de cobertura arquitectónica, el análisis de impacto de cambios, la planificación de pruebas, la gestión de incidencias y la evolución de la solución durante su ciclo de vida.]*

*Recomendación: La matriz deberá permitir identificar de manera sencilla los elementos arquitectónicos afectados por un requerimiento y facilitar el análisis de impacto ante cambios funcionales o técnicos. Se recomienda mantener un nivel de detalle suficiente para apoyar las actividades de desarrollo, calidad, soporte y mantenimiento de la solución, puede enlazarse a un archivo Excel con la información más relavante.*

*Ejemplo (referencial)*

| REQUERIMIENTO | TIPO | ELEMENTO ARQUITECTONICO | ADR | VISTA ARQUITECTONICA |
|---|---|---|---|---|
|   |   |   |   |   |
|   |   |   |   |   |
|   |   |   |   |   |

## ANEXO C: DECISIONES ARQUITECTÓNICAS (ADRs)

Este anexo registra las decisiones arquitectónicas significativas tomadas durante el diseño del sistema. Su propósito es preservar la memoria institucional, facilitar la trazabilidad de las decisiones y evitar que se repitan análisis ya realizados.

> 📋 **Orientación para el arquitecto**
> Los ADRs (Architecture Decision Records) son el registro de las decisiones importantes que tomaste durante el diseño y por qué las tomaste. Son la diferencia entre un documento de arquitectura que solo describe qué se construyó y uno que explica por qué se construyó así.
> **¿Cuándo registrar un ADR?** Registra una decisión cuando cumpla al menos una de estas condiciones:
> - Tuviste que elegir entre dos o más alternativas válidas
> - La decisión tiene impacto en la estructura del sistema o en cómo los componentes se relacionan
> - La decisión podría ser cuestionada en el futuro por alguien que no estuvo en la discusión
> - La decisión tiene consecuencias negativas o compromisos que el equipo debe conocer
> **¿Qué NO registrar como ADR?**
> - Decisiones de implementación o configuración (qué puerto usar, qué nombre ponerle a una tabla)
> - Decisiones obvias que no requirieron evaluación de alternativas
> - Detalles de diseño interno de un componente
> **Ejemplos de decisiones que sí merecen un ADR en un proyecto típico de la ONP:**
> - Elección del patrón de arquitectura (microservicios vs monolito modular)
> - Elección del orquestador de contenedores y si se usa cluster existente o nuevo
> - Elección del patrón de integración con legados (sincrónico vs asincrónico, directo vs cola)
> - Elección de la estrategia de autenticación externa con entidades como SBS
> - Adopción de OpenTelemetry como estándar de telemetría
> **¿Cuántos ADRs elaborar?** No hay un número mínimo ni máximo. En un proyecto de mediana complejidad como el PAST, entre 5 y 10 ADRs es un rango razonable. Si tienes menos de 3, probablemente estás sub-registrando decisiones importantes.
> Una vez completado esta sección, elimina este bloque de orientación.

### C.1 Resumen de decisiones

> 📋 **Orientación para el arquitecto**
> Esta tabla es el índice de todos los ADRs del documento. Complétala una vez que hayas elaborado todos los ADRs en la sección C.2. El estado refleja en qué punto está cada decisión: una decisión puede estar en revisión si aún no ha sido validada con los stakeholders relevantes.

*Una vez completado esta sección, elimina este bloque de orientación.*

| ID | Título | Estado | Fecha |
|---|---|---|---|
| AD-001 | [Título de la decisión] | Aprobado | [DD/MM/AAAA] |
| AD-002 | [Título de la decisión] | En revisión | [DD/MM/AAAA] |

**Estados posibles:** Propuesto / En revisión / Aprobado / Descartado / Reemplazado por [AD-XXX]

### C.2 Detalle de decisiones

> 📋 **Orientación para el arquitecto**
> Completa una ficha por cada decisión listada en C.1. La clave de un buen ADR está en el campo "Contexto": debe explicar la situación real que te llevó a tomar la decisión, no solo enunciar la decisión en sí. Un lector que no estuvo en las reuniones debe poder entender por qué era necesario decidir algo y qué estaba en juego.
> El campo "Alternativas evaluadas" debe incluir al menos dos opciones reales que fueron consideradas. Si solo hubo una opción posible, probablemente no necesita ser un ADR.
> Duplica el bloque de ficha para cada decisión adicional.

*Una vez completado esta sección, elimina este bloque de orientación.*

#### Formato Propuesto de ADR:

| Componente / Sección | Descripción / Contenido Sugerido |
|---|---|
| Título Principal | # ADR-[ID] · [Título Descriptivo de la Decisión] |
| Metadatos | ID: AD-XXXEstado: Propuesto / En revisión / Aprobado / Descartado / Reemplazado Fecha: AAAA-MM-DD Alineación Normativa: Alineado o EXCEPCIÓN Secciones del Documento: [ej. 2.1 Estilo arquitectónico] Relacionada con: [AD-YYY] (descripción breve) |
| 1. Contexto y Problemática | Describir la situación que motiva la decisión: el problema técnico/de negocio, restricciones (infraestructura, cronograma), fuerzas en tensión. Puede incluir tablas de dominio si aplica. |
| 2. Decisión | Declaración directa y explícita de la opción elegida. Qué se adopta, cómo se delimita y patrones clave aplicados. |
| 3. Justificación | Sustento técnico de por qué esta solución es la mejor para este contexto específico, referenciando atributos de calidad y normativas. |
| 4. Alternativas Consideradas | Enumerar alternativas evaluadas y descartadas. Para cada una (ej. ### A. [Opción] — *Descartada*): - Descripción: Resumen de la opción y sus ventajas. - Motivo de descarte: Razón técnica u operativa por la que se rechazó. |
| 5. Consecuencias | Positivas: Lista de beneficios (mantenibilidad, rendimiento, etc.). Negativas: Lista de compromisos (trade-offs), complejidad añadida o costos. Riesgos Aceptados (Sub-tabla): Riesgo asumido y su justificación/mitigación. |
| 6. Verificación | Sub-tabla con dos columnas: - Qué demuestra la decisión: Condición o invariante arquitectónica. - Cómo se comprueba: Prueba, métrica, endpoint o comando empírico. |

## ANEXO D: ATRIBUTOS DE CALIDAD

Este anexo describe los atributos de calidad relevantes para el sistema y cómo la arquitectura propuesta los aborda. Sirve como vínculo entre los requisitos no funcionales y las decisiones arquitectónicas.

> 📋 **Orientación para el arquitecto**
> Los atributos de calidad son las características del sistema que no se refieren a qué hace (funcionalidad) sino a cómo lo hace: qué tan disponible es, qué tan seguro, qué tan rápido, qué tan fácil de mantener. Son los requisitos no funcionales elevados al nivel arquitectónico.
> **¿Cómo completar esta tabla?**
> - La columna "Atributo" lista la característica de calidad. Usa los atributos relevantes para el sistema; no tienes que incluir todos los que aparecen en el ejemplo si no aplican.
> - La columna "Requisito / Expectativa" expresa el nivel esperado de ese atributo: debe ser concreto y medible si es posible (ej. "99.5% de disponibilidad en horario hábil" es mejor que "alta disponibilidad").
> - La columna "Decisión arquitectónica que lo aborda" vincula el atributo con la decisión concreta que lo satisface. Si hay un ADR relacionado, referenciarlo (ej. "Ver AD-003").
> **Diferencia con los ADRs:** el ADR explica POR QUÉ se tomó una decisión. Esta tabla explica QUÉ atributo de calidad satisface cada decisión. Son complementarios.
> **Error frecuente:** confundir atributos de calidad con requisitos funcionales. "El sistema debe registrar la afiliación" es un requisito funcional. "El sistema debe registrar la afiliación en menos de 3 segundos el 95% de las veces" es un atributo de calidad (rendimiento).
> **Atributos comunes a considerar:** Disponibilidad, Seguridad, Rendimiento, Escalabilidad, Observabilidad, Mantenibilidad, Interoperabilidad, Recuperabilidad (RTO/RPO).
> Una vez completada esta sección, elimina este bloque de orientación.

### D.1 Atributos de calidad y su cobertura arquitectónica

| Atributo | Requisito / Expectativa | Decisión arquitectónica que lo aborda |
|---|---|---|
| Disponibilidad y Resiliencia | [ej. 99.5% uptime en horario hábil y tolerancia a fallos transaccionales] | [ej. Despliegue en K8s con réplicas/probes; aislamiento de fallos en llamadas externas según la matriz por criticidad, timeout estricto y Bulkhead siempre, Circuit Breaker con Resilience4j solo en Microservicios o bajo ADR — Ver AD-00X] |
| Seguridad | [ej. Autenticación obligatoria en todas las APIs públicas y Zero Trust] | [ej. WAF + JWT vía SAA/token para APIs internas; autenticación SBS para EAF] |
| Escalabilidad | [ej. Soporte para N usuarios concurrentes en pico electoral] | [ej. Contenedorización inmutable en K8s con autoescalado horizontal (HPA) — Ver AD-00X] |
| Observabilidad (Google SRE 4 Golden Signals) | [ej. Monitoreo obligatorio de las 4 Señales Doradas: Latencia, Tráfico, Errores y Saturación] | [ej. OpenTelemetry + centralización de logs ECS con trace_id y propagación de headers X-Request-ID / CodDetRespuesta, Ver AD-00X] |
| Mantenibilidad | [ej. Capacidad de actualizar o reemplazar un servicio sin afectar los demás] | [ej. Bounded Contexts independientes con contratos OpenAPI 3.0 Code-First] |
| Interoperabilidad | [ej. Integración con 10+ entidades externas del Estado y legados internos] | [ej. Servicio de fachada para Entidades Externas y Capa Anticorrupción (ACL) para legados ONP] |
| Recuperabilidad | [ej. RTO máximo de X horas, RPO máximo de Y horas] | [ej. Estrategia de backup inmutable y recuperación coordinada sobre Oracle 19c / K8s PV] |

## ANEXO E: RIESGOS, DEUDA TÉCNICA Y OPORTUNIDADES DE MEJORA

Este anexo registra los riesgos arquitectónicos identificados, la deuda técnica conocida, las oportunidades de mejora sobre la arquitectura actual y las excepciones a los lineamientos institucionales. Su propósito es hacer explícitas las decisiones de compromiso y las desviaciones tomadas durante el diseño, y establecer un plan de seguimiento.

> 📋 Orientación para el arquitecto — Anexo E completo
> Este anexo tiene cuatro partes con propósitos distintos pero complementarios.
> **Riesgos:** son situaciones que podrían ocurrir y afectar negativamente la arquitectura o el proyecto. Se registran para que el equipo los tenga presentes y pueda mitigarlos proactivamente. Un riesgo arquitectónico es diferente de un riesgo de proyecto: no es "el proveedor puede no entregar a tiempo" sino "si la latencia del servicio de RENIEC supera X ms, el proceso de afiliación fallará y no hay mecanismo de reintento".
> **Deuda técnica:** son decisiones de compromiso que se tomaron conscientemente por razones de tiempo, recursos o información incompleta, sabiendo que no son la solución ideal a largo plazo. Registrarla es importante para que no se pierda el conocimiento de que existe y que en algún momento debe resolverse.
> **Diferencia clave entre riesgo y deuda técnica:**
> - El **riesgo** es algo que puede pasar y que debemos evitar o mitigar.
> - La **deuda técnica** es algo que ya pasó (una decisión subóptima que ya tomamos) y que debemos resolver en el futuro.
> Las oportunidades de mejora (E.3) y las excepciones a lineamientos (E.4) se explican con su propio criterio de registro en sus respectivas secciones.
> Una vez completado este anexo, elimina este bloque de orientación.

### E.1 Riesgos arquitectónicos

> 📋 **Orientación para el arquitecto**
> Lista los riesgos que identificaste durante el diseño. Para cada riesgo, evalúa su probabilidad de ocurrencia y su impacto en el sistema si ocurre, y propón una acción de mitigación concreta. La mitigación no tiene que eliminar el riesgo por completo; puede ser una acción para reducir su probabilidad o su impacto.
> **Fuentes comunes de riesgos en proyectos ONP:**
> - Dependencia de servicios de entidades externas que no están bajo control de la ONP (RENIEC, SBS, SUNAT)
> - Disponibilidad y capacidad de la infraestructura institucional
> - Integración con sistemas legados cuya documentación es incompleta
> - Cambios de alcance en requisitos que impacten la arquitectura definida

*Una vez completado esta sección, elimina este bloque de orientación.*

| ID | Descripción del Riesgo | Probabilidad | Impacto | Mitigación |
|---|---|---|---|---|
| R-001 | [Descripción concreta del riesgo: qué podría ocurrir y cuál sería su consecuencia arquitectónica] | Alta / Media / Baja | Alto / Medio / Bajo | [Acción concreta para reducir la probabilidad o el impacto] |
| R-002 | [Descripción concreta del riesgo] | Alta / Media / Baja | Alto / Medio / Bajo | [Acción de mitigación] |

### E.2 Deuda técnica conocida

> 📋 **Orientación para el arquitecto**
> Lista las decisiones subóptimas que se tomaron conscientemente. Para cada una indica qué se hizo, por qué no se hizo de la forma ideal (tiempo, información incompleta, dependencia de otro equipo) y cuándo o cómo se planea resolver.
> **MANDATO INSTITUCIONAL DE DEUDA TÉCNICA CERO:**
> Toda deuda técnica admitida por compromisos de cronograma o dependencias externas debe:
> 1. Estar asociada obligatoriamente a un **Ticket de Refactorización registrado en el Backlog** oficial de GitLab / Jira del proyecto.
> 2. Contar con una **estrategia de mitigación de bajo riesgo**, como el uso de **Feature Toggles (PA14)** para encender/apagar el comportamiento temporal sin re-despliegues complejos.
> 3. Tener un **horizonte de remediación acotado en Sprints** (prioridad alta/media) pactado formalmente antes de obtener la conformidad de paso a Producción.

*Una vez completado esta sección, elimina este bloque de orientación.*

| ID | Descripción | Prioridad | Plan de resolución (y Ticket en Backlog) |
|---|---|---|---|
| DT-001 | [Qué se hizo de forma subóptima, por qué se tomó esa decisión y cuál es el impacto de no resolverlo] | Alta / Media / Baja | [Acción concreta, Ticket en Jira/GitLab (ONP-XXXX) y horizonte de tiempo en Sprints para resolverlo] |
| DT-002 | [Descripción de la deuda técnica] | Alta / Media / Baja | [Plan de resolución con Ticket e Hito de remediación] |

### E.3 Oportunidades de mejora arquitectónica

> 📋 **Orientación para el arquitecto**
> Una oportunidad de mejora es distinta de un riesgo y de una deuda técnica: no es algo que podría salir mal (riesgo) ni una decisión subóptima ya asumida conscientemente (deuda técnica), sino un cambio que mejoraría la arquitectura actual si se ejecutara. Suele identificarse al revisar la implementación contra el diseño (arquitectura as-built) o al evaluar la evolución del sistema.
> **Cuándo registrar una oportunidad de mejora:**
> Se identifica una oportunidad de mejora cuando ocurre alguna de estas situaciones:
> Existe una consolidación o simplificación posible de componentes que hoy están más fragmentados de lo necesario.
> El equipo de desarrollo adoptó de facto una práctica que conviene formalizar o extender al resto del sistema.
> Se detecta una brecha entre el diseño documentado y la implementación real que conviene corregir, sin que constituya un riesgo activo ni una deuda técnica ya asumida.
> Si la mejora es estructural (afecta a varios componentes o a la topología del sistema), documéntala con el mismo nivel de detalle que un ADR: contexto, alternativas evaluadas y cómo se comprobaría su éxito. Si es una mejora táctica y acotada, regístrala directamente en la tabla.

*Una vez completado esta sección, elimina este bloque de orientación.*

| ID | Descripción | Prioridad | Acción sugerida |
|---|---|---|---|
| OM-001 | [Descripción de la oportunidad de mejora: qué se observó y por qué representa una mejora sobre el estado actual] | Alta / Media / Baja | [Acción concreta sugerida para materializar la mejora] |
| OM-002 | [Descripción de la oportunidad de mejora] | Alta / Media / Baja | [Acción sugerida] |

### E.4 Registro de excepciones

> 📋 **Orientación para el arquitecto**
> El registro de excepciones documenta las desviaciones respecto a los estándares, lineamientos o componentes aprobados institucionalmente por Arquitectura OTI, ya sea porque se adoptaron antes de una validación formal o porque responden a una restricción del proyecto. Su propósito es que ninguna desviación quede implícita.
> **Diferencia con Riesgos y Deuda Técnica:**
> Una excepción es una decisión ya tomada que se aparta de un estándar institucional (ej. un componente de infraestructura no evaluado por Arquitectura OTI); no es una situación futura (riesgo) ni una decisión subóptima del propio diseño (deuda técnica), aunque las tres pueden estar relacionadas.
> Toda excepción sin control compensatorio identificado, o cuya revisión esté marcada como bloqueante, debe quedar señalada para su resolución antes del pase a producción.

*Una vez completado esta sección, elimina este bloque de orientación.*

| ID | Excepción | Riesgo aceptado | Control compensatorio | Revisión |
|---|---|---|---|---|
| EXC-001 | [Descripción de la desviación respecto al estándar o lineamiento institucional, y si fue una decisión tomada con o sin participación de Arquitectura OTI] | [Qué queda expuesto por aceptar esta excepción] | [Medida que reduce el riesgo mientras la excepción no se resuelve, o "Ninguno" si no existe] | [Cuándo debe revisarse: ej. "Bloqueante para el pase a producción" o una fecha/hito] |
| EXC-002 | [Descripción de la excepción] | [Riesgo aceptado] | [Control compensatorio] | [Revisión] |

*Documento elaborado por la Oficina de Tecnologías de la Información — ONP*
