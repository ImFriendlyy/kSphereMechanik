package vv0ta3fa9.plugin.kSphereMechanik.utils;

import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;
import vv0ta3fa9.plugin.kSphereMechanik.models.Enchantment;
import vv0ta3fa9.plugin.kSphereMechanik.models.EnchantmentType;

public class CapacityCalculator {
    private final KSphereMechanik plugin;

    public CapacityCalculator(KSphereMechanik plugin) {
        this.plugin = plugin;
    }

    /**
     * Вычисляет стоимость зачарования в единицах емкости
     * Формула: базовая стоимость (2) + (уровень - 1) * стоимость уровня (1)
     */
    public int calculateCost(EnchantmentType type, int level) {
        int baseCost = plugin.getConfigManager().getEnchantmentBaseCost();
        int levelCost = plugin.getConfigManager().getEnchantmentLevelCost();
        
        if (level <= 1) {
            return baseCost;
        }
        
        return baseCost + (level - 1) * levelCost;
    }

    public Enchantment createEnchantment(EnchantmentType type, int level) {
        int cost = calculateCost(type, level);
        return new Enchantment(type, level, cost);
    }
}

