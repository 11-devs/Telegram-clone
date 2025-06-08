package Shared.Models.Setting;

public class GeneralSetting extends Setting {



    private Language language = Language.English;
    private Theme theme = Theme.LIGHTMODE;

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }
}
