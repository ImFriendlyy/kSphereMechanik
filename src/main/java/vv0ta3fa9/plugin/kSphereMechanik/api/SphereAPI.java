package vv0ta3fa9.plugin.kSphereMechanik.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;
import vv0ta3fa9.plugin.kSphereMechanik.models.*;
import vv0ta3fa9.plugin.kSphereMechanik.utils.ItemBuilder;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Основной API класс для интеграции с kSphereMechanik
 * Предоставляет высокоуровневые методы для работы со сферами
 */
public class SphereAPI {

    private static KSphereMechanik plugin;

    /**
     * Инициализация API. Должна быть вызвана при включении плагина
     */
    public static void init(KSphereMechanik kSphereMechanik) {
        plugin = kSphereMechanik;
    }

    /**
     * Проверяет, инициализирован ли API
     */
    public static boolean isInitialized() {
        return plugin != null;
    }

    /**
     * Получает экземпляр главного плагина
     */
    public static KSphereMechanik getPlugin() {
        if (!isInitialized()) {
            throw new IllegalStateException("SphereAPI не инициализирован! Вызовите SphereAPI.init() при включении вашего плагина.");
        }
        return plugin;
    }

    // ===== ОСНОВНЫЕ МЕТОДЫ РАБОТЫ СО СФЕРАМИ =====

    /**
     * Проверяет, является ли предмет сферой
     * @param item предмет для проверки
     * @return true если предмет является сферой
     */
    public static boolean isSphere(ItemStack item) {
        return getPlugin().getSphereManager().getItemBuilder().isSphere(item);
    }

    /**
     * Получает сферу из предмета
     * @param item предмет сферы
     * @return сфера или null если предмет не является сферой
     */
    public static Sphere getSphere(ItemStack item) {
        return getPlugin().getSphereManager().getSphereFromItem(item);
    }

    /**
     * Создает новую сферу
     * @param type тип сферы
     * @param rank ранг сферы
     * @return созданная сфера
     */
    public static Sphere createSphere(SphereType type, SphereRank rank) {
        return getPlugin().getSphereManager().createSphere(type, rank);
    }

    /**
     * Создает сферу и выдает игроку
     * @param player игрок
     * @param type тип сферы
     * @param rank ранг сферы
     * @return ItemStack сферы или null если не удалось создать
     */
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

    // ===== РАБОТА С ЗАЧАРОВАНИЯМИ =====

    /**
     * Добавляет зачарование к сфере
     * @param sphere сфера
     * @param enchantmentType тип зачарования
     * @param level уровень
     * @return true если зачарование добавлено успешно
     */
    public static boolean addEnchantment(Sphere sphere, EnchantmentType enchantmentType, int level) {
        return getPlugin().getSphereManager().addEnchantment(sphere, enchantmentType, level);
    }

    /**
     * Удаляет зачарование со сферы
     * @param sphere сфера
     * @param enchantmentType тип зачарования
     * @return true если зачарование удалено
     */
    public static boolean removeEnchantment(Sphere sphere, EnchantmentType enchantmentType) {
        return getPlugin().getSphereManager().removeEnchantment(sphere, enchantmentType);
    }

    /**
     * Проверяет, имеет ли сфера зачарование
     * @param sphere сфера
     * @param enchantmentType тип зачарования
     * @return true если зачарование есть
     */
    public static boolean hasEnchantment(Sphere sphere, EnchantmentType enchantmentType) {
        return sphere.hasEnchantment(enchantmentType);
    }

    /**
     * Получает уровень зачарования на сфере
     * @param sphere сфера
     * @param enchantmentType тип зачарования
     * @return уровень зачарования или 0 если его нет
     */
    public static int getEnchantmentLevel(Sphere sphere, EnchantmentType enchantmentType) {
        return sphere.getEnchantmentLevel(enchantmentType);
    }

    // ===== РАБОТА С ЭФФЕКТАМИ =====

    /**
     * Применяет все эффекты сферы к игроку
     * @param player игрок
     * @param sphere сфера
     */
    public static void applySphereEffects(Player player, Sphere sphere) {
        getPlugin().getEnchantmentManager().applyEnchantments(player, sphere);
    }

    /**
     * Убирает все эффекты сферы у игрока
     * @param player игрок
     * @param sphere сфера (null для снятия всех эффектов сфер)
     */
    public static void removeSphereEffects(Player player, Sphere sphere) {
        getPlugin().getEnchantmentManager().removeEnchantments(player, sphere);
    }

    /**
     * Обновляет эффекты сферы у игрока (сначала снимает старые, потом применяет новые)
     * @param player игрок
     * @param sphere сфера
     */
    public static void updateSphereEffects(Player player, Sphere sphere) {
        removeSphereEffects(player, null);
        if (sphere != null) {
            applySphereEffects(player, sphere);
        }
    }

    // ===== РАБОТА СО СПОСОБНОСТЯМИ =====

    /**
     * Активирует способность сферы
     * @param player игрок
     * @param sphere сфера
     * @return true если способность активирована
     */
    public static boolean activateAbility(Player player, Sphere sphere) {
        return getPlugin().getAbilityManager().activateAbility(player, sphere);
    }

    /**
     * Проверяет, может ли игрок активировать способность
     * @param player игрок
     * @param sphere сфера
     * @return true если может активировать
     */
    public static boolean canActivateAbility(Player player, Sphere sphere) {
        if (sphere == null || !sphere.isActive() || sphere.getAbility() == null) {
            return false;
        }

        long cooldown = getPlugin().getConfigManager().getAbilityCooldown(sphere.getAbility().name());
        long timeSinceLastUse = System.currentTimeMillis() - sphere.getLastAbilityUse();
        return timeSinceLastUse >= cooldown;
    }

    // ===== ПОЛУЧЕНИЕ ИНФОРМАЦИИ =====

    /**
     * Получает все сферы определенного типа у игрока
     * @param player игрок
     * @param type тип сферы (null для всех типов)
     * @return список сфер
     */
    public static List<Sphere> getPlayerSpheres(Player player, SphereType type) {
        return getPlugin().getSphereManager().getPlayerSpheres(player, type);
    }

    /**
     * Получает сферу в off-hand слоте игрока
     * @param player игрок
     * @return сфера или null
     */
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

    /**
     * Проверяет, есть ли у игрока активная сфера
     * @param player игрок
     * @return true если есть активная сфера
     */
    public static boolean hasActiveSphere(Player player) {
        Sphere sphere = getCurrentSphere(player);
        return sphere != null && sphere.isActive();
    }

    // ===== ПЕРЕРОЖДЕНИЕ =====

    /**
     * Перерождает сферу
     * @param sphere сфера для перерождения
     * @param player игрок (для проверки уровня опыта)
     * @return true если перерождение успешно
     */
    public static boolean rebirthSphere(Sphere sphere, Player player) {
        return getPlugin().getSphereManager().rebirthSphere(sphere, player);
    }

    /**
     * Проверяет, может ли сфера быть перерождена
     * @param sphere сфера
     * @param player игрок
     * @return true если может быть перерождена
     */
    public static boolean canRebirthSphere(Sphere sphere, Player player) {
        if (sphere == null || sphere.isReborn()) {
            return false;
        }

        int requiredLevel = getPlugin().getConfigManager().getRebirthExpLevel();
        return player.getLevel() >= requiredLevel;
    }

    // ===== СОХРАНЕНИЕ И ЗАГРУЗКА =====

    /**
     * Сохраняет сферу
     * @param sphere сфера для сохранения
     */
    public static void saveSphere(Sphere sphere) {
        getPlugin().getDataManager().saveSphere(sphere);
    }

    /**
     * Загружает сферу по ID
     * @param sphereId ID сферы
     * @return сфера или null
     */
    public static Sphere loadSphere(UUID sphereId) {
        return getPlugin().getDataManager().loadSphere(sphereId);
    }

    /**
     * Загружает сферу асинхронно с колбэком
     * @param sphereId ID сферы
     * @param callback функция обратного вызова
     */
    public static void loadSphereAsync(UUID sphereId, Consumer<Sphere> callback) {
        getPlugin().getDataManager().loadSphereAsync(sphereId, callback);
    }

    // ===== УТИЛИТЫ =====

    /**
     * Получает ItemBuilder для создания предметов сфер
     * @return ItemBuilder
     */
    public static ItemBuilder getItemBuilder() {
        return getPlugin().getSphereManager().getItemBuilder();
    }

    /**
     * Получает список всех доступных типов сфер
     * @return массив типов сфер
     */
    public static SphereType[] getSphereTypes() {
        return SphereType.values();
    }

    /**
     * Получает список всех доступных рангов сфер
     * @return массив рангов сфер
     */
    public static SphereRank[] getSphereRanks() {
        return SphereRank.values();
    }

    /**
     * Получает список всех доступных типов зачарований
     * @return массив типов зачарований
     */
    public static EnchantmentType[] getEnchantmentTypes() {
        return EnchantmentType.values();
    }

    /**
     * Получает список всех доступных типов способностей
     * @return массив типов способностей
     */
    public static AbilityType[] getAbilityTypes() {
        return AbilityType.values();
    }

    /**
     * Очищает сферу (удаляет все зачарования)
     * @param sphere сфера для очистки
     */
    public static void clearSphere(Sphere sphere) {
        getPlugin().getSphereManager().clearSphere(sphere);
    }

    /**
     * Обновляет предмет сферы в инвентаре игрока
     * @param player игрок
     * @param sphere сфера
     * @return обновленный ItemStack
     */
    public static ItemStack updateSphereItem(Player player, Sphere sphere) {
        return getPlugin().getSphereManager().updateSphereItem(player.getInventory().getItemInOffHand(), sphere);
    }
}
