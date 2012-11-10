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
import org.bukkit.entity.Painting;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import com.sk89q.rulelists.KnownAttachment;
import com.sk89q.rulelists.RuleSet;

/**
 * Listener for painting related events. This event is deprecated and it is provided for
 * compatibility with older versions of Bukkit.
 */
@Deprecated
public class WorldGuardPaintingListener implements Listener {

    private WorldGuardPlugin plugin;

    /**
     * Construct the listener.
     *
     * @param plugin WorldGuard plugin
     */
    WorldGuardPaintingListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register events.
     */
    void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPaintingBreak(PaintingBreakEvent event) {
        Painting painting = event.getPainting();
        World world = painting.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ENTITY_DAMAGE);
        BukkitContext context = new BukkitContext(plugin, event);
        if (event instanceof PaintingBreakByEntityEvent) {
            context.setSourceEntity(((PaintingBreakByEntityEvent) event).getRemover());
        }
        context.setTargetEntity(event.getPainting());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPaintingPlace(PaintingPlaceEvent event) {
        Block placedOn = event.getBlock();
        World world = placedOn.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ENTITY_SPAWN);
        BukkitContext context = new BukkitContext(plugin, event);
        context.setSourceEntity(event.getPlayer());
        context.setTargetEntity(event.getPainting());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }
}
