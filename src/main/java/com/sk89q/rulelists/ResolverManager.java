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

public class ResolverManager {

    private final Map<Class<? extends Resolver>, Map<String, Resolver>> resolvers
            = new HashMap<Class<? extends Resolver>, Map<String, Resolver>>();
    private final Map<Resolver, String> reverse = new HashMap<Resolver, String>();

    public <T extends Resolver> void register(Class<T> clazz, String id, T resolver) {
        Map<String, Resolver> map = resolvers.get(clazz);
        if (map == null) {
            map = new HashMap<String, Resolver>();
            resolvers.put(clazz, map);
        }

        reverse.put(resolver, id);

        map.put(id.toLowerCase(), resolver);
    }

    public String getId(Object object) {
        return reverse.get(object);
    }

    @SuppressWarnings("unchecked")
    public <T extends Resolver> T get(Class<T> clazz, String id) throws DefinitionException {
        Map<String, Resolver> map = resolvers.get(clazz);
        if (map == null) {
            throw new DefinitionException("Don't know how to resolve "
                    + clazz.getCanonicalName());
        }

        Resolver resolver = map.get(id);
        if (resolver == null) {
            throw new DefinitionException("Don't know how to resolve "
                    + clazz.getCanonicalName());
        }

        return (T) resolver;
    }

}
