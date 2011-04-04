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

package com.sk89q.worldguard.bukkit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ReportWriter {

    private static final SimpleDateFormat dateFmt =
        new SimpleDateFormat("yyyy-MM-dd kk:mm Z");
    
    private WorldGuardPlugin plugin;
    private Date date = new Date();
    private StringBuilder output = new StringBuilder();
    
    public ReportWriter(WorldGuardPlugin plugin) {
        this.plugin = plugin;
        
        appendHeader(plugin);
        appendServerInformation(plugin.getServer());
        appendPluginInformation(plugin.getServer().getPluginManager().getPlugins());
        appendWorldInformation(plugin.getServer().getWorlds());
        appendGlobalConfiguration(plugin.getGlobalConfiguration());
        appendWorldConfigurations(plugin, plugin.getServer().getWorlds(),
                plugin.getGlobalRegionManager(), plugin.getGlobalConfiguration());
        appendRule();
        appendln("END OF REPORT");
        appendln();
    }
    
    private void appendln(String text) {
        output.append(text);
        output.append("\r\n");
    }
    
    private void appendln(String text, Object ... args) {
        output.append(String.format(text, args));
        output.append("\r\n");
    }
    
    private void appendln() {
        output.append("\r\n");
    }
    
    private void appendRule() {
        output.append("--------------------------------------------------\r\n");
    }
    
    private void appendHeader(WorldGuardPlugin plugin) {
        appendln("WorldGuard Configuration Report");
        appendln("Generated " + dateFmt.format(date));
        appendln();
        appendln("Version: " + plugin.getDescription().getVersion());
        appendln();
    }
    
    private void appendGlobalConfiguration(ConfigurationManager config) {
        appendRule();
        appendln("GLOBAL CONFIGURATION:");
        appendRule();
        appendln();
        
        Class<? extends ConfigurationManager> cls = config.getClass();
        for (Field field : cls.getFields()) {
            try {
                Object val = field.get(config);
                appendln("%-30s: %s", field.getName(), String.valueOf(val));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
            }
        }
        
        appendln();
    }
    
    private void appendServerInformation(Server server) {
        appendRule();
        appendln("SERVER:");
        appendRule();
        appendln();

        appendln("%-15s: %s", "Server ID", server.getServerId());
        appendln("%-15s: %s", "Server name", server.getServerName());
        appendln("%-15s: %s", "Implementation", server.getVersion());
        //appendln("%-15s: %s:%d", "Address", server.getIp(), server.getPort());
        appendln("%-15s: %d/%d", "Player count", 
                server.getOnlinePlayers().length, server.getMaxPlayers());
        
        appendln();
    }
    
    private void appendPluginInformation(Plugin[] plugins) {
        appendRule();
        appendln("PLUGINS (%d)", plugins.length);
        appendRule();
        appendln();
        
        for (Plugin plugin : plugins) {
            appendln("%s %s <%s>:", plugin.getDescription().getName(),
                    plugin.getDescription().getVersion(),
                    plugin.getDescription().getWebsite());
            appendln("    %-15s: %s", "Entry point", plugin.getDescription().getMain());
            appendln("    %-15s: %s", "Data folder", plugin.getDataFolder().getAbsoluteFile());
        }
        
        appendln();
    }
    
    private void appendWorldInformation(List<World> worlds) {
        appendRule();
        appendln("WORLDS (%d)", worlds.size());
        appendRule();
        appendln();
        
        int i = 0;
        for (World world : worlds) {
            int loadedChunkCount = world.getLoadedChunks().length;
            
            appendln("%d. %s:", i, world.getName());
            appendln("    Information:");
            appendln("        %-15s: %s", "ID", world.getId());
            appendln("        %-15s: %s", "Environment", world.getEnvironment().toString());
            appendln("        %-15s: %d", "Player #", world.getPlayers().size());
            appendln("        %-15s: %d", "Entity #", world.getEntities().size());
            appendln("        %-15s: %d", "Loaded chunk #", loadedChunkCount);
            appendln("    Entities:");
            
            Map<Class<? extends Entity>, Integer> entityCounts =
                    new HashMap<Class<? extends Entity>, Integer>();
            
            // Collect entities
            for (Entity entity : world.getEntities()) {
                Class<? extends Entity> cls = entity.getClass();
                
                if (entityCounts.containsKey(cls)) {
                    entityCounts.put(cls, entityCounts.get(cls) + 1);
                } else {
                    entityCounts.put(cls, 1);
                }
            }
            
            // Print entities
            for (Map.Entry<Class<? extends Entity>, Integer> entry
                    : entityCounts.entrySet()) {
                appendln("        %-25s: %d [%f]", entry.getKey().getSimpleName(),
                        entry.getValue(), (float) (entry.getValue() / (double) loadedChunkCount));
            }
            
            i++;
        }
        
        appendln();
    }
    
    private void appendWorldConfigurations(WorldGuardPlugin plugin, List<World> worlds,
            GlobalRegionManager regionMgr, ConfigurationManager mgr) {
        appendRule();
        appendln("WORLD CONFIGURATIONS");
        appendRule();
        appendln();

        int i = 0;
        for (World world : worlds) {
            appendln("%d. %s:", i, world.getName());
            
            WorldConfiguration config = mgr.get(world);
            
            appendln("    Configuration:");
            appendln("        %-30s: %s", "File",
                    (new File(plugin.getDataFolder(), "worlds/"
                            + world.getName() + "/config.yml")).getAbsoluteFile());
            
            Class<? extends WorldConfiguration> cls = config.getClass();
            for (Field field : cls.getFields()) {
                try {
                    Object val = field.get(config);
                    appendln("        %-30s: %s", field.getName(), String.valueOf(val));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                }
            }

            appendln("    Blacklist:");
            appendln("        %-15s: %s", "File",
                    (new File(plugin.getDataFolder(), "worlds/"
                            + world.getName() + "/blacklist.txt")).getAbsoluteFile());
            if (config.getBlacklist() == null) {
                appendln("        DISABLED / NO ENTRIES");
            } else {
                appendln("        %-15s: %s", "Number of items",
                        config.getBlacklist().getItemCount());
                appendln("        %-15s: %s", "Is whitelist",
                        config.getBlacklist().isWhitelist() ? "YES" : "NO");
            }

            appendln("    Region manager:");
            appendln("        %-15s: %s", "File",
                    (new File(plugin.getDataFolder(), "worlds/"
                            + world.getName() + "/regions.yml")).getAbsoluteFile());
            RegionManager worldRegions = regionMgr.get(world);
            appendln("        %-15s: %s", "Type",
                    worldRegions.getClass().getCanonicalName());
            appendln("        %-15s: %s", "Number of regions",
                    worldRegions.getRegions().size());
            appendln("        Global region:");
            ProtectedRegion globalRegion = worldRegions.getRegion("__global__");
            if (globalRegion == null) {
                appendln("            UNDEFINED");
            } else {
                appendln("            %-20s: %s", "Type",
                        globalRegion.getClass().getCanonicalName());
                for (Flag<?> flag : DefaultFlag.getFlags()) {
                    if (flag instanceof StateFlag) {
                        appendln("            %-20s: %s", flag.getName(),
                                globalRegion.getFlag(flag));
                    }
                }
            }
        }
        
        appendln();
    }
    
    public void write(File file) throws IOException {
        FileWriter writer = null;
        BufferedWriter out;
        
        try {
            writer = new FileWriter(file);
            out = new BufferedWriter(writer);
            out.write(output.toString());
            out.close();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
    @Override
    public String toString() {
        return output.toString();
    }
    
}
