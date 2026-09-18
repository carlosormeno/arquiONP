import { Page } from '@playwright/test';

/**
 * Helpers para simular las respuestas del core Spring Boot en las pruebas
 * e2e, respetando el contrato `ApiResponseWrapper` de `LIN-FE-ANG-001 §9.1`.
 * Evita que la suite e2e dependa de un backend real desplegado.
 *
 * Los patrones de `page.route()` usan la URL absoluta de `environment.apiUrl`
 * (entorno de desarrollo, `http://localhost:8080/api/v1`) en vez de un patrón
 * relativo tipo `**\/usuarios*`: un patrón relativo también intercepta la
 * navegación de nivel superior de Playwright hacia la propia ruta SPA
 * `http://localhost:4200/usuarios` (mismo sufijo de path), sirviendo el JSON
 * de la API en lugar de `index.html` y rompiendo el arranque de Angular.
 */
const API_BASE = 'http://localhost:8080/api/v1';

export async function mockLoginExitoso(page: Page): Promise<void> {
  await page.route(`${API_BASE}/auth/login`, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        codHttp: 200,
        codDetRespuesta: '000',
        menDetRespuesta: 'Éxito',
        data: { token: 'tok-e2e-123', nombre: 'Juan Pérez', rol: 'ADMIN' },
        errors: null,
        meta: { timestamp: new Date().toISOString(), requestId: 'e2e-req-1', version: '1' },
      }),
    });
  });
}

export async function mockLoginFallido(page: Page): Promise<void> {
  await page.route(`${API_BASE}/auth/login`, async (route) => {
    await route.fulfill({
      status: 401,
      contentType: 'application/json',
      body: JSON.stringify({
        codHttp: 401,
        codDetRespuesta: '300',
        menDetRespuesta: 'Credenciales inválidas',
        data: null,
        errors: [{ campo: 'clave', mensaje: 'Usuario o clave incorrectos' }],
        meta: null,
      }),
    });
  });
}

export async function mockUsuariosListar(page: Page, usuarios: unknown[]): Promise<void> {
  await page.route(`${API_BASE}/usuarios**`, async (route) => {
    if (route.request().method() !== 'GET') {
      await route.fallback();
      return;
    }
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        codHttp: 200,
        codDetRespuesta: '000',
        menDetRespuesta: 'Éxito',
        data: usuarios,
        errors: null,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: 'e2e-req-2',
          version: '1',
          totalElementos: usuarios.length,
        },
      }),
    });
  });
}

export async function mockUsuarioObtener(page: Page, id: number, usuario: unknown): Promise<void> {
  await page.route(`${API_BASE}/usuarios/${id}`, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        codHttp: 200,
        codDetRespuesta: '000',
        menDetRespuesta: 'Éxito',
        data: usuario,
        errors: null,
        meta: null,
      }),
    });
  });
}

export async function mockUsuarioCrearExitoso(page: Page): Promise<void> {
  await page.route(`${API_BASE}/usuarios`, async (route) => {
    if (route.request().method() !== 'POST') {
      await route.fallback();
      return;
    }
    await route.fulfill({
      status: 201,
      contentType: 'application/json',
      body: JSON.stringify({
        codHttp: 201,
        codDetRespuesta: '000',
        menDetRespuesta: 'Usuario creado',
        data: { id: 99, nombre: 'Nuevo Usuario', dni: '11223344', correo: 'nuevo@onp.gob.pe', estado: 'ACTIVO' },
        errors: null,
        meta: null,
      }),
    });
  });
}

export async function mockUsuarioCrearInvalido(page: Page): Promise<void> {
  await page.route(`${API_BASE}/usuarios`, async (route) => {
    if (route.request().method() !== 'POST') {
      await route.fallback();
      return;
    }
    await route.fulfill({
      status: 400,
      contentType: 'application/json',
      body: JSON.stringify({
        codHttp: 400,
        codDetRespuesta: '100',
        menDetRespuesta: 'Error de validación',
        data: null,
        errors: [{ campo: 'correo', mensaje: 'El correo ya está registrado' }],
        meta: null,
      }),
    });
  });
}

/** Deja una sesión SAA activa en sessionStorage, replicando AuthService. */
export async function iniciarSesionSimulada(page: Page): Promise<void> {
  await page.addInitScript(() => {
    sessionStorage.setItem(
      'onp.saa.token',
      JSON.stringify({ token: 'tok-e2e-123', nombre: 'Juan Pérez', rol: 'ADMIN' }),
    );
  });
}
