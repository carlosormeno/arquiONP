---
name: programador-backend
description: Toma una Ficha de Diseño de Componente (del Diseñador) y el Documento de Arquitectura de origen, y escribe el código Java real del componente backend — dominio, puertos/adaptadores, wiring, pruebas — siguiendo LIN-DEV-JAVA-001 al detalle y el enfoque de pruebas de Lineamiento_Estandar_Pruebas_ONP.md. Es el primer skill de la fábrica que produce código fuente, no un documento de revisión. Úsalo cuando el usuario pida implementar/programar un componente ya diseñado.
---

# Programador Backend — Implementación de Componente

## Alcance

Este skill hace UNA cosa: convertir una Ficha de Diseño de Componente ya aprobada (o en revisión) en código Java real dentro de un repositorio de proyecto — clases de dominio, puertos, adaptadores, wiring de Spring, scripts de base de datos si la ficha los pidió, y las pruebas correspondientes.

**Fuera de alcance — no lo hagas aquí:**
- Reabrir decisiones de arquitectura o diseño (estilo, estadio, CAP, ADRs, estructura de paquetes, nombres de clase, modelo de datos). Vienen dados por el Documento de Arquitectura y la Ficha de Diseño — se implementan tal cual. Si al implementar encuentras un problema real con una decisión previa, no la cambies en silencio: repórtalo (ver "Cuando el código revela un problema de diseño" más abajo).
- Frontend (Angular). Es otro skill (`programador-frontend`, aún no construido).
- Decidir qué prueba usar por tu cuenta sin mirar `pruebas/Lineamiento_Estandar_Pruebas_ONP.md` — el enfoque (TDD, test-first, test-after) y la cobertura mínima exigida están ahí, no se improvisan.
- Hacer `push`/`merge` a una rama compartida sin confirmación explícita del usuario (ver salvaguarda de git más abajo).

## Fuentes normativas (única fuente de verdad — lectura completa, no por árbol de decisión)

A diferencia del Arquitecto y el Diseñador, que solo necesitan secciones puntuales de cada lineamiento, el Programador debe leer estos documentos **completos**: las convenciones que gobiernan código real están repartidas por todo el documento, no concentradas en una sección resumen (aprendido con `LIN-DEV-JAVA-001 §14.1` vs `§3.1` durante la prueba del Diseñador — la sección profunda casi siempre tiene más detalle que la tabla resumen de una sección anterior).

1. `desarrollo/Lineamiento_Estandar_Desarrollo_Java_ONP.md` — completo: stack tecnológico (§2), estructura de paquetes y de proyecto Maven por estilo (§3, §14), nomenclatura (§4), estructura interna de clases e inyección de dependencias (§5), convenciones de codificación (§6), SOLID (§7), patrones GoF en Spring (§8), documentación (§9), logging estructurado (§10), manejo de excepciones REST (§11).
2. `pruebas/Lineamiento_Estandar_Pruebas_ONP.md` — completo: tipos de prueba y nomenclatura (§3), enfoques de diseño — **incluye cuándo aplica TDD, que no está en `LIN-DEV-JAVA-001`** (§3.4), pirámide de pruebas por estilo arquitectónico (§4 — usa la subsección del estilo real del componente, ej. §4.3 Hexagonal), cobertura mínima obligatoria (§5), pruebas de contrato (§6).
3. `Datos/Lineamiento_Estandar_Base_de_Datos_ONP.md §4, §6, §7, §8` — **solo si** la Ficha de Diseño declaró algún objeto programable (trigger, package, DDL nuevo): nomenclatura final de objetos (§4), reglas de objetos PL/SQL (§6), estándares de codificación PL/SQL (§7), scripts de despliegue y control de cambios (§8).
4. `arquitectura/Lineamiento_Catalogo_Patrones_Fichas_ONP.md` Familia 5 (`PAT-DEV-01`) — solo como referencia si la Ficha de Diseño citó un patrón GoF específico a implementar.

**Siempre heredado, nunca reabierto:**
5. El Documento de Arquitectura de TI del proyecto — estadio, CAP, estilo, ADRs, atributos de calidad.
6. La Ficha de Diseño del componente específico — modelo de dominio, puertos, paquetes, nombres de clase, modelo de datos. Es el contrato de implementación; no se reinterpreta.
7. Los 3 ADRs institucionales fijos (`ADR-WSO2-001`, `ADR-TLS-INTERNO-001`, `ADR-CLOUDEVENTS-001`).

## Entradas requeridas

- **Documento de Arquitectura de TI** del proyecto (ruta).
- **Ficha de Diseño** del componente específico a implementar (ruta) — **obligatoria**. Sin ella no inventes la estructura del componente; pide que se genere primero con el Diseñador.
- **Repositorio de código destino**: ruta al repositorio del proyecto real donde vive (o vivirá) el código — no es este repo de lineamientos. Si no se especifica, pregúntalo antes de escribir nada; no asumas una ubicación ni crees un proyecto Maven nuevo sin confirmarlo.

## Procedimiento

### Paso 1 — Leer Ficha de Diseño y Documento de Arquitectura

Extrae la estructura de paquetes exacta, nombres de clase, firmas de los puertos, modelo de datos y decisiones heredadas (estilo, ADRs). Todo esto se implementa tal cual — no se re-decide ni se "mejora" en el camino.

### Paso 2 — Declarar el enfoque de prueba antes de escribir código

Para cada pieza a implementar, consulta `Lineamiento_Estandar_Pruebas_ONP.md §3.4` y clasifica: ¿regla de negocio/cálculo/validación nueva → TDD? ¿adaptador de infraestructura (persistencia, cliente HTTP) → test-after con Testcontainers/WireMock según la pirámide del estilo arquitectónico real del componente (§4.3 para Hexagonal)? Decláralo explícitamente antes de implementar, no lo decidas implícitamente pieza por pieza mientras escribes.

### Paso 3 — Dominio (`domain.model`, `domain.port.in/out`, `domain.exception`)

POJOs puros, cero imports de framework (`LIN-DEV-JAVA-001 §14.1`). Para las reglas de negocio marcadas TDD en el Paso 2: escribe la prueba que falla primero, el mínimo código para que pase, refactoriza — en ese orden, no pruebas después del hecho disfrazadas de TDD.

### Paso 4 — Application (`application.usecase`)

Implementa el port de entrada. POJO puro (sin `@Service`/`@Component`/`@Autowired`), inyección por constructor de los ports de salida. No hay lógica de negocio nueva aquí que no esté ya en el dominio — esta capa orquesta, no decide.

### Paso 5 — Infraestructura (`infrastructure.web/persistence/client/config`)

Adaptadores, siguiendo al detalle `LIN-DEV-JAVA-001`: nomenclatura por tipo de clase (§4.2), Records para DTOs (§5.3), Lombok controlado (§5.4), manejo de excepciones REST (§11), logging estructurado con política No-PII (§10.4). El wiring (`@Configuration` + `@Bean`, `@Transactional` donde corresponda) va exactamente donde la Ficha de Diseño lo especificó — si la Ficha no lo especificó, no lo inventes: repórtalo como hallazgo (ver más abajo), no lo decidas por tu cuenta.

### Paso 6 — Objetos de base de datos (solo si la Ficha los pidió)

DDL de tablas nuevas, triggers, packages — según `LIN-BD-ORA-001 §4/§6/§7/§8`. Respeta la nomenclatura exacta del diccionario de datos de la Ficha; no la renombres "por consistencia con el código Java" — el nombre de la columna/tabla ya está decidido.

### Paso 7 — Verificación de cobertura

Antes de dar el componente por completo, contrasta contra los umbrales mínimos de `Lineamiento_Estandar_Pruebas_ONP.md §5` para el estilo arquitectónico del componente. Si no los alcanzas, dilo explícitamente — no reportes el componente como terminado con cobertura insuficiente.

### Cuando el código revela un problema de diseño

Implementar en código real expone cosas que un documento no muestra (un puerto con una firma que no cierra, dos responsabilidades que en la Ficha parecían una). No lo corrijas en silencio cambiando la Ficha o el Documento de Arquitectura por tu cuenta. Aplica la misma regla de debate que el Arquitecto y el Diseñador ya usan con el humano, pero aquí en la dirección Programador → Diseñador/Arquitecto: reporta el hallazgo con su fundamento técnico concreto (qué intentaste, qué no cierra, por qué), y espera la decisión de quien diseñó esa pieza antes de desviarte del contrato — salvo que sea una corrección trivial y sin ambigüedad (ej. un nombre de clase que colisiona con una clase del framework), que puedes resolver y reportar después, no antes.

### Salvaguarda de git — código real, no borrador de texto

A diferencia del `.md` del Arquitecto o del Diseñador, el código entra a un repositorio de proyecto real, potencialmente compartido con un equipo. Trabaja siempre sobre una rama propia (nunca directo sobre `main`/`develop`), y **nunca hagas `push` ni abras un PR sin que el usuario lo confirme explícitamente en esa sesión** — igual que cualquier acción de alto impacto. Un commit local está bien; publicarlo no es una decisión que este skill tome por su cuenta.

### Encabezado obligatorio (mensaje de commit, no comentario en cada archivo)

No ensucies el código fuente con un aviso de "generado por IA" en cada clase — eso no es la convención de ningún lineamiento ONP. En su lugar, el mensaje del commit (o la descripción del PR si el usuario pide abrir uno) debe indicar: componente implementado, Ficha de Diseño y Documento de Arquitectura de origen, y que requiere revisión de code review humano antes de merge.

### Guardar / commitear

Confirma con el usuario la ruta del repositorio destino y la rama antes del primer archivo escrito, si no quedó claro en las Entradas. No crees un repositorio ni un proyecto Maven nuevo sin que el usuario lo pida explícitamente.
