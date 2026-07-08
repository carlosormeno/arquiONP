# Lineamiento de Estándar de Diseño de Software y Patrones Tácticos en la ONP

**Código:** LIN-DIS-001  
**Versión:** 0.1.0  
**Fecha:** 2026-07-08  
**Autor:** Oficina de Tecnologías de la Información — ONP  
**Estado:** Vigente / Estándar de Nivel 2  
**Clasificación:** Lineamiento táctico de diseño. Segundo nivel en la jerarquía arquitectónica institucional. Aterriza las decisiones macro dictadas en `LIN-ARQ-001` hacia el diseño interior de contenedores, módulos, capas, límites de dominio e interfaces. De cumplimiento obligatorio por Tech Leads, Arquitectos de Software y Desarrolladores Senior.

---

## 1. Introducción y Relación con el Nivel 1 (`LIN-ARQ-001`)

### 1.1 Del Nivel 1 (Macro) al Nivel 2 (Táctico): El Interior del Contenedor

Mientras que el Marco Rector (`LIN-ARQ-001`) decide **cuáles** son los sistemas, en qué estadios se ubican (Monolito Modular vs. Microservicios) y bajo qué protocolos de red interactúan, el presente estándar de Nivel 2 (`LIN-DIS-001`) prescribe **cómo diseñar la estructura interna y el código de cada uno de esos módulos o microservicios**.

```
┌──────────────────────────────────────────────────────────────────────────┐
│ NIVEL 1: MARCO RECTOR (`LIN-ARQ-001`)                                    │
│ Decisión: "El módulo onp-aportes es un Bounded Context del Monolito      │
│           Modular y expone servicios REST / Kafka CloudEvents v1.0".     │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │ Define el límite externo
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│ NIVEL 2: ESTÁNDAR TÁCTICO (`LIN-DIS-001` - PRESENTE DOCUMENTO)           │
│ Decisión: "Dentro de onp-aportes se aplica Arquitectura Hexagonal con    │
│           aislamiento de dominio, Agregados DDD, y patrón ACL para SOA". │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │ Guía el diseño de paquetes e interfaces
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│ NIVEL 3: ESTÁNDAR DE DESARROLLO JAVA (`LIN-DEV-JAVA-001`)                │
│ Decisión: "La interfaz AportesPort se implementa en AportesJpaAdapter    │
│           usando @Service, @Transactional y Patrones GoF Spring".        │
└──────────────────────────────────────────────────────────────────────────┘
```

### 1.2 Principios Rectores Tácticos

Todo diseño modular interno y estructuración de clases en los sistemas ONP debe responder a tres principios innegociables:

1. **Alta Cohesión y Bajo Acoplamiento (`PRA07`):** Un paquete o módulo (`pe.gob.onp.core.expedientes`) debe agrupar elementos íntimamente relacionados por un único propósito de negocio (Alta Cohesión). A la vez, debe conocer y depender del menor número posible de otros paquetes institucionales (Bajo Acoplamiento).
2. **Fuente Única de Verdad Táctica (`PRA10` / *Single Source of Truth*):** Dentro de un sistema, cada concepto previsional (ej. `Expediente`, `Pensionista`, `Liquidacion`) es soberano de una sola entidad o agregado. Prohibido duplicar el estado autoritativo de un mismo dato en múltiples tablas o módulos internos.
3. **Separación de Responsabilidades (`PR09` / *Separation of Concerns*):** Las reglas de presentación (JSON/DTOs), la orquestación transaccional (`Use Cases`), las reglas puras del negocio (`Entities`/`Value Objects`) y la infraestructura de acceso a datos (`JPA`/`SQL`) son capas ontológicamente distintas que jamás deben mezclarse en una misma clase Java.

---

## 2. Arquitectura Táctica de Módulos y Capas

La estructura de carpetas y dependencias internas de un módulo Java se rige por uno de los dos estilos tácticos oficiales, cuya elección no es estética sino que depende de la complejidad funcional del componente.

### 2.1 Arquitectura en Capas (*Layered Architecture*)

**Cuándo usar en la ONP:** Módulos de soporte, cruds administrativos simples, catálogos auxiliares o flujos lineales que no encierran reglas previsionales ni validaciones de negocio complejas.

```
┌──────────────────────────────────────────────────────────┐
│ pe.gob.onp.soporte.catalogos.api (Presentación / REST)   │
└────────────────────────────┬─────────────────────────────┘
                             │ DTOs de Entrada / Salida
                             ▼
┌──────────────────────────────────────────────────────────┐
│ pe.gob.onp.soporte.catalogos.service (Aplicación)        │
└────────────────────────────┬─────────────────────────────┘
                             │ LLama directamente
                             ▼
┌──────────────────────────────────────────────────────────┐
│ pe.gob.onp.soporte.catalogos.repository (Persistencia/JPA│
└──────────────────────────────────────────────────────────┘
```

- **Regla de Dependencia en Capas:** La dependencia fluye unidireccionalmente hacia abajo: `api -> service -> repository`. Queda prohibido que un `Controller` en `api` acceda directamente a un `Repository` saltándose la capa `service`.

### 2.2 Arquitectura Hexagonal (*Ports & Adapters*)

**Cuándo usar en la ONP:** **Obligatorio** para todos los módulos del *Core Previsional* (Aportes, Pensiones, Expedientes, Liquidaciones, Tesorería), módulos con tres o más integraciones externas y cualquier módulo candidato a extraerse en el futuro como un microservicio independiente (*Estadio 3*).

```
         ADAPTADORES DE ENTRADA                 CAPA DE DOMINIO PURA                ADAPTADORES DE SALIDA
         (pe.gob.onp.*.infrastructure.in)     (pe.gob.onp.*.domain)               (pe.gob.onp.*.infrastructure.out)
       ┌──────────────────────────────┐     ┌──────────────────────────────┐     ┌──────────────────────────────┐
       │                              │     │   ┌──────────────────────┐   │     │                              │
───────┼─► [@RestController]          ├─────┼───► [RegistrarAportePort]│   │     │   [PensionistaJpaAdapter]    ├─────► [Oracle DB]
       │   (AporteController)         │     │   │     (Port in)        │   │     │               │              │
       │                              │     │   └──────────┬───────────┘   │     │               ▲              │
       │                              │     │              │               │     │               │              │
───────┼─► [KafkaEventListener]       │     │              ▼               │     │   ┌───────────┴──────────┐   │
       │   (AporteConsumerAdapter)    │     │   ┌──────────────────────┐   │     │   │PensionistaRepository │   │
       └──────────────────────────────┘     │   │RegistrarAporteService│   │     │   │      (Port out)      │   │
                                            │   │    (Use Case)        │   │     │   └──────────────────────┘   │
                                            │   └──────────┬───────────┘   │     │                              │
                                            │              │               │     │   ┌──────────────────────┐   │
                                            │              ▼               │     │   │  ReniecHttpAdapter   ├─────► [RENIEC SOA]
                                            │   ┌──────────────────────┐   │     │   │      (Adapter)       │   │
                                            │   │ Aporte / Pensionista │   │     │   └───────────▲──────────┘   │
                                            │   │ (Agregado / Entidad) │   │     │               │              │
                                            │   └──────────────────────┘   │     │   ┌───────────┴──────────┐   │
                                            │                              │     │   │  ConsultaReniecPort  │   │
                                            │                              │     │   │      (Port out)      │   │
                                            └──────────────────────────────┘     └───┴──────────────────────┴───┘
```

#### Regla de Oro del Paquete `domain` (Aislamiento Absoluto)
> **Mandato Arquitectónico Innegociable:** El paquete `domain/` (tanto sus modelos como sus interfaces o puertos) es **Java puro desprovisto de dependencias de infraestructura o frameworks técnicos**. Queda **terminantemente prohibido** que cualquier clase o interfaz ubicada dentro de `domain/` contenga sentencias `import` hacia:
> - `jakarta.persistence.*` / `javax.persistence.*` (`@Entity`, `@Table`, `@Id`)
> - `org.springframework.*` (`@Autowired`, `@Service`, `@Transactional`, `@Component`)
> - `com.fasterxml.jackson.*` (`@JsonProperty`, `@JsonIgnore`)
> **El incumplimiento de esta regla rompe el modelo hexagonal, acopla el negocio a la base de datos y causará el rechazo inmediato del Pull Request en revisión de código.**

#### Puertos vs. Adaptadores en la ONP
- **Puertos de Entrada (`port.in`):** Interfaces Java que expresan lo que el exterior puede solicitarle al dominio (ej. `RegistrarAporteUseCase`, `AprobarExpedienteUseCase`).
- **Puertos de Salida (`port.out`):** Interfaces Java dentro de `domain/` que expresan lo que el negocio requiere de la infraestructura externa para funcionar (ej. `PensionistaRepository`, `ConsultaReniecPort`, `NotificacionPort`).
- **Adaptadores (`infrastructure.in/out`):** Implementaciones técnicas concretas fuera del dominio (`@RestController`, `@Repository` de Spring Data JPA, `RestClient` HTTP) que consumen un puerto de entrada o implementan un puerto de salida.

---

## 3. Domain-Driven Design (DDD) en el Monolito Modular

En sistemas construidos bajo el **Monolito Modular (*Estadio 2*)**, los límites de cada módulo corresponden exactamente a un **Subdominio o Bounded Context de DDD**.

### 3.1 Mapa de Contextos (*Context Map*) y Relaciones Permitidas

El *Context Map* rige cómo interactúan dos módulos institucionales (*Bounded Contexts*) entre sí. En la ONP se regulan cuatro tipos de relaciones tácticas:

| Tipo de Relación en DDD | Dirección y Dinámica | Regla Táctica de Implementación en la ONP |
|---|---|---|
| **Cliente-Proveedor (*Customer-Supplier / Upstream-Downstream*)** | El proveedor (`Upstream` / ex: `onp-aportes`) expone contratos estables que el cliente (`Downstream` / ex: `onp-expedientes`) consume. | El módulo proveedor expone únicamente un paquete público `application.api` con DTOs de lectura/escritura (`records`). Prohibido que el consumidor acceda al paquete `domain` o `infrastructure` del proveedor. |
| **Capa Anticorrupción (*Anti-Corruption Layer — ACL*)** | El consumidor interpone un traductor para proteger su modelo interior ante el modelo externo de un tercero o un sistema legacy heredado. | **Mandatoria** siempre que un módulo nuevo consuma servicios heredados de JBoss o entidades externas (SUNAT, RENIEC, PIDE). Ver sección `§5.3`. |
| **Conformista (*Conformist*)** | El consumidor adopta sin cambios el modelo de datos entregado por el módulo proveedor. | **Permitido únicamente** para consumo de catálogos generales, tablas referenciales o maestros universales (ej. Ubigeo, Tipos de Documento, Tablas de Regímenes Básicos). |
| **Caminos Separados (*Separate Ways*)** | Dos contextos no tienen relación de negocio coherente ni necesitan compartir información de forma directa. | Prohibido acoplar en código Java (`pom.xml`) subdominios divergentes (ej. Mesa de Partes Virtual y Reservas Actuariales). Su eventual comunicación debe ser 100% asíncrona vía bus Apache Kafka (`ADR-012`). |

### 3.2 Building Blocks Tácticos del Dominio

Dentro de la capa de dominio puro (`domain/model/`) de un *Bounded Context*, se utilizan cuatro patrones de modelado:

```java
// 1. VALUE OBJECT (Objeto de Valor): Inmutable, sin identidad, comparado por sus atributos.
// Implementación mandatoria con Java Records.
public record MontoMonetario(BigDecimal valor, String moneda) {
    public MontoMonetario {
        Objects.requireNonNull(valor, "Valor monetario no puede ser nulo");
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto monetario no puede ser negativo");
        }
    }
    public MontoMonetario sumar(MontoMonetario otro) {
        if (!this.moneda.equals(otro.moneda)) throw new IllegalArgumentException("Monedas incompatibles");
        return new MontoMonetario(this.valor.add(otro.valor), this.moneda);
    }
}

// 2. ENTITY (Entidad de Dominio): Posee identidad única (ExpedienteId) que persiste en el tiempo.
// Contiene la lógica transaccional y mutaciones controladas. NO es una entidad JPA.
public class Expediente {
    private final ExpedienteId id;
    private EstadoExpediente estado;
    private final List<DocumentoAdjunto> documentos = new ArrayList<>();

    public Expediente(ExpedienteId id, EstadoExpediente estadoInicial) {
        this.id = id;
        this.estado = estadoInicial;
    }

    public void presentarParaRevision() {
        if (this.documentos.isEmpty()) {
            throw new ReglaNegocioVioladaException("No se puede presentar un expediente sin documentos");
        }
        this.estado = this.estado.toRevision();
    }
}

// 3. AGGREGATE ROOT (Raíz de Agregado): Entidad principal que encapsula un clúster de entidades y 
// Value Objects interconectados, garantizando la consistencia transaccional (ACID local) de todo el árbol.
// REGLA: Los repositorios de dominio (Port Out) SOLO cargan y guardan Aggregate Roots completos.
public class PensionistaAggregateRoot {
    private final PensionistaId id;
    private final DatosPersonales datosPersonales;
    private final List<Beneficiario> beneficiarios; // Solo modificable vía métodos del Aggregate Root
}

// 4. DOMAIN SERVICE (Servicio de Dominio): Lógica que involucra o coordina dos o más Aggregate Roots
// sin pertenecer exclusivamente a ninguno de ellos. (Ej: CalculadorActuarialDomainService).
```

### 3.3 Desacoplamiento Estricto: Entidad de Dominio Puro vs. Entidad JPA

Una confusión común en desarrolladores es intentar usar una clase con anotación `@Entity` de JPA como el `Aggregate Root` o la entidad pura del negocio. **Esta práctica está prohibida en Arquitectura Hexagonal y DDD en la ONP**.

| Dimensión | Entidad de Dominio Puro (`pe.gob.onp.*.domain.model`) | Entidad de Persistencia JPA (`pe.gob.onp.*.infrastructure.out.persistence.entity`) |
|---|---|---|
| **Propósito** | Encapsular las reglas previsionales, invariantes y transiciones de estado en memoria. | Representar fielmente la estructura tabular de las tablas en la base de datos Oracle (`TB_EXPEDIENTE`). |
| **Anotaciones** | **Ninguna.** Cero anotaciones `@Entity`, `@Table`, `@Column` o `@Getter/@Setter` de Lombok para romper encapsulamiento. | `@Entity`, `@Table(name="...")`, `@Id`, `@SequenceGenerator`, `@Version` y anotaciones JPA. |
| **Constructor** | Constructores de negocio que exigen datos válidos por construcción. | Constructor vacío (`protected` o `public`) exigido por la especificación JPA/Hibernate. |
| **Relaciones** | Relaciones conceptuales puras (ej. listas de Value Objects inmutables o IDs de otros agregados). | `@OneToMany`, `@ManyToOne`, `@JoinColumn`, `FetchType.LAZY`. |
| **Sincronización** | Traducida de ida y vuelta en la capa de infraestructura por un `Mapper` / `Adapter`. | Persistida físicamente en Oracle mediante `Spring Data JPA Repository`. |

### 3.4 Gobierno del Núcleo Compartido (*Shared Kernel — `onp-common-domain`*)

En el Monolito Modular, para no duplicar código transversal pero evitar introducir un monolito oculto dentro de una librería compartida, la OTI gobierna con celo militar el contenido del módulo Maven `onp-common-domain` (*Shared Kernel - PD09*).

| Elemento Técnico | Estado en el Shared Kernel (`onp-common-domain`) | Ejemplos Institucionales Autorizados / Prohibidos |
|---|---|---|
| **Value Objects Universales** | **✅ PERMITIDO** | Tipos primitivos transversales al Estado: `Dni`, `Ruc`, `MontoMonetario`, `PeriodoTributario`, `NumeroExpediente`. |
| **Jerarquía de Excepciones Raíz** | **✅ PERMITIDO** | Excepciones base de arquitectura: `OnpDomainException`, `RecursoNoEncontradoException`, `ReglaNegocioVioladaException`. |
| **Primitivas y Envolventes Core** | **✅ PERMITIDO** | Clases base de paginación (`PageResult<T>`), interfaces marcador (`DomainEvent`, `AggregateRoot`). |
| **Entidades JPA (`@Entity`)** | **❌ PROHIBIDO TERMINANTEMENTE** | Ninguna tabla de Oracle puede mapearse en la librería común. Cada módulo (`onp-expedientes`, `onp-aportes`) es dueño exclusivo de sus entidades JPA. |
| **Lógica / Servicios Previsionales** | **❌ PROHIBIDO TERMINANTEMENTE** | Clases como `CalculoPensionService` o `ValidadorDeudaService` deben pertenecer exclusivamente a sus *Bounded Contexts*. |
| **Puertos de Persistencia o JPA** | **❌ PROHIBIDO TERMINANTEMENTE** | Prohibido poner interfaces de repositorios o clientes HTTP en el *Shared Kernel*. |

---

## 4. Patrones Tácticos de Lógica de Negocio y Consulta

### 4.1 Comparativa de las 4 Estrategias de Lógica de Dominio

La OTI reconoce cuatro estrategias para organizar la lógica transaccional, ordenadas según la complejidad del requerimiento del submódulo:

| Estrategia de Lógica (`§6 LIN-ARQ-000`) | Definición Táctica y Estructura | Cuándo Aplicar en Módulos de la ONP |
|---|---|---|
| **1. Transaction Script** | Cada caso de uso es un método procedimental en un Servicio (`@Service @Transactional`) que invoca secuencialmente queries sobre tablas y realiza cálculos lineales directos. | Módulos CRUD de soporte administrativo, generación de registros simples o flujos de carga masiva sin reglas cruzadas de estado. |
| **2. Active Record** | La entidad que mapea la fila de la base de datos (`@Entity` JPA) contiene getters/setters y encapsula en sí misma pequeñas validaciones simples y operaciones locales de su propio registro. | Módulos intermedios de catálogos enriquecidos o tablas maestras del sistema donde no existen invariantes complejos que abarquen múltiples tablas. |
| **3. Table Module** | Un único objeto o clase de servicio organiza y calcula reglas masivas sobre **toda una colección o tabla de registros en memoria** al mismo tiempo, en lugar de instanciar miles de objetos individuales. | **Procesos por lotes (*Batch*) y Cálculos Actuariales Masivos.** Ej: Cálculo simultáneo de devengados para 50,000 pensionistas en una planilla mensual de pago sin desbordar el *Heap* de la JVM. |
| **4. Domain Model (DDD)** | Un modelo rico orientado a objetos con `Aggregate Roots`, `Entities` y `Value Objects` que protegen estrictamente sus invariantes transaccionales en memoria aislados de la infraestructura. | **Núcleo Previsional Core:** Expedientes, Liquidaciones, Trámites de Jubilación y Aportes, donde una transición inválida causa graves perjuicios económicos o legales al Estado. |

### 4.2 Patrón CQRS — Separación de Modelos de Escritura y Lectura (*PA07*)

El patrón **CQRS (*Command Query Responsibility Segregation*)** se adopta mandatoriamente cuando las operaciones que consultan un dato (*Queries*) y las que lo modifican (*Commands*) tienen necesidades de indexación, latencia y rendimiento tan opuestas que usar una sola tabla en Oracle degrada ambos extremos.

```
┌────────────────────────────────────────────────────────────────────────┐
│ LADO DE ESCRITURA (Command Side - ACID / Relacional)                   │
│                                                                        │
│ [Command: RegistrarAporte] ──► [Handler] ──► [Oracle DB: TB_APORTE]    │
│                                                     │                  │
└─────────────────────────────────────────────────────┼──────────────────┘
                                                      │ Sincronización Eventual
                                                      ▼
┌────────────────────────────────────────────────────────────────────────┐
│ LADO DE LECTURA (Query Side - Desnormalizado / NoSQL)                  │
│                                                                        │
│ [Query: BuscarExpediente360] ◄── [Read Repo] ◄── [MongoDB / Redis]     │
└────────────────────────────────────────────────────────────────────────┘
```

#### Variantes Aprobadas de Sincronización en CQRS
1. **Variante A — Transactional Outbox + Kafka (Estándar Primario):** Dentro de la misma transacción ACID de Oracle donde se modifica la tabla del negocio, se inserta un registro de evento en la tabla `TB_OUTBOX`. Un proceso asíncrono o *relay* publica el evento en Apache Kafka y un consumidor proyecta la vista en el motor de lectura NoSQL.
2. **Variante B — CDC (*Change Data Capture*) + Kafka (Agnóstico al Código):** Se utiliza una herramienta a nivel de motor de base de datos (**Debezium + Kafka Connect**) que captura los cambios en tiempo real leyendo directamente los logs binarios transaccionales (`LogMiner`/`XStream` en Oracle o `WAL` en PostgreSQL). **Requisito en ONP:** Toda implementación de Variante B sobre Oracle requiere validación previa con los Administradores de Base de Datos (DBA) para confirmar el licenciamiento de `LogMiner` y el impacto de *Supplemental Logging*.

#### Elección del Store de Lectura (`Read Model`)
| Patrón de Consulta Táctico en el Caso de Uso | Store de Lectura Asignado | Justificación Táctica |
|---|---|---|
| **Búsqueda puntual por clave o ID** (`DNI`, `NumeroExpediente`). | **Redis / Key-Value** | Latencia sub-milisegundo (< 2ms). Proyección pre-cacheada en memoria de los datos del ciudadano. |
| **Documento Agregado Complejo 360°** (Expediente completo con todos sus anexos, aportes y liquidaciones consolidados). | **MongoDB** | Estructura documental desnormalizada (`JSON / BSON`). Evita ejecutar *Queries* relacionales pesadas con 8+ *JOINs* sobre tablas históricas en Oracle durante el horario de atención ciudadana. |
| **Búsqueda por Texto Libre difuso, filtros combinados y facetas.** | **Elasticsearch** | Índices invertidos y *scoring* de relevancia no viables de forma óptima en motores relacionales o documentales clásicos. |

---

## 5. Patrones Tácticos de Interfaz, Agregación e Integración

### 5.1 Patrón BFF (*Backend for Frontend — PT09*)

Para evitar que una interfaz de usuario SPA (Angular) realice 10 peticiones REST secuenciales desde el navegador para armar una sola pantalla, o que reciba DTOs genéricos con 80 campos innecesarios, se prescribe el patrón **BFF (*Backend for Frontend*)**.

```
┌──────────────────────────────────────┐     ┌──────────────────────────────────────┐
│  SPA ANGULAR (Mesa de Partes Web)    │     │  APP MÓVIL (Mesa de Partes Móvil)    │
└──────────────────┬───────────────────┘     └──────────────────┬───────────────────┘
                   │ 1 Petición REST / DTO a Medida             │ 1 Petición REST / DTO Ligero
                   ▼                                            ▼
┌───────────────────────────────────────────────────────────────────────────────────┐
│ CAPA BFF (pe.gob.onp.bff.mesapartes.* - Spring Boot en Kubernetes)                │
│ • Agrega y consolida llamados hacia 3 servicios internos (Expedientes, Aportes)   │
│ • Limpia, formatea y reduce el DTO exclusivamente a lo que la UI requiere       │
└──────────────────────────────────────────┬────────────────────────────────────────┘
                                           │ Orquestación interna gRPC / REST
                                           ▼
┌───────────────────────────────────────────────────────────────────────────────────┐
│ MICROSERVICIOS Y MÓDULOS DEL CORE PREVISIONAL (APIs de Dominio)                   │
└───────────────────────────────────────────────────────────────────────────────────┘
```

- **Regla Táctica:** El BFF es una capa de **presentación y agregación pura**. Prohibido que un BFF contenga reglas de cálculo previsional, acceso directo a bases de datos Oracle, o transacciones ACID locales de negocio.

### 5.2 Patrón Gateway-Aggregation (*PT10*) y Facade Arquitectónico (*PT12*)

- **Gateway-Aggregation (`PT10`):** Cuando una operación interna requiere recopilar datos estáticos desde múltiples repositorios independientes para armar un payload consolidado, se encapsula la lógica de dispersión y agregación en una clase `GatewayAggregator` dentro de la capa `application/service/`, evitando exponer el *chatterness* hacia el controlador REST o al consumidor externo.
- **Facade Arquitectónico de Integración (`PT12`):** Ubicado estrictamente en la capa de infraestructura (`infrastructure/adapter/out/`), un Facade Arquitectónico encapsula la heterogeneidad, autenticación criptográfica (`WS-Security / mTLS`) y los detalles de protocolo de un ecosistema externo complejo, presentando un puerto limpio hacia la capa de aplicación del módulo.

### 5.3 Patrón Anti-Corruption Layer (*ACL — PT11*) en Integraciones

En toda integración con sistemas legados o externos, la Capa Anticorrupción (ACL) se implementa de forma obligatoria mediante tres componentes coordinados dentro de `infrastructure/adapter/out/`:

```java
// 1. EL DTO DE RESPUESTA DEL TERCERO (Estructura ajena, con nombres de campos y formatos propios)
public class ReniecPersonaResponseXml {
    public String numDni;
    public String primerNombre;
    public String apePaterno;
    public String apeMaterno;
    public String fecNac; // Ejemplo: formato "dd/MM/yyyy" ajeno al estándar ONP
}

// 2. EL MAPPER TRADUCTOR (La verdadera Anti-Corruption Layer)
@Component
public class ReniecAclMapper {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DatosPersona toDomain(ReniecPersonaResponseXml response) {
        if (response == null) throw new RecursoNoEncontradoException("RENIEC retornó vacío");
        LocalDate fechaNac = LocalDate.parse(response.fecNac, FMT);
        return new DatosPersona(
            new Dni(response.numDni),
            response.primerNombre,
            response.apePaterno + " " + response.apeMaterno,
            fechaNac
        );
    }
}

// 3. EL ADAPTER DE SALIDA QUE CONSUME EL PUERTO DE DOMINIO Y EJECUTA LA TRADUCCIÓN
@Repository
@RequiredArgsConstructor
public class ReniecHttpAdapter implements ConsultaPersonaExternalPort {
    private final RestClient reniecClient;
    private final ReniecAclMapper aclMapper;

    @Override
    public DatosPersona consultarPorDni(Dni dni) {
        ReniecPersonaResponseXml response = reniecClient.get()
            .uri("/persona/{dni}", dni.valor())
            .retrieve()
            .body(ReniecPersonaResponseXml.class);
        return aclMapper.toDomain(response); // Aislamiento garantizado: el dominio solo ve DatosPersona
    }
}
```

---

## 6. Resiliencia Táctica en Comunicaciones Externas (*Design for Failure*)

Todo llamado por red desde un adaptador de infraestructura hacia un servicio externo o base de datos es inherentemente falible. Para evitar efectos dominó y colapsos en cascada dentro del clúster de Kubernetes, es mandatoria la implementación de patrones de tolerancia a fallos mediante la librería oficial institucional: **Resilience4j**.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ ADAPTADOR DE SALIDA ONP (ReniecHttpAdapter / PagoExternalAdapter)           │
│                                                                             │
│  [Petición Saliente]                                                        │
│         │                                                                   │
│         ▼                                                                   │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │ BULKHEAD (Aislamiento de Hilos)                                       │  │
│  │ Limita a máx. 20 hilos concurrentes hacia este proveedor externo.     │  │
│  └──────────────────────────────────┬────────────────────────────────────┘  │
│                                     ▼                                       │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │ CIRCUIT BREAKER (Resilience4j)                                        │  │
│  │ Evalúa ventana de 100 peticiones. Si >50% fallan -> APERTURA CIRCUITO │  │
│  └──────────────────────────────────┬────────────────────────────────────┘  │
│                                     ▼                                       │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │ TIMEOUT ESTRICTO                                                      │  │
│  │ Connection Timeout: 2 segundos / Read Timeout: 3 segundos máx.        │  │
│  └──────────────────────────────────┬────────────────────────────────────┘  │
└─────────────────────────────────────┼───────────────────────────────────────┘
                                      ▼
                      [ RED EXTERIOR / SERVICIO RENIEC ]
```

### 6.1 Estrategia de Timeouts Finos

Queda **terminantemente prohibido** dejar clientes HTTP, JDBC o SOAP con *timeouts* por defecto (infinitos o > 30 segundos). Todo adaptador deberá configurar explícitamente:
- **Connection Timeout (Máx. 2 segundos):** Tiempo máximo para establecer el saludo inicial TCP/TLS con el servidor externo. Si no responde en 2s, el servidor externo está inalcanzable.
- **Read Timeout (Máx. 3 a 5 segundos según criticidad):** Tiempo máximo esperando el cuerpo de la respuesta una vez establecida la conexión.

### 6.2 Patrón Circuit Breaker (Cortacircuitos)

El *Circuit Breaker* debe proteger todo punto de salida hacia servicios de terceros (SUNAT, RENIEC, PIDE, pasarelas bancarias) con una máquina de estados obligatoria:

| Estado del Circuit Breaker | Comportamiento Táctico y Condición de Transición |
|---|---|
| **CERRADO (*Closed*)** | Funcionamiento normal. El tráfico fluye libremente. Si la tasa de fallos o lentitud (>3s) supera el **50% en una ventana móvil de 100 peticiones**, el cortacircuitos salta al estado *ABIERTO*. |
| **ABIERTO (*Open*)** | **Interrupción inmediata de llamadas por red.** Durante **30 segundos**, el adaptador rechaza las peticiones localmente y ejecuta la lógica de contingencia (*Fallback* — ej. retornar error amigable o dato en caché), protegiendo los hilos de la JVM de quedarse esperando. |
| **SEMI-ABIERTO (*Half-Open*)** | Al vencer los 30 segundos, el cortacircuitos permite el paso de **10 peticiones de prueba** hacia el servidor externo. Si al menos 8 tienen éxito, el circuito se restablece a *CERRADO*; si continúan fallando, regresa a *ABIERTO* por otros 30 segundos. |

### 6.3 Patrón Bulkhead (Compartimentación)

El patrón *Bulkhead* aisla recursos en compartimentos estancos para evitar que la lentitud o caída de un servicio secundario (ej. generador externo de reportes PDF) acapare todos los hilos Tomcat/container del microservicio principal, tumbando la atención de los trámites core.
- **Implementación Mandatoria:** Uso de `ThreadPoolBulkhead` o `SemaphoreBulkhead` de **Resilience4j** sobre adaptadores lentos o de terceros, asignando un pool máximo acotado (ejemplo: máximo 15 hilos y 10 en cola por cada proveedor externo). Si el pool se llena, las peticiones excedentes rebotan de inmediato con `BulkheadFullException` sin poner en riesgo la estabilidad del resto de endpoints del contenedor.
