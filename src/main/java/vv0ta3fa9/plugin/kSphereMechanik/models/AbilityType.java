package vv0ta3fa9.plugin.kSphereMechanik.models;

public enum AbilityType {
    VAMPIRE("Вампир", "Восстановление HP при нанесении урона"),
    BERSERK("Берсерк", "Временное увеличение урона и скорости"),
    STEADFAST("Стойкий", "Временная защита/сопротивление урону"),
    DISPEL("Развеивание", "Снятие негативных эффектов"),
    DASH("Рывок", "Телепорт на небольшое расстояние вперед");

    private final String displayName;
    private final String description;

    AbilityType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
