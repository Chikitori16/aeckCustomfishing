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
 * FishCombatManager — quản lý cơ chế chiến đấu với cá:
 *
 * - Mỗi người chơi có một CombatFish đang hoạt động
 * - HP cá hiển thị trên BossBar (màu thay đổi theo % HP)
 * - Nhấn chuột trái khi cầm cần câu = tấn công 1 lượt
 * - Cá KHÔNG tự thoát; trận chiến kết thúc khi:
 *     (a) HP cá về 0 → bắt được cá
 *     (b) Người chơi thoát server → cá thoát (thông báo)
 */
public class FishCombatManager {

    private final CustomFish plugin;
    private final Map<UUID, CombatFish> activeCombats = new ConcurrentHashMap<>();

    public FishCombatManager(CustomFish plugin) {
        this.plugin = plugin;
    }

    // -------------------------------------------------------------------------
    // Data class
    // -------------------------------------------------------------------------

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

        /** HP cơ bản theo phẩm chất + cân nặng */
        public static double calcHp(String quality, double weight) {
            double base = switch (quality.toLowerCase()) {
                case "godly"     -> 380;
                case "mythic"    -> 260;
                case "legendary" -> 180;
                case "epic"      -> 120;
                case "rare"      -> 80;
                default          -> 50;
            };
            return base + weight * 2.0;
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public boolean isInCombat(Player player) {
        return activeCombats.containsKey(player.getUniqueId());
    }

    public CombatFish getCombat(Player player) {
        return activeCombats.get(player.getUniqueId());
    }

    /**
     * Bắt đầu trận chiến mới với cá.
     * Gọi từ FishCatchListener khi CAUGHT_FISH.
     */
    public void startCombat(Player player, CombatFish fish) {
        // Kết thúc trận cũ nếu còn
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
     * Thực hiện 1 lượt tấn công của người chơi lên cá.
     */
    public void attack(Player player) {
        CombatFish fish = activeCombats.get(player.getUniqueId());
        if (fish == null) return;

        PlayerDataManager.PlayerData data = PlayerDataManager.getInstance().get(player);
        FishingRodManager rm = plugin.getRodManager();
        FishingRodManager.FishingRod rod = rm.getRod(data.rodId);
        FishingRodManager.Hook hook = rm.getHook(data.hookId);

        double rodDmg  = (rod  != null) ? rod.damage  : 10;
        double hookDmg = (hook != null) ? hook.damage : 5;
        double base    = rodDmg + hookDmg;

        // Power cấp độ cũng tăng nhẹ sát thương chiến đấu (+5%/cấp)
        double powerBonus = plugin.getConfig().getDouble("skilltree.power-bonus", 0.10) * data.powerLevel * 0.5;
        base *= (1 + powerBonus);

        // Crit
        boolean isCrit = rod != null && new Random().nextDouble() < rod.critChance;
        double finalDmg = isCrit ? base * (rod.critDamage) : base;
        finalDmg = Math.max(1, finalDmg);

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
     * Kết thúc trận chiến.
     * @param caught true nếu cá bị bắt, false nếu cá thoát
     */
    public void endCombat(Player player, boolean caught) {
        CombatFish fish = activeCombats.remove(player.getUniqueId());
        if (fish == null) return;

        if (fish.bossBar != null) {
            fish.bossBar.removeAll();
        }

        if (caught) {
            // Trao item cá
            ItemStack item = buildFishItem(fish);
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(item);
            if (!overflow.isEmpty()) {
                // Inventory đầy: thả item xuống đất
                player.getWorld().dropItemNaturally(player.getLocation(), overflow.get(0));
                player.sendMessage(CustomFish.PREFIX + "\u00a7eInventory \u0111\u1ea7y! C\u00e1 r\u01a1i xu\u1ed1ng \u0111\u1ea5t.");
            }

            player.sendMessage(CustomFish.PREFIX
                    + "\u00a7a\u2714 C\u00e2u \u0111\u01b0\u1ee3c: \u00a7f" + stripColor(fish.displayName)
                    + " \u00a77(\u00a7e" + String.format("%.2f", fish.weight) + "kg\u00a77)"
                    + " \u00a7a| +\u00a7b" + fish.expGain + " EXP"
                    + " \u00a7a| \u00a7e$" + String.format("%.0f", fish.price));

            // EXP
            plugin.getExpManager().addExp(player, fish.expGain);

            // Nhiệm vụ
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

            // Giải đấu
            plugin.getTournament().recordCatch(player, fish.weight);

            // MySQL log
            plugin.getMySQL().logCatchAsync(player.getName(), fish.material.name(),
                    fish.quality, fish.length, fish.weight);

        } else {
            if (player.isOnline()) {
                player.sendMessage(CustomFish.PREFIX
                        + "\u00a7cCu\u1ed9c chi\u1ebfn k\u1ebft th\u00fac. C\u00e1 \u0111\u00e3 tho\u00e1t!");
            }
        }
    }

    /** Gọi khi plugin tắt để xóa hết boss bar */
    public void removeAll() {
        for (CombatFish fish : activeCombats.values()) {
            if (fish.bossBar != null) fish.bossBar.removeAll();
        }
        activeCombats.clear();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

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

        meta.getPersistentDataContainer().set(
                CustomFish.FISH_PRICE_KEY, PersistentDataType.DOUBLE, fish.price
        );
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
