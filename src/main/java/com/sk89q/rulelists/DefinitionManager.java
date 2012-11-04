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

package com.sk89q.rulelists;

import java.util.HashMap;
import java.util.Map;

import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.config.ConfigurationValue;
import com.sk89q.rebar.config.Loader;

/**
 * Manages loaders for definitions of the given generic type.
 *
 * @author sk89q
 * @param <V> type to create
 */
public class DefinitionManager<V> {

    /**
     * Map of factories.
     */
    private Map<String, Loader<? extends V>> factories = new HashMap<String, Loader<? extends V>>();

    /**
     * Register a loader.
     *
     * @param id ID, case does not matter
     * @param factory factory
     */
    public void register(String id, Loader<? extends V> factory) {
        factories.put(id.toLowerCase(), factory);
    }

    /**
     * Create a new instance.
     *
     * @param id type
     * @param node configuration node
     * @param value short-hand value, possibly null
     * @return an object
     * @throws DefinitionException thrown on error when parsing or instantiating
     */
    V newInstance(String id, ConfigurationNode node, ConfigurationValue value) throws DefinitionException {
        Loader<? extends V> factory;

        if (value != null) {
            node = node.clone();
            node.set("_", value.get());
        }

        if ((factory = factories.get(id)) != null) {
            return factory.read(node);
        }

        throw new DefinitionException("Don't know what '" + id
                + "' is (typo? criteria/action doesn't exist?)");
    }

}
