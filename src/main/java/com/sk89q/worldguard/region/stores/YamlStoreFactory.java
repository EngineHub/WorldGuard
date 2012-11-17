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
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.region.stores;

import java.io.File;
import java.io.IOException;
import java.util.Formatter;

import org.apache.commons.lang.Validate;

/**
 * Creates {@link YamlStore}s.
 *
 * @see YamlStore
 */
public class YamlStoreFactory implements RegionStoreFactory {

    private File baseDir;
    private String pathFormat;

    public YamlStoreFactory(File baseDir, String pathFormat) {
        setBaseDir(baseDir);
        setPathFormat(pathFormat);
    }

    /**
     * Get the base directory to store the region files.
     *
     * @return the base directory
     */
    public File getBaseDir() {
        return baseDir;
    }

    /**
     * Set the base directory store the region files.
     *
     * @param baseDir base directory
     */
    public void setBaseDir(File baseDir) {
        Validate.notNull(baseDir, "Base directory parameter cannot be null");
        this.baseDir = baseDir;
    }

    /**
     * Get the path format for region files.
     *
     * @return path format
     * @see #setPathFormat(String) for an explanation of the format
     */
    public String getPathFormat() {
        return pathFormat;
    }

    /**
     * Set the path format for region files.
     * <p>
     * The path format is a string where %s will be replaced with the region
     * set ID. For example, "worlds/%s/regions.yml" would be a valid format.
     * {@link Formatter} is used to format the format and % does have to be escaped.
     *
     * @param pathFormat the format
     * @see Formatter
     */
    public void setPathFormat(String pathFormat) {
        Validate.notNull(pathFormat, "Path format cannot be null");
        this.pathFormat = pathFormat;
    }

    /**
     * Get the file that is used to store the data for the given ID.
     *
     * @param id the ID
     * @return the file object
     */
    public File getFile(String id) {
        Validate.notNull(id, "ID cannot be null");
        return new File(baseDir, String.format(pathFormat, id));
    }

    @Override
    public RegionStore getStore(String id) {
        Validate.notNull(id, "ID cannot be null");
        return new YamlStore(getFile(id));
    }

    @Override
    public void close() throws IOException {
        // Nothing to do
    }

}
