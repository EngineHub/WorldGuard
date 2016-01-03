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

import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.World;

import static com.sk89q.worldguard.bukkit.BukkitUtil.isBlockWater;
import static com.sk89q.worldguard.bukkit.BukkitUtil.setBlockToWater;

public final class SpongeUtil {

    private SpongeUtil() {
    }

    /**
     * Remove water around a sponge.
     *
     * @param plugin The plugin instace
     * @param world The world the sponge isin
     * @param ox The x coordinate of the 'sponge' block
     * @param oy The y coordinate of the 'sponge' block
     * @param oz The z coordinate of the 'sponge' block
     */
    public static void clearSpongeWater(WorldGuardPlugin plugin, World world, int ox, int oy, int oz) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        for (int cx = -wcfg.spongeRadius; cx <= wcfg.spongeRadius; cx++) {
            for (int cy = -wcfg.spongeRadius; cy <= wcfg.spongeRadius; cy++) {
                for (int cz = -wcfg.spongeRadius; cz <= wcfg.spongeRadius; cz++) {
                    if (isBlockWater(world, ox + cx, oy + cy, oz + cz)) {
                        world.getBlockAt(ox + cx, oy + cy, oz + cz).setTypeId(0);
                    }
                }
            }
        }
    }

    /**
     * Add water around a sponge.
     * 
     * @param plugin The plugin instance
     * @param world The world the sponge is located in
     * @param ox The x coordinate of the 'sponge' block
     * @param oy The y coordinate of the 'sponge' block
     * @param oz The z coordinate of the 'sponge' block
     */
    public static void addSpongeWater(WorldGuardPlugin plugin, World world, int ox, int oy, int oz) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        // The negative x edge
        int cx = ox - wcfg.spongeRadius - 1;
        for (int cy = oy - wcfg.spongeRadius - 1; cy <= oy + wcfg.spongeRadius + 1; cy++) {
            for (int cz = oz - wcfg.spongeRadius - 1; cz <= oz + wcfg.spongeRadius + 1; cz++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx + 1, cy, cz);
                }
            }
        }

        // The positive x edge
        cx = ox + wcfg.spongeRadius + 1;
        for (int cy = oy - wcfg.spongeRadius - 1; cy <= oy + wcfg.spongeRadius + 1; cy++) {
            for (int cz = oz - wcfg.spongeRadius - 1; cz <= oz + wcfg.spongeRadius + 1; cz++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx - 1, cy, cz);
                }
            }
        }

        // The negative y edge
        int cy = oy - wcfg.spongeRadius - 1;
        for (cx = ox - wcfg.spongeRadius - 1; cx <= ox + wcfg.spongeRadius + 1; cx++) {
            for (int cz = oz - wcfg.spongeRadius - 1; cz <= oz + wcfg.spongeRadius + 1; cz++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx, cy + 1, cz);
                }
            }
        }

        // The positive y edge
        cy = oy + wcfg.spongeRadius + 1;
        for (cx = ox - wcfg.spongeRadius - 1; cx <= ox + wcfg.spongeRadius + 1; cx++) {
            for (int cz = oz - wcfg.spongeRadius - 1; cz <= oz + wcfg.spongeRadius + 1; cz++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx, cy - 1, cz);
                }
            }
        }

        // The negative z edge
        int cz = oz - wcfg.spongeRadius - 1;
        for (cx = ox - wcfg.spongeRadius - 1; cx <= ox + wcfg.spongeRadius + 1; cx++) {
            for (cy = oy - wcfg.spongeRadius - 1; cy <= oy + wcfg.spongeRadius + 1; cy++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx, cy, cz + 1);
                }
            }
        }

        // The positive z edge
        cz = oz + wcfg.spongeRadius + 1;
        for (cx = ox - wcfg.spongeRadius - 1; cx <= ox + wcfg.spongeRadius + 1; cx++) {
            for (cy = oy - wcfg.spongeRadius - 1; cy <= oy + wcfg.spongeRadius + 1; cy++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx, cy, cz - 1);
                }
            }
        }
    }
}
