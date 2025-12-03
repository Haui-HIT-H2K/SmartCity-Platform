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
