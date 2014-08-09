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

package com.sk89q.worldguard.bukkit.listener.module;

import com.google.common.base.Function;
import com.sk89q.worldguard.bukkit.listener.Materials;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

import static com.sk89q.worldguard.bukkit.BukkitUtil.isBlockWater;
import static com.sk89q.worldguard.bukkit.BukkitUtil.setBlockToWater;

public class SpongeListener implements Listener {

    private final Function<World, SpongeBehavior> function;

    public SpongeListener(Function<World, SpongeBehavior> function) {
        this.function = function;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        World world = event.getBlock().getWorld();
        Block blockFrom = event.getBlock();
        Block blockTo = event.getToBlock();

        SpongeBehavior behavior = function.apply(world);

        if (behavior.enabled && Materials.isWater(blockFrom.getType())) {
            int ox = blockTo.getX();
            int oy = blockTo.getY();
            int oz = blockTo.getZ();

            for (int cx = -behavior.radius; cx <= behavior.radius; cx++) {
                for (int cy = -behavior.radius; cy <= behavior.radius; cy++) {
                    for (int cz = -behavior.radius; cz <= behavior.radius; cz++) {
                        Block sponge = world.getBlockAt(ox + cx, oy + cy, oz + cz);

                        if (sponge.getType() == Material.SPONGE && (!behavior.redstone || !sponge.isBlockIndirectlyPowered())) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block target = event.getBlock();
        World world = target.getWorld();

        SpongeBehavior behavior = function.apply(world);

        if (behavior.enabled && target.getType() == Material.SPONGE) {
            if (behavior.redstone && target.isBlockIndirectlyPowered()) {
                return;
            }

            int ox = target.getX();
            int oy = target.getY();
            int oz = target.getZ();

            clearSpongeWater(world, ox, oy, oz, behavior);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        Block blockTo = event.getBlock();
        World world = blockTo.getWorld();

        SpongeBehavior behavior = function.apply(world);

        if (behavior.enabled && behavior.redstone) {
            int ox = blockTo.getX();
            int oy = blockTo.getY();
            int oz = blockTo.getZ();

            for (int cx = -1; cx <= 1; cx++) {
                for (int cy = -1; cy <= 1; cy++) {
                    for (int cz = -1; cz <= 1; cz++) {
                        Block sponge = world.getBlockAt(ox + cx, oy + cy, oz + cz);
                        if (sponge.getType() == Material.SPONGE && sponge.isBlockIndirectlyPowered()) {
                            clearSpongeWater(world, ox + cx, oy + cy, oz + cz, behavior);
                        } else if (sponge.getType() == Material.SPONGE && !sponge.isBlockIndirectlyPowered()) {
                            addSpongeWater(world, ox + cx, oy + cy, oz + cz, behavior);
                        }
                    }
                }
            }
        }
    }

    private static void clearSpongeWater(World world, int ox, int oy, int oz, SpongeBehavior behavior) {
        for (int cx = -behavior.radius; cx <= behavior.radius; cx++) {
            for (int cy = -behavior.radius; cy <= behavior.radius; cy++) {
                for (int cz = -behavior.radius; cz <= behavior.radius; cz++) {
                    if (isBlockWater(world, ox + cx, oy + cy, oz + cz)) {
                        world.getBlockAt(ox + cx, oy + cy, oz + cz).setType(Material.AIR);
                    }
                }
            }
        }
    }

    private static void addSpongeWater(World world, int ox, int oy, int oz, SpongeBehavior behavior) {
        // The negative x edge
        int cx = ox - behavior.radius - 1;
        for (int cy = oy - behavior.radius - 1; cy <= oy + behavior.radius + 1; cy++) {
            for (int cz = oz - behavior.radius - 1; cz <= oz + behavior.radius + 1; cz++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx + 1, cy, cz);
                }
            }
        }

        // The positive x edge
        cx = ox + behavior.radius + 1;
        for (int cy = oy - behavior.radius - 1; cy <= oy + behavior.radius + 1; cy++) {
            for (int cz = oz - behavior.radius - 1; cz <= oz + behavior.radius + 1; cz++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx - 1, cy, cz);
                }
            }
        }

        // The negative y edge
        int cy = oy - behavior.radius - 1;
        for (cx = ox - behavior.radius - 1; cx <= ox + behavior.radius + 1; cx++) {
            for (int cz = oz - behavior.radius - 1; cz <= oz + behavior.radius + 1; cz++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx, cy + 1, cz);
                }
            }
        }

        // The positive y edge
        cy = oy + behavior.radius + 1;
        for (cx = ox - behavior.radius - 1; cx <= ox + behavior.radius + 1; cx++) {
            for (int cz = oz - behavior.radius - 1; cz <= oz + behavior.radius + 1; cz++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx, cy - 1, cz);
                }
            }
        }

        // The negative z edge
        int cz = oz - behavior.radius - 1;
        for (cx = ox - behavior.radius - 1; cx <= ox + behavior.radius + 1; cx++) {
            for (cy = oy - behavior.radius - 1; cy <= oy + behavior.radius + 1; cy++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx, cy, cz + 1);
                }
            }
        }

        // The positive z edge
        cz = oz + behavior.radius + 1;
        for (cx = ox - behavior.radius - 1; cx <= ox + behavior.radius + 1; cx++) {
            for (cy = oy - behavior.radius - 1; cy <= oy + behavior.radius + 1; cy++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx, cy, cz - 1);
                }
            }
        }
    }

    public static class SpongeBehavior {
        private final boolean enabled;
        private final int radius;
        private final boolean redstone;

        public SpongeBehavior(boolean enabled, int radius, boolean redstone) {
            this.enabled = enabled;
            this.radius = radius;
            this.redstone = redstone;
        }
    }

}
