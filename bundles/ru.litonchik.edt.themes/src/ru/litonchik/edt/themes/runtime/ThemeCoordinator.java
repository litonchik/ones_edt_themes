package ru.litonchik.edt.themes.runtime;

import org.eclipse.core.runtime.preferences.*;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.IThemeManager;
import ru.litonchik.edt.themes.edt.*;
import ru.litonchik.edt.themes.model.*;

public final class ThemeCoordinator implements AutoCloseable {
    private final SnapshotPreferences preferences;
    private final EdtSettingsAdapter adapter;
    private final SnapshotService snapshots;
    private final ProfileState state;
    private final IThemeEngine engine;
    private final IEclipsePreferences cssPrefs = InstanceScope.INSTANCE.getNode(EdtSettingsAdapter.CSS_NODE);
    private final IEclipsePreferences.IPreferenceChangeListener listener;
    private volatile boolean ownApply, closed;
    public ThemeCoordinator() throws Exception {
        EdtCompatibility.verify();
        engine = PlatformUI.getWorkbench().getService(IThemeEngine.class);
        IThemeManager manager = PlatformUI.getWorkbench().getThemeManager();
        if (engine == null || engine.getActiveTheme() == null) throw new IllegalStateException("Штатный theme engine недоступен");
        preferences = new SnapshotPreferences(); adapter = new EdtSettingsAdapter(engine,manager);
        snapshots = new SnapshotService(adapter,preferences);
        state = new ProfileState(preferences.activeProfile(),preferences.includeBsl());
        listener = event -> {
            if (closed || ownApply || state.activeProfile() == null || !"themeid".equals(event.getKey())) return;
            java.util.Map<SettingKey,OriginalValue> observed;
            try {
                // ThemeEngine persists themeid before native CSS rewrites color preferences.
                observed = snapshots.observeValues(k -> k.scope().equals("instance") && !k.node().equals(EdtSettingsAdapter.CSS_NODE) && !k.node().equals(SnapshotPreferences.PLUGIN_ID));
            } catch (Exception e) {
                PlatformUI.getWorkbench().getDisplay().asyncExec(() -> ThemeStartup.report(e)); return;
            }
            PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
                if (!closed && engine.getActiveTheme() != null) onThemeChanged(engine.getActiveTheme().getId(),observed);
            });
        };
        cssPrefs.addPreferenceChangeListener(listener);
    }
    public String activeProfile() { return state.activeProfile(); }
    public boolean includeBsl() { return state.includeBsl(); }
    public boolean hasSnapshot() { return !snapshots.snapshot().original().isEmpty(); }
    public void start() throws Exception {
        String savedProfile = preferences.activeProfile();
        if (savedProfile != null && !PaletteCatalog.isKnown(savedProfile))
            ThemeStartup.inform("Сохранённый профиль недоступен: " + savedProfile + ". Выберите палитру повторно или восстановите исходное оформление.");
        if (snapshots.snapshot().pending()) {
            ownApply = true;
            try {
                RestoreResult recovered = snapshots.recover();
                preferences.saveProfile(null,state.includeBsl()); state.release();
                if (!recovered.successful()) throw new IllegalStateException("Не удалось восстановить незавершённое применение: " + recovered.errorSummary());
                ThemeStartup.inform("Восстановлен незавершённый журнал. Выберите профиль повторно. Конфликтов: " + recovered.conflicts().size());
            } finally { ownApply = false; }
        } else if (state.activeProfile() != null) {
            if (!PaletteCatalog.mode(state.activeProfile()).cssId().equals(engine.getActiveTheme().getId())) onThemeChanged(engine.getActiveTheme().getId());
            else if (PaletteCatalog.mode(state.activeProfile()) == ThemeMode.LIGHT) adapter.restoreLightRegistry(snapshots.snapshot().lastApplied());
            // Persistent scoped values are consumed by native startup. Do not reapply: it would overwrite manual changes.
        }
        LightUiStyles.refresh(engine);
        EditorRulerColors.start();
    }
    public void apply(String profile, boolean includeBsl) throws Exception {
        apply(profile,includeBsl,paletteDrafts());
    }
    public PaletteDrafts paletteDrafts() {
        var saved = new java.util.LinkedHashMap<String,java.util.Map<String,String>>();
        for (String profile : PaletteDrafts.PROFILES) {
            var colors = new java.util.LinkedHashMap<String,String>();
            for (String role : Palette.REQUIRED) {
                var value = adapter.read(personalKey(profile,role));
                if (value.present()) colors.put(role,value.value());
            }
            saved.put(profile,colors);
        }
        return new PaletteDrafts(saved);
    }
    private SettingKey personalKey(String profile,String role) {
        return new SettingKey("instance",SnapshotPreferences.PLUGIN_ID,"palette."+profile+"."+role);
    }
    public void apply(String profile, boolean includeBsl, PaletteDrafts drafts) throws Exception {
        EdtCompatibility.verify();
        var personal = new java.util.LinkedHashMap<SettingKey,OriginalValue>();
        for (String id : PaletteDrafts.PROFILES) for (String role : drafts.touched(id)) {
            String hex = drafts.overrides(id).get(role);
            personal.put(personalKey(id,role),hex == null ? OriginalValue.absent() : OriginalValue.of(hex));
        }
        Palette palette = profile == null ? null : drafts.resolve(profile);
        if (!state.beginApply()) return;
        ownApply = true;
        try {
            if (profile == null) {
                RestoreResult result = snapshots.restore(false,java.util.Map.of(),personal);
                if (!result.successful()) throw new IllegalStateException("Ошибка восстановления: " + result.errorSummary());
                state.release(); drafts.accept(); return;
            }
            var desired = adapter.plan(palette,includeBsl);
            if (!includeBsl) {
                var bslKeys = new java.util.LinkedHashSet<SettingKey>();
                adapter.plan(palette,true).keySet().stream().filter(k -> k.node().equals("com._1c.g5.v8.dt.bsl.ui")).forEach(bslKeys::add);
                // Dark CSS touches BSL even when palette BSL is disabled. Neutralize those native writes.
                desired = snapshots.withOriginals(desired,bslKeys);
            }
            ThemeMode mode = PaletteCatalog.mode(profile);
            desired = snapshots.withNativeDefaults(desired,NativeSettingsCatalog.load(mode),NativeSettingsCatalog.load(mode == ThemeMode.DARK ? ThemeMode.LIGHT : ThemeMode.DARK));
            desired.put(new SettingKey("instance",SnapshotPreferences.PLUGIN_ID,"activeProfile"),OriginalValue.of(profile));
            desired.put(new SettingKey("instance",SnapshotPreferences.PLUGIN_ID,"includeBsl"),OriginalValue.of(Boolean.toString(includeBsl)));
            snapshots.capture(desired.keySet());
            var explicit = SettingCatalog.load().roleTargets(drafts.touched(profile),includeBsl,PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getId(),PaletteCatalog.mode(profile));
            snapshots.apply(desired,explicit,personal);
            state.activate(profile,includeBsl);
            drafts.accept();
        } catch (Exception e) {
            // Journal still contains originals even if applying failed. Never declare a new profile successful.
            throw e;
        } finally { ownApply = false; state.endApply(); }
    }
    public RestoreResult restore(boolean force) throws Exception {
        return restore(force,java.util.Map.of());
    }
    private RestoreResult restore(boolean force, java.util.Map<SettingKey,OriginalValue> observed) throws Exception {
        if (!state.beginApply()) throw new IllegalStateException("Применение уже выполняется");
        ownApply = true;
        try {
            RestoreResult result = snapshots.restore(force,observed);
            if (result.successful()) state.release();
            return result;
        } finally { ownApply = false; state.endApply(); }
    }
    public void onThemeChanged(String cssId) {
        onThemeChanged(cssId,java.util.Map.of());
    }
    private void onThemeChanged(String cssId, java.util.Map<SettingKey,OriginalValue> observed) {
        if (!state.onThemeChanged(cssId)) return;
        try {
            RestoreResult result = restore(false,observed);
            if (!result.successful()) ThemeStartup.report(new IllegalStateException("Ошибка автоматического восстановления: " + result.errorSummary()));
        } catch (Exception e) { ThemeStartup.report(e); }
    }
    @Override public void close() { closed = true; cssPrefs.removePreferenceChangeListener(listener); }
}
