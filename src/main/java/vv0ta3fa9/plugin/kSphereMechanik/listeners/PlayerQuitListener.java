package vv0ta3fa9.plugin.kSphereMechanik.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;

public class PlayerQuitListener implements Listener {
    private final KSphereMechanik plugin;

    public PlayerQuitListener(KSphereMechanik plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getEnchantmentManager().removeEnchantments(event.getPlayer(), null);
        
        plugin.getDebugLogger().logDetailed("Игрок " + event.getPlayer().getName() + " вышел");
    }
}

