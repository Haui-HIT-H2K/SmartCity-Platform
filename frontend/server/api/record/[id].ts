export default defineEventHandler(async (event) => {
  const id = getRouterParam(event, "id");
  const config = useRuntimeConfig();
  if (!id) {
    throw createError({
      statusCode: 400,
      statusMessage: "Record ID is required",
    });
  }

  try {
    // Get headers for forwarding
    const backendUrl = config.apiSecret || "http://smart-city-backend:8080";

    const record = await $fetch(`${backendUrl}/api/data/${id}`);

    return record;
  } catch (error: any) {
    console.error("Error fetching record:", error);

    throw createError({
      statusCode: error.statusCode || 500,
      statusMessage: error.message || "Failed to fetch record",
    });
  }
});
