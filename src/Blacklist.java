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

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;

/**
 *
 * @author sk89q
 */
public class Blacklist {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");
    
    /**
     * List of entries by block ID.
     */
    private Map<Integer,List<BlacklistEntry>> blacklist
            = new HashMap<Integer,List<BlacklistEntry>>();
    /**
     * Logger.
     */
    private BlacklistLogger blacklistLogger = new BlacklistLogger();

    /**
     * Blacklist contains on-acquire events.
     */
    private boolean hasOnAcquire;

    /**
     * Returns whether the list is empty.
     * 
     * @return
     */
    public boolean isEmpty() {
        return blacklist.isEmpty();
    }

    /**
     * Get the entries for an item or list.
     */
    public List<BlacklistEntry> getEntries(int id) {
        return blacklist.get(id);
    }
    
    /**
     * Get the logger.
     * 
     * @return
     */
    public BlacklistLogger getLogger() {
        return blacklistLogger;
    }

    /**
     * Called on block destruction. Returns true to let the action pass
     * through.
     *
     * @param block
     * @param player
     * @return
     */
    public boolean onDestroy(final Block block, final Player player) {
        List<BlacklistEntry> entries = getEntries(block.getType());
        if (entries == null) {
            return true;
        }
        boolean ret = true;
        for (BlacklistEntry entry : entries) {
            if (!entry.onDestroy(block, player)) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * Called on block break. Returns true to let the action pass
     * through.
     *
     * @param block
     * @param player
     * @return
     */
    public boolean onBreak(final Block block, final Player player) {
        List<BlacklistEntry> entries = getEntries(block.getType());
        if (entries == null) {
            return true;
        }
        boolean ret = true;
        for (BlacklistEntry entry : entries) {
            if (!entry.onBreak(block, player)) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * Called on left click. Returns true to let the action pass through.
     *
     * @param item
     * @param player
     * @return
     */
    public boolean onDestroyWith(int item, Player player) {
        List<BlacklistEntry> entries = getEntries(item);
        if (entries == null) {
            return true;
        }
        boolean ret = true;
        for (BlacklistEntry entry : entries) {
            if (!entry.onDestroyWith(item, player)) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * Called on right click. Returns true to let the action pass through.
     *
     * @param item
     * @param player
     * @return
     */
    public boolean onCreate(int item, Player player) {
        List<BlacklistEntry> entries = getEntries(item);
        if (entries == null) {
            return true;
        }
        boolean ret = true;
        for (BlacklistEntry entry : entries) {
            if (!entry.onCreate(item, player)) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * Called on right click upon. Returns true to let the action pass through.
     *
     * @param item
     * @param player
     * @return
     */
    public boolean onUse(Block block, Player player) {
        List<BlacklistEntry> entries = getEntries(block.getType());
        if (entries == null) {
            return true;
        }
        boolean ret = true;
        for (BlacklistEntry entry : entries) {
            if (!entry.onUse(block, player)) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * Called on right click upon. Returns true to let the action pass through.
     *
     * @param item
     * @param player
     * @return
     */
    public boolean onSilentUse(Block block, Player player) {
        List<BlacklistEntry> entries = getEntries(block.getType());
        if (entries == null) {
            return true;
        }
        boolean ret = true;
        for (BlacklistEntry entry : entries) {
            if (!entry.onSilentUse(block, player)) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * Called on drop. Returns true to let the action pass through.
     *
     * @param item
     * @param player
     * @return
     */
    public boolean onDrop(int item, Player player) {
        List<BlacklistEntry> entries = getEntries(item);
        if (entries == null) {
            return true;
        }
        boolean ret = true;
        for (BlacklistEntry entry : entries) {
            if (!entry.onDrop(item, player)) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * Called on acquire. Returns true to let the action pass through.
     *
     * @param item
     * @param player
     * @return
     */
    public boolean onAcquire(int item, Player player) {
        List<BlacklistEntry> entries = getEntries(item);
        if (entries == null) {
            return true;
        }
        boolean ret = true;
        for (BlacklistEntry entry : entries) {
            if (!entry.onAcquire(item, player)) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * Called on acquire. Returns true to let the action pass through.
     *
     * @param item
     * @param player
     * @return
     */
    public boolean onSilentAcquire(int item, Player player) {
        List<BlacklistEntry> entries = getEntries(item);
        if (entries == null) {
            return true;
        }
        boolean ret = true;
        for (BlacklistEntry entry : entries) {
            if (!entry.onSilentAcquire(item, player)) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * Load the blacklist.
     *
     * @param file
     * @return
     * @throws IOException
     */
    public void load(File file) throws IOException {
        FileReader input = null;
        Map<Integer,List<BlacklistEntry>> blacklist =
                new HashMap<Integer,List<BlacklistEntry>>();

        try {
            input = new FileReader(file);
            BufferedReader buff = new BufferedReader(input);

            hasOnAcquire = false;

            String line;
            List<BlacklistEntry> currentEntries = null;
            while ((line = buff.readLine()) != null) {
                line = line.trim();

                // Blank line
                if (line.length() == 0) {
                    continue;
                } else if (line.charAt(0) == ';' || line.charAt(0) == '#') {
                    continue;
                }

                if (line.matches("^\\[.*\\]$")) {
                    String[] items = line.substring(1, line.length() - 1).split(",");
                    currentEntries = new ArrayList<BlacklistEntry>();

                    for (String item : items) {
                        int id = 0;

                        try {
                            id = Integer.parseInt(item.trim());
                        } catch (NumberFormatException e) {
                            id = etc.getDataSource().getItem(item.trim());
                            if (id == 0) {
                                logger.log(Level.WARNING, "WorldGuard: Unknown block name: "
                                        + item);
                                break;
                            }
                        }

                        BlacklistEntry entry = new BlacklistEntry(this);
                        if (blacklist.containsKey(id)) {
                            blacklist.get(id).add(entry);
                        } else {
                            List<BlacklistEntry> entries = new ArrayList<BlacklistEntry>();
                            entries.add(entry);
                            blacklist.put(id, entries);
                        }
                        currentEntries.add(entry);
                    }
                } else if (currentEntries != null) {
                    String[] parts = line.split("=");

                    if (parts.length == 1) {
                        logger.log(Level.WARNING, "Found option with no value "
                                + file.getName() + " for '" + line + "'");
                        continue;
                    }

                    boolean unknownOption = false;

                    for (BlacklistEntry entry : currentEntries) {
                        if (parts[0].equalsIgnoreCase("ignore-groups")) {
                            entry.setIgnoreGroups(parts[1].split(","));
                        } else if (parts[0].equalsIgnoreCase("on-destroy")) {
                            entry.setDestroyActions(parts[1].split(","));
                        } else if (parts[0].equalsIgnoreCase("on-break")) {
                            entry.setBreakActions(parts[1].split(","));
                        } else if (parts[0].equalsIgnoreCase("on-left")
                                || parts[0].equalsIgnoreCase("on-destroy-with")) {
                            entry.setDestroyWithActions(parts[1].split(","));
                        } else if (parts[0].equalsIgnoreCase("on-right")
                                || parts[0].equalsIgnoreCase("on-create")) {
                            entry.setCreateActions(parts[1].split(","));
                        } else if (parts[0].equalsIgnoreCase("on-right-on")
                                || parts[0].equalsIgnoreCase("on-use")) {
                            entry.setUseActions(parts[1].split(","));
                        } else if (parts[0].equalsIgnoreCase("on-drop")) {
                            entry.setDropActions(parts[1].split(","));
                        } else if (parts[0].equalsIgnoreCase("on-acquire")) {
                            hasOnAcquire = true;
                            entry.setAcquireActions(parts[1].split(","));
                        } else if (parts[0].equalsIgnoreCase("message")) {
                            entry.setMessage(parts[1].trim());
                        } else if (parts[0].equalsIgnoreCase("comment")) {
                            entry.setComment(parts[1].trim());
                        } else {
                            unknownOption = true;
                        }
                    }

                    if (unknownOption) {
                        logger.log(Level.WARNING, "Unknown option '" + parts[0]
                                + "' in " + file.getName() + " for '" + line + "'");
                    }
                } else {
                    logger.log(Level.WARNING, "Found option with no heading "
                            + file.getName() + " for '" + line + "'");
                }
            }

            this.blacklist = blacklist;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e2) {
            }
        }
    }

    /**
     * Blacklist contains on-acquire events.
     * 
     * @return
     */
    public boolean hasOnAcquire() {
        return hasOnAcquire;
    }
}
