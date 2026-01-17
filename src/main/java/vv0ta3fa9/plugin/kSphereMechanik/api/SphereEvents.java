package vv0ta3fa9.plugin.kSphereMechanik.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;
import vv0ta3fa9.plugin.kSphereMechanik.api.events.*;
import vv0ta3fa9.plugin.kSphereMechanik.models.*;

/**
 * Класс для вызова API событий сфер
 * Используется внутренними механизмами плагина для оповещения других плагинов
 */
public class SphereEvents {

    private static KSphereMechanik plugin;

    /**
     * Инициализация событий
     */
    public static void init(KSphereMechanik kSphereMechanik) {
        plugin = kSphereMechanik;
    }

    /**
     * Вызывает событие создания сферы
     */
    public static SphereCreateEvent callSphereCreateEvent(Player player, Sphere sphere, SphereType type, SphereRank rank) {
        SphereCreateEvent event = new SphereCreateEvent(player, sphere, type, rank);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Вызывает событие добавления зачарования
     */
    public static SphereEnchantEvent callSphereEnchantEvent(Player player, Sphere sphere, EnchantmentType enchantmentType, int level) {
        SphereEnchantEvent event = new SphereEnchantEvent(player, sphere, enchantmentType, level);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Вызывает событие активации способности
     */
    public static SphereAbilityActivateEvent callSphereAbilityActivateEvent(Player player, Sphere sphere, AbilityType abilityType) {
        SphereAbilityActivateEvent event = new SphereAbilityActivateEvent(player, sphere, abilityType);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Вызывает событие перерождения сферы
     */
    public static SphereRebirthEvent callSphereRebirthEvent(Player player, Sphere sphere) {
        SphereRebirthEvent event = new SphereRebirthEvent(player, sphere);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
}
