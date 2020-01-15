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

package com.sk89q.worldguard.protection.managers.storage.file;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.util.logging.Logger;

class YamlCommon {
    public static final Logger log = Logger.getLogger(YamlRegionFile.class.getCanonicalName());
    public static final Yaml ERROR_DUMP_YAML;

    static {
        DumperOptions options = new DumperOptions();
        options.setIndent(4);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.AUTO);

        ERROR_DUMP_YAML = new Yaml(new SafeConstructor(), new Representer(), options);
    }

    public static final String YAML_GLOBAL_NAMESPACE_NAME = "__global_ns__";

    private YamlCommon() {
    }

    /**
     * Dump the given object as YAML for debugging purposes.
     *
     * @param object the object
     * @return the YAML string or an error string if dumping fals
     */
    public static String toYamlOutput(Object object) {
        try {
            return ERROR_DUMP_YAML.dump(object).replaceAll("(?m)^", "\t");
        } catch (Throwable t) {
            return "<error while dumping object>";
        }
    }
}
