package com.sk89q.worldguard.bukkit;

import java.util.logging.Logger;

import org.bukkit.event.Event;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.PluginManager;

import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Methods;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class WorldGuardServerListener extends ServerListener {
    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");
    private WorldGuardPlugin plugin;
    private Methods Methods = null;

    public WorldGuardServerListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
        this.Methods = new Methods();
    }

    public void registerEvents() {
        registerEvent("PLUGIN_ENABLE", Event.Priority.Monitor);
        registerEvent("PLUGIN_DISABLE", Event.Priority.Monitor);
    }

    /**
     * Register an event, but not failing if the event is not implemented.
     *
     * @param typeName
     * @param priority
     *
     * @todo This method is duplicated over all listeners, ugly.
     */
    private void registerEvent(String typeName, Event.Priority priority) {
        try {
            Event.Type type = Event.Type.valueOf(typeName);
            PluginManager pm = plugin.getServer().getPluginManager();
            pm.registerEvent(type, this, priority, plugin);
        } catch (IllegalArgumentException e) {
            logger.info("WorldGuard: Unable to register missing event type " + typeName);
        }
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        if (this.Methods != null && this.Methods.hasMethod()) {
            Boolean check = this.Methods.checkDisabled(event.getPlugin());

            if(check) {
                this.plugin.paymentMethod = null;
                System.out.println("[WorldGuard] Payment method was disabled. No longer accepting payments.");
            }
        }
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        if (!this.Methods.hasMethod()) {
            if(this.Methods.setMethod(event.getPlugin())) {
                this.plugin.paymentMethod = this.Methods.getMethod();
                System.out.println("[WorldGuard] Payment method found (" + this.plugin.paymentMethod.getName() + " version: " + this.plugin.paymentMethod.getVersion() + ")");
            }
        }
    }
}
