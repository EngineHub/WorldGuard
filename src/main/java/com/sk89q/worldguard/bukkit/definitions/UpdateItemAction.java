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

package com.sk89q.worldguard.bukkit.definitions;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.sk89q.rulelists.Action;
import com.sk89q.worldguard.bukkit.BukkitContext;
import com.sk89q.worldguard.bukkit.resolvers.EntityResolver;
import com.sk89q.worldguard.bukkit.resolvers.ItemStackSlot;
import com.sk89q.worldguard.bukkit.resolvers.ItemStackSlotResolver;

public class UpdateItemAction implements Action<BukkitContext> {

    private final EntityResolver entityResolver;
    private final ItemStackSlotResolver itemResolver;
    private boolean destroy = false;
    private short newData = -1;

    public UpdateItemAction(EntityResolver entityResolver, ItemStackSlotResolver itemResolver) {
        this.entityResolver = entityResolver;
        this.itemResolver = itemResolver;
    }

    public boolean isDestroy() {
        return destroy;
    }

    public void setDestroy(boolean destroy) {
        this.destroy = destroy;
    }

    public short getNewData() {
        return newData;
    }

    public void setNewData(short newData) {
        this.newData = newData;
    }

    @Override
    public void apply(BukkitContext context) {
        ItemStackSlot slot = null;
        ItemStack item;

        if (entityResolver == null) {
            item = context.getItem();
        } else {
            Entity entity = entityResolver.resolve(context);

            if (entity == null) {
                return;
            }

            slot = itemResolver.resolve(entity);
            item = slot.get();
        }

        if (item == null) {
            return;
        }

        boolean updated = false;

        if (destroy) {
            item = null;
            updated = true;
        } else if (newData >= 0) {
            item.setDurability(newData);
            updated = true;
        }

        if (updated && slot != null) {
            slot.update(item);
        }
    }

}