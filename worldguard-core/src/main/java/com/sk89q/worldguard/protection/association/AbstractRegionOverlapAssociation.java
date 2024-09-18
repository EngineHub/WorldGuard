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
import com.sk89q.worldguard.protection.FlagValueCalculator;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class AbstractRegionOverlapAssociation implements RegionAssociable {

    @Nullable
    protected Set<ProtectedRegion> source;
    private boolean effectivelyEmpty;
    private final boolean useMaxPriorityAssociation;
    private int maxPriority;

    protected AbstractRegionOverlapAssociation(@Nullable Set<ProtectedRegion> source, boolean useMaxPriorityAssociation) {
        this.source = source;
        this.useMaxPriorityAssociation = useMaxPriorityAssociation;
    }

    protected void calcMaxPriority() {
        checkNotNull(source);
        boolean effectivelyEmpty = true;
        int maxPriority = Integer.MIN_VALUE;

        for (ProtectedRegion region : source) {
            int priority = region.getPriority();

            // Potential endless recurrence? No, because there is no region group flag.
            if ((effectivelyEmpty || priority > maxPriority) && FlagValueCalculator.getEffectiveFlagOf(region, Flags.PASSTHROUGH, this) != State.ALLOW) {
                effectivelyEmpty = false;

                if (useMaxPriorityAssociation) {
                    maxPriority = priority;
                } else {
                    break;
                }
            }
        }

        this.effectivelyEmpty = effectivelyEmpty;
        this.maxPriority = maxPriority;
    }

    private boolean checkNonplayerProtectionDomains(ProtectedRegion region) {
        if (source.isEmpty()) {
            return false;
        }

        // Potential endless recurrence? No, because there is no region group flag.
        Set<String> domains = FlagValueCalculator.getEffectiveFlagOf(region, Flags.NONPLAYER_PROTECTION_DOMAINS, this);

        if (domains == null || domains.isEmpty()) {
            return false;
        }

        for (ProtectedRegion sourceRegion : source) {
            if (sourceRegion.getPriority() < maxPriority) {
                continue;
            }

            // Potential endless recurrence? No, because there is no region group flag.
            Set<String> sourceDomains = FlagValueCalculator.getEffectiveFlagOf(sourceRegion, Flags.NONPLAYER_PROTECTION_DOMAINS, this);

            if (sourceDomains == null || sourceDomains.isEmpty()) {
                continue;
            }

            if (!Collections.disjoint(sourceDomains, domains)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Association getAssociation(List<ProtectedRegion> regions) {
        checkNotNull(source);
        for (ProtectedRegion region : regions) {
            while (region != null) {
                if (region.getId().equals(ProtectedRegion.GLOBAL_REGION) && effectivelyEmpty) {
                    return Association.OWNER;
                }

                if (source.contains(region) && region.getPriority() >= maxPriority) {
                    return Association.OWNER;
                }

                if (checkNonplayerProtectionDomains(region)) {
                    return Association.OWNER;
                }

                region = region.getParent();
            }
        }

        return Association.NON_MEMBER;
    }
}
