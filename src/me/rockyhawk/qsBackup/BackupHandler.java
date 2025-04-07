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
    private boolean isBackupRunning = false; // Flag to track if backup is running, isCancelled() is not in older mc versions

    public BackupHandler(QuickSave plugin) {
        this.plugin = plugin;
    }

    public void callRunnable() {
        // cancel if not cancelled
        if (autoBackup != null && isBackupRunning) {
            autoBackup.cancel();
            isBackupRunning = false;
        }
        // return if auto backup is disabled
        if (!plugin.config.getBoolean("autoBackup")) {
            return;
        }
        // get interval value, cannot be below 1
        int interval = plugin.config.getInt("backupInterval");
        if(interval <= 0){
            interval = 1;
        }
        int intervalInTicks = interval * 60 * 20; // converting minutes to ticks
        // run task
        autoBackup = new BukkitRunnable() {
            @Override
            public void run() {
                if (autoBackupCounter >= plugin.config.getStringList("backupWorlds").size() - 1) {
                    autoBackupCounter = 0;
                } else {
                    autoBackupCounter += 1;
                }
                List<String> backupWorlds = new ArrayList<>();

                // Check for asyncBackups config value
                if (!plugin.config.getBoolean("asyncBackup")) { // If asyncBackups is false, add all worlds to backup list
                    backupWorlds.addAll(plugin.config.getStringList("backupWorlds"));
                } else { // If asyncBackups is true, continue with the current behavior
                    backupWorlds.add(plugin.config.getStringList("backupWorlds").get(autoBackupCounter));
                }

                Bukkit.getConsoleSender().sendMessage(plugin.colorize(plugin.tag + plugin.config.getString("saving")));
                createNewBackup(backupWorlds);
            }
        }.runTaskTimer(plugin,
                plugin.config.getBoolean("asyncBackup") ? intervalInTicks / plugin.config.getStringList("backupWorlds").size() : intervalInTicks,
                plugin.config.getBoolean("asyncBackup") ? intervalInTicks / plugin.config.getStringList("backupWorlds").size() : intervalInTicks);

        isBackupRunning = true; // Set the flag to true when the backup task starts
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
                plugin.getServer().getConsoleSender().sendMessage(plugin.colorize(plugin.tag + plugin.config.getString("noWorld")));
                continue;
            }
            // Get world object from the world name
            World world = Bukkit.getWorld(worldName);
            plugin.zipper.zip(world, worldToBackup, saveFolder.getAbsolutePath() + File.separator + worldName + File.separator + strDate + ".zip");
        }
    }

    public void cancelBackup() {
        if (autoBackup != null && isBackupRunning) {
            autoBackup.cancel();
            isBackupRunning = false; // Reset the flag when the task is cancelled
        }
    }

    public boolean isRunning() {
        return isBackupRunning;
    }
}