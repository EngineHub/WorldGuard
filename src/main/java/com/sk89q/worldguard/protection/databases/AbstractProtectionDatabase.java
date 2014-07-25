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

package com.sk89q.worldguard.protection.databases;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.worldguard.protection.managers.RegionManager;

public abstract class AbstractProtectionDatabase implements ProtectionDatabase {

    @Override
    public void load(RegionManager manager) throws ProtectionDatabaseException {
        load();
        manager.setRegions(getRegions());
    }

    @Override
    public final void save(RegionManager manager) throws ProtectionDatabaseException {
        save(manager, false);
    }

    @Override
    public ListenableFuture<?> load(RegionManager manager, boolean async) {
        try {
            load(manager);
        } catch (ProtectionDatabaseException e) {
            return Futures.immediateFailedFuture(e);
        }
        return Futures.immediateCheckedFuture(this);
    }

    @Override
    public ListenableFuture<?> save(RegionManager manager, boolean async) {
        setRegions(manager.getRegions());
        try {
            save();
        } catch (ProtectionDatabaseException e) {
            return Futures.immediateFailedFuture(e);
        }
        return Futures.immediateCheckedFuture(this);
    }

}
