package me.rockyhawk.qsBackup.fileclasses;

import me.rockyhawk.qsBackup.quickSaveMain;

import java.io.File;

public class OldBackupRemoval {
    quickSaveMain plugin;
    public OldBackupRemoval(quickSaveMain pl) { this.plugin = pl; }

    public void deleteOldBackups(){
        if(plugin.config.getBoolean("config.maximumBackupEnabled")){
            //delete old backups
            long maximumBytes = plugin.config.getInt("config.maximumBackupSize")*1048576L;
            long sizeofFolderBytes = getFileFolderSize(plugin.saveFolder);
            while(sizeofFolderBytes > maximumBytes){
                try {
                    long oldestDate = Long.MAX_VALUE;
                    File oldestFile = null;
                    File[] fList = plugin.saveFolder.listFiles();
                    if(fList != null){
                        for (File ftemp : fList) {
                            if (ftemp.isFile()) {
                                //skip files in root Backup folder directory
                            } else if (ftemp.isDirectory()) {
                                for (File ftempFile : ftemp.listFiles()) {
                                    if (ftempFile.lastModified() < oldestDate) {
                                        oldestDate = ftempFile.lastModified();
                                        oldestFile = ftempFile;
                                    }
                                }
                            }
                        }
                    }
                    oldestFile.delete();
                }catch(NullPointerException oof){
                    //directory probably doesn't exist for some reason
                    break;
                }
                //refresh to check again
                sizeofFolderBytes = getFileFolderSize(plugin.saveFolder);
            }
        }
    }
    public long getFileFolderSize(File dir) {
        long size = 0;
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    size += file.length();
                } else
                    size += getFileFolderSize(file);
            }
        } else if (dir.isFile()) {
            size += dir.length();
        }
        return size;
    }
}
