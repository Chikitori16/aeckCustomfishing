package online.aeck.customfish;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
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
    private FishingQuest questManager;
    private FishCombatManager combatManager;

    public enum Season { XUAN, HA, THU, DONG }
    private Season currentSeason;

    public static final String PREFIX = "\u00a78[\u00a7bAECK.ONLINE\u00a78] \u00a7r";

    @Override
    public void onEnable() {

        saveDefaultConfig();
        createSpecialFishConfig();

        FISH_PRICE_KEY = new NamespacedKey(this, "aeck_fish_price");

        expManager   = new FishingExpManager(this);
        skillManager = new SkillTreeManager(this);
        rodManager   = new FishingRodManager(this);
        tournament   = new FishingTournament(this);
        mysql        = new MySQLManager(this);
        questManager = new FishingQuest();
        combatManager = new FishCombatManager(this);

        econBridge = EconomyBridge.create(this);
        if (!econBridge.isEnabled()) {
            getLogger().warning("Kh\u00f4ng t\u00ecm th\u1ea5y Vault Economy! Plugin v\u1eabn ch\u1ea1y nh\u01b0ng kh\u00f4ng c\u00f3 h\u1ec7 th\u1ed1ng ti\u1ec1n t\u1ec7.");
        } else {
            getLogger().info("Economy provider: " + econBridge.providerName());
        }

        currentSeason = Season.values()[new Random().nextInt(Season.values().length)];
        getLogger().info("M\u00f9a kh\u1edfi \u0111\u1ed9ng: " + currentSeason);

        // ----------------------------------------------------------------
        // EVENTS
        // ----------------------------------------------------------------

        // Listener câu cá → khởi động combat
        Bukkit.getPluginManager().registerEvents(
                new FishCatchListener(this, rodManager, combatManager), this
        );

        // Listener chiến đấu: click trái tấn công, block cast khi combat
        Bukkit.getPluginManager().registerEvents(
                new FishCombatListener(combatManager), this
        );

        FishSellGUI sellGUI = new FishSellGUI(this, econBridge);
        Bukkit.getPluginManager().registerEvents(sellGUI, this);

        // ----------------------------------------------------------------
        // COMMANDS
        // ----------------------------------------------------------------
        getCommand("banca").setExecutor(sellGUI);
        getCommand("muacauca").setExecutor(new SeasonCommand(this));
        getCommand("topcauca").setExecutor(new TopFishGUI(this));
        getCommand("fish").setExecutor(new FishCommand(this));
        getCommand("nangcapcan").setExecutor(new RodUpgradeCommand(this));
        getCommand("kinhnghiemcauca").setExecutor(new FishingExpCommand(expManager));
        getCommand("kynangcauca").setExecutor(
                new SkillTreeCommand(this, skillManager, expManager)
        );

        // ----------------------------------------------------------------
        // QUEST SYSTEM
        // ----------------------------------------------------------------
        QuestLoader loader = new QuestLoader(this);
        List<FishingQuest.Quest> quests = loader.loadQuests();
        FishingQuestGUI questGUI = new FishingQuestGUI(questManager, quests);
        Bukkit.getPluginManager().registerEvents(questGUI, this);

        getCommand("nhiemvucauca").setExecutor((sender, command, label, args) -> {
            if (sender instanceof org.bukkit.entity.Player p) {
                questGUI.open(p);
                return true;
            }
            sender.sendMessage("Ch\u1ec9 d\u00e0nh cho ng\u01b0\u1eddi ch\u01a1i.");
            return true;
        });

        getCommand("nvcauca").setExecutor((sender, command, label, args) -> {
            if (sender instanceof org.bukkit.entity.Player p) {
                questGUI.open(p);
                return true;
            }
            sender.sendMessage("Ch\u1ec9 d\u00e0nh cho ng\u01b0\u1eddi ch\u01a1i.");
            return true;
        });

        // Admin inventory GUI
        FishInventoryGUI fishInventoryGUI = new FishInventoryGUI(this);
        Bukkit.getPluginManager().registerEvents(fishInventoryGUI, this);
        getCommand("fishinventory").setExecutor(fishInventoryGUI);

        // ----------------------------------------------------------------
        // PLACEHOLDERAPI
        // ----------------------------------------------------------------
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new FishPlaceholders(this).register();
        }

        // ----------------------------------------------------------------
        // TOURNAMENT: tự động mỗi 2 giờ
        // ----------------------------------------------------------------
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (!tournament.isRunning()) tournament.start();
        }, 0L, 2 * 60 * 60 * 20L);

        // ----------------------------------------------------------------
        // SEASON TIMER: tự động đổi mùa mỗi 30 phút thực
        // ----------------------------------------------------------------
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            Season[] seasons = Season.values();
            int next = (Arrays.asList(seasons).indexOf(currentSeason) + 1) % seasons.length;
            currentSeason = seasons[next];
            Bukkit.broadcastMessage(PREFIX
                    + "\u00a7fM\u00f9a \u0111\u00e3 thay \u0111\u1ed5i! Hi\u1ec7n t\u1ea1i: \u00a7b\u00a7l" + currentSeason);
        }, 36000L, 36000L);

        getLogger().info("CustomFish \u0111\u00e3 kh\u1edfi \u0111\u1ed9ng th\u00e0nh c\u00f4ng!");
    }

    @Override
    public void onDisable() {
        if (combatManager != null) combatManager.removeAll();
        if (mysql != null) mysql.shutdown();
        getLogger().info("CustomFish \u0111\u00e3 t\u1eaft.");
    }

    private void createSpecialFishConfig() {
        File file = new File(getDataFolder(), "fishspecial.yml");
        if (!file.exists()) saveResource("fishspecial.yml", false);
        specialFishConfig = YamlConfiguration.loadConfiguration(file);
    }

    // Getters
    public MySQLManager getMySQL() { return mysql; }
    public FileConfiguration getSpecialFishConfig() { return specialFishConfig; }
    public Season getCurrentSeason() { return currentSeason; }
    public void setCurrentSeason(Season season) { this.currentSeason = season; }
    public FishingRodManager getRodManager() { return rodManager; }
    public FishingExpManager getExpManager() { return expManager; }
    public FishingTournament getTournament() { return tournament; }
    public EconomyBridge getEconomyBridge() { return econBridge; }
    public FishingQuest getQuestManager() { return questManager; }
    public FishCombatManager getCombatManager() { return combatManager; }
}
