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

import java.sql.PreparedStatement;
import java.sql.SQLException;

class StatementBatch {

    public static final int MAX_BATCH_SIZE = 100;

    private final PreparedStatement stmt;
    private final int batchSize;
    private int count = 0;

    StatementBatch(PreparedStatement stmt, int batchSize) {
        this.stmt = stmt;
        this.batchSize = batchSize;
    }

    public void addBatch() throws SQLException {
        stmt.addBatch();
        count++;
        if (count > batchSize) {
            stmt.executeBatch();
            count = 0;
        }
    }

    public void executeRemaining() throws SQLException {
        if (count > 0) {
            count = 0;
            stmt.executeBatch();
        }
    }

}
