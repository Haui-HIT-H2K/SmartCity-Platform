<!--
  Copyright 2025 Haui.HIT - H2K

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<template>
  <div>
    <!-- Form Title -->
    <div class="text-center mb-6">
      <h2 class="text-2xl font-bold text-white mb-2">Đăng ký tài khoản</h2>
      <p class="text-gray-400 text-sm">Tạo tài khoản để sử dụng hệ thống</p>
    </div>

    <!-- Register Form -->
    <form @submit.prevent="handleRegister" class="space-y-5">
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
            :disabled="otpSent"
            placeholder="your@email.com"
            class="w-full pl-12 pr-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
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
            :disabled="otpSent"
            placeholder="••••••••"
            minlength="6"
            class="w-full pl-12 pr-12 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
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

      <!-- OTP Section -->
      <div>
        <label for="otp" class="block text-sm font-medium text-gray-300 mb-2">
          Mã OTP
        </label>
        <div class="flex gap-3">
          <div class="relative flex-1">
            <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
              <KeyRound class="w-5 h-5 text-gray-400" />
            </div>
            <input
              id="otp"
              v-model="form.otp"
              type="text"
              maxlength="6"
              :required="otpSent"
              :disabled="!otpSent"
              placeholder="Nhập mã 6 số"
              class="w-full pl-12 pr-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed tracking-widest font-mono text-lg"
            />
          </div>
          <button
            type="button"
            @click="sendOtp"
            :disabled="sendingOtp || !form.email || !form.password || (otpSent && countdown > 0)"
            class="px-4 py-3 bg-purple-500/20 hover:bg-purple-500/30 border border-purple-500/30 text-purple-400 hover:text-purple-300 font-medium rounded-xl transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap flex items-center gap-2"
          >
            <Loader2 v-if="sendingOtp" class="w-4 h-4 animate-spin" />
            <Send v-else class="w-4 h-4" />
            <span v-if="countdown > 0">{{ countdown }}s</span>
            <span v-else>{{ otpSent ? 'Gửi lại' : 'Gửi mã' }}</span>
          </button>
        </div>
        <p v-if="otpSent" class="text-xs text-green-400 mt-2 flex items-center gap-1">
          <CheckCircle class="w-4 h-4" />
          Mã OTP đã được gửi đến email của bạn
        </p>
      </div>

      <!-- Error Message -->
      <div v-if="error" class="flex items-center gap-2 p-3 bg-red-500/10 border border-red-500/30 rounded-xl">
        <AlertCircle class="w-5 h-5 text-red-400 flex-shrink-0" />
        <span class="text-red-400 text-sm">{{ error }}</span>
      </div>

      <!-- Success Message -->
      <div v-if="success" class="flex items-center gap-2 p-3 bg-green-500/10 border border-green-500/30 rounded-xl">
        <CheckCircle class="w-5 h-5 text-green-400 flex-shrink-0" />
        <span class="text-green-400 text-sm">Đăng ký thành công! Đang chuyển hướng...</span>
      </div>

      <!-- Register Button -->
      <button
        type="submit"
        :disabled="loading || !otpVerified"
        class="w-full py-3 px-4 bg-gradient-to-r from-purple-500 to-pink-500 hover:from-purple-400 hover:to-pink-400 text-white font-semibold rounded-xl shadow-lg shadow-purple-500/25 hover:shadow-purple-500/40 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
      >
        <Loader2 v-if="loading" class="w-5 h-5 animate-spin" />
        <UserPlus v-else class="w-5 h-5" />
        <span>Đăng ký</span>
      </button>
    </form>

    <!-- Divider -->
    <div class="relative my-6">
      <div class="absolute inset-0 flex items-center">
        <div class="w-full border-t border-white/10"></div>
      </div>
      <div class="relative flex justify-center text-sm">
        <span class="px-4 bg-transparent text-gray-500">Đã có tài khoản?</span>
      </div>
    </div>

    <!-- Login Button -->
    <NuxtLink
      to="/login"
      class="w-full py-3 px-4 bg-white/5 hover:bg-white/10 border border-white/10 hover:border-white/20 text-white font-semibold rounded-xl transition-all duration-300 flex items-center justify-center gap-2"
    >
      <LogIn class="w-5 h-5" />
      <span>Đăng nhập</span>
    </NuxtLink>
  </div>
</template>

<script setup lang="ts">
import { Mail, Lock, Eye, EyeOff, LogIn, UserPlus, AlertCircle, CheckCircle, Loader2, Send, KeyRound } from 'lucide-vue-next'

definePageMeta({
  layout: 'auth'
})

const router = useRouter()

const form = ref({
  email: '',
  password: '',
  otp: ''
})
const showPassword = ref(false)
const error = ref('')
const success = ref(false)
const loading = ref(false)
const sendingOtp = ref(false)
const otpSent = ref(false)
const otpVerified = ref(false)
const countdown = ref(0)
const verifyingOtp = ref(false)

// Watch OTP input to auto-verify when 6 digits entered
watch(() => form.value.otp, async (newOtp) => {
  if (newOtp.length === 6 && otpSent.value && !verifyingOtp.value) {
    verifyingOtp.value = true
    error.value = ''
    
    try {
      const response: any = await $fetch('http://localhost:8080/api/auth/verify-otp', {
        method: 'POST',
        body: {
          email: form.value.email,
          otp: newOtp
        }
      })
      
      if (response && response.verified) {
        otpVerified.value = true
        error.value = ''
      } else {
        otpVerified.value = false
        error.value = response?.message || 'Mã OTP không đúng hoặc đã hết hạn'
      }
    } catch (err: any) {
      otpVerified.value = false
      error.value = 'Không thể xác thực OTP. Vui lòng thử lại.'
    } finally {
      verifyingOtp.value = false
    }
  } else if (newOtp.length < 6) {
    otpVerified.value = false
    if (error.value === 'Mã OTP không đúng hoặc đã hết hạn' || error.value === 'Không thể xác thực OTP. Vui lòng thử lại.') {
      error.value = ''
    }
  }
})

const sendOtp = async () => {
  if (!form.value.email || !form.value.password) {
    error.value = 'Vui lòng nhập email và mật khẩu trước'
    return
  }

  sendingOtp.value = true
  error.value = ''

  try {
    const response: any = await $fetch('http://localhost:8080/api/auth/send-otp', {
      method: 'POST',
      body: {
        email: form.value.email
      }
    })

    if (response) {
      otpSent.value = true
      
      // Start countdown
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    }

  } catch (err: any) {
    console.error('Send OTP error:', err)
    error.value = err.data?.message || 'Không thể gửi mã OTP. Vui lòng thử lại.'
  } finally {
    sendingOtp.value = false
  }
}

const handleRegister = async () => {
  if (!otpVerified.value) {
    error.value = 'Vui lòng xác nhận mã OTP'
    return
  }

  loading.value = true
  error.value = ''
  success.value = false

  try {
    const { data, error: apiError } = await useFetch('http://localhost:8080/api/auth/signup', {
      method: 'POST',
      body: {
        username: form.value.email,
        password: form.value.password,
        roles: ['user']
      }
    })

    if (apiError.value) {
      throw new Error(apiError.value.data?.message || 'Đăng ký thất bại')
    }

    if (data.value) {
      success.value = true
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    }
  } catch (err: any) {
    error.value = err.message || 'Đã xảy ra lỗi'
  } finally {
    loading.value = false
  }
}
</script>
