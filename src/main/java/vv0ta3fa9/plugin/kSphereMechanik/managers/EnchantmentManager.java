package vv0ta3fa9.plugin.kSphereMechanik.managers;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;
import vv0ta3fa9.plugin.kSphereMechanik.models.Enchantment;
import vv0ta3fa9.plugin.kSphereMechanik.models.EnchantmentType;
import vv0ta3fa9.plugin.kSphereMechanik.models.Sphere;

/**
 * Менеджер для применения эффектов зачарований
 */
public class EnchantmentManager {
    private final KSphereMechanik plugin;

    public EnchantmentManager(KSphereMechanik plugin) {
        this.plugin = plugin;
    }

    /**
     * Применяет все эффекты зачарований сферы к игроку
     */
    public void applyEnchantments(Player player, Sphere sphere) {
        if (player == null || sphere == null) {
            plugin.getDebugLogger().logFull("applyEnchantments: игрок или сфера null");
            return;
        }

        plugin.getDebugLogger().logFull("Применение эффектов сферы " + sphere.getId() + " для игрока " + player.getName() + ". Зачарований: " + sphere.getEnchantments().size());

        for (Enchantment enchantment : sphere.getEnchantments()) {
            plugin.getDebugLogger().logFull("Применение зачарования: " + enchantment.getType() + " уровень " + enchantment.getLevel());
            applyEnchantment(player, enchantment);
        }
    }

    /**
     * Применяет эффект одного зачарования
     */
    private void applyEnchantment(Player player, Enchantment enchantment) {
        EnchantmentType type = enchantment.getType();
        int level = enchantment.getLevel();
        ConfigManager cfg = plugin.getConfigManager();

        switch (type) {
            case DAMAGE_BOOST:
                // Обрабатывается в DamageListener
                break;
                
            case SPEED_BOOST:
                double speedMultiplier = cfg.getEnchantmentValue("speed_boost", "speed_level_per_level");
                plugin.getDebugLogger().logFull("SPEED_BOOST: multiplier=" + speedMultiplier + ", enchantment_level=" + level);
                int speedLevel = (int) (speedMultiplier * level);
                if (speedLevel == 0) {
                    speedLevel = level; // fallback
                    plugin.getDebugLogger().logFull("SPEED_BOOST: использован fallback, speedLevel=" + speedLevel);
                }
                plugin.getDebugLogger().logFull("Применение SPEED_BOOST: уровень " + speedLevel + " (эффект уровень " + Math.max(0, speedLevel - 1) + ")");
                player.removePotionEffect(PotionEffectType.SPEED);
                player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED, Integer.MAX_VALUE, Math.max(0, speedLevel - 1), false, false
                ));
                plugin.getDebugLogger().logFull("SPEED_BOOST применен к " + player.getName());
                break;
                
            case HASTE:
                int hasteLevel = (int) (cfg.getEnchantmentValue("haste", "haste_level_per_level") * level);
                if (hasteLevel == 0) hasteLevel = level; // fallback
                player.removePotionEffect(PotionEffectType.HASTE);
                player.addPotionEffect(new PotionEffect(
                    PotionEffectType.HASTE, Integer.MAX_VALUE, Math.max(0, hasteLevel - 1), false, false
                ));
                break;
                
            case FIRE_RESISTANCE:
                int duration = cfg.getEnchantmentIntValue("fire_resistance", "duration_ticks");
                if (duration == 0) duration = -1; // fallback to infinite
                player.addPotionEffect(new PotionEffect(
                    PotionEffectType.FIRE_RESISTANCE, duration, 0, false, false
                ));
                break;
                
            case DAMAGE_REDUCTION:
                // Обрабатывается в DamageListener
                break;
                
            case EXTRA_HEALTH:
                int hearts = (int) (cfg.getEnchantmentValue("extra_health", "hearts_per_level") * level);
                if (hearts == 0) hearts = 2 * level; // fallback
                double baseHealth = 20.0;
                double bonusHealth = hearts * 2.0;
                player.setMaxHealth(baseHealth + bonusHealth);
                break;
                
            case REBIRTH:
                break;
        }
    }

    /**
     * Удаляет все эффекты зачарований сферы у игрока
     * Если sphere == null, удаляет все эффекты (когда сфера убрана)
     */
    public void removeEnchantments(Player player, Sphere sphere) {
        if (player == null) return;

        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.HASTE);
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);

        if (sphere == null || sphere.hasEnchantment(EnchantmentType.EXTRA_HEALTH)) {
            double currentHealth = player.getHealth();
            player.setMaxHealth(20.0);
            if (currentHealth > 20.0) {
                player.setHealth(20.0);
            } else {
                player.setHealth(currentHealth);
            }
        }
    }

    /**
     * Получает множитель урона от зачарования
     */
    public double getDamageMultiplier(Sphere sphere) {
        if (sphere == null || !sphere.hasEnchantment(EnchantmentType.DAMAGE_BOOST)) {
            return 1.0;
        }
        
        int level = sphere.getEnchantmentLevel(EnchantmentType.DAMAGE_BOOST);
        double multiplierPerLevel = plugin.getConfigManager()
            .getEnchantmentValue("damage_boost", "damage_multiplier_per_level");
        if (multiplierPerLevel == 0) multiplierPerLevel = 0.15; // fallback
        
        return 1.0 + (multiplierPerLevel * level);
    }

    /**
     * Получает процент снижения урона
     */
    public double getDamageReduction(Sphere sphere) {
        if (sphere == null || !sphere.hasEnchantment(EnchantmentType.DAMAGE_REDUCTION)) {
            return 0.0;
        }
        
        int level = sphere.getEnchantmentLevel(EnchantmentType.DAMAGE_REDUCTION);
        double reductionPerLevel = plugin.getConfigManager()
            .getEnchantmentValue("damage_reduction", "reduction_percent_per_level");
        if (reductionPerLevel == 0) reductionPerLevel = 0.10; // fallback
        
        return reductionPerLevel * level;
    }
}
