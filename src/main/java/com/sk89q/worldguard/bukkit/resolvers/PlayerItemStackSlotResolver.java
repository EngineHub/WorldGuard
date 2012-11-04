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

package com.sk89q.worldguard.bukkit.resolvers;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerItemStackSlotResolver implements ItemStackSlotResolver {

    public enum Slot {
        HELD,
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS;
    }

    private final Slot slot;

    public PlayerItemStackSlotResolver(Slot slot) {
        this.slot = slot;
    }

    @Override
    public ItemStackSlot resolve(Entity entity) {
        if (entity != null && entity instanceof HumanEntity) {
            final PlayerInventory inventory = ((HumanEntity) entity).getInventory();

            switch (slot) {
            case HELD:
                final ItemStack item = nonNull(inventory.getItemInHand());
                return new ItemStackSlot() {
                    @Override
                    public void update(ItemStack stack) {
                        inventory.setItemInHand(stack);
                    }

                    @Override
                    public ItemStack get() {
                        return item;
                    }
                };

            case HELMET:
                final ItemStack helmet = nonNull(inventory.getHelmet());
                return new ItemStackSlot() {
                    @Override
                    public void update(ItemStack stack) {
                        inventory.setHelmet(stack);
                    }

                    @Override
                    public ItemStack get() {
                        return helmet;
                    }
                };

            case CHESTPLATE:
                final ItemStack chestPlate = nonNull(inventory.getChestplate());
                return new ItemStackSlot() {
                    @Override
                    public void update(ItemStack stack) {
                        inventory.setChestplate(stack);
                    }

                    @Override
                    public ItemStack get() {
                        return chestPlate;
                    }
                };

            case LEGGINGS:
                final ItemStack leggings = nonNull(inventory.getLeggings());
                return new ItemStackSlot() {
                    @Override
                    public void update(ItemStack stack) {
                        inventory.setLeggings(stack);
                    }

                    @Override
                    public ItemStack get() {
                        return leggings;
                    }
                };

            case BOOTS:
                final ItemStack boots = nonNull(inventory.getBoots());
                return new ItemStackSlot() {
                    @Override
                    public void update(ItemStack stack) {
                        inventory.setBoots(stack);
                    }

                    @Override
                    public ItemStack get() {
                        return boots;
                    }
                };
            }
        }

        return null;
    }

    private static ItemStack nonNull(ItemStack item) {
        return item == null ? new ItemStack(0) : item;
    }

}
