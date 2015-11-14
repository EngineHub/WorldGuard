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

package com.sk89q.worldguard.domains;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.util.ChangeTracked;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Contains groups in a domain.
 */
public class GroupDomain implements Domain, ChangeTracked {

    private final Set<String> groups = new CopyOnWriteArraySet<String>();
    private boolean dirty = true;

    /**
     * Create a new instance.
     */
    public GroupDomain() {
    }

    /**
     * Create a new instance with copies from another domain.
     *
     * @param domain the domain to copy values from
     */
    public GroupDomain(GroupDomain domain) {
        checkNotNull(domain, "domain");
        groups.addAll(domain.getGroups());
    }

    /**
     * Create a new instance.
     *
     * @param groups an array of groups
     */
    public GroupDomain(String[] groups) {
        checkNotNull(groups);
        for (String group : groups) {
            addGroup(group);
        }
    }

    /**
     * Add the name of the group to the domain.
     *
     * @param name the name of the group.
     */
    public void addGroup(String name) {
        checkNotNull(name);
        if (!name.trim().isEmpty()) {
            setDirty(true);
            groups.add(name.trim().toLowerCase());
        }
    }

    /**
     * Remove the given group from the domain.
     *
     * @param name the name of the group
     */
    public void removeGroup(String name) {
        checkNotNull(name);
        setDirty(true);
        groups.remove(name.trim().toLowerCase());
    }

    @Override
    public boolean contains(LocalPlayer player) {
        checkNotNull(player);
        for (String group : groups) {
            if (player.hasGroup(group)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the set of group names.
     *
     * @return the set of group names
     */
    public Set<String> getGroups() {
        return Collections.unmodifiableSet(groups);
    }

    @Override
    public boolean contains(UUID uniqueId) {
        return false; // GroupDomains can't contain UUIDs
    }

    @Override
    public boolean contains(String playerName) {
        return false; // GroupDomains can't contain player names.
    }

    @Override
    public int size() {
        return groups.size();
    }

    @Override
    public void clear() {
        setDirty(true);
        groups.clear();
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public String toString() {
        return "{" +
                "names=" + groups +
                '}';
    }

}
