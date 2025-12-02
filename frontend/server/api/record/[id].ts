export default defineEventHandler(async (event) => {
    const id = getRouterParam(event, 'id')

    if (!id) {
        throw createError({
            statusCode: 400,
            statusMessage: 'Record ID is required'
        })
    }

    try {
        // Get headers for forwarding
        const headers = getHeaders(event)

        // Fetch from backend API
        const record = await $fetch(`http://localhost:8080/api/data/${id}`, {
            headers: {
                'host': headers.host || 'localhost:8080',
            }
        })

        return record
    } catch (error: any) {
        console.error('Error fetching record:', error)

        throw createError({
            statusCode: error.statusCode || 500,
            statusMessage: error.message || 'Failed to fetch record'
        })
    }
})
