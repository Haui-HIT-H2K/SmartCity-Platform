import type { WebSocketMessage } from '~/types/alerts';

export const useWebSocket = () => {
    const socket = ref<WebSocket | null>(null);
    const connected = ref(false);
    const reconnectAttempts = ref(0);
    const maxReconnectAttempts = 10;
    const baseReconnectDelay = 1000;

    const onMessage = ref<((message: WebSocketMessage) => void) | null>(null);

    const connect = (url: string = 'ws://localhost:8080') => {
        if (socket.value?.readyState === WebSocket.OPEN) {
            console.log('[WebSocket] Already connected');
            return;
        }

        console.log(`[WebSocket] Connecting to ${url}...`);

        try {
            socket.value = new WebSocket(url);

            socket.value.onopen = () => {
                console.log('[WebSocket] Connected successfully');
                connected.value = true;
                reconnectAttempts.value = 0;
            };

            socket.value.onmessage = (event) => {
                try {
                    const message: WebSocketMessage = JSON.parse(event.data);
                    if (onMessage.value) {
                        onMessage.value(message);
                    }
                } catch (error) {
                    console.error('[WebSocket] Failed to parse message:', error);
                }
            };

            socket.value.onerror = (error) => {
                console.error('[WebSocket] Error:', error);
            };

            socket.value.onclose = () => {
                console.log('[WebSocket] Connection closed');
                connected.value = false;

                // Auto-reconnect with exponential backoff
                if (reconnectAttempts.value < maxReconnectAttempts) {
                    const delay = baseReconnectDelay * Math.pow(2, reconnectAttempts.value);
                    console.log(`[WebSocket] Reconnecting in ${delay}ms...`);
                    reconnectAttempts.value++;

                    setTimeout(() => {
                        connect(url);
                    }, delay);
                } else {
                    console.error('[WebSocket] Max reconnection attempts reached');
                }
            };
        } catch (error) {
            console.error('[WebSocket] Connection failed:', error);
        }
    };

    const disconnect = () => {
        if (socket.value) {
            reconnectAttempts.value = maxReconnectAttempts; // Prevent auto-reconnect
            socket.value.close();
            socket.value = null;
            connected.value = false;
        }
    };

    const send = (data: any) => {
        if (socket.value?.readyState === WebSocket.OPEN) {
            socket.value.send(JSON.stringify(data));
        } else {
            console.warn('[WebSocket] Cannot send, not connected');
        }
    };

    // Cleanup on unmount
    onUnmounted(() => {
        disconnect();
    });

    return {
        connected,
        connect,
        disconnect,
        send,
        onMessage,
    };
};
