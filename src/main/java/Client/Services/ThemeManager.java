package Client.Services;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Scene;

import java.util.prefs.Preferences;

/**
 * Manages the application's theme.
 * Handles loading, applying, and saving the user's theme preference.
 * This is a singleton to ensure a single source of truth for the theme.
 */
public class ThemeManager {

    private static ThemeManager instance;
    private final ObjectProperty<Theme> currentTheme;
    private final Preferences prefs;

    public enum Theme {
        DARK("Dark", "theme-dark"),
        LIGHT("Light", "theme-light");

        private final String displayName;
        private final String styleClass;

        Theme(String displayName, String styleClass) {
            this.displayName = displayName;
            this.styleClass = styleClass;
        }

        public String getStyleClass() {
            return styleClass;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private ThemeManager() {
        prefs = Preferences.userNodeForPackage(ThemeManager.class);
        String savedThemeName = prefs.get("appTheme", Theme.DARK.name());
        Theme savedTheme = Theme.DARK; // Default
        try {
            savedTheme = Theme.valueOf(savedThemeName);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid theme name in preferences: " + savedThemeName + ". Defaulting to DARK.");
        }
        currentTheme = new SimpleObjectProperty<>(savedTheme);

        // Add a listener to save the theme whenever it changes
        currentTheme.addListener((obs, oldTheme, newTheme) -> {
            if (newTheme != null) {
                prefs.put("appTheme", newTheme.name());
                System.out.println("Theme changed and saved: " + newTheme.name());
            }
        });
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public ObjectProperty<Theme> currentThemeProperty() {
        return currentTheme;
    }

    public Theme getCurrentTheme() {
        return currentTheme.get();
    }

    public void setTheme(Theme theme) {
        currentTheme.set(theme);
    }

    /**
     * Applies the current theme to a given Scene.
     * It removes old theme classes and adds the current one.
     * @param scene The Scene to apply the theme to.
     */
    public void applyTheme(Scene scene) {
        if (scene == null) return;
        ObservableList<String> styleClasses = scene.getRoot().getStyleClass();

        // Remove any existing theme classes
        for (Theme theme : Theme.values()) {
            styleClasses.remove(theme.getStyleClass());
        }

        // Add the current theme class
        System.out.println("Change theme to: "+getCurrentTheme());
        styleClasses.add(getCurrentTheme().getStyleClass());
    }
}