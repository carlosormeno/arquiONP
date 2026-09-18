// lighthouserc.js — Gate de calidad Core Web Vitals (LIN-FE-ANG-001 §15.2)
//
// Los umbrales replicados aquí son los de aplicación directa a Angular según
// §15.2; el documento dueño de los siete umbrales de LIN-ARQ-001 §7.2
// (ARQ-R-007) es la fuente normativa — ver aviso en §15.2 del lineamiento.
// La integración en el pipeline CI/CD está en LIN-CICD-001 §9.4 (job
// `lighthouse`, gate de bloqueo).
module.exports = {
  ci: {
    collect: {
      url: ['http://localhost:4200'],
      startServerCommand: 'npx http-server dist/onp-template-frontend/browser -p 4200',
      numberOfRuns: 3,
    },
    assert: {
      assertions: {
        'categories:performance': ['error', { minScore: 0.85 }],
        'categories:accessibility': ['error', { minScore: 0.9 }],
        'first-contentful-paint': ['error', { maxNumericValue: 1800 }],
        'largest-contentful-paint': ['error', { maxNumericValue: 2500 }],
        'total-blocking-time': ['error', { maxNumericValue: 200 }],
        'cumulative-layout-shift': ['error', { maxNumericValue: 0.1 }],
        interactive: ['error', { maxNumericValue: 3500 }],
      },
    },
    upload: {
      target: 'temporary-public-storage',
    },
  },
};
