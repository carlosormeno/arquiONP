import { defineConfig, devices } from '@playwright/test';

/**
 * Configuración Playwright — herramienta e2e institucional ONP.
 * Ver `LIN-FE-ANG-001 §14.2` y `LIN-TEST-001 §3.3`/`§12.4`.
 *
 * Las pruebas interceptan las llamadas a la API (`page.route`) para no
 * depender de un backend real: verifican el comportamiento del frontend
 * frente a los contratos de `ApiResponseWrapper` (§9.1), no la integración
 * de punta a punta con un core Spring Boot.
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env['CI'],
  retries: process.env['CI'] ? 2 : 0,
  reporter: 'list',
  use: {
    baseURL: 'http://localhost:4200',
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: {
    command: 'npx ng serve --configuration development',
    url: 'http://localhost:4200',
    reuseExistingServer: !process.env['CI'],
    timeout: 120_000,
  },
});
