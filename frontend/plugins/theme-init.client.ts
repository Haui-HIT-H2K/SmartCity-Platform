/** 

 * Copyright 2025 Haui.HIT - H2K

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 */

// Apply theme immediately on page load to prevent flash
export default defineNuxtPlugin({
    name: 'theme-init',
    enforce: 'pre', // Run before other plugins
    setup() {
        if (process.client) {
            // Apply theme class immediately, before Vue hydration
            const savedTheme = localStorage.getItem('theme')
            const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
            const theme = savedTheme || (prefersDark ? 'dark' : 'light')

            const html = document.documentElement
            const body = document.body

            if (theme === 'dark') {
                html.classList.add('dark')
                body.classList.add('dark')
            } else {
                html.classList.remove('dark')
                body.classList.remove('dark')
            }
        }
    }
})
