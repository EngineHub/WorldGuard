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

/**
 * An attachment is analogous to an event.
 */
public final class Attachment {

    private static int NEXT_ID = 0;

    private final int id;
    private final String alias;

    /**
     * Create a new attachment with the given alias. The alias is used in rule list
     * files.
     *
     * @param alias alias name
     */
    public Attachment(String alias) {
        this.alias = alias;

        // No internal ID
        id = -1;
    }

    /**
     * Create a new attachment with the given alias. The alias is used in rule list
     * files.
     *
     * @param alias alias name
     * @param isInternal true to assign this attachment a standard internal ID
     */
    Attachment(String alias, boolean isInternal) {
        this.alias = alias;

        id = isInternal ? NEXT_ID++ : -1;
    }

    /**
     * Get the internal ID.
     *
     * @return ID number, possibly -1 for no internal ID
     */
    int getId() {
        return id;
    }

    /**
     * Get the alias used inside rule list files.
     *
     * @return alias
     */
    public String getAlias() {
        return alias;
    }

}
