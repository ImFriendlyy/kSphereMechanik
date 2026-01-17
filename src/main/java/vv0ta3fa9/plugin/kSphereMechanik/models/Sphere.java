package vv0ta3fa9.plugin.kSphereMechanik.models;

import java.util.*;

/**
 * Базовая модель сферы
 */
public class Sphere {
    private final UUID id;
    private final SphereType type;
    private final SphereRank rank;
    private final List<Enchantment> enchantments;
    private AbilityType ability; // null для обычных сфер
    private boolean reborn; // статус перерождения
    private long lastAbilityUse; // время последнего использования способности

    public Sphere(UUID id, SphereType type, SphereRank rank) {
        this.id = id;
        this.type = type;
        this.rank = rank;
        this.enchantments = new ArrayList<>();
        this.ability = null;
        this.reborn = false;
        this.lastAbilityUse = 0;
    }

    public UUID getId() {
        return id;
    }

    public SphereType getType() {
        return type;
    }

    public SphereRank getRank() {
        return rank;
    }

    public List<Enchantment> getEnchantments() {
        return Collections.unmodifiableList(enchantments);
    }

    public AbilityType getAbility() {
        return ability;
    }

    public void setAbility(AbilityType ability) {
        this.ability = ability;
    }

    public boolean isReborn() {
        return reborn;
    }

    public void setReborn(boolean reborn) {
        this.reborn = reborn;
    }

    public long getLastAbilityUse() {
        return lastAbilityUse;
    }

    public void setLastAbilityUse(long lastAbilityUse) {
        this.lastAbilityUse = lastAbilityUse;
    }

    /**
     * Добавляет зачарование, если есть место по емкости
     */
    public boolean addEnchantment(Enchantment enchantment, int maxCapacity) {
        if (type == SphereType.DONATE) {
            enchantments.add(enchantment);
            return true;
        }

        int currentCapacity = calculateUsedCapacity();
        if (currentCapacity + enchantment.getCapacityCost() <= maxCapacity) {
            enchantments.removeIf(e -> e.getType() == enchantment.getType());
            enchantments.add(enchantment);
            return true;
        }
        return false;
    }

    public boolean removeEnchantment(EnchantmentType type) {
        return enchantments.removeIf(e -> e.getType() == type);
    }

    public void clearEnchantments() {
        enchantments.clear();
    }

    public int calculateUsedCapacity() {
        return enchantments.stream()
                .mapToInt(Enchantment::getCapacityCost)
                .sum();
    }

    public boolean hasEnchantment(EnchantmentType type) {
        return enchantments.stream()
                .anyMatch(e -> e.getType() == type);
    }

    public int getEnchantmentLevel(EnchantmentType type) {
        return enchantments.stream()
                .filter(e -> e.getType() == type)
                .mapToInt(Enchantment::getLevel)
                .findFirst()
                .orElse(0);
    }

    public boolean isActive() {
        return type == SphereType.ACTIVE && ability != null;
    }
}

