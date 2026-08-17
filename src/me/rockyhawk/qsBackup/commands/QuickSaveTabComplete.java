package me.rockyhawk.qsBackup.commands;

import me.rockyhawk.qsBackup.QuickSave;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class QuickSaveTabComplete implements TabCompleter {
    QuickSave plugin;
    public QuickSaveTabComplete(QuickSave pl) { this.plugin = pl; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(label.equalsIgnoreCase("qs") || label.equalsIgnoreCase("quicksave")) {
            if (sender instanceof Player && args.length == 1) {
                ArrayList<String> autoComplete = new ArrayList<>();

                if(sender.hasPermission("quicksave.admin.reload")){
                    autoComplete.add("reload");
                }
                if(sender.hasPermission("quicksave.admin.backup")){
                    autoComplete.add("backup");
                }
                if(sender.hasPermission("quicksave.admin.status")){
                    autoComplete.add("status");
                }
                if(sender.hasPermission("quicksave.version")){
                    autoComplete.add("version");
                }

                return autoComplete;
            }
            if (sender instanceof Player && args.length == 2) {
                if(sender.hasPermission("quicksave.admin.backup")) {
                    if (args[0].equalsIgnoreCase("backup")) {
                        return plugin.config.getStringList("backup_worlds");
                    }
                }
            }
        }
        return null;
    }
}