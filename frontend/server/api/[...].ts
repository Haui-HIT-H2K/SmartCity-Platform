/** 

 * Copyright 2025 Haui.HIT - H2K

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 */

import {
  defineEventHandler,
  getHeaders,
  getQuery,
  readBody,
  createError,
} from "h3";

// Proxy all /api/* requests to backend
export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig();
  const path = event.path.replace("/api", "");

  // Get backend URL from runtime config (already has proper fallback)
  const backendUrl = config.apiSecret || config.public.apiBase;
  const targetUrl = `${backendUrl}/api${path}`;

  // Forward the request to backend
  try {
    // Get headers and remove host to avoid conflicts
    const requestHeaders = getHeaders(event);
    const headers: Record<string, string> = {};

    for (const [key, value] of Object.entries(requestHeaders)) {
      if (key !== "host" && value !== undefined) {
        headers[key] = value;
      }
    }

    const response = await $fetch(targetUrl, {
      method: event.method,
      headers,
      // @ts-ignore
      body:
        event.method !== "GET" && event.method !== "HEAD"
          ? await readBody(event)
          : undefined,
      params: getQuery(event),
    });

    return response;
  } catch (error: any) {
    console.error(`Proxy error for ${targetUrl}:`, error.message);
    throw createError({
      statusCode: error.statusCode || 500,
      statusMessage: error.message || "Backend service unavailable",
    });
  }
});
