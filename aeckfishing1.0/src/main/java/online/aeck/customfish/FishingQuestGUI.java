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

import java.util.List;

public class FishingQuestGUI implements Listener {
    public static final String GUI_TITLE = "§aNhiệm Vụ Câu Cá";
    private final FishingQuest questManager;
    private final List<FishingQuest.Quest> availableQuests;

    public FishingQuestGUI(FishingQuest questManager, List<FishingQuest.Quest> availableQuests) {
        this.questManager = questManager;
        this.availableQuests = availableQuests;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, GUI_TITLE);
        FishingQuest.Quest activeQuest = questManager.getQuest(player);
        for (int i = 0; i < availableQuests.size() && i < 9; i++) {
            FishingQuest.Quest quest = availableQuests.get(i);
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            boolean isActive = activeQuest != null && quest.name.equals(activeQuest.name);
            meta.setDisplayName((isActive ? "§a[Đang làm] " : "§b") + quest.name);
            String progressLine = isActive ? "§7Tiến độ: " + activeQuest.progress + "/" + activeQuest.targetFish : "§7Tiến độ: 0/" + quest.targetFish;
            String statusLine = isActive ? (activeQuest.completed ? "§6Đã hoàn thành! Nhấn để nhận thưởng." : "§eĐang làm nhiệm vụ.") : "§aNhấn để nhận nhiệm vụ";
            meta.setLore(java.util.Arrays.asList(
                "§7" + quest.description,
                progressLine,
                "§eThưởng: " + quest.rewardExp + " exp, " + quest.rewardItem,
                statusLine
            ));
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        if (slot >= 0 && slot < availableQuests.size()) {
            FishingQuest.Quest quest = availableQuests.get(slot);
            FishingQuest.Quest activeQuest = questManager.getQuest(player);
            if (activeQuest != null && quest.name.equals(activeQuest.name)) {
                if (activeQuest.completed) {
                    boolean claimed = questManager.claimReward(player);
                    if (claimed) {
                        player.sendMessage("§aĐã nhận thưởng nhiệm vụ!");
                            // Đã hoàn thiện logic thưởng exp và item trong FishingQuest.claimReward()
                            // Có thể thêm xử lý ngoại lệ nếu cần
                            try {
                                // Kiểm tra lại phần thưởng item
                                FishingQuest.Quest quest = questManager.getQuest(player);
                                if (quest != null && quest.rewardItem != null) {
                                    org.bukkit.Material mat = org.bukkit.Material.matchMaterial(quest.rewardItem);
                                    if (mat == null) {
                                        player.sendMessage("§cVật phẩm thưởng không hợp lệ: " + quest.rewardItem);
                                    }
                                }
                            } catch (Exception e) {
                                player.sendMessage("§cCó lỗi khi nhận vật phẩm thưởng.");
                            }
                    } else {
                        player.sendMessage("§cKhông thể nhận thưởng.");
                    }
                } else {
                    player.sendMessage("§eBạn đang làm nhiệm vụ này.");
                }
                player.closeInventory();
            } else if (activeQuest == null) {
                questManager.assignQuest(player, quest);
                player.sendMessage("§aĐã nhận nhiệm vụ: " + quest.name);
                player.closeInventory();
            } else {
                player.sendMessage("§cBạn chỉ có thể làm 1 nhiệm vụ cùng lúc.");
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        // listener auto handled; avoid unregister to prevent conflicts
    }
}
