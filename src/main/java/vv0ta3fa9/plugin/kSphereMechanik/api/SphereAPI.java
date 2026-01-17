package vv0ta3fa9.plugin.kSphereMechanik.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;
import vv0ta3fa9.plugin.kSphereMechanik.models.*;
import vv0ta3fa9.plugin.kSphereMechanik.utils.ItemBuilder;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class SphereAPI {

    private static KSphereMechanik plugin;

    public static void init(KSphereMechanik kSphereMechanik) {
        plugin = kSphereMechanik;
    }

    public static boolean isInitialized() {
        return plugin != null;
    }

    public static KSphereMechanik getPlugin() {
        if (!isInitialized()) {
            throw new IllegalStateException("SphereAPI не инициализирован! Вызовите SphereAPI.init() при включении вашего плагина.");
        }
        return plugin;
    }

    public static boolean isSphere(ItemStack item) {
        return getPlugin().getSphereManager().getItemBuilder().isSphere(item);
    }

    public static Sphere getSphere(ItemStack item) {
        return getPlugin().getSphereManager().getSphereFromItem(item);
    }

    public static Sphere createSphere(SphereType type, SphereRank rank) {
        return getPlugin().getSphereManager().createSphere(type, rank);
    }

    public static ItemStack giveSphere(Player player, SphereType type, SphereRank rank) {
        return getPlugin().getSphereManager().giveSphereToPlayer(player, type, rank);
    }

    /**
     * Создает сферу с зачарованием и выдает игроку
     * @param player игрок
     * @param type тип сферы
     * @param rank ранг сферы
     * @param enchantmentType тип зачарования
     * @param level уровень зачарования
     * @return ItemStack сферы или null если не удалось создать
     */
    public static ItemStack giveSphereWithEnchantment(Player player, SphereType type, SphereRank rank,
                                                     EnchantmentType enchantmentType, int level) {
        Sphere sphere = createSphere(type, rank);
        if (addEnchantment(sphere, enchantmentType, level)) {
            return getPlugin().getSphereManager().giveSphereToPlayer(player, sphere);
        }
        return null;
    }

    public static boolean addEnchantment(Sphere sphere, EnchantmentType enchantmentType, int level) {
        return getPlugin().getSphereManager().addEnchantment(sphere, enchantmentType, level);
    }

    public static boolean removeEnchantment(Sphere sphere, EnchantmentType enchantmentType) {
        return getPlugin().getSphereManager().removeEnchantment(sphere, enchantmentType);
    }

    public static boolean hasEnchantment(Sphere sphere, EnchantmentType enchantmentType) {
        return sphere.hasEnchantment(enchantmentType);
    }

    public static int getEnchantmentLevel(Sphere sphere, EnchantmentType enchantmentType) {
        return sphere.getEnchantmentLevel(enchantmentType);
    }

    public static void applySphereEffects(Player player, Sphere sphere) {
        getPlugin().getEnchantmentManager().applyEnchantments(player, sphere);
    }


    public static void removeSphereEffects(Player player, Sphere sphere) {
        getPlugin().getEnchantmentManager().removeEnchantments(player, sphere);
    }

    public static void updateSphereEffects(Player player, Sphere sphere) {
        removeSphereEffects(player, null);
        if (sphere != null) {
            applySphereEffects(player, sphere);
        }
    }

    public static boolean activateAbility(Player player, Sphere sphere) {
        return getPlugin().getAbilityManager().activateAbility(player, sphere);
    }

    public static boolean canActivateAbility(Player player, Sphere sphere) {
        if (sphere == null || !sphere.isActive() || sphere.getAbility() == null) {
            return false;
        }

        long cooldown = getPlugin().getConfigManager().getAbilityCooldown(sphere.getAbility().name());
        long timeSinceLastUse = System.currentTimeMillis() - sphere.getLastAbilityUse();
        return timeSinceLastUse >= cooldown;
    }

    public static Sphere getOffHandSphere(Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        return isSphere(offHand) ? getSphere(offHand) : null;
    }

    /**
     * Получает текущую сферу игрока (в off-hand)
     * @param player игрок
     * @return сфера или null
     */
    public static Sphere getCurrentSphere(Player player) {
        return getOffHandSphere(player);
    }

    public static boolean hasActiveSphere(Player player) {
        Sphere sphere = getCurrentSphere(player);
        return sphere != null && sphere.isActive();
    }


    public static boolean rebirthSphere(Sphere sphere, Player player) {
        return getPlugin().getSphereManager().rebirthSphere(sphere, player);
    }

    public static boolean canRebirthSphere(Sphere sphere, Player player) {
        if (sphere == null || sphere.isReborn()) {
            return false;
        }

        int requiredLevel = getPlugin().getConfigManager().getRebirthExpLevel();
        return player.getLevel() >= requiredLevel;
    }


    public static void saveSphere(Sphere sphere) {
        getPlugin().getDataManager().saveSphere(sphere);
    }

    public static Sphere loadSphere(UUID sphereId) {
        return getPlugin().getDataManager().loadSphere(sphereId);
    }

    public static void loadSphereAsync(UUID sphereId, Consumer<Sphere> callback) {
        getPlugin().getDataManager().loadSphereAsync(sphereId, callback);
    }

    public static ItemBuilder getItemBuilder() {
        return getPlugin().getSphereManager().getItemBuilder();
    }

    public static SphereType[] getSphereTypes() {
        return SphereType.values();
    }

    public static SphereRank[] getSphereRanks() {
        return SphereRank.values();
    }

    public static EnchantmentType[] getEnchantmentTypes() {
        return EnchantmentType.values();
    }

    public static AbilityType[] getAbilityTypes() {
        return AbilityType.values();
    }

    public static void clearSphere(Sphere sphere) {
        getPlugin().getSphereManager().clearSphere(sphere);
    }

    public static ItemStack updateSphereItem(Player player, Sphere sphere) {
        return getPlugin().getSphereManager().updateSphereItem(player.getInventory().getItemInOffHand(), sphere);
    }
}
