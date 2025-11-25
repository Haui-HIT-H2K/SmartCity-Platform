export const useTheme = () => {
    const theme = ref<'light' | 'dark'>('light');

    // Initialize theme from localStorage or system preference
    const initTheme = () => {
        if (typeof window === 'undefined') return;

        const savedTheme = localStorage.getItem('theme') as 'light' | 'dark' | null;

        if (savedTheme) {
            theme.value = savedTheme;
        } else {
            // Check system preference
            const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
            theme.value = prefersDark ? 'dark' : 'light';
        }

        applyTheme(theme.value);
    };

    // Apply theme to document
    const applyTheme = (newTheme: 'light' | 'dark') => {
        if (typeof document === 'undefined') return;

        document.documentElement.setAttribute('data-theme', newTheme);
    };

    // Toggle between light and dark
    const toggleTheme = () => {
        theme.value = theme.value === 'dark' ? 'light' : 'dark';
        applyTheme(theme.value);
        localStorage.setItem('theme', theme.value);
    };

    // Set specific theme
    const setTheme = (newTheme: 'light' | 'dark') => {
        theme.value = newTheme;
        applyTheme(newTheme);
        localStorage.setItem('theme', newTheme);
    };

    // Initialize on mount
    onMounted(() => {
        initTheme();
    });

    return {
        theme: readonly(theme),
        toggleTheme,
        setTheme,
    };
};
