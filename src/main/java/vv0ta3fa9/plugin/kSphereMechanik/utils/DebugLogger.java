package vv0ta3fa9.plugin.kSphereMechanik.utils;

import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;
import vv0ta3fa9.plugin.kSphereMechanik.managers.ConfigManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 * Система отладки с уровнями детализации
 * Запись в файл выполняется асинхронно для оптимизации
 */
public class DebugLogger {
    private final KSphereMechanik plugin;
    private boolean enabled;
    private int level;
    private boolean logToFile;
    private File logFile;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public DebugLogger(KSphereMechanik plugin) {
        this.plugin = plugin;
    }
    
    public void reload() {
        ConfigManager configManager = plugin.getConfigManager();
        if (configManager == null) {
            plugin.getLogger().warning("ConfigManager еще не инициализирован, используем значения по умолчанию");
            enabled = false;
            level = 1;
            logToFile = false;
            return;
        }
        
        enabled = configManager.isDebugEnabled();
        level = configManager.getDebugLevel();
        logToFile = configManager.isLogToFile();
        
        if (logToFile) {
            String logPath = configManager.getLogFile();
            logFile = new File(plugin.getDataFolder().getParentFile(), logPath);
            if (!logFile.getParentFile().exists()) {
                logFile.getParentFile().mkdirs();
            }
        }
    }

    public void log(String message) {
        log(1, message);
    }

    public void log(int logLevel, String message) {
        if (!enabled || logLevel > level) {
            return;
        }

        String formatted = "[DEBUG-" + logLevel + "] " + message;

        plugin.getLogger().info(formatted);

        if (logToFile && logFile != null) {
            writeToFile(formatted);
        }
    }

    public void logDetailed(String message) {
        log(2, message);
    }
    public void logFull(String message) {
        log(3, message);
    }
    public void logException(String message, Throwable throwable) {
        if (!enabled) return;
        
        log(3, message + ": " + throwable.getMessage());
        if (logToFile && logFile != null) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
                    pw.println("[" + dateFormat.format(new Date()) + "] EXCEPTION: " + message);
                    throwable.printStackTrace(pw);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "Не удалось записать исключение в файл", e);
                }
            });
        }
    }

    private void writeToFile(String message) {
        final String logMessage = "[" + dateFormat.format(new Date()) + "] " + message + "\n";
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (FileWriter fw = new FileWriter(logFile, true)) {
                fw.write(logMessage);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Не удалось записать в файл отладки", e);
            }
        });
    }

    public void sendToChat(org.bukkit.entity.Player player, String message) {
        if (!enabled) return;
        
        if (plugin.getConfigManager().isShowInChat() && player.hasPermission("kspheres.debug")) {
            player.sendMessage(plugin.getColorizer().colorize("&7[DEBUG] &r" + message));
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getLevel() {
        return level;
    }
}

