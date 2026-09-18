import { expect, test } from '@playwright/test';

import { iniciarSesionSimulada, mockLoginExitoso, mockLoginFallido } from '../support/mock-api';

/**
 * Flujos obligatorios de LIN-FE-ANG-001 §14.2: login exitoso, login fallido
 * y acceso a ruta protegida sin sesión.
 */
test.describe('Login', () => {
  test('login exitoso redirige a Home', async ({ page }) => {
    await mockLoginExitoso(page);
    await page.goto('/login');

    await page.getByLabel('Usuario *').fill('jperez');
    await page.getByLabel('Clave *').fill('secreto123');
    await page.getByRole('button', { name: 'Ingresar' }).click();

    await expect(page).toHaveURL(/\/home$/);
  });

  test('login fallido muestra un mensaje de error visible', async ({ page }) => {
    await mockLoginFallido(page);
    await page.goto('/login');

    await page.getByLabel('Usuario *').fill('jperez');
    await page.getByLabel('Clave *').fill('clave-incorrecta');
    await page.getByRole('button', { name: 'Ingresar' }).click();

    await expect(page.getByText('Usuario o clave incorrectos.')).toBeVisible();
    await expect(page).toHaveURL(/\/login$/);
  });
});

test.describe('Protección de rutas', () => {
  test('acceso a ruta protegida sin sesión redirige a /login', async ({ page }) => {
    await page.goto('/home');
    await expect(page).toHaveURL(/\/login$/);
  });

  test('con sesión activa se accede directamente a Home', async ({ page }) => {
    await iniciarSesionSimulada(page);
    await page.goto('/home');
    await expect(page).toHaveURL(/\/home$/);
  });
});
