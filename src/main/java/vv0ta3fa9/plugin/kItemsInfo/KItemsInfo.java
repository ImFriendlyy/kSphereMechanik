package vv0ta3fa9.plugin.kItemsInfo;

import org.bukkit.plugin.java.JavaPlugin;
import vv0ta3fa9.plugin.kItemsInfo.commands.CommandManager;
import vv0ta3fa9.plugin.kItemsInfo.utils.Color.Colorizer;
import vv0ta3fa9.plugin.kItemsInfo.utils.Color.impl.LegacyColorizer;

public final class KItemsInfo extends JavaPlugin {

    private Colorizer colorizer;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        try {
            colorizer = new LegacyColorizer(this);
            commandManager = new CommandManager(this);
            registerCommands();
        } catch (Exception e) {
            getLogger().severe("ОШИБКА ВКЛЮЧЕНИЯ ПЛАГИНА! Выключение плагина...");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
    }

    private void registerCommands() {
        if (getCommand("kitemsinfo") != null) {
            getCommand("kitemsinfo").setExecutor(commandManager);
        } else {
            getLogger().severe("Команда 'kitemsinfo' не найдена в plugin.yml!");
        }
    }

    public Colorizer getColorizer() {
        return colorizer;
    }
}

