/** 
 * Copyright 2025 Haui.HIT - H2K
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig();
  const backendUrl = config.public.apiBase || "http://localhost:8080";
  const name = getRouterParam(event, "name");
  
  if (!name) {
    throw createError({
      statusCode: 400,
      statusMessage: "Node name is required",
    });
  }
  
  try {
    const response = await $fetch(`${backendUrl}/api/nodes/${name}`, {
      method: "DELETE",
    });
    return response;
  } catch (error: any) {
    console.error("Error deleting node:", error);
    throw createError({
      statusCode: error.statusCode || 500,
      statusMessage: error.message || "Failed to delete node",
    });
  }
});
