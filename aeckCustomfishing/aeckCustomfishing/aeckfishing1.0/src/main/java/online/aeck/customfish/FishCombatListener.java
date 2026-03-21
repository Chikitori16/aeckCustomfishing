package online.aeck.customfish;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FishCombatListener — xử lý input chiến đấu với cá:
 *
 * - Chuột Trái (arm swing) khi cầm cần câu → tấn công cá (1 lượt)
 * - Chuột Phải khi cầm cần câu & đang chiến đấu → chặn, hiển thị hướng dẫn
 * - FISHING event khi đang chiến đấu → chặn (không cho quăng mới)
 * - Player thoát server → kết thúc trận, cá thoát
 */
public class FishCombatListener implements Listener {

    private final FishCombatManager combatManager;

    /** Cooldown tấn công: 800ms giữa các lượt */
    private static final long ATTACK_COOLDOWN_MS = 800;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public FishCombatListener(FishCombatManager combatManager) {
        this.combatManager = combatManager;
    }

    /**
     * Chuột Trái (arm swing) khi cầm cần câu → 1 lượt tấn công.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onSwing(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;

        Player player = event.getPlayer();
        if (!combatManager.isInCombat(player)) return;

        // Phải cầm cần câu
        Material hand = player.getInventory().getItemInMainHand().getType();
        if (hand != Material.FISHING_ROD) return;

        // Kiểm tra cooldown
        long now = System.currentTimeMillis();
        long last = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long remaining = ATTACK_COOLDOWN_MS - (now - last);
        if (remaining > 0) {
            player.sendActionBar("\u00a7cCh\u1edd \u00a7e" + String.format("%.1f", remaining / 1000.0) + "s...");
            return;
        }
        cooldowns.put(player.getUniqueId(), now);

        // Tấn công
        combatManager.attack(player);
    }

    /**
     * Chuột Phải (right-click) khi đang chiến đấu → chặn quăng cần.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!combatManager.isInCombat(player)) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Material hand = player.getInventory().getItemInMainHand().getType();
        if (hand != Material.FISHING_ROD) return;

        event.setCancelled(true);
        player.sendActionBar("\u00a7c\u2620 \u0110ang chi\u1ebfn \u0111\u1ea5u v\u1edbi c\u00e1! \u00a7eChu\u1ed9t Tr\u00e1i \u0111\u1ec3 t\u1ea5n c\u00f4ng.");
    }

    /**
     * Ngăn quăng cần mới (PlayerFishEvent.FISHING) khi đang chiến đấu.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onFishing(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.FISHING) return;
        if (!combatManager.isInCombat(event.getPlayer())) return;

        event.setCancelled(true);
        event.getPlayer().sendActionBar(
                "\u00a7c\u2620 \u0110ang chi\u1ebfn \u0111\u1ea5u v\u1edbi c\u00e1! \u00a7eChu\u1ed9t Tr\u00e1i \u0111\u1ec3 t\u1ea5n c\u00f4ng.");
    }

    /**
     * Người chơi thoát → kết thúc trận, cá thoát.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (combatManager.isInCombat(player)) {
            combatManager.endCombat(player, false);
        }
        cooldowns.remove(player.getUniqueId());
    }
}
