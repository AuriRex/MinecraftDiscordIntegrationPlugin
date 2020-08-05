package me.auri.discordintegration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Variables {

    private HashMap<String, Object> vars = new HashMap<>();

    FileConfiguration conf = null;

    public Variables() {

    }

    public Variables(String path) {
        load(path);
    }

    public void debug() {

        System.out.println("DEBUG START");

        vars.forEach((k, v) -> {

            System.out.println(k + " -> " + v);

        });

        System.out.println("DEBUG END");

    }

    public void load(String path) {
        conf = YamlConfiguration.loadConfiguration(new File(path));
        conf.getKeys(true).forEach(k -> {
            if(! (conf.get(k) instanceof MemorySection))
                vars.put(k, conf.get(k));
        });
    }

    public void save(String path) throws IOException {

        vars.forEach((k, v) -> {
            if(v instanceof ChatColor) {
                v = v.toString();
            }
            conf.set(k, v);
        });

        conf.save(path);
    }

    public void set(String key, Object value) {
        vars.put(key, value);
    }

    public void setDefault(String key, Object value) {
        if(!vars.containsKey(key)) {
            set(key, value);
        }
    }

    public String get(String key) {
        if(!vars.containsKey(key)) return null;
        return vars.get(key).toString();
    }

    public boolean getBool(String key) {
    	return Boolean.valueOf(get(key));
    }
    
    public boolean getBoolObject(String key) {
        Object ret = vars.get(key);
        if(ret instanceof Boolean)
            return (boolean) ret;
        return false;
    }

    public int getInt(String key) {
        Object ret = vars.get(key);
        if(ret instanceof Integer)
            return (int) ret;
        return 0;
    }

}