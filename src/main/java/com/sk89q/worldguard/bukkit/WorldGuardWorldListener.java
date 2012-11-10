// $Id$
/*
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * All rights reserved.
*/
package com.sk89q.worldguard.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import com.sk89q.rulelists.KnownAttachment;
import com.sk89q.rulelists.RuleSet;

/**
 * Listener for world events.
 */
public class WorldGuardWorldListener implements Listener {

    private WorldGuardPlugin plugin;

    /**
     * Construct the listener.
     *
     * @param plugin WorldGuard plugin
     */
    public WorldGuardWorldListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register the events.
     */
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getWorld());

        /* --- No short-circuit returns below this line --- */

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.WORLD_LOAD);
        BukkitContext context = new BukkitContext(plugin, event);
        rules.process(context);
    }

    @EventHandler
    public void onWorldLoad(WorldUnloadEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.WORLD_UNLOAD);
        BukkitContext context = new BukkitContext(plugin, event);
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getWorld());

        /* --- No short-circuit returns below this line --- */

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.WORLD_SAVE);
        BukkitContext context = new BukkitContext(plugin, event);
        rules.process(context);
    }
}
