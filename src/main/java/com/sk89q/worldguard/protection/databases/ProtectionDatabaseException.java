// $Id$
/*
 * WorldGuard ProtectionDatabaseException
 * Copyright (C) 2011 Nicholas Steicke <http://narthollis.net>
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

package com.sk89q.worldguard.protection.databases;

import java.lang.Exception;

public class ProtectionDatabaseException extends Exception {
    private static final long serialVersionUID = 1L;

    public ProtectionDatabaseException() {
        super();
    }

    public ProtectionDatabaseException(String message) {
        super(message);
    }

    public ProtectionDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtectionDatabaseException(Throwable cause) {
        super(cause);
    }
}
