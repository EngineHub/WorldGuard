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
import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Michael
 */
public class RegionCommandHandler extends WgCommand {

    private Map<String, WgCommand> commandMap;

    public RegionCommandHandler()
    {
        this.commandMap = new HashMap<String, WgCommand>();

        this.commandMap.put("addmember", new CommandRegionAddMember());
        this.commandMap.put("addowner", new CommandRegionAddMember());
        this.commandMap.put("claim", new CommandRegionClaim());
        this.commandMap.put("define", new CommandRegionDefine());
        this.commandMap.put("delete", new CommandRegionDelete());
        this.commandMap.put("flag", new CommandRegionFlag());
        this.commandMap.put("info", new CommandRegionInfo());
        this.commandMap.put("list", new CommandRegionList());
        this.commandMap.put("load", new CommandRegionLoad());
        this.commandMap.put("removemember", new CommandRegionRemoveMember());
        this.commandMap.put("removeowner", new CommandRegionRemoveMember());
        this.commandMap.put("save", new CommandRegionSave());
        this.commandMap.put("setparent", new CommandRegionSetParent());
    }

    public boolean handle(CommandSender sender, String senderName, String command, String[] args, CommandHandler ch, WorldGuardPlugin wg) throws CommandHandlingException {

        if (!wg.useRegions) {
            sender.sendMessage(ChatColor.RED + "Regions are disabled.");
            return true;
        }

        ch.checkArgs(args, 1, -1);           

        String subCommand = args[0].toLowerCase();

        WgCommand wgcmd = commandMap.get(subCommand);
        if (wgcmd == null) {
            return false;
        }

        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, args.length - 1);

        wgcmd.handle(sender, senderName, subCommand, subArgs, ch, wg);

        return true;
    }
}
