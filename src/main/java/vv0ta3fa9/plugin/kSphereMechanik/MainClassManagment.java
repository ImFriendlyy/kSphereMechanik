package vv0ta3fa9.plugin.kSphereMechanik;

import vv0ta3fa9.plugin.kSphereMechanik.api.SphereAPI;
import vv0ta3fa9.plugin.kSphereMechanik.api.SphereEvents;
import vv0ta3fa9.plugin.kSphereMechanik.utils.Color.impl.LegacyColorizer;

public final class MainClassManagment extends KSphereMechanik {

    @Override
    public void onEnable() {
        try {
            colorizer = new LegacyColorizer();
            classRegister();

            SphereAPI.init(this);
            SphereEvents.init(this);

            int loadedSpheres = dataManager.loadAllSpheres();
            getLogger().info("Загружено сфер: " + loadedSpheres);

            registerCommands();
            registerListeners();
            startEffectCheckTasks();
            getLogger().info("kSphereMechanik успешно загружен!");
            debugLogger.log("Плагин включен");
        } catch (Exception e) {
            getLogger().severe("ОШИБКА ВКЛЮЧЕНИЯ ПЛАГИНА! Выключение плагина...");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.stopAutoSave();
            dataManager.saveAllSpheresSync();
        }
        getLogger().info("kSphereMechanik выключен!");
    }
}
