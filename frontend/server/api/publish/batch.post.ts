/**
 * API handler for publishing batch data
 * POST /api/publish/batch
 */
import { getAuthHeaders } from "~/server/utils/auth";

export default defineEventHandler(async (event) => {
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
