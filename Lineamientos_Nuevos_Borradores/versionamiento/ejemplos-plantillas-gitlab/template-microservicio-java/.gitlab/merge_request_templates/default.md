<!--
EJEMPLO DE REFERENCIA — LIN-VER-001 Anexo F
Plantilla institucional ONP — Microservicio independiente Hexagonal (Estadio 3)
Plataforma/Infraestructura es responsable de mantener esta plantilla en el proyecto GitLab real.
-->

## Descripción del cambio
[Resumen breve del cambio]

## Referencia
- Requerimiento / Incidencia / Tarea:
- ADR asociado, si aplica:
- ¿Sigue vigente el cumplimiento de los 6 criterios de extracción a Microservicio (ARQ-R-001,
  LIN-ARQ-001 §2.1)? [Sí / No — si no, evaluar repliegue a Monolito Modular]

## Tipo de cambio
- [ ] Funcional
- [ ] Técnico / refactor
- [ ] API / OpenAPI
- [ ] Base de datos
- [ ] Seguridad
- [ ] Observabilidad
- [ ] Resiliencia (Circuit Breaker / Bulkhead / Timeout, LIN-DIS-001 §6)
- [ ] K8s / despliegue
- [ ] Documentación

## Evidencia de pruebas
- [ ] Pruebas unitarias ejecutadas
- [ ] Pruebas de arquitectura (ArchUnit) ejecutadas — pureza hexagonal domain/application
- [ ] Pruebas de integración ejecutadas
- [ ] Pruebas E2E ejecutadas, si aplica
- [ ] Pruebas de contrato ejecutadas, si aplica
- Resultado / enlace / adjunto:

## Impacto en API
- [ ] No aplica
- [ ] OpenAPI actualizado
- [ ] Cambio compatible
- [ ] Cambio incompatible — requiere ADR o versión mayor

## Impacto en base de datos
- [ ] No aplica
- [ ] Script de migración incluido
- [ ] Script de reversa o compensación incluido
- [ ] Prueba en ambiente controlado

## Impacto en resiliencia (LIN-DIS-001 §6)
- [ ] No aplica
- [ ] Se agregó/modificó un punto de salida externo — ¿tiene Circuit Breaker + Bulkhead +
      Timeout configurados? (obligatorio en Microservicio, DIS-R-009)
- [ ] Se ajustaron umbrales de Resilience4j (ventana, failure-rate-threshold, wait-duration)

## Impacto en seguridad
- [ ] No aplica
- [ ] Requiere revisión de Seguridad
- [ ] Maneja datos personales
- [ ] Cambia autenticación/autorización
- [ ] Cambia secretos o configuración sensible

## Impacto en despliegue
- [ ] No aplica
- [ ] Cambia Dockerfile
- [ ] Cambia manifiestos K8s
- [ ] Cambia variables de entorno
- [ ] Cambia recursos/probes

## Plan de reversa
[Cómo revertir el cambio si falla, distinguiendo despliegue y BD cuando aplique]

## Revisores
- Revisor técnico:
- Arquitectura, si aplica:
- Seguridad, si aplica:
- Plataforma, si aplica:
