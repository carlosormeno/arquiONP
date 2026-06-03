# Lineamiento Estándar de Pruebas ONP
**Código:** LIN-TEST-001
**Versión:** v0.1.0
**Estado:** Borrador
**Fecha:** 2026-05-26
**Propietario:** Arquitectura de Software — OTI
**Revisores:** Desarrollo, QA, Seguridad de la Información

---

## Control de cambios

| Versión | Fecha | Autor | Descripción |
|---|---|---|---|
| v0.1.0 | 2026-05-26 | Arquitectura OTI | Borrador inicial |

---

## Tabla de contenido

**Parte normativa común**
1. [Objetivo y alcance](#1-objetivo-y-alcance)
2. [Normativa y documentos relacionados](#2-normativa-y-documentos-relacionados)
3. [Tipos de prueba](#3-tipos-de-prueba)
   - [3.3 Resumen de herramientas por capa](#33-resumen-de-herramientas-por-capa)
   - [3.4 Enfoques de diseño de pruebas](#34-enfoques-de-diseño-de-pruebas)
4. [Pirámide de pruebas por estilo arquitectónico](#4-pirámide-de-pruebas-por-estilo-arquitectónico)
5. [Cobertura mínima obligatoria](#5-cobertura-mínima-obligatoria)
6. [Pruebas de contrato](#6-pruebas-de-contrato)
7. [Relación con CI/CD](#7-relación-con-cicd)
8. [Evidencias obligatorias](#8-evidencias-obligatorias)
   - [8.5 Pruebas de seguridad (gestionadas por UFSD)](#85-pruebas-de-seguridad-gestionadas-por-ufsd)
9. [Criterios mínimos de aceptación para paso a QA y Producción](#9-criterios-mínimos-de-aceptación-para-paso-a-qa-y-producción)
10. [Responsabilidades](#10-responsabilidades)

**Capítulos por tecnología**

11. [Capítulo: Backend Java](#11-capítulo-backend-java)
12. [Capítulo: Frontend Angular](#12-capítulo-frontend-angular)
13. [Capítulo: PL/SQL Legacy](#13-capítulo-plsql-legacy)

**Cierre**

14. [Checklist de pruebas](#14-checklist-de-pruebas)
15. [Anti-patrones](#15-anti-patrones)
16. [Proceso ADR para desviaciones](#16-proceso-adr-para-desviaciones)
17. [Glosario](#17-glosario)

**Anexos**

- [Anexo A — Ejemplo completo Backend Java](#anexo-a--ejemplo-completo-backend-java)
- [Anexo B — Ejemplo completo Frontend Angular](#anexo-b--ejemplo-completo-frontend-angular)
- [Anexo C — Ejemplo completo PL/SQL Legacy](#anexo-c--ejemplo-completo-plsql-legacy)

---

## Parte normativa común

---

## 1. Objetivo y alcance

### 1.1 Objetivo

Este lineamiento define la estrategia de pruebas institucional de la ONP: qué tipos de prueba son obligatorios, qué cobertura mínima aplica por estilo arquitectónico, qué evidencias deben generarse y qué criterios determinan si un artefacto es apto para pasar a QA o Producción.

Los lineamientos Java (LIN-DEV-JAVA-001), Frontend (LIN-FE-ANG-001) y Base de Datos (LIN-BD-ORA-001) aplican esta estrategia dentro de su propio dominio. Este lineamiento es la fuente autoritativa de la técnica.

### 1.2 Principio rector

> Las pruebas no verifican que el código está bien diseñado. Verifican que el comportamiento observable es el esperado y que no cambia sin saberlo.

Este principio aplica especialmente a sistemas legacy: el objetivo de una prueba no es validar la calidad del código existente, sino capturar su comportamiento actual para poder modificarlo con seguridad.

### 1.3 Alcance

| Componente | Cubierto por |
|---|---|
| Backend Java / Spring Boot | [sección 11](#11-capitulo-backend-java) |
| Frontend Angular | [sección 12](#12-capitulo-frontend-angular) |
| PL/SQL procedures, packages, functions legacy | [sección 13](#13-capitulo-plsql-legacy) |
| APIs REST (contrato) | [sección 6](#6-pruebas-de-contrato) y [sección 11.4](#114-pruebas-de-contrato-java) |
| Integración Java → Oracle | [sección 11.3](#113-pruebas-de-integracion) y [sección 13](#13-capitulo-plsql-legacy) |

### 1.4 Fuera de alcance

| Tema | Responsable |
|---|---|
| Ejecución automática en pipeline (cuándo y cómo) | LIN-CICD-001 |
| Pruebas de penetración (Ethical Hacking), DAST y retest de vulnerabilidades | UFSD / Seguridad Digital — este lineamiento no define la técnica; sí reconoce el resultado como evidencia complementaria y criterio de aceptación cuando aplique (ver [sección 8.5](#85-pruebas-de-seguridad-gestionadas-por-ufsd) y [sección 9.2](#92-criterios-de-paso-a-produccion-merge-a-rama-master)) |
| Pruebas de carga y rendimiento | `LIN-PERF-001` — dueño de herramienta (JMeter preferente, k6 y Gatling como alternativas), tipos de prueba, escenarios, umbrales y criterios de aceptación de performance |
| Pruebas de usabilidad formal | Diseño UX |

---

## 2. Normativa y documentos relacionados

| Documento | Código | Relación |
|---|---|---|
| Marco Rector de Arquitectura de Software | LIN-ARQ-000 | Define pirámide por estilo; este documento la desarrolla |
| Estándar de Desarrollo Java | LIN-DEV-JAVA-001 | Aplica este lineamiento en contexto Java; no redefine |
| Estándar de Base de Datos Oracle | LIN-BD-ORA-001 | Aplica pruebas de caracterización para PL/SQL; no redefine |
| Estándar de Diseño Web Frontend Angular | LIN-FE-ANG-001 | Aplica este lineamiento en contexto Angular; no redefine |
| Lineamiento de Seguridad en Aplicaciones | LIN-SEC-APP-001 | Pruebas de seguridad complementarias a las funcionales |
| Estándar de CI/CD | LIN-CICD-001 | Dueño de gates automáticos y publicación de reportes |

---

## 3. Tipos de prueba

### 3.1 Clasificación institucional ONP

| Tipo | Abreviatura | Qué verifica | Dependencias externas |
|---|---|---|---|
| **Unitaria** | UT | Lógica de una unidad aislada (clase, función, pipe) | Ninguna — todo mockeado |
| **Integración** | IT | Colaboración entre componentes reales (BD, HTTP, filesystem) | Base de datos, servidor embebido |
| **Caracterización** | CT | Comportamiento actual observable de código legacy | BD real (OracleContainer o QA) |
| **Contrato** | CONT | Acuerdo entre proveedor y consumidor de una API | Depende del enfoque (ver [sección 6](#6-pruebas-de-contrato)) |
| **E2E** (Extremo a Extremo) | E2E | Flujo de negocio completo desde UI hasta BD | Stack completo levantado |
| **Accesibilidad** | ACC | Cumplimiento básico de estándares WCAG (frontend) | Navegador |

### 3.2 Convenciones de nomenclatura

| Tipo | Sufijo Java | Sufijo Angular | Tag JUnit |
|---|---|---|---|
| Unitaria | `NombreClaseTest.java` | `nombre.component.spec.ts` | `@Tag("unit")` |
| Integración | `NombreClaseIT.java` | `nombre.integration.spec.ts` | `@Tag("integration")` |
| Caracterización | `NombreClaseCT.java` | — | `@Tag("characterization")` |
| E2E (Playwright) | — | `nombre.e2e.spec.ts` | — |
| E2E (Cypress — si ya existe) | — | `nombre.e2e.cy.ts` | — |

Maven Surefire ejecuta `*Test.java`. Maven Failsafe ejecuta `*IT.java` y `*CT.java`.

### 3.3 Resumen de herramientas por capa

| Capa | Tipo de prueba | Herramientas aprobadas |
|---|---|---|
| **Backend Java** | Unitaria | JUnit 5, Mockito, AssertJ |
| **Backend Java** | Integración (BD, HTTP) | Testcontainers, OracleContainer, MockMvc, RestAssured |
| **Backend Java** | Contrato API | OpenAPI validator (RestAssured), Pact, Spring Cloud Contract |
| **Backend Java** | Cobertura | JaCoCo Maven Plugin |
| **Frontend Angular** | Unitaria de componentes y servicios | Jest (recomendado), Angular TestBed, HttpClientTestingModule |
| **Frontend Angular** | E2E — flujos de usuario en navegador | **Playwright** (preferente); Cypress solo si ya existe en el proyecto |
| **Frontend Angular** | Accesibilidad | axe-core / playwright-axe / cypress-axe |
| **PL/SQL Legacy** | Caracterización | JUnit 5 + Testcontainers/OracleContainer + JdbcTemplate (desde Java) |
| **APIs REST** | Contrato | OpenAPI validator (mínimo siempre), Pact o Spring Cloud Contract (si aplica [sección 6.2](#62-obligatoriedad)) |

> **Regla de herramientas E2E:** Las pruebas E2E con Playwright o Cypress son para flujos de usuario en navegador. No se usan para probar APIs backend — esa responsabilidad pertenece a las pruebas de integración (MockMvc, RestAssured) y las pruebas de contrato.

### 3.4 Enfoques de diseño de pruebas

Este lineamiento diferencia entre **tipos de prueba** y **enfoques de diseño de pruebas**.

- Los **tipos** definen qué evidencia técnica debe existir: unitarias, integración, contrato, E2E, caracterización. Son verificables y obligatorios según los criterios de [sección 9](#9-criterios-minimos-de-aceptacion-para-paso-a-qa-y-produccion).
- Los **enfoques** definen cómo el equipo incorpora esas pruebas al proceso de desarrollo. ONP no impone un único enfoque, pero establece recomendaciones por situación.

> **Lo que se audita es el tipo y la cobertura, no el enfoque.** Un equipo puede aplicar TDD o escribir las pruebas al final — lo que se exige es que las pruebas existan, pasen y alcancen los umbrales de [sección 5](#5-si-alguna-falla-y-el-cambio-era-intencional-actualizar-la-ct-y-documentar-en-pr).

#### Enfoques reconocidos

| Enfoque | Descripción | Cuándo aplica en ONP |
|---|---|---|
| **TDD** (Test-Driven Development) | Escribir la prueba antes del código. Ciclo: prueba falla → mínimo código para que pase → refactorizar | Reglas de negocio nuevas, cálculos, validaciones, casos de uso |
| **BDD** (Behavior-Driven Development) | Describir el comportamiento esperado en lenguaje de negocio antes de implementar. **No exige Gherkin ni Cucumber** — los escenarios pueden expresarse en lenguaje natural estructurado (`describe`/`it` en español, o un documento de criterios de aceptación) | Flujos E2E con criterios de aceptación definidos por el área funcional |
| **Contract-first** | Definir el contrato OpenAPI antes de implementar el controller | APIs REST publicadas en WSO2 o con múltiples consumidores |
| **Test-after** | Implementar primero, escribir pruebas después | Aceptado; menos recomendado para lógica de dominio crítica |
| **Caracterización** | Documentar el comportamiento observable actual antes de modificar | Código legacy — PL/SQL, módulos sin documentación funcional |
| **Regresión** | Escribir una prueba que reproduce el bug antes de corregirlo | Corrección de bugs en producción — obliga a que el fix no se revierta |

#### Recomendación por situación

| Situación | Tipo mínimo de prueba | Enfoque recomendado |
|---|---|---|
| Regla de negocio o cálculo nuevo | Unitaria | TDD |
| Caso de uso con lógica significativa | Unitaria + integración | TDD o test-first |
| API REST publicada para múltiples consumidores | Contrato | Contract-first (openapi.yml antes del controller) |
| Flujo de usuario crítico en navegador | E2E | BDD — describir el escenario en lenguaje funcional antes de automatizar |
| Bug reproducido en producción | Unitaria o integración | Regresión — prueba que falla primero, luego el fix |
| Procedure PL/SQL con lógica no documentada | Caracterización | Caracterización — capturar comportamiento actual antes de tocar |
| Refactorización de módulo existente | Unitaria + integración | Pruebas en verde antes de refactorizar; CT si hay legacy |

---

## 4. Pirámide de pruebas por estilo arquitectónico

La pirámide define la distribución relativa de esfuerzo de prueba según el estilo del sistema. No es una regla de porcentajes absolutos, sino una guía de priorización.

### 4.1 Monolito Simple

```
        ▲ E2E (recomendado — happy path por flujo)
       ███
      █████  Integración (obligatorio — 1 por endpoint REST mínimo)
     ███████
    █████████ Unitaria (obligatorio — capas service y domain)
```

| Capa | Tipo de prueba prioritario | Cobertura mínima |
|---|---|---|
| Controller | Integración (MockMvc) | Happy path + escenarios de seguridad y error aplicables (401, 403, 400, 404 según el endpoint) |
| Service | Unitaria | ≥75% instrucción |
| Repository | Integración (Testcontainers) | 1 test por método personalizado |
| Entity/Model | Unitaria (validaciones) | ≥80% instrucción |

### 4.2 Monolito Modular (Maven multi-módulo)

```
        ▲ E2E (recomendado)
       ███
      █████  Integración entre módulos (obligatorio)
     ███████
    █████████ Unitaria por módulo (obligatorio)
```

| Módulo | Tipo prioritario | Cobertura mínima |
|---|---|---|
| `domain` | Unitaria | ≥85% instrucción |
| `application` | Unitaria | ≥80% instrucción |
| `infrastructure` | Integración | 1 test por adaptador externo |
| `api` | Integración (MockMvc) | Happy path + escenarios de seguridad y error aplicables |

### 4.3 Hexagonal / Ports & Adapters

```
        ▲ E2E (obligatorio — happy path por caso de uso)
       ████
      ██████  Integración por puerto de salida (obligatorio)
     ████████
    ██████████ Unitaria — casos de uso (obligatorio)
```

| Componente | Tipo prioritario | Cobertura mínima |
|---|---|---|
| Entidades de dominio | Unitaria | ≥85% instrucción |
| Casos de uso (Use Cases) | Unitaria | ≥85% instrucción |
| Puertos de entrada (interfaces) | Integración | 1 test por contrato de puerto |
| Adaptadores de salida (BD, HTTP, MQ) | Integración | 1 test por adaptador |

---

## 5. Cobertura mínima obligatoria

La cobertura se mide con JaCoCo (backend Java). Para Angular, la cobertura de statements se mide con el runner configurado (Jest/Karma).

### 5.1 Umbrales por estilo (backend Java)

| Estilo | Mínimo global | Capas de lógica de negocio |
|---|---|---|
| Monolito Simple | ≥65% instrucción | Service: ≥75%; Domain/Model: ≥80% |
| Monolito Modular | ≥70% instrucción | domain: ≥85%; application: ≥80% |
| Hexagonal | ≥70% instrucción | Casos de uso: ≥85%; Dominio: ≥85% |

> **Qué NO medir con umbrales duros:** Controllers (mejor cubiertos con integración), adaptadores de infraestructura, clases de configuración y clases generadas.

### 5.2 Umbrales frontend Angular

| Tipo | Umbral mínimo |
|---|---|
| Statements | ≥70% |
| Branches | ≥65% |
| Functions | ≥70% |

### 5.3 Cobertura en PL/SQL

La cobertura de PL/SQL no se mide con herramientas de instrumentación, sino con **cobertura de casos** documentada:

- Mínimo 3 casos de prueba de caracterización por procedure con lógica de negocio crítica.
- Los casos deben cubrir: ruta principal, valor límite/borde, y condición de error o nulo.

---

## 6. Pruebas de contrato

### 6.1 Definición

Una prueba de contrato verifica que el proveedor de una API cumple las expectativas del consumidor, sin necesitar que ambos estén levantados al mismo tiempo. Es la herramienta que evita que un cambio en el backend rompa silenciosamente a sus consumidores.

### 6.2 Obligatoriedad

| Caso | Regla |
|---|---|
| API interna simple, un solo consumidor controlado | Recomendado |
| API publicada en WSO2 para varios consumidores | **Obligatorio** |
| API consumida por entidades externas | **Obligatorio** |
| API crítica (autenticación, autorización, pagos, trámites previsionales) | **Obligatorio** |
| Microservicio o módulo candidato a microservicio | **Obligatorio** |
| Integración legacy difícil de estabilizar | Recomendado con prioridad alta |

### 6.3 Herramientas aprobadas

| Herramienta | Cuándo usarla |
|---|---|
| **OpenAPI validation** | Mínimo obligatorio para toda API REST: el esquema publicado en Swagger debe ser el que el cliente recibe realmente |
| **Pact** | Cuando el consumidor lidera la definición del contrato (consumer-driven contract testing) |
| **Spring Cloud Contract** | Cuando el proveedor Spring Boot controla el contrato y genera los stubs para los consumidores |

Para APIs nuevas en ONP que deban cumplir la regla de Obligatorio, el equipo elige entre Pact y Spring Cloud Contract mediante ADR. OpenAPI validation aplica siempre, sin excepción.

### 6.4 Mínimo de OpenAPI validation

Toda API REST debe tener al menos una prueba que valide que la respuesta real del servicio cumple el esquema definido en el `openapi.yml`. Se reconocen dos niveles:

| Nivel | Descripción | Cuándo aplicar |
|---|---|---|
| **Mínimo aceptable** | Validar la respuesta contra un JSON Schema derivado del OpenAPI (`pension-response.json`). Verifica la estructura del body, pero no cubre headers, path params ni códigos de estado del contrato completo | APIs internas sin múltiples consumidores |
| **Preferente** | Validar request y response contra el `openapi.yml` completo usando un OpenAPI validator (p. ej. `OpenApiValidationFilter` de RestAssured o `atlassian-oai-validator`). Cubre todo el contrato | APIs publicadas en WSO2 o con consumidores externos |

> Validar un JSON Schema suelto no equivale a validar todo el contrato OpenAPI. Para APIs críticas o publicadas, usar el nivel preferente.

```java
// Nivel preferente: OpenApiValidationFilter apuntando al openapi.yml completo
@Test
void schemaDeRespuesta_debeSerValidoSegunOpenApi() {
    given()
        .filter(validationFilter) // OpenApiValidationFilter apuntando al openapi.yml
        .header("Authorization", "Bearer " + tokenValido)
    .when()
        .get("/api/v1/pensiones/{id}", 12345L)
    .then()
        .statusCode(200)
        .assertThat(); // el filtro valida request y response contra el contrato completo
}

// Nivel mínimo aceptable: JSON Schema derivado del OpenAPI
@Test
void schemaDeRespuesta_cumpleJsonSchema() {
    given()
        .header("Authorization", "Bearer " + tokenValido)
    .when()
        .get("/api/v1/pensiones/12345")
    .then()
        .statusCode(200)
        .body(matchesJsonSchemaInClasspath("openapi/schemas/pension-response.json"));
}
```

---

## 7. Relación con CI/CD

Este lineamiento define qué pruebas existen, cuáles son obligatorias, cómo se clasifican, qué cobertura mínima aplica y qué evidencia se debe generar. LIN-CICD-001 define cuándo se ejecutan en el pipeline y qué gates bloquean el merge o el despliegue.

Reglas que aplican desde este lineamiento para garantizar la automatizabilidad:

1. **Toda prueba debe poder ejecutarse sin intervención manual.** No deben depender de datos insertados a mano, servicios externos no reproducibles o estados de entorno no controlados.
2. **Toda prueba debe generar reporte.** JUnit XML (Surefire/Failsafe), JaCoCo HTML/XML, reporte de cobertura Angular — todos deben ser artefactos generables por el build.
3. **Los umbrales de cobertura deben estar en el código** (pom.xml, jest.config.js), no solo en el pipeline. Si el pipeline falla porque alguien los quitó del pom.xml, el pipeline debe poder detectarlo.
4. **Ningún criterio de prueba puede quedar definido únicamente en el pipeline.** Si la regla no está en este lineamiento o en el código del proyecto, no existe.
5. **Las pruebas de integración usan contenedores reproducibles** (Testcontainers), no bases de datos compartidas del equipo. Para pruebas de caracterización PL/SQL legacy, OracleContainer es preferente; cuando el procedure dependa de configuraciones no reproducibles en XE (grants, DB links, scheduler jobs, synonyms), se permite el uso de un ambiente QA controlado con dataset documentado, anonimizado y ejecución repetible.

---

## 8. Evidencias obligatorias

Para que un artefacto se considere apto para pasar a QA, deben existir los siguientes reportes generados automáticamente por el build:

### 8.1 Backend Java

| Evidencia | Generada por | Formato |
|---|---|---|
| Resultados de pruebas unitarias | Maven Surefire | XML en `target/surefire-reports/` |
| Resultados de pruebas de integración | Maven Failsafe | XML en `target/failsafe-reports/` |
| Reporte de cobertura JaCoCo | JaCoCo Maven Plugin | HTML + XML en `target/site/jacoco/` |
| Validación de cobertura mínima | JaCoCo check goal | Build falla si no alcanza umbral |

### 8.2 Frontend Angular

| Evidencia | Generada por | Formato |
|---|---|---|
| Resultados de pruebas unitarias | Jest o Karma | XML JUnit + HTML |
| Cobertura de código | Istanbul (incluido en Jest/Karma) | HTML en `coverage/` |
| Resultados E2E | Cypress o Playwright | Video + HTML report |

### 8.3 PL/SQL Legacy

| Evidencia | Generada por | Formato |
|---|---|---|
| Pruebas de caracterización ejecutadas | Maven Failsafe (`*CT.java`) | XML en `target/failsafe-reports/` |
| Dataset de entrada/salida documentado | Comentarios en el `*CT.java` | Código fuente versionado |
| Resultado de regresión antes de modificar | Mismas pruebas corriendo en verde | Reporte de Failsafe |

### 8.4 Contrato

| Evidencia | Generada por | Formato |
|---|---|---|
| Contrato OpenAPI validado | OpenAPI validator (RestAssured o similar) | Test report |
| Pact broker verification (si aplica) | Pact Maven plugin | Pact broker o archivo local |
| Spring Cloud Contract stubs (si aplica) | Spring Cloud Contract plugin | JAR de stubs en repositorio Maven |

### 8.5 Pruebas de seguridad (gestionadas por UFSD)

> Este lineamiento no define la técnica de las pruebas de seguridad. La UFSD (Unidad Funcional de Seguridad Digital) es responsable de ejecutar Ethical Hacking, DAST y retest. Lo que sí establece LIN-TEST-001 es qué evidencias el equipo de desarrollo debe entregar a la UFSD para habilitar su ejecución, y qué resultado debe quedar registrado como criterio de aceptación.

**Cuándo aplican pruebas de seguridad gestionadas por UFSD:**

| Tipo | Cuándo es obligatorio |
|---|---|
| **Ethical Hacking** (Penetration Testing) | Nuevas aplicaciones; nuevos servicios o endpoints de API; cambios funcionales o arquitectónicos que incrementen la superficie de exposición del sistema |
| **DAST** (Análisis Dinámico) | Mantenimientos o modificaciones en aplicaciones que ya están en operación |
| **Retest de vulnerabilidades** | Cuando una vulnerabilidad identificada por UFSD fue subsanada — confirmar el cierre |

**Responsabilidades del equipo de desarrollo durante pruebas de seguridad:**

El equipo de desarrollo debe entregar a la UFSD los insumos necesarios para ejecutar la evaluación:

| Insumo | Descripción |
|---|---|
| Acceso al ambiente de pruebas | URLs base del sistema/API en ambiente QA correctamente levantado |
| Usuarios de prueba | Credenciales temporales con los distintos perfiles de acceso del sistema |
| Colección Postman / matriz de endpoints | Listado de todos los endpoints expuestos con sus parámetros y ejemplos |
| Guía funcional básica | Descripción de los flujos principales para orientar la evaluación |

**Evidencias resultantes (responsabilidad de UFSD):**

| Evidencia | Formato |
|---|---|
| Matriz de vulnerabilidades | Documento con hallazgos clasificados por criticidad, estado y responsable de remediación |
| Acta de reunión de cierre | Firmada por UFSD, desarrollo y plataforma — registra las vulnerabilidades subsanadas y el resultado del retest |
| Opinión favorable de UFSD | Habilitación formal para el pase a Producción cuando el proyecto lo requiere |

Estas evidencias se adjuntan al expediente del proyecto y son requisito para el pase a Producción en los casos donde el Ethical Hacking es obligatorio (ver [sección 9.2](#92-criterios-de-paso-a-produccion-merge-a-rama-master)).

---

## 9. Criterios mínimos de aceptación para paso a QA y Producción

> Estos criterios definen las condiciones técnicas mínimas verificables. LIN-CICD-001 los consume para configurar los gates automáticos en pipeline — este lineamiento no define cuándo ni cómo se ejecutan las validaciones automáticas.

### 9.1 Criterios de paso a QA (merge a rama `ONP_DESA` → `ONP_QA`)

Un artefacto **no puede pasar a QA** si:

| Criterio | Bloquea |
|---|---|
| Cobertura JaCoCo inferior al umbral del estilo | Sí |
| Prueba unitaria o de integración fallida | Sí |
| Prueba de caracterización fallida (si existen CT) | Sí |
| Cobertura Angular inferior al umbral | Sí |
| Prueba de contrato fallida (si es obligatorio) | Sí |
| Prueba de caracterización faltante antes de modificar procedure legacy con lógica crítica | Sí |

### 9.2 Criterios de paso a Producción (merge a rama `master`)

Además de los criterios de QA:

| Criterio adicional | Bloquea |
|---|---|
| Prueba E2E de happy path fallida | Sí |
| Validación OpenAPI schema fallida | Sí |
| Reporte de cobertura no generado (evidencia ausente) | Sí |
| Opinión favorable de UFSD ausente, cuando el proyecto requiere Ethical Hacking (nuevas apps, nuevos endpoints, cambios de exposición) | Sí |
| Vulnerabilidades críticas o altas sin subsanar ni retest aprobado por UFSD | Sí |

### 9.3 Excepciones

Toda excepción a los criterios de aceptación requiere ADR firmado por Arquitectura de Software que documente:
- Por qué no se puede cumplir el criterio
- Qué control compensatorio existe
- Fecha de revisión para levantar la excepción

---

## 10. Responsabilidades

### 10.1 Equipo de desarrollo

| Responsabilidad | Detalle |
|---|---|
| Escribir pruebas junto con el código | No como tarea separada al final |
| Mantener los umbrales de cobertura en el pom.xml | Ver [sección 11.5](#115-configuracion-jacoco-en-pomxml) |
| Escribir pruebas de caracterización antes de modificar legacy | Mínimo 3 casos — ver [sección 13](#13-capitulo-plsql-legacy) |
| Generar y conservar evidencias de prueba | Reportes en el build, no capturas de pantalla manuales |
| Corregir pruebas antes de hacer merge | Una prueba fallida no es deuda técnica aceptable |

### 10.2 Arquitectura de Software

| Responsabilidad | Detalle |
|---|---|
| Definir estilo arquitectónico por sistema | Determina la pirámide y umbrales aplicables |
| Aprobar excepciones (ADR) | Cuando un sistema no puede cumplir algún criterio |
| Mantener este lineamiento | Ante cambios en herramientas o prácticas institucionales |
| Revisar pruebas de caracterización de sistemas críticos | Antes de la primera modificación funcional |

### 10.3 QA / Testing

| Responsabilidad | Detalle |
|---|---|
| Definir escenarios E2E por flujo de negocio | En coordinación con el equipo de desarrollo |
| Validar evidencias de prueba antes de aprobar paso a QA | Reportes JaCoCo, Surefire, Failsafe |
| Identificar huecos de cobertura en zonas de riesgo | Más allá del porcentaje global |

---

## 11. Capítulo: Backend Java

> **Alcance de cobertura backend:** Los servicios backend Java deben cubrirse mediante pruebas unitarias, pruebas de integración, pruebas de endpoints REST y pruebas de contrato cuando aplique (ver [sección 6.2](#62-obligatoriedad)), con medición de cobertura usando JaCoCo. Las herramientas E2E (Playwright, Cypress) no reemplazan estas pruebas y no deben usarse para probar APIs REST del backend.

### 11.1 Herramientas aprobadas

| Herramienta | Versión | Propósito |
|---|---|---|
| JUnit 5 (Jupiter) | ≥5.10 | Framework de pruebas unitarias e integración |
| Mockito | ≥5.x | Mocking de dependencias en pruebas unitarias |
| AssertJ | ≥3.24 | Aserciones fluidas y legibles |
| Testcontainers | ≥1.19 | Contenedores Docker reproducibles para integración |
| OracleContainer | `gvenzl/oracle-xe:21-slim-faststart` | Oracle para pruebas de integración y caracterización |
| MockMvc | incluido en Spring Boot Test | Pruebas de controladores REST sin servidor real |
| JaCoCo Maven Plugin | ≥0.8.11 | Medición de cobertura de código |
| RestAssured | ≥5.x | Pruebas de APIs REST (alternativa a MockMvc para IT) |
| OpenAPI Validator (RestAssured) | `io.rest-assured:json-schema-validator` | Validación de schema OpenAPI en pruebas |

### 11.2 Pruebas unitarias

**Estructura de una prueba unitaria:**

```java
// PensionCalculatorServiceTest.java
@ExtendWith(MockitoExtension.class)
class PensionCalculatorServiceTest {

    @Mock
    private AportesRepository aportesRepository;

    @InjectMocks
    private PensionCalculatorService service;

    @Test
    @DisplayName("calcular pension - con 20 años de aportes debe superar minimo vital")
    void calcularPension_con20AnosAportes_superaMinimoVital() {
        // Arrange
        var afiliado = Afiliado.builder().dni("12345678").anosAportes(20).build();
        when(aportesRepository.totalAportesPor(afiliado.getDni()))
            .thenReturn(new BigDecimal("45000.00"));

        // Act
        Pension pension = service.calcular(afiliado);

        // Assert
        assertThat(pension.getMonto()).isGreaterThanOrEqualTo(new BigDecimal("500.00"));
        assertThat(pension.getModalidad()).isEqualTo(Modalidad.RENTA_VITALICIA);
    }

    @Test
    @DisplayName("calcular pension - sin aportes suficientes debe lanzar excepcion de negocio")
    void calcularPension_sinAportesSuficientes_lanzaExcepcionNegocio() {
        var afiliado = Afiliado.builder().dni("99999999").anosAportes(2).build();
        when(aportesRepository.totalAportesPor(any())).thenReturn(BigDecimal.ZERO);

        assertThatThrownBy(() -> service.calcular(afiliado))
            .isInstanceOf(AporteInsuficienteException.class)
            .hasMessageContaining("años mínimos");
    }
}
```

**Reglas para pruebas unitarias:**

1. Una clase de prueba por clase bajo prueba.
2. Un método de prueba por comportamiento, no por método del SUT.
3. El nombre del test describe el escenario: `método_condición_resultado` o `displayName` en español.
4. No hay lógica de negocio en el `@BeforeEach` — solo setup del fixture.
5. No acceden a base de datos, filesystem ni red.

### 11.3 Pruebas de integración

**Prueba de integración con repositorio Oracle (Testcontainers):**

```java
// AportesRepositoryIT.java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class AportesRepositoryIT {

    @Container
    static OracleContainer oracle = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart")
        .withDatabaseName("XEPDB1")
        .withUsername("onp_test")
        .withPassword("onp_test");

    @DynamicPropertySource
    static void oracleProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", oracle::getJdbcUrl);
        registry.add("spring.datasource.username", oracle::getUsername);
        registry.add("spring.datasource.password", oracle::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "oracle.jdbc.OracleDriver");
    }

    @Autowired
    private AportesRepository repository;

    @Test
    @DisplayName("totalAportesPor - debe sumar correctamente los aportes del afiliado")
    void totalAportesPor_afiliadoConAportes_devuelveSumaCorrecta() {
        // dataset insertado via Flyway en el contenedor de prueba
        BigDecimal total = repository.totalAportesPor("12345678");
        assertThat(total).isEqualByComparingTo(new BigDecimal("45000.00"));
    }
}
```

**Prueba de integración de controller (MockMvc):**

```java
// PensionesControllerIT.java
@SpringBootTest
@AutoConfigureMockMvc
class PensionesControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PensionCalculatorService calculatorService;

    @Test
    @DisplayName("GET /api/v1/pensiones/{id} - responde 200 con schema correcto")
    @WithMockUser(username = "jperez", authorities = {"CONSULTAR_PENSION"})
    void getPension_idValido_responde200ConSchemaValido() throws Exception {
        when(calculatorService.buscarPorId(12345L))
            .thenReturn(PensionDto.builder()
                .id(12345L).monto(new BigDecimal("850.00")).estado("ACTIVA").build());

        mockMvc.perform(get("/api/v1/pensiones/12345")
                .header("X-Request-ID", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codDetRespuesta").value("000"))
            .andExpect(jsonPath("$.data.monto").value(850.00))
            .andExpect(jsonPath("$.data.estado").value("ACTIVA"));
    }

    @Test
    @DisplayName("GET /api/v1/pensiones/{id} - sin token responde 401")
    void getPension_sinToken_responde401() throws Exception {
        mockMvc.perform(get("/api/v1/pensiones/12345"))
            .andExpect(status().isUnauthorized());
    }
}
```

### 11.4 Pruebas de contrato (Java)

**OpenAPI validation — mínimo obligatorio:**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>json-schema-validator</artifactId>
    <scope>test</scope>
</dependency>
```

```java
// PensionesApiContractIT.java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PensionesApiContractIT {

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("GET /api/v1/pensiones/{id} - respuesta cumple schema OpenAPI")
    void getPension_respuestaCumpleSchemaOpenApi() {
        RestAssured.given()
            .port(port)
            .header("Authorization", "Bearer " + obtenerTokenPrueba())
        .when()
            .get("/api/v1/pensiones/12345")
        .then()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath("openapi/schemas/pension-response.json"));
    }
}
```

**Spring Cloud Contract (cuando sea obligatorio):**

```groovy
// src/test/resources/contracts/pensiones/obtener_pension_existente.groovy
Contract.make {
    description "GET /api/v1/pensiones/{id} - pension existente retorna 200"
    request {
        method GET()
        url "/api/v1/pensiones/12345"
        headers { header("Authorization", matching("Bearer .+")) }
    }
    response {
        status 200
        body([
            codDetRespuesta: "000",
            data: [id: 12345, monto: 850.00, estado: "ACTIVA"]
        ])
        headers { contentType(applicationJson()) }
    }
}
```

### 11.5 Configuración JaCoCo en pom.xml

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <!-- Instrumentación -->
        <execution>
            <id>prepare-agent</id>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <!-- Reporte HTML/XML -->
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <!-- Validación de umbral — build falla si no se alcanza -->
        <execution>
            <id>check</id>
            <phase>verify</phase>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <!-- Ajustar según el estilo arquitectónico del sistema -->
                            <limit>
                                <counter>INSTRUCTION</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.65</minimum> <!-- Monolito Simple: 0.65 / Modular y Hexagonal: 0.70 -->
                            </limit>
                        </limits>
                    </rule>
                    <!-- Umbral más alto para capa de negocio -->
                    <rule>
                        <element>PACKAGE</element>
                        <includes>
                            <include>pe/gob/onp/*/service/**</include>
                            <include>pe/gob/onp/*/domain/**</include>
                        </includes>
                        <limits>
                            <limit>
                                <counter>INSTRUCTION</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.75</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
                <!-- Excluir de la medición -->
                <excludes>
                    <exclude>pe/gob/onp/**/config/**</exclude>
                    <exclude>pe/gob/onp/**/dto/**</exclude>
                    <exclude>pe/gob/onp/**/*Application.class</exclude>
                </excludes>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## 12. Capítulo: Frontend Angular

### 12.1 Herramientas aprobadas

| Herramienta | Propósito |
|---|---|
| Jest | Runner de pruebas unitarias (recomendado sobre Karma para Angular 17+) |
| Angular TestBed | Fixture de componentes Angular para pruebas unitarias |
| HttpClientTestingModule | Mock de HttpClient para pruebas de servicios |
| **Playwright** | **Pruebas E2E — herramienta preferente.** Multi-browser, API moderna, soporte nativo para TypeScript |
| Cypress | Pruebas E2E — permitido únicamente en proyectos donde ya está en uso. Nuevos proyectos deben usar Playwright |
| axe-core / playwright-axe / cypress-axe | Validación de accesibilidad básica (plugin según el runner E2E elegido) |
| Istanbul | Cobertura de código (incluido en Jest) |

**Playwright es la herramienta institucional preferente para pruebas E2E.** Cypress está permitido solo en proyectos que ya lo tienen adoptado — no debe incorporarse en proyectos nuevos ni en proyectos que aún no hayan elegido herramienta E2E.

### 12.2 Pruebas unitarias de componentes

```typescript
// pension-detalle.component.spec.ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PensionDetalleComponent } from './pension-detalle.component';
import { PensionService } from '../../services/pension.service';
import { of } from 'rxjs';

describe('PensionDetalleComponent', () => {
  let component: PensionDetalleComponent;
  let fixture: ComponentFixture<PensionDetalleComponent>;
  let pensionServiceSpy: jasmine.SpyObj<PensionService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('PensionService', ['obtenerPorId']);
    await TestBed.configureTestingModule({
      imports: [PensionDetalleComponent],
      providers: [{ provide: PensionService, useValue: spy }]
    }).compileComponents();

    fixture = TestBed.createComponent(PensionDetalleComponent);
    component = fixture.componentInstance;
    pensionServiceSpy = TestBed.inject(PensionService) as jasmine.SpyObj<PensionService>;
  });

  it('debe mostrar el monto de la pension cuando el servicio responde', () => {
    pensionServiceSpy.obtenerPorId.and.returnValue(
      of({ id: 1, monto: 850, estado: 'ACTIVA' })
    );

    component.ngOnInit();
    fixture.detectChanges();

    const montoEl = fixture.nativeElement.querySelector('[data-testid="pension-monto"]');
    expect(montoEl.textContent).toContain('850');
  });

  it('debe mostrar mensaje de error cuando el servicio falla', () => {
    pensionServiceSpy.obtenerPorId.and.returnValue(
      throwError(() => new Error('Error de red'))
    );

    component.ngOnInit();
    fixture.detectChanges();

    const errorEl = fixture.nativeElement.querySelector('[data-testid="error-mensaje"]');
    expect(errorEl).toBeTruthy();
  });
});
```

**Reglas para pruebas de componentes:**

1. Usar `data-testid` en elementos del template — no selectores de clase CSS que pueden cambiar.
2. No probar implementación interna — probar lo que el usuario ve.
3. Un `describe` por componente; un `it` por comportamiento.
4. Stubs y spies en lugar de servicios reales.

### 12.3 Pruebas de servicios HTTP

```typescript
// pension.service.spec.ts
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PensionService } from './pension.service';

describe('PensionService', () => {
  let service: PensionService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PensionService]
    });
    service = TestBed.inject(PensionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // verifica que no quedaron requests sin procesar
  });

  it('obtenerPorId - debe llamar a la URL correcta con el ID', () => {
    service.obtenerPorId(12345).subscribe(pension => {
      expect(pension.monto).toBe(850);
    });

    const req = httpMock.expectOne('/api/v1/pensiones/12345');
    expect(req.request.method).toBe('GET');
    req.flush({ codDetRespuesta: '000', data: { id: 12345, monto: 850, estado: 'ACTIVA' } });
  });

  it('obtenerPorId - ante 401 debe propagar el error', () => {
    service.obtenerPorId(99999).subscribe({
      error: (err) => expect(err.status).toBe(401)
    });

    const req = httpMock.expectOne('/api/v1/pensiones/99999');
    req.flush('No autorizado', { status: 401, statusText: 'Unauthorized' });
  });
});
```

### 12.4 Pruebas E2E

> **Alcance de las pruebas E2E:** Las pruebas E2E con Playwright o Cypress aplican a aplicaciones web frontend, validando flujos completos desde la perspectiva del usuario en el navegador. No reemplazan las pruebas unitarias, de integración, de API ni de contrato del backend. Para probar servicios backend Java, usar MockMvc, RestAssured, Spring Boot Test, OpenAPI validator, Pact o Spring Cloud Contract.

El ejemplo siguiente usa Cypress (válido para proyectos que ya lo tienen). En proyectos nuevos, reemplazar por el equivalente Playwright.

```typescript
// consulta-pension.e2e.cy.ts (Cypress — proyectos existentes)
// Equivalente Playwright: consulta-pension.e2e.spec.ts
describe('Consulta de pensión — flujo principal', () => {

  beforeEach(() => {
    cy.login('jperez', 'clave-prueba'); // comando custom que autentica via SAA
  });

  it('debe mostrar la pension del afiliado al buscar por DNI', () => {
    cy.visit('/pensiones/consulta');

    cy.get('[data-testid="input-dni"]').type('12345678');
    cy.get('[data-testid="btn-buscar"]').click();

    cy.get('[data-testid="resultado-monto"]').should('be.visible');
    cy.get('[data-testid="resultado-estado"]').should('contain', 'ACTIVA');
  });

  it('debe mostrar mensaje de error al buscar DNI inexistente', () => {
    cy.visit('/pensiones/consulta');

    cy.get('[data-testid="input-dni"]').type('99999999');
    cy.get('[data-testid="btn-buscar"]').click();

    cy.get('[data-testid="mensaje-error"]').should('be.visible');
    cy.get('[data-testid="resultado-monto"]').should('not.exist');
  });
});
```

**Reglas E2E:**

1. Al menos un test E2E por flujo de negocio principal (happy path).
2. Usar comandos/fixtures de autenticación reutilizables — no hardcodear credenciales en el test.
3. Las pruebas E2E no deben depender de datos de producción — usar ambiente QA con dataset controlado.
4. No usar esperas fijas (`cy.wait(N)`, `page.waitForTimeout(N)`) — usar aserciones que esperan activamente el elemento o estado.
5. Las pruebas E2E son exclusivamente para flujos de usuario en navegador — no se usan para validar endpoints REST del backend directamente.

### 12.5 Accesibilidad básica

```typescript
// accesibilidad.e2e.cy.ts
import 'cypress-axe';

describe('Accesibilidad básica', () => {
  it('la página de consulta de pensión no tiene violaciones críticas', () => {
    cy.visit('/pensiones/consulta');
    cy.injectAxe();
    cy.checkA11y(null, {
      includedImpacts: ['critical', 'serious']
    });
  });
});
```

Requerimientos mínimos de accesibilidad que deben pasar:
- Imágenes con `alt` descriptivo
- Formularios con `label` asociado a cada `input`
- Contraste de color suficiente (WCAG AA)
- Navegación por teclado en flujos principales

### 12.6 Configuración de cobertura (jest.config.js)

```javascript
// jest.config.js
module.exports = {
  coverageThreshold: {
    global: {
      statements: 70,
      branches: 65,
      functions: 70,
      lines: 70
    }
  },
  coverageReporters: ['html', 'lcov', 'text-summary'],
  coverageDirectory: 'coverage'
};
```

---

## 13. Capítulo: PL/SQL Legacy

### 13.1 Contexto y principio

Los procedures, packages y functions PL/SQL de la ONP contienen lógica de negocio crítica acumulada durante años. En muchos casos no existe documentación funcional que describa con precisión qué hace un procedure ni bajo qué condiciones.

> **Principio:** Antes de modificar un procedure, package o function con lógica de negocio crítica, el equipo debe demostrar que entiende su comportamiento actual mediante pruebas de caracterización. No como ejercicio de calidad de código, sino como red de seguridad del refactor.

La observabilidad de PL/SQL desde Java se logra a través del adapter Java que lo invoca (ver LIN-DEV-JAVA-001 sección 8 y LIN-BD-ORA-001 sección 6.0). Las pruebas de caracterización se escriben en Java, invocando el procedure real sobre OracleContainer.

### 13.2 Pruebas de caracterización — definición y técnica

**Qué es una prueba de caracterización:**

Una prueba de caracterización captura el comportamiento observable actual de un componente legacy. No pregunta "¿es este el comportamiento correcto?", sino "¿este es el comportamiento que existe hoy?". Si el componente cambia de comportamiento, la prueba falla — eso es exactamente lo que debe hacer.

**Cuándo es obligatoria:**

- Antes de cualquier modificación funcional a un procedure, package o function con lógica de negocio crítica
- Cuando el equipo no puede responder con certeza: "¿qué devuelve este procedure con estos parámetros?"
- Cuando LIN-BD-ORA-001 sección 6.0 lo exige explícitamente

**Cuándo se escribe:**

- Siempre **antes** del cambio funcional, nunca después
- Si no existe previamente, es responsabilidad de quien va a hacer el cambio crearla

**Mínimo de casos por procedure:**

| Caso | Descripción |
|---|---|
| Caso 1: ruta principal | Input típico — el caso más frecuente en producción |
| Caso 2: valor límite o borde | Input en el borde de una condición (0, null, máximo) |
| Caso 3: condición de error o excepción | Input que genera error, excepción o resultado vacío |

### 13.3 Estructura de una prueba de caracterización

```java
// SpCalcularPensionCT.java
// Prueba de caracterización — NO modifica el comportamiento, solo lo documenta
// Ejecutar con: mvn failsafe:integration-test -Dgroups=characterization
@Tag("characterization")
@Testcontainers
class SpCalcularPensionCT {

    @Container
    static OracleContainer oracle = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart")
        .withInitScript("sql/sp_calcular_pension.sql") // el procedure real
        .withInitScript("sql/dataset_caracterizacion.sql"); // datos controlados de QA

    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        var ds = new DriverManagerDataSource(
            oracle.getJdbcUrl(), oracle.getUsername(), oracle.getPassword());
        jdbc = new JdbcTemplate(ds);
    }

    /**
     * Caso 1: afiliado con 20 años de aportes — ruta principal.
     * Comportamiento capturado el 2026-05-26 en ambiente QA.
     * Resultado esperado: monto = 850.00, modalidad = 'RENTA_VITALICIA'.
     */
    @Test
    @DisplayName("[CT] SP_CALCULAR_PENSION - afiliado con 20 años de aportes retorna pension RENTA_VITALICIA")
    void rutaPrincipal_afiliadoCon20AniosAportes_retornaRentaVitalicia() {
        var result = jdbc.call(
            con -> {
                var cs = con.prepareCall("{call SP_CALCULAR_PENSION(?, ?, ?)}");
                cs.setString(1, "12345678"); // DNI del afiliado de prueba
                cs.registerOutParameter(2, Types.NUMERIC);  // monto
                cs.registerOutParameter(3, Types.VARCHAR);  // modalidad
                return cs;
            },
            List.of(
                SqlParameter.in(Types.VARCHAR),
                new SqlOutParameter("monto", Types.NUMERIC),
                new SqlOutParameter("modalidad", Types.VARCHAR)
            )
        );

        assertThat(result.get("monto"))
            .as("monto de pension para afiliado con 20 años de aportes")
            .isEqualByComparingTo(new BigDecimal("850.00"));
        assertThat(result.get("modalidad"))
            .as("modalidad de pension")
            .isEqualTo("RENTA_VITALICIA");
    }

    /**
     * Caso 2: afiliado en el límite mínimo (exactamente 5 años de aportes).
     * Comportamiento capturado el 2026-05-26 en ambiente QA.
     * Resultado esperado: monto = 500.00 (pensión mínima).
     */
    @Test
    @DisplayName("[CT] SP_CALCULAR_PENSION - afiliado en limite minimo retorna pension minima")
    void valorLimite_afiliadoConMinimoDeAportes_retornaPensionMinima() {
        var result = jdbc.call(/* ... DNI afiliado con exactamente 5 años ... */);

        assertThat(result.get("monto"))
            .as("monto de pension mínima")
            .isEqualByComparingTo(new BigDecimal("500.00"));
    }

    /**
     * Caso 3: afiliado sin aportes registrados.
     * Comportamiento capturado el 2026-05-26 en ambiente QA.
     * Resultado esperado: el procedure lanza excepción de aplicación o retorna código de error.
     */
    @Test
    @DisplayName("[CT] SP_CALCULAR_PENSION - afiliado sin aportes genera error o resultado nulo")
    void condicionError_afiliadoSinAportes_generaResultadoEspecificado() {
        assertThatThrownBy(() -> jdbc.call(/* ... DNI sin aportes ... */))
            .isInstanceOf(DataAccessException.class);
        // Alternativa si retorna código de error en lugar de excepción:
        // assertThat(result.get("codigo_error")).isEqualTo(-1);
    }
}
```

### 13.4 Dataset controlado para caracterización

El dataset debe:

1. Provenir de datos reales anonimizados de QA, no de datos inventados.
2. Estar versionado en el repositorio del proyecto (`src/test/resources/sql/`).
3. Ser reproducible: mismos datos → mismo resultado siempre.
4. No contener datos personales en claro (aplicar anonimización de DNI, nombres).

```sql
-- dataset_caracterizacion.sql
-- Dataset de prueba para SP_CALCULAR_PENSION
-- Generado: 2026-05-26 desde datos anonimizados de QA

-- Afiliado con 20 años de aportes (caso 1 - ruta principal)
INSERT INTO AFILIADOS (DNI, NOMBRE, ANOS_APORTES) VALUES ('12345678', 'TEST AFILIADO 01', 20);
INSERT INTO APORTES (DNI, MONTO, FECHA) VALUES ('12345678', 2250, DATE '2006-01-01');
-- ... más aportes para llegar a 45000 total ...

-- Afiliado en límite mínimo (caso 2 - valor límite)
INSERT INTO AFILIADOS (DNI, NOMBRE, ANOS_APORTES) VALUES ('87654321', 'TEST AFILIADO 02', 5);
-- ... aportes mínimos ...

COMMIT;
```

### 13.5 Prueba de regresión antes de modificar

El flujo obligatorio antes de modificar un procedure con lógica de negocio crítica:

```
1. ¿Existen pruebas de caracterización para este procedure?
   ├── SÍ → Ejecutarlas. ¿Pasan todas? → Continúa al paso 2.
   │         ¿Alguna falla? → Detener. Investigar antes de modificar.
   └── NO  → Escribir pruebas de caracterización (mínimo 3 casos).
             Ejecutarlas. Confirmar que capturan el comportamiento actual.
             Luego continúa al paso 2.

2. Hacer la modificación funcional.

3. Ejecutar pruebas de caracterización nuevamente.
   ├── Todas pasan → La modificación no rompió el comportamiento existente.
   └── Alguna falla → La modificación cambió el comportamiento.
                       Si el cambio era intencional: actualizar la prueba de caracterización
                       y documentar el cambio en el ADR o en el PR.
                       Si fue involuntario: revertir la modificación.
```

---

## 14. Checklist de pruebas

### Para nuevos sistemas (antes del primer deploy a QA)

```
UNITARIAS
[ ] Pruebas unitarias para todas las clases de servicio y dominio
[ ] Cobertura JaCoCo ≥ umbral del estilo arquitectónico elegido
[ ] Pruebas de servicio Angular para todos los servicios HTTP
[ ] Cobertura de statements Angular ≥ 70%

INTEGRACIÓN
[ ] Al menos 1 prueba de integración por endpoint REST
[ ] Testcontainers configurado con OracleContainer para pruebas de repositorio
[ ] MockMvc o RestAssured para pruebas de controladores
[ ] HttpClientTestingModule en pruebas de servicios Angular

CONTRATO
[ ] Validación OpenAPI schema implementada (toda API REST)
[ ] Prueba de contrato formal (Pact o Spring Cloud Contract) si aplica según tabla [sección 6.2](#62-obligatoriedad)

E2E
[ ] Al menos 1 prueba E2E por flujo de negocio principal
[ ] Prueba de accesibilidad básica con axe-core

EVIDENCIAS
[ ] Reporte JaCoCo generado (target/site/jacoco/)
[ ] Reportes Surefire y Failsafe generados
[ ] Reporte de cobertura Angular generado (coverage/)
[ ] Validación de umbral JaCoCo en pom.xml (build falla si no alcanza)
```

### Para modificación de procedure PL/SQL legacy

```
[ ] Pruebas de caracterización existentes o recién escritas (mínimo 3 casos)
[ ] Dataset controlado versionado en src/test/resources/sql/
[ ] Pruebas de caracterización pasan en verde ANTES del cambio
[ ] Pruebas de caracterización pasan en verde DESPUÉS del cambio
[ ] Si alguna falló tras el cambio: documentado en PR si fue intencional, revertido si fue involuntario
```

---

## 15. Anti-patrones

| Anti-patrón | Riesgo | Regla |
|---|---|---|
| **Prueba que siempre pasa** — `assertTrue(true)` o test vacío | Falsa sensación de cobertura | El reporte JaCoCo cubre líneas; no garantiza que las aserciones sean significativas — el reviewer debe verificar |
| **Test que toca BD de desarrollo compartida** | No reproducible; órdenes de ejecución importan | Testcontainers para toda prueba de integración |
| **Prueba unitaria que prueba el framework** — verificar que Spring inyecta el bean | No es prueba del negocio | Solo probar lógica propia de la aplicación |
| **Cobertura como único criterio** — el pom tiene 70% y se da por cumplido | Puede tenerse 70% sin probar nada significativo | La cobertura es umbral mínimo, no objetivo; el review identifica zonas de riesgo sin pruebas reales |
| **Prueba de caracterización escrita después del cambio** | No sirve — ya no captura el comportamiento original | Siempre antes del cambio |
| **Dataset inventado para caracterización** | No refleja casos reales de producción | Dataset de QA anonimizado |
| **E2E que usa `cy.wait(2000)` o `page.waitForTimeout(N)`** | Frágil — falla en entornos lentos | Usar aserciones que esperan activamente: `cy.get(...).should('be.visible')`, `expect(locator).toBeVisible()` |
| **Prueba que depende del orden de ejecución** — un test modifica estado que usa el siguiente | No aislado; difícil de depurar | Cada test debe ser independiente; usar `@BeforeEach` / `afterEach` para cleanup |
| **Ignorar pruebas fallidas con `@Disabled`** sin ADR | Deuda técnica invisible | `@Disabled` solo con ADR que documente por qué y cuándo se rehabilita |
| **Usar Playwright o Cypress para probar APIs backend** — hacer llamadas HTTP directas a endpoints REST desde una prueba E2E | Las herramientas E2E son para flujos de usuario en navegador; usarlas para APIs duplica esfuerzo y genera pruebas lentas y frágiles sin valor adicional | Para probar endpoints REST: usar MockMvc, RestAssured, Spring Boot Test, OpenAPI validator, Pact o Spring Cloud Contract |
| **Incorporar Cypress en un proyecto nuevo** cuando la herramienta E2E aún no está elegida | Cypress es legado institucional — Playwright es la herramienta preferente para nuevos proyectos | Nuevos proyectos usan Playwright; Cypress solo en proyectos donde ya está adoptado |

---

## 16. Proceso ADR para desviaciones

Cualquier desviación a los umbrales, tipos de prueba obligatorios o herramientas definidas en este lineamiento requiere ADR aprobado por Arquitectura de Software.

### Casos que típicamente requieren ADR

- Sistema legacy que no puede alcanzar la cobertura mínima sin riesgo de regresión
- Adopción de herramienta de prueba no listada en este lineamiento
- Omisión de prueba de contrato en un caso catalogado como Obligatorio
- `@Disabled` en pruebas que no pueden rehabilitarse a corto plazo
- Procedure legacy que no tiene dataset suficiente para caracterización

### Formato mínimo del ADR de pruebas

```markdown
# ADR-TEST-NNN — [Título de la decisión]

## Contexto
[Sistema, tipo de prueba, umbral o herramienta que no puede cumplirse]

## Decisión
[Qué se acepta como alternativa y por qué]

## Riesgo aceptado
[Qué queda sin verificar automáticamente]

## Control compensatorio
[Revisión manual, prueba parcial, cobertura manual de casos críticos]

## Fecha de revisión
[Cuándo se reevalúa si la excepción sigue siendo válida]

## Aprobado por
[Arquitectura de Software]
```

---

## 17. Glosario

| Término | Definición |
|---|---|
| **Prueba unitaria (UT)** | Prueba que verifica una unidad de código en aislamiento total, sin dependencias externas reales |
| **Prueba de integración (IT)** | Prueba que verifica la colaboración entre componentes reales, incluyendo BD o servicios HTTP |
| **Prueba de caracterización (CT)** | Prueba que documenta el comportamiento actual observable de código legacy para proteger refactors |
| **Prueba de contrato (CONT)** | Prueba que verifica que proveedor y consumidor de una API cumplen un acuerdo formal |
| **Prueba E2E** | Prueba que ejecuta un flujo completo de negocio desde la UI del navegador hasta el backend y sus integraciones del ambiente. Valida el comportamiento observable para el usuario — no hace asserts directos sobre tablas de BD. Aplica a aplicaciones web frontend; no reemplaza las pruebas de integración o contrato del backend |
| **Playwright** | Framework de pruebas E2E moderno; herramienta institucional preferente para nuevos proyectos Angular |
| **JaCoCo** | Java Code Coverage — herramienta de instrumentación que mide qué líneas/instrucciones ejecutaron las pruebas |
| **Testcontainers** | Librería Java que levanta contenedores Docker reproducibles para pruebas de integración |
| **OracleContainer** | Testcontainer específico para Oracle XE, usando imagen `gvenzl/oracle-xe:21-slim-faststart` |
| **Surefire** | Plugin Maven que ejecuta pruebas `*Test.java` durante `mvn test` |
| **Failsafe** | Plugin Maven que ejecuta pruebas `*IT.java` y `*CT.java` durante `mvn verify` |
| **MockMvc** | Utilidad de Spring Test para probar controllers REST sin levantar servidor HTTP |
| **Pact** | Framework de contract testing consumer-driven |
| **Spring Cloud Contract** | Framework de contract testing provider-driven para ecosistema Spring |
| **OpenAPI validation** | Verificación de que una respuesta HTTP real cumple el schema definido en el openapi.yml |
| **Dataset controlado** | Conjunto de datos reproducibles y anonimizados usados como entrada en pruebas de caracterización |
| **Happy path** | Escenario de prueba que sigue el flujo principal sin errores |
| **Istanbul** | Herramienta de cobertura de código JavaScript/TypeScript, incluida en Jest |
| **axe-core** | Motor de análisis de accesibilidad web, usado en pruebas automatizadas vía cypress-axe |
| **ADR** | Architecture Decision Record — registro de decisión de arquitectura |

---

## Anexo A — Ejemplo completo Backend Java

Escenario: sistema de consulta de pensiones, estilo Monolito Simple.

### A.1 Estructura de archivos de prueba

```
src/test/java/pe/gob/onp/pensiones/
├── service/
│   └── PensionCalculatorServiceTest.java      (@Tag("unit"))
├── repository/
│   └── AportesRepositoryIT.java               (@Tag("integration"))
├── controller/
│   └── PensionesControllerIT.java             (@Tag("integration"))
├── contract/
│   └── PensionesApiContractIT.java            (@Tag("integration"))
└── legacy/
    └── SpCalcularPensionCT.java               (@Tag("characterization"))

src/test/resources/
├── sql/
│   ├── sp_calcular_pension.sql                (procedure real)
│   └── dataset_caracterizacion.sql            (datos controlados)
└── openapi/
    └── schemas/
        └── pension-response.json              (schema extraído del openapi.yml)
```

### A.2 pom.xml — configuración de plugins de prueba

```xml
<!-- Maven Surefire — ejecuta *Test.java -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>**/*IT.java</exclude>
            <exclude>**/*CT.java</exclude>
        </excludes>
    </configuration>
</plugin>

<!-- Maven Failsafe — ejecuta *IT.java y *CT.java -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>integration-test</goal>
                <goal>verify</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

---

## Anexo B — Ejemplo completo Frontend Angular

### B.1 Estructura de archivos de prueba

```
src/
├── app/
│   └── pensiones/
│       ├── pension-detalle/
│       │   ├── pension-detalle.component.ts
│       │   └── pension-detalle.component.spec.ts   (unitaria)
│       └── services/
│           ├── pension.service.ts
│           └── pension.service.spec.ts              (servicio HTTP)
└── e2e/
    ├── consulta-pension.e2e.cy.ts                   (E2E happy path)
    └── accesibilidad.e2e.cy.ts                      (axe-core)
```

### B.2 cypress.config.ts

```typescript
import { defineConfig } from 'cypress';

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:4200',
    specPattern: 'src/e2e/**/*.e2e.cy.ts',
    supportFile: 'src/e2e/support/commands.ts',
    video: true,
    screenshotOnRunFailure: true,
  },
});
```

### B.3 Comando custom de login (cypress/support/commands.ts)

```typescript
Cypress.Commands.add('login', (username: string, password: string) => {
  cy.session([username, password], () => {
    cy.visit('/login');
    cy.get('[data-testid="input-usuario"]').type(username);
    cy.get('[data-testid="input-password"]').type(password);
    cy.get('[data-testid="btn-ingresar"]').click();
    cy.url().should('include', '/inicio');
  });
});
```

---

## Anexo C — Ejemplo completo PL/SQL Legacy

### C.1 Flujo de trabajo completo para modificar SP_CALCULAR_PENSION

```bash
# 1. Ejecutar pruebas de caracterización existentes
mvn failsafe:integration-test -Dgroups=characterization -Dtest.legacy=SP_CALCULAR_PENSION

# 2. Verificar que todas pasan en verde (antes del cambio)
# Si alguna falla → investigar antes de continuar

# 3. Hacer el cambio funcional en el procedure

# 4. Ejecutar pruebas de caracterización nuevamente
mvn failsafe:integration-test -Dgroups=characterization -Dtest.legacy=SP_CALCULAR_PENSION

# 5. Si alguna falla y el cambio era intencional → actualizar la CT y documentar en PR
# Si alguna falla y fue involuntario → revertir el cambio
```

### C.2 Anonimización del dataset

```sql
-- Script de anonimización para extraer datos de QA a dataset de caracterización
-- Ejecutar en QA — NUNCA en PROD

SELECT
    LPAD(ROWNUM, 8, '0') AS DNI_ANONIMIZADO,   -- reemplaza DNI real
    'TEST AFILIADO ' || ROWNUM AS NOMBRE,         -- reemplaza nombre real
    ANOS_APORTES,
    MONTO_TOTAL_APORTES
FROM AFILIADOS_QA
WHERE ANOS_APORTES IN (20, 5, 0)   -- capturar casos representativos
ORDER BY ANOS_APORTES;
```
