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

import com.sk89q.rebar.config.AbstractNodeLoader;
import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.util.LoggerUtils;
import com.sk89q.rulelists.DefinitionException;
import com.sk89q.rulelists.RuleListUtils;
import com.sk89q.rulelists.RuleListsManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.resolvers.EntityResolver;

public class UpdateEntityActionLoader extends AbstractNodeLoader<UpdateEntityAction> {

    private final WorldGuardPlugin wg;
    private final RuleListsManager manager;

    public UpdateEntityActionLoader(WorldGuardPlugin wg, RuleListsManager manager) {
        this.wg = wg;
        this.manager = manager;
    }

    @Override
    public UpdateEntityAction read(ConfigurationNode node) throws DefinitionException {
        EntityResolver entityResolver = manager.getResolvers()
                .get(EntityResolver.class, node.getString("entity", "source"));

        UpdateEntityAction criteria = new UpdateEntityAction(wg, entityResolver);

        criteria.setExplode(node.getBoolean("explode"));
        criteria.setFallDistance(node.getFloat("set-fall-distance"));
        criteria.setFireTicks(node.getInt("set-fire-ticks"));
        criteria.setTeleportSafely(node.getBoolean("teleport-safely"));

        criteria.setHealth(node.getInt("set-health"));
        criteria.setDamage(node.getInt("damage"));
        criteria.setRemainingAir(node.getInt("set-air"));

        criteria.setFoodLevel(node.getInt("food-level"));
        criteria.setSaturationLevel(node.getFloat("set-saturation"));
        criteria.setExhaustionLevel(node.getFloat("set-exhaustion"));
        criteria.setWalkSpeed(node.getFloat("set-walk-speed"));
        criteria.setAllowFlight(node.getBoolean("set-allow-flight"));
        criteria.setFlying(node.getBoolean("set-flying"));
        criteria.setFlySpeed(node.getFloat("set-fly-speed"));
        criteria.setSneaking(node.getBoolean("set-sneaking"));
        criteria.setSprinting(node.getBoolean("set-sprinting"));
        criteria.setInvincible(node.getBoolean("set-invincible"));
        criteria.setOpenWorkbench(node.getBoolean("open-workbench"));
        criteria.setPerformCommand(node.getString("perform-command"));

        RuleListUtils.warnUnknown(node, LoggerUtils.getLogger(getClass()),
                                  "entity", "explode", "set-fall-distance",
                                  "set-fire-ticks", "set-health", "damage", "set-air",
                                  "food-level", "set-saturation", "set-exhaustion",
                                  "set-walk-speed", "set-allow-flight", "set-flying",
                                  "set-fly-speed", "set-sneaking", "set-sprinting",
                                  "set-invincible", "open-workbench", "perform-command",
                                  "teleport-safely");

        return criteria;
    }

}