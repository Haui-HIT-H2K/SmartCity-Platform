<template>
  <div>
    <!-- Form Title -->
    <div class="text-center mb-6">
      <h2 class="text-2xl font-bold text-white mb-2">Đăng nhập</h2>
      <p class="text-gray-400 text-sm">Nhập thông tin để truy cập hệ thống</p>
    </div>

    <!-- Login Form -->
    <form @submit.prevent="handleLogin" class="space-y-5">
      <!-- Email Input -->
      <div>
        <label for="email" class="block text-sm font-medium text-gray-300 mb-2">
          Email
        </label>
        <div class="relative">
          <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
            <Mail class="w-5 h-5 text-gray-400" />
          </div>
          <input
            id="email"
            v-model="form.email"
            type="email"
            required
            placeholder="your@email.com"
            class="w-full pl-12 pr-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all duration-300"
          />
        </div>
      </div>

      <!-- Password Input -->
      <div>
        <label for="password" class="block text-sm font-medium text-gray-300 mb-2">
          Mật khẩu
        </label>
        <div class="relative">
          <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
            <Lock class="w-5 h-5 text-gray-400" />
          </div>
          <input
            id="password"
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            required
            placeholder="••••••••"
            class="w-full pl-12 pr-12 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all duration-300"
          />
          <button
            type="button"
            @click="showPassword = !showPassword"
            class="absolute inset-y-0 right-0 pr-4 flex items-center text-gray-400 hover:text-gray-300 transition-colors"
          >
            <EyeOff v-if="showPassword" class="w-5 h-5" />
            <Eye v-else class="w-5 h-5" />
          </button>
        </div>
      </div>

      <!-- Error Message -->
      <div v-if="error" class="flex items-center gap-2 p-3 bg-red-500/10 border border-red-500/30 rounded-xl">
        <AlertCircle class="w-5 h-5 text-red-400 flex-shrink-0" />
        <span class="text-red-400 text-sm">{{ error }}</span>
      </div>

      <!-- Login Button -->
      <button
        type="submit"
        :disabled="loading"
        class="w-full py-3 px-4 bg-gradient-to-r from-cyan-500 to-blue-500 hover:from-cyan-400 hover:to-blue-400 text-white font-semibold rounded-xl shadow-lg shadow-cyan-500/25 hover:shadow-cyan-500/40 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
      >
        <Loader2 v-if="loading" class="w-5 h-5 animate-spin" />
        <LogIn v-else class="w-5 h-5" />
        <span>Đăng nhập</span>
      </button>
    </form>

    <!-- Divider -->
    <div class="relative my-6">
      <div class="absolute inset-0 flex items-center">
        <div class="w-full border-t border-white/10"></div>
      </div>
      <div class="relative flex justify-center text-sm">
        <span class="px-4 bg-transparent text-gray-500">Hoặc</span>
      </div>
    </div>

    <!-- Register Button -->
    <NuxtLink
      to="/register"
      class="w-full py-3 px-4 bg-white/5 hover:bg-white/10 border border-white/10 hover:border-white/20 text-white font-semibold rounded-xl transition-all duration-300 flex items-center justify-center gap-2"
    >
      <UserPlus class="w-5 h-5" />
      <span>Tạo tài khoản mới</span>
    </NuxtLink>
  </div>
</template>

<script setup lang="ts">
import { Mail, Lock, Eye, EyeOff, LogIn, UserPlus, AlertCircle, Loader2 } from 'lucide-vue-next'
import { useAuthStore } from '~/stores/auth'

definePageMeta({
  layout: 'auth'
})

const authStore = useAuthStore()
const router = useRouter()

const form = ref({
  email: '',
  password: ''
})
const showPassword = ref(false)
const error = ref('')
const loading = ref(false)

const handleLogin = async () => {
  loading.value = true
  error.value = ''

  try {
    const response: any = await $fetch('http://localhost:8080/api/auth/signin', {
      method: 'POST',
      body: {
        username: form.value.email,
        password: form.value.password
      }
    })

    if (response && response.token) {
      authStore.setToken(response.token)
      authStore.setUser({
        id: response.id,
        username: response.username,
        roles: response.roles
      })
      
      // Redirect based on role
      const isAdmin = response.roles?.includes('ROLE_ADMIN')
      await navigateTo(isAdmin ? '/' : '/data-explorer')
    } else {
      throw new Error('Đăng nhập thất bại')
    }
  } catch (err: any) {
    console.error('Login error:', err)
    error.value = err.data?.message || err.message || 'Đăng nhập thất bại'
  } finally {
    loading.value = false
  }
}
</script>
