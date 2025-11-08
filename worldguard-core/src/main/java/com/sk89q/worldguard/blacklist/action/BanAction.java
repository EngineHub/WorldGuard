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

import com.sk89q.worldguard.blacklist.BlacklistEntry;
import com.sk89q.worldguard.blacklist.event.BlacklistEvent;
import com.sk89q.worldguard.WorldGuard;

import static com.google.common.base.Preconditions.checkNotNull;

public class BanAction implements Action {

    private final BlacklistEntry entry;

    public BanAction(BlacklistEntry entry) {
        checkNotNull(entry);
        this.entry = entry;
    }

    @Override
    public ActionResult apply(BlacklistEvent event, boolean silent, boolean repeating, boolean forceRepeat) {
        if (silent) {
            return ActionResult.INHERIT;
        }

        if (event.getPlayer() != null) {
            String message = entry.getMessage();

            if (message != null) {
                String formatted = String.format(message, event.getTarget().getFriendlyName());
                event.getPlayer().ban(message("blacklist.action.ban.reason", formatted));
            } else {
                String detail = message("blacklist.action.common.cannot", event.getDescription(), event.getTarget().getFriendlyName());
                event.getPlayer().ban(message("blacklist.action.ban.reason", detail));
            }
        }

        return ActionResult.INHERIT;
    }

    private String message(String key, Object... arguments) {
        return WorldGuard.getInstance().getLocalization().format(key, arguments);
    }

}
