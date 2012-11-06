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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.rebar.util.MaterialPattern;
import com.sk89q.rulelists.Criteria;
import com.sk89q.worldguard.bukkit.BukkitContext;
import com.sk89q.worldguard.bukkit.resolvers.EntityResolver;
import com.sk89q.worldguard.bukkit.resolvers.ItemStackSlotResolver;

public class ItemCriteria implements Criteria<BukkitContext> {

    private final EntityResolver entityResolver;
    private final ItemStackSlotResolver itemResolver;
    private MaterialPattern[] patterns = new MaterialPattern[0];
    private Set<PotionEffectType> potionEffects = new HashSet<PotionEffectType>();
    private Boolean hasData = false;

    public ItemCriteria(EntityResolver entityResolver, ItemStackSlotResolver itemResolver) {
        this.entityResolver = entityResolver;
        this.itemResolver = itemResolver;
    }

    public Boolean hasDataCheck() {
        return hasData;
    }

    public void setDataCheck(Boolean hasData) {
        this.hasData = hasData;
    }

    public MaterialPattern[] getPatterns() {
        return patterns;
    }

    public void setPatterns(MaterialPattern[] patterns) {
        this.patterns = patterns;
    }

    public void setPatterns(List<MaterialPattern> patterns) {
        MaterialPattern[] arr = new MaterialPattern[patterns.size()];
        patterns.toArray(arr);
        this.patterns = arr;
    }

    public Set<PotionEffectType> getPotionEffects() {
        return potionEffects;
    }

    public void setPotionEffects(Set<PotionEffectType> effects) {
        this.potionEffects = effects;
    }

    @Override
    public boolean matches(BukkitContext context) {
        ItemStack item;
        boolean found = false;

        if (entityResolver == null) {
            item = context.getItem();
        } else {
            Entity entity = entityResolver.resolve(context);

            if (entity == null) {
                return false;
            }

            item = itemResolver.resolve(entity).get();
        }

        if (item == null) {
            return false;
        }

        if (hasData != null) {
            found = hasData == (item.getDurability() != 0);
            if (!found) {
                return false;
            }
        }

        if (item.getType() == Material.POTION && potionEffects.size() > 0) {
            for (PotionEffect effect : Potion.fromItemStack(item).getEffects()) {
                if (potionEffects.contains(effect.getType())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        for (MaterialPattern pattern : patterns) {
            if (pattern.matches(item.getTypeId(), item.getDurability())) {
                found = true;
                break;
            }
        }

        return found;
    }

}