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


package com.sk89q.worldguard.bukkit.commands;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Michael
 */
public class CommandHandler {

    private WorldGuardPlugin wg;
    private Map<String, WgCommand> commandMap;

    private static Pattern groupPattern = Pattern.compile("^[gG]:(.+)$");
    public static int CMD_LIST_SIZE = 9;
    
    public CommandHandler(WorldGuardPlugin wg)
    {
        this.wg = wg;
        this.commandMap = new HashMap<String, WgCommand>();

        this.commandMap.put("allowfire", new CommandAllowFire());
        this.commandMap.put("god", new CommandGod());
        this.commandMap.put("heal", new CommandHeal());
        this.commandMap.put("locate", new CommandLocate());
        this.commandMap.put("region", new RegionCommandHandler());
        this.commandMap.put("reloadwg", new CommandReloadWG());
        this.commandMap.put("slay", new CommandSlay());
        this.commandMap.put("stack", new CommandStack());
        this.commandMap.put("stopfire", new CommandStopFire());
        this.commandMap.put("tpregrion", new CommandTpRegion());
    }
    
    
    public boolean handleCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        try {

            String cmdName = cmd.getName().toLowerCase();
            WgCommand wgcmd = commandMap.get(cmdName);
            if (wgcmd == null) {
                return false;
            }

            String senderName = sender instanceof Player ? ((Player)sender).getName() : "Console";
            
            wgcmd.handle(sender, senderName, cmdName, args, this, wg);
            return true;

        } catch (InsufficientArgumentsException e) {
            if (e.getHelp() != null) {
                sender.sendMessage(ChatColor.RED + e.getHelp());
                return true;
            } else {
                return false;
            }
        } catch (InsufficientPermissionsException e) {
            sender.sendMessage(ChatColor.RED + "You don't have sufficient permission.");
            return true;
        } catch (CommandHandlingException e) {
            return true;
        } catch (Throwable t) {
            sender.sendMessage(ChatColor.RED + "ERROR: " + t.getMessage());
            t.printStackTrace();
            return true;
        }

    }

      /**
     * Checks to make sure that there are enough but not too many arguments.
     *
     * @param args
     * @param min
     * @param max -1 for no maximum
     * @throws InsufficientArgumentsException
     */
    public static void checkArgs(String[] args, int min, int max)
            throws InsufficientArgumentsException {
        if (args.length < min || (max != -1 && args.length > max)) {
            throw new InsufficientArgumentsException();
        }
    }

    /**
     * Checks to make sure that there are enough but not too many arguments.
     *
     * @param args
     * @param min
     * @param max -1 for no maximum
     * @param help
     * @throws InsufficientArgumentsException
     */
    public static void checkArgs(String[] args, int min, int max, String help)
            throws InsufficientArgumentsException {
        if (args.length < min || (max != -1 && args.length > max)) {
            throw new InsufficientArgumentsException(help);
        }
    }

     /**
     * Thrown when command handling has raised an exception.
     *
     * @author sk89q
     */
    public static class CommandHandlingException extends Exception {
        private static final long serialVersionUID = 7912130636812036780L;
    }

    /**
     * Thrown when a player has insufficient permissions.
     *
     * @author sk89q
     */
    public static class InsufficientPermissionsException extends CommandHandlingException {
        private static final long serialVersionUID = 9087662707619954750L;
    }

    /**
     * Thrown when a command wasn't given sufficient arguments.
     *
     * @author sk89q
     */
    public static class InsufficientArgumentsException extends CommandHandlingException {
        private static final long serialVersionUID = 4153597953889773788L;
        private final String help;

        public InsufficientArgumentsException() {
            help = null;
        }

        public InsufficientArgumentsException(String msg) {
            this.help = msg;
        }

        public String getHelp() {
            return help;
        }
    }

    
    /**
     * Checks for the command or /region.
     *
     * @param player
     * @param cmd
     * @return
     */
    public boolean canUseRegionCommand(Player player, String cmd) {
        return wg.hasPermission(player, "/region")
                || wg.hasPermission(player, cmd);
    }

    /**
     * Checks to see if there are sufficient permissions, otherwise an exception
     * is raised in that case.
     *
     * @param player
     * @param permission
     * @throws InsufficientPermissionsException
     */
    public void checkRegionPermission(Player player, String permission)
            throws InsufficientPermissionsException {
        if (!wg.hasPermission(player, "/region") && !wg.hasPermission(player, permission)) {
            throw new InsufficientPermissionsException();
        }
    }

    /**
     * Checks to see if there are sufficient permissions, otherwise an exception
     * is raised in that case.
     *
     * @param sender
     * @param permission
     * @throws InsufficientPermissionsException
     */
    public void checkPermission(CommandSender sender, String permission)
            throws InsufficientPermissionsException {
        if (!(sender instanceof Player)) {
            return;
        }
        if (!wg.hasPermission((Player)sender, permission)) {
            throw new InsufficientPermissionsException();
        }
    }

     /**
     * Parse a group/player DefaultDomain specification for areas.
     *
     * @param domain
     * @param split
     * @param startIndex
     */
    public static void addToDomain(DefaultDomain domain,
            String[] split, int startIndex) {
        for (int i = startIndex; i < split.length; i++) {
            String s = split[i];
            Matcher m = groupPattern.matcher(s);
            if (m.matches()) {
                domain.addGroup(m.group(1));
            } else {
                domain.addPlayer(s);
            }
        }
    }

    /**
     * Parse a group/player DefaultDomain specification for areas.
     *
     * @param domain
     * @param split
     * @param startIndex
     */
    public static void removeFromDomain(DefaultDomain domain,
            String[] split, int startIndex) {
        for (int i = startIndex; i < split.length; i++) {
            String s = split[i];
            Matcher m = groupPattern.matcher(s);
            if (m.matches()) {
                domain.removeGroup(m.group(1));
            } else {
                domain.removePlayer(s);
            }
        }
    }

    /**
     * Parse a group/player DefaultDomain specification for areas.
     *
     * @param split
     * @param startIndex
     * @return
     */
    public static DefaultDomain parseDomainString(String[] split, int startIndex) {
        DefaultDomain domain = new DefaultDomain();

        for (int i = startIndex; i < split.length; i++) {
            String s = split[i];
            Matcher m = groupPattern.matcher(s);
            if (m.matches()) {
                domain.addGroup(m.group(1));
            } else {
                domain.addPlayer(s);
            }
        }

        return domain;
    }

}
