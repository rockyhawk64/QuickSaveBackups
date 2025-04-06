package me.rockyhawk.qsBackup.webserver;

import me.rockyhawk.qsBackup.QuickSave;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class WebServer {
    private final QuickSave plugin;
    private Server server;

    public WebServer(QuickSave plugin) {
        this.plugin = plugin;
    }

    public void start() {
        server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        try {
            ServletHolder holder = new ServletHolder("default", DefaultServlet.class);
            holder.setInitParameter("resourceBase", WebServer.class.getClassLoader().getResource("static").toExternalForm());
            holder.setInitParameter("dirAllowed", "true");
            context.addServlet(holder, "/");
            context.addServlet(new ServletHolder(new PluginServlet(plugin)), "/api");

            server.setHandler(context);
            server.start();

            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[QuickSave] Web server started on port 8080");
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[QuickSave] Failed to start web server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        if (server != null) {
            try {
                server.stop();
                Bukkit.getConsoleSender().sendMessage(ChatColor.WHITE + "[QuickSave] Web server stopped.");
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[QuickSave] Failed to stop web server: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public boolean isRunning() {
        return server != null && server.isStarted();
    }
}