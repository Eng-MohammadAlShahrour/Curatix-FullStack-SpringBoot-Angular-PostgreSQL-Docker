/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        navy: {
          50:  '#F0F4F8',
          100: '#D9E2EC',
          500: '#2D4A6B',
          700: '#152F4A',
          900: '#0A2540',
        },
        gold: {
          50:  '#FBF7ED',
          100: '#F4EAD1',
          400: '#D4B876',
          500: '#C9A961',
          600: '#B08D45',
          700: '#8A6D2F',
        },
        cream: {
          DEFAULT: '#F8F7F4',
          50:  '#FEFDFB',
          100: '#F8F7F4',
          200: '#EEEBE3',
        },
      },
      fontFamily: {
        serif: ['"Playfair Display"', 'Georgia', 'serif'],
        sans:  ['Inter', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        card: '0 1px 3px rgba(10,37,64,0.06), 0 4px 16px rgba(10,37,64,0.04)',
        'card-hover': '0 4px 12px rgba(10,37,64,0.10), 0 8px 32px rgba(10,37,64,0.08)',
      },
    },
  },
  plugins: [],
};
