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

package com.sk89q.worldguard.bukkit.cause;

import com.google.common.base.Joiner;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An instance of this object describes the actors that played a role in
 * causing an event, with the ability to describe a situation where one actor
 * controls several other actors to create the event.
 *
 * <p>For example, if a player fires an arrow that hits an item frame, the player
 * is the initiator, while the arrow is merely controlled by the player to
 * hit the item frame.</p>
 */
public class Cause {

    private static final Cause UNKNOWN = new Cause(Collections.emptyList());

    private final List<Object> causes;

    /**
     * Create a new instance.
     *
     * @param causes a list of causes
     */
    private Cause(List<Object> causes) {
        checkNotNull(causes);
        this.causes = causes;
    }

    /**
     * Return whether a cause is known.
     *
     * @return true if known
     */
    public boolean isKnown() {
        return !causes.isEmpty();
    }

    @Nullable
    public Player getPlayerRootCause() {
        for (Object object : causes) {
            if (object instanceof Player) {
                return (Player) object;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return Joiner.on(" | ").join(causes);
    }

    /**
     * Expand an cause object.
     *
     * @param list the list to add elements to
     * @param element an array of objects
     */
    private static void expand(List<Object> list, @Nullable Object ... element) {
        if (element != null) {
            for (Object o : element) {
                if (o == null) {
                    continue;
                }

                if (o instanceof Projectile) {
                    expand(list, ((Projectile) o).getShooter());
                } else {
                    list.add(o);
                }
            }
        }
    }

    /**
     * Create a new instance with the given objects as the cause,
     * where the first-most object is the initial initiator and those
     * following it are controlled by the previous entry.
     *
     * @param cause an array of causing objects
     * @return a cause
     */
    public static Cause create(@Nullable Object ... cause) {
        if (cause != null) {
            List<Object> causes = new ArrayList<Object>(cause.length);
            expand(causes, cause);
            return new Cause(causes);
        } else {
            return UNKNOWN;
        }
    }

    /**
     * Create a new instance that indicates that the cause is not known.
     *
     * @return a cause
     */
    public static Cause unknown() {
        return UNKNOWN;
    }

}
