package online.aeck.customfish;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.List;

public class TopFishGUI implements CommandExecutor {
    private final CustomFish plugin;
    public TopFishGUI(CustomFish plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!(s instanceof Player)) return true;
        Player p = (Player) s;
        Inventory inv = Bukkit.createInventory(null, 27, "§0Top 10 Thợ Câu AECK");
        p.openInventory(inv);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<MySQLManager.FishRecord> top = plugin.getMySQL().getTopFish(10);
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (int i = 0; i < top.size(); i++) {
                    MySQLManager.FishRecord r = top.get(i);
                    ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                    ItemMeta m = item.getItemMeta();
                    m.setDisplayName("§eHạng #" + (i + 1) + " §b" + r.playerName);
                    m.setLore(Arrays.asList("§7§m-----------------", "§fCá: §e" + r.fishType, "§fNặng: §c" + String.format("%.1f", r.weight) + "kg", "§7§m-----------------"));
                    item.setItemMeta(m);
                    inv.setItem(i + 9, item);
                }
            });
        });
        return true;
    }
}