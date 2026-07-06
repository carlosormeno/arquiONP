# Lineamiento de Diseño y Arquitectura de Software ONP

**Código:** LIN-ARQ-000  
**Versión:** 0.1.18  
**Fecha:** 2026-07-06  
**Autor:** Oficina de Tecnologías de la Información — ONP  
**Estado:** Borrador de trabajo interno  
**Clasificación:** Marco rector interno. No es un entregable oficial de la lista de documentos de arquitectura; es el documento normativo base que guía la redacción de todos los lineamientos técnicos formales. Todo lineamiento derivado debe ser consistente con las decisiones de este documento.

---

## Historial de versiones

| Versión | Fecha | Descripción |
|---------|-------|-------------|
| 0.1.0 | 2026-05-21 | Versión inicial del documento de trabajo |
| 0.1.1 | 2026-05-28 | Alinea el checklist de observabilidad con el modelo YAML institucional y con overrides operativos definidos por Plataforma |
| 0.1.2 | 2026-05-28 | Restringe la adopción de mensajería/event bus ad hoc mientras `LIN-BUS-001` no exista y define la regla transitoria de excepción |
| 0.1.3 | 2026-06-09 | Incorpora el marco de Four Golden Signals (Google SRE) en sección 11.5 como fundamento conceptual del dashboard mínimo de métricas |
| 0.1.4 | 2026-07-02 | Incorpora sección 3.7 de Resiliencia y Tolerancia a Fallos (*Design for Failure*) con matriz de timeouts según criticidad y criterios de adopción de Resilience4j condicionados a Microservicios o excepciones en Monolito Modular |
| 0.1.5 | 2026-07-02 | Incorpora sección 3.8 con estándares de implementación para patrones oficiales PT09 (BFF), PT10 (Gateway-Aggregation) y PT12 (Facade Arquitectónico) adaptados al Monolito Modular |
| 0.1.6 | 2026-07-02 | Corrige §3.7.1–3.7.2: estandariza configuración de clientes externos en Apache HttpClient 5 integrando connection timeout, read timeout y Bulkhead en una sola factory; elimina inconsistencia con JdkClientHttpRequestFactory |
| 0.1.7 | 2026-07-02 | Incorpora sección 3.9 de Patrones de Dominio y Relación en Monolito Modular con Mapa de Contextos (PD08) y gobierno de Shared Kernel (PD09) |
| 0.1.8 | 2026-07-02 | Incorpora sección 3.10 CQRS con dos variantes (Outbox+Kafka y CDC+Kafka), diagrama de flujo por variante y reglas ONP (ADR obligatorio); añade MongoDB como motor de lectura válido en §5.3 |
| 0.1.9 | 2026-07-02 | Incorpora sección 3.11 Principios Rectores Transversales: PRA07 Loose Coupling/High Cohesion, PRA10 Single Source of Truth, PR09 Separation of Concerns; añade PR09 en §7.2 |
| 0.1.10 | 2026-07-02 | Incorpora sección 6.4.1 con la guía normativa de Building Blocks DDD en contexto Spring (Agregado PD01, Entidad y Value Object PD02), separando explícitamente el modelo puramente de dominio de las entidades JPA |
| 0.1.11 | 2026-07-02 | Incorpora sección 3.9.3 con el patrón Lenguaje Publicado (Published Language — PD10), normando el uso de CloudEvents v1.0 (CNCF) en integración asíncrona sobre Kafka y OpenAPI 3.0 en REST |
| 0.1.12 | 2026-07-02 | Incorpora sección 2.2.1 normando el patrón Feature Toggle (PA14) en sus 4 variantes como complemento operativo mandatorio de Strangler Fig (PT08) para Trunk-Based Development y Branch by Abstraction |
| 0.1.13 | 2026-07-02 | Incorpora sección 3.8.4 normando la Arquitectura Orientada a Servicios (SOA — E07) como estándar mandatorio de interoperabilidad gubernamental (PIDE, RENIEC, SUNAT), exigiendo aislamiento ACL (PT11) y resiliencia extrema (PT06/PI08) |
| 0.1.14 | 2026-07-02 | Correcciones de consistencia: §3.8.4 timeout referencia §3.7.1 en lugar de rango hardcodeado; §2.2.1 Ops Toggle aclara que Unleash es el estándar on-premise y LaunchDarkly requiere ADR+Seguridad; §3 tabla intro actualiza §3.8 para incluir interoperabilidad gubernamental |
| 0.1.15 | 2026-07-02 | Revisión exhaustiva de congruencia interna: sincroniza referencias y títulos en la tabla introductoria de §3 con las subsecciones §3.8 y §3.9 actualizadas, y complementa el glosario del Apéndice B con los términos Feature Toggle y Published Language |
| 0.1.16 | 2026-07-02 | Auditoría de cierre: numera §5.3 y §5.4 (headings sin número), corrige referencia §9.3 al formato §12, añade aviso "pendiente de redacción" en §2.2.1 para LIN-DEV-JAVA-001 §14, agrega ADR-014 (Feature Toggle/Unleash) en Apéndice A, amplía Apéndice B con CQRS y CDC, y refuerza obligatoriedad de Circuit Breaker en §3.8.4 para proveedores SOA de alta volumetría |
| 0.1.17 | 2026-07-03 | Revisión estructural de §3, §5, §8 y §10: elimina separadores visuales redundantes de §3 (sustituidos por tabla introductoria); restructura §8 en 4 grupos (GoF Estructurales/Creacionales/Comportamiento + Capas y DDD) añadiendo 5 patrones nuevos (Strategy, Observer, Command, State, Decorator); declara formalmente PRA09 en §3.11 y cierra el deferral en §3.9.3; numera §5.1–§5.2 (Oracle y BD no relacional); actualiza §10.1 con EDA y DDD desglosado en Value Object y Aggregate Root |
| 0.1.18 | 2026-07-06 | Auditoría de citas cruzadas con los 14 lineamientos de Nivel 3: agrega referencia a LIN-VER-001 §6.1 en las dos menciones de Trunk-Based Development (§2.2.1 y ADR-014); agrega referencia a LIN-API-REST-001 en la intro de §3.8; agrega referencia a LIN-IaC-001 en §11.1 para el aprovisionamiento declarativo de infraestructura |

---

## Tabla de contenidos

1. [Marco General](#1-marco-general)
2. [Hoja de ruta de evolución arquitectónica de ONP](#2-hoja-de-ruta-de-evolución-arquitectónica-de-onp)
3. [Estilos y Patrones de Arquitectura](#3-estilos-y-patrones-de-arquitectura)
4. [Estrategia de Frontend](#4-estrategia-de-frontend)
5. [Estrategia de Datos](#5-estrategia-de-datos)
6. [Estrategias para organizar la lógica de negocio](#6-estrategias-para-organizar-la-lógica-de-negocio)
7. [Principios de diseño](#7-principios-de-diseño)
8. [Patrones de diseño de código](#8-patrones-de-diseño-de-código)
9. [Estructura de proyecto](#9-estructura-de-proyecto)
10. [Estrategia de pruebas](#10-estrategia-de-pruebas)
11. [Arquitectura de despliegue](#11-arquitectura-de-despliegue)
12. [Perfil del contratista por estilo arquitectónico](#12-perfil-del-contratista-por-estilo-arquitectónico)

---

## 1. Marco General

### 1.1 Propósito

Este documento establece el marco de diseño y arquitectura de software que rige el desarrollo de sistemas en la Oficina de Normalización Previsional (ONP). Define los estilos arquitectónicos permitidos, los principios de diseño obligatorios, los patrones de código aprobados y la hoja de ruta de evolución de los sistemas.

No es un documento de gobierno aprobado formalmente. Es la referencia técnica interna que el equipo de arquitectura usa para redactar los lineamientos oficiales de forma coherente y alineada.

**Todo lineamiento técnico formal que se redacte debe ser consistente con las decisiones de este documento.**

### 1.2 Relación con documentos aprobados

| Documento aprobado | Cómo se relaciona con este marco |
|---|---|
| Lineamiento de Estándares de Tecnología v2.0 | Define el stack tecnológico obligatorio: Java + Spring Boot + Maven para backend nuevo. Este documento respeta esa decisión y la operacionaliza. |
| Lineamiento sobre Arquitectura Patrón de Apps y BD v1.0 | Define estilos y fichas de patrones. Este documento lo extiende (añade Modular Monolith, Hexagonal, DDD) y da profundidad de implementación que el documento aprobado no tiene. |
| Arquitectura de referencia v0.1 (Dic. 2025) | Incorpora patrones adicionales (CQRS, Saga, Anti-Corruption Layer, Strangler Fig). Este documento los integra dentro de un marco de uso coherente. |

### 1.3 Alcance y audiencia

**Alcance:** Todos los sistemas de software desarrollados o contratados por ONP a partir de la fecha de aprobación de este documento.

**Audiencia principal:** Arquitectos de TI de ONP.

**Audiencia secundaria:** Líderes técnicos y desarrolladores senior contratados (Locadores), Fabricas de Software. El personal contratado recibe el conjunto de lineamientos formales derivados de este documento, no este documento directamente.

---

## 2. Hoja de ruta de evolución arquitectónica de ONP

ONP no parte de cero. Tiene sistemas legacy en servidores de aplicación tradicionales (JBoss, WebLogic) y sistemas nuevos que comienzan como monolitos. La hoja de ruta define tres estadios y el patrón de migración entre ellos.

### 2.1 Los tres estadios

```
Estadio 1          Estadio 2                    Estadio 3
─────────          ─────────                    ─────────
Monolito           Monolito Modular             Microservicios
Tradicional   ──►  (Maven Multi-módulo)    ──►  Selectivos
(legacy)           (punto de llegada           (solo donde
                    para sistemas nuevos)        se justifica)
```

**Estadio 1 — Monolito Tradicional:** Sistemas legacy existentes. El objetivo no es reescribirlos sino migrarlos progresivamente usando Strangler Fig y Feature Toggles (ver [sección 2.2](#22-patron-de-migracion-strangler-fig)).

**Estadio 2 — Monolito Modular:** Es el **punto de llegada por defecto para todo sistema nuevo**. Se implementa como un proyecto Maven multi-módulo (ver [sección 9](#9-estructura-de-proyecto)). Cada módulo tiene límites claros, su propio paquete raíz y puede evolucionar de forma independiente. La frontera entre módulos se respeta igual que si fueran servicios separados: sin dependencias circulares, sin acceso directo entre capas de módulos distintos.

**Estadio 3 — Microservicios Selectivos:** Un módulo del Estadio 2 puede extraerse como microservicio independiente cuando cumple los criterios señalados en el punto [sección 3.5.2](#352-cuándo-usar--criterios-y-matriz-de-extracción-a-microservicios). No se diseña para microservicios desde el inicio.

### 2.2 Patrón de migración: Strangler Fig y Feature Toggles

El **Strangler Fig** es un patrón de migración que toma su nombre de la higuera estranguladora: una planta que crece alrededor de un árbol existente hasta reemplazarlo completamente, sin derribarlo. En software, el sistema nuevo crece en paralelo al legacy y lo reemplaza de forma progresiva — nunca hay una reescritura total ni un corte abrupto.

Para migrar sistemas del Estadio 1 al Estadio 2 sin reescritura completa:

```
                    ┌─────────────────┐
                    │   API Gateway   │  ← enrutador de tráfico
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼                             ▼
   ┌──────────────────┐         ┌──────────────────────┐
   │  Sistema Legacy  │         │  Sistema Nuevo       │
   │  (Estadio 1)     │         │  (Estadio 2)         │
   │                  │         │  módulo extraído     │
   └──────────────────┘         └──────────────────────┘
```

**Principio:** No se toca el sistema legacy. Se construye el módulo nuevo en paralelo. El gateway redirige gradualmente el tráfico al nuevo módulo. Cuando el 100% del tráfico migra, el componente legacy correspondiente se retira.

**Regla ONP:** Toda migración de sistema legacy debe documentarse como un ADR (Architecture Decision Record) con la ruta de migración explícita.

#### 2.2.1 Complemento operativo mandatorio: Feature Toggles

Mientras que el patrón *Strangler Fig* gestiona el enrutamiento perimetral en el API Gateway o proxy de red para desviar tráfico desde el sistema legado hacia el nuevo módulo, **dentro de la aplicación** es obligatorio utilizar el patrón **Feature Toggle** (*Alternador de Funcionalidades*) para controlar en tiempo de ejecución, la ejecución de nuevos fragmentos de código sin requerir un redespliegue ni ramas de Git longevas.

##### A. Tipos de Toggles y Criterio de Uso Institucional

| Categoría | Propósito en ONP | Dinamismo | Vida Útil Permitida |
|---|---|---|---|
| **Release Toggle** | Habilitar *Trunk-Based Development* (ver **LIN-VER-001 §6.1**). Permite integrar código incompleto a la rama `main` a diario sin activar el flujo en producción hasta terminar la funcionalidad. | Estático / Archivo de configuración | **Corto plazo** (máximo 2 a 4 semanas; se borra inmediatamente al liberar el *release*). |
| **Ops Toggle** | Mecanismo de *Kill-Switch* / Degradación elegante. Permite apagar un algoritmo pesado o una nueva integración en **< 1 segundo** ante una alerta de sobrecarga sin reiniciar pods en Kubernetes. | Dinámico — **Unleash** (self-hosted, estándar ONP) o **Spring Cloud Config** + flags en YAML (alternativa ligera sin infraestructura adicional). LaunchDarkly y otros SaaS solo con ADR aprobado por Arquitectura OTI + Seguridad (requieren salida de red externa, incompatible con entorno on-premise sin autorización). | **Largo plazo** (mientras el caso de uso se considere crítico operativamente). |
| **Experiment Toggle** | Pruebas A/B o *Canary Launching* interno (ej. enrutar al 5% de usuarios al nuevo motor de cálculo de reserva matemática para comparar precisión). | Dinámico por petición / usuario | **Medio plazo** (dura lo que dure la fase de evaluación y validación actuarial). |
| **Permission Toggle** | Habilitar funcionalidades beta o administrativas únicamente a ciertos roles de usuario (ej. auditores o supervisores ONP). | Dinámico por contexto de seguridad (JWT) | **Largo plazo** o permanente en control de accesos. |

##### B. Regla Antideuda Técnica de Toggles

> **Mandato de Deuda Técnica Cero:** Todo *Release Toggle* o *Experiment Toggle* creado en el código fuente conlleva una **deuda técnica temporal programada**. Al finalizar la migración o validación de la funcionalidad, el equipo de desarrollo está **obligado a eliminar el toggle, sus condicionales (`if/else`) y el código legado asociado** en el sprint inmediatamente posterior. Mantener *toggles* muertos en el código se considera un anti-patrón de mantenibilidad sancionable en revisión de código (`LIN-DEV-JAVA-001 §14` *(pendiente de redacción)*).

##### C. Integración con Branch by Abstraction

Para migraciones profundas dentro del Monolito Modular donde no interviene la red (ej. reemplazar una librería de acceso a datos o un conector SOAP hacia SUNAT por un cliente REST), se combina el Feature Toggle con el patrón **Branch by Abstraction**: se define una interfaz Java común en `domain.port.out` y una factoría o *Router* en la capa de infraestructura que evalúa el estado del *toggle* para inyectar la implementación antigua o la moderna sin alterar la lógica de negocio.

---

## 3. Estilos y Patrones de Arquitectura

Esta sección organiza los estilos y patrones de arquitectura adoptados por ONP agrupados de la siguiente manera:

| Grupo | Pregunta | Secciones |
|---|---|---|
| **Organización del código** | ¿Cómo organizo el código dentro de un módulo? | Arquitectura en capas (Layered) [3.1](#31-arquitectura-en-capas-layered), Arquitectura Hexagonal (Ports & Adapters) [3.2](#32-arquitectura-hexagonal-ports--adapters) |
| **Estructura del sistema** | ¿Cómo estructuro y despliego el sistema completo? | Monolito puro [3.3](#33-monolito-puro), Monolito Modular [3.4](#34-monolito-modular), Microservicios [3.5](#35-microservicios) |
| **Comunicación** | ¿Cómo se comunican los componentes o servicios? | Arquitectura Orientada a Eventos (EDA) [3.6](#36-arquitectura-orientada-a-eventos-eda) |
| **Resiliencia, integración e interoperabilidad** | ¿Cómo protejo las llamadas a sistemas externos, gestiono canales de consumo e integro con entidades del Estado? | Resiliencia y Tolerancia a Fallos (Design for Failure) [3.7](#37-resiliencia-y-tolerancia-a-fallos-design-for-failure), Patrones de Integración, Agregación, Fachada e Interoperabilidad SOA [3.8](#38-patrones-de-integración-agregación-fachada-e-interoperabilidad-soa-e07) |
| **Fronteras entre módulos** | ¿Cómo relaciono los bounded contexts dentro del Monolito Modular? | Patrones de Dominio y Relación entre Contextos en el Monolito Modular [3.9](#39-patrones-de-dominio-y-relación-entre-contextos-en-el-monolito-modular-pd08-pd09-pd10) |
| **Consistencia y proyecciones** | ¿Cuándo y cómo separo escritura de lectura con CQRS? | CQRS — Separación de Modelos de Escritura y Lectura [3.10](#310-cqrs--separación-de-modelos-de-escritura-y-lectura-pa07) |
| **Principios rectores** | ¿Qué principios gobiernan todas las decisiones de §3? | Principios rectores transversales [3.11](#311-principios-rectores-transversales-pra07-pra10-pr09) |

**Los grupos no son decisiones independientes.** La elección de cómo organizas el código dentro de un módulo condiciona hasta dónde puede llegar ese módulo en la hoja de ruta de evolución.

#### Camino de un sistema nuevo

Todo sistema nuevo nace como **Monolito Modular**. Cada módulo interno usa **Capas simples [(3.1)](#31-arquitectura-en-capas-layered)**, y por defecto, porque es suficiente para la mayoría de los casos. Ahora bien, cuando un módulo específico del monolito modular cumple con las condiciones y criterios detallados en la [sección 3.5.2 (Criterios y Matriz de Extracción)](#352-cuándo-usar--criterios-y-matriz-de-extracción-a-microservicios), dicho módulo se convierte en un candidato legítimo para extracción; en ese momento, lo primero será refactorizar el módulo seleccionado a una arquitectura del tipo **Hexagonal [(3.2)](#32-arquitectura-hexagonal-ports--adapters)** para aislar su frontera y luego se extrae. No se debe diseñar para microservicios desde el inicio porque tiene un costo operativo y de consistencia muy alto.

```
  Módulo nuevo en Monolito Modular
  (Capas simples por defecto)
           │
           │  puede quedarse aquí indefinidamente
           │  si no hay necesidad de extraerlo
           │
           │  cuando cumple los 6 criterios de 3.5:
           │  escala diferenciada, equipo dedicado,
           │  bounded context claro, datos propios...
           │
           ▼
  Paso 1 ── Refactorizar a Hexagonal (3.2)
           │
           │  los ports definen exactamente qué es
           │  interno y qué necesita del exterior.
           │  Sin esa frontera clara, la extracción
           │  genera acoplamiento oculto.
           │
           ▼
  Paso 2 ── Extraer como Microservicio (3.5)
```

#### Camino de un sistema legado

Un sistema legado (monolito puro, Estadio 1) **sí puede evolucionar** hacia Monolito Modular. El camino es el patrón **Strangler Fig [(2.2)](#22-patrón-de-migración-strangler-fig-y-feature-toggles)**: se construyen módulos nuevos con la arquitectura correcta al lado del legado, y el tráfico se redirige gradualmente. El código legado no se toca ni se exige refactorizar a Hexagonal para iniciar la migración.

```
  Sistema legado
  (Monolito puro, JBoss/WebLogic)
           │
           │  Strangler Fig:
           │  construir módulos nuevos al lado,
           │  redirigir tráfico gradualmente,
           │  retirar componentes legados cuando
           │  el 100% del tráfico migró
           │
           ▼
  Monolito Modular (Estadio 2)
  ── y desde ahí aplica el camino de arriba ──
```

> **¿Y MVC?** MVC (Model-View-Controller) no aparece en esta tabla porque no es una arquitectura completa — es el patrón que describe cómo se organiza solo la capa de presentación: el Controller recibe el request, procesa con el Model y devuelve una View (en APIs REST, el JSON). En Spring Boot esto está implícito en `@RestController` y no requiere una decisión explícita. La decisión real de arquitectura es cómo organizas las capas de aplicación, dominio e infraestructura — que es exactamente lo que cubren [3.1](#31-arquitectura-en-capas-layered) y [3.2](#32-arquitectura-hexagonal-ports--adapters).

### 3.1 Arquitectura en capas (Layered)

Organiza el código en cuatro capas horizontales con responsabilidades separadas. Las dependencias siempre fluyen de arriba hacia abajo — nunca al revés. Es el estilo más simple disponible y el punto de partida correcto para la mayoría de módulos nuevos en ONP.

#### Diagrama

```
┌─────────────────────────────────┐
│         Capa de Presentación    │  Controllers, DTOs de entrada/salida
├─────────────────────────────────┤
│         Capa de Aplicación      │  Services, casos de uso, orquestación
├─────────────────────────────────┤
│         Capa de Dominio         │  Entidades, reglas de negocio
├─────────────────────────────────┤
│         Capa de Infraestructura │  Repositorios JPA, clientes HTTP, MQ
└─────────────────────────────────┘
```

#### Cuándo usar

Es el estilo por defecto para todo módulo nuevo en ONP, tanto en sistemas legacy existentes (Monolito puro, [3.3](#33-monolito-puro)) como en sistemas nuevos (Monolito Modular, [3.4](#34-monolito-modular)). Adoptar cuando:

- la lógica del módulo es simple o moderada (CRUD, Transaction Script, Active Record);
- el módulo tiene menos de 3 integraciones externas;
- no se prevé cambio de tecnología de persistencia ni de protocolo de entrada.

Un módulo con esta arquitectura puede vivir indefinidamente en el Monolito Modular. Si en el futuro se convierte en candidato a microservicio, deberá aplicarse lo indicado en el punto 3 **Camino de un sistema nuevo**.

#### Reglas ONP

**Regla de dependencia:** las capas superiores dependen de las inferiores. La capa de Dominio no depende de Infraestructura, usa interfaces que Infraestructura implementa. Ningún Controller accede directamente a un repositorio.

### 3.2 Arquitectura Hexagonal (Ports & Adapters)

Variante avanzada que invierte la dependencia entre dominio e infraestructura. En Capas simples ([3.1](#31-arquitectura-en-capas-layered)), el dominio termina dependiendo de la infraestructura: las entidades usan anotaciones JPA, los servicios llaman directamente a clientes HTTP. Hexagonal rompe esa dependencia: el dominio es Java puro y es la infraestructura la que se adapta al dominio, no al revés.

#### Conceptos clave

**Dominio** — el núcleo del sistema. Contiene las entidades, los value objects y las reglas de negocio. No importa ningún framework.

**Port** — una `interface` Java que actúa como contrato en la frontera del dominio. Define QUÉ puede hacerse o necesitarse, sin decir CÓMO. Hay dos tipos:

| Tipo | Significado | Dónde vive la interface | Quién la implementa | Ejemplo ONP |
|---|---|---|---|---|
| **Port de entrada** | Lo que el exterior puede pedirle al sistema | Capa `application/` | Application Service | `RegistrarAporteUseCase` |
| **Port de salida** | Lo que el dominio necesita del exterior | Capa `domain/` | Adapter en `infrastructure/` | `PensionistaRepository` |

**Adapter** — la implementación concreta de un port. Vive en infraestructura y conecta el dominio con un sistema externo concreto (Oracle, RENIEC, RabbitMQ, etc.).

#### Diagrama

**Flujo de una operación** — un request REST que registra un aporte recorre las tres capas así:

```
  [HTTP Request]
        │
        ▼
  ┌──────────────────┐
  │  AporteController│  Adapter de entrada — traduce el request a un Command
  │  (infrastructure)│  y llama al port de entrada
  └────────┬─────────┘
           │ llama a
           ▼
  ┌───────────────────────────────────────────────────┐
  │                  APPLICATION                      │
  │                                                   │
  │  RegistrarAporteUseCase ◄── implementado por ──►  │
  │  (port de entrada)          RegistrarAporteService│
  │                             (orquesta el dominio, │
  │                              llama port de salida)│
  └──────────────────────┬────────────────────────────┘
                         │ llama a
                         ▼
  ┌───────────────────────────────────────────────────┐
  │                    DOMINIO                        │
  │  PensionistaRepository  (port de salida)          │
  │  (interface — el dominio no sabe si es Oracle     │
  │   o cualquier otra BD)                            │
  └──────────────────────┬────────────────────────────┘
                         │ implementado por
                         ▼
  ┌──────────────────────┐
  │  PensionistaJpaRepo  │  Adapter de salida — habla con Oracle
  │  (infrastructure)    │
  └──────────────────────┘
```

**Vista de conjunto:**

```
┌───────────────────────────────────────────────────────────────┐
│                       INFRAESTRUCTURA                         │
│                                                               │
│  ┌───────────────┐                  ┌──────────────────────┐  │
│  │ Adapter REST  │                  │ Adapter JPA          │  │
│  │ (Controller)  │                  │ (RepositoryImpl)     │  │
│  └───────┬───────┘                  └──────────┬───────────┘  │
│          │ implementa port entrada             │ implementa   │
│          │                                     │ port salida  │
└──────────┼─────────────────────────────────────┼──────────────┘
           │                                     │
           ▼                                     ▼
┌──────────────────────────────────────────────────────────────┐
│                       APPLICATION                            │
│                                                              │
│   Port entrada ──► Application Service ──► Port salida       │
│   (interface)       (orquesta dominio)     (interface)       │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
            ┌───────────────────────────────┐
            │            DOMINIO            │
            │  Entidades, Value Objects,    │
            │  reglas de negocio            │
            │  (sin imports de framework)   │
            └───────────────────────────────┘
```

#### Cuándo usar

Adoptar Hexagonal sobre Capas simples cuando se cumple al menos uno de estos criterios:

- el módulo tiene 3 o más integraciones externas;
- se prevé cambio de motor de base de datos o de protocolo de entrada;
- el módulo es candidato a extraerse como microservicio ([3.5](#35-microservicios)) — Hexagonal es obligatorio antes de la extracción.

#### Reglas ONP

La regla de dependencia es estricta: `infrastructure → application → domain`. Si una clase en `domain` importa `jakarta.persistence` o `org.springframework`, la frontera está rota. Ver [sección 9.3](#93-hexagonal--clean) para la estructura Maven concreta.

### 3.3 Monolito puro

Un único proceso desplegable donde toda la lógica, acceso a datos y presentación coexisten sin fronteras explícitas entre módulos. Es el **Estadio 1** de la hoja de ruta de ONP y el estado actual de los sistemas legacy en producción.

#### Diagrama

```
┌──────────────────────────────────────────┐
│             MONOLITO PURO                │
│  Presentación + Lógica + Datos           │
│  (sin fronteras entre módulos)           │
│  Servidor: JBoss / WebLogic              │
└──────────────────────────────────────────┘
                    │
                    ▼
             ┌────────────┐
             │  Oracle BD │
             └────────────┘
```

#### Cuándo usar

ONP **no construye nuevos sistemas como Monolito puro**. Este estilo existe únicamente en los sistemas legacy heredados. Los motivos para no usarlo en sistemas nuevos son:

- Sin fronteras entre módulos, cualquier cambio tiene un radio de impacto impredecible.
- El acoplamiento acumulado hace el sistema cada vez más difícil de probar y desplegar.
- No existe un camino natural hacia Monolito Modular ni Microservicios sin reescritura total.

#### Reglas ONP

**Por qué existe en ONP:** los sistemas legacy fueron construidos antes de que las prácticas de modularización fueran estándar. Son sistemas productivos que no se reescriben, se migran progresivamente usando el patrón Strangler Fig ([2.2](#22-patron-de-migracion-strangler-fig)).

**Camino de migración:** todo sistema en Estadio 1 debe tener un plan documentado de migración hacia [3.4](#34-monolito-modular) (Monolito Modular) usando el patrón Strangler Fig ([2.2](#22-patron-de-migracion-strangler-fig)).

### 3.4 Monolito Modular

El Estadio 2 de ONP y el punto de llegada por defecto para todo sistema nuevo. Un único proceso desplegable organizado en módulos lógicos con fronteras explícitas. La clave es que es **un solo JAR** — no hay red ni comunicación entre procesos — pero cada módulo tiene su propio límite de responsabilidad que se respeta igual que si fuera un servicio separado.

#### Diagrama

```
┌──────────────────────────────── 1 solo JAR desplegable ───────────────────────────────┐
│                                                                                       │
│  ┌──────────────────┐    ┌───────────────────┐    ┌──────────────────┐                │
│  │    Módulo        │    │    Módulo         │    │    Módulo        │                │
│  │   Expedientes    │    │    Aportes        │    │  Prestaciones    │                │
│  │  ──────────────  │    │  ──────────────   │    │  ──────────────  │                │
│  │  API             │    │  API              │    │  API             │                │
│  │  Application     │    │  Application      │    │  Application     │                │
│  │  Domain          │    │  Domain           │    │  Domain          │                │
│  │  Infrastructure  │    │  Infrastructure   │    │  Infrastructure  │                │
│  └──────────────────┘    └───────────────────┘    └──────────────────┘                │
│                                                                                       │
│  Frontera explícita — sin dependencias circulares entre módulos                       │
└───────────────────────────────────────────────────────────────────────────────────────┘
                                          │
                                          ▼
                                   ┌──────────────┐
                                   │  Oracle BD   │
                                   │ (compartida) │
                                   └──────────────┘
```

#### Cuándo usar

Es el destino por defecto para:

- todo sistema nuevo desarrollado en ONP;
- todo sistema legacy migrado progresivamente desde el Estadio 1 vía Strangler Fig ([2.2](#22-patron-de-migracion-strangler-fig)).

No se salta directamente a Microservicios ([3.5](#35-microservicios)) desde el Monolito Modular sin antes cumplir los criterios de extracción definidos en [3.5.2](#352-cuándo-usar--criterios-y-matriz-de-extracción-a-microservicios).

#### Reglas ONP

Las dependencias entre módulos son explícitas y unidireccionales: ningún módulo accede directamente a los paquetes internos de otro. No hay dependencias circulares. Ver [sección 9.2](#92-monolito-modular) para la estructura Maven concreta.

### 3.5 Microservicios

Estilo donde cada módulo se despliega como un proceso independiente con su propia base de datos. Al separar en microservicios se pierde la garantía ACID del Monolito Modular: `@Transactional` solo abarca una conexión a una BD y no puede coordinar dos servicios distintos. Esta pérdida de consistencia fuerte es el punto de partida para entender cuándo microservicios aplica y cuándo no. El detalle de las propiedades ACID en el contexto Oracle está en **LIN-BD-ORA-001 sección 3.9**.

#### 3.5.1 Teorema CAP — contexto obligatorio antes de decidir microservicios

El teorema CAP (Brewer, 2000) establece que un sistema distribuido solo puede garantizar simultáneamente **dos** de estas tres propiedades:

- **C — Consistency (Consistencia):** toda lectura recibe el dato más reciente escrito
- **A — Availability (Disponibilidad):** toda solicitud recibe una respuesta, aunque no sea el dato más reciente
- **P — Partition Tolerance (Tolerancia a partición):** el sistema sigue funcionando aunque haya fallas de red entre nodos

En la práctica, en un sistema distribuido **P es no negociable** — las redes fallan. La decisión real es entre **CP** o **AP**:

#### Diagrama

```
Monolito Modular → una sola BD → ACID garantizado → CAP no aplica

Microservicios   → BD por servicio → red entre servicios → CAP aplica
                   la elección CP vs AP es una decisión de arquitectura explícita
```

| Elección | Qué sacrificas | Patrón resultante | Ejemplo de cuándo podría  aplica en ONP |
|---|---|---|---|
| **CP** | Disponibilidad — el sistema puede rechazar requests si no puede garantizar consistencia | Transacciones distribuidas (evitar), 2PC (evitar en microservicios) | Cálculo de pensión, liquidación, aportes — un dato incorrecto es inaceptable |
| **AP** | Consistencia fuerte — aceptas consistencia eventual | Saga + Outbox + Dead Letter Queue | Consultas de estado, notificaciones, historial — un dato ligeramente desactualizado es aceptable |

#### 3.5.2 Cuándo usar — Criterios y Matriz de Extracción a Microservicios

Reservado para módulos del Monolito Modular maduro ([3.4](#34-monolito-modular)) que cumplen **todos** los criterios indicados en la siguiente tabla. Si algún criterio no se cumple, el módulo permanece en el Monolito Modular. Es importante indicar que **por ningún motivo se deben crear microservicios como reemplazo de modulos del Monolito Modular por razones de moda o preferencia tecnológica**, esto solo se debe hacer cuando el módulo cumple los criterios aquí indicados y luego de un análisis riguroso de sus implicaciones. Los criterios se evalúan de forma conjunta, es decir, no basta con cumplir uno o dos criterios para decidir extraer un módulo como microservicio.

| Criterio | Descripción |
|---|---|
| Escala diferenciada | El módulo necesita escalar de forma independiente al resto del sistema |
| Equipo dedicado | Existe un equipo completo (dev + QA + ops) que lo posee |
| Frontera de dominio clara | El módulo tiene un bounded context bien definido, sin lógica compartida con otros módulos |
| Datos propios | El módulo tiene su propia base de datos; no comparte tablas con otros módulos |
| Tolerancia a fallo independiente | El sistema principal puede operar aunque este módulo falle |
| SLO definido | Tiene Service Level Objectives formales documentados (ver **LIN-OBS-001**) |

#### 3.5.3 Reglas ONP para Microservicios

**Regla — elección CAP:** al extraer un módulo como microservicio se debe declarar explícitamente en el ADR si el servicio es CP o AP, y qué patrón gestiona la consistencia. Un microservicio sin esta decisión documentada no está listo para producción.

**Regla — estructura previa obligatoria:** todo módulo que cumpla los seis criterios debe refactorizarse a **Arquitectura Hexagonal** ([3.2](#32-arquitectura-hexagonal-ports--adapters)) antes de iniciar la extracción. Los ports definen qué es interno, los adapters definen qué es externo. Sin esa frontera clara, la extracción genera acoplamiento oculto. Ver [sección 9.3](#93-hexagonal--clean) para la estructura Maven correspondiente.

**Regla — sin ACID entre microservicios:** coordinar dos BDs distintas requeriría Two-Phase Commit (2PC), que introduce bloqueos distribuidos, acoplamiento fuerte y puntos únicos de falla — exactamente lo opuesto de lo que se busca con microservicios. La alternativa correcta es el patrón **Saga** con **Transactional Outbox**. El detalle de implementación está en **LIN-BUS-001 sección 9**.

### 3.6 Arquitectura Orientada a Eventos (EDA)

Estilo donde los componentes se comunican a través de **eventos** en lugar de llamadas directas. Un productor registra que algo ocurrió; los consumidores reaccionan de forma asíncrona y desacoplada a través de un broker. EDA no reemplaza a REST — es el estilo correcto solo en los contextos específicos descritos más abajo.

#### Diagrama

```
[Productor]  ──► evento ──►  [Broker]  ──► evento ──►  [Consumidor A]
                                        ──► evento ──►  [Consumidor B]
```

La diferencia fundamental con REST:

| Dimensión | REST / Sincrónico | EDA / Asíncrono |
|---|---|---|
| Dirección | El productor llama al consumidor directamente | El productor emite al broker; no conoce a los consumidores |
| Tiempo | El productor espera la respuesta | El productor continúa sin esperar |
| Acoplamiento | Alto — el productor conoce la API del consumidor | Bajo — solo comparten el contrato del evento |
| Consistencia | Fuerte (el resultado es inmediato) | Eventual (el consumidor procesa en su tiempo) |

#### Cuándo usar

EDA es el estilo correcto cuando:

- la acción del productor está completa sin importar cuándo reacciona el consumidor (notificaciones, auditoría, reporte);
- el sistema necesita desacoplar dos contextos sin crear una dependencia directa de ciclo de despliegue;
- la consistencia eventual es aceptable para ese flujo de negocio;
- el patrón Saga coordina una transacción distribuida entre sistemas con BD propia — sean microservicios o aplicativos monolíticos;
- el Monolito Modular necesita notificar a sistemas externos (auditoría, reportes, otros aplicativos) sin acoplarse directamente a ellos.

EDA requiere la infraestructura de un broker. ONP adopta **Apache Kafka** como broker institucional (ver **LIN-BUS-001 sección 4**). Los criterios detallados de cuándo usar, están en **LIN-BUS-001 sección 4.3**. Todo sistema que adopte EDA debe cumplir con **LIN-BUS-001**.

#### Cuándo NO usar

| Situación | Por qué EDA no corresponde |
|---|---|
| Lógica de negocio core con requisito ACID (cálculo de pensión, liquidación, aportes) | EDA implica consistencia eventual. En estos contextos se requiere consistencia fuerte, usar `@Transactional` en el Monolito Modular. |
| El productor necesita la respuesta inmediata del consumidor | Si el productor no puede avanzar sin la respuesta, el patrón correcto es REST sincrónico. EDA con correlación para simular sincronismo añade complejidad sin beneficio. |
| Desacoplar por desacoplar sin análisis de consistencia | El diseño del evento, el esquema de compensación y la operabilidad del broker tienen un costo real. Desacoplar sin justificación no es una mejora arquitectónica. |
| Equipo sin observabilidad para sistemas asíncronos | EDA es opaco sin trazas distribuidas que conecten el trace del productor con el del consumidor. Si el stack de observabilidad (LIN-OBS-001) no está maduro, EDA es difícil de operar. LIN-BUS-001 eleva esta condición a prerequisito formal: ningún flujo EDA entra a producción sin trazabilidad completa. |

#### Patrones EDA aplicables en ONP

| Patrón | Propósito | Cuándo usar |
|---|---|---|
| **Saga — coreografía** | Coordinar una transacción distribuida entre sistemas con BD propia sin 2PC. Cada participante emite un evento al completar su paso; el siguiente lo consume y reacciona. No hay coordinador central. | Flujos con pocos pasos y baja complejidad condicional, entre microservicios o aplicativos monolíticos. |
| **Saga — orquestación** | Un orquestador central emite comandos y reacciona a eventos de respuesta. Más trazable que la coreografía pero introduce un coordinador con estado. | Flujos con muchos pasos, condiciones complejas o múltiples rutas de compensación. Aplica sobre microservicios y sobre monolitos — ver variante ONP abajo. |
| **Transactional Outbox** | Garantiza que el evento se publica solo si la transacción de BD confirma. Evita pérdida de evento cuando el proceso falla entre el `COMMIT` y la publicación al broker. | Obligatorio cada vez que se publica un evento desde un servicio con BD propia. Sin Outbox, hay riesgo de pérdida silenciosa de evento. |
| **Event-Driven CQRS** | Separa el modelo de escritura del de lectura. Las escrituras producen eventos que actualizan proyecciones de lectura optimizadas. | Solo cuando el volumen de consultas justifica un modelo de lectura separado y la latencia eventual en las proyecciones es aceptable. Ver punto **[3.10](#310-cqrs--separación-de-modelos-de-escritura-y-lectura-pa07)**. para la norma completa de adopción, variantes (Outbox/CDC) y criterios de elección del read model. |

> **Event Sourcing** no está en la lista. Almacenar el estado como secuencia de eventos introduce complejidad operativa (versionado de esquemas de eventos, proyecciones, replay) que supera el beneficio en los sistemas actuales de ONP. Su adopción requiere ADR aprobado por Arquitectura OTI.

##### Saga por orquestación — variante ONP sobre aplicativos monolíticos (Kafka + REST)

El patrón Saga no requiere microservicios. En ONP, donde el parque de aplicativos está compuesto mayoritariamente por monolitos con BD propia, Saga se podría implementar combinando **Kafka como canal de transporte** y **REST como mecanismo de ejecución** en cada participante.

```
Orquestador (app separada — gestor del flujo y estado)
    │
    ├──► topic: onp.saga.{flujo}.paso1.comando
    │         └──► Monolito A consume → llama su propio POST /api/v1/... → persiste en su BD
    │              └──► publica en: onp.saga.{flujo}.paso1.respuesta { estado: OK|FALLO }
    │                                       ↑
    │              Orquestador lee respuesta─┘
    │              Si OK → avanza al paso 2
    │              Si FALLO → inicia compensaciones en orden inverso
    │
    ├──► topic: onp.saga.{flujo}.paso2.comando
    │    ...
    └──► Saga COMPLETADA / Saga FALLIDA (todo compensado)
```

**Roles y responsabilidades:**

| Componente | Responsabilidad |
|---|---|
| **Orquestador** | Mantiene el estado de la Saga en BD propia. Publica comandos en Kafka. Lee respuestas. Decide avanzar o compensar. |
| **Kafka** | Canal de transporte desacoplado. El orquestador no conoce la URL de cada monolito — solo el tópico. |
| **Monolito participante** | Consume el comando Kafka. Llama su propio servicio REST interno. Publica la respuesta. No sabe que está en una Saga. |

**Estado que el orquestador debe persistir:**

```sql
SAGA_INSTANCIA
├── saga_id          UUID      -- identificador del flujo completo
├── tipo             VARCHAR   -- ej. 'PENSION_COMPLETA'
├── estado           VARCHAR   -- INICIADA | EN_PROGRESO | COMPENSANDO | COMPLETADA | FALLIDA
├── paso_actual      INTEGER
├── contexto         JSON      -- IDs de cada operación ejecutada por paso (para compensación)
├── creado_en        TIMESTAMP
└── actualizado_en   TIMESTAMP
```

**Lo que cada monolito participante debe garantizar:**

- **Idempotencia:** si el comando llega más de una vez (reintento por timeout), el monolito no duplica el efecto. Usar `sagaId` + número de paso como clave de idempotencia.
- **Endpoint de compensación:** operación de negocio inversa con auditoría. No es un DELETE físico — es una reversión con trazabilidad.
- **Desconocimiento de la Saga:** el servicio REST del monolito es una operación normal. El conocimiento del flujo completo vive solo en el orquestador.

**Cuándo usar esta variante:**

| Criterio | Saga REST + Kafka (esta variante) | Saga microservicios pura |
|---|---|---|
| Participantes | Monolitos existentes con BD propia | Microservicios extraídos con BD propia |
| Adopción | Baja — los monolitos solo agregan consumer/producer Kafka | Mayor — requiere diseño de microservicio completo |
| Cuándo aplica en ONP | Corto/mediano plazo — parque actual de aplicativos | Largo plazo — tras extracción de microservicios |

> El detalle de la convención de tópicos Saga y la estructura de CloudEvents extendido está en **LIN-BUS-001 sección 9.4**.

#### Reglas ONP

**Prerequisito arquitectónico:** todo sistema que adopte EDA — independientemente de si es un Monolito Modular o un microservicio — debe tener definidos explícitamente sus **ports y adapters** (Arquitectura Hexagonal, [3.2](#32-arquitectura-hexagonal-ports--adapters) de modo que Kafka sea un adaptador de infraestructura, no una dependencia interna del dominio. El broker no es un atajo para omitir esa frontera (ver **LIN-BUS-001 sección 1.3**). EDA no requiere microservicios: el Monolito Modular puede adoptar EDA para comunicarse con sistemas externos o coordinar flujos Saga con otros aplicativos.

**Broker institucional:** ONP opera un único broker Kafka institucional. No se aprueban brokers paralelos por proyecto sin ADR aprobado por Arquitectura OTI (ver **LIN-BUS-001 sección 4.1**, principio P1).

**Estándar de estructura de evento — CloudEvents v1.0:** la estructura de todos los eventos publicados en el bus institucional cumple **CloudEvents v1.0** (especificación CNCF). Garantiza interoperabilidad con otras instituciones del Estado y el ecosistema cloud-native. El detalle de la estructura de CloudEvents (campos, tipos, formato `traceparent` W3C TraceContext) está en **LIN-BUS-001 sección 5.2**. La decisión de adopción está documentada en **ADR-CLOUDEVENTS-001**.

**Observabilidad obligatoria:** ningún flujo EDA entra a producción sin trazabilidad completa — correlación de trazas productor→consumidor. Esta condición es un prerequisito formal (LIN-BUS-001, principio P7). Ver **LIN-OBS-001** para la configuración del stack.

**Documento rector:** el estándar completo de diseño, operación y gobierno del bus de eventos está en **LIN-BUS-001 — Lineamiento de Mensajería y Bus de Eventos ONP**. Esta sección es una introducción al estilo; LIN-BUS-001 es la referencia normativa obligatoria para cualquier adopción.

### 3.7 Resiliencia y Tolerancia a Fallos (*Design for Failure*)

En la hoja de ruta de la ONP, el **Monolito Modular (Estadio 2)** es la arquitectura por defecto. Al ejecutarse en un único proceso desplegable (un solo JAR), la comunicación entre módulos internos es intra-JVM (llamadas a métodos en memoria), por lo que **no requiere** patrones de resiliencia de red para interactuar entre sí.

Sin embargo, bajo el principio innegociable de **Design for Failure**, toda integración de salida por red hacia sistemas externos del Estado (como por ejemplo RENIEC, SUNAT, PIDE, entre otros), APIs bancarias o brokers de eventos deben estar blindadas. Un fallo en un tercero externo nunca debe provocar el agotamiento del pool de hilos (`Thread Pool Exhaustion`) del servidor de aplicaciones ni tumbar el aplicativo.

#### 3.7.1 Matriz de Timeouts según Demanda y Criticidad

Está **prohibido** desplegar clientes de integración exterior (HTTP, JDBC, Kafka) sin timeouts explícitos. En lugar de aplicar tiempos fijos genéricos, los umbrales de espera se configuran en función de la **demanda concurrente** y la **criticidad en la ruta interactiva del usuario**:

| Categoría del Servicio Externo | Ejemplo en ONP | Perfil de Demanda | Connection Timeout | Read Timeout | Estrategia ante Fallo |
|---|---|---|---|---|---|
| **Alta Demanda / Ruta Crítica Interactiva** | **RENIEC**, SAA (Token), Validación DNI en ventanilla virtual | Muy alta (cientos de req/min). Bloquea directamente al ciudadano en pantalla. | `≤ 1.5s - 2s` | `2s - 3s` | *Fail-Fast*: Abortar rápido para liberar hilos del monolito. Notificar en UI o consultar caché temporal si existe. |
| **Demanda Media / Consulta Negocio** | **PIDE**, Consulta de deudas, padrones externos | Media. Consultas durante la tramitación interna de un expediente. | `≤ 2s` | `4s - 6s` | Reintento simple acotado (solo si es lectura idempotente). |
| **Baja Demanda / Proceso Asincrónico o Diferido** | **SUNAT**, PLAME, Conciliaciones por lote, validaciones nocturnas | Baja concurrencia en tiempo real o ejecutado por *workers* en segundo plano. | `≤ 3s` | `10s - 15s` | Mayor tolerancia de espera al no bloquear la UI del ciudadano. Ante fallo persistente, derivar a cola o DLQ. |

En **Spring Boot 3 / Java 21**, esto se parametriza usando **Apache HttpClient 5** (`HttpComponentsClientHttpRequestFactory`), que en una sola configuración provee connection timeout, read timeout y límite de conexiones por ruta (Bulkhead). Los tres controles van juntos para evitar configuraciones incompletas:

```java
@Configuration
public class ExternalClientsConfig {

    @Bean
    @Qualifier("reniecClient")
    public RestClient reniecRestClient(@Value("${onp.externos.reniec.url}") String url) {
        var factory = clienteRestFactory(
            Duration.ofMillis(1500), // connection timeout: Alta demanda — Fail-Fast
            Duration.ofSeconds(2),   // read timeout
            10                       // Bulkhead: máx 10 conexiones concurrentes a RENIEC
        );
        return RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }

    @Bean
    @Qualifier("sunatClient")
    public RestClient sunatRestClient(@Value("${onp.externos.sunat.url}") String url) {
        var factory = clienteRestFactory(
            Duration.ofSeconds(3),  // connection timeout: Proceso diferido — mayor tolerancia
            Duration.ofSeconds(12), // read timeout
            5                       // Bulkhead: proceso diferido, menor concurrencia esperada
        );
        return RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }

    private HttpComponentsClientHttpRequestFactory clienteRestFactory(
            Duration connectTimeout, Duration readTimeout, int maxConnPerRoute) {
        var connManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setMaxConnPerRoute(maxConnPerRoute)
            .setMaxConnTotal(maxConnPerRoute * 3)
            .build();
        var httpClient = HttpClients.custom()
            .setConnectionManager(connManager)
            .evictExpiredConnections()
            .build();
        var factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return factory;
    }
}
```

> **Por qué Apache HttpClient 5 y no `JdkClientHttpRequestFactory`:** El cliente JDK nativo no expone control de connection timeout a nivel de factory ni límites de conexiones por ruta. Apache HttpClient 5 (`httpclient5`) centraliza los tres controles en una sola configuración, eliminando la posibilidad de desplegar un cliente sin alguno de los tres blindajes.

#### 3.7.2 Resiliencia Nativa en el Monolito Modular — Reintentos

El Bulkhead queda resuelto en la configuración de `clienteRestFactory` del punto anterior (`setMaxConnPerRoute`). El complemento son los reintentos acotados:

**Reintentos Nativos Acotados:** Para mitigar microcortes en lecturas idempotentes, se prefiere **Spring Retry** simple. **Prohibido** reintentar automáticamente operaciones de escritura (`POST`, `PUT`) no idempotentes.

```java
// 2 intentos en total: 1 inicial + 1 reintento
@Retryable(maxAttempts = 2, backoff = @Backoff(delay = 500, multiplier = 2))
public DatosPersonaReniec consultarDni(String dni) { ... }
```

#### 3.7.3 Adopción de Circuit Breaker con Resilience4j — Criterios y Condicionales

**Resilience4j (`resilience4j-spring-boot3`)** no es el estándar por defecto en un Monolito Modular. Su adopción está condicionada a los siguientes criterios:

1. **Criterio Primario (Microservicios — Estadio 3):** El patrón es **obligatorio** cuando el sistema opera como **Microservicios** (habiéndose cumplido los criterios indicados en [3.5.2](#352-cuando-usar--criterios-y-matriz-de-extracción-a-microservicios)). Con múltiples servicios llamándose por red, un *Circuit Breaker* formal (estados *Closed*, *Open*, *Half-Open*) con *Fallback* es indispensable para evitar colapsos en cascada.

2. **Criterio de Excepción en Monolito Modular:** Un Monolito Modular puede adoptar Resilience4j **únicamente** para envolver el *Adapter* de salida hacia un proveedor externo que cumpla **ambas** condiciones:
   - **(a) Volumetría masiva en ruta crítica interactiva:** el proveedor recibe cientos de llamadas por minuto en la ruta directa del ciudadano (ej. RENIEC en ventanilla virtual).
   - **(b) Necesidad de corte automático sin intento de red (*fast fail*):** el timeout + pool de conexiones de [3.7.1](#371-matriz-de-timeouts-según-demanda-y-criticidad) no es suficiente porque, bajo carga alta con el proveedor caído, los slots del pool se agotan igualmente esperando el timeout en cada intento. El Circuit Breaker detecta el patrón de fallo y corta de inmediato sin consumir un slot de conexión, protegiendo el monolito completo.

   > **Lo que NO justifica el ADR:** necesitar Bulkhead o Retry — ambos ya están cubiertos por Apache HttpClient 5 (`setMaxConnPerRoute`) y Spring Retry ([3.7.2](#372-resiliencia-nativa-en-el-monolito-modular--reintentos)). Resilience4j en Monolito Modular se justifica exclusivamente por la **máquina de estados del Circuit Breaker**.

   Esta adopción excepcional requiere **ADR aprobado** por Arquitectura.

### 3.8 Patrones de Integración, Agregación, Fachada e Interoperabilidad SOA

Para gestionar eficientemente la comunicación entre los canales de consumo (SPAs, aplicaciones móviles, interoperabilidad PIDE) y los backends institucionales (parque heredado y Monolitos Modulares nuevos), son oficiales y de aplicación regulada los siguientes cuatro patrones. Esta sección cubre el estilo de integración e interoperabilidad; el contrato REST concreto que exponen estos patrones (naming, versionado, paginación, manejo de errores, WSO2, OpenAPI 3.0) se rige por **LIN-API-REST-001 — Estándar de APIs REST**.

#### 3.8.1 Facade Arquitectónico de Integración

**Propósito:** Ocultar la complejidad, heterogeneidad y protocolos de múltiples sistemas externos o legados (ej. consultar RENIEC, SUNAT y un monolito heredado en JBoss) detrás de una interfaz unificada y limpia adaptada al dominio de la ONP.

**Diferencia con el patrón GoF Facade ([8.1.3](#813-facade-simplificación-de-subsistema-interno)):** Actúa en la capa de **Infraestructura de Integración**, combinando múltiples adaptadores de salida (`Adapters`), aplicando *Anti-Corruption Layer (ACL)*, control de resiliencia ([3.7](#37-resiliencia-y-tolerancia-a-fallos-design-for-failure)) y orquestación externa sin contaminar la lógica de negocio.

```java
@Component
public class GobiernoIntegracionFacadeAdapter implements VerificacionCiudadanoPort {

    private final ReniecClient reniecClient;
    private final SunatClient sunatClient;
    private final PerfilIntegralMapper mapper;

    @Override
    public PerfilVerificado consultarPerfilIntegral(String dni) {
        CompletableFuture<DatosPersonaReniec> reniecFuture = 
            CompletableFuture.supplyAsync(() -> reniecClient.consultar(dni));
        CompletableFuture<DatosContribuyenteSunat> sunatFuture = 
            CompletableFuture.supplyAsync(() -> sunatClient.consultarRucPorDni(dni));

        CompletableFuture.allOf(reniecFuture, sunatFuture).join();
        return mapper.consolidar(reniecFuture.join(), sunatFuture.join());
    }
}
```

#### 3.8.2 Gateway-Aggregation

**Propósito:** Consolidar múltiples consultas en una sola respuesta hacia el cliente, reduciendo el *chattiness* (exceso de peticiones HTTP por red) y mejorando los tiempos de carga en la pantalla del ciudadano.

**Reglas de aplicación según Estilo Arquitectónico:**
1. **En Monolito Modular (Estadio 2):** La agregación **no se realiza por red**. Se ejecuta en memoria mediante un *Controller de Agregación* o *Query Service* que invoca los APIs de los diferentes módulos internos del JAR (`onp-expedientes` + `onp-aportes`), devolviendo el DTO unificado.
2. **En Microservicios (Estadio 3):** Se implementa como un servicio especializado o en el **API Gateway institucional (WSO2)**, ejecutando llamadas concurrentes a los microservicios descendentes.

#### 3.8.3 Backend for Frontend — BFF

**Propósito:** Crear una capa adaptada y especializada en las necesidades de presentación y comunicación de un canal de consumo concreto (ej. SPA Angular, App Móvil iOS/Android, o portal ciudadano), desacoplando los requerimientos de la interfaz de usuario respecto a los servicios del backend core.

**Capacidades y Casos de Uso Clave en el Ecosistema ONP:**
* **Adaptación de Modelos por Canal:** Moldear las respuestas del backend core al formato exacto que necesita cada tipo de pantalla, reduciendo el sobre-envío de datos (*over-fetching*) en redes móviles y evitando lógica de transformación compleja en el dispositivo del ciudadano.
* **Mediación de Seguridad frente al API Manager (WSO2 / SSO):** El BFF es un punto ideal para implementar el patrón *Token Handler*. En arquitecturas donde el backend core está protegido por el **API Manager institucional (WSO2)** y políticas de SSO, el BFF puede actuar como puente: gestiona sesiones ligeras o cookies seguras (`HttpOnly`) con la SPA o App Móvil, intercepta las peticiones e inyecta los tokens criptográficos institucionales (`Authorization: Bearer <Token>`) al llamar al API Manager. Esto protege los tokens contra ataques XSS en el navegador y simplifica la seguridad del canal.

**Criterios de Adopción Normativa en ONP:**
* **Adopción Recomendada:** Cuando un sistema presta servicios a **dos o más canales de consumo diferenciados** (ej. Portal Web Angular + Aplicación Móvil nativa) con flujos de navegación, anchos de banda o requisitos de seguridad distintos, o cuando un canal requiere de mediación especializada frente al API Manager.
* **Evitar Sobreingeniería:** Si un proyecto cuenta con **un único canal estándar** (ej. una única SPA institucional en Angular consumiendo directamente su backend) y no existe requisito de traducción de protocolos o mediación SSO externa, **no se construye un servicio BFF separado**. En este escenario, la propia capa de presentación REST (`controller/`) del Monolito Modular sirve el contrato directamente, evitando saltos de red y sobrecarga operativa innecesarios.

#### 3.8.4 Interoperabilidad Gubernamental SOA

La **Arquitectura Orientada a Servicios (SOA)** no es el estilo arquitectónico interno de construcción de software en ONP (donde rigen el Monolito Modular y Microservicios), sino el **estilo mandatorio e institucional de interoperabilidad G2G (Gobierno a Gobierno)** para el intercambio de información con otras entidades del Estado Peruano (RENIEC, SUNAT, ESSALUD, MIDIS, Poder Judicial) a través de la **Plataforma de Interoperabilidad del Estado (PIDE - PCM)** o canales B2G directos.

##### A. Reglas Mandatorias de Consumo SOA / PIDE

1. **Aislamiento Estricto vía Anti-Corruption Layer (ACL):**
   Los servicios gubernamentales externos exponen contratos heterogéneos (SOAP/WSDL, XML antiguo, o REST con esquemas heredados). Está **terminantemente prohibido** que las clases generadas (ej. Stubs JAX-WS, esquemas XML Beans) o DTOs externos penetren en la capa de negocio (`domain.model.*`). Todo consumo SOA exige un adaptador en `infrastructure.client.*` que aplique ACL para traducir la respuesta a los *Value Objects* inmutables de ONP.

2. **Blindaje de Resiliencia Extrema (*Design for Failure*):**
   Dado que los servicios externos gubernamentales están fuera del control operativo de la ONP y pueden presentar latencias impredecibles o ventanas de mantenimiento no avisadas, **toda invocación síncrona a la PIDE / SUNAT / RENIEC** debe implementar de forma obligatoria la siguiente tríada de resiliencia:
   - **Timeout Estricto:** Los umbrales de connection timeout y read timeout se aplican según la categoría del servicio externo definida en la **Matriz de Timeouts (`§3.7.1`)**. No se aplica un rango único: RENIEC (alta demanda, ruta crítica interactiva) exige valores más agresivos que PIDE (demanda media) o SUNAT (proceso diferido/batch).
   - **Circuit Breaker:** Configurado con Resilience4j ([3.7.3](#373-adopción-de-circuit-breaker-con-resilience4j--criterios-y-condicionales)). Los proveedores SOA como RENIEC en ventanilla virtual satisfacen ambos criterios de la sección 3.7.3 (volumetría masiva en ruta crítica interactiva + necesidad de corte automático sin intento de red), por lo que su adopción es obligatoria aquí — no discrecional. Si RENIEC o SUNAT superan el umbral de fallos (ej. 50% en una ventana de 20 peticiones), el circuito se abre para evitar agotar el *pool* de hilos de los servidores de la ONP.
   - **Estrategia de Fallback / Degradación Elegante:** Ante la apertura del circuito o caída externa, el sistema debe devolver un mensaje claro y estandarizado al ciudadano o, si la normativa legal lo permite, consultar una caché local de contingencia de corta duración o encolar el trámite para verificación asíncrona posterior.

3. **Seguridad y Gestión de Credenciales perimetrales:**
   La firma de mensajes SOAP (WS-Security), el intercambio de certificados digitales de cliente (mTLS) y la autenticación con tokens de la PIDE deben ser gestionados en el **API Gateway institucional (WSO2)** o encapsulados en fábricas HTTP especializadas de infraestructura, nunca codificados en la lógica de las aplicaciones.

##### B. Matriz de Patrones Obligatorios para Consumo SOA

```
┌────────────────────────────────────────────────────────────────────────┐
│ MONOLITO MODULAR ONP (onp-expedientes)                                 │
│                                                                        │
│   [ExpedienteService] (Dominio Puro - Java 21)                         │
│           │                                                            │
│           ▼ (inyección por puerto out)                                 │
│   [ReniecConsultaPort]                                                 │
│           │                                                            │
│ ┌─────────┴──────────────────────────────────────────────────────────┐ │
│ │ ADAPTADOR INFRAESTRUCTURA (Infrastructure Layer)                   │ │
│ │                                                                    │ │
│ │   ┌────────────────────────────────────────────────────────────┐   │ │
│ │   │ Resilience4j Circuit Breaker + Timeout (Máx 4s)            │   │ │
│ │   └─────────────────────────────┬──────────────────────────────┘   │ │
│ │                                 │                                  │ │
│ │   ┌─────────────────────────────▼──────────────────────────────┐   │ │
│ │   │ ACL Mapper (Convierte SOAP/XML a Dni/PersonaOnpVO)         │   │ │
│ │   └─────────────────────────────┬──────────────────────────────┘   │ │
│ └─────────────────────────────────┼──────────────────────────────────┘ │
└───────────────────────────────────┼────────────────────────────────────┘
                                    │ mTLS / WS-Security
                                    ▼
                 ┌───────────────────────────────────────┐
                 │ PIDE / RENIEC / SUNAT (Servicios SOA) │
                 └───────────────────────────────────────┘
```

### 3.9 Patrones de Dominio y Relación entre Contextos en el Monolito Modular

El diseño de un **Monolito Modular (Estadio 2)** exige delimitar fronteras estrictas entre sus subdominios funcionales (*Bounded Contexts*). Para garantizar un bajo acoplamiento y posibilitar la extracción limpia a microservicios en el futuro, es obligatorio regirse por dos patrones fundamentales de *Domain-Driven Design (DDD)*:

```
┌────────────────────────────────────────────────────────────────────────┐
│ MONOLITO MODULAR ONP                                                   │
│                                                                        │
│   ┌─────────────────────┐               ┌──────────────────────────┐   │
│   │   onp-expedientes   │               │       onp-aportes        │   │
│   │  (Downstream - D)   │               │     (Upstream - U)       │   │
│   │                     │               │                          │   │
│   │  [ExpedienteService]│               │                          │   │
│   │          │          │               │                          │   │
│   │          ▼          │               │                          │   │
│   │ [AportesQueryPort]  │               │   [AportesPublicApi]     │   │
│   └──────────┬──────────┘               └────────────▲─────────────┘   │
│              │ ACL (Anti-Corruption Layer)           │                 │
│              └───────────────────────────────────────┘                 │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ PD09 - SHARED KERNEL (onp-common-domain)                       │   │
│   │ Tipos Inmutables: Dni, Ruc, MontoMonetario, OnpDomainException │   │
│   └────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

#### 3.9.1 Mapa de Contextos (*Context Map*)

El *Context Map* es el contrato formal y arquitectónico que gobierna cómo interactúan los diferentes módulos o subdominios entre sí dentro del sistema. En ONP se regulan las siguientes cuatro relaciones:

1. **Cliente-Proveedor (*Customer-Supplier* / Upstream-Downstream):** El módulo proveedor (*Upstream*, ej. `onp-aportes`) expone un **API pública en Java** (interfaz de capa de aplicación o DTO inmutable). El módulo consumidor (*Downstream*, ej. `onp-expedientes`) lo consume. Cualquier cambio en los requerimientos del consumidor debe coordinarse en el contrato de esa API.
2. **Capa Anticorrupción (*Anti-Corruption Layer — ACL*, [8.4.3](#843-anti-corruption-layer-acl)):** Obligatoria cuando un módulo nuevo consume datos de un módulo heredado o de un subdominio externo complejo. El consumidor implementa una interfaz propia (*Port*) y un *Adapter* interno que traduce el modelo de datos del proveedor a su propio modelo de dominio, evitando que los cambios en el proveedor rompan sus reglas de negocio.
3. **Conformista (*Conformist*):** Permitido **únicamente** para consultas de catálogos o datos referenciales simples (ej. tipos de documento, ubigeo). El consumidor acepta y utiliza directamente los DTOs de salida del proveedor sin capa de traducción.
4. **Caminos Separados (*Separate Ways*):** Si dos subdominios no tienen relación de negocio coherente (ej. Mesa de Partes Virtual y Cálculo de Reserva Matemática), está **terminantemente prohibido** acoplarlos en código. Deben operar de forma completamente independiente o comunicarse vía eventos de dominio asíncronos en el Bus de Kafka ([3.6](#36-kafka-como-canal-de-transporte)).

#### 3.9.2 Núcleo Compartido (*Shared Kernel*)

El *Shared Kernel* representa el subconjunto estrictamente acotado de código que se comparte libremente en memoria entre todos los *Bounded Contexts* del Monolito Modular (típicamente encapsulado en el módulo Maven `onp-common-domain`).

Debido a que cualquier modificación en el *Shared Kernel* impacta y obliga a recompilar/probar a todos los demás módulos, su contenido está sujeto a un estricto gobierno:

| Elemento | Condición en el Shared Kernel | Ejemplo Aprobado en ONP |
|---|---|---|
| **Value Objects Universales** | **✅ PERMITIDO** | Tipos inmutables transversales a toda la entidad: `Dni`, `Ruc`, `PeriodoTributario`, `MontoMonetario`, `NumeroExpediente`. |
| **Excepciones Base del Dominio** | **✅ PERMITIDO** | Jerarquía raíz de errores: `OnpDomainException`, `RecursoNoEncontradoException`, `ReglaNegocioVioladaException`. |
| **Envolventes / Primitivas de Arquitectura** | **✅ PERMITIDO** | Clases base de paginación (`PageResult<T>`), interfaces marcador (`DomainEvent`, `AggregateRoot`). |
| **Entidades de Base de Datos (`@Entity`)** | **❌ PROHIBIDO TERMINANTEMENTE** | NINGUNA entidad JPA puede compartirse en el Kernel. Cada *Bounded Context* es dueño soberano y exclusivo de sus tablas y entidades. |
| **Lógica de Negocio / Servicios** | **❌ PROHIBIDO TERMINANTEMENTE** | Flujos de cálculo, validaciones de expedientes o reglas previsionales deben vivir exclusivamente en sus módulos respectivos. |
| **Repositorios o Adapters HTTP/JDBC** | **❌ PROHIBIDO TERMINANTEMENTE** | Prohibido incluir acceso a infraestructura en el núcleo de dominio compartido. |

#### 3.9.3 Lenguaje Publicado (*Published Language*)

El patrón *Published Language* define el contrato público, estable y común mediante el cual dos o más *Bounded Contexts* intercambian información. Su objetivo principal es evitar que los consumidores se acoplen a las estructuras internas o modelos de datos privados (`domain.model.*` o `infrastructure.persistence.entity.*`) del proveedor.

En ONP, la exposición de un Lenguaje Publicado es mandatoria para toda comunicación externa a la frontera de un contexto y se articula bajo dos estándares institucionales según el protocolo:

| Tipo de Comunicación | Estándar Institucional | Registro y Gobierno del Contrato | Reglas de Evolución |
|---|---|---|---|
| **Asíncrona (Eventos sobre Bus de Kafka)** | **CloudEvents v1.0 (CNCF)** con esquema de payload en JSON Schema o Avro | **Confluent Schema Registry** institucional (`LIN-BUS-001 sección 5.2`). Decisión regida por `ADR-CLOUDEVENTS-001`. | Inmutabilidad de eventos publicados (ver [Event Immutability](#inmutabilidad-de-eventos-event-immutability)). Evolución bajo regla *Full Compatibility* (prohibido eliminar campos o agregar obligatorios sin valor por defecto). |
| **Síncrona (APIs REST entre módulos/servicios)** | **OpenAPI 3.0+** (YAML/JSON) | Especificación en el repositorio Git del proveedor y publicada en el Portal de APIs (WSO2 / Swagger Hub). | Versionamiento semántico en la URL (`/api/v1/...`). Los DTOs de salida (`application.dto.*`) son inmutables (`records`). |

**Ejemplo de Lenguaje Publicado para Evento de Dominio (CloudEvent v1.0 en JSON):**

```json
{
  "specversion": "1.0",
  "id": "f81d4fae-7dec-11d0-a765-00a0c91e6bf6",
  "source": "urn:onp:core:expedientes",
  "type": "pe.gob.onp.core.expedientes.expedientepresentado.v1",
  "time": "2026-07-02T16:30:00Z",
  "datacontenttype": "application/json",
  "data": {
    "numeroExpediente": "EXP-2026-0008421",
    "dniPensionista": "08241578",
    "tipoTramite": "JUBILACION_GENERAL",
    "fechaPresentacion": "2026-07-02T16:30:00Z"
  }
}
```
> **Nota normativa:** El objeto interior `data` representa el modelo de dominio traducido a lenguaje público (sin identificadores internos de BD de Oracle). Los consumidores procesan únicamente este esquema público sin conocer las entidades JPA del servicio emisor.

#### 3.9.4 Regla de Oro del Acoplamiento en Monolito Modular

> **Regla de Soberanía de Dominio:** Ningún módulo de un Monolito Modular puede hacer un `import` directo a paquetes de la capa `domain.*` o `infrastructure.*` de otro módulo. La comunicación entre módulos se realiza exclusivamente a través de los paquetes explícitos de exposición: `application.api.*` o `application.dto.*`. **El incumplimiento de esta regla se considera un anti-patrón crítico que bloquea el pase a producción en CI/CD**.

### 3.10 CQRS — Separación de Modelos de Escritura y Lectura

CQRS (*Command Query Responsibility Segregation*) separa el modelo de escritura del de lectura: las operaciones que modifican estado (*Commands*) y las que solo consultan (*Queries*) usan modelos, stores y rutas de código distintos. Esto permite optimizar cada lado de forma independiente — el write model para consistencia y durabilidad; el read model para velocidad y forma de consulta.

**En ONP, el write model es relacional con ACID** (Oracle hoy, potencialmente otros motores en el futuro — ver tabla de variante B en [3.10.3](#3103-variante-b--cdc--kafka--nosql-agnóstico-al-motor)). El read model es un store optimizado para el patrón de consulta del caso de uso (ver sección **[5.3](#53-cqrs--elección-del-read-model)**): Redis para lookups por clave, MongoDB para documentos agregados con filtros compuestos, Elasticsearch para búsqueda de texto libre.

CQRS no es el estilo por defecto. Aplica cuando los patrones de lectura son lo suficientemente distintos al modelo de escritura como para justificar la complejidad operativa de mantener dos stores sincronizados.

#### 3.10.1 Cuándo aplicar

| Detonador | Señal observable |
|---|---|
| Consultas complejas sobre el modelo transaccional | Queries con múltiples JOINs, vistas materializadas, índices de función que degradan el rendimiento de Oracle |
| Patrones de lectura radicalmente distintos al modelo de escritura | La pantalla necesita datos de 3 módulos distintos consolidados; el write model los tiene normalizados en tablas separadas |
| Requisito de búsqueda de texto libre o filtros dinámicos | No resolubles eficientemente en Oracle — candidato a Elasticsearch |
| Volumen de lecturas que supera la capacidad transaccional | Reportes y dashboards que compiten con transacciones OLTP en el mismo Oracle |

**Cuándo NO aplicar:**
- CRUD simple donde el modelo de escritura y lectura son el mismo.
- Cuando la complejidad de sincronizar dos stores supera el beneficio de consulta.
- Como solución a problemas de índices o tuning que no han sido resueltos primero en Oracle.

#### 3.10.2 Variante A — Transactional Outbox + Kafka → NoSQL (estándar actual)

La aplicación publica eventos al bus usando el patrón **Transactional Outbox** ([3.6](#36-arquitectura-orientada-a-eventos-eda)). Un consumer Kafka construye y mantiene el read model en el store NoSQL adecuado.

```
┌──────────────────────────────────────────────────────────────┐
│  WRITE SIDE (Command)                                        │
│                                                              │
│  Command → Handler → Oracle (ACID)                           │
│                    └── tabla OUTBOX (misma transacción)      │
└────────────────────────────┬─────────────────────────────────┘
                             │ relay process
                             ▼
                        [ Kafka ]
                             │ consumer / proyección
                             ▼
┌──────────────────────────────────────────────────────────────┐
│  READ SIDE (Query)                                           │
│                                                              │
│  Read Store NoSQL (Redis / Elasticsearch / réplica)          │
│       ↑                                                      │
│  Query → Read Repository → Read Store                        │
└──────────────────────────────────────────────────────────────┘
```

**Características:**
- Requiere código en la aplicación (Outbox table + relay process).
- Compatible con cualquier motor relacional que soporte transacciones.
- Reutiliza la infraestructura Kafka ya institucionalizada.
- La sincronización es explícita y trazable en el código.

**Restricción:** el relay del Outbox no puede fallar silenciosamente — debe tener monitoreo activo y DLQ configurado (ver **LIN-BUS-001 sección 8**).

#### 3.10.3 Variante B — CDC + Kafka → NoSQL (agnóstico al motor)

*Change Data Capture* (CDC) captura los cambios directamente del **log de transacciones del motor de base de datos**, sin modificar el código de la aplicación. La herramienta de referencia es **Debezium**, que publica los cambios como eventos en Kafka.

```
┌──────────────────────────────────────────────────────────────┐
│  WRITE SIDE (Command)                                        │
│                                                              │
│  Command → Handler → BD Relacional (ACID)                    │
│                          │                                   │
│                    transaction log                           │
│                    (redo log / WAL / binlog)                 │
└────────────────────────────┬─────────────────────────────────┘
                             │ Debezium (CDC connector)
                             ▼
                        [ Kafka ]
                             │ consumer / proyección
                             ▼
┌──────────────────────────────────────────────────────────────┐
│  READ SIDE (Query)                                           │
│                                                              │
│  Read Store NoSQL (Redis / Elasticsearch / réplica)          │
│       ↑                                                      │
│  Query → Read Repository → Read Store                        │
└──────────────────────────────────────────────────────────────┘
```

**Características:**
- Cero código en la aplicación — captura cambios a nivel de motor.
- Agnóstico al motor relacional: cada motor tiene su conector Debezium.
- Requiere Kafka Connect como infraestructura adicional.

**Implicaciones por motor:**

| Motor | Mecanismo CDC | Consideraciones en ONP |
|---|---|---|
| **Oracle** | LogMiner o XStream | Requiere supplemental logging habilitado. Puede implicar revisión de licenciamiento Oracle. Validar con DBA e infraestructura antes de adoptar. |
| **PostgreSQL** | WAL (Write-Ahead Log) | Sin costo de licencia adicional. Habilitación simple (`wal_level = logical`). |
| **MySQL / MariaDB** | Binlog | Configuración estándar, ampliamente soportado. |

#### 3.10.4 Elección del read store

La elección del store NoSQL para el read model depende del patrón de consulta, no de la variante CDC/Outbox. Ver sección **[5.3](#53-cqrs--elección-del-read-model) — CQRS: elección del read model**.

#### Reglas ONP

**Adopción mediante ADR obligatorio:** toda implementación CQRS — en cualquiera de sus variantes — requiere **ADR aprobado por Arquitectura OTI**. El ADR debe declarar:
1. El detonador que justifica CQRS (ver sección [3.10.1](#3101-cuando-aplicar-cqrs).
2. La variante elegida (A — Outbox o B — CDC) y la justificación.
3. El motor relacional de escritura y el store NoSQL de lectura elegido (ver sección [5.3](#53-cqrs--elección-del-read-model)).
4. El mecanismo de sincronización y su monitoreo.
5. Si es Variante B con Oracle: validación de LogMiner/XStream con DBA e infraestructura confirmada.

**El ADR define la variante; este lineamiento define las opciones disponibles y los criterios de elección.**

**Event Sourcing no es CQRS:** almacenar el estado como secuencia de eventos (Event Sourcing) es un patrón distinto y de mayor complejidad operativa. Su adopción requiere ADR separado (ver sección [3.6](#36-arquitectura-orientada-a-eventos-eda) — nota sobre Event Sourcing).

### 3.11 Principios rectores transversales

Los patrones y estilos definidos previamente no son decisiones arbitrarias — están gobernados por cuatro principios que actúan como criterio de validación de cualquier decisión arquitectónica en ONP. Aquí se declaran como norma rectora el fundamente de los puntos indicados previamente dentro de la sección 3.

#### Bajo Acoplamiento / Alta Cohesión (*Loose Coupling / High Cohesion*)

**Declaración:** Un módulo, servicio o componente debe conocer y depender del menor número posible de otros módulos (bajo acoplamiento). Internamente, todos sus elementos deben estar relacionados por una única responsabilidad clara (alta cohesión).

**Por qué este principio gobierna las decisiones:**

| Decisión | Cómo aplica |
|---|---|
| Monolito Modular con fronteras explícitas ([3.4](#34-monolito-modular)) | Los módulos se comunican solo por `application.api.*` — sin imports directos entre dominios |
| Regla de Soberanía de Dominio ([3.9.3](#393-lenguaje-publicado-published-language)) | Ningún módulo accede a las tablas o dominio interno de otro |
| Criterios de extracción a microservicios ([3.5](#35-microservicios)) | Un módulo solo se extrae cuando tiene bounded context claro y datos propios — alta cohesión verificada |
| EDA como desacoplamiento ([3.6](#36-arquitectura-orientada-a-eventos-eda)) | El productor no conoce a los consumidores — acoplamiento mínimo por diseño |

**Violación detectable:** dependencia circular entre módulos, imports entre capas `domain.*` de módulos distintos, o un servicio que modifica datos de otro módulo directamente.

---

#### Inmutabilidad de Eventos (*Event Immutability*)

**Declaración:** Un evento publicado en el bus de mensajería es inmutable. Una vez publicado, su contenido no puede modificarse ni eliminarse. Si la realidad de negocio cambia, se publica un nuevo evento que corrige o complementa al anterior — nunca se edita el original.

**Por qué este principio gobierna las decisiones:**

| Decisión | Cómo aplica |
|---|---|
| Published Language / CloudEvents v1.0 ([3.9.3](#393-lenguaje-publicado-published-language)) | Los eventos incluyen `id` único e inmutable + `time` de ocurrencia — el campo `data` no puede mutar tras la publicación |
| Full Compatibility en Schema Registry ([3.9.3](#393-lenguaje-publicado-published-language)) | Prohibido eliminar campos o agregar obligatorios sin valor por defecto — protege la legibilidad histórica de eventos anteriores |
| Transactional Outbox ([3.10.2](#3102-variante-a--transactional-outbox--kafka--nosql-estándar-actual)) | Los registros en la tabla Outbox no se borran ni modifican tras el envío — son el registro histórico del sistema |
| EDA ([3.6](#36-arquitectura-orientada-a-eventos-eda)) | Los consumidores pueden reprocesar eventos desde el inicio del topic; solo es fiable si los eventos son inmutables |

**Violación detectable:** actualización del payload de un evento ya publicado en Kafka, borrado de mensajes fuera de la política de retención del topic, o reutilización del mismo `id` de evento para dos publicaciones con contenidos distintos.

---

#### Fuente Única de Verdad por Dominio (*Single Source of Truth*)

**Declaración:** Cada dato tiene exactamente un sistema de registro autoritativo. Ningún otro sistema puede modificar ese dato directamente — solo puede leerlo o recibir una proyección derivada. Cuando dos sistemas tienen valores distintos para el mismo dato, el sistema propietario es el que manda.

**Por qué este principio gobierna las decisiones:**

| Decisión | Cómo aplica |
|---|---|
| Write model en CQRS ([3.10](#310-cqrs--separación-de-modelos-de-escritura-y-lectura)) | Oracle (o el motor transaccional) es la fuente de verdad. El read model en MongoDB / Redis / Elasticsearch es una proyección derivada — nunca autoritativa |
| Soberanía de dominio ([3.9.3](#393-lenguaje-publicado-published-language)) | Cada Bounded Context es dueño soberano y exclusivo de sus tablas. Otro módulo no puede escribir en ellas |
| Mecanismo de sincronización obligatorio ([3.10](#310-cqrs--separación-de-modelos-de-escritura-y-lectura), [5.3](#53-cqrs--elección-del-read-model)) | Toda proyección debe documentar explícitamente cómo se sincroniza con su fuente de verdad |
| Oracle como motor transaccional principal ([5](#5-estrategia-de-datos)) | La BD relacional con ACID es la fuente de verdad por defecto hasta que un ADR establezca otro sistema como propietario de un dominio específico |

**Violación detectable:** dos sistemas que pueden modificar el mismo dato, un read model sin mecanismo de sincronización documentado, o un módulo que escribe en las tablas de otro.

---

#### Separación de Responsabilidades (*Separation of Concerns*)

**Declaración:** Cada módulo, capa, clase o función debe abordar una única preocupación bien definida. Mezclar responsabilidades distintas en el mismo artefacto dificulta el cambio, las pruebas y el razonamiento sobre el sistema.

**Por qué este principio gobierna las decisiones:**

| Decisión | Cómo aplica |
|---|---|
| Arquitectura en Capas ([3.1](#31-arquitectura-en-capas-layered)) | Presentación, Aplicación, Dominio e Infraestructura son responsabilidades distintas — ninguna capa invade la del otro |
| Arquitectura Hexagonal ([3.2](#32-arquitectura-hexagonal-ports--adapters)) | El dominio no sabe cómo se persiste ni cómo se llama por red — esas son responsabilidades de los adapters |
| CQRS ([3.10](#310-cqrs--separación-de-modelos-de-escritura-y-lectura)) | Escribir estado y consultar estado son responsabilidades distintas con modelos, stores y rutas de código propias |
| Frontend separado del backend ([4](#4-estrategia-de-frontend)) | La presentación y la lógica de negocio son responsabilidades distintas en repositorios y despliegues independientes |

**Violación detectable:** un `@RestController` que accede directamente a un repositorio JPA, una entidad de dominio con anotaciones `@JsonProperty`, o un servicio de negocio que envía correos y calcula pensiones.

---

## 4. Estrategia de Frontend

El frontend de los sistemas ONP se implementa como una SPA separada del backend: proyectos independientes, repositorios independientes, ciclos de despliegue independientes. El backend Java expone únicamente APIs REST; el frontend las consume.

### Framework adoptado

| Prioridad | Framework | Condición de uso |
|---|---|---|
| **1 — Primario** | **Angular** | Opción por defecto para todo proyecto SPA nuevo |
| 2 — Alternativa | React | Solo si existe una restricción técnica o contractual documentada que impide usar Angular |
| 3 — Alternativa | Vue | Igual que React — requiere justificación documentada en ADR |

La preferencia por Angular se basa en: TypeScript estricto obligatorio, estructura comparable a Spring Boot (reduce decisiones de arquitectura propias), CLI robusto, entre otros.

Cuando se usa React o Vue, los estándares de **LIN-FE-ANG-001** aplican igualmente. El framework alternativo no exime del cumplimiento de métricas de performance ni de los anti-patrones prohibidos.

### Modelo de separación

```
┌─────────────────────┐        REST + JSON         ┌──────────────────────┐
│   SPA (Angular)     │  ──────────────────────►   │  Backend Spring Boot │
│                     │  ◄──────────────────────   │  (Java)              │
│  Repositorio propio │        HTTP/HTTPS          │  Repositorio propio  │
│  Pipeline propio    │                            │  Pipeline propio     │
│  Despliegue propio  │                            │  Despliegue propio   │
└─────────────────────┘                            └──────────────────────┘
```

**Reglas de separación:**
- El SPA no accede directamente a la base de datos ni a servicios internos
- Toda lógica de negocio reside en el backend — el frontend solo presenta y valida formato
- La autenticación usa **SAA** (token institucional): el token se gestiona en el SPA, el backend lo valida en cada request vía `SaaTokenValidationFilter`. El objetivo futuro es OAuth2/OIDC con WSO2 (ver LIN-SEC-APP-001)
- CORS configurado explícitamente en el backend — no se usa `*` en producción

### Métricas de performance obligatorias (Core Web Vitals)

ONP adopta **Core Web Vitals** como el framework de medición de performance frontend. Los siguientes umbrales son obligatorios y medibles con Lighthouse:

| Métrica | Qué mide | Umbral mínimo ONP |
|---|---|---|
| **LCP** (Largest Contentful Paint) | Tiempo hasta que el contenido principal es visible | < 2.5s |
| **INP** (Interaction to Next Paint) | Tiempo de respuesta a interacciones del usuario | < 200ms |
| **CLS** (Cumulative Layout Shift) | Estabilidad visual — elementos que no saltan | < 0.1 |
| **FCP** (First Contentful Paint) | Primer elemento visible en pantalla | < 1.8s |
| **TTI** (Time to Interactive) | Cuando la página responde completamente | < 3.5s |
| **TBT** (Total Blocking Time) | Tiempo que el hilo principal está bloqueado | < 200ms |
| **FPS** en animaciones | Fluidez de transiciones y animaciones | ≥ 60fps |

Estas métricas son **gates de calidad en CI/CD** — un build que no las cumple no pasa a producción. Ver **LIN-CICD-001** (en borrador) para la integración de Lighthouse en el pipeline.

### Técnicas obligatorias para cumplir los umbrales

- **Lazy loading de rutas:** cada módulo Angular se carga solo cuando el usuario navega a él (`loadChildren`)
- **Code splitting:** no se genera un bundle único — se divide por módulo funcional
- **Tree shaking:** importar únicamente lo que se usa; prohibido `import * from`
- **Imágenes optimizadas:** formato WebP, dimensiones correctas, atributo `loading="lazy"`
- **Compresión:** gzip o brotli habilitado en el servidor de assets (nginx)
- **Cache de assets:** nombres de archivos con hash para cache busting determinista

> Para técnicas de optimización específicas de Angular — incluyendo optimización nativa del LCP (`preload`, `preconnect`, SSR) y estabilidad del CLS con bloques deferibles (`@defer`) — ver **LIN-FE-ANG-001 sección 15.2**.

### DOM real y DOM virtual

Comprender la diferencia entre DOM real y DOM virtual es obligatorio para cualquier desarrollador frontend en ONP. De este conocimiento se derivan la mayoría de las reglas de anti-patrones de **LIN-FE-ANG-001**.

**DOM real:** es la representación en memoria del árbol de nodos HTML que mantiene el navegador. Cada modificación directa al DOM real (añadir nodos, cambiar estilos, leer dimensiones) puede desencadenar un ciclo de **reflow** (recálculo de layout) y **repaint** (redibujado de píxeles). En páginas complejas, forzar estos ciclos innecesariamente es la causa más común de bajo FPS y alto TBT.

**DOM virtual:** es una representación ligera en memoria del DOM real usada por React y Vue. El framework calcula la diferencia entre el estado anterior y el nuevo (diffing), y aplica al DOM real solo los cambios mínimos necesarios. Esto reduce los ciclos de reflow/repaint.

**Angular no usa DOM virtual.** Usa un mecanismo de **detección de cambios** (Zone.js o Signals a partir de Angular 17) que rastrean qué propiedades cambiaron y actualizan solo los nodos afectados del DOM real.

```
React / Vue                          Angular
───────────────────────────         ──────────────────────────────
Estado cambia                        Estado cambia
    │                                    │
    ▼                                    ▼
Virtual DOM nuevo                    Change Detection
    │                                (Zone.js / Signals)
    ▼                                    │
Diff con Virtual DOM anterior            ▼
    │                                Solo nodos afectados
    ▼                                    │
Mínimos cambios al DOM real              ▼
                                     DOM real actualizado
```

**Por qué esto prohíbe ciertos patrones:**

| Anti-patrón | Por qué viola el modelo |
|---|---|
| `document.getElementById(...).style.color = 'red'` | Bypass del framework — el framework pierde el control del estado del DOM |
| `setTimeout(fn, 0)` para forzar actualización del DOM | Hackea el ciclo de detección de cambios en lugar de usar el mecanismo correcto |
| Leer `element.offsetHeight` dentro de un bucle | Fuerza reflow en cada iteración — colapsa el FPS |
| Mutar directamente props/estado sin pasar por el framework | El diff no detecta el cambio — la UI queda desincronizada del estado |

**Mecanismos correctos por framework:**

| Necesidad | Angular | React | Vue |
|---|---|---|---|
| Forzar detección de cambios | `ChangeDetectorRef.detectChanges()` | `setState()` / `useState` setter | `nextTick()` |
| Acceder a un nodo del DOM | `@ViewChild` + `ElementRef` | `useRef()` | `ref` + `$refs` |
| Ejecutar código tras render | `ngAfterViewInit` | `useEffect(() => {}, [])` | `onMounted` |
| Animaciones eficientes | `@angular/animations` | CSS transitions / Framer Motion | `<Transition>` |

Los anti-patrones JavaScript prohibidos completos (incluyendo `setTimeout(fn, 0)`, `setInterval` sin cleanup, promesas sin manejo de error y uso de `any` en TypeScript) se detallan en **LIN-FE-ANG-001 — Estándar de Diseño Web Frontend Angular**.

---

## 5. Estrategia de Datos

La base de datos relacional Oracle es el **estándar por defecto** de ONP para todo sistema transaccional. Una BD no relacional no reemplaza a Oracle, es una capa complementaria que resuelve un caso de uso específico que el modelo relacional no cubre eficientemente.

> ONP ya opera una BD no relacional en producción: **Elasticsearch**, utilizada por el stack de observabilidad (logs estructurados y trazas — ver LIN-OBS-001). No es un caso hipotético.

### 5.1 Oracle como estándar transaccional

```
BD relacional (Oracle)   → núcleo transaccional siempre
                           ACID obligatorio para cálculo de pensiones,
                           aportes, liquidaciones y expedientes

BD no relacional         → capa complementaria
                           solo cuando el caso de uso lo justifica
                           por sus propios méritos técnicos
```

### 5.2 BD no relacional complementaria

#### Detonadores para analizar BD no relacional

El análisis de adopción se inicia cuando **al menos uno** de los siguientes detonadores se verifica con evidencia medible, no por intuición:

| Detonador | Señal observable | Tipo de BD candidata |
|---|---|---|
| **Patrón de acceso no es CRUD** | Búsqueda de texto libre, consultas por similitud, traversal de relaciones complejas. Síntoma: queries con 6+ JOINs, índices de función sobre VARCHAR2, vistas materializadas para simular búsqueda. | Document store / Search |
| **Volumen de escrituras supera lo que ACID puede sostener** | Tablas `LOG_` o `HIS_` con millones de inserciones diarias que generan contención. La consistencia eventual es aceptable para esos datos. | Column-family / Document store |
| **Esquema cambia más rápido que los ciclos de pase** | Entidades con atributos altamente variables. Síntoma: tablas con columnas `ATRIB_1..N` o patrón EAV (Entity-Attribute-Value). | Document store |
| **Rendimiento de lectura no se resuelve con tuning Oracle** | Reportes o dashboards con tiempos >5 segundos después de aplicar índices, particionamiento y vistas materializadas. El caso de uso tolera consistencia eventual. | Search / Column-family |
| **Datos con ciclo de vida corto y TTL** | Sesiones, tokens, rate limiting, contadores temporales. Síntoma: tablas `TMP_` o `GTT_` que actúan como caché de estado de aplicación. | Key-value / Cache |

#### Tipos de BD no relacional y cuándo aplica en ONP

| Tipo | Tecnologías de referencia | Cuándo aplica en ONP |
|---|---|---|
| **Document store** | MongoDB, Couchbase | Entidades con estructura variable, datos semi-estructurados de integración externa, configuración dinámica |
| **Key-value / Cache** | Redis | Tokens SAA, sesiones, rate limiting, contadores temporales, cualquier dato con TTL explícito |
| **Search** | Elasticsearch | Búsqueda de texto libre sobre expedientes o documentos. Ya en uso productivo para logs y trazas (LIN-OBS-001) |
| **Column-family** | Apache Cassandra | Escritura masiva de eventos, audit logs de alto volumen, series temporales de negocio |
| **Time-series** | InfluxDB, TimescaleDB | Métricas de negocio con altísima frecuencia de escritura. Las métricas de infraestructura ya las cubre Prometheus (LIN-OBS-001) |

#### Lo que NO es un detonador válido

- "NoSQL escala mejor" sin evidencia de que Oracle sea el cuello de botella.
- "El equipo quiere aprender la tecnología".
- "La arquitectura de referencia del proveedor la usa".
- "Es más simple que modelar en relacional" — la simplicidad de escritura no compensa la pérdida de ACID ni las garantías de integridad referencial.

#### Regla de gobierno

La adopción de cualquier BD no relacional en ONP requiere:

1. **ADR aprobado por Arquitectura OTI** con el detonador verificado, el tipo de BD seleccionado y la justificación técnica.
2. **Nuevo lineamiento** que sea específico para la tecnología de un NoSQL, equivalente a LIN-BD-ORA-001 para Oracle, que cubra diseño de datos, operación, seguridad y observabilidad.
3. **Validación de Plataforma** sobre viabilidad operativa en K8s (backups, monitoreo, alta disponibilidad).
4. **Piloto controlado** antes de uso productivo, con criterios de éxito documentados y fecha de evaluación.

Mientras el lineamiento específico de la tecnología no exista, su uso productivo no está autorizado.

### 5.3 CQRS — elección del read model

CQRS separa el modelo de escritura del de lectura. El **write model es relacional con ACID** (Oracle hoy; otros motores posibles en el futuro según [3.10](#310-cqrs--separación-de-modelos-de-escritura-y-lectura)). La elección del read model no es única — depende del patrón de consulta que debe servir.

| Patrón de consulta del read model | Store adecuado | Razón |
|---|---|---|
| Búsqueda por clave o identificador único (DNI, ID expediente) | Redis / Key-value | Latencia sub-milisegundo; proyección precalculada del agregado |
| Documento agregado con múltiples filtros y estructura anidada | **MongoDB** | Proyección rica del agregado de dominio (ej. expediente completo con aportes y prestaciones en un solo documento). Consultas con filtros compuestos, índices flexibles y aggregation pipeline. Ideal cuando el read model es un documento, no una búsqueda de texto libre. |
| Búsqueda de texto libre, filtros múltiples, facetas | Elasticsearch | Índices invertidos, scoring, agregaciones flexibles |
| Consultas que requieren joins moderados y el modelo relacional es el correcto | Read replica relacional | Cuando la complejidad relacional no desaparece al proyectar |
| Agregaciones analíticas, reportes, tendencias históricas | Capa Gold del Medallion | Ver dominio BI más abajo |

**El read model se elige por el patrón de consulta, no por preferencia tecnológica.** Un mismo sistema puede tener más de un read model si sirve en casos de uso con patrones distintos.

**Condición obligatoria:** Todo read model debe tener su mecanismo de sincronización con el write model explícitamente documentado — evento de dominio + Transactional Outbox, CDC, o ELT programado según el caso. Sin ese mecanismo, el read model es un dato desconectado que producirá inconsistencias silenciosas.

### 5.4 Dominio complementario — Business Intelligence

ONP está construyendo una plataforma de BI basada en **Arquitectura Medallion** (Bronze → Silver → Gold) con almacenamiento en Parquet, versionado de datos con Nessie y gobierno con OpenMetadata. Este dominio es **complementario** al stack transaccional Oracle y al modelo CQRS operacional — no los reemplaza.

```
Oracle (OLTP)
    │
    ├── Read model NoSQL      → CQRS operacional (app queries, baja latencia)
    │
    └── Medallion Lakehouse   → BI / Analytics
           Bronze (raw) → Silver (clean) → Gold (biz-ready)
           [Parquet + Nessie]    gobernado por OpenMetadata
```

La capa **Gold** del Medallion es la fuente natural para los read models analíticos de CQRS — reportes, dashboards y análisis histórico. La capa **Bronze/Silver** se alimenta desde Oracle vía ELT y es el mecanismo de sincronización para ese tipo de consultas.

El detalle de la arquitectura de datos — Medallion, Parquet, Nessie, OpenMetadata y gobierno de datos — se define en el lineamiento **LIN-BI-001** (Lineamiento de Explotación y Analítica de Datos).

---

## 6. Estrategias para organizar la lógica de negocio

La elección del modelo de lógica de dominio es una de las decisiones de mayor impacto. Se definen cuatro estrategias en orden creciente de complejidad.

### 6.1 Transaction Script

**Cuándo usar:** Módulos con lógica simple, flujos de trabajo lineales, sin reglas de negocio complejas. La mayoría de módulos CRUD de ONP encajan aquí.

```java
@Service
@Transactional
public class RegistrarPagoService {

    private final PagoRepository pagoRepository;
    private final PensionistaBuscador pensionistaBuscador;

    public RegistroResult ejecutar(RegistrarPagoCommand cmd) {
        Pensionista pensionista = pensionistaBuscador.buscar(cmd.dniPensionista());
        if (!pensionista.estaActivo()) {
            throw new PensionistaNoPuedePagarException(cmd.dniPensionista());
        }
        Pago pago = new Pago(pensionista.getId(), cmd.monto(), cmd.periodo());
        pagoRepository.guardar(pago);
        return new RegistroResult(pago.getId());
    }
}
```

### 6.2 Active Record

**Cuándo usar:** Entidades con lógica de validación propia que no involucra otros agregados. Conveniente con Spring Data JPA cuando las entidades son simples.

```java
@Entity
public class Pensionista {

    @Id
    private Long id;
    private String dni;
    private EstadoPensionista estado;
    private LocalDate fechaNacimiento;

    public void activar() {
        if (this.estado == EstadoPensionista.SUSPENDIDO) {
            throw new EstadoInvalidoException("Pensionista suspendido no puede activarse directamente");
        }
        this.estado = EstadoPensionista.ACTIVO;
    }

    public boolean estaActivo() {
        return EstadoPensionista.ACTIVO.equals(this.estado);
    }

    public int calcularEdad() {
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }
}
```

### 6.3 Table Module

**Cuándo usar:** Lógica que opera sobre colecciones de registros (reportes, cálculos agregados, reglas que aplican a grupos de entidades). Común en módulos de cálculo de pensiones.

```java
@Service
public class CalculoPensionModule {

    public List<ResultadoCalculo> calcularPensionesDelPeriodo(List<Pensionista> pensionistas, Periodo periodo) {
        return pensionistas.stream()
            .filter(Pensionista::estaActivo)
            .map(p -> calcularParaUno(p, periodo))
            .collect(toList());
    }

    private ResultadoCalculo calcularParaUno(Pensionista p, Periodo periodo) {
        BigDecimal base = determinarBase(p);
        BigDecimal factor = obtenerFactor(p.getTipoRegimen(), periodo);
        return new ResultadoCalculo(p.getId(), base.multiply(factor));
    }
}
```

### 6.4 Domain-Driven Design (DDD)

DDD es la estrategia de mayor complejidad y overhead. En ONP **solo se aplica cuando se cumplen los seis criterios siguientes de forma simultánea**. Si alguno no se cumple, se usa Transaction Script o Active Record.

| # | Criterio obligatorio |
|---|---|
| 1 | El sistema es un **sistema core** de ONP (ej. cálculo de pensiones, gestión de aportes, liquidación). No aplica a sistemas de soporte administrativo. |
| 2 | El dominio tiene **reglas de negocio complejas** que cambian frecuentemente y que los expertos del negocio no pueden expresar en términos simples. |
| 3 | Existe un **experto de dominio disponible** (funcionario ONP con conocimiento profundo) que puede colaborar activamente durante el diseño. |
| 4 | El equipo de desarrollo tiene **experiencia previa con DDD**. No se aplica DDD como experimento de aprendizaje en producción. |
| 5 | El módulo tiene **vida útil larga** (más de 5 años) y se prevén cambios frecuentes en las reglas de negocio. |
| 6 | El bounded context está **claramente delimitado** y no comparte lógica de dominio con otros contextos. |

Cuando los seis criterios se cumplen, los conceptos DDD aplicables son: **Agregado raíz**, **Value Object** y **Domain Events**. El siguiente ejemplo ilustra los tres con terminología del dominio ONP — no es código de un sistema real, es una referencia de cómo se estructura cada concepto.

**Ejemplo de referencia — Agregado raíz con Value Object y Domain Events:**

```java
// ─────────────────────────────────────────────────────────────────
// AGREGADO RAÍZ: ExpedientePension
// Encapsula las reglas de negocio y las transiciones de estado.
// Nadie puede modificar el estado del expediente sin pasar por
// sus métodos. Cada operación válida emite un Domain Event.
// ─────────────────────────────────────────────────────────────────
public class ExpedientePension {

    private final ExpedienteId id;
    private final DniPensionista dni;       // Value Object — ver abajo
    private EstadoExpediente estado;
    private final List<DocumentoAdjunto> documentos = new ArrayList<>();
    private final List<DomainEvent> eventos = new ArrayList<>();

    // Regla de negocio: solo se pueden adjuntar documentos si el
    // expediente está en estado BORRADOR. Cualquier otra transición lanza
    // una excepción de dominio (no una excepción técnica genérica).
    public void presentar(List<DocumentoAdjunto> docs) {
        if (this.estado != EstadoExpediente.BORRADOR) {
            throw new ExpedienteNoPuedeModificarseException(this.id);
        }
        this.documentos.addAll(docs);
        this.estado = EstadoExpediente.PRESENTADO;
        // Domain Event: notifica que el expediente fue presentado.
        // Otros bounded contexts (ej. notificaciones) reaccionarán a este evento.
        eventos.add(new ExpedientePresentadoEvent(this.id, LocalDateTime.now()));
    }

    // Regla de negocio: solo se puede aprobar si está EN_REVISION.
    public void aprobar(FuncionarioId aprobador) {
        if (this.estado != EstadoExpediente.EN_REVISION) {
            throw new TransicionEstadoInvalidaException(this.estado, EstadoExpediente.APROBADO);
        }
        this.estado = EstadoExpediente.APROBADO;
        eventos.add(new ExpedienteAprobadoEvent(this.id, aprobador, LocalDateTime.now()));
    }

    // Entrega los eventos acumulados y limpia la lista interna.
    // El Application Service los publica al bus después del commit.
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> copia = new ArrayList<>(eventos);
        eventos.clear();
        return copia;
    }
}

// ─────────────────────────────────────────────────────────────────
// VALUE OBJECT: DniPensionista
// Representa el DNI como tipo de dominio con validación propia.
// Si el valor no es válido, el objeto no puede crearse.
// Esto elimina la necesidad de validar el DNI en cada servicio.
// ─────────────────────────────────────────────────────────────────
public record DniPensionista(String valor) {
    public DniPensionista {
        if (valor == null || !valor.matches("\\d{8}")) {
            throw new DniInvalidoException(valor);
        }
    }
}
```

#### 6.4.1 Guía normativa de Building Blocks DDD en contexto Spring

Al implementar Domain-Driven Design en los sistemas core de ONP sobre el stack Java 21 / Spring Boot 3, es obligatorio diferenciar y aplicar correctamente los bloques de construcción (*Building Blocks*) del dominio. El mayor antipatrón en proyectos Spring que afirman usar DDD es acoplar el modelo de dominio con el framework de persistencia.

##### A. Entidad de Dominio vs. Entidad JPA

En ONP es **prohibido terminantemente** mezclar la Entidad de Dominio con la Entidad JPA (`@Entity`). Deben existir en paquetes y capas distintas:

| Característica | Entidad de Dominio | Entidad JPA (`@Entity`) |
|---|---|---|
| **Capa / Paquete** | `domain.model.*` (Dominio puro) | `infrastructure.persistence.entity.*` (Infraestructura) |
| **Anotaciones de Framework** | **CERO**. Sin `@Entity`, `@Table`, `@Id`, `@Column` ni anotaciones Spring Data. | Obligatorias (`@Entity`, `@Table`, `@Id`, etc.) |
| **Enfoque** | Encapsula reglas de negocio, invariantes, transiciones de estado e identidad conceptual. | Mapeo objeto-relacional (ORM) hacia filas y columnas de Oracle DB. |
| **Exposición de Setters** | **PROHIBIDO `@Setter` o setters públicos abarcadores**. El estado cambia solo mediante métodos de negocio significativos (`aprobar()`, `archivar()`). | Permitido o requerido por Hibernate/JPA para hidratar objetos. |
| **Sincronización** | N/A | Convertida hacia y desde la Entidad de Dominio exclusivamente vía Mappers en el adaptador de repositorio. |

**Ejemplo de diferenciación obligatoria:**

```java
// 1. ENTIDAD DE DOMINIO PURA (domain.model.Pensionista)
public class Pensionista {
    private final PensionistaId id;
    private DniPensionista dni;
    private EstadoPensionista estado;

    public Pensionista(PensionistaId id, DniPensionista dni, EstadoPensionista estado) {
        this.id = id;
        this.dni = dni;
        this.estado = estado;
    }

    // Método de negocio (invariante)
    public void suspenderPago(String motivo) {
        if (this.estado == EstadoPensionista.FALLECIDO) {
            throw new ReglaNegocioVioladaException("No se puede suspender a un pensionista fallecido");
        }
        this.estado = EstadoPensionista.SUSPENDIDO;
    }
}

// 2. ENTIDAD JPA DE INFRAESTRUCTURA (infrastructure.persistence.entity.PensionistaEntity)
@Entity
@Table(name = "T_PENSIONISTA", schema = "PEN")
public class PensionistaEntity {
    @Id
    @Column(name = "ID_PENSIONISTA")
    private Long id;

    @Column(name = "NUM_DNI", nullable = false, length = 8)
    private String dni;

    @Column(name = "COD_ESTADO", nullable = false, length = 20)
    private String estado;

    // Getters y Setters para uso exclusivo de Hibernate y Mappers
}
```

##### B. Value Object en Java 21

Un **Value Object (Objeto de Valor)** modela una característica descriptiva que carece de identidad propia. Dos Value Objects son iguales si sus atributos tienen el mismo valor.

- **Implementación como Java `record`:** En ONP, todo Value Object debe implementarse como `record` de Java 21, lo que garantiza inmutabilidad, semántica de valor (`equals` y `hashCode` automáticos) y concisión.
- **Autovalidación obligatoria en constructor compacto:** Ningún Value Object puede instanciarse en estado inválido. La validación vive dentro de su constructor compacto.
- **Persistencia JPA:** Cuando un Value Object de dominio deba guardarse en base de datos, el adaptador de persistencia lo mapea como campo primitivo en la entidad JPA o utiliza `@Embedded`/`AttributeConverter` en la capa de infraestructura.

```java
// domain.model.MontoPension (Value Object autovalidado)
public record MontoPension(BigDecimal valor, String moneda) {
    public MontoPension {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioVioladaException("El monto de pensión debe ser positivo");
        }
        if (moneda == null || (!moneda.equals("PEN") && !moneda.equals("USD"))) {
            throw new ReglaNegocioVioladaException("Moneda no permitida: " + moneda);
        }
    }
}
```

##### C. Agregado y Agregado Raíz (`PD01` — *Aggregate Root*

Un **Aggregate** es un clúster de entidades y Value Objects que se tratan como una única unidad transaccional para garantizar la consistencia en modificaciones concurrentes.

1. **Frontera de Consistencia Transaccional:** Toda modificación a cualquier entidad dentro del agregado debe realizarse invocando métodos en el ***Aggregate Root***. Ningún servicio de aplicación o adaptador externo puede obtener una referencia directa y modificar una entidad interna del agregado evadiendo la raíz.
2. **Referencia por ID entre Aggregates:** Un Aggregate Root **nunca debe mantener referencias directas en memoria (punteros de objeto Java)** a otro Aggregate Root. La referencia entre aggregates se realiza exclusivamente mediante sus identificadores (Value Objects de ID).
   - *Mal:* `public class Expediente { private Pensionista pensionista; }`
   - *Bien:* `public class Expediente { private PensionistaId pensionistaId; }`
3. **Gestión de Transacciones y Eventos en Spring Boot:**
   - En la capa de aplicación (`application.service.*`), el caso de uso abre una transacción Spring (`@Transactional`), carga el Aggregate Root mediante el puerto de repositorio, ejecuta la operación de negocio en el agregado y guarda el agregado modificado.
   - Las transiciones de estado en el Aggregate Root emiten **Eventos de Dominio (`DomainEvent`)**. La comunicación de cambios hacia otros aggregates o bounded contexts se realiza consumiendo estos eventos de forma asíncrona tras el *commit* de la transacción (utilizando `ApplicationEventPublisher` de Spring o el patrón Outbox §3.6).

---

## 7. Principios de diseño

### 7.1 SOLID

Los cinco principios SOLID son **obligatorios** en todo código producido para ONP. Se aplican a nivel de clase y de módulo.

#### S — Single Responsibility Principle

Cada clase tiene una única razón para cambiar.

```java
// MAL: una clase hace demasiado
public class PensionService {
    public void calcular(Pension p) { /* cálculo */ }
    public void enviarCorreo(Pension p) { /* notificación */ }
    public void guardarEnBD(Pension p) { /* persistencia */ }
    public String formatearPDF(Pension p) { /* presentación */ }
}

// BIEN: cada responsabilidad en su clase
public class CalculoPensionService { public void calcular(Pension p) { } }
public class NotificacionPensionService { public void notificar(Pension p) { } }
public class PensionRepository { public void guardar(Pension p) { } }
public class PensionPdfFormatter { public String formatear(Pension p) { return ""; } }
```

#### O — Open/Closed Principle

Abierto para extensión, cerrado para modificación. Usar abstracciones e interfaces.

```java
// Interfaz cerrada para modificación
public interface CalculadorFactor {
    BigDecimal calcular(Pensionista pensionista, Periodo periodo);
}

// Extensiones sin tocar el código existente
public class FactorRegimen19990 implements CalculadorFactor {
    @Override
    public BigDecimal calcular(Pensionista p, Periodo periodo) {
        return BigDecimal.valueOf(0.025).multiply(BigDecimal.valueOf(p.getAnosAportes()));
    }
}

public class FactorRegimen20530 implements CalculadorFactor {
    @Override
    public BigDecimal calcular(Pensionista p, Periodo periodo) {
        return BigDecimal.valueOf(0.030).multiply(BigDecimal.valueOf(p.getAnosAportes()));
    }
}

// El servicio no cambia al agregar un régimen nuevo
@Service
public class CalculoPensionService {
    private final Map<TipoRegimen, CalculadorFactor> calculadores;

    public BigDecimal calcular(Pensionista p, Periodo periodo) {
        return calculadores.get(p.getTipoRegimen()).calcular(p, periodo);
    }
}
```

#### L — Liskov Substitution Principle

Las subclases deben ser sustituibles por sus tipos base sin alterar el comportamiento esperado.

```java
public abstract class DocumentoPrevisional {
    public abstract boolean esValido();
    // Contrato: esValido() nunca lanza excepción, siempre retorna boolean
}

// MAL: viola LSP — lanza excepción donde el contrato dice que no
public class DeclaracionJurada extends DocumentoPrevisional {
    @Override
    public boolean esValido() {
        throw new UnsupportedOperationException("No implementado");
    }
}

// BIEN: respeta el contrato del tipo base
public class DeclaracionJurada extends DocumentoPrevisional {
    private final LocalDate fechaFirma;
    @Override
    public boolean esValido() {
        return fechaFirma != null && fechaFirma.isBefore(LocalDate.now());
    }
}
```

#### I — Interface Segregation Principle

Interfaces pequeñas y específicas. Los clientes no deben depender de métodos que no usan.

```java
// MAL: interfaz demasiado amplia
public interface PensionRepository {
    void guardar(Pension p);
    Pension buscarPorId(Long id);
    List<Pension> buscarTodos();
    void eliminar(Long id);
    List<Pension> reporteMensual(Periodo periodo);
    BigDecimal sumarMontosPorPeriodo(Periodo periodo);
}

// BIEN: interfaces segregadas por responsabilidad
public interface PensionWriter { void guardar(Pension p); }
public interface PensionReader { Pension buscarPorId(Long id); List<Pension> buscarTodos(); }
public interface PensionReporter { List<Pension> reporteMensual(Periodo p); BigDecimal sumarMontos(Periodo p); }

// La implementación JPA implementa todas; cada servicio depende solo de lo que usa
@Repository
public class PensionJpaRepository implements PensionWriter, PensionReader, PensionReporter {
    // ...
}
```

#### D — Dependency Inversion Principle

Los módulos de alto nivel no dependen de los de bajo nivel. Ambos dependen de abstracciones. Spring Boot facilita este principio mediante inyección de dependencias.

```java
// La capa de aplicación define la abstracción (port de salida)
public interface NotificacionPort {
    void notificar(String destinatario, String mensaje);
}

// La capa de infraestructura implementa la abstracción (adapter)
@Component
public class CorreoNotificacionAdapter implements NotificacionPort {
    private final JavaMailSender mailSender;

    @Override
    public void notificar(String destinatario, String mensaje) {
        // implementación de envío de correo
    }
}

// El servicio de aplicación depende de la abstracción, no de la implementación
@Service
public class AprobarExpedienteService {
    private final NotificacionPort notificacion; // inyectado por Spring

    public void ejecutar(Long expedienteId) {
        // lógica de aprobación...
        notificacion.notificar(pensionista.getCorreo(), "Su expediente fue aprobado");
    }
}
```

### 7.2 Otros principios obligatorios

| Principio | Regla en ONP |
|---|---|
| **DRY** (Don't Repeat Yourself) | Lógica de negocio duplicada en dos lugares = deuda técnica crítica. Si dos servicios calculan lo mismo, se extrae a una clase de dominio compartida. |
| **KISS** (Keep It Simple) | La solución más simple que funciona correctamente es la correcta. No se diseña para casos de uso hipotéticos. |
| **YAGNI** (You Aren't Gonna Need It) | No se implementa funcionalidad que no tenga un requerimiento concreto y aprobado. |
| **Separation of Concerns** | Cada clase o función aborda una única preocupación. Un `@RestController` no contiene lógica de negocio; una entidad de dominio no tiene anotaciones `@JsonProperty`. Ver declaración formal y contexto arquitectónico en la sección **[3.11](#311-principios-rectores-transversales)**. |

---

## 8. Patrones de diseño de código

Los patrones de esta sección provienen del catálogo GoF (Gang of Four), del modelo de capas (Repository, Mapper) y del lenguaje DDD (ACL). Su uso debe ser justificado — no se aplican por costumbre sino porque resuelven un problema concreto.

| Grupo | Patrones | Secciones |
|---|---|---|
| **GoF — Estructurales** | Adapter, Decorator, Facade | [8.1.1](#811-adapter-integración-externa), [8.1.2](#812-decorator-comportamiento-adicional-sin-modificar-la-clase), [8.1.3](#813-facade-simplificación-de-subsistemas-interno) |
| **GoF — Creacionales** | Factory Method, Builder, Singleton | [8.2.1](#821-factory-method-creación-compleja-o-variable-por-tipo), [8.2.2](#822-builder-construcción-paso-a-paso), [8.2.3](#823-singleton-instancia-única) |
| **GoF — Comportamiento** | Strategy, Observer, Command, State | [8.3.1](#831-strategy-algoritmo-intercambiable), [8.3.2](#832-observer-notificación-asíncrona), [8.3.3](#833-command-ejecución-asíncrona), [8.3.4](#834-state-ciclo-de-vida-con-transiciones-controladas) |
| **Capas y DDD** | Repository, Mapper, ACL | [8.4.1](#841-repository-aislamiento-de-persistencia), [8.4.2](#842-mapper-transformación-entre-capas), [8.4.3](#843-anti-corruption-layer-acl) |

---

### 8.1 GoF — Estructurales

Patrones que se ocupan de cómo se componen clases y objetos para formar estructuras más grandes.

#### 8.1.1 Adapter (Integración externa)

**Propósito:** Convertir la interfaz de un sistema externo a la interfaz que el dominio espera.  
**Categoría:** Estructural (GoF).  
**Cuándo usar:** Integraciones con sistemas externos (por ejemplo: RENIEC, SUNAT, PIDE, PLAME, entre otros).

```java
// Port esperado por el dominio
public interface ConsultaReniecPort {
    DatosPersona consultarPorDni(String dni);
}

// Adapter que habla con RENIEC
@Component
@RequiredArgsConstructor
public class ReniecHttpAdapter implements ConsultaReniecPort {

    private final RestClient reniecClient;
    private final ReniecResponseMapper mapper;

    @Override
    public DatosPersona consultarPorDni(String dni) {
        ReniecResponse response = reniecClient.get()
            .uri("/personas/{dni}", dni)
            .retrieve()
            .body(ReniecResponse.class);
        return mapper.toDomain(response);
    }
}
```

#### 8.1.2 Decorator (Comportamiento adicional sin modificar la clase)

**Propósito:** Añadir responsabilidades a un objeto de forma dinámica sin modificar su clase ni usar herencia.  
**Categoría:** Estructural (GoF).  
**Cuándo usar en ONP:** Cuando se necesita añadir comportamiento transversal (caché, logging, auditoría, métricas) a una implementación existente sin tocarla. Útil para decorar repositorios con caché o clientes externos con registro de tiempos de respuesta.

```java
// Port de dominio (sin cambios)
public interface PensionistaRepository {
    Optional<Pensionista> buscarPorDni(String dni);
    Pensionista guardar(Pensionista pensionista);
}

// Implementación original (sin cambios)
@Repository
@RequiredArgsConstructor
class PensionistaJpaRepository implements PensionistaRepository {
    private final PensionistaJpaEntityRepository jpa;
    private final PensionistaMapper mapper;

    @Override
    public Optional<Pensionista> buscarPorDni(String dni) {
        return jpa.findByDni(dni).map(mapper::toDomain);
    }

    @Override
    public Pensionista guardar(Pensionista pensionista) {
        return mapper.toDomain(jpa.save(mapper.toEntity(pensionista)));
    }
}

// Decorator que añade caché — envuelve la implementación sin modificarla
@Component
@Primary
@RequiredArgsConstructor
class CachedPensionistaRepository implements PensionistaRepository {

    private final PensionistaJpaRepository delegado;
    private final Cache<String, Pensionista> cache;

    @Override
    public Optional<Pensionista> buscarPorDni(String dni) {
        Pensionista cached = cache.getIfPresent(dni);
        if (cached != null) return Optional.of(cached);
        Optional<Pensionista> resultado = delegado.buscarPorDni(dni);
        resultado.ifPresent(p -> cache.put(dni, p));
        return resultado;
    }

    @Override
    public Pensionista guardar(Pensionista pensionista) {
        Pensionista guardado = delegado.guardar(pensionista);
        cache.put(guardado.getDni(), guardado);
        return guardado;
    }
}
```

**Regla ONP:** el Decorator implementa el mismo port que el objeto que decora — Spring inyecta el decorador gracias a `@Primary`. El objeto decorado es package-private para que solo el decorador pueda instanciarlo directamente.

#### 8.1.3 Facade (Simplificación de subsistema interno)

**Propósito:** Ocultar la complejidad de un subsistema interno detrás de una interfaz simplificada, sin cruzar fronteras de red ni integrar sistemas externos.  
**Categoría:** Estructural (GoF).  
**Cuándo usar en ONP:** Cuando un caso de uso de aplicación necesita coordinar varios componentes internos del mismo módulo y exponer esa coordinación como una operación simple. Aplica dentro de la capa `application/` o como clase de soporte en `domain/`.

**Distinción clave con el Facade Arquitectónico de Integración ([3.8.1](#381-facade-arquitectónico-de-integración)):**

| | GoF Facade ([8.1.3](#813-facade-simplificación-de-subsistema-interno)) | Facade Arquitectónico ([3.8.1](#381-facade-arquitectónico-de-integración)) |
|---|---|---|
| **Alcance** | Dentro de un módulo — sin red | Infraestructura de integración — sistemas externos |
| **Qué oculta** | Complejidad de colaboración interna | Heterogeneidad de protocolos y sistemas externos |
| **Dónde vive** | `application/` o `domain/` | `infrastructure/` |
| **ACL / Resiliencia** | No aplica | Obligatorio |

```java
// Subsistema de cálculo de pensión — tres servicios internos
@Component
@RequiredArgsConstructor
class CalculoPensionFacade {

    private final HistorialAportesService historialService;
    private final TablasBeneficiosService tablasBeneficios;
    private final ReglasElegibilidadService reglasElegibilidad;

    public ResultadoCalculo calcularPension(String codigoPensionista) {
        HistorialAportes historial = historialService.obtener(codigoPensionista);
        reglasElegibilidad.validar(historial);
        return tablasBeneficios.calcular(historial);
    }
}

// El Application Service solo ve la fachada
@Service
@RequiredArgsConstructor
public class LiquidarPensionService implements LiquidarPensionUseCase {

    private final CalculoPensionFacade calculoFacade;
    private final PensionRepository pensionRepository;

    @Override
    @Transactional
    public void liquidar(String codigoPensionista) {
        ResultadoCalculo resultado = calculoFacade.calcularPension(codigoPensionista);
        pensionRepository.registrar(resultado.toPension());
    }
}
```

**Regla ONP:** el GoF Facade no implementa lógica de negocio — solo coordina. Si la coordinación requiere decisiones de dominio, esa lógica pertenece al Aggregate Root o a un Domain Service, no a la fachada.

---

### 8.2 GoF — Creacionales

Patrones que abstraen el proceso de creación de objetos, haciéndolo independiente del tipo concreto.

#### 8.2.1 Factory Method (Creación compleja o variable por tipo)

**Propósito:** Centralizar la lógica de creación de objetos complejos o que varían según tipo, sin exponer la clase concreta al llamador.  
**Categoría:** Creacional (GoF).  
**Cuándo usar:** Cuando la construcción de un objeto requiere lógica de negocio, validaciones, o varía según parámetros de dominio.

```java
@Component
public class ExpedienteFactory {

    public Expediente crearInicial(SolicitanteId solicitante, TipoRegimen regimen) {
        return switch (regimen) {
            case REGIMEN_19990 -> new ExpedienteRegimen19990(
                ExpedienteId.nuevo(),
                solicitante,
                LocalDateTime.now(),
                EstadoExpediente.BORRADOR
            );
            case REGIMEN_20530 -> new ExpedienteRegimen20530(
                ExpedienteId.nuevo(),
                solicitante,
                LocalDateTime.now(),
                EstadoExpediente.BORRADOR
            );
        };
    }
}
```

#### 8.2.2 Builder (Construcción paso a paso)

**Propósito:** Construir objetos complejos paso a paso, especialmente cuando tienen muchos campos opcionales.  
**Categoría:** Creacional (GoF).  
**Cuándo usar:** Objetos con más de 4 parámetros o con campos opcionales. En ONP: DTOs de respuesta, objetos de configuración, criterios de búsqueda.

```java
public class BusquedaPensionistaCriteria {

    private final String dni;
    private final TipoRegimen regimen;
    private final EstadoPensionista estado;
    private final LocalDate fechaInscripcionDesde;
    private final LocalDate fechaInscripcionHasta;
    private final int pagina;
    private final int tamano;

    private BusquedaPensionistaCriteria(Builder builder) {
        this.dni = builder.dni;
        this.regimen = builder.regimen;
        this.estado = builder.estado;
        this.fechaInscripcionDesde = builder.fechaInscripcionDesde;
        this.fechaInscripcionHasta = builder.fechaInscripcionHasta;
        this.pagina = builder.pagina;
        this.tamano = builder.tamano;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String dni;
        private TipoRegimen regimen;
        private EstadoPensionista estado;
        private LocalDate fechaInscripcionDesde;
        private LocalDate fechaInscripcionHasta;
        private int pagina = 0;
        private int tamano = 20;

        public Builder dni(String dni) { this.dni = dni; return this; }
        public Builder regimen(TipoRegimen r) { this.regimen = r; return this; }
        public Builder estado(EstadoPensionista e) { this.estado = e; return this; }
        public Builder fechaDesde(LocalDate f) { this.fechaInscripcionDesde = f; return this; }
        public Builder fechaHasta(LocalDate f) { this.fechaInscripcionHasta = f; return this; }
        public Builder pagina(int p) { this.pagina = p; return this; }
        public Builder tamano(int t) { this.tamano = t; return this; }

        public BusquedaPensionistaCriteria build() { return new BusquedaPensionistaCriteria(this); }
    }
}

// Uso
BusquedaPensionistaCriteria criteria = BusquedaPensionistaCriteria.builder()
    .regimen(TipoRegimen.REGIMEN_19990)
    .estado(EstadoPensionista.ACTIVO)
    .fechaDesde(LocalDate.of(2020, 1, 1))
    .pagina(0)
    .tamano(50)
    .build();
```

> **Nota:** Lombok `@Builder` es una alternativa válida para simplificar la implementación. Se prefiere cuando el equipo ya usa Lombok en el proyecto.

#### 8.2.3 Singleton (Instancia única gestionada por Spring)

**Propósito:** Garantizar una única instancia de una clase durante toda la vida de la aplicación.  
**Categoría:** Creacional (GoF).  
**Cuándo usar en ONP:** Spring Boot gestiona este ciclo de vida automáticamente. Los beans `@Service`, `@Repository`, `@Component` son singleton por defecto. **No implementar Singleton manualmente** — declarar el bean con la anotación Spring correspondiente.

```java
// MAL: Singleton manual en un contexto Spring
public class ConfiguracionSistema {
    private static ConfiguracionSistema instancia;
    private ConfiguracionSistema() {}
    public static ConfiguracionSistema getInstance() {
        if (instancia == null) instancia = new ConfiguracionSistema(); // no es thread-safe
        return instancia;
    }
}

// BIEN: Spring gestiona la instancia única
@Configuration
public class ConfiguracionSistema {
    @Value("${onp.sistema.nombre}")
    private String nombre;

    @Bean
    public SomeSharedComponent componenteCompartido() {
        return new SomeSharedComponent(nombre);
    }
}
```

El Singleton manual solo se justifica en utilidades puras sin dependencias de Spring que se usan en contextos donde el contenedor no está disponible (ej. pruebas unitarias sin contexto).

---

### 8.3 GoF — Comportamiento

Patrones que definen cómo los objetos se comunican y distribuyen responsabilidades entre sí.

#### 8.3.1 Strategy (Algoritmo intercambiable)

**Propósito:** Encapsular una familia de algoritmos detrás de una interfaz común, permitiendo intercambiarlos sin modificar el contexto que los usa.  
**Categoría:** Comportamiento (GoF).  
**Cuándo usar en ONP:** Cuando la lógica de negocio varía según un tipo o categoría y quieres evitar bloques `if/switch` en el servicio. Caso típico: cálculo de pensión varía por régimen (19990 vs 20530).

```java
// Interfaz común para todas las estrategias
public interface CalculoPensionStrategy {
    ResultadoCalculo calcular(HistorialAportes historial);
    boolean aplica(TipoRegimen regimen);
}

// Estrategia para Régimen 19990
@Component
public class CalculoRegimen19990Strategy implements CalculoPensionStrategy {

    @Override
    public ResultadoCalculo calcular(HistorialAportes historial) {
        BigDecimal monto = historial.getMesesAportados()
            .multiply(FACTOR_MENSUAL_19990);
        return new ResultadoCalculo(monto, TipoRegimen.REGIMEN_19990);
    }

    @Override
    public boolean aplica(TipoRegimen regimen) {
        return TipoRegimen.REGIMEN_19990.equals(regimen);
    }
}

// Estrategia para Régimen 20530 (cédula viva)
@Component
public class CalculoRegimen20530Strategy implements CalculoPensionStrategy {

    @Override
    public ResultadoCalculo calcular(HistorialAportes historial) {
        BigDecimal monto = historial.getRemuneracionReferencial()
            .multiply(PORCENTAJE_CEDULA_VIVA);
        return new ResultadoCalculo(monto, TipoRegimen.REGIMEN_20530);
    }

    @Override
    public boolean aplica(TipoRegimen regimen) {
        return TipoRegimen.REGIMEN_20530.equals(regimen);
    }
}

// El contexto delega en la estrategia correcta — Spring inyecta todas las implementaciones
@Service
@RequiredArgsConstructor
public class CalculoPensionService {

    private final List<CalculoPensionStrategy> estrategias;

    public ResultadoCalculo calcular(HistorialAportes historial, TipoRegimen regimen) {
        return estrategias.stream()
            .filter(e -> e.aplica(regimen))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Sin estrategia para régimen: " + regimen))
            .calcular(historial);
    }
}
```

**Regla ONP:** cada estrategia es un `@Component` independiente. Spring los inyecta como `List<CalculoPensionStrategy>` en el contexto — no se necesita un `switch` ni un registro manual.

#### 8.3.2 Observer (Domain Events con Spring Events)

**Propósito:** Notificar a múltiples componentes de un cambio de estado sin acoplarlos al emisor.  
**Categoría:** Comportamiento (GoF).  
**Cuándo usar en ONP:** Cuando un Aggregate Root emite Domain Events y otros componentes del sistema necesitan reaccionar sin conocerse mutuamente. La implementación Spring es `ApplicationEventPublisher` + `@EventListener`.

```java
// 1. El Domain Event — inmutable, sin dependencias de framework
public record AporteRegistradoEvent(
    String codigoPensionista,
    BigDecimal monto,
    YearMonth periodo,
    Instant occurredOn
) {}

// 2. El Application Service publica el evento tras confirmar la transacción
@Service
@RequiredArgsConstructor
public class RegistrarAporteService implements RegistrarAporteUseCase {

    private final AporteRepository aporteRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void registrar(RegistrarAporteCommand cmd) {
        Aporte aporte = new Aporte(cmd.codigoPensionista(), cmd.monto(), cmd.periodo());
        aporteRepository.guardar(aporte);
        eventPublisher.publishEvent(new AporteRegistradoEvent(
            cmd.codigoPensionista(), cmd.monto(), cmd.periodo(), Instant.now()
        ));
    }
}

// 3. Observadores desacoplados — no saben nada del servicio emisor
@Component
public class NotificacionAporteListener {

    @EventListener
    @Async
    public void onAporteRegistrado(AporteRegistradoEvent evento) {
        // notificar al pensionista — en hilo separado, no bloquea la transacción
    }
}

@Component
public class AuditoriaAporteListener {

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAporteRegistrado(AporteRegistradoEvent evento) {
        // registrar en log de auditoría en transacción separada e independiente
    }
}
```

**Regla ONP:** los Domain Events se publican dentro de la transacción del caso de uso. Los listeners que requieren aislamiento transaccional usan `Propagation.REQUIRES_NEW`. Los que no deben bloquear el hilo principal usan `@Async`.

#### 8.3.3 Command (Operación encapsulada como objeto)

**Propósito:** Encapsular una solicitud de negocio como un objeto inmutable, separando quien la invoca de quien la ejecuta.  
**Categoría:** Comportamiento (GoF).  
**Cuándo usar en ONP:** En el lado de escritura del patrón CQRS ([3.10](#310-cqrs--separación-de-modelos-de-escritura-y-lectura)). Cada operación de negocio que modifica estado se modela como un `record` inmutable que viaja desde el Controller hasta el Application Service (Use Case).

```java
// Command — record inmutable con validación en el constructor compacto
public record RegistrarAporteCommand(
    String codigoPensionista,
    BigDecimal monto,
    YearMonth periodo,
    String usuarioRegistrador
) {
    public RegistrarAporteCommand {
        Objects.requireNonNull(codigoPensionista, "codigoPensionista requerido");
        Objects.requireNonNull(monto, "monto requerido");
        if (monto.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("monto debe ser positivo");
    }
}

// El Controller construye el Command desde el DTO de entrada
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/aportes")
public class AporteController {

    private final RegistrarAporteUseCase registrarAporte;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void registrar(@RequestBody @Valid RegistrarAporteRequest request,
                          @AuthenticationPrincipal String usuario) {
        registrarAporte.registrar(new RegistrarAporteCommand(
            request.codigoPensionista(),
            new BigDecimal(request.monto()),
            YearMonth.of(request.anio(), request.mes()),
            usuario
        ));
    }
}
```

**Regla ONP:** los Commands son `record` de Java 21 — inmutables por construcción. La validación de negocio va en el constructor compacto del `record`, no en el Controller ni en el Application Service.

#### 8.3.4 State (Ciclo de vida con transiciones controladas)

**Propósito:** Permitir que un objeto altere su comportamiento cuando su estado interno cambia, delegando las transiciones válidas en el propio estado.  
**Categoría:** Comportamiento (GoF).  
**Cuándo usar en ONP:** Cuando un objeto de dominio tiene un ciclo de vida con estados bien definidos y reglas de transición estrictas. En ONP: `Expediente` (BORRADOR → EN_REVISION → APROBADO | RECHAZADO), `Pension` (CALCULADA → APROBADA → PAGADA | SUSPENDIDA).

```java
// Los estados encapsulan sus transiciones válidas — una transición inválida lanza excepción
public enum EstadoExpediente {

    BORRADOR {
        @Override public EstadoExpediente presentar() { return EN_REVISION; }
    },
    EN_REVISION {
        @Override public EstadoExpediente aprobar()  { return APROBADO; }
        @Override public EstadoExpediente rechazar() { return RECHAZADO; }
    },
    APROBADO,
    RECHAZADO;

    public EstadoExpediente presentar() {
        throw new IllegalStateException("Transición 'presentar' no válida desde: " + this);
    }
    public EstadoExpediente aprobar() {
        throw new IllegalStateException("Transición 'aprobar' no válida desde: " + this);
    }
    public EstadoExpediente rechazar() {
        throw new IllegalStateException("Transición 'rechazar' no válida desde: " + this);
    }
}

// El Aggregate Root expone los métodos de transición — nunca se setea el estado directamente
public class Expediente {

    private EstadoExpediente estado = EstadoExpediente.BORRADOR;

    public void presentar() {
        this.estado = estado.presentar();
        // emitir ExpedientePresentadoEvent
    }

    public void aprobar(String motivo) {
        this.estado = estado.aprobar();
        // emitir ExpedienteAprobadoEvent
    }

    public void rechazar(String motivo) {
        this.estado = estado.rechazar();
        // emitir ExpedienteRechazadoEvent
    }
}
```

**Regla ONP:** ningún servicio de aplicación ni repositorio asigna directamente el estado de un Aggregate Root (`expediente.setEstado(...)`). Toda transición ocurre a través de los métodos de negocio del propio agregado, que validan la transición y emiten el Domain Event correspondiente.

---

### 8.4 Capas y DDD

Patrones de estructuración que no son GoF pero son fundamentales en la arquitectura en capas y el modelo de dominio de ONP.

#### 8.4.1 Repository (Aislamiento de persistencia)

**Propósito:** Aislar la lógica de dominio del mecanismo de persistencia.  
**Categoría:** Patrón de acceso a datos (DDD / Fowler).  
**Cuándo usar:** Siempre que se acceda a una base de datos desde la capa de aplicación.

```java
// Port (capa de dominio) — interfaz Java pura, sin imports de JPA
public interface PensionistaRepository {
    Optional<Pensionista> buscarPorDni(String dni);
    Pensionista guardar(Pensionista pensionista);
    List<Pensionista> buscarActivosPorRegimen(TipoRegimen regimen);
}

// Adapter (capa de infraestructura) — implementa el port con JPA
@Repository
@RequiredArgsConstructor
public class PensionistaJpaRepository implements PensionistaRepository {

    private final PensionistaJpaEntityRepository jpa;
    private final PensionistaMapper mapper;

    @Override
    public Optional<Pensionista> buscarPorDni(String dni) {
        return jpa.findByDni(dni).map(mapper::toDomain);
    }

    @Override
    public Pensionista guardar(Pensionista pensionista) {
        PensionistaEntity entity = mapper.toEntity(pensionista);
        return mapper.toDomain(jpa.save(entity));
    }
}
```

#### 8.4.2 Mapper (Transformación entre capas)

**Propósito:** Transformar objetos entre capas (DTO ↔ Domain ↔ Entity) sin mezclar responsabilidades.  
**Categoría:** Patrón de capas (Translator).  
**Cuándo usar:** Siempre que se transfieran datos entre capas.

```java
@Component
public class PensionMapper {

    public PensionResponseDto toDto(Pension pension) {
        return new PensionResponseDto(
            pension.getId(),
            pension.getMonto().toPlainString(),
            pension.getPeriodo().toString(),
            pension.getEstado().name()
        );
    }

    public Pension toDomain(RegistrarAporteCommand cmd) {
        return new Pension(
            null,
            new Monto(new BigDecimal(cmd.monto())),
            Periodo.of(cmd.anio(), cmd.mes()),
            EstadoPension.BORRADOR
        );
    }

    public PensionEntity toEntity(Pension pension) {
        PensionEntity e = new PensionEntity();
        e.setId(pension.getId());
        e.setMonto(pension.getMonto().valor());
        e.setPeriodo(pension.getPeriodo().toString());
        e.setEstado(pension.getEstado().name());
        return e;
    }
}
```

#### 8.4.3 Anti-Corruption Layer (ACL)

**Propósito:** Aislar el dominio de ONP de los modelos de datos de sistemas externos. Evita que conceptos externos contaminen el modelo de dominio propio.  
**Categoría:** Patrón DDD.  
**Cuándo usar:** Obligatorio en integraciones con RENIEC, SUNAT, PIDE, PLAME y cualquier sistema externo del Estado.

```java
// El dominio ONP tiene su propio modelo
public record DatosPersona(String dni, String nombres, String apellidos, LocalDate fechaNacimiento) {}

// El sistema RENIEC retorna su propio modelo (no controlado por ONP)
public class ReniecResponse {
    public String numDni;
    public String primerNombre;
    public String segundoNombre;
    public String apePaterno;
    public String apeMaterno;
    public String fecNacimiento; // formato "dd/MM/yyyy" — diferente al estándar ONP
}

// El mapper ES la Anti-Corruption Layer
@Component
public class ReniecResponseMapper {
    private static final DateTimeFormatter FORMATO_RENIEC = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DatosPersona toDomain(ReniecResponse response) {
        String nombres = Stream.of(response.primerNombre, response.segundoNombre)
            .filter(n -> n != null && !n.isBlank())
            .collect(joining(" "));
        String apellidos = response.apePaterno + " " + response.apeMaterno;
        LocalDate fechaNacimiento = LocalDate.parse(response.fecNacimiento, FORMATO_RENIEC);
        return new DatosPersona(response.numDni, nombres, apellidos, fechaNacimiento);
    }
}
```

---

## 9. Estructura de proyecto

ONP define tres estructuras de proyecto según el estilo arquitectónico del sistema. La elección sigue directamente del estilo declarado en la sección [3](#3-estilos-y-patrones-de-arquitectura) — no es libre.

| Estructura | Estilo | Cuándo usar |
|---|---|---|
| **Monolito simple (capas)** | Layered | Sistema sin candidatura a microservicio; lógica Transaction Script o Active Record |
| **Monolito Modular** | Modular | Punto de llegada por defecto para todo sistema nuevo |
| **Hexagonal / Clean** | Hexagonal | Módulo que cumple los seis criterios de microservicio (3.5) — obligatorio antes de extraer |

### 9.1 Monolito simple (capas)

Tres capas con responsabilidades claras: presentación (`controller`), aplicación/negocio (`service`), persistencia (`repository`). La regla de dependencia fluye hacia abajo: `controller → service → repository`. Los controllers no acceden a repositorios directamente.

Un único módulo Maven (un solo `pom.xml`, no multi-módulo), con paquetes organizados por capa:

```
onp-sistema/
│   pom.xml                    ← POM único — packaging: jar
└── src/main/java/
    └── pe/gob/onp/sistema/
        ├── controller/        ← Controllers, DTOs de entrada/salida
        ├── service/           ← Services, lógica de negocio
        ├── repository/        ← Interfaces Spring Data JPA
        ├── domain/            ← Entidades JPA, enumerados
        ├── exception/         ← Excepciones propias + @ControllerAdvice global
        └── config/            ← Configuración Spring (beans, CORS, seguridad)
```

> **Regla:** los `controller/` dependen de `service/`; los `service/` dependen de `repository/` y `domain/`. Ningún controller accede directamente a un repositorio.

**Concerns transversales — dónde van**

Las carpetas que no son una capa (`auth/`, `util/`, `health/`, `common/`) no tienen lugar propio en la estructura — su contenido pertenece a alguna de las capas existentes:

| Si tienes... | Va en... | Por qué |
|---|---|---|
| Filtros de seguridad, Spring Security config | `config/` | Es configuración de infraestructura |
| Endpoints de login / logout / token | `controller/` | Son presentación como cualquier otro endpoint |
| Spring Actuator, health checks | `config/` | Son beans de configuración |
| Utilidades de dominio (formateos de RUC, DNI) | `domain/` | Pertenecen al dominio, no a una capa técnica |
| Utilidades técnicas (parsers, conversores) | `config/` | Si no son dominio, son infraestructura técnica |
| DTOs compartidos entre controllers | `controller/` | Los DTOs son presentación |
| Constantes de negocio | `domain/` | Son parte del modelo de dominio |

> **Señal de alerta:** si un proyecto tiene carpetas `util/`, `common/` o `shared/` como carpetas de primer nivel, es síntoma de que esas clases no encontraron su lugar arquitectónico. La pregunta correcta no es "¿dónde pongo esto?" sino "¿a qué capa pertenece esto?".

### 9.2 Monolito Modular

Cinco módulos Maven con fronteras explícitas. Las dependencias fluyen hacia el interior: `boot → api/infrastructure → application → domain`. El módulo `domain` no depende de ningún otro módulo del proyecto. Esta es la estructura de destino por defecto para todo sistema nuevo en ONP.

**Estructura Maven** — dos niveles: padre que coordina, hijos que implementan:

```
onp-sistema/                              ← POM padre — packaging: pom, sin código Java
│   pom.xml                                 <dependencyManagement>: centraliza versiones
│                                           de librerías compartidas (Spring Boot, Lombok,
│                                           MapStruct, etc.). Los hijos heredan versiones
│                                           y solo declaran la dependencia sin versión.
│
├── onp-expedientes/                      ← módulo hijo: Expedientes
│   │   pom.xml                           ← POM hijo, declara <parent>: onp-sistema
│   └── src/main/java/
│       ├── api/                          ← paquete: Controllers, DTOs
│       ├── application/                  ← paquete: Services, casos de uso
│       ├── domain/                       ← paquete: Entidades, ports
│       └── infrastructure/               ← paquete: JPA, clientes HTTP
│
├── onp-aportes/                          ← módulo hijo: Aportes
│   │   pom.xml                           ← POM hijo, declara <parent>: onp-sistema
│   └── src/main/java/
│       ├── api/
│       ├── application/
│       ├── domain/
│       └── infrastructure/
│
├── onp-prestaciones/                     ← módulo hijo: Prestaciones
│   │   pom.xml                           ← POM hijo, declara <parent>: onp-sistema
│   └── src/main/java/
│       ├── api/
│       ├── application/
│       ├── domain/
│       └── infrastructure/
│
└── onp-boot/                             ← módulo hijo — packaging: jar
        pom.xml                           ← POM hijo, declara <parent>: onp-sistema
                                             depende de onp-expedientes, onp-aportes, etc.
                                             produce el JAR ejecutable final
```

> **Regla:** toda librería usada por más de un módulo se declara en `<dependencyManagement>` del padre. Los hijos agregan la dependencia sin versión — Maven la hereda. Nunca se duplican versiones entre módulos hijos.

### 9.3 Hexagonal / Clean

Tres anillos concéntricos con una única dirección de dependencia permitida: `infrastructure → application → domain`. Si una clase en `domain` importa `jakarta.persistence` o `org.springframework`, la frontera está rota.

Un único módulo Maven con paquetes organizados por anillo:

```
onp-modulo/
│   pom.xml                              ← POM único — packaging: jar
└── src/main/java/
    └── pe/gob/onp/modulo/
        ├── domain/
        │   ├── model/                   ← Entidades, Value Objects, Agregados
        │   │                               (Java puro — sin imports de Spring ni JPA)
        │   └── port/out/                ← interfaces que el dominio necesita del exterior
        │                                   (ej. PensionistaRepository, ReniecPort)
        ├── application/
        │   ├── port/in/                 ← interfaces de casos de uso
        │   │                               (ej. RegistrarAporteUseCase)
        │   └── service/                 ← implementan port/in, orquestan domain,
        │                                   usan domain/port/out
        └── infrastructure/
            ├── adapter/
            │   ├── in/
            │   │   └── rest/            ← Controllers REST (llaman a application/port/in)
            │   └── out/
            │       ├── persistence/     ← JPA (implementan domain/port/out)
            │       └── client/          ← Clientes HTTP externos (RENIEC, SUNAT, PIDE)
            └── config/                  ← Configuración Spring, beans, seguridad
```

> **Regla:** `domain/` es Java puro — cero imports de `jakarta.*` o `org.springframework.*`. `application/` puede usar anotaciones Spring en los services (`@Service`, `@Transactional`). Todo lo demás de infraestructura vive en `infrastructure/`.

> Para la estructura concreta de paquetes, convenciones de nomenclatura y configuración Maven de cada estilo, ver **LIN-DEV-JAVA-001 sección 12**.

---

## 10. Estrategia de pruebas

### 10.1 Pirámide de pruebas por estilo arquitectónico

La distribución de la pirámide de pruebas la determina el **estilo arquitectónico** — no la estrategia de lógica de dominio. Son dos dimensiones distintas:

- **Estilo arquitectónico** ([3](#3-estilos-y-patrones-de-arquitectura)): define cómo está estructurado el sistema → determina la proporción de la pirámide
- **Estrategia de lógica de dominio** ([6](#6-estrategias-para-organizar-la-lógica-de-negocio)): define cómo se organiza la lógica de negocio → afecta el foco y complejidad de las pruebas unitarias, no la proporción general

```
                      ╱╲
                     ╱  ╲        ← E2E / aceptación
                    ╱────╲          (pocas, lentas, frágiles)
                   ╱      ╲
                  ╱────────╲     ← Integración
                 ╱          ╲       (moderadas)
                ╱────────────╲
               ╱              ╲  ← Unitarias
              ╱────────────────╲    (muchas, rápidas, deterministas)
```

**Distribución por estilo arquitectónico:**

| Estilo arquitectónico | Unitarias | Integración | E2E | Cobertura mínima |
|---|---|---|---|---|
| Monolito simple (Layered) | 60% | 35% | 5% | 70% líneas |
| Monolito Modular | 55% | 35% | 10% | 75% líneas |
| Hexagonal (candidato a MS) | 70% | 25% | 5% | 80% líneas |
| Microservicio | 60% | 30% | 10% | 80% líneas |
| EDA (consumidor Kafka) | 50% | 45% | 5% | 75% líneas |

**Efecto de la estrategia de dominio sobre las pruebas unitarias:**

| Estrategia de dominio ([6](#6-estrategias-para-organizar-la-lógica-de-negocio)) | Efecto en pruebas unitarias |
|---|---|
| Transaction Script | Unitarias sobre el Service — requieren mocks de repositorios |
| Active Record | Unitarias sobre la entidad — lógica en el modelo, fáciles de aislar |
| Table Module | Unitarias sobre el módulo con colecciones en memoria |
| DDD — Value Object | Pruebas sobre `record` Java puro — sin Spring, sin mocks; validan invariantes del constructor compacto |
| DDD — Aggregate Root | Pruebas sobre el agregado completo — sin Spring, sin mocks de infra; verifican transiciones de estado y eventos de dominio emitidos |

> Los porcentajes detallados, herramientas obligatorias, naming conventions y gates de CI/CD se definen en **LIN-TEST-001 — Estándar de Pruebas ONP**.

### 10.2 Qué se prueba por capa

| Capa | Tipo de prueba | Herramientas |
|---|---|---|
| Domain | Unitaria pura (sin Spring) | JUnit 5 |
| Application | Unitaria con mocks | JUnit 5 + Mockito |
| Infrastructure (JPA) | Integración con BD real | `@DataJpaTest` + Testcontainers |
| Infrastructure (HTTP) | Integración con servidor simulado | WireMock |
| API (Controllers) | Slice test | `@WebMvcTest` + MockMvc |
| Sistema completo | Integración full | `@SpringBootTest` + Testcontainers |

---

## 11. Arquitectura de despliegue

### 11.1 Contextos de despliegue en ONP

ONP define tres contextos de despliegue. Lo importante no es solo qué es cada contexto sino **cómo se llega a él** — hay dos caminos distintos según si el sistema es legacy o nuevo.

```
SISTEMA LEGACY                         SISTEMA NUEVO
(ya existe en JBoss/WebLogic)          (desarrollo nuevo)
        │                                      │
        ▼                                      │
┌───────────────────┐                          │
│   LEGACY          │                          │
│  JBoss / WebLogic │                          │
│  (sin cambio)     │                          │
└────────┬──────────┘                          │
         │  Strangler Fig                      │
         │  (migracion gradual)                │
         ▼                                     │
┌───────────────────┐                          │  Solo si existe
│   TRANSICION      │                          │  restriccion
│  Docker +         │◄─────────────────────────┤  documentada
│  Docker Compose   │                          │  en ADR
└────────┬──────────┘                          │
         │  cuando la                          │
         │  infraestructura K8s                │
         │  esta disponible                    │
         ▼                                     ▼
┌──────────────────────────────────────────────────┐
│                    TARGET                        │
│                    K8s                           │
│   (destino de todo sistema nuevo y migrado)      │
└──────────────────────────────────────────────────┘
```

**Reglas ONP:**
- **Sistema nuevo → K8s directamente.** No pasa por Transición salvo que exista una restricción técnica o de infraestructura documentada en un ADR.
- **Sistema legacy → Transición primero, luego K8s.** La migración usa Strangler Fig (ver [2.2](#22-patrón-de-migración-strangler-fig-y-feature-toggles)). No se salta la etapa de contenedores: es donde se valida que la aplicación funciona correctamente en Docker antes de orquestarla en K8s.
- **Transición no es un destino final** — es una etapa temporal de migración.

| Contexto | Descripción | Runtime | Backend | Frontend SPA |
|---|---|---|---|---|
| **Legacy** | Sistemas existentes sin modificar | JVM en servidor de aplicación | JBoss / WebLogic | N/A |
| **Transición** | Etapa temporal de migración de legacy hacia K8s | Docker Engine | Docker + Docker Compose | nginx en Docker sirviendo build estático |
| **Target** | Destino de todo sistema nuevo y todo sistema migrado | **containerd** (via K8s) | K8s | K8s Ingress + nginx (`ng build --configuration production`) |

**Nota sobre el runtime en K8s:** el clúster ONP usa **containerd** como container runtime, no Docker. El `Dockerfile` sigue siendo el estándar de construcción de imágenes — produce imágenes OCI compatibles con containerd. La diferencia aplica en operación: para inspeccionar contenedores en los nodos se usa `crictl`, no `docker`. Docker Engine solo existe en la etapa de Transición (Docker Compose) y en los entornos de desarrollo local.

**Aprovisionamiento de la infraestructura:** los tres contextos de esta tabla (Legacy, Transición, Target) describen el destino de ejecución; cómo se aprovisiona esa infraestructura de forma declarativa y reproducible (clústeres K8s, redes, cuentas de servicio) se rige por **LIN-IaC-001 — Infraestructura como Código**, con Terraform/Ansible como estándar on-premise.

### 11.2 Estándar de contenedores

Todo sistema nuevo (Estadio 2 en adelante) debe entregarse con Dockerfile. El estándar es:

```dockerfile
# Etapa 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY onp-expedientes/pom.xml onp-expedientes/
COPY onp-aportes/pom.xml onp-aportes/
COPY onp-prestaciones/pom.xml onp-prestaciones/
COPY onp-boot/pom.xml onp-boot/
RUN ./mvnw dependency:go-offline -q
COPY . .
RUN ./mvnw package -DskipTests -q

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S onp && adduser -S onp -G onp
WORKDIR /app
COPY --from=build /app/onp-{sistema}-boot/target/*.jar app.jar
RUN chown onp:onp app.jar
USER onp
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Reglas obligatorias:** multi-stage build, usuario no root, imagen base Alpine, JRE (no JDK) en runtime.

### 11.3 Requerimientos Kubernetes por estilo arquitectónico

| Estilo | Réplicas mínimas | Liveness probe | Readiness probe | SLO mínimo |
|---|---|---|---|---|
| Monolito Modular | 2 | `/actuator/health/liveness` | `/actuator/health/readiness` | 99.0% |
| Microservicio | 2 | `/actuator/health/liveness` | `/actuator/health/readiness` | 99.5% |

Los SLOs se definen y miden con el stack de observabilidad OTEL/Prometheus/Grafana. Ver **LIN-OBS-001 — Log, Trazabilidad y Observabilidad ONP** para la configuración completa.

> **Detalles de Dockerfile, política de namespaces, tagging de imágenes, recursos CPU/memoria y restricciones de seguridad en pods:** ver **LIN-K8S-001 — Contenedores y Orquestación ONP**.

### 11.4 K8s vs VM — criterio de decisión

El contenedor en K8s es el destino por defecto para todo sistema nuevo. Una VM se justifica solo cuando se cumple al menos uno de los siguientes criterios, y la decisión debe documentarse en un ADR:

| Criterio para usar VM | Ejemplo en ONP |
|---|---|
| **Base de datos con requisitos de I/O específicos** | Oracle RAC, instancias con storage dedicado y tuning de SO — el overhead de contenedor impacta la performance |
| **Dependencia de hardware físico** | Licencias de software atadas a MAC address o número de CPU, dongles, acceso a dispositivos específicos |
| **Aplicación legacy no contenorizable** | Sistema que requiere instalación de agentes en el SO, servicios de Windows, o dependencias de registro del sistema |
| **Requisito regulatorio o de auditoría** | Normativa que exige que el sistema corra en infraestructura dedicada sin multitenancy |

**Fuera de estos criterios, elegir VM sobre K8s requiere justificación explícita en ADR.** No se acepta "es más simple" o "el equipo no conoce Kubernetes" como criterio — eso se resuelve con capacitación, no con deuda arquitectónica.

### 11.5 Observabilidad como requisito arquitectónico

Todo sistema que llega a producción en ONP — independientemente del estilo arquitectónico — debe implementar los cuatro pilares de observabilidad. **No hay excepciones.** Un sistema sin observabilidad no está listo para producción.

| Pilar | Qué se requiere | Stack ONP |
|---|---|---|
| **Trazas distribuidas** | Todo request HTTP genera un trace con spans por operación. Las operaciones lentas o con error son visibles en Jaeger. | Micrometer Tracing + OTEL Collector → Jaeger |
| **Logs estructurados** | Logs en formato JSON con campos mínimos: `traceId`, `spanId`, `timestamp`, `level`, `service`, `message`. Sin logs en texto plano en producción. | Logback + LogstashEncoder → OTEL Collector → Elasticsearch/Kibana |
| **Métricas** | El servicio expone métricas de JVM, HTTP y negocio. Prometheus las recoge. Grafana las visualiza. El dashboard mínimo obligatorio cubre las cuatro señales doradas (ver abajo). | Spring Actuator + Micrometer → Prometheus → Grafana |
| **Health checks** | Endpoints de liveness y readiness implementados y configurados en el manifiesto K8s. El servicio que no responde se reinicia automáticamente. | `/actuator/health/liveness` y `/actuator/health/readiness` |

#### Marco de referencia — Four Golden Signals (Google SRE)

El dashboard de Grafana obligatorio definido en **LIN-OBS-001 sección 9.3** está diseñado para cubrir las cuatro señales doradas del libro *Site Reliability Engineering* de Google. Estas señales son suficientes para diagnosticar el comportamiento de cualquier servicio en producción:

| Señal | Qué mide | Métrica ONP (Micrometer / Prometheus) |
|---|---|---|
| **Latencia** | Tiempo que tarda el servicio en responder — incluyendo la latencia de las respuestas de error, que no debe confundirse con la latencia de los éxitos. | `http.server.requests` — percentiles P50, P95, P99 |
| **Tráfico** | Volumen de demanda sobre el sistema en un instante dado. | `http.server.requests` — tasa de peticiones (req/s) |
| **Errores** | Tasa de peticiones que fallan — explícitamente (HTTP 5xx) o implícitamente (HTTP 200 con contenido incorrecto). | `http.server.requests` — tasa de respuestas 4xx y 5xx |
| **Saturación** | Qué tan "lleno" está el servicio: los recursos más restringidos determinan la capacidad máxima. | Uso de memoria JVM, conexiones activas al pool de BD, uso de CPU del pod |

> **Por qué estas cuatro:** cualquier degradación de un servicio se manifiesta primero en una o más de estas señales. Monitorear solo logs o solo trazas no es suficiente; las métricas de las cuatro señales permiten detectar problemas antes de que los usuarios los reporten.

**Checklist mínimo antes de pasar a producción:**

- [ ] `spring-boot-starter-actuator` incluido y configurado
- [ ] `logback-spring.xml` con `LogstashEncoder` y `OpenTelemetryAppender`
- [ ] Dependencias OTEL/Micrometer presentes en `pom.xml`
- [ ] Configuración por perfil (`application-{env}.yml`) definida y `SPRING_PROFILES_ACTIVE` configurado en despliegue; si existen overrides OTEL, son gestionados por Plataforma según `LIN-K8S-001`
- [ ] Liveness y readiness probe declarados en el Deployment de K8s
- [ ] Al menos un dashboard de Grafana con métricas del servicio
- [ ] Traza de prueba visible en Jaeger antes del go-live

> El detalle completo de configuración — dependencias Maven, `application-{env}.yml`, `logback-spring.xml`, `@NewSpan`, retención y dashboards mínimos — está en **LIN-OBS-001** (Lineamiento de Log Centralizado, Trazabilidad y Observabilidad).

---

## 12. Perfil del contratista por estilo arquitectónico

ONP no tiene desarrolladores propios de planta. Todo el desarrollo es contratado. Este perfil define qué debe validarse en un proceso de contratación según el estilo arquitectónico del sistema a desarrollar.

### 12.1 Perfil base (obligatorio para cualquier contratación)

Independientemente del estilo, todo proveedor o profesional contratado debe demostrar:

- Java 21 LTS (uso de records, sealed classes, pattern matching, virtual threads)
- Spring Boot 3.x (autoconfiguración, actuator, profiles)
- Maven (multi-módulo como mínimo)
- REST con Spring MVC y documentación OpenAPI 3 (Swagger)
- Pruebas unitarias con JUnit 5 + Mockito
- Git (branching, pull requests, conventional commits)
- Docker (Dockerfile básico)

### 12.2 Perfiles por estilo

| Estilo | Habilidades adicionales requeridas | Señal de alarma |
|---|---|---|
| **Transaction Script / Active Record** | JPA/Hibernate, Spring Data, manejo de transacciones | No conoce `@Transactional` o usa `SELECT *` |
| **Monolito Modular** | Maven multi-módulo, diseño de módulos con fronteras explícitas, SOLID | No puede explicar por qué evitar dependencias circulares |
| **Hexagonal** | Ports & Adapters, inyección de dependencias avanzada, pruebas de dominio sin Spring | Mezcla lógica de negocio en Controllers o Repositories |
| **Microservicios** | Spring Cloud o Kubernetes-native, circuit breaker, distributed tracing, gestión de transacciones distribuidas | No conoce Saga ni las consecuencias de la consistencia eventual |
| **DDD** | Bounded contexts, agregados, value objects, domain events, CQRS básico | No puede distinguir un agregado de una entidad JPA |
| **Frontend SPA (Angular)** | Angular 17+, TypeScript estricto, RxJS, lazy loading de rutas, Angular CLI, Core Web Vitals, Lighthouse | Usa `setTimeout(fn, 0)` para resolver problemas de timing; no conoce el event loop; manipula el DOM directamente |

### 12.3 Validación en el proceso de contratación

**Prueba técnica recomendada por perfil:**

- **Base + Transaction Script:** Implementar un CRUD con validaciones de negocio, manejo de excepciones y pruebas unitarias. Tiempo: 3 horas.
- **Modular:** Diseñar la estructura de módulos Maven para un sistema dado y justificar las decisiones. Tiempo: 2 horas.
- **Hexagonal:** Implementar un port de salida con su adapter y prueba de integración con WireMock. Tiempo: 4 horas.
- **Microservicios:** Diseñar la comunicación entre dos servicios incluyendo manejo de fallo y trazabilidad. Tiempo: 3 horas.
- **DDD:** Modelar un agregado con su lógica de dominio, eventos de dominio y pruebas unitarias puras. Tiempo: 5 horas.

---

## Apéndice A — Decisiones arquitectónicas registradas

| ID | Decisión | Fecha | Estado |
|---|---|---|---|
| ADR-001 | Java + Spring Boot + Maven es el stack obligatorio para backend nuevo | 2026-05-21 | Aceptada |
| ADR-002 | Monolito Modular como punto de llegada por defecto para sistemas nuevos | 2026-05-21 | Aceptada |
| ADR-003 | DDD requiere los seis criterios simultáneos; no se usa en sistemas de soporte | 2026-05-21 | Aceptada |
| ADR-004 | Strangler Fig como patrón de migración de sistemas legacy | 2026-05-21 | Aceptada |
| ADR-005 | Anti-Corruption Layer obligatorio en integraciones con RENIEC, SUNAT, PIDE, PLAME | 2026-05-21 | Aceptada |
| ADR-006 | Angular como framework SPA primario; React y Vue permitidos solo con justificación documentada en ADR | 2026-05-21 | Aceptada |
| ADR-007 | Core Web Vitals como framework de medición de performance frontend; umbrales obligatorios como gate de CI/CD | 2026-05-21 | Aceptada |
| ADR-008 | Todo microservicio debe declarar explícitamente su elección CAP (CP o AP) en el ADR de extracción; sin esa decisión no pasa a producción | 2026-05-21 | Aceptada |
| ADR-009 | K8s con containerd como runtime de producción; Docker Engine solo en desarrollo local y etapa de Transición | 2026-05-21 | Aceptada |
| ADR-010 | Observabilidad (trazas, logs estructurados, métricas, health checks) es requisito obligatorio de producción; ningún sistema se despliega sin los cuatro pilares | 2026-05-21 | Aceptada |
| ADR-011 | K8s es el destino por defecto; el uso de VM requiere criterio documentado en ADR | 2026-05-21 | Aceptada |
| ADR-012 | LIN-BUS-001 formaliza y reemplaza la regla transitoria de mensajería; Apache Kafka es el broker institucional aprobado; toda adopción de EDA debe cumplir LIN-BUS-001 | 2026-06-05 | Aceptada |
| ADR-013 | CloudEvents v1.0 (CNCF) como estándar institucional de estructura para todos los eventos del bus; habilita interoperabilidad con instituciones del Estado y ecosistema cloud-native | 2026-06-08 | Aceptada |
| ADR-014 | Feature Toggle (PA14) con Unleash (self-hosted) como plataforma estándar on-premise para Trunk-Based Development (ver LIN-VER-001 §6.1); LaunchDarkly y otros SaaS requieren ADR adicional aprobado por Arquitectura OTI + Seguridad | 2026-07-02 | Aceptada |

---

## Apéndice B — Glosario rápido

| Término | Definición en el contexto ONP |
|---|---|
| **Monolito Modular** | Sistema único desplegable organizado en módulos Maven con fronteras explícitas y sin dependencias circulares |
| **Port** | `interface` Java que define el contrato entre el dominio y el mundo exterior — dice QUÉ puede hacer o necesitar el sistema, sin decir CÓMO. **Port de entrada:** lo que el exterior puede hacer con el dominio (ej. `RegistrarAporteUseCase`). **Port de salida:** lo que el dominio necesita del exterior (ej. `PensionistaRepository`). |
| **Adapter** | Implementación concreta de un port que conecta el dominio con un sistema externo o mecanismo de infraestructura |
| **ACL (Anti-Corruption Layer)** | Mapper o conjunto de mappers que traduce el modelo de un sistema externo al modelo de dominio de ONP |
| **Bounded Context** | Límite explícito dentro del cual un modelo de dominio es coherente y tiene significado único |
| **Strangler Fig** | Patrón de migración donde el sistema nuevo reemplaza progresivamente funcionalidades del legacy sin reescritura total |
| **Transaction Script** | Estrategia de lógica de dominio donde cada operación de negocio es un procedimiento secuencial en un servicio |
| **Feature Toggle** | Mecanismo que permite habilitar o deshabilitar fragmentos de código en tiempo de ejecución sin redespliegue (`PA14`) |
| **Published Language** | Estándar de contrato explícito (CloudEvents v1.0 o OpenAPI 3.0) para el intercambio de datos entre Bounded Contexts sin exponer modelos internos (`PD10`) |
| **CQRS** | Command Query Responsibility Segregation — patrón que separa el modelo de escritura (comandos) del modelo de lectura (queries) en modelos físicamente distintos para optimizar cada operación de forma independiente (`PA07`) |
| **CDC (Change Data Capture)** | Técnica que captura cambios en la base de datos a nivel de log binario (ej. Debezium + Oracle LogMiner/XStream) para propagar modificaciones al read model sin polling activo — variante B del patrón CQRS en ONP |
| **ADR** | Architecture Decision Record — registro formal de una decisión de arquitectura con contexto, decisión y consecuencias |
| **SLO** | Service Level Objective — objetivo cuantitativo de confiabilidad de un servicio (ej. 99.5% de disponibilidad mensual) |
