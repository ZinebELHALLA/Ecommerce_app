/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#7da0fa', // Soft Purple-Blue
        'primary-dark': '#5b7bbd',
        secondary: '#f3a4b5', // Pastel Pink
        surface: '#ffffff',
        background: '#f8f9fa',
        text: '#333333',
        muted: '#6c757d'
      },
      fontFamily: {
        sans: ['Quicksand', 'sans-serif'],
      },
      borderRadius: {
        'xl': '1rem',
        '2xl': '1.5rem',
      }
    },
  },
  plugins: [],
}
