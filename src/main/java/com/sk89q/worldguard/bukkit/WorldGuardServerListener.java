package com.sk89q.worldguard.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginManager;

/**
 * Listener for server events.
 */
class WorldGuardServerListener implements Listener {

    private final WorldGuardPlugin plugin;

    /**
     * Construct the listener.
     *
     * @param plugin WorldGuard plugin
     */
    WorldGuardServerListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register events.
     */
    void registerEvents() {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(this, plugin);
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        // Legacy god mode
        if (event.getPlugin().getDescription().getName().equalsIgnoreCase("CommandBook")) {
            plugin.getGlobalStateManager().updateCommandBookGodMode();
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        // Legacy god mode
        if (event.getPlugin().getDescription().getName().equalsIgnoreCase("CommandBook")) {
            plugin.getGlobalStateManager().updateCommandBookGodMode();
        }
    }
}
