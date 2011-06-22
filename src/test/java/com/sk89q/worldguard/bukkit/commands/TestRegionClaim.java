package com.sk89q.worldguard.bukkit.commands;

// JUnit and Mockito
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

// Imports for mocking
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

// Exceptions
import com.sk89q.minecraft.util.commands.CommandException;
import java.io.IOException;

public class TestRegionClaim {
    Player sender;
    LocalPlayer localPlayer;
    WorldGuardPlugin plugin;
    CommandContext args;
    World world;
    RegionManager mgr;
    ProtectedRegion region;
    WorldConfiguration wcfg;
    GlobalRegionManager grm;
    ConfigurationManager cm;
    Player player;
    DefaultDomain owners;
    Set<String> setOwners;
    WorldEditPlugin worldEdit;
    Selection sel;
    ApplicableRegionSet regions;

    @Before
    public void setUp() throws Exception {
        // Mock objects
        sender = mock(Player.class);
        localPlayer = mock(LocalPlayer.class);
        plugin = mock(WorldGuardPlugin.class);
        args = mock(CommandContext.class);
        world = mock(World.class);
        mgr = mock(RegionManager.class);
        region = mock(ProtectedRegion.class);
        wcfg = mock(WorldConfiguration.class);
        grm = mock(GlobalRegionManager.class);
        cm = mock(ConfigurationManager.class);
        player = mock(Player.class);
        owners = mock(DefaultDomain.class);
        worldEdit = mock(WorldEditPlugin.class);
        sel = mock(Selection.class);
        regions = mock(ApplicableRegionSet.class);
        
        setOwners = new HashSet<String>();
        setOwners.add("Shaniqua");
        setOwners.add("Taniqua");
        setOwners.add("Shanaynay");
        
        // Set up function stubbing
        when(plugin.checkPlayer(sender)).thenReturn(player);
        when(plugin.wrapPlayer(player)).thenReturn(localPlayer);
        when(sender.getWorld()).thenReturn(world);
        when(args.argsLength()).thenReturn(1);
        when(args.getString(0)).thenReturn("test_region");
        when(plugin.getGlobalRegionManager()).thenReturn(grm);
        when(grm.get(world)).thenReturn(mgr);
        when(mgr.getRegion("test_region")).thenReturn(region);
        when(plugin.getGlobalStateManager()).thenReturn(cm);
        when(player.getWorld()).thenReturn(world);
        when(cm.get(world)).thenReturn(wcfg);
        wcfg.maxRegionCountPerPlayer = 1;
        when(mgr.getRegionCountOfPlayer(localPlayer)).thenReturn(0);
        when(region.getOwners()).thenReturn(owners);
        when(owners.getPlayers()).thenReturn(setOwners);
        when(player.getName()).thenReturn("Shanaynay");
        when(plugin.getWorldEdit()).thenReturn(worldEdit);
        when(worldEdit.getSelection(player)).thenReturn(sel);
        when(worldEdit.getSelection(player)).thenReturn(null);
        when(mgr.getApplicableRegions(region)).thenReturn(regions);
        when(regions.size()).thenReturn(5000);// Inside a town
        wcfg.claimOnlyInsideExistingRegions = true;
        when(region.volume()).thenReturn(5000);
        wcfg.maxClaimVolume = 10000;
    }

    // Incorrect parameter syntax w/ 0 parameters
    @Test
    public void testNoParameters() {
        try {
            when(args.argsLength()).thenReturn(0);
            RegionCommands.claim(args, plugin, sender);
            fail();
        } catch(CommandException ex) {
            assertEquals(ex.getMessage(), "Incorrect parameter syntax!");
        }
    }
    
    // Incorrect parameter syntax w/ 2 parameters
    @Test
    public void testTooManyParameters() {
        try {
            when(args.argsLength()).thenReturn(2);
            RegionCommands.claim(args, plugin, sender);
            fail();
        } catch(CommandException ex) {
            assertEquals(ex.getMessage(), "Incorrect parameter syntax!");
        }
    }
    
    // Use __global__ as a region
    @Test
    public void testGlobalRegionId() {
        try {
            when(args.getString(0)).thenReturn("__global__");
            RegionCommands.claim(args, plugin, sender);
            fail();
        } catch(CommandException ex) {
            assertEquals(ex.getMessage(), "A region cannot be named __global__");
        }
    }
        
    // Could not find region by that id
    @Test
    public void testRegionNotFound() {
        try {
            when(mgr.getRegion("test_region")).thenReturn(null);
            RegionCommands.claim(args, plugin, sender);            
            fail();
        } catch(CommandException ex) {
            assertEquals(ex.getMessage(), "Could not find a region by that ID.");
        }
    }
     
    // You own too many regions
    @Test
    public void testTooManyRegionsOwned() {
        try {
            Set<String> emptyOwners = new HashSet<String>();
            when(owners.getPlayers()).thenReturn(emptyOwners);
            
            when(mgr.getRegionCountOfPlayer(localPlayer)).thenReturn(1);
            RegionCommands.claim(args, plugin, sender);
            fail();
        } catch(CommandException ex) {
            assertEquals(ex.getMessage(), "You own too many regions, delete one first to claim a new one.");
        }
    }
    
    // Region already has an owner
    @Test
    public void testRegionAlreadyHasOwner() {
        try {
            when(player.getName()).thenReturn("Chris");
            RegionCommands.claim(args, plugin, sender);
            fail();
        } catch(CommandException ex) {
            assertEquals(ex.getMessage(), "This region already has an owner - you can't claim or redefine it.");
        }
    }
    
    // Owners reset
    @Test
    public void testResetOwners() throws CommandException, IOException {
        sel = null;
        
        RegionCommands.claim(args, plugin, sender);
        
        assertEquals(owners.getPlayers().size(), 1);
        assertTrue(owners.getPlayers().contains("Shanaynay"));
        verify(mgr).save();
        verify(sender).sendMessage(anyString());
    }
    
    // Owners and Selection reset
    @Test
    @Ignore("No way to mock static methods - need to implement PowerMock")
    public void testResetOwnersAndSelection() throws CommandException, IOException {
        RegionCommands.claim(args, plugin, sender);
        
        assertEquals(owners.getPlayers().size(), 1);
        assertTrue(owners.getPlayers().contains("Shanaynay"));
        verify(mgr).save();
        verify(sender).sendMessage(anyString());
    }
    
    // Region must be within another region; claimOnlyInsideExistingRegions
    @Test
    public void testClaimOnlyInsideExistingRegions() {
        Set<String> emptyOwners = new HashSet<String>();
        when(owners.getPlayers()).thenReturn(emptyOwners);
        
        when(regions.size()).thenReturn(0);
        
        try {
            RegionCommands.claim(args, plugin, sender);
            fail();
        } catch(CommandException ex) {
            assertEquals(ex.getMessage(), "You may only claim regions inside existing regions that you or your group own.");
        }
    }
    
    // Region is too large
    @Test
    public void testRegionIsTooLarge() throws CommandException {
        Set<String> emptyOwners = new HashSet<String>();
        when(owners.getPlayers()).thenReturn(emptyOwners);
        
        wcfg.maxClaimVolume = 4999;
        RegionCommands.claim(args, plugin, sender);
        
        verify(player).sendMessage(ChatColor.RED + "This region is too large to claim.");
        verify(player).sendMessage(ChatColor.RED + "Max. volume: " + wcfg.maxClaimVolume + ", your volume: " + region.volume());
    }
    
    // Finally, claim a region
    @Test
    public void testClaimRegion() throws CommandException, IOException {
        Set<String> emptyOwners = new HashSet<String>();
        when(owners.getPlayers()).thenReturn(emptyOwners);
        
        RegionCommands.claim(args, plugin, sender);
        
        verify(mgr).save();
        verify(sender).sendMessage(ChatColor.YELLOW + "Region saved as test_region.");
    }
}
