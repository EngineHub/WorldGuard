// $Id$
/*
 * This file is a part of WorldGuard.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY), without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.rulelists;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Contains names of standard attachments.
 */
public final class DefaultAttachments {

    public final static Attachment BLOCK_BREAK = new Attachment("block-break", true);
    public final static Attachment BLOCK_PLACE = new Attachment("block-place", true);
    public final static Attachment BLOCK_SPREAD = new Attachment("block-spread", true);
    public final static Attachment BLOCK_PHYSICS = new Attachment("block-physics", true);
    public final static Attachment BLOCK_INTERACT = new Attachment("block-interact", true);
    public final static Attachment BLOCK_FADE = new Attachment("block-fade", true);
    public final static Attachment BLOCK_FORM = new Attachment("block-form", true);
    public final static Attachment PLAYER_JOIN = new Attachment("player-join", true);
    public final static Attachment PLAYER_RESPAWN = new Attachment("player-respawn", true);
    public final static Attachment PLAYER_QUIT = new Attachment("player-quit", true);
    public final static Attachment ENTITY_EXPLODE = new Attachment("entity-explode", true);
    public final static Attachment ENTITY_DAMAGE = new Attachment("entity-damage", true);
    public final static Attachment ENTITY_DEATH = new Attachment("entity-death", true);
    public final static Attachment ENTITY_IGNITE = new Attachment("entity-ignite", true);
    public final static Attachment ENTITY_SPAWN = new Attachment("entity-spawn", true);
    public final static Attachment ENTITY_STRIKE = new Attachment("entity-strike", true);
    public final static Attachment ENTITY_INTERACT = new Attachment("entity-interact", true);
    public final static Attachment CHAT = new Attachment("chat", true);
    public final static Attachment ITEM_DROP = new Attachment("item-drop", true);
    public final static Attachment ITEM_PICKUP = new Attachment("item-pickup", true);
    public final static Attachment ITEM_USE = new Attachment("item-use", true);
    public final static Attachment WEATHER_PHENOMENON = new Attachment("weather-phenomenon", true);
    public final static Attachment WEATHER_TRANSITION = new Attachment("weather-transition", true);
    public final static Attachment WORLD_LOAD = new Attachment("world-load", true);
    public final static Attachment WORLD_UNLOAD = new Attachment("world-unload", true);
    public final static Attachment WORLD_SAVE = new Attachment("world-save", true);

    private DefaultAttachments() {

    }

    /**
     * Register the attachments in this class with the given manager.
     *
     * @param manager the manager
     */
    public static void registerWith(AttachmentManager manager) {
        for (Field field : DefaultAttachments.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers())
                    && Attachment.class.isAssignableFrom(field.getType())) {
                try {
                    manager.register((Attachment) field.get(null));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Failed to register default attachment", e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to register default attachment", e);
                }
            }
        }
    }

}
