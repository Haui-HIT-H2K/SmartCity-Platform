// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: "2025-07-15",
  devtools: { enabled: true },
  srcDir: "src/",
  ssr: false,
  css: ["leaflet/dist/leaflet.css", "~/assets/css/main.css"],
});
