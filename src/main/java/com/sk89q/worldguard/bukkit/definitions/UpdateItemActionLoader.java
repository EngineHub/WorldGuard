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
import com.sk89q.worldguard.bukkit.resolvers.EntityResolver;
import com.sk89q.worldguard.bukkit.resolvers.ItemStackSlotResolver;

public class UpdateItemActionLoader extends AbstractNodeLoader<UpdateItemAction> {

    private final RuleListsManager manager;

    public UpdateItemActionLoader(RuleListsManager manager) {
        this.manager = manager;
    }

    @Override
    public UpdateItemAction read(ConfigurationNode node) throws DefinitionException {
        String entity = node.getString("entity");
        EntityResolver entityResolver = null;
        ItemStackSlotResolver resolver = null;

        if (entity != null) {
            entityResolver = manager.getResolvers()
                    .get(EntityResolver.class, entity);
            resolver = manager.getResolvers()
                    .get(ItemStackSlotResolver.class, node.getString("item", ItemStackSlotResolver.DEFAULT));
        }

        boolean destroy = node.getBoolean("destroy", false);
        short newData = (short) node.getInt("set-data", -1);

        UpdateItemAction action = new UpdateItemAction(entityResolver, resolver);
        action.setDestroy(destroy);
        action.setNewData(newData);

        RuleListUtils.warnUnknown(node, LoggerUtils.getLogger(getClass()),
                                  "item", "entity", "destroy", "set-data");

        return action;
    }

}