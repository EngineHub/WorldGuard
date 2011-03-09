/**
 * 
 */
package com.sk89q.worldguard.bukkit.commands;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.GlobalConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.flags.Flags;
import com.sk89q.worldguard.protection.regions.flags.RegionFlag.RegionGroup;

/**
 * @author wallnuss
 *
 */
public class CommandTpRegion extends WgCommand {

    @Override
    public boolean handle(CommandSender sender, String senderName,
            String command, String[] args, GlobalConfiguration cfg, WorldGuardPlugin plugin)
            throws CommandHandlingException {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command");
            return true;
        }

        Player player = (Player) sender;
        plugin.checkPermission(sender, "region.teleport");

        CommandHandler.checkArgs(args, 1, 2, "/tpregion <region name> {spawn}");

        String id = args[0];
        Boolean spawn = false;
        if (args.length == 2 && args[1].equals("spawn")) {
            plugin.checkPermission(sender, "region.teleport.spawn");
            spawn = true;
        }
        RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().getRegionManager(player.getWorld().getName());
        ProtectedRegion region = mgr.getRegion(id);
        if (region != null) {

            RegionGroup flagright;
            if (spawn) {
                flagright = region.getFlags().getRegionGroupFlag(Flags.SPAWN_PERM).getValue(RegionGroup.ALL);
            } else {
                flagright = region.getFlags().getRegionGroupFlag(Flags.TELE_PERM).getValue(RegionGroup.ALL);
            }

            LocalPlayer lPlayer = BukkitPlayer.wrapPlayer(plugin, player);
            if (flagright == RegionGroup.OWNER) {
                if (!region.isOwner(lPlayer)) {
                    plugin.checkPermission(sender, "region.teleport.override");
                }
            } else if (flagright == RegionGroup.MEMBER) {
                if (!region.isMember(lPlayer)) {
                    plugin.checkPermission(sender, "region.teleport.override");
                }
            }

            Location location = null;

            if (spawn) {
                location = region.getFlags().getLocationFlag(Flags.SPAWN_LOC).getValue(cfg.getWorldGuardPlugin().getServer());
            } else {
                location = region.getFlags().getLocationFlag(Flags.TELE_LOC).getValue(cfg.getWorldGuardPlugin().getServer());
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
