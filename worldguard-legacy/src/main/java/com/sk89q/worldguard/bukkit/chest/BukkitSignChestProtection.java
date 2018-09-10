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

package com.sk89q.worldguard.bukkit.chest;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.chest.SignChestProtection;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

public class BukkitSignChestProtection extends SignChestProtection {

    private Boolean isProtectedSign(Sign sign, LocalPlayer player) {
        if (sign.getLine(0).equalsIgnoreCase("[Lock]")) {
            if (player == null) { // No player, no access
                return true;
            }
            
            String name = player.getName();
            return !name.equalsIgnoreCase(sign.getLine(1).trim())
                    && !name.equalsIgnoreCase(sign.getLine(2).trim())
                    && !name.equalsIgnoreCase(sign.getLine(3).trim());
        }
        
        return null;
    }

    @Override
    public Boolean isProtectedSign(Location block, LocalPlayer player) {
        BlockState state = BukkitAdapter.adapt(block).getBlock().getState();
        if (!(state instanceof Sign)) {
            return null;
        }
        return isProtectedSign((Sign) state, player);
    }
}
