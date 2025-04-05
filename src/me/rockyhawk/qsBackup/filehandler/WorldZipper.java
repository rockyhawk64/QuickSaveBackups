package me.rockyhawk.qsBackup.filehandler;

import me.rockyhawk.qsBackup.QuickSave;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WorldZipper {
    QuickSave plugin;
    public WorldZipper(QuickSave pl) { this.plugin = pl; }

    public void zip(World world, File worldDirectory, String destZipFile) {
        if(plugin.pluginStatus.contains(world.getName())){
            plugin.getServer().getConsoleSender().sendMessage(plugin.colourize(plugin.tag + plugin.config.getString("format.alreadyBackup") + ChatColor.WHITE + " " + worldDirectory.getName()));
            return;
        }

        // Ensure saving and disabling autosaving happens in correct order
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.pluginStatus.add(world.getName());
                world.setAutoSave(false); // Disable autosaving

                world.save(); // Save the world
                // Save player data manually to avoid inventory inconsistencies
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.saveData();
                }

                // Start backup process in a separate thread
                new Thread(() -> {
                    try {
                        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destZipFile))) {
                            zipDirectory(worldDirectory, worldDirectory.getName(), zos);
                        }

                        plugin.getServer().getConsoleSender().sendMessage(plugin.colourize(plugin.tag + plugin.config.getString("format.finishedBackup") + ChatColor.WHITE + " " + worldDirectory.getName()));
                        plugin.oldBackup.checkWorldForOldBackups(new File(plugin.saveFolder.getAbsolutePath() + File.separator + worldDirectory.getName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                        plugin.getServer().getConsoleSender().sendMessage(plugin.colourize(plugin.tag + plugin.config.getString("format.failedBackup") + ChatColor.WHITE + " " + worldDirectory.getName()));
                    }

                    // Re-enable autosaving once backup is finished
                    Bukkit.getScheduler().runTask(plugin, () -> world.setAutoSave(true));
                    plugin.pluginStatus.remove(world.getName());
                }).start();
            }
        }.runTask(plugin); // Ensures world.save() and world.setAutoSave(false) run on the main thread
    }

    private void zipDirectory(File folder, String parentFolder, ZipOutputStream zos) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zos);
                continue;
            }

            try {
                zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));

                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                    int BUFFER_SIZE = 4096;
                    byte[] bytesIn = new byte[BUFFER_SIZE];
                    int read;
                    while ((read = bis.read(bytesIn)) != -1) {
                        zos.write(bytesIn, 0, read);
                    }
                }

                zos.closeEntry();
            } catch (IOException ignore) {
                // skipping file, most likely 'session.lock' file which can be ignored
            }
        }
    }
}
