package me.rockyhawk.qsBackup;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

public class Updater {
    //Change this to your own plugin
    QuickSave plugin;
    public Updater(QuickSave pl) {
        this.plugin = pl;
    }

    private final String PLUGIN_NAME = "QuickSave"; //The plugin namey
    private final String FILE_NAME = "Quick.Save.jar"; //The exact file name that the file appears as on Github. It should be the same name between versions.
    private final String GITHUB_USERNAME = "rockyhawk64"; //The username the repository is under
    private final String GITHUB_REPOSITORY = "QuickSaveBackups"; //Repository name on Github
    public boolean MINOR_UPDATES_ONLY = false; //Uses 4 number versions, 1.2.X.X <-- the X numbers indicate minor versions. If true it will only update if a minor update is available
    public String DOWNLOAD_VERSION_MANUALLY = null; //if this is set to something by your plugin, it will download that version on restart. can be a version number, 'latest' or 'cancel'

    /*
    By RockyHawk

    The plugin.yml file should be in the github at /resource/plugin.yml
    and the first line of the file should be the version number.

    public void onDisable() {
        updater.doAutoUpdate(this.getFile().getName());
    }
     */

    public String cachedLatestVersion = "null";

    public String checkForNewUpdate(boolean sendMessages){
        //refresh latest version
        getLatestVersion(sendMessages);

        if(plugin.getDescription().getVersion().contains("-")){
            if(sendMessages) {
                Bukkit.getConsoleSender().sendMessage("[" + PLUGIN_NAME + "]" + ChatColor.GREEN + " Running a custom version.");
            }
            return null;
        }

        //if update is true there is a new update
        boolean update = !cachedLatestVersion.equals(plugin.getDescription().getVersion());

        if(update){
            if(sendMessages) {
                Bukkit.getConsoleSender().sendMessage("[" + PLUGIN_NAME + "]" + ChatColor.GOLD + " ================================================");
                Bukkit.getConsoleSender().sendMessage("[" + PLUGIN_NAME + "]" + ChatColor.AQUA + " An update for " + PLUGIN_NAME + " is available.");
                Bukkit.getConsoleSender().sendMessage("[" + PLUGIN_NAME + "]" + ChatColor.GOLD + " ================================================");
            }
        }
        return cachedLatestVersion;
    }

    //the pluginFileName can only be obtained from the main class
    public void doAutoUpdate(String pluginFileName){
        String latestVersion = cachedLatestVersion;
        String thisVersion = plugin.getDescription().getVersion();

        if(DOWNLOAD_VERSION_MANUALLY != null) {
            if (DOWNLOAD_VERSION_MANUALLY.equals("latest")) {
                downloadFile(latestVersion, pluginFileName);
            }else{
                downloadFile(DOWNLOAD_VERSION_MANUALLY, pluginFileName);
            }
            return;
        }

        if(latestVersion.equals(thisVersion) || thisVersion.contains("-")){
            //no need to update or running custom version
            return;
        }
        if(MINOR_UPDATES_ONLY){
            //only update versions that will not break
            if(thisVersion.split("\\.")[1].equals(latestVersion.split("\\.")[1]) && thisVersion.split("\\.")[0].equals(latestVersion.split("\\.")[0])){
                //the first and second number of the version is the same, updates: [major.major.minor.minor]
                downloadFile(latestVersion,pluginFileName);
            }
        }else{
            downloadFile(latestVersion,pluginFileName);
        }
    }

    private void getLatestVersion(boolean sendMessages){
        //check for null
        if(cachedLatestVersion.equals("null")){
            cachedLatestVersion = plugin.getDescription().getVersion();
        }

        //using an array allows editing while still being final
        new BukkitRunnable(){
          public void run(){
              HttpURLConnection connection;
              try {
                  connection = (HttpURLConnection) new URL("https://raw.githubusercontent.com/" + GITHUB_USERNAME + "/" + GITHUB_REPOSITORY + "/master/resource/plugin.yml").openConnection();
                  connection.connect();
                  cachedLatestVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine().split("\\s")[1];
                  connection.disconnect();
              } catch (IOException ignore) {
                  Bukkit.getConsoleSender().sendMessage("[" + PLUGIN_NAME + "]" + ChatColor.RED + " Could not access github.");
              }
          }
        }.runTask(plugin);

        if(cachedLatestVersion.contains("-")){
            if(sendMessages) {
                Bukkit.getConsoleSender().sendMessage("[" + PLUGIN_NAME + "]" + ChatColor.RED + " Cannot check for update.");
            }
        }
    }

    private void downloadFile(String latestVersion, String pluginFileName) {
        BufferedInputStream in = null;
        FileOutputStream fout = null;

        try {
            this.plugin.getLogger().info("Downloading new update: " + "v" + latestVersion);
            URL fileUrl = new URL("https://github.com/" + GITHUB_USERNAME + "/" + GITHUB_REPOSITORY + "/releases/download/" + latestVersion + "/" + FILE_NAME);
            int fileLength = fileUrl.openConnection().getContentLength();
            in = new BufferedInputStream(fileUrl.openStream());
            fout = new FileOutputStream(new File(new File(".").getAbsolutePath() + "/plugins/", pluginFileName));
            byte[] data = new byte[1024];

            long downloaded = 0L;

            int count;
            while((count = in.read(data, 0, 1024)) != -1) {
                downloaded += count;
                fout.write(data, 0, count);
                int percent = (int)(downloaded * 100L / (long)fileLength);
                if (percent % 10 == 0) {
                    this.plugin.getLogger().info("Downloading update: " + percent + "% of " + fileLength + " bytes.");
                }
            }
            this.plugin.getLogger().info("Finished updating.");
        } catch (Exception var22) {
            this.plugin.getLogger().log(Level.WARNING, "Could not download update.", var22);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException var21) {
                this.plugin.getLogger().log(Level.SEVERE, null, var21);
            }

            try {
                if (fout != null) {
                    fout.close();
                }
            } catch (IOException var20) {
                this.plugin.getLogger().log(Level.SEVERE, null, var20);
            }

        }

    }
}
