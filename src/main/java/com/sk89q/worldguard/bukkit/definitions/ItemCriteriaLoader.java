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
import java.util.Set;

import org.bukkit.potion.PotionEffectType;

import com.sk89q.rebar.config.AbstractNodeLoader;
import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.config.LoaderBuilderException;
import com.sk89q.rebar.config.types.MaterialPatternLoaderBuilder;
import com.sk89q.rebar.config.types.StaticFieldLoaderBuilder;
import com.sk89q.rebar.util.LoggerUtils;
import com.sk89q.rebar.util.MaterialPattern;
import com.sk89q.rulelists.DefinitionException;
import com.sk89q.rulelists.RuleListUtils;
import com.sk89q.rulelists.RuleListsManager;
import com.sk89q.worldguard.bukkit.resolvers.EntityResolver;
import com.sk89q.worldguard.bukkit.resolvers.ItemStackSlotResolver;

public class ItemCriteriaLoader extends AbstractNodeLoader<ItemCriteria> {

    private final RuleListsManager manager;
    private MaterialPatternLoaderBuilder materialLoader = new MaterialPatternLoaderBuilder();
    private StaticFieldLoaderBuilder<PotionEffectType> effectLoader =
            new StaticFieldLoaderBuilder<PotionEffectType>(PotionEffectType.class);

    public ItemCriteriaLoader(RuleListsManager manager) {
        this.manager = manager;
    }

    @Override
    public ItemCriteria read(ConfigurationNode node) throws DefinitionException {
        String entity = node.getString("entity");
        EntityResolver entityResolver = null;
        ItemStackSlotResolver resolver = null;

        if (entity != null) {
            entityResolver = manager.getResolvers()
                    .get(EntityResolver.class, entity);
            resolver = manager.getResolvers()
                    .get(ItemStackSlotResolver.class, node.getString("item", ItemStackSlotResolver.DEFAULT));
        }

        // has-data
        Boolean hasData = null;
        if (node.contains("has-data")) {
            hasData = node.getBoolean("has-data", false);
        }

        // Patterns
        List<MaterialPattern> patterns = node.contains(INLINE) ? node.listOf(INLINE,
                materialLoader) : node.listOf("material", materialLoader);
        if (patterns.size() == 0 && hasData == null) {
            throw new LoaderBuilderException("No block materials specified");
        }

        // Potion effects
        Set<PotionEffectType> effects = node.setOf("potion-effect", effectLoader);

        ItemCriteria criteria = new ItemCriteria(entityResolver, resolver);
        criteria.setPatterns(patterns);
        criteria.setDataCheck(hasData);
        criteria.setPotionEffects(effects);

        RuleListUtils.warnUnknown(node, LoggerUtils.getLogger(getClass()),
                                  "item", "entity", "has-data", "material", "potion-effect");

        return criteria;
    }

}