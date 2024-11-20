package io.github.organism;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class FileHandler {

    public void write_cfg(GameConfig cfg, String path) {

        String file_content =
            "radius:" + cfg.radius + "\n" +
            "seed:" + cfg.seed + "\n" +
            "map_view_size_param:" + cfg.map_view_size_param + "\n" +
            "resources:" + cfg.resources + "\n" +
            "vertex_density:" + cfg.vertex_density + "\n" +
            "resource_layout:" + cfg.layout + "\n" +
            "difficulty:" + cfg.difficulty + "\n" +
            "human_players:" + cfg.human_players + "\n" +
            "bot_players:" + cfg.bot_players;

            try {
                FileWriter myWriter = new FileWriter("filename.txt");
                myWriter.write(file_content);
                myWriter.close();
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }

    public GameConfig read_cfg(String path) {
        GameConfig cfg = new GameConfig();
        HashMap<String, String> vals = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String [] fields = line.split(":");
                vals.put(fields[0], fields[1]);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        cfg.radius = Integer.parseInt(vals.get("radius"));
        cfg.seed = Long.parseLong(vals.get("seed"));
        cfg.map_view_size_param = Float.parseFloat(vals.get("map_view_size_param"));
        cfg.resources = Float.parseFloat(vals.get("resources"));
        cfg.vertex_density = Float.parseFloat(vals.get("vertex_density"));
        cfg.layout = vals.get("layout");
        cfg.difficulty = vals.get("difficulty");
        cfg.human_players = Integer.parseInt(vals.get("human_players"));
        cfg.bot_players = Integer.parseInt(vals.get("bot_players"));

        return cfg;
    }
}
