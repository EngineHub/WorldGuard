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

import static com.sk89q.rulelists.RuleEntryLoader.INLINE;

import com.sk89q.rebar.config.AbstractNodeLoader;
import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.util.LoggerUtils;
import com.sk89q.rulelists.DefinitionException;
import com.sk89q.rulelists.RuleListUtils;
import com.sk89q.rulelists.RuleListsManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.resolvers.EntityResolver;

public class PermissionCriteriaLoader extends AbstractNodeLoader<PermissionCriteria> {

    private final WorldGuardPlugin wg;
    private final RuleListsManager manager;

    public PermissionCriteriaLoader(WorldGuardPlugin wg, RuleListsManager manager) {
        this.wg = wg;
        this.manager = manager;
    }

    @Override
    public PermissionCriteria read(ConfigurationNode node) throws DefinitionException {
        EntityResolver entityResolver = manager.getResolvers()
                .get(EntityResolver.class, node.getString("entity", "source"));

        String permission = node.contains(INLINE) ?
                node.getString(INLINE, "") :
                node.getString("permission", "");

        PermissionCriteria criteria = new PermissionCriteria(wg, entityResolver);
        criteria.setPermission(permission);

        RuleListUtils.warnUnknown(node, LoggerUtils.getLogger(getClass()),
                                  "entity", "permission");

        return criteria;
    }

}