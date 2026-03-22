package online.aeck.customfish;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

/**
 * FishSellGUI — bán cá (27 ô, 3 hàng).
 *
 * Layout (viền hồng, nội dung trong kính trắng hàng 2):
 *  Slot 11 (trắng): Thông tin về việc bán
 *  Slot 13 (trắng): Nút XÁC NHẬN BÁN TẤT CẢ
 *  Slot 15 (trắng): Thống kê cá trong túi
 */
public class FishSellGUI implements CommandExecutor, Listener {

    private final CustomFish plugin;
    private final EconomyBridge econ;
    private static final String GUI_TITLE = "\u00a7d\u00a7lB\u00e1n C\u00e1 - AECK";

    public FishSellGUI(CustomFish plugin, EconomyBridge econ) {
        this.plugin = plugin;
        this.econ = econ;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        openGUI(player);
        return true;
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);

        // ── Slot 11 (trắng trái): Thông tin ─────────────────────────────────
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("\u00a7e\u00a7lTh\u00f4ng Tin B\u00e1n");
        infoMeta.setLore(Arrays.asList(
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                "\u00a77S\u1ebd b\u00e1n to\u00e0n b\u1ed9 c\u00e1",
                "\u00a77trong t\u00fai \u0111\u1ed3 c\u1ee7a b\u1ea1n.",
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                "\u00a77Ch\u1ec9 c\u00e1 c\u00f3 gi\u00e1 tr\u1ecb",
                "\u00a77m\u1edbi b\u00e1n \u0111\u01b0\u1ee3c."
        ));
        info.setItemMeta(infoMeta);
        inv.setItem(11, info);

        // ── Slot 13 (trắng giữa): Nút bán ───────────────────────────────────
        ItemStack sellButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta sellMeta = sellButton.getItemMeta();
        sellMeta.setDisplayName("\u00a7a\u00a7lX\u00c1C NH\u1eacN B\u00c1N T\u1ea4T C\u1ea2");
        sellMeta.setLore(Arrays.asList(
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                "\u00a77Nh\u1ea5n \u0111\u1ec3 b\u00e1n to\u00e0n b\u1ed9 c\u00e1",
                "\u00a77trong t\u00fai \u0111\u1ed3 ng\u01b0\u1eddi ch\u01a1i.",
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"
        ));
        sellButton.setItemMeta(sellMeta);
        inv.setItem(13, sellButton);

        // ── Slot 15 (trắng phải): Thống kê ──────────────────────────────────
        int fishCount = countFish(player);
        ItemStack stats = new ItemStack(Material.TROPICAL_FISH);
        ItemMeta statsMeta = stats.getItemMeta();
        statsMeta.setDisplayName("\u00a7b\u00a7lTh\u1ed1ng K\u00ea");
        statsMeta.setLore(Arrays.asList(
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                "\u00a77S\u1ed1 c\u00e1 trong t\u00fai: \u00a7e" + fishCount,
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"
        ));
        stats.setItemMeta(statsMeta);
        inv.setItem(15, stats);

        // ── Điền viền hồng & nền trắng ──────────────────────────────────────
        GuiUtil.fillBorder27(inv);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null
                || event.getCurrentItem().getType() == Material.AIR) return;

        if (event.getCurrentItem().getType() == Material.EMERALD_BLOCK) {
            sellAllFish((Player) event.getWhoClicked());
        }
    }

    private void sellAllFish(Player player) {
        double totalMoney = 0;
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || !item.hasItemMeta()) continue;
            Double price = item.getItemMeta().getPersistentDataContainer()
                    .get(CustomFish.FISH_PRICE_KEY, PersistentDataType.DOUBLE);
            if (price != null) {
                totalMoney += price * item.getAmount();
                player.getInventory().setItem(i, null);
            }
        }

        if (totalMoney > 0) {
            boolean ok = econ.deposit(player, totalMoney);
            if (ok) {
                player.sendMessage(CustomFish.PREFIX
                        + "\u00a7aB\u1ea1n \u0111\u00e3 b\u00e1n m\u1edb c\u00e1 v\u00e0 thu v\u1ec1 \u00a7e$"
                        + String.format("%.0f", totalMoney));
            } else {
                player.sendMessage(CustomFish.PREFIX
                        + "\u00a7cKh\u00f4ng th\u1ec3 c\u1ed9ng ti\u1ec1n (Economy ch\u01b0a s\u1eb5n s\u00e0ng).");
            }
        } else {
            player.sendMessage(CustomFish.PREFIX
                    + "\u00a7cB\u1ea1n kh\u00f4ng c\u00f3 c\u00e1 \u0111\u1ec3 b\u00e1n!");
        }
        player.closeInventory();
    }

    private int countFish(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !item.hasItemMeta()) continue;
            Double price = item.getItemMeta().getPersistentDataContainer()
                    .get(CustomFish.FISH_PRICE_KEY, PersistentDataType.DOUBLE);
            if (price != null) count += item.getAmount();
        }
        return count;
    }
}
