package online.aeck.customfish;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * FishCatchListener — khi cá cắn câu (CAUGHT_FISH):
 *   1. Hủy sự kiện gốc
 *   2. Tính thông số cá theo chất lượng (dùng FishStats cho tỉ lệ hợp lý)
 *   3. Kiểm tra dây câu
 *   4. Khởi động FishCombatManager.startCombat()
 */
public class FishCatchListener implements Listener {

    private final CustomFish plugin;
    private final FishingRodManager rodManager;
    private final FishCombatManager combatManager;
    private final Random random = new Random();

    private static final String[] QUALITY_ORDER =
            {"common", "rare", "epic", "legendary", "mythic", "godly"};

    public FishCatchListener(CustomFish plugin,
                             FishingRodManager rodManager,
                             FishCombatManager combatManager) {
        this.plugin = plugin;
        this.rodManager = rodManager;
        this.combatManager = combatManager;
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof Item caughtItem)) return;

        Player player = event.getPlayer();

        // Hủy sự kiện gốc; xóa entity cá nổi trên nước
        event.setCancelled(true);
        caughtItem.remove();

        FileConfiguration config = plugin.getConfig();

        // ── Lấy trang bị người chơi ─────────────────────────────────────────
        PlayerDataManager.PlayerData data = PlayerDataManager.getInstance().get(player);
        FishingRodManager.FishingRod rod  = rodManager.getRod(data.rodId);
        FishingRodManager.Line       line = rodManager.getLine(data.lineId);
        FishingRodManager.Hook       hook = rodManager.getHook(data.hookId);
        FishingRodManager.Bait       bait = rodManager.getBait(data.baitId);

        double hookDamage    = (hook != null) ? hook.damage    : 5;
        double lineMaxWeight = (line != null) ? line.maxWeight : 50;
        double baitRareBonus = (bait != null) ? bait.rareBonus : 0.0;
        double masteryBonus  = config.getDouble("skilltree.mastery-bonus", 0.05) * data.masteryLevel;
        double powerBonus    = config.getDouble("skilltree.power-bonus",   0.10) * data.powerLevel;
        double priceMulti    = config.getDouble("price-multiplier", 1.0);

        // ── Xác định loại cá theo lưỡi câu ──────────────────────────────────
        Material fishMat = pickFishMaterial(hook);

        // ── Xác định phẩm chất (bait bonus giảm ngưỡng) ─────────────────────
        SpecialFishResult special = rollSpecialFish(baitRareBonus, fishMat);
        String quality;
        String displayName;

        if (special != null) {
            quality     = special.quality;
            fishMat     = special.material;
            displayName = special.displayName;
        } else {
            quality     = determineQuality(config, baitRareBonus);
            displayName = getQualityColor(quality) + "C\u00e1 " + quality.toUpperCase();
        }

        // ── Sinh cân nặng ngẫu nhiên theo tier ──────────────────────────────
        // Lưỡi câu tăng nhẹ phạm vi cân nặng (mỗi 10 damage → +5% range)
        double hookWeightBonus = 1.0 + hookDamage / 200.0;
        double[] wRange = FishStats.weightRange(quality);
        double minW = wRange[0];
        double maxW = wRange[1] * hookWeightBonus;
        double weight = minW + random.nextDouble() * (maxW - minW);

        // Chiều dài tỉ lệ với cân nặng (cá nặng thì dài hơn, có noise ±10%)
        double length = FishStats.estimateLength(quality, weight);

        // ── Kiểm tra dây câu ──────────────────────────────────────────────────
        if (weight > lineMaxWeight) {
            player.sendMessage(CustomFish.PREFIX
                    + "\u00a7cCon c\u00e1 n\u1eb7ng \u00a7e" + String.format("%.1f", weight)
                    + " kg \u00a7c\u0111\u00e3 \u0111\u1ee9t d\u00e2y c\u00e2u tho\u00e1t!"
                    + " \u00a77(D\u00e2y ch\u1ecbu t\u1ed1i \u0111a: \u00a7b" + lineMaxWeight + "kg\u00a77)");
            return;
        }

        // ── Giá (có mastery bonus + weight bonus trong tier) ─────────────────
        boolean isCrit   = rod != null && random.nextDouble() < rod.critChance;
        double critMulti = (isCrit && rod != null) ? rod.critDamage : 1.0;
        double price = FishStats.calcPrice(quality, weight, priceMulti)
                * (1.0 + masteryBonus) * critMulti;

        // ── EXP ──────────────────────────────────────────────────────────────
        int baseExp = config.getInt("exp-per-quality." + quality, 5);
        int expGain = (int)(baseExp * (1.0 + powerBonus) * critMulti);

        // ── Xây dựng CombatFish và khởi động trận chiến ──────────────────────
        FishCombatManager.CombatFish combatFish = new FishCombatManager.CombatFish();
        combatFish.displayName = displayName;
        combatFish.quality     = quality;
        combatFish.material    = fishMat;
        combatFish.weight      = weight;
        combatFish.length      = length;
        combatFish.price       = price;
        combatFish.expGain     = expGain;
        combatFish.maxHp       = FishStats.calcHp(quality, weight);
        combatFish.currentHp   = combatFish.maxHp;

        combatManager.startCombat(player, combatFish);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Material pickFishMaterial(FishingRodManager.Hook hook) {
        if (hook != null && !hook.fishTypes.isEmpty()) {
            String typeName = hook.fishTypes.get(random.nextInt(hook.fishTypes.size()));
            Material mat = Material.matchMaterial(typeName);
            if (mat != null) return mat;
        }
        Material[] defaults = {Material.COD, Material.SALMON,
                Material.TROPICAL_FISH, Material.PUFFERFISH};
        return defaults[random.nextInt(defaults.length)];
    }

    private SpecialFishResult rollSpecialFish(double baitRareBonus, Material hookFishMat) {
        List<Map<?, ?>> specials = plugin.getSpecialFishConfig().getMapList("special-fish");
        for (Map<?, ?> sf : specials) {
            double base   = sf.get("chance") != null
                    ? Double.parseDouble(sf.get("chance").toString()) : 0.0;
            double chance = Math.min(base + baitRareBonus * base, 0.99);
            if (random.nextDouble() < chance) {
                String matName = sf.get("material") != null ? sf.get("material").toString() : "COD";
                Material mat   = Material.matchMaterial(matName);
                if (mat == null) mat = Material.COD;
                String name    = sf.get("display-name") != null
                        ? ((String) sf.get("display-name")).replace("&", "\u00a7")
                        : "C\u00e1 \u0110\u1eb7c Bi\u1ec7t";
                String q       = sf.get("fixed-quality") != null
                        ? sf.get("fixed-quality").toString() : "common";
                return new SpecialFishResult(mat, name, q);
            }
        }
        return null;
    }

    private record SpecialFishResult(Material material, String displayName, String quality) {}

    private String determineQuality(FileConfiguration config, double baitRareBonus) {
        ConfigurationSection thresholds = config.getConfigurationSection("quality-thresholds");
        if (thresholds == null) return "common";

        double roll = random.nextDouble() * 100;
        String best = "common";
        double bestThreshold = 0;

        for (String tier : QUALITY_ORDER) {
            double raw      = thresholds.getDouble(tier, 0);
            double adjusted = Math.max(0, raw - raw * baitRareBonus);
            if (roll >= adjusted && adjusted >= bestThreshold) {
                best = tier;
                bestThreshold = adjusted;
            }
        }
        return best;
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
}
