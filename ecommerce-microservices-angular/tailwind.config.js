/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#FF6584', // Cute Vibrant Pink
        'primary-dark': '#E05470',
        secondary: '#43D9C0', // Mint
        accent: '#FFD93D', // Yellow
        surface: '#ffffff',
        background: '#FFF5F7', // Very Subtle Pink background
        text: '#4A4A4A', // Softer Black
        muted: '#8D99AE'
      },
      fontFamily: {
        sans: ['Quicksand', 'sans-serif'],
      },
      borderRadius: {
        'xl': '1rem',
        '2xl': '1.5rem',
        '3xl': '2rem', // More rounded
      },
      boxShadow: {
        'cute': '0 10px 25px -5px rgba(255, 101, 132, 0.15), 0 8px 10px -6px rgba(255, 101, 132, 0.1)',
      }
    },
  },
  plugins: [],
}
