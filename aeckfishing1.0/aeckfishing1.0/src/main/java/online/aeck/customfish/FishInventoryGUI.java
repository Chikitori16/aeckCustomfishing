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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * FishInventoryGUI — /fishinventory (Admin only).
 *
 * Hệ thống trang bị đã bị xóa. Chỉ còn cá chất lượng & cá đặc biệt.
 *
 * Layout 54 ô (viền hồng + nội dung trắng):
 *  Slot 0         : Tiêu đề (hồng)
 *  Slots 1-6      : 6 loại cá theo chất lượng (hồng hàng 1, có thể click lấy)
 *  Slot 9         : Label "Cá Đặc Biệt" (hồng trái hàng 2)
 *  Slots 10-16    : Cá đặc biệt từ config (trắng hàng 2, max 7)
 *  Slot 49        : Thông tin admin (hồng dưới giữa)
 *  Còn lại        : viền hồng & nền trắng
 */
public class FishInventoryGUI implements CommandExecutor, Listener {

    private static final String GUI_TITLE =
            "\u00a76\u00a7l[Admin] \u00a7fKho C\u00e1 Plugin";

    private final CustomFish plugin;
    private final Map<Integer, SlotAction> slotActions = new HashMap<>();

    public FishInventoryGUI(CustomFish plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Ch\u1ec9 d\u00e0nh cho ng\u01b0\u1eddi ch\u01a1i.");
            return true;
        }
        if (!player.hasPermission("aeck.admin")) {
            player.sendMessage(CustomFish.PREFIX + "\u00a7cB\u1ea1n kh\u00f4ng c\u00f3 quy\u1ec1n!");
            return true;
        }
        openGUI(player);
        return true;
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE);
        slotActions.clear();

        // ── Slot 0 (hồng trên trái): Tiêu đề ────────────────────────────────
        inv.setItem(0, makeLabel(Material.CHEST,
                "\u00a76\u00a7l[Admin] Kho C\u00e1",
                "\u00a77Click c\u00e1 \u0111\u1ec3 l\u1ea5y v\u00e0o t\u00fai.",
                "\u00a77Shift+Click \u0111\u1ec3 l\u1ea5y 64."));

        // ── Slots 1-6 (hồng hàng 1): 6 cá theo chất lượng ──────────────────
        String[][] qualities = {
                {"common",    "\u00a7fC\u00e1 Th\u01b0\u1eddng",             "COD"},
                {"rare",      "\u00a79\u00a7lC\u00e1 Hi\u1ebfm",             "SALMON"},
                {"epic",      "\u00a75\u00a7lC\u00e1 S\u1eed Thi",           "TROPICAL_FISH"},
                {"legendary", "\u00a7e\u00a7lC\u00e1 Huy\u1ec1n Tho\u1ea1i", "PUFFERFISH"},
                {"mythic",    "\u00a7d\u00a7lC\u00e1 Th\u1ea7n Tho\u1ea1i",  "SALMON"},
                {"godly",     "\u00a76\u00a7lC\u00e1 Th\u00e1nh",            "TROPICAL_FISH"},
        };
        for (int i = 0; i < qualities.length; i++) {
            int slot    = i + 1;
            String q    = qualities[i][0];
            String name = qualities[i][1];
            Material mat = Material.matchMaterial(qualities[i][2]);
            if (mat == null) mat = Material.COD;

            double[] len = FishStats.lengthRange(q);
            double[] wt  = FishStats.weightRange(q);
            double midLen = (len[0] + len[1]) / 2.0;
            double midWt  = (wt[0] + wt[1]) / 2.0;
            double price  = FishStats.basePrice(q);

            inv.setItem(slot, makeFishItem(mat, name, q, midLen, midWt, price));

            final String fq = q; final Material fmat = mat; final String fname = name;
            slotActions.put(slot, (p, shift) -> {
                int amount = shift ? 64 : 1;
                double[] l2 = FishStats.lengthRange(fq);
                double[] w2 = FishStats.weightRange(fq);
                double rndLen = l2[0] + Math.random() * (l2[1] - l2[0]);
                double rndWt  = w2[0] + Math.random() * (w2[1] - w2[0]);
                double p2 = FishStats.basePrice(fq)
                        * plugin.getConfig().getDouble("price-multiplier", 1.0);
                ItemStack give = makeFishItem(fmat, fname, fq, rndLen, rndWt, p2);
                give.setAmount(amount);
                overflow(p, give);
                p.sendMessage(CustomFish.PREFIX
                        + "\u00a7aNh\u1eadn \u00a7f" + amount + "x " + stripColor(fname));
            });
        }

        // ── Slot 9 (hồng trái hàng 2): Label Cá Đặc Biệt ────────────────────
        inv.setItem(9, makeLabel(Material.PUFFERFISH,
                "\u00a76\u00a7lC\u00e1 \u0110\u1eb7c Bi\u1ec7t",
                "\u00a77Shift+Click l\u1ea5y 64"));

        // ── Slots 10-16 (trắng hàng 2): Cá đặc biệt ─────────────────────────
        List<Map<?, ?>> specials = plugin.getSpecialFishConfig().getMapList("special-fish");
        for (int i = 0; i < specials.size() && i < 7; i++) {
            Map<?, ?> sf  = specials.get(i);
            int slot      = 10 + i;
            String matStr = sf.get("material") != null ? sf.get("material").toString() : "COD";
            Material mat  = Material.matchMaterial(matStr);
            if (mat == null) mat = Material.COD;
            String name   = sf.get("display-name") != null
                    ? ((String) sf.get("display-name")).replace("&", "\u00a7")
                    : "C\u00e1 \u0110\u1eb7c Bi\u1ec7t";
            String q      = sf.get("fixed-quality") != null
                    ? sf.get("fixed-quality").toString() : "common";
            double chance = sf.get("chance") != null
                    ? Double.parseDouble(sf.get("chance").toString()) : 0.0;

            double[] len  = FishStats.lengthRange(q);
            double[] wt   = FishStats.weightRange(q);
            double midLen = (len[0] + len[1]) / 2.0;
            double midWt  = (wt[0] + wt[1]) / 2.0;
            double price  = FishStats.basePrice(q);

            // Tạo item với dòng tỉ lệ bổ sung
            ItemStack item = makeFishItem(mat, name, q, midLen, midWt, price);
            ItemMeta meta  = item.getItemMeta();
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();
            lore.add(lore.size() - 1,
                    "\u00a77T\u1ec9 l\u1ec7: \u00a7e" + String.format("%.1f", chance * 100) + "%");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot, item);

            final Material fmat = mat; final String fname = name; final String fq = q;
            slotActions.put(slot, (p, shift) -> {
                int amount = shift ? 64 : 1;
                double[] l2 = FishStats.lengthRange(fq);
                double[] w2 = FishStats.weightRange(fq);
                double rndLen2 = l2[0] + Math.random() * (l2[1] - l2[0]);
                double rndWt2  = w2[0] + Math.random() * (w2[1] - w2[0]);
                double p2 = FishStats.basePrice(fq)
                        * plugin.getConfig().getDouble("price-multiplier", 1.0);
                ItemStack give = makeFishItem(fmat, fname, fq, rndLen2, rndWt2, p2);
                give.setAmount(amount);
                overflow(p, give);
                p.sendMessage(CustomFish.PREFIX
                        + "\u00a7aNh\u1eadn \u00a7f" + amount + "x " + stripColor(fname));
            });
        }

        // ── Slot 49 (hồng dưới giữa): Info ───────────────────────────────────
        inv.setItem(49, makeLabel(Material.NETHER_STAR,
                "\u00a7e\u00a7lH\u01b0\u1edbng D\u1eabn",
                "\u00a77Click: l\u1ea5y 1 c\u00e1",
                "\u00a77Shift+Click: l\u1ea5y 64 c\u00e1",
                "\u00a77C\u00e1 \u0111\u01b0\u1ee3c t\u1ea1o ng\u1eabu nhi\u00ean trong t\u1ea7m c\u1ee7a ch\u1ea5t l\u01b0\u1ee3ng."));

        // ── Điền viền hồng & nền trắng ──────────────────────────────────────
        GuiUtil.fillBorder54(inv);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getType() == Material.AIR) return;

        SlotAction action = slotActions.get(event.getSlot());
        if (action == null) return;
        action.execute(player, event.isShiftClick());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Singleton listener — không unregister.
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ItemStack makeFishItem(Material mat, String name, String quality,
                                   double length, double weight, double price) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta  = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        lore.add("\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        lore.add("\u00a77Chi\u1ec1u d\u00e0i: \u00a7f" + String.format("%.1f", length) + " cm");
        lore.add("\u00a77C\u00e2n n\u1eb7ng: \u00a7f" + String.format("%.2f", weight) + " kg");
        lore.add(getQualityColor(quality) + "Ph\u1ea9m ch\u1ea5t: " + quality.toUpperCase());
        lore.add("\u00a7eGi\u00e1 b\u00e1n: \u00a7a$" + String.format("%.0f", price));
        lore.add("\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        lore.add("\u00a77Shift+Click l\u1ea5y 64 | Click l\u1ea5y 1");
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(
                CustomFish.FISH_PRICE_KEY, PersistentDataType.DOUBLE, price);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeLabel(Material mat, String name, String... loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta  = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(new ArrayList<>(Arrays.asList(loreLines)));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    private void overflow(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover.get(0));
        }
    }

    private String getQualityColor(String quality) {
        return switch (quality.toLowerCase()) {
            case "godly"     -> "\u00a76\u00a7l";
            case "mythic"    -> "\u00a7d\u00a7l";
            case "legendary" -> "\u00a7e\u00a7l";
            case "epic"      -> "\u00a75\u00a7l";
            case "rare"      -> "\u00a79\u00a7l";
            default          -> "\u00a7f";
        };
    }

    private String stripColor(String s) {
        return s == null ? "" : s.replaceAll("\u00a7.", "");
    }

    @FunctionalInterface
    interface SlotAction {
        void execute(Player player, boolean shift);
    }
}
