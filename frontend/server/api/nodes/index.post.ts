/** 
 * Copyright 2025 Haui.HIT - H2K
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

import { getAuthHeaders } from "~/server/utils/auth";

export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig();
  const backendUrl = config.public.apiBase || "http://localhost:8080";
  const body = await readBody(event);
  const headers = getAuthHeaders(event);
  
  try {
    const response = await $fetch(`${backendUrl}/api/nodes`, {
      method: "POST",
      headers,
      body,
    });
    return response;
  } catch (error: any) {
    console.error("Error creating node:", error);
    throw createError({
      statusCode: error.statusCode || 500,
      statusMessage: error.message || "Failed to create node",
      data: error.data,
    });
  }
});
