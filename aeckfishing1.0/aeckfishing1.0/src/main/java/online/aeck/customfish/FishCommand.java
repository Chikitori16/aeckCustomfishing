package online.aeck.customfish;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class FishCommand implements CommandExecutor {
    private final CustomFish plugin;

    public FishCommand(CustomFish plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String[] args) {

        if (!sender.hasPermission("aeck.admin")) {
            sender.sendMessage(CustomFish.PREFIX + "\u00a7cB\u1ea1n kh\u00f4ng c\u00f3 quy\u1ec1n!");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getFishConfigManager().reloadConfig();
            sender.sendMessage(CustomFish.PREFIX
                    + "\u00a7a\u0110\u00e3 t\u1ea3i l\u1ea1i c\u1ea5u h\u00ecnh th\u00e0nh c\u00f4ng!"
                    + " \u00a77(config.yml + fish.yml)");
            return true;
        }

        sender.sendMessage(CustomFish.PREFIX + "\u00a77S\u1eed d\u1ee5ng: \u00a7f/fish reload");
        return true;
    }
}
