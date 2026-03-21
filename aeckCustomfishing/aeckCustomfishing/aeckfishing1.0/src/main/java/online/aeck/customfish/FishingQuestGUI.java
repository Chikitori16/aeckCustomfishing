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
    public static final String GUI_TITLE = "\u00a7aNhi\u1ec7m V\u1ee5 C\u00e2u C\u00e1";
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
            meta.setDisplayName((isActive ? "\u00a7a[\u0110ang l\u00e0m] " : "\u00a7b") + quest.name);
            String progressLine = isActive ? "\u00a77Ti\u1ebfn \u0111\u1ed9: " + activeQuest.progress + "/" + activeQuest.targetFish : "\u00a77Ti\u1ebfn \u0111\u1ed9: 0/" + quest.targetFish;
            String statusLine = isActive ? (activeQuest.completed ? "\u00a76\u0110\u00e3 ho\u00e0n th\u00e0nh! Nh\u1ea5n \u0111\u1ec3 nh\u1eadn th\u01b0\u1edfng." : "\u00a7e\u0110ang l\u00e0m nhi\u1ec7m v\u1ee5.") : "\u00a7aNh\u1ea5n \u0111\u1ec3 nh\u1eadn nhi\u1ec7m v\u1ee5";
            meta.setLore(java.util.Arrays.asList(
                "\u00a77" + quest.description,
                progressLine,
                "\u00a7eTh\u01b0\u1edfng: " + quest.rewardExp + " exp, " + quest.rewardItem,
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
                        player.sendMessage("\u00a7a\u0110\u00e3 nh\u1eadn th\u01b0\u1edfng nhi\u1ec7m v\u1ee5!");
                        try {
                            FishingQuest.Quest claimedQuest = questManager.getQuest(player);
                            if (claimedQuest != null && claimedQuest.rewardItem != null) {
                                org.bukkit.Material mat = org.bukkit.Material.matchMaterial(claimedQuest.rewardItem);
                                if (mat == null) {
                                    player.sendMessage("\u00a7cV\u1eadt ph\u1ea9m th\u01b0\u1edfng kh\u00f4ng h\u1ee3p l\u1ec7: " + claimedQuest.rewardItem);
                                }
                            }
                        } catch (Exception e) {
                            player.sendMessage("\u00a7cC\u00f3 l\u1ed7i khi nh\u1eadn v\u1eadt ph\u1ea9m th\u01b0\u1edfng.");
                        }
                    } else {
                        player.sendMessage("\u00a7cKh\u00f4ng th\u1ec3 nh\u1eadn th\u01b0\u1edfng.");
                    }
                } else {
                    player.sendMessage("\u00a7eB\u1ea1n \u0111ang l\u00e0m nhi\u1ec7m v\u1ee5 n\u00e0y.");
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
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
    }
}
