package com.sk89q.worldguard.bukkit;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.config.YamlConfigurationManager;
import com.sk89q.worldguard.util.report.Unreported;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BukkitConfigurationManager extends YamlConfigurationManager {

    @Unreported private WorldGuardPlugin plugin;
    @Unreported private ConcurrentMap<String, BukkitWorldConfiguration> worlds = new ConcurrentHashMap<>();

    private boolean hasCommandBookGodMode;

    /**
     * Construct the object.
     *
     * @param plugin The plugin instance
     */
    public BukkitConfigurationManager(WorldGuardPlugin plugin) {
        super();
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public void copyDefaults() {
        // Create the default configuration file
        plugin.createDefaultConfiguration(new File(plugin.getDataFolder(), "config.yml"), "config.yml");
    }

    @Override
    public void unload() {
        worlds.clear();
    }

    @Override
    public void postLoad() {
        // Load configurations for each world
        for (World world : WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getWorlds()) {
            get(world);
        }
    }

    /**
     * Get the configuration for a world.
     *
     * @param world The world to get the configuration for
     * @return {@code world}'s configuration
     */
    @Override
    public BukkitWorldConfiguration get(World world) {
        String worldName = world.getName();
        BukkitWorldConfiguration config = worlds.get(worldName);
        BukkitWorldConfiguration newConfig = null;

        while (config == null) {
            if (newConfig == null) {
                newConfig = new BukkitWorldConfiguration(plugin, worldName, this.getConfig());
            }
            worlds.putIfAbsent(world.getName(), newConfig);
            config = worlds.get(world.getName());
        }

        return config;
    }

    public void updateCommandBookGodMode() {
        try {
            if (plugin.getServer().getPluginManager().isPluginEnabled("CommandBook")) {
                Class.forName("com.sk89q.commandbook.GodComponent");
                hasCommandBookGodMode = true;
                return;
            }
        } catch (ClassNotFoundException ignore) {}
        hasCommandBookGodMode = false;
    }

    public boolean hasCommandBookGodMode() {
        return hasCommandBookGodMode;
    }
}
