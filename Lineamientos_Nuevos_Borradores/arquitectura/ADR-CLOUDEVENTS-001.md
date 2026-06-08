# ADR-CLOUDEVENTS-001 — Adopción de CloudEvents v1.0 como estándar de envelope para eventos del bus institucional

**Fecha:** 2026-06-08
**Estado:** Aceptada
**Ámbito:** Mensajería, Arquitectura, Interoperabilidad

## Contexto

El lineamiento LIN-BUS-001 define el bus de eventos institucional sobre Apache Kafka. Todo evento publicado en el bus requiere un envelope que transporte metadatos del evento (identificador, tipo, origen, timestamp, traza distribuida) junto al payload de negocio.

La primera versión del envelope ONP usaba un esquema propietario con campos de nombres arbitrarios (`eventId`, `eventType`, `eventVersion`, `occurredAt`, `traceId`, `payload`). Este esquema cumplía los requisitos técnicos internos pero presentaba dos limitaciones:

1. **No interoperabilidad:** cualquier sistema externo (otra institución del Estado, herramienta CNCF) que reciba un evento ONP necesita implementar un parser ad hoc — no existe un estándar compartido.
2. **Campo `traceId` no conformante:** el formato raw de trace ID no es compatible con el estándar W3C TraceContext (`traceparent`), que Micrometer/OTEL ya usa internamente. Propagar el trace requería transformación manual.

**CloudEvents v1.0** es la especificación del Cloud Native Computing Foundation (CNCF) para describir eventos de forma interoperable. Es el estándar adoptado por Kubernetes, Knative, Azure Event Grid, AWS EventBridge y la mayoría de brokers cloud-native modernos. Su extensión oficial de distributed tracing usa `traceparent` en formato W3C TraceContext.

## Decisión

La ONP adopta **CloudEvents v1.0** como el estándar de envelope para todos los eventos publicados en el bus institucional.

El envelope institutcional conforme a CloudEvents v1.0 es:

```json
{
  "specversion":     "1.0",
  "id":              "UUID v4",
  "source":          "/onp/{servicio}",
  "type":            "pe.gob.onp.{dominio}.{clasificacion}.{descripcion}",
  "time":            "ISO 8601 UTC",
  "datacontenttype": "application/json",
  "dataschema":      "/onp/schemas/{dominio}/{clasificacion}/{descripcion}/{version}",
  "traceparent":     "W3C TraceContext: 00-{traceId32hex}-{spanId16hex}-{flags}",
  "data":            { ... }
}
```

La correspondencia con el envelope propietario anterior es:

| Campo anterior | Campo CloudEvents | Cambio |
|---|---|---|
| `eventId` | `id` | Renombrado |
| `eventType` | `type` | Renombrado + formato reverse-DNS: `pe.gob.onp.*` |
| `eventVersion` | `dataschema` | Reemplazado por URI de esquema versionado |
| `occurredAt` | `time` | Renombrado |
| `source` | `source` | Cambia a formato URI: `/onp/{servicio}` |
| `traceId` | `traceparent` | Reemplazado por W3C TraceContext |
| `payload` | `data` | Renombrado |
| — | `specversion` | Nuevo campo obligatorio, siempre `"1.0"` |
| — | `datacontenttype` | Nuevo campo, siempre `"application/json"` |

## Consecuencias

**Positivas:**
- Interoperabilidad inmediata con cualquier sistema que implemente CloudEvents v1.0, sin adaptadores.
- `traceparent` W3C TraceContext es nativo en Micrometer/OTEL — la propagación de trazas productor→consumidor no requiere transformación.
- Los herramientas del ecosistema CNCF (Knative Eventing, conectores Kafka con CloudEvents converter) funcionan sin configuración adicional.
- Facilita la integración futura con otras instituciones del Estado peruano que adopten el mismo estándar.

**A gestionar:**
- Los servicios que ya publicaban eventos con el envelope propietario deben migrar sus productores y consumidores al nuevo esquema. La migración se coordina entre el equipo productor y todos sus consumidores conocidos — seguir la regla de evolución de contratos de LIN-BUS-001 §5.5.
- El campo `type` cambia de formato (`expedientes.ciclovida.presentado` → `pe.gob.onp.expedientes.ciclovida.presentado`). Los consumidores que filtran por `type` deben actualizar sus filtros.

## Controles

- El contrato de todo evento nuevo registrado en el catálogo (LIN-BUS-001 Apéndice B) debe usar el envelope CloudEvents v1.0.
- El checklist de go-live (LIN-BUS-001 §12) incluye verificación de conformidad del envelope.
- Todo consumidor que filtre por `type` debe usar el formato reverse-DNS completo.

## Revisión requerida

Este ADR debe revisarse si:

- CloudEvents publica una versión mayor (v2.0) con cambios incompatibles.
- La ONP adopta un broker o plataforma que requiera un perfil CloudEvents diferente.
- Se establece un estándar nacional de interoperabilidad del Estado que defina un envelope distinto.
