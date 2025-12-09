import { useAuthStore } from '~/stores/auth';

export default defineNuxtRouteMiddleware((to, from) => {
  const authStore = useAuthStore();
  
  // Initialize store if page refreshed
  if (!authStore.token) {
    authStore.initialize();
  }

  // Allow access to login and register pages without authentication
  const publicPaths = ['/login', '/register'];
  if (publicPaths.includes(to.path)) {
    // If already authenticated, redirect based on role
    if (authStore.isAuthenticated) {
      return navigateTo(authStore.isAdmin ? '/' : '/data-explorer');
    }
    return;
  }

  // Redirect unauthenticated users to login
  if (!authStore.isAuthenticated) {
    return navigateTo('/login');
  }

  // Admin-only routes - redirect non-admin users to data-explorer
  const adminOnlyPaths = ['/', '/system-control', '/nodes'];
  if (adminOnlyPaths.includes(to.path) && !authStore.isAdmin) {
    return navigateTo('/data-explorer');
  }
});
