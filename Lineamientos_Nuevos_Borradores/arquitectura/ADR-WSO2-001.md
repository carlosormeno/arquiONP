# ADR-WSO2-001 — Transición de SAA hacia WSO2 API Manager

**Código:** ADR-WSO2-001  
**Fecha:** 2026-05-28  
**Estado:** Propuesta de ADR    
**Última actualización:** 2026-08-21 — incorpora checklist de graduación (`GOB-CHK-001` H41)
**Ámbito:** Seguridad, APIs, Plataforma, Arquitectura
**ID en la matriz institucional:** `ADR-015` (`LIN-ARQ-001`, Apéndice A) — mismo identificador de decisión, no uno adicional  

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
- Cuando WSO2 pase a estado operativo, Arquitectura emitirá una actualización de los documentos listados abajo.
- En ese momento se definirá qué servicios migran primero y bajo qué criterios.

#### Checklist de graduación — documentos a actualizar

No depende de memoria: cada casilla se marca al actualizar el documento, y la graduación no se declara completa hasta que todas lo estén.

- [ ] **`LIN-SEC-APP-001 §7.2`** — el modelo objetivo pasa a vigente: validación de token delegada al gateway, scopes OAuth2 por operación, rate limiting en WSO2
- [ ] **`SEC-R-001` (LIN-SEC-APP-001 §7.1)** — retirar las reglas «mientras WSO2 no esté operativo»; el rate limiting básico en la aplicación deja de ser obligatorio
- [ ] **`LIN-SEC-APP-001 §8`** — `SaaTokenValidationFilter` deja de ser obligatorio en cada servicio; definir si se retira o se conserva como defensa en profundidad
- [ ] **`API-R-001` (LIN-API-REST-001 §2.5)** — retirar el bloque «Estado de implementación: fase PoC»; las reglas objetivo pasan a ser exigibles
- [ ] **`LIN-API-REST-001 §7.1`** — el token deja de ser opaco y pasa a JWT verificable localmente; revisar toda mención a «token opaco» en el corpus
- [ ] **`LIN-API-REST-001 §8.4`** — el rate limiting vuelve al gateway; retirar la regla transitoria de aplicación
- [ ] **`LIN-API-REST-001 §10.2`** — retirar la vía transitoria de gate sin WSO2; los estados `CREATED`/`PUBLISHED` pasan a ser exigibles
- [ ] **`LIN-API-REST-001 §10.3`** — activar los ítems diferidos: URL base en Publisher, plan de suscripción, publicación en Dev Portal
- [ ] **`LIN-FE-ANG-001 §9.3`** — revisar los dos escenarios de manejo de token frente al nuevo modelo OAuth2/OIDC
- [ ] **`ADR-TLS-INTERNO-001`** — WSO2 asume la terminación TLS; es disparador de revisión declarado en ese ADR
- [ ] **`LIN-K8S-001 §12.2`** — la exposición vía API Manager deja de ser «preferente» y pasa a obligatoria
- [ ] **`GOB-MAT-001`** — actualizar el tema «API Gateway y API Manager» y el estado de este ADR
- [ ] **Registrar en producción las APIs ya desplegadas**, conservando su estado equivalente (`LIN-API-REST-001 §10.2`)

> **Las APIs ya en producción no se rehacen:** se registran en WSO2 con el estado que les corresponda. La graduación del PoC no invalida lo construido bajo la vía transitoria.

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
