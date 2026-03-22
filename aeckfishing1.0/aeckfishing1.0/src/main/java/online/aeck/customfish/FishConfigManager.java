package online.aeck.customfish;

import org.bukkit.block.Biome;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * FishConfigManager — quan ly file misc/fish.yml.
 *
 * - Load va parse tat ca cac loai ca tu file cau hinh.
 * - Loc danh sach ca thoa man: Biome + Time + moi truong (nuoc/dung nham).
 * - Chon ngau nhien co trong so dua tren truong Rarity.
 * - Ho tro reloadConfig() tai lai ma khong can restart server.
 *
 * Cac truong trong fish.yml:
 *   Lava_Only  : true  → chi xuat hien khi cau tren dung nham
 *   Water_Only : true  → chi xuat hien khi cau duoi nuoc (mac dinh)
 *   Neu ca hai deu false → xuat hien ca hai moi truong.
 */
public class FishConfigManager {

    private final CustomFish plugin;
    private FileConfiguration fishConfig;
    private final List<FishEntry> entries = new ArrayList<>();
    private final Random random = new Random();

    private static final int DAY_START   = 0;
    private static final int DAY_END     = 12000;

    public FishConfigManager(CustomFish plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Tai lai fish.yml tu dia — goi tu lenh /fish reload. */
    public void reloadConfig() {
        loadConfig();
    }

    /**
     * Chon ngau nhien co trong so 1 loai ca thoa man tat ca dieu kien.
     * Tra ve null neu khong co loai nao phu hop.
     *
     * @param biome     Biome tai vi tri nguoi choi
     * @param worldTime Thoi gian the gioi (World#getTime())
     * @param isLava    true neu nguoi choi dang cau tren dung nham
     */
    public FishEntry rollFish(Biome biome, long worldTime, boolean isLava) {
        String timePeriod = resolveTimePeriod(worldTime);

        List<FishEntry> eligible = new ArrayList<>();
        for (FishEntry e : entries) {
            if (!matchesEnvironment(e, isLava)) continue;
            if (!matchesBiome(e, biome)) continue;
            if (!matchesTime(e, timePeriod)) continue;
            eligible.add(e);
        }

        if (eligible.isEmpty()) return null;

        int totalWeight = eligible.stream().mapToInt(e -> e.rarity).sum();
        if (totalWeight <= 0) return eligible.get(0);

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (FishEntry e : eligible) {
            cumulative += e.rarity;
            if (roll < cumulative) return e;
        }
        return eligible.get(eligible.size() - 1);
    }

    public List<FishEntry> getEntries() {
        return entries;
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private void loadConfig() {
        File miscDir = new File(plugin.getDataFolder(), "misc");
        if (!miscDir.exists()) miscDir.mkdirs();

        File fishFile = new File(miscDir, "fish.yml");
        if (!fishFile.exists()) {
            plugin.saveResource("misc/fish.yml", false);
        }

        fishConfig = YamlConfiguration.loadConfiguration(fishFile);

        InputStream defStream = plugin.getResource("misc/fish.yml");
        if (defStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            fishConfig.setDefaults(defaults);
        }

        parseEntries();
        plugin.getLogger().info("[CustomFish] Da tai " + entries.size() + " loai ca tu fish.yml");
    }

    private void parseEntries() {
        entries.clear();
        ConfigurationSection fishSection = fishConfig.getConfigurationSection("Fish");
        if (fishSection == null) {
            plugin.getLogger().warning("[CustomFish] Khong tim thay section 'Fish' trong fish.yml!");
            return;
        }

        for (String key : fishSection.getKeys(false)) {
            ConfigurationSection sec = fishSection.getConfigurationSection(key);
            if (sec == null) continue;

            FishEntry entry = new FishEntry();
            entry.key = key;
            entry.displayName = sec.getString("Display_Name", "&fCa " + key)
                    .replace("&", "\u00a7");
            entry.rarity = Math.max(1, sec.getInt("Rarity", 1));

            String matName = sec.getString("Material", "COD");
            entry.material = Material.matchMaterial(matName);
            if (entry.material == null) {
                plugin.getLogger().warning("[CustomFish] Material khong hop le: "
                        + matName + " (ca: " + key + "). Dung COD.");
                entry.material = Material.COD;
            }

            List<Double> weightRange = sec.getDoubleList("Weight_Range");
            entry.weightMin = weightRange.size() >= 1 ? weightRange.get(0) : 0.5;
            entry.weightMax = weightRange.size() >= 2 ? weightRange.get(1) : 5.0;
            if (entry.weightMin > entry.weightMax) entry.weightMax = entry.weightMin;

            List<Double> lengthRange = sec.getDoubleList("Length_Range");
            entry.lengthMin = lengthRange.size() >= 1 ? lengthRange.get(0) : 10.0;
            entry.lengthMax = lengthRange.size() >= 2 ? lengthRange.get(1) : 50.0;
            if (entry.lengthMin > entry.lengthMax) entry.lengthMax = entry.lengthMin;

            entry.pricePerKg      = sec.getDouble("Price_Per_Kg", 10.0);
            entry.timeRequired    = sec.getStringList("Time_Required");
            entry.biomes          = sec.getStringList("Biomes");
            entry.lavaOnly        = sec.getBoolean("Lava_Only", false);
            entry.waterOnly       = sec.getBoolean("Water_Only", false);
            entry.customModelData = Math.max(0, sec.getInt("Custom_Model_Data", 0));
            entry.commandsOnCatch = sec.getStringList("Commands_On_Catch");

            entries.add(entry);
        }
    }

    private String resolveTimePeriod(long worldTime) {
        long t = worldTime % 24000;
        return (t >= DAY_START && t <= DAY_END) ? "DAY" : "NIGHT";
    }

    /**
     * Kiem tra moi truong (nuoc / dung nham):
     *  - lavaOnly  = true : chi xuat hien khi cau dung nham
     *  - waterOnly = true : chi xuat hien khi cau nuoc
     *  - ca hai false     : xuat hien ca hai moi truong
     */
    private boolean matchesEnvironment(FishEntry entry, boolean isLava) {
        if (entry.lavaOnly && !isLava) return false;
        if (entry.waterOnly && isLava) return false;
        return true;
    }

    private boolean matchesBiome(FishEntry entry, Biome biome) {
        if (entry.biomes == null || entry.biomes.isEmpty()) return true;
        String biomeName = biome.name();
        for (String b : entry.biomes) {
            if (b.equalsIgnoreCase(biomeName)) return true;
        }
        return false;
    }

    private boolean matchesTime(FishEntry entry, String currentPeriod) {
        if (entry.timeRequired == null || entry.timeRequired.isEmpty()) return true;
        for (String t : entry.timeRequired) {
            if (t.equalsIgnoreCase(currentPeriod)) return true;
        }
        return false;
    }

    // ── Data class ────────────────────────────────────────────────────────────

    public static class FishEntry {
        public String key;
        public String displayName;
        public int rarity;
        public Material material;
        public double weightMin;
        public double weightMax;
        public double lengthMin;
        public double lengthMax;
        public double pricePerKg;
        /** true = chi xuat hien khi cau dung nham */
        public boolean lavaOnly = false;
        /** true = chi xuat hien khi cau nuoc */
        public boolean waterOnly = false;
        /**
         * CustomModelData gan len ItemMeta (>= 1 de ap dung, 0 = khong dung).
         * Dung voi resource pack de hien thi model 3D rieng cho tung loai ca.
         */
        public int customModelData = 0;
        public List<String> timeRequired = new ArrayList<>();
        public List<String> biomes = new ArrayList<>();
        public List<String> commandsOnCatch = new ArrayList<>();
    }
}
