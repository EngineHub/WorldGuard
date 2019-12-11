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

package com.sk89q.worldguard.util.profile.resolver;

//import com.destroystokyo.paper.profile.PlayerProfile;
import com.sk89q.worldguard.util.profile.Profile;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;

public final class PaperProfileService extends SingleRequestService {
    private static final PaperProfileService INSTANCE = new PaperProfileService();

    private PaperProfileService() {
        if (!PaperLib.isPaper()) {
            throw new IllegalStateException("Attempt to access PaperProfileService on non-Paper server.");
        }
    }

    @Override
    public int getIdealRequestLimit() {
        return Integer.MAX_VALUE;
    }

    @Override
    @Nullable
    public Profile findByName(String name) {
//        PlayerProfile profile = Bukkit.createProfile(name);
//        if (profile.completeFromCache()) {
//            //noinspection ConstantConditions - completeFromCache guarantees non-null on success
//            return new Profile(profile.getId(), profile.getName());
//        }
        return null;
    }

    public static PaperProfileService getInstance() {
        return INSTANCE;
    }
}
