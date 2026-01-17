package vv0ta3fa9.plugin.kSphereMechanik.models;

/**
 * Ранг сферы определяет её максимальную емкость
 */
public enum SphereRank {
    RARE("Редкая", 3),
    MYTHIC("Мифическая", 5),
    LEGENDARY("Легендарная", 7);

    private final String displayName;
    private final int maxCapacity;

    SphereRank(String displayName, int maxCapacity) {
        this.displayName = displayName;
        this.maxCapacity = maxCapacity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }
}

