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

import java.util.Set;

import com.sk89q.rebar.config.AbstractNodeLoader;
import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.config.types.EnumLoaderBuilder;
import com.sk89q.rebar.util.LoggerUtils;
import com.sk89q.rulelists.DefinitionException;
import com.sk89q.rulelists.RuleListUtils;

public class PhenomenonCriteriaLoader extends AbstractNodeLoader<PhenomenonCriteria> {

    public enum Phenomenon {
        LIGHTNING;
    }

    private EnumLoaderBuilder<Phenomenon> typeLoader =
            new EnumLoaderBuilder<Phenomenon>(Phenomenon.class);

    @Override
    public PhenomenonCriteria read(ConfigurationNode node) throws DefinitionException {
        Set<Phenomenon> types = node.contains(INLINE) ?
                node.setOf(INLINE, typeLoader) :
                node.setOf("type", typeLoader);

        PhenomenonCriteria criteria = new PhenomenonCriteria(types.contains(Phenomenon.LIGHTNING));

        RuleListUtils.warnUnknown(node, LoggerUtils.getLogger(getClass()),
                                  "type");

        return criteria;
    }

}