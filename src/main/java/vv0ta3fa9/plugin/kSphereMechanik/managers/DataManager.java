package vv0ta3fa9.plugin.kSphereMechanik.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;
import vv0ta3fa9.plugin.kSphereMechanik.models.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер для сохранения и загрузки данных сфер
 * Все операции с файлами выполняются асинхронно для оптимизации производительности
 */
public class DataManager {
    private final KSphereMechanik plugin;
    private final File dataFolder;
    private final Gson gson;
    private final Map<UUID, Sphere> loadedSpheres; // Кеш загруженных сфер
    private int autoSaveTaskId = -1;

    public DataManager(KSphereMechanik plugin) {
        this.plugin = plugin;
        
        // Убеждаемся, что папка плагина создана
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.getLogger().info("Создана папка плагина: " + plugin.getDataFolder().getAbsolutePath());
        }
        
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            boolean created = dataFolder.mkdirs();
            if (created) {
                plugin.getLogger().info("Создана папка data: " + dataFolder.getAbsolutePath());
            }
        }
        
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.loadedSpheres = new ConcurrentHashMap<>();
        startAutoSave();
    }

    /**
     * Запускает автоматическое сохранение
     */
    private void startAutoSave() {
        int interval = plugin.getConfigManager().getAutoSaveInterval() * 20; // секунды -> тики
        
        autoSaveTaskId = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            saveAllSpheres();
        }, interval, interval).getTaskId();
        
        plugin.getDebugLogger().logDetailed("Автосохранение запущено с интервалом " + interval + " тиков");
    }

    /**
     * Останавливает автоматическое сохранение
     */
    public void stopAutoSave() {
        if (autoSaveTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(autoSaveTaskId);
            autoSaveTaskId = -1;
        }
    }

    /**
     * Сохраняет сферу (асинхронно)
     */
    public void saveSphere(Sphere sphere) {
        if (sphere == null) {
            plugin.getDebugLogger().logFull("Попытка сохранить null сферу");
            return;
        }

        plugin.getDebugLogger().logFull("Сохранение сферы: " + sphere.getId());

        // Обновляем кеш синхронно
        loadedSpheres.put(sphere.getId(), sphere);

        // Сохранение в файл - асинхронно
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String format = plugin.getConfigManager().getDataFormat().toLowerCase();

            plugin.getDebugLogger().logFull("Сохранение сферы " + sphere.getId() + " в формате " + format);

            if (format.equals("json")) {
                saveSphereJson(sphere);
            } else {
                saveSphereYaml(sphere);
            }

            plugin.getDebugLogger().logFull("Сфера сохранена: " + sphere.getId());
        });
    }

    /**
     * Сохраняет сферу в JSON формате
     */
    private void saveSphereJson(Sphere sphere) {
        File file = new File(dataFolder, sphere.getId().toString() + ".json");
        
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            JsonObject json = new JsonObject();
            json.addProperty("id", sphere.getId().toString());
            json.addProperty("type", sphere.getType().name());
            json.addProperty("rank", sphere.getRank().name());
            json.addProperty("reborn", sphere.isReborn());
            json.addProperty("ability", sphere.getAbility() != null ? sphere.getAbility().name() : null);
            json.addProperty("lastAbilityUse", sphere.getLastAbilityUse());
            
            // Зачарования
            JsonObject enchantmentsJson = new JsonObject();
            for (Enchantment enchantment : sphere.getEnchantments()) {
                JsonObject enchantJson = new JsonObject();
                enchantJson.addProperty("type", enchantment.getType().name());
                enchantJson.addProperty("level", enchantment.getLevel());
                enchantJson.addProperty("cost", enchantment.getCapacityCost());
                enchantmentsJson.add(enchantment.getType().name(), enchantJson);
            }
            json.add("enchantments", enchantmentsJson);
            
            gson.toJson(json, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить сферу " + sphere.getId() + ": " + e.getMessage());
            plugin.getDebugLogger().logException("Ошибка сохранения сферы", e);
        }
    }

    /**
     * Сохраняет сферу в YAML формате
     */
    private void saveSphereYaml(Sphere sphere) {
        File file = new File(dataFolder, sphere.getId().toString() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        config.set("id", sphere.getId().toString());
        config.set("type", sphere.getType().name());
        config.set("rank", sphere.getRank().name());
        config.set("reborn", sphere.isReborn());
        config.set("ability", sphere.getAbility() != null ? sphere.getAbility().name() : null);
        config.set("lastAbilityUse", sphere.getLastAbilityUse());
        
        // Зачарования
        List<Map<String, Object>> enchantmentsList = new ArrayList<>();
        for (Enchantment enchantment : sphere.getEnchantments()) {
            Map<String, Object> enchantMap = new HashMap<>();
            enchantMap.put("type", enchantment.getType().name());
            enchantMap.put("level", enchantment.getLevel());
            enchantMap.put("cost", enchantment.getCapacityCost());
            enchantmentsList.add(enchantMap);
        }
        config.set("enchantments", enchantmentsList);
        
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить сферу " + sphere.getId() + ": " + e.getMessage());
            plugin.getDebugLogger().logException("Ошибка сохранения сферы", e);
        }
    }

    /**
     * Загружает сферу по ID (синхронно, если в кеше, иначе асинхронно)
     */
    public Sphere loadSphere(UUID sphereId) {
        if (loadedSpheres.containsKey(sphereId)) {
            plugin.getDebugLogger().logFull("Сфера найдена в кеше: " + sphereId);
            return loadedSpheres.get(sphereId);
        }

        plugin.getDebugLogger().logFull("Загрузка сферы из файла: " + sphereId);

        String format = plugin.getConfigManager().getDataFormat().toLowerCase();

        Sphere sphere;
        if (format.equals("json")) {
            sphere = loadSphereJson(sphereId);
        } else {
            sphere = loadSphereYaml(sphereId);
        }

        if (sphere != null) {
            loadedSpheres.put(sphereId, sphere);
            plugin.getDebugLogger().logFull("Сфера загружена и помещена в кеш: " + sphereId);
        } else {
            plugin.getDebugLogger().logFull("Не удалось загрузить сферу: " + sphereId);
        }

        return sphere;
    }

    /**
     * Загружает сферу асинхронно с колбэком
     */
    public void loadSphereAsync(UUID sphereId, java.util.function.Consumer<Sphere> callback) {
        if (loadedSpheres.containsKey(sphereId)) {
            callback.accept(loadedSpheres.get(sphereId));
            return;
        }
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String format = plugin.getConfigManager().getDataFormat().toLowerCase();
            Sphere sphere;
            
            if (format.equals("json")) {
                sphere = loadSphereJson(sphereId);
            } else {
                sphere = loadSphereYaml(sphereId);
            }

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                callback.accept(sphere);
            });
        });
    }

    /**
     * Загружает сферу из JSON
     */
    private Sphere loadSphereJson(UUID sphereId) {
        File file = new File(dataFolder, sphereId.toString() + ".json");
        if (!file.exists()) {
            return null;
        }
        
        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            
            SphereType type = SphereType.valueOf(json.get("type").getAsString());
            SphereRank rank = SphereRank.valueOf(json.get("rank").getAsString());
            Sphere sphere = new Sphere(sphereId, type, rank);
            
            sphere.setReborn(json.get("reborn").getAsBoolean());
            
            if (json.has("ability") && !json.get("ability").isJsonNull()) {
                sphere.setAbility(AbilityType.valueOf(json.get("ability").getAsString()));
            }
            
            if (json.has("lastAbilityUse")) {
                sphere.setLastAbilityUse(json.get("lastAbilityUse").getAsLong());
            }

            if (json.has("enchantments")) {
                JsonObject enchantmentsJson = json.getAsJsonObject("enchantments");
                for (String key : enchantmentsJson.keySet()) {
                    JsonObject enchantJson = enchantmentsJson.getAsJsonObject(key);
                    EnchantmentType enchantType = EnchantmentType.valueOf(enchantJson.get("type").getAsString());
                    int level = enchantJson.get("level").getAsInt();
                    int cost = enchantJson.get("cost").getAsInt();
                    Enchantment enchantment = new Enchantment(enchantType, level, cost);
                    sphere.addEnchantment(enchantment, Integer.MAX_VALUE);
                }
            }
            
            loadedSpheres.put(sphereId, sphere);
            return sphere;
        } catch (Exception e) {
            plugin.getLogger().severe("Не удалось загрузить сферу " + sphereId + ": " + e.getMessage());
            plugin.getDebugLogger().logException("Ошибка загрузки сферы", e);
            return null;
        }
    }

    /**
     * Загружает сферу из YAML
     */
    private Sphere loadSphereYaml(UUID sphereId) {
        File file = new File(dataFolder, sphereId.toString() + ".yml");
        if (!file.exists()) {
            return null;
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        SphereType type = SphereType.valueOf(config.getString("type"));
        SphereRank rank = SphereRank.valueOf(config.getString("rank"));
        Sphere sphere = new Sphere(sphereId, type, rank);
        
        sphere.setReborn(config.getBoolean("reborn", false));
        
        if (config.contains("ability")) {
            sphere.setAbility(AbilityType.valueOf(config.getString("ability")));
        }
        
        if (config.contains("lastAbilityUse")) {
            sphere.setLastAbilityUse(config.getLong("lastAbilityUse"));
        }
        
        // Зачарования
        if (config.contains("enchantments")) {
            List<Map<?, ?>> enchantmentsList = config.getMapList("enchantments");
            for (Map<?, ?> enchantMap : enchantmentsList) {
                EnchantmentType enchantType = EnchantmentType.valueOf((String) enchantMap.get("type"));
                int level = (Integer) enchantMap.get("level");
                int cost = (Integer) enchantMap.get("cost");
                Enchantment enchantment = new Enchantment(enchantType, level, cost);
                sphere.addEnchantment(enchantment, Integer.MAX_VALUE);
            }
        }
        
        loadedSpheres.put(sphereId, sphere);
        return sphere;
    }

    /**
     * Сохраняет все сферы из реестра
     */
    public void saveAllSpheres() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Collection<Sphere> spheres = plugin.getSphereManager().getAllSpheres();
            int saved = 0;
            
            for (Sphere sphere : spheres) {
                String format = plugin.getConfigManager().getDataFormat().toLowerCase();
                
                if (format.equals("json")) {
                    saveSphereJson(sphere);
                } else {
                    saveSphereYaml(sphere);
                }
                saved++;
            }
            
            plugin.getDebugLogger().logFull("Сохранено сфер: " + saved);
        });
    }

    /**
     * Сохраняет все сферы
     */
    public void saveAllSpheresSync() {
        Collection<Sphere> spheres = plugin.getSphereManager().getAllSpheres();
        int saved = 0;
        
        for (Sphere sphere : spheres) {
            String format = plugin.getConfigManager().getDataFormat().toLowerCase();
            
            if (format.equals("json")) {
                saveSphereJson(sphere);
            } else {
                saveSphereYaml(sphere);
            }
            saved++;
        }
        
        plugin.getDebugLogger().logFull("Сохранено сфер при выключении: " + saved);
    }

    /**
     * Удаляет файл сферы
     */
    public void deleteSphere(UUID sphereId) {
        loadedSpheres.remove(sphereId);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String format = plugin.getConfigManager().getDataFormat().toLowerCase();
            String extension = format.equals("json") ? ".json" : ".yml";
            File file = new File(dataFolder, sphereId.toString() + extension);
            
            if (file.exists()) {
                file.delete();
            }
        });
    }

    /**
     * Очищает кеш
     */
    public void clearCache() {
        loadedSpheres.clear();
    }
    
    /**
     * Загружает все сферы из папки data при старте плагина
     * Выполняется синхронно чтобы гарантировать загрузку до использования
     */
    public int loadAllSpheres() {
        if (!dataFolder.exists()) {
            return 0;
        }
        
        File[] files = dataFolder.listFiles();
        if (files == null || files.length == 0) {
            return 0;
        }
        
        int loaded = 0;
        String format = plugin.getConfigManager().getDataFormat().toLowerCase();
        String extension = format.equals("json") ? ".json" : ".yml";
        
        for (File file : files) {
            if (!file.getName().endsWith(extension)) {
                continue;
            }
            
            try {
                String fileName = file.getName();
                String uuidStr = fileName.substring(0, fileName.length() - extension.length());
                UUID sphereId = UUID.fromString(uuidStr);
                
                Sphere sphere;
                if (format.equals("json")) {
                    sphere = loadSphereJson(sphereId);
                } else {
                    sphere = loadSphereYaml(sphereId);
                }
                
                if (sphere != null) {
                    plugin.getSphereManager().registerSphere(sphere);
                    loaded++;
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Пропущен файл с неверным именем: " + file.getName());
            }
        }
        
        plugin.getLogger().info("Загружено сфер из файлов: " + loaded);
        return loaded;
    }
}

