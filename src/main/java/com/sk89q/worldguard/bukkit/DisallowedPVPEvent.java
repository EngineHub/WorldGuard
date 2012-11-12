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

package com.sk89q.worldguard.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * This event is fired when PVP is disallowed between players due to a "pvp deny" flag.
 * Cancelling this event allows the PVP in spite of this.
 * 
 * @author Score_Under
 */
public class DisallowedPVPEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled = false;
    private final Player attacker;
    private final Player defender;
    private final EntityDamageByEntityEvent event;

    public DisallowedPVPEvent(final Player attacker, final Player defender, EntityDamageByEntityEvent event) {
        this.attacker = attacker;
        this.defender = defender;
        this.event = event;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * @return the attacking player.
     */
    public Player getAttacker() {
        return attacker;
    }

    /**
     * @return the defending player.
     */
    public Player getDefender() {
        return defender;
    }

    public EntityDamageByEntityEvent getCause() {
        return event;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
