/**
 * 
 */
package com.sk89q.worldguard.bukkit.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import com.sk89q.worldguard.protection.regions.AreaFlags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * @author wallnuss
 *
 */
public class CommandTpRegion extends WgCommand {

    /**
     * @see com.sk89q.worldguard.bukkit.commands.WgCommand#handle(org.bukkit.command.CommandSender, java.lang.String, java.lang.String, java.lang.String[], com.sk89q.worldguard.bukkit.commands.CommandHandler, com.sk89q.worldguard.bukkit.WorldGuardPlugin)
     */
    @Override
    public boolean handle(CommandSender sender, String senderName,
            String command, String[] args, CommandHandler ch,
            WorldGuardPlugin wg) throws CommandHandlingException {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command");
            return true;
        }
        
        Player player = (Player) sender;
        ch.checkPermission(sender, "/tpregion");
        ch.checkArgs(args, 1, 1, "/tpregion <region name>");
        
        String id = args[0];
        
        RegionManager mgr = wg.getGlobalRegionManager().getRegionManager(player.getWorld().getName());
        ProtectedRegion region = mgr.getRegion(id);
        if(region !=null){
            AreaFlags flags = region.getFlags();

            Double x =flags.getDoubleFlag("teleport", "x");
            Double y =flags.getDoubleFlag("teleport", "y");
            Double z =flags.getDoubleFlag("teleport", "z");
            World world=wg.getServer().getWorld(flags.getFlag("teleport", "world"));

            if(x != null && y !=null && z != null &&world !=null){
                Location location = new Location(world, x, y, z);
                player.teleportTo(location);
                return true;
            }else{
                player.sendMessage(ChatColor.RED + "Region: "+id+" has no teleport location assign.");
            }
        }else{
            player.sendMessage(ChatColor.RED + "Region: "+id+" not defined");
        }
        
        return false;
    }

}
