import { expect, test } from '@playwright/test';

import {
  iniciarSesionSimulada,
  mockUsuarioCrearExitoso,
  mockUsuarioCrearInvalido,
  mockUsuariosListar,
} from '../support/mock-api';

test.describe('Formulario de usuarios', () => {
  test.beforeEach(async ({ page }) => {
    await iniciarSesionSimulada(page);
  });

  test('alta exitosa muestra confirmación y regresa al listado', async ({ page }) => {
    await mockUsuarioCrearExitoso(page);
    await mockUsuariosListar(page, []);

    await page.goto('/usuarios/new');
    await page.getByLabel('Nombre *').fill('Nuevo Usuario');
    await page.getByLabel('DNI *').fill('11223344');
    await page.getByLabel('Correo *').fill('nuevo@onp.gob.pe');
    await page.getByRole('button', { name: 'Guardar' }).click();

    await expect(page.getByText('Usuario creado.')).toBeVisible();
    await expect(page).toHaveURL(/\/usuarios$/);
  });

  test('alta con datos inválidos muestra errores por campo sin llamar a la API', async ({ page }) => {
    await page.goto('/usuarios/new');

    let seLlamoALaApi = false;
    await page.route('**/usuarios', async (route) => {
      if (route.request().method() === 'POST') {
        seLlamoALaApi = true;
      }
      await route.continue();
    });

    // Formulario vacío: dispara validaciones "required" sin llegar a la API.
    await page.getByRole('button', { name: 'Guardar' }).click();

    await expect(page.getByText('Campo requerido').first()).toBeVisible();
    expect(seLlamoALaApi).toBe(false);
  });

  test('la API rechaza el alta y el error se mapea al campo correspondiente', async ({ page }) => {
    await mockUsuarioCrearInvalido(page);

    await page.goto('/usuarios/new');
    await page.getByLabel('Nombre *').fill('Nuevo Usuario');
    await page.getByLabel('DNI *').fill('11223344');
    await page.getByLabel('Correo *').fill('repetido@onp.gob.pe');
    await page.getByRole('button', { name: 'Guardar' }).click();

    await expect(page.getByText('El correo ya está registrado')).toBeVisible();
    await expect(page).toHaveURL(/\/usuarios\/new$/);
  });
});
