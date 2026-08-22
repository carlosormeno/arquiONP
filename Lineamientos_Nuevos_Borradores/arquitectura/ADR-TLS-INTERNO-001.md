# ADR-TLS-INTERNO-001 — Terminación TLS en el perímetro y tráfico intra-cluster sobre HTTP

**Código:** ADR-TLS-INTERNO-001  
**Fecha:** 2026-08-09  
**Estado:** Propuesta de ADR  
**Ámbito:** Seguridad, Plataforma, APIs, Arquitectura
**ID en la matriz institucional:** `ADR-016` (`LIN-ARQ-001`, Apéndice A) — mismo identificador de decisión, no uno adicional  

## Contexto

`SEC-R-001` (LIN-SEC-APP-001 §7.1) establece HTTPS obligatorio en todos los ambientes compartidos, admitiendo una única excepción: desarrollo local en la máquina del desarrollador.

`API-R-001` (LIN-API-REST-001 §2.5) describe, como parte del modelo objetivo con WSO2 API Manager, la terminación TLS en el perímetro: el gateway resuelve el handshake y reenvía la petición al backend por la red interna del cluster. La consecuencia natural de esa topología es que los pods reciben tráfico HTTP.

Ambas afirmaciones no podían coexistir. La revisión de fondo de `LIN-API-REST-001` (`GOB-CHK-001` H24.4) dejó la topología marcada como no aplicable a la espera de esta decisión, precisamente para no relajar una regla de seguridad desde un documento que no es su dueño.

La cuestión de fondo no es si el tráfico interno va cifrado, sino **cuál es el límite de confianza**. Terminar TLS en el perímetro y confiar en la red del cluster es la práctica habitual en Kubernetes, pero solo es defendible si el acceso a esa red interna está efectivamente restringido. Sin restricción de red, "tráfico interno" significa que cualquier pod del cluster —incluido uno comprometido o ajeno al sistema— puede alcanzar en claro a cualquier servicio.

La ONP no dispone hoy de malla de servicios. `K8S-R-003` (LIN-K8S-001 §9.4) reserva expresamente el patrón Sidecar para cuando Plataforma habilite formalmente una malla (Envoy/Istio) con terminación mTLS entre pods, y `LIN-K8S-001` Anexo E ya define la `NetworkPolicy` como el mecanismo disponible para restringir el tráfico a nivel de pod.

## Decisión

**Se admite el tráfico intra-cluster sobre HTTP como excepción acotada a `SEC-R-001` (LIN-SEC-APP-001 §7.1)**, bajo tres condiciones de cumplimiento obligatorio y verificable. La excepción no es general: aplica al tramo comprendido entre el punto de terminación TLS y el pod destino, dentro del mismo cluster.

### Condición 1 — TLS termina en el perímetro, no antes

El handshake TLS se resuelve en el Ingress Controller o en el gateway WSO2. Todo tráfico que provenga de fuera del cluster —consumidores externos, entidades del Estado, portales ciudadanos, redes de la propia ONP fuera del cluster— viaja cifrado hasta ese punto. Ningún `Service` de tipo `NodePort` o `LoadBalancer` puede exponer un backend directamente sin pasar por él.

### Condición 2 — `NetworkPolicy` obligatoria en todo servicio que reciba tráfico interno

Esta es la condición que sustituye al cifrado como control. Sin ella la excepción no se sostiene y queda revocada de hecho.

`K8S-R-002` (LIN-K8S-001 §9.1) clasificaba la `NetworkPolicy` como *«recomendada; obligatoria para servicios críticos»*. Con este ADR pasa a ser **obligatoria para todo servicio que reciba tráfico dentro del cluster**, con la política mínima del Anexo E de ese lineamiento: aceptar tráfico únicamente del Ingress Controller y de los pods del mismo sistema, denegar el resto.

Un servicio sin `NetworkPolicy` no puede acogerse a esta excepción y debe servir HTTPS extremo a extremo.

### Condición 3 — Migración a mTLS cuando exista malla de servicios

Cuando Plataforma habilite formalmente una malla de servicios (`K8S-R-003` (LIN-K8S-001 §9.4), excepción legítima 2), el cifrado entre pods pasa a resolverse con mTLS gestionado por la infraestructura y **esta excepción queda sin efecto**. La migración no es opcional ni queda a criterio de cada equipo.

## Alcance y límites

| Tramo | Regla |
|---|---|
| Consumidor externo → Ingress / gateway WSO2 | HTTPS obligatorio, sin excepción |
| Ingress / gateway → pod backend (mismo cluster) | HTTP admitido bajo las tres condiciones |
| Pod → pod, mismo cluster | HTTP admitido bajo las tres condiciones |
| Pod → servicio fuera del cluster (SAA, RENIEC, SUNAT, Oracle) | HTTPS obligatorio, sin excepción |
| Cualquier tramo que atraviese el borde del cluster | HTTPS obligatorio, sin excepción |

Esta excepción **no** autoriza HTTP en ambientes compartidos fuera de Kubernetes: un servicio desplegado en máquina virtual o servidor de aplicaciones sigue sujeto a `SEC-R-001` (LIN-SEC-APP-001 §7.1) sin matices.

## Consecuencias

- Se elimina la contradicción entre `SEC-R-001` (LIN-SEC-APP-001 §7.1) y `API-R-001` (LIN-API-REST-001 §2.5) sin relajar la postura de seguridad: se sustituye un control (cifrado en tránsito interno) por otro verificable (restricción de red), no se retira.
- El coste de operar certificados por pod —emisión, rotación, truststore en cada JVM— se evita en una plataforma que hoy no tiene ni `cert-manager` ni malla declarados en el corpus.
- Aumenta la carga de configuración en `LIN-K8S-001`: la `NetworkPolicy` deja de ser recomendada y pasa a ser un requisito de despliegue más, verificable en el gate.
- El riesgo residual es el movimiento lateral dentro del cluster si una `NetworkPolicy` está mal definida o ausente. Se mitiga con la Condición 2 y su verificación en el gate de publicación.

## Controles compensatorios

- La `NetworkPolicy` de cada servicio se revisa en el Merge Request que introduce o modifica sus manifiestos (`LIN-VER-001 §18`).
- El gate de publicación de `LIN-API-REST-001 §10.3` verifica su existencia antes de autorizar el paso a producción.
- Los datos previsionales y personales siguen sujetos a `Cache-Control: no-store` y a la clasificación de datos de `LIN-API-REST-001 §10.3` Parte B, con independencia del cifrado del tramo interno.

## Revisión requerida

Este ADR debe revisarse cuando ocurra alguno de estos eventos:

- Plataforma habilite formalmente una malla de servicios con mTLS entre pods;
- WSO2 API Manager pase de PoC a entorno institucional operativo y asuma la terminación TLS;
- se detecte un incidente de movimiento lateral dentro del cluster;
- Seguridad de la Información emita una directiva que exija cifrado en tránsito extremo a extremo sin excepción de red.

## Documentos afectados

| Documento | Efecto |
|---|---|
| `SEC-R-001` (LIN-SEC-APP-001 §7.1) | Incorpora la excepción acotada y sus tres condiciones |
| `K8S-R-002` (LIN-K8S-001 §9.1) | `NetworkPolicy` pasa de recomendada a obligatoria para servicios que reciben tráfico interno |
| `LIN-API-REST-001 §2.2` y `§2.5` | Retiran la reserva y remiten a este ADR |
| `GOB-MAT-001` | Registra el ADR y el cambio de estado del tema |
