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
import com.sk89q.rebar.config.AbstractNodeLoader;
import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.config.LoaderBuilderException;
import com.sk89q.rebar.config.types.StringLoaderBuilder;

/**
 * A loader that will load the built-in rules used. It handles a slightly different
 * format that also stores parameter information and other goodies.
 */
public class RuleTemplateEntryLoader extends AbstractNodeLoader<List<RuleEntry>> {

    private static final StringLoaderBuilder stringLoader = new StringLoaderBuilder();

    private final ConfigurationNode config;
    private final RuleEntryLoader entryLoader;

    /**
     * Construct the loader with the given rule lists manager.
     *
     * @param manager the manager
     * @param config the configuration node to read settings from
     */
    public RuleTemplateEntryLoader(RuleListsManager manager, ConfigurationNode config) {
        entryLoader = new RuleEntryLoader(manager);
        this.config = config;
    }

    @Override
    public List<RuleEntry> read(ConfigurationNode node) throws LoaderBuilderException {
        List<RuleEntry> rules = new ArrayList<RuleEntry>();

        List<String> settings = node.listOf("setting", stringLoader);
        if (settings.size() == 0) {
            throw new LoaderBuilderException("No 'setting' value in:\n" + node.toString());
        }

        String selectedSetting = settings.get(0);
        String type = node.getString("type", "boolean");

        if (settings.size() > 1) {
            boolean migrate = false;
            boolean found = false;
            Object value = null;

            for (String setting : settings) {
                if (config.contains(setting)) {
                    value = config.get(setting);
                    found = true;
                    break;
                } else {
                    migrate = true;
                }
            }

            if (found && migrate) {
                config.set(settings.get(0), value);

                int i = 0;
                for (String setting : settings) {
                    if (i++ != 0) {
                        config.remove(setting);
                    }
                }
            }
        }

        boolean active = false;

        // See if the setting is active
        if (type.equals("boolean")) {
            boolean negate = node.getBoolean("negate", false);
            boolean enabled = config.getBoolean(selectedSetting, negate);

            if (negate) {
                enabled = !enabled;
            }

            if (enabled) {
                active = true;
            }
        } else if (type.equals("list")) {
            Object val = config.get(selectedSetting);

            if (val == null) {
                config.set(selectedSetting, new ArrayList<Object>());
            } else if (val instanceof List<?> && ((List<?>) val).size() > 0) {
                active = true;
            }
        }

        // Load the setting
        if (active) {
            List<String> paramPaths = node.getStringList("parameters.target", new ArrayList<String>());
            if (paramPaths.size() > 0) {
                Object val = config.get(selectedSetting);
                for (String path : paramPaths) {
                    node.set(path, val);
                }
            }

            for (RuleEntry rule : node.listOf("rules", entryLoader)) {
                rules.add(rule);
            }
        }

        return rules;
    }

}
