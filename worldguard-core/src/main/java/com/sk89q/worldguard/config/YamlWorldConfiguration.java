/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.config;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.report.Unreported;

import java.util.ArrayList;
import java.util.List;

public abstract class YamlWorldConfiguration extends WorldConfiguration {

    @Unreported protected YAMLProcessor parentConfig;
    @Unreported protected YAMLProcessor config;

    public boolean getBoolean(String node, boolean def) {
        boolean val = parentConfig.getBoolean(node, def);

        if (config.getProperty(node) != null) {
            return config.getBoolean(node, def);
        } else {
            return val;
        }
    }

    public String getString(String node, String def) {
        String val = parentConfig.getString(node, def);

        if (config.getProperty(node) != null) {
            return config.getString(node, def);
        } else {
            return val;
        }
    }

    public int getInt(String node, int def) {
        int val = parentConfig.getInt(node, def);

        if (config.getProperty(node) != null) {
            return config.getInt(node, def);
        } else {
            return val;
        }
    }

    @SuppressWarnings("unused")
    private double getDouble(String node, double def) {
        double val = parentConfig.getDouble(node, def);

        if (config.getProperty(node) != null) {
            return config.getDouble(node, def);
        } else {
            return val;
        }
    }

    public List<Integer> getIntList(String node, List<Integer> def) {
        List<Integer> res = parentConfig.getIntList(node, def);

        if (res == null || res.size() == 0) {
            parentConfig.setProperty(node, new ArrayList<Integer>());
        }

        if (config.getProperty(node) != null) {
            res = config.getIntList(node, def);
        }

        return res;
    }

    public List<String> getStringList(String node, List<String> def) {
        List<String> res = parentConfig.getStringList(node, def);

        if (res == null || res.size() == 0) {
            parentConfig.setProperty(node, new ArrayList<String>());
        }

        if (config.getProperty(node) != null) {
            res = config.getStringList(node, def);
        }

        return res;
    }

    public List<String> getKeys(String node) {
        List<String> res = parentConfig.getKeys(node);

        if (res == null || res.size() == 0) {
            res = config.getKeys(node);
        }
        if (res == null) {
            res = new ArrayList<String>();
        }

        return res;
    }

    public Object getProperty(String node) {
        Object res = parentConfig.getProperty(node);

        if (config.getProperty(node) != null) {
            res = config.getProperty(node);
        }

        return res;
    }

}
