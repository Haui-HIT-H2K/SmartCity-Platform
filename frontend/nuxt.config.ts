import { defineNuxtConfig } from "nuxt/config";

// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },

  modules: [
    '@nuxtjs/tailwindcss',
    '@pinia/nuxt',
  ],


  app: {
    head: {
      title: 'Smart City Dashboard',
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        { name: 'description', content: 'Real-time IoT monitoring dashboard for Smart City Platform' }
      ],
      link: [
        { rel: 'preconnect', href: 'https://fonts.googleapis.com' },
        { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: '' },
        { rel: 'stylesheet', href: 'https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap' }
      ]
    }
  },

  nitro: {
    devProxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    },
    // Enable compression
    compressPublicAssets: true,
    minify: true
  },

  // Route-level caching rules
  routeRules: {
    '/api/data': {
      cache: {
        maxAge: 30, // 30 seconds cache
        swr: true   // Stale-while-revalidate
      }
    },
    '/api/data/**': {
      cache: {
        maxAge: 300 // 5 minutes for individual records
      }
    }
  },

  runtimeConfig: {
    public: {
      // Backend URL - use correct Docker container name
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://smart-city-backend:8080'
    }
  },

  ssr: true,
})
