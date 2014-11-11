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

package com.sk89q.worldguard.util.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

public class RecordMessagePrefixer extends Handler {

    private final Logger parentLogger;
    private final String prefix;

    public RecordMessagePrefixer(Logger parentLogger, String prefix) {
        checkNotNull(parentLogger);
        checkNotNull(prefix);

        this.parentLogger = parentLogger;
        this.prefix = prefix;
    }

    @Override
    public void publish(LogRecord record) {
        // Ideally we would make a copy of the record
        record.setMessage(prefix + record.getMessage());
        parentLogger.log(record);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

    /**
     * Register a prefix handler on the given logger.
     *
     * @param logger the logger
     * @param prefix the prefix
     */
    public static void register(Logger logger, String prefix) {
        checkNotNull(logger);

        // Fix issues with multiple classloaders loading the same class
        String className = RecordMessagePrefixer.class.getCanonicalName();

        logger.setUseParentHandlers(false);
        for (Handler handler : logger.getHandlers()) {
            if (handler.getClass().getCanonicalName().equals(className)) {
                logger.removeHandler(handler);
            }
        }
        logger.addHandler(new RecordMessagePrefixer(logger.getParent(), prefix));
    }

}
