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

import com.sk89q.worldguard.util.profile.Profile;
import io.papermc.lib.PaperLib;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.UUID;

/**
 * @deprecated Use {@link com.sk89q.worldguard.util.profile.resolver.PaperPlayerService} instead
 */
@Deprecated(forRemoval = true)
public final class PaperProfileService extends SingleRequestService {
    private static final PaperPlayerService SUPER = PaperPlayerService.getInstance();
    private static final PaperProfileService INSTANCE = new PaperProfileService();

    private PaperProfileService() {
        if (!PaperLib.isPaper()) {
            throw new IllegalStateException("Попытка получить доступ к PaperProfileService на не-Paper сервере.");
        }
    }

    @Override
    public int getIdealRequestLimit() {
        return SUPER.getIdealRequestLimit();
    }

    @Override
    @Nullable
    public Profile findByName(String name) throws IOException, InterruptedException {
        return SUPER.findByName(name);
    }

    @Nullable
    @Override
    public Profile findByUuid(UUID uuid) throws IOException, InterruptedException {
        return SUPER.findByUuid(uuid);
    }

    public static PaperProfileService getInstance() {
        return INSTANCE;
    }
}
