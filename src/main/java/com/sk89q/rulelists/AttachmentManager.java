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
import java.util.Map;

/**
 * Manages known attachment points.
 *
 * @see Attachment
 */
public class AttachmentManager {

    private final Map<String, Attachment> attachments = new HashMap<String, Attachment>();

    /**
     * Register an {@link Attachment}.
     *
     * @param name name of the attachment (used in a rule's 'when' clause) (case insensitive)
     * @param attachment attachment
     */
    public void register(String name, Attachment attachment) {
        attachments.put(name.toLowerCase(), attachment);
    }

    /**
     * Get an attachment.
     *
     * @param name name of the attachment (case insensitive)
     * @return attachment
     */
    public Attachment get(String name) {
        return attachments.get(name.toLowerCase());
    }

    /**
     * Forget all the rules in the registered attachments.
     */
    public void forgetRules() {
        for (Attachment attachment : attachments.values()) {
            attachment.forgetRules();
        }
    }

}
