/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./components/**/*.{js,vue,ts}",
        "./layouts/**/*.vue",
        "./pages/**/*.vue",
        "./plugins/**/*.{js,ts}",
        "./app.vue",
    ],
    darkMode: 'class',
    theme: {
        extend: {
            fontFamily: {
                sans: ['Inter', 'sans-serif'],
            },
            colors: {
                primary: {
                    DEFAULT: '#00f0ff',
                    50: '#e6feff',
                    100: '#ccfdff',
                    200: '#99fbff',
                    300: '#66f9ff',
                    400: '#33f6ff',
                    500: '#00f0ff',
                    600: '#00c0cc',
                    700: '#009099',
                    800: '#006066',
                    900: '#003033',
                },
                dark: {
                    DEFAULT: '#0a0e27',
                    lighter: '#131829',
                    card: '#1a1f3a',
                    border: '#2a2f4a',
                },
                light: {
                    DEFAULT: '#f8fafc',
                    lighter: '#f1f5f9',
                    card: '#ffffff',
                    border: '#e2e8f0',
                },
                neon: {
                    blue: '#00f0ff',
                    pink: '#ff00ff',
                    green: '#00ff41',
                    purple: '#9d00ff',
                },
            },
            backgroundImage: {
                'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
                'gradient-cyber': 'linear-gradient(135deg, #0a0e27 0%, #1a1f3a 100%)',
                'gradient-cyber-light': 'linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%)',
            },
            boxShadow: {
                'neon': '0 0 20px rgba(0, 240, 255, 0.5)',
                'neon-sm': '0 0 10px rgba(0, 240, 255, 0.3)',
                'glow': '0 0 30px rgba(0, 240, 255, 0.4), inset 0 0 10px rgba(0, 240, 255, 0.1)',
            },
            animation: {
                'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
                'glow': 'glow 2s ease-in-out infinite alternate',
                'slide-in': 'slideIn 0.3s ease-out',
            },
            keyframes: {
                glow: {
                    '0%': { boxShadow: '0 0 20px rgba(0, 240, 255, 0.5)' },
                    '100%': { boxShadow: '0 0 30px rgba(0, 240, 255, 0.8), 0 0 40px rgba(0, 240, 255, 0.4)' },
                },
                slideIn: {
                    '0%': { transform: 'translateY(-10px)', opacity: '0' },
                    '100%': { transform: 'translateY(0)', opacity: '1' },
                },
            },
        },
    },
    plugins: [],
}
