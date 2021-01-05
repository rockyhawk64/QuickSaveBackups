package me.rockyhawk.qsBackup.fileclasses;

import me.rockyhawk.qsBackup.quickSaveMain;
import org.bukkit.ChatColor;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WorldZipper {
    quickSaveMain plugin;
    public WorldZipper(quickSaveMain pl) { this.plugin = pl; }

    private boolean currentlyBackingUp = false;

    public void zip(String worldName, File worldDirectory, String destZipFile){
        //create new thread to backup world on this thread
        new Thread (new Runnable(){
            @Override
            public void run() {
                try {
                    //zip new file
                    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destZipFile));
                    zipDirectory(worldDirectory, worldDirectory.getName(), zos);
                    zos.flush();
                    zos.close();

                    //delete old file
                    plugin.getServer().getConsoleSender().sendMessage(plugin.colourize(plugin.tag + ChatColor.GREEN + "Finished backing up " + ChatColor.WHITE + worldName));
                    plugin.oldBackup.deleteOldBackups();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void zipDirectory(File folder, String parentFolder, ZipOutputStream zos){
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zos);
                continue;
            }

            try {
                zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                long bytesRead = 0;
                int BUFFER_SIZE = 4096;
                byte[] bytesIn = new byte[BUFFER_SIZE];
                int read = 0;
                while ((read = bis.read(bytesIn)) != -1) {
                    zos.write(bytesIn, 0, read);
                    bytesRead += read;
                }
                zos.closeEntry();
            }catch (IOException ignore){
                //skipping file, most likely 'session.lock' file
            }
        }
    }
}
