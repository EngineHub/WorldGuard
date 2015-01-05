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

package com.sk89q.worldguard.protection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Calculates the value of a flag given a list of regions and an optional
 * global region.
 *
 * <p>Since there may be multiple overlapping regions, regions with
 * differing priorities, regions with inheritance, flags with region groups
 * assigned to them, and much more, the task of calculating the "effective"
 * value of a flag is far from trivial. This class abstracts away the
 * difficult with a number of methods for performing these calculations.</p>
 */
public class FlagValueCalculator {

    private final List<ProtectedRegion> regions;
    @Nullable
    private final ProtectedRegion globalRegion;
    private final Iterable<ProtectedRegion> applicable;

    /**
     * Create a new instance.
     *
     * @param regions a list of applicable regions that <strong>must be sorted by priority descending</strong>
     * @param globalRegion an optional global region (null to not use one)
     */
    public FlagValueCalculator(List<ProtectedRegion> regions, @Nullable ProtectedRegion globalRegion) {
        checkNotNull(regions);

        this.regions = regions;
        this.globalRegion = globalRegion;

        if (globalRegion != null) {
            applicable = Iterables.concat(regions, Arrays.asList(globalRegion));
        } else {
            applicable = regions;
        }
    }

    /**
     * Returns an iterable of regions sorted by priority (descending), with
     * the global region tacked on at the end if one exists.
     *
     * @return an iterable
     */
    private Iterable<ProtectedRegion> getApplicable() {
        return applicable;
    }

    /**
     * Return the membership status of the given subject, indicating
     * whether there are no (counted) regions in the list of regions,
     * whether the subject is a member of all regions, or whether
     * the region is not a member of all regions.
     *
     * <p>A region is "counted" if it doesn't have the
     * {@link DefaultFlag#PASSTHROUGH} flag set to {@code ALLOW}. (The
     * explicit purpose of the PASSTHROUGH flag is to have the region
     * be skipped over in this check.)</p>
     *
     * <p>This method is mostly for internal use. It's not particularly
     * useful.</p>
     *
     * @param subject the subject
     * @return the membership result
     */
    public Result getMembership(RegionAssociable subject) {
        checkNotNull(subject);

        int minimumPriority = Integer.MIN_VALUE;
        boolean foundApplicableRegion = false;

        // Say there are two regions in one location: CHILD and PARENT (CHILD
        // is a child of PARENT). If there are two overlapping regions in WG, a
        // subject has to be a member of /both/ (or flags permit) in order to
        // build in that location. However, inheritance is supposed
        // to allow building if the subject is a member of just CHILD. That
        // presents a problem.
        //
        // To rectify this, we keep two sets. When we iterate over the list of
        // regions, there are two scenarios that we may encounter:
        //
        // 1) PARENT first, CHILD later:
        //    a) When the loop reaches PARENT, PARENT is added to needsClear.
        //    b) When the loop reaches CHILD, parents of CHILD (which includes
        //       PARENT) are removed from needsClear.
        //    c) needsClear is empty again.
        //
        // 2) CHILD first, PARENT later:
        //    a) When the loop reaches CHILD, CHILD's parents (i.e. PARENT) are
        //       added to hasCleared.
        //    b) When the loop reaches PARENT, since PARENT is already in
        //       hasCleared, it does not add PARENT to needsClear.
        //    c) needsClear stays empty.
        //
        // As long as the process ends with needsClear being empty, then
        // we have satisfied all membership requirements.

        Set<ProtectedRegion> needsClear = new HashSet<ProtectedRegion>();
        Set<ProtectedRegion> hasCleared = new HashSet<ProtectedRegion>();

        for (ProtectedRegion region : getApplicable()) {
            // Don't consider lower priorities below minimumPriority
            // (which starts at Integer.MIN_VALUE). A region that "counts"
            // (has the flag set OR has members) will raise minimumPriority
            // to its own priority.
            if (getPriority(region) < minimumPriority) {
                break;
            }

            // If PASSTHROUGH is set, ignore this region
            if (getEffectiveFlag(region, DefaultFlag.PASSTHROUGH, subject) == State.ALLOW) {
                continue;
            }

            minimumPriority = getPriority(region);
            foundApplicableRegion = true;

            if (!hasCleared.contains(region)) {
                if (!RegionGroup.MEMBERS.contains(subject.getAssociation(region))) {
                    needsClear.add(region);
                } else {
                    // Need to clear all parents
                    removeParents(needsClear, hasCleared, region);
                }
            }
        }

        if (foundApplicableRegion) {
            return needsClear.isEmpty() ? Result.SUCCESS : Result.FAIL;
        } else {
            return Result.NO_REGIONS;
        }
    }


    /**
     * Get the effective value for a list of state flags. The rules of
     * states is observed here; that is, {@code DENY} overrides {@code ALLOW},
     * and {@code ALLOW} overrides {@code NONE}.
     *
     * <p>A subject can be provided that is used to determine whether the value
     * of a flag on a particular region should be used. For example, if a
     * flag's region group is set to {@link RegionGroup#MEMBERS} and the given
     * subject is not a member, then the region would be skipped when
     * querying that flag. If {@code null} is provided for the subject, then
     * only flags that use {@link RegionGroup#ALL},
     * {@link RegionGroup#NON_MEMBERS}, etc. will apply.</p>
     *
     * @param subject an optional subject, which would be used to determine the region group to apply
     * @param flags a list of flags to check
     * @return a state
     */
    @Nullable
    public State queryState(@Nullable RegionAssociable subject, StateFlag... flags) {
        State value = null;

        for (StateFlag flag : flags) {
            value = StateFlag.combine(value, queryValue(subject, flag));
            if (value == State.DENY) {
                break;
            }
        }

        return value;
    }

    /**
     * Get the effective value for a list of state flags. The rules of
     * states is observed here; that is, {@code DENY} overrides {@code ALLOW},
     * and {@code ALLOW} overrides {@code NONE}.
     *
     * <p>This method is the same as
     * {@link #queryState(RegionAssociable, StateFlag...)}.</p>
     *
     * @param subject an optional subject, which would be used to determine the region group to apply
     * @param flag a flag to check
     * @return a state
     */
    @Nullable
    public State queryState(@Nullable RegionAssociable subject, StateFlag flag) {
        return queryValue(subject, flag);
    }

    /**
     * Get the effective value for a flag. If there are multiple values
     * (for example, if there are multiple regions with the same priority
     * but with different farewell messages set, there would be multiple
     * completing values), then the selected (or "winning") value will depend
     * on the flag type.
     *
     * <p>Only some flag types actually have a strategy for picking the
     * "best value." For most types, the actual value that is chosen to be
     * returned is undefined (it could be any value). As of writing, the only
     * type of flag that can consistently return the same 'best' value is
     * {@link StateFlag}.</p>
     *
     * <p>A subject can be provided that is used to determine whether the value
     * of a flag on a particular region should be used. For example, if a
     * flag's region group is set to {@link RegionGroup#MEMBERS} and the given
     * subject is not a member, then the region would be skipped when
     * querying that flag. If {@code null} is provided for the subject, then
     * only flags that use {@link RegionGroup#ALL},
     * {@link RegionGroup#NON_MEMBERS}, etc. will apply.</p>
     *
     * @param subject an optional subject, which would be used to determine the region group to apply
     * @param flag the flag
     * @return a value, which could be {@code null}
     */
    @Nullable
    public <V> V queryValue(@Nullable RegionAssociable subject, Flag<V> flag) {
        Collection<V> values = queryAllValues(subject, flag);
        return flag.chooseValue(values);
    }

    /**
     * Get the effective values for a flag, returning a collection of all
     * values. It is up to the caller to determine which value, if any,
     * from the collection will be used.
     *
     * <p>A subject can be provided that is used to determine whether the value
     * of a flag on a particular region should be used. For example, if a
     * flag's region group is set to {@link RegionGroup#MEMBERS} and the given
     * subject is not a member, then the region would be skipped when
     * querying that flag. If {@code null} is provided for the subject, then
     * only flags that use {@link RegionGroup#ALL},
     * {@link RegionGroup#NON_MEMBERS}, etc. will apply.</p>
     *
     * @param subject an optional subject, which would be used to determine the region group to apply
     * @param flag the flag
     * @return a collection of values
     */
    @SuppressWarnings("unchecked")
    public <V> Collection<V> queryAllValues(@Nullable RegionAssociable subject, Flag<V> flag) {
        checkNotNull(flag);

        // Check to see whether we have a subject if this is BUILD
        if (flag == DefaultFlag.BUILD && subject == null) {
            throw new NullPointerException("The BUILD flag is handled in a special fashion and requires a non-null subject parameter");
        }

        int minimumPriority = Integer.MIN_VALUE;

        // Say there are two regions in one location: CHILD and PARENT (CHILD
        // is a child of PARENT). If the two are overlapping regions in WG,
        // both with values set, then we have a problem. Due to inheritance,
        // only the CHILD's value for the flag should be used because it
        // overrides its parent's value, but default behavior is to collect
        // all the values into a list.
        //
        // To rectify this, we keep a map of consideredValues (region -> value)
        // and an ignoredRegions set. When we iterate over the list of
        // regions, there are two scenarios that we may encounter:
        //
        // 1) PARENT first, CHILD later:
        //    a) When the loop reaches PARENT, PARENT's value is added to
        //       consideredValues
        //    b) When the loop reaches CHILD, parents of CHILD (which includes
        //       PARENT) are removed from consideredValues (so we no longer
        //       consider those values). The CHILD's value is then added to
        //       consideredValues.
        //    c) In the end, only CHILD's value exists in consideredValues.
        //
        // 2) CHILD first, PARENT later:
        //    a) When the loop reaches CHILD, CHILD's value is added to
        //       consideredValues. In addition, the CHILD's parents (which
        //       includes PARENT) are added to ignoredRegions.
        //    b) When the loop reaches PARENT, since PARENT is in
        //       ignoredRegions, the parent is skipped over.
        //    c) In the end, only CHILD's value exists in consideredValues.

        Map<ProtectedRegion, V> consideredValues = new HashMap<ProtectedRegion, V>();
        Set<ProtectedRegion> ignoredRegions = new HashSet<ProtectedRegion>();

        for (ProtectedRegion region : getApplicable()) {
            // Don't consider lower priorities below minimumPriority
            // (which starts at Integer.MIN_VALUE). A region that "counts"
            // (has the flag set) will raise minimumPriority to its own
            // priority.
            if (getPriority(region) < minimumPriority) {
                break;
            }

            V value = getEffectiveFlag(region, flag, subject);
            int priority = getPriority(region);

            if (value != null) {
                if (!ignoredRegions.contains(region)) {
                    minimumPriority = priority;

                    ignoreValuesOfParents(consideredValues, ignoredRegions, region);
                    consideredValues.put(region, value);

                    if (value == State.DENY) {
                        // Since DENY overrides all other values, there
                        // is no need to consider any further regions
                        break;
                    }
                }
            }

            // The BUILD flag (of lower priorities) can be overridden if
            // this region has members... this check is here due to legacy
            // reasons
            if (priority != minimumPriority && flag == DefaultFlag.BUILD
                    && getEffectiveFlag(region, DefaultFlag.PASSTHROUGH, subject) != State.ALLOW) {
                minimumPriority = getPriority(region);
            }
        }

        if (flag == DefaultFlag.BUILD && consideredValues.isEmpty()) {
            switch (getMembership(subject)) {
                case FAIL:
                    return ImmutableList.of();
                case SUCCESS:
                    return (Collection<V>) ImmutableList.of(State.ALLOW);
            }
        }

        if (consideredValues.isEmpty()) {
            V fallback = flag.getDefault();
            return fallback != null ? ImmutableList.of(fallback) : (Collection<V>) ImmutableList.of();
        }

        return consideredValues.values();
    }

    /**
     * Get the effective priority of a region, overriding a region's priority
     * when appropriate (i.e. with the global region).
     *
     * @param region the region
     * @return the priority
     */
    public int getPriority(final ProtectedRegion region) {
        if (region == globalRegion) {
            return Integer.MIN_VALUE;
        } else {
            return region.getPriority();
        }
    }

    /**
     * Get a region's state flag, checking parent regions until a value for the
     * flag can be found (if one even exists).
     *
     * @param region the region
     * @param flag the flag
     * @param subject an subject object
     * @return the value
     */
    @SuppressWarnings("unchecked")
    public <V> V getEffectiveFlag(final ProtectedRegion region, Flag<V> flag, @Nullable RegionAssociable subject) {
        if (region == globalRegion) {
            if (flag == DefaultFlag.PASSTHROUGH) {
                // Has members/owners -> the global region acts like
                // a regular region without PASSTHROUGH
                if (region.hasMembersOrOwners() || region.getFlag(DefaultFlag.PASSTHROUGH) == State.DENY) {
                    return null;
                } else {
                    return (V) State.ALLOW;
                }

            } else if (flag == DefaultFlag.BUILD) {
                // Legacy behavior -> we can't let people change BUILD on
                // the global region
                State value = region.getFlag(DefaultFlag.BUILD);
                return value != State.ALLOW ? (V) value : null;
            }
        }

        ProtectedRegion current = region;

        while (current != null) {
            V value = current.getFlag(flag);

            if (value != null) {
                boolean use = true;

                if (flag.getRegionGroupFlag() != null) {
                    RegionGroup group = current.getFlag(flag.getRegionGroupFlag());
                    if (group == null) {
                        group = flag.getRegionGroupFlag().getDefault();
                    }

                    if (group == null) {
                        use = false;
                    } else if (subject == null) {
                        use = group.contains(Association.NON_MEMBER);
                    } else if (!group.contains(subject.getAssociation(region))) {
                        use = false;
                    }
                }

                if (use) {
                    return value;
                }
            }

            current = current.getParent();
        }

        return null;
    }

    /**
     * Clear a region's parents for isFlagAllowed().
     *
     * @param needsClear the regions that should be cleared
     * @param hasCleared the regions already cleared
     * @param region the region to start from
     */
    private void removeParents(Set<ProtectedRegion> needsClear, Set<ProtectedRegion> hasCleared, ProtectedRegion region) {
        ProtectedRegion parent = region.getParent();

        while (parent != null) {
            if (!needsClear.remove(parent)) {
                hasCleared.add(parent);
            }

            parent = parent.getParent();
        }
    }

    /**
     * Clear a region's parents for getFlag().
     *
     * @param needsClear The regions that should be cleared
     * @param hasCleared The regions already cleared
     * @param region The region to start from
     */
    private void ignoreValuesOfParents(Map<ProtectedRegion, ?> needsClear, Set<ProtectedRegion> hasCleared, ProtectedRegion region) {
        ProtectedRegion parent = region.getParent();

        while (parent != null) {
            if (needsClear.remove(parent) == null) {
                hasCleared.add(parent);
            }

            parent = parent.getParent();
        }
    }

    /**
     * Describes the membership result from
     * {@link #getMembership(RegionAssociable)}.
     */
    public static enum Result {
        /**
         * Indicates that there are no regions or the only regions are
         * ones with {@link DefaultFlag#PASSTHROUGH} enabled.
         */
        NO_REGIONS,

        /**
         * Indicates that the player is not a member of all overlapping
         * regions.
         */
        FAIL,

        /**
         * Indicates that the player is a member of all overlapping
         * regions.
         */
        SUCCESS
    }

}
