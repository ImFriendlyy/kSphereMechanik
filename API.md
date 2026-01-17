## Пример интеграции (ExamplePlugin)

Создадим простой плагин, который использует kSphereMechanik API:

### plugin.yml
```yaml
name: ExamplePlugin
version: 1.0
main: com.example.ExamplePlugin
depend: [kSphereMechanik]

commands:
  mysphere:
    description: Выдать тестовую сферу
  checksphere:
    description: Проверить сферу в руке
```

### ExamplePlugin.java
```java
package com.example;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import vv0ta3fa9.plugin.kSphereMechanik.api.SphereAPI;
import vv0ta3fa9.plugin.kSphereMechanik.api.events.*;

public class ExamplePlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getCommand("mysphere").setExecutor(this);
        getCommand("checksphere").setExecutor(this);

        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("ExamplePlugin включен!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только для игроков!");
            return true;
        }

        Player player = (Player) sender;

        switch (command.getName().toLowerCase()) {
            case "mysphere":
                return handleMySphere(player);
            case "checksphere":
                return handleCheckSphere(player);
        }

        return false;
    }

    private boolean handleMySphere(Player player) {
        // Выдача сферы с зачарованием
        SphereAPI.giveSphereWithEnchantment(
            player,
            SphereType.ACTIVE,
            SphereRank.LEGENDARY,
            EnchantmentType.DAMAGE_BOOST,
            3
        );

        player.sendMessage("§aВы получили мощную сферу!");
        return true;
    }

    private boolean handleCheckSphere(Player player) {
        Sphere sphere = SphereAPI.getCurrentSphere(player);

        if (sphere == null) {
            player.sendMessage("§cУ вас нет сферы в левой руке!");
            return true;
        }

        player.sendMessage("§6Информация о сфере:");
        player.sendMessage("§7Тип: §e" + sphere.getType());
        player.sendMessage("§7Ранг: §e" + sphere.getRank());
        player.sendMessage("§7Емкость: §e" +
            sphere.calculateUsedCapacity() + "/" +
            SphereAPI.getPlugin().getConfigManager().getRankCapacity(sphere.getRank()));

        if (SphereAPI.hasActiveSphere(player) && sphere.getAbility() != null) {
            player.sendMessage("§7Способность: §e" + sphere.getAbility().getDisplayName());
        }

        return true;
    }

    // Слушатели событий сфер

    @EventHandler
    public void onSphereCreate(SphereCreateEvent event) {
        Player player = event.getPlayer();
        Sphere sphere = event.getSphere();

        getLogger().info(player.getName() + " создал сферу " +
                        sphere.getType() + " " + sphere.getRank());

        // Можно отменить создание
        // event.setCancelled(true);
    }

    @EventHandler
    public void onSphereEnchant(SphereEnchantEvent event) {
        Player player = event.getPlayer();
        Sphere sphere = event.getSphere();

        player.sendMessage("§aЗачарование §e" +
            event.getEnchantmentType().getDisplayName() +
            "§a добавлено!");
    }

    @EventHandler
    public void onSphereAbilityActivate(SphereAbilityActivateEvent event) {
        Player player = event.getPlayer();

        // Можно отменить активацию
        // if (playerHasCooldown) {
        //     event.setCancelled(true);
        //     player.sendMessage("§cСпособность на перезарядке!");
        // }
    }

    @EventHandler
    public void onSphereRebirth(SphereRebirthEvent event) {
        Player player = event.getPlayer();

        player.sendMessage("§dПоздравляем с перерождением!");
        // Можно дать бонусы
    }
}
```

## Важные замечания

- Все методы SphereAPI потокобезопасны и могут вызываться из любого потока
- События вызываются в главном потоке Bukkit
- Для работы с инвентарем используйте главный поток

- API инициализируется автоматически при включении kSphereMechanik
