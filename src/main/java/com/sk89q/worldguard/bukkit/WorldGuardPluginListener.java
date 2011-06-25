package com.sk89q.worldguard.bukkit;

import java.util.logging.Logger;

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.iConomy.iConomy;
/**
 * Handles plugin load and unload events.
 * 
 * @author Donald Scott
 * @since 6/24/11
 */
public class WorldGuardPluginListener extends ServerListener {
    private WorldGuardPlugin plugin;

    /**
     * Logger for plugin hook messages.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");
    
    public WorldGuardPluginListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void registerEvents() {
    	PluginManager pm = plugin.getServer().getPluginManager();
    	pm.registerEvent(Type.PLUGIN_ENABLE, this, Priority.Monitor, plugin);
        pm.registerEvent(Type.PLUGIN_DISABLE, this, Priority.Monitor, plugin);
	}
    
    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        if (iConomyManager.isloaded()) {
            if (event.getPlugin().getDescription().getName().equals("iConomy")) {
            	iConomyManager.deInitialize();
                logger.info("[WorldGuard] un-hooked from iConomy.");
            }
        }
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        if (!iConomyManager.isloaded()) {
            Plugin iConomy = plugin.getServer().getPluginManager().getPlugin("iConomy");

            if (iConomy != null) {
                if (iConomy.isEnabled() && iConomy.getClass().getName().equals("com.iConomy.iConomy")) {
                	iConomyManager.initialize((iConomy)iConomy);
                    logger.info("[WorldGuard] hooked into iConomy.");
                }
            }
        }
    }	
}
