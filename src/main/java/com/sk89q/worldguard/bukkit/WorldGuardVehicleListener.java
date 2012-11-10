// $Id$
/*
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * All rights reserved.
*/
package com.sk89q.worldguard.bukkit;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import com.sk89q.rulelists.KnownAttachment;
import com.sk89q.rulelists.RuleSet;

/**
 * Listener for vehicle events.
 */
class WorldGuardVehicleListener implements Listener {

    private WorldGuardPlugin plugin;

    /**
     * Construct the listener.
     *
     * @param plugin WorldGuard plugin
     */
    WorldGuardVehicleListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register the events.
     */
    void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Vehicle vehicle = event.getVehicle();
        Entity destroyer = event.getAttacker();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(vehicle.getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ENTITY_DAMAGE);
        BukkitContext context = new BukkitContext(plugin, event);
        context.setSourceEntity(destroyer);
        context.setTargetEntity(vehicle);
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }
}
