package me.rockyhawk.qsBackup.webserver;

import com.google.gson.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.rockyhawk.qsBackup.QuickSave;
import org.bukkit.Bukkit;
import org.eclipse.jetty.server.Server;

import java.io.*;

public class PluginServlet extends HttpServlet {
    private final QuickSave plugin;
    private Server server;

    public PluginServlet(QuickSave plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("serverVersion", Bukkit.getVersion());
        response.addProperty("backupsRunning", plugin.pluginStatus.size());
        response.addProperty("autoBackupStatus", plugin.backupHandler.isRunning());
        response.addProperty("autoBackupAsync", plugin.config.getBoolean("asyncBackup"));

        // Get the list of worlds being backed up
        JsonArray backupWorldsJsonArray = new JsonArray();
        for (String world : plugin.config.getStringList("backupWorlds")) {
            JsonElement worldJsonElement = new JsonPrimitive(world);
            backupWorldsJsonArray.add(worldJsonElement);  // Add each world to the JSON array
        }
        response.add("backupWorlds", backupWorldsJsonArray);

        resp.setContentType("application/json");
        resp.getWriter().println(response);
    }
}