package de.lmcstudio.fancytagsgui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
    private static final MiniMessage MM = MiniMessage.miniMessage();

    /** Priorität des temporären Nodes, der zum Aktivieren genutzt wird. */
    private static final int TEMP_PRIORITY = 9999;

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

        // --- SUFFIXE AUS LUCKPERMS SAMMELN ---
        LuckPerms luckPerms = plugin.getLuckPerms();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());

        List<SuffixNode> realSuffixes = new ArrayList<>();
        String activeTempValue = null;

        if (user != null) {
            for (Node node : user.getNodes()) {
                if (node instanceof SuffixNode suffixNode) {
                    if (suffixNode.getPriority() == TEMP_PRIORITY) {
                        // Das ist der aktuell "aktive" Tag (temporärer Node)
                        activeTempValue = extractSuffixFromNode(suffixNode);
                    } else {
                        // Echter, verfügbarer Suffix
                        realSuffixes.add(suffixNode);
                    }
                }
            }
        }

        // Sortierung: Höchste Weight zuerst
        realSuffixes.sort(Comparator.comparingInt(SuffixNode::getPriority).reversed());

        // Slots für die Suffixe
        int[] suffixSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

        for (int i = 0; i < realSuffixes.size() && i < suffixSlots.length; i++) {
            SuffixNode node = realSuffixes.get(i);
            String suffixValue = extractSuffixFromNode(node);

            // KORREKT: Vergleich mit dem Wert des temporären Nodes,
            // nicht mit getCurrentSuffix() – sonst matcht es nie.
            boolean isSelected = suffixValue.equals(activeTempValue);

            ItemStack item = createTagItem(suffixValue, node.getPriority(), isSelected);
            inv.setItem(suffixSlots[i], item);
        }

        // --- INFO-ITEM ---
        ItemStack infoItem = createInfoItem(player, realSuffixes, activeTempValue);
        inv.setItem(4, infoItem);

        // --- "TAG DEAKTIVIEREN"-ITEM ---
        ItemStack removeItem = createRemoveItem();
        inv.setItem(49, removeItem);

        player.openInventory(inv);
    }

    /**
     * Liest den Suffix-String aus dem Node-Key.
     * Format: "suffix.<weight>.<value>"
     */
    private static String extractSuffixFromNode(SuffixNode node) {
        String key = node.getKey();
        String[] parts = key.split("\\.", 3);
        if (parts.length >= 3) {
            return parts[2];
        }
        return "§7Unbekannt";
    }

    private static ItemStack createTagItem(String suffix, int weight, boolean isSelected) {
        Material material = isSelected ? Material.ENCHANTED_BOOK : Material.NAME_TAG;
        ItemStack item = new ItemStack(material);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Leerzeichen davor, damit Name und Tag getrennt sind
            Component displayName = MM.deserialize(" " + suffix);

            if (isSelected) {
                displayName = displayName
                        .decoration(TextDecoration.BOLD, true)
                        .color(NamedTextColor.GREEN);
            }

            meta.displayName(displayName);

            List<Component> lore = new ArrayList<>();
            lore.add(MM.deserialize("<gray>Beschreibung</gray>"));
            lore.add(Component.empty());
            lore.add(MM.deserialize("<red>Information:</red>"));
            lore.add(MM.deserialize("<white>Klicke um diesen Suffix</white>"));
            lore.add(MM.deserialize("<white>zu aktivieren.</white>"));
            lore.add(Component.empty());
            lore.add(MM.deserialize("<gray>Gewichtung: </gray><yellow>" + weight + "</yellow>"));

            if (isSelected) {
                lore.add(MM.deserialize("<green>Status: ✔ AUSGEWÄHLT</green>"));
                lore.add(MM.deserialize("<green>▶ Dieser Tag ist aktuell aktiv</green>"));
            } else {
                lore.add(MM.deserialize("<gray>Status: </gray><green>Verfügbar</green>"));
                lore.add(MM.deserialize("<yellow>▶ KLICKE zum Ausrüsten</yellow>"));
            }

            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createInfoItem(Player player, List<SuffixNode> suffixNodes, String activeTempValue) {
        ItemStack item = new ItemStack(Material.PURPLE_SHULKER_BOX);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MM.deserialize("<aqua>DEINE TAGS</aqua>"));

            String currentDisplay = activeTempValue != null
                    ? activeTempValue
                    : "<gray>Keiner</gray>";

            List<Component> lore = Arrays.asList(
                    MM.deserialize("<gray>Beschreibung</gray>"),
                    Component.empty(),
                    MM.deserialize("<blue>Information:</blue>"),
                    MM.deserialize("<white>Hier kannst du sehen, welchen</white>"),
                    MM.deserialize("<white>Tag du aktuell ausgerüstet hast.</white>"),
                    Component.empty(),
                    MM.deserialize("<aqua>✦ Aktueller Tag: </aqua><gray>" + currentDisplay + "</gray>"),
                    MM.deserialize("<aqua>↗ Verfügbare Tags: </aqua><yellow>" + suffixNodes.size() + "</yellow>")
            );
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createRemoveItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MM.deserialize("<red>TAG DEAKTIVIEREN</red>"));
            List<Component> lore = Arrays.asList(
                    MM.deserialize("<gray>Beschreibung</gray>"),
                    Component.empty(),
                    MM.deserialize("<red>Information:</red>"),
                    MM.deserialize("<white>Klicke hier um deinen</white>"),
                    MM.deserialize("<white>aktuellen Tag zu deaktivieren.</white>"),
                    MM.deserialize("<gray>Der Tag wird nicht gelöscht,</gray>"),
                    MM.deserialize("<gray>sondern nur vorübergehend</gray>"),
                    MM.deserialize("<gray>ausgeblendet.</gray>"),
                    Component.empty(),
                    MM.deserialize("<yellow>▶ KLICKE zum Deaktivieren</yellow>")
            );
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
