package vv0ta3fa9.plugin.kSphereMechanik.listeners;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;
import vv0ta3fa9.plugin.kSphereMechanik.commands.RunesCommand;
import vv0ta3fa9.plugin.kSphereMechanik.models.EnchantmentType;
import vv0ta3fa9.plugin.kSphereMechanik.models.Sphere;

import java.util.List;

public class DeathListener implements Listener {
    private final KSphereMechanik plugin;

    public DeathListener(KSphereMechanik plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        double finalDamage = event.getFinalDamage();
        if (player.getHealth() - finalDamage > 0) {
            return;
        }

        Sphere rebirthSphere = findRebirthSphere(player);

        if (rebirthSphere == null) {
            return;
        }
        if (rebirthSphere.isReborn()) {
            return;
        }


        RunesCommand runesCommand = (RunesCommand) plugin.getServer()
            .getPluginCommand("runes").getExecutor();
        if (runesCommand != null && !runesCommand.isAutoRebirthEnabled(player)) {
            return;
        }

        double hpRestore = plugin.getConfigManager().getRebirthHpRestore();
        event.setCancelled(true);
        showRebirthEffects(player);

        double maxHealth = player.getMaxHealth();
        player.setHealth(maxHealth * hpRestore);

        List<String> effects = plugin.getConfigManager().getRebirthEffects();
        for (String effectStr : effects) {
            String[] parts = effectStr.split(":");
            if (parts.length == 3) {
                try {
                    org.bukkit.potion.PotionEffectType effectType = 
                        org.bukkit.potion.PotionEffectType.getByName(parts[0]);
                    int duration = Integer.parseInt(parts[1]);
                    int level = Integer.parseInt(parts[2]);
                    
                    if (effectType != null) {
                        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            effectType, duration, level - 1, false, true
                        ));
                    }
                } catch (Exception e) {
                    plugin.getDebugLogger().logException("Ошибка применения эффекта перерождения", e);
                }
            }
        }

        rebirthSphere.setReborn(true);
        ItemStack updated = plugin.getSphereManager().updateSphereItem(player.getInventory().getItemInOffHand(), rebirthSphere);
        player.getInventory().setItemInOffHand(updated);
        
        String message = plugin.getMessagesManager().getMessage("rebirth_triggered");
        player.sendMessage(plugin.getColorizer().colorize(plugin.getMessagesManager().getMessage("prefix") + message));
        
        plugin.getDebugLogger().log("Перерождение активировано для " + player.getName());
    }

    private Sphere findRebirthSphere(Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand != null && plugin.getSphereManager().getItemBuilder().isSphere(offHand)) {
            Sphere sphere = plugin.getSphereManager().getSphereFromItem(offHand);
            if (sphere != null && sphere.hasEnchantment(EnchantmentType.REBIRTH)) {
                return sphere;
            }
        }
        return null;
    }

    private void showRebirthEffects(Player player) {
        Location loc = player.getLocation();
        Location particleLoc = loc.clone().add(0, 1, 0);

        player.getWorld().spawnParticle(Particle.ENCHANT, particleLoc, 30, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.HEART, particleLoc, 20, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.CRIT, particleLoc, 25, 0.5, 0.5, 0.5, 0.1);

        player.getWorld().spawnParticle(Particle.ENCHANT, particleLoc, 15, 0.5, 0.5, 0.5, 0.1);

        try {
            player.getWorld().playSound(loc, Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
        } catch (Exception e) {
            try {
                player.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.8f);
                player.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            } catch (Exception e2) {
                plugin.getDebugLogger().logException("Не удалось воспроизвести звук перерождения", e2);
            }
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (int i = 0; i < 3; i++) {
                final int iteration = i;
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (!player.isOnline()) return;
                    
                    Location effectLoc = player.getLocation().add(0, 1, 0);
                    for (int angle = 0; angle < 360; angle += 20) {
                        double radians = Math.toRadians(angle);
                        double radius = 0.5 + (iteration * 0.2);
                        double x = Math.cos(radians) * radius;
                        double z = Math.sin(radians) * radius;
                        Location spiralLoc = effectLoc.clone().add(x, iteration * 0.3, z);

                        Particle particle = (iteration == 0) ? Particle.ENCHANT : 
                                          (iteration == 1) ? Particle.HEART : Particle.CRIT;
                        player.getWorld().spawnParticle(particle, spiralLoc, 1, 0, 0, 0, 0);
                    }
                }, i * 2L);
            }
        });
    }

}

