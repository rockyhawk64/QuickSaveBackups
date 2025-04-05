package me.rockyhawk.qsBackup.filehandler;

import me.rockyhawk.qsBackup.QuickSave;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Predicate;

public class OldBackupRemoval {
    QuickSave plugin;

    public OldBackupRemoval(QuickSave pl) {
        this.plugin = pl;
    }

    private static final long BYTES_PER_MEGABYTE = 1048576L;

    public void checkWorldForOldBackups(File directory) {
        if(plugin.config.getBoolean("amount.maximum_enabled")) {
            deleteFiles(directory, dir -> dir.listFiles() != null && dir.listFiles().length > plugin.config.getInt("amount.maximum_value"));
        }
        if(plugin.config.getBoolean("folder_size.maximum_enabled")) {
            deleteFiles(directory, dir -> getFileFolderSize(dir) > plugin.config.getInt("folder_size.maximum_value") * BYTES_PER_MEGABYTE);
        }
    }

    private void deleteFiles(File directory, Predicate<File> shouldDelete) {
        while (shouldDelete.test(directory)) {
            File oldestFile = getOldestFile(directory);
            if (oldestFile != null) {
                try {
                    Files.delete(oldestFile.toPath());
                } catch (java.nio.file.FileSystemException e) {
                    plugin.getServer().getConsoleSender().sendMessage(plugin.colourize(
                            plugin.tag + ChatColor.RED + "Could not delete file " + oldestFile.getName() + " because it's in use by another process."));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private File getOldestFile(File directory) {
        long oldestDate = Long.MAX_VALUE;
        File oldestFile = null;
        for (File ftempFile : directory.listFiles()) {
            if (ftempFile.lastModified() < oldestDate) {
                oldestDate = ftempFile.lastModified();
                oldestFile = ftempFile;
            }
        }
        return oldestFile;
    }

    private long getFileFolderSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                size += file.length();
            } else {
                size += getFileFolderSize(file);
            }
        }
        return size;
    }
}