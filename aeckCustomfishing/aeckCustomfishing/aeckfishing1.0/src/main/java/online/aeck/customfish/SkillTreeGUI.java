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
import java.util.List;

/**
 * SkillTreeGUI — cây kỹ năng với 2 nhánh:
 *  Slot 2 — Thuần Thục (Mastery): tăng giá bán cá +5%/cấp
 *  Slot 6 — Sức Mạnh (Power): tăng EXP nhận được +10%/cấp
 *  Slot 4 — Thông tin người chơi
 */
public class SkillTreeGUI implements Listener {

    public static final String GUI_TITLE = "\u00a7aCây Kỹ Năng Câu Cá";

    private final SkillTreeManager skillManager;
    private final FishingExpManager expManager;

    public SkillTreeGUI(SkillTreeManager skillManager, FishingExpManager expManager) {
        this.skillManager = skillManager;
        this.expManager = expManager;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, GUI_TITLE);
        PlayerDataManager.PlayerData data = PlayerDataManager.getInstance().get(player);

        // Slot 4: Thông tin người chơi
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("\u00a7b\u00a7lThông Tin Câu Cá");
        infoMeta.setLore(Arrays.asList(
                "\u00a77Cấp: \u00a7e" + data.level,
                "\u00a77EXP: \u00a7e" + data.exp + "/" + data.expToLevel(),
                "\u00a77Điểm kỹ năng còn lại: \u00a76" + data.skillPoints,
                "\u00a78\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac",
                "\u00a77Trang bị hiện tại:",
                "\u00a77 Cần: \u00a7f" + data.rodId,
                "\u00a77 Dây: \u00a7f" + data.lineId,
                "\u00a77 Lưỡi: \u00a7f" + data.hookId,
                "\u00a77 Mồi: \u00a7f" + data.baitId
        ));
        info.setItemMeta(infoMeta);
        inv.setItem(4, info);

        // Slot 2: Thuần Thục (Mastery)
        int masteryLevel = data.masteryLevel;
        double masteryEffect = masteryLevel * 5;
        ItemStack mastery = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta masteryMeta = mastery.getItemMeta();
        masteryMeta.setDisplayName("\u00a7b\u00a7lThuần Thục \u00a77(Cấp: \u00a7e" + masteryLevel + "\u00a77/5)");
        String masteryNextCost = masteryLevel >= 5 ? "\u00a7cTối đa" : "\u00a77Chi phí: \u00a7e" + getMasteryCost(masteryLevel) + " điểm";
        masteryMeta.setLore(Arrays.asList(
                "\u00a77Hiệu ứng: Tăng giá bán cá",
                "\u00a7a+" + (int) masteryEffect + "% \u00a77hiện tại",
                "\u00a77Mỗi cấp: \u00a7a+5%",
                masteryNextCost,
                "",
                masteryLevel >= 5
                        ? "\u00a7cĐã đạt cấp tối đa!"
                        : "\u00a7eNhấn để nâng cấp"
        ));
        mastery.setItemMeta(masteryMeta);
        inv.setItem(2, mastery);

        // Slot 6: Sức Mạnh (Power)
        int powerLevel = data.powerLevel;
        double powerEffect = powerLevel * 10;
        ItemStack power = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta powerMeta = power.getItemMeta();
        powerMeta.setDisplayName("\u00a76\u00a7lSức Mạnh \u00a77(Cấp: \u00a7e" + powerLevel + "\u00a77/5)");
        String powerNextCost = powerLevel >= 5 ? "\u00a7cTối đa" : "\u00a77Chi phí: \u00a7e" + getPowerCost(powerLevel) + " điểm";
        powerMeta.setLore(Arrays.asList(
                "\u00a77Hiệu ứng: Tăng EXP câu cá",
                "\u00a7a+" + (int) powerEffect + "% \u00a77hiện tại",
                "\u00a77Mỗi cấp: \u00a7a+10%",
                powerNextCost,
                "",
                powerLevel >= 5
                        ? "\u00a7cĐã đạt cấp tối đa!"
                        : "\u00a7eNhấn để nâng cấp"
        ));
        power.setItemMeta(powerMeta);
        inv.setItem(6, power);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 2) {
            boolean ok = skillManager.upgradeMastery(player);
            if (ok) {
                player.sendMessage(CustomFish.PREFIX + "\u00a7aNâng cấp Thuần Thục thành công!");
            } else {
                PlayerDataManager.PlayerData d = PlayerDataManager.getInstance().get(player);
                if (d.masteryLevel >= 5) {
                    player.sendMessage(CustomFish.PREFIX + "\u00a7cThuần Thục đã đạt cấp tối đa!");
                } else {
                    player.sendMessage(CustomFish.PREFIX + "\u00a7cKhông đủ điểm kỹ năng! Cần: " + getMasteryCost(d.masteryLevel));
                }
            }
            open(player);

        } else if (slot == 6) {
            boolean ok = skillManager.upgradePower(player);
            if (ok) {
                player.sendMessage(CustomFish.PREFIX + "\u00a7aNâng cấp Sức Mạnh thành công!");
            } else {
                PlayerDataManager.PlayerData d = PlayerDataManager.getInstance().get(player);
                if (d.powerLevel >= 5) {
                    player.sendMessage(CustomFish.PREFIX + "\u00a7cSức Mạnh đã đạt cấp tối đa!");
                } else {
                    player.sendMessage(CustomFish.PREFIX + "\u00a7cKhông đủ điểm kỹ năng! Cần: " + getPowerCost(d.powerLevel));
                }
            }
            open(player);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        org.bukkit.event.HandlerList.unregisterAll(this);
    }

    private int getMasteryCost(int currentLevel) {
        int[] costs = {5, 7, 10, 12, 15};
        return (currentLevel < costs.length) ? costs[currentLevel] : 9999;
    }

    private int getPowerCost(int currentLevel) {
        int[] costs = {10, 20, 50, 50, 60};
        return (currentLevel < costs.length) ? costs[currentLevel] : 9999;
    }
}
