# LIN-DEV-JAVA-001 — Estándar de Desarrollo Java ONP
## Oficina de Normalización Previsional — OTI
### Código: LIN-DEV-JAVA-001 | Versión 0.1.2 | Estado: Borrador | Marco rector: LIN-ARQ-000

---

## Control de versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 0.1.0 | 2026-05-22 | OTI | Versión inicial |
| 0.1.1 | 2026-05-28 | OTI | Alinea la configuración institucional a YAML, corrige el árbol de proyecto y adopta Checkstyle junto a PMD |
| 0.1.2 | 2026-07-06 | OTI | Cierre de brechas Nivel 3 (PR01-PR08, PD04-PD06, PA14): Inclusión de secciones 10.4, 11.5, 14.6, catálogo de plantillas Java y reconciliación total con LIN-ARQ-000 y LIN-BD-ORA-001 |

---

## Tabla de contenidos

- [sección 1 Alcance y vigencia](#1-alcance-y-vigencia)
  - [sección 1.3 Configuración inicial de un proyecto nuevo — orden recomendado](#13-configuración-inicial-de-un-proyecto-nuevo--orden-recomendado)
- [sección 2 Stack tecnológico mandatorio](#2-stack-tecnológico-mandatorio)
- [sección 3 Organización del código fuente](#3-organización-del-código-fuente)
- [sección 4 Convenciones de nomenclatura](#4-convenciones-de-nomenclatura)
- [sección 5 Estructura interna de clases](#5-estructura-interna-de-clases)
- [sección 6 Convenciones de codificación](#6-convenciones-de-codificación)
- [sección 7 Principios SOLID aplicados a clases y métodos Java 21](#7-principios-solid-aplicados-a-clases-y-métodos-java-21)
- [sección 8 Patrones de diseño de código (GoF) en Spring Boot 3](#8-patrones-de-diseño-de-código-gof-en-spring-boot-3)
- [sección 9 Documentación del código](#9-documentación-del-código)
- [sección 10 Logging estructurado](#10-logging-estructurado)
- [sección 11 Manejo de excepciones en la capa REST](#11-manejo-de-excepciones-en-la-capa-rest)
- [sección 12 Calidad de código](#12-calidad-de-código)
- [sección 13 Convenciones Spring Boot](#13-convenciones-spring-boot)
- [sección 14 Estructura de proyecto y gestión de dependencias Maven](#14-estructura-de-proyecto-y-gestión-de-dependencias-maven)
- [sección 15 Pruebas](#15-pruebas)
- [sección 16 Revisión de código](#16-revisión-de-código)
- [sección 17 Proceso de excepción a este estándar](#17-proceso-de-excepción-a-este-estándar)
- [Anexo A: Plantilla Javadoc estándar ONP](#anexo-a-plantilla-javadoc-estándar-onp)
- [Anexo B: Configuración Checkstyle recomendada](#anexo-b-configuración-checkstyle-recomendada)
- [Anexo C: Tabla completa de sufijos de clase](#anexo-c-tabla-completa-de-sufijos-de-clase)
- [Anexo D: Auditoría JPA — campos obligatorios LIN-BD-ORA-001](#anexo-d-auditoría-jpa--campos-obligatorios-lin-bd-ora-001)

---

## sección 1 Alcance y vigencia

### 1.1 Propósito

Este lineamiento establece las convenciones de codificación, nomenclatura, estructura y calidad que deben seguir todos los proyectos Java desarrollados o contratados por la Oficina de Tecnologías de la Información (OTI) de la ONP.

El objetivo no es restringir creatividad técnica sino garantizar que el código producido por distintos equipos —internos y contratistas— sea legible, mantenible y auditables con el mismo criterio. El 80% del costo del software está en su mantenimiento; estas convenciones protegen esa inversión.

### 1.2 Ámbito de aplicación

Este estándar aplica a:

- Todo proyecto Java 21 con Spring Boot 3.x iniciado desde cero en la ONP.
- Módulos nuevos añadidos a sistemas existentes cuando se migren a la arquitectura objetivo.
- Contratistas y proveedores que desarrollen software bajo contrato para la ONP.

**No aplica** a:

- Mantenimiento correctivo mínimo de sistemas legacy sin migración planificada.
- Scripts de automatización o código exploratorio/POC no destinado a producción.

### 1.3 Configuración inicial de un proyecto nuevo — orden recomendado

Al iniciar un proyecto Spring Boot en ONP, configurar los siguientes componentes en este orden. Cada paso referencia la sección del estándar donde está la implementación completa.

| Paso | Qué configurar | Sección | Descripción |
|---|---|---|---|
| 1 | Dependencias OTEL en `pom.xml` | LIN-OBS-001 sección 4 | Habilita trazas distribuidas, logs estructurados y métricas |
| 2 | `RequestIdFilter` `@Order(1)` | [sección 11.4.5](#1145-filtro-de-correlacion-requestidfilter) | Genera o propaga `X-Request-ID` y lo pone en el MDC para correlacionar todas las líneas de log de una petición |
| 3 | `SaaTokenValidationFilter` `@Order(2)` | LIN-SEC-APP-001 sección 8.3 | Valida el token SAA llamando al endpoint institucional y pone `user.id` en el MDC |
| 4 | `CanonicalRequestLogFilter` `@Order(3)` | LIN-OBS-001 sección 7 | Emite el log canónico al finalizar cada petición leyendo `user.id` del MDC |
| 5 | `ApiResponseWrapper` + `GlobalExceptionHandler` | [sección 11.4.4](#1144-estructura-de-respuesta-estandar-apiresponsewrapper), sección 9 | Contrato estándar de respuesta para todos los endpoints |
| 6 | `OpenApiConfig` + anotaciones Swagger | LIN-API-REST-001 sección 6, [sección 11.4.1](#1141-dependencia-maven)–11.4.3 | Contrato OpenAPI publicado desde el arranque del servicio |
| 7 | `AuditoriaBase` extendida en entidades JPA | Anexo D | Pobla automáticamente los 6 campos de auditoría obligatorios (LIN-BD-ORA-001 sección 5) |

> Los pasos 2, 3 y 4 forman la cadena de filtros obligatoria. El orden `@Order` es crítico: si se altera, `user.id` puede no estar disponible en el MDC cuando el log canónico lo necesita.

### 1.4 Relación con otros documentos

> **Importante:** **Supremacía Jerárquica del Marco Rector (LIN-ARQ-000):**  
> `LIN-ARQ-000` es el **documento rector de jerarquía superior (Nivel 2)** que rige de manera absoluta sobre todos los estándares y lineamientos técnicos específicos de **Nivel 3** (incluyendo el presente documento, `LIN-API-REST-001`, `LIN-BD-ORA-001`, `LIN-OBS-001`, etc.). Este estándar implementa de forma táctica y operativa en Java 21 / Spring Boot 3 los principios arquitectónicos (PR01–PR08), patrones de diseño (PD04–PD06) y lineamientos de contención de deuda técnica (PA14) definidos en `LIN-ARQ-000`. **Ante cualquier vacío, conflicto o presunta discrepancia de interpretación entre este documento y el marco rector, prevalecerán siempre y en todo momento los mandatos, patrones y directivas de LIN-ARQ-000.**

| Documento | Relación |
|-----------|----------|
| **LIN-ARQ-000 — Marco Rector de Diseño y Arquitectura de Software** | **Documento Rector (Nivel 2) de supremacía jerárquica.** Rige y fundamenta todos los mandatos arquitectónicos de este estándar. |
| LIN-API-REST-001 — Estándar de APIs REST | Complementa [sección 11.4](#114-api-rest-y-documentacion-openapi): convenciones REST detalladas |
| LIN-BD-ORA-001 — Estándar de Base de Datos Oracle | Complementa [sección 11.3](#113-transacciones): convenciones de persistencia |
| LIN-OBS-001 — Log, Trazabilidad y Observabilidad | Complementa sección 8: logging estructurado avanzado |

---

## sección 2 Stack tecnológico mandatorio

El stack siguiente es la base sobre la que aplica este estándar. Cualquier desviación requiere justificación en un ADR (Architecture Decision Record) y aprobación de la OTI.

| Componente | Versión | Rol |
|------------|---------|-----|
| Java | 21 LTS | Lenguaje de plataforma |
| Spring Boot | 3.x más reciente estable | Framework de aplicación |
| Maven | 3.9+ | Construcción y dependencias |
| Lombok | última estable | Reducción de boilerplate |
| MapStruct | última estable | Mapeo de objetos |
| SLF4J + Logback | Spring Boot default | Logging |
| JUnit 5 | Spring Boot default | Pruebas unitarias |
| Mockito | Spring Boot default | Mocking en pruebas |
| Testcontainers | última estable | Pruebas de integración con BD real |
| Checkstyle | última estable | Calidad de código — convenciones de forma adoptadas junto a PMD |
| JaCoCo | última estable | Cobertura de pruebas (CI) |

> **Java 21 LTS** es la versión de plataforma de la ONP. Aporta Virtual Threads (Project Loom), Records estables, Pattern Matching completo y mejoras en GC. No se aceptarán proyectos nuevos en versiones anteriores. Al utilizar hilos virtuales, evite el uso de bloques o métodos `synchronized` y llamadas de bloqueo nativas en código de alta concurrencia para prevenir el "Thread Pinning" (bloqueo del carrier thread del S.O.); use `java.util.concurrent.locks.ReentrantLock` en su lugar.
> **Lombok y MapStruct:** Al combinarlos, declare siempre Lombok antes de MapStruct en los procesadores de anotaciones del `maven-compiler-plugin` para asegurar la correcta generación de código de mapeo.

---

## sección 3 Organización del código fuente

### 3.1 Estructura de paquetes

Todos los proyectos ONP usan la raíz de paquete:

```
pe.gob.onp.<sistema>.<módulo>.<capa>
```

Donde:
- `<sistema>` — nombre corto del sistema (ej: `pensiones`, `tramites`, `recaudacion`)
- `<módulo>` — funcionalidad de negocio (ej: `aportaciones`, `expedientes`, `pagos`)
- `<capa>` — depende del estilo arquitectónico (ver tablas abajo)

La estructura de paquetes **no es libre**: se deriva directamente del estilo arquitectónico declarado en el ADR del proyecto. La descripción detallada de estructuras Maven se encuentra en **[sección 12.1](#121-estructura-de-proyecto-por-estilo-arquitectonico)**; aquí se muestra la vista de paquetes Java para cada estilo.

| Estilo | Cuándo aplica | Estructura de paquetes |
|---|---|---|
| **Monolito Simple** | Sistema sin candidatura a microservicio; lógica Transaction Script o Active Record | `controller / service / repository / entity` |
| **Monolito Modular** | Punto de llegada por defecto para todo sistema nuevo en ONP | Módulos Maven: `domain / application / infrastructure / api / boot` |
| **Hexagonal** | Módulo que cumple los seis criterios de microservicio — obligatorio antes de extraer | `domain / application / infrastructure` con ports/adapters |

---

#### Estilo 1 — Monolito Simple (arquitectura en capas)

Un único módulo Maven. Flujo de dependencias: `controller → service → repository → entity`.

| Paquete | Contenido | Anotación Spring |
|---|---|---|
| `controller` | Controladores REST | `@RestController` |
| `service` | Interfaces de servicio | — |
| `service.impl` | Implementaciones de servicio | `@Service` |
| `repository` | Repositorios Spring Data | `@Repository` |
| `entity` | Entidades JPA | `@Entity` |
| `dto` | Records/POJOs de transferencia (Request, Response) | — |
| `exception` | Jerarquía de excepciones y handler global | — |
| `config` | Configuración de Spring, OpenAPI | `@Configuration` |

```
pe.gob.onp.{sistema}.controller
pe.gob.onp.{sistema}.service
pe.gob.onp.{sistema}.service.impl
pe.gob.onp.{sistema}.repository
pe.gob.onp.{sistema}.entity
pe.gob.onp.{sistema}.dto
pe.gob.onp.{sistema}.exception
pe.gob.onp.{sistema}.config
```

**Restricción:** `controller` no accede a `repository` directamente. Si la lógica de un service supera ~300 líneas, es señal de que el sistema necesita Monolito Modular.

---

#### Estilo 2 — Monolito Modular (multi-módulo Maven)

Cinco módulos Maven con fronteras explícitas. Es el destino por defecto para todo sistema nuevo. Ver estructura Maven completa en **[sección 12.1](#121-estructura-de-proyecto-por-estilo-arquitectonico)**.

| Módulo Maven | Paquetes internos | Dependencias del módulo |
|---|---|---|
| `onp-{sistema}-domain` | `model/`, `port/in/`, `port/out/`, `exception/` | Ninguna (cero imports de framework) |
| `onp-{sistema}-application` | `service/`, `command/`, `dto/` | Solo `domain` |
| `onp-{sistema}-infrastructure` | `persistence/`, `client/`, `messaging/`, `mapper/` | `domain` + `application` |
| `onp-{sistema}-api` | `controller/`, `dto/`, `mapper/` | Solo `application` |
| `onp-{sistema}-boot` | `Application.java`, `resources/` | Todos (ensamblador) |

**Regla de dependencia entre módulos:**
```
domain  ←  application  ←  infrastructure
                ↑
               api
                ↑
               boot
```
`domain` no importa nada de `infrastructure`, `api` ni `boot`. `api` no accede a `infrastructure` directamente — siempre pasa por `application`.

---

#### Estilo 3 — Hexagonal / Ports & Adapters (pre-requisito para microservicio)

Obligatorio antes de extraer un módulo como microservicio. El dominio no tiene ningún import de Spring, JPA ni ningún framework. Las dependencias apuntan siempre hacia adentro: `infrastructure → application → domain`.

| Paquete | Contenido | Restricción |
|---|---|---|
| `domain.model` | Entidades de dominio, Value Objects, Agregados | Sin imports de framework |
| `domain.port.in` | Interfaces de casos de uso (puertos de entrada) | Sin imports de framework |
| `domain.port.out` | Interfaces de repositorio/cliente (puertos de salida) | Sin imports de framework |
| `domain.exception` | Excepciones del lenguaje del dominio | Sin imports de framework |
| `application.usecase` | Implementaciones de ports de entrada | Solo depende de `domain` |
| `infrastructure.web` | Adaptadores REST (`@RestController`) | Implementa `domain.port.in` |
| `infrastructure.persistence` | Adaptadores JPA (`@Repository`, `@Entity`) | Implementa `domain.port.out` |
| `infrastructure.config` | Configuración de Spring | — |

```
pe.gob.onp.{modulo}.domain.model
pe.gob.onp.{modulo}.domain.port.in
pe.gob.onp.{modulo}.domain.port.out
pe.gob.onp.{modulo}.domain.exception
pe.gob.onp.{modulo}.application.usecase
pe.gob.onp.{modulo}.infrastructure.web
pe.gob.onp.{modulo}.infrastructure.persistence
pe.gob.onp.{modulo}.infrastructure.config
```

> **Señal de violación:** si una clase en `domain` importa `jakarta.persistence`, `org.springframework` o cualquier librería de infraestructura, la frontera está rota y el módulo no es candidato a extracción.

---

> `config` y `util` suelen vivir a nivel de sistema (no de módulo) cuando son compartidos. En Hexagonal, `util` solo puede contener utilidades puras sin dependencia de Spring.

### 3.2 Reglas de archivo

| Regla | Detalle |
|-------|---------|
| Una unidad por archivo | Una clase, interfaz, enum o record por archivo `.java` |
| Tamaño máximo de clase | 500 líneas — señal de que la clase tiene demasiadas responsabilidades |
| Tamaño máximo de método | 30 líneas — señal de que el método hace demasiado |
| Imports sin wildcard | `import java.util.*` **PROHIBIDO**; un import por línea |
| Orden del archivo fuente | `package` → imports (agrupados) → declaración de clase |

**Agrupación de imports** (sin líneas en blanco entre grupos, sí entre grupos distintos):

```java
// 1. Java estándar
import java.util.List;
import java.util.Optional;

// 2. Jakarta / Spring
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

// 3. Librerías de terceros
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 4. Paquetes internos ONP
import pe.gob.onp.pensiones.expedientes.domain.Expediente;
```

---

## sección 4 Convenciones de nomenclatura

### 4.1 Reglas generales

| Caso | Aplica a | Ejemplo |
|------|----------|---------|
| `UpperCamelCase` | Clases, interfaces, enums, records, annotations | `ExpedienteController` |
| `lowerCamelCase` | Métodos, variables locales, parámetros, atributos de instancia | `calcularMontoPension()` |
| `UPPER_SNAKE_CASE` | Constantes (`public static final`) y valores de enum | `ESTADO_ACTIVO` |
| `kebab-case` | URLs de API, nombres de aplicación en configuración | `/expedientes-pension` |

**Idioma:** los nombres del dominio de negocio van en **español** (refleja el lenguaje del negocio previsional). Los términos técnicos de infraestructura van en **inglés** (alineado con las APIs de los frameworks).

```java
// Dominio en español
public class ExpedientePension { ... }
public Optional<ExpedientePension> obtenerPorDni(String dni) { ... }

// Infraestructura técnica en inglés
@Configuration
public class SecurityConfig { ... }
public class DatabaseHealthIndicator { ... }
```

### 4.2 Nomenclatura por tipo de clase

| Tipo | Sufijo | Ejemplo completo |
|------|--------|-----------------|
| Controlador REST | `Controller` | `ExpedienteController` |
| Interfaz de servicio (Application Service) | `Service` | `ExpedienteService`, `SolicitudPensionService` |
| Implementación de servicio (Application Service) | `ServiceImpl` | `ExpedienteServiceImpl`, `SolicitudPensionServiceImpl` |
| Servicio de dominio puro (POJO) | `DomainService` | `CalculoPensionVitaliciaDomainService` |
| Puerto de repositorio (Dominio) | `Repository` | `ExpedienteRepository`, `AportanteRepository` |
| Adaptador de repositorio JPA (Infra) | `JpaRepository` | `ExpedienteJpaRepository` |
| Adaptador de repositorio JDBC/Oracle (Infra) | `JdbcRepository` / `OracleRepository` | `AportanteJdbcRepository` |
| Entidad de dominio | *(sin sufijo)* | `Expediente`, `Pensionista` |
| DTO de entrada (request) | `Request` | `CrearExpedienteRequest` |
| DTO de salida (response) | `Response` | `ExpedienteResponse` |
| DTO genérico / interno | `Dto` | `ExpedienteDto` |
| Mapeador | `Mapper` | `ExpedienteMapper` |
| Excepción de dominio | `Exception` | `ExpedienteNoEncontradoException` |
| Clase de configuración | `Config` | `JpaConfig`, `SecurityConfig` |
| Clase de constantes | `Constants` | `EstadoExpedienteConstants` |
| Enum de dominio | *(sin sufijo)* | `EstadoExpediente`, `TipoDocumento` |
| Clase de utilidades | `Util` | `FechaUtil`, `FormatoUtil` |
| Aspecto (AOP) | `Aspect` | `AuditoriaAspect` |
| Filtro HTTP | `Filter` | `AutenticacionFilter` |
| Handler de excepciones | `Handler` | `GlobalExceptionHandler` |

### 4.3 Nomenclatura de paquetes

- Todo en minúsculas, sin guiones, sin underscores, sin números
- Máximo 4 niveles de profundidad después de `pe.gob.onp`
- Nombres cortos y descriptivos del dominio

```java
// BIEN
pe.gob.onp.pensiones.expedientes.service

// MAL — mayúsculas
pe.gob.onp.Pensiones.Expedientes.Service

// MAL — guiones o underscores
pe.gob.onp.pensiones.control_deuda.service
```

### 4.4 Nomenclatura de métodos

Los métodos deben comenzar con un **verbo en infinitivo** que represente la acción. El verbo es la primera palabra, en lowerCamelCase.

**Verbos de referencia por categoría:**

| Categoría | Verbos típicos |
|-----------|---------------|
| Consulta | `obtener`, `buscar`, `listar`, `encontrar`, `verificar`, `existir` |
| Modificación | `crear`, `actualizar`, `eliminar`, `guardar`, `registrar` |
| Cálculo | `calcular`, `determinar`, `estimar`, `convertir` |
| Validación | `validar`, `verificar` |
| Procesamiento | `procesar`, `ejecutar`, `enviar`, `notificar` |

```java
// BIEN
Optional<Expediente> obtenerPorDni(String dni);
List<Expediente> listarPorEstado(EstadoExpediente estado);
BigDecimal calcularMontoPension(Long expedienteId);
void validarFechaNacimiento(LocalDate fechaNacimiento);

// MAL — no comienza con verbo
Expediente expedientePorDni(String dni);        // confunde con propiedad
BigDecimal montoPension(Long expedienteId);     // parece getter
```

### 4.5 Nomenclatura de variables y atributos

- **lowerCamelCase**, descriptivos, en español para dominio
- Mínimo 3 caracteres, excepto índices de bucle (`i`, `j`, `k`) y variables de stream (`e`, `p`)
- **Sin prefijos húngaros**: `strNombre`, `intCantidad`, `bActivo` → PROHIBIDO
- Variables de tipo `Optional`: prefijo `optional` o nombre que indique opcionalidad

```java
// BIEN
private String nombresCompletos;
private BigDecimal montoMensual;
private LocalDate fechaInicio;
Optional<Expediente> optionalExpediente = repository.findById(id);

// MAL
private String strNombres;          // prefijo húngaro
private BigDecimal m;               // demasiado corto, sin semántica
private LocalDate fecha1;           // numeración sin semántica
```

### 4.6 Constantes y enumeraciones

Las constantes (`public static final`) van en `UPPER_SNAKE_CASE`. Se agrupan en clases o enums dedicados; **nunca** en interfaces.

```java
// BIEN — enum de dominio
public enum EstadoExpediente {
    ACTIVO, SUSPENDIDO, CANCELADO, EN_REVISION
}

// BIEN — clase de constantes para valores sin comportamiento
public final class CodigoErrorConstants {
    private CodigoErrorConstants() {}
    public static final String EXPEDIENTE_NO_ENCONTRADO = "EXP-001";
    public static final String PENSIONISTA_INACTIVO      = "PEN-002";
}

// MAL — constantes en interfaz
public interface Constantes {
    String ESTADO_ACTIVO = "ACTIVO";  // Las interfaces no son para constantes
}

// MAL — literal en lógica de negocio
if (expediente.getEstado().equals("ACTIVO")) { ... }  // PROHIBIDO

// BIEN
if (expediente.getEstado() == EstadoExpediente.ACTIVO) { ... }
```

---

## sección 5 Estructura interna de clases

### 5.1 Orden de declaración

Todo archivo de clase debe seguir este orden:

1. Constantes estáticas (`public static final` primero, luego `private static final`)
2. Logger (si no usa Lombok `@Slf4j`)
3. Atributos de instancia (públicos primero, luego privados)
4. Bloque estático inicializador (si existe, usar con moderación)
5. Constructores (del más parámetros al menos)
6. Métodos públicos de negocio (agrupados por funcionalidad, no por visibilidad)
7. Métodos privados de soporte
8. `equals`, `hashCode`, `toString` (si no usa Lombok o Records)

> **Agrupación por funcionalidad, no por visibilidad.** Un método privado que solo usa `crearExpediente()` debe estar contiguo a él, aunque haya otros métodos públicos después. El objetivo es que el lector pueda entender un flujo completo sin saltar el archivo.

### 5.2 Inyección de dependencias

**Siempre por constructor.** Nunca mediante `@Autowired` en campo.

**Por qué:** la inyección por constructor hace explícitas las dependencias, permite instanciar la clase en pruebas unitarias sin contenedor Spring, y evita que el objeto exista en estado inconsistente.

```java
// MAL — inyección por campo
@Service
public class ExpedienteServiceImpl implements ExpedienteService {

    @Autowired
    private ExpedienteRepository expedienteRepository;

    @Autowired
    private PensionistaRepository pensionistaRepository;
}

// BIEN — inyección por constructor con Lombok
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpedienteServiceImpl implements ExpedienteService {

    private final ExpedienteRepository expedienteRepository;
    private final PensionistaRepository pensionistaRepository;
}
```

### 5.3 Records para DTOs

Usar `record` (Java 16+) para objetos de transferencia de datos: son inmutables por diseño, generan automáticamente constructor, getters, `equals`, `hashCode` y `toString`.

```java
// Request de entrada — inmutable y validado
public record CrearExpedienteRequest(
    @NotNull(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos")
    String dni,

    @NotBlank(message = "El nombre es obligatorio")
    String nombresCompletos,

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    LocalDate fechaNacimiento
) {}

// Response de salida — inmutable
public record ExpedienteResponse(
    Long id,
    String dni,
    String nombresCompletos,
    EstadoExpediente estado,
    LocalDate fechaCreacion
) {}
```

**No usar Records para:**
- Entidades JPA (JPA requiere clase mutable con constructor sin argumentos)
- Clases con lógica de negocio compleja
- Clases que necesiten herencia

### 5.4 Uso controlado de Lombok

Lombok reduce el boilerplate, pero debe usarse con criterio.

| Anotación | Uso | Observación |
|-----------|-----|-------------|
| `@Slf4j` | Sí, en toda clase que necesite log | Genera `private static final Logger log` |
| `@RequiredArgsConstructor` | Sí, para inyección por constructor | Solo en campos `final` |
| `@Builder` | Sí, para clases con muchos parámetros | Facilita construcción legible |
| `@Value` | Sí, para clases inmutables simples | Genera clase final con todos los campos `final` |
| `@Data` | Solo en DTOs simples | No usar en entidades JPA |
| `@Getter` / `@Setter` | En entidades JPA en vez de `@Data` | Más control sobre qué exponer |
| `@SneakyThrows` | **PROHIBIDO** | Oculta checked exceptions, rompe el contrato del método |
| `@EqualsAndHashCode` en `@Entity` | **PROHIBIDO** | Lombok genera equals basado en todos los campos; en JPA causa problemas con proxies Hibernate |

```java
// Uso correcto de Lombok en entidad JPA
@Getter
@Setter
@Entity
@Table(name = "expediente")
@NoArgsConstructor
public class Expediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dni", nullable = false, length = 8)
    private String dni;

    // equals y hashCode manuales basados solo en el id de negocio
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Expediente other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
```

---

## sección 6 Convenciones de codificación

### 6.1 Formato

| Regla | Valor |
|-------|-------|
| Indentación | 4 espacios (no tabs) |
| Longitud máxima de línea | 120 caracteres |
| Estilo de llaves | K&R: llave de apertura en la misma línea |
| Llaves obligatorias | Siempre, incluso en bloques de una línea |
| Líneas en blanco | Una entre métodos; ninguna al inicio/fin de un bloque |

```java
// BIEN — K&R, llaves siempre presentes
if (expediente.isActivo()) {
    procesar(expediente);
} else {
    rechazar(expediente);
}

// MAL — sin llaves
if (expediente.isActivo())
    procesar(expediente);

// MAL — llave en línea nueva (Allman style)
if (expediente.isActivo())
{
    procesar(expediente);
}
```

### 6.2 Declaración de variables

- **Declarar en el scope más estrecho posible**, lo más cerca de su uso
- Una declaración por línea; no mezclar tipos en una misma línea
- Usar `var` cuando el tipo es evidente por la expresión del lado derecho (Java 10+)
- No declarar variables que no se van a usar

```java
// BIEN — declaración junto al uso, var donde el tipo es obvio
var expedientes = expedienteRepository.findAll();
var pensionista = obtenerPensionista(dni);

// BIEN — tipo explícito cuando var no aporta claridad
Map<String, List<Expediente>> expedientesPorEstado = agruparPorEstado(expedientes);

// MAL — var donde el tipo no es obvio
var resultado = servicio.procesar(datos);  // ¿qué tipo es resultado?

// MAL — múltiples variables en una línea
int total, procesados, rechazados;

// MAL — variable declarada lejos de su uso
String mensaje;
// ... 20 líneas de código ...
mensaje = "Expediente procesado";
```

### 6.3 Sentencias de control

**Regla general:** reducir el anidamiento usando *early return*. El código feliz (sin errores) debe quedar al nivel más externo.

```java
// MAL — anidamiento profundo
public BigDecimal calcularPension(Long expedienteId) {
    var expediente = repository.findById(expedienteId);
    if (expediente.isPresent()) {
        if (expediente.get().isActivo()) {
            if (expediente.get().tienePeriodosAportados()) {
                return calcular(expediente.get());
            }
        }
    }
    return BigDecimal.ZERO;
}

// BIEN — early return
public BigDecimal calcularPension(Long expedienteId) {
    var expediente = repository.findById(expedienteId)
        .orElseThrow(() -> new ExpedienteNoEncontradoException(expedienteId));

    if (!expediente.isActivo()) return BigDecimal.ZERO;
    if (!expediente.tienePeriodosAportados()) return BigDecimal.ZERO;

    return calcular(expediente);
}
```

**Máximo 3 niveles de anidamiento.** Si se supera, extraer a métodos privados.

**Switch expressions** (Java 14+) preferidos sobre switch statements para asignaciones:

```java
// MAL — switch statement para asignación
String descripcion;
switch (estado) {
    case ACTIVO:
        descripcion = "Activo";
        break;
    case SUSPENDIDO:
        descripcion = "Suspendido";
        break;
    default:
        descripcion = "Desconocido";
}

// BIEN — switch expression
String descripcion = switch (estado) {
    case ACTIVO     -> "Activo";
    case SUSPENDIDO -> "Suspendido";
    default         -> "Desconocido";
};
```

**Reglas adicionales:**
- `if`/`else` siempre con llaves `{}`
- `switch` siempre con `default`
- **PROHIBIDO:** asignaciones dentro de condiciones: `if ((x = getValue()) > 0)`
- **PROHIBIDO:** condiciones con efectos secundarios: `if (lista.remove(elemento))`

### 6.4 Manejo de colecciones

Declarar siempre con la **interfaz**, no con la implementación concreta:

```java
// BIEN — interfaz en declaración
List<Expediente> expedientes = new ArrayList<>();
Map<String, Pensionista> pensionistasPorDni = new HashMap<>();

// MAL — implementación concreta en declaración
ArrayList<Expediente> expedientes = new ArrayList<>();
```

**Colecciones prohibidas (obsoletas desde Java 2):**

| PROHIBIDO | Usar en su lugar |
|-----------|-----------------|
| `Vector` | `ArrayList` |
| `Hashtable` | `HashMap` |
| `Enumeration` | `Iterator` o enhanced for |
| `Stack` | `Deque` / `ArrayDeque` |

**Colecciones vacías:** retornar siempre colección vacía, nunca `null`.

```java
// MAL
public List<Expediente> listarPorEstado(EstadoExpediente estado) {
    if (estado == null) return null;  // NUNCA null
    ...
}

// BIEN
public List<Expediente> listarPorEstado(EstadoExpediente estado) {
    if (estado == null) return Collections.emptyList();
    ...
}
```

**Stream API** para transformaciones sobre colecciones (evita bucles manuales repetitivos):

```java
// MAL — bucle manual para filtrar y transformar
List<ExpedienteResponse> activos = new ArrayList<>();
for (Expediente exp : expedientes) {
    if (exp.getEstado() == EstadoExpediente.ACTIVO) {
        activos.add(mapper.toResponse(exp));
    }
}

// BIEN — Stream API
List<ExpedienteResponse> activos = expedientes.stream()
    .filter(exp -> exp.getEstado() == EstadoExpediente.ACTIVO)
    .map(mapper::toResponse)
    .toList();
```

### 6.5 Nulos y Optional

**Los métodos públicos nunca retornan null** para indicar ausencia. Usar `Optional<T>` para búsquedas que pueden no encontrar resultado.

```java
// MAL — null como señal de ausencia
public Expediente buscarPorDni(String dni) {
    return repository.findByDni(dni);  // puede retornar null
}

// BIEN — Optional comunica la posibilidad de ausencia
public Optional<Expediente> buscarPorDni(String dni) {
    return repository.findByDni(dni);
}
```

**Reglas de uso de Optional:**
- No usar `Optional` como parámetro de método
- No usar `Optional` como campo de clase o entidad JPA
- No usar `Optional.get()` sin verificar presencia; preferir `orElseThrow`, `orElse`, `orElseGet`, `ifPresent`

```java
// MAL — Optional.get() sin verificación
var expediente = repository.findById(id).get();  // NoSuchElementException si no existe

// BIEN — manejo explícito de la ausencia
var expediente = repository.findById(id)
    .orElseThrow(() -> new ExpedienteNoEncontradoException(id));
```

### 6.6 Cadenas (String)

**Concatenación en bucles:** usar `StringBuilder`, no el operador `+`. En cada iteración con `+` se crea un nuevo objeto `String` en memoria.

```java
// MAL — concatenación con + en bucle
String resultado = "";
for (String linea : lineas) {
    resultado = resultado + linea + "\n";  // objeto nuevo en cada iteración
}

// BIEN — StringBuilder
var sb = new StringBuilder();
for (String linea : lineas) {
    sb.append(linea).append('\n');
}
String resultado = sb.toString();
```

**Comparación:** nunca con `==` (compara referencias, no contenido).

```java
// MAL
if (estado == "ACTIVO") { ... }

// BIEN — colocar el literal primero para evitar NullPointerException
if ("ACTIVO".equals(estado)) { ... }

// MEJOR — usar enum, eliminar la cadena literal
if (estadoExpediente == EstadoExpediente.ACTIVO) { ... }
```

**Text blocks** (Java 15+) para literales multilínea (JSON, SQL, plantillas):

```java
// BIEN — text block para SQL o JSON legible
String sql = """
    SELECT e.id, e.dni, e.estado
    FROM expediente e
    WHERE e.estado = :estado
    ORDER BY e.fecha_creacion DESC
    """;
```

### 6.7 Tipos numéricos y monetarios

Los valores monetarios, porcentajes y cualquier valor de alta precisión decimal **deben** representarse con `BigDecimal`. El uso de `float` o `double` para dinero es un error de diseño: ambos tipos usan representación binaria de punto flotante que introduce errores de redondeo.

```java
// MAL — float/double para montos
double montoPension = 850.50;
float tasaInteres = 0.035f;

// BIEN — BigDecimal
BigDecimal montoPension = new BigDecimal("850.50");
BigDecimal tasaInteres  = new BigDecimal("0.035");

// MAL — BigDecimal desde double (hereda los errores de punto flotante)
BigDecimal monto = new BigDecimal(850.50);  // resultado: 850.4999999999...

// BIEN — BigDecimal desde String
BigDecimal monto = new BigDecimal("850.50");

// Constantes útiles
BigDecimal.ZERO   // 0
BigDecimal.ONE    // 1
BigDecimal.TEN    // 10
```

**Operaciones aritméticas con BigDecimal:**

```java
BigDecimal resultado = monto
    .multiply(tasa)
    .setScale(2, RoundingMode.HALF_UP);  // siempre especificar escala y modo de redondeo
```

### 6.8 Excepciones

**Jerarquía de excepciones de dominio:** cada sistema debe definir una excepción base que extienda `RuntimeException`, y subtipos para cada error de negocio.

```java
// Excepción base del dominio
public class ExpedienteException extends RuntimeException {
    public ExpedienteException(String mensaje) {
        super(mensaje);
    }
    public ExpedienteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

// Subtipos específicos
public class ExpedienteNoEncontradoException extends ExpedienteException {
    public ExpedienteNoEncontradoException(Long id) {
        super("No existe expediente con id: " + id);
    }
}

public class ExpedienteEstadoInvalidoException extends ExpedienteException {
    public ExpedienteEstadoInvalidoException(EstadoExpediente estado) {
        super("Operación no permitida para expediente en estado: " + estado);
    }
}
```

**Reglas:**

```java
// PROHIBIDO — catch vacío
try {
    procesarExpediente(id);
} catch (Exception e) {
    // silencio — PROHIBIDO
}

// PROHIBIDO — capturar Exception genérica sin relanzar
try {
    procesarExpediente(id);
} catch (Exception e) {
    log.error("Error");  // sin stack trace, sin relanzar — PROHIBIDO
}

// BIEN — registrar con stack trace y relanzar o envolver
try {
    integrarConSistemaExterno(datos);
} catch (IOException e) {
    log.error("Error al integrar con sistema externo para expediente {}", id, e);
    throw new IntegracionExternaException("Fallo al integrar expediente " + id, e);
}
```

**No usar excepciones para flujo de control:**

```java
// MAL — excepción como control de flujo
try {
    int indice = Integer.parseInt(valor);
    return lista.get(indice);
} catch (NumberFormatException e) {
    return valorPorDefecto;  // la excepción es el "else" — PROHIBIDO
}

// BIEN
if (!valor.matches("\\d+")) return valorPorDefecto;
return lista.get(Integer.parseInt(valor));
```

### 6.9 Antipatrones de rendimiento

```java
// MAL — instanciación innecesaria de String
String texto = new String("Hola");  // el literal ya es un String — siempre incorrecto

// BIEN
String texto = "Hola";

// MAL — objetos innecesarios en bucle
for (Expediente exp : expedientes) {
    BigDecimal factor = new BigDecimal("1.03");  // nuevo objeto en cada iteración
    exp.setMonto(exp.getMonto().multiply(factor));
}

// BIEN — reutilizar el objeto inmutable
final BigDecimal FACTOR_REAJUSTE = new BigDecimal("1.03");
for (Expediente exp : expedientes) {
    exp.setMonto(exp.getMonto().multiply(FACTOR_REAJUSTE));
}

// MAL — creación de objetos en bucle con concatenación
List<String> resumen = new ArrayList<>();
for (Expediente exp : expedientes) {
    resumen.add("Expediente: " + exp.getId() + " - " + exp.getDni());  // StringBuilder implícito nuevo cada vez
}

// BIEN — String.format o texto formateado
List<String> resumen = expedientes.stream()
    .map(exp -> "Expediente: %d - %s".formatted(exp.getId(), exp.getDni()))
    .toList();
```

---

## sección 7 Principios SOLID aplicados a clases y métodos Java 21

Los principios **SOLID** orientan el diseño granular de clases, interfaces y métodos para evitar la degradación estructural (*Big Ball of Mud*). Su cumplimiento en Java 21 y Spring Boot 3 es obligatorio para toda fábrica de software o equipo interno.

### 7.1 Single Responsibility Principle (SRP) en Servicios Spring

Cada clase, servicio o componente Spring (`@Service`, `@Component`, `@Repository`) debe tener una **única razón para cambiar**. Un servicio transaccional no debe mezclar lógica previsional con envío de correos SMTP, formateo de PDFs o invocaciones directas a sistemas REST de terceros.

```java
// MAL — El servicio asume responsabilidades divergentes que evolucionan a ritmos distintos
@Service
public class ExpedienteMonoliticoService {
    public void aprobar(Expediente exp) { /* lógica de aprobación previsional */ }
    public void enviarCorreoSmtp(Expediente exp) { /* conexión e integracion con servidor de correo */ }
    public byte[] generarResolucionPdf(Expediente exp) { /* renderizado y maquetación PDF con iText */ }
}

// BIEN — Segregación de responsabilidades mediante puertos de salida y servicios especializados
@Service
@RequiredArgsConstructor
public class AprobarExpedienteService {
    private final ExpedienteRepository repository;
    private final NotificacionPort notificacionPort;
    private final GeneradorResolucionPort generadorPort;

    @Transactional
    public Resolucion aprobar(Long expedienteId) {
        Expediente exp = repository.obtenerPorId(expedienteId);
        exp.aprobar();
        repository.guardar(exp);
        notificacionPort.notificarAprobacion(exp);
        return generadorPort.generarResolucion(exp);
    }
}
```

### 7.2 Open/Closed Principle (OCP) mediante Polimorfismo e Interfaces

El código debe estar **abierto a la extensión y cerrado a la modificación**. Cuando la normativa de la ONP introduzca una variación en el cálculo o una nueva regla (ej. nuevo régimen previsional o nueva tabla de beneficio), queda **terminantemente prohibido** encadenar condicionales (`if/else` o `switch`) gigantes dentro de la misma clase. Se debe implementar el polimorfismo mediante interfaces y la inyección en listas de Spring.

```java
// Puerto o Estrategia de cálculo previsional
public interface CalculadorRentaStrategy {
    boolean soporta(TipoRegimen regimen);
    BigDecimal calcular(Pensionista pensionista, HistorialAportes historial);
}

@Service
@RequiredArgsConstructor
public class CalculadoraPensionService {
    // Spring inyecta automáticamente TODOS los beans que implementen la interfaz
    private final List<CalculadorRentaStrategy> estrategias;

    public BigDecimal calcularPension(Pensionista p, HistorialAportes h) {
        return estrategias.stream()
            .filter(e -> e.soporta(p.getRegimen()))
            .findFirst()
            .orElseThrow(() -> new ReglaPrevisionalException("Régimen no soportado: " + p.getRegimen()))
            .calcular(p, h);
    }
}
```

### 7.3 Liskov Substitution Principle (LSP) en Jerarquías de Dominio

Las clases o implementaciones derivadas deben poder sustituir a su abstracción base sin romper las garantías transaccionales ni los contratos precondición/postcondición.
- Prohibido lanzar `UnsupportedOperationException` en métodos heredados de una interfaz o clase base.
- Prohibido retornar `null` cuando la interfaz base promete un `Optional<T>` o un objeto válido.

### 7.4 Interface Segregation Principle (ISP) en Puertos y Contratos

Es preferible contar con múltiples interfaces específicas orientadas al cliente o caso de uso en lugar de una interfaz monolítica.

```java
// MAL — Interfaz gigante que obliga al consumidor a depender de métodos que jamás invoca
public interface ExpedienteRepository {
    void guardar(Expediente exp);
    void eliminar(ExpedienteId id);
    List<ExpedienteReporte> generarReporteAnualActuarial();
    void purgarHistorialAntiguo();
}

// BIEN — Segregación según responsabilidad operacional y analítica
public interface ExpedienteCommandRepository {
    void guardar(Expediente exp);
}

public interface ExpedienteQueryRepository {
    Optional<Expediente> obtenerPorId(ExpedienteId id);
}

public interface ExpedienteReporteRepository {
    List<ExpedienteReporte> generarReporteAnualActuarial();
}
```

### 7.5 Dependency Inversion Principle (DIP) e Inyección por Constructor

Los módulos de alto nivel (`domain/`) no deben depender de módulos de bajo nivel (`infrastructure/in/out`, `jpa`, `rest`); ambos deben depender de abstracciones (interfaces o puertos).
- **Mandato en Spring Boot 3:** La inyección de dependencias debe realizarse **exclusivamente por constructor** (`@RequiredArgsConstructor` de Lombok con atributos `private final` o constructor explícito).
- Queda **prohibido** el uso de `@Autowired` sobre campos (`field injection`) o métodos setter.

---

## sección 8 Patrones de diseño de código (GoF) en Spring Boot 3

La implementación de patrones *Gang of Four* (GoF) en la ONP aprovecha el contenedor de inversión de control (IoC) de Spring Boot 3 para mantener un código limpio, extensible y testeable.

### 8.1 Patrones Estructurales en Spring

#### 8.1.1 Decorator (`@Primary` para Funcionalidades Transversales)
Permite añadir comportamientos ortogonales (auditoría en memoria, caché multinivel, métricas personalizadas) sobre un adaptador o puerto sin tocar la lógica de negocio base ni el consumidor.

```java
@Repository
@RequiredArgsConstructor
public class PensionistaJpaAdapter implements PensionistaRepository {
    private final SpringDataPensionistaRepository jpaRepo;
    @Override
    public Optional<Pensionista> obtenerPorDni(String dni) { return jpaRepo.findByDni(dni); }
}

@Component
@Primary
@RequiredArgsConstructor
public class CachedPensionistaRepositoryDecorator implements PensionistaRepository {
    private final PensionistaJpaAdapter delegado;
    private final Cache<String, Pensionista> cacheMemoria;

    @Override
    public Optional<Pensionista> obtenerPorDni(String dni) {
        Pensionista cached = cacheMemoria.getIfPresent(dni);
        if (cached != null) return Optional.of(cached);
        Optional<Pensionista> resultado = delegado.obtenerPorDni(dni);
        resultado.ifPresent(p -> cacheMemoria.put(dni, p));
        return resultado;
    }
}
```

#### 8.1.2 Facade de Subsistema Interno (`@Component`)
Provee una interfaz unificada y simple hacia un conjunto de servicios de aplicación o puertos dentro de un mismo módulo complejo, reduciendo el acoplamiento de los controladores REST.

```java
@Component
@RequiredArgsConstructor
public class TramiteJubilacionFacade {
    private final ExpedienteService expedienteService;
    private final CalculoPensionService calculoService;
    private final NotificacionPort notificacionPort;

    @Transactional
    public TramiteCompletadoResponse iniciarTramiteJubilacion(SolicitudJubilacionCommand command) {
        Expediente exp = expedienteService.crearExpediente(command);
        BigDecimal monto = calculoService.calcularRentaVitalicia(exp.getId());
        exp.asignarMontoCalculado(monto);
        notificacionPort.notificarInicioTramite(exp);
        return new TramiteCompletadoResponse(exp.getId(), monto, exp.getEstado());
    }
}
```

### 8.2 Patrones Creacionales en Java 21 y Spring

#### 8.2.1 Factory Method con Pattern Matching de Java 21
Centraliza la instanciación de agregados o entidades pre-configuradas según el tipo de régimen o solicitud previsional.

```java
@Component
public class LiquidacionFactory {
    public Liquidacion crearLiquidacionInicial(Expediente expediente) {
        return switch (expediente.getRegimen()) {
            case REGIMEN_19990 -> new LiquidacionRegimen19990(LiquidacionId.nuevo(), expediente.getId());
            case REGIMEN_20530 -> new LiquidacionRegimen20530(LiquidacionId.nuevo(), expediente.getId());
        };
    }
}
```

#### 8.2.2 Builder (Lombok `@Builder`)
Obligatorio para la construcción de DTOs, comandos o registros inmutables que poseen más de 4 atributos o campos de configuración opcionales.

### 8.3 Patrones de Comportamiento para Reemplazo de Bifurcaciones

#### 8.3.1 Observer mediante Eventos de Spring (`ApplicationEventPublisher` + `@EventListener`)
Desacopla totalmente la ejecución transaccional principal de procesos reactivos secundarios (auditoría asíncrona, envío de notificaciones al ciudadano o actualización de reportes).

```java
// 1. Evento inmutable (Record Java 21)
public record ResolucionEmitidaEvent(String numeroResolucion, String dniPensionista, Instant fechaEmision) {}

// 2. Emisor en el Servicio de Aplicación
@Service
@RequiredArgsConstructor
public class EmisionResolucionService {
    private final ApplicationEventPublisher eventPublisher;
    @Transactional
    public void emitir(Long resolucionId) {
        // ... persistir resolucion en Oracle ...
        eventPublisher.publishEvent(new ResolucionEmitidaEvent("RES-2026-9918", "08241578", Instant.now()));
    }
}

// 3. Receptores (Listeners independientes)
@Component
public class NotificacionResolucionListener {
    @EventListener
    @Async
    public void enviarSmsAlCiudadano(ResolucionEmitidaEvent event) { /* envío SMS / Correo */ }
}
```

#### 8.3.2 State mediante Enums con Comportamiento
Gestiona el ciclo de vida transaccional de entidades con estados finitos (`BORRADOR`, `EN_REVISION`, `OBSERVADO`, `APROBADO`) delegando en el propio estado las transiciones permitidas.

---

## sección 9 Documentación del código

### 9.1 Cuándo escribir Javadoc

| Elemento | Javadoc obligatorio | Observación |
|----------|--------------------|-|
| Interfaz de servicio público | Sí | Documenta el contrato |
| Método público de servicio | Sí | @param, @return, @throws |
| Enum de dominio | Sí, en la clase | Los valores son autoexplicativos |
| Implementación de servicio | No (referencia a la interfaz) | Solo si añade comportamiento no documentado en la interfaz |
| Controlador REST | No | OpenAPI/Swagger documenta la API |
| Entidades JPA | No, salvo campos no obvios | El esquema de BD tiene su propia documentación |
| Clases de configuración | No | El nombre y las anotaciones son suficientes |
| Métodos triviales (getters/setters) | No | El nombre ya dice todo |
| Métodos privados | Solo si el algoritmo no es obvio | Comentario de bloque, no Javadoc |

### 9.2 Formato de Javadoc

**Cabecera de clase o interfaz:**

```java
/**
 * Gestiona el ciclo de vida de los expedientes de pensión.
 *
 * <p>Un expediente agrupa toda la información necesaria para determinar
 * el derecho a pensión de un asegurado. Las operaciones de modificación
 * de estado requieren que el expediente esté en estado ACTIVO.</p>
 *
 * @author Nombre Apellido
 * @since 1.0.0
 */
public interface ExpedienteService { ... }
```

**Método de servicio:**

```java
/**
 * Obtiene el expediente de pensión identificado por el DNI del pensionista.
 *
 * @param dni Documento Nacional de Identidad (8 dígitos numéricos)
 * @return el expediente activo, o vacío si no existe registro para el DNI
 * @throws IllegalArgumentException si el DNI tiene formato inválido
 */
Optional<ExpedienteResponse> obtenerPorDni(String dni);

/**
 * Calcula el monto de pensión mensual aplicando las reglas vigentes.
 *
 * @param expedienteId identificador del expediente
 * @return monto calculado en soles con dos decimales
 * @throws ExpedienteNoEncontradoException si el expediente no existe
 * @throws ExpedienteEstadoInvalidoException si el expediente no está activo
 */
BigDecimal calcularMontoPension(Long expedienteId);
```

### 9.3 Comentarios internos (no Javadoc)

Un comentario interno solo justifica **por qué**, nunca explica **qué** (el código ya lo dice). Si necesitas explicar qué hace el código, es señal de que el código debe refactorizarse para ser más legible.

```java
// MAL — comenta el qué (obvio del código)
// Obtiene el expediente por id
var expediente = repository.findById(id);

// BIEN — comenta el por qué (no obvio)
// El cálculo usa el factor 1.03 porque el reglamento interno art. 24
// establece un reajuste anual fijo independientemente del IPC
BigDecimal montoReajustado = monto.multiply(new BigDecimal("1.03"));
```

**PROHIBIDO: código comentado.** Git conserva el historial; el código comentado solo ensucia.

```java
// PROHIBIDO
// expediente.setEstado(EstadoExpediente.SUSPENDIDO);
// notificarPensionista(expediente);
```

---

## sección 10 Logging estructurado

> **Fuente autoritativa:** Las normas de logging, trazabilidad y observabilidad están definidas en **LIN-OBS-001** (Lineamiento de Log Centralizado, Trazabilidad y Observabilidad). Este sección 10 es un resumen orientado a la implementación Java; ante cualquier conflicto, prevalece LIN-OBS-001. La configuración completa de `logback-spring.xml`, `Mask.java`, `CanonicalRequestLogFilter.java`, `RequestIdFilter.java`, política No PII y campos ECS se encuentran en LIN-OBS-001 secciones 4.6–4.10 y sección 6.

### 10.1 Framework mandatorio

| Usar | No usar |
|------|---------|
| SLF4J + Logback (Spring Boot default) | `System.out.println()` |
| `@Slf4j` de Lombok | Log4j directamente |
| `log.debug()`, `log.info()`, etc. | `java.util.logging` |

```java
// BIEN — @Slf4j genera: private static final Logger log = LoggerFactory.getLogger(...)
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpedienteServiceImpl implements ExpedienteService {

    private final ExpedienteRepository repository;

    public ExpedienteResponse obtener(Long id) {
        log.debug("Buscando expediente con id: {}", id);
        var expediente = repository.findById(id)
            .orElseThrow(() -> new ExpedienteNoEncontradoException(id));
        log.info("Expediente {} recuperado para DNI {}", id, expediente.getDni());
        return mapper.toResponse(expediente);
    }
}

// MAL
public class ExpedienteServiceImpl {
    public ExpedienteResponse obtener(Long id) {
        System.out.println("Buscando expediente: " + id);  // PROHIBIDO
        ...
    }
}
```

### 10.2 Niveles de log y criterio de uso

| Nivel | Prioridad | Cuándo usarlo | Ejemplo |
|-------|-----------|---------------|---------|
| `ERROR` | Alta | Error que requiere atención inmediata; siempre con stack trace | Fallo al conectar con BD, excepción no controlada |
| `WARN` | Media | Situación anómala pero recuperable; el sistema sigue funcionando | Reintento por timeout, dato faltante con valor por defecto |
| `INFO` | Baja | Evento de negocio significativo; legible por personal no técnico | Expediente creado, proceso de pago completado |
| `DEBUG` | Muy baja | Diagnóstico técnico para desarrollo; **nunca activo en producción por defecto** | Parámetros de entrada, resultado de consulta |

**Nivel por defecto en producción:**

| Paquete | Nivel |
|---|---|
| `pe.gob.onp.*` | `INFO` — permite registrar eventos de negocio significativos exigidos por LIN-ARQ-000 sección 9.5 |
| `org.springframework.*`, `org.hibernate.*`, librerías de terceros | `WARN` — reduce ruido de frameworks |

Subir `pe.gob.onp.*` a `DEBUG` solo para diagnóstico puntual y de forma temporal; revertir al terminar.

### 10.3 Formato de mensajes de log

Usar siempre los **parámetros `{}`** de SLF4J; nunca concatenar con `+`. SLF4J evalúa el mensaje solo si el nivel está habilitado, evitando el costo de construcción del string innecesariamente.

```java
// MAL — concatenación, construye el String aunque DEBUG esté desactivado
log.debug("Procesando expediente " + expediente.getId() + " de DNI " + expediente.getDni());

// BIEN — parámetros lazy, el String se construye solo si DEBUG está activo
log.debug("Procesando expediente {} de DNI {}", expediente.getId(), expediente.getDni());
```

**Proteger llamadas DEBUG** cuando la construcción del mensaje es costosa:

```java
if (log.isDebugEnabled()) {
    log.debug("Estado completo del expediente: {}", expediente.toDetailString());
}
```

**ERROR siempre con el objeto excepción** como último parámetro (SLF4J imprime el stack trace automáticamente):

```java
try {
    integrarConSistemaExterno(datos);
} catch (IntegracionException e) {
    log.error("Fallo al integrar expediente {} con sistema externo", expedienteId, e);
    throw new IntegracionExternaException("Fallo de integración", e);
}
```

### 10.4 Qué NO registrar (Política No PII)

> **Ver LIN-OBS-001 sección 6.2** para la tabla completa y los métodos de enmascaramiento. El incumplimiento viola la **Ley N.° 29733** de Protección de Datos Personales.

Nunca incluir en logs:
- Contraseñas, PINs, tokens de sesión o autenticación
- Datos personales sensibles en claro: DNI completo, nombre completo en mensajes de error
- Números de cuenta bancaria, datos de tarjeta
- Datos de salud o situación previsional detallada

Usar `Mask.*` (definida en LIN-OBS-001 sección 4.8) para enmascarar datos antes de loggearlos:

```java
// PROHIBIDO — datos en claro (Ley N.° 29733)
log.info("Usuario autenticado: {} con contraseña: {}", usuario, password);
log.info("Consulta para DNI: {}", dni);

// BIEN — identificador enmascarado o interno
log.info("Usuario autenticado: {}", usuario.getId());
log.info("Consulta para DNI: {}", Mask.dni(dni));   // Mask de LIN-OBS-001 sección 4.8
```

---

## sección 11 Manejo de excepciones en la capa REST

### 11.1 Handler global centralizado

Toda excepción no controlada debe ser capturada por un `@RestControllerAdvice` global que la convierta en una respuesta HTTP estándar. Los controladores **no** deben tener bloques `try/catch` para excepciones de negocio.

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${info.app.version:1.0.0}")
    private String appVersion;

    @ExceptionHandler(ExpedienteNoEncontradoException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleNotFound(ExpedienteNoEncontradoException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        var requestId = MDC.get("http.request.id");
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponseWrapper.error(404, "201", ex.getMessage(), null, requestId, appVersion));
    }

    @ExceptionHandler(ExpedienteEstadoInvalidoException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleEstadoInvalido(ExpedienteEstadoInvalidoException ex) {
        log.warn("Estado inválido para operación: {}", ex.getMessage());
        var requestId = MDC.get("http.request.id");
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponseWrapper.error(409, "409", ex.getMessage(), null, requestId, appVersion));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleValidacion(MethodArgumentNotValidException ex) {
        var requestId = MDC.get("http.request.id");
        List<ApiResponseWrapper.CampoError> errores = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> new ApiResponseWrapper.CampoError(e.getField(), e.getDefaultMessage()))
            .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponseWrapper.error(400, "100", "Error de validacion en los datos enviados.", errores, requestId, appVersion));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleGenerico(Exception ex) {
        log.error("Error no controlado", ex);
        var requestId = MDC.get("http.request.id");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponseWrapper.error(500, "500", "Error interno del servidor. Referencie el requestId al equipo de soporte.", null, requestId, appVersion));
    }
}
```

> Se utiliza `ApiResponseWrapper` (definido en [sección 11.4.4](#1144-estructura-de-respuesta-estandar-apiresponsewrapper)) como envoltorio estándar de todas las respuestas de error en cumplimiento con **LIN-API-REST-001**. Esto garantiza homogeneidad en el formato devuelto por las APIs de la institución.

### 11.2 Tabla de HTTP status codes

| Situación | HTTP Status |
|-----------|------------|
| Recurso no encontrado | 404 Not Found |
| Datos de entrada inválidos | 400 Bad Request |
| Estado de negocio inválido para la operación | 409 Conflict |
| Sin autorización | 401 Unauthorized |
| Sin permiso suficiente | 403 Forbidden |
| Error interno del servidor | 500 Internal Server Error |
| Error en integración con sistema externo | 502 Bad Gateway |

### 11.3 PROHIBIDO

- Exponer stack traces en respuestas al cliente
- Retornar `200 OK` con un campo `error` en el cuerpo
- Silenciar excepciones en el controlador con `try/catch` sin relanzar

---

## sección 12 Calidad de código

### 12.1 Métricas mínimas obligatorias

| Métrica | Umbral | Herramienta |
|---------|--------|-------------|
| Complejidad ciclomática por método | ≤ 10 | Checkstyle / PMD |
| Longitud máxima de método | 30 líneas | Checkstyle |
| Longitud máxima de clase | 500 líneas | Checkstyle |
| Cobertura de pruebas — servicios de negocio | ≥ 80% | JaCoCo |
| Cobertura de pruebas — clases de utilidad | ≥ 90% | JaCoCo |
| Cobertura de pruebas — controladores REST | ≥ 70% | JaCoCo |

### 12.2 Tabla de antipatrones prohibidos

| Antipatrón | Alternativa correcta |
|------------|---------------------|
| `System.out.println()` | `log.debug()` / `log.info()` |
| `catch (Exception e) {}` (silencioso) | Registrar y relanzar, o manejar apropiadamente |
| Retornar `null` para indicar ausencia | `Optional<T>` o colección vacía |
| Números/cadenas mágicas en lógica | `enum` o constante `static final` |
| `Vector`, `Hashtable`, `Enumeration` | `ArrayList`, `HashMap`, `Iterator` |
| `StringBuffer` en código monohilo | `StringBuilder` |
| `@Autowired` en campo de instancia | Inyección por constructor |
| `float`/`double` para valores monetarios | `BigDecimal` |
| `new String("texto")` | `"texto"` directamente |
| `import java.util.*` (wildcard) | Import explícito por clase |
| Código comentado en rama principal | Eliminar; git conserva la historia |
| `Optional.get()` sin verificar presencia | `.orElseThrow()`, `.orElse()`, `.ifPresent()` |
| `@SneakyThrows` de Lombok | Declarar o manejar la excepción explícitamente |
| `@EqualsAndHashCode` en `@Entity` | `equals`/`hashCode` manuales basados en id de negocio |
| Prefijo húngaro en variables (`strNombre`) | Solo nombre descriptivo sin prefijo |


### 12.3 Análisis estático de código (PMD)

El análisis estático con PMD es **obligatorio para proyectos nuevos** y se adopta de forma gradual en proyectos existentes. Su objetivo es detectar problemas de diseño, rendimiento, seguridad y cumplimiento de estándares de forma automática, sin necesidad de ejecutar el programa.

| Alcance | Tratamiento |
|---|---|
| Proyectos nuevos | PMD obligatorio con ruleset institucional; violaciones críticas bloquean el build |
| Proyectos existentes | Recomendado; adopción gradual sin bloquear build inicialmente |

#### 10.3.1 Configuración de Maven (pom.xml)

Todo proyecto debe declarar el plugin de PMD en su archivo de construcción, forzando la compilación fallida (`failOnViolation`) en caso de detectar errores críticos de calidad:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-pmd-plugin</artifactId>
    <version>3.21.0</version>
    <configuration>
        <rulesets>
            <ruleset>${project.basedir}/onp-pmd-ruleset.xml</ruleset>
        </rulesets>
        <printFailingErrors>true</printFailingErrors>
        <verbose>true</verbose>
        <failOnViolation>true</failOnViolation>
        <failurePriority>1</failurePriority> <!-- Falla el build si hay reglas de prioridad 1 (Crítico) -->
        <linkXRef>false</linkXRef>
    </configuration>
    <executions>
        <execution>
            <id>pmd-check</id>
            <phase>verify</phase>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

#### 10.3.2 Archivo de reglas institucional (`onp-pmd-ruleset.xml`)

El archivo `onp-pmd-ruleset.xml` debe estar en la raíz de cada repositorio. Contiene una mezcla de reglas estándar y reglas de cumplimiento específicas de la ONP. Puedes [descargar el archivo de reglas onp-pmd-ruleset.xml](file:///home/carlos/Documentos/Telemetria-traza-swagger/Lineamientos_Nuevos_Borradores/desarrollo/plantillas/onp-pmd-ruleset.xml) directamente.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ruleset name="ONP Java Ruleset"
         xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0 http://pmd.sourceforge.net/ruleset_2_0_0.xsd">

    <description>Reglas de calidad de codigo obligatorias para desarrollos Java en la ONP.</description>

    <!-- 1. Reglas de Calidad Estructura y Limpieza -->
    <rule ref="category/java/bestpractices.xml">
        <exclude name="GuardLogStatement"/> <!-- Excluido para simplificar llamadas de log -->
    </rule>
    
    <!-- Evitar System.out.println() y e.printStackTrace() - Obligatorio por LIN-OBS-001 -->
    <rule ref="category/java/bestpractices.xml/SystemPrintln" message="ONP-STD: Use el logger de SLF4J (logback) en lugar de System.out.println()"/>
    <rule ref="category/java/bestpractices.xml/AvoidPrintStackTrace" message="ONP-STD: Use log.error('Mensaje', e) en lugar de e.printStackTrace()"/>

    <!-- 2. Reglas de Errores Comunes -->
    <rule ref="category/java/errorprone.xml">
        <exclude name="AvoidLiteralsInIfCondition"/> <!-- Demasiado restrictivo para constantes simples -->
    </rule>
    
    <!-- Prohíbe catch vacío o silencioso -->
    <rule ref="category/java/errorprone.xml/EmptyCatchBlock" message="ONP-STD: No se permiten bloques catch vacios. Gestione o registre la excepcion."/>

    <!-- 3. Reglas de Rendimiento -->
    <rule ref="category/java/performance.xml"/>

    <!-- 4. Reglas de Seguridad (LIN-SEC-APP-001) -->
    <rule ref="category/java/security.xml"/>
    <rule ref="category/java/security.xml/HardCodedCredential" message="ONP-STD: Se detecto una credencial o clave secreta fija en codigo. Mueva a variables de entorno o Kubernetes Secrets (LIN-K8S-001 sección 8) o variables de pipeline GitLab (LIN-CICD-001 sección 13.4)."/>

    <!-- 5. Reglas Personalizadas ONP (Validacion de Estandares) -->
    
    <!-- Regla 5.1: Prohibir inyección en campos con @Autowired (LIN-DEV-JAVA-001 sección 10.2) -->
    <rule name="EvitarInyeccionPorCampo"
          language="java"
          message="ONP-STD: La inyeccion en campos mediante @Autowired esta prohibida. Debe usar inyeccion por constructor."
          class="net.sourceforge.pmd.lang.rule.XPathRule">
        <description>Garantiza que todas las dependencias sean inyectadas por constructor para permitir pruebas unitarias limpias.</description>
        <priority>1</priority>
        <properties>
            <property name="xpath">
                <value>
                    <![CDATA[
                    //FieldDeclaration[annotation[@Image='Autowired']]
                    ]]>
                </value>
            </property>
        </properties>
    </rule>

    <!-- Regla 5.2: Advertir sobre métodos en @RestController que no usen ApiResponseWrapper (LIN-API-REST-001) -->
    <!-- Prioridad 2 (advertencia): algunos métodos legítimos no retornan ResponseEntity<ApiResponseWrapper> -->
    <!-- (health checks, redirects, streaming). Validar manualmente en code review antes de subir a prioridad 1. -->
    <rule name="ObligarWrapperRespuestas"
          language="java"
          message="ONP-STD: Los metodos de controladores REST (@RestController) deben retornar ResponseEntity wrapped con ApiResponseWrapper."
          class="net.sourceforge.pmd.lang.rule.XPathRule">
        <description>Asegura la homogeneidad de contratos de respuesta en todas las APIs de la ONP.</description>
        <priority>2</priority>
        <properties>
            <property name="xpath">
                <value>
                    <![CDATA[
                    //ClassOrInterfaceDeclaration[annotation[@Image='RestController']]
                    //MethodDeclaration[
                        not(ResultType//ClassOrInterfaceType[@Image='ResponseEntity' 
                            and (
                                TypeArguments//ClassOrInterfaceType[@Image='ApiResponseWrapper']
                                or TypeArguments//ClassOrInterfaceType[@Image='Void']
                            )])
                    ]
                    ]]>
                </value>
            </property>
        </properties>
    </rule>

    <!-- Regla 5.3: Sufijo de nomenclatura para controladores (LIN-DEV-JAVA-001) -->
    <rule name="NomenclaturaController"
          language="java"
          message="ONP-STD: Las clases anotadas con @RestController deben terminar con el sufijo 'Controller'."
          class="net.sourceforge.pmd.lang.rule.XPathRule">
        <description>Enforce convention naming for REST controllers.</description>
        <priority>2</priority>
        <properties>
            <property name="xpath">
                <value>
                    <![CDATA[
                    //ClassOrInterfaceDeclaration[annotation[@Image='RestController'] and not(ends-with(@Image, 'Controller'))]
                    ]]>
                </value>
            </property>
        </properties>
    </rule>

</ruleset>
```

### 12.4 Principios de Diseño Transversales (DRY, KISS, YAGNI)

> **Nota:** Los principios **SOLID** orientados a clases y métodos Java se desarrollan en la [sección 7](#7-principios-solid-aplicados-a-clases-y-métodos-java-21), mientras que los patrones **GoF** se definen en la [sección 8](#8-patrones-de-diseño-de-código-gof-en-spring-boot-3). A continuación se resumen las pautas de diseño arquitectónico y metodológico que complementan a SOLID.

Todo desarrollo en Java 21 y Spring Boot 3 dentro de la ONP debe regirse estrictamente por los principios fundamentales de ingeniería de software. Estos principios orientan la toma de decisiones técnicas para garantizar que el código sea mantenible, testeable y resistente a la degradación arquitectónica en el largo plazo.

#### 12.4.1 Resumen de Principios SOLID en el Ecosistema ONP

| Principio | Aplicación Práctica en Spring Boot 3 / Java 21 | Anti-patrón Prohibido |
|---|---|---|
| **S — Single Responsibility (SRP)** | Una clase debe tener una única razón para cambiar. Los servicios de aplicación orquestan; los servicios de dominio calculan y aplican reglas de negocio; los repositorios acceden a datos. | *God Objects* o servicios monolíticos (ej. `PensionService` con > 1000 líneas que valida HTTP, calcula rentas, llama a PL/SQL y formatea correos). |
| **O — Open/Closed (OCP)** | Abierto a extensión, cerrado a modificación. Usar polimorfismo, interfaces y el patrón Estrategia inyectado por Spring (`@Service`, `List<CalculadorPensionStrategy>`). | Sentencias `switch` o `if-else` en cadena que crecen infinitamente cada vez que aparece una nueva modalidad o régimen previsional. |
| **L — Liskov Substitution (LSP)** | Las clases derivadas o implementaciones de una interfaz deben poder sustituir a su abstracción sin alterar la corrección del programa ni lanzar excepciones inesperadas. | Implementaciones que lanzan `UnsupportedOperationException` en métodos de la interfaz o violan los contratos de retorno esperados. |
| **I — Interface Segregation (ISP)** | Es preferible tener múltiples interfaces específicas orientadas al cliente (o caso de uso) que una única interfaz de propósito general. | Interfaces gigantes (`IAdministracionSistema`) que obligan a los clientes a implementar métodos que no necesitan o no utilizan. |
| **D — Dependency Inversion (DIP)** | Los módulos de alto nivel (Dominio) no deben depender de los de bajo nivel (Infraestructura/JPA/REST); ambos deben depender de abstracciones (Puertos/Interfaces). | Importar clases de infraestructura (ej. `AportanteJpaEntity`, `WSO2Client`) directamente en las entidades o servicios de la capa de dominio puro. |

##### Ejemplos de Implementación SOLID (OCP y DIP en Spring Boot 3)

**INCORRECTO (Violación de OCP y DIP - Acoplamiento a implementaciones y condicionales infinitos):**
```java
@Service
public class CalculadoraPensionService {
    // Violación DIP: depende directamente del adaptador de persistencia JPA de infraestructura
    @Autowired
    private AportanteJpaRepository aportanteRepo;

    public BigDecimal calcular(String dni, String regimen) {
        AportanteJpaEntity aportante = aportanteRepo.buscarPorDni(dni);
        // Violación OCP: cada nuevo régimen obliga a modificar este método
        if ("DL_19990".equals(regimen)) {
            return aportante.getSueldoPromedio().multiply(new BigDecimal("0.50"));
        } else if ("DL_20530".equals(regimen)) {
            return aportante.getSueldoPromedio().multiply(new BigDecimal("0.80"));
        } else if ("RENTA_VITALICIA".equals(regimen)) {
            return aportante.getSueldoPromedio().multiply(new BigDecimal("0.65"));
        }
        throw new IllegalArgumentException("Régimen no soportado");
    }
}
```

**CORRECTO (Cumplimiento de OCP y DIP - Inyección de abstracciones y patrón Estrategia):**
```java
// Abstracción en el Dominio (Puerto)
public interface CalculadorRegimenStrategy {
    boolean soporta(String regimen);
    BigDecimal calcularPension(Aportante aportante);
}

@Service
public class CalculadoraPensionService {
    // Cumplimiento DIP: depende del puerto del repositorio en dominio
    private final AportanteRepository aportanteRepository;
    // Cumplimiento OCP: Spring inyecta todas las estrategias implementadas
    private final List<CalculadorRegimenStrategy> estrategias;

    public CalculadoraPensionService(AportanteRepository aportanteRepository,
                                   List<CalculadorRegimenStrategy> estrategias) {
        this.aportanteRepository = aportanteRepository;
        this.estrategias = estrategias;
    }

    public BigDecimal calcular(String dni, String regimen) {
        Aportante aportante = aportanteRepository.obtenerPorDni(dni)
            .orElseThrow(() -> new DomainException("Aportante no encontrado: " + dni));
            
        return estrategias.stream()
            .filter(e -> e.soporta(regimen))
            .findFirst()
            .orElseThrow(() -> new DomainException("Régimen previsional no soportado: " + regimen))
            .calcularPension(aportante);
    }
}
```

#### 12.4.2 DRY (Don't Repeat Yourself) — Reutilización Responsable

El principio DRY establece que toda pieza de conocimiento o lógica de negocio debe tener una representación única y autoritativa en el sistema.
- **Aplicación correcta:** Centralizar validaciones previsionales, algoritmos actuariales y transformaciones de datos en servicios de dominio o librerías institucionales aprobadas (`core-common`).
- **Límite arquitectónico (Prevención de Acoplamiento):** DRY se aplica a la *duplicación de conocimiento de negocio*, no necesariamente a la coincidencia accidental de código. Se prohíbe acoplar dos microservicios o módulos de dominio independientes compartiendo modelos de base de datos o clases internas únicamente por evitar duplicar 50 líneas de código (lo que generaría un monolito distribuido).

**INCORRECTO (Duplicación de regla de negocio previsional en múltiples controladores):**
```java
// En AfiliacionController.java
if (aportes < 240 || edad < 65) {
    throw new ValidacionException("No cumple requisitos mínimos de jubilación");
}
// En SolicitudPensionController.java (se repite la misma lógica mágica)
if (aportes < 240 || edad < 65) {
    throw new ValidacionException("No cumple requisitos mínimos de jubilación");
}
```

**CORRECTO (Conocimiento centralizado en el Dominio):**
```java
// En ReglasJubilacionDomainService.java (Dominio puro)
public void validarRequisitosJubilacion(int mesesAporte, int edadAños) {
    if (mesesAporte < MESES_MINIMOS_LEY || edadAños < EDAD_MINIMA_LEY) {
        throw new RequisitosJubilacionNoCumplidosException(mesesAporte, edadAños);
    }
}
```

#### 12.4.3 KISS (Keep It Simple, Stupid) — Simplicidad y Legibilidad

La simplicidad es un objetivo arquitectónico de primer nivel. El código debe ser directo, legible y fácil de entender para cualquier desarrollador que se incorpore al equipo.
- Aprovechar las características nativas de Java 21: preferir `Records` para DTOs inmutables, `Pattern Matching` y `Sealed Classes` en lugar de jerarquías complejas de herencia.
- Prohibido el "sobre-ingeniería" (over-engineering): evitar el uso innecesario de genéricos altamente complejos, metaprogramación excesiva, reflexión o patrones de diseño estéticos donde una función simple y clara resuelve el problema.

**INCORRECTO (Sobre-ingeniería y clases verbosas para transporte de datos simples):**
```java
public class AportanteResumenDto {
    private String dni;
    private String nombreCompleto;
    private BigDecimal totalAportes;

    public AportanteResumenDto() {}
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public BigDecimal getTotalAportes() { return totalAportes; }
    public void setTotalAportes(BigDecimal totalAportes) { this.totalAportes = totalAportes; }
    // + equals, hashCode, toString... (50 líneas de boilerplate)
}
```

**CORRECTO (Uso de Records nativos de Java 21):**
```java
public record AportanteResumenDto(
    String dni,
    String nombreCompleto,
    BigDecimal totalAportes
) {}
```

#### 12.4.4 YAGNI (You Aren't Gonna Need It) — Cero Esfuerzo Especulativo

No se debe escribir código, interfaces ni abstracciones basándose en suposiciones de necesidades futuras no confirmadas en el alcance actual del requerimiento o ticket funcional.
- **Regla estricta:** Si una abstracción o interfaz tiene una única implementación y no existe evidencia arquitectónica ni requerimiento formal de múltiples implementaciones futuras, se debe implementar de forma directa (o con una interfaz simple sin capas de indirección vacías).
- Toda funcionalidad especulativa o "por si acaso" es considerada deuda técnica prematura y será rechazada en la revisión de Pull Request.

---

## sección 13 Convenciones Spring Boot

### 13.1 Configuración

- Usar `application.yml` (preferido sobre `.properties`) por legibilidad y soporte de jerarquía
- Separar configuración por perfil: `application-dev.yml`, `application-prod.yml`
- Prefijo de propiedades personalizadas ONP: `onp.<sistema>.<propiedad>`

```yaml
# application.yml
spring:
  application:
    name: pensiones-expedientes
  datasource:
    url: ${DB_URL}          # NUNCA el valor en el archivo
    username: ${DB_USER}
    password: ${DB_PASSWORD}

onp:
  pensiones:
    monto-minimo-pension: 500.00
    factor-reajuste-anual: 1.03
```

**Secretos y credenciales:** nunca en archivos de configuración del repositorio. En runtime de aplicación usar Kubernetes Secrets (ver **LIN-K8S-001 sección 8**); en el contexto de pipeline CI/CD usar variables protegidas de GitLab (ver **LIN-CICD-001 sección 13.4**). La política de secretos se rige por **LIN-SEC-APP-001 sección 12**.

### 13.2 Validación de datos de entrada

Validar en la **capa de controlador** con Bean Validation. La capa de servicio asume datos válidos.

```java
// Controlador — activa la validación con @Valid
@PostMapping
public ResponseEntity<ExpedienteResponse> crear(
        @Valid @RequestBody CrearExpedienteRequest request) {
    var response = expedienteService.crear(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

// Record de request — define las restricciones
public record CrearExpedienteRequest(
    @NotNull @Pattern(regexp = "\\d{8}") String dni,
    @NotBlank @Size(max = 200)           String nombresCompletos,
    @NotNull @Past                        LocalDate fechaNacimiento
) {}
```

El `GlobalExceptionHandler` ([sección 9.1](#91-handler-global-centralizado)) captura `MethodArgumentNotValidException` automáticamente.

### 13.3 Transacciones

- `@Transactional` solo en la **capa de servicio** (implementación, no interfaz)
- En repositorios: Spring Data JPA ya gestiona transacciones en sus métodos
- En controladores: **nunca**

```java
@Service
@RequiredArgsConstructor
public class ExpedienteServiceImpl implements ExpedienteService {

    private final ExpedienteRepository repository;

    @Override
    @Transactional(readOnly = true)     // solo lectura: optimiza la sesión JPA
    public Optional<ExpedienteResponse> obtenerPorDni(String dni) {
        return repository.findByDni(dni).map(mapper::toResponse);
    }

    @Override
    @Transactional                       // escritura: garantiza atomicidad
    public ExpedienteResponse crear(CrearExpedienteRequest request) {
        var expediente = mapper.toDomain(request);
        var guardado = repository.save(expediente);
        log.info("Expediente creado con id {}", guardado.getId());
        return mapper.toResponse(guardado);
    }
}
```

> Ver sección 3.4 del Lineamiento de Arquitectura (doc. interno) para la discusión ACID/CAP y el rol de `@Transactional` en la estrategia de consistencia.

### 11.4 API REST y documentación OpenAPI

Todo servicio que exponga endpoints HTTP debe seguir las convenciones de esta sección sin excepción. **La documentación Swagger es un requisito de entrega**: un servicio sin documentación no se considera completo y no debe pasar a revisión de código.

**Convenciones del controlador:**
- URLs en `kebab-case`: `/expedientes-pension`, `/periodos-aportacion`
- Un controlador por recurso de dominio
- Métodos del controlador delegados íntegramente al servicio; sin lógica de negocio
- Respuestas tipadas con `ApiResponseWrapper` ([sección 11.4.4](#1144-estructura-de-respuesta-estandar-apiresponsewrapper)); nunca retornar `Object` o `Map<String, Object>` sin tipado
- Las anotaciones Swagger se colocan **únicamente** en los `@RestController`; no en Service ni Repository

#### 11.4.1 Dependencia Maven

Agregar en `pom.xml`:

```xml
<!-- SpringDoc: genera OpenAPI 3.0 y Swagger UI para Spring Boot 3.x -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```

#### 13.4.2 Bean de configuración OpenAPI

Crear una sola vez por proyecto en `src/main/java/<paquete-base>/config/OpenApiConfig.java`. Puedes [descargar la plantilla OpenApiConfig.java](file:///home/carlos/Documentos/Telemetria-traza-swagger/Lineamientos_Nuevos_Borradores/desarrollo/plantillas/OpenApiConfig.java) directamente.

```java
package pe.gob.onp.pensiones.expedientes.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI(
            @Value("${spring.application.name}") String appName,
            @Value("${info.app.version:1.0.0}") String version,
            @Value("${app.description:Servicio web ONP}") String desc) {
        return new OpenAPI()
            .info(new Info()
                .title(appName)
                .version(version)
                .description(desc)
                .contact(new Contact()
                    .name("OTI — Innovacion y Desarrollo")
                    .email("oti@onp.gob.pe")))
            .externalDocs(new ExternalDocumentation()
                .description("Documentacion interna ONP")
                .url("https://gitlab.onp.gob.pe"));
    }
}
```

#### 13.4.3 Configuración por entorno

**`application-dev.yml` y `application-qa.yml`** — Swagger habilitado en entornos no productivos:

```yaml
swagger:
  enabled: true

springdoc:
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
  api-docs:
    enabled: true
```

**`application-prod.yml`** — Swagger deshabilitado por defecto en producción:

```yaml
swagger:
  enabled: ${SWAGGER_ENABLED:false}

springdoc:
  swagger-ui:
    enabled: ${SWAGGER_ENABLED:false}
  api-docs:
    enabled: ${SWAGGER_ENABLED:false}
```

Para habilitar Swagger temporalmente en PROD sin redesplegar, agregar la variable al `Deployment` de Kubernetes y revertirla al terminar:

```yaml
env:
  - name: SWAGGER_ENABLED
    value: "true"
```

> **ADVERTENCIA:** La exposición permanente del API spec en producción es un riesgo de seguridad. Deshabilitar (`SWAGGER_ENABLED=false`) inmediatamente después de la actividad.

#### 13.4.4 Estructura de respuesta estándar — ApiResponseWrapper

> **Fuente autoritativa:** el contrato de `ApiResponseWrapper<T>` está definido en **LIN-API-REST-001**. Esta sección documenta únicamente la implementación Java. Cualquier cambio al contrato debe hacerse en LIN-API-REST-001 y reflejarse aquí.

Todos los endpoints retornan `ApiResponseWrapper<T>`. Crear una sola vez por proyecto en `src/main/java/<paquete-base>/dto/common/ApiResponseWrapper.java`. Puedes [descargar la plantilla ApiResponseWrapper.java](file:///home/carlos/Documentos/Telemetria-traza-swagger/Lineamientos_Nuevos_Borradores/desarrollo/plantillas/ApiResponseWrapper.java) directamente.

```java
package pe.gob.onp.pensiones.expedientes.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Estructura de respuesta estandar ONP")
public class ApiResponseWrapper<T> {

    private Integer      codHttp;
    private String       codDetRespuesta;
    private String       menDetRespuesta;
    private T            data;
    private List<CampoError> errors;
    private Meta         meta;

    public ApiResponseWrapper(Integer codHttp, String codDetRespuesta, String menDetRespuesta,
                              T data, List<CampoError> errors,
                              String requestId, String version) {
        this.codHttp         = codHttp;
        this.codDetRespuesta = codDetRespuesta;
        this.menDetRespuesta = menDetRespuesta;
        this.data            = data;
        this.errors          = errors;
        this.meta            = new Meta(Instant.now().toString(), requestId, version);
    }

    public static <T> ApiResponseWrapper<T> ok(T data, String requestId, String version) {
        return new ApiResponseWrapper<>(200, "000",
            "Operacion completada correctamente.", data, null, requestId, version);
    }

    public static <T> ApiResponseWrapper<T> error(int codHttp, String codDet, String msg,
            List<CampoError> errors, String requestId, String version) {
        return new ApiResponseWrapper<>(codHttp, codDet, msg, null, errors, requestId, version);
    }

    public Integer           getCodHttp()         { return codHttp; }
    public String            getCodDetRespuesta() { return codDetRespuesta; }
    public String            getMenDetRespuesta() { return menDetRespuesta; }
    public T                 getData()            { return data; }
    public List<CampoError>  getErrors()          { return errors; }
    public Meta              getMeta()            { return meta; }

    @Schema(description = "Metadatos de la respuesta")
    public static class Meta {
        private String timestamp;
        private String requestId;
        private String version;

        public Meta(String timestamp, String requestId, String version) {
            this.timestamp = timestamp;
            this.requestId = requestId;
            this.version   = version;
        }
        public String getTimestamp() { return timestamp; }
        public String getRequestId() { return requestId; }
        public String getVersion()   { return version; }
    }

    @Schema(description = "Error de validacion de un campo")
    public static class CampoError {
        private String campo;
        private String mensaje;

        public CampoError(String campo, String mensaje) {
            this.campo  = campo;
            this.mensaje = mensaje;
        }
        public String getCampo()   { return campo; }
        public String getMensaje() { return mensaje; }
    }
}
```

**Uso en un controller:**

```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponseWrapper<ExpedienteResponse>> obtener(@PathVariable Long id) {
    var requestId = MDC.get("http.request.id");
    var data = expedienteService.obtener(id);
    return ResponseEntity.ok(ApiResponseWrapper.ok(data, requestId, appVersion));
}
```

**Campos de la respuesta:**

| Campo | Tipo | Descripción | Obligatorio |
|-------|------|-------------|-------------|
| `codHttp` | Integer | Código de estado HTTP de la respuesta | Sí |
| `codDetRespuesta` | String | Código institucional ONP (3 dígitos) | Sí |
| `menDetRespuesta` | String | Mensaje descriptivo del resultado | Sí |
| `data` | T / null | Resultado tipado de la operación; null en caso de error | Sí |
| `errors` | Array / null | Errores de validación por campo; null si no aplica | Solo en validación |
| `meta.timestamp` | String | Fecha y hora ISO 8601 UTC | Sí |
| `meta.requestId` | String | ID de correlación X-Request-ID leído del MDC | Sí |
| `meta.version` | String | Versión del servicio que respondió | Sí |

**Tabla de códigos `codDetRespuesta`:** la tabla completa y autoritativa de códigos está en **LIN-API-REST-001 sección 4.1**. No se duplica aquí para evitar desincronización. Ante cualquier duda sobre qué código usar en una situación específica, consultar ese documento.

#### 13.4.5 Filtro de correlación — RequestIdFilter

Crear una sola vez por proyecto en `src/main/java/<paquete-base>/filter/RequestIdFilter.java`. Habilita el campo `meta.requestId` en todas las respuestas y lo propaga al MDC para correlacionar peticiones entre servicios en los logs. Puedes [descargar la plantilla RequestIdFilter.java](file:///home/carlos/Documentos/Telemetria-traza-swagger/Lineamientos_Nuevos_Borradores/desarrollo/plantillas/RequestIdFilter.java) directamente.

```java
package pe.gob.onp.pensiones.expedientes.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Request-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest req,
            HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String requestId = req.getHeader(HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put("http.request.id", requestId);
        res.setHeader(HEADER, requestId);
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.remove("http.request.id");
        }
    }
}
```

**Comportamiento:** lee `X-Request-ID` de la petición; si el cliente no lo envía, genera un UUID propio. Añade el valor al MDC (visible en logs estructurados) y lo devuelve en el header de la respuesta para que el cliente pueda correlacionarlo.

#### 13.4.6 Anotaciones Swagger en Controllers

**`@Tag` a nivel de clase** — agrupa los endpoints bajo un nombre de dominio en Swagger UI:

```java
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Expedientes",
     description = "Operaciones de gestión de expedientes de pensión.")
@RestController
@RequestMapping("/api/v1/expedientes")
public class ExpedienteController { }
```

| Atributo | Regla |
|----------|-------|
| `name` | Nombre del dominio en español, singular, mayúscula inicial. Sin "Controller" ni "API". Ej: `Expedientes` |
| `description` | Qué agrupa el controller. Terminar con punto. |

**`@Operation` y `@ApiResponses` por método** — los imports son comunes para todos los métodos:

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
```

**GET — Consulta por ID:**

```java
@Operation(
    summary = "Obtener expediente por ID",
    description = "Retorna los datos de un expediente a partir de su identificador interno.")
@ApiResponses({
    @ApiResponse(responseCode = "200",
        description = "Expediente encontrado.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
    @ApiResponse(responseCode = "404",
        description = "Expediente no encontrado.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
    @ApiResponse(responseCode = "500",
        description = "Error interno del servidor.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
})
@GetMapping("/{id}")
public ResponseEntity<ApiResponseWrapper> obtener(@PathVariable Long id) { ... }
```

**POST — Creación de recurso:**

```java
@Operation(
    summary = "Crear expediente de pensión",
    description = "Registra un nuevo expediente a partir de los datos del pensionista.")
@ApiResponses({
    @ApiResponse(responseCode = "200",
        description = "Expediente creado correctamente.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
    @ApiResponse(responseCode = "400",
        description = "Error de validacion en los datos enviados.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
    @ApiResponse(responseCode = "500",
        description = "Error interno del servidor.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
})
@PostMapping
public ResponseEntity<ApiResponseWrapper> crear(
        @RequestBody @Valid CrearExpedienteRequest request) { ... }
```

**PUT — Actualización de recurso:**

```java
@Operation(
    summary = "Actualizar expediente",
    description = "Actualiza los datos de un expediente existente. " +
                  "Solo se modifican los campos incluidos en el cuerpo de la peticion.")
@ApiResponses({
    @ApiResponse(responseCode = "200",
        description = "Expediente actualizado correctamente.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
    @ApiResponse(responseCode = "400",
        description = "Error de validacion en los datos enviados.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
    @ApiResponse(responseCode = "404",
        description = "Expediente no encontrado.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
    @ApiResponse(responseCode = "500",
        description = "Error interno del servidor.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
})
@PutMapping("/{id}")
public ResponseEntity<ApiResponseWrapper> actualizar(
        @PathVariable Long id,
        @RequestBody @Valid ActualizarExpedienteRequest request) { ... }
```

**DELETE — Eliminación de recurso:**

```java
@Operation(
    summary = "Eliminar expediente",
    description = "Elimina el registro de un expediente del sistema. La operacion es irreversible.")
@ApiResponses({
    @ApiResponse(responseCode = "204",
        description = "Expediente eliminado correctamente."),
    @ApiResponse(responseCode = "404",
        description = "Expediente no encontrado.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
    @ApiResponse(responseCode = "500",
        description = "Error interno del servidor.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
})
@DeleteMapping("/{id}")
public ResponseEntity<Void> eliminar(@PathVariable Long id) { ... }
```

**Referencia rápida de códigos HTTP por método:**

| Método | 200 | 201 | 204 | 400 | 404 | 500 |
|--------|-----|-----|-----|-----|-----|-----|
| GET | Recurso encontrado | — | — | — | No encontrado | Error servidor |
| POST | — | **Recurso creado** | — | Datos inválidos | — | Error servidor |
| PUT | Actualizado | — | — | Datos inválidos | No encontrado | Error servidor |
| DELETE | — | — | Eliminado OK | — | No encontrado | Error servidor |

> El POST exitoso de creación retorna `201 Created` con el recurso creado en el cuerpo — `ResponseEntity.status(HttpStatus.CREATED).body(...)`. El DELETE exitoso retorna `204` sin cuerpo — `ResponseEntity<Void>`.

**Reglas para `@Operation`:**

| Atributo | Regla |
|----------|-------|
| `summary` | Verbo en infinitivo + objeto. Máximo 80 caracteres, sin punto final. Ej: `Obtener expediente por ID` |
| `description` | Qué recibe, qué valida, qué retorna. Terminar con punto. Opcional si el `summary` es suficientemente claro. |

**Reglas para `@ApiResponse`:**

| Atributo | Regla |
|----------|-------|
| `responseCode` | String entre comillas. Documentar siempre: 200/204 (éxito), 400 (si hay body de request), 404 (si busca por ID), 500. |
| `description` | Cuándo ocurre esa respuesta. Terminar con punto. |
| `content` | Siempre `ApiResponseWrapper.class`. Omitir únicamente en 204. |

#### 13.4.7 Anotaciones @Schema en DTOs

Agregar `@Schema` en las clases DTO para que Swagger UI muestre descripciones y ejemplos en el formulario de prueba:

```java
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para crear un nuevo expediente de pensión")
public record CrearExpedienteRequest(

    @Schema(description = "DNI del pensionista (8 dígitos numéricos)", example = "12345678")
    @NotNull @Pattern(regexp = "\\d{8}")
    String dni,

    @Schema(description = "Nombres y apellidos completos", example = "Juan Perez Garcia")
    @NotBlank @Size(max = 200)
    String nombresCompletos,

    @Schema(description = "Fecha de nacimiento en formato ISO 8601", example = "1965-03-15")
    @NotNull @Past
    LocalDate fechaNacimiento
) {}
```

#### 13.4.8 Propagación de MDC en entornos asíncronos y multihilo

Dado que MDC (`Mapped Diagnostic Context`) utiliza internamente almacenamiento a nivel de hilo (`ThreadLocal`), el contexto de correlación (`http.request.id` y `user.id`) se pierde cuando se delega el trabajo a hilos secundarios, incluyendo llamadas `@Async`, tareas programadas `@Scheduled` o hilos virtuales.

Para asegurar la trazabilidad distribuida, se debe registrar un `TaskDecorator` que copie el contexto de MDC al crear nuevos hilos. Puedes [descargar la plantilla AsyncMdcConfig.java](file:///home/carlos/Documentos/Telemetria-traza-swagger/Lineamientos_Nuevos_Borradores/desarrollo/plantillas/AsyncMdcConfig.java) directamente.

```java
package pe.gob.onp.pensiones.expedientes.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Bean;
import java.util.Map;

@Configuration
@EnableAsync
public class AsyncMdcConfig {

    @Bean
    public TaskDecorator mdcTaskDecorator() {
        return runnable -> {
            // Captura el contexto del hilo padre
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    runnable.run();
                } finally {
                    // Limpia el MDC al retornar el hilo al pool
                    MDC.clear();
                }
            };
        };
    }
}
```

### 13.5 Patrones Tácticos de Dominio (Repository, Domain Service, Application Service)

En coherencia con los estilos arquitectónicos de Monolito Modular y Arquitectura Hexagonal / Limpia promovidos por **LIN-ARQ-000**, el diseño interno de los componentes en Spring Boot 3 debe segregar claramente las responsabilidades en tres patrones tácticos fundamentales.

#### 13.5.1 Application Service (Servicios de Aplicación / Orquestadores)

Los servicios de aplicación son el punto de entrada transaccional para los casos de uso del sistema. Su rol es coordinar la ejecución del flujo, delegar las decisiones al dominio y conectar con la infraestructura, sin contener reglas de negocio puras.

- **Ubicación en el paquete:** `pe.gob.onp.<sistema>.<modulo>.application.service`
- **Responsabilidades:**
  - Gestionar el límite transaccional (uso de `@Transactional` de Spring).
  - Coordinar la autenticación, autorización y validación de permisos de usuario (en integración con SAA).
  - Iniciar o continuar spans de observabilidad (`@NewSpan` según **LIN-OBS-001**).
  - Invocar repositorios para cargar entidades del dominio, invocar a los servicios de dominio para aplicar las reglas de negocio, y guardar los cambios.
  - Publicar eventos de integración o de dominio (ej. vía Spring ApplicationEventPublisher o CloudEvents / Kafka según **LIN-BUS-001**).
- **Prohibiciones:** No deben ejecutar cálculos previsionales, condicionales complejos de negocio ni consultas SQL directas.
- **Nomenclatura (Sección 4.2):** En la convención de nombres de la ONP, el rol arquitectónico de "Application Service" corresponde directamente a la interfaz con sufijo `Service` (ej. `SolicitudPensionService`) y su implementación con sufijo `ServiceImpl` (ej. `SolicitudPensionServiceImpl`), o directamente como clase `Service` en estilos modulares.

> **Referencia Institucional:** Ver plantillas completas en [SolicitudPensionService.java](file:///home/carlos/Documentos/Telemetria-traza-swagger/Lineamientos_Nuevos_Borradores/desarrollo/plantillas/SolicitudPensionService.java) e [SolicitudPensionServiceImpl.java](file:///home/carlos/Documentos/Telemetria-traza-swagger/Lineamientos_Nuevos_Borradores/desarrollo/plantillas/SolicitudPensionServiceImpl.java).

**Ejemplo de Orquestación Transaccional y Observabilidad en Application Service:**
```java
// pe.gob.onp.pensiones.solicitud.application.service.SolicitudPensionServiceImpl
@Service
public class SolicitudPensionServiceImpl implements SolicitudPensionService {

    private final AportanteRepository aportanteRepository;
    private final CalculoPensionVitaliciaDomainService calculoDomainService;
    private final ApplicationEventPublisher eventPublisher;

    public SolicitudPensionServiceImpl(AportanteRepository aportanteRepository,
                                     CalculoPensionVitaliciaDomainService calculoDomainService,
                                     ApplicationEventPublisher eventPublisher) {
        this.aportanteRepository = aportanteRepository;
        this.calculoDomainService = calculoDomainService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @NewSpan("solicitud-pension-registrar")
    public SolicitudPensionResponse registrarSolicitud(@SpanTag("dni") String dni, int añosAporte) {
        // 1. Carga de entidad vía puerto del repositorio en dominio
        Aportante aportante = aportanteRepository.obtenerPorDni(dni)
            .orElseThrow(() -> new AportanteNoEncontradoException(dni));

        // 2. Delegación de lógica previsional al servicio de dominio puro (POJO)
        BigDecimal montoCalculado = calculoDomainService.calcularMontoVitalicio(aportante, añosAporte);
        aportante.asignarPensionVitalicia(montoCalculado);

        // 3. Persistencia de cambios
        aportanteRepository.guardar(aportante);

        // 4. Publicación de evento de dominio (LIN-BUS-001)
        eventPublisher.publishEvent(new SolicitudPensionRegistradaEvent(aportante.getId(), montoCalculado));

        return new SolicitudPensionResponse(aportante.getId(), montoCalculado, "REGISTRADA");
    }
}
```

#### 13.5.2 Domain Service (Servicios de Dominio) — Pureza Hexagonal y Registro DI

Los servicios de dominio encapsulan la lógica de negocio pura, reglas previsionales, cálculos actuariales o invariantes que no pertenecen de forma natural a una sola entidad o que requieren coordinar múltiples agregados del dominio.

- **Ubicación en el paquete:** `pe.gob.onp.<sistema>.<modulo>.domain.service`
- **Pureza del Dominio (Mandatorio):** Un servicio de dominio **debe ser una clase Java pura (POJO)**. Queda estrictamente prohibido incluir en esta capa anotaciones de infraestructura de Spring (`@Service`, `@Component`, `@Transactional`, `@Autowired`, `@Value`), anotaciones de persistencia JPA (`@Entity`, `@Table`) o dependencias de librerías de transporte/serialización (Jackson, OpenAPI, WSO2, clientes SOAP).
- **Estandarización de Registro en Spring Boot (`@Configuration` / `@Bean`):** Para preservar la pureza hexagonal sin renunciar a la gestión del ciclo de vida y la inyección de dependencias del contenedor de Spring, los servicios de dominio se registrarán obligatoriamente mediante clases de configuración (`@Configuration`) en la capa de aplicación o infraestructura del módulo.

> **Referencia Institucional:** Ver plantilla completa en [CalculoPensionVitaliciaDomainService.java](file:///home/carlos/Documentos/Telemetria-traza-swagger/Lineamientos_Nuevos_Borradores/desarrollo/plantillas/CalculoPensionVitaliciaDomainService.java).

**Ejemplo de Servicio de Dominio Puro (POJO sin anotaciones Spring):**
```java
// pe.gob.onp.pensiones.calculo.domain.service.CalculoPensionVitaliciaDomainService
public class CalculoPensionVitaliciaDomainService {
    
    private final TablaActuarialRepository tablaActuarialRepository;

    // Constructor puro para inyección por parámetro
    public CalculoPensionVitaliciaDomainService(TablaActuarialRepository tablaActuarialRepository) {
        this.tablaActuarialRepository = tablaActuarialRepository;
    }

    public BigDecimal calcularMontoVitalicio(Aportante aportante, int añosAporte) {
        if (añosAporte < 20) {
            throw new ReglaPrevisionalException("Años de aporte insuficientes para renta vitalicia");
        }
        BigDecimal factor = tablaActuarialRepository.obtenerFactorEsperanzaVida(aportante.getEdad());
        return aportante.getFondoAcumulado().multiply(factor);
    }
}
```

**Ejemplo de Registro e Inyección vía `@Configuration` en Capa de Aplicación:**
```java
// pe.gob.onp.pensiones.calculo.application.config.DomainServiceConfig
@Configuration
public class DomainServiceConfig {

    @Bean
    public CalculoPensionVitaliciaDomainService calculoPensionVitaliciaDomainService(
            TablaActuarialRepository tablaActuarialRepository) {
        // Spring inyecta la implementación del repositorio y gestiona el bean como Singleton
        return new CalculoPensionVitaliciaDomainService(tablaActuarialRepository);
    }
}
```

#### 13.5.3 Repository (Repositorios de Dominio vs. Adaptadores de Persistencia)

El patrón Repositorio media entre el dominio y las capas de mapeo de datos, actuando como una colección en memoria de entidades de dominio.

- **Segregación Hexagonal y Nomenclatura (Sección 12):**
  - **Puerto (Interfaz en Dominio):** La interfaz del repositorio (`AportanteRepository`) se define en la capa de dominio (`...domain.repository`). No expone tipos de JPA ni excepciones de base de datos; retorna entidades de dominio y tipos `Optional`.
  - **Adaptador (Implementación en Infraestructura):** Reside en la capa de infraestructura (`...infrastructure.persistence`) y adopta obligatoriamente el sufijo `JpaRepository` (ej. `AportanteJpaRepository`) si utiliza Spring Data JPA, o `JdbcRepository` / `OracleRepository` (ej. `AportanteJdbcRepository`) si utiliza `JdbcTemplate` o llamados a procedimientos PL/SQL.
- **Cumplimiento con LIN-BD-ORA-001:** Todo adaptador de repositorio que invoque procedures o packages PL/SQL legacy debe cumplir estrictamente con:
  - Encapsular la llamada mediante puertos limpios.
  - Traducir las excepciones técnicas de Oracle (`RAISE_APPLICATION_ERROR`, SQLCODE) a excepciones limpias de la jerarquía de aplicación, preservando el *backtrace* técnico sin exponer el stacktrace SQL a la capa REST.
  - Utilizar obligatoriamente *bind variables* (o parámetros parametrizados por Spring/JDBC) para prevenir inyecciones SQL y optimizar el plan cache de Oracle.

> **Referencia Institucional:** Ver plantilla completa en [AportanteJdbcRepository.java](file:///home/carlos/Documentos/Telemetria-traza-swagger/Lineamientos_Nuevos_Borradores/desarrollo/plantillas/AportanteJdbcRepository.java).

**Ejemplo de Puerto en Dominio y Adaptador Oracle con Traducción de Excepciones:**
```java
// PUERTO EN DOMINIO: pe.gob.onp.pensiones.solicitud.domain.repository.AportanteRepository
public interface AportanteRepository {
    Optional<Aportante> obtenerPorDni(String dni);
    void guardar(Aportante aportante);
}

// ADAPTADOR EN INFRAESTRUCTURA: pe.gob.onp.pensiones.solicitud.infrastructure.persistence.AportanteJdbcRepository
@Repository
public class AportanteJdbcRepository implements AportanteRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Aportante> rowMapper;

    public AportanteJdbcRepository(JdbcTemplate jdbcTemplate, RowMapper<Aportante> rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    @Override
    public Optional<Aportante> obtenerPorDni(String dni) {
        final String sql = "SELECT ID_APORTANTE, C_DNI, N_EDAD, N_FONDO_ACUMULADO, IN_ACTIVO " +
                           "FROM PE_ESQ_PENSIONES.TBL_APORTANTE WHERE C_DNI = ? AND IN_ACTIVO = 1";
        try {
            List<Aportante> resultados = jdbcTemplate.query(sql, rowMapper, dni);
            return resultados.stream().findFirst();
        } catch (DataAccessException ex) {
            throw traducirExcepcionOracle("obtenerPorDni", ex);
        }
    }

    private RuntimeException traducirExcepcionOracle(String operacion, DataAccessException ex) {
        if (ex.getRootCause() instanceof SQLException sqlEx) {
            int sqlCode = sqlEx.getErrorCode();
            if (sqlCode >= 20000 && sqlCode <= 20999) {
                // Traducción de error de negocio lanzado por PL/SQL (RAISE_APPLICATION_ERROR)
                return new ReglaPrevisionalOracleException(sqlEx.getMessage(), sqlCode);
            }
            return new InfrastructureException("Error técnico de BD en " + operacion + " [SQLCODE=" + sqlCode + "]", sqlEx);
        }
        return new InfrastructureException("Error de acceso a datos en " + operacion, ex);
    }
}
```

---

## sección 14 Estructura de proyecto y gestión de dependencias Maven

> Este sección 14 es la referencia de implementación para las estructuras definidas conceptualmente en el **Lineamiento de Arquitectura sección 7**.

### 14.1 Estructura de proyecto por estilo arquitectónico

La elección de estructura sigue directamente del estilo arquitectónico declarado — no es libre.

| Estructura | Estilo | Cuándo usar |
|---|---|---|
| **Monolito simple (capas)** | Layered | Sistema sin candidatura a microservicio; lógica Transaction Script o Active Record |
| **Monolito Modular** | Modular | Punto de llegada por defecto para todo sistema nuevo |
| **Hexagonal / Clean** | Hexagonal | Módulo que cumple los seis criterios de microservicio — obligatorio antes de extraer |

#### Monolito simple (capas)

Un único módulo Maven. Tres paquetes con responsabilidades diferenciadas.

```
onp-{sistema}/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/pe/gob/onp/{sistema}/
    │   │   ├── controller/      ← REST Controllers, DTOs de entrada/salida
    │   │   ├── service/         ← Lógica de aplicación y negocio
    │   │   ├── repository/      ← Interfaces Spring Data JPA
    │   │   ├── entity/          ← Entidades JPA
    │   │   ├── dto/             ← DTOs de request y response
    │   │   ├── exception/       ← Excepciones y handler global
    │   │   └── config/          ← Configuración Spring, OpenAPI
    │   └── resources/
    │       ├── application.yml
    │       └── logback-spring.xml
    └── test/
        └── java/pe/gob/onp/{sistema}/
            ├── controller/      ← @WebMvcTest
            ├── service/         ← Unitarias con Mockito
            └── repository/      ← @DataJpaTest
```

**Regla de dependencia:** `controller → service → repository`. Los controllers no acceden a repositorios directamente.

#### Monolito Modular (multi-módulo Maven)

Cinco módulos Maven con fronteras explícitas. Es el punto de destino por defecto para todo sistema nuevo en ONP.

```
onp-{sistema}/
├── pom.xml                          ← POM padre (packaging: pom)
│
├── onp-{sistema}-domain/            ← Sin dependencias de framework
│   ├── pom.xml
│   └── src/main/java/pe/gob/onp/{sistema}/domain/
│       ├── model/                   ← Entidades, agregados, value objects
│       ├── port/
│       │   ├── in/                  ← Ports de entrada (casos de uso)
│       │   └── out/                 ← Ports de salida (repositorios, clientes)
│       └── exception/               ← Excepciones de dominio
│
├── onp-{sistema}-application/       ← Depende solo de domain
│   ├── pom.xml
│   └── src/main/java/pe/gob/onp/{sistema}/application/
│       ├── service/                 ← Implementaciones de ports de entrada
│       ├── command/                 ← Objetos de comando (entrada)
│       └── dto/                     ← DTOs de respuesta de casos de uso
│
├── onp-{sistema}-infrastructure/    ← Depende de domain y application
│   ├── pom.xml
│   └── src/main/java/pe/gob/onp/{sistema}/infrastructure/
│       ├── persistence/             ← Entidades JPA, repositorios Spring Data
│       ├── client/                  ← Clientes HTTP (RestClient, Feign)
│       ├── messaging/               ← Productores/consumidores de eventos
│       └── mapper/                  ← Mappers entre capas
│
├── onp-{sistema}-api/               ← Depende de application
│   ├── pom.xml
│   └── src/main/java/pe/gob/onp/{sistema}/api/
│       ├── controller/              ← REST Controllers
│       ├── dto/                     ← Request/Response DTOs de la API
│       └── mapper/                  ← Mappers API DTO ↔ Application DTO
│
└── onp-{sistema}-boot/              ← Depende de todos los módulos
    ├── pom.xml
    └── src/main/java/pe/gob/onp/{sistema}/
        ├── Application.java         ← @SpringBootApplication
        └── src/main/resources/
            ├── application.yml
            ├── application-dev.yml
            ├── application-qa.yml
            ├── application-prod.yml
            └── logback-spring.xml
```

**Regla de dependencia entre módulos:**

```
domain          ← no depende de ningún otro módulo del proyecto
application     ← depende de domain
infrastructure  ← depende de domain y application
api             ← depende de application
boot            ← depende de api e infrastructure (ensamblador)
```

- `domain` no importa nada de `infrastructure`, `api` ni `boot`
- `api` no accede a `infrastructure` directamente — siempre pasa por `application`
- Prohibidas las dependencias circulares entre módulos

#### Hexagonal / Clean (candidato a microservicio)

Obligatorio para todo módulo que cumpla los seis criterios de microservicio (Lineamiento de Arquitectura sección 3.4). Puede vivir dentro del Monolito Modular como un módulo más, o desplegado de forma independiente — la estructura interna es la misma en ambos casos.

```
onp-{modulo}/                        ← puede ser un submódulo del Monolito Modular
├── pom.xml
└── src/
    ├── main/
    │   └── java/pe/gob/onp/{modulo}/
    │       ├── domain/              ← ANILLO INTERIOR — cero imports de framework
    │       │   ├── model/           ← Entidades de dominio, Value Objects, Agregados
    │       │   ├── port/
    │       │   │   ├── in/          ← Ports de entrada: casos de uso (interfaces)
    │       │   │   └── out/         ← Ports de salida: repositorios, clientes (interfaces)
    │       │   └── exception/       ← Excepciones de dominio
    │       ├── application/         ← CAPA DE APLICACIÓN
    │       │   └── usecase/         ← Implementaciones de ports de entrada (casos de uso)
    │       └── infrastructure/      ← ANILLO EXTERIOR — implementa los ports
    │           ├── web/             ← Adapter de entrada: Controllers REST, DTOs HTTP
    │           ├── persistence/     ← Adapter de salida: Entidades JPA, Spring Data repos
    │           ├── client/          ← Adapter de salida: clientes HTTP externos (RENIEC, SUNAT)
    │           ├── messaging/       ← Adapter de salida: productores/consumidores de eventos
    │           └── config/          ← Configuración Spring, wiring de ports y adapters
    └── test/
        └── java/pe/gob/onp/{modulo}/
            ├── domain/              ← Unitarias puras: JUnit 5 sin Spring, sin Mockito de infra
            └── infrastructure/
                ├── web/             ← @WebMvcTest
                ├── persistence/     ← @DataJpaTest + Testcontainers
                └── client/          ← WireMock
```

**Regla de dependencia:** `infrastructure → domain`. El dominio no importa nada de `infrastructure`. Spring, JPA, RestClient — todo eso existe solo en `infrastructure`.

El siguiente ejemplo muestra cómo se aplica esta regla con el concepto de **port/adapter**: el dominio define solo la interfaz que necesita (el port), sin saber cómo se implementa. La infraestructura implementa esa interfaz usando JPA, Spring Data u otra tecnología (el adapter). El dominio nunca ve ni importa ninguna clase de Spring o JPA.

```java
// ──────────────────────────────────────────────────────────────
// PORT (en domain/port/out/)
// El dominio define QUÉ necesita: "necesito poder buscar
// un Pensionista por DNI". No sabe si viene de Oracle,
// de una API externa o de memoria — eso no le importa.
// Este archivo no tiene ningún import de Spring ni de JPA.
// ──────────────────────────────────────────────────────────────
public interface PensionistaRepository {
    Optional<Pensionista> buscarPorDni(String dni);
}

// ──────────────────────────────────────────────────────────────
// ADAPTER (en infrastructure/persistence/)
// La infraestructura dice CÓMO lo hace: con JPA y Spring Data.
// Implementa el port del dominio, por eso el dominio no necesita
// saber que existe JPA. Si mañana se cambia Oracle por otro motor,
// solo cambia esta clase — el dominio no se toca.
// ──────────────────────────────────────────────────────────────
@Repository
public class PensionistaJpaRepository implements PensionistaRepository {

    private final PensionistaJpaDao jpa; // Spring Data JPA — solo existe aquí

    @Override
    public Optional<Pensionista> buscarPorDni(String dni) {
        return jpa.findByDni(dni)
                  .map(PensionistaMapper::toDomain); // convierte Entity JPA → objeto de dominio
    }
}
```

**Señal de alerta:** si una clase dentro de `domain/` importa `jakarta.persistence`, `org.springframework` o cualquier librería de infraestructura, la regla está rota y la arquitectura dejó de ser hexagonal.

**Correspondencia con Clean Architecture y Onion:**

| Hexagonal (ONP) | Clean Architecture | Onion Architecture |
|---|---|---|
| `domain/model` | Entities | Domain Model |
| `application/usecase` | Use Cases / Interactors | Application Services |
| `domain/port/in` | Input Port | — |
| `domain/port/out` | Output Port / Gateway | Repository interfaces |
| `infrastructure/web` | Interface Adapters (Controllers) | UI / Infrastructure |
| `infrastructure/persistence` | Interface Adapters (Gateways) | Infrastructure |

### 14.2 Convenciones de nomenclatura Maven

| Elemento | Convención | Ejemplo |
|---|---|---|
| Artifact ID del proyecto | `onp-{sistema}` | `onp-pensiones` |
| Group ID | `pe.gob.onp.{sistema}` | `pe.gob.onp.pensiones` |
| Módulo Maven | `onp-{sistema}-{capa}` | `onp-pensiones-domain` |
| Paquete raíz | `pe.gob.onp.{sistema}.{capa}` | `pe.gob.onp.pensiones.domain` |
| Clase de entidad JPA | `{Entidad}Entity` | `PensionistaEntity` |
| Clase de dominio | `{Entidad}` | `Pensionista` |
| DTO de request | `{Accion}{Recurso}Request` | `CrearExpedienteRequest` |
| DTO de response | `{Recurso}Response` | `ExpedienteResponse` |
| Servicio de aplicación | `{Accion}{Recurso}Service` | `AprobarExpedienteService` |
| Adapter de infraestructura | `{Sistema}HttpAdapter` | `ReniecHttpAdapter` |

### 14.3 Configuración del POM por estilo

La estructura del POM varía según el estilo arquitectónico. El punto de decisión clave es si el sistema produce **un único artefacto desplegable** (un solo módulo Maven) o **varios artefactos independientes** (multi-módulo con POM padre).

#### Monolito simple — un solo módulo Maven

El POM declara directamente `spring-boot-starter-parent` como padre. No existe POM padre personalizado.

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.X</version>
    <relativePath/>
</parent>

<groupId>pe.gob.onp</groupId>
<artifactId>onp-{sistema}</artifactId>
<version>1.0.0</version>
<packaging>jar</packaging>
```

#### Monolito Modular — multi-módulo Maven

Existe un POM padre personalizado que declara los módulos y centraliza versiones de dependencias internas. Los módulos hijo **no** especifican versión en dependencias propias del sistema.

**POM padre** (`pom.xml` en la raíz del repositorio):

```xml
<groupId>pe.gob.onp.{sistema}</groupId>
<artifactId>onp-{sistema}</artifactId>
<version>1.0.0-SNAPSHOT</version>
<packaging>pom</packaging>

<modules>
    <module>onp-{sistema}-domain</module>
    <module>onp-{sistema}-application</module>
    <module>onp-{sistema}-infrastructure</module>
    <module>onp-{sistema}-api</module>
    <module>onp-{sistema}-boot</module>
</modules>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring-boot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Disponible en todos los módulos hijos -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**POM hijo** (ejemplo: `onp-{sistema}-application/pom.xml`):

```xml
<parent>
    <groupId>pe.gob.onp.{sistema}</groupId>
    <artifactId>onp-{sistema}</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>

<artifactId>onp-{sistema}-application</artifactId>
<packaging>jar</packaging>

<dependencies>
    <dependency>
        <groupId>pe.gob.onp.{sistema}</groupId>
        <artifactId>onp-{sistema}-domain</artifactId>
        <version>${project.version}</version>
    </dependency>
</dependencies>
```

#### Hexagonal / Clean — un solo módulo Maven

La separación en hexagonal es de **paquetes Java** (`domain`, `infrastructure`), no de módulos Maven. El POM usa directamente `spring-boot-starter-parent`, igual que el monolito simple.

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.X</version>
    <relativePath/>
</parent>

<groupId>pe.gob.onp</groupId>
<artifactId>onp-{modulo}</artifactId>
<version>1.0.0</version>
<packaging>jar</packaging>
```

**Resumen:**

| Estilo arquitectónico | POM padre personalizado | Módulos Maven |
|---|---|---|
| Monolito simple | No — Spring Boot parent directo | 1 |
| Monolito Modular | Sí | N (uno por capa/dominio funcional) |
| Hexagonal / Clean | No — Spring Boot parent directo | 1 |

### 14.4 Reglas de dependencias de librerías

- Las versiones se definen en el **POM padre** o en un BOM importado; los módulos hijo no especifican versiones.
- Usar el scope correcto:
  - `test` — JUnit, Mockito, Testcontainers
  - `provided` — Lombok (el compilador lo necesita, no el runtime)
  - `runtime` — drivers de BD (no referenciar la clase Driver directamente)
- No declarar dependencias transitivas explícitamente salvo que haya conflicto de versión documentado.
- Verificar la licencia de toda dependencia de terceros antes de incluirla.

### 14.5 Plugins estándar obligatorios en CI

```xml
<!-- Compilación y procesamiento de anotaciones (Lombok + MapStruct) -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <source>21</source>
        <target>21</target>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok-mapstruct-binding</artifactId>
                <version>0.2.0</version>
            </path>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>${mapstruct.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>

<!-- Construcción del jar ejecutable -->
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>

<!-- Verificación de estilo de código -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>${checkstyle.plugin.version}</version>
    <configuration>
        <configLocation>checkstyle-onp.xml</configLocation>
        <failsOnError>true</failsOnError>
        <consoleOutput>true</consoleOutput>
    </configuration>
    <executions>
        <execution>
            <phase>validate</phase>
            <goals><goal>check</goal></goals>
        </execution>
    </executions>
</plugin>

<!-- Cobertura de pruebas -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>${jacoco.version}</version>
    <executions>
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <execution>
            <id>check</id>
            <phase>verify</phase>
            <goals><goal>check</goal></goals>
            <configuration>
                <excludes>
                    <exclude>**/dto/**</exclude>
                    <exclude>**/entity/**</exclude>
                    <exclude>**/config/**</exclude>
                    <exclude>**/exception/**</exclude>
                    <exclude>**/*Application.*</exclude>
                </excludes>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## sección 15 Pruebas

> Para la estrategia completa de pruebas (pirámide por estilo arquitectónico, proporción unitaria/integración/e2e) ver **sección 8 del Lineamiento de Arquitectura** (doc. interno).

### 15.1 Nomenclatura de tests

| Elemento | Convención | Ejemplo |
|----------|-----------|---------|
| Clase de test unitario | `<ClaseTesteada>Test` | `ExpedienteServiceImplTest` |
| Clase de test de integración | `<ClaseTesteada>IT` | `ExpedienteControllerIT` |
| Método de test | `deberia<Resultado>Cuando<Condicion>` | `deberiaLanzarExcepcionCuandoExpedienteNoExiste` |

### 15.2 Estructura de test (AAA)

Todo test debe seguir la estructura **Arrange → Act → Assert**, con una línea en blanco entre secciones:

```java
@Test
@DisplayName("Debe lanzar ExpedienteNoEncontradoException cuando el id no existe")
void deberiaLanzarExcepcionCuandoExpedienteNoExiste() {
    // Arrange
    var idInexistente = 999L;
    when(repository.findById(idInexistente)).thenReturn(Optional.empty());

    // Act & Assert (para excepciones, ambas fases van juntas)
    assertThatThrownBy(() -> service.obtener(idInexistente))
        .isInstanceOf(ExpedienteNoEncontradoException.class)
        .hasMessageContaining("999");
}

@Test
@DisplayName("Debe retornar el expediente cuando el id existe")
void deberiaRetornarExpedienteCuandoIdExiste() {
    // Arrange
    var expediente = ExpedienteTestFactory.unExpedienteActivo();
    when(repository.findById(expediente.getId())).thenReturn(Optional.of(expediente));

    // Act
    var resultado = service.obtener(expediente.getId());

    // Assert
    assertThat(resultado.id()).isEqualTo(expediente.getId());
    assertThat(resultado.estado()).isEqualTo(EstadoExpediente.ACTIVO);
}
```

### 15.3 Cobertura mínima por capa

| Capa | Cobertura mínima | Tipo de prueba |
|------|-----------------|---------------|
| Servicios de negocio | 80% | Unitarias (Mockito) |
| Controladores REST | 70% | Integración (`@WebMvcTest`) |
| Repositorios (queries custom) | 100% de los custom queries | Integración (Testcontainers) |
| Clases de utilidad | 90% | Unitarias |
| Mappers (MapStruct) | No obligatorio | MapStruct genera código verificado |

### 15.4 Testcontainers para repositorios

Las pruebas de repositorio no usan H2 ni base de datos en memoria: usan la **misma base de datos** que producción vía Testcontainers. ONP usa **Oracle 19c** en producción — las pruebas deben usar `OracleContainer` para garantizar que el SQL, las funciones de fecha, las secuencias y el comportamiento de NULL sean idénticos al entorno real.

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ExpedienteRepositoryTest {

    @Container
    static OracleContainer oracle = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart")
            .withDatabaseName("XEPDB1")
            .withUsername("onp_test")
            .withPassword("onp_test");

    @DynamicPropertySource
    static void configurarDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", oracle::getJdbcUrl);
        registry.add("spring.datasource.username", oracle::getUsername);
        registry.add("spring.datasource.password", oracle::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "oracle.jdbc.OracleDriver");
    }

    @Autowired
    private ExpedienteRepository repository;

    @Test
    void deberiaBuscarExpedientePorDni() {
        // Arrange
        var expediente = new Expediente();
        expediente.setDni("12345678");
        repository.save(expediente);

        // Act
        var resultado = repository.findByDni("12345678");

        // Assert
        assertThat(resultado).isPresent();
    }
}
```

---

## sección 16 Revisión de código

### 16.1 Pull Request como gate obligatorio

**Ningún cambio se integra a la rama principal sin Pull Request aprobado.** La autoaprobación está prohibida. Todo PR requiere al menos un revisor técnico distinto al autor.

### 16.2 Condiciones mínimas para aprobar un PR

Un PR no puede aprobarse si alguna de las siguientes condiciones no se cumple:

| Condición | Verificación |
|---|---|
| Pipeline de CI verde | Build, tests y Checkstyle pasados sin errores |
| Cobertura de pruebas dentro del umbral | JaCoCo no reporta degradación por debajo del mínimo del [sección 13.3](#133-cobertura-minima-por-capa) |
| Sin antipatrones del [sección 10.2](#102-tabla-de-antipatrones-prohibidos) | El revisor verifica la tabla de antipatrones prohibidos |
| Sin credenciales ni secretos en el código | Búsqueda manual o con herramienta de detección de secretos |
| Sin código comentado | Ver [sección 7.3](#73-comentarios-internos-no-javadoc) — el código comentado no llega a rama principal |
| Descripción del PR completa | Contexto del cambio, qué se modificó y cómo probarlo |

### 16.3 Responsabilidades del revisor

- Verificar que la lógica de negocio implementada coincide con el requerimiento
- Verificar que la estructura de paquetes sigue el estilo arquitectónico declarado (sección 3)
- Verificar que los tests cubren los casos de borde relevantes
- No aprobar un PR que no entienda completamente — preguntar es parte del proceso

### 16.4 Tamaño máximo de PR

Un PR que modifica más de **400 líneas** de código productivo (excluidos tests y configuración) es señal de que debe dividirse. PRs grandes reducen la calidad de la revisión y aumentan el riesgo de integración.

### 16.5 Proceso de excepción

Cualquier desviación de este estándar — incluyendo omitir la revisión por urgencia — requiere **ADR aprobado por Arquitectura** con: contexto, decisión, consecuencias, vigencia de la excepción y fecha de revisión. Ver sección 17.

### 16.6 Patrón Feature Toggle y Deuda Técnica Cero (PA14)

En alineación con el patrón arquitectónico **PA14 (Feature Toggle)** de **LIN-ARQ-000** y las directivas de control de cambios de **LIN-VER-001**, el uso de *Feature Toggles* (o banderas de funcionalidad) es un mecanismo permitido para el despliegue continuo y la entrega progresiva, pero está sujeto a un riguroso control de ciclo de vida para garantizar la **deuda técnica cero**.

#### 14.6.1 Estrategia Tecnológica Oficial en Dos Niveles
Para mantener un stack mínimo, homogéneo y eficiente en Spring Boot 3, la ONP estandariza la implementación de *Feature Toggles* en dos niveles operativos, en conformidad con **ADR-014 (LIN-ARQ-000 Apéndice A)**:

| Nivel Operativo | Tecnologías Estándar | Cuándo Utilizar |
|---|---|---|
| **Nivel 1: Nativo / Estático** | `Spring Profiles` y `@ConditionalOnProperty` / `@ConditionalOnExpression` configurados en `application.yml` o inyectados vía **K8s ConfigMaps**. | Toggles estructurales, activación de adaptadores externos, migraciones de infraestructura o funcionalidades de conmutación poco frecuente que toleran el reinicio del pod en el despliegue. |
| **Nivel 2: Dinámico / Runtime** | **Unleash** como estándar institucional on-premise mandatorio. Librerías cliente en Java como **Togglz** se permiten únicamente como proveedor conectado al backend de Unleash (`togglz-unleash-provider`). | Exclusivamente para funcionalidades de alta criticidad o conmutación frecuente en tiempo real en Producción, donde el negocio o la operación requiere apagar/encender flujos sin reiniciar contenedores en Kubernetes. Cualquier plataforma externa alternativa requiere ADR de Arquitectura y Seguridad. |

**Ejemplo de Toggle Nivel 1 (Nativo con `@ConditionalOnProperty`):**
```java
@Configuration
@ConditionalOnProperty(name = "onp.features.calculador-v2.enabled", havingValue = "true", matchIfMissing = false)
public class CalculadorV2Config {
    
    @Bean
    public CalculoPensionDomainService calculoPensionDomainService(TablaActuarialRepository repo) {
        return new CalculoPensionV2DomainService(repo);
    }
}
```

**Ejemplo de Toggle Nivel 2 (Dinámico en Runtime con Cliente Togglz / Unleash Provider):**
```java
// pe.gob.onp.pensiones.solicitud.application.service.SolicitudPensionServiceImpl
@Service
public class SolicitudPensionServiceImpl implements SolicitudPensionService {

    private final FeatureManager featureManager; // Cliente Togglz conectado al backend institucional Unleash (ADR-014)
    private final CalculoPensionVitaliciaDomainService calculoV1;
    private final CalculoPensionV2DomainService calculoV2;

    public SolicitudPensionServiceImpl(FeatureManager featureManager,
                                     CalculoPensionVitaliciaDomainService calculoV1,
                                     CalculoPensionV2DomainService calculoV2) {
        this.featureManager = featureManager;
        this.calculoV1 = calculoV1;
        this.calculoV2 = calculoV2;
    }

    public BigDecimal calcularPension(Aportante aportante, int añosAporte) {
        // Evaluación dinámica en runtime sin reiniciar pod (Release Toggle - caducidad mandatoria: 1 sprint tras Go-Live)
        if (featureManager.isActive(OnpFeatures.CALCULO_ACTUARIAL_V2)) {
            return calculoV2.calcularMontoVitalicio(aportante, añosAporte);
        }
        return calculoV1.calcularMontoVitalicio(aportante, añosAporte);
    }
}
```

#### 14.6.2 Ciclo de Vida y Caducidad Acotada (Deuda Técnica Cero)
En estricta coherencia con **LIN-ARQ-000 §2.2.1.A**, la obligación de caducidad y retiro del código fuente aplica diferenciadamente según la clasificación del toggle:

| Clasificación según LIN-ARQ-000 | Plazo y Obligación de Retiro |
|---|---|
| **Release Toggle** | **Corto plazo (Mandatorio):** Se elimina en el sprint inmediatamente posterior al go-live (plazo máximo: **1 sprint / 14 días calendario**). |
| **Experiment Toggle** | **Medio plazo (Mandatorio):** Se elimina una vez concluido el experimento previsional o la validación A/B (plazo máximo según definición del experimento, típicamente **1 sprint / 14 días calendario** tras el veredicto). |
| **Ops Toggle** | **Largo plazo (Exento de borrado automático):** Permanece en el código mientras el caso de uso se considere crítico para la resiliencia operativa o degradación controlada. Sujeto a auditoría periódica. |
| **Permission Toggle** | **Largo plazo o Permanente (Exento de borrado automático):** Controla características reservadas a roles, suscripciones o segmentos especiales (en coordinación con SAA). |

Para los toggles de caducidad obligatoria (*Release* y *Experiment*), su existencia añade complejidad ciclomática y ramas condicionales que deben limpiarse rigurosamente:

| Fase del Toggle | Acción Obligatoria (*Release* y *Experiment*) |
|---|---|
| **Creación (Desarrollo / MR)** | Todo toggle debe declararse con un propósito claro, un ticket asociado y una **fecha máxima de caducidad proyectada** en los comentarios o en el catálogo de variables/toggles del proyecto. |
| **Estabilización (Go-Live / PROD)** | Una vez que la funcionalidad asociada ha sido desplegada en Producción (`PUBLISHED`), estabilizada y verificada por el negocio, el toggle entra en estado de expiración. |
| **Retiro / Limpieza (Gate PA14)** | Es **obligatorio** crear una tarea técnica en el sprint inmediatamente posterior al go-live (plazo máximo: **1 sprint / 14 días calendario**) para eliminar completamente las ramas condicionales del código, remover la configuración del toggle y eliminar la funcionalidad obsoleta o reemplazada. |

#### 14.6.3 Verificación en Pull Request (Gate de Revisión)
Durante la revisión de código (Sección 14.2), el revisor y el líder técnico deben hacer cumplir las siguientes reglas incompatibles con la aprobación del PR:
1. **Rechazar toggles sin fecha de retiro:** Todo nuevo PR que introduzca un *Release Toggle* o *Experiment Toggle* debe indicar en la descripción del PR y en el código cuándo y cómo se eliminará.
2. **Rechazar toggles anidados:** Se prohíbe anidar condicionales de múltiples *Feature Toggles* dentro del mismo bloque de lógica de dominio (ej. `if (featureA.isEnabled()) { if (featureB.isEnabled()) { ... } }`).
3. **Auditoría periódica de código muerto:** No se aprobarán nuevos PRs de características a equipos o módulos que acumulen *Release/Experiment Toggles* expirados y no retirados del código fuente, haciendo efectiva la contención de deuda técnica institucional.

---

## sección 17 Proceso de excepción a este estándar

> **Importante:** **Gobernanza y Supremacía de LIN-ARQ-000:** En estricta coherencia con la supremacía jerárquica del marco rector de **Nivel 2**, ningún ADR podrá ser aprobado ni será válido si contraviene los principios arquitectónicos fundamentales (PR01–PR08) o mandatos rectores de **LIN-ARQ-000**, salvo autorización expresa y excepcional de la Dirección de Arquitectura de la OTI.

Toda desviación de las reglas establecidas en este documento requiere un ADR (Architecture Decision Record) aprobado formalmente por el equipo de Arquitectura de la OTI antes de implementarse.

El ADR debe incluir:

| Campo | Descripción |
|---|---|
| Contexto | Qué situación o restricción impide cumplir el estándar |
| Decisión | Qué se hará en lugar de seguir el estándar |
| Alternativas evaluadas | Qué otras opciones se consideraron y por qué se descartaron |
| Consecuencias | Qué riesgos o deuda técnica introduce la excepción |
| Vigencia | Hasta cuándo aplica la excepción (fecha o condición) |
| Responsable | Quién asume la responsabilidad técnica de la excepción |
| Revisión | Fecha en que se revisará si la excepción sigue siendo válida |

**No se acepta la urgencia como justificación para omitir este proceso.** Las excepciones urgentes se aprueban rápido, no se omiten.

---

## Anexo A: Plantilla Javadoc estándar ONP

### A.1 Cabecera de clase o interfaz

```java
/**
 * [Descripción concisa de la responsabilidad de la clase, una o dos oraciones.]
 *
 * <p>[Párrafo opcional con detalles relevantes: restricciones, invariantes,
 * consideraciones de uso.]</p>
 *
 * @author Nombre Apellido
 * @since X.Y.Z
 */
```

### A.2 Método de servicio público

```java
/**
 * [Descripción de lo que hace el método: verbo en tercera persona singular.]
 *
 * @param nombreParam [Descripción del parámetro, incluyendo restricciones de formato si aplica]
 * @return [Descripción del valor retornado; si es Optional, describir cuándo está vacío]
 * @throws NombreExcepcion [Condición que provoca la excepción]
 */
```

### A.3 Enum de dominio

```java
/**
 * Estados posibles de un expediente de pensión a lo largo de su ciclo de vida.
 *
 * @author Nombre Apellido
 * @since 1.0.0
 */
public enum EstadoExpediente {

    /** Expediente vigente; se pueden realizar operaciones sobre él. */
    ACTIVO,

    /** Expediente temporalmente bloqueado por auditoría o proceso judicial. */
    SUSPENDIDO,

    /** Expediente cerrado definitivamente. No admite modificaciones. */
    CANCELADO
}
```

---

## Anexo B: Configuración Checkstyle recomendada

Archivo `checkstyle-onp.xml` a colocar en la raíz del proyecto o en un módulo de configuración compartido. Puedes [descargar el archivo checkstyle-onp.xml](file:///home/carlos/Documentos/Telemetria-traza-swagger/Lineamientos_Nuevos_Borradores/desarrollo/plantillas/checkstyle-onp.xml) directamente.

```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
    "https://checkstyle.org/dtds/configuration_1_3.dtd">
<module name="Checker">

    <property name="charset" value="UTF-8"/>
    <property name="severity" value="error"/>

    <module name="TreeWalker">

        <!-- Nomenclatura -->
        <module name="TypeName"/>              <!-- UpperCamelCase para tipos -->
        <module name="MethodName"/>            <!-- lowerCamelCase para métodos -->
        <module name="LocalVariableName"/>     <!-- lowerCamelCase para variables -->
        <module name="ConstantName"/>          <!-- UPPER_SNAKE_CASE para constantes -->
        <module name="PackageName">
            <property name="format" value="^[a-z]+(\.[a-z][a-z0-9]*)*$"/>
        </module>

        <!-- Imports -->
        <module name="AvoidStarImport"/>       <!-- sin wildcard imports -->
        <module name="UnusedImports"/>         <!-- sin imports no usados -->
        <module name="RedundantImport"/>

        <!-- Formato -->
        <module name="Indentation">
            <property name="basicOffset" value="4"/>
            <property name="tabWidth" value="4"/>
        </module>
        <module name="LineLength">
            <property name="max" value="120"/>
        </module>
        <module name="NeedBraces"/>            <!-- llaves siempre obligatorias -->
        <module name="LeftCurly"/>             <!-- llave de apertura en la misma línea -->
        <module name="OneStatementPerLine"/>   <!-- una sentencia por línea -->
        <module name="MultipleVariableDeclarations"/> <!-- una variable por declaración -->

        <!-- Complejidad -->
        <module name="CyclomaticComplexity">
            <property name="max" value="10"/>
        </module>
        <module name="MethodLength">
            <property name="max" value="30"/>
        </module>

        <!-- Buenas prácticas -->
        <module name="EmptyCatchBlock"/>       <!-- catch vacío prohibido -->
        <module name="DefaultComesLast"/>      <!-- default al final de switch -->
        <module name="MissingSwitchDefault"/> <!-- switch sin default prohibido -->
        <module name="EqualsHashCode"/>        <!-- equals y hashCode juntos o ninguno -->
        <module name="StringLiteralEquality"/> <!-- no comparar String con == -->

    </module>

    <!-- Longitud máxima de archivo -->
    <module name="FileLength">
        <property name="max" value="500"/>
    </module>

</module>
```

---

## Anexo C: Tabla completa de sufijos de clase

| Sufijo | Tipo | Anotación Spring | Capa | Ejemplo |
|--------|------|-----------------|------|---------|
| `Controller` | Controlador REST | `@RestController` | Web | `ExpedienteController` |
| `Service` | Interfaz de servicio (Application Service) | — | Aplicación | `ExpedienteService`, `SolicitudPensionService` |
| `ServiceImpl` | Implementación de servicio (Application Service) | `@Service` | Aplicación | `ExpedienteServiceImpl`, `SolicitudPensionServiceImpl` |
| `DomainService` | Servicio de dominio puro (POJO) | — (`@Bean` en Config) | Dominio | `CalculoPensionVitaliciaDomainService` |
| `Repository` | Puerto de repositorio (Dominio) | — | Dominio | `ExpedienteRepository`, `AportanteRepository` |
| `JpaRepository` | Adaptador JPA (Infraestructura) | `@Repository` | Infraestructura | `ExpedienteJpaRepository` |
| `JdbcRepository` / `OracleRepository` | Adaptador JDBC/Oracle (Infraestructura) | `@Repository` | Infraestructura | `AportanteJdbcRepository` |
| *(sin sufijo)* | Entidad de dominio | `@Entity` | Dominio | `Expediente` |
| *(sin sufijo)* | Objeto de valor | — | Dominio | `Monto`, `Periodo` |
| `Request` | DTO de entrada | — | Web | `CrearExpedienteRequest` |
| `Response` | DTO de salida | — | Web | `ExpedienteResponse` |
| `Dto` | DTO interno entre capas | — | Aplicación | `ExpedienteDto` |
| `Mapper` | Mapeador entre capas | `@Mapper` (MapStruct) | Aplicación | `ExpedienteMapper` |
| `Exception` | Excepción de dominio o técnica | — | Dominio/Infra | `ExpedienteNoEncontradoException` |
| `Config` | Clase de configuración | `@Configuration` | Infra | `SecurityConfig` |
| `Constants` | Clase de constantes de dominio | — | Dominio | `EstadoExpedienteConstants` |
| *(sin sufijo)* | Enum de dominio | — | Dominio | `EstadoExpediente` |
| `Util` | Utilidades sin estado | — | Transversal | `FechaUtil` |
| `Aspect` | Aspecto (AOP) | `@Aspect` | Infra | `AuditoriaAspect` |
| `Filter` | Filtro HTTP | — | Web | `AutenticacionFilter` |
| `Handler` | Manejador de excepciones | `@RestControllerAdvice` | Web | `GlobalExceptionHandler` |
| `IT` | Test de integración | — | Test | `ExpedienteControllerIT` |
| `Test` | Test unitario | — | Test | `ExpedienteServiceImplTest` |

---

## Anexo D: Auditoría JPA — campos obligatorios LIN-BD-ORA-001

**LIN-BD-ORA-001 sección 5** exige seis columnas de auditoría en toda tabla permanente:

| Columna Oracle | Tipo | Nulabilidad | Contenido |
|---|---|---|---|
| `ID_USUA_CREA` | `VARCHAR2(30)` | NOT NULL | Usuario que creó el registro |
| `FE_USUA_CREA` | `TIMESTAMP` | NOT NULL | Fecha y hora de creación |
| `DE_TERM_CREA` | `VARCHAR2(39)` | NOT NULL | IP del cliente en la creación |
| `ID_USUA_MODI` | `VARCHAR2(30)` | NULL | Usuario de la última modificación |
| `FE_USUA_MODI` | `TIMESTAMP` | NULL | Fecha y hora de la última modificación |
| `DE_TERM_MODI` | `VARCHAR2(39)` | NULL | IP del cliente en la modificación |

La implementación recomendada en Spring Boot es una clase base anotada con `@MappedSuperclass` que usa `@PrePersist` y `@PreUpdate` para poblar los campos de forma transparente. El usuario se lee del MDC (puesto allí por `SaaTokenValidationFilter`) y la IP se obtiene del contexto HTTP actual vía `RequestContextHolder`.

### D.1 Clase base de auditoría

Crear una sola vez por proyecto en `src/main/java/<paquete-base>/persistence/entity/AuditoriaBase.java`. Puedes [descargar la plantilla AuditoriaBase.java](file:///home/carlos/Documentos/Telemetria-traza-swagger/Lineamientos_Nuevos_Borradores/desarrollo/plantillas/AuditoriaBase.java) directamente.

```java
package pe.gob.onp.pensiones.expedientes.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

@MappedSuperclass
public abstract class AuditoriaBase {

    @Column(name = "ID_USUA_CREA", length = 30, nullable = false, updatable = false)
    private String idUsuaCrea;

    @Column(name = "FE_USUA_CREA", nullable = false, updatable = false)
    private Instant feUsuaCrea;

    @Column(name = "DE_TERM_CREA", length = 39, nullable = false, updatable = false)
    private String deTermCrea;

    @Column(name = "ID_USUA_MODI", length = 30)
    private String idUsuaModi;

    @Column(name = "FE_USUA_MODI")
    private Instant feUsuaModi;

    @Column(name = "DE_TERM_MODI", length = 39)
    private String deTermModi;

    @PrePersist
    protected void onCrear() {
        String usuario  = resolverUsuario();
        String terminal = resolverTerminal();
        this.idUsuaCrea = usuario;
        this.feUsuaCrea = Instant.now();
        this.deTermCrea = terminal;
        // Los campos _MODI quedan NULL en la creación, conforme a LIN-BD-ORA-001 sección 5.3
        this.idUsuaModi = null;
        this.feUsuaModi = null;
        this.deTermModi = null;
    }

    @PreUpdate
    protected void onModificar() {
        // Los campos _CREA nunca se modifican (updatable = false en columna).
        this.idUsuaModi = resolverUsuario();
        this.feUsuaModi = Instant.now();
        this.deTermModi = resolverTerminal();
    }

    // Lee el usuario del MDC, donde SaaTokenValidationFilter (@Order 2) lo puso.
    // En jobs o batch sin contexto HTTP retorna "sistema".
    private String resolverUsuario() {
        String userId = MDC.get("user.id");
        return (userId != null && !userId.isBlank()) ? userId : "sistema";
    }

    // Obtiene la IP real del cliente respetando proxies y el gateway WSO2.
    // Trunca a 39 caracteres (máximo de DE_TERM_CREA / DE_TERM_MODI en Oracle).
    private String resolverTerminal() {
        try {
            ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String ip = attrs.getRequest().getHeader("X-Forwarded-For");
                if (ip == null || ip.isBlank()) {
                    ip = attrs.getRequest().getRemoteAddr();
                } else {
                    // X-Forwarded-For puede tener múltiples IPs separadas por coma;
                    // la primera es la del cliente original.
                    ip = ip.split(",")[0].trim();
                }
                return ip.substring(0, Math.min(ip.length(), 39));
            }
        } catch (Exception ignored) {}
        return "N/A";
    }
}
```

### D.2 Uso en entidades JPA

Toda entidad de tabla permanente debe extender `AuditoriaBase`. No se necesita ningún código adicional — los callbacks `@PrePersist` y `@PreUpdate` se disparan automáticamente por JPA:

```java
import jakarta.persistence.*;

@Entity
@Table(name = "PE_NOTIFICACION", schema = "NOTIFICACIONES")
public class NotificacionEntity extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "SEQ_PE_NOTIFICACION")
    @SequenceGenerator(name = "SEQ_PE_NOTIFICACION",
                       sequenceName = "NOTIFICACIONES.SEQ_PE_NOTIFICACION",
                       allocationSize = 1)
    @Column(name = "ID_NOTIFICACION")
    private Long id;

    @Column(name = "DE_NOTIFICACION", length = 500, nullable = false)
    private String descripcion;

    // ... resto de columnas del negocio
    // Los 6 campos de auditoría los hereda de AuditoriaBase — no declarar aquí.
}
```

### D.3 Restricciones y casos límite

| Escenario | Comportamiento |
|---|---|
| Petición HTTP autenticada con SAA | `id_usua_crea` = usuario del MDC; `de_term_crea` = IP real del cliente |
| Job batch o scheduled task (sin HTTP) | `id_usua_crea` = `"sistema"`; `de_term_crea` = `"N/A"` |
| `X-Forwarded-For` presente (gateway WSO2 / proxy) | Se usa la primera IP de la lista |
| Tabla GTT o staging batch | No aplica `AuditoriaBase` — ver LIN-BD-ORA-001 sección 5.2 |
| Campo `updatable = false` en `_CREA` | JPA garantiza que no se sobreescriben en UPDATE |

---

*Estándar de Desarrollo Java — ONP v0.1.2*
*OTI — Oficina de Tecnologías de la Información*
