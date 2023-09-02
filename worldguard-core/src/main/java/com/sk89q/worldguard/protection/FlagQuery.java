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

import static com.sk89q.worldguard.protection.flags.StateFlag.combine;
import static com.sk89q.worldguard.protection.flags.StateFlag.denyToNone;
import static com.sk89q.worldguard.protection.flags.StateFlag.test;

import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.MapFlag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Common methods for querying flags.
 */
public interface FlagQuery {

    /**
     * Returns true if the BUILD flag allows the action, but it
     * can be overridden by a list of other flags. The BUILD flag will not
     * override the other flags, but the other flags can override BUILD. If
     * neither BUILD or any of the flags permit the action, then false will
     * be returned.
     *
     * <p>Use this method when checking flags that are related to build
     * protection. For example, lighting fire in a region should not be
     * permitted unless the player is a member of the region or the
     * LIGHTER flag allows it. However, the LIGHTER flag should be able
     * to allow lighting fires even if BUILD is set to DENY.</p>
     *
     * <p>How this method works (BUILD can be overridden by other flags but
     * not the other way around) is inconsistent, but it's required for
     * legacy reasons.</p>
     *
     * <p>This method does not check the region bypass permission. That must
     * be done by the calling code.</p>
     *
     * @param subject the subject
     * @param flags the flags
     * @return true if the result was {@code ALLOW}
     */
    default boolean testBuild(RegionAssociable subject, StateFlag... flags) {
        if (flags.length == 0) {
            return testState(subject, Flags.BUILD);
        }

        return test(
                denyToNone(queryState(subject, Flags.BUILD)),
                queryState(subject, flags));
    }

    /**
     * Returns true if the BUILD flag allows the action, but it
     * can be overridden by a list of other flags. The BUILD flag will not
     * override the other flags, but the other flags can override BUILD. If
     * neither BUILD or any of the flags permit the action, then false will
     * be returned.
     *
     * <p>Use this method when checking flags that are related to build
     * protection. For example, lighting fire in a region should not be
     * permitted unless the player is a member of the region or the
     * LIGHTER flag allows it. However, the LIGHTER flag should be able
     * to allow lighting fires even if BUILD is set to DENY.</p>
     *
     * <p>This method does include parameters for a {@link MapFlag}.</p>
     *
     * <p>How this method works (BUILD can be overridden by other flags but
     * not the other way around) is inconsistent, but it's required for
     * legacy reasons.</p>
     *
     * <p>This method does not check the region bypass permission. That must
     * be done by the calling code.</p>
     *
     * @param subject the subject
     * @param flag the MapFlag
     * @param key the key for the MapFlag
     * @param fallback the fallback flag for MapFlag
     * @param flags the flags
     * @return true if the result was {@code ALLOW}
     */
    default <K> boolean testBuild(RegionAssociable subject, MapFlag<K, State> flag, K key,
                                 @Nullable StateFlag fallback, StateFlag... flags) {
        if (flag == null)
            return testBuild(subject, flags);

        if (flags.length == 0) {
            return test(
                    denyToNone(queryState(subject, Flags.BUILD)),
                    queryMapValue(subject, flag, key, fallback)
            );
        }

        return test(
                denyToNone(queryState(subject, Flags.BUILD)),
                queryMapValue(subject, flag, key, fallback),
                queryState(subject, flags)
        );
    }

    /**
     * Test whether the (effective) value for a list of state flags equals
     * {@code ALLOW}.
     *
     * <p>{@code subject} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (Flags on a region can be changed so that they only apply
     * to certain users.) The subject argument is required if the
     * {@link Flags#BUILD} flag is in the list of flags.</p>
     *
     * @param subject an optional subject, which would be used to determine the region groups that apply
     * @param flags a list of flags to check
     * @return true if the result was {@code ALLOW}
     */
    default boolean testState(@Nullable RegionAssociable subject, StateFlag... flags) {
        return test(queryState(subject, flags));
    }

    /**
     * Get the (effective) value for a list of state flags. The rules of
     * states is observed here; that is, {@code DENY} overrides {@code ALLOW},
     * and {@code ALLOW} overrides {@code NONE}. One flag may override another.
     *
     * <p>{@code subject} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (Flags on a region can be changed so that they only apply
     * to certain users.) The subject argument is required if the
     * {@link Flags#BUILD} flag is in the list of flags.</p>
     *
     * @param subject an optional subject, which would be used to determine the region groups that apply
     * @param flags a list of flags to check
     * @return a state
     */
    @Nullable
    default State queryState(@Nullable RegionAssociable subject, StateFlag... flags) {
        State value = null;

        for (StateFlag flag : flags) {
            value = combine(value, queryValue(subject, flag));
            if (value == State.DENY) {
                break;
            }
        }

        return value;
    }

    /**
     * Get the effective value for a flag. If there are multiple values
     * (for example, multiple overlapping regions with
     * the same priority may have the same flag set), then the selected
     * (or "winning") value will depend on the flag type.
     *
     * <p>Only some flag types actually have a strategy for picking the
     * "best value." For most types, the actual value that is chosen to be
     * returned is undefined (it could be any value). As of writing, the only
     * type of flag that actually has a strategy for picking a value is the
     * {@link StateFlag}.</p>
     *
     * <p>{@code subject} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (Flags on a region can be changed so that they only apply
     * to certain users.) The subject argument is required if the
     * {@link Flags#BUILD} flag is the flag being queried.</p>
     *
     * @param subject an optional subject, which would be used to determine the region group to apply
     * @param flag the flag
     * @return a value, which could be {@code null}
     */
    @Nullable
    default <V> V queryValue(@Nullable RegionAssociable subject, Flag<V> flag) {
        Collection<V> values = queryAllValues(subject, flag, true);
        return flag.chooseValue(values);
    }

    /**
     * Get the effective value for a key in a {@link MapFlag}. If there are multiple values
     * (for example, if there are multiple regions with the same priority
     * but with different farewell messages set, there would be multiple
     * completing values), then the selected (or "winning") value will be undefined.
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
     * @param flag the flag of type {@link MapFlag}
     * @param key the key for the map flag
     * @return a value, which could be {@code null}
     */
    @Nullable
    default <V, K> V queryMapValue(@Nullable RegionAssociable subject, MapFlag<K, V> flag, K key) {
        return queryMapValue(subject, flag, key, null);
    }

    /**
     * Get the effective value for a key in a {@link MapFlag}. If there are multiple values
     * (for example, if there are multiple regions with the same priority
     * but with different farewell messages set, there would be multiple
     * completing values), then the selected (or "winning") value will be undefined.
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
     * @param flag the flag of type {@link MapFlag}
     * @param key the key for the map flag
     * @return a value, which could be {@code null}
     */
    @Nullable
    <V, K> V queryMapValue(@Nullable RegionAssociable subject, MapFlag<K, V> flag, K key, @Nullable Flag<V> fallback);

    /**
     * Get the effective values for a flag, returning a collection of all
     * values. It is up to the caller to determine which value, if any,
     * from the collection will be used.
     *
     * <p>{@code subject} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (Flags on a region can be changed so that they only apply
     * to certain users.) The subject argument is required if the
     * {@link Flags#BUILD} flag is the flag being queried.</p>
     *
     * @param subject an optional subject, which would be used to determine the region group to apply
     * @param flag the flag
     * @return a collection of values
     */
    default <V> Collection<V> queryAllValues(@Nullable RegionAssociable subject, Flag<V> flag) {
        return queryAllValues(subject, flag, false);
    }

    /**
     * Get the effective values for a flag, returning a collection of all
     * values. It is up to the caller to determine which value, if any,
     * from the collection will be used.
     *
     * <p>{@code subject} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (Flags on a region can be changed so that they only apply
     * to certain users.) The subject argument is required if the
     * {@link Flags#BUILD} flag is the flag being queried.</p>
     *
     * @param subject an optional subject, which would be used to determine the region group to apply
     * @param flag the flag
     * @param acceptOne if possible, return only one value if it doesn't matter
     * @return a collection of values
     */
    <V> Collection<V> queryAllValues(@Nullable RegionAssociable subject, Flag<V> flag, boolean acceptOne);

}
