package online.aeck.customfish;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class GuiUtil {

    private GuiUtil() {}

    public static final int[] PINK_BORDER_54 = {
        0,1,2,3,4,5,6,7,8,
        9,17,
        18,26,
        27,35,
        36,44,
        45,46,47,48,49,50,51,52,53
    };

    public static final int[] WHITE_INNER_54 = {
        10,11,12,13,14,15,16,
        19,20,21,22,23,24,25,
        28,29,30,31,32,33,34,
        37,38,39,40,41,42,43
    };

    public static final int[] PINK_BORDER_27 = {
        0,1,2,3,4,5,6,7,8,
        9,17,
        18,19,20,21,22,23,24,25,26
    };

    public static final int[] WHITE_INNER_27 = {
        10,11,12,13,14,15,16
    };

    public static ItemStack pinkGlass() {
        return glass(Material.PINK_STAINED_GLASS_PANE);
    }

    public static ItemStack whiteGlass() {
        return glass(Material.WHITE_STAINED_GLASS_PANE);
    }

    private static ItemStack glass(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    public static void fillBorder54(Inventory inv) {
        ItemStack pink = pinkGlass();
        ItemStack white = whiteGlass();
        for (int slot : PINK_BORDER_54) {
            if (inv.getItem(slot) == null) inv.setItem(slot, pink);
        }
        for (int slot : WHITE_INNER_54) {
            if (inv.getItem(slot) == null) inv.setItem(slot, white);
        }
    }

    public static void fillBorder27(Inventory inv) {
        ItemStack pink = pinkGlass();
        ItemStack white = whiteGlass();
        for (int slot : PINK_BORDER_27) {
            if (inv.getItem(slot) == null) inv.setItem(slot, pink);
        }
        for (int slot : WHITE_INNER_27) {
            if (inv.getItem(slot) == null) inv.setItem(slot, white);
        }
    }
}
