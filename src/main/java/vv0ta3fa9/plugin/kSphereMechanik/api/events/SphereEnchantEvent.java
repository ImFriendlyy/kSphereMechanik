package vv0ta3fa9.plugin.kSphereMechanik.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import vv0ta3fa9.plugin.kSphereMechanik.models.EnchantmentType;
import vv0ta3fa9.plugin.kSphereMechanik.models.Sphere;

/**
 * Событие, вызываемое при добавлении зачарования к сфере
 */
public class SphereEnchantEvent extends SphereEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private final EnchantmentType enchantmentType;
    private final int level;

    public SphereEnchantEvent(Player player, Sphere sphere, EnchantmentType enchantmentType, int level) {
        super(player, sphere);
        this.enchantmentType = enchantmentType;
        this.level = level;
    }

    /**
     * Получает тип зачарования
     */
    public EnchantmentType getEnchantmentType() {
        return enchantmentType;
    }

    /**
     * Получает уровень зачарования
     */
    public int getLevel() {
        return level;
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
