package online.aeck.customfish;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FishCombatManager — quan ly co che chien dau voi ca.
 *
 * Khong phu thuoc vao trang bi. Sat thuong co ban co dinh = 10.
 * Power skill tang nhe sat thuong chien dau (+5%/cap).
 * Crit co dinh 5% / nhan 1.5x.
 */
public class FishCombatManager {

    private final CustomFish plugin;
    private final Map<UUID, CombatFish> activeCombats = new ConcurrentHashMap<>();

    public FishCombatManager(CustomFish plugin) {
        this.plugin = plugin;
    }

    // ── Data class ────────────────────────────────────────────────────────────

    public static class CombatFish {
        public String displayName;
        public String quality;
        public Material material;
        public double currentHp;
        public double maxHp;
        public double weight;
        public double length;
        public double price;
        public int expGain;
        public BossBar bossBar;
        /** Key trong fish.yml (null neu la ca thuong theo quality cu) */
        public String fishKey;
        /**
         * CustomModelData gan len item (>= 1 de ap dung, 0 = khong dung).
         * Resource pack dung so nay de render model 3D rieng cho tung loai ca.
         */
        public int customModelData = 0;
        /** Danh sach lenh chay sau khi bat duoc ca (tu Commands_On_Catch) */
        public List<String> commandsOnCatch = new ArrayList<>();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public boolean isInCombat(Player player) {
        return activeCombats.containsKey(player.getUniqueId());
    }

    public CombatFish getCombat(Player player) {
        return activeCombats.get(player.getUniqueId());
    }

    /**
     * Bat dau tran chien moi voi ca. Goi tu FishCatchListener.
     */
    public void startCombat(Player player, CombatFish fish) {
        endCombat(player, false);

        BossBar bar = Bukkit.createBossBar(
                buildBarTitle(fish),
                getBarColor(fish.currentHp, fish.maxHp),
                BarStyle.SEGMENTED_10
        );
        bar.setProgress(1.0);
        bar.addPlayer(player);
        fish.bossBar = bar;

        activeCombats.put(player.getUniqueId(), fish);

        player.sendMessage(CustomFish.PREFIX
                + "\u00a7c\u2620 C\u00e1 \u00a7f" + stripColor(fish.displayName)
                + " \u00a77(" + fish.quality.toUpperCase() + ")\u00a7c xu\u1ea5t hi\u1ec7n!"
                + " \u2764 HP: \u00a7e" + (int) fish.maxHp);
        player.sendMessage("\u00a77\u2192 Nh\u1ea5n \u00a7e\u00a7lChu\u1ed9t Tr\u00e1i \u00a77(gi\u1eef c\u1ea7n c\u00e2u) \u0111\u1ec3 t\u1ea5n c\u00f4ng!");
    }

    /**
     * Thuc hien 1 luot tan cong.
     * Sat thuong co ban = 10, Power skill +5%/cap, crit 5% / x1.5.
     */
    public void attack(Player player) {
        CombatFish fish = activeCombats.get(player.getUniqueId());
        if (fish == null) return;

        PlayerDataManager.PlayerData data = PlayerDataManager.getInstance().get(player);

        double base = 10.0;
        double powerBonus = plugin.getConfig().getDouble("skilltree.power-bonus", 0.10)
                * data.powerLevel * 0.5;
        base *= (1.0 + powerBonus);

        boolean isCrit  = new Random().nextDouble() < 0.05;
        double finalDmg = Math.max(1, isCrit ? base * 1.5 : base);

        fish.currentHp -= finalDmg;

        String dmgColor = isCrit ? "\u00a76\u2605 CRIT! \u00a7c" : "\u00a7c";
        String dmgText  = dmgColor + "-" + String.format("%.0f", finalDmg) + " HP";

        if (fish.currentHp <= 0) {
            fish.currentHp = 0;
            player.sendMessage(dmgText + " \u00a7a\u2192 C\u00e1 \u0111\u00e3 b\u1ecb h\u1ea1!");
            endCombat(player, true);
        } else {
            double progress = Math.max(0, Math.min(1.0, fish.currentHp / fish.maxHp));
            fish.bossBar.setProgress(progress);
            fish.bossBar.setColor(getBarColor(fish.currentHp, fish.maxHp));
            fish.bossBar.setTitle(buildBarTitle(fish));
            player.sendMessage(dmgText
                    + " \u00a77| HP c\u00f2n: \u00a7c" + (int) fish.currentHp
                    + "\u00a77/\u00a7c" + (int) fish.maxHp);
        }
    }

    /**
     * Ket thuc tran chien.
     * @param caught true neu ca bi bat, false neu thoat
     */
    public void endCombat(Player player, boolean caught) {
        CombatFish fish = activeCombats.remove(player.getUniqueId());
        if (fish == null) return;

        if (fish.bossBar != null) fish.bossBar.removeAll();

        if (caught) {
            ItemStack item = buildFishItem(fish);
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(item);
            if (!overflow.isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), overflow.get(0));
                player.sendMessage(CustomFish.PREFIX + "\u00a7eInventory \u0111\u1ea7y! C\u00e1 r\u01a1i xu\u1ed1ng \u0111\u1ea5t.");
            }

            player.sendMessage(CustomFish.PREFIX
                    + "\u00a7a\u2714 C\u00e2u \u0111\u01b0\u1ee3c: \u00a7f" + stripColor(fish.displayName)
                    + " \u00a77(\u00a7e" + String.format("%.2f", fish.weight) + "kg\u00a77)"
                    + " \u00a7a| +\u00a7b" + fish.expGain + " EXP"
                    + " \u00a7a| \u00a7e$" + String.format("%.0f", fish.price));

            plugin.getExpManager().addExp(player, fish.expGain);

            boolean questDone = plugin.getQuestManager().addProgress(player, fish.quality);
            if (questDone) {
                player.sendMessage(CustomFish.PREFIX
                        + "\u00a76\u2605 Nhi\u1ec7m v\u1ee5 ho\u00e0n th\u00e0nh! D\u00f9ng /nhiemvucauca \u0111\u1ec3 nh\u1eadn th\u01b0\u1edfng.");
            } else {
                FishingQuest.Quest quest = plugin.getQuestManager().getQuest(player);
                if (quest != null && !quest.completed) {
                    player.sendMessage("\u00a77[Nhi\u1ec7m v\u1ee5] " + quest.name
                            + ": \u00a7e" + quest.progress + "\u00a77/\u00a7e" + quest.targetFish);
                }
            }

            plugin.getTournament().recordCatch(player, fish.weight);
            plugin.getMySQL().logCatchAsync(player.getName(), fish.material.name(),
                    fish.quality, fish.length, fish.weight);

            // ── Thuc thi Commands_On_Catch ─────────────────────────────────────
            executeCommands(player, fish);

        } else {
            if (player.isOnline()) {
                player.sendMessage(CustomFish.PREFIX
                        + "\u00a7cCu\u1ed9c chi\u1ebfn k\u1ebft th\u00fac. C\u00e1 \u0111\u00e3 tho\u00e1t!");
            }
        }
    }

    /** Xoa het boss bar khi plugin tat */
    public void removeAll() {
        for (CombatFish fish : activeCombats.values()) {
            if (fish.bossBar != null) fish.bossBar.removeAll();
        }
        activeCombats.clear();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Thuc thi danh sach lenh Commands_On_Catch voi placeholder da thay the.
     * Placeholder: %player%, %fish%, %weight%, %length%
     */
    private void executeCommands(Player player, CombatFish fish) {
        if (fish.commandsOnCatch == null || fish.commandsOnCatch.isEmpty()) return;

        String playerName  = player.getName();
        String fishName    = stripColor(fish.displayName);
        String weightStr   = String.format("%.2f", fish.weight);
        String lengthStr   = String.format("%.1f", fish.length);

        for (String rawCmd : fish.commandsOnCatch) {
            if (rawCmd == null || rawCmd.isBlank()) continue;
            String cmd = rawCmd
                    .replace("%player%", playerName)
                    .replace("%fish%",   fishName)
                    .replace("%weight%", weightStr)
                    .replace("%length%", lengthStr)
                    .replace("&", "\u00a7");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
    }

    private String buildBarTitle(CombatFish fish) {
        return fish.displayName + " \u00a78| \u00a7c\u2764 "
                + (int) fish.currentHp + "\u00a77/\u00a7c" + (int) fish.maxHp;
    }

    private BarColor getBarColor(double hp, double maxHp) {
        double pct = hp / maxHp;
        if (pct > 0.6) return BarColor.GREEN;
        if (pct > 0.3) return BarColor.YELLOW;
        return BarColor.RED;
    }

    /**
     * Xay dung ItemStack cho con ca:
     *  - Luu gia (FISH_PRICE_KEY), can nang (FISH_WEIGHT_KEY),
     *    chieu dai (FISH_LENGTH_KEY) vao PersistentDataContainer (Double).
     */
    private ItemStack buildFishItem(CombatFish fish) {
        ItemStack item = new ItemStack(fish.material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(fish.displayName);

        List<String> lore = new ArrayList<>();
        lore.add("\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        lore.add("\u00a77Chi\u1ec1u d\u00e0i: \u00a7f" + String.format("%.1f", fish.length) + " cm");
        lore.add("\u00a77C\u00e2n n\u1eb7ng: \u00a7f" + String.format("%.2f", fish.weight) + " kg");
        lore.add(getQualityColor(fish.quality) + "Ph\u1ea9m ch\u1ea5t: " + fish.quality.toUpperCase());
        lore.add("\u00a7eGi\u00e1 b\u00e1n: \u00a7a$" + String.format("%.0f", fish.price));
        lore.add("\u00a77Kinh nghi\u1ec7m: \u00a7b+" + fish.expGain);
        lore.add("\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        lore.add("\u00a77D\u00f9ng /banca \u0111\u1ec3 b\u00e1n");
        meta.setLore(lore);

        // CustomModelData — ap dung model tu resource pack (chi khi > 0)
        if (fish.customModelData > 0) {
            meta.setCustomModelData(fish.customModelData);
        }

        // PersistentDataContainer — Double, doc duoc boi cac plugin khac
        meta.getPersistentDataContainer().set(
                CustomFish.FISH_PRICE_KEY,  PersistentDataType.DOUBLE, fish.price);
        meta.getPersistentDataContainer().set(
                CustomFish.FISH_WEIGHT_KEY, PersistentDataType.DOUBLE, fish.weight);
        meta.getPersistentDataContainer().set(
                CustomFish.FISH_LENGTH_KEY, PersistentDataType.DOUBLE, fish.length);

        item.setItemMeta(meta);
        return item;
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
}
