package vv0ta3fa9.plugin.kSphereMechanik;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import vv0ta3fa9.plugin.kSphereMechanik.commands.RunesCommand;
import vv0ta3fa9.plugin.kSphereMechanik.commands.SphereCommand;
import vv0ta3fa9.plugin.kSphereMechanik.listeners.*;
import vv0ta3fa9.plugin.kSphereMechanik.managers.*;
import vv0ta3fa9.plugin.kSphereMechanik.models.EnchantmentType;
import vv0ta3fa9.plugin.kSphereMechanik.utils.Color.Colorizer;
import vv0ta3fa9.plugin.kSphereMechanik.utils.DebugLogger;

public class KSphereMechanik extends JavaPlugin {

    protected Colorizer colorizer;
    protected ConfigManager configManager;
    protected MessagesManager messagesManager;
    protected SphereManager sphereManager;
    protected DataManager dataManager;
    protected EnchantmentManager enchantmentManager;
    protected AbilityManager abilityManager;
    protected DebugLogger debugLogger;
    protected RunesCommand runesCommand;
    protected SphereCommand sphereCommand;

    protected void classRegister() {
        getLogger().info("Регистрация классов...");
        configManager = new ConfigManager(this);
        messagesManager = new MessagesManager(this);

        debugLogger = new DebugLogger(this);
        debugLogger.reload();

        EnchantmentType.loadFromConfig(configManager);

        sphereManager = new SphereManager(this);
        dataManager = new DataManager(this);
        enchantmentManager = new EnchantmentManager(this);
        abilityManager = new AbilityManager(this);
        sphereCommand = new SphereCommand(this);
        runesCommand = new RunesCommand(this);
    }

    protected void registerCommands() {
        getLogger().info("Регистрация команд...");
        if (getCommand("sphere") != null) {
            getCommand("sphere").setExecutor(sphereCommand);
            getCommand("sphere").setTabCompleter(sphereCommand);
            getLogger().info("Команда sphere зарегистрирована");
        }
        if (getCommand("runes") != null) {
            getCommand("runes").setExecutor(runesCommand);
            getCommand("runes").setTabCompleter(runesCommand);
            getLogger().info("Команда runes зарегистрирована");
        }
    }

    protected void registerListeners() {
        getLogger().info("Регистрация ивентов...");
        getServer().getPluginManager().registerEvents(new SphereUseListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new DamageListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerItemHeldListener(this), this);
    }

    protected void startEffectCheckTasks() {
        int globalCheckInterval = configManager.getGlobalCheckInterval();

        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : getServer().getOnlinePlayers()) {
                PlayerItemHeldListener.checkAndApplyEffects(this, player);
            }
            debugLogger.logFull("Фоновая синхронизация эффектов: " + getServer().getOnlinePlayers().size() + " игроков");
        }, globalCheckInterval, globalCheckInterval);

        debugLogger.log("Фоновая проверка эффектов: каждые " + globalCheckInterval + " тиков");
    }

    public Colorizer getColorizer() {
        return colorizer;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public SphereManager getSphereManager() {
        return sphereManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public EnchantmentManager getEnchantmentManager() {
        return enchantmentManager;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    public DebugLogger getDebugLogger() {
        return debugLogger;
    }
}

