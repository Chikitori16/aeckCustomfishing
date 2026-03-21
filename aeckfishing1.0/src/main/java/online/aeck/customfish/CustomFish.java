package online.aeck.customfish;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Random;

public class CustomFish extends JavaPlugin {

    private SkillTreeManager skillManager;
    private FishingTournament tournament;
    private FishingExpManager expManager;

    public static NamespacedKey FISH_PRICE_KEY;

    private FileConfiguration specialFishConfig;
    private MySQLManager mysql;
    private FishingRodManager rodManager;
    private EconomyBridge econBridge;

    public enum Season { XUAN, HA, THU, DONG }

    private Season currentSeason;

    public static final String PREFIX = "§8[§bAECK.ONLINE§8] §r";

    @Override
    public void onEnable() {

        saveDefaultConfig();
        createSpecialFishConfig();

        FISH_PRICE_KEY = new NamespacedKey(this, "aeck_fish_price");

        expManager = new FishingExpManager(this);
        skillManager = new SkillTreeManager(this);
        rodManager = new FishingRodManager(this);
        tournament = new FishingTournament(this);
        mysql = new MySQLManager(this);

        econBridge = EconomyBridge.create(this);

        if (!econBridge.isEnabled()) {
            getLogger().severe("Không tìm thấy Vault Economy!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Economy provider: " + econBridge.providerName());

        currentSeason = Season.values()[new Random().nextInt(Season.values().length)];

        /* EVENTS */

        Bukkit.getPluginManager().registerEvents(
                new FishCatchListener(this, rodManager), this
        );

        FishSellGUI sellGUI = new FishSellGUI(this, econBridge);
        Bukkit.getPluginManager().registerEvents(sellGUI, this);

        /* COMMANDS */

        getCommand("banca").setExecutor(sellGUI);
        getCommand("muacauca").setExecutor(new SeasonCommand(this));
        getCommand("topcauca").setExecutor(new TopFishGUI(this));
        getCommand("fish").setExecutor(new FishCommand(this));
        getCommand("nangcapcan").setExecutor(new RodUpgradeCommand(this));
        getCommand("kinhnghiemcauca").setExecutor(new FishingExpCommand(expManager));

        getCommand("kynangcauca").setExecutor(
                new SkillTreeCommand(this, skillManager, expManager)
        );

        /* QUEST SYSTEM */

        QuestLoader loader = new QuestLoader(this);
        List<FishingQuest.Quest> quests = loader.loadQuests();

        FishingQuest questManager = new FishingQuest();
        FishingQuestGUI questGUI = new FishingQuestGUI(questManager, quests);

        Bukkit.getPluginManager().registerEvents(questGUI, this);

        getCommand("nhiemvucauca").setExecutor((sender, command, label, args) -> {

            if (sender instanceof org.bukkit.entity.Player p) {
                questGUI.open(p);
                return true;
            }

            sender.sendMessage("Chỉ dành cho người chơi.");
            return true;
        });

        /* PLACEHOLDERAPI */

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new FishPlaceholders(this).register();
        }

        /* TOURNAMENT */

        Bukkit.getScheduler().runTaskTimer(this, () -> {

            if (!tournament.isRunning()) {
                tournament.start();
            }

        }, 0L, 2 * 60 * 60 * 20L);
    }

    private void createSpecialFishConfig() {

        File file = new File(getDataFolder(), "fishspecial.yml");

        if (!file.exists()) {
            saveResource("fishspecial.yml", false);
        }

        specialFishConfig = YamlConfiguration.loadConfiguration(file);
    }

    public MySQLManager getMySQL() {
        return mysql;
    }

    public FileConfiguration getSpecialFishConfig() {
        return specialFishConfig;
    }

    public Season getCurrentSeason() {
        return currentSeason;
    }

    public FishingRodManager getRodManager() {
        return rodManager;
    }

    public FishingExpManager getExpManager() {
        return expManager;
    }

    public FishingTournament getTournament() {
        return tournament;
    }

    public EconomyBridge getEconomyBridge() {
        return econBridge;
    }
    public void setCurrentSeason(Season season) {
        this.currentSeason = season;
    }
}