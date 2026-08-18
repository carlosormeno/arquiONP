# LIN-IAC-001 — Lineamiento de Infraestructura como Código ONP

**Código:** LIN-IAC-001  
**Versión:** v0.1.3  
**Estado:** En revisión  
**Fecha:** 2026-08-17  
**Propietario documental:** Arquitectura de Software — OTI  
**Revisores sugeridos:** Plataforma/Infraestructura, Seguridad Digital, Arquitectura, Desarrollo  
**Marco rector:** LIN-ARQ-001 — Marco Rector de Arquitectura de Software  
**Herramienta institucional:** Terraform + GitLab Ultimate  

---

## Control de cambios

| Versión | Fecha | Autor | Descripción |
|---|---|---|---|
| v0.1.0 | 2026-05-28 | Arquitectura OTI | Borrador inicial del lineamiento de infraestructura como código |
| v0.1.1 | 2026-07-09 | Arquitectura OTI | Corrige la referencia al Marco Rector: `LIN-ARQ-000` (congelado) → `LIN-ARQ-001` (vigente) en encabezado y §2 |
| v0.1.2 | 2026-07-10 | Arquitectura OTI | Corrige contradicción interna: el checklist §12.1 exigía una rama `develop` protegida que §5.4 rechaza explícitamente ("sería redundante y crearía confusión"). El checklist ahora solo exige `main` protegida |
| v0.1.3 | 2026-08-17 | Arquitectura OTI | Revisión de fondo (`GOB-CHK-001` H32). **(1) `§6` no normaba el respaldo ni la recuperación del estado de Terraform**, que es el activo más crítico del repositorio: perderlo deja a Terraform sin el mapeo entre código y recursos reales, y el siguiente `apply` puede recrear infraestructura existente o destruir la que no reconoce. Nueva `§6.3` con versionado, respaldo externo, retención, **prueba de restauración anual** y control del desbloqueo forzado. **(2) Dos modelos de madurez usaban la misma nomenclatura.** Este lineamiento numera «Fase 0…5» y `LIN-CICD-001` «Fase 0…7»; ambos se citan mutuamente sin calificar la escala, de modo que «Fase 4» en una cita cruzada era ambiguo. Toda referencia queda calificada. **(3) `§10.3` exigía documentar un cambio manual de emergencia «en un ADR»** — instrumento equivocado: un ADR registra una decisión institucional que obliga al corpus, no una desviación operativa puntual. Pasa a `EXC-IAC-NNN`, y `§14` se retitula en consecuencia. **(4)** `§2` no listaba `LIN-BD-ORA-001`, `LIN-OBS-001` ni `GOB-MAT-001`. El documento pasa a **En revisión** |

---

## Tabla de contenido

1. [Objetivo y alcance](#1-objetivo-y-alcance)
2. [Normativa y documentos relacionados](#2-normativa-y-documentos-relacionados)
3. [Principios rectores](#3-principios-rectores)
4. [Modelo de madurez IaC ONP](#4-modelo-de-madurez-iac-onp)
5. [Repositorio dedicado de IaC](#5-repositorio-dedicado-de-iac)
6. [Gestión del estado de Terraform](#6-gestión-del-estado-de-terraform)
7. [Gestión de secretos y variables](#7-gestión-de-secretos-y-variables)
8. [Buenas prácticas en Terraform](#8-buenas-prácticas-en-terraform) *(incluye 8.6 — VMware vSphere on-premise)*
9. [Pipeline de validación de IaC](#9-pipeline-de-validación-de-iac)
10. [Detección de drift](#10-detección-de-drift)
11. [Responsabilidades](#11-responsabilidades)
12. [Checklist de conformidad](#12-checklist-de-conformidad)
13. [Anti-patrones](#13-anti-patrones)
14. [Proceso ADR para excepciones](#14-proceso-adr-para-excepciones)
15. [Glosario](#15-glosario)
16. [Anexos](#16-anexos)

---

## 1. Objetivo y alcance

### 1.1 Objetivo

Este lineamiento establece el estándar institucional para adoptar Infraestructura como Código (IaC) en la ONP usando Terraform como herramienta declarativa de aprovisionamiento. Define una ruta de madurez progresiva que parte del aprovisionamiento manual documentado y avanza hacia la gestión declarativa versionada, con integración en pipelines GitLab CI/CD y adopción gradual de principios GitOps.

El objetivo no es imponer automatización total desde el inicio, sino establecer las bases estructurales correctas desde el primer día para que la adopción sea sostenible y el equipo de Plataforma pueda avanzar sin refactorizaciones mayores.

### 1.2 Alcance

Aplica a todo recurso de infraestructura aprovisionado o gestionado por el equipo de Plataforma e Infraestructura de la OTI:

| Elemento | Aplica | Observación |
|---|---:|---|
| Servidores y VMs | Sí | Aprovisionamiento declarativo cuando aplique; incluye VMs en VMware vSphere |
| Redes y segmentos | Sí | Reglas de firewall, VLANs, grupos de seguridad, segmentos NSX-T |
| Clústeres Kubernetes | Sí | Definición declarativa del clúster y namespaces base |
| Bases de datos de infraestructura | Sí | Instancias, parámetros, acceso de red |
| Almacenamiento | Sí | Volúmenes persistentes, datastores, buckets |
| Registro de contenedores y artefactos | Sí, progresivo | Según madurez |
| Configuración de SO y nodos | Fuera de alcance | Ver [sección 1.3](#13-fuera-de-alcance) |
| Despliegue de aplicaciones | Fuera de alcance | Responsabilidad de `LIN-CICD-001` |

> **Entornos on-premise VMware:** este lineamiento aplica íntegramente a infraestructura gestionada sobre VMware vSphere/vCenter. El provider `hashicorp/vsphere` es el mecanismo Terraform aprobado para aprovisionar VMs, redes y almacenamiento en el entorno on-premise de la OTI. Ver [sección 8.6](#86-consideraciones-para-entornos-vmware-vsphere-on-premise) para consideraciones específicas.

### 1.3 Fuera de alcance

| Tema | Documento / responsable |
|---|---|
| Gestión de configuración de VMs y nodos (paquetes, SO) | Ansible u herramienta aprobada; lineamiento separado |
| Despliegue de aplicaciones y manifiestos Kubernetes | LIN-CICD-001 / LIN-K8S-001 |
| Estrategia de ramas, MR, tags y releases | LIN-VER-001 |
| Seguridad en aplicaciones, SAST, SCA, secretos de app | LIN-SEC-APP-001 |
| Observabilidad de aplicaciones | LIN-OBS-001 |

> **Terraform y Ansible son herramientas complementarias, no excluyentes.** Terraform aprovisiona la infraestructura; Ansible gestiona la configuración de lo que corre sobre ella. Ambas disciplinas coexisten bajo este lineamiento y su lineamiento complementario.

---

## 2. Normativa y documentos relacionados

| Documento | Código | Relación |
|---|---|---|
| Marco Rector de Arquitectura de Software | LIN-ARQ-001 | Define modelo arquitectónico general |
| Versionamiento y Control de Cambios | LIN-VER-001 | Define ramas, MR, tags y trazabilidad aplicables al repo de IaC |
| Integración y Entrega Continua | LIN-CICD-001 | Define pipeline de validación y gates; LIN-IAC-001 es el dueño del estándar IaC |
| Contenedores y Orquestación | LIN-K8S-001 | Clústeres K8s cuya infraestructura se aprovisiona con Terraform |
| Seguridad en Aplicaciones | LIN-SEC-APP-001 | Controles de secretos, tokens y credenciales en pipelines |
| Directiva de Desarrollo de Software Seguro | DIR-SEC-SW-001 | Marco superior para controles de seguridad |
| Estándar de Base de Datos Oracle | LIN-BD-ORA-001 | Infraestructura de base de datos aprovisionada o parametrizada vía IaC |
| Log, Trazabilidad y Observabilidad | LIN-OBS-001 | Componentes de observabilidad (colector OTEL, namespaces) aprovisionados vía IaC |
| Matriz de Propiedad Documental | GOB-MAT-001 | Determina qué documento es dueño de cada tema y norma el registro de excepciones |

---

## 3. Principios rectores

| # | Principio | Descripción |
|---|---|---|
| P1 | **Madurez progresiva** | IaC se adopta por fases sin exigir automatización total desde el inicio. |
| P2 | **Git como única fuente de verdad** | Toda modificación de infraestructura debe trazarse a un commit aprobado en el repositorio de IaC. |
| P3 | **Declarativo e idempotente** | El estado deseado se declara en código; aplicarlo múltiples veces produce el mismo resultado. |
| P4 | **Inmutabilidad** | La infraestructura no se modifica manualmente en producción. Los cambios pasan por código y pipeline. |
| P5 | **Separación de ambientes** | Cada ambiente (`dev`, `qa`, `prod`) tiene su propio directorio, estado y pipeline independientes. |
| P6 | **Seguridad por defecto** | Ningún secreto, contraseña o token puede existir en el código Git. |
| P7 | **Repositorio dedicado** | El código IaC vive en un repositorio GitLab separado del código de aplicación. |
| P8 | **Visibilidad del plan** | Todo `terraform apply` debe estar precedido de un `terraform plan` revisado y aprobado. |
| P9 | **Detección de drift** | El estado real de la infraestructura se compara periódicamente contra el estado declarado. |
| P10 | **Aprobación humana donde corresponda** | El apply en producción siempre requiere aprobación manual explícita. |

---

## 4. Modelo de madurez IaC ONP

La ONP adopta un modelo de madurez progresivo para IaC. Cada fase tiene criterios de graduación explícitos que deben cumplirse antes de avanzar a la siguiente.

> ⚠️ **Estas fases no son las de `LIN-CICD-001`.** Ese lineamiento tiene su propio modelo de madurez, también numerado «Fase 0…7», y ambos documentos se citan mutuamente. **Son escalas distintas y no comparables**: la Fase 5 de IaC (GitOps pleno) no equivale a la Fase 5 de CI/CD (seguridad dinámica y performance). Toda cita entre ambos documentos debe calificar la escala — «Fase 3 de IaC», «Fase 4 de CI/CD»— y nunca decir «Fase N» a secas.

| Fase | Nombre | Estado objetivo |
|---:|---|---|
| 0 | Aprovisionamiento manual documentado | Estado actual |
| 1 | Adopción local y aprendizaje (sandbox) | Primeros archivos Terraform en repositorio |
| 2 | Colaboración y estado compartido | Backend remoto GitLab activo |
| 3 | Integración en CI/CD (modo read-only) | Pipeline de validación activo; apply aún manual |
| 4 | Estructuración modular | Módulos reutilizables por ambiente |
| 5 | GitOps e infraestructura inmutable plena | Apply automatizado por ambiente con aprobación en producción |

### 4.1 Fase 0 — Aprovisionamiento manual documentado

Describe el estado actual de la OTI.

Incluye:
- creación de recursos vía consola web o scripts locales no versionados;
- inventario documentado en diagramas o expediente técnico;
- control mediante checklist manual.

**Criterio de graduación:** el equipo documenta el inventario completo de recursos de al menos un ambiente en un repositorio Git (aunque sea en Markdown o diagrama), y se crea el repositorio dedicado `oti-plataforma/infrastructure-iac` con la estructura de directorios base definida en la [sección 5](#5-repositorio-dedicado-de-iac).

### 4.2 Fase 1 — Adopción local y aprendizaje

Incluye:
- escritura de los primeros archivos `.tf` para recursos no críticos;
- uso de providers oficiales del Terraform Registry;
- ejecución local en máquinas de desarrollo o sandbox con estado local (`terraform.tfstate`);
- foco en entrenamiento sin impacto en ambientes compartidos.

**Criterio de graduación:** al menos 2 recursos no críticos (por ejemplo, namespace Kubernetes, grupo de seguridad) gestionados con Terraform en sandbox, con peer review aprobado en GitLab mediante Merge Request.

### 4.3 Fase 2 — Colaboración y estado compartido

Incluye:
- migración del estado de Terraform al backend HTTP de GitLab (GitLab-Managed Terraform State);
- activación de *state locking* automático para prevenir sobreescrituras concurrentes;
- eliminación de archivos `terraform.tfstate` locales del repositorio.

**Criterio de graduación:** el estado migrado al backend HTTP de GitLab verificado con `terraform state list` desde al menos dos máquinas distintas; archivo `.gitignore` actualizado para excluir `*.tfstate` y `*.tfstate.backup`.

### 4.4 Fase 3 — Integración en CI/CD (modo read-only)

Incluye:
- pipeline de validación automática en Merge Requests al repositorio de IaC;
- ejecución obligatoria de `terraform fmt -check`, `terraform validate` y `terraform plan`;
- publicación del plan como artefacto del pipeline para revisión;
- `terraform apply` todavía ejecutado manualmente por el equipo de Plataforma desde terminal autorizada;
- activación de la detección de drift semanal (ver [sección 10](#10-deteccion-de-drift)).

**Criterio de graduación:** pipeline activo durante al menos 30 días sin falsos positivos; al menos un caso de drift detectado y corregido mediante MR documentado.

### 4.5 Fase 4 — Estructuración modular

Incluye:
- separación clara de módulos locales reutilizables en `modules/`;
- uso de variables y outputs tipados con descripciones en todos los módulos;
- revisión y actualización de la estructura de directorios por ambiente.

**Criterio de graduación:** al menos un módulo local reutilizado en 2 ambientes distintos, con variables y outputs tipados y documentados.

### 4.6 Fase 5 — GitOps e infraestructura inmutable plena

Incluye:
- `terraform apply` automatizado en ambientes `dev` y `qa` tras merge a la rama correspondiente;
- `terraform apply` en `prod` restringido a tags de release con aprobación manual obligatoria en GitLab;
- cero cambios manuales tolerados en producción.

**Criterio de graduación:** cero cambios manuales en producción durante 90 días consecutivos; toda modificación trazable a un MR aprobado con evidencia de plan publicado.

---

## 5. Repositorio dedicado de IaC

### 5.1 Decisión arquitectónica

El código de IaC vive en un **repositorio Git dedicado y separado** de los repositorios de aplicación.

| Opción | Veredicto | Razón |
|---|---|---|
| Repositorio dedicado (`oti-plataforma/infrastructure-iac`) | **Adoptado** | Separación de concerns, control de acceso independiente, estado de Terraform limpio por proyecto GitLab |
| Rama en repositorio de aplicación | Rechazado | Las ramas son versiones del mismo contenido, no separación de responsabilidades. Mezcla historia de aplicación con infraestructura |
| Directorio en monorepo de aplicación | Rechazado | Complica el alcance de pipelines CI y hace imposible restringir independientemente quién modifica infraestructura |

**Nombre de referencia:** `oti-plataforma/infrastructure-iac` (ajustar según nomenclatura GitLab de la OTI).

### 5.2 Permisos en GitLab

| Rol en GitLab | Equipo | Capacidad |
|---|---|---|
| Maintainer / Owner | Plataforma e Infraestructura | Merge, configuración, pipelines, apply |
| Developer | Arquitectura de Software | Revisión técnica, aprobación de MR |
| Reporter | Equipos de desarrollo | Visualización de planes y artefactos |

### 5.3 Estructura de directorios

> **Regla crítica:** la estructura de directorios por ambiente se define desde el **primer commit** del repositorio, aunque inicialmente solo `environments/dev/` tenga contenido real. Esto evita refactorizaciones del estado de Terraform en fases posteriores.

```
infrastructure-iac/
├── .gitignore                         # Excluye *.tfstate, *.tfstate.backup, .terraform/, terraform.tfvars
├── .gitlab-ci.yml                     # Pipeline de validación IaC
├── README.md
├── environments/
│   ├── dev/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   ├── outputs.tf
│   │   ├── versions.tf                # required_providers y required_version
│   │   └── terraform.tfvars.example   # Plantilla de valores (sin secretos)
│   ├── qa/
│   │   └── (misma estructura que dev/)
│   └── prod/
│       └── (misma estructura que dev/)
└── modules/
    ├── kubernetes/
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    ├── networking/
    └── database/
```

### 5.4 Gestión de ramas

Aplica `LIN-VER-001` con el **modelo objetivo GitLab Flow simplificado** (disciplinado con principios TBD). El repositorio de IaC es un buen candidato para este modelo porque la separación de ambientes la provee la **estructura de directorios** (`environments/dev/`, `qa/`, `prod/`), no las ramas. Tener una rama `develop` de larga vida sería redundante y crearía confusión (¿rama del ambiente dev o rama de trabajo?).

| Rama | Propósito | Protección |
|---|---|---|
| `main` | Única fuente de verdad; refleja el estado declarado de toda la infraestructura | Protegida; requiere MR + aprobación de al menos un miembro de Plataforma |
| `feature/*` o `fix/*` | Cambios específicos de infraestructura; vida corta (días, no semanas) | Sin protección; se integra a `main` vía MR |

> **¿Qué determina a qué ambiente afecta un cambio?** El directorio modificado dentro del MR, no la rama. Un MR que toca solo `environments/dev/` solo afecta dev cuando se aplique. El pipeline detecta qué directorio cambió y ejecuta el `terraform plan` correspondiente.

---

## 6. Gestión del estado de Terraform

### 6.1 Backend remoto — GitLab-Managed Terraform State

A partir de la **Fase 2 de IaC**, el estado de Terraform debe almacenarse en el backend HTTP nativo de GitLab Ultimate. Esto provee:

- *state locking* automático mediante `POST`/`DELETE` HTTP para prevenir sobreescrituras concurrentes;
- trazabilidad del estado asociada al proyecto GitLab;
- sin infraestructura adicional (no requiere S3, MinIO ni Consul).

Cada ambiente tiene su propia clave de estado:

| Ambiente | Clave de estado (`TF_STATE_NAME`) |
|---|---|
| dev | `dev` |
| qa | `qa` |
| prod | `prod` |

Ejemplo de bloque `backend` en `versions.tf`:

```hcl
terraform {
  required_version = "~> 1.7"

  backend "http" {
    # Valores inyectados en el pipeline mediante before_script
    # Ver Anexo B para el comando terraform init completo
  }

  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.27"
    }
  }
}
```

### 6.2 Prohibición de Terraform Workspaces para separación de ambientes

**No se deben usar Terraform Workspaces para separar ambientes.** Los Workspaces comparten la misma configuración de backend y aumentan el riesgo de ejecutar `terraform apply` en el ambiente incorrecto. La separación de ambientes se realiza exclusivamente mediante directorios independientes en `environments/`.

### 6.3 Respaldo y recuperación del estado

El archivo de estado es **el activo más crítico del repositorio de IaC**, por encima del propio código: el código puede reescribirse, pero el estado es lo único que mapea cada recurso declarado con el recurso real aprovisionado. Perderlo o corromperlo deja a Terraform sin ese mapeo, y el siguiente `apply` puede intentar **recrear infraestructura que ya existe** o destruir la que no reconoce.

| Regla | Detalle |
|---|---|
| **Versionado del estado** | El backend HTTP de GitLab conserva versiones anteriores del estado. Plataforma debe verificar que el versionado esté activo en cada proyecto de IaC antes de graduar a Fase 2. |
| **Respaldo externo** | Además del versionado de GitLab, se exporta una copia del estado de `prod` mediante `terraform state pull` en el pipeline programado de detección de drift (§10.1), y se almacena en el repositorio de respaldos institucional. Depender de una sola plataforma para el estado y su respaldo es un punto único de fallo. |
| **Retención** | Mínimo **90 días** de versiones recuperables para `prod`; 30 días para `dev` y `qa`. |
| **Prueba de recuperación** | Al menos una vez al año Plataforma debe **ejecutar una restauración de prueba** del estado de un ambiente no productivo y dejar constancia. Un respaldo cuya restauración nunca se probó no es un respaldo. |
| **Antes de un `apply` en `prod`** | Verificar que existe una versión del estado inmediatamente anterior, recuperable. |
| **Bloqueo huérfano** | Si un pipeline interrumpido deja el estado bloqueado, el desbloqueo (`terraform force-unlock`) lo ejecuta únicamente Plataforma, dejando registro del motivo. Nunca desde una terminal local sin registro. |

> **Relación con la brecha institucional de respaldo.** El corpus ONP **no norma todavía** respaldo, recuperación ante desastres ni valores institucionales de RTO/RPO (`GOB-CHK-001` H11.2). Esta sección cubre **únicamente el estado de Terraform**, que es responsabilidad de este lineamiento. No sustituye a la política institucional pendiente, y los valores de retención de arriba deberán revisarse cuando esa política exista.

### 6.4 Estado local

El archivo `terraform.tfstate` y su backup **no deben versionarse en Git**. El `.gitignore` debe incluir:

```
.terraform/
*.tfstate
*.tfstate.backup
terraform.tfvars
```

---

## 7. Gestión de secretos y variables

### 7.1 Reglas generales

| Regla | Descripción |
|---|---|
| **Prohibición absoluta de secretos en Git** | Ningún secreto, contraseña, token, certificado o clave puede existir en ningún archivo del repositorio, incluyendo `terraform.tfvars` |
| **Variables protegidas en GitLab CI/CD** | Las credenciales se inyectan en el pipeline como variables protegidas y enmascaradas con el prefijo `TF_VAR_*` |
| **`terraform.tfvars` en `.gitignore`** | El archivo con valores reales nunca se versiona; se documenta en `terraform.tfvars.example` sin valores sensibles |
| **`sensitive = true`** | Toda variable que contenga datos sensibles debe declararse con `sensitive = true` en `variables.tf` |
| **HashiCorp Vault (fase avanzada)** | Para ambientes de mayor madurez, las credenciales se obtienen dinámicamente desde Vault |

### 7.2 Ejemplo de declaración correcta

```hcl
# variables.tf
variable "db_password" {
  description = "Contraseña de la base de datos de infraestructura"
  type        = string
  sensitive   = true
}
```

```hcl
# terraform.tfvars.example  (versionado en Git, sin valores reales)
db_password = "REEMPLAZAR_CON_VARIABLE_GITLAB_CI"
```

### 7.3 Inyección en pipeline GitLab

Las credenciales Terraform se inyectan como variables `TF_VAR_*` en GitLab CI/CD Settings → Variables, declaradas como **protegidas** y **enmascaradas**:

```
TF_VAR_db_password = <valor real>
```

Terraform mapea automáticamente `TF_VAR_*` a la variable de input correspondiente (`var.db_password`) en tiempo de ejecución.

> La mecánica completa de configuración de variables de pipeline (tipos, alcance, convenciones y reglas) se rige por **LIN-CICD-001 13.4**. La política de secretos (prohibiciones y rotación) se rige por **LIN-SEC-APP-001 12**.

---

## 8. Buenas prácticas en Terraform

### 8.1 Versionamiento de providers y CLI

Todo módulo y directorio de ambiente debe declarar restricciones de versión explícitas en `versions.tf`:

**Entorno Kubernetes (on-premise o nube):**

```hcl
terraform {
  required_version = "~> 1.7"

  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.27"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.13"
    }
  }
}
```

**Entorno VMware vSphere on-premise (ONP):**

```hcl
terraform {
  required_version = "~> 1.7"

  required_providers {
    vsphere = {
      source  = "hashicorp/vsphere"
      version = "~> 2.7"
    }
    nsxt = {                        # Solo si se usa NSX-T para redes definidas por software
      source  = "hashicorp/nsxt"
      version = "~> 3.6"
    }
  }
}
```

El operador `~>` (pesimista) es obligatorio en todos los casos. No se permiten versiones sin restricción (`version = "latest"` o sin declarar).

### 8.2 Etiquetado obligatorio de recursos

Todo recurso creado debe incluir al menos las siguientes etiquetas (*tags*):

| Etiqueta | Descripción | Ejemplo |
|---|---|---|
| `propietario` | Equipo o área responsable | `plataforma-oti` |
| `ambiente` | Ambiente de despliegue | `dev` / `qa` / `prod` |
| `sistema` | Sistema o aplicación relacionada | `k8s-onp` |
| `centro_de_costo` | Centro de costo institucional | `oti-infraestructura` |
| `gestionado_por` | Herramienta de gestión | `terraform` |

### 8.3 Variables y outputs

- Toda variable debe tener `description` y `type` declarados.
- Todo output debe tener `description` declarado.
- Las variables sensibles deben declararse con `sensitive = true`.
- No usar variables sin tipo (`type = any`) salvo justificación en ADR.

### 8.4 Nomenclatura de recursos

Los nombres de recursos deben seguir el patrón: `{sistema}-{ambiente}-{tipo}`.

Ejemplo: `onp-prod-k8s-cluster`, `onp-dev-db-postgres`.

### 8.5 Organización de archivos por directorio de ambiente

Cada directorio de ambiente debe tener al mínimo:

| Archivo | Contenido |
|---|---|
| `main.tf` | Recursos principales e invocaciones a módulos |
| `variables.tf` | Declaración de variables de entrada |
| `outputs.tf` | Declaración de salidas |
| `versions.tf` | `required_version` y `required_providers` |
| `terraform.tfvars.example` | Plantilla de valores sin datos sensibles |

### 8.6 Consideraciones para entornos VMware vSphere on-premise

Esta sección complementa las buenas prácticas generales para el contexto específico de la ONP, donde la infraestructura se aprovisiona sobre VMware vSphere/vCenter.

#### 8.6.1 Autenticación con vCenter

Las credenciales de vCenter son secretos de infraestructura y se rigen por la regla general de la [sección 7](#7-gestion-de-secretos-y-variables). El provider `vsphere` se configura mediante variables de entorno o variables Terraform sensibles — **nunca con valores en texto plano en el código**.

```hcl
# variables.tf
variable "vcenter_server" {
  description = "FQDN o IP del servidor vCenter"
  type        = string
}

variable "vcenter_user" {
  description = "Usuario de servicio para Terraform en vCenter"
  type        = string
  sensitive   = true
}

variable "vcenter_password" {
  description = "Contraseña del usuario de servicio en vCenter"
  type        = string
  sensitive   = true
}
```

```hcl
# main.tf — configuración del provider
provider "vsphere" {
  vsphere_server       = var.vcenter_server
  user                 = var.vcenter_user
  password             = var.vcenter_password
  allow_unverified_ssl = false   # Ver [sección 13](#13-anti-patrones) — anti-patrón
}
```

Las variables se inyectan en el pipeline con el prefijo `TF_VAR_*`:

```
TF_VAR_vcenter_server   = vcenter.onp.gob.pe
TF_VAR_vcenter_user     = svc-terraform@onp.local
TF_VAR_vcenter_password = <variable protegida y enmascarada en GitLab>
```

#### 8.6.2 Etiquetado de recursos en vSphere

En VMware, las etiquetas no son simples metadatos: requieren crear primero una **categoría de etiqueta** y luego la etiqueta como recursos Terraform separados. Las etiquetas obligatorias definidas en la [sección 8.2](#82-etiquetado-obligatorio-de-recursos) se implementan así:

```hcl
# modules/vsphere-tagging/main.tf — módulo reutilizable de etiquetado
resource "vsphere_tag_category" "propietario" {
  name        = "propietario"
  cardinality = "SINGLE"
  associable_types = ["VirtualMachine", "Datastore", "Network"]
}

resource "vsphere_tag_category" "ambiente" {
  name        = "ambiente"
  cardinality = "SINGLE"
  associable_types = ["VirtualMachine", "Datastore", "Network"]
}

resource "vsphere_tag_category" "gestionado_por" {
  name        = "gestionado_por"
  cardinality = "SINGLE"
  associable_types = ["VirtualMachine", "Datastore", "Network"]
}

resource "vsphere_tag" "propietario" {
  name        = var.propietario
  category_id = vsphere_tag_category.propietario.id
}

resource "vsphere_tag" "ambiente" {
  name        = var.ambiente
  category_id = vsphere_tag_category.ambiente.id
}

resource "vsphere_tag" "gestionado_por" {
  name        = "terraform"
  category_id = vsphere_tag_category.gestionado_por.id
}
```

Las categorías de etiqueta se crean **una sola vez** en el ambiente y se reutilizan en todos los recursos. Se recomienda encapsular esto en el módulo `modules/vsphere-tagging/` y consumirlo desde cada directorio de ambiente.

#### 8.6.3 Estructura de recursos vSphere habituales

| Recurso vSphere | Resource type Terraform |
|---|---|
| Máquina virtual | `vsphere_virtual_machine` |
| Red (port group distribuido) | `vsphere_distributed_port_group` |
| Datastore | referenciado como data source (`data.vsphere_datastore`) |
| Resource pool | referenciado como data source (`data.vsphere_resource_pool`) |
| Segmento NSX-T | `nsxt_policy_segment` |
| Grupo de seguridad NSX-T | `nsxt_policy_group` |
| Regla de firewall NSX-T | `nsxt_policy_security_policy` |

Los objetos de infraestructura existentes (datacenter, cluster, datastore, red) se referencian como **data sources**, no como recursos gestionados, para evitar que Terraform intente crearlos o destruirlos:

```hcl
data "vsphere_datacenter" "dc" {
  name = "ONP-DC"
}

data "vsphere_datastore" "ds" {
  name          = "DS-SAN-01"
  datacenter_id = data.vsphere_datacenter.dc.id
}

data "vsphere_network" "vlan_app" {
  name          = "VLAN-APP-100"
  datacenter_id = data.vsphere_datacenter.dc.id
}
```

---

## 9. Pipeline de validación de IaC

### 9.1 Etapas obligatorias (Fase 3 de IaC en adelante)

| Etapa | Comando | Obligatorio |
|---|---|---|
| Formato | `terraform fmt -check -recursive` | Sí, bloquea MR |
| Validación | `terraform validate` | Sí, bloquea MR |
| Plan | `terraform plan -out=tfplan` | Sí, publica como artefacto |
| Apply | `terraform apply tfplan` | Manual (**Fase 3/4 de IaC**); automatizado por ambiente en **Fase 5 de IaC**, siempre con aprobación en `prod` (§9.3) |

### 9.2 Pipeline referencial

Ver Anexo B para el `.gitlab-ci.yml` completo con autenticación al backend HTTP de GitLab.

### 9.3 Apply en producción

El `terraform apply` en `prod` **siempre requiere**:
- plan publicado y revisado;
- aprobación manual explícita de al menos un miembro de Plataforma en GitLab;
- tag de release asociado según `LIN-VER-001`.

---

## 10. Detección de drift

El *drift* ocurre cuando el estado real de la infraestructura difiere del estado declarado en código, generalmente por cambios manuales fuera de banda (vía consola web, CLI o emergencias).

### 10.1 Mecanismo

A partir de la **Fase 3 de IaC**, se configura un pipeline programado en GitLab (GitLab CI Scheduled Pipeline) que ejecuta `terraform plan` sin apply al menos una vez por semana en cada ambiente activo.

### 10.2 Tratamiento de desvíos

| Resultado del plan programado | Acción requerida |
|---|---|
| Sin cambios | Sin acción; evidencia de conformidad |
| Cambios detectados (drift) | Registrar en el issue tracker; resolver mediante MR al repositorio de IaC |
| Cambio manual intencional aceptado | Crear MR de sincronización para reflejar el estado real en código |

### 10.3 Regla

Ningún cambio manual en infraestructura gestionada por Terraform puede quedar sin trazabilidad en el repositorio de IaC. Si el cambio fue una emergencia, se registra como **excepción `EXC-IAC-NNN`** (ver §14) y se sincroniza en código dentro de 5 días hábiles.

> Un cambio manual de emergencia **no es un ADR**. Un ADR registra una decisión arquitectónica institucional que obliga al corpus (`LIN-ARQ-001`, Apéndice A); una emergencia operativa es una desviación puntual, con vigencia acotada y fecha de remediación. Confundirlos llena el registro institucional de incidencias y le resta valor.

---

## 11. Responsabilidades

| Rol | Responsabilidad |
|---|---|
| Plataforma / Infraestructura | Mantener el repositorio de IaC, ejecutar applies, gestionar el backend de estado, configurar runners y permisos |
| Arquitectura de Software | Definir lineamientos, revisar excepciones, aprobar ADRs, garantizar coherencia entre lineamientos |
| Seguridad Digital | Validar gestión de secretos, credenciales en pipelines y controles de acceso al repositorio de IaC |
| Desarrollo | Colaborar en la definición de recursos que sus sistemas requieren; no ejecutar cambios directos en el repositorio de IaC |
| Líder técnico | Asegurar que los cambios de infraestructura asociados a su sistema pasen por el proceso aprobado |

---

## 12. Checklist de conformidad

### 12.1 Repositorio

```text
[ ] Repositorio dedicado creado en GitLab (oti-plataforma/infrastructure-iac o equivalente)
[ ] Estructura de directorios environments/dev|qa|prod/ y modules/ creada desde el primer commit
[ ] .gitignore incluye *.tfstate, *.tfstate.backup, .terraform/, terraform.tfvars
[ ] README con instrucciones de inicialización y uso
[ ] Rama main protegida con MR obligatorio (no existe rama develop — ver §5.4)
```

### 12.2 Configuración de Terraform

```text
[ ] versions.tf presente en cada directorio de ambiente con required_version y required_providers
[ ] Todos los providers usan operador ~> para versionamiento
[ ] No se usan Terraform Workspaces para separar ambientes
[ ] Backend HTTP de GitLab configurado (Fase 2+)
[ ] terraform.tfvars excluido de Git; terraform.tfvars.example versionado
```

### 12.3 Secretos y variables

```text
[ ] Sin secretos, tokens ni contraseñas en ningún archivo del repositorio
[ ] Variables sensibles declaradas con sensitive = true
[ ] Credenciales inyectadas via TF_VAR_* como variables protegidas/enmascaradas en GitLab
[ ] terraform.tfvars.example no contiene valores reales
```

### 12.4 Pipeline

```text
[ ] .gitlab-ci.yml presente con stages: validate y plan
[ ] terraform fmt -check bloquea MR si falla
[ ] terraform validate bloquea MR si falla
[ ] terraform plan publica artefacto con el resultado
[ ] Pipeline de detección de drift programado semanalmente (Fase 3+)
[ ] Apply en producción requiere aprobación manual en GitLab
```

### 12.5 Buenas prácticas

```text
[ ] Todo recurso tiene etiquetas obligatorias (propietario, ambiente, sistema, centro_de_costo, gestionado_por)
[ ] Todas las variables tienen description y type
[ ] Todos los outputs tienen description
[ ] Nomenclatura de recursos sigue el patrón {sistema}-{ambiente}-{tipo}
```

---

## 13. Anti-patrones

| Anti-patrón | Riesgo | Regla |
|---|---|---|
| Cambios manuales directos en infraestructura gestionada por Terraform | Drift, pérdida de trazabilidad | Toda modificación debe pasar por MR y pipeline |
| `terraform apply` sin plan revisado | Cambios no intencionados en infraestructura | Plan + aprobación siempre |
| Estado de Terraform en archivo local versionado en Git | Expone recursos y credenciales; conflictos de concurrencia | Backend remoto desde Fase 2 |
| Secretos o tokens en archivos `.tf` o `terraform.tfvars` versionados | Exposición de credenciales | Variables protegidas GitLab CI/CD |
| Usar `terraform workspace` para separar ambientes | Riesgo de apply en ambiente incorrecto | Directorios independientes por ambiente |
| Provider sin versión o con versión no restringida | Roturas por upgrades automáticos | Operador `~>` obligatorio |
| Un solo directorio para todos los ambientes | Apply en dev rompe prod | Directorios separados desde el primer commit |
| `terraform apply` automático en producción sin aprobación | Riesgo de indisponibilidad no controlada | Aprobación manual obligatoria en prod |
| Variables sin `type` ni `description` | Módulos ilegibles e incorrectos | `type` y `description` obligatorios |
| Ignorar alertas de drift por semanas | Estado divergente acumulado | Resolver dentro de 5 días hábiles |
| Agregar archivos `.tf` directamente en pipelines de aplicación | Mezcla de responsabilidades | Solo en repositorio dedicado de IaC |
| `allow_unverified_ssl = true` en el provider vsphere | Habilita ataques man-in-the-middle contra vCenter | Instalar el certificado de la CA corporativa en el runner; nunca deshabilitar la verificación TLS |
| Usar credenciales personales de vCenter en el pipeline | Fuga de credenciales y pérdida de trazabilidad al rotar contraseñas | Crear una cuenta de servicio dedicada `svc-terraform@dominio` con permisos mínimos en vCenter |
| Gestionar con Terraform objetos vSphere pre-existentes como `resource` en lugar de `data` | Terraform intentará destruir y recrear el datacenter, cluster o datastore real | Los objetos de infraestructura existentes siempre como `data source`; solo los recursos nuevos como `resource` |

---

## 14. Proceso de excepción

> **Instrumento correcto: `EXC-IAC-NNN`, no un ADR.** Conforme a `GOB-MAT-001` (Registro de decisiones y excepciones), una desviación de este lineamiento en un proyecto concreto se registra como excepción con vigencia acotada y fecha de revisión. El `ADR-NNN` queda reservado a decisiones institucionales del Comité de Arquitectura, que obligan a todo el corpus.

Toda excepción a este lineamiento requiere un ADR aprobado por Arquitectura. Si afecta seguridad, producción o continuidad operativa, requiere validación adicional de Seguridad Digital y/o Plataforma.

### 14.1 Casos que requieren ADR

- No usar backend remoto en Fase 2 o superior.
- Usar Terraform Workspaces para separar ambientes.
- Aplicar cambios manuales de emergencia en infraestructura gestionada sin sincronización posterior.
- Usar herramienta IaC alternativa a Terraform.
- Ejecutar `terraform apply` automático en producción sin aprobación manual.
- No ejecutar detección de drift en ambiente de producción.
- Versionar secretos o tokens en el repositorio de IaC.

### 14.2 Formato mínimo

```markdown
# ADR-IAC-NNN — [Título]

## Contexto
[Descripción de la restricción o excepción requerida]

## Decisión
[Qué se permitirá excepcionalmente y por cuánto tiempo]

## Riesgo aceptado
[Riesgo operativo, seguridad, trazabilidad o continuidad]

## Control compensatorio
[Medida temporal o alternativa mientras dure la excepción]

## Fecha de revisión
[Fecha para reevaluar o resolver]

## Aprobaciones
[Arquitectura / Plataforma / Seguridad Digital, según corresponda]
```

---

## 15. Glosario

| Término | Definición |
|---|---|
| IaC | Infraestructura como Código; gestión declarativa y versionada de recursos de infraestructura |
| Terraform | Herramienta de IaC de HashiCorp para aprovisionamiento declarativo multi-provider |
| Provider | Plugin de Terraform que interactúa con una plataforma específica (Kubernetes, AWS, VMware, etc.) |
| State / Estado | Archivo que registra la correspondencia entre código Terraform y recursos reales aprovisionados |
| Backend | Destino remoto donde Terraform almacena y bloquea el archivo de estado |
| State locking | Mecanismo que previene modificaciones concurrentes del estado de Terraform |
| Drift | Desviación entre el estado real de la infraestructura y el estado declarado en código |
| Plan | Salida de `terraform plan`; muestra los cambios que se aplicarían sin ejecutarlos |
| Apply | Ejecución de `terraform apply`; materializa los cambios declarados en el código |
| Módulo | Componente reutilizable de Terraform que encapsula recursos relacionados |
| Workspace | Característica de Terraform para múltiples estados en la misma configuración; no recomendado para separar ambientes |
| GitOps | Modelo de operación donde Git es la única fuente de verdad para el estado de infraestructura y despliegues |
| Idempotencia | Propiedad por la cual aplicar el mismo código múltiples veces produce el mismo resultado |
| Inmutabilidad | Los recursos no se modifican in-place; se destruyen y recrean con la nueva configuración |
| Ansible | Herramienta de gestión de configuración complementaria a Terraform; gestiona SO, paquetes y servicios dentro de los recursos |
| ADR | Architecture Decision Record; documento que registra una decisión arquitectónica con su contexto y consecuencias |
| vSphere | Plataforma de virtualización de VMware; comprende el hipervisor ESXi y la capa de gestión vCenter |
| vCenter | Servidor de gestión centralizada de VMware; es el endpoint al que se conecta el provider `hashicorp/vsphere` |
| ESXi | Hipervisor bare-metal de VMware que ejecuta las máquinas virtuales |
| NSX-T | Plataforma de red definida por software (SDN) de VMware; gestionada con el provider `hashicorp/nsxt` |
| Data source | Construcción Terraform que lee el estado de un recurso existente sin gestionarlo; obligatorio para objetos vSphere pre-existentes (datacenters, clusters, datastores) |
| Cuenta de servicio | Cuenta técnica dedicada (`svc-terraform`) con permisos mínimos en vCenter usada por Terraform en pipelines; nunca credenciales personales |

---

## 16. Anexos

### Anexo A — Configuración del backend HTTP de GitLab

El bloque `backend "http"` se configura con los valores inyectados por el pipeline en el `before_script`. El `versions.tf` declara el bloque vacío:

```hcl
# versions.tf
terraform {
  required_version = "~> 1.7"

  backend "http" {}

  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.27"
    }
  }
}
```

El `terraform init` completo con los parámetros del backend se ejecuta en el pipeline (ver Anexo B). Los valores de `address`, `lock_address`, `username` y `password` son provistos en runtime por variables de GitLab CI/CD (`CI_API_V4_URL`, `CI_PROJECT_ID`, `CI_JOB_TOKEN`), no hardcodeados en código.

### Anexo B — Pipeline referencial de validación de IaC

```yaml
# .gitlab-ci.yml — Pipeline referencial LIN-IAC-001 (Fase 3)
# Repositorio: oti-plataforma/infrastructure-iac

variables:
  TF_IMAGE: hashicorp/terraform:1.7

# Función reutilizable de inicialización del backend por ambiente
.terraform_init: &terraform_init
  image: ${TF_IMAGE}
  before_script:
    - cd ${CI_PROJECT_DIR}/environments/${ENV_NAME}
    - terraform init
        -backend-config="address=${CI_API_V4_URL}/projects/${CI_PROJECT_ID}/terraform/state/${ENV_NAME}"
        -backend-config="lock_address=${CI_API_V4_URL}/projects/${CI_PROJECT_ID}/terraform/state/${ENV_NAME}/lock"
        -backend-config="unlock_address=${CI_API_V4_URL}/projects/${CI_PROJECT_ID}/terraform/state/${ENV_NAME}/lock"
        -backend-config="username=gitlab-ci-token"
        -backend-config="password=${CI_JOB_TOKEN}"
        -backend-config="lock_method=POST"
        -backend-config="unlock_method=DELETE"
        -backend-config="retry_wait_min=5"

stages:
  - validate
  - plan
  - drift

# Formato — se ejecuta una sola vez sobre todo el repositorio
fmt:
  image: ${TF_IMAGE}
  stage: validate
  script:
    - terraform fmt -check -recursive ${CI_PROJECT_DIR}
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"

# Plan por ambiente — se activa solo si cambiaron archivos en ese directorio
plan_dev:
  <<: *terraform_init
  stage: plan
  variables:
    ENV_NAME: dev
  script:
    - terraform validate
    - terraform plan -out=tfplan
    - terraform show -no-color tfplan > plan.txt
  artifacts:
    name: "plan-dev-${CI_COMMIT_SHORT_SHA}"
    paths: [plan.txt, tfplan]
    expose_as: "Terraform Plan dev"
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
      changes: [environments/dev/**/*]

plan_qa:
  <<: *terraform_init
  stage: plan
  variables:
    ENV_NAME: qa
  script:
    - terraform validate
    - terraform plan -out=tfplan
    - terraform show -no-color tfplan > plan.txt
  artifacts:
    name: "plan-qa-${CI_COMMIT_SHORT_SHA}"
    paths: [plan.txt, tfplan]
    expose_as: "Terraform Plan qa"
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
      changes: [environments/qa/**/*]

plan_prod:
  <<: *terraform_init
  stage: plan
  variables:
    ENV_NAME: prod
  script:
    - terraform validate
    - terraform plan -out=tfplan
    - terraform show -no-color tfplan > plan.txt
  artifacts:
    name: "plan-prod-${CI_COMMIT_SHORT_SHA}"
    paths: [plan.txt, tfplan]
    expose_as: "Terraform Plan prod"
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
      changes: [environments/prod/**/*]

# Detección de drift — pipeline programado semanal, un job por ambiente
drift_dev:
  <<: *terraform_init
  stage: drift
  variables:
    ENV_NAME: dev
  script:
    - terraform plan -detailed-exitcode
  rules:
    - if: $CI_PIPELINE_SOURCE == "schedule"

drift_qa:
  <<: *terraform_init
  stage: drift
  variables:
    ENV_NAME: qa
  script:
    - terraform plan -detailed-exitcode
  rules:
    - if: $CI_PIPELINE_SOURCE == "schedule"

drift_prod:
  <<: *terraform_init
  stage: drift
  variables:
    ENV_NAME: prod
  script:
    - terraform plan -detailed-exitcode
  rules:
    - if: $CI_PIPELINE_SOURCE == "schedule"
```

**Notas:**
- `CI_JOB_TOKEN` autentica al runner con el backend HTTP de GitLab sin necesidad de token personal.
- Los jobs `plan_*` usan `changes:` para activarse **solo si se modificaron archivos en ese directorio de ambiente** — coherente con TBD: una sola rama `main`, el directorio modificado determina qué ambiente se planea.
- El `apply` no aparece en este pipeline (Fase 3 es read-only). Se agrega como job `when: manual` en Fase 4 y automatizado por ambiente en Fase 5.
- Las variables `TF_VAR_*` con secretos deben declararse como **protegidas y enmascaradas** en GitLab CI/CD → Settings → Variables.
- Los jobs `drift_*` se ejecutan únicamente en Scheduled Pipelines de GitLab (pipeline programado semanal), no en MRs.

### Anexo C — Estructura de repositorio de referencia

```
infrastructure-iac/
├── .gitignore
│   # Contenido mínimo:
│   # .terraform/
│   # *.tfstate
│   # *.tfstate.backup
│   # terraform.tfvars
├── .gitlab-ci.yml
├── README.md
│   # Instrucciones: requisitos, inicialización local, cómo crear MR, cómo ejecutar plan
├── environments/
│   ├── dev/
│   │   ├── main.tf              # Recursos del ambiente dev
│   │   ├── variables.tf         # Variables con type y description
│   │   ├── outputs.tf           # Outputs con description
│   │   ├── versions.tf          # required_version y required_providers
│   │   └── terraform.tfvars.example
│   ├── qa/
│   │   └── (misma estructura)
│   └── prod/
│       └── (misma estructura)
└── modules/
    ├── kubernetes/
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    ├── networking/
    │   └── ...
    └── database/
        └── ...
```
