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
package com.sk89q.worldguard.protection.flags;

import org.bukkit.command.CommandSender;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 *
 * @author sk89q
 */
public class RegionGroupFlag extends Flag<RegionGroupFlag.RegionGroup> {
    
    public static enum RegionGroup {
        MEMBERS,
        OWNERS
    }
    
    public RegionGroupFlag(String name, char legacyCode) {
        super(name, legacyCode);
    }

    public RegionGroupFlag(String name) {
        super(name);
    }

    @Override
    public RegionGroup parseInput(WorldGuardPlugin plugin, CommandSender sender,
            String input) throws InvalidFlagFormat {
        input = input.trim();
        
        if (input.equalsIgnoreCase("members") || input.equalsIgnoreCase("member")) {
            return RegionGroup.MEMBERS;
        } else if (input.equalsIgnoreCase("owners") || input.equalsIgnoreCase("owner")) {
            return RegionGroup.OWNERS;
        } else if (input.equalsIgnoreCase("everyone") || input.equalsIgnoreCase("anyone")) {
            return null;
        } else {
            throw new InvalidFlagFormat("Not none/allow/deny: " + input);
        }
    }

    @Override
    public RegionGroup unmarshal(Object o) {
        String str = o.toString();
        if (str.equalsIgnoreCase("members")) {
            return RegionGroup.MEMBERS;
        } else if (str.equalsIgnoreCase("owners")) {
            return RegionGroup.OWNERS;
        } else {
            return null;
        }
    }

    @Override
    public Object marshal(RegionGroup o) {
        if (o == RegionGroup.MEMBERS) {
            return "members";
        } else if (o == RegionGroup.OWNERS) {
            return "owners";
        } else {
            return null;
        }
    }
    
}
