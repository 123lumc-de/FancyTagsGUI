package de.lmcstudio.fancytagsgui;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.SuffixNode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TagMenuHolder {

    public static final String MENU_TITLE = "§8» Tag Auswählen | Menu";

    public static void openMenu(Player player, FancyTagsGUI plugin) {
        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE);

        // Glasscheiben als Platzhalter für das Menü
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glassPane.setItemMeta(glassMeta);
        }
        // Füllt das Inventar mit Glasscheiben
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, glassPane);
        }

        // --- SUFFIXE AUS LUCKPERMS AUSLESEN ---
        LuckPerms luckPerms = plugin.getLuckPerms();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());

        List<String> suffixValues = new ArrayList<>();

        if (user != null) {
            // Alle Suffix-Nodes des Users auslesen
            for (Node node : user.getNodes()) {
                if (node instanceof SuffixNode suffixNode) {
                    suffixValues.add(suffixNode.getSuffix());
                }
            }
        }

        // Falls keine Suffixe vorhanden sind, füge einen Platzhalter hinzu
        if (suffixValues.isEmpty()) {
            suffixValues.add("§7Keine Suffixe verfügbar");
        }

        // Slots für die Suffixe (links oben, wie im Screenshot)
        int[] suffixSlots = {10, 11, 12, 13, 14, 15, 16};

        for (int i = 0; i < suffixValues.size() && i < suffixSlots.length; i++) {
            ItemStack item = createTagItem(suffixValues.get(i));
            inv.setItem(suffixSlots[i], item);
        }

        // Das "Tag entfernen"-Item (Barrier mit rotem Symbol, wie im Screenshot)
        ItemStack removeItem = createRemoveItem();
        inv.setItem(49, removeItem); // Slot in der Mitte unten

        player.openInventory(inv);
    }

    private static ItemStack createTagItem(String suffix) {
        ItemStack item = new ItemStack(Material.NAME_TAG); // NameTag als Icon für Suffix
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
