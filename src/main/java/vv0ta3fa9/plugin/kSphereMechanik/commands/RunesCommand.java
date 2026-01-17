package vv0ta3fa9.plugin.kSphereMechanik.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Команда /runes - настройки игрока
 */
public class RunesCommand implements CommandExecutor, TabCompleter {
    private final KSphereMechanik plugin;
    private final Map<UUID, Boolean> autoRebirthSettings; // UUID игрока -> включено ли авто-перерождение

    public RunesCommand(KSphereMechanik plugin) {
        this.plugin = plugin;
        this.autoRebirthSettings = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getColorizer().colorize("&cЭта команда доступна только игрокам!"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showSettings(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "autorebirth":
            case "ar":
            case "rebirth":
                handleAutoRebirth(player, args);
                break;
                
            case "master":
                handleMaster(player, args);
                break;
                
            default:
                showSettings(player);
        }

        return true;
    }

    private void showSettings(Player player) {
        boolean autoRebirth = autoRebirthSettings.getOrDefault(player.getUniqueId(), true);
        
        player.sendMessage(plugin.getColorizer().colorize("&6=== Настройки Сфер ==="));
        player.sendMessage(plugin.getColorizer().colorize("&7Авто-перерождение: " + (autoRebirth ? "&aВключено" : "&cВыключено")));
        player.sendMessage(plugin.getColorizer().colorize("&e/runes rebirth <on|off> &7- Изменить авто-перерождение"));
        player.sendMessage(plugin.getColorizer().colorize("&e/runes master clear &7- Очистить сферу в руке"));
    }

    private void handleAutoRebirth(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getColorizer().colorize("&cИспользование: /runes autorebirth <on|off>"));
            return;
        }

        String action = args[1].toLowerCase();
        boolean enabled;
        
        if (action.equals("on") || action.equals("вкл") || action.equals("true")) {
            enabled = true;
        } else if (action.equals("off") || action.equals("выкл") || action.equals("false")) {
            enabled = false;
        } else {
            player.sendMessage(plugin.getColorizer().colorize("&cИспользуйте: on или off"));
            return;
        }

        autoRebirthSettings.put(player.getUniqueId(), enabled);
        player.sendMessage(plugin.getColorizer().colorize("&aАвто-перерождение " + (enabled ? "включено" : "выключено")));

        plugin.getDebugLogger().logDetailed("Игрок " + player.getName() + " изменил авто-перерождение: " + enabled);
    }

    /**
     * Обработка команды master
     */
    private void handleMaster(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getColorizer().colorize("&cИспользование: /runes master clear"));
            return;
        }

        String action = args[1].toLowerCase();
        if (!action.equals("clear")) {
            player.sendMessage(plugin.getColorizer().colorize("&cИспользование: /runes master clear"));
            return;
        }

        org.bukkit.inventory.ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand == null || !plugin.getSphereManager().getItemBuilder().isSphere(offHand)) {
            player.sendMessage(plugin.getColorizer().colorize("&cВы должны держать сферу в левой руке (off-hand)!"));
            return;
        }

        vv0ta3fa9.plugin.kSphereMechanik.models.Sphere sphere = plugin.getSphereManager().getSphereFromItem(offHand);
        if (sphere == null) {
            player.sendMessage(plugin.getColorizer().colorize("&cНе удалось найти сферу!"));
            return;
        }

        if (sphere.getType() == vv0ta3fa9.plugin.kSphereMechanik.models.SphereType.DONATE) {
            player.sendMessage(plugin.getColorizer().colorize("&cДонатные сферы нельзя очистить!"));
            return;
        }

        if (sphere.getEnchantments().isEmpty()) {
            player.sendMessage(plugin.getColorizer().colorize("&cСфера уже пуста!"));
            return;
        }

        plugin.getSphereManager().clearSphere(sphere);
        org.bukkit.inventory.ItemStack updated = plugin.getSphereManager().updateSphereItem(offHand, sphere);
        player.getInventory().setItemInOffHand(updated);
        
        player.sendMessage(plugin.getColorizer().colorize(plugin.getMessagesManager().getMessage("prefix") +
            plugin.getMessagesManager().getMessage("sphere_cleared")));
        
        plugin.getDebugLogger().log("Игрок " + player.getName() + " очистил сферу " + sphere.getId());
    }

    /**
     * Проверяет, включено ли авто-перерождение для игрока
     */
    public boolean isAutoRebirthEnabled(Player player) {
        return autoRebirthSettings.getOrDefault(player.getUniqueId(), true);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        if (args.length == 1) {
            return Arrays.asList("rebirth", "master").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("rebirth")) {
            return Arrays.asList("on", "off").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("master")) {
            return Arrays.asList("clear").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}

