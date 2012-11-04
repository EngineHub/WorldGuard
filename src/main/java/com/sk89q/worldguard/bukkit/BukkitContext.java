// $Id$
/*
 * This file is a part of WorldGuard.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.bukkit;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import com.sk89q.rulelists.Context;

public class BukkitContext extends Context {

    private final Event event;
    private String message;
    private Entity sourceEntity;
    private Entity targetEntity;
    private ItemStack item;
    private BlockState sourceBlock;
    private BlockState targetBlock;
    private BlockState replacedTargetBlock;

    public BukkitContext(Event event) {
        this.event = event;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Entity getSourceEntity() {
        return sourceEntity;
    }

    public void setSourceEntity(Entity sourceEntity) {
        this.sourceEntity = sourceEntity;
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(Entity targetEntity) {
        this.targetEntity = targetEntity;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public BlockState getSourceBlock() {
        return sourceBlock;
    }

    public void setSourceBlock(BlockState block) {
        this.sourceBlock = block;
    }

    public BlockState getTargetBlock() {
        return targetBlock;
    }

    public void setTargetBlock(BlockState targetBlock) {
        this.targetBlock = targetBlock;
    }

    public BlockState getPlacedBlock() {
        return replacedTargetBlock;
    }

    public void setPlacedBlock(BlockState replacedTargetBlock) {
        this.replacedTargetBlock = replacedTargetBlock;
    }

    public Event getEvent() {
        return event;
    }

}
