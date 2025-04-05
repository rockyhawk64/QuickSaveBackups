package me.rockyhawk.qsBackup;

import me.rockyhawk.qsBackup.commands.QuickSaveCommand;
import me.rockyhawk.qsBackup.tabcomplete.QuickSaveTabComplete;
import me.rockyhawk.qsBackup.filehandler.OldBackupRemoval;
import me.rockyhawk.qsBackup.filehandler.WorldZipper;
import org.bstats.bukkit.Metrics;
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
import java.util.HashSet;
import java.util.List;

public class QuickSave extends JavaPlugin {
    public YamlConfiguration config;
    public File saveFolder;
    public HashSet<String> pluginStatus = new HashSet<>();
    private int autoBackupCounter = 0;

    public WorldZipper zipper = new WorldZipper(this);
    public OldBackupRemoval oldBackup = new OldBackupRemoval(this);

    public String tag;
    public BukkitTask autoBackup;

    public void onEnable() {
        Bukkit.getLogger().info("[QuickSave] RockyHawk's QuickSave v" + this.getDescription().getVersion() + " Plugin Loading...");

        this.config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + File.separator + "config.yml"));
        this.saveFolder = new File(this.getDataFolder() + File.separator + "backups");

        this.getCommand("quicksave").setTabCompleter(new QuickSaveTabComplete(this));
        this.getCommand("quicksave").setExecutor(new QuickSaveCommand(this));

        this.config.addDefault("config.version", 1.2);
        this.config.addDefault("config.autoBackup", true); //if auto backups are enabled
        this.config.addDefault("config.asyncBackup", false); //if world backups are async
        this.config.addDefault("config.backupInterval", 360); //Measured in minutes, 360 is 6 hours

        this.config.addDefault("format.tag", "&3[&bQuickSave&3]");
        this.config.addDefault("format.perms", "&cNo permission.");
        this.config.addDefault("format.noWorld", "&cWorld not Found in Config.");
        this.config.addDefault("format.alreadyBackup", "&cAlready backing up:");
        this.config.addDefault("format.failedBackup", "&cFailed to back up:");
        this.config.addDefault("format.finishedBackup", "&aFinished backing up:");
        this.config.addDefault("format.reload", "&aReloaded.");
        this.config.addDefault("format.saving", "&aStarting new backup.");

        this.config.addDefault("format.noStatus", "&aCurrently not backing up any worlds.");
        this.config.addDefault("format.status", "&aCurrently backing up:");

        this.config.addDefault("folder_size.maximum_enabled", false); //if you want to set a maximum
        this.config.addDefault("folder_size.maximum_value", 3000); //this is in megabytes, set to 5GB by default

        this.config.addDefault("amount.maximum_enabled", true); //if you want to set a maximum
        this.config.addDefault("amount.maximum_value", 10); //amount of backups allowed per world

        tag = config.getString("format.tag") + " ";

        //bStats instance initialise
        try {
            new Metrics(this, 6727);
        }catch(Exception e){
            Bukkit.getLogger().info("[QuickSave] Could not start bStats instance, ignoring...");
        }

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
    }

    public void onDisable() {
        autoBackup.cancel();
    }

    public String colourize(String input){
        return ChatColor.translateAlternateColorCodes('&',input);
    }

    public void createNewBackup(List<String> backupWorlds){
        saveFolder.mkdir();
        File rootServerFolder = new File(getServer().getWorldContainer().getPath());
        //get the date for the file names
        String strDate = new SimpleDateFormat("dd-MMM-yyyy HH-mm-ss").format(Calendar.getInstance().getTime());
        for (String worldName : backupWorlds) {
            new File(saveFolder.getAbsolutePath() + File.separator + worldName).mkdir();
            File worldToBackup = new File(rootServerFolder.getAbsolutePath() + File.separator + worldName);
            if(!worldToBackup.exists()){
                this.getServer().getConsoleSender().sendMessage(colourize(tag + config.getString("format.noWorld")));
                continue;
            }
            //Get world object from the world name
            World world = Bukkit.getWorld(worldName);
            zipper.zip(world, worldToBackup, saveFolder.getAbsolutePath() + File.separator + worldName + File.separator + strDate + ".zip");
        }
    }

    public void callRunnable(){
        //cancel if not cancelled
        if(autoBackup != null && !autoBackup.isCancelled()) {
            autoBackup.cancel();
        }
        //return if auto backup is disabled
        if(!config.getBoolean("config.autoBackup")){
            return;
        }
        //get interval value
        int intervalInTicks = config.getInt("config.backupInterval") * 60 * 20; // converting minutes to ticks
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

                // Check for asyncBackups config value
                if(!config.getBoolean("config.asyncBackup")) { // If asyncBackups is false, add all worlds to backup list
                    backupWorlds.addAll(config.getStringList("config.worldsToBackup"));
                    getServer().getConsoleSender().sendMessage(colourize(tag + ChatColor.AQUA + "Backing up all worlds..."));
                } else { // If asyncBackups is true, continue with the current behavior
                    backupWorlds.add(config.getStringList("config.worldsToBackup").get(autoBackupCounter));
                    getServer().getConsoleSender().sendMessage(colourize(tag + ChatColor.AQUA + "Backing up world " + ChatColor.WHITE + config.getStringList("config.worldsToBackup").get(autoBackupCounter) + "..."));
                }

                createNewBackup(backupWorlds);
            }
        }.runTaskTimer(this,
                config.getBoolean("config.asyncBackup") ? intervalInTicks / config.getStringList("config.worldsToBackup").size() : intervalInTicks,
                config.getBoolean("config.asyncBackup") ? intervalInTicks / config.getStringList("config.worldsToBackup").size() : intervalInTicks );
    }
}