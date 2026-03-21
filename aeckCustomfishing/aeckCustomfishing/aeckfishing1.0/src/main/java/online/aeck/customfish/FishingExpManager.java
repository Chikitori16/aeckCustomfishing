package online.aeck.customfish;

import org.bukkit.entity.Player;

public class FishingExpManager {

    private final CustomFish plugin;

    public FishingExpManager(CustomFish plugin) {
        this.plugin = plugin;
    }

    public void addExp(Player player, int amount) {

        PlayerDataManager.PlayerData data =
                PlayerDataManager.getInstance().get(player);

        data.exp += amount;

        while (data.exp >= data.expToLevel()) {
            data.exp -= data.expToLevel();
            data.level++;
            data.skillPoints++;

            player.sendMessage(CustomFish.PREFIX +
                    "§aBạn đã lên cấp câu cá! Nhận 1 điểm kỹ năng.");
        }
    }

    public PlayerDataManager.PlayerData getExp(Player player) {
        return PlayerDataManager.getInstance().get(player);
    }
}