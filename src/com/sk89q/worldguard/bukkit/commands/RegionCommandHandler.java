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

    public RegionCommandHandler() {
        this.commandMap = new HashMap<String, WgRegionCommand>();

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

    }

    public boolean handle(CommandSender sender, String senderName, String command, String[] args, WorldGuardConfiguration cfg) throws CommandHandlingException {

        String worldName;
        String subCommand;

        if (sender instanceof Player) {
            CommandHandler.checkArgs(args, 1, -1);
            worldName = ((Player) sender).getWorld().getName();
            subCommand = args[0].toLowerCase();
        } else {
            CommandHandler.checkArgs(args, 2, -1);
            worldName = args[0];
            subCommand = args[1].toLowerCase();
        }

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
        if (sender instanceof Player) {
            subArgs = new String[args.length - 1];
            System.arraycopy(args, 1, subArgs, 0, args.length - 1);
        } else {
            subArgs = new String[args.length - 2];
            System.arraycopy(args, 2, subArgs, 0, args.length - 2);
        }

        wgcmd.handle(sender, senderName, subCommand, subArgs, cfg, wcfg);

        return true;
    }
}
