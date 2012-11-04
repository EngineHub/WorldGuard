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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.sk89q.rebar.config.AbstractNodeLoader;
import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.config.ConfigurationValue;
import com.sk89q.rebar.config.LoaderBuilderException;
import com.sk89q.rebar.util.LoggerUtils;

/**
 * A loader that will load rule definitions for the RuleLists.
 */
public class RuleEntryLoader extends AbstractNodeLoader<RuleEntry> {

    public static final String INLINE = "_";
    private static Logger logger = LoggerUtils.getLogger(RuleEntryLoader.class, "[WorldGuard] RuleList: ");

    private final DefinitionLoader<Criteria<?>> criteriaLoader;
    private final DefinitionLoader<Action<?>> actionLoader;

    /**
     * Construct the loader.
     *
     * @param ruleListsManager the action lists manager
     */
    public RuleEntryLoader(RuleListsManager ruleListsManager) {
        criteriaLoader = new DefinitionLoader<Criteria<?>>(ruleListsManager.getCriterion(), true);
        actionLoader = new DefinitionLoader<Action<?>>(ruleListsManager.getActions(), false);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public RuleEntry read(ConfigurationNode node) {
        if (node.size() == 0) {
            return null;
        }

        List<String> when = node.getStringList("when", new ArrayList<String>());

        // Check to see if the rule has any events defined
        if (when.size() == 0) {
            logger.warning("A rule has missing 'when' clause:\n" + node.toString());
            return null;
        }

        try {
            DefinedRule rule = new DefinedRule();

            // Get criterion from the 'if' subnode
            for (Criteria criteria : node.listOf("if", criteriaLoader)) {
                rule.add(criteria);
            }

            // Get actions from the 'then' subnode
            for (Action action : node.listOf("then", actionLoader)) {
                rule.add(action);
            }

            // A rule needs actions!
            if (rule.hasActions()) {
                return new RuleEntry(when, rule);
            } else {
                logger.warning("The following rule has no actions in:\n" + node.toString());
                return null;
            }
        } catch (LoaderBuilderException e) {
            logger.warning("A rule had the error '" + e.getMessage() + ", and the rule is:\n" + node.toString());
            return null;
        }
    }

    /**
     * Inverts an criteria, wrapping it in a {@link InvertedCriteria}.
     *
     * @param criteria to invert
     * @param inverted true to invert (false to not)
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <V> V invertCriteria(V object, boolean inverted) {
        if (object instanceof Criteria && inverted) {
            return (V) new InvertedCriteria((Criteria<?>) object);
        }

        return object;
    }

    /**
     * Loads specific definitions (criterion or actions) defined.
     *
     * @param <T> type of definition ({@link Action} or {@link Criteria})
     */
    private class DefinitionLoader<T> extends AbstractNodeLoader<T> {

        private final DefinitionManager<T> manager;
        private final boolean strict;

        /**
         * Construct the object.
         *
         * @param manager the definition manager
         * @param strict true to throw exceptions on error
         */
        public DefinitionLoader(DefinitionManager<T> manager, boolean strict) {
            this.manager = manager;
            this.strict = strict;
        }

        @Override
        public T read(ConfigurationNode node) {
            String id = node.getString("?");
            boolean inverted = node.getBoolean("negate", false);

            try {
                // Check for long hand notation (?: class)
                if (id != null) {
                    return invertCriteria(manager.newInstance(id, node, null), inverted);
                }

                // Check for short-hand notation (?class: value)
                for (String key : node.getKeys(ConfigurationNode.ROOT)) {
                    if (key.startsWith("?")) {
                        id = key;

                        Object value = node.get(key);
                        return invertCriteria(
                                manager.newInstance(key.substring(1), node, new ConfigurationValue(value)), inverted);
                    }
                }

                // No class defined!
                logger.warning("EventListeners: Missing '?' parameter in definition " +
                		"(to define what kind of criteria/action it is)");

                return null;
            } catch (DefinitionException e) {
                logger.warning("EventListeners: Invalid definition " +
                        "identified by type '" + id + "': " + e.getMessage());

                // Throw an exception in strict mode
                if (strict) {
                    throw new LoaderBuilderException();
                }

                return null;
            }
        }

    }

}
