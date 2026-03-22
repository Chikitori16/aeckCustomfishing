package online.aeck.customfish;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Mở SkillTreeGUI singleton — không tạo instance mới mỗi lần.
 * Listener đã được đăng ký một lần trong CustomFish.onEnable().
 */
public class SkillTreeCommand implements CommandExecutor {

    private final SkillTreeGUI gui;

    public SkillTreeCommand(SkillTreeGUI gui) {
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        gui.open(player);
        return true;
    }
}
