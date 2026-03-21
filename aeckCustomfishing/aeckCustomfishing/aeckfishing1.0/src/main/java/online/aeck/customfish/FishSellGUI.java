package online.aeck.customfish;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class FishSellGUI implements CommandExecutor, Listener {
    private final CustomFish plugin;
    private final EconomyBridge econ;
    private final String GUI_TITLE = "§0Bán Cá - AECK.ONLINE";

    public FishSellGUI(CustomFish plugin, EconomyBridge econ) {
        this.plugin = plugin;
        this.econ = econ;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);

        ItemStack sellButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = sellButton.getItemMeta();
        meta.setDisplayName("§a§lXÁC NHẬN BÁN TẤT CẢ");
        sellButton.setItemMeta(meta);

        inv.setItem(13, sellButton);
        player.openInventory(inv);
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        if (event.getCurrentItem().getType() == Material.EMERALD_BLOCK) {
            sellAllFish((Player) event.getWhoClicked());
        }
    }

    private void sellAllFish(Player player) {
        double totalMoney = 0;
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || !item.hasItemMeta()) continue;

            Double price = item.getItemMeta().getPersistentDataContainer().get(
                CustomFish.FISH_PRICE_KEY, PersistentDataType.DOUBLE
            );

            if (price != null) {
                totalMoney += price * item.getAmount();
                player.getInventory().setItem(i, null);
            }
        }

        if (totalMoney > 0) {
            boolean ok = econ.deposit(player, totalMoney);
            if (ok) {
                player.sendMessage(CustomFish.PREFIX + "§aBạn đã bán mớ cá và thu về §e$" + String.format("%.0f", totalMoney));
            } else {
                player.sendMessage(CustomFish.PREFIX + "§cKhông thể cộng tiền (hệ thống Economy chưa sẵn sàng).");
            }
        } else {
            player.sendMessage(CustomFish.PREFIX + "§cBạn không có loại cá đặc biệt nào để bán!");
        }
        player.closeInventory();
    }
}