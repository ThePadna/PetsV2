package main;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import pet.PetType;

import java.io.File;
import java.io.IOException;

/**
 * Created by Cory on 23/05/2017.
 */
public class Data {

    public static void setPet(Player p, PetType petType, boolean has) {
        Pets instance = Pets.getInstance();
        File f = new File(instance.getDataFolder(), "/players/");
        if(!f.exists()) {
            f.mkdirs();
        }
        File player = new File(f, p.getUniqueId().toString() + ".yml");
        if(!player.exists()) {
            try {
                player.createNewFile();
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(player);
                yaml.set(petType.name(), has);
                yaml.save(player);
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(player);
            yaml.set(petType.name(), has);
            try {
                yaml.save(player);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean hasPet(Player p, PetType petType) {
        Pets instance = Pets.getInstance();
        File f = new File(instance.getDataFolder(), "/players/");
        if(!f.exists()) {
            f.mkdirs();
        }
        File player = new File(f, p.getUniqueId().toString() + ".yml");
        if(!player.exists()) return false;
        else {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(player);
            return (boolean)yaml.getBoolean(petType.name());
        }
    }


}
