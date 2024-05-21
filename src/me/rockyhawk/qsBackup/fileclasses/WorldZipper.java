package me.rockyhawk.qsBackup.fileclasses;

import me.rockyhawk.qsBackup.QuickSave;
import org.bukkit.ChatColor;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WorldZipper {
    QuickSave plugin;
    public WorldZipper(QuickSave pl) { this.plugin = pl; }

    public void zip(File worldDirectory, String destZipFile){
        new Thread (() -> {
            try {
                try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destZipFile))) {
                    zipDirectory(worldDirectory, worldDirectory.getName(), zos);
                }

                plugin.getServer().getConsoleSender().sendMessage(plugin.colourize(plugin.tag + ChatColor.GREEN + "Finished backing up " + ChatColor.WHITE + worldDirectory.getName()));
                plugin.oldBackup.checkWorldForOldBackups(new File(plugin.saveFolder.getAbsolutePath() + File.separator + worldDirectory.getName()));
            } catch (IOException e) {
                e.printStackTrace();
                plugin.getServer().getConsoleSender().sendMessage(plugin.colourize(plugin.tag + ChatColor.RED + "Failed to back up " + ChatColor.WHITE + worldDirectory.getName()));
            }
        }).start();
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
