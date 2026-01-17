package vv0ta3fa9.plugin.kSphereMechanik.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import vv0ta3fa9.plugin.kSphereMechanik.models.Sphere;

/**
 * Событие, вызываемое при перерождении сферы
 */
public class SphereRebirthEvent extends SphereEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    public SphereRebirthEvent(Player player, Sphere sphere) {
        super(player, sphere);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
