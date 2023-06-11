package me.rockyhawk.qsBackup.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import me.rockyhawk.qsBackup.QuickSave;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class quickSaveCommand implements CommandExecutor {
    QuickSave plugin;

    public quickSaveCommand(QuickSave pl) {
        this.plugin = pl;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length < 1) {
            sendHelpMessage(sender);
            return true;
        }

        switch(args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "version":
                handleVersion(sender);
                break;
            case "backup":
                handleBackup(sender, args);
                break;
            default:
                sendHelpMessage(sender);
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("quicksave.admin.reload")) {
            sendNoPermissionMessage(sender);
            return;
        }

        plugin.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + "config.yml"));
        plugin.tag = plugin.config.getString("format.tag") + " ";
        plugin.callRunnable();
        sender.sendMessage(plugin.colourize(plugin.tag + plugin.config.getString("format.reload")));
    }

    private void handleVersion(CommandSender sender) {
        if (!sender.hasPermission("quicksave.version")) {
            sendNoPermissionMessage(sender);
            return;
        }

        sender.sendMessage(plugin.colourize(plugin.tag));
        sender.sendMessage(ChatColor.GREEN + "Version " + ChatColor.GRAY + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GREEN + "Developer " + ChatColor.GRAY + "RockyHawk");
        sender.sendMessage(ChatColor.GREEN + "Command " + ChatColor.GRAY + "/qs");
    }

    private void handleBackup(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quicksave.admin.backup")) {
            sendNoPermissionMessage(sender);
            return;
        }

        List<String> backupWorlds = new ArrayList<>();
        if(args.length > 1 && plugin.config.getStringList("config.worldsToBackup").contains(args[1])) {
            backupWorlds.add(args[1]);
            sender.sendMessage(plugin.colourize(plugin.tag + plugin.config.getString("format.saving")));
            plugin.createNewBackup(backupWorlds);
        } else {
            sender.sendMessage(plugin.colourize(plugin.tag + plugin.config.getString("format.noWorld")));
        }
    }

    private void sendHelpMessage(CommandSender sender){
        if (!sender.hasPermission("quicksave.help")) {
            sendNoPermissionMessage(sender);
            return;
        }

        sender.sendMessage(plugin.colourize(plugin.tag + ChatColor.GREEN + "Commands:"));

        if(sender.hasPermission("quicksave.admin.reload")){
            sender.sendMessage(ChatColor.GREEN + "/qs reload " + ChatColor.WHITE + "Reloads plugin config.");
        }
        if(sender.hasPermission("quicksave.admin.backup")){
            sender.sendMessage(ChatColor.GREEN + "/qs backup [world] " + ChatColor.WHITE + "Creates a new backup for worlds.");
        }
        if(sender.hasPermission("quicksave.version")){
            sender.sendMessage(ChatColor.GREEN + "/qs version " + ChatColor.WHITE + "Display the current version");
        }
    }

    private void sendNoPermissionMessage(CommandSender sender) {
        sender.sendMessage(plugin.colourize(plugin.tag + plugin.config.getString("format.perms")));
    }
}
