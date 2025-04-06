package me.rockyhawk.qsBackup;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BackupHandler {
    private final QuickSave plugin;
    private int autoBackupCounter = 0;
    private BukkitTask autoBackup;

    public BackupHandler(QuickSave plugin) {
        this.plugin = plugin;
    }

    public void callRunnable() {
        // cancel if not cancelled
        if (autoBackup != null && !autoBackup.isCancelled()) {
            autoBackup.cancel();
        }
        // return if auto backup is disabled
        if (!plugin.config.getBoolean("config.autoBackup")) {
            return;
        }
        // get interval value
        int intervalInTicks = plugin.config.getInt("config.backupInterval") * 60 * 20; // converting minutes to ticks
        // run task
        autoBackup = new BukkitRunnable() {
            @Override
            public void run() {
                if (autoBackupCounter >= plugin.config.getStringList("config.backupWorlds").size() - 1) {
                    autoBackupCounter = 0;
                } else {
                    autoBackupCounter += 1;
                }
                List<String> backupWorlds = new ArrayList<>();

                // Check for asyncBackups config value
                if (!plugin.config.getBoolean("config.asyncBackup")) { // If asyncBackups is false, add all worlds to backup list
                    backupWorlds.addAll(plugin.config.getStringList("config.backupWorlds"));
                } else { // If asyncBackups is true, continue with the current behavior
                    backupWorlds.add(plugin.config.getStringList("config.backupWorlds").get(autoBackupCounter));
                }

                Bukkit.getConsoleSender().sendMessage(plugin.colorize(plugin.tag + plugin.config.getString("format.saving")));
                createNewBackup(backupWorlds);
            }
        }.runTaskTimer(plugin,
                plugin.config.getBoolean("config.asyncBackup") ? intervalInTicks / plugin.config.getStringList("config.backupWorlds").size() : intervalInTicks,
                plugin.config.getBoolean("config.asyncBackup") ? intervalInTicks / plugin.config.getStringList("config.backupWorlds").size() : intervalInTicks);
    }

    public void createNewBackup(List<String> backupWorlds) {
        File saveFolder = plugin.saveFolder;
        saveFolder.mkdir();
        File rootServerFolder = new File(plugin.getServer().getWorldContainer().getPath());
        // get the date for the file names
        String strDate = new SimpleDateFormat("dd-MMM-yyyy HH-mm-ss").format(Calendar.getInstance().getTime());
        for (String worldName : backupWorlds) {
            new File(saveFolder.getAbsolutePath() + File.separator + worldName).mkdir();
            File worldToBackup = new File(rootServerFolder.getAbsolutePath() + File.separator + worldName);
            if (!worldToBackup.exists()) {
                plugin.getServer().getConsoleSender().sendMessage(plugin.colorize(plugin.tag + plugin.config.getString("format.noWorld")));
                continue;
            }
            // Get world object from the world name
            World world = Bukkit.getWorld(worldName);
            plugin.zipper.zip(world, worldToBackup, saveFolder.getAbsolutePath() + File.separator + worldName + File.separator + strDate + ".zip");
        }
    }

    public void cancelBackup() {
        if (autoBackup != null && !autoBackup.isCancelled()) {
            autoBackup.cancel();
        }
    }

    public boolean isRunning() {
        return !autoBackup.isCancelled();
    }
}