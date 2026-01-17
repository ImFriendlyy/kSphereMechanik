package vv0ta3fa9.plugin.kSphereMechanik.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;

public class PlayerJoinListener implements Listener {
    private final KSphereMechanik plugin;

    public PlayerJoinListener(KSphereMechanik plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getDebugLogger().logDetailed("Игрок " + event.getPlayer().getName() + " присоединился");

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (event.getPlayer().isOnline()) {
                PlayerItemHeldListener.updateEffects(plugin, event.getPlayer());
            }
        }, 20L);
    }
}

