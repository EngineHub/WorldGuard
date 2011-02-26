/**
 * 
 */
package com.sk89q.worldguard.bukkit.commands;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * @author wallnuss
 *
 */
public class CommandTpRegion extends WgCommand {

    public boolean handle(CommandSender sender, String senderName, String command, String[] args, WorldGuardConfiguration cfg) throws CommandHandlingException {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command");
            return true;
        }

        Player player = (Player) sender;
        cfg.checkPermission(sender, "tpregion");

        CommandHandler.checkArgs(args, 1, 2, "/tpregion <region name> {spawn}");

        String id = args[0];
        Boolean spawn = false;
        if (args.length == 2 && args[1].equals("spawn")) {
            cfg.checkPermission(player, "tpregion.spawn");
            spawn = true;
        }
        RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().getRegionManager(player.getWorld().getName());
        ProtectedRegion region = mgr.getRegion(id);
        if (region != null) {

            String flagright = "all";
            if (spawn) {
                flagright = region.getFlags().getFlag("spawn", "settings");
            } else {
                flagright = region.getFlags().getFlag("teleport", "settings");
            }

            LocalPlayer lPlayer = BukkitPlayer.wrapPlayer(cfg, player);
            if (flagright.equals("owner")) {
                if (!region.isOwner(lPlayer)) {
                    cfg.checkPermission(player, "tpregion.override");
                }
            } else if (flagright.equals("meber")) {
                if (!region.isMember(lPlayer)) {
                    cfg.checkPermission(player, "tpregion.override");
                }
            }

            Location location = null;

            if (spawn) {
                location = region.getFlags().getLocationFlag(cfg.getWorldGuardPlugin().getServer(), "spawn");
            } else {
                location = region.getFlags().getLocationFlag(cfg.getWorldGuardPlugin().getServer(), "teleport");
            }
            if (location != null) {
                player.teleportTo(location);
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "Region: " + id + " has no teleport/spawn location assign.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Region: " + id + " not defined");
        }

        return false;
    }
}
