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

package com.sk89q.worldguard.util.logging;

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.component.SubtleFormat;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Sends all logger messages to a player.
 */
public class LoggerToChatHandler extends Handler {
    private final Formatter formatter = new Formatter() {
        @Override
        public String format(LogRecord record) {
            return formatMessage(record);
        }
    };

    /**
     * Player.
     */
    private Actor player;

    /**
     * Construct the object.
     *
     * @param player
     */
    public LoggerToChatHandler(Actor player) {
        this.player = player;
    }

    /**
     * Close the handler.
     */
    @Override
    public void close() {
    }

    /**
     * Flush the output.
     */
    @Override
    public void flush() {
    }

    /**
     * Publish a log record.
     */
    @Override
    public void publish(LogRecord record) {
        player.print(SubtleFormat.wrap(record.getLevel().getName() + ": ").append(TextComponent.of(formatter.format(record), TextColor.WHITE)));
    }
}
