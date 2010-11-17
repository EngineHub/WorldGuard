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
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author sk89q
 */
public class BlacklistEntry {
    /**
     * Used to prevent spamming.
     */
    private static Map<String,Integer> lastAffected =
            new HashMap<String,Integer>();
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("WorldGuard.Blacklist");
    /**
     * List of groups to not affect.
     */
    private Set<String> ignoreGroups;
    /**
     * List of actions to perform on destruction.
     */
    private String[] destroyActions;
    /**
     * List of actions to perform on left click.
     */
    private String[] leftClickActions;
    /**
     * List of actions to perform on right click.
     */
    private String[] rightClickActions;

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
     * @return the destroyActions
     */
    public String[] getDestroyActions() {
        return destroyActions;
    }

    /**
     * @param destroyActions the destroyActions to set
     */
    public void setDestroyActions(String[] destroyActions) {
        this.destroyActions = destroyActions;
    }

    /**
     * @return the leftClickActions
     */
    public String[] getLeftClickActions() {
        return leftClickActions;
    }

    /**
     * @param leftClickActions the leftClickActions to set
     */
    public void setLeftClickActions(String[] leftClickActions) {
        this.leftClickActions = leftClickActions;
    }

    /**
     * @return the rightClickActions
     */
    public String[] getRightClickActions() {
        return rightClickActions;
    }

    /**
     * @param rightClickActions the rightClickActions to set
     */
    public void setRightClickActions(String[] rightClickActions) {
        this.rightClickActions = rightClickActions;
    }

    /**
     * Returns true if this player should be ignored.
     *
     * @param player
     * @return
     */
    public boolean shouldIgnore(Player player) {
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
     * Announces a message to all administrators.
     * 
     * @param str
     */
    public void notifyAdmins(String str) {
        for (Player player : etc.getServer().getPlayerList()) {
            if (player.canUseCommand("/wprotectalerts")) {
                player.sendMessage(Colors.LightPurple + "WorldGuard: " + str);
            }
        }
    }

    /**
     * Log a message.
     * 
     * @param message
     */
    public void log(String message) {
        logger.log(Level.INFO, message);
    }

    /**
     * Called on block destruction. Returns true to let the action pass
     * through.
     *
     * @param block
     * @param player
     * @return
     */
    public boolean onDestroy(Block block, Player player) {
        if (destroyActions == null) {
            return true;
        }
        boolean ret = process(block.getType(), player, destroyActions);
        lastAffected.put(player.getName(), block.getType());
        return ret;
    }

    /**
     * Called on left click. Returns true to let the action pass through.
     *
     * @param item
     * @param player
     * @return
     */
    public boolean onLeftClick(int item, Player player) {
        if (leftClickActions == null) {
            return true;
        }
        boolean ret = process(item, player, leftClickActions);
        lastAffected.put(player.getName(), item);
        return ret;
    }

    /**
     * Called on right right. Returns true to let the action pass through.
     *
     * @param item
     * @param player
     * @return
     */
    public boolean onRightClick(int item, Player player) {
        if (rightClickActions == null) {
            return true;
        }
        boolean ret = process(item, player, rightClickActions);
        lastAffected.put(player.getName(), item);
        return ret;
    }

    /**
     * Internal method to handle the actions.
     * 
     * @param id
     * @param player
     * @param actions
     * @return
     */
    private boolean process(int id, Player player, String[] actions) {
        if (shouldIgnore(player)) {
            return true;
        }

        String name = player.getName();
        boolean repeating = lastAffected.containsKey(name)
                && lastAffected.get(name) == id;

        boolean ret = true;
        
        for (String action : actions) {
            if (action.equalsIgnoreCase("deny")) {
                ret = false;
            } else if (action.equalsIgnoreCase("kick")) {
                player.kick("Performed disallowed action with "
                        + etc.getDataSource().getItem(id) + ".");
            } else if (!repeating) {
                if (action.equalsIgnoreCase("notify")) {
                    notifyAdmins(player.getName() + " on destroy: "
                            + etc.getDataSource().getItem(id));
                } else if (action.equalsIgnoreCase("log")) {
                    log(player.getName() + " on destroy: "
                            + etc.getDataSource().getItem(id));
                } else if (!repeating && action.equalsIgnoreCase("tell")) {
                    player.sendMessage("Can't do that with "
                            + etc.getDataSource().getItem(id) + ".");
                }
            }
        }

        return ret;
    }

    /**
     * Forget a player.
     *
     * @param player
     */
    public static void forgetPlayer(Player player) {
        lastAffected.remove(player.getName());
    }

    /**
     * Forget all players.
     *
     * @param player
     */
    public static void forgetAllPlayers() {
        lastAffected = new HashMap<String,Integer>();
    }
}
