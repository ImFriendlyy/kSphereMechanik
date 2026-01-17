package vv0ta3fa9.plugin.kSphereMechanik.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;
import vv0ta3fa9.plugin.kSphereMechanik.models.AbilityType;
import vv0ta3fa9.plugin.kSphereMechanik.models.Sphere;

import java.util.List;

public class DamageListener implements Listener {
    private final KSphereMechanik plugin;

    public DamageListener(KSphereMechanik plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player attacker = (Player) event.getDamager();
        Sphere sphere = findPlayerSphere(attacker);
        
        if (sphere == null) {
            return;
        }

        double damageMultiplier = plugin.getEnchantmentManager().getDamageMultiplier(sphere);

        if (attacker.hasMetadata("berserk_multiplier")) {
            List<MetadataValue> values = attacker.getMetadata("berserk_multiplier");
            if (!values.isEmpty()) {
                double berserkMultiplier = values.get(0).asDouble();
                damageMultiplier *= berserkMultiplier;
            }
        }
        
        double finalDamage = event.getDamage() * damageMultiplier;
        event.setDamage(finalDamage);

        if (sphere.isActive() && sphere.getAbility() == AbilityType.VAMPIRE) {
            plugin.getAbilityManager().handleVampire(attacker, finalDamage);
        }
        
        plugin.getDebugLogger().logFull("Урон нанесен: " + finalDamage + " (множитель: " + damageMultiplier + ")");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Sphere sphere = findPlayerSphere(player);
        
        if (sphere == null) {
            return;
        }

        double reduction = plugin.getEnchantmentManager().getDamageReduction(sphere);

        if (player.hasMetadata("steadfast_reduction")) {
            List<MetadataValue> values = player.getMetadata("steadfast_reduction");
            if (!values.isEmpty()) {
                double steadfastReduction = values.get(0).asDouble();
                reduction += steadfastReduction;
            }
        }
        
        if (reduction > 0) {
            double finalDamage = event.getDamage() * (1.0 - reduction);
            event.setDamage(Math.max(0, finalDamage));
            
            plugin.getDebugLogger().logFull("Урон получен: " + finalDamage + " (снижение: " + (reduction * 100) + "%)");
        }
    }

    private Sphere findPlayerSphere(Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand != null && plugin.getSphereManager().getItemBuilder().isSphere(offHand)) {
            Sphere sphere = plugin.getSphereManager().getSphereFromItem(offHand);
            if (sphere != null) {
                return sphere;
            }
        }
        return null;
    }
}

