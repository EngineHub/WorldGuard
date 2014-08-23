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

package com.sk89q.worldguard.protection.managers.storage.file;

import com.sk89q.worldguard.protection.managers.storage.RegionDatabase;
import com.sk89q.worldguard.protection.managers.storage.RegionDriver;
import com.sk89q.worldguard.protection.managers.storage.StorageException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stores region data in a {root_dir}/{id}/{filename} pattern on disk
 * using {@link YamlRegionFile}.
 */
public class DirectoryYamlDriver implements RegionDriver {

    private final File rootDir;
    private final String filename;

    /**
     * Create a new instance.
     *
     * @param rootDir the directory where the world folders reside
     * @param filename the filename (i.e. "regions.yml")
     */
    public DirectoryYamlDriver(File rootDir, String filename) {
        checkNotNull(rootDir);
        checkNotNull(filename);
        this.rootDir = rootDir;
        this.filename = filename;
    }

    /**
     * Get the path for the given ID.
     *
     * @param id the ID
     * @return the file path
     */
    private File getPath(String id) {
        checkNotNull(id);

        File f = new File(rootDir, id + File.separator + filename);
        try {
            f.getCanonicalPath();
            return f;
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid file path for the world's regions file");
        }
    }

    @Override
    public RegionDatabase get(String id) {
        checkNotNull(id);

        File file = getPath(id);

        return new YamlRegionFile(id, file);
    }

    @Override
    public List<RegionDatabase> getAll() throws StorageException {
        List<RegionDatabase> stores = new ArrayList<RegionDatabase>();

        File files[] = rootDir.listFiles();
        if (files != null) {
            for (File dir : files) {
                if (dir.isDirectory() && new File(dir, "regions.yml").isFile()) {
                    stores.add(new YamlRegionFile(dir.getName(), getPath(dir.getName())));
                }
            }
        }

        return stores;
    }

}
