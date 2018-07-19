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

package com.sk89q.worldguard.blacklist.logger;

import javax.annotation.Nullable;
import java.io.BufferedWriter;

public class LogFileWriter implements Comparable<LogFileWriter> {

    public String path;
    private BufferedWriter writer;
    private long lastUse;

    /**
     * Construct the object.
     *
     * @param path The path to write to
     * @param writer The writer for the file
     */
    public LogFileWriter(String path, BufferedWriter writer) {
        this.path = path;
        this.writer = writer;
        lastUse = System.currentTimeMillis();
    }

    /**
     * File path.
     *
     * @return The path the logger is logging to
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the writer being logged to
     */
    public BufferedWriter getWriter() {
        return writer;
    }

    /**
     * @return the lastUse
     */
    public long getLastUse() {
        return lastUse;
    }

    /**
     * Update last use time.
     */
    public void updateLastUse() {
        lastUse = System.currentTimeMillis();
    }

    @Override
    public int compareTo(@Nullable LogFileWriter other) {
        if (other == null) {
            return 1;
        } else if (lastUse > other.lastUse) {
            return 1;
        } else if (lastUse < other.lastUse) {
            return -1;
        } else {
            return 0;
        }
    }

}
