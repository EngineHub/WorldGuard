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

package com.sk89q.worldguard.bukkit.listener.debounce.legacy;

import com.sk89q.worldguard.bukkit.listener.debounce.legacy.EntityEntityEventDebounce.Key;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class EntityEntityEventDebounce extends AbstractEventDebounce<Key> {

    public EntityEntityEventDebounce(int debounceTime) {
        super(debounceTime);
    }

    public <T extends Event & Cancellable> void debounce(Entity source, Entity target, Cancellable originalEvent, T firedEvent) {
        super.debounce(new Key(source, target), originalEvent, firedEvent);
    }

    protected static class Key {
        private final Entity source;
        private final Entity target;

        public Key(Entity source, Entity target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (!source.equals(key.source)) return false;
            if (!target.equals(key.target)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = source.hashCode();
            result = 31 * result + target.hashCode();
            return result;
        }
    }

}
