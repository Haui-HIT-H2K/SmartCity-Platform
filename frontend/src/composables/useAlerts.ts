import type { Alert, DataMetrics } from '~/types/alerts';

const STORAGE_KEY = 'smart-city-alerts';
const ALERT_RETENTION_MS = 30 * 60 * 1000; // 30 minutes in milliseconds

// Load alerts from localStorage, filtering out those older than 30 minutes
const loadAlertsFromStorage = (): Alert[] => {
    if (typeof window === 'undefined') return [];

    try {
        const stored = localStorage.getItem(STORAGE_KEY);
        if (!stored) return [];

        const alerts = JSON.parse(stored) as Alert[];
        const now = Date.now();

        // Filter alerts - keep only those from last 30 minutes
        return alerts.filter(alert => {
            const alertTime = new Date(alert.timestamp).getTime();
            return (now - alertTime) < ALERT_RETENTION_MS;
        });
    } catch (error) {
        console.error('Failed to load alerts from storage:', error);
        return [];
    }
};

// Save alerts to localStorage
const saveAlertsToStorage = (alerts: Alert[]) => {
    if (typeof window === 'undefined') return;

    try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(alerts));
    } catch (error) {
        console.error('Failed to save alerts to storage:', error);
    }
};

export const useAlerts = () => {
    // Initialize with alerts from localStorage
    const activeAlerts = ref<Alert[]>(loadAlertsFromStorage());
    const recentAlerts = ref<Alert[]>([]);
    const currentMetrics = ref<DataMetrics>({
        throughput: [],
        breakdown: { hot: 0, warm: 0, cold: 0 },
        timestamp: Date.now(),
    });

    const addAlert = (alert: Alert) => {
        // Check if alert already exists
        const exists = activeAlerts.value.some(a => a.id === alert.id);
        if (exists) return;

        // Add to active alerts
        activeAlerts.value.push(alert);

        // Add to recent alerts (max 20)
        recentAlerts.value.unshift(alert);
        if (recentAlerts.value.length > 20) {
            recentAlerts.value.pop();
        }

        // Save to localStorage
        saveAlertsToStorage(activeAlerts.value);
    };

    const removeAlert = (id: string) => {
        activeAlerts.value = activeAlerts.value.filter(alert => alert.id !== id);
        saveAlertsToStorage(activeAlerts.value);
    };

    const updateMetrics = (metrics: DataMetrics) => {
        currentMetrics.value = metrics;
    };

    // Auto-cleanup: Remove alerts older than 30 minutes
    const cleanupOldAlerts = () => {
        const now = Date.now();
        const before = activeAlerts.value.length;

        activeAlerts.value = activeAlerts.value.filter(alert => {
            const alertTime = new Date(alert.timestamp).getTime();
            return (now - alertTime) < ALERT_RETENTION_MS;
        });

        if (activeAlerts.value.length !== before) {
            saveAlertsToStorage(activeAlerts.value);
        }
    };

    // Run cleanup every minute
    if (typeof window !== 'undefined') {
        setInterval(cleanupOldAlerts, 60000); // 1 minute
    }

    return {
        activeAlerts,
        recentAlerts,
        currentMetrics,
        addAlert,
        removeAlert,
        updateMetrics,
    };
};
