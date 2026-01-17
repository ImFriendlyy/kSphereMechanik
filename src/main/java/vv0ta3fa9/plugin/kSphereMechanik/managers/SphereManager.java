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
    public Sphere createSphere(SphereType type, SphereRank rank) {
        UUID sphereId = UUID.randomUUID();
        Sphere sphere = new Sphere(sphereId, type, rank);
        sphereRegistry.put(sphereId, sphere);
        
        plugin.getDebugLogger().logDetailed("Создана сфера: " + type + " " + rank + " (ID: " + sphereId + ")");
        return sphere;
    }

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

        Sphere sphere = sphereRegistry.get(sphereId);
        if (sphere != null) {
            plugin.getDebugLogger().logFull("Сфера найдена в реестре: " + sphereId);
            return sphere;
        }

        plugin.getDebugLogger().logFull("Сфера не найдена в реестре, пытаемся загрузить из файла: " + sphereId);

        sphere = plugin.getDataManager().loadSphere(sphereId);
        if (sphere != null) {
            sphereRegistry.put(sphereId, sphere);
            plugin.getDebugLogger().logFull("Сфера загружена из файла: " + sphereId);
        } else {
            plugin.getDebugLogger().logFull("Не удалось загрузить сферу из файла: " + sphereId);
        }

        return sphere;
    }

    public ItemStack updateSphereItem(ItemStack item, Sphere sphere) {
        if (item == null || sphere == null) {
            return null;
        }
        
        return itemBuilder.createSphereItem(sphere);
    }

    public boolean addEnchantment(Sphere sphere, vv0ta3fa9.plugin.kSphereMechanik.models.EnchantmentType type, int level) {
        if (sphere == null) return false;

        if (level > type.getMaxLevel()) {
            return false;
        }
        
        int maxCapacity = plugin.getConfigManager().getRankCapacity(sphere.getRank());
        vv0ta3fa9.plugin.kSphereMechanik.models.Enchantment enchantment = capacityCalculator.createEnchantment(type, level);
        
        boolean added = sphere.addEnchantment(enchantment, maxCapacity);
        if (added) {
            plugin.getDebugLogger().logDetailed("Добавлено зачарование " + type + " Ур." + level + " к сфере " + sphere.getId());
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                plugin.getDataManager().saveSphere(sphere);
            });
        }
        
        return added;
    }

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

    public void clearSphere(Sphere sphere) {
        if (sphere == null) return;
        
        sphere.clearEnchantments();
        plugin.getDebugLogger().log("Очищена сфера " + sphere.getId());
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDataManager().saveSphere(sphere);
        });
    }

    public boolean rebirthSphere(Sphere sphere, Player player) {
        if (sphere == null || player == null) return false;

        int requiredLevel = plugin.getConfigManager().getRebirthExpLevel();
        if (player.getLevel() < requiredLevel) {
            return false;
        }
        
        if (sphere.getType() == SphereType.NORMAL) {
            clearSphere(sphere);
            sphere.setReborn(false);
        } else if (sphere.getType() == SphereType.ACTIVE) {
            double chance = plugin.getConfigManager().getRebirthAbilityChangeChance();
            if (Math.random() < chance) {
                vv0ta3fa9.plugin.kSphereMechanik.models.AbilityType[] abilities = vv0ta3fa9.plugin.kSphereMechanik.models.AbilityType.values();
                vv0ta3fa9.plugin.kSphereMechanik.models.AbilityType newAbility = abilities[new Random().nextInt(abilities.length)];
                sphere.setAbility(newAbility);
                plugin.getDebugLogger().log("Способность изменена на " + newAbility);
            }
        }
        
        sphere.setReborn(true);

        SphereEvents.callSphereRebirthEvent(player, sphere);

        plugin.getDebugLogger().log("Сфера " + sphere.getId() + " перерождена игроком " + player.getName());
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDataManager().saveSphere(sphere);
        });
        return true;
    }


    public void registerSphere(Sphere sphere) {
        if (sphere != null) {
            sphereRegistry.put(sphere.getId(), sphere);
        }
    }

    public void unregisterSphere(UUID sphereId) {
        sphereRegistry.remove(sphereId);
    }

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

