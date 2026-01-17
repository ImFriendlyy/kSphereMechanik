package vv0ta3fa9.plugin.kSphereMechanik.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import vv0ta3fa9.plugin.kSphereMechanik.models.Sphere;
import vv0ta3fa9.plugin.kSphereMechanik.models.SphereRank;
import vv0ta3fa9.plugin.kSphereMechanik.models.SphereType;

/**
 * Событие, вызываемое при создании новой сферы
 */
public class SphereCreateEvent extends SphereEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private final SphereType sphereType;
    private final SphereRank sphereRank;

    public SphereCreateEvent(Player player, Sphere sphere, SphereType sphereType, SphereRank sphereRank) {
        super(player, sphere);
        this.sphereType = sphereType;
        this.sphereRank = sphereRank;
    }

    /**
     * Получает тип создаваемой сферы
     */
    public SphereType getSphereType() {
        return sphereType;
    }

    /**
     * Получает ранг создаваемой сферы
     */
    public SphereRank getSphereRank() {
        return sphereRank;
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
