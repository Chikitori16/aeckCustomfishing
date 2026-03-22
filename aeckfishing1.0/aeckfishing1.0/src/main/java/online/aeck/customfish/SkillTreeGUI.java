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

/**
 * SkillTreeGUI — cây kỹ năng (27 ô, 3 hàng). Singleton listener.
 *
 * FIX BUG: Không gọi HandlerList.unregisterAll() khi đóng GUI.
 * Được đăng ký một lần duy nhất khi plugin khởi động.
 *
 * Layout (viền hồng, nội dung trong kính trắng hàng 2):
 *  Slot 11 (trắng): Thuần Thục (Mastery)
 *  Slot 13 (trắng): Thông tin người chơi
 *  Slot 15 (trắng): Sức Mạnh (Power)
 */
public class SkillTreeGUI implements Listener {

    public static final String GUI_TITLE = "\u00a7d\u00a7lC\u00e2y K\u1ef9 N\u0103ng C\u00e2u C\u00e1";

    private final SkillTreeManager skillManager;

    public SkillTreeGUI(SkillTreeManager skillManager) {
        this.skillManager = skillManager;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);
        PlayerDataManager.PlayerData data = PlayerDataManager.getInstance().get(player);

        // ── Slot 13 (trắng giữa): Thông tin người chơi ──────────────────────
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("\u00a7b\u00a7lTh\u00f4ng Tin C\u00e2u C\u00e1");
        infoMeta.setLore(Arrays.asList(
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                "\u00a77C\u1ea5p: \u00a7e" + data.level,
                "\u00a77EXP: \u00a7e" + data.exp + "/" + data.expToLevel(),
                "\u00a77\u0110i\u1ec3m k\u1ef9 n\u0103ng: \u00a76" + data.skillPoints,
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                "\u00a77Thu\u1ea7n Th\u1ee5c: \u00a7b" + data.masteryLevel + "/5"
                        + " \u00a77(\u00a7a+" + (data.masteryLevel * 5) + "% gi\u00e1\u00a77)",
                "\u00a77S\u1ee9c M\u1ea1nh: \u00a76" + data.powerLevel + "/5"
                        + " \u00a77(\u00a7a+" + (data.powerLevel * 10) + "% EXP\u00a77)",
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"
        ));
        info.setItemMeta(infoMeta);
        inv.setItem(13, info);

        // ── Slot 11 (trắng trái): Thuần Thục (Mastery) ──────────────────────
        int masteryLevel = data.masteryLevel;
        ItemStack mastery = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta masteryMeta = mastery.getItemMeta();
        masteryMeta.setDisplayName("\u00a7b\u00a7lThu\u1ea7n Th\u1ee5c \u00a77(C\u1ea5p: \u00a7e" + masteryLevel + "\u00a77/5)");
        String masteryNextCost = masteryLevel >= 5
                ? "\u00a7cT\u1ed1i \u0111a"
                : "\u00a77Chi ph\u00ed: \u00a7e" + getMasteryCost(masteryLevel) + " \u0111i\u1ec3m";
        masteryMeta.setLore(Arrays.asList(
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                "\u00a77Hi\u1ec7u \u1ee9ng: T\u0103ng gi\u00e1 b\u00e1n c\u00e1",
                "\u00a7a+" + (masteryLevel * 5) + "% \u00a77hi\u1ec7n t\u1ea1i",
                "\u00a77M\u1ed7i c\u1ea5p: \u00a7a+5%",
                masteryNextCost,
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                masteryLevel >= 5
                        ? "\u00a7c\u0110\u00e3 \u0111\u1ea1t c\u1ea5p t\u1ed1i \u0111a!"
                        : "\u00a7eNh\u1ea5n \u0111\u1ec3 n\u00e2ng c\u1ea5p"
        ));
        mastery.setItemMeta(masteryMeta);
        inv.setItem(11, mastery);

        // ── Slot 15 (trắng phải): Sức Mạnh (Power) ──────────────────────────
        int powerLevel = data.powerLevel;
        ItemStack power = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta powerMeta = power.getItemMeta();
        powerMeta.setDisplayName("\u00a76\u00a7lS\u1ee9c M\u1ea1nh \u00a77(C\u1ea5p: \u00a7e" + powerLevel + "\u00a77/5)");
        String powerNextCost = powerLevel >= 5
                ? "\u00a7cT\u1ed1i \u0111a"
                : "\u00a77Chi ph\u00ed: \u00a7e" + getPowerCost(powerLevel) + " \u0111i\u1ec3m";
        powerMeta.setLore(Arrays.asList(
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                "\u00a77Hi\u1ec7u \u1ee9ng: T\u0103ng EXP c\u00e2u c\u00e1",
                "\u00a7a+" + (powerLevel * 10) + "% \u00a77hi\u1ec7n t\u1ea1i",
                "\u00a77M\u1ed7i c\u1ea5p: \u00a7a+10%",
                powerNextCost,
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                powerLevel >= 5
                        ? "\u00a7c\u0110\u00e3 \u0111\u1ea1t c\u1ea5p t\u1ed1i \u0111a!"
                        : "\u00a7eNh\u1ea5n \u0111\u1ec3 n\u00e2ng c\u1ea5p"
        ));
        power.setItemMeta(powerMeta);
        inv.setItem(15, power);

        // ── Điền viền hồng & nền trắng ──────────────────────────────────────
        GuiUtil.fillBorder27(inv);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getSlot();

        if (slot == 11) {
            boolean ok = skillManager.upgradeMastery(player);
            if (ok) {
                player.sendMessage(CustomFish.PREFIX + "\u00a7aN\u00e2ng c\u1ea5p Thu\u1ea7n Th\u1ee5c th\u00e0nh c\u00f4ng!");
            } else {
                PlayerDataManager.PlayerData d = PlayerDataManager.getInstance().get(player);
                if (d.masteryLevel >= 5)
                    player.sendMessage(CustomFish.PREFIX + "\u00a7cThu\u1ea7n Th\u1ee5c \u0111\u00e3 \u0111\u1ea1t c\u1ea5p t\u1ed1i \u0111a!");
                else
                    player.sendMessage(CustomFish.PREFIX + "\u00a7cKh\u00f4ng \u0111\u1ee7 \u0111i\u1ec3m k\u1ef9 n\u0103ng! C\u1ea7n: " + getMasteryCost(d.masteryLevel));
            }
            open(player);

        } else if (slot == 15) {
            boolean ok = skillManager.upgradePower(player);
            if (ok) {
                player.sendMessage(CustomFish.PREFIX + "\u00a7aN\u00e2ng c\u1ea5p S\u1ee9c M\u1ea1nh th\u00e0nh c\u00f4ng!");
            } else {
                PlayerDataManager.PlayerData d = PlayerDataManager.getInstance().get(player);
                if (d.powerLevel >= 5)
                    player.sendMessage(CustomFish.PREFIX + "\u00a7cS\u1ee9c M\u1ea1nh \u0111\u00e3 \u0111\u1ea1t c\u1ea5p t\u1ed1i \u0111a!");
                else
                    player.sendMessage(CustomFish.PREFIX + "\u00a7cKh\u00f4ng \u0111\u1ee7 \u0111i\u1ec3m k\u1ef9 n\u0103ng! C\u1ea7n: " + getPowerCost(d.powerLevel));
            }
            open(player);
        }
    }

    // BUG FIX: Không unregisterAll khi đóng GUI.
    // Listener này là singleton và cần tồn tại suốt vòng đời plugin.
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Không làm gì — singleton listener không tự hủy.
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
