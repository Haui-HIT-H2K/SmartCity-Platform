// Leaflet client-side plugin for Nuxt 3
// This ensures Leaflet CSS is loaded only on client side

export default defineNuxtPlugin(() => {
  // Import Leaflet CSS on client side only
  if (process.client) {
    import('leaflet/dist/leaflet.css')
  }
})
