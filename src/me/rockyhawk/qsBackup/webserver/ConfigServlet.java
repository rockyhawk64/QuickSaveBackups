package me.rockyhawk.qsBackup.webserver;

import com.google.gson.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.rockyhawk.qsBackup.QuickSave;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigServlet extends HttpServlet {
    private final QuickSave plugin;

    public ConfigServlet(QuickSave plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject response = new JsonObject();

        // Dynamically add configuration properties to the response
        addConfigValuesToResponse(response, plugin.config.getValues(true));

        resp.setContentType("application/json");
        resp.getWriter().println(response);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        StringBuilder body = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }

        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(body.toString()).getAsJsonObject();

        // Iterate over all keys in the received JSON and save them to plugin.config
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (value.isJsonArray()) {
                // If the value is a JSON array, convert it to a List<String> and set it
                List<String> list = new ArrayList<>();
                for (JsonElement item : value.getAsJsonArray()) {
                    list.add(item.getAsString());
                }
                plugin.config.set(key, list);
            } else if (value.isJsonPrimitive()) {
                // If the value is a primitive, determine if it's a boolean, number, or string
                JsonPrimitive primitiveValue = value.getAsJsonPrimitive();

                if (primitiveValue.isBoolean()) {
                    plugin.config.set(key, primitiveValue.getAsBoolean());
                } else if (primitiveValue.isNumber()) {
                    Number number = primitiveValue.getAsNumber();

                    if (number instanceof Integer) {
                        plugin.config.set(key, number.intValue()); // Ensures integer handling
                    } else if (number instanceof Long) {
                        plugin.config.set(key, number.longValue()); // For handling long values
                    } else {
                        // Otherwise, store the number as a plain float/double
                        plugin.config.set(key, number.doubleValue());
                    }
                } else {
                    plugin.config.set(key, primitiveValue.getAsString());
                }
            }
        }

        // Save the updated configuration to the config.yml file
        saveConfig();

        // Reload plugin if necessary
        plugin.reloadPlugin();

        JsonObject response = new JsonObject();
        response.addProperty("status", "saved");

        resp.setContentType("application/json");
        resp.getWriter().println(response);
    }

    private void saveConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        YamlConfiguration configYaml = YamlConfiguration.loadConfiguration(configFile);

        // Iterate through all keys in plugin.config and save them to the config.yml
        for (String key : plugin.config.getKeys(true)) {
            Object value = plugin.config.get(key);
            saveValueToConfig(configYaml, key, value);
        }

        try {
            // Save the updated YamlConfiguration back to the file
            configYaml.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Utility method to save a value based on its type
    private void saveValueToConfig(YamlConfiguration configYaml, String key, Object value) {
        if (value instanceof List) {
            // Save lists (e.g., backupWorlds)
            configYaml.set(key, value);
        } else {
            // Save primitive types (strings, numbers, booleans)
            configYaml.set(key, value);
        }
    }


    private JsonArray toJsonArray(List<String> list) {
        JsonArray array = new JsonArray();
        for (String item : list) {
            array.add(new JsonPrimitive(item));
        }
        return array;
    }

    private void addConfigValuesToResponse(JsonObject response, Map<String, Object> configValues) {
        for (Map.Entry<String, Object> entry : configValues.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof List) {
                // Handle lists (e.g., backupWorlds)
                response.add(key, toJsonArray((List<String>) value));
            } else if (value instanceof Map) {
                // Recursively handle nested maps
                JsonObject subObject = new JsonObject();
                addConfigValuesToResponse(subObject, (Map<String, Object>) value);
                response.add(key, subObject);
            } else {
                // Handle primitive types
                response.addProperty(key, value.toString());
            }
        }
    }
}
