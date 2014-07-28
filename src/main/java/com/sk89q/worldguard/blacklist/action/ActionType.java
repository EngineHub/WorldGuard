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

package com.sk89q.worldguard.blacklist.action;

import com.sk89q.worldguard.blacklist.Blacklist;
import com.sk89q.worldguard.blacklist.BlacklistEntry;

public enum ActionType {

    ALLOW("allow") {
        @Override
        public Action parseInput(Blacklist blacklist, BlacklistEntry entry) {
            return AllowAction.getInstance();
        }
    },
    DENY("deny") {
        @Override
        public Action parseInput(Blacklist blacklist, BlacklistEntry entry) {
            return DenyAction.getInstance();
        }
    },
    BAN("ban") {
        @Override
        public Action parseInput(Blacklist blacklist, BlacklistEntry entry) {
            return new BanAction(entry);
        }
    },
    KICK("kick") {
        @Override
        public Action parseInput(Blacklist blacklist, BlacklistEntry entry) {
            return new KickAction(entry);
        }
    },
    LOG("log") {
        @Override
        public Action parseInput(Blacklist blacklist, BlacklistEntry entry) {
            return new LogAction(blacklist, entry);
        }
    },
    NOTIFY("notify") {
        @Override
        public Action parseInput(Blacklist blacklist, BlacklistEntry entry) {
            return new NotifyAction(blacklist, entry);
        }
    },
    TELL("tell") {
        @Override
        public Action parseInput(Blacklist blacklist, BlacklistEntry entry) {
            return new TellAction(entry);
        }
    };

    private final String actionName;

    ActionType(String actionName) {
        this.actionName = actionName;
    }

    public abstract Action parseInput(Blacklist blacklist, BlacklistEntry entry);

    public String getActionName() {
        return actionName;
    }
}
