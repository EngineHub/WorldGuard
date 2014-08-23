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

package com.sk89q.worldguard.protection.managers.storage.sql;

import com.sk89q.worldguard.protection.managers.storage.sql.TableCache.GroupNameCache;
import com.sk89q.worldguard.protection.managers.storage.sql.TableCache.UserNameCache;
import com.sk89q.worldguard.protection.managers.storage.sql.TableCache.UserUuidCache;
import com.sk89q.worldguard.util.sql.DataSourceConfig;

import java.sql.Connection;

class DomainTableCache {

    private final UserNameCache userNameCache;
    private final UserUuidCache userUuidCache;
    private final GroupNameCache groupNameCache;

    DomainTableCache(DataSourceConfig config, Connection conn) {
        userNameCache = new UserNameCache(config, conn);
        userUuidCache = new UserUuidCache(config, conn);
        groupNameCache = new GroupNameCache(config, conn);
    }

    public UserNameCache getUserNameCache() {
        return userNameCache;
    }

    public UserUuidCache getUserUuidCache() {
        return userUuidCache;
    }

    public GroupNameCache getGroupNameCache() {
        return groupNameCache;
    }

}
