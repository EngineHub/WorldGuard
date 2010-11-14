// $Id$
/*
 * WorldProtect
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldprotect;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Level;

/**
 * Used for formatting.
 *
 * @author sk89q
 */
public class SimpleLogFormat extends Formatter {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public String format(LogRecord record) {
        StringBuilder text = new StringBuilder();
        Level level = record.getLevel();

        text.append("[");
        text.append(dateFormat.format(new Date(record.getMillis())));
        text.append("] ");
        text.append(record.getMessage());
        text.append("\r\n");
        
        return text.toString();
    }
}
