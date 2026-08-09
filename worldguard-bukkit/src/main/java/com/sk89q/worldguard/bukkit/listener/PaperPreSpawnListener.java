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

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitConfigurationManager;
import com.sk89q.worldguard.bukkit.BukkitWorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.Set;

/**
 * Cancels blocked natural spawns before the server constructs the entity.
 *
 * The CreatureSpawnEvent checks in {@link WorldGuardEntityListener#onCreatureSpawn}
 * fire at the very end of the spawn pipeline: by then the server has picked a spawn
 * position, run the placement checks, constructed the mob and run finalizeSpawn, and
 * the cancelled mob is thrown away. Since a cancelled spawn never counts toward the
 * mob cap, the natural spawner keeps retrying the same area at full rate, so regions
 * that deny mob-spawning become permanent spawn attempt hotspots that pay entity
 * construction over and over for nothing.
 *
 * This listener applies the same natural spawn checks in Paper's
 * PreCreatureSpawnEvent, before the entity exists. Cancelling at that stage also
 * makes the server end the remaining attempts for the chunk in that spawn cycle, so
 * the retry pressure disappears too: on a flat test world with a region denying
 * mob-spawning over the whole spawn range and the mob cap kept empty, the attempt
 * rate around a single player collapsed from roughly 75000 attempts per second to
 * roughly 60 per second, with no entities constructed.
 *
 * Only NATURAL spawns are handled; every other spawn reason keeps going through the
 * CreatureSpawnEvent checks unchanged. The listener is only registered when the
 * Paper event is available.
 */
public class PaperPreSpawnListener extends AbstractListener {

    public PaperPreSpawnListener(WorldGuardPlugin plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPreCreatureSpawn(PreCreatureSpawnEvent event) {
        if (event.getReason() != SpawnReason.NATURAL) {
            return;
        }

        BukkitConfigurationManager cfg = getConfig();

        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }

        Location eventLoc = event.getSpawnLocation();
        BukkitWorldConfiguration wcfg = getWorldConfig(eventLoc.getWorld());

        EntityType entityType = event.getType();
        com.sk89q.worldedit.world.entity.EntityType weEntityType = BukkitAdapter.adapt(entityType);

        if (weEntityType != null && wcfg.blockCreatureSpawn.contains(weEntityType)) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.useRegions && cfg.useRegionsCreatureSpawnEvent) {
            ApplicableRegionSet set =
                    WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(eventLoc));

            if (!set.testState(null, Flags.MOB_SPAWNING)) {
                event.setCancelled(true);
                return;
            }

            Set<com.sk89q.worldedit.world.entity.EntityType> entityTypes = set.queryValue(null, Flags.DENY_SPAWN);
            if (entityTypes != null && weEntityType != null && entityTypes.contains(weEntityType)) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.blockGroundSlimes && entityType == EntityType.SLIME && eventLoc.getY() >= 60) {
            event.setCancelled(true);
        }
    }
}
