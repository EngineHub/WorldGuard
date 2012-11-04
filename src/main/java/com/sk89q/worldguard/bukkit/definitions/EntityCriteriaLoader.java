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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;

import com.sk89q.rebar.config.AbstractNodeLoader;
import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.config.types.EnumLoaderBuilder;
import com.sk89q.rebar.util.LoggerUtils;
import com.sk89q.rulelists.DefinitionException;
import com.sk89q.rulelists.RuleListUtils;
import com.sk89q.rulelists.RuleListsManager;
import com.sk89q.worldguard.bukkit.resolvers.EntityResolver;

public class EntityCriteriaLoader extends AbstractNodeLoader<EntityCriteria> {

    private final RuleListsManager manager;
    private EnumLoaderBuilder<EntityType> typeLoader =
            new EnumLoaderBuilder<EntityType>(EntityType.class);

    public EntityCriteriaLoader(RuleListsManager manager) {
        this.manager = manager;
    }

    @Override
    public EntityCriteria read(ConfigurationNode node) throws DefinitionException {
        EntityResolver entityResolver = manager.getResolvers()
                .get(EntityResolver.class, node.getString("entity", "source"));

        Set<EntityType> types = node.contains(INLINE) ?
                node.setOf(INLINE, typeLoader) :
                node.setOf("type", typeLoader);

        EntityCriteria criteria = new EntityCriteria(entityResolver);

        List<Class<?>> ofTypes = new ArrayList<Class<?>>();

        if (node.getBoolean("is-animal", false))
            ofTypes.add(Animals.class);
        if (node.getBoolean("is-monster", false))
            ofTypes.add(Monster.class);
        if (node.getBoolean("is-creature", false))
            ofTypes.add(Creature.class);
        if (node.getBoolean("is-living", false))
            ofTypes.add(LivingEntity.class);
        if (node.getBoolean("is-player", false))
            ofTypes.add(Player.class);
        if (node.getBoolean("is-tameable", false))
            ofTypes.add(Tameable.class);

        criteria.setTypes(types);
        criteria.setOfTypes(ofTypes.toArray(new Class<?>[ofTypes.size()]));
        criteria.setIsTamed(node.getBoolean("is-tamed"));

        RuleListUtils.warnUnknown(node, LoggerUtils.getLogger(getClass()),
                                  "entity", "type", "is-animal", "is-monster", "is-creature",
                                  "is-living", "is-player", "is-tameable", "is-tamed");

        return criteria;
    }

}