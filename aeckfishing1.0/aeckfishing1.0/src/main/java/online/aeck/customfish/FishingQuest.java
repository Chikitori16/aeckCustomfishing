package online.aeck.customfish;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FishingQuest {

    private static final String[] QUALITY_ORDER = {"common", "rare", "epic", "legendary", "mythic", "godly"};

    public static class Quest {
        public String name;
        public String description;
        public int targetFish;
        public int progress;
        public boolean completed;
        public int rewardExp;
        public String rewardItem;
        /**
         * minQuality: chỉ tính khi cá câu được có phẩm chất >= minQuality.
         * null hoặc "common" = chấp nhận mọi cá.
         */
        public String minQuality;

        public Quest(String name, String description, int targetFish, int rewardExp, String rewardItem, String minQuality) {
            this.name = name;
            this.description = description;
            this.targetFish = targetFish;
            this.progress = 0;
            this.completed = false;
            this.rewardExp = rewardExp;
            this.rewardItem = rewardItem;
            this.minQuality = (minQuality != null) ? minQuality.toLowerCase() : "common";
        }
    }

    private final Map<UUID, Quest> activeQuests = new HashMap<>();

    public void assignQuest(Player player, Quest quest) {
        Quest copy = new Quest(quest.name, quest.description, quest.targetFish,
                quest.rewardExp, quest.rewardItem, quest.minQuality);
        activeQuests.put(player.getUniqueId(), copy);
    }

    public Quest getQuest(Player player) {
        return activeQuests.get(player.getUniqueId());
    }

    /**
     * Thêm tiến độ nhiệm vụ. Trả về true nếu nhiệm vụ vừa hoàn thành.
     * @param player người chơi
     * @param quality phẩm chất cá vừa câu được
     */
    public boolean addProgress(Player player, String quality) {
        Quest quest = getQuest(player);
        if (quest == null || quest.completed) return false;

        if (!meetsMinQuality(quality, quest.minQuality)) return false;

        quest.progress++;
        if (quest.progress >= quest.targetFish) {
            quest.completed = true;
            return true;
        }
        return false;
    }

    public boolean claimReward(Player player) {
        Quest quest = getQuest(player);
        if (quest == null || !quest.completed) return false;

        PlayerDataManager.PlayerData data = PlayerDataManager.getInstance().get(player);
        data.exp += quest.rewardExp;
        player.sendMessage("\u00a7bNh\u1eadn " + quest.rewardExp + " kinh nghi\u1ec7m c\u00e2u c\u00e1!");

        org.bukkit.Material mat = org.bukkit.Material.matchMaterial(quest.rewardItem);
        if (mat != null) {
            org.bukkit.inventory.ItemStack reward = new org.bukkit.inventory.ItemStack(mat);
            player.getInventory().addItem(reward);
            player.sendMessage("\u00a7bNh\u1eadn v\u1eadt ph\u1ea9m: \u00a7e" + mat.name());
        }

        activeQuests.remove(player.getUniqueId());
        return true;
    }

    public void removeQuest(Player player) {
        activeQuests.remove(player.getUniqueId());
    }

    /** So sánh phẩm chất: quality >= minQuality */
    public static boolean meetsMinQuality(String quality, String minQuality) {
        if (minQuality == null || minQuality.equalsIgnoreCase("common")) return true;
        int qIdx = qualityIndex(quality);
        int mIdx = qualityIndex(minQuality);
        return qIdx >= mIdx;
    }

    public static int qualityIndex(String quality) {
        if (quality == null) return 0;
        for (int i = 0; i < QUALITY_ORDER.length; i++) {
            if (QUALITY_ORDER[i].equalsIgnoreCase(quality)) return i;
        }
        return 0;
    }
}
