package me.rockyhawk.qsBackup.webserver;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.rockyhawk.qsBackup.QuickSave;
import org.eclipse.jetty.server.Server;

import java.io.*;

public class PluginServlet extends HttpServlet {
    private final QuickSave plugin;
    private Server server;

    public PluginServlet(QuickSave plugin) {
        this.plugin = plugin;
    }
    private String statusMessage = "Hello from server!";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("statusMessage", statusMessage);

        resp.setContentType("application/json");
        resp.getWriter().println(response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        StringBuilder bodyBuilder = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            bodyBuilder.append(line);
        }

        JsonObject json = JsonParser.parseString(bodyBuilder.toString()).getAsJsonObject();

        if (json.has("statusMessage")) {
            statusMessage = json.get("statusMessage").getAsString();
        }

        JsonObject response = new JsonObject();
        response.addProperty("status", "updated");

        resp.setContentType("application/json");
        resp.getWriter().println(response);
    }
}