import { expect, test } from '@playwright/test';

import { iniciarSesionSimulada, mockUsuarioObtener, mockUsuariosListar } from '../support/mock-api';

const API_BASE = 'http://localhost:8080/api/v1';

const USUARIOS_MOCK = [
  { id: 1, nombre: 'Juan Pérez', dni: '12345678', correo: 'juan@onp.gob.pe', estado: 'ACTIVO' },
  { id: 2, nombre: 'Ana Torres', dni: '87654321', correo: 'ana@onp.gob.pe', estado: 'INACTIVO' },
];

test.describe('Listado de usuarios', () => {
  test.beforeEach(async ({ page }) => {
    await iniciarSesionSimulada(page);
  });

  test('listado con filtros muestra los resultados correctos', async ({ page }) => {
    await mockUsuariosListar(page, USUARIOS_MOCK);
    await page.goto('/usuarios');

    await expect(page.getByRole('cell', { name: 'Juan Pérez', exact: true })).toBeVisible();
    await expect(page.getByRole('cell', { name: 'Ana Torres', exact: true })).toBeVisible();

    // Aplicar un filtro y verificar que se reenvía la búsqueda con esos criterios
    let ultimaUrlSolicitada = '';
    await page.route(`${API_BASE}/usuarios**`, async (route) => {
      if (route.request().method() === 'GET') {
        ultimaUrlSolicitada = route.request().url();
      }
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          codHttp: 200,
          codDetRespuesta: '000',
          menDetRespuesta: 'Éxito',
          data: [USUARIOS_MOCK[0]],
          errors: null,
          meta: { timestamp: '', requestId: '', version: '1', totalElementos: 1 },
        }),
      });
    });

    await page.getByLabel('Nombre').fill('Juan');
    await page.getByRole('button', { name: 'Buscar' }).click();

    await expect(page.getByRole('cell', { name: 'Juan Pérez', exact: true })).toBeVisible();
    await expect(page.getByRole('cell', { name: 'Ana Torres', exact: true })).toHaveCount(0);
    expect(ultimaUrlSolicitada).toContain('nombre=Juan');
  });

  test('navega al detalle de un usuario desde el listado', async ({ page }) => {
    await mockUsuariosListar(page, USUARIOS_MOCK);
    await mockUsuarioObtener(page, 1, USUARIOS_MOCK[0]);

    await page.goto('/usuarios');
    await page.getByRole('link', { name: 'Ver Juan Pérez' }).click();

    await expect(page).toHaveURL(/\/usuarios\/1$/);
    await expect(page.locator('mat-card-title')).toHaveText('Juan Pérez');
    await expect(page.getByText('12345678')).toBeVisible();
  });
});
