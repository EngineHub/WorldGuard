package com.sk89q.worldguard.bukkit;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.herocraftonline.dthielke.lists.Lists;

public class WorldGuardServerListener extends ServerListener {

	/**
     * Plugin.
     */
    private WorldGuardPlugin plugin;
    
    /**
     * Construct the object;
     * 
     * @param plugin
     */
    public WorldGuardServerListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Register events
     */
    public void registerEvents() {
    	PluginManager pm = plugin.getServer().getPluginManager();
    	
    	pm.registerEvent(Event.Type.PLUGIN_ENABLE, this, Priority.Normal, plugin);
    	pm.registerEvent(Event.Type.PLUGIN_DISABLE, this, Priority.Normal, plugin);
    }
    
    /**
     * Called when a plugin is enabled
     * 
     * @param event Relevant event details
     */
    @Override
    public void onPluginEnable(PluginEnableEvent event) {
    	if (plugin.listsPlugin == null) {
    		Plugin lists = plugin.getServer().getPluginManager().getPlugin("Lists");
    		
    		if (lists != null) {
    			if (lists.isEnabled()) {
    				plugin.listsPlugin = (Lists) lists;
    				WorldGuardPlugin.logger.info("WorldGuard Lists support enabled (using Lists " 
    						+ plugin.listsPlugin.getDescription().getVersion() + ")");
    			}
    		}
    	}
    }
    
    /**
     * Called when a plugin is disabled
     * 
     * @param event Relevant event details
     */
    @Override
    public void onPluginDisable(PluginDisableEvent event) {
    	if (plugin.listsPlugin != null) {
    		String pluginName = event.getPlugin().getDescription().getName();
    		
    		if (pluginName.equals("Lists")) {
    			plugin.listsPlugin = null;
    			WorldGuardPlugin.logger.info("WorldGuard Lists support disabled");
    		}
    	}
    }
}
