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

package com.sk89q.worldguard.blacklist.target;

import com.google.common.base.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

public class DataValueRangeMatcher implements TargetMatcher {

    private final int typeId;
    private final Predicate<Short> dataMatcher;

    public DataValueRangeMatcher(int typeId, Predicate<Short> dataMatcher) {
        checkNotNull(dataMatcher);
        this.typeId = typeId;
        this.dataMatcher = dataMatcher;
    }

    @Override
    public int getMatchedTypeId() {
        return typeId;
    }

    @Override
    public boolean test(Target target) {
        return typeId == target.getTypeId() && isDataInRange(target.getData());
    }

    private boolean isDataInRange(short data) {
        return dataMatcher.apply(data);
    }

}
