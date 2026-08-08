# GLOSARIO ONP — Términos Transversales

**Código:** GLOSARIO-ONP  
**Versión:** 0.2.2 (Corrige "Estadios de Topología" a la escala oficial 1/2/3 de `LIN-ARQ-001 §2.1` — la definición anterior usaba una numeración 0/1/2 inexistente en el marco rector y citaba una sección errónea)  
**Fecha:** 2026-08-05  
**Estado:** Vigente / Operativo  
**Propósito:** consolidar términos de uso recurrente en los lineamientos ONP para reducir ambigüedad entre arquitectura, diseño táctico, desarrollo, seguridad, base de datos, pruebas y plataforma en su jerarquía de 3 niveles.

---

## 1. Alcance

Este glosario no reemplaza definiciones especializadas de cada lineamiento. Su función es fijar un vocabulario común cuando un término aparece en varios documentos del corpus.

Si un lineamiento necesita una definición más específica para su dominio, debe:

- mantener consistencia con este glosario;
- agregar solo la precisión contextual necesaria;
- evitar redefinir el término con significado incompatible.

---

## 2. Términos organizativos y de gobierno

| Término | Definición ONP |
|---|---|
| **Arquitectura OTI** | Rol responsable de las decisiones de arquitectura, lineamientos técnicos, excepciones y gobierno del corpus documental. |
| **Documento dueño** | Lineamiento o documento que es fuente autoritativa de un tema. Los demás pueden consumirlo o referenciarlo, pero no redefinirlo. |
| **Documento consumidor** | Documento que implementa, aplica o referencia una regla definida por otro documento dueño. |
| **Corpus documental** | Conjunto completo de lineamientos, matrices, ADR, plantillas y documentos de apoyo vigentes para desarrollar software en ONP. |
| **Modelo en 3 Niveles** | Estructura federada del gobierno arquitectónico de la OTI dividida en: **Nivel 1 Macro** (`LIN-ARQ-001`), **Nivel 2 Táctico** (`LIN-DIS-001`) y **Nivel 3 Micro/Código** (`LIN-DEV-JAVA-001`). |
| **Cantera Histórica** | Documento o especificación técnica anterior (ej. `LIN-ARQ-000`) preservado con fines de trazabilidad evolutiva e invariable (`Congelado`), que ya no rige como norma de diseño en nuevos proyectos. |
| **Baseline** | Punto de partida institucional reutilizable. Puede ser documental, técnico o de pipeline. |
| **ADR** | Architecture Decision Record. Registro formal de una decisión de arquitectura, su contexto, alternativas, consecuencias, vigencia y responsables. |
| **Excepción** | Desviación aprobada respecto de un lineamiento. Debe quedar documentada y trazable, normalmente mediante ADR y firma de la Dirección de Arquitectura. |
| **Vigente** | Documento o decisión que debe usarse operativamente en proyectos nuevos o cambios relevantes. |
| **Borrador** | Documento existente pero todavía sujeto a ajustes antes de declararse operativo o vigente. |
| **Conforme** | Estado usado en la matriz cuando dueño y consumidores están alineados en la versión revisada. |
| **Resuelto** | Estado usado en la matriz cuando una inconsistencia previa fue corregida y existe evidencia mínima de cierre. |

---

## 3. Términos de ciclo de vida y entrega

| Término | Definición ONP |
|---|---|
| **Ambiente** | Contexto técnico de ejecución de una solución. Típicamente `DEV`, `QA`, `PROD` o equivalentes institucionales. |
| **DEV** | Ambiente de desarrollo compartido o de validación temprana. No equivale al equipo local del desarrollador. |
| **QA** | Ambiente de aseguramiento de calidad funcional, técnica o integrada antes de producción. |
| **PROD** | Ambiente productivo usado por usuarios reales o procesos institucionales operativos. |
| **Pase** | Movimiento controlado de cambios hacia un ambiente determinado, con evidencia, aprobación y trazabilidad. |
| **Release** | Conjunto versionado de cambios aprobado para despliegue o promoción. |
| **Promoción** | Paso del mismo artefacto entre ambientes sin reconstrucción, cambiando solo configuración externa cuando aplica. |
| **Artefacto** | Resultado versionado de construcción o entrega: binario, imagen, manifiesto, paquete de scripts, plantilla, etc. |
| **Imagen inmutable** | Imagen de contenedor que no se modifica entre ambientes; se promueve tal cual. |
| **Pipeline** | Flujo automatizado de validación, build, pruebas, seguridad, empaquetado y/o despliegue en GitLab. |
| **Gate** | Regla de control que bloquea o permite avanzar una etapa del pipeline o del proceso de entrega. |

---

## 4. Términos de desarrollo y arquitectura

| Término | Definición ONP |
|---|---|
| **Monolito simple** | Aplicación desplegable única con estructura básica en capas y complejidad moderada. |
| **Monolito modular** | Aplicación desplegable única organizada en módulos explícitos con fronteras claras y dependencias controladas. Topología: `LIN-ARQ-001 §2.1` (Estadio 2, estándar por defecto); diseño interno: `LIN-DIS-001 §3`. |
| **Hexagonal** | Estilo donde dominio y casos de uso se aíslan de frameworks mediante puertos y adaptadores (`LIN-DIS-001 §2.3`). |
| **Dominio** | Modelo del problema de negocio que el software resuelve, con sus reglas, conceptos y restricciones. |
| **Bounded Context** | Límite explícito dentro del cual un modelo de dominio es consistente y tiene significado unívoco. |
| **Estadios de Topología** | Clasificación evolutiva de sistemas ONP según `LIN-ARQ-001 §2.1`: **Estadio 1** (Monolito Tradicional / Legacy en JBoss/WebLogic), **Estadio 2** (Monolito Modular — estándar por defecto para todo proyecto nuevo) y **Estadio 3** (Microservicios Selectivos — excepción regulada por 6 criterios). La migración del Estadio 1 al 2 se realiza con el patrón Strangler Fig (`LIN-ARQ-001 §2.2`), que es una estrategia de transición, no un estadio. |
| **Port** | Interfaz que expresa una capacidad requerida o expuesta por el dominio o la aplicación. |
| **Adapter** | Implementación concreta que conecta puertos con infraestructura o sistemas externos (`LIN-DEV-JAVA-001 §8`). |
| **ACL (Anti-Corruption Layer)** | Capa o conjunto de mappers que traduce y aísla entre modelos externos/legacy y el modelo limpio de ONP (`LIN-DIS-001 §5.4`; obligatoriedad en interoperabilidad gubernamental: `LIN-ARQ-001 §4.3`). |
| **BFF (Backend for Frontend)** | Capa de agregación y adaptación dedicada a un canal de consumo (SPA, móvil) que compone y traduce respuestas de servicios internos hacia el formato que ese canal necesita (`LIN-DIS-001 §5.1`). |
| **CDC (Change Data Capture)** | Técnica que captura los cambios de datos ocurridos en un origen (típicamente logs de la BD, ej. Oracle LogMiner) y los propaga como eventos, sin requerir cambios en la aplicación origen (`LIN-DIS-001 §4.2`). |
| **Transaction Script** | Estrategia de organización de lógica de negocio donde cada operación se implementa como un procedimiento lineal en la capa de servicio, sin modelo de dominio propio (`LIN-DIS-001 §4.1`). |
| **Published Language** | Contrato de datos publicado y versionado que un productor expone a sus consumidores (CloudEvents/Schema Registry para eventos, OpenAPI para REST), de forma que ningún consumidor dependa de la estructura interna del productor (`LIN-ARQ-001 §4.1-4.2`, `LIN-BUS-001 §5.2`). |
| **Contrato API** | Acuerdo observable de una API: rutas, métodos, payloads, códigos, headers, semántica y restricciones (`LIN-API-REST-001`). |
| **Contrato de respuesta** | Estructura estándar de respuesta usada por los servicios, por ejemplo `ApiResponseWrapper`. |
| **Catálogo normativo** | Lista oficial de valores o reglas aprobadas por documento dueño. Puede existir con o sin persistencia física. |

---

## 5. Términos de observabilidad y operación

| Término | Definición ONP |
|---|---|
| **Observabilidad** | Capacidad de entender el comportamiento de un sistema a partir de logs, métricas, trazas y señales operativas correlacionadas. |
| **Log canónico** | Registro estructurado mínimo y consistente de una petición o evento relevante, con campos obligatorios definidos por el lineamiento dueño. |
| **Traza distribuida** | Representación del recorrido de una petición a través de componentes y servicios relacionados. |
| **Métrica** | Medición cuantitativa de comportamiento o estado del sistema, expuesta para monitoreo. |
| **`X-Request-ID`** | Identificador de correlación de petición visible en headers, logs y metadatos de respuesta. |
| **`requestId`** | Valor de correlación expuesto en el contrato de respuesta y alineado al `X-Request-ID`. |
| **`traceId`** | Identificador de la traza distribuida. |
| **`spanId`** | Identificador de una unidad de trabajo dentro de la traza. |
| **Four Golden Signals** | Las 4 métricas doradas de Google SRE de monitoreo mandatorio para producción: Latencia, Tráfico, Errores y Saturación (`LIN-ARQ-001 §5.3`). |
| **Override operativo** | Valor inyectado por plataforma u operación para ajustar comportamiento por ambiente sin modificar la configuración base versionada. |
| **Health check** | Señal técnica para determinar si una aplicación está viva, lista o en condiciones de recibir tráfico (`Liveness/Readiness Probes`). |

---

## 6. Términos de seguridad

| Término | Definición ONP |
|---|---|
| **SAA** | Sistema de Administración de Accesos vigente en el modelo actual de seguridad institucional. |
| **WSO2** | Plataforma objetivo de API Management y seguridad para el modelo futuro de OAuth2/OIDC, sujeta a transición institucional. |
| **Autenticación** | Proceso de verificar quién es el actor que invoca el sistema. |
| **Autorización** | Proceso de verificar qué operación puede ejecutar un actor ya autenticado. |
| **Secreto** | Dato sensible que no debe exponerse en código o repositorio: contraseñas, tokens, claves, certificados, etc. |
| **K8s Secret** | Mecanismo de Kubernetes para inyectar o gestionar secretos en despliegues. |
| **Datos sensibles** | Información protegida por regulación, riesgo operativo o política institucional, como PII, credenciales o material criptográfico. |
| **No PII** | Regla de no registrar datos personales identificables en logs, trazas o métricas. |

---

## 7. Términos de base de datos

| Término | Definición ONP |
|---|---|
| **Migración versionada** | Cambio de BD versionado, trazable y mantenido en repositorio bajo convención institucional. |
| **Script de reversa** | Script que deshace o compensa un cambio cuando es técnicamente viable. |
| **Script compensatorio** | Script diseñado para restaurar estado lógico cuando un rollback transaccional no aplica, especialmente en DDL Oracle. |
| **Reversa** | Estrategia completa para revertir o compensar un cambio de BD, despliegue o configuración. No siempre equivale a `ROLLBACK`. |
| **PL/SQL legacy** | Lógica existente en packages, procedures o functions heredadas, con comportamiento sensible o dependencias operativas que requieren gobierno diferenciado. |
| **Prueba de caracterización** | Prueba que captura comportamiento actual de un componente legacy antes de modificarlo. |
| **Cambio mixto** | Cambio que combina elementos versionados nuevos y componentes legacy/manuales en un mismo pase. |

---

## 8. Términos de versionamiento y colaboración

| Término | Definición ONP |
|---|---|
| **Merge Request (MR)** | Mecanismo obligatorio de integración y revisión de cambios en GitLab para ramas protegidas. |
| **Branching model** | Estrategia de ramas adoptada por un proyecto para desarrollar, integrar y promover cambios. |
| **GitLab Flow simplificado** | Modelo objetivo para proyectos nuevos: `main` único, ramas cortas, revisión por MR y promoción del mismo artefacto. |
| **Ramas por promoción** | Modelo vigente en parte del legacy institucional donde las ramas representan estados o ambientes de avance. |
| **Tag semántico** | Marca versionada con formato `MAJOR.MINOR.PATCH` para releases aprobados. |
| **Template de proyecto** | Repositorio o estructura base reutilizable para iniciar un componente con convenciones institucionales. |

---

## 9. Regla de mantenimiento

Cuando un término nuevo aparezca repetidamente en dos o más lineamientos y tenga riesgo de interpretaciones distintas, debe agregarse aquí o referenciar un glosario especializado compatible.

Si un término definido aquí entra en conflicto con una definición ya aprobada en un lineamiento dueño, prevalece el documento dueño y el glosario debe actualizarse en la siguiente revisión.
