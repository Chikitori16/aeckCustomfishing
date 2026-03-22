package online.aeck.customfish;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Map;

public class FishingTournament {
    private final CustomFish plugin;
    private boolean running = false;
    private long endTime = 0;
    private final Map<String, Double> playerWeights = new HashMap<>();

    public FishingTournament(CustomFish plugin) { this.plugin = plugin; }

    public void start() {
        running = true;
        endTime = System.currentTimeMillis() + 10 * 60 * 1000;
        playerWeights.clear();
        Bukkit.broadcastMessage("§6[AECK] Giải đấu câu cá bắt đầu! Ai câu được cá nặng nhất trong 10 phút sẽ thắng!");
        new BukkitRunnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() >= endTime) {
                    running = false;
                    announceWinner();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    public void recordCatch(Player player, double weight) {
        if (!running) return;
        playerWeights.put(player.getName(), Math.max(playerWeights.getOrDefault(player.getName(), 0.0), weight));
    }

    private void announceWinner() {
        String winner = null;
        double max = 0;
        for (Map.Entry<String, Double> e : playerWeights.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                winner = e.getKey();
            }
        }
        if (winner != null) {
            Bukkit.broadcastMessage("§aNgười thắng giải đấu câu cá: §e" + winner + " §b(cá nặng: " + String.format("%.1f", max) + "kg)");
        } else {
            Bukkit.broadcastMessage("§cKhông ai thắng giải đấu!");
        }
    }

    public boolean isRunning() { return running; }
}
