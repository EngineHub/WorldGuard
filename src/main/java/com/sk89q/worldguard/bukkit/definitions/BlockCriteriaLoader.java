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

import java.util.List;

import com.sk89q.rebar.config.AbstractNodeLoader;
import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.config.LoaderBuilderException;
import com.sk89q.rebar.config.types.EnumLoaderBuilder;
import com.sk89q.rebar.config.types.MaterialPatternLoaderBuilder;
import com.sk89q.rebar.util.LoggerUtils;
import com.sk89q.rebar.util.MaterialPattern;
import com.sk89q.rulelists.DefinitionException;
import com.sk89q.rulelists.RuleListUtils;
import com.sk89q.rulelists.RuleListsManager;
import com.sk89q.worldguard.bukkit.definitions.BlockCriteria.Direction;
import com.sk89q.worldguard.bukkit.resolvers.BlockResolver;

public class BlockCriteriaLoader extends AbstractNodeLoader<BlockCriteria> {

    private final RuleListsManager manager;
    private MaterialPatternLoaderBuilder materialLoader = new MaterialPatternLoaderBuilder();
    private EnumLoaderBuilder<Direction> dirLoader = new EnumLoaderBuilder<Direction>(Direction.class);

    public BlockCriteriaLoader(RuleListsManager manager) {
        this.manager = manager;
    }

    @Override
    public BlockCriteria read(ConfigurationNode node)
            throws DefinitionException {
        BlockResolver resolver = manager.getResolvers()
                .get(BlockResolver.class, node.getString("block", "target"));

        // Load patterns
        List<MaterialPattern> patterns = node.contains(INLINE) ?
                node.listOf(INLINE, materialLoader) : node.listOf("material", materialLoader);
        if (patterns.size() == 0) {
            throw new LoaderBuilderException("No block materials specified");
        }

        // Load direction
        Direction direction = node.getOf("relative", dirLoader, Direction.AT);

        BlockCriteria criteria = new BlockCriteria(resolver);
        criteria.setPatterns(patterns);
        criteria.setDirection(direction);

        RuleListUtils.warnUnknown(node, LoggerUtils.getLogger(getClass()),
                                  "block", "material", "relative");

        return criteria;
    }

}