package me.rockyhawk.qsBackup.fileclasses;

import me.rockyhawk.qsBackup.QuickSave;

import java.io.File;

public class OldBackupRemoval {
    QuickSave plugin;
    public OldBackupRemoval(QuickSave pl) { this.plugin = pl; }

    public void checkForOldBackups(){
        //delete old backups
        try {
            //list the files/worlds in the backup folder
            File[] fList = plugin.saveFolder.listFiles();
            if(fList != null){
                //loop through the files/worlds in the backup folder
                for (File ftemp : fList) {
                    if (ftemp.isFile()) {
                        //skip files in root Backup folder directory
                    } else if (ftemp.isDirectory()) {
                        checkWorldForOldBackups(ftemp);
                    }
                }
            }
        }catch(NullPointerException oof){
            //directory probably doesn't exist for some reason
        }
    }

    public void checkWorldForOldBackups(File directory){
        deleteMaxAmount(directory);
        deleteMaxStorage(directory);
    }

    private void deleteMaxAmount(File directory){
        if(!plugin.config.getBoolean("amount.maximum_enabled")){
            return;
        }
        long maximumAmount = plugin.config.getInt("amount.maximum_value");
        //if the file in the backup folder is a directory
        long amountInFolder = directory.listFiles().length;
        //loop to find the oldest file, delete it and then check byte size parameters
        while(amountInFolder > maximumAmount) {
            long oldestDate = Long.MAX_VALUE;
            File oldestFile = null;
            for (File ftempFile : directory.listFiles()) {
                if (ftempFile.lastModified() < oldestDate) {
                    oldestDate = ftempFile.lastModified();
                    oldestFile = ftempFile;
                }
            }
            if(oldestFile.delete()){/*do nothing*/}
            amountInFolder = directory.listFiles().length;
        }
    }

    private void deleteMaxStorage(File directory){
        if(!plugin.config.getBoolean("folder_size.maximum_enabled")){
            return;
        }
        long maximumBytes = plugin.config.getInt("folder_size.maximum_value")*1048576L;
        //if the file in the backup folder is a directory
        long sizeofFolderBytes = getFileFolderSize(directory);
        //loop to find the oldest file, delete it and then check byte size parameters
        while(sizeofFolderBytes > maximumBytes) {
            long oldestDate = Long.MAX_VALUE;
            File oldestFile = null;
            for (File ftempFile : directory.listFiles()) {
                if (ftempFile.lastModified() < oldestDate) {
                    oldestDate = ftempFile.lastModified();
                    oldestFile = ftempFile;
                }
            }
            if(oldestFile.delete()){/*do nothing*/}
            sizeofFolderBytes = getFileFolderSize(directory);
        }
    }

    private long getFileFolderSize(File dir) {
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
