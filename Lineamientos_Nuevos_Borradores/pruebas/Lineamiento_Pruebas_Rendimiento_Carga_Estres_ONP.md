# LIN-PERF-001 — Lineamiento de Pruebas de Rendimiento, Carga y Estrés ONP

**Código:** LIN-PERF-001  
**Versión:** v0.1.5  
**Estado:** En revisión  
**Fecha:** 2026-08-17  
**Propietario documental:** Arquitectura de Software — OTI  
**Revisores sugeridos:** Desarrollo, QA, Plataforma/Infraestructura, Seguridad Digital, Arquitectura  
**Marco rector:** LIN-ARQ-001 — Marco Rector de Arquitectura de Software  
**Herramienta preferente:** JMeter  

---

## Control de cambios

| Versión | Fecha | Autor | Descripción |
|---|---|---|---|
| v0.1.0 | 2026-05-28 | Arquitectura OTI | Borrador inicial del lineamiento de pruebas de rendimiento, carga y estrés |
| v0.1.1 | 2026-07-06 | Arquitectura OTI | §12.1 separa el ambiente `PQA` (etapa de rama legado de `LIN-VER-001 §5`, sin clúster propio) del ambiente `Preproducción`/`UAT` (ambiente real opcional, requiere ADR según `LIN-K8S-001 §4.4`) — antes se listaban como si fueran equivalentes. Actualiza §13 (evidencia mínima del informe) para reflejar el mismo cambio |
| v0.1.2 | 2026-07-10 | Arquitectura OTI | Migra Marco rector de `LIN-ARQ-000` (congelado) a `LIN-ARQ-001` (vigente) |
| v0.1.3 | 2026-08-17 | Arquitectura OTI | Revisión de fondo (`GOB-CHK-001` H30). **(1) `§8.3` era una tercera lista de Core Web Vitals**, sin umbrales y bajo la fórmula «cuando aplique», frente al gate **mandatorio** de `LIN-ARQ-001 §7.2`. Remite ahora al dueño y conserva lo que sí le es propio: medir el frontend **con el backend bajo carga**, que es distinto del Lighthouse de laboratorio del gate. **(2) `§11` admitía datos personales reales «con autorización»** y regulaba solo el contenido de los scripts, dejando fuera el riesgo real: restaurar un respaldo de producción en un ambiente de pruebas, que traslada el padrón de afiliados —DNI, nombres, montos, historial de aportes— a un entorno con controles más débiles. Se exige **enmascaramiento previo e irreversible**, con tabla de tratamiento por tipo de dato. **(3)** `§2` no listaba `LIN-DIS-001`, `LIN-BUS-001`, `GOB-MAT-001` ni la Ley N.° 29733. El documento pasa a **En revisión** |
| v0.1.4 | 2026-08-17 | Arquitectura OTI | `§6.4` normaba cómo determinar la criticidad de un sistema, pero la escala Alta/Media/Baja se usaba sin dueño en `LIN-CICD-001` y `LIN-K8S-001`. Se declara a `LIN-ARQ-001 §5.4.1` fuente institucional de las bandas; este lineamiento conserva los criterios de determinación (`GOB-CHK-001` H11.2) |
| v0.1.5 | 2026-08-18 | Arquitectura OTI | El apartado de excepción titulaba «Proceso ADR para excepciones» y no definía identificador: una desviación de este lineamiento se registraba como «un ADR», instrumento que `GOB-MAT-001` reserva a las decisiones **institucionales** del Comité. Pasa a **`EXC-PERF-NNN`**, con vigencia acotada y fecha de revisión obligatoria (`GOB-CHK-001` H38) |

---

## Tabla de contenido

1. [Objetivo y alcance](#1-objetivo-y-alcance)  
2. [Normativa y documentos relacionados](#2-normativa-y-documentos-relacionados)  
3. [Principios rectores](#3-principios-rectores)  
4. [Relación con requisitos no funcionales](#4-relación-con-requisitos-no-funcionales)  
5. [Tipos de pruebas de rendimiento](#5-tipos-de-pruebas-de-rendimiento)  
6. [Criterios de aplicabilidad](#6-criterios-de-aplicabilidad)  
7. [Herramientas permitidas](#7-herramientas-permitidas)  
8. [Métricas obligatorias](#8-métricas-obligatorias)  
9. [Criterios de aceptación y umbrales](#9-criterios-de-aceptación-y-umbrales)  
10. [Diseño de escenarios de prueba](#10-diseño-de-escenarios-de-prueba)  
11. [Datos de prueba](#11-datos-de-prueba)  
12. [Ambientes de ejecución](#12-ambientes-de-ejecución)  
13. [Evidencia mínima del informe de performance](#13-evidencia-mínima-del-informe-de-performance)  
14. [Relación con observabilidad](#14-relación-con-observabilidad)  
15. [Relación con base de datos e integraciones](#15-relación-con-base-de-datos-e-integraciones)  
16. [Relación con CI/CD](#16-relación-con-cicd)  
17. [Responsabilidades](#17-responsabilidades)  
18. [Checklist de conformidad](#18-checklist-de-conformidad)  
19. [Anti-patrones](#19-anti-patrones)  
20. [Proceso ADR para excepciones](#20-proceso-adr-para-excepciones)  
21. [Glosario](#21-glosario)  
22. [Anexos](#22-anexos)  

---

## 1. Objetivo y alcance

### 1.1 Objetivo

Este lineamiento establece las reglas mínimas para definir, diseñar, ejecutar, analizar y aceptar pruebas de rendimiento, carga, estrés, concurrencia, volumen y resistencia sobre aplicaciones, APIs, servicios backend, integraciones, procesos batch, workers y componentes críticos desarrollados o mantenidos por la ONP.

Su propósito es asegurar que las soluciones cumplan requisitos no funcionales medibles de rendimiento antes de su pase a QA, producción o ante cambios relevantes que puedan afectar tiempos de respuesta, throughput, estabilidad, concurrencia o consumo de recursos.

Este lineamiento no se limita al uso de una herramienta. Define la estrategia institucional para validar rendimiento. JMeter se adopta como herramienta preferente, pero los criterios principales son los RNF, los escenarios, las métricas y la evidencia de cumplimiento.

### 1.2 Alcance

Aplica a:

| Componente | Aplica | Observación |
|---|---:|---|
| APIs REST backend | Sí | Especialmente endpoints críticos o de alto consumo |
| Servicios Java / Spring Boot | Sí | Incluye APIs, adapters, workers y jobs |
| Frontend Angular | Parcial | Métricas web, tiempos percibidos y flujos críticos |
| Integraciones con SAA | Sí | Cuando afecten autenticación, autorización o latencia de operación |
| Integraciones con Oracle | Sí | Incluye consultas críticas y uso de conexiones |
| Integraciones con servicios externos | Sí | RENIEC, SUNAT, SBS u otros, cuando aplique |
| Procedures, packages o functions PL/SQL críticos | Sí | Medidos desde adapter Java o ambiente controlado |
| Jobs batch o procesos masivos | Sí | Validar duración, throughput y volumen |
| Workers o procesos asíncronos | Sí | Validar capacidad de procesamiento y colas |
| Aplicaciones contenerizadas en K8s | Sí | Validar consumo, límites, réplicas y saturación |
| Sistemas legacy intervenidos | Sí | Cuando el cambio pueda degradar rendimiento |
| Herramientas de plataforma | No | Salvo evaluación específica de Plataforma |

### 1.3 Fuera de alcance

| Tema | Documento / responsable |
|---|---|
| Pruebas unitarias, integración, contrato, E2E y caracterización funcional | LIN-TEST-001 |
| Ejecución automática en pipeline | LIN-CICD-001 |
| Diseño de infraestructura Kubernetes | LIN-K8S-001 |
| Observabilidad base: logs, trazas, métricas y health checks | LIN-OBS-001 |
| Seguridad, DAST, SAST, SCA y vulnerabilidades | LIN-SEC-APP-001 |
| Estándares de SQL, PL/SQL y optimización de BD | LIN-BD-ORA-001 |
| Dimensionamiento final de infraestructura | Plataforma / Infraestructura / Arquitectura |
| Pruebas de usabilidad | UX / Diseño funcional |

---

## 2. Normativa y documentos relacionados

| Documento | Código | Relación |
|---|---|---|
| Marco Rector de Arquitectura de Software | LIN-ARQ-001 | Define la necesidad de RNF medibles y criterios de arquitectura |
| Estándar de Pruebas | LIN-TEST-001 | Excluye performance y remite a este lineamiento |
| Estándar de Desarrollo Java | LIN-DEV-JAVA-001 | Define stack backend y pruebas funcionales Java |
| Estándar de APIs REST | LIN-API-REST-001 | Define contratos, endpoints y criterios REST |
| Estándar de Base de Datos Oracle | LIN-BD-ORA-001 | Define criterios de BD, PL/SQL y optimización |
| Estándar de Frontend Angular | LIN-FE-ANG-001 | Define frontend y métricas web aplicables |
| Log, Trazabilidad y Observabilidad | LIN-OBS-001 | Provee métricas, trazas y logs para analizar resultados |
| Seguridad en Aplicaciones | LIN-SEC-APP-001 | Complementa con controles de seguridad |
| Contenedores y Orquestación | LIN-K8S-001 | Define recursos, límites, réplicas, health checks y despliegue |
| Versionamiento y Control de Cambios | LIN-VER-001 | Define MR, tags, releases y trazabilidad de cambios |
| Integración y Entrega Continua | LIN-CICD-001 | Ejecutará y publicará reportes de performance cuando corresponda |
| Lineamiento de Diseño de Software | LIN-DIS-001 | `§6.1` — matriz de timeouts por criticidad, referencia para interpretar la latencia observada |
| Lineamiento de Mensajería y Bus de Eventos | LIN-BUS-001 | Consumer lag y throughput del broker en escenarios asíncronos |
| Matriz de Propiedad Documental | GOB-MAT-001 | Determina qué documento es dueño de cada tema |
| Ley N.° 29733 — Protección de Datos Personales | — | Marco legal aplicable a los datos usados en pruebas (§11) |

---

## 3. Principios rectores

| # | Principio | Descripción |
|---|---|---|
| P1 | **RNF antes que herramienta** | Una prueba de rendimiento parte de requisitos no funcionales medibles; la herramienta solo ejecuta y mide. |
| P2 | **Medir comportamiento observable** | Se mide la experiencia del usuario, consumidor o proceso: latencia, errores, throughput y estabilidad. |
| P3 | **Escenarios representativos** | La prueba debe representar operaciones reales, datos realistas y carga esperada. |
| P4 | **p95 como métrica principal** | El promedio no es suficiente; el p95 será la métrica base para evaluar experiencia bajo carga. |
| P5 | **Trazabilidad del resultado** | Todo informe debe indicar versión, ambiente, fecha, escenario, carga, datos, métricas y conclusión. |
| P6 | **No impactar producción sin autorización** | Las pruebas de carga o estrés en producción están prohibidas salvo autorización expresa. |
| P7 | **Performance por criticidad** | No todo cambio requiere prueba de rendimiento completa; se exige según criticidad, RNF o riesgo técnico. |
| P8 | **Observabilidad obligatoria** | Una prueba sin métricas de aplicación, infraestructura o BD genera evidencia incompleta. |
| P9 | **Comparabilidad histórica** | Los resultados deben permitir comparar versiones y detectar degradaciones. |
| P10 | **Automatización progresiva** | La ejecución podrá incorporarse gradualmente a CI/CD, sin que este documento redefina pipelines. |

---

## 4. Relación con requisitos no funcionales

### 4.1 Regla principal

Toda prueba de rendimiento debe partir de requisitos no funcionales medibles. No se aprueba una prueba cuyo objetivo sea solamente “ver si aguanta” sin umbrales definidos.

Cada operación crítica debe declarar, como mínimo:

| RNF | Descripción | Ejemplo |
|---|---|---|
| Usuarios concurrentes esperados | Número de usuarios o procesos simultáneos | 300 usuarios concurrentes |
| Throughput esperado | Transacciones por segundo/minuto | 50 TPS |
| Tiempo máximo p95 | Tiempo de respuesta máximo para el 95% de peticiones | p95 ≤ 2 s |
| Tiempo máximo p99 | Tiempo de respuesta máximo para el 99% de peticiones | p99 ≤ 5 s |
| Tasa máxima de error | Porcentaje de errores permitido | < 1% |
| Duración mínima | Tiempo de ejecución de la prueba | 30 min |
| Volumen de datos | Tamaño o cantidad de registros representativos | 10 millones de registros |
| Consumo máximo esperado | CPU/memoria/conexiones aceptables | CPU < 75% sostenido |

### 4.2 Plantilla mínima de RNF de rendimiento

```text
Operación crítica:
Sistema / componente:
Endpoint / proceso:
Usuarios concurrentes esperados:
Throughput esperado:
p95 máximo:
p99 máximo:
Tasa máxima de error:
Duración de la prueba:
Volumen de datos:
Integraciones involucradas:
Ambiente objetivo:
Observaciones:
```

### 4.3 Ausencia de RNF

Cuando un proyecto no cuente con RNF definidos, Arquitectura, Desarrollo, QA y el área usuaria deberán establecer umbrales preliminares antes de ejecutar pruebas de rendimiento.

Si no es posible establecer umbrales completos, como mínimo se debe ejecutar una prueba base para obtener una línea de referencia.

---

## 5. Tipos de pruebas de rendimiento

| Tipo | Objetivo | Cuándo aplica |
|---|---|---|
| Smoke performance | Validar rápidamente que el sistema responde dentro de rangos básicos | Antes de pruebas mayores o después de despliegue |
| Carga | Validar comportamiento bajo carga esperada | Sistemas con usuarios concurrentes o consumo relevante |
| Estrés | Identificar límite del sistema por encima de carga esperada | Sistemas críticos o con alta concurrencia |
| Resistencia / soak | Validar estabilidad por periodo prolongado | Procesos críticos o servicios 24x7 |
| Spike | Validar respuesta ante picos bruscos | Campañas, cierres, trámites masivos, alta demanda |
| Volumen | Validar comportamiento con grandes datos | Consultas, batch, reportes, históricos |
| Concurrencia | Validar acceso simultáneo a recursos compartidos | Operaciones con BD, sesiones, bloqueos o colas |
| Degradación / regresión | Comparar versión nueva contra línea base | Antes de producción o ante cambios críticos |

### 5.1 Smoke performance

Prueba breve de bajo costo para confirmar que el sistema no presenta degradaciones evidentes.

```text
Duración: 5 a 10 minutos
Carga: baja o moderada
Objetivo: validar disponibilidad, latencia básica y error rate
Uso: despliegue a QA, release candidate, verificación post-cambio
```

### 5.2 Prueba de carga

Valida si el sistema soporta la carga esperada.

```text
Duración: 30 a 60 minutos
Carga: usuarios concurrentes esperados
Objetivo: validar p95, p99, TPS y error rate definidos en RNF
```

### 5.3 Prueba de estrés

Busca el límite del sistema.

```text
Carga: superior a la esperada
Objetivo: identificar punto de saturación y comportamiento de degradación
Resultado esperado: el sistema degrada controladamente, no falla caóticamente
```

### 5.4 Prueba de resistencia

Valida estabilidad sostenida.

```text
Duración: 2 a 8 horas o según criticidad
Objetivo: detectar fugas de memoria, agotamiento de conexiones, degradación progresiva
```

---

## 6. Criterios de aplicabilidad

### 6.1 Cuándo es obligatorio

| Caso | Prueba de rendimiento |
|---|---|
| Sistema crítico institucional | Obligatoria |
| API de alto consumo | Obligatoria |
| Sistema con alta concurrencia | Obligatoria |
| Sistema con RNF de rendimiento definidos | Obligatoria |
| Cambio en consulta pesada o endpoint crítico | Obligatoria |
| Cambio en procedure, package o function PL/SQL crítico | Obligatoria |
| Cambio en integración SAA, Oracle o servicio externo crítico | Obligatoria |
| Job batch o proceso masivo | Obligatoria |
| Cambio de arquitectura de despliegue | Obligatoria |
| Cambio de recursos K8s, réplicas, límites o conexión BD | Recomendada / obligatoria según impacto |

### 6.2 Cuándo es recomendada

| Caso | Prueba de rendimiento |
|---|---|
| Frontend interno de bajo uso | Recomendada |
| Servicio interno con uso moderado | Recomendada |
| Cambio técnico interno con posible impacto de rendimiento | Recomendada |
| Refactorización de componente con consumo relevante | Recomendada |
| Cambio de librería o driver crítico | Recomendada |

### 6.3 Cuándo no aplica

| Caso | Prueba de rendimiento |
|---|---|
| Cambio documental | No aplica |
| Cambio visual menor sin impacto de integración | No aplica |
| Corrección menor sin impacto en lógica ni consultas | No aplica |
| Ajuste de texto, etiquetas o mensajes | No aplica |
| Legacy sin intervención funcional | No aplica, salvo evaluación |

### 6.4 Determinación de criticidad

> **La escala de criticidad y sus bandas son institucionales: `LIN-ARQ-001 §5.4.1`.** Este lineamiento norma **cómo se determina** la criticidad de un sistema; el marco rector define las tres bandas —Alta, Media, Baja— y qué exigencias de continuidad conlleva cada una. La criticidad se asigna una vez y rige transversalmente: pruebas de rendimiento, resiliencia, fases de CI/CD, recursos de despliegue y objetivos de recuperación.

La criticidad se determina considerando:

- impacto institucional;
- cantidad de usuarios;
- exposición externa;
- datos personales o sensibles;
- frecuencia de uso;
- dependencia de otros sistemas;
- impacto en procesos misionales;
- historial de incidentes;
- complejidad de consultas o integraciones;
- exigencia de disponibilidad.

---

## 7. Herramientas permitidas

### 7.1 Herramienta preferente: JMeter

La ONP adopta **JMeter** como herramienta preferente para pruebas de rendimiento, carga, estrés, concurrencia y resistencia, especialmente para:

- APIs REST;
- servicios SOAP legacy;
- integraciones JDBC/Oracle;
- escenarios con múltiples protocolos;
- pruebas institucionales donde se requiera revisión visual y trazabilidad;
- pruebas ejecutadas por QA o equipos mixtos.

### 7.2 Herramientas alternativas permitidas

Además de JMeter, se permiten las siguientes herramientas:

| Herramienta | Uso recomendado |
|---|---|
| **k6** | Pruebas como código, APIs modernas, ejecución en CI/CD, integración futura con Grafana |
| **Gatling** | Pruebas como código para equipos Java/Kotlin/Scala, escenarios versionables y mantenibles |

### 7.3 Uso de herramientas distintas

El uso de herramientas distintas a JMeter, k6 o Gatling requiere justificación técnica mediante ADR o aprobación de Arquitectura/QA, según criticidad.

Toda herramienta alternativa debe generar evidencia equivalente:

- escenarios versionados;
- usuarios concurrentes;
- ramp-up;
- duración;
- throughput;
- tiempos promedio;
- p95;
- p99;
- tasa de error;
- reporte reproducible;
- configuración de ambiente;
- versión del sistema probado.

### 7.4 Criterio institucional

```text
JMeter = herramienta preferente.
k6 = alternativa para APIs modernas y CI/CD.
Gatling = alternativa para equipos Java y pruebas como código.
Otras herramientas = requieren justificación.
```

---

## 8. Métricas obligatorias

### 8.1 Métricas de prueba

| Métrica | Descripción | Obligatoria |
|---|---|---|
| Usuarios concurrentes | Usuarios o procesos simultáneos simulados | Sí |
| Ramp-up | Tiempo para alcanzar la carga objetivo | Sí |
| Duración | Tiempo total de prueba | Sí |
| Throughput / TPS | Transacciones por segundo/minuto | Sí |
| Tiempo promedio | Promedio de tiempo de respuesta | Sí |
| p95 | Percentil 95 de tiempo de respuesta | Sí |
| p99 | Percentil 99 de tiempo de respuesta | Sí para críticos |
| Error rate | Porcentaje de errores | Sí |
| Latencia por endpoint | Tiempo por operación crítica | Sí |
| Máximo observado | Peor tiempo de respuesta | Recomendado |
| Apdex | Índice de satisfacción de respuesta | Opcional |

### 8.2 Métricas de aplicación e infraestructura

| Métrica | Descripción | Obligatoria |
|---|---|---|
| CPU | Uso de CPU de aplicación/pod/nodo | Sí |
| Memoria | Uso de memoria y tendencia | Sí |
| Reinicios de pod | Reinicios durante prueba | Sí si aplica K8s |
| Saturación de pod | CPU/memoria cerca de límites | Sí si aplica K8s |
| Réplicas | Número de pods activos | Sí si aplica K8s |
| Pool de conexiones | Uso de conexiones BD | Sí si aplica BD |
| Tiempo de consultas | Consultas o procedures críticos | Sí si aplica BD |
| Tiempo de integraciones | SAA, Oracle, servicios externos | Sí si aplica |
| Colas pendientes | Backlog o lag de workers | Sí si aplica |

### 8.3 Métricas frontend

> **Documento dueño: `ARQ-R-007` (LIN-ARQ-001 §7.2).** Los umbrales de Core Web Vitals y de las métricas Lighthouse complementarias —LCP, INP, CLS, FCP, TTI, TBT y FPS— son **gate de bloqueo mandatorio** para la promoción de un build de frontend a producción. Este lineamiento **no publica esos valores** para no convertirse en una fuente paralela, y no puede relajarlos: no son «cuando aplique».

Además de los umbrales del marco rector, una prueba de performance de un frontend mide:

| Métrica | Uso |
|---|---|
| Tiempo de carga inicial | Carga de la SPA bajo la carga del escenario, no en condiciones de laboratorio |
| Errores de red | Fallos HTTP en flujos críticos durante la prueba |
| Tiempo total de flujo | Duración de la operación de usuario extremo a extremo |

> **Qué aporta esta prueba frente a Lighthouse.** El gate de `ARQ-R-007` (LIN-ARQ-001 §7.2) se mide con Lighthouse CI sobre un build aislado (`LIN-CICD-001 §9.4`). Lo que aquí se mide es distinto y complementario: cómo se comporta el frontend **cuando el backend está bajo la carga del escenario**. Un LCP conforme en laboratorio puede degradarse cuando la API tarda cinco veces más por saturación.

---

## 9. Criterios de aceptación y umbrales

### 9.1 Regla general

Los umbrales no deben ser universales para todos los sistemas. Deben definirse por sistema, operación crítica, criticidad y RNF.

Sin embargo, todo escenario debe contar con criterios explícitos de aceptación antes de ejecutarse.

### 9.2 Umbrales mínimos a definir

Todo escenario crítico debe definir:

| Criterio | Ejemplo |
|---|---|
| p95 máximo | p95 ≤ 2 s |
| p99 máximo | p99 ≤ 5 s |
| Error rate máximo | < 1% |
| Throughput mínimo | ≥ 50 TPS |
| Usuarios concurrentes | 300 |
| Duración mínima | 30 min |
| CPU/memoria aceptable | CPU sostenido < 75% |
| Pool BD sin agotamiento | Uso < 80% |
| Sin reinicios de pod | 0 reinicios durante prueba |

### 9.3 Criterios por criticidad

| Criticidad | Exigencia mínima |
|---|---|
| Alta | RNF obligatorio, p95/p99, error rate, carga, estrés o resistencia según caso |
| Media | RNF mínimo, p95, error rate y prueba de carga básica |
| Baja | Smoke performance o línea base justificada |

### 9.4 Resultado de evaluación

Todo informe debe concluir con uno de estos estados:

| Estado | Descripción |
|---|---|
| Cumple | Todos los criterios fueron alcanzados |
| Cumple con observaciones | Hay desviaciones menores o riesgos controlados |
| No cumple | Uno o más criterios críticos no fueron alcanzados |
| No concluyente | La prueba no es válida por ambiente, datos, errores de configuración o evidencia insuficiente |

---

## 10. Diseño de escenarios de prueba

### 10.1 Elementos mínimos del escenario

Cada escenario debe documentar:

- nombre del escenario;
- objetivo;
- operación o flujo probado;
- endpoint, proceso o job;
- datos de entrada;
- usuarios concurrentes;
- ramp-up;
- duración;
- think time, si aplica;
- tasa esperada de transacciones;
- criterios de aceptación;
- dependencias;
- ambiente;
- versión del sistema;
- riesgos o limitaciones.

### 10.2 Escenarios por tipo de componente

| Componente | Escenarios mínimos |
|---|---|
| API REST | Operaciones críticas, errores esperados, autenticación/autorización si aplica |
| Backend Java | Casos de uso críticos y adapters externos |
| Frontend Angular | Flujos de usuario críticos y carga inicial |
| PL/SQL crítico | Ejecución vía adapter o prueba controlada |
| Job batch | Tiempo total, volumen procesado, errores y reintentos |
| Worker | Throughput, backlog, retry, DLQ si aplica |
| Integración externa | Latencia, timeouts, error rate, degradación controlada |

### 10.3 Pruebas con dependencias externas

Cuando una dependencia externa no pueda ser sometida a carga, se debe:

- usar mocks o simuladores controlados;
- acordar ventana de prueba con el proveedor interno/externo;
- limitar la carga para no afectar terceros;
- documentar la limitación del escenario;
- separar tiempo propio del sistema y tiempo de dependencia.

### 10.4 Autenticación y sesiones

Los escenarios deben considerar el modelo real de autenticación cuando aplique, incluyendo:

- token SAA;
- OAuth2/OIDC cuando corresponda;
- expiración de sesión;
- permisos;
- usuarios de prueba controlados;
- no uso de credenciales reales en scripts.

---

## 11. Datos de prueba

### 11.1 Principios

Los datos deben ser representativos y seguros.

Reglas:

- **no usar datos personales reales**: se usan datos sintéticos o anonimizados, sin excepción por conveniencia. Ley N.° 29733 de Protección de Datos Personales;
- preferir datos sintéticos sobre anonimizados cuando el escenario lo permita;
- documentar volumen y distribución de datos;
- evitar que todos los usuarios consulten el mismo registro si eso distorsiona la prueba;
- asegurar que los datos permitan repetir la prueba;
- evitar scripts que alteren producción sin autorización.

### 11.2 Datos para pruebas de volumen

Para pruebas de volumen se debe documentar:

- cantidad de registros;
- antigüedad de datos;
- distribución;
- índices existentes;
- particiones si aplica;
- tamaño de tablas;
- cardinalidad de filtros;
- supuestos relevantes.

### 11.3 Datos sensibles

#### El riesgo principal no está en el script, está en la base de datos

Una prueba de volumen necesita datos realistas, y el atajo habitual es **restaurar un respaldo de producción en el ambiente de pruebas**. Para una entidad previsional eso traslada a un ambiente con controles más débiles el padrón de afiliados completo: DNI, nombres, montos de pensión e historial de aportes. Es la vía por la que se materializa una fuga de datos personales, y ningún control sobre el contenido del script la detiene.

**Regla:** restaurar datos productivos en DEV, QA o cualquier ambiente no productivo requiere **enmascaramiento previo e irreversible** de los datos personales, ejecutado antes de que el respaldo sea accesible desde el ambiente destino. La autorización de Seguridad de la Información no sustituye al enmascaramiento: lo condiciona.

| Dato | Tratamiento obligatorio antes de exponerlo en un ambiente no productivo |
|---|---|
| DNI | Sustitución por identificador sintético con el mismo formato y dígito verificador válido |
| Nombres y apellidos | Sustitución por datos sintéticos |
| Dirección, teléfono, correo | Sustitución por datos sintéticos |
| Montos de pensión y aportes | Se conservan si el escenario lo exige, **desligados** de la identidad real |
| Datos de salud y bancarios | Sustitución o exclusión |

> **Alcance de esta sección.** Aquí se norma el tratamiento de datos para **pruebas de rendimiento**, que es donde el volumen hace tentador el atajo. Una política transversal de datos de prueba y enmascaramiento para todos los ambientes no productivos sigue **pendiente** en el corpus (`GOB-CHK-001` H11.3) y corresponde a `LIN-SEC-APP-001` como dueño de la protección de datos.

#### Contenido de los scripts

Los scripts de prueba no deben contener:

- DNI reales;
- nombres completos reales;
- contraseñas;
- tokens;
- claves;
- certificados;
- datos personales sensibles;
- cadenas de conexión productivas.

---

## 12. Ambientes de ejecución

### 12.1 Ambientes permitidos

| Ambiente | Uso |
|---|---|
| DEV | Pruebas exploratorias o smoke performance |
| QA | Pruebas formales de carga y aceptación |
| Preproducción / UAT (si existe — ver `LIN-K8S-001 §4.4`) | Pruebas cercanas a producción. Es, de hecho, el detonador de ADR más frecuente para justificar la adopción de este ambiente opcional |
| Producción | Solo con autorización expresa y alcance controlado |

> **Nota sobre `PQA`:** el ambiente `PQA` del modelo vigente de ramas de `LIN-VER-001 §5` (`ONP_DESA → ONP_PQA → ONP_QA → master`) es una etapa de precalidad/estabilización de código, **no un ambiente de despliegue independiente con clúster propio**. No debe confundirse con Preproducción/UAT. Las pruebas de rendimiento durante la etapa `PQA` se ejecutan en el ambiente real donde esa rama despliega (típicamente DEV o QA), no en un ambiente separado para efectos de esta norma.

### 12.2 Condiciones mínimas del ambiente

Para que una prueba sea válida, el ambiente debe documentar:

- versión desplegada;
- recursos asignados;
- número de réplicas;
- configuración relevante;
- base de datos usada;
- volumen de datos;
- dependencias externas;
- restricciones del ambiente;
- diferencias frente a producción.

### 12.3 Producción

Las pruebas de carga, estrés, spike o resistencia en producción están prohibidas salvo autorización expresa.

Cuando se autoricen, deben contar con:

- ventana aprobada;
- alcance controlado;
- monitoreo activo;
- plan de reversión;
- comunicación a Operaciones;
- responsables identificados;
- criterio de interrupción.

---

## 13. Evidencia mínima del informe de performance

Todo informe debe incluir:

| Sección | Contenido |
|---|---|
| Resumen ejecutivo | Cumple / no cumple / observaciones |
| Objetivo | Qué se buscó validar |
| Sistema y versión | Componente, tag, commit o release |
| Ambiente | DEV, QA, Preproducción/UAT (si existe) |
| Fecha y hora | Momento de ejecución |
| Escenarios | Operaciones probadas |
| Configuración de carga | Usuarios, ramp-up, duración, throughput |
| Datos de prueba | Volumen, origen, anonimización |
| Resultados | promedio, p95, p99, TPS, error rate |
| Recursos | CPU, memoria, pods, conexiones, BD |
| Integraciones | tiempos de SAA, Oracle, externos |
| Hallazgos | cuellos de botella, errores, saturación |
| Evidencias | reportes JMeter/k6/Gatling, dashboards, logs |
| Conclusión | estado final |
| Recomendaciones | acciones técnicas |
| Anexos | scripts, capturas, configuración |

### 13.1 Evidencias aceptadas

- reporte HTML de JMeter;
- dashboard exportado o captura de Grafana;
- resultados de k6;
- reportes de Gatling;
- logs relevantes;
- trazas de Jaeger;
- métricas Prometheus;
- evidencia de BD;
- archivo de escenario versionado;
- resumen de ejecución.

### 13.2 Trazabilidad

El informe debe asociarse a:

- requerimiento;
- incidencia;
- release;
- Merge Request;
- tag;
- versión de imagen;
- ADR si aplica.

---

## 14. Relación con observabilidad

`LIN-PERF-001` consume las capacidades definidas por `LIN-OBS-001`.

Durante la prueba se debe observar:

- logs estructurados;
- trace.id;
- span.id;
- X-Request-ID;
- métricas Actuator/Prometheus;
- dashboards Grafana;
- trazas Jaeger;
- errores 4xx/5xx;
- saturación;
- latencia por dependencia.

### 14.1 Regla

Una prueba de rendimiento sin observabilidad mínima puede considerarse no concluyente, porque no permite explicar las causas del resultado.

### 14.2 Correlación

Los resultados de performance deben poder correlacionarse con:

- endpoint;
- operación;
- usuario de prueba o grupo lógico;
- trace.id;
- request.id;
- timestamp;
- error;
- dependencia externa.

---

## 15. Relación con base de datos e integraciones

### 15.1 Base de datos Oracle

Cuando el escenario involucre Oracle, se debe revisar:

- tiempo de consultas;
- uso de índices;
- plan de ejecución cuando aplique;
- uso de pool de conexiones;
- bloqueos;
- waits relevantes;
- sesiones activas;
- consumo de CPU en BD;
- comportamiento de procedures críticos.

### 15.2 PL/SQL crítico

Cuando se pruebe lógica PL/SQL crítica, se debe:

- identificar procedure, package o function;
- documentar parámetros;
- ejecutar con datos representativos;
- medir duración;
- registrar errores;
- comparar contra línea base si existe;
- coordinar con DBA o responsable técnico cuando aplique.

### 15.3 Integraciones

Para integraciones con SAA, RENIEC, SUNAT, SBS u otros servicios externos, se debe:

- medir latencia propia y latencia de dependencia;
- documentar timeouts;
- documentar errores;
- evitar afectar servicios externos con carga no autorizada;
- usar simuladores cuando corresponda;
- considerar degradación controlada.

---

## 16. Relación con CI/CD

`LIN-PERF-001` define qué medir, cómo diseñar escenarios, qué métricas usar, qué umbrales aceptar y qué evidencia entregar.

`LIN-CICD-001` define cuándo ejecutar estas pruebas en GitLab, cómo publicar reportes y cuándo aplicar gates automáticos o aprobaciones manuales.

### 16.1 Fases de automatización

| Fase CI/CD | Tratamiento de performance |
|---|---|
| Fase 0 | Evidencia manual adjunta al MR o expediente técnico |
| Fase 1–2 | No obligatorio en cada pipeline |
| Fase 3 | Smoke performance opcional/recomendado para críticos |
| Fase 4 | JMeter/k6/Gatling ejecutado por release candidate para críticos |
| Fase futura | Gate automático según umbrales definidos en este lineamiento |

### 16.2 Repositorio de scripts

Los scripts de performance deben versionarse en el repositorio del proyecto o en repositorio especializado.

Estructura sugerida:

```text
performance/
├── README.md
├── jmeter/
│   ├── escenarios/
│   ├── data/
│   └── reports/
├── k6/
│   └── scripts/
└── gatling/
    └── simulations/
```

---

## 17. Responsabilidades

| Rol | Responsabilidad |
|---|---|
| Desarrollo | Identificar operaciones críticas, preparar endpoints, corregir degradaciones |
| QA | Diseñar y ejecutar pruebas, preparar datos, consolidar evidencia |
| Arquitectura | Definir criterios por criticidad, revisar RNF, aprobar excepciones |
| Plataforma/Infraestructura | Proveer ambiente, monitoreo, recursos y métricas de infraestructura |
| Seguridad Digital | Validar uso seguro de datos, credenciales y ventanas cuando aplique |
| DBA / responsable BD | Apoyar análisis de consultas, bloqueos, pool y PL/SQL crítico |
| Área usuaria / funcional | Validar escenarios de negocio y volúmenes esperados |
| Proyecto / líder técnico | Asegurar que la evidencia forme parte del pase correspondiente |

---

## 18. Checklist de conformidad

### 18.1 Antes de ejecutar

```text
[ ] Se identificaron operaciones críticas
[ ] Existen RNF medibles o línea base preliminar
[ ] Se definieron usuarios concurrentes
[ ] Se definió ramp-up
[ ] Se definió duración
[ ] Se definieron p95, p99 y error rate esperados
[ ] Se prepararon datos representativos
[ ] Se validó que no se usan datos sensibles reales sin autorización
[ ] Se validó ambiente de prueba
[ ] Se coordinó con Plataforma/DBA si aplica
[ ] Se versionó el script de prueba
```

### 18.2 Durante la ejecución

```text
[ ] Se monitorea CPU y memoria
[ ] Se monitorean pods/réplicas si aplica
[ ] Se monitorea pool de conexiones
[ ] Se monitorean errores HTTP
[ ] Se monitorean integraciones externas
[ ] Se monitorean logs y trazas
[ ] Se registran hora de inicio y fin
[ ] Se detiene la prueba si supera criterios de riesgo
```

### 18.3 Después de ejecutar

```text
[ ] Se generó reporte de herramienta
[ ] Se consolidaron métricas promedio, p95, p99, TPS y error rate
[ ] Se anexaron dashboards o capturas
[ ] Se documentaron hallazgos
[ ] Se concluyó cumple / no cumple / cumple con observaciones / no concluyente
[ ] Se registraron recomendaciones
[ ] Se asoció evidencia a release, MR o expediente técnico
```

---

## 19. Anti-patrones

| Anti-patrón | Riesgo | Regla |
|---|---|---|
| Ejecutar JMeter sin RNF | No se puede decidir si cumple | Definir umbrales antes |
| Medir solo promedio | Oculta degradación para usuarios lentos | Usar p95/p99 |
| Probar con datos irreales | Resultado no representativo | Usar datos realistas y seguros |
| Cargar servicios externos sin autorización | Riesgo operativo/legal | Coordinar o simular |
| Ejecutar estrés en producción sin aprobación | Riesgo de incidente | Prohibido salvo autorización |
| No monitorear infraestructura | No se explica la causa del resultado | Usar observabilidad |
| No versionar scripts | Prueba no reproducible | Versionar en GitLab |
| Usar credenciales reales en scripts | Riesgo de seguridad | Usar secretos controlados |
| Hacer una sola prueba aislada | No hay comparabilidad | Mantener línea base |
| Aprobar con error rate alto | Riesgo productivo | Definir máximo permitido |
| Ignorar BD | Cuellos de botella no detectados | Incluir métricas Oracle cuando aplique |

---

## 20. Proceso de excepción (`EXC-PERF-NNN`)

> **Instrumento correcto: `EXC-PERF-NNN`, no un ADR.** Conforme a `GOB-MAT-001` (Registro de decisiones y excepciones), la desviación de un lineamiento **en un proyecto concreto** se registra como excepción con vigencia acotada y **fecha de revisión**, nunca indefinida. El `ADR-NNN` queda reservado a decisiones **institucionales** del Comité de Arquitectura, que obligan a todo el corpus; llevar allí cada desviación de cada sistema vaciaría de valor ese registro. La excepción se aprueba por Arquitectura OTI y se registra en el documento de arquitectura del sistema (`GOB-PLA-001`, Anexo E, criterio 14).


Toda excepción relevante requiere ADR aprobado por Arquitectura. Si la excepción afecta seguridad, datos sensibles, producción o infraestructura crítica, requiere además validación de Seguridad Digital y/o Plataforma.

### 20.1 Casos que requieren ADR

- No ejecutar prueba de performance en sistema crítico.
- Usar herramienta distinta a JMeter, k6 o Gatling.
- Ejecutar prueba de carga o estrés en producción.
- Aceptar pase con incumplimiento de RNF crítico.
- Usar datos reales sensibles.
- No contar con observabilidad mínima durante la prueba.
- No versionar scripts de prueba.
- No contar con línea base para sistema crítico.

### 20.2 Formato mínimo

```markdown
# ADR-PERF-NNN — [Título]

## Contexto
[Descripción de la restricción o excepción requerida]

## Decisión
[Qué se permitirá excepcionalmente]

## Riesgo aceptado
[Riesgo operativo, seguridad, rendimiento o arquitectura]

## Control compensatorio
[Medida temporal o alternativa]

## Fecha de revisión
[Fecha para reevaluar]

## Aprobaciones
[Arquitectura / QA / Plataforma / Seguridad, según corresponda]
```

---

## 21. Glosario

| Término | Definición |
|---|---|
| Performance | Capacidad del sistema para responder dentro de tiempos y recursos esperados |
| Prueba de carga | Validación bajo carga esperada |
| Prueba de estrés | Validación por encima de carga esperada para identificar límites |
| Soak test | Prueba prolongada para validar estabilidad |
| Spike test | Prueba ante picos bruscos de carga |
| Throughput | Transacciones procesadas por unidad de tiempo |
| TPS | Transacciones por segundo |
| p95 | Percentil 95; 95% de respuestas están por debajo de ese valor |
| p99 | Percentil 99; 99% de respuestas están por debajo de ese valor |
| Error rate | Porcentaje de errores sobre el total de peticiones |
| Ramp-up | Tiempo para alcanzar la carga objetivo |
| Think time | Pausa simulada entre acciones de usuario |
| Línea base | Resultado de referencia para comparar versiones futuras |
| JMeter | Herramienta preferente de pruebas de carga y rendimiento |
| k6 | Herramienta alternativa para pruebas como código y CI/CD |
| Gatling | Herramienta alternativa para equipos Java y pruebas como código |

---

## 22. Anexos

### Anexo A — Plantilla de escenario de performance

```markdown
# Escenario de performance

## Identificación
- Sistema:
- Componente:
- Versión:
- Release / tag:
- Ambiente:
- Fecha:

## Objetivo
[Qué se busca validar]

## Operación crítica
- Endpoint / proceso:
- Método HTTP:
- Dependencias:
- Datos de prueba:

## Configuración de carga
- Usuarios concurrentes:
- Ramp-up:
- Duración:
- Think time:
- Throughput esperado:

## Criterios de aceptación
- p95 máximo:
- p99 máximo:
- Error rate máximo:
- TPS mínimo:
- CPU/memoria aceptable:
- Pool BD aceptable:

## Observabilidad
- Dashboard:
- Logs:
- Trazas:
- Métricas BD:

## Resultado
- Promedio:
- p95:
- p99:
- TPS:
- Error rate:
- CPU:
- Memoria:
- Conclusión:
```

### Anexo B — Plantilla de informe de performance

```markdown
# Informe de performance

## 1. Resumen ejecutivo
[Cumple / no cumple / cumple con observaciones / no concluyente]

## 2. Objetivo de la prueba
[Descripción]

## 3. Alcance
[Componentes, endpoints, procesos]

## 4. Ambiente
[Ambiente, recursos, versión, datos]

## 5. Escenarios ejecutados
[Tabla de escenarios]

## 6. Configuración de carga
[Usuarios, ramp-up, duración, throughput]

## 7. Resultados
[Promedio, p95, p99, TPS, error rate]

## 8. Métricas de infraestructura
[CPU, memoria, pods, BD, conexiones]

## 9. Hallazgos
[Cuellos de botella, errores, saturación]

## 10. Conclusión
[Estado final]

## 11. Recomendaciones
[Acciones]

## 12. Evidencias
[Reportes, dashboards, logs, scripts]
```

### Anexo C — Estructura sugerida de carpeta performance

```text
performance/
├── README.md
├── jmeter/
│   ├── escenarios/
│   │   └── consulta-expediente.jmx
│   ├── data/
│   │   └── usuarios-prueba.csv
│   └── reports/
│       └── .gitkeep
├── k6/
│   └── scripts/
│       └── consulta-expediente.js
└── gatling/
    └── simulations/
        └── ConsultaExpedienteSimulation.java
```

### Anexo D — Ejemplo de línea base

```text
Sistema: PAST
Componente: API Solicitudes
Operación: Consulta de solicitud
Versión: 1.0.0
Ambiente: QA
Usuarios concurrentes: 100
Duración: 30 min
Throughput: 35 TPS
Promedio: 850 ms
p95: 1.8 s
p99: 3.2 s
Error rate: 0.2%
CPU promedio: 55%
Memoria promedio: 62%
Conclusión: Cumple
```
