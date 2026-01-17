package vv0ta3fa9.plugin.kSphereMechanik.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;
import vv0ta3fa9.plugin.kSphereMechanik.models.Sphere;

public class SphereUseListener implements Listener {
    private final KSphereMechanik plugin;

    public SphereUseListener(KSphereMechanik plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack offHand = event.getPlayer().getInventory().getItemInOffHand();
        if (offHand == null || !plugin.getSphereManager().getItemBuilder().isSphere(offHand)) {
            return;
        }

        Sphere sphere = plugin.getSphereManager().getSphereFromItem(offHand);
        if (sphere == null || !sphere.isActive()) {
            return;
        }

        boolean activated = plugin.getAbilityManager().activateAbility(event.getPlayer(), sphere);
        
        if (activated) {
            ItemStack updated = plugin.getSphereManager().updateSphereItem(offHand, sphere);
            event.getPlayer().getInventory().setItemInOffHand(updated);
        }
        
        event.setCancelled(true);
    }
}

