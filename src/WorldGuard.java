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
import java.util.List;
import java.util.ArrayList;
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
    private WorldGuardListener listener;

    /**
     * Initialize the plugin.
     */
    public WorldGuard() {
        listener = new WorldGuardListener(this);
    }

    /**
     * Initializes the plugin.
     */
    @Override
    public void initialize() {
        PluginLoader loader = etc.getLoader();

        List<String> missingFeatures = new ArrayList<String>();

        registerHook("COMMAND", PluginListener.Priority.MEDIUM);
        registerHook("SERVERCOMMAND", PluginListener.Priority.MEDIUM);
        if (!registerHook("EXPLODE", PluginListener.Priority.HIGH)) {
            missingFeatures.add("disabling TNT or creeper explosions");
        }
        if (!registerHook("IGNITE", PluginListener.Priority.HIGH)) {
            missingFeatures.add("disabling fire or lava fire");
        }
        if (!registerHook("FLOW", PluginListener.Priority.HIGH)) {
            missingFeatures.add("controlling lava flow or sponges");
        }
        registerHook("LOGINCHECK", PluginListener.Priority.HIGH);
        registerHook("LOGIN", PluginListener.Priority.MEDIUM);
        registerHook("BLOCK_CREATED", PluginListener.Priority.HIGH);
        registerHook("BLOCK_DESTROYED", PluginListener.Priority.CRITICAL);
        registerHook("BLOCK_BROKEN", PluginListener.Priority.HIGH);
        registerHook("DISCONNECT", PluginListener.Priority.HIGH);
        registerHook("ITEM_DROP", PluginListener.Priority.HIGH);
        if (!registerHook("ITEM_PICK_UP", PluginListener.Priority.HIGH)) {
            missingFeatures.add("denying item pickups");
        }
        registerHook("COMPLEX_BLOCK_CHANGE", PluginListener.Priority.HIGH);
        registerHook("COMPLEX_BLOCK_SEND", PluginListener.Priority.HIGH);
        registerHook("INVENTORY_CHANGE", PluginListener.Priority.HIGH);
        if (!registerHook("BLOCK_PHYSICS", PluginListener.Priority.MEDIUM)) {
            missingFeatures.add("controlling physics on gravel, sand, or portal blocks");
        }

        if (missingFeatures.size() > 0) {
            logger.log(Level.WARNING, "WorldGuard: Your version of hMod does not support "
                    + joinString(missingFeatures, ", ", 0) + ".");
        }
    }

    /**
     * Conditionally registers a hook.
     *
     * @param name
     * @param priority
     * @return where the hook was registered correctly
     */
    public boolean registerHook(String name, PluginListener.Priority priority) {
        try {
            PluginLoader.Hook hook = PluginLoader.Hook.valueOf(name);
            etc.getLoader().addListener(hook, listener, this, priority);
            return true;
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "WorldGuard: Missing hook " + name + "!");
            return false;
        }
    }

    /**
     * Enables the plugin.
     */
    @Override
    public void enable() {
        logger.log(Level.INFO, "WorldGuard version " + getVersion() + " loaded");
        listener.loadConfiguration();

        etc.getInstance().addCommand("/stopfire", "Globally stop fire spread");
        etc.getInstance().addCommand("/allowfire", "Globally re-enable fire spread");
    }

    /**
     * Disables the plugin.
     */
    @Override
    public void disable() {
        try {
            listener.disable();
            BlacklistEntry.forgetAllPlayers();
        } catch (Throwable t) {
        }

        etc.getInstance().removeCommand("/stopfire");
        etc.getInstance().removeCommand("/allowfire");
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

    /**
     * Joins a string from an array of strings.
     *
     * @param str
     * @param delimiter
     * @return
     */
    private static String joinString(List<String> str, String delimiter,
            int initialIndex) {
        if (str.size() == 0) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(str.get(0));
        for (int i = initialIndex + 1; i < str.size(); i++) {
            buffer.append(delimiter).append(str.get(i));
        }
        return buffer.toString();
    }
}
