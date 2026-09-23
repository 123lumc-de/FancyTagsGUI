package de.lmcstudio.fancytagsgui;

import net.luckperms.api.LuckPerms;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FancyTagsGUI extends JavaPlugin {

    private LuckPerms luckPerms;

    @Override
    public void onEnable() {
        // LuckPerms API laden
        if (getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            getLogger().severe("LuckPerms wurde nicht gefunden! Das Plugin wird deaktiviert.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.luckPerms = getServer().getServicesManager().load(LuckPerms.class);

        // Listener registrieren
        getServer().getPluginManager().registerEvents(new TagMenuListener(this), this);

        // bStats initialisieren
        int pluginId = 34231;
        Metrics metrics = new Metrics(this, pluginId);

        getLogger().info("FancyTagsGUI von lmcstudio wurde aktiviert!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("fancytagsgui")) {
            TagMenuHolder.openMenu(player, this);
            return true;
        }
        return false;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }
}
