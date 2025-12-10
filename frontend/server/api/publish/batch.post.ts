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
 * 
 * API handler for publishing batch data
 * POST /api/publish/batch
 */
import { getAuthHeaders } from "~/server/utils/auth";

export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig();
  const backendUrl = config.public.apiBase || "http://localhost:8080";
  const body = await readBody(event);
  const headers = getAuthHeaders(event);
  
  try {
    const response = await $fetch(`${backendUrl}/api/publish/batch`, {
      method: "POST",
      headers,
      body,
    });
    return response;
  } catch (error: any) {
    console.error("Error publishing batch data:", error);
    throw createError({
      statusCode: error.statusCode || 500,
      statusMessage: error.message || "Failed to publish batch data",
    });
  }
});
