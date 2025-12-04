SPDX-License-Identifier: Apache-2.0
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
