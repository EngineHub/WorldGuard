// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.bukkit;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Hanging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import com.sk89q.rulelists.DefaultAttachments;
import com.sk89q.rulelists.RuleSet;

/**
 * Listener for hanging entity events.
 */
class WorldGuardHangingListener implements Listener {

    private WorldGuardPlugin plugin;

    /**
     * Construct the listener.
     *
     * @param plugin WorldGuard plugin
     */
    WorldGuardHangingListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register events.
     */
    void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingingBreak(HangingBreakEvent event) {
        Hanging hanging = event.getEntity();
        World world = hanging.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(DefaultAttachments.ENTITY_DAMAGE);
        BukkitContext context = new BukkitContext(plugin, event);
        if (event instanceof HangingBreakByEntityEvent) {
            context.setSourceEntity(((HangingBreakByEntityEvent) event).getRemover());
        }
        context.setTargetEntity(event.getEntity());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Block placedOn = event.getBlock();
        World world = placedOn.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(DefaultAttachments.ENTITY_SPAWN);
        BukkitContext context = new BukkitContext(plugin, event);
        context.setSourceEntity(event.getPlayer());
        context.setTargetEntity(event.getEntity());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }
}
