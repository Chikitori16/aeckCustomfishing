package online.aeck.customfish;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class FishingRodManager {

    private final CustomFish plugin;

    private FileConfiguration rodConfig;

    private final Map<String, FishingRod> rods = new HashMap<>();
    private final Map<String, Line> lines = new HashMap<>();
    private final Map<String, Hook> hooks = new HashMap<>();
    private final Map<String, Bait> baits = new HashMap<>();

    public FishingRodManager(CustomFish plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {

        File file = new File(plugin.getDataFolder(), "fishingrod.yml");

        if (!file.exists())
            plugin.saveResource("fishingrod.yml", false);

        rodConfig = YamlConfiguration.loadConfiguration(file);

        rods.clear();
        lines.clear();
        hooks.clear();
        baits.clear();

        loadRods();
        loadLines();
        loadHooks();
        loadBaits();
    }

    private void loadRods() {

        List<Map<?, ?>> rodList = rodConfig.getMapList("rods");

        for (Map<?, ?> map : rodList) {

            FishingRod rod = FishingRod.fromMap(map);

            rods.put(rod.id, rod);
        }
    }

    private void loadLines() {

        List<Map<?, ?>> lineList = rodConfig.getMapList("lines");

        for (Map<?, ?> map : lineList) {

            Line line = Line.fromMap(map);

            lines.put(line.id, line);
        }
    }

    private void loadHooks() {

        List<Map<?, ?>> hookList = rodConfig.getMapList("hooks");

        for (Map<?, ?> map : hookList) {

            Hook hook = Hook.fromMap(map);

            hooks.put(hook.id, hook);
        }
    }

    private void loadBaits() {

        List<Map<?, ?>> baitList = rodConfig.getMapList("baits");

        for (Map<?, ?> map : baitList) {

            Bait bait = Bait.fromMap(map);

            baits.put(bait.id, bait);
        }
    }

    public FishingRod getRod(String id) {
        return rods.get(id);
    }

    public Line getLine(String id) {
        return lines.get(id);
    }

    public Hook getHook(String id) {
        return hooks.get(id);
    }

    public Bait getBait(String id) {
        return baits.get(id);
    }

    public static class FishingRod {

        public String id, displayName, line, hook, bait;

        public double damage, speed, critChance, critDamage;

        public static FishingRod fromMap(Map<?, ?> map) {

            FishingRod r = new FishingRod();

            r.id = (String) map.get("id");

            r.displayName = (String) map.get("display-name");

            r.damage = map.get("damage") != null
                    ? Double.parseDouble(map.get("damage").toString())
                    : 10;

            r.speed = map.get("speed") != null
                    ? Double.parseDouble(map.get("speed").toString())
                    : 1.0;

            r.critChance = map.get("crit-chance") != null
                    ? Double.parseDouble(map.get("crit-chance").toString())
                    : 0.05;

            r.critDamage = map.get("crit-damage") != null
                    ? Double.parseDouble(map.get("crit-damage").toString())
                    : 2.0;

            r.line = (String) map.get("line");
            r.hook = (String) map.get("hook");
            r.bait = (String) map.get("bait");

            return r;
        }
    }

    public static class Line {

        public String id;

        public double maxWeight;

        public static Line fromMap(Map<?, ?> map) {

            Line l = new Line();

            l.id = (String) map.get("id");

            l.maxWeight = map.get("max-weight") != null
                    ? Double.parseDouble(map.get("max-weight").toString())
                    : 50;

            return l;
        }
    }

    public static class Hook {

        public String id;

        public List<String> fishTypes = new ArrayList<>();

        public static Hook fromMap(Map<?, ?> map) {

            Hook h = new Hook();

            h.id = (String) map.get("id");

            Object obj = map.get("fish-types");

            if (obj instanceof List<?>) {

                for (Object o : (List<?>) obj) {

                    h.fishTypes.add(o.toString());
                }
            }

            return h;
        }
    }

    public static class Bait {

        public String id, effect;

        public static Bait fromMap(Map<?, ?> map) {

            Bait b = new Bait();

            b.id = (String) map.get("id");

            b.effect = (String) map.get("effect");

            return b;
        }
    }
}