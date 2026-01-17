package vv0ta3fa9.plugin.kSphereMechanik.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;
import vv0ta3fa9.plugin.kSphereMechanik.models.AbilityType;
import vv0ta3fa9.plugin.kSphereMechanik.models.Enchantment;
import vv0ta3fa9.plugin.kSphereMechanik.models.Sphere;
import vv0ta3fa9.plugin.kSphereMechanik.models.SphereRank;
import vv0ta3fa9.plugin.kSphereMechanik.models.SphereType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Утилита для создания предметов сфер
 */
public class ItemBuilder {
    private final KSphereMechanik plugin;
    private final NamespacedKey sphereIdKey;
    private final NamespacedKey sphereTagKey;

    public ItemBuilder(KSphereMechanik plugin) {
        this.plugin = plugin;
        this.sphereIdKey = new NamespacedKey(plugin, "sphere_id");
        this.sphereTagKey = new NamespacedKey(plugin, "sphere");
    }

    /**
     * Создает ItemStack для сферы
     */
    public ItemStack createSphereItem(Sphere sphere) {
        Material material = Material.valueOf(plugin.getConfigManager().getSphereMaterial());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        int modelData = getModelData(sphere.getType(), sphere.getRank());
        meta.setCustomModelData(modelData);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(sphereIdKey, PersistentDataType.STRING, sphere.getId().toString());
        pdc.set(sphereTagKey, PersistentDataType.BOOLEAN, true);

        String name = buildSphereName(sphere);
        meta.setDisplayName(plugin.getColorizer().colorize(name));

        List<String> lore = buildSphereLore(sphere);
        meta.setLore(lore);
        meta.setUnbreakable(true);
        meta.setMaxStackSize(1);
        item.setItemMeta(meta);
        return item;
    }

    private int getModelData(SphereType type, SphereRank rank) {
        return plugin.getConfigManager().getSphereModelData(type.name(), rank.name());
    }

    /**
     * Строит имя сферы с поддержкой tooltips (значения из кэша)
     */
    private String buildSphereName(Sphere sphere) {
        String typeColor = sphere.getType().getColorCode();
        String typeName = sphere.getType().getDisplayName();
        String baseName = typeColor + typeName + " Сфера ";

        if (plugin.getConfigManager().isTooltipsEnabled()) {
            String tooltipTemplate = plugin.getConfigManager().getTooltipItemName();
            return tooltipTemplate.replace("{item_name}", baseName);
        }
        
        return baseName;
    }

    private List<String> buildSphereLore(Sphere sphere) {
        List<String> baseLore = new ArrayList<>();
        KSphereMechanik plugin = this.plugin;
        String rankName = sphere.getRank().getDisplayName();

        int used = sphere.calculateUsedCapacity();
        int max = plugin.getConfigManager().getRankCapacity(sphere.getRank());

        String capacityLine = (plugin.getMessagesManager().getMessage("lore_copacity"))
                .replace("{used}", "" + used)
                .replace("{max}", "" + max);
        if (sphere.getType() == SphereType.DONATE) {
            capacityLine = (plugin.getMessagesManager().getMessage("lore_copacity"))
                    .replace("{used}", "" + used)
                    .replace("{max}", "∞");
        }
        baseLore.add(capacityLine);

        baseLore.add("");

        if (sphere.getType() == SphereType.ACTIVE) {
            if (sphere.isActive() && sphere.getAbility() != null) {
                AbilityType ability = sphere.getAbility();
                baseLore.add(plugin.getMessagesManager().getMessage("lore_skill"));
                baseLore.add(" &7- &e" + ability.getDisplayName());
                baseLore.add(" &7" + ability.getDescription());
                baseLore.add(plugin.getMessagesManager().getMessage("lore_used"));
                baseLore.add("");
            }

            if (!sphere.getEnchantments().isEmpty()) {
                baseLore.add(plugin.getMessagesManager().getMessage("lore_enchant"));
                for (Enchantment enchantment : sphere.getEnchantments()) {
                    baseLore.add(" &7- &e" + enchantment.toString());
                }
                baseLore.add("");
            }
        } else {
            if (!sphere.getEnchantments().isEmpty()) {
                baseLore.add(plugin.getMessagesManager().getMessage("lore_enchant"));
                for (Enchantment enchantment : sphere.getEnchantments()) {
                    baseLore.add(" &7- &e" + enchantment.toString());
                }
                baseLore.add("");
            }

            if (sphere.isActive() && sphere.getAbility() != null) {
                AbilityType ability = sphere.getAbility();
                baseLore.add(plugin.getMessagesManager().getMessage("lore_skill"));
                baseLore.add(" &7- &e" + ability.getDisplayName());
                baseLore.add(" &7" + ability.getDescription());
                baseLore.add(plugin.getMessagesManager().getMessage("lore_used"));
                baseLore.add("");
            }
        }

        if (sphere.isReborn()) {
            baseLore.add(plugin.getMessagesManager().getMessage("lore_rebirth"));
        }

        baseLore.add(rankName);


//        baseLore.add("&8ID: " + sphere.getId().toString());

        List<String> finalLore = new ArrayList<>();
        boolean tooltipsEnabled = plugin.getConfigManager().isTooltipsEnabled();
        String tooltipTemplate = plugin.getConfigManager().getTooltipItemLore();
        
        for (String line : baseLore) {
            String coloredLine = plugin.getColorizer().colorize(line);
            if (tooltipsEnabled) {
                String tooltipLine = tooltipTemplate.replace("{item_lore}", coloredLine);
                finalLore.add(plugin.getColorizer().colorize(tooltipLine));
            } else {
                finalLore.add(coloredLine);
            }
        }

        return finalLore;
    }

    public UUID extractSphereId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String idStr = pdc.get(sphereIdKey, PersistentDataType.STRING);

        if (idStr == null) return null;

        try {
            return UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isSphere(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.getOrDefault(sphereTagKey, PersistentDataType.BOOLEAN, false);
    }
}

