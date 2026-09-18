// tailwind.config.js — LIN-FE-ANG-001 §8.5
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        'onp-primary': 'var(--onp-primary)',
        'onp-accent': 'var(--onp-accent)',
        'onp-success': 'var(--onp-success)',
        'onp-error': 'var(--onp-error)',
      },
      fontFamily: {
        onp: 'var(--onp-font-family)',
      },
    },
  },
  // Evitar conflictos con Angular Material (preflight resetea estilos base)
  corePlugins: {
    preflight: false,
  },
};
