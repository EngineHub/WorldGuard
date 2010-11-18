// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.net.URL;
import java.io.*;

/**
 * Entry point for the plugin for hey0's mod.
 *
 * @author sk89q
 */
public class WorldGuard extends Plugin {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");
    /**
     * Listener for the plugin system.
     */
    private static final WorldGuardListener listener =
            new WorldGuardListener();
    /**
     * Low priority version.
     */
    private static final WorldGuardListener.LowPriorityListener lowPriorityListener =
            listener.getLowPriorityListener();

    /**
     * Initializes the plugin.
     */
    @Override
    public void initialize() {
        PluginLoader loader = etc.getLoader();

        loader.addListener(PluginLoader.Hook.EXPLODE, listener, this,
                PluginListener.Priority.HIGH);
        loader.addListener(PluginLoader.Hook.IGNITE, listener, this,
                PluginListener.Priority.HIGH);
        loader.addListener(PluginLoader.Hook.FLOW, listener, this,
                PluginListener.Priority.HIGH);
        loader.addListener(PluginLoader.Hook.LOGINCHECK, listener, this,
                PluginListener.Priority.HIGH);
        loader.addListener(PluginLoader.Hook.BLOCK_CREATED, listener, this,
                PluginListener.Priority.HIGH);
        loader.addListener(PluginLoader.Hook.BLOCK_DESTROYED, listener, this,
                PluginListener.Priority.HIGH);
        loader.addListener(PluginLoader.Hook.BLOCK_DESTROYED, lowPriorityListener, this,
                PluginListener.Priority.LOW);
        loader.addListener(PluginLoader.Hook.DISCONNECT, listener, this,
                PluginListener.Priority.HIGH);
    }

    /**
     * Enables the plugin.
     */
    @Override
    public void enable() {
        listener.loadConfiguration();

        logger.log(Level.INFO, "WorldGuard version " + getVersion() + " loaded");
    }

    /**
     * Disables the plugin.
     */
    @Override
    public void disable() {
        BlacklistEntry.forgetAllPlayers();
    }

    /**
     * Get the WorldGuard version.
     *
     * @return
     */
    private String getVersion() {
        try {
            String classContainer = WorldGuard.class.getProtectionDomain()
                    .getCodeSource().getLocation().toString();
            URL manifestUrl = new URL("jar:" + classContainer + "!/META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(manifestUrl.openStream());
            Attributes attrib = manifest.getMainAttributes();
            String ver = (String)attrib.getValue("WorldGuard-Version");
            return ver != null ? ver : "(unavailable)";
        } catch (IOException e) {
            return "(unknown)";
        }
    }
}
