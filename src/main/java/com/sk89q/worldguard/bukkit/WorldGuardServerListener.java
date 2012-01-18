package com.sk89q.worldguard.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginManager;

/**
 * @author zml2008
 */
public class WorldGuardServerListener implements Listener {

    private final WorldGuardPlugin plugin;

    public WorldGuardServerListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerEvents() {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(this, plugin);
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        plugin.getGlobalStateManager().updateCommandBookGodMode();
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        plugin.getGlobalStateManager().updateCommandBookGodMode();
    }
}
