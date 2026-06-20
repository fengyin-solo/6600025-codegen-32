/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,ts}'],
  theme: {
    extend: {
      colors: {
        gray: {
          650: '#2d3748',
          750: '#1e293b',
        }
      }
    }
  },
  plugins: []
}
