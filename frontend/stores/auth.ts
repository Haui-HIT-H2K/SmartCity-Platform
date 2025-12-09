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
        this.user = userCookie.value as User;
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
