/**
 * Copyright 2025 Haui.HIT - H2K
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
