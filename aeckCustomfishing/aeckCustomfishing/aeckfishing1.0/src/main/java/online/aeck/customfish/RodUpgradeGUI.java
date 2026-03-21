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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * RodUpgradeGUI — GUI quản lý trang bị câu cá (54 ô, 6 hàng):
 *  Hàng 1 (0-8):  Chọn Cần Câu
 *  Hàng 2 (9-17): Chọn Dây Câu
 *  Hàng 3 (18-26): Chọn Lưỡi Câu
 *  Hàng 4 (27-35): Chọn Mồi Câu
 *  Hàng 5-6: trống (phân cách)
 *
 * Item đang được trang bị sẽ có hiệu ứng phát sáng (enchant glow).
 * Nhấn vào item để trang bị ngay lập tức.
 */
public class RodUpgradeGUI implements Listener {

    public static final String GUI_TITLE = "\u00a7bTrang Bị Câu Cá - AECK";

    private final CustomFish plugin;
    private final FishingRodManager rodManager;

    // Mapping slot → loại và ID
    private final java.util.Map<Integer, SlotInfo> slotMap = new java.util.HashMap<>();

    public RodUpgradeGUI(CustomFish plugin) {
        this.plugin = plugin;
        this.rodManager = plugin.getRodManager();
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE);
        slotMap.clear();

        PlayerDataManager.PlayerData data = PlayerDataManager.getInstance().get(player);

        // --- Hàng 1: Label Cần Câu ---
        inv.setItem(0, makeSectionLabel(Material.FISHING_ROD, "\u00a7a\u00a7lCần Câu", "\u00a77Chọn cần câu muốn sử dụng"));

        // --- Điền cần câu từ slot 1 ---
        Collection<FishingRodManager.FishingRod> rods = rodManager.getAllRods();
        int slot = 1;
        for (FishingRodManager.FishingRod rod : rods) {
            if (slot >= 9) break;
            boolean equipped = rod.id.equals(data.rodId);
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Sát thương: \u00a7c" + rod.damage);
            lore.add("\u00a77Tốc độ: \u00a7e" + rod.speed);
            lore.add("\u00a77Crit: \u00a76" + (int)(rod.critChance * 100) + "% x" + rod.critDamage);
            lore.add("");
            lore.add(equipped ? "\u00a7a\u2714 Đang trang bị" : "\u00a7eNhấn để trang bị");
            ItemStack item = makeItem(Material.FISHING_ROD,
                    rod.displayName.replace("&", "\u00a7"),
                    lore, equipped);
            inv.setItem(slot, item);
            slotMap.put(slot, new SlotInfo("rod", rod.id));
            slot++;
        }

        // --- Hàng 2: Label Dây Câu ---
        inv.setItem(9, makeSectionLabel(Material.STRING, "\u00a7e\u00a7lDây Câu", "\u00a77Dây câu quyết định cân nặng tối đa của cá"));

        Collection<FishingRodManager.Line> lines = rodManager.getAllLines();
        slot = 10;
        for (FishingRodManager.Line line : lines) {
            if (slot >= 18) break;
            boolean equipped = line.id.equals(data.lineId);
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Cân nặng tối đa: \u00a7b" + line.maxWeight + " kg");
            lore.add("\u00a77(Cá nặng hơn sẽ đứt dây)");
            lore.add("");
            lore.add(equipped ? "\u00a7a\u2714 Đang trang bị" : "\u00a7eNhấn để trang bị");
            ItemStack item = makeItem(Material.STRING,
                    line.displayName.replace("&", "\u00a7"),
                    lore, equipped);
            inv.setItem(slot, item);
            slotMap.put(slot, new SlotInfo("line", line.id));
            slot++;
        }

        // --- Hàng 3: Label Lưỡi Câu ---
        inv.setItem(18, makeSectionLabel(Material.TRIPWIRE_HOOK, "\u00a7b\u00a7lLưỡi Câu", "\u00a77Sát thương giúp chiến đấu với cá khổng lồ"));

        Collection<FishingRodManager.Hook> hooks = rodManager.getAllHooks();
        slot = 19;
        for (FishingRodManager.Hook hook : hooks) {
            if (slot >= 27) break;
            boolean equipped = hook.id.equals(data.hookId);
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Sát thương lưỡi: \u00a7c+" + hook.damage);
            lore.add("\u00a77(Cộng vào cần câu khi chiến đấu cá kh. lồ)");
            lore.add("\u00a77Loại cá: \u00a7f" + String.join(", ", hook.fishTypes));
            lore.add("");
            lore.add(equipped ? "\u00a7a\u2714 Đang trang bị" : "\u00a7eNhấn để trang bị");
            ItemStack item = makeItem(Material.TRIPWIRE_HOOK,
                    hook.displayName.replace("&", "\u00a7"),
                    lore, equipped);
            inv.setItem(slot, item);
            slotMap.put(slot, new SlotInfo("hook", hook.id));
            slot++;
        }

        // --- Hàng 4: Label Mồi Câu ---
        inv.setItem(27, makeSectionLabel(Material.WHEAT_SEEDS, "\u00a7d\u00a7lMồi Câu", "\u00a77Mồi tăng tỉ lệ câu được cá hiếm"));

        Collection<FishingRodManager.Bait> baits = rodManager.getAllBaits();
        slot = 28;
        for (FishingRodManager.Bait bait : baits) {
            if (slot >= 36) break;
            boolean equipped = bait.id.equals(data.baitId);
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Bonus cá hiếm: \u00a7d+" + (int)(bait.rareBonus * 100) + "%");
            lore.add("\u00a77Hiệu ứng: \u00a7f" + bait.effect);
            lore.add("");
            lore.add(equipped ? "\u00a7a\u2714 Đang trang bị" : "\u00a7eNhấn để trang bị");
            ItemStack item = makeItem(Material.WHEAT_SEEDS,
                    bait.displayName.replace("&", "\u00a7"),
                    lore, equipped);
            inv.setItem(slot, item);
            slotMap.put(slot, new SlotInfo("bait", bait.id));
            slot++;
        }

        // --- Hàng 5: Tổng kết trang bị hiện tại ---
        ItemStack summary = makeSummary(data);
        inv.setItem(40, summary);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getSlot();

        SlotInfo info = slotMap.get(slot);
        if (info == null) return;

        PlayerDataManager.PlayerData data = PlayerDataManager.getInstance().get(player);
        boolean changed = false;

        switch (info.type) {
            case "rod" -> { data.rodId = info.id; changed = true; }
            case "line" -> { data.lineId = info.id; changed = true; }
            case "hook" -> { data.hookId = info.id; changed = true; }
            case "bait" -> { data.baitId = info.id; changed = true; }
        }

        if (changed) {
            player.sendMessage(CustomFish.PREFIX
                    + "\u00a7aĐã trang bị: \u00a7f" + info.id);
            // Làm mới GUI
            Bukkit.getScheduler().runTask(plugin, () -> open(player));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        org.bukkit.event.HandlerList.unregisterAll(this);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ItemStack makeSectionLabel(Material mat, String name, String... loreLine) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        for (String s : loreLine) lore.add(s);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeItem(Material mat, String name, List<String> lore, boolean glow) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        if (glow) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        } else {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeSummary(PlayerDataManager.PlayerData data) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("\u00a7d\u00a7lTrang Bị Hiện Tại");

        FishingRodManager rm = rodManager;
        FishingRodManager.FishingRod rod = rm.getRod(data.rodId);
        FishingRodManager.Line line = rm.getLine(data.lineId);
        FishingRodManager.Hook hook = rm.getHook(data.hookId);
        FishingRodManager.Bait bait = rm.getBait(data.baitId);

        double totalDmg = (rod != null ? rod.damage : 0) + (hook != null ? hook.damage : 0);

        List<String> lore = new ArrayList<>();
        lore.add("\u00a77Cần: \u00a7f" + (rod != null ? rod.displayName.replace("&", "\u00a7") : data.rodId));
        lore.add("\u00a77Dây: \u00a7f" + (line != null ? line.displayName.replace("&", "\u00a7") : data.lineId)
                + " \u00a77(tối đa \u00a7b" + (line != null ? line.maxWeight : "?") + "kg\u00a77)");
        lore.add("\u00a77Lưỡi: \u00a7f" + (hook != null ? hook.displayName.replace("&", "\u00a7") : data.hookId)
                + " \u00a77(+\u00a7c" + (hook != null ? hook.damage : 0) + " dmg\u00a77)");
        lore.add("\u00a77Mồi: \u00a7f" + (bait != null ? bait.displayName.replace("&", "\u00a7") : data.baitId)
                + " \u00a77(+\u00a7d" + (bait != null ? (int)(bait.rareBonus * 100) : 0) + "% hiếm\u00a77)");
        lore.add("\u00a78\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        lore.add("\u00a77Tổng sát thương: \u00a7c" + String.format("%.0f", totalDmg));
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private record SlotInfo(String type, String id) {}
}
