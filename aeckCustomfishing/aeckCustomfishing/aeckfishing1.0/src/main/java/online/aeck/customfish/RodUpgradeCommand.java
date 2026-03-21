package online.aeck.customfish;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RodUpgradeCommand implements CommandExecutor {

    private final CustomFish plugin;

    public RodUpgradeCommand(CustomFish plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        RodUpgradeGUI gui = new RodUpgradeGUI(plugin);
        gui.open(player);
        Bukkit.getPluginManager().registerEvents(gui, plugin);
        return true;
    }
}
