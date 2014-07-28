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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.event.BlacklistEvent;
import com.sk89q.worldguard.blacklist.target.Target;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileHandler implements LoggerHandler {

    private static Pattern pattern = Pattern.compile("%.");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private int cacheSize = 10;
    private String pathPattern;
    private String worldName;
    private TreeMap<String,LogFileWriter> writers = new TreeMap<String,LogFileWriter>();
    
    private final Logger logger;

    /**
     * Construct the object.
     *
     * @param pathPattern The pattern for the log file path
     * @param worldName The name of the world
     * @param logger The logger used to log errors
     */
    public FileHandler(String pathPattern, String worldName, Logger logger) {
        this.pathPattern = pathPattern;
        this.worldName = worldName;
        this.logger = logger;
    }

    /**
     * Construct the object.
     *
     * @param pathPattern The pattern for logfile paths
     * @param cacheSize The size of the file cache
     * @param worldName The name of the associated world
     * @param logger The logger to log errors with
     */
    public FileHandler(String pathPattern, int cacheSize, String worldName, Logger logger) {
        if (cacheSize < 1) {
            throw new IllegalArgumentException("Cache size cannot be less than 1");
        }
        this.pathPattern = pathPattern;
        this.cacheSize = cacheSize;
        this.worldName = worldName;
        this.logger = logger;
    }

    /**
     * Build the path.
     *
     * @param playerName The name of the player
     * @return The path for the logfile
     */
    private String buildPath(String playerName) {
        GregorianCalendar calendar = new GregorianCalendar();

        Matcher m = pattern.matcher(pathPattern);
        StringBuffer buffer = new StringBuffer();

        // Pattern replacements
        while (m.find()) {
            String group = m.group();
            String rep = "?";

            if (group.matches("%%")) {
                rep = "%";
            } else if (group.matches("%u")) {
                rep = playerName.toLowerCase().replaceAll("[^A-Za-z0-9_]", "_");
                if (rep.length() > 32) { // Actual max length is 16
                    rep = rep.substring(0, 32);
                }

            }else if (group.matches("%w")) {
                rep = worldName.toLowerCase().replaceAll("[^A-Za-z0-9_]", "_");
                if (rep.length() > 32) { // Actual max length is 16
                    rep = rep.substring(0, 32);
                }

            // Date and time
            } else if (group.matches("%Y")) {
                rep = String.valueOf(calendar.get(Calendar.YEAR));
            } else if (group.matches("%m")) {
                rep = String.format("%02d", calendar.get(Calendar.MONTH));
            } else if (group.matches("%d")) {
                rep = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
            } else if (group.matches("%W")) {
                rep = String.format("%02d", calendar.get(Calendar.WEEK_OF_YEAR));
            } else if (group.matches("%H")) {
                rep = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
            } else if (group.matches("%h")) {
                rep = String.format("%02d", calendar.get(Calendar.HOUR));
            } else if (group.matches("%i")) {
                rep = String.format("%02d", calendar.get(Calendar.MINUTE));
            } else if (group.matches("%s")) {
                rep = String.format("%02d", calendar.get(Calendar.SECOND));
            }

            m.appendReplacement(buffer, rep);
        }

        m.appendTail(buffer);

        return buffer.toString();
    }

    /**
     * Log a message.
     *
     * @param player The player to log
     * @param message The message to log
     * @param comment The comment associated with the logged event
     */
    private void log(LocalPlayer player, String message, String comment) {
        String path = buildPath(player.getName());
        try {
            String date = dateFormat.format(new Date());
            String line = "[" + date + "] " + player.getName() + ": " + message
                    + (comment != null ? " (" + comment + ")" : "") + "\r\n";

            LogFileWriter writer = writers.get(path);

            // Writer already exists!
            if (writer != null) {
                try {
                    BufferedWriter out = writer.getWriter();
                    out.write(line);
                    out.flush();
                    writer.updateLastUse();
                    return;
                } catch (IOException e) {
                    // Failed initial rewrite... let's re-open
                }
            }

            // Make parent directory
            File file = new File(path);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            FileWriter stream = new FileWriter(path, true);
            BufferedWriter out = new BufferedWriter(stream);
            out.write(line);
            out.flush();
            writer = new LogFileWriter(path, out);
            writers.put(path, writer);

            // Check to make sure our cache doesn't get too big!
            if (writers.size() > cacheSize) {
                Iterator<Map.Entry<String,LogFileWriter>> it =
                        writers.entrySet().iterator();

                // Remove some entries
                for (; it.hasNext(); ) {
                    Map.Entry<String,LogFileWriter> entry = it.next();
                    try {
                        entry.getValue().getWriter().close();
                    } catch (IOException ignore) {
                    }
                    it.remove();

                    // Trimmed enough
                    if (writers.size() <= cacheSize) {
                        break;
                    }
                }
            }

        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to log blacklist event to '"
                    + path + "': " + e.getMessage());
        }
    }

    /**
     * Gets the coordinates in text form for the log.
     *
     * @param pos The position to get coordinates for
     * @return The position's coordinates in human-readable form
     */
    private String getCoordinates(Vector pos) {
        return "@" + pos.getBlockX() + "," + pos.getBlockY() + "," + pos.getBlockZ();
    }

    private void logEvent(BlacklistEvent event, String text, Target target, Vector pos, String comment) {
        log(event.getPlayer(), "Tried to " + text + " " + target.getFriendlyName() + " " + getCoordinates(pos), comment);
    }

    @Override
    public void logEvent(BlacklistEvent event, String comment) {
        logEvent(event, event.getDescription(), event.getTarget(), event.getPosition(), comment);
    }

    /**
     * Get an item's friendly name with its ID.
     *
     * @param id The id to get a friendly name for
     * @return The friendly name
     */
    private static String getFriendlyItemName(int id) {
        ItemType type = ItemType.fromID(id);
        if (type != null) {
            return type.getName() + " (#" + id + ")";
        } else {
            return "#" + id + "";
        }
    }

    @Override
    public void close() {
        for (Map.Entry<String,LogFileWriter> entry : writers.entrySet()) {
            try {
                entry.getValue().getWriter().close();
            } catch (IOException ignore) {
            }
        }

        writers.clear();
    }

}
