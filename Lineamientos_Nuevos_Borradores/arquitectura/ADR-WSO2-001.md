# ADR-WSO2-001 — Transición de SAA hacia WSO2 API Manager

**Código:** ADR-WSO2-001  
**Fecha:** 2026-05-28  
**Estado:** Propuesta de ADR  
**Ámbito:** Seguridad, APIs, Plataforma, Arquitectura

## Contexto

La ONP opera hoy con SAA como mecanismo institucional vigente para autenticación, autorización y gestión de accesos. En paralelo, WSO2 API Manager se encuentra en prueba de concepto como plataforma objetivo para gateway, publicación y validación de tokens bajo un modelo OAuth2/OIDC.

Mientras WSO2 no esté operativo en producción, los servicios backend continúan validando el token SAA de manera directa mediante `SaaTokenValidationFilter`.

La coexistencia entre ambos modelos requiere gobierno explícito para evitar implementaciones paralelas divergentes.

## Decisión

1. **Estado actual oficial**
- SAA sigue siendo el mecanismo institucional obligatorio.
- WSO2 permanece en PoC hasta comunicación formal de Arquitectura y Plataforma.

2. **Regla de implementación para servicios nuevos**
- Mientras WSO2 no esté operativo en producción, todo backend protegido implementa `SaaTokenValidationFilter`.
- No se adopta OAuth2/OIDC por cuenta propia en servicios nuevos sin ADR adicional aprobado.

3. **Regla de transición**
- Cuando WSO2 pase a estado operativo, Arquitectura emitirá una actualización de `LIN-SEC-APP-001`, `LIN-API-REST-001` y este ADR.
- En ese momento se definirá qué servicios migran primero y bajo qué criterios.

4. **Criterio mínimo para graduar el PoC**
- gateway WSO2 operativo en un entorno institucional estable;
- validación de token y publicación de APIs probadas end-to-end;
- responsabilidades claras entre Plataforma, Arquitectura y Desarrollo;
- lineamientos actualizados para backend, frontend y observabilidad.

## Consecuencias

- Se evita que equipos distintos adopten modelos incompatibles en paralelo.
- Se preserva la continuidad operativa con SAA mientras madura WSO2.
- La migración futura queda gobernada, no implícita.

## Controles compensatorios

- Toda referencia a WSO2 en lineamientos vigentes debe leerse como objetivo de transición, no como obligación operativa actual.
- Toda excepción a esta regla requiere ADR aprobado por Arquitectura.

## Revisión requerida

Este ADR debe revisarse cuando ocurra alguno de estos eventos:

- WSO2 pase de PoC a entorno institucional operativo;
- se apruebe el primer servicio productivo publicado exclusivamente vía WSO2;
- cambie el modelo institucional de identidad o federación.
