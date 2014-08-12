/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.bukkit.listener;

import com.google.common.base.Joiner;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.bukkit.event.entity.DestroyEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.SpawnEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.UseEntityEvent;
import com.sk89q.worldguard.bukkit.event.inventory.UseItemEvent;
import com.sk89q.worldguard.util.cause.Cause;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;

import java.util.List;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

public class DebuggingListener extends AbstractListener {

    private final Logger logger;

    /**
     * Construct the listener.
     *
     * @param plugin an instance of WorldGuardPlugin
     * @param logger the logger
     */
    public DebuggingListener(WorldGuardPlugin plugin, Logger logger) {
        super(plugin);
        checkNotNull(logger);
        this.logger = logger;
    }

    @EventHandler
    public void onPlaceBlock(PlaceBlockEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append("PLACE");
        builder.append(" ");
        builder.append("").append(event.getEffectiveMaterial());
        builder.append(" ");
        builder.append("@").append(toBlockString(event.getTarget()));
        builder.append(" ");
        builder.append("[").append(toCause(event.getCauses())).append("]");
        builder.append(" ");
        builder.append(":").append(event.getOriginalEvent().getEventName());
        logger.info(builder.toString());
    }

    @EventHandler
    public void onBreakBlock(BreakBlockEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append("DIG");
        builder.append(" ");
        builder.append("").append(event.getEffectiveMaterial());
        builder.append(" ");
        builder.append("[").append(toCause(event.getCauses())).append("]");
        builder.append(" ");
        builder.append("@").append(toBlockString(event.getTarget()));
        builder.append(" ");
        builder.append(":").append(event.getOriginalEvent().getEventName());
        logger.info(builder.toString());
    }

    @EventHandler
    public void onUseBlock(UseBlockEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append("INTERACT");
        builder.append(" ");
        builder.append("").append(event.getEffectiveMaterial());
        builder.append(" ");
        builder.append("[").append(toCause(event.getCauses())).append("]");
        builder.append(" ");
        builder.append("@").append(toBlockString(event.getTarget()));
        builder.append(" ");
        builder.append(":").append(event.getOriginalEvent().getEventName());
        logger.info(builder.toString());
    }

    @EventHandler
    public void onSpawnEntity(SpawnEntityEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append("SPAWN");
        builder.append(" ");
        builder.append("").append(event.getEffectiveType());
        builder.append(" ");
        builder.append("[").append(toCause(event.getCauses())).append("]");
        builder.append(" ");
        builder.append("@").append(toBlockString(event.getTarget()));
        builder.append(" ");
        builder.append(":").append(event.getOriginalEvent().getEventName());
        logger.info(builder.toString());
    }

    @EventHandler
    public void onDestroyEntity(DestroyEntityEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append("DESTROY");
        builder.append(" ");
        builder.append("").append(event.getEntity().getType());
        builder.append(" ");
        builder.append("[").append(toCause(event.getCauses())).append("]");
        builder.append(" ");
        builder.append("@").append(toBlockString(event.getTarget()));
        builder.append(" ");
        builder.append(":").append(event.getOriginalEvent().getEventName());
        logger.info(builder.toString());
    }

    @EventHandler
    public void onUseEntity(UseEntityEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append("INTERACT");
        builder.append(" ");
        builder.append("").append(event.getEntity().getType());
        builder.append(" ");
        builder.append("[").append(toCause(event.getCauses())).append("]");
        builder.append(" ");
        builder.append("@").append(toBlockString(event.getTarget()));
        builder.append(" ");
        builder.append(":").append(event.getOriginalEvent().getEventName());
        logger.info(builder.toString());
    }

    @EventHandler
    public void onUseItem(UseItemEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append("USE");
        builder.append(" ");
        builder.append("").append(event.getItemStack().getType());
        builder.append(" ");
        builder.append("[").append(toCause(event.getCauses())).append("]");
        builder.append(" ");
        builder.append("@").append(event.getWorld().getName());
        builder.append(" ");
        builder.append(":").append(event.getOriginalEvent().getEventName());
        logger.info(builder.toString());
    }

    private static String toCause(List<? extends Cause<?>> causes) {
        return Joiner.on("|").join(causes);
    }

    private static String toBlockString(Location location) {
        return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

}
