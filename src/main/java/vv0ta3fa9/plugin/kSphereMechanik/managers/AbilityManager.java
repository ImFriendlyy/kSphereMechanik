package vv0ta3fa9.plugin.kSphereMechanik.managers;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;
import vv0ta3fa9.plugin.kSphereMechanik.api.SphereEvents;
import vv0ta3fa9.plugin.kSphereMechanik.models.AbilityType;
import vv0ta3fa9.plugin.kSphereMechanik.models.Sphere;

import java.util.Collection;
import java.util.List;

/**
 * Менеджер для обработки активных способностей
 */
public class AbilityManager {
    private final KSphereMechanik plugin;

    public AbilityManager(KSphereMechanik plugin) {
        this.plugin = plugin;
    }

    /**
     * Активирует способность сферы
     */
    public boolean activateAbility(Player player, Sphere sphere) {
        if (player == null || sphere == null || !sphere.isActive()) {
            return false;
        }

        AbilityType ability = sphere.getAbility();
        if (ability == null) return false;

        long cooldown = plugin.getConfigManager().getAbilityCooldown(ability.name());
        long timeSinceLastUse = System.currentTimeMillis() - sphere.getLastAbilityUse();
        if (timeSinceLastUse < cooldown) {
            long remaining = (cooldown - timeSinceLastUse) / 1000;
            String message = plugin.getMessagesManager().getMessage("ability_cooldown")
                .replace("{time}", String.valueOf(remaining));
            player.sendMessage(plugin.getColorizer().colorize(message));
            return false;
        }

        boolean activated = false;
        switch (ability) {
            case VAMPIRE:
                activated = true;
                break;
            case BERSERK:
                activated = activateBerserk(player, sphere);
                break;
            case STEADFAST:
                activated = activateSteadfast(player, sphere);
                break;
            case DISPEL:
                activated = activateDispel(player, sphere);
                break;
            case DASH:
                activated = activateDash(player, sphere);
                break;
        }

        if (activated) {
            SphereEvents.callSphereAbilityActivateEvent(player, sphere, ability);

            sphere.setLastAbilityUse(System.currentTimeMillis());
            String message = plugin.getMessagesManager().getMessage("ability_activated")
                .replace("{ability}", ability.getDisplayName());
            player.sendMessage(plugin.getColorizer().colorize(message));

            showActivationParticles(player, ability);

            plugin.getDebugLogger().logDetailed("Способность " + ability + " активирована игроком " + player.getName());
        }

        return activated;
    }

    /**
     * Активирует способность Берсерк
     */
    private boolean activateBerserk(Player player, Sphere sphere) {
        long duration = (long) plugin.getConfigManager().getAbilityValue("berserk", "duration");
        double damageMultiplier = plugin.getConfigManager().getAbilityValue("berserk", "damage_multiplier");
        int speedLevel = (int) plugin.getConfigManager().getAbilityValue("berserk", "speed_level");
        int strengthLevel = (int) plugin.getConfigManager().getAbilityValue("berserk", "strength_level");

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) duration / 50, speedLevel - 1, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, (int) duration / 50, strengthLevel - 1, false, true));

        player.setMetadata("berserk_multiplier", new org.bukkit.metadata.FixedMetadataValue(plugin, damageMultiplier));

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.removeMetadata("berserk_multiplier", plugin);
        }, duration / 50);

        return true;
    }

    private boolean activateSteadfast(Player player, Sphere sphere) {
        long duration = (long) plugin.getConfigManager().getAbilityValue("steadfast", "duration");
        int resistanceLevel = (int) plugin.getConfigManager().getAbilityValue("steadfast", "resistance_level");
        double reduction = plugin.getConfigManager().getAbilityValue("steadfast", "damage_reduction_percent");

        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, (int) duration / 50, resistanceLevel - 1, false, true));

        player.setMetadata("steadfast_reduction", new org.bukkit.metadata.FixedMetadataValue(plugin, reduction));
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.removeMetadata("steadfast_reduction", plugin);
        }, duration / 50);

        return true;
    }

    private boolean activateDispel(Player player, Sphere sphere) {
        boolean onlyNegative = plugin.getConfigManager().getConfig()
            .getBoolean("abilities.dispel.only_negative", true);
        
        Collection<PotionEffect> effects = player.getActivePotionEffects();
        int removed = 0;
        
        for (PotionEffect effect : effects) {
            PotionEffectType type = effect.getType();

            if (onlyNegative) {
                if (!isNegativeEffect(type)) {
                    continue;
                }
            }

            List<String> blacklist = plugin.getConfigManager().getConfig()
                .getStringList("abilities.dispel.blacklist");
            if (blacklist.contains(type.getKey().getKey())) {
                continue;
            }
            
            player.removePotionEffect(type);
            removed++;
        }

        return removed > 0;
    }

    /**
     * Активирует способность Рывок
     */
    private boolean activateDash(Player player, Sphere sphere) {
        double distance = plugin.getConfigManager().getAbilityValue("dash", "distance");
        int invulnerabilityTicks = (int) plugin.getConfigManager().getAbilityValue("dash", "invulnerability_ticks");

        Location loc = player.getLocation();
        Location targetLoc = loc.clone();

        for (double i = 0.5; i <= distance; i += 0.5) {
            Location checkLoc = loc.clone().add(loc.getDirection().multiply(i));
            if (!checkLoc.getBlock().getType().isSolid() &&
                !checkLoc.getBlock().getRelative(0, 1, 0).getType().isSolid()) {
                targetLoc = checkLoc;
            } else {
                break;
            }
        }

        if (targetLoc.equals(loc)) {
            player.sendMessage(plugin.getColorizer().colorize(
                plugin.getMessagesManager().getMessage("dash_blocked")));
            return false;
        }

        player.teleport(targetLoc);

        if (invulnerabilityTicks > 0) {
            player.setNoDamageTicks(Math.max(player.getNoDamageTicks(), invulnerabilityTicks));
        }

        return true;
    }

    /**
     * Обрабатывает эффект Вампира при нанесении урона
     */
    public void handleVampire(Player player, double damage) {
        double lifestealPercent = plugin.getConfigManager().getAbilityValue("vampire", "lifesteal_percent");
        double minDamage = plugin.getConfigManager().getAbilityValue("vampire", "min_damage");
        
        if (damage < minDamage) return;
        
        double healAmount = damage * lifestealPercent;
        double newHealth = Math.min(player.getHealth() + healAmount, player.getMaxHealth());
        player.setHealth(newHealth);
        
        plugin.getDebugLogger().logFull("Вампир восстановил " + healAmount + " HP игроку " + player.getName());
    }

    /**
     * Проверяет, является ли эффект негативным
     */
    private boolean isNegativeEffect(PotionEffectType type) {
        // Список известных негативных эффектов
        return type == PotionEffectType.POISON ||
               type == PotionEffectType.WITHER ||
               type == PotionEffectType.SLOWNESS ||
               type == PotionEffectType.WEAKNESS ||
               type == PotionEffectType.BLINDNESS ||
               type == PotionEffectType.HUNGER ||
               type == PotionEffectType.LEVITATION ||
               type == PotionEffectType.UNLUCK ||
               type == PotionEffectType.BAD_OMEN ||
               type == PotionEffectType.DARKNESS;
    }

    private void showActivationParticles(Player player, AbilityType ability) {
        Location loc = player.getLocation().add(0, 1, 0);
        Particle particle = Particle.HEART;
        int count = 30;

        switch (ability) {
            case VAMPIRE:
                particle = Particle.DAMAGE_INDICATOR;
                count = 20;
                break;
            case BERSERK:
                particle = Particle.FLAME;
                count = 40;
                break;
            case STEADFAST:
                particle = Particle.CRIT;
                count = 25;
                break;
            case DISPEL:
                particle = Particle.ENCHANT;
                count = 30;
                break;
            case DASH:
                particle = Particle.CLOUD;
                count = 15;
                break;
        }
        
        // Показываем партиклы вокруг игрока
        player.getWorld().spawnParticle(particle, loc, count, 0.5, 0.5, 0.5, 0.1);
    }
}


