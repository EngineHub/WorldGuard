// $Id$
/*
 * WorldGuard
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

package com.sk89q.worldguard.blacklist.loggers;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;
import java.util.regex.*;
import java.util.*;
import java.text.SimpleDateFormat;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.events.*;

/**
 *
 * @author sk89q
 */
public class FileLoggerHandler implements BlacklistLoggerHandler {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");
    /**
     * Regex for patterns in the path.
     */
    private static Pattern pattern = Pattern.compile("%.");
    /**
     * Date format.
     */
    private static SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyyy-MM-dd HH:mm:ss");

    /**
     * Number of files to keep open at a time.
     */
    private int cacheSize = 10;
    /**
     * Path pattern.
     */
    private String pathPattern;
    /**
     * World name.
     */
    private String worldName;
    /**
     * Cache of writers.
     */
    private TreeMap<String,FileLoggerWriter> writers =
            new TreeMap<String,FileLoggerWriter>();

    /**
     * Construct the object.
     *
     * @param pathPattern
     * @param worldName 
     */
    public FileLoggerHandler(String pathPattern, String worldName) {
        this.pathPattern = pathPattern;
        this.worldName = worldName;
    }

    /**
     * Construct the object.
     *
     * @param pathPattern
     * @param cacheSize
     * @param worldName 
     */
    public FileLoggerHandler(String pathPattern, int cacheSize, String worldName) {
        if (cacheSize < 1) {
            throw new IllegalArgumentException("Cache size cannot be less than 1");
        }
        this.pathPattern = pathPattern;
        this.cacheSize = cacheSize;
        this.worldName = worldName;
    }

    /**
     * Build the path.
     * 
     * @return
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
     * @param player
     * @param message
     */
    private void log(LocalPlayer player, String message, String comment) {
        String path = buildPath(player.getName());
        try {
            String date = dateFormat.format(new Date());
            String line = "[" + date + "] " + player.getName() + ": " + message
                    + (comment != null ? " (" + comment + ")" : "") + "\r\n";
            
            FileLoggerWriter writer = writers.get(path);

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
            writer = new FileLoggerWriter(path, out);
            writers.put(path, writer);

            // Check to make sure our cache doesn't get too big!
            if (writers.size() > cacheSize) {
                Iterator<Map.Entry<String,FileLoggerWriter>> it =
                        writers.entrySet().iterator();

                // Remove some entries
                for (; it.hasNext(); ) {
                    Map.Entry<String,FileLoggerWriter> entry = it.next();
                    try {
                        entry.getValue().getWriter().close();
                    } catch (IOException e) {
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
     * @param pos
     * @return
     */
    private String getCoordinates(Vector pos) {
        return "@" + pos.getBlockX() + "," + pos.getBlockY() + "," + pos.getBlockZ();
    }
    
    private void logEvent(BlacklistEvent event, String text, int id, Vector pos, String comment) {
        log(event.getPlayer(), "Tried to " + text + " " + getFriendlyItemName(id)
                + " " + getCoordinates(pos), comment);
    }
    
    /**
     * Log an event.
     *
     * @param event
     */
    public void logEvent(BlacklistEvent event, String comment) {
        // Block break
        if (event instanceof BlockBreakBlacklistEvent) {
            BlockBreakBlacklistEvent evt = (BlockBreakBlacklistEvent)event;
            logEvent(event, "break", evt.getType(), evt.getPosition(), comment);
        
        // Block place
        } else if (event instanceof BlockPlaceBlacklistEvent) {
            BlockPlaceBlacklistEvent evt = (BlockPlaceBlacklistEvent)event;
            logEvent(event, "place", evt.getType(), evt.getPosition(), comment);
        
        // Block interact
        } else if (event instanceof BlockInteractBlacklistEvent) {
            BlockInteractBlacklistEvent evt = (BlockInteractBlacklistEvent)event;
            logEvent(event, "interact with", evt.getType(), evt.getPosition(), comment);
        
        // Destroy with
        } else if (event instanceof DestroyWithBlacklistEvent) {
            DestroyWithBlacklistEvent evt = (DestroyWithBlacklistEvent)event;
            logEvent(event, "destroy with", evt.getType(), evt.getPosition(), comment);
            
        // Acquire
        } else if (event instanceof ItemAcquireBlacklistEvent) {
            ItemAcquireBlacklistEvent evt = (ItemAcquireBlacklistEvent)event;
            logEvent(event, "acquire", evt.getType(), evt.getPosition(), comment);
        
        // Drop
        } else if (event instanceof ItemDropBlacklistEvent) {
            ItemDropBlacklistEvent evt = (ItemDropBlacklistEvent)event;
            logEvent(event, "drop", evt.getType(), evt.getPosition(), comment);
            
        // Use
        } else if (event instanceof ItemUseBlacklistEvent) {
            ItemUseBlacklistEvent evt = (ItemUseBlacklistEvent)event;
            logEvent(event, "use", evt.getType(), evt.getPosition(), comment);
        
        // Unknown
        } else {
            log(event.getPlayer(), "Unknown event: "
                    + event.getClass().getCanonicalName(), comment);
        }
    }

    /**
     * Get an item's friendly name with its ID.
     *
     * @param id
     */
    private static String getFriendlyItemName(int id) {
        ItemType type = ItemType.fromID(id);
        if (type != null) {
            return type.getName() + " (#" + id + ")";
        } else {
            return "#" + id + "";
        }
    }

    /**
     * Close handles.
     */
    public void close() {
        for (Map.Entry<String,FileLoggerWriter> entry : writers.entrySet()) {
            try {
                entry.getValue().getWriter().close();
            } catch (IOException e) {
            }
        }

        writers.clear();
    }
}
