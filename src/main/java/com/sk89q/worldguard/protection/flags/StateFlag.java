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
public class StateFlag extends Flag<StateFlag.State> {

    public enum State {
        ALLOW,
        DENY
    }

    private boolean def;

    public StateFlag(String name, boolean def, RegionGroup defaultGroup) {
        super(name, defaultGroup);
        this.def = def;
    }

    public StateFlag(String name, boolean def) {
        super(name);
        this.def = def;
    }

    public boolean getDefault() {
        return def;
    }

    @Override
    public State parseInput(WorldGuardPlugin plugin, CommandSender sender,
            String input) throws InvalidFlagFormat {
        input = input.trim();

        if (input.equalsIgnoreCase("allow")) {
            return State.ALLOW;
        } else if (input.equalsIgnoreCase("deny")) {
            return State.DENY;
        } else if (input.equalsIgnoreCase("none")) {
            return null;
        } else {
            throw new InvalidFlagFormat("Expected none/allow/deny but got '" + input + "'");
        }
    }

    @Override
    public State unmarshal(Object o) {
        String str = o.toString();
        if (str.equalsIgnoreCase("allow")) {
            return State.ALLOW;
        } else if (str.equalsIgnoreCase("deny")) {
            return State.DENY;
        } else {
            return null;
        }
    }

    @Override
    public Object marshal(State o) {
        if (o == State.ALLOW) {
            return "allow";
        } else if (o == State.DENY) {
            return "deny";
        } else {
            return null;
        }
    }

}
