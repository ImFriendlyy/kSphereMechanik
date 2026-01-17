package vv0ta3fa9.plugin.kSphereMechanik.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import vv0ta3fa9.plugin.kSphereMechanik.models.AbilityType;
import vv0ta3fa9.plugin.kSphereMechanik.models.Sphere;

/**
 * Событие, вызываемое при активации способности сферы
 */
public class SphereAbilityActivateEvent extends SphereEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private final AbilityType abilityType;

    public SphereAbilityActivateEvent(Player player, Sphere sphere, AbilityType abilityType) {
        super(player, sphere);
        this.abilityType = abilityType;
    }

    /**
     * Получает тип активируемой способности
     */
    public AbilityType getAbilityType() {
        return abilityType;
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
