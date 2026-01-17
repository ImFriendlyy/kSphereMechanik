package vv0ta3fa9.plugin.kSphereMechanik.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vv0ta3fa9.plugin.kSphereMechanik.KSphereMechanik;
import vv0ta3fa9.plugin.kSphereMechanik.api.SphereEvents;
import vv0ta3fa9.plugin.kSphereMechanik.models.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Главная команда /sphere
 */
public class SphereCommand implements CommandExecutor, TabCompleter {
    private final KSphereMechanik plugin;

    public SphereCommand(KSphereMechanik plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        if (subcommand.equals("give") && args.length == 2) {
            if (!sender.hasPermission("kspheres.give")) {
                sendMessage(sender, "no_permission");
                return true;
            }
            handleGiveSimple(sender, args);
            return true;
        }

        if (subcommand.equals("give") && args.length >= 5) {
            if (!sender.hasPermission("kspheres.give")) {
                sendMessage(sender, "no_permission");
                return true;
            }
            handleGiveWithEnchantment(sender, args);
            return true;
        }

        switch (subcommand) {
            case "help":
                sendHelp(sender);
                break;
                
            case "give":
                if (!sender.hasPermission("kspheres.give")) {
                    sendMessage(sender, "no_permission");
                    return true;
                }
                handleGive(sender, args);
                break;
                
            case "clear":
                if (!(sender instanceof Player)) {
                    sendMessageDirect(sender, "&cЭта команда доступна только игрокам!");
                    return true;
                }
                handleClear((Player) sender);
                break;
                
            case "master":
                if (!(sender instanceof Player)) {
                    sendMessageDirect(sender, "&cЭта команда доступна только игрокам!");
                    return true;
                }
                if (args.length < 2) {
                    sendMessageDirect(sender, "&cИспользование: /sphere master clear");
                    return true;
                }
                if (args[1].equalsIgnoreCase("clear")) {
                    handleMasterClear((Player) sender);
                } else {
                    sendMessageDirect(sender, "&cИспользование: /sphere master clear");
                }
                break;
                
            case "reload":
                if (!sender.hasPermission("kspheres.reload")) {
                    sendMessage(sender, "no_permission");
                    return true;
                }
                handleReload(sender);
                break;
                
                   case "debug":
                       if (!sender.hasPermission("kspheres.debug")) {
                           sendMessage(sender, "no_permission");
                           return true;
                       }
                       handleDebug(sender, args);
                       break;

                   case "test":
                       if (!(sender instanceof Player)) {
                           sendMessageDirect(sender, "&cЭта команда доступна только игрокам!");
                           return true;
                       }
                       if (!sender.hasPermission("kspheres.admin")) {
                           sendMessage(sender, "no_permission");
                           return true;
                       }
                       handleTest((Player) sender);
                       break;
                
            case "enchant":
            case "add":
                if (!(sender instanceof Player)) {
                    sendMessageDirect(sender, "&cЭта команда доступна только игрокам!");
                    return true;
                }
                if (!sender.hasPermission("kspheres.enchant")) {
                    sendMessage(sender, "no_permission");
                    return true;
                }
                handleEnchant((Player) sender, args);
                break;
                
            default:
                sendHelp(sender);
        }

        return true;
    }

    private void handleEnchant(Player player, String[] args) {
        if (args.length < 3) {
            sendMessageDirect(player, "&cИспользование: /sphere enchant <тип> <уровень>");
            sendMessageDirect(player, "&7Типы зачарований:");
            for (EnchantmentType type : EnchantmentType.values()) {
                sendMessageDirect(player, "&7- &f" + type.name() + " &7(" + type.getDisplayName() + ", макс. ур. " + type.getMaxLevel() + ")");
            }
            return;
        }

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand == null || !plugin.getSphereManager().getItemBuilder().isSphere(offHand)) {
            sendMessageDirect(player, "&cДержите сферу в левой руке (off-hand)!");
            return;
        }

        Sphere sphere = plugin.getSphereManager().getSphereFromItem(offHand);
        if (sphere == null) {
            sendMessageDirect(player, "&cНе удалось найти сферу!");
            return;
        }

        EnchantmentType enchantType;
        try {
            enchantType = EnchantmentType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            sendMessageDirect(player, "&cНеверный тип зачарования: " + args[1]);
            sendMessageDirect(player, "&7Доступные типы: " + Arrays.toString(EnchantmentType.values()));
            return;
        }

        int level;
        try {
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sendMessageDirect(player, "&cНеверный уровень: " + args[2]);
            return;
        }

        if (level < 1 || level > enchantType.getMaxLevel()) {
            sendMessageDirect(player, "&cУровень должен быть от 1 до " + enchantType.getMaxLevel() + "!");
            return;
        }

        boolean added = plugin.getSphereManager().addEnchantment(sphere, enchantType, level);

        if (added) {
            // Вызываем событие зачарования
            SphereEvents.callSphereEnchantEvent(player, sphere, enchantType, level);

            ItemStack updatedItem = plugin.getSphereManager().updateSphereItem(offHand, sphere);
            player.getInventory().setItemInOffHand(updatedItem);
            
            int usedCapacity = sphere.calculateUsedCapacity();
            int maxCapacity = plugin.getConfigManager().getRankCapacity(sphere.getRank());
            
            sendMessageDirect(player, "&aЗачарование &e" + enchantType.getDisplayName() + " Ур." + level + " &aдобавлено!");
            sendMessageDirect(player, "&7Емкость: &e" + usedCapacity + "/" + maxCapacity);
        } else {
            int usedCapacity = sphere.calculateUsedCapacity();
            int maxCapacity = plugin.getConfigManager().getRankCapacity(sphere.getRank());
            int cost = plugin.getSphereManager().getCapacityCalculator().calculateCost(enchantType, level);
            
            sendMessageDirect(player, "&cНедостаточно места для зачарования!");
            sendMessageDirect(player, "&7Текущая емкость: &e" + usedCapacity + "/" + maxCapacity);
            sendMessageDirect(player, "&7Требуется: &c" + cost + " &7единиц");
            
            if (sphere.getType() == SphereType.DONATE) {
                sendMessageDirect(player, "&7Примечание: Донатные сферы не должны иметь ограничений по емкости!");
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        sendMessageDirect(sender, "&6=== kSpheres - Помощь ===");
        sendMessageDirect(sender, "&e/sphere help &7- Показать эту помощь");
        if (sender.hasPermission("kspheres.give")) {
            sendMessageDirect(sender, "&e/sphere give <игрок> &7- Выдать случайную сферу");
            sendMessageDirect(sender, "&e/sphere give <игрок> <тип> <ранг> &7- Выдать сферу");
            sendMessageDirect(sender, "&e/sphere give <игрок> NORMAL <ранг> <зачарование> <уровень> &7- Выдать обычную сферу с зачарованием");
            sendMessageDirect(sender, "&e/sphere give <игрок> ACTIVE <ранг> <зачарование> <уровень> <способность> &7- Выдать активную сферу");
        }
        if (sender instanceof Player && sender.hasPermission("kspheres.enchant")) {
            sendMessageDirect(sender, "&e/sphere enchant <тип> <уровень> &7- Добавить зачарование на сферу в off-hand");
        }
        if (sender instanceof Player) {
            sendMessageDirect(sender, "&e/sphere clear &7- Очистить сферу в off-hand");
            sendMessageDirect(sender, "&e/sphere master clear &7- Очистить сферу (мастер)");
        }
        if (sender.hasPermission("kspheres.reload")) {
            sendMessageDirect(sender, "&e/sphere reload &7- Перезагрузить конфиг");
        }
        if (sender.hasPermission("kspheres.debug")) {
            sendMessageDirect(sender, "&e/sphere debug <on|off|level> &7- Управление отладкой");
        }
    }

    private void handleGiveSimple(CommandSender sender, String[] args) {
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sendMessageWithReplacement(sender, "player_not_found", args[1]);
            return;
        }
        
        // Выдаем случайную сферу (можно настроить в конфиге)
        SphereType type = SphereType.NORMAL;
        SphereRank rank = SphereRank.RARE;
        
        plugin.getSphereManager().giveSphereToPlayer(target, type, rank);
        sendMessageWithReplacement(sender, "sphere_given", target.getName());
    }

    private void handleGiveWithEnchantment(CommandSender sender, String[] args) {
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sendMessageWithReplacement(sender, "player_not_found", args[1]);
            return;
        }

        SphereType type;
        try {
            type = SphereType.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sendMessageDirect(sender, "&cНеверный тип сферы: " + args[2]);
            sendMessageDirect(sender, "&7Доступные типы: NORMAL, ACTIVE, DONATE");
            return;
        }

        SphereRank rank;
        try {
            rank = SphereRank.valueOf(args[3].toUpperCase());
        } catch (IllegalArgumentException e) {
            sendMessageDirect(sender, "&cНеверный ранг: " + args[3]);
            sendMessageDirect(sender, "&7Доступные ранги: RARE, MYTHIC, LEGENDARY");
            return;
        }

        if (type == SphereType.ACTIVE) {
            if (args.length < 7) {
                sendMessageDirect(sender, "&cДля ACTIVE сферы нужно указать способность!");
                sendMessageDirect(sender, "&cИспользование: /sphere give <игрок> ACTIVE <ранг> <зачарование> <уровень> <способность>");
                sendMessageDirect(sender, "&7Пример: /sphere give Player ACTIVE LEGENDARY DAMAGE_BOOST 3 VAMPIRE");
                sendMessageDirect(sender, "&7Способности: " + Arrays.toString(AbilityType.values()));
                return;
            }

            AbilityType ability;
            try {
                ability = AbilityType.valueOf(args[6].toUpperCase());
            } catch (IllegalArgumentException e) {
                sendMessageDirect(sender, "&cНеверная способность: " + args[6]);
                sendMessageDirect(sender, "&7Доступные способности: " + Arrays.toString(AbilityType.values()));
                return;
            }

            handleGiveActiveWithEnchantment(sender, args, target, rank, ability);
        } else {
            if (args.length < 6) {
                sendMessageDirect(sender, "&cИспользование: /sphere give <игрок> <тип> <ранг> <зачарование> <уровень>");
                sendMessageDirect(sender, "&7Пример: /sphere give Player NORMAL LEGENDARY DAMAGE_BOOST 3");
                return;
            }

            handleGiveNormalWithEnchantment(sender, args, target, type, rank);
        }
    }

    private void handleGiveNormalWithEnchantment(CommandSender sender, String[] args, Player target, SphereType type, SphereRank rank) {
        EnchantmentType enchantType;
        try {
            enchantType = EnchantmentType.valueOf(args[4].toUpperCase());
        } catch (IllegalArgumentException e) {
            sendMessageDirect(sender, "&cНеверный тип зачарования: " + args[4]);
            sendMessageDirect(sender, "&7Доступные типы: " + Arrays.toString(EnchantmentType.values()));
            return;
        }

        int level;
        try {
            level = Integer.parseInt(args[5]);
        } catch (NumberFormatException e) {
            sendMessageDirect(sender, "&cНеверный уровень: " + args[5]);
            return;
        }

        if (level < 1 || level > enchantType.getMaxLevel()) {
            sendMessageDirect(sender, "&cУровень должен быть от 1 до " + enchantType.getMaxLevel() + "!");
            return;
        }

        Sphere sphere = plugin.getSphereManager().createSphere(type, rank);

        boolean added = plugin.getSphereManager().addEnchantment(sphere, enchantType, level);
        if (!added) {
            sendMessageDirect(sender, "&cНе удалось добавить зачарование! Возможно, недостаточно места по емкости.");
            sendMessageDirect(sender, "&7Тип: " + type + ", Ранг: " + rank + " (емкость: " +
                plugin.getConfigManager().getRankCapacity(rank) + ")");
            return;
        }

        plugin.getSphereManager().giveSphereToPlayer(target, sphere);

        sendMessageDirect(sender, "&aСфера выдана игроку &e" + target.getName() + "&a!");
        sendMessageDirect(sender, "&7Тип: &e" + type + "&7, Ранг: &e" + rank);
        sendMessageDirect(sender, "&7Зачарование: &e" + enchantType.getDisplayName() + " Ур." + level);

        if (target.isOnline()) {
            target.sendMessage(plugin.getColorizer().colorize("&aВы получили сферу с зачарованием &e" +
                enchantType.getDisplayName() + " Ур." + level + "&a!"));
        }
    }

    private void handleGiveActiveWithEnchantment(CommandSender sender, String[] args, Player target, SphereRank rank, AbilityType ability) {
        EnchantmentType enchantType;
        try {
            enchantType = EnchantmentType.valueOf(args[4].toUpperCase());
        } catch (IllegalArgumentException e) {
            sendMessageDirect(sender, "&cНеверный тип зачарования: " + args[4]);
            sendMessageDirect(sender, "&7Доступные типы: " + Arrays.toString(EnchantmentType.values()));
            return;
        }

        int level;
        try {
            level = Integer.parseInt(args[5]);
        } catch (NumberFormatException e) {
            sendMessageDirect(sender, "&cНеверный уровень: " + args[5]);
            return;
        }

        if (level < 1 || level > enchantType.getMaxLevel()) {
            sendMessageDirect(sender, "&cУровень должен быть от 1 до " + enchantType.getMaxLevel() + "!");
            return;
        }

        Sphere sphere = plugin.getSphereManager().createSphere(SphereType.ACTIVE, rank);
        sphere.setAbility(ability);

        boolean added = plugin.getSphereManager().addEnchantment(sphere, enchantType, level);
        if (!added) {
            sendMessageDirect(sender, "&cНе удалось добавить зачарование! Возможно, недостаточно места по емкости.");
            sendMessageDirect(sender, "&7Тип: ACTIVE, Ранг: " + rank + " (емкость: " +
                plugin.getConfigManager().getRankCapacity(rank) + ")");
            return;
        }

        plugin.getSphereManager().giveSphereToPlayer(target, sphere);

        sendMessageDirect(sender, "&aСфера выдана игроку &e" + target.getName() + "&a!");
        sendMessageDirect(sender, "&7Тип: &eACTIVE&7, Ранг: &e" + rank);
        sendMessageDirect(sender, "&7Зачарование: &e" + enchantType.getDisplayName() + " Ур." + level);
        sendMessageDirect(sender, "&7Способность: &e" + ability.getDisplayName());

        if (target.isOnline()) {
            target.sendMessage(plugin.getColorizer().colorize("&aВы получили активную сферу с зачарованием &e" +
                enchantType.getDisplayName() + " Ур." + level + "&a и способностью &e" + ability.getDisplayName() + "&a!"));
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sendMessageDirect(sender, "&cИспользование: /sphere give <игрок> <тип> <ранг>");
            sendMessageDirect(sender, "&7Типы: NORMAL, ACTIVE, DONATE");
            sendMessageDirect(sender, "&7Ранги: RARE, MYTHIC, LEGENDARY");
            return;
        }

        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sendMessageWithReplacement(sender, "player_not_found", args[1]);
            return;
        }

        SphereType type;
        try {
            type = SphereType.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sendMessage(sender, "invalid_sphere_type");
            return;
        }

        SphereRank rank;
        try {
            rank = SphereRank.valueOf(args[3].toUpperCase());
        } catch (IllegalArgumentException e) {
            sendMessage(sender, "invalid_sphere_rank");
            return;
        }

        plugin.getSphereManager().giveSphereToPlayer(target, type, rank);
        sendMessageWithReplacement(sender, "sphere_given", target.getName());
    }

    private void handleClear(Player player) {
        ItemStack item = player.getInventory().getItemInOffHand();
        if (item == null || !plugin.getSphereManager().getItemBuilder().isSphere(item)) {
            sendMessageDirect(player, "&cВы должны держать сферу в левой руке (off-hand)!");
            return;
        }

        Sphere sphere = plugin.getSphereManager().getSphereFromItem(item);
        if (sphere == null) {
            sendMessageDirect(player, "&cНе удалось найти сферу!");
            return;
        }

        plugin.getSphereManager().clearSphere(sphere);
        ItemStack updated = plugin.getSphereManager().updateSphereItem(item, sphere);
        player.getInventory().setItemInOffHand(updated);
        sendMessage(player, "sphere_cleared");
    }

    private void handleMasterClear(Player player) {
        ItemStack item = player.getInventory().getItemInOffHand();
        if (item == null || !plugin.getSphereManager().getItemBuilder().isSphere(item)) {
            sendMessageDirect(player, "&cВы должны держать сферу в левой руке (off-hand)!");
            return;
        }

        Sphere sphere = plugin.getSphereManager().getSphereFromItem(item);
        if (sphere == null) {
            sendMessageDirect(player, "&cНе удалось найти сферу!");
            return;
        }

        // Донатные сферы не очищаются
        if (sphere.getType() == vv0ta3fa9.plugin.kSphereMechanik.models.SphereType.DONATE) {
            sendMessageDirect(player, "&cДонатные сферы нельзя очистить!");
            return;
        }

        if (sphere.getEnchantments().isEmpty()) {
            sendMessageDirect(player, "&cСфера уже пуста!");
            return;
        }

        plugin.getSphereManager().clearSphere(sphere);
        ItemStack updated = plugin.getSphereManager().updateSphereItem(item, sphere);
        player.getInventory().setItemInOffHand(updated);
        sendMessage(player, "sphere_cleared");
    }

    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().reloadConfig();
        plugin.getMessagesManager().reloadConfig();
        plugin.getDebugLogger().reload();
        sendMessage(sender, "config_reloaded");
        sendMessageDirect(sender, "&7Конфигурация перезагружена...");
    }

    private void handleDebug(CommandSender sender, String[] args) {
        if (args.length < 2) {
            boolean enabled = plugin.getDebugLogger().isEnabled();
            int level = plugin.getDebugLogger().getLevel();
            sendMessageDirect(sender, "&7Отладка: " + (enabled ? "&aВключена" : "&cВыключена") + " &7(уровень: " + level + ")");
            return;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "on":
                // Используем setValue() для обновления конфига и кэша одновременно
                plugin.getConfigManager().setValue("debug.enabled", true);
                plugin.getConfigManager().saveConfig();
                plugin.getDebugLogger().reload();
                sendMessageDirect(sender, "&aОтладка включена");
                break;
                
            case "off":
                // Используем setValue() для обновления конфига и кэша одновременно
                plugin.getConfigManager().setValue("debug.enabled", false);
                plugin.getConfigManager().saveConfig();
                plugin.getDebugLogger().reload();
                sendMessageDirect(sender, "&cОтладка выключена");
                break;
                
            case "level":
                if (args.length < 3) {
                    sendMessageDirect(sender, "&cИспользование: /sphere debug level <1|2|3>");
                    return;
                }
                try {
                    int level = Integer.parseInt(args[2]);
                    if (level < 1 || level > 3) {
                        sendMessageDirect(sender, "&cУровень должен быть от 1 до 3");
                        return;
                    }
                    // Используем setValue() для обновления конфига и кэша одновременно
                    plugin.getConfigManager().setValue("debug.level", level);
                    plugin.getConfigManager().saveConfig();
                    plugin.getDebugLogger().reload();
                    sendMessageDirect(sender, "&aУровень отладки установлен: " + level);
                } catch (NumberFormatException e) {
                    sendMessageDirect(sender, "&cНеверное число!");
                }
                break;
                
            default:
                sendMessageDirect(sender, "&cИспользование: /sphere debug <on|off|level>");
        }
    }

    private void sendMessage(CommandSender sender, String key) {
        String message = plugin.getMessagesManager().getMessage(key);
        sender.sendMessage(plugin.getColorizer().colorize(plugin.getMessagesManager().getMessage("prefix") + message));
    }

    private void sendMessageWithReplacement(CommandSender sender, String key, String replacement) {
        String message = plugin.getMessagesManager().getMessage(key).replace("{player}", replacement);
        sender.sendMessage(plugin.getColorizer().colorize(plugin.getMessagesManager().getMessage("prefix") + message));
    }

    private void sendMessageDirect(CommandSender sender, String message) {
        sender.sendMessage(plugin.getColorizer().colorize(plugin.getMessagesManager().getMessage("prefix") + message));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
               List<String> commands = new ArrayList<>(Arrays.asList("help", "give", "clear", "reload", "debug", "master", "enchant", "add", "test"));
            return commands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (args.length == 2) {
                return plugin.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 3) {
                return Arrays.asList("NORMAL", "ACTIVE", "DONATE").stream()
                        .filter(s -> s.startsWith(args[2].toUpperCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 4) {
                return Arrays.asList("RARE", "MYTHIC", "LEGENDARY").stream()
                        .filter(s -> s.startsWith(args[3].toUpperCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 5) {
                return Arrays.stream(EnchantmentType.values())
                        .map(EnchantmentType::name)
                        .filter(s -> s.startsWith(args[4].toUpperCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 6) {
                try {
                    EnchantmentType type = EnchantmentType.valueOf(args[4].toUpperCase());
                    List<String> levels = new ArrayList<>();
                    for (int i = 1; i <= type.getMaxLevel(); i++) {
                        levels.add(String.valueOf(i));
                    }
                    return levels.stream()
                            .filter(s -> s.startsWith(args[5]))
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    return completions;
                }
            } else if (args.length == 7) {
                if ("ACTIVE".equalsIgnoreCase(args[2])) {
                    return Arrays.stream(AbilityType.values())
                            .map(AbilityType::name)
                            .filter(s -> s.startsWith(args[6].toUpperCase()))
                            .collect(Collectors.toList());
                }
            }
        }

        if (args[0].equalsIgnoreCase("debug")) {
            if (args.length == 2) {
                return Arrays.asList("on", "off", "level").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 3 && args[1].equalsIgnoreCase("level")) {
                return Arrays.asList("1", "2", "3").stream()
                        .filter(s -> s.startsWith(args[2]))
                        .collect(Collectors.toList());
            }
        }

        if (args[0].equalsIgnoreCase("master")) {
            if (args.length == 2) {
                return Arrays.asList("clear").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args[0].equalsIgnoreCase("enchant") || args[0].equalsIgnoreCase("add")) {
            if (args.length == 2) {
                return Arrays.stream(EnchantmentType.values())
                        .map(EnchantmentType::name)
                        .filter(s -> s.startsWith(args[1].toUpperCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 3) {
                try {
                    EnchantmentType type = EnchantmentType.valueOf(args[1].toUpperCase());
                    List<String> levels = new ArrayList<>();
                    for (int i = 1; i <= type.getMaxLevel(); i++) {
                        levels.add(String.valueOf(i));
                    }
                    return levels.stream()
                            .filter(s -> s.startsWith(args[2]))
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    return completions;
                }
            }
        }
        
        return completions;
    }

    private void handleTest(Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand == null || offHand.getType().isAir()) {
            sendMessageDirect(player, "&cУ вас нет предмета в левой руке!");
            return;
        }

        if (!plugin.getSphereManager().getItemBuilder().isSphere(offHand)) {
            sendMessageDirect(player, "&cПредмет в левой руке не является сферой!");
            return;
        }

        Sphere sphere = plugin.getSphereManager().getSphereFromItem(offHand);
        if (sphere == null) {
            sendMessageDirect(player, "&cНе удалось найти данные сферы!");
            return;
        }

        sendMessageDirect(player, "&aПрименяю эффекты сферы &e" + sphere.getId() + "&a...");
        plugin.getEnchantmentManager().applyEnchantments(player, sphere);
        sendMessageDirect(player, "&aЭффекты применены!");
    }
}

