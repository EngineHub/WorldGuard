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

package com.sk89q.worldguard.chest;

import com.sk89q.worldedit.blocks.BlockID;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

/**
 * Sign-based chest protection.
 * 
 * @author sk89q
 */
public class SignChestProtection implements ChestProtection {
    
    public boolean isProtected(Block block, Player player) {
        if (isChest(block.getTypeId())) {
            Block below = block.getRelative(0, -1, 0);
            return isProtectedSignAround(below, player);
        } else if (block.getTypeId() == BlockID.SIGN_POST) {
            return isProtectedSignAndChestBinary(block, player);
        } else {
            Block above = block.getRelative(0, 1, 0);
            Boolean res = isProtectedSign(above, player);
            if (res != null) return res;
            return false;
        }
    }
    
    public boolean isProtectedPlacement(Block block, Player player) {
        return isProtectedSignAround(block, player);
    }
    
    private boolean isProtectedSignAround(Block searchBlock, Player player) {
        Block side;
        Boolean res;
        
        side = searchBlock;
        res = isProtectedSign(side, player);
        if (res != null && res) return res;
        
        side = searchBlock.getRelative(-1, 0, 0);
        res = isProtectedSignAndChest(side, player);
        if (res != null && res) return res;
        
        side = searchBlock.getRelative(1, 0, 0);
        res = isProtectedSignAndChest(side, player);
        if (res != null && res) return res;
        
        side = searchBlock.getRelative(0, 0, -1);
        res = isProtectedSignAndChest(side, player);
        if (res != null && res) return res;
        
        side = searchBlock.getRelative(0, 0, 1);
        res = isProtectedSignAndChest(side, player);
        if (res != null && res) return res;
        
        return false;
    }
    
    private Boolean isProtectedSign(Sign sign, Player player) {
        if (sign.getLine(0).equalsIgnoreCase("[Lock]")) {
            if (player == null) { // No player, no access
                return true;
            }
            
            String name = player.getName();
            if (name.equalsIgnoreCase(sign.getLine(1).trim())
                    || name.equalsIgnoreCase(sign.getLine(2).trim())
                    || name.equalsIgnoreCase(sign.getLine(3).trim())) {
                return false;
            }
            
            // No access!
            return true;
        }
        
        return null;
    }
    
    private Boolean isProtectedSign(Block block, Player player) {
        BlockState state = block.getState();
        if (state == null || !(state instanceof Sign)) {
            return null;
        }
        return isProtectedSign((Sign) state, player);
    }
    
    private Boolean isProtectedSignAndChest(Block block, Player player) {
        if (!isChest(block.getRelative(0, 1, 0).getTypeId())) {
            return null;
        }
        return isProtectedSign(block, player);
    }
    
    private boolean isProtectedSignAndChestBinary(Block block, Player player) {
        Boolean res = isProtectedSignAndChest(block, player);
        return !(res == null || !res);
    }

    public boolean isAdjacentChestProtected(Block searchBlock, Player player) {
        Block side;
        Boolean res;
        
        side = searchBlock;
        res = isProtected(side, player);
        if (res != null && res) return res;
        
        side = searchBlock.getRelative(-1, 0, 0);
        res = isProtected(side, player);
        if (res != null && res) return res;
        
        side = searchBlock.getRelative(1, 0, 0);
        res = isProtected(side, player);
        if (res != null && res) return res;
        
        side = searchBlock.getRelative(0, 0, -1);
        res = isProtected(side, player);
        if (res != null && res) return res;
        
        side = searchBlock.getRelative(0, 0, 1);
        res = isProtected(side, player);
        if (res != null && res) return res;
        
        return false;
    }

    @Deprecated
    public boolean isChest(Material material) {
        return isChest(material.getId());
    }

    public boolean isChest(int type) {
        return type == BlockID.CHEST
                || type == BlockID.DISPENSER
                || type == BlockID.FURNACE
                || type == BlockID.BURNING_FURNACE;
    }
}
