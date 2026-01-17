package vv0ta3fa9.plugin.kSphereMechanik.models;

import java.util.Objects;

/**
 * Зачарование сферы с уровнем и стоимостью
 */
public class Enchantment {
    private final EnchantmentType type;
    private final int level;
    private final int capacityCost;

    public Enchantment(EnchantmentType type, int level, int capacityCost) {
        this.type = type;
        this.level = level;
        this.capacityCost = capacityCost;
    }

    public EnchantmentType getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public int getCapacityCost() {
        return capacityCost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enchantment that = (Enchantment) o;
        return level == that.level && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, level);
    }

    /**
     * Форматирует зачарование с displayName из конфига
     */
    public String format(String displayName) {
        return displayName + " Ур." + level;
    }

    @Override
    public String toString() {
        // Fallback - использует значение из enum
        return type.getDisplayName() + " Ур." + level;
    }
}

