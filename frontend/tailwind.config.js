SPDX-License-Identifier: Apache-2.0
/** 

 * Copyright 2025 Haui.HIT - H2K

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 */

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
                    DEFAULT: '#3b82f6',
                    50: '#eff6ff',
                    100: '#dbeafe',
                    200: '#bfdbfe',
                    300: '#93c5fd',
                    400: '#60a5fa',
                    500: '#3b82f6',
                    600: '#2563eb',
                    700: '#1d4ed8',
                    800: '#1e40af',
                    900: '#1e3a8a',
                },
                dark: {
                    DEFAULT: '#0a0e27',
                    lighter: '#131829',
                    card: '#1a1f3a',
                    border: '#2a2f4a',
                },
                light: {
                    DEFAULT: '#f9fafb',
                    lighter: '#f3f4f6',
                    card: '#ffffff',
                    border: '#e5e7eb',
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
                'gradient-cyber-light': 'linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%)',
            },
            boxShadow: {
                'neon': '0 0 20px rgba(59, 130, 246, 0.5)',
                'neon-sm': '0 0 10px rgba(59, 130, 246, 0.3)',
                'glow': '0 0 30px rgba(59, 130, 246, 0.4), inset 0 0 10px rgba(59, 130, 246, 0.1)',
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
