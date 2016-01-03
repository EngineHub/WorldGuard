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

package com.sk89q.worldguard.blacklist;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.sk89q.worldguard.blacklist.target.Target;
import com.sk89q.worldguard.blacklist.target.TargetMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkNotNull;

class MatcherIndex {

    private static final MatcherIndex EMPTY_INSTANCE = new MatcherIndex(HashBasedTable.<Integer, TargetMatcher, BlacklistEntry>create());
    private final Table<Integer, TargetMatcher, BlacklistEntry> entries;

    private MatcherIndex(Table<Integer, TargetMatcher, BlacklistEntry> entries) {
        checkNotNull(entries);
        this.entries = entries;
    }

    public List<BlacklistEntry> getEntries(Target target) {
        List<BlacklistEntry> found = new ArrayList<BlacklistEntry>();
        for (Entry<TargetMatcher, BlacklistEntry> entry : entries.row(target.getTypeId()).entrySet()) {
            if (entry.getKey().test(target)) {
                found.add(entry.getValue());
            }
        }
        return found;
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public static MatcherIndex getEmptyInstance() {
        return EMPTY_INSTANCE;
    }

    public static class Builder {
        private final Table<Integer, TargetMatcher, BlacklistEntry> entries = HashBasedTable.create();

        public Builder add(TargetMatcher matcher, BlacklistEntry entry) {
            checkNotNull(matcher);
            checkNotNull(entries);
            entries.put(matcher.getMatchedTypeId(), matcher, entry);
            return this;
        }

        public MatcherIndex build() {
            return new MatcherIndex(entries);
        }
    }

}
