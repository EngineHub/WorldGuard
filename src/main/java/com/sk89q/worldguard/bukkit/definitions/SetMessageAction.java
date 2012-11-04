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
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.bukkit.definitions;

import com.sk89q.rulelists.Action;
import com.sk89q.rulelists.ExpressionParser;
import com.sk89q.worldguard.bukkit.BukkitContext;

public class SetMessageAction implements Action<BukkitContext> {

    private ExpressionParser parser;
    private String message;

    public SetMessageAction(String message) {
        this.message = message;
    }

    public ExpressionParser getParser() {
        return parser;
    }

    public void setParser(ExpressionParser parser) {
        this.parser = parser;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void apply(BukkitContext context) {
        String newMessage = message;

        if (parser != null) {
            newMessage = parser.format(context, newMessage);
        }

        context.setMessage(newMessage);
    }

}