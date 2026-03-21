package online.aeck.customfish;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestLoader {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public QuestLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {

        File file = new File(plugin.getDataFolder(), "quests.yml");

        if (!file.exists()) {
            plugin.saveResource("quests.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public List<FishingQuest.Quest> loadQuests() {

        List<FishingQuest.Quest> quests = new ArrayList<>();

        List<Map<?, ?>> questList = config.getMapList("quests");

        for (Map<?, ?> map : questList) {

            String name = (String) map.get("name");

            String description = (String) map.get("description");

            int targetFish =
                    map.get("targetFish") != null
                            ? Integer.parseInt(map.get("targetFish").toString())
                            : 10;

            int rewardExp =
                    map.get("rewardExp") != null
                            ? Integer.parseInt(map.get("rewardExp").toString())
                            : 50;

            String rewardItem =
                    map.get("rewardItem") != null
                            ? map.get("rewardItem").toString()
                            : "COD";

            FishingQuest.Quest quest =
                    new FishingQuest.Quest(
                            name,
                            description,
                            targetFish,
                            rewardExp,
                            rewardItem
                    );

            quests.add(quest);
        }

        return quests;
    }
}