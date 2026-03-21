package online.aeck.customfish;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
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
 * FishInventoryGUI — lệnh /fishinventory (admin only).
 *
 * GUI 54 ô hiển thị tất cả vật phẩm trong plugin:
 *  Hàng 1 (0-8):  Cá theo phẩm chất (6 loại) + dải phân cách
 *  Hàng 2 (9-17): Cá đặc biệt từ config
 *  Hàng 3 (18-26): Cần câu
 *  Hàng 4 (27-35): Dây câu
 *  Hàng 5 (36-44): Lưỡi câu
 *  Hàng 6 (45-53): Mồi câu
 *
 * Click chuột phải = lấy 1 item (cá hoặc trang bị trực tiếp).
 * Click chuột trái = lấy 1 stack (cá) hoặc trang bị (equipment).
 */
public class FishInventoryGUI implements CommandExecutor, Listener {

    private static final String GUI_TITLE = "\u00a76\u00a7l[Admin] \u00a7fKho V\u1eadt Ph\u1ea9m Plugin";

    private final CustomFish plugin;
    // slot → action khi click
    private final Map<Integer, SlotAction> slotActions = new HashMap<>();

    public FishInventoryGUI(CustomFish plugin) {
        this.plugin = plugin;
    }

    // -------------------------------------------------------------------------
    // Command
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Open GUI
    // -------------------------------------------------------------------------

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE);
        slotActions.clear();

        // ── Hàng 1: Cá theo phẩm chất ──────────────────────────────────────
        inv.setItem(0, makeSectionLabel(Material.TROPICAL_FISH,
                "\u00a7f\u00a7lC\u00e1 Theo Ph\u1ea9m Ch\u1ea5t",
                "\u00a77Click \u0111\u1ec3 l\u1ea5y v\u1eadt ph\u1ea9m"));

        String[][] qualities = {
                {"common",    "\u00a7fC\u00e1 Th\u01b0\u1eddng",     "COD"},
                {"rare",      "\u00a79\u00a7lC\u00e1 Hi\u1ebfm",      "SALMON"},
                {"epic",      "\u00a75\u00a7lC\u00e1 S\u1eed Thi",     "TROPICAL_FISH"},
                {"legendary", "\u00a7e\u00a7lC\u00e1 Huy\u1ec1n Tho\u1ea1i", "PUFFERFISH"},
                {"mythic",    "\u00a7d\u00a7lC\u00e1 Th\u1ea7n Tho\u1ea1i",   "SALMON"},
                {"godly",     "\u00a76\u00a7lC\u00e1 Th\u00e1nh",     "TROPICAL_FISH"},
        };
        for (int i = 0; i < qualities.length; i++) {
            int slot = i + 1;
            String q       = qualities[i][0];
            String name    = qualities[i][1];
            Material mat   = Material.matchMaterial(qualities[i][2]);
            if (mat == null) mat = Material.COD;

            double[] len    = FishStats.lengthRange(q);
            double[] wt     = FishStats.weightRange(q);
            double midLen   = (len[0] + len[1]) / 2.0;
            double midWt    = (wt[0] + wt[1]) / 2.0;
            double price    = FishStats.basePrice(q) * 1.0; // price-multiplier=1 cho sample

            ItemStack item = makeFishItem(mat, name, q, midLen, midWt, price, 0);
            inv.setItem(slot, item);

            final String fq = q;
            final Material fmat = mat;
            final String fname = name;
            slotActions.put(slot, (p, shift) -> {
                int amount = shift ? 64 : 1;
                double[] l2 = FishStats.lengthRange(fq);
                double[] w2 = FishStats.weightRange(fq);
                double rndLen = l2[0] + Math.random() * (l2[1] - l2[0]);
                double rndWt  = w2[0] + Math.random() * (w2[1] - w2[0]);
                double p2 = FishStats.basePrice(fq) * plugin.getConfig().getDouble("price-multiplier", 1.0);
                ItemStack give = makeFishItem(fmat, fname, fq, rndLen, rndWt, p2, 0);
                give.setAmount(amount);
                overflow(p, give);
                p.sendMessage(CustomFish.PREFIX + "\u00a7aNh\u1eadn \u00a7f" + amount + "x " + stripColor(fname));
            });
        }

        // Divider slot 7
        inv.setItem(7, filler(Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(8, filler(Material.GRAY_STAINED_GLASS_PANE));

        // ── Hàng 2: Cá đặc biệt ────────────────────────────────────────────
        inv.setItem(9, makeSectionLabel(Material.PUFFERFISH,
                "\u00a76\u00a7lC\u00e1 \u0110\u1eb7c Bi\u1ec7t",
                "\u00a77Rare / Special fish t\u1eeb config"));

        List<Map<?, ?>> specials = plugin.getSpecialFishConfig().getMapList("special-fish");
        for (int i = 0; i < specials.size() && i < 8; i++) {
            Map<?, ?> sf  = specials.get(i);
            int slot      = 10 + i;
            String matStr = sf.get("material") != null ? sf.get("material").toString() : "COD";
            Material mat  = Material.matchMaterial(matStr);
            if (mat == null) mat = Material.COD;
            String name   = sf.get("display-name") != null
                    ? ((String) sf.get("display-name")).replace("&", "\u00a7") : "C\u00e1 \u0110B";
            String q      = sf.get("fixed-quality") != null ? sf.get("fixed-quality").toString() : "common";
            double chance = sf.get("chance") != null ? Double.parseDouble(sf.get("chance").toString()) : 0;

            double[] len  = FishStats.lengthRange(q);
            double[] wt   = FishStats.weightRange(q);
            double midLen = (len[0] + len[1]) / 2.0;
            double midWt  = (wt[0] + wt[1]) / 2.0;
            double price  = FishStats.basePrice(q);

            List<String> extraLore = new ArrayList<>();
            extraLore.add("\u00a77T\u1ec9 l\u1ec7 xu\u1ea5t hi\u1ec7n: \u00a7e" + String.format("%.1f", chance * 100) + "%");

            ItemStack item = makeFishItemExtra(mat, name, q, midLen, midWt, price, 0, extraLore);
            inv.setItem(slot, item);

            final Material fmat = mat;
            final String fname = name;
            final String fq = q;
            slotActions.put(slot, (p, shift) -> {
                int amount = shift ? 64 : 1;
                double[] l2 = FishStats.lengthRange(fq);
                double[] w2 = FishStats.weightRange(fq);
                double rndLen = l2[0] + Math.random() * (l2[1] - l2[0]);
                double rndWt  = w2[0] + Math.random() * (w2[1] - w2[0]);
                double p2 = FishStats.basePrice(fq) * plugin.getConfig().getDouble("price-multiplier", 1.0);
                ItemStack give = makeFishItem(fmat, fname, fq, rndLen, rndWt, p2, 0);
                give.setAmount(amount);
                overflow(p, give);
                p.sendMessage(CustomFish.PREFIX + "\u00a7aNh\u1eadn \u00a7f" + amount + "x " + stripColor(fname));
            });
        }

        // ── Hàng 3: Cần câu ─────────────────────────────────────────────────
        inv.setItem(18, makeSectionLabel(Material.FISHING_ROD,
                "\u00a7a\u00a7lC\u1ea7n C\u00e2u", "\u00a77Click \u0111\u1ec3 trang b\u1ecb"));

        List<FishingRodManager.FishingRod> rods = new ArrayList<>(plugin.getRodManager().getAllRods());
        for (int i = 0; i < rods.size() && i < 8; i++) {
            FishingRodManager.FishingRod rod = rods.get(i);
            int slot = 19 + i;
            PlayerDataManager.PlayerData data = PlayerDataManager.getInstance().get(player);
            boolean equipped = rod.id.equals(data.rodId);

            List<String> lore = Arrays.asList(
                    "\u00a77S\u00e1t th\u01b0\u01a1ng: \u00a7c" + rod.damage,
                    "\u00a77T\u1ed1c \u0111\u1ed9: \u00a7e" + rod.speed,
                    "\u00a77Crit: \u00a76" + (int)(rod.critChance*100) + "% x" + rod.critDamage,
                    equipped ? "\u00a7a\u2714 \u0110ang trang b\u1ecb" : "\u00a7eClick \u0111\u1ec3 trang b\u1ecb"
            );
            inv.setItem(slot, makeEquipItem(Material.FISHING_ROD,
                    rod.displayName.replace("&", "\u00a7"), lore, equipped));

            slotActions.put(slot, (p, shift) -> {
                PlayerDataManager.PlayerData d = PlayerDataManager.getInstance().get(p);
                d.rodId = rod.id;
                p.sendMessage(CustomFish.PREFIX + "\u00a7aTrang b\u1ecb c\u1ea7n: \u00a7f"
                        + rod.displayName.replace("&", "\u00a7"));
                openGUI(p);
            });
        }

        // ── Hàng 4: Dây câu ──────────────────────────────────────────────────
        inv.setItem(27, makeSectionLabel(Material.STRING,
                "\u00a7e\u00a7lD\u00e2y C\u00e2u", "\u00a77C\u00e2n n\u1eb7ng t\u1ed1i \u0111a m\u00e0 d\u00e2y ch\u1ecbu \u0111\u01b0\u1ee3c"));

        List<FishingRodManager.Line> lines = new ArrayList<>(plugin.getRodManager().getAllLines());
        for (int i = 0; i < lines.size() && i < 8; i++) {
            FishingRodManager.Line line = lines.get(i);
            int slot = 28 + i;
            PlayerDataManager.PlayerData data = PlayerDataManager.getInstance().get(player);
            boolean equipped = line.id.equals(data.lineId);

            List<String> lore = Arrays.asList(
                    "\u00a77T\u1ed1i \u0111a: \u00a7b" + line.maxWeight + " kg",
                    equipped ? "\u00a7a\u2714 \u0110ang trang b\u1ecb" : "\u00a7eClick \u0111\u1ec3 trang b\u1ecb"
            );
            inv.setItem(slot, makeEquipItem(Material.STRING,
                    line.displayName.replace("&", "\u00a7"), lore, equipped));

            slotActions.put(slot, (p, shift) -> {
                PlayerDataManager.PlayerData d = PlayerDataManager.getInstance().get(p);
                d.lineId = line.id;
                p.sendMessage(CustomFish.PREFIX + "\u00a7aTrang b\u1ecb d\u00e2y: \u00a7f"
                        + line.displayName.replace("&", "\u00a7"));
                openGUI(p);
            });
        }

        // ── Hàng 5: Lưỡi câu ────────────────────────────────────────────────
        inv.setItem(36, makeSectionLabel(Material.TRIPWIRE_HOOK,
                "\u00a7b\u00a7lL\u01b0\u1ee1i C\u00e2u", "\u00a77S\u00e1t th\u01b0\u01a1ng + lo\u1ea1i c\u00e1"));

        List<FishingRodManager.Hook> hooks = new ArrayList<>(plugin.getRodManager().getAllHooks());
        for (int i = 0; i < hooks.size() && i < 8; i++) {
            FishingRodManager.Hook hook = hooks.get(i);
            int slot = 37 + i;
            PlayerDataManager.PlayerData data = PlayerDataManager.getInstance().get(player);
            boolean equipped = hook.id.equals(data.hookId);

            List<String> lore = Arrays.asList(
                    "\u00a77S\u00e1t th\u01b0\u01a1ng: \u00a7c+" + hook.damage,
                    "\u00a77Lo\u1ea1i c\u00e1: \u00a7f" + String.join(", ", hook.fishTypes),
                    equipped ? "\u00a7a\u2714 \u0110ang trang b\u1ecb" : "\u00a7eClick \u0111\u1ec3 trang b\u1ecb"
            );
            inv.setItem(slot, makeEquipItem(Material.TRIPWIRE_HOOK,
                    hook.displayName.replace("&", "\u00a7"), lore, equipped));

            slotActions.put(slot, (p, shift) -> {
                PlayerDataManager.PlayerData d = PlayerDataManager.getInstance().get(p);
                d.hookId = hook.id;
                p.sendMessage(CustomFish.PREFIX + "\u00a7aTrang b\u1ecb l\u01b0\u1ee1i: \u00a7f"
                        + hook.displayName.replace("&", "\u00a7"));
                openGUI(p);
            });
        }

        // ── Hàng 6: Mồi câu ─────────────────────────────────────────────────
        inv.setItem(45, makeSectionLabel(Material.WHEAT_SEEDS,
                "\u00a7d\u00a7lM\u1ed3i C\u00e2u", "\u00a77T\u0103ng t\u1ef7 l\u1ec7 c\u00e1 hi\u1ebfm"));

        List<FishingRodManager.Bait> baits = new ArrayList<>(plugin.getRodManager().getAllBaits());
        for (int i = 0; i < baits.size() && i < 8; i++) {
            FishingRodManager.Bait bait = baits.get(i);
            int slot = 46 + i;
            PlayerDataManager.PlayerData data = PlayerDataManager.getInstance().get(player);
            boolean equipped = bait.id.equals(data.baitId);

            List<String> lore = Arrays.asList(
                    "\u00a77Bonus hi\u1ebfm: \u00a7d+" + (int)(bait.rareBonus * 100) + "%",
                    "\u00a77Hi\u1ec7u \u1ee9ng: \u00a7f" + bait.effect,
                    equipped ? "\u00a7a\u2714 \u0110ang trang b\u1ecb" : "\u00a7eClick \u0111\u1ec3 trang b\u1ecb"
            );
            inv.setItem(slot, makeEquipItem(Material.WHEAT_SEEDS,
                    bait.displayName.replace("&", "\u00a7"), lore, equipped));

            slotActions.put(slot, (p, shift) -> {
                PlayerDataManager.PlayerData d = PlayerDataManager.getInstance().get(p);
                d.baitId = bait.id;
                p.sendMessage(CustomFish.PREFIX + "\u00a7aTrang b\u1ecb m\u1ed3i: \u00a7f"
                        + bait.displayName.replace("&", "\u00a7"));
                openGUI(p);
            });
        }

        player.openInventory(inv);
    }

    // -------------------------------------------------------------------------
    // Event Handlers
    // -------------------------------------------------------------------------

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        SlotAction action = slotActions.get(event.getSlot());
        if (action == null) return;

        boolean shift = event.isShiftClick();
        action.execute(player, shift);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        // Không unregister — cùng 1 instance dùng chung cho mọi admin
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ItemStack makeFishItem(Material mat, String name, String quality,
                                   double length, double weight, double price, int expGain) {
        return makeFishItemExtra(mat, name, quality, length, weight, price, expGain, Collections.emptyList());
    }

    private ItemStack makeFishItemExtra(Material mat, String name, String quality,
                                        double length, double weight, double price,
                                        int expGain, List<String> extraLore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        List<String> lore = new ArrayList<>();
        lore.add("\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        lore.add("\u00a77Chi\u1ec1u d\u00e0i: \u00a7f" + String.format("%.1f", length) + " cm");
        lore.add("\u00a77C\u00e2n n\u1eb7ng: \u00a7f" + String.format("%.2f", weight) + " kg");
        lore.add(getQualityColor(quality) + "Ph\u1ea9m ch\u1ea5t: " + quality.toUpperCase());
        lore.add("\u00a7eGi\u00e1 b\u00e1n: \u00a7a$" + String.format("%.0f", price));
        if (expGain > 0) lore.add("\u00a77EXP: \u00a7b+" + expGain);
        lore.addAll(extraLore);
        lore.add("\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        lore.add("\u00a77D\u00f9ng /banca \u0111\u1ec3 b\u00e1n");
        meta.setLore(lore);

        // Giá trị bán
        meta.getPersistentDataContainer().set(
                CustomFish.FISH_PRICE_KEY, PersistentDataType.DOUBLE, price
        );
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeEquipItem(Material mat, String name, List<String> lore, boolean glow) {
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

    private ItemStack makeSectionLabel(Material mat, String name, String... loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>(Arrays.asList(loreLines));
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack filler(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
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
