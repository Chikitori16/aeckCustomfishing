package online.aeck.customfish;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FishingExpCommand implements CommandExecutor {
    private final FishingExpManager expManager;
    public FishingExpCommand(FishingExpManager expManager) { this.expManager = expManager; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        PlayerDataManager.PlayerData data = PlayerDataManager.getInstance().get(player);
        player.sendMessage("§bKinh nghiệm câu cá: §e" + data.exp + "/" + data.expToLevel());
        player.sendMessage("§aCấp câu cá: §e" + data.level);
        player.sendMessage("§dĐiểm kỹ năng: §e" + data.skillPoints);
        return true;
    }
}
