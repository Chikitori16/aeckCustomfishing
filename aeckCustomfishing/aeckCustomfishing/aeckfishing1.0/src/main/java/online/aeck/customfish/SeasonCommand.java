package online.aeck.customfish;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SeasonCommand implements CommandExecutor {

    private final CustomFish plugin;

    public SeasonCommand(CustomFish plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("aeck.admin")) {
            sender.sendMessage(CustomFish.PREFIX + "§cBạn không có quyền.");
            return true;
        }

        if (args.length == 0) {

            sender.sendMessage(CustomFish.PREFIX +
                    "§fMùa hiện tại: §b" + plugin.getCurrentSeason());

            sender.sendMessage("§7/muacauca <XUAN|HA|THU|DONG>");

            return true;
        }

        try {

            CustomFish.Season newSeason =
                    CustomFish.Season.valueOf(args[0].toUpperCase());

            plugin.setCurrentSeason(newSeason);

            sender.sendMessage(CustomFish.PREFIX +
                    "§aĐã chuyển sang mùa: §l" + newSeason);

            Bukkit.broadcastMessage(CustomFish.PREFIX +
                    "§fHiện tại đang là §b§lMùa " + newSeason);

        } catch (Exception e) {

            sender.sendMessage(CustomFish.PREFIX +
                    "§cMùa không hợp lệ.");
        }

        return true;
    }
}