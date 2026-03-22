package online.aeck.customfish;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.Bukkit;
import java.lang.reflect.Method;
import net.milkbowl.vault.economy.Economy;

/**
 * EconomyBridge: abstraction over economy providers.
 * - Nếu Vault tồn tại, dùng Vault.
 * - Nếu không, cố gắng tìm plugin khác (ví dụ CoinEngine) và dùng reflection
 *   để gọi một trong các phương thức phổ biến (depositPlayer, addCoins, addBalance, giveCoins...).
 */
public interface EconomyBridge {
    boolean deposit(Player player, double amount);
    boolean isEnabled();
    String providerName();

    // Factory
    static EconomyBridge create(org.bukkit.plugin.java.JavaPlugin plugin) {
        // 1) Try Vault
        Plugin vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");
        if (vaultPlugin != null) {
            RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null && rsp.getProvider() != null) {
                Economy ec = rsp.getProvider();
                return new VaultEconomyBridge(ec);
            }
        }

        // 2) Try to find any plugin that may implement economy methods (fallback reflection)
        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            if (p == null) continue;
            // skip Vault since we already tried
            if ("Vault".equalsIgnoreCase(p.getName())) continue;

            // attempt to find a suitable method on plugin instance/class
            Method found = null;
            boolean takesPlayer = false;
            for (String methodName : new String[] {"depositPlayer","addBalance","addCoins","giveCoins","addToBalance","addMoney"}) {
                try {
                    // try (Player, double)
                    found = p.getClass().getMethod(methodName, Player.class, double.class);
                    takesPlayer = true;
                    break;
                } catch (NoSuchMethodException ignored) {}
                try {
                    // try (String, double)
                    found = p.getClass().getMethod(methodName, String.class, double.class);
                    takesPlayer = false;
                    break;
                } catch (NoSuchMethodException ignored) {}
            }
            if (found != null) {
                return new ReflectionEconomyBridge(p, found, takesPlayer);
            }
        }

        // 3) nothing found
        return new DisabledEconomyBridge();
    }
}

// Vault implementation
class VaultEconomyBridge implements EconomyBridge {
    private final Economy econ;
    VaultEconomyBridge(Economy econ) { this.econ = econ; }

    @Override
    public boolean deposit(org.bukkit.entity.Player player, double amount) {
        try {
            econ.depositPlayer(player, amount);
            return true;
        } catch (Exception e) {
            pluginLogger("Vault deposit failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isEnabled() { return econ != null; }

    @Override
    public String providerName() { return "Vault"; }

    private void pluginLogger(String msg) { Bukkit.getLogger().warning("[CustomFish][EconomyBridge] " + msg); }
}

// Reflection fallback implementation
class ReflectionEconomyBridge implements EconomyBridge {
    private final Plugin provider;
    private final Method method;
    private final boolean takesPlayer;

    ReflectionEconomyBridge(Plugin provider, Method method, boolean takesPlayer) {
        this.provider = provider;
        this.method = method;
        this.takesPlayer = takesPlayer;
        this.method.setAccessible(true);
    }

    @Override
    public boolean deposit(org.bukkit.entity.Player player, double amount) {
        try {
            if (takesPlayer) {
                method.invoke(provider, player, amount);
            } else {
                method.invoke(provider, player.getName(), amount);
            }
            return true;
        } catch (Exception e) {
            pluginLogger("Reflection deposit failed for plugin " + provider.getName() + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isEnabled() { return provider != null && method != null; }

    @Override
    public String providerName() { return provider.getName(); }

    private void pluginLogger(String msg) { Bukkit.getLogger().warning("[CustomFish][EconomyBridge] " + msg); }
}

// Disabled implementation
class DisabledEconomyBridge implements EconomyBridge {
    @Override public boolean deposit(org.bukkit.entity.Player player, double amount) { return false; }
    @Override public boolean isEnabled() { return false; }
    @Override public String providerName() { return "NONE"; }
}