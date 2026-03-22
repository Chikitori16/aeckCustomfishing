package online.aeck.customfish;

import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FishCatchListener — xu ly su kien cau ca:
 *
 *  Nuoc (vanilla):
 *   CAUGHT_FISH → huy event, chay logic bat ca.
 *
 *  Dung nham (Lava Fishing — bat/tat qua config):
 *   FISHING   → neu phao roi vao dung nham, len lich "ca can" sau thoi gian ngau nhien.
 *   REEL_IN   → neu "ca can" dang hoat dong, kich hoat bat ca; nguoc lai huy lich.
 *   FAILED/IN_GROUND → don dep tat ca task lava.
 *
 *  Hieu suat: Biome + Time chi doc 1 lan trong moi lan bat ca.
 *  Khong vong lap chay ngam ngoai BukkitScheduler.runTaskLater (fire-and-forget).
 */
public class FishCatchListener implements Listener {

    private final CustomFish plugin;
    private final FishCombatManager combatManager;
    private final Random random = new Random();

    // Lava fishing state per player
    private final Map<UUID, BukkitTask> pendingLavaTasks = new ConcurrentHashMap<>();
    private final Set<UUID> activeLavaBites = ConcurrentHashMap.newKeySet();

    private static final String[] QUALITY_ORDER =
            {"common", "rare", "epic", "legendary", "mythic", "godly"};

    private static final Material[] FISH_MATERIALS = {
            Material.COD, Material.SALMON,
            Material.TROPICAL_FISH, Material.PUFFERFISH
    };

    public FishCatchListener(CustomFish plugin, FishCombatManager combatManager) {
        this.plugin = plugin;
        this.combatManager = combatManager;
    }

    // ── Main event handler ────────────────────────────────────────────────────

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        switch (event.getState()) {
            case CAUGHT_FISH   -> handleWaterCatch(event, player);
            case FISHING       -> handleCast(event, player);
            case REEL_IN       -> handleReelIn(event, player);
            case FAILED_ATTEMPT, IN_GROUND -> cleanupLava(player.getUniqueId());
            default            -> { /* bo qua cac state khac */ }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cleanupLava(event.getPlayer().getUniqueId());
    }

    // ── Nuoc: vanilla CAUGHT_FISH ─────────────────────────────────────────────

    private void handleWaterCatch(PlayerFishEvent event, Player player) {
        if (!(event.getCaught() instanceof Item caughtItem)) return;
        event.setCancelled(true);
        caughtItem.remove();
        executeCatch(player, false);
    }

    // ── Dung nham: len lich "ca can" ─────────────────────────────────────────

    private void handleCast(PlayerFishEvent event, Player player) {
        UUID uuid = player.getUniqueId();
        cleanupLava(uuid);

        if (!plugin.getConfig().getBoolean("lava-fishing.enabled", false)) return;
        if (!isBobberInLava(event.getHook())) return;

        int minWait = plugin.getConfig().getInt("lava-fishing.min-wait-seconds", 5);
        int maxWait = plugin.getConfig().getInt("lava-fishing.max-wait-seconds", 20);
        int range   = Math.max(1, maxWait - minWait);
        long delayTicks = (long)(minWait + random.nextInt(range)) * 20L;

        FishHook hook = event.getHook();

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            pendingLavaTasks.remove(uuid);
            if (!player.isOnline()) return;

            activeLavaBites.add(uuid);

            if (plugin.getConfig().getBoolean("lava-fishing.play-sound", true)) {
                hook.getWorld().playSound(
                        hook.getLocation(),
                        Sound.BLOCK_LAVA_POP,
                        1.5f, 0.6f
                );
            }

            player.sendMessage(CustomFish.PREFIX
                    + "\u00a7c\u2764 C\u00f3 g\u00ec \u0111\u00f3 \u0111ang c\u1eabn c\u00e2u trong dung n\u1edbm!"
                    + " \u00a77\u2192 Cu\u1ed9n ngay!");
        }, delayTicks);

        pendingLavaTasks.put(uuid, task);
    }

    // ── Dung nham: nguoi choi cuon cau ───────────────────────────────────────

    private void handleReelIn(PlayerFishEvent event, Player player) {
        UUID uuid = player.getUniqueId();

        BukkitTask pending = pendingLavaTasks.remove(uuid);
        if (pending != null) pending.cancel();

        if (activeLavaBites.remove(uuid)) {
            event.setCancelled(true);
            executeCatch(player, true);
        }
    }

    private void cleanupLava(UUID uuid) {
        BukkitTask task = pendingLavaTasks.remove(uuid);
        if (task != null) task.cancel();
        activeLavaBites.remove(uuid);
    }

    private boolean isBobberInLava(FishHook hook) {
        if (hook == null) return false;
        return hook.getLocation().getBlock().getType() == Material.LAVA;
    }

    // ── Logic bat ca chung (nuoc + dung nham) ─────────────────────────────────

    /**
     * @param isLava true neu dang cau tren dung nham
     */
    private void executeCatch(Player player, boolean isLava) {
        FileConfiguration config = plugin.getConfig();
        PlayerDataManager.PlayerData data = PlayerDataManager.getInstance().get(player);

        double masteryBonus = config.getDouble("skilltree.mastery-bonus", 0.05) * data.masteryLevel;
        double powerBonus   = config.getDouble("skilltree.power-bonus",   0.10) * data.powerLevel;
        double priceMulti   = config.getDouble("price-multiplier", 1.0);

        Biome biome     = player.getLocation().getBlock().getBiome();
        long  worldTime = player.getWorld().getTime();

        FishConfigManager.FishEntry fishEntry =
                plugin.getFishConfigManager().rollFish(biome, worldTime, isLava);

        boolean isCrit   = random.nextDouble() < 0.05;
        double critMulti = isCrit ? 1.5 : 1.0;

        FishCombatManager.CombatFish combatFish;
        if (fishEntry != null) {
            combatFish = buildFromFishEntry(fishEntry, masteryBonus, priceMulti, critMulti, powerBonus, config);
        } else {
            combatFish = buildFromQualitySystem(config, masteryBonus, powerBonus, priceMulti, critMulti);
        }

        combatManager.startCombat(player, combatFish);
    }

    // ── Builders ──────────────────────────────────────────────────────────────

    private FishCombatManager.CombatFish buildFromFishEntry(
            FishConfigManager.FishEntry entry,
            double masteryBonus, double priceMulti, double critMulti,
            double powerBonus, FileConfiguration config) {

        double weight = entry.weightMin
                + random.nextDouble() * (entry.weightMax - entry.weightMin);
        double length = entry.lengthMin
                + random.nextDouble() * (entry.lengthMax - entry.lengthMin);

        double price = entry.pricePerKg * weight * priceMulti
                * (1.0 + masteryBonus) * critMulti;

        String quality = "rare";
        int baseExp    = config.getInt("exp-per-quality." + quality, 5);
        int expGain    = (int)(baseExp * (1.0 + powerBonus) * critMulti);

        FishCombatManager.CombatFish cf = new FishCombatManager.CombatFish();
        cf.fishKey         = entry.key;
        cf.displayName     = entry.displayName;
        cf.quality         = quality;
        cf.material        = entry.material;
        cf.weight          = weight;
        cf.length          = length;
        cf.price           = price;
        cf.expGain         = expGain;
        cf.maxHp           = FishStats.calcHp(quality, weight);
        cf.currentHp       = cf.maxHp;
        cf.customModelData = entry.customModelData;
        cf.commandsOnCatch = entry.commandsOnCatch;
        return cf;
    }

    private FishCombatManager.CombatFish buildFromQualitySystem(
            FileConfiguration config,
            double masteryBonus, double powerBonus,
            double priceMulti, double critMulti) {

        Material fishMat = FISH_MATERIALS[random.nextInt(FISH_MATERIALS.length)];

        SpecialFishResult special = rollSpecialFish();
        String quality;
        String displayName;

        if (special != null) {
            quality     = special.quality;
            fishMat     = special.material;
            displayName = special.displayName;
        } else {
            quality     = determineQuality(config);
            displayName = getQualityColor(quality) + "C\u00e1 " + quality.toUpperCase();
        }

        double[] wRange = FishStats.weightRange(quality);
        double weight = wRange[0] + random.nextDouble() * (wRange[1] - wRange[0]);
        double length = FishStats.estimateLength(quality, weight);

        double price  = FishStats.calcPrice(quality, weight, priceMulti)
                * (1.0 + masteryBonus) * critMulti;
        int baseExp   = config.getInt("exp-per-quality." + quality, 5);
        int expGain   = (int)(baseExp * (1.0 + powerBonus) * critMulti);

        FishCombatManager.CombatFish cf = new FishCombatManager.CombatFish();
        cf.fishKey     = null;
        cf.displayName = displayName;
        cf.quality     = quality;
        cf.material    = fishMat;
        cf.weight      = weight;
        cf.length      = length;
        cf.price       = price;
        cf.expGain     = expGain;
        cf.maxHp       = FishStats.calcHp(quality, weight);
        cf.currentHp   = cf.maxHp;
        return cf;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private SpecialFishResult rollSpecialFish() {
        List<Map<?, ?>> specials = plugin.getSpecialFishConfig().getMapList("special-fish");
        for (Map<?, ?> sf : specials) {
            double chance = sf.get("chance") != null
                    ? Double.parseDouble(sf.get("chance").toString()) : 0.0;
            if (random.nextDouble() < chance) {
                String matName = sf.get("material") != null ? sf.get("material").toString() : "COD";
                Material mat   = Material.matchMaterial(matName);
                if (mat == null) mat = Material.COD;
                String name = sf.get("display-name") != null
                        ? ((String) sf.get("display-name")).replace("&", "\u00a7")
                        : "C\u00e1 \u0110\u1eb7c Bi\u1ec7t";
                String q = sf.get("fixed-quality") != null
                        ? sf.get("fixed-quality").toString() : "common";
                return new SpecialFishResult(mat, name, q);
            }
        }
        return null;
    }

    private record SpecialFishResult(Material material, String displayName, String quality) {}

    private String determineQuality(FileConfiguration config) {
        ConfigurationSection thresholds = config.getConfigurationSection("quality-thresholds");
        if (thresholds == null) return "common";
        double roll = random.nextDouble() * 100;
        String best = "common";
        double bestThreshold = 0;
        for (String tier : QUALITY_ORDER) {
            double adjusted = thresholds.getDouble(tier, 0);
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
