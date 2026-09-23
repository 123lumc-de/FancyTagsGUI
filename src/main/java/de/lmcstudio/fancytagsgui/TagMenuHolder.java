package de.lmcstudio.fancytagsgui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class TagMenuHolder {

    public static final String MENU_TITLE = "§8» Tag Auswählen | Menu";

    public static void openMenu(Player player, FancyTagsGUI plugin) {
        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE);

        // Platzhalter-Suffixe (Hier könntest du dynamisch die Suffixe des Spielers aus LuckPerms laden)
        // Für das Beispiel nehmen wir zwei Suffixe an
        String[] availableSuffixes = {"§6[RIZZLER]", "§b[EPIC]"};

        // Slots für die Suffixe (z.B. Slot 10 und 12)
        int[] suffixSlots = {10, 12};

        for (int i = 0; i < availableSuffixes.length && i < suffixSlots.length; i++) {
            ItemStack item = createTagItem(availableSuffixes[i]);
            inv.setItem(suffixSlots[i], item);
        }

        // Das "Tag entfernen"-Item (Slot 49 ist die Mitte der unteren Reihe)
        ItemStack removeItem = createRemoveItem();
        inv.setItem(49, removeItem);

        // Inventar öffnen
        player.openInventory(inv);
    }

    private static ItemStack createTagItem(String suffix) {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(suffix);
            List<String> lore = Arrays.asList(
                    "§7Beschreibung",
                    "",
                    "§cInformation:",
                    "§fKlicke um einen Suffix hinter",
                    "§fdeinem Namen zu haben.",
                    "",
                    "§7Status: §aVerfügbar",
                    "§e▶ KLICKE §ezum Ausrüsten",
                    "§9Minecraft"
            );
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createRemoveItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cTAG ENTFERNEN");
            List<String> lore = Arrays.asList(
                    "§7Beschreibung",
                    "",
                    "§cInformation:",
                    "§fKlicke hier um deinen",
                    "§faktuellen Tag zu entfernen",
                    "",
                    "§e▶ KLICKE §ezum Entfernen",
                    "§9Minecraft"
            );
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
