package online.aeck.customfish;

import org.bukkit.entity.Player;
import java.util.List;

public class SkillTreeManager {

    private final CustomFish plugin;

    public SkillTreeManager(CustomFish plugin) {
        this.plugin = plugin;
    }

    public PlayerDataManager.PlayerData getSkill(Player player) {
        return PlayerDataManager.getInstance().get(player);
    }

    public boolean upgradeMastery(Player player) {

        PlayerDataManager.PlayerData data =
                PlayerDataManager.getInstance().get(player);

        List<Integer> costList =
                plugin.getConfig().getIntegerList("skilltree.mastery-cost");

        int required = (data.masteryLevel < costList.size())
                ? costList.get(data.masteryLevel)
                : 9999;

        if (data.masteryLevel >= 5) return false;
        if (data.skillPoints < required) return false;

        data.skillPoints -= required;
        data.masteryLevel++;

        return true;
    }

    public boolean upgradePower(Player player) {

        PlayerDataManager.PlayerData data =
                PlayerDataManager.getInstance().get(player);

        List<Integer> costList =
                plugin.getConfig().getIntegerList("skilltree.power-cost");

        int required = (data.powerLevel < costList.size())
                ? costList.get(data.powerLevel)
                : 9999;

        if (data.powerLevel >= 5) return false;
        if (data.skillPoints < required) return false;

        data.skillPoints -= required;
        data.powerLevel++;

        return true;
    }
}

