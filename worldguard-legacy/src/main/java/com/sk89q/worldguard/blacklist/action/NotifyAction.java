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
import com.sk89q.worldguard.blacklist.event.BlacklistEvent;

import static com.google.common.base.Preconditions.checkNotNull;

public class NotifyAction extends RepeatGuardedAction {

    private final Blacklist blacklist;
    private final BlacklistEntry entry;

    public NotifyAction(Blacklist blacklist, BlacklistEntry entry) {
        checkNotNull(blacklist);
        checkNotNull(entry);
        this.blacklist = blacklist;
        this.entry = entry;
    }

    @Override
    protected ActionResult applyNonRepeated(BlacklistEvent event, boolean silent) {
        if (silent) {
            return ActionResult.INHERIT;
        }

        blacklist.notify(event, entry.getComment());

        return ActionResult.INHERIT;
    }

}
