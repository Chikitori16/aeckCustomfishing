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

import java.util.Arrays;

public class SkillTreeGUI implements Listener {

    public static final String GUI_TITLE = "§aCây Kỹ Năng Câu Cá";

    private final SkillTreeManager skillManager;
    private final FishingExpManager expManager;

    public SkillTreeGUI(SkillTreeManager skillManager, FishingExpManager expManager) {
        this.skillManager = skillManager;
        this.expManager = expManager;
    }

    public void open(Player player) {

        Inventory inv = Bukkit.createInventory(null, 9, GUI_TITLE);

        PlayerDataManager.PlayerData data =
                PlayerDataManager.getInstance().get(player);

        ItemStack mastery = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = mastery.getItemMeta();

        meta.setDisplayName("§bThuần thục (Cấp: " + data.masteryLevel + "/5)");
        meta.setLore(Arrays.asList(
                "§7Tăng 5% kinh nghiệm mỗi cấp",
                "§7Điểm kỹ năng: " + data.skillPoints,
                "§eNhấn để nâng cấp"
        ));

        mastery.setItemMeta(meta);
        inv.setItem(3, mastery);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();

        if (event.getSlot() == 3) {

            boolean upgraded = skillManager.upgradeMastery(player);

            if (upgraded) {
                player.sendMessage("§aĐã nâng cấp Thuần thục!");
            } else {
                player.sendMessage("§cKhông đủ điểm hoặc đã tối đa!");
            }

            open(player);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        org.bukkit.event.HandlerList.unregisterAll(this);
    }
}