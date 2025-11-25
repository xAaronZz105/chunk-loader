package de.xaaronzz105.chunkLoader;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class chunkLoadersCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        boolean hasPermission = false;
        boolean hasStarPermission = false;
        for (String permission : List.of("chunkloaders.command.*", "chunkloaders.command.giveloader", "chunkloaders.command.list")) {
            if (commandSender.hasPermission(permission)) {
                hasPermission = true;
                if (permission.equals("chunkloaders.command.*")) hasStarPermission = true;
                break;
            }
        }
        if (!hasPermission) return true;
        if (strings.length < 1) {
            commandSender.sendMessage("Plase use /chunkloaders [list|give]");
            return true;
        }

        switch (strings[0]) {
            case "list" -> {
                if (!commandSender.hasPermission("chunkloaders.command.list") && !hasStarPermission) return true;

                List<Map<?, ?>> loaderList = Main.getInstance().getConfig().getMapList("loaders");
                if (loaderList.isEmpty()) {
                    commandSender.sendMessage("There are no chunk loaders in this world!");
                    return true;
                }

                if (!(commandSender instanceof Player p)) {
                    StringBuilder baseMsg = new StringBuilder("Chunk loaders in this world:\n");

                    for (Map<?, ?> loader : loaderList) {
                        LoaderData loaderData = LoaderData.extract(loader);
                        if (loaderData == null) continue;

                        TextComponent loaderMsg = getChunkLoaderComponent(loaderData, false);

                        baseMsg.append(loaderMsg);
                        baseMsg.append("§f, \n");
                    }
                    baseMsg.setLength(baseMsg.length() - 2);
                    commandSender.sendMessage(baseMsg.toString());
                    return true;
                }

                TextComponent baseMsg = new TextComponent("Chunk loaders in this world:\n");

                for (Map<?, ?> loader : loaderList) {
                    LoaderData loaderData = LoaderData.extract(loader);
                    if (loaderData == null) continue;

                    TextComponent loaderMsg = getChunkLoaderComponent(loaderData, true);

                    baseMsg.addExtra(loaderMsg);
                    if (loaderList.getLast() != loader) {
                        baseMsg.addExtra("§f, \n");
                    }
                }

                p.spigot().sendMessage(baseMsg);
            }

            case "give" -> {
                if (!commandSender.hasPermission("chunkloaders.command.giveloader") && !hasStarPermission) return true;

                if (strings.length < 3) {
                    commandSender.sendMessage("Please use /chunkloaders give <player> <loaderType> §7<amount>");
                    return true;
                }

                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(strings[1]);
                if (!offlinePlayer.isOnline()) {
                    commandSender.sendMessage("that player isn't online!");
                    return true;
                }
                Player player = offlinePlayer.getPlayer();

                ChunkLoaders loader = ChunkLoaders.safeValueOf(strings[2]);

                if (loader == null) {
                    commandSender.sendMessage(strings[2] + " is no valid loader!");
                    return true;
                }

                int amount = 1;
                if (strings.length > 3) {
                    Integer amountToSet = parseIntOrNull(strings[3]);
                    if (amountToSet != null) amount = amountToSet;
                }

                ItemStack stack = loader.itemStack;
                stack.setAmount(amount);

                player.give(stack);
                player.playSound(player, Sound.ENTITY_ITEM_PICKUP, 0.2F, 2F);
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        boolean hasPermission = false;
        boolean hasStarPermission = false;
        for (String permission : List.of("chunkloaders.command.*", "chunkloaders.command.giveloader", "chunkloaders.command.list")) {
            if (commandSender.hasPermission(permission)) {
                hasPermission = true;
                if (permission.equals("chunkloaders.command.*")) hasStarPermission = true;
                break;
            }
        }
        if (!hasPermission) return List.of();


        switch (strings.length) {
            case 1 -> {
                if (hasStarPermission) return List.of("list", "give");
                List<String> toReturn = new ArrayList<>();
                if (commandSender.hasPermission("chunkloaders.command.list")) toReturn.add("list");
                if (commandSender.hasPermission("chunkloaders.command.give")) toReturn.add("give");

                return toReturn;
            }
            case 2 -> {
                if (!strings[0].equals("give")) return List.of();
                if (!commandSender.hasPermission("chunkloaders.command.give")) return List.of();

                List<String> toReturn = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) toReturn.add(player.getName());
                return toReturn;
            }
            case 3 -> {
                if (!strings[0].equals("give")) return List.of();
                if (!commandSender.hasPermission("chunkloaders.command.give")) return List.of();

                List<String> toReturn = new ArrayList<>();
                for (ChunkLoaders loader : ChunkLoaders.values()) toReturn.add(loader.name());
                return toReturn;
            }
            case 4 -> {
                if (!strings[0].equals("give")) return List.of();
                if (!commandSender.hasPermission("chunkloaders.command.give")) return List.of();

                return List.of("1", "64");
            }
            default -> {
                return List.of();
            }
        }
    }


    private TextComponent getChunkLoaderComponent(LoaderData loaderData, boolean withClickEvent) {
        int x = (int) loaderData.location().getX();
        int y = (int) loaderData.location().getY();
        int z = (int) loaderData.location().getZ();
        String worldName = loaderData.location().getWorld().getKey().asString();
        int area = loaderData.loaderType().area;

        TextComponent textComponent = new TextComponent("§a[" + x + ", " + y + ", " + z + " in " + worldName + " ("+ area + "by" + area +")" + "]");
        if (withClickEvent) textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/execute in " + worldName + " run tp @s " + x  + " " + y + " " + z));

        return textComponent;
    }

    private static Integer parseIntOrNull(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
