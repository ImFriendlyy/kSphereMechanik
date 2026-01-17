package vv0ta3fa9.plugin.kSphereMechanik.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;

import java.io.File;
import java.io.IOException;

public class MessagesManager {
    private final KSphereMechanik plugin;
    private FileConfiguration message;
    private File messagesFile;

    public MessagesManager(KSphereMechanik plugin) {
        this.plugin = plugin;
        loadMessagesConfig();
    }

    public void loadMessagesConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            plugin.saveDefaultConfig();
        }

        message = YamlConfiguration.loadConfiguration(messagesFile);
        plugin.getLogger().info("Конфигурация загружена");
    }

    public void saveConfig() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                message.save(messagesFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось сохранить messages.yml: " + e.getMessage());
            }
        });
    }

    public void reloadConfig() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!messagesFile.exists()) {
                plugin.saveDefaultConfig();
            }
            message = YamlConfiguration.loadConfiguration(messagesFile);
            plugin.getLogger().info("Конфигурация messages.yml перезагружена");
        });
    }

    public String getMessage(String key) {
        String msg = message.getString("messages." + key);
        return msg != null ? msg : "&cСообщение не найдено: " + key;
    }

}
