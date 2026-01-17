package vv0ta3fa9.plugin.kSphereMechanik.managers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;
import vv0ta3fa9.plugin.kSphereMechanik.api.SphereEvents;
import vv0ta3fa9.plugin.kSphereMechanik.models.*;
import vv0ta3fa9.plugin.kSphereMechanik.utils.CapacityCalculator;
import vv0ta3fa9.plugin.kSphereMechanik.utils.ItemBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер для управления сферами игроков
 */
public class SphereManager {
    private final KSphereMechanik plugin;
    private final Map<UUID, Sphere> playerSpheres; // UUID игрока -> сфера
    private final Map<UUID, Sphere> sphereRegistry; // UUID сферы -> сфера
    private final ItemBuilder itemBuilder;
    private final CapacityCalculator capacityCalculator;

    public SphereManager(KSphereMechanik plugin) {
        this.plugin = plugin;
        this.playerSpheres = new ConcurrentHashMap<>();
        this.sphereRegistry = new ConcurrentHashMap<>();
        this.itemBuilder = new ItemBuilder(plugin);
        this.capacityCalculator = new CapacityCalculator(plugin);
    }

    /**
     * Создает новую сферу
     */
    public Sphere createSphere(SphereType type, SphereRank rank) {
        UUID sphereId = UUID.randomUUID();
        Sphere sphere = new Sphere(sphereId, type, rank);
        sphereRegistry.put(sphereId, sphere);
        
        plugin.getDebugLogger().logDetailed("Создана сфера: " + type + " " + rank + " (ID: " + sphereId + ")");
        return sphere;
    }

    /**
     * Выдает сферу игроку
     * Сохранение сферы выполняется асинхронно в фоне
     */
    public ItemStack giveSphereToPlayer(Player player, SphereType type, SphereRank rank) {
        Sphere sphere = createSphere(type, rank);

        SphereEvents.callSphereCreateEvent(player, sphere, type, rank);

        ItemStack item = itemBuilder.createSphereItem(sphere);
        player.getInventory().addItem(item);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDataManager().saveSphere(sphere);
        });

        plugin.getDebugLogger().log("Выдана сфера игроку " + player.getName() + ": " + type + " " + rank);
        return item;
    }

    public ItemStack giveSphereToPlayer(Player player, Sphere sphere) {
        SphereEvents.callSphereCreateEvent(player, sphere, sphere.getType(), sphere.getRank());

        ItemStack item = itemBuilder.createSphereItem(sphere);
        player.getInventory().addItem(item);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDataManager().saveSphere(sphere);
        });

        plugin.getDebugLogger().log("Выдана сфера игроку " + player.getName() + ": " + sphere.getType() + " " + sphere.getRank());
        return item;
    }

    /**
     * Получает сферу из ItemStack
     * Если сфера не в реестре - пытается загрузить из файла
     */
    public Sphere getSphereFromItem(ItemStack item) {
        if (item == null || !itemBuilder.isSphere(item)) {
            plugin.getDebugLogger().logFull("Предмет не является сферой");
            return null;
        }

        UUID sphereId = itemBuilder.extractSphereId(item);
        if (sphereId == null) {
            plugin.getDebugLogger().logFull("Не удалось извлечь ID сферы из предмета");
            return null;
        }

        plugin.getDebugLogger().logFull("Извлечен ID сферы: " + sphereId);

        // Сначала проверяем реестр
        Sphere sphere = sphereRegistry.get(sphereId);
        if (sphere != null) {
            plugin.getDebugLogger().logFull("Сфера найдена в реестре: " + sphereId);
            return sphere;
        }

        plugin.getDebugLogger().logFull("Сфера не найдена в реестре, пытаемся загрузить из файла: " + sphereId);

        // Если нет в реестре - пытаемся загрузить из файла
        sphere = plugin.getDataManager().loadSphere(sphereId);
        if (sphere != null) {
            // Регистрируем загруженную сферу
            sphereRegistry.put(sphereId, sphere);
            plugin.getDebugLogger().logFull("Сфера загружена из файла: " + sphereId);
        } else {
            plugin.getDebugLogger().logFull("Не удалось загрузить сферу из файла: " + sphereId);
        }

        return sphere;
    }

    /**
     * Обновляет ItemStack сферы (после изменения зачарований и т.д.)
     */
    public ItemStack updateSphereItem(ItemStack item, Sphere sphere) {
        if (item == null || sphere == null) {
            return null;
        }
        
        return itemBuilder.createSphereItem(sphere);
    }

    /**
     * Добавляет зачарование к сфере
     * Сохранение выполняется асинхронно
     */
    public boolean addEnchantment(Sphere sphere, vv0ta3fa9.plugin.kSphereMechanik.models.EnchantmentType type, int level) {
        if (sphere == null) return false;
        
        // Проверка максимального уровня
        if (level > type.getMaxLevel()) {
            return false;
        }
        
        int maxCapacity = plugin.getConfigManager().getRankCapacity(sphere.getRank());
        vv0ta3fa9.plugin.kSphereMechanik.models.Enchantment enchantment = capacityCalculator.createEnchantment(type, level);
        
        boolean added = sphere.addEnchantment(enchantment, maxCapacity);
        if (added) {
            plugin.getDebugLogger().logDetailed("Добавлено зачарование " + type + " Ур." + level + " к сфере " + sphere.getId());
            // Сохраняем асинхронно
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                plugin.getDataManager().saveSphere(sphere);
            });
        }
        
        return added;
    }

    /**
     * Удаляет зачарование из сферы
     * Сохранение выполняется асинхронно
     */
    public boolean removeEnchantment(Sphere sphere, vv0ta3fa9.plugin.kSphereMechanik.models.EnchantmentType type) {
        if (sphere == null) return false;
        
        boolean removed = sphere.removeEnchantment(type);
        if (removed) {
            plugin.getDebugLogger().logDetailed("Удалено зачарование " + type + " из сферы " + sphere.getId());
            // Сохраняем асинхронно
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                plugin.getDataManager().saveSphere(sphere);
            });
        }
        
        return removed;
    }

    /**
     * Очищает все зачарования сферы
     * Сохранение выполняется асинхронно
     */
    public void clearSphere(Sphere sphere) {
        if (sphere == null) return;
        
        sphere.clearEnchantments();
        plugin.getDebugLogger().log("Очищена сфера " + sphere.getId());
        // Сохраняем асинхронно
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDataManager().saveSphere(sphere);
        });
    }

    /**
     * Перерождает сферу
     * Сохранение выполняется асинхронно
     */
    public boolean rebirthSphere(Sphere sphere, Player player) {
        if (sphere == null || player == null) return false;
        
        // Проверка уровня опыта
        int requiredLevel = plugin.getConfigManager().getRebirthExpLevel();
        if (player.getLevel() < requiredLevel) {
            return false;
        }
        
        if (sphere.getType() == SphereType.NORMAL) {
            // Обычная сфера - очистка всех зачарований
            clearSphere(sphere);
            sphere.setReborn(false);
        } else if (sphere.getType() == SphereType.ACTIVE) {
            // Активная сфера - зачарования остаются, способность может измениться
            double chance = plugin.getConfigManager().getRebirthAbilityChangeChance();
            if (Math.random() < chance) {
                // Меняем способность на случайную
                vv0ta3fa9.plugin.kSphereMechanik.models.AbilityType[] abilities = vv0ta3fa9.plugin.kSphereMechanik.models.AbilityType.values();
                vv0ta3fa9.plugin.kSphereMechanik.models.AbilityType newAbility = abilities[new Random().nextInt(abilities.length)];
                sphere.setAbility(newAbility);
                plugin.getDebugLogger().log("Способность изменена на " + newAbility);
            }
        }
        
        sphere.setReborn(true);

        // Вызываем событие перерождения
        SphereEvents.callSphereRebirthEvent(player, sphere);

        plugin.getDebugLogger().log("Сфера " + sphere.getId() + " перерождена игроком " + player.getName());
        // Сохраняем асинхронно
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDataManager().saveSphere(sphere);
        });
        return true;
    }

    /**
     * Регистрирует сферу
     */
    public void registerSphere(Sphere sphere) {
        if (sphere != null) {
            sphereRegistry.put(sphere.getId(), sphere);
        }
    }

    /**
     * Удаляет сферу из реестра
     */
    public void unregisterSphere(UUID sphereId) {
        sphereRegistry.remove(sphereId);
    }

    /**
     * Получает все зарегистрированные сферы
     */
    public Collection<Sphere> getAllSpheres() {
        return Collections.unmodifiableCollection(sphereRegistry.values());
    }

    public ItemBuilder getItemBuilder() {
        return itemBuilder;
    }

    public CapacityCalculator getCapacityCalculator() {
        return capacityCalculator;
    }
}

