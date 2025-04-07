package me.rockyhawk.qsBackup.webserver;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.rockyhawk.qsBackup.QuickSave;

import java.io.File;
import java.io.IOException;

public class BackupServlet extends HttpServlet {
    private final QuickSave plugin;

    public BackupServlet(QuickSave plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject response = new JsonObject();

        // Get the current world backup list and send it to web
        JsonArray backupFilesJsonArray = new JsonArray();

        // Loop through the backup worlds defined in config
        for (String world : plugin.config.getStringList("backupWorlds")) {
            // Get the backup directory for this world
            File backupDir = new File(plugin.saveFolder, world);

            // Check if directory exists
            if (backupDir.exists() && backupDir.isDirectory()) {
                // Get a list of backup files in the directory
                File[] backupFiles = backupDir.listFiles();

                if (backupFiles != null) {
                    for (File backupFile : backupFiles) {
                        if (backupFile.isFile()) {
                            // Create a JSON object for each backup file
                            JsonObject backupFileJson = new JsonObject();

                            // Get world name, size, and file name
                            String fileName = backupFile.getName();
                            long fileSize = backupFile.length();
                            String worldName = world;  // Assuming world name is tied to the backup folder

                            // Add data to the JSON object
                            backupFileJson.add("world", new JsonPrimitive(worldName));
                            backupFileJson.add("fileName", new JsonPrimitive(fileName));
                            backupFileJson.add("size", new JsonPrimitive(fileSize));

                            // Add this backup file info to the main array
                            backupFilesJsonArray.add(backupFileJson);
                        }
                    }
                }
            }
        }
        // Add the backup files array to the response
        response.add("backupFiles", backupFilesJsonArray);

        // Set the content type and return the response
        resp.setContentType("application/json");
        resp.getWriter().println(response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Handle POST requests if needed
    }
}