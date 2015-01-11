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

package com.sk89q.worldguard.protection.flags;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Stores a bi-state value.
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

    @Override
    public State getDefault() {
        return def ? State.ALLOW : null;
    }

    @Override
    public boolean hasConflictStrategy() {
        return true;
    }

    @Override
    @Nullable
    public State chooseValue(Collection<State> values) {
        if (!values.isEmpty()) {
            return combine(values);
        } else {
            return null;
        }
    }

    /**
     * Whether setting this flag to {@link State#ALLOW} is prevented on
     * the global region.
     *
     * <p>This value is only changed, at least in WorldGuard, for the
     * {@link DefaultFlag#BUILD} flag.</p>
     *
     * @return Whether {@code ALLOW} is prevented
     */
    public boolean preventsAllowOnGlobal() {
        return false;
    }

    @Override
    public State parseInput(WorldGuardPlugin plugin, CommandSender sender, String input) throws InvalidFlagFormat {
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

    /**
     * Test whether at least one of the given states is {@code ALLOW}
     * but none are set to {@code DENY}.
     *
     * @param states zero or more states
     * @return true if the condition is matched
     */
    public static boolean test(State... states) {
        boolean allowed = false;

        for (State state : states) {
            if (state == State.DENY) {
                return false;
            } else if (state == State.ALLOW) {
                allowed = true;
            }
        }

        return allowed;
    }

    /**
     * Combine states, letting {@code DENY} override {@code ALLOW} and
     * {@code ALLOW} override {@code NONE} (or null).
     *
     * @param states zero or more states
     * @return the new state
     */
    @Nullable
    public static State combine(State... states) {
        boolean allowed = false;

        for (State state : states) {
            if (state == State.DENY) {
                return State.DENY;
            } else if (state == State.ALLOW) {
                allowed = true;
            }
        }

        return allowed ? State.ALLOW : null;
    }

    /**
     * Combine states, letting {@code DENY} override {@code ALLOW} and
     * {@code ALLOW} override {@code NONE} (or null).
     *
     * @param states zero or more states
     * @return the new state
     */
    @Nullable
    public static State combine(Collection<State> states) {
        boolean allowed = false;

        for (State state : states) {
            if (state == State.DENY) {
                return State.DENY;
            } else if (state == State.ALLOW) {
                allowed = true;
            }
        }

        return allowed ? State.ALLOW : null;
    }

    /**
     * Turn a boolean into either {@code NONE} (null) or {@code ALLOW} if
     * the boolean is false or true, respectively.
     *
     * @param flag a boolean value
     * @return a state
     */
    @Nullable
    public static State allowOrNone(boolean flag) {
        return flag ? State.ALLOW : null;
    }

    /**
     * Turn {@code DENY} into {@code NONE} (null).
     *
     * @param state a state
     * @return a state
     */
    @Nullable
    public static State denyToNone(State state) {
        return state == State.DENY ? null : state;
    }

}
