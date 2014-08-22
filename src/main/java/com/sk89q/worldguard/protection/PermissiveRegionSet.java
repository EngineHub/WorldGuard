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

import com.google.common.collect.Iterators;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * A virtual region result set that is highly permissive, considering everyone
 * a member. Returned flag values are default values (when available).
 */
public class PermissiveRegionSet extends AbstractRegionSet {

    private static final PermissiveRegionSet INSTANCE = new PermissiveRegionSet();

    private PermissiveRegionSet() {
    }

    @Override
    public boolean isVirtual() {
        return true;
    }

    @Override
    public boolean testBuild(RegionAssociable subject, StateFlag... flags) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <V> V queryValue(@Nullable RegionAssociable subject, Flag<V> flag) {
        return flag.getDefault();
    }

    @Override
    public <V> Collection<V> queryAllValues(@Nullable RegionAssociable subject, Flag<V> flag) {
        return Collections.emptySet();
    }

    @Override
    public boolean isOwnerOfAll(LocalPlayer player) {
        return true;
    }

    @Override
    public boolean isMemberOfAll(LocalPlayer player) {
        return true;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Set<ProtectedRegion> getRegions() {
        return Collections.emptySet();
    }

    @Override
    public Iterator<ProtectedRegion> iterator() {
        return Iterators.emptyIterator();
    }

    /**
     * Get an instance.
     *
     * @return an instance
     */
    public static PermissiveRegionSet getInstance() {
        return INSTANCE;
    }

}
