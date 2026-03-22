package online.aeck.customfish;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private static PlayerDataManager instance;

    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    private PlayerDataManager() {}

    public static PlayerDataManager getInstance() {
        if (instance == null) instance = new PlayerDataManager();
        return instance;
    }

    public PlayerData get(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), id -> new PlayerData());
    }

    public void remove(Player player) {
        cache.remove(player.getUniqueId());
    }

    public static class PlayerData {

        public int exp = 0;
        public int level = 1;
        public int skillPoints = 0;
        public int masteryLevel = 0;
        public int powerLevel = 0;

        public int expToLevel() {
            return 100 + (level * 20);
        }
    }
}
