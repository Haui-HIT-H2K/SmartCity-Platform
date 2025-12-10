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

import { H3Event } from 'h3';

interface LoginRequest {
  username: string;
  password: string;
}

interface LoginResponse {
  token: string;
  id: string;
  username: string;
  roles: string[];
}

export default defineEventHandler(async (event: H3Event): Promise<LoginResponse> => {
  const config = useRuntimeConfig();
  const body = await readBody<LoginRequest>(event);

  // Use Docker internal URL for backend
  const backendUrl = config.apiSecret || 'http://smart-city-backend:8080';

  try {
    const response = await $fetch<LoginResponse>(`${backendUrl}/api/auth/signin`, {
      method: 'POST',
      body: {
        username: body.username,
        password: body.password,
      },
    });

    return response;
  } catch (error: any) {
    console.error('Login proxy error:', error);
    throw createError({
      statusCode: error.statusCode || 500,
      statusMessage: error.data?.message || 'Login failed',
    });
  }
});
