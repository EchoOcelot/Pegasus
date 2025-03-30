package io.github.echoocelot.pegasus;

import io.github.echoocelot.pegasus.listener.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class Pegasus extends JavaPlugin {

    @Override
    public void onEnable() {
        registerListeners(
                new MountListener(),
                new DismountListener()
        );

        if(getServer().getPluginManager().isPluginEnabled("Towny")) {
            registerListeners(
                    new TownyTeleportListener()
            );
        }

        if(getServer().getPluginManager().isPluginEnabled("Essentials")) {
            registerListeners(
                    new EssentialsTeleportListener()
            );
        }
    }

    private void registerListeners(Listener... listeners) {
        PluginManager pm = getServer().getPluginManager();

        for (Listener listener : listeners) {
            pm.registerEvents(listener, this);
        }
    }

    public static boolean isFoliaEnabled() {
        try {
            Class.forName("io.papermc.paper.threadedregions.ThreadedRegionizer");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // Currently unused method
    public static int getEssentialsTeleportDelay() {
        if(Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            Plugin essentialsPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
            if (essentialsPlugin == null) {
                Bukkit.getServer().getConsoleSender().sendMessage("Pegasus: Trouble contacting Essentials plugin for config");
                return -1;
            }
            File dataFolder = essentialsPlugin.getDataFolder();
            File file = new File(dataFolder, "config.yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            return config.getInt("teleport-delay");
        }
        else {
            Bukkit.getServer().getConsoleSender().sendMessage("Pegasus: Trouble contacting Essentials plugin for config");
            return -1;
        }
    }

    // Currently unused method
    public static List<String> getEssentialsWarps() {
        List<String> emptyList = new ArrayList<>();
        if(Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            Plugin essentialsPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
            if (essentialsPlugin == null) {
                Bukkit.getServer().getConsoleSender().sendMessage("Pegasus: Trouble contacting Essentials plugin for warp list");
                return emptyList;
            }
            File dataFolder = essentialsPlugin.getDataFolder();
            File warpsFolder = new File(dataFolder, "/warps");
            File[] listOfFiles = warpsFolder.listFiles();
            if(listOfFiles == null) return emptyList;
            List<String> fileNames = new ArrayList<>();
            for(File file : listOfFiles) {
                fileNames.add(file.getName());
            }
            return fileNames;
        }
        else {
            Bukkit.getServer().getConsoleSender().sendMessage("Pegasus: Trouble contacting Essentials plugin for warp list");
            return emptyList;
        }
    }

    public static FileConfiguration getWarpInfo(String warpName) {
        if(Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            Plugin essentialsPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
            if (essentialsPlugin == null) {
                Bukkit.getServer().getConsoleSender().sendMessage("Pegasus: Trouble contacting Essentials plugin for warp info");
                return null;
            }
            File dataFolder = essentialsPlugin.getDataFolder();
            File file = new File(dataFolder, "/warps/" + warpName + ".yml");
            return YamlConfiguration.loadConfiguration(file);
        }
        else {
            Bukkit.getServer().getConsoleSender().sendMessage("Pegasus: Trouble contacting Essentials plugin for warp info");
            return null;
        }
    }
}
