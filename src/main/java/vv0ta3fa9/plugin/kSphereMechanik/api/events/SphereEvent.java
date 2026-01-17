package vv0ta3fa9.plugin.kSphereMechanik.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import vv0ta3fa9.plugin.kSphereMechanik.models.Sphere;

/**
 * Базовое событие для всех событий, связанных со сферами
 */
public abstract class SphereEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Sphere sphere;

    public SphereEvent(Player player, Sphere sphere) {
        this.player = player;
        this.sphere = sphere;
    }

    /**
     * Получает игрока, связанного с событием
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Получает сферу, связанную с событием
     */
    public Sphere getSphere() {
        return sphere;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
