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
 * FishingQuestGUI — nhiệm vụ câu cá (54 ô).
 *
 * Layout (viền hồng, nội dung trong kính trắng):
 *  Slot 4  (hồng trên): Tiêu đề
 *  Slot 49 (hồng dưới): Hướng dẫn
 *  Nhiệm vụ: tối đa 9, nằm trong vùng trắng:
 *   Hàng 2 trắng: slots 10-16 (7 nhiệm vụ đầu)
 *   Hàng 3 trắng: slots 19-20 (2 nhiệm vụ tiếp)
 */
public class FishingQuestGUI implements Listener {

    public static final String GUI_TITLE = "\u00a7d\u00a7lNhi\u1ec7m V\u1ee5 C\u00e2u C\u00e1";

    private final FishingQuest questManager;
    private final List<FishingQuest.Quest> availableQuests;

    // ánh xạ slot → chỉ số nhiệm vụ
    private final java.util.Map<Integer, Integer> questSlotMap = new java.util.HashMap<>();

    public FishingQuestGUI(FishingQuest questManager, List<FishingQuest.Quest> availableQuests) {
        this.questManager = questManager;
        this.availableQuests = availableQuests;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE);
        questSlotMap.clear();

        FishingQuest.Quest activeQuest = questManager.getQuest(player);

        // ── Slot 4 (hồng trên): Tiêu đề ────────────────────────────────────
        ItemStack title = new ItemStack(Material.FISHING_ROD);
        ItemMeta titleMeta = title.getItemMeta();
        titleMeta.setDisplayName("\u00a7d\u00a7l\u272a Nhi\u1ec7m V\u1ee5 C\u00e2u C\u00e1");
        titleMeta.setLore(Arrays.asList(
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                "\u00a77Ch\u1ecdn nhi\u1ec7m v\u1ee5 \u0111\u1ec3 th\u1ef1c hi\u1ec7n.",
                "\u00a77Ch\u1ec9 \u0111\u01b0\u1ee3c l\u00e0m 1 nhi\u1ec7m v\u1ee5 c\u00f9ng l\u00fac.",
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"
        ));
        title.setItemMeta(titleMeta);
        inv.setItem(4, title);

        // ── Nhiệm vụ trong vùng trắng ───────────────────────────────────────
        // Slot mapping: i=0→10, ..., i=6→16, i=7→19, i=8→20
        for (int i = 0; i < availableQuests.size() && i < 9; i++) {
            int guiSlot = (i < 7) ? (10 + i) : (19 + i - 7);
            FishingQuest.Quest quest = availableQuests.get(i);

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            boolean isActive = activeQuest != null && quest.name.equals(activeQuest.name);

            meta.setDisplayName((isActive ? "\u00a7a[\u0110ang l\u00e0m] " : "\u00a7b") + quest.name);

            String progressLine = isActive
                    ? "\u00a77Ti\u1ebfn \u0111\u1ed9: \u00a7e" + activeQuest.progress + "/" + activeQuest.targetFish
                    : "\u00a77Ti\u1ebfn \u0111\u1ed9: \u00a7e0/" + quest.targetFish;

            String statusLine;
            if (isActive) {
                statusLine = activeQuest.completed
                        ? "\u00a76\u0110\u00e3 ho\u00e0n th\u00e0nh! Nh\u1ea5n \u0111\u1ec3 nh\u1eadn th\u01b0\u1edfng."
                        : "\u00a7e\u0110ang th\u1ef1c hi\u1ec7n nhi\u1ec7m v\u1ee5.";
            } else {
                statusLine = activeQuest == null ? "\u00a7aNh\u1ea5n \u0111\u1ec3 nh\u1eadn" : "\u00a7c\u0110ang b\u1eadn nhi\u1ec7m v\u1ee5 kh\u00e1c";
            }

            meta.setLore(Arrays.asList(
                    "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                    "\u00a77" + quest.description,
                    progressLine,
                    "\u00a7eTh\u01b0\u1edfng: \u00a7a" + quest.rewardExp + " exp\u00a77, \u00a7f" + quest.rewardItem,
                    "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                    statusLine
            ));
            item.setItemMeta(meta);
            inv.setItem(guiSlot, item);
            questSlotMap.put(guiSlot, i);
        }

        // ── Slot 49 (hồng dưới): Hướng dẫn ─────────────────────────────────
        ItemStack guide = new ItemStack(Material.BOOK);
        ItemMeta guideMeta = guide.getItemMeta();
        guideMeta.setDisplayName("\u00a77H\u01b0\u1edbng D\u1eabn");
        guideMeta.setLore(Arrays.asList(
                "\u00a77\u2022 Nh\u1ea5n v\u00e0o nhi\u1ec7m v\u1ee5 \u0111\u1ec3 nh\u1eadn.",
                "\u00a77\u2022 Ho\u00e0n th\u00e0nh \u0111\u1ec3 nh\u1eadn th\u01b0\u1edfng.",
                "\u00a77\u2022 Ch\u1ec9 l\u00e0m \u0111\u01b0\u1ee3c 1 nhi\u1ec7m v\u1ee5 c\u00f9ng l\u00fac."
        ));
        guide.setItemMeta(guideMeta);
        inv.setItem(49, guide);

        // ── Điền viền hồng & nền trắng ──────────────────────────────────────
        GuiUtil.fillBorder54(inv);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        Integer questIndex = questSlotMap.get(slot);
        if (questIndex == null || questIndex >= availableQuests.size()) return;

        FishingQuest.Quest quest = availableQuests.get(questIndex);
        FishingQuest.Quest activeQuest = questManager.getQuest(player);

        if (activeQuest != null && quest.name.equals(activeQuest.name)) {
            if (activeQuest.completed) {
                boolean claimed = questManager.claimReward(player);
                if (claimed) {
                    player.sendMessage("\u00a7a\u0110\u00e3 nh\u1eadn th\u01b0\u1edfng nhi\u1ec7m v\u1ee5!");
                } else {
                    player.sendMessage("\u00a7cKh\u00f4ng th\u1ec3 nh\u1eadn th\u01b0\u1edfng.");
                }
            } else {
                player.sendMessage("\u00a7eBan \u0111ang l\u00e0m nhi\u1ec7m v\u1ee5 n\u00e0y.");
            }
            player.closeInventory();
        } else if (activeQuest == null) {
            questManager.assignQuest(player, quest);
            player.sendMessage("\u00a7a\u0110\u00e3 nh\u1eadn nhi\u1ec7m v\u1ee5: " + quest.name);
            player.closeInventory();
        } else {
            player.sendMessage("\u00a7cB\u1ea1n ch\u1ec9 c\u00f3 th\u1ec3 l\u00e0m 1 nhi\u1ec7m v\u1ee5 c\u00f9ng l\u00fac.");
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
    }
}
