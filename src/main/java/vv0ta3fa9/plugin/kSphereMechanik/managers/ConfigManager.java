package vv0ta3fa9.plugin.kSphereMechanik.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;
import vv0ta3fa9.plugin.kSphereMechanik.models.SphereRank;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Менеджер конфигурации плагина
 * Все значения получаются через геттеры напрямую из FileConfiguration
 */
public class ConfigManager {
    private final KSphereMechanik plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(KSphereMechanik plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("Конфигурация загружена");
    }

    public void saveConfig() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                config.save(configFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось сохранить config.yml: " + e.getMessage());
            }
        });
    }

    public void reloadConfig() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!configFile.exists()) {
                plugin.saveDefaultConfig();
            }
            config = YamlConfiguration.loadConfiguration(configFile);
            plugin.getLogger().info("Конфигурация перезагружена");
        });
    }


    public void setValue(String path, Object value) {
        config.set(path, value);
    }

    public int getRankCapacity(SphereRank rank) {
        return config.getInt("spheres.ranks." + rank.name().toLowerCase() + ".capacity", rank.getMaxCapacity());
    }

    public String getRankDisplayName(SphereRank rank) {
        return config.getString("spheres.ranks." + rank.name().toLowerCase() + ".display_name", rank.getDisplayName());
    }

    public String getSphereMaterial() {
        return config.getString("spheres.item_material", "NETHER_STAR");
    }

    public int getSphereModelData(String type, String rank) {
        return config.getInt("spheres.model_data." + type.toLowerCase() + "." + rank.toLowerCase(), 1000);
    }

    // Tooltips
    public boolean isTooltipsEnabled() {
        return config.getBoolean("tooltips.enabled", true);
    }

    public String getTooltipItemName() {
        return config.getString("tooltips.item_name", " ꀀ {item_name}");
    }

    public String getTooltipItemLore() {
        return config.getString("tooltips.item_lore", " ꀅ {item_lore}");
    }

    // Зачарования
    public int getEnchantmentBaseCost() {
        return config.getInt("enchantments.base_cost", 2);
    }

    public int getEnchantmentLevelCost() {
        return config.getInt("enchantments.level_cost", 1);
    }

    public String getEnchantmentDisplayName(String enchantmentType) {
        return config.getString("enchantments." + enchantmentType.toLowerCase() + ".display_name", enchantmentType);
    }

    public int getEnchantmentMaxLevel(String enchantmentType) {
        return config.getInt("enchantments." + enchantmentType.toLowerCase() + ".max_level", 1);
    }

    public double getEnchantmentValue(String enchantmentType, String key) {
        return config.getDouble("enchantments." + enchantmentType.toLowerCase() + "." + key, 0.0);
    }

    public int getEnchantmentIntValue(String enchantmentType, String key) {
        return config.getInt("enchantments." + enchantmentType.toLowerCase() + "." + key, 0);
    }

    public List<String> getRebirthEffects() {
        return config.getStringList("enchantments.rebirth.effects_after_rebirth");
    }

    // Способности
    public long getAbilityCooldown(String abilityType) {
        return config.getLong("abilities." + abilityType.toLowerCase() + ".cooldown", 60000);
    }

    public double getAbilityValue(String abilityType, String key) {
        return config.getDouble("abilities." + abilityType.toLowerCase() + "." + key, 1.0);
    }

    public int getAbilityIntValue(String abilityType, String key) {
        return config.getInt("abilities." + abilityType.toLowerCase() + "." + key, 0);
    }

    // Перерождение
    public int getRebirthExpLevel() {
        return config.getInt("rebirth.exp_level_required", 30);
    }

    public double getRebirthAbilityChangeChance() {
        return config.getDouble("rebirth.ability_change_chance", 0.5);
    }

    public double getRebirthHpRestore() {
        return config.getDouble("rebirth.hp_restore_percent", 1.0);
    }


    // Отладка
    public boolean isDebugEnabled() {
        return config.getBoolean("debug.enabled", false);
    }

    public int getDebugLevel() {
        return config.getInt("debug.level", 1);
    }

    public boolean isLogToFile() {
        return config.getBoolean("debug.log_to_file", true);
    }

    public String getLogFile() {
        return config.getString("debug.log_file", "logs/kspheres-debug.log");
    }

    public boolean isShowInChat() {
        return config.getBoolean("debug.show_in_chat", false);
    }

    // Данные
    public String getDataFormat() {
        return config.getString("data.format", "json");
    }

    public int getAutoSaveInterval() {
        return config.getInt("data.auto_save_interval", 300);
    }

    // Производительность
    public int getGlobalCheckInterval() {
        return config.getInt("performance.global_check_interval", 600);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
