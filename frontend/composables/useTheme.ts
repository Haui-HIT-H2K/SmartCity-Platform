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

export type Theme = 'light' | 'dark'

export const useTheme = () => {
  // Don't initialize with specific value on SSR - let client handle it
  const theme = useState<Theme | undefined>('theme', () => undefined)

  const getInitialTheme = (): Theme => {
    if (process.client) {
      const savedTheme = localStorage.getItem('theme') as Theme | null
      if (savedTheme) return savedTheme

      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      return prefersDark ? 'dark' : 'light'
    }
    return 'dark' // Fallback for SSR
  }

  const updateHtmlClass = (currentTheme: Theme) => {
    if (process.client) {
      const html = document.documentElement
      if (currentTheme === 'dark') {
        html.classList.add('dark')
        document.body.classList.add('dark')
      } else {
        html.classList.remove('dark')
        document.body.classList.remove('dark')
      }
    }
  }

  const setTheme = (newTheme: Theme) => {
    theme.value = newTheme
    if (process.client) {
      localStorage.setItem('theme', newTheme)
      updateHtmlClass(newTheme)
    }
  }

  const toggleTheme = () => {
    const currentTheme = theme.value || 'dark'
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark'
    setTheme(newTheme)
  }

  const initTheme = () => {
    if (process.client) {
      // Initialize theme from localStorage or system preference
      const initialTheme = getInitialTheme()
      theme.value = initialTheme
      updateHtmlClass(initialTheme)
    }
  }

  // Watch for system theme changes
  if (process.client) {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    const handleChange = (e: MediaQueryListEvent) => {
      // Only auto-switch if user hasn't set a preference
      if (!localStorage.getItem('theme')) {
        setTheme(e.matches ? 'dark' : 'light')
      }
    }
    mediaQuery.addEventListener('change', handleChange)

    onUnmounted(() => {
      mediaQuery.removeEventListener('change', handleChange)
    })
  }

  return {
    theme: readonly(theme) as Readonly<Ref<Theme>>,
    setTheme,
    toggleTheme,
    initTheme,
    isDark: computed(() => theme.value === 'dark'),
    isLight: computed(() => theme.value === 'light'),
  }
}

