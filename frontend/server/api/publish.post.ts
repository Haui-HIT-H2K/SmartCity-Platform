/**
 * API handler for publishing single data
 * POST /api/publish
 */
import { getAuthHeaders } from "~/server/utils/auth";

export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig();
  const backendUrl = config.public.apiBase || "http://localhost:8080";
  const body = await readBody(event);
  const headers = getAuthHeaders(event);
  
  try {
    const response = await $fetch(`${backendUrl}/api/publish`, {
      method: "POST",
      headers,
      body,
    });
    return response;
  } catch (error: any) {
    console.error("Error publishing data:", error);
    throw createError({
      statusCode: error.statusCode || 500,
      statusMessage: error.message || "Failed to publish data",
    });
  }
});
