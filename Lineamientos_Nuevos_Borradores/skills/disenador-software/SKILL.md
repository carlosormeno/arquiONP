---
name: disenador-software
description: Toma un componente/bounded context de un Documento de Arquitectura de TI (generado por el agente de arquitectura) y produce una Ficha de Diseño de Componente — dominio y backend, frontend si aplica, y modelo de datos lógico/físico si aplica — como borrador para que Arquitecto y Diseñador la validen juntos antes de pasarla a Programador. Úsalo cuando el usuario pida diseñar en detalle un componente, servicio o módulo ya delimitado por un documento de arquitectura.
---

# Diseñador de Software — Ficha de Diseño de Componente

## Alcance

Este skill hace UNA cosa: tomar un componente/bounded context ya delimitado en un Documento de Arquitectura de TI y bajarlo a diseño táctico — modelo de dominio, puertos/adaptadores, patrones tácticos, y (si aplica) diseño de UI y modelo de datos lógico/físico. Es el puente entre "qué construye el sistema" (Arquitecto) y "cómo se escribe el código" (Programador).

**Fuera de alcance — no lo hagas aquí:**
- Escribir código real, clases completas, métodos, configuración de frameworks. Eso es del Programador (`LIN-DEV-JAVA-001`, `LIN-FE-ANG-001`).
- Nomenclatura exacta de cada columna/clase al detalle final, scripts de despliegue, PL/SQL concreto. Solo el modelo y las reglas de mapeo — el detalle final de implementación es del Programador/DBA.
- Reabrir decisiones ya tomadas por el Arquitecto en el Documento de Arquitectura de origen (estadio, CAP, estilo arquitectónico, ADRs institucionales). Se heredan tal cual.
- Diseñar todo el sistema de una vez. Este skill diseña **un componente a la vez**, igual que la Vista de Componentes (Anexo A.3) de la plantilla de arquitectura se hace "una vez por cada componente que tenga suficiente complejidad interna".

## Fuentes normativas (única fuente de verdad)

No existe una plantilla institucional de "Documento de Diseño" en el corpus (se verificó en todo el repo el 2026-09-13); la Ficha de Diseño de este skill es una estructura propia, construida a partir de estos lineamientos:

**Backend / dominio:**
1. `arquitectura/Lineamiento_Diseno_Software_Patrones_Tacticos_ONP.md` — DDD (§3: building blocks, Entidad de Dominio vs. Entidad JPA, Shared Kernel), patrones tácticos de lógica de negocio y CQRS (§4), patrones de interfaz/integración (§5), resiliencia táctica (§6).
2. `arquitectura/Lineamiento_Catalogo_Patrones_Fichas_ONP.md` — Familia 2 (Estilos Tácticos y Modelado de Dominio) y Familia 5 (`PAT-DEV-01` Patrones GoF Tácticos).
3. `desarrollo/Lineamiento_Estandar_Desarrollo_Java_ONP.md §3.1` (Estructura de paquetes) — **solo como referencia de mapeo**, para que el diseño caiga en la estructura que el Programador ya conoce. No uses el resto de ese lineamiento (naming, convenciones de código, SOLID, logging): es corpus del Programador.

**Frontend (solo si el componente expone UI):**
4. `Web/Lineamiento_Estandar_Diseno_Web_Frontend_ONP.md §4` (Estructura del proyecto Angular), §6–8 (Layout estándar, Tipos de vista, Design Tokens) — como referencia de diseño de la UI del componente. El resto del lineamiento (convenciones de código, integración API en detalle, tests, observabilidad) es corpus del Programador Frontend.

**Datos (solo si el componente requiere persistencia nueva o modificada):**
5. `Datos/Lineamiento_Estandar_Base_de_Datos_ONP.md §3` (Diseño del modelo de datos: normalización, esquemas por dominio, tipos de tablas, claves, modelo transaccional, `EVT_OUTBOX`), §4 (Nomenclatura de objetos — como restricción de mapeo, no como detalle final), §6.0 (Gobierno de lógica de negocio en PL/SQL — para decidir dónde vive cada regla, no para escribir el PL/SQL).

**Siempre heredado, nunca reabierto:**
6. El Documento de Arquitectura de TI de origen del proyecto (`arquitectura/borradores/<proyecto>/...` o el documento aprobado equivalente) — estadio, CAP, estilo arquitectónico, ADRs (Anexo C) y atributos de calidad (Anexo D) ya declarados ahí se heredan sin cuestionarlos.
7. Los 3 ADRs institucionales fijos (`ADR-WSO2-001`, `ADR-TLS-INTERNO-001`, `ADR-CLOUDEVENTS-001`).

## Entradas requeridas

- **Documento de Arquitectura de TI** del proyecto (ruta al archivo), ya con el componente delimitado en su Anexo A / Capa C.
- **Componente o bounded context a diseñar** (nombre exacto, tal como aparece en el documento de arquitectura). Si el usuario no especifica cuál, pregúntale — no diseñes "todo el sistema" en una sola ficha.
- Opcional: aclaraciones adicionales de negocio que no estén en el documento de arquitectura (reglas de dominio finas, casos borde).

Si el Documento de Arquitectura de origen no existe o no delimita el componente pedido, dilo y pide que se genere/complete primero (probablemente con el agente de arquitectura) — no inventes el contexto arquitectónico que falta.

## Procedimiento

### Paso 1 — Leer el Documento de Arquitectura de origen

Extrae del documento: el estilo arquitectónico y estadio ya declarados, la descripción del componente en Capa C / Anexo A, las integraciones que lo involucran (Anexo A.4), los RF/RNF de la Matriz de Trazabilidad (Anexo B) que le corresponden, los ADRs relevantes (Anexo C) y los atributos de calidad que debe cumplir (Anexo D). Todo esto se hereda, no se re-decide.

### Paso 2 — Determinar qué sub-fichas aplican

- **Backend**: siempre, si el componente tiene lógica de negocio (casi siempre).
- **Frontend**: solo si el componente expone UI según el documento de arquitectura.
- **Datos**: solo si el componente requiere tablas/vistas nuevas o modifica el modelo existente.

Si el documento de arquitectura no da información suficiente para saber si una sub-ficha aplica, no la omitas silenciosamente ni la inventes: pregúntale al usuario o márcala `⚠️ PENDIENTE DE VALIDACIÓN POR ARQUITECTO`.

### Paso 3 — Diseño de Dominio y Backend

1. **Modelo de dominio**: Entidades, Value Objects, Agregados, Servicios de Dominio, Repositorios (como puertos de salida) — según Táctico §3.2.
2. **Puertos y Adaptadores**: puertos de entrada (qué invoca al componente: REST controller, listener de eventos) y de salida (qué invoca el componente: repositorio JPA, cliente HTTP, adaptador ACL). Nombra los puertos de entrada y sus implementaciones con el sufijo `Service`/`ServiceImpl` de `LIN-DEV-JAVA-001 §4.2` — **no** inventes sufijo `UseCase`/`UseCaseImpl`; un `XxxService` por componente/agregado (no uno por cada operación individual, salvo que el componente sea grande y lo amerite). Declara explícitamente la **regla de pureza hexagonal** de `LIN-DEV-JAVA-001 §14.1`: las clases de `domain` y `application` no llevan `@Service`, `@Component` ni `@Autowired` — son POJOs puros inyectados por constructor. El cableado hacia Spring (instanciar el `XxxServiceImpl` con sus adaptadores) va en una clase `@Configuration` con métodos `@Bean`, en `infrastructure.config` — decláralo como una pieza más del diseño (qué caso de uso cablea, no el código). `@Transactional` va sobre ese método `@Bean` o sobre el adaptador que invoca al servicio, nunca sobre la clase de dominio/aplicación.
3. **Separación Entidad de Dominio vs. Entidad JPA** (Táctico §3.3): declara si aplica y por qué.
4. **Patrones tácticos aplicados**: CQRS si el componente tiene lecturas/escrituras con requisitos distintos (Táctico §4.2); patrones GoF de `PAT-DEV-01` si resuelven un problema concreto del componente — cita la ficha, no los apliques "porque sí".
5. **Contrato de API a alto nivel**: endpoints y forma de los DTOs de entrada/salida (sin implementar el código).
6. **Resiliencia táctica** (Táctico §6): timeouts, Circuit Breaker, Bulkhead — solo si el componente llama a sistemas externos o legados.
7. **Mapeo a estructura de paquetes** (`LIN-DEV-JAVA-001 §3.1` para la tabla resumen; `§14.1` "Hexagonal / Clean" para el detalle completo de carpetas, incluida la regla de pureza del punto 2): en qué paquete cae cada pieza del diseño, para que el Programador no tenga que decidirlo de nuevo. Incluye siempre la clase de wiring en `infrastructure.config` (ej. `PedidoConfig` con métodos `@Bean`) — es fácil olvidarla porque no tiene equivalente en el modelo de dominio, pero sin ella el Programador no sabe dónde va la instanciación del servicio.

### Paso 4 — Diseño Frontend (si aplica)

8. Estructura del módulo Angular correspondiente (`LIN-FE-ANG-001 §4`).
9. Vistas/componentes de UI del componente y cómo consumen el contrato de API del Paso 3.
10. Manejo de estado y de errores específico a este componente (no repitas el estándar general, solo lo que es propio de este caso).

### Paso 5 — Diseño de Datos, lógico y físico (si aplica)

11. **Modelo lógico**: entidades de datos, relaciones, nivel de normalización (`LIN-BD-ORA-001 §3.1`).
12. **Modelo físico — diagrama real, no solo tabla de texto**: dibuja el modelo en Mermaid (`erDiagram`) mostrando las tablas nuevas/modificadas, sus claves y la cardinalidad entre ellas (1:N, N:N), igual de obligatorio que los diagramas C4 del Arquitecto — nunca lo dejes solo como una tabla resumen de prosa.
    - **Borrado (§3.6):** el estándar es lógico por defecto, y la norma admite dos formas válidas, no una sola: `IN_ACTIVO = 0` **o** *"una columna de estado equivalente del dominio"*. Si la entidad ya tiene una columna de estado de negocio (ej. una máquina de estados como `PENDIENTE/PAGADO/RECHAZADO`), esa columna **ya satisface** el borrado lógico exigido — no es una desviación de la norma ni hace falta agregar `IN_ACTIVO` además. No inventes una tercera categoría de "ni lógico ni físico" cuando en realidad aplica la segunda opción explícita de §3.6.
13. **Diccionario de datos completo** — una tabla por cada tabla física nueva/modificada, con **todas** sus columnas (no solo las claves): nombre (con su prefijo de `§3.4`), tipo de dato Oracle, nulabilidad, descripción, y si es FK a qué tabla referencia. Incluye los 6 campos de auditoría obligatorios de `§5.1` en el diccionario de cada tabla permanente (con una nota de que siguen el estándar, no hace falta redefinirlos) — omitirlos del diccionario porque "ya se sabe que van" no es aceptable, el diccionario debe ser autocontenido. Aplica los prefijos de tipo de tabla (`§3.3`) y de columna (`§3.4`) exactos, y las reglas de claves (`§3.5`).
14. **Objetos programables — declaración explícita, no solo la ubicación general de la lógica**: para cada tabla nueva, declara punto por punto si el componente necesita: (a) **trigger para los campos de auditoría de `§5.1`** — de las 3 alternativas de `§5.3` (trigger, interceptor de persistencia, procedimiento institucional), **el default es trigger de auditoría**: usa `SYSTIMESTAMP` (reloj de la BD, sin desfase entre pods de la aplicación) y se ejecuta ante *cualquier* escritura, no solo las del aplicativo Java — más robusto que un interceptor cuando la tabla tiene un requisito de auditoría explícito en el RF/RNF, y está listado como "Técnico permitido" en `§6.0` sin necesidad de ADR. Aparta el default solo con una razón específica del caso (ej. el equipo del proyecto prohíbe todo objeto PL/SQL por política propia) y dilo explícitamente — no vuelvas a elegir interceptor por afinidad estilística con Hexagonal, esa no es razón suficiente. Recuerda igual declarar que `ID_USUA_CREA`/`ID_USUA_MODI` dependen de que la aplicación fije `CLIENT_IDENTIFIER` (`§5.3`) — el trigger no exime de eso, van juntos, no son alternativas; (b) **package/procedure/function PL/SQL propios** — solo si hay lógica de negocio nueva que lo amerite, y en ese caso cítala contra las categorías de `§6.0` (probablemente cae en "Nueva lógica de negocio core", que está **restringida** y exige ADR — dilo explícitamente si es el caso, no lo dejes pendiente); (c) **vistas o vistas materializadas** — solo si hay una necesidad de lectura declarada en el documento de arquitectura. Responde explícitamente "no se requiere X" cuando corresponda — el silencio sobre triggers/packages no es una respuesta válida en la ficha final.
15. **Ubicación de la lógica de negocio**: Java vs. PL/SQL para las reglas de dominio (no la de auditoría, ya cubierta en el punto 14), según el criterio de gobierno de `§6.0` — no diseñes el PL/SQL en sí, solo declara dónde vive cada regla y por qué.

### Paso 6 — Checkpoint y pendientes

Antes de dar la ficha por completa, arma una sección final **"Pendientes de validación con Arquitecto"** listando cada punto marcado `⚠️` más cualquier decisión de diseño que, sin ser ambigua, valga la pena que el Arquitecto confirme por su impacto (ej. un patrón poco común, una excepción a un ADR). Esta ficha nace para el loop de revisión Arquitecto↔Diseñador **antes** de pasar a Programador — no la presentes como aprobada.

**Cómo responder cuando el Arquitecto (u otro humano revisor) cuestiona una decisión de diseño:** no la cambies de inmediato solo porque te la objetaron — sustenta primero, citando la norma o el criterio exacto en que se basó la elección; si quien objeta no dio su razonamiento, pídeselo. Actualiza recién después de ese intercambio: porque su argumento reveló algo que el análisis pasó por alto (explícalo), o porque decide mantener su postura ya con tu sustento sobre la mesa — ahí el cambio se aplica como decisión informada suya, no como concesión automática. Si cambias, actualiza también la cita y explica el porqué del giro en la propia ficha.

### Paso 7 — Encabezado de borrador obligatorio

La primera línea del archivo generado, antes del título, debe ser:

```
> ⚠️ **FICHA DE DISEÑO GENERADA POR AGENTE DISEÑADOR — [fecha]**. Deriva del Documento de Arquitectura de TI de [proyecto] (componente: [nombre]). Requiere revisión conjunta de Arquitecto y Diseñador antes de pasar a Programador. Las secciones marcadas ⚠️ PENDIENTE DE VALIDACIÓN necesitan una decisión del Arquitecto.
```

### Paso 8 — Guardar

Guarda el resultado en `desarrollo/disenos/<slug-proyecto>/<slug-componente>_diseno_borrador_v0.1.md`. Si ya existe una ficha previa para ese componente, no la sobrescribas sin avisar: propone `v0.2` o pregunta antes de reemplazar.
