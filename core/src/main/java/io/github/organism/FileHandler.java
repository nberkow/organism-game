package io.github.organism;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.File;
import java.util.BitSet;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class FileHandler {

    OrganismGame game;
    public boolean write_mode = false;
    public Random rng;

    public FileHandler(OrganismGame g){
        game = g;
    }

    public void write_cfg(GameConfig cfg, String name, String extension) {
        // Ensure the directory exists
        String dir = "map_configs";
        FileHandle dirHandle = Gdx.files.local(dir);
        if (!dirHandle.exists()) {
            dirHandle.mkdirs();
        }
        
        // Use forward slashes for cross-platform compatibility
        FileHandle handle = Gdx.files.local(dir + "/" + name + "." + extension);
        
        if (rng == null) {
            rng = new Random();
        }
        rng.setSeed(cfg.seed);
        int seed = rng.nextInt();

        String file_content =
            "radius:" + cfg.radius + "\n" +
            "seed:" + seed + "\n" +
            "map_view_size_param:" + cfg.map_view_size_param + "\n" +
            "resources:" + cfg.resources + "\n" +
            "vertex_density:" + cfg.vertex_density + "\n" +
            "resource_layout:" + cfg.layout + "\n" +
            "difficulty:" + cfg.difficulty + "\n" +
            "human_players:" + cfg.humanPlayers + "\n" +
            "bot_players:" + cfg.botPlayers;

        handle.writeString(file_content, false);
    }

    public GameConfig read_cfg(String name, String extension) {
        String dir = "map_configs";
        FileHandle handle = Gdx.files.local(dir + "/" + name + "." + extension);
        
        // If file doesn't exist, return default config
        if (!handle.exists()) {
            System.out.println("Config file not found: " + name + "." + extension + ", using defaults");
            return new GameConfig();
        }
        
        try {
            GameConfig cfg = new GameConfig();
            HashMap<String, String> vals = new HashMap<>();

            String [] lines = handle.readString().split("\n");
            for (String line : lines){
                if (line.trim().isEmpty()) continue;
                String [] fields = line.split(":", 2);
                if (fields.length == 2) {
                    vals.put(fields[0].trim(), fields[1].trim());
                }
            }

            // Parse with defaults if values are missing
            cfg.radius = vals.containsKey("radius") ? Integer.parseInt(vals.get("radius")) : 7;
            cfg.seed = vals.containsKey("seed") ? Long.parseLong(vals.get("seed")) : System.currentTimeMillis();
            cfg.map_view_size_param = vals.containsKey("map_view_size_param") ? Float.parseFloat(vals.get("map_view_size_param")) : 130f;
            cfg.resources = vals.containsKey("resources") ? Float.parseFloat(vals.get("resources")) : 1f;
            cfg.vertex_density = vals.containsKey("vertex_density") ? Float.parseFloat(vals.get("vertex_density")) : 4f;
            cfg.layout = vals.getOrDefault("layout", "radial");
            cfg.difficulty = vals.getOrDefault("difficulty", "easy");
            cfg.humanPlayers = vals.containsKey("human_players") ? Integer.parseInt(vals.get("human_players")) : 1;
            cfg.botPlayers = vals.containsKey("bot_players") ? Integer.parseInt(vals.get("bot_players")) : 2;

            return cfg;
        } catch (Exception e) {
            System.out.println("Error reading config file: " + e.getMessage());
            return new GameConfig();
        }
    }

}
