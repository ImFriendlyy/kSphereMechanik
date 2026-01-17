package vv0ta3fa9.plugin.kSphereMechanik.models;

/**
 * Тип сферы определяет её функциональность
 */
public enum SphereType {
    NORMAL("Обычная", "&7"),
    ACTIVE("Активная", "&e"),
    DONATE("Донатная", "&6&l");

    private final String displayName;
    private final String colorCode;

    SphereType(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }
}

