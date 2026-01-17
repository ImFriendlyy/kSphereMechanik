package vv0ta3fa9.plugin.kSphereMechanik.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;
import vv0ta3fa9.plugin.kSphereMechanik.models.Sphere;

public class PlayerItemHeldListener implements Listener {
    private final KSphereMechanik plugin;
    private static final int OFF_HAND_SLOT = 40;

    public PlayerItemHeldListener(KSphereMechanik plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                checkAndUpdateEffects(player);
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        plugin.getDebugLogger().logFull("InventoryClickEvent для " + player.getName() + ", slot=" + event.getSlot() + ", rawSlot=" + event.getRawSlot());

        boolean offHandAffected = false;

        if (event.getRawSlot() == OFF_HAND_SLOT || event.getSlot() == OFF_HAND_SLOT) {
            offHandAffected = true;
            plugin.getDebugLogger().logFull("Off-hand slot affected by direct click");
        }
        if (event.isShiftClick()) {
            offHandAffected = true;
            plugin.getDebugLogger().logFull("Off-hand slot affected by shift-click");
        }
        if (event.getHotbarButton() == OFF_HAND_SLOT) {
            offHandAffected = true;
            plugin.getDebugLogger().logFull("Off-hand slot affected by hotbar swap");
        }
        if (offHandAffected) {
            plugin.getDebugLogger().logFull("Off-hand affected, scheduling effect update for " + player.getName());
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    checkAndUpdateEffects(player);
                }
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                checkAndUpdateEffects(player);
            }
        }, 1L);
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getEnchantmentManager().removeEnchantments(player, null);
    }
    private void handleOffHandChange(Player player, ItemStack oldItem, ItemStack newItem) {
        boolean newIsSphere = oldItem != null && plugin.getSphereManager().getItemBuilder().isSphere(oldItem);
        boolean oldWasSphere = newItem != null && plugin.getSphereManager().getItemBuilder().isSphere(newItem);

        plugin.getDebugLogger().logFull("handleOffHandChange: oldWasSphere=" + oldWasSphere + ", newIsSphere=" + newIsSphere);

        if (oldWasSphere) {
            plugin.getEnchantmentManager().removeEnchantments(player, null);
            plugin.getDebugLogger().logFull("Сняты эффекты старой сферы для " + player.getName());
        }
        if (newIsSphere) {
            plugin.getDebugLogger().logFull("Пытаемся применить эффекты новой сферы для " + player.getName());
            Sphere sphere = plugin.getSphereManager().getSphereFromItem(newItem);
            if (sphere != null) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        plugin.getDebugLogger().logFull("Применяем эффекты сферы " + sphere.getId() + " для " + player.getName());
                        plugin.getEnchantmentManager().applyEnchantments(player, sphere);
                        plugin.getDebugLogger().logFull("Эффекты сферы применены: " + player.getName());
                    }
                }, 1L);
            } else {
                plugin.getDebugLogger().logFull("Не удалось получить сферу из предмета для " + player.getName());
            }
        } else {
            plugin.getDebugLogger().logFull("Новый предмет не является сферой для " + player.getName());
        }
    }
    public void checkAndUpdateEffects(Player player) {
        plugin.getEnchantmentManager().removeEnchantments(player, null);

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand != null && !offHand.getType().isAir() &&
            plugin.getSphereManager().getItemBuilder().isSphere(offHand)) {

            Sphere sphere = plugin.getSphereManager().getSphereFromItem(offHand);
            if (sphere != null) {
                plugin.getDebugLogger().logFull("Сфера найдена, применяем эффекты: " + sphere.getId());
                plugin.getEnchantmentManager().applyEnchantments(player, sphere);
                plugin.getDebugLogger().logFull("Эффекты обновлены для " + player.getName());
            } else {
                plugin.getDebugLogger().logFull("Сфера не найдена в реестре для " + player.getName());
            }
        } else {
            plugin.getDebugLogger().logFull("В off-hand нет сферы или предмета для " + player.getName());
        }
    }
    public static void checkAndApplyEffects(KSphereMechanik plugin, Player player) {
        plugin.getEnchantmentManager().removeEnchantments(player, null);

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand != null && !offHand.getType().isAir() &&
            plugin.getSphereManager().getItemBuilder().isSphere(offHand)) {

            Sphere sphere = plugin.getSphereManager().getSphereFromItem(offHand);
            if (sphere != null) {
                plugin.getEnchantmentManager().applyEnchantments(player, sphere);
            }
        }
    }
    public static void updateEffects(KSphereMechanik plugin, Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();

        plugin.getEnchantmentManager().removeEnchantments(player, null);

        if (offHand != null && !offHand.getType().isAir() &&
            plugin.getSphereManager().getItemBuilder().isSphere(offHand)) {

            plugin.getDebugLogger().logFull("Найдена сфера в off-hand игрока " + player.getName());

            Sphere sphere = plugin.getSphereManager().getSphereFromItem(offHand);
            if (sphere != null) {
                plugin.getDebugLogger().logFull("Сфера загружена: " + sphere.getId() + ", тип: " + sphere.getType() + ", ранк: " + sphere.getRank());
                plugin.getEnchantmentManager().applyEnchantments(player, sphere);
                plugin.getDebugLogger().logFull("Эффекты применены для " + player.getName());
            } else {
                plugin.getDebugLogger().logFull("Не удалось загрузить сферу из предмета для " + player.getName());
            }
        } else {
            plugin.getDebugLogger().logFull("Сфера не найдена в off-hand игрока " + player.getName() + " (предмет: " + (offHand != null ? offHand.getType() : "null") + ")");
        }
    }
}
