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
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.storage.RegionDatabaseUtils;
import com.sk89q.worldguard.protection.regions.*;

import java.util.*;
import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldguard.protection.managers.storage.file.YamlCommon.*;

class YamlReaderVersionTwo implements YamlReader {
    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public Set<ProtectedRegion> load(FlagRegistry flagRegistry, YAMLProcessor config) {
        Map<String, ProtectedRegion> loaded = new HashMap<>();
        Map<String, YAMLNode> regionData = config.getNodes("regions");

        if (regionData == null) {
            return Collections.emptySet(); // No regions are even configured
        }

        Map<ProtectedRegion, String> parentSets = new LinkedHashMap<>();

        for (Map.Entry<String, YAMLNode> namespaceEntry : regionData.entrySet()) {
            String namespaceKey = namespaceEntry.getKey();

            String namespace = namespaceKey;
            if (namespace.equals(YAML_GLOBAL_NAMESPACE_NAME)) {
                namespace = null;
            }

            for (Map.Entry<String, Object> entry : namespaceEntry.getValue().getMap().entrySet()) {
                String name = entry.getKey();
                YAMLNode node = config.getNode("regions." + namespaceKey + "." + name);

                String type = node.getString("type");
                ProtectedRegion region;

                RegionIdentifier id = new RegionIdentifier(namespace, name);
                try {
                    if (type == null) {
                        log.warning("Undefined region type for region '" + id + "'!\n" +
                                "Here is what the region data looks like:\n\n" + toYamlOutput(node.getMap()) + "\n");
                        continue;
                    } else if (type.equals("cuboid")) {
                        Vector3 pt1 = checkNotNull(node.getVector("min"));
                        Vector3 pt2 = checkNotNull(node.getVector("max"));
                        BlockVector3 min = pt1.getMinimum(pt2).toBlockPoint();
                        BlockVector3 max = pt1.getMaximum(pt2).toBlockPoint();
                        region = new ProtectedCuboidRegion(id, min, max);
                    } else if (type.equals("poly2d")) {
                        Integer minY = checkNotNull(node.getInt("min-y"));
                        Integer maxY = checkNotNull(node.getInt("max-y"));
                        List<BlockVector2> points = node.getBlockVector2List("points", null);
                        region = new ProtectedPolygonalRegion(id, points, minY, maxY);
                    } else if (type.equals("global")) {
                        region = new GlobalProtectedRegion(id);
                    } else {
                        log.warning("Unknown region type for region '" + id + "'!\n" +
                                "Here is what the region data looks like:\n\n" + toYamlOutput(node.getMap()) + "\n");
                        continue;
                    }

                    Integer priority = checkNotNull(node.getInt("priority"));
                    region.setPriority(priority);
                    FlagParser.One.setFlags(flagRegistry, region, node.getNode("flags"));
                    region.setOwners(DomainParser.One.parseDomain(node.getNode("owners")));
                    region.setMembers(DomainParser.One.parseDomain(node.getNode("members")));

                    loaded.put(id.getLegacyQualifiedName(), region);

                    String parentQualifiedName = node.getString("parent");
                    if (parentQualifiedName != null) {
                        parentSets.put(region, parentQualifiedName);
                    }
                } catch (NullPointerException e) {
                    log.log(Level.WARNING,
                            "Unexpected NullPointerException encountered during parsing for the region '" + id + "'!\n" +
                                    "Here is what the region data looks like:\n\n" + toYamlOutput(node.getMap()) +
                                    "\n\nNote: This region will disappear as a result!", e);
                }
            }
        }

        // Relink parents
        RegionDatabaseUtils.relinkParents(loaded, parentSets);

        return new HashSet<>(loaded.values());
    }
}
