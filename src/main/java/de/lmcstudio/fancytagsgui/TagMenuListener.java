package de.lmcstudio.fancytagsgui;

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

    public TagMenuListener(FancyTagsGUI plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(TagMenuHolder.MENU_TITLE)) {
            return;
        }

        event.setCancelled(true);

        if (event.getWhoClicked() instanceof Player player) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) {
                return;
            }

            // Barrier = Tag deaktivieren (nicht löschen!)
            if (clickedItem.getType() == Material.BARRIER) {
                deactivateSuffix(player);
                player.closeInventory();
                return;
            }

            // NAME_TAG = Suffix aktivieren
            if (clickedItem.getType() == Material.NAME_TAG) {
                String suffixName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                        .plainText().serialize(clickedItem.getItemMeta().displayName());
                activateSuffix(player, suffixName);
                player.closeInventory();
            }
        }
    }

    /**
     * Aktiviert einen Suffix, ohne den alten zu löschen.
     * Der neue Suffix wird als temporärer Node mit hoher Priorität gesetzt,
     * der den alten überschreibt.
     */
    private void activateSuffix(Player player, String suffixValue) {
        User user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        // Temporären Suffix mit Priorität 9999 setzen (überschreibt alles)
        // Nach 30 Tagen abgelaufen (praktisch "unendlich")
        Node tempSuffix = SuffixNode.builder(suffixValue, 9999)
                .expiry(30, TimeUnit.DAYS)
                .build();

        // Alten temporären Suffix entfernen (falls vorhanden)
        user.data().clear(node -> node instanceof SuffixNode
                && node.getPriority() == 9999);

        user.data().add(tempSuffix);
        plugin.getLuckPerms().getUserManager().saveUser(user);

        player.sendMessage("§aTag aktiviert: " + suffixValue);
    }

    /**
     * Deaktiviert den aktuellen Tag, ohne ihn zu löschen.
     * Der temporäre Node wird entfernt, der originale Suffix bleibt erhalten.
     */
    private void deactivateSuffix(Player player) {
        User user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        // Nur den temporären Node (Priorität 9999) entfernen
        user.data().clear(node -> node instanceof SuffixNode
                && node.getPriority() == 9999);

        plugin.getLuckPerms().getUserManager().saveUser(user);

        player.sendMessage("§cTag deaktiviert. Dein ursprünglicher Suffix ist wieder sichtbar.");
    }
}
