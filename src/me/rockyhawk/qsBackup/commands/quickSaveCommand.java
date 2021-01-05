package me.rockyhawk.qsBackup.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import me.rockyhawk.qsBackup.quickSaveMain;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class quickSaveCommand implements CommandExecutor {
    quickSaveMain plugin;

    public quickSaveCommand(quickSaveMain pl) {
        this.plugin = pl;
    }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 1){
            if(args[0].equalsIgnoreCase("reload")){
                if (sender.hasPermission("quicksave.admin.reload")) {
                    plugin.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + "config.yml"));
                    plugin.tag = plugin.config.getString("config.format.tag") + " ";
                    sender.sendMessage(plugin.colourize(plugin.tag + plugin.config.getString("config.format.reload")));
                }else{
                    sender.sendMessage(plugin.colourize(plugin.tag + plugin.config.getString("config.format.perms")));
                }
                return true;
            }
            if(args[0].equalsIgnoreCase("version")){
                if (sender.hasPermission("quicksave.version")) {
                    sender.sendMessage(plugin.colourize(plugin.tag));
                    sender.sendMessage(ChatColor.GREEN + "Version " + ChatColor.GRAY + plugin.getDescription().getVersion());
                    sender.sendMessage(ChatColor.GREEN + "Developer " + ChatColor.GRAY + "RockyHawk");
                    sender.sendMessage(ChatColor.GREEN + "Command " + ChatColor.GRAY + "/qs");
                }else{
                    sender.sendMessage(plugin.colourize(plugin.tag + plugin.config.getString("config.format.perms")));
                }
                return true;
            }
            if(args[0].equalsIgnoreCase("help")){
                sendHelpMessage(sender);
                return true;
            }
            if(args[0].equalsIgnoreCase("backup")){
                if (sender.hasPermission("quicksave.admin.backup")) {
                    sender.sendMessage(plugin.colourize(plugin.tag + plugin.config.getString("config.format.saving")));
                    plugin.createNewBackup(plugin.config.getStringList("config.worldsToBackup"));
                }else{
                    sender.sendMessage(plugin.colourize(plugin.tag + plugin.config.getString("config.format.perms")));
                }
                return true;
            }
        }
        if(args.length == 2){
            if(args[0].equalsIgnoreCase("backup")){
                if (sender.hasPermission("quicksave.admin.backup")) {
                    List<String> backupWorlds = new ArrayList<>();
                    if(plugin.config.getStringList("config.worldsToBackup").contains(args[1])){
                        backupWorlds.add(args[1]);
                        sender.sendMessage(plugin.colourize(plugin.tag + plugin.config.getString("config.format.saving")));
                        plugin.createNewBackup(backupWorlds);
                    }else{
                        sender.sendMessage(plugin.colourize(plugin.tag + plugin.config.getString("config.format.noWorld")));
                    }
                }else{
                    sender.sendMessage(plugin.colourize(plugin.tag + plugin.config.getString("config.format.perms")));
                }
                return true;
            }
        }
        sendHelpMessage(sender);
        return true;
    }
    public void sendHelpMessage(CommandSender sender){
        if (sender.hasPermission("quicksave.help")) {
            sender.sendMessage(plugin.colourize(plugin.tag + ChatColor.GREEN + "Commands:"));
            if(sender.hasPermission("quicksave.admin.reload")){
                sender.sendMessage(ChatColor.GREEN + "/qs reload " + ChatColor.WHITE + "Reloads plugin config.");
            }
            if(sender.hasPermission("quicksave.admin.backup")){
                sender.sendMessage(ChatColor.GREEN + "/qs backup " + ChatColor.WHITE + "Creates a new backup for worlds.");
            }
            if(sender.hasPermission("quicksave.version")){
                sender.sendMessage(ChatColor.GREEN + "/qs version " + ChatColor.WHITE + "Display the current version");
            }
        }else{
            sender.sendMessage(plugin.colourize(plugin.tag + plugin.config.getString("config.format.perms")));
        }
    }
}
