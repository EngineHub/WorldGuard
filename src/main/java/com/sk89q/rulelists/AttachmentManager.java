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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sk89q.rebar.config.Loader;
import com.sk89q.rebar.config.LoaderBuilderException;

/**
 * A database of valid attachments.
 */
public final class AttachmentManager implements Loader<Attachment> {

    private Set<Attachment> attachments = new HashSet<Attachment>();
    private Map<String, Attachment> aliasMap = new HashMap<String, Attachment>();

    public AttachmentManager() {
    }

    /**
     * Register an attachment.
     *
     * @param attachment the attachment
     */
    public synchronized void register(Attachment attachment) {
        if (attachments.contains(attachment)
                || aliasMap.containsKey(attachment.getAlias().toLowerCase())) {
            throw new IllegalArgumentException("Attachment '" + attachment + "' already registered.");
        }

        attachments.add(attachment);
        aliasMap.put(attachment.getAlias().toLowerCase(), attachment);
    }

    /**
     * Get an attachment by its alias.
     *
     * @param alias alias, case in-sensitive
     * @return an attachment or null
     */
    public synchronized Attachment lookup(String alias) {
        return aliasMap.get(alias.toLowerCase());
    }

    @Override
    public Attachment read(Object value) throws LoaderBuilderException {
        String id = String.valueOf(value);

        Attachment attachment = lookup(id);
        if (attachment == null) {
            throw new LoaderBuilderException("Unknown RuleList attachment: " + id);
        }

        return attachment;
    }

}
