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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;

/**
 * This is the legacy class for accessing region data.
 *
 * @deprecated use {@link WorldGuardPlatform#getRegionContainer()}
 */
@Deprecated
public class GlobalRegionManager {

    /**
     * Create a new instance.
     *
     * @param container the container
     */
    public GlobalRegionManager(RegionContainer container) {
        checkNotNull(container);
    }

    /**
     * Test the value of a state flag at a location.
     *
     * @param flag the flag
     * @param location the location
     * @return true if set to true
     * @deprecated use {@link RegionContainer#createQuery()}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public boolean allows(StateFlag flag, Location location) {
        return StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().queryState(location, (RegionAssociable) null, flag));
    }
}
