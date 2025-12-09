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

import { defineStore } from 'pinia';

interface User {
  id: string;
  username: string;
  roles: string[];
}

interface AuthState {
  user: User | null;
  token: string | null;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: null,
    token: null,
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => state.user?.roles.includes('ROLE_ADMIN') || false,
  },
  actions: {
    setToken(token: string) {
      this.token = token;
      const cookie = useCookie('auth_token');
      cookie.value = token;
    },
    setUser(user: User) {
      this.user = user;
      const cookie = useCookie('auth_user');
      cookie.value = JSON.stringify(user);
    },
    initialize() {
      const tokenCookie = useCookie('auth_token');
      const userCookie = useCookie('auth_user');
      
      if (tokenCookie.value) {
        this.token = tokenCookie.value as string;
      }
      if (userCookie.value) {
        this.user = typeof userCookie.value === 'string' 
          ? JSON.parse(userCookie.value) as User 
          : userCookie.value as User;
      }
    },
    logout() {
      this.token = null;
      this.user = null;
      const tokenCookie = useCookie('auth_token');
      const userCookie = useCookie('auth_user');
      tokenCookie.value = null;
      userCookie.value = null;
      navigateTo('/login');
    },
  },
});
