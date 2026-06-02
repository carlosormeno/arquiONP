# [Nombre del Sistema] — Frontend Angular

> **EJEMPLO DE REFERENCIA — LIN-VER-001 Anexo F**
> Este README es la estructura mínima institucional. Reemplazar los placeholders con información real del sistema.
> Plataforma/Infraestructura es responsable de mantener el proyecto plantilla en GitLab Ultimate.

## Descripción

[Descripción breve del componente: qué hace, a qué sistema pertenece, qué dominio funcional atiende]

**Código de sistema:** [ej. NOTIFICACION_ELECTRONICA]  
**Tipo:** Frontend Angular SPA  
**Propietario técnico:** [nombre del líder técnico]  
**Código lineamiento:** LIN-VER-001  

## Prerrequisitos

| Herramienta | Versión mínima | Notas |
|---|---|---|
| Node.js | 20 LTS | |
| npm | 10+ | |
| Angular CLI | 17+ | `npm install -g @angular/cli` |
| Docker | 24+ | Para build de imagen |
| kubectl + kustomize | 1.27+ / 5+ | Para despliegue en K8s |

## Ejecución local

```bash
# Clonar el repositorio
git clone https://gitlab.onp.gob.pe/aplicaciones/nombre-sistema/frontend-nombre-sistema.git

# Instalar dependencias
npm ci

# Ejecutar en modo desarrollo
ng serve

# Ejecutar pruebas unitarias
ng test --watch=false

# Ejecutar pruebas E2E (Playwright)
npx playwright test
```

> Para desarrollo local, copiar `src/environments/environment.local.ts.example` a `environment.local.ts`
> y configurar las URLs de APIs locales. El archivo `.local.ts` está excluido del repositorio.

## Estructura del proyecto

```
src/
├── app/
│   ├── core/            # Módulo core: guards, interceptors, servicios singleton
│   ├── shared/          # Componentes, pipes y directivas reutilizables
│   └── features/        # Módulos funcionales por dominio
├── environments/        # Configuraciones por ambiente
└── assets/              # Recursos estáticos
e2e/                     # Pruebas E2E Playwright
docs/
└── adr/                 # Architecture Decision Records
k8s/
├── base/                # Manifiestos base (Kustomize)
└── overlays/            # Overlays por ambiente: dev, qa, prod
```

## Registro de imagen

```
registry.gitlab.onp.gob.pe/aplicaciones/nombre-sistema/frontend-nombre-sistema:<version>
```

Ver LIN-K8S-001 para el proceso completo de construcción y promoción de imágenes.

## Configuración nginx

El contenedor de producción usa nginx con `try_files` para soportar el enrutamiento Angular SPA.
Ver `nginx.conf` en la raíz y LIN-K8S-001 Anexo D para el detalle.

## Contacto

- **Líder técnico:** [nombre]
- **Arquitectura OTI:** arquitectura@onp.gob.pe
- **Plataforma/Infraestructura:** [canal interno]
