package online.aeck.customfish;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FishPlaceholders extends PlaceholderExpansion {

    private final CustomFish plugin;

    public FishPlaceholders(CustomFish plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "customfish";
    }

    @Override
    public @NotNull String getAuthor() {
        return "AECK";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {

        if (params.equalsIgnoreCase("season")) {
            return switch (plugin.getCurrentSeason()) {
                case XUAN -> "§aXuân";
                case HA -> "§eHạ";
                case THU -> "§6Thu";
                case DONG -> "§bĐông";
            };
        }

        Player online = player.getPlayer();
        if (online == null) return "0";

        PlayerDataManager.PlayerData data =
                PlayerDataManager.getInstance().get(online);

        if (data == null) return "0";

        if (params.equalsIgnoreCase("exp")) {
            return String.valueOf(data.exp);
        }

        if (params.equalsIgnoreCase("level")) {
            return String.valueOf(data.level);
        }

        if (params.equalsIgnoreCase("skill")) {
            return String.valueOf(data.skillPoints);
        }

        return null;
    }
}