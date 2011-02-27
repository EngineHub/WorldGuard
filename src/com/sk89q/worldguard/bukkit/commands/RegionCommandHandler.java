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

import com.sk89q.worldguard.bukkit.WorldGuardConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardWorldConfiguration;
import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Michael
 */
public class RegionCommandHandler extends WgCommand {

    private Map<String, WgRegionCommand> commandMap;
    private Map<String, String> aliasMap;

    public RegionCommandHandler() {
        this.commandMap = new HashMap<String, WgRegionCommand>();
        this.aliasMap = new HashMap<String, String>();

        WgRegionCommand addmember = new CommandRegionAddMember();
        WgRegionCommand removemember = new CommandRegionRemoveMember();

        // commands that DO support console as sender
        this.commandMap.put("save", new CommandRegionSave());
        this.commandMap.put("load", new CommandRegionLoad());
        this.commandMap.put("list", new CommandRegionList());
        this.commandMap.put("info", new CommandRegionInfo());
        this.commandMap.put("flag", new CommandRegionFlag());
        this.commandMap.put("removemember", removemember);
        this.commandMap.put("removeowner", removemember);
        this.commandMap.put("setparent", new CommandRegionSetParent());
        this.commandMap.put("delete", new CommandRegionDelete());
        this.commandMap.put("addmember", addmember);
        this.commandMap.put("addowner", addmember);
        this.commandMap.put("priority", new CommandRegionPriority());

        // commands that DO NOT support console as sender
        this.commandMap.put("define", new CommandRegionDefine());
        this.commandMap.put("claim", new CommandRegionClaim());


        this.aliasMap.put("rd", "define");
        this.aliasMap.put("rc", "claim");
        this.aliasMap.put("rf", "flag");
        this.aliasMap.put("ri", "info");
        this.aliasMap.put("rlist", "list");
        this.aliasMap.put("rp", "priority");
    }

    public boolean handle(CommandSender sender, String senderName, String command, String[] args, WorldGuardConfiguration cfg) throws CommandHandlingException {

        String worldName;
        String subCommand;
        int argsStartAt;

        if (!command.equals("region")) {
            subCommand = this.aliasMap.get(command);
            if (subCommand == null) {
                return false;
            }
            if (sender instanceof Player) {
                worldName = ((Player) sender).getWorld().getName();
                argsStartAt = 0;
            } else {
                CommandHandler.checkArgs(args, 1, -1);
                worldName = args[0];
                argsStartAt = 1;
            }
        } else {
            if (sender instanceof Player) {
                CommandHandler.checkArgs(args, 1, -1);
                worldName = ((Player) sender).getWorld().getName();
                subCommand = args[0].toLowerCase();
                argsStartAt = 1;
            } else {
                CommandHandler.checkArgs(args, 2, -1);
                worldName = args[0];
                subCommand = args[1].toLowerCase();
                argsStartAt = 2;
            }
        }

        System.out.println(subCommand);

        Server server = cfg.getWorldGuardPlugin().getServer();
        if (server.getWorld(worldName) == null) {
            sender.sendMessage("Invalid world specified.");
            return true;
        }

        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(worldName);

        if (!wcfg.useRegions) {
            sender.sendMessage(ChatColor.RED + "Regions are disabled in this world.");
            return true;
        }

        WgRegionCommand wgcmd = commandMap.get(subCommand);
        if (wgcmd == null) {
            return false;
        }

        String[] subArgs;
        if (argsStartAt > 0) {
            subArgs = new String[args.length - argsStartAt];
            System.arraycopy(args, argsStartAt, subArgs, 0, args.length - argsStartAt);
        } else {
            subArgs = args;
        }

        cfg.getWorldGuardPlugin().getGlobalRegionManager().reloadDataWhereRequired();
        wgcmd.handle(sender, senderName, subCommand, subArgs, cfg, wcfg);

        return true;
    }

}
