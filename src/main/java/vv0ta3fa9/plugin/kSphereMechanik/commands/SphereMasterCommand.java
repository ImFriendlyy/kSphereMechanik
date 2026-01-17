package vv0ta3fa9.plugin.kSphereMechanik.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;
import vv0ta3fa9.plugin.kSphereMechanik.models.Sphere;
import vv0ta3fa9.plugin.kSphereMechanik.models.SphereType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Команда /spheremaster - работа с Мастером Рун
 * Позволяет очищать и перерождать сферы (только команда, без GUI)
 */
public class SphereMasterCommand implements CommandExecutor, TabCompleter {
    private final KSphereMechanik plugin;

    public SphereMasterCommand(KSphereMechanik plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getColorizer().colorize("&cЭта команда доступна только игрокам!"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || !plugin.getSphereManager().getItemBuilder().isSphere(item)) {
            player.sendMessage(plugin.getColorizer().colorize("&cВы должны держать сферу в руке!"));
            return true;
        }

        Sphere sphere = plugin.getSphereManager().getSphereFromItem(item);
        if (sphere == null) {
            player.sendMessage(plugin.getColorizer().colorize("&cНе удалось найти сферу!"));
            return true;
        }

        switch (subcommand) {
            case "clear":
                handleClear(player, sphere, item);
                break;
                
            case "rebirth":
            case "reb":
                handleRebirth(player, sphere, item);
                break;
                
            default:
                showHelp(player);
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage(plugin.getColorizer().colorize("&6=== Мастер Рун ==="));
        player.sendMessage(plugin.getColorizer().colorize("&e/spheremaster clear &7- Очистить сферу от зачарований"));
        player.sendMessage(plugin.getColorizer().colorize("&e/spheremaster rebirth &7- Переродить сферу"));
    }

    private void handleClear(Player player, Sphere sphere, ItemStack item) {
        if (sphere.getEnchantments().isEmpty()) {
            player.sendMessage(plugin.getColorizer().colorize("&cСфера уже пуста!"));
            return;
        }

        plugin.getSphereManager().clearSphere(sphere);
        ItemStack updated = plugin.getSphereManager().updateSphereItem(item, sphere);
        player.getInventory().setItemInMainHand(updated);
        
        player.sendMessage(plugin.getColorizer().colorize(plugin.getMessagesManager().getMessage("prefix") +
            plugin.getMessagesManager().getMessage("sphere_cleared")));
        
        plugin.getDebugLogger().log("Игрок " + player.getName() + " очистил сферу " + sphere.getId());
    }

    private void handleRebirth(Player player, Sphere sphere, ItemStack item) {
        int requiredLevel = plugin.getConfigManager().getRebirthExpLevel();
        if (player.getLevel() < requiredLevel) {
            String message = plugin.getMessagesManager().getMessage("rebirth_not_enough_exp")
                .replace("{level}", String.valueOf(requiredLevel));
            player.sendMessage(plugin.getColorizer().colorize(plugin.getMessagesManager().getMessage("prefix") + message));
            return;
        }

        boolean success = plugin.getSphereManager().rebirthSphere(sphere, player);
        if (!success) {
            player.sendMessage(plugin.getColorizer().colorize("&cНе удалось переродить сферу!"));
            return;
        }

        ItemStack updated = plugin.getSphereManager().updateSphereItem(item, sphere);
        player.getInventory().setItemInMainHand(updated);
        
        String message = plugin.getMessagesManager().getMessage("rebirth_success");
        player.sendMessage(plugin.getColorizer().colorize(plugin.getMessagesManager().getMessage("prefix") + message));

        if (sphere.getType() == vv0ta3fa9.plugin.kSphereMechanik.models.SphereType.ACTIVE && sphere.getAbility() != null) {
            String abilityMessage = plugin.getMessagesManager().getMessage("rebirth_ability_changed")
                .replace("{ability}", sphere.getAbility().getDisplayName());
            player.sendMessage(plugin.getColorizer().colorize(plugin.getMessagesManager().getMessage("prefix") + abilityMessage));
        }
        
        plugin.getDebugLogger().log("Игрок " + player.getName() + " переродил сферу " + sphere.getId());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("clear", "rebirth", "reb").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}

