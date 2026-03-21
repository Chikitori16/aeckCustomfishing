package online.aeck.customfish;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FishingQuest {
    public static class Quest {
        public String name;
        public String description;
        public int targetFish;
        public int progress;
        public boolean completed;
        public int rewardExp;
        public String rewardItem;

        public Quest(String name, String description, int targetFish, int rewardExp, String rewardItem) {
            this.name = name;
            this.description = description;
            this.targetFish = targetFish;
            this.progress = 0;
            this.completed = false;
            this.rewardExp = rewardExp;
            this.rewardItem = rewardItem;
        }
    }

    private final Map<UUID, Quest> activeQuests = new HashMap<>();

    public void assignQuest(Player player, Quest quest) {
        activeQuests.put(player.getUniqueId(), quest);
    }

    public Quest getQuest(Player player) {
        return activeQuests.get(player.getUniqueId());
    }

    public void addProgress(Player player) {
        Quest quest = getQuest(player);
        if (quest != null && !quest.completed) {
            quest.progress++;
            if (quest.progress >= quest.targetFish) {
                quest.completed = true;
            }
        }
    }

    public boolean claimReward(Player player) {
        Quest quest = getQuest(player);
        if (quest != null && quest.completed) {
            // Thưởng exp
            PlayerDataManager.PlayerData data = PlayerDataManager.getInstance().get(player);
            data.exp += quest.rewardExp;
            player.sendMessage("§bNhận " + quest.rewardExp + " kinh nghiệm câu cá!");
            // Thưởng item
            org.bukkit.Material mat = org.bukkit.Material.matchMaterial(quest.rewardItem);
            if (mat != null) {
                org.bukkit.inventory.ItemStack reward = new org.bukkit.inventory.ItemStack(mat);
                player.getInventory().addItem(reward);
                player.sendMessage("§bNhận vật phẩm: " + mat.name());
            }
            quest.completed = false;
            return true;
        }
        return false;
    }
}
