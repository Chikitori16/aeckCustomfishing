package online.aeck.customfish;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class RodUpgradeGUI implements Listener {
    public static final String GUI_TITLE = "§bNâng Cấp Cần Câu";

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, GUI_TITLE);
        ItemStack rodSlot = new ItemStack(Material.FISHING_ROD);
        ItemMeta rodMeta = rodSlot.getItemMeta();
        rodMeta.setDisplayName("§aĐặt cần câu vào đây");
        rodSlot.setItemMeta(rodMeta);
        inv.setItem(4, rodSlot);

        ItemStack lineSlot = new ItemStack(Material.STRING);
        ItemMeta lineMeta = lineSlot.getItemMeta();
        lineMeta.setDisplayName("§eDây câu");
        lineSlot.setItemMeta(lineMeta);
        inv.setItem(2, lineSlot);

        ItemStack hookSlot = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta hookMeta = hookSlot.getItemMeta();
        hookMeta.setDisplayName("§bLưỡi câu");
        hookSlot.setItemMeta(hookMeta);
        inv.setItem(6, hookSlot);

        ItemStack baitSlot = new ItemStack(Material.WHEAT_SEEDS);
        ItemMeta baitMeta = baitSlot.getItemMeta();
        baitMeta.setDisplayName("§dMồi câu");
        baitSlot.setItemMeta(baitMeta);
        inv.setItem(0, baitSlot);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);
        // Xử lý logic nâng cấp khi người chơi đặt cần câu và linh kiện vào slot
        // ...
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        org.bukkit.event.HandlerList.unregisterAll(this);
    }
}
