package me.rockyhawk.qsBackup.commands;

import me.rockyhawk.qsBackup.QuickSave;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class QuickSaveTabComplete implements TabCompleter {
    QuickSave plugin;
    public QuickSaveTabComplete(QuickSave pl) { this.plugin = pl; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(label.equalsIgnoreCase("qs") || label.equalsIgnoreCase("quicksave")) {
            if (sender instanceof Player && args.length == 1) {
                ArrayList<String> autoComplete = new ArrayList<>(); //all panels
                autoComplete.add("help");
                autoComplete.add("backup");
                autoComplete.add("status");
                autoComplete.add("reload");
                autoComplete.add("version");
                return autoComplete;
            }
            if (sender instanceof Player && args.length == 2) {
                if (args[0].equalsIgnoreCase("backup")) {
                    return plugin.config.getStringList("backupWorlds");
                }
            }
        }
        return null;
    }
}