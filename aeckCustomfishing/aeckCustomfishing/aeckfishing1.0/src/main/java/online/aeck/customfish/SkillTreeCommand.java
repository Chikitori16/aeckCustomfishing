package online.aeck.customfish;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillTreeCommand implements CommandExecutor {

    private final CustomFish plugin;
    private final SkillTreeManager skillManager;
    private final FishingExpManager expManager;

    public SkillTreeCommand(CustomFish plugin,
                            SkillTreeManager skillManager,
                            FishingExpManager expManager) {

        this.plugin = plugin;
        this.skillManager = skillManager;
        this.expManager = expManager;
    }

    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args) {

        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        SkillTreeGUI gui = new SkillTreeGUI(skillManager, expManager);

        gui.open(player);

        Bukkit.getPluginManager().registerEvents(gui, plugin);

        return true;
    }
}