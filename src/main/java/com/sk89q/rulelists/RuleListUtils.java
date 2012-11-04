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

import java.util.List;
import java.util.logging.Logger;

import com.sk89q.rebar.config.ConfigurationNode;

/**
 * RuleList-related utility methods.
 */
public class RuleListUtils {

    private RuleListUtils() {
    }

    /**
     * Warn of unknown directives in a rule list block.
     *
     * @param node node
     * @param logger the logger to send to
     * @param expected expected keys
     */
    public static void warnUnknown(ConfigurationNode node, Logger logger, String ... expected) {
        List<String> keys = node.getKeys(ConfigurationNode.ROOT);

        for (String key : keys) {
            boolean found = false;

            if (key.equals("?")) continue;
            if (key.equals("negate")) continue;
            if (key.equals("_")) continue;
            if (key.length() > 0 && key.charAt(0) == '?') continue;

            for (String expect : expected) {
                if (expect.equals(key)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                logger.warning("[WorldGuard] RuleList: Unexpected parameter of '" + key + "' in " + node);
            }
        }
    }

}
