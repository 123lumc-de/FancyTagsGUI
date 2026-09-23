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
import java.util.Comparator;
import java.util.List;

public class TagMenuHolder {

    public static final String MENU_TITLE = "§8» Tag Auswählen | Menu";

    public static void openMenu(Player player, FancyTagsGUI plugin) {
        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE);

        // --- GLASSCHEIBEN ALS PLATZHALTER ---
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glassPane.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, glassPane);
        }

        // --- ALLE SUFFIXE AUS LUCKPERMS AUSLESEN ---
        LuckPerms luckPerms = plugin.getLuckPerms();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());

        List<SuffixNode> allSuffixes = new ArrayList<>();

        if (user != null) {
            // Durchlaufe ALLE Nodes (inklusive vererbter) und filtere SuffixNodes
            for (Node node : user.getNodes()) {
                if (node instanceof SuffixNode suffixNode) {
                    allSuffixes.add(suffixNode);
                }
            }
        }

        // Sortierung: Höchste Weight zuerst
        allSuffixes.sort(Comparator.comparingInt(SuffixNode::getPriority).reversed());

        // Slots für die Suffixe (links oben, wie im Screenshot)
        int[] suffixSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

        for (int i = 0; i < allSuffixes.size() && i < suffixSlots.length; i++) {
            SuffixNode node = allSuffixes.get(i);
            // KORREKT: getSuffix() existiert nicht, wir nutzen die MetaData-API
            // oder speichern den Wert direkt beim Erstellen des Nodes
            ItemStack item = createTagItem("§7Suffix #" + (i + 1), node.getPriority());
            inv.setItem(suffixSlots[i], item);
        }

        // --- INFO-ITEM: "DEINE TAGS" (Slot 4, oben Mitte) ---
        ItemStack infoItem = createInfoItem(player, allSuffixes);
        inv.setItem(4, infoItem);

        // --- "TAG ENTFERNEN"-ITEM (Slot 49, Mitte unten) ---
        ItemStack removeItem = createRemoveItem();
        inv.setItem(49, removeItem);

        player.openInventory(inv);
    }

    private static ItemStack createTagItem(String suffix, int weight) {
        ItemStack item = new ItemStack(Material.NAME_TAG);
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
                    "§7Gewichtung: §e" + weight,
                    "§7Status: §aVerfügbar",
                    "§e▶ KLICKE §ezum Ausrüsten",
                    "§9Minecraft"
            );
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createInfoItem(Player player, List<SuffixNode> suffixNodes) {
        ItemStack item = new ItemStack(Material.PURPLE_SHULKER_BOX);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bDEINE TAGS");
            List<String> lore = Arrays.asList(
                    "§7Beschreibung",
                    "",
                    "§9Information:",
                    "§fHier kannst du sehen, welchen",
                    "§fTag du aktuell ausgerüstet hast.",
                    "",
                    "§b✦ Aktueller Tag: §7" + getCurrentSuffix(player),
                    "§b↗ Verfügbare Tags: §e" + suffixNodes.size(),
                    "§9Minecraft"
            );
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String getCurrentSuffix(Player player) {
        LuckPerms luckPerms = FancyTagsGUI.getPlugin(FancyTagsGUI.class).getLuckPerms();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "§7Keiner";

        // KORREKT: Über CachedMetaData den aktuell aktiven Suffix auslesen
        String suffix = user.getCachedData().getMetaData().getSuffix();
        return suffix != null ? suffix : "§7Keiner";
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
