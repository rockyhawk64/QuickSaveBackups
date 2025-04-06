package me.rockyhawk.qsBackup;

import me.rockyhawk.qsBackup.commands.QuickSaveCommand;
import me.rockyhawk.qsBackup.tabcomplete.QuickSaveTabComplete;
import me.rockyhawk.qsBackup.filehandler.OldBackupRemoval;
import me.rockyhawk.qsBackup.filehandler.WorldZipper;
import me.rockyhawk.qsBackup.webserver.WebServer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceReader;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashSet;

public class QuickSave extends JavaPlugin {
    public YamlConfiguration config;
    public File saveFolder;
    public HashSet<String> pluginStatus = new HashSet<>();

    public WorldZipper zipper = new WorldZipper(this);
    public OldBackupRemoval oldBackup = new OldBackupRemoval(this);

    public String tag;

    public BackupHandler backupHandler;
    public WebServer webServer;

    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage("[QuickSave] RockyHawk's QuickSave v" + this.getDescription().getVersion() + " Plugin Loading...");

        this.config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + File.separator + "config.yml"));

        this.getCommand("quicksave").setTabCompleter(new QuickSaveTabComplete(this));
        this.getCommand("quicksave").setExecutor(new QuickSaveCommand(this));

        //save the config.yml file
        File configFile = new File(this.getDataFolder() + File.separator + "config.yml");
        if (!configFile.exists()) {
            //generate a new config file from internal resources
            try {
                FileConfiguration configFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(this.getResource("config.yml")));
                configFileConfiguration.save(configFile);
                this.config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + File.separator + "config.yml"));
            } catch (IOException var11) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[QuickSave] WARNING: Could not save the config file!");
            }
        } else {
            //check if the config file has any missing elements
            try {
                YamlConfiguration configFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(this.getResource("config.yml")));
                this.config.addDefaults(configFileConfiguration);
                this.config.options().copyDefaults(true);
                this.config.save(new File(this.getDataFolder() + File.separator + "config.yml"));
            } catch (IOException var10) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[QuickSave] WARNING: Could not save the config file!");
            }
        }

        //Load backup folder location
        String backupPath = config.getString("backupLocation");
        this.saveFolder = new File(backupPath.toLowerCase().contains("p") ? this.getDataFolder() : new File("."), "backups");

        tag = config.getString("tag") + " ";

        //bStats instance initialise
        try {
            new Metrics(this, 6727);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[QuickSave] Could not start bStats instance, ignoring...");
        }

        Bukkit.getConsoleSender().sendMessage(ChatColor.WHITE + "[QuickSave] RockyHawk's QuickSave v" + this.getDescription().getVersion() + " Plugin Loaded!");

        // Initialize Backup and call the auto backup task
        this.backupHandler = new BackupHandler(this);
        this.backupHandler.callRunnable();

        // Initialize Web Server
        if(config.getBoolean("webInterface")) {
            this.webServer = new WebServer(this);
            webServer.start();
        }
    }

    public void onDisable() {
        if (backupHandler != null) {
            backupHandler.cancelBackup();
        }
    }

    public void reloadPlugin(){
        config = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "config.yml"));

        String backupPath = config.getString("backupLocation");
        saveFolder = new File(backupPath.toLowerCase().contains("p") ? getDataFolder() : new File("."), "backups");

        tag = config.getString("tag") + " ";
        backupHandler.callRunnable();
        getServer().getConsoleSender().sendMessage(colorize(tag + config.getString("reload")));
    }

    public String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public Reader getReaderFromStream(InputStream initialStream) throws IOException {
        //this reads the encrypted resource files in the jar file
        byte[] buffer = IOUtils.toByteArray(initialStream);
        return new CharSequenceReader(new String(buffer));
    }
}
