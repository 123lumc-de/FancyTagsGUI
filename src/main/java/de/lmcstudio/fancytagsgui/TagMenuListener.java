package de.lmcstudio.fancytagsgui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
    private static final int TEMP_PRIORITY = 9999;

    public TagMenuListener(FancyTagsGUI plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(TagMenuHolder.MENU_TITLE)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }

        // Barrier = Tag deaktivieren
        if (clickedItem.getType() == Material.BARRIER) {
            deactivateSuffix(player);
            player.closeInventory();
            return;
        }

        // NameTag = Suffix aktivieren
        if (clickedItem.getType() == Material.NAME_TAG) {
            if (clickedItem.getItemMeta() == null || clickedItem.getItemMeta().displayName() == null) {
                return;
            }

            // KORRIGIERT: MiniMessage.serialize() statt PlainTextComponentSerializer
            // Dadurch bleiben alle Formatierungen (Farben, Bold, etc.) erhalten
            String suffixValue = MiniMessage.miniMessage()
                    .serialize(clickedItem.getItemMeta().displayName());

            activateSuffix(player, suffixValue);
            player.closeInventory();
        }
    }

    private void activateSuffix(Player player, String suffixValue) {
        User user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        // Alten temporären Node entfernen
        user.data().clear(node -> node instanceof SuffixNode suffixNode
                && suffixNode.getPriority() == TEMP_PRIORITY);

        // Neuen temporären Node setzen
        Node tempSuffix = SuffixNode.builder(suffixValue, TEMP_PRIORITY)
                .expiry(30, TimeUnit.DAYS)
                .build();

        user.data().add(tempSuffix);
        plugin.getLuckPerms().getUserManager().saveUser(user);

        player.sendMessage("§aDein Tag wurde aktiviert.");
    }

    private void deactivateSuffix(Player player) {
        User user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        user.data().clear(node -> node instanceof SuffixNode suffixNode
                && suffixNode.getPriority() == TEMP_PRIORITY);

        plugin.getLuckPerms().getUserManager().saveUser(user);

        player.sendMessage("§cDein Tag wurde deaktiviert.");
    }
}
