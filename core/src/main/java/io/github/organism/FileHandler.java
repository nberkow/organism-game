package io.github.organism;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.File;
import java.util.Arrays;
import java.util.BitSet;
import java.util.UUID;
import java.awt.Point;
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
        FileHandle handle = Gdx.files.local( name + "." + extension);
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
            "human_players:" + cfg.human_players + "\n" +
            "bot_players:" + cfg.bot_players;

        handle.writeString(file_content, false);

    }

    public GameConfig read_cfg(String name, String extension) {
        String dir = "map_configs";

        FileHandle handle = Gdx.files.local( dir + "/" +    name + "." + extension);
        GameConfig cfg = new GameConfig();
        HashMap<String, String> vals = new HashMap<>();

        String [] lines = handle.readString().split("\n");
        for (String line : lines){
            String [] fields = line.split(":");
            vals.put(fields[0], fields[1]);
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

    public void save_model(HMM model, String name){
        String dir = "model_configs";
        UUID uuid = UUID.randomUUID();
        String uuid_string = uuid.toString();


        FileHandle handle = Gdx.files.local( dir + "/" + uuid_string + ".hmm");
        int states = model.transition_weights.length;
        int inputs = model.transition_weights[0][0].length;
        BitSet mask = model.transition_bit_mask;
        String mask_string = bitset_to_hex(mask);

        handle.writeString(name + "\n", true);
        handle.writeString(states + "\n", true);
        handle.writeString(inputs + "\n", true);
        handle.writeString(mask_string + "\n", true);

        ArrayList<String> content = get_strings(model);

        String all_lines = String.join("\n", content);
        handle.writeString(all_lines, true);
        System.out.println("saved: " + dir + "/" + uuid_string + ".hmm");
    }

    private static ArrayList<String> get_strings(HMM model) {
        ArrayList<String> content = new ArrayList<>();

        for (int i = 0; i< model.emission_weights.length; i++){
            for (int j = 0; j< model.emission_weights[i].length; j++){
                for (int k = 0; k < model.emission_weights[i][j].length; k++){
                    content.add(i + "\t" + j + "\t" + k + "\t" + model.emission_weights[i][j][k]);
                }
            }
        }

        for (int i = 0; i< model.transition_weights.length; i++){
            for (int j = 0; j< model.transition_weights[i].length; j++){
                for (int k = 0; k < model.transition_weights[i][j].length; k++){
                    content.add(i + "\t" + j + "\t" + k + "\t" + model.transition_weights[i][j][k]);
                }
            }
        }
        return content;
    }

    public ArrayList<HMM> load_models() {
        String dir_name = "model_configs";
        File directory = new File(dir_name);
        String[] file_names = directory.list();

        ArrayList<HMM> models = new ArrayList<>();
        if (file_names != null){
            for (String f : file_names){
                models.add(load_model(f));
            }
        }

        return(models);
    }

    private static String bitset_to_hex(BitSet bitSet) {
        byte[] bytes = bitSet.toByteArray();
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

    private static BitSet hex_to_bitset(String hex_string) {
        byte[] bytes = hex_string_to_bytearray(hex_string);
        return BitSet.valueOf(bytes);
    }

    private static byte[] hex_string_to_bytearray(String hexString) {
        int len = hexString.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }
    public HMM load_model(String file_name){

        String dir = "model_configs";
        FileHandle handle = Gdx.files.local( dir + "/" + file_name);
        String [] content = handle.readString().split("\n");

        String name = content[0].trim();
        int states = Integer.parseInt(content[1]);
        int inputs = Integer.parseInt(content[2]);
        String mask_string = content[3];
        BitSet mask = hex_to_bitset(mask_string);

        int index = 4;
        HMM model = new HMM(game, states, inputs);
        model.name = name;
        model.transition_bit_mask = mask;

        model.emission_weights = new double[states][4][inputs];
        for (int i=0; i<states * 4 * inputs; i++){
            String [] elements = content[index].split("\t");
            int a = Integer.parseInt(elements[0]);
            int b = Integer.parseInt(elements[1]);
            int c = Integer.parseInt(elements[2]);
            double w = Double.parseDouble(elements[3]);
            model.emission_weights[a][b][c] = w;
            index ++;
        }

        model.transition_weights = new double[states][states][inputs];
        for (int i=0; i<states * states * inputs; i++){
            String [] elements = content[index].split("\t");
            int a = Integer.parseInt(elements[0]);
            int b = Integer.parseInt(elements[1]);
            int c = Integer.parseInt(elements[2]);
            double w = Double.parseDouble(elements[3]);
            model.transition_weights[a][b][c] = w;
            index ++;
        }

        return model;
    }
}
