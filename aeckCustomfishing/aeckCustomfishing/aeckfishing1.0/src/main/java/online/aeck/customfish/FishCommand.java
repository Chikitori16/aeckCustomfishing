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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("aeck.admin")) {
            sender.sendMessage(CustomFish.PREFIX + "§cBạn không có quyền!");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig(); // Tải lại file config.yml từ đĩa
            sender.sendMessage(CustomFish.PREFIX + "§aĐã tải lại cấu hình thành công!");
            return true;
        }

        sender.sendMessage(CustomFish.PREFIX + "§7Sử dụng: §f/fish reload");
        return true;
    }
}
