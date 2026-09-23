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

public class TagMenuListener implements Listener {

    private final FancyTagsGUI plugin;

    public TagMenuListener(FancyTagsGUI plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Prüfen ob das Inventar unser Menü ist (anhand des Titels)
        if (!event.getView().getTitle().equals(TagMenuHolder.MENU_TITLE)) {
            return;
        }

        // Verhindern, dass Items aus dem Menü genommen werden
        event.setCancelled(true);

        if (event.getWhoClicked() instanceof Player player) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) {
                return;
            }

            // Wenn das "Tag entfernen"-Item geklickt wurde
            if (clickedItem.getType() == Material.BARRIER) {
                removeSuffix(player);
                player.closeInventory();
                return;
            }

            // Wenn ein Tag-Item geklickt wurde
            if (clickedItem.getType() == Material.BLAZE_ROD) {
                String suffix = clickedItem.getItemMeta().getDisplayName();
                applySuffix(player, suffix);
                player.closeInventory();
            }
        }
    }

    private void applySuffix(Player player, String suffix) {
        User user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        // Wichtig: Zuerst alle vorhandenen Suffixe entfernen, um Konflikte zu vermeiden
        user.data().clear(node -> node instanceof SuffixNode);

        // Neuen Suffix setzen
        Node suffixNode = SuffixNode.builder(suffix, 100).build();
        user.data().add(suffixNode);

        // Änderungen speichern
        plugin.getLuckPerms().getUserManager().saveUser(user);
        player.sendMessage("§aDein Tag wurde zu " + suffix + " §ageändert.");
    }

    private void removeSuffix(Player player) {
        User user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        // Alle Suffixe entfernen
        user.data().clear(node -> node instanceof SuffixNode);

        plugin.getLuckPerms().getUserManager().saveUser(user);
        player.sendMessage("§cDein Tag wurde entfernt.");
    }
}
