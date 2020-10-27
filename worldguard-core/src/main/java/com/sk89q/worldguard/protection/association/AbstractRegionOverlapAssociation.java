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

package com.sk89q.worldguard.protection.association;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractRegionOverlapAssociation implements RegionAssociable {

    @Nullable
    protected Set<ProtectedRegion> source;
    private boolean useMaxPriorityAssociation;
    private boolean useOwnerAssociation;
    private int maxPriority;
    private Set<ProtectedRegion> maxPriorityRegions;

    protected AbstractRegionOverlapAssociation(@Nullable Set<ProtectedRegion> source, boolean useMaxPriorityAssociation, boolean useOwnerAssociation) {
        this.source = source;
        this.useMaxPriorityAssociation = useMaxPriorityAssociation;
        this.useOwnerAssociation = useOwnerAssociation;
    }

    protected void calcMaxPriority() {
        checkNotNull(source);
        int best = 0;
        Set<ProtectedRegion> bestRegions = new HashSet<>();
        for (ProtectedRegion region : source) {
            int priority = region.getPriority();
            if (priority > best) {
                best = priority;
                bestRegions.clear();
                bestRegions.add(region);
            } else if (priority == best) {
                bestRegions.add(region);
            }
        }
        this.maxPriority = best;
        this.maxPriorityRegions = bestRegions;
    }

    private boolean hasAnyRegionAnyOwner(Collection<? extends ProtectedRegion> regions, DefaultDomain owners) {
        for (ProtectedRegion region : regions) {
            if (!Collections.disjoint(region.getOwners().getUniqueIds(), owners.getUniqueIds())) {
                return true;
            }

            if (!Collections.disjoint(region.getOwners().getPlayers(), owners.getPlayers())) {
                return true;
            }

            // Assuming players of different groups are disjoint and groups are not empty.
            if (!Collections.disjoint(region.getOwners().getGroups(), owners.getGroups())) {
                return true;
            }

            // A complete check would require the following code.
            // However, this is clearly not possible.
            /*for (String ownerGroup : owners.getGroups()) {
                if (isEmptyGroup(ownerGroup)) {
                    continue;
                }

                if (region.getOwners().getGroups().contains(ownerGroup)) {
                    return true;
                }

                for (String regionGroup : region.getOwners().getGroups()) {
                    if (!areDisjointGroups(regionGroup, ownerGroup)) {
                        return true;
                    }
                }
            }*/

            // Check if the group domains contain players from the player domains.
            // This would require either to convert from player uniqueId/name to LocalPlayer
            // or implementations of the GroupDomain#contains(UUID/String) methods.
            // Both can be implemented in the future via WorldGuardPlatform for example.
            /*for (UUID uniqueId : owners.getUniqueIds()) {
                if (region.getOwners().getGroupDomain().contains(uniqueId)) {
                    return true;
                }
            }

            for (String player : owners.getPlayers()) {
                if (region.getOwners().getGroupDomain().contains(player)) {
                    return true;
                }
            }

            for (UUID uniqueId : region.getOwners().getUniqueIds()) {
                if (owners.getGroupDomain().contains(uniqueId)) {
                    return true;
                }
            }

            for (String player : region.getOwners().getPlayers()) {
                if (owners.getGroupDomain().contains(player)) {
                    return true;
                }
            }*/
        }

        return false;
    }

    @Override
    public Association getAssociation(List<ProtectedRegion> regions) {
        checkNotNull(source);
        for (ProtectedRegion region : regions) {
            if ((region.getId().equals(ProtectedRegion.GLOBAL_REGION) && source.isEmpty())) {
                return Association.OWNER;
            }

            if (source.contains(region)) {
                if (useMaxPriorityAssociation) {
                    int priority = region.getPriority();
                    if (priority == maxPriority) {
                        return Association.OWNER;
                    }
                } else {
                    return Association.OWNER;
                }
            }

            if (useOwnerAssociation) {
                Set<ProtectedRegion> source;

                if (useMaxPriorityAssociation) {
                    source = maxPriorityRegions;
                } else {
                    source = this.source;
                }

                if (hasAnyRegionAnyOwner(source, region.getOwners())) {
                    return Association.OWNER;
                }
            }
        }

        return Association.NON_MEMBER;
    }
}
