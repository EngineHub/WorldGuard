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

import com.google.common.util.concurrent.ListeningExecutorService;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.protection.databases.mysql.MySQLDatabaseImpl;

import java.util.logging.Logger;

/**
 * A store that persists regions in a MySQL database.
 */
public class MySQLDatabase extends MySQLDatabaseImpl {

    /**
     * Create a new instance.
     *
     * @param executor the executor to perform loads and saves in
     * @param config the configuration
     * @param worldName the world name
     * @param logger a logger
     * @throws ProtectionDatabaseException thrown on error
     */
    public MySQLDatabase(ConfigurationManager config, String worldName, Logger logger) throws ProtectionDatabaseException {
        super(config, worldName, logger);
    }

}
