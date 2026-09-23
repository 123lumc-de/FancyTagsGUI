package de.lmcstudio.fancytagsgui;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.SuffixNode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

public class TagMenuListener implements Listener {

    private final FancyTagsGUI plugin;

    /** Priorität des "temporären" Aktivierungs-Nodes, der alle anderen überschreibt. */
    private static final int TEMP_PRIORITY = 9999;

    public TagMenuListener(FancyTagsGUI plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Nur auf unser Menü reagieren
        if (!event.getView().getTitle().equals(TagMenuHolder.MENU_TITLE)) {
            return;
        }

        // Verhindern, dass Items aus dem Menü genommen werden
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }

        // --- "TAG DEAKTIVIEREN"-ITEM (Barrier) ---
        if (clickedItem.getType() == Material.BARRIER) {
            deactivateSuffix(player);
            player.closeInventory();
            return;
        }

        // --- TAG-ITEM (NameTag) ---
        if (clickedItem.getType() == Material.NAME_TAG) {
            if (clickedItem.getItemMeta() == null || clickedItem.getItemMeta().displayName() == null) {
                return;
            }

            // Anzeigenamen (Component) zu reinem Text serialisieren
            String suffixValue = PlainTextComponentSerializer.plainText()
                    .serialize(clickedItem.getItemMeta().displayName());

            activateSuffix(player, suffixValue);
            player.closeInventory();
        }
    }

    /**
     * Aktiviert einen Suffix, ohne andere Suffixe zu löschen.
     * Der neue Suffix wird als temporärer Node mit hoher Priorität gesetzt
     * und überschreibt damit die Anzeige, ohne die Original-Nodes anzutasten.
     */
    private void activateSuffix(Player player, String suffixValue) {
        User user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        // Alten temporären Node entfernen (falls schon einer aktiv war)
        // KORREKT: Pattern-Matching-Cast, damit getPriority() verfügbar ist
        user.data().clear(node -> node instanceof SuffixNode suffixNode
                && suffixNode.getPriority() == TEMP_PRIORITY);

        // Neuen temporären Node setzen
        Node tempSuffix = SuffixNode.builder(suffixValue, TEMP_PRIORITY)
                .expiry(30, TimeUnit.DAYS)
                .build();

        user.data().add(tempSuffix);

        // WICHTIG: Änderungen speichern
        plugin.getLuckPerms().getUserManager().saveUser(user);

        player.sendMessage("§aDein Tag wurde aktiviert: §f" + suffixValue);
    }

    /**
     * Deaktiviert den aktuellen Tag, ohne ihn zu löschen.
     * Es wird nur der temporäre Node entfernt – der ursprüngliche Suffix
     * (z. B. aus einer Gruppe) bleibt erhalten und wird wieder sichtbar.
     */
    private void deactivateSuffix(Player player) {
        User user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        // Nur den temporären Node (Priorität 9999) entfernen
        // KORREKT: Pattern-Matching-Cast, damit getPriority() verfügbar ist
        user.data().clear(node -> node instanceof SuffixNode suffixNode
                && suffixNode.getPriority() == TEMP_PRIORITY);

        plugin.getLuckPerms().getUserManager().saveUser(user);

        player.sendMessage("§cDein Tag wurde deaktiviert. Der ursprüngliche Suffix ist wieder sichtbar.");
    }
}
