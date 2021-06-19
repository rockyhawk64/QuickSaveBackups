package me.rockyhawk.qsBackup;

import me.rockyhawk.qsBackup.commands.quickSaveCommand;
import me.rockyhawk.qsBackup.completeTabs.qsTabComplete;
import me.rockyhawk.qsBackup.fileclasses.OldBackupRemoval;
import me.rockyhawk.qsBackup.fileclasses.WorldZipper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class QuickSave extends JavaPlugin {
    public YamlConfiguration config;
    public File saveFolder;
    private int autoBackupCounter = 0;

    public WorldZipper zipper = new WorldZipper(this);
    public OldBackupRemoval oldBackup = new OldBackupRemoval(this);
    public Updater updater = new Updater(this);

    public String tag;
    public BukkitTask autoBackup;

    public void onEnable() {
        Bukkit.getLogger().info("[QuickSave] RockyHawk's QuickSave v" + this.getDescription().getVersion() + " Plugin Loading...");

        this.config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + File.separator + "config.yml"));
        this.saveFolder = new File(this.getDataFolder() + File.separator + "backups");

        this.getCommand("quicksave").setTabCompleter(new qsTabComplete(this));
        this.getCommand("quicksave").setExecutor(new quickSaveCommand(this));
        new Metrics(this);
        this.config.addDefault("config.version", 1.0);
        this.config.addDefault("config.format.tag", "&3[&bQuickSave&3]");
        this.config.addDefault("config.format.perms", "&cNo permission.");
        this.config.addDefault("config.format.noWorld", "&cWorld not Found in Config.");
        this.config.addDefault("config.format.reload", "&aReloaded.");
        this.config.addDefault("config.format.saving", "&aStarting new backup.");
        this.config.addDefault("config.autoBackup", true); //if auto backups are enabled
        this.config.addDefault("config.backupTickInterval", 288000); //72000 is 1 hour

        this.config.addDefault("updater.auto_update", true); //automatically update the plugin

        this.config.addDefault("folder_size.maximum_enabled", true); //if you want to set a maximum
        this.config.addDefault("folder_size.maximum_value", 750); //this is in megabytes, set to

        this.config.addDefault("amount.maximum_enabled", true); //if you want to set a maximum
        this.config.addDefault("amount.maximum_value", 40); //amount of backups allowed per world

        tag = config.getString("config.format.tag") + " ";

        List<String> backupWorlds = new ArrayList();
        for(World temp : getServer().getWorlds()){
            backupWorlds.add(temp.getName());
        }
        this.config.addDefault("config.worldsToBackup", backupWorlds);
        this.config.options().copyDefaults(true);
        try {
            this.config.save(new File(this.getDataFolder() + File.separator + "config.yml"));
        } catch (IOException var10) {
            Bukkit.getConsoleSender().sendMessage(tag + ChatColor.RED + "WARNING: Could not save the config file!");
        }
        Bukkit.getLogger().info("[QuickSave] RockyHawk's QuickSave v" + this.getDescription().getVersion() + " Plugin Loaded!");
        //call the auto backup task
        callRunnable();
        /*BukkitTask auto_backup = new BukkitRunnable(){
            @Override
            public void run(){
                if(!config.getBoolean("config.autoBackup")){
                    return;
                }
                if(autoBackupCounter >= config.getStringList("config.worldsToBackup").size()-1){
                    autoBackupCounter=0;
                }else{
                    autoBackupCounter+=1;
                }
                List<String> backupWorlds = new ArrayList();
                backupWorlds.add(config.getStringList("config.worldsToBackup").get(autoBackupCounter));
                createNewBackup(backupWorlds);
                getServer().getConsoleSender().sendMessage(colourize(tag + ChatColor.AQUA + "Backing up world " + ChatColor.WHITE + config.getStringList("config.worldsToBackup").get(autoBackupCounter) + "..."));
            }
        }.runTaskTimer(this, (config.getInt("config.backupTickInterval")/config.getStringList("config.worldsToBackup").size()), (config.getInt("config.backupTickInterval")/config.getStringList("config.worldsToBackup").size())); //20 ticks == 1 second (5 ticks = 0.25 of a second)*/
    }

    public void onDisable() {
        autoBackup.cancel();
        if (this.config.getBoolean("updater.auto_update")) {
            updater.doAutoUpdate(this.getFile().getName());
        }
    }

    public String colourize(String input){
        return ChatColor.translateAlternateColorCodes('&',input);
    }

    public void createNewBackup(List<String> backupWorlds){
        if(!saveFolder.exists()){
            saveFolder.mkdir();
        }
        File rootServerFolder = new File(getServer().getWorldContainer().getPath());
        //get the date for the file names
        String strDate = new SimpleDateFormat("dd-MMM-yyyy HH-mm-ss").format(Calendar.getInstance().getTime());
        for(String worldName : backupWorlds){
            if(!new File(saveFolder.getAbsolutePath() + File.separator + worldName).exists()){
                new File(saveFolder.getAbsolutePath() + File.separator + worldName).mkdir();
            }
            zipper.zip(new File(rootServerFolder.getAbsolutePath() + File.separator + worldName), saveFolder.getAbsolutePath() + File.separator + worldName + File.separator + strDate + ".zip");
        }
    }

    public void callRunnable(){
        //cancel if not cancelled
        if(autoBackup != null){
            if(!autoBackup.isCancelled()){
                autoBackup.cancel();
            }
        }
        //return if auto backup is disabled
        if(!config.getBoolean("config.autoBackup")){
            return;
        }
        //run task
        autoBackup = new BukkitRunnable(){
            @Override
            public void run(){
                if(autoBackupCounter >= config.getStringList("config.worldsToBackup").size()-1){
                    autoBackupCounter=0;
                }else{
                    autoBackupCounter+=1;
                }
                List<String> backupWorlds = new ArrayList();
                backupWorlds.add(config.getStringList("config.worldsToBackup").get(autoBackupCounter));
                createNewBackup(backupWorlds);
                getServer().getConsoleSender().sendMessage(colourize(tag + ChatColor.AQUA + "Backing up world " + ChatColor.WHITE + config.getStringList("config.worldsToBackup").get(autoBackupCounter) + "..."));
            }
        }.runTaskTimer(this, (config.getInt("config.backupTickInterval")/config.getStringList("config.worldsToBackup").size()), (config.getInt("config.backupTickInterval")/config.getStringList("config.worldsToBackup").size())); //20 ticks == 1 second (5 ticks = 0.25 of a second)
    }
}