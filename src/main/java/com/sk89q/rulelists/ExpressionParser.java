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

package com.sk89q.rulelists;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionParser {

    private static final char COLOR_CHAR = '\u00A7';
    private static final Pattern classicColors = Pattern.compile("&[A-Fa-z0-9klmnor]");

    public String format(Context context, String string) {
        Matcher m = classicColors.matcher(string);
        string = m.replaceAll(COLOR_CHAR + "$1");
        return string;
    }

}
