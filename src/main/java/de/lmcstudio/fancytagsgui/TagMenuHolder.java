package de.lmcstudio.fancytagsgui;

import net.kyori.adventure.text.Component;
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

        // --- ALLE SUFFIXE AUS LUCKPERMS SAMMELN ---
        LuckPerms luckPerms = plugin.getLuckPerms();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());

        List<SuffixNode> allSuffixes = new ArrayList<>();

        if (user != null) {
            for (Node node : user.getNodes()) {
                if (node instanceof SuffixNode suffixNode) {
                    allSuffixes.add(suffixNode);
                }
            }
        }

        // Sortierung: Höchste Weight zuerst
        allSuffixes.sort(Comparator.comparingInt(SuffixNode::getPriority).reversed());

        // Slots für die Suffixe
        int[] suffixSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

        for (int i = 0; i < allSuffixes.size() && i < suffixSlots.length; i++) {
            SuffixNode node = allSuffixes.get(i);
            String suffixValue = extractSuffixFromNode(node);
            // MiniMessage-Parsing für den Namen
            Component displayName = MM.deserialize(suffixValue);
            ItemStack item = createTagItem(displayName, node.getPriority());
            inv.setItem(suffixSlots[i], item);
        }

        // --- INFO-ITEM ---
        ItemStack infoItem = createInfoItem(player, allSuffixes);
        inv.setItem(4, infoItem);

        // --- "TAG DEAKTIVIEREN"-ITEM (Barrier) ---
        ItemStack removeItem = createRemoveItem();
        inv.setItem(49, removeItem);

        player.openInventory(inv);
    }

    private static String extractSuffixFromNode(SuffixNode node) {
        String key = node.getKey();
        String[] parts = key.split("\\.", 3);
        if (parts.length >= 3) {
            return parts[2];
        }
        return "§7Unbekannt";
    }

    private static ItemStack createTagItem(Component displayName, int weight) {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(displayName);
            // Lore mit MiniMessage (als Components)
            List<Component> lore = Arrays.asList(
                    MM.deserialize("<gray>Beschreibung</gray>"),
                    Component.empty(),
                    MM.deserialize("<red>Information:</red>"),
                    MM.deserialize("<white>Klicke um diesen Suffix</white>"),
                    MM.deserialize("<white>zu aktivieren.</white>"),
                    Component.empty(),
                    MM.deserialize("<gray>Gewichtung: </gray><yellow>" + weight + "</yellow>"),
                    MM.deserialize("<gray>Status: </gray><green>Verfügbar</green>"),
                    MM.deserialize("<yellow>▶ KLICKE zum Ausrüsten</yellow>")
            );
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createInfoItem(Player player, List<SuffixNode> suffixNodes) {
        ItemStack item = new ItemStack(Material.PURPLE_SHULKER_BOX);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MM.deserialize("<aqua>DEINE TAGS</aqua>"));
            List<Component> lore = Arrays.asList(
                    MM.deserialize("<gray>Beschreibung</gray>"),
                    Component.empty(),
                    MM.deserialize("<blue>Information:</blue>"),
                    MM.deserialize("<white>Hier kannst du sehen, welchen</white>"),
                    MM.deserialize("<white>Tag du aktuell ausgerüstet hast.</white>"),
                    Component.empty(),
                    MM.deserialize("<aqua>✦ Aktueller Tag: </aqua><gray>" + getCurrentSuffix(player) + "</gray>"),
                    MM.deserialize("<aqua>↗ Verfügbare Tags: </aqua><yellow>" + suffixNodes.size() + "</yellow>")
            );
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String getCurrentSuffix(Player player) {
        LuckPerms luckPerms = FancyTagsGUI.getPlugin(FancyTagsGUI.class).getLuckPerms();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "<gray>Keiner</gray>";

        String suffix = user.getCachedData().getMetaData().getSuffix();
        return suffix != null ? suffix : "<gray>Keiner</gray>";
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
