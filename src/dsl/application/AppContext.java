package dsl.application;

import dsl.services.EventLogService;
import dsl.services.MetricsService;
import dsl.services.SettingsService;
import dsl.ui.ThemeManager;

/** Composition root — created once and injected everywhere. */
public final class AppContext {

    private final EventLogService eventLog = new EventLogService();
    private final MetricsService metrics = new MetricsService();
    private final SettingsService settings = SettingsService.load();
    private ThemeManager themeManager;

    public EventLogService eventLog() { return eventLog; }
    public MetricsService metrics()   { return metrics; }
    public SettingsService settings() { return settings; }

    public ThemeManager themeManager() { return themeManager; }

    public void attachThemeManager(ThemeManager tm) {
        this.themeManager = tm;
    }
}