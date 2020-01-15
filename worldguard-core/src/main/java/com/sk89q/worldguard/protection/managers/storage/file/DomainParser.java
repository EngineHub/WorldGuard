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

import com.sk89q.util.yaml.YAMLNode;
import com.sk89q.worldguard.domains.DefaultDomain;

import java.util.UUID;
import java.util.logging.Level;

class DomainParser {
    public static class One {
        public static DefaultDomain parseDomain(YAMLNode node) {
            if (node == null) {
                return new DefaultDomain();
            }

            DefaultDomain domain = new DefaultDomain();

            for (String name : node.getStringList("players", null)) {
                if (!name.isEmpty()) {
                    domain.addPlayer(name);
                }
            }

            for (String stringId : node.getStringList("unique-ids", null)) {
                try {
                    domain.addPlayer(UUID.fromString(stringId));
                } catch (IllegalArgumentException e) {
                    YamlCommon.log.log(Level.WARNING, "Failed to parse UUID '" + stringId + "'", e);
                }
            }

            for (String name : node.getStringList("groups", null)) {
                if (!name.isEmpty()) {
                    domain.addGroup(name);
                }
            }

            return domain;
        }
    }
}
