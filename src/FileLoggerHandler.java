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

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;
import java.util.regex.*;
import java.util.Map;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TreeMap;
import java.util.Iterator;
import java.text.SimpleDateFormat;

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
     * Cache of writers.
     */
    private TreeMap<String,FileLoggerWriter> writers =
            new TreeMap<String,FileLoggerWriter>();

    /**
     * Construct the object.
     *
     * @param pathPattern
     */
    public FileLoggerHandler(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    /**
     * Construct the object.
     *
     * @param pathPattern
     * @param cacheSize
     */
    public FileLoggerHandler(String pathPattern, int cacheSize) {
        if (cacheSize < 1) {
            throw new IllegalArgumentException("Cache size cannot be less than 1");
        }
        this.pathPattern = pathPattern;
        this.cacheSize = cacheSize;
    }

    /**
     * Build the path.
     * 
     * @return
     */
    private String buildPath(String playerName) {
        GregorianCalendar calendar = new GregorianCalendar();

        Pattern p = Pattern.compile("%.");
        Matcher m = p.matcher(pathPattern);
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
    private void log(Player player, String message, String comment) {
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
     * Log a block destroy attempt.
     *
     * @param player
     * @param block
     */
    public void logDestroyAttempt(Player player, Block block, String comment) {
        log(player, "Tried to destroy " + getFriendlyItemName(block.getType()),
                comment);
    }

    /**
     * Log a block break attempt.
     *
     * @param player
     * @param block
     */
    public void logBreakAttempt(Player player, Block block, String comment) {
        log(player, "Tried to break " + getFriendlyItemName(block.getType()),
                comment);
    }

    /**
     * Log a right click on attempt.
     *
     * @param player
     * @param block
     */
    public void logUseAttempt(Player player, Block block, String comment) {
        log(player, "Tried to use " + getFriendlyItemName(block.getType()),
                comment);
    }

    /**
     * Log an attempt to destroy with an item.
     *
     * @param player
     * @param item
     */
    public void logDestroyWithAttempt(Player player, int item, String comment) {
        log(player, "Tried to destroy with " + getFriendlyItemName(item), comment);
    }

    /**
     * Log a block creation attempt.
     *
     * @param player
     * @param item
     */
    public void logCreateAttempt(Player player, int item, String comment) {
        log(player, "Tried to create " + getFriendlyItemName(item), comment);
    }

    /**
     * Log a drop attempt.
     *
     * @param player
     * @param item
     */
    public void logDropAttempt(Player player, int item, String comment) {
        log(player, "Tried to drop " + getFriendlyItemName(item), comment);
    }

    /**
     * Get an item's friendly name with its ID.
     *
     * @param id
     */
    private static String getFriendlyItemName(int id) {
        return etc.getDataSource().getItem(id) + " (#" + id + ")";
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
