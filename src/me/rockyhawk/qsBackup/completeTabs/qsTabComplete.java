package me.rockyhawk.qsBackup.completeTabs;

import me.rockyhawk.qsBackup.quickSaveMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class qsTabComplete implements TabCompleter {
    quickSaveMain plugin;
    public qsTabComplete(quickSaveMain pl) { this.plugin = pl; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(label.equalsIgnoreCase("qs") || label.equalsIgnoreCase("quicksave")) {
            if (sender instanceof Player && args.length == 1) {
                ArrayList<String> autoComplete = new ArrayList<>(); //all panels
                autoComplete.add("help");
                autoComplete.add("backup");
                autoComplete.add("reload");
                autoComplete.add("version");
                return autoComplete;
            }
            if (sender instanceof Player && args.length == 2) {
                if (args[0].equalsIgnoreCase("backup")) {
                    return plugin.config.getStringList("config.worldsToBackup");
                }
            }
        }
        return null;
    }
}