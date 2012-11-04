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
 * A context has some shared variables that may be used by a number of different
 * events. Because the context contains common information, criteria and
 * actions do not have to be custom tailored to specific implementation-specific
 * events, and can refer to the the properties set on the context. The place
 * where the information is known as to what is considered the proper valid
 * value of a common property is during the initial handling of
 * the event in the implementation.
 * </p>
 * Implementations need to have custom contexts that can carry more
 * information than offered in this abstract implementation. Criteria and
 * actions can be implement-specific by tailoring themselves to one of these
 * implementation-specific contexts.
 * 
 * @author sk89q
 */
public abstract class Context {
    
    private boolean matches = false;
    private boolean cancelled = false;

    /**
     * Gets whether the event associated with this context has been set to
     * be cancelled.
     * 
     * @return true if the event is set to be cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }
    
    /**
     * Set the event associated with this context to be cancelled, if possible.
     */
    public void cancel() {
        setCancelled(true);
    }

    /**
     * Set the event associated with this context to be cancelled, if possible.
     * 
     * @param cancelled true to cancel
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Set as to whether all the conditions associated with this context have
     * evaluated to being true.
     * 
     * @param matches match state
     */
    void setMatches(boolean matches) {
        this.matches = matches;
    }
    
    /**
     * Returns whether all the conditions associated with this context
     * have been evaluated to true.
     * 
     * @return true if all the conditions have been true
     */
    public boolean matches() {
        return matches;
    }

}
