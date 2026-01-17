package vv0ta3fa9.plugin.kSphereMechanik.models;

import vv0ta3fa9.plugin.kSphereMechanik.managers.ConfigManager;

import java.util.HashMap;
import java.util.Map;

public enum EnchantmentType {


    DAMAGE_BOOST("Повышение урона", 3),
    SPEED_BOOST("Повышение скорости", 3),
    HASTE("Спешка", 2),
    FIRE_RESISTANCE("Огнеупорность", 1),
    DAMAGE_REDUCTION("Снижение урона", 2),
    EXTRA_HEALTH("Дополнительные HP", 2),
    REBIRTH("Перерождение", 1);

    private final String defaultDisplayName;
    private final int defaultMaxLevel;

    private static final Map<EnchantmentType, String> displayNames = new HashMap<>();
    private static final Map<EnchantmentType, Integer> maxLevels = new HashMap<>();
    private static boolean loaded = false;

    EnchantmentType(String defaultDisplayName, int defaultMaxLevel) {
        this.defaultDisplayName = defaultDisplayName;
        this.defaultMaxLevel = defaultMaxLevel;
    }

    public static void loadFromConfig(ConfigManager config) {
        displayNames.clear();
        maxLevels.clear();

        for (EnchantmentType type : values()) {
            String name = config.getEnchantmentDisplayName(type.name());
            int level = config.getEnchantmentMaxLevel(type.name());

            displayNames.put(type, name);
            maxLevels.put(type, level);
        }

        loaded = true;
    }

    public String getDisplayName() {
        if (loaded && displayNames.containsKey(this)) {
            return displayNames.get(this);
        }
        return defaultDisplayName;
    }

    public int getMaxLevel() {
        if (loaded && maxLevels.containsKey(this)) {
            return maxLevels.get(this);
        }
        return defaultMaxLevel;
    }

    public static boolean isLoaded() {
        return loaded;
    }
}
