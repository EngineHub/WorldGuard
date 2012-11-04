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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains names of standard attachments.
 */
public enum KnownAttachment {

    BLOCK_BREAK("block-break"),
    BLOCK_PLACE("block-place"),
    BLOCK_SPREAD("block-spread"),
    BLOCK_PHYSICS("block-physics"),
    BLOCK_INTERACT("block-interact"),
    BLOCK_FADE("block-fade"),
    BLOCK_FORM("block-form"),
    PLAYER_SPAWN("player-spawn"),
    PLAYER_RESPAWN("player-respawn"),
    PLAYER_QUIT("player-quit"),
    ENTITY_EXPLODE("entity-explode"),
    ENTITY_DAMAGE("entity-damage"),
    ENTITY_DEATH("entity-death"),
    ENTITY_IGNITE("entity-ignite"),
    ENTITY_SPAWN("entity-spawn"),
    ENTITY_STRIKE("entity-strike"),
    ENTITY_INTERACT("entity-interact"),
    ITEM_DROP("item-drop"),
    ITEM_PICKUP("item-pickup"),
    ITEM_USE("item-use"),
    WEATHER_PHENOMENON("weather-phenomenon"),
    WEATHER_TRANSITION("weather-transition"),
    WORLD_LOAD("world-load"),
    WORLD_UNLOAD("world-unload"),
    WORLD_SAVE("world-save");

    private final static Map<String, KnownAttachment> ids = new HashMap<String, KnownAttachment>();

    static {
        for (KnownAttachment attachment : EnumSet.allOf(KnownAttachment.class)) {
            ids.put(attachment.getId().toLowerCase(), attachment);
        }
    }

    private final String id;

    KnownAttachment(String id) {
        this.id = id;
    }

    /**
     * Get the attachment's string ID.
     *
     * @return string ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get an attachment from a given string ID. Not case sensitive.
     *
     * @param id string ID
     * @return attachment name
     */
    public static KnownAttachment fromId(String id) {
        return ids.get(id.toLowerCase());
    }

}
