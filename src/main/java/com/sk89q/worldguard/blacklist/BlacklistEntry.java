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

package com.sk89q.worldguard.blacklist;

import java.util.Set;
import java.util.HashSet;
import org.bukkit.ChatColor;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.events.BlacklistEvent;
import com.sk89q.worldguard.blacklist.events.BlockBreakBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.BlockInteractBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.BlockPlaceBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.DestroyWithBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.ItemAcquireBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.ItemDropBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.ItemUseBlacklistEvent;

/**
 *
 * @author sk89q
 */
public class BlacklistEntry {
    /**
     * Parent blacklist entry.
     */
    private Blacklist blacklist;
    /**
     * List of groups to not affect.
     */
    private Set<String> ignoreGroups;
    
    private String[] breakActions;
    private String[] destroyWithActions;
    private String[] placeActions;
    private String[] interactActions;
    private String[] useActions;
    private String[] dropActions;
    private String[] acquireActions;
    
    private String message;
    private String comment;

    /**
     * Construct the object.
     * 
     * @param blacklist
     */
    public BlacklistEntry(Blacklist blacklist) {
        this.blacklist = blacklist;
    }

    /**
     * @return the ignoreGroups
     */
    public String[] getIgnoreGroups() {
        return ignoreGroups.toArray(new String[ignoreGroups.size()]);
    }

    /**
     * @param ignoreGroups the ignoreGroups to set
     */
    public void setIgnoreGroups(String[] ignoreGroups) {
        Set<String> ignoreGroupsSet = new HashSet<String>();
        for (String group : ignoreGroups) {
            ignoreGroupsSet.add(group.toLowerCase());
        }
        this.ignoreGroups = ignoreGroupsSet;
    }

    /**
     * @return
     */
    public String[] getBreakActions() {
        return breakActions;
    }

    /**
     * @param actions
     */
    public void setBreakActions(String[] actions) {
        this.breakActions = actions;
    }

    /**
     * @return
     */
    public String[] getDestroyWithActions() {
        return destroyWithActions;
    }

    /**
     * @param actions
     */
    public void setDestroyWithActions(String[] actions) {
        this.destroyWithActions = actions;
    }

    /**
     * @return
     */
    public String[] getPlaceActions() {
        return placeActions;
    }

    /**
     * @param actions
     */
    public void setPlaceActions(String[] actions) {
        this.placeActions = actions;
    }

    /**
     * @return
     */
    public String[] getInteractActions() {
        return interactActions;
    }

    /**
     * @param actions
     */
    public void setInteractActions(String[] actions) {
        this.interactActions = actions;
    }

    /**
     * @return
     */
    public String[] getUseActions() {
        return useActions;
    }

    /**
     * @param actions
     */
    public void setUseActions(String[] actions) {
        this.useActions = actions;
    }

    /**
     * @return
     */
    public String[] getDropActions() {
        return dropActions;
    }

    /**
     * @param actions
     */
    public void setDropActions(String[] actions) {
        this.dropActions = actions;
    }

    /**
     * @return
     */
    public String[] getAcquireActions() {
        return acquireActions;
    }

    /**
     * @param actions
     */
    public void setAcquireActions(String[] actions) {
        this.acquireActions = actions;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Returns true if this player should be ignored.
     *
     * @param player
     * @return
     */
    public boolean shouldIgnore(LocalPlayer player) {
        if (ignoreGroups == null) {
            return false;
        }
        for (String group : player.getGroups()) {
            if (ignoreGroups.contains(group.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * Get the associated actions with an event.
     * 
     * @param event
     * @return
     */
    private String[] getActions(BlacklistEvent event) {
        if (event instanceof BlockBreakBlacklistEvent) {
            return breakActions;
            
        } else if (event instanceof BlockPlaceBlacklistEvent) {
            return placeActions;
            
        } else if (event instanceof BlockInteractBlacklistEvent) {
            return interactActions;
            
        } else if (event instanceof DestroyWithBlacklistEvent) {
            return destroyWithActions;
            
        } else if (event instanceof ItemAcquireBlacklistEvent) {
            return acquireActions;
            
        } else if (event instanceof ItemDropBlacklistEvent) {
            return dropActions;
            
        } else if (event instanceof ItemUseBlacklistEvent) {
            return useActions;
            
        } else {
            return null;
        }
    }

    /**
     * Method to handle the event.
     * 
     * @param useAsWhitelist 
     * @param event
     * @param forceRepeat
     * @param silent
     * @return
     */
    public boolean check(boolean useAsWhitelist, BlacklistEvent event, boolean forceRepeat, boolean silent) {
        LocalPlayer player = event.getPlayer();
        
        if (shouldIgnore(player)) {
            return true;
        }

        String name = player.getName();
        long now = System.currentTimeMillis();
        boolean repeating = false;

        // Check to see whether this event is being repeated
        BlacklistTrackedEvent tracked = blacklist.lastAffected.get(name);
        if (tracked != null) {
            if (tracked.matches(event, now)) {
                repeating = true;
            }
        } else {
            blacklist.lastAffected.put(name, new BlacklistTrackedEvent(event, now));
        }

        String actions[] = getActions(event);

        
        boolean ret = useAsWhitelist ? false : true;
        
        // Nothing to do
        if (actions == null) {
            return useAsWhitelist ? false : true;
        }
        
        for (String action : actions) {
            // Deny
            if (action.equalsIgnoreCase("deny")) {
                if (silent) {
                    return false;
                }
                
                ret = false;

            // Allow
            } else if (action.equalsIgnoreCase("allow")) {
                if (silent) {
                    return true;
                }

                ret = true;

            // Kick
            } else if (action.equalsIgnoreCase("kick")) {
                if (silent) {
                    continue;
                }
                
                if (this.message != null) {
                    player.kick(String.format(this.message,
                            getFriendlyItemName(event.getType())));
                } else {
                    player.kick("You can't " + event.getDescription() + " "
                            + getFriendlyItemName(event.getType()));
                }

            // Ban
            } else if (action.equalsIgnoreCase("ban")) {
                if (silent) {
                    continue;
                }
                
                if (this.message != null) {
                    player.ban("Banned: " + String.format(this.message,
                            getFriendlyItemName(event.getType())));
                } else {
                    player.ban("Banned: You can't "
                            + event.getDescription() + " "
                            + getFriendlyItemName(event.getType()));
                }
            
            } else if (!silent && (!repeating || forceRepeat)) {
                // Notify
                if (action.equalsIgnoreCase("notify")) {
                    blacklist.notify(event, comment);
                
                // Log
                } else if (action.equalsIgnoreCase("log")) {
                    blacklist.getLogger().logEvent(event, comment);

                // Tell
                } else if (action.equalsIgnoreCase("tell")) {
                    if (this.message != null) {
                        player.printRaw(ChatColor.YELLOW +
                                String.format(message, getFriendlyItemName(event.getType()))
                                + ".");
                    } else {
                        player.printRaw(ChatColor.YELLOW + "You're not allowed to "
                                + event.getDescription() + " "
                                + getFriendlyItemName(event.getType()) + ".");
                    }
                }
            }
        }

        return ret;
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
}
