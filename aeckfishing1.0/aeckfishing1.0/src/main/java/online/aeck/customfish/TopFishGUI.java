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

import java.util.Arrays;
import java.util.List;

/**
 * TopFishGUI — bảng xếp hạng top 10 (54 ô).
 *
 * Layout (viền hồng, nội dung trong kính trắng):
 *  Slot 4  (hồng trên): Tiêu đề
 *  Slot 49 (hồng dưới): Hướng dẫn
 *  Top 10: slots 10-16 (hạng 1-7), slots 19-21 (hạng 8-10)
 */
public class TopFishGUI implements CommandExecutor, Listener {

    private static final String GUI_TITLE = "\u00a7d\u00a7lTop 10 Th\u1ee3 C\u00e2u AECK";

    private final CustomFish plugin;

    public TopFishGUI(CustomFish plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE);

        // ── Slot 4 (hồng trên): Tiêu đề ─────────────────────────────────────
        ItemStack title = new ItemStack(Material.FISHING_ROD);
        ItemMeta titleMeta = title.getItemMeta();
        titleMeta.setDisplayName("\u00a76\u00a7l\u272a Top 10 Th\u1ee3 C\u00e2u");
        titleMeta.setLore(Arrays.asList(
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                "\u00a77B\u1ea3ng x\u1ebfp h\u1ea1ng c\u00e1 n\u1eb7ng nh\u1ea5t",
                "\u00a77t\u1eeb tr\u01b0\u1edbc \u0111\u1ebfn nay.",
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"
        ));
        title.setItemMeta(titleMeta);
        inv.setItem(4, title);

        // ── Slot 49 (hồng dưới): Hướng dẫn ──────────────────────────────────
        ItemStack guide = new ItemStack(Material.BOOK);
        ItemMeta guideMeta = guide.getItemMeta();
        guideMeta.setDisplayName("\u00a77\u0110ang t\u1ea3i d\u1eef li\u1ec7u...");
        guide.setItemMeta(guideMeta);
        inv.setItem(49, guide);

        // ── Điền viền hồng & nền trắng ───────────────────────────────────────
        GuiUtil.fillBorder54(inv);

        player.openInventory(inv);

        // ── Load dữ liệu async ────────────────────────────────────────────────
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<MySQLManager.FishRecord> top = plugin.getMySQL().getTopFish(10);
            Bukkit.getScheduler().runTask(plugin, () -> {
                // Slot mapping: hạng 1-7 → 10-16, hạng 8-10 → 19-21
                int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
                String[] medals = {"\u00a76\u00a7l#1", "\u00a77\u00a7l#2", "\u00a76\u00a7l#3",
                        "\u00a7e#4", "\u00a7e#5", "\u00a7e#6", "\u00a7e#7",
                        "\u00a7e#8", "\u00a7e#9", "\u00a7e#10"};

                for (int i = 0; i < top.size() && i < slots.length; i++) {
                    MySQLManager.FishRecord r = top.get(i);
                    ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                    ItemMeta m = item.getItemMeta();
                    m.setDisplayName(medals[i] + " \u00a7f" + r.playerName);
                    m.setLore(Arrays.asList(
                            "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                            "\u00a77C\u00e1: \u00a7e" + r.fishType,
                            "\u00a77N\u1eb7ng: \u00a7c" + String.format("%.1f", r.weight) + " kg",
                            "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"
                    ));
                    item.setItemMeta(m);
                    inv.setItem(slots[i], item);
                }

                // Cập nhật slot 49 sau khi load xong
                ItemStack done = new ItemStack(Material.BOOK);
                ItemMeta doneMeta = done.getItemMeta();
                doneMeta.setDisplayName("\u00a77T\u1ed5ng: \u00a7e" + top.size() + " \u00a77ng\u01b0\u1eddi");
                done.setItemMeta(doneMeta);
                inv.setItem(49, done);
            });
        });

        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);
    }
}
