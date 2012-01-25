package com.sk89q.worldguard.bukkit;

import org.bukkit.event.Event;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.PluginManager;

/**
 * @author zml2008
 */
public class WorldGuardServerListener extends ServerListener {
    
    private final WorldGuardPlugin plugin;
    
    public WorldGuardServerListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void registerEvents() {
        registerEvent("PLUGIN_ENABLE");
        registerEvent("PLUGIN_DISABLE");
    }

    /**
     * Register an event, but not failing if the event is not implemented.
     *
     * @param typeName
     */
    private void registerEvent(String typeName) {
        try {
            Event.Type type = Event.Type.valueOf(typeName);
            PluginManager pm = plugin.getServer().getPluginManager();
            pm.registerEvent(type, this, Event.Priority.Normal, plugin);
        } catch (IllegalArgumentException e) {
            plugin.logger.info("WorldGuard: Unable to register missing event type " + typeName);
        }
    }
    
    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        plugin.getGlobalStateManager().updateCommandBookGodMode();
    }
    
    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        plugin.getGlobalStateManager().updateCommandBookGodMode();
    }
}
