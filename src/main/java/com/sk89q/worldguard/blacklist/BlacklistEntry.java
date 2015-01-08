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

package com.sk89q.worldguard.blacklist;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.action.Action;
import com.sk89q.worldguard.blacklist.action.ActionResult;
import com.sk89q.worldguard.blacklist.event.BlacklistEvent;
import com.sk89q.guavabackport.cache.LoadingCache;

import javax.annotation.Nullable;
import java.util.*;

public class BlacklistEntry {

    private Blacklist blacklist;
    private Set<String> ignoreGroups;
    private Set<String> ignorePermissions;
    private Map<Class<? extends BlacklistEvent>, List<Action>> actions = new HashMap<Class<? extends BlacklistEvent>, List<Action>>();

    private String message;
    private String comment;

    /**
     * Construct the object.
     *
     * @param blacklist The blacklist that contains this entry
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
     * @return the ignoreGroups
     */
    public String[] getIgnorePermissions() {
        return ignorePermissions.toArray(new String[ignorePermissions.size()]);
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
     * @param ignorePermissions the ignorePermissions to set
     */
    public void setIgnorePermissions(String[] ignorePermissions) {
        Set<String> ignorePermissionsSet = new HashSet<String>();
        Collections.addAll(ignorePermissionsSet, ignorePermissions);
        this.ignorePermissions = ignorePermissionsSet;
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
     * @param player The player to check
     * @return whether this player should be ignored for blacklist blocking
     */
    public boolean shouldIgnore(@Nullable LocalPlayer player) {
        if (player == null) {
            return false; // This is the case if the cause is a dispenser, for example
        }

        if (ignoreGroups != null) {
            for (String group : player.getGroups()) {
                if (ignoreGroups.contains(group.toLowerCase())) {
                    return true;
                }
            }
        }

        if (ignorePermissions != null) {
            for (String perm : ignorePermissions) {
                if (player.hasPermission(perm)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get the associated actions with an event.
     *
     * @param eventCls The event's class
     * @return The actions for the given event
     */
    public List<Action> getActions(Class<? extends BlacklistEvent> eventCls) {
        List<Action> ret = actions.get(eventCls);
        if (ret == null) {
            ret = new ArrayList<Action>();
            actions.put(eventCls, ret);
        }
        return ret;
    }

    /**
     * Method to handle the event.
     *
     * @param useAsWhitelist Whether this entry is being used in a whitelist
     * @param event The event to check
     * @param forceRepeat Whether to force repeating notifications even within the delay limit
     * @param silent Whether to prevent notifications from happening
     * @return Whether the action was allowed
     */
    public boolean check(boolean useAsWhitelist, BlacklistEvent event, boolean forceRepeat, boolean silent) {
        LocalPlayer player = event.getPlayer();

        if (shouldIgnore(player)) {
            return true;
        }

        boolean repeating = false;
        String eventCacheKey = event.getCauseName();
        LoadingCache<String, TrackedEvent> repeatingEventCache = blacklist.getRepeatingEventCache();

        // Check to see whether this event is being repeated
        TrackedEvent tracked = repeatingEventCache.getUnchecked(eventCacheKey);
        if (tracked.matches(event)) {
            repeating = true;
        } else {
            tracked.setLastEvent(event);
            tracked.resetTimer();
        }

        List<Action> actions = getActions(event.getClass());

        boolean ret = !useAsWhitelist;

        // Nothing to do
        if (actions == null) {
            return ret;
        }

        for (Action action : actions) {
            ActionResult result = action.apply(event, silent, repeating, forceRepeat);
            switch (result) {
                case INHERIT:
                    continue;
                case ALLOW:
                    ret = true;
                    break;
                case DENY:
                    ret = false;
                    break;
                case ALLOW_OVERRIDE:
                    return true;
                case DENY_OVERRIDE:
                    return false;
            }
        }

        return ret;
    }

}
