package com.sk89q.worldguard.bukkit;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.FlagContextCreateEvent;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.session.BukkitSessionManager;
import com.sk89q.worldguard.session.SessionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import java.util.Collection;
import java.util.Set;

public class BukkitWorldGuardPlatform implements WorldGuardPlatform {

    private SessionManager sessionManager;
    private final BukkitConfigurationManager configuration;
    private final BukkitRegionContainer regionContainer;

    public BukkitWorldGuardPlatform() {
        sessionManager = new BukkitSessionManager(WorldGuardPlugin.inst());
        configuration = new BukkitConfigurationManager(WorldGuardPlugin.inst());
        regionContainer = new BukkitRegionContainer(WorldGuardPlugin.inst());
        regionContainer.initialize();
    }

    @Override
    public void notifyFlagContextCreate(FlagContext.FlagContextBuilder flagContextBuilder) {
        Bukkit.getServer().getPluginManager().callEvent(new FlagContextCreateEvent(flagContextBuilder));
    }

    @Override
    public BukkitConfigurationManager getGlobalStateManager() {
        return configuration;
    }

    @Override
    public World getWorldByName(String worldName) {
        return BukkitAdapter.adapt(Bukkit.getServer().getWorld(worldName));
    }

    @Override
    public String replaceColorMacros(String string) {
        return BukkitUtil.replaceColorMacros(string);
    }

    public String replaceMacros(Actor sender, String message) {
        Collection<? extends Player> online = Bukkit.getServer().getOnlinePlayers();

        message = message.replace("%name%", sender.getName());
        message = message.replace("%id%", sender.getUniqueId().toString());
        message = message.replace("%online%", String.valueOf(online.size()));

        if (sender instanceof LocalPlayer) {
            LocalPlayer player = (LocalPlayer) sender;
            World world = (World) player.getExtent();

            message = message.replace("%world%", world.getName());
            message = message.replace("%health%", String.valueOf(player.getHealth()));
        }

        return message;
    }

    @Override
    public SessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    public void broadcastNotification(String message) {
        Bukkit.broadcast(message, "worldguard.notify");
        Set<Permissible> subs = Bukkit.getPluginManager().getPermissionSubscriptions("worldguard.notify");
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (!(subs.contains(player) && player.hasPermission("worldguard.notify")) &&
                    WorldGuardPlugin.inst().hasPermission(player, "worldguard.notify")) { // Make sure the player wasn't already broadcasted to.
                player.sendMessage(message);
            }
        }
        WorldGuard.logger.info(message);
    }

    @Override
    public void unload() {
        configuration.unload();
        regionContainer.unload();
    }

    @Override
    public RegionContainer getRegionContainer() {
        return this.regionContainer;
    }

    @Override
    public GameMode getDefaultGameMode() {
        return GameModes.get(Bukkit.getServer().getDefaultGameMode().name().toLowerCase());
    }
}
