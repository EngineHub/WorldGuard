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

package com.sk89q.worldguard.protection.flags;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.FlagValueCalculator;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * A flag carries extra data on a region.
 */
public abstract class Flag<T> {

    private final String name;
    private final RegionGroupFlag regionGroup;

    /**
     * Create a new flag.
     *
     * @param name The name of the flag
     * @param defaultGroup The default group
     */
    public Flag(String name, @Nullable RegionGroup defaultGroup) {
        this.name = name;
        this.regionGroup = defaultGroup != null ? new RegionGroupFlag(name + "-group", defaultGroup) : null;
    }

    /**
     * Create a new flag with {@link RegionGroup#ALL} as the default group.
     *
     * @param name The name of the flag
     */
    public Flag(String name) {
        this(name, RegionGroup.ALL);
    }

    /**
     * Get the name of the flag.
     *
     * @return The name of the flag
     */
    public String getName() {
        return name;
    }

    /**
     * Get the default value.
     *
     * @return The default value, if one exists, otherwise {@code null} may be returned
     */
    @Nullable
    public T getDefault() {
        return null;
    }

    @Nullable
    public T chooseValue(Collection<T> values) {
        if (!values.isEmpty()) {
            return values.iterator().next();
        } else {
            return null;
        }
    }

    /**
     * Whether the flag can take a list of values and choose a "best one."
     *
     * <p>This is the case with the {@link StateFlag} where {@code DENY}
     * overrides {@code ALLOW}, but most flags just return the
     * first result from a list.</p>
     *
     * <p>This flag is primarily used to optimize flag lookup in
     * {@link FlagValueCalculator}.</p>
     *
     * @return Whether a best value can be chosen
     */
    public boolean hasConflictStrategy() {
        return false;
    }

    /**
     * Whether the flag implicitly has a value set as long as
     * {@link DefaultFlag#PASSTHROUGH} is not set.
     *
     * <p>This value is only changed, at least in WorldGuard, for the
     * {@link DefaultFlag#BUILD} flag.</p>
     *
     * @return Whether the flag is ignored
     */
    public boolean implicitlySetWithMembership() {
        return false;
    }

    /**
     * Whether, if the flag is not set at all, the value should be derived
     * from membership.
     *
     * <p>This value is only changed, at least in WorldGuard, for the
     * {@link DefaultFlag#BUILD} flag.</p>
     *
     * @return Whether membership is used
     */
    public boolean usesMembershipAsDefault() {
        return false;
    }

    /**
     * Whether the flag requires that a subject is specified in
     * {@link FlagValueCalculator}.
     *
     * <p>This value is only changed, at least in WorldGuard, for the
     * {@link DefaultFlag#BUILD} flag.</p>
     *
     * @return Whether a subject is required
     */
    public boolean requiresSubject() {
        return false;
    }

    /**
     * Get the region group flag.
     *
     * <p>Every group has a region group flag except for region group flags
     * themselves.</p>
     *
     * @return The region group flag
     */
    public RegionGroupFlag getRegionGroupFlag() {
        return regionGroup;
    }

    /**
     * Parse a given input to coerce it to a type compatible with the flag.
     *
     * @param plugin The plugin
     * @param sender The sender
     * @param input THe input
     * @return The coerced type
     * @throws InvalidFlagFormat Raised if the input is invalid
     */
    public abstract T parseInput(WorldGuardPlugin plugin, CommandSender sender, String input) throws InvalidFlagFormat;

    /**
     * Convert a raw type that was loaded (from a YAML file, for example)
     * into the type that this flag uses.
     *
     * @param o The object
     * @return The unmarshalled type
     */
    public abstract T unmarshal(@Nullable Object o);

    /**
     * Convert the value stored for this flag into a type that can be
     * serialized into YAML.
     *
     * @param o The object
     * @return The marshalled type
     */
    public abstract Object marshal(T o);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "name='" + name + '\'' +
                '}';
    }
}
