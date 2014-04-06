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

import java.io.File;
import java.io.IOException;

import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

public class Locale {

    private static final String LOCALE_HEADER = "#\r\n" +
            "# WorldGuard's locale configuration file\r\n" +
            "#\r\n" +
            "# This is the locale configuration file. Anything placed into here will\r\n" +
            "# be applied to strings appearing in the chat.\r\n" +
            "#\r\n" +
            "# About editing this file:\r\n" +
            "# - DO NOT USE TABS. You MUST use spaces or Bukkit will complain. If\r\n" +
            "#   you use an editor like Notepad++ (recommended for Windows users), you\r\n" +
            "#   must configure it to \"replace tabs with spaces.\" In Notepad++, this can\r\n" +
            "#   be changed in Settings > Preferences > Language Menu.\r\n" +
            "# - Don't get rid of the indents. They are indented so some entries are\r\n" +
            "#   in categories (like \"enforce-single-session\" is in the \"protection\"\r\n" +
            "#   category.\r\n" +
            "# - If you want to check the format of this file before putting it\r\n" +
            "#   into WorldGuard, paste it into http://yaml-online-parser.appspot.com/\r\n" +
            "#   and see if it gives \"ERROR:\".\r\n" +
            "# - Lines starting with # are comments and so they are ignored.\r\n" +
            "#\r\n";

    /**
     * Reference to the plugin.
     */
    private WorldGuardPlugin plugin;

    /**
     * The language configuration.
     */
    private String language;

    /**
     * The locale configuration
     */
    private YAMLProcessor locale;

    /**
     * The locale values
     */
    public String blockBreak;
    public String blockBreakChest;
    public String blockPlace;
    public String blockPlaceChest;
    public String signChangeChest;
    public String signChangeNotPost;
    public String signChangeInvalidSign;
    public String signChangeUnsafeBlock;
    public String signChangeChestProtected;
    public String signChangeNoChestProtection;
    public String signChangeCantBuild;
    public String entityCancelPVP;
    public String entityCancelPVPOthers;
    public String entityItemFrameDestroy;
    public String hangingBreak;
    public String hangingPlace;
    public String hangingEntityInteract;
    public String playerRegionCantEnter;
    public String playerRegionCantLeave;
    public String playerRegionNotifyEnter;
    public String playerRegionNotifyLeave;
    public String playerJoinActivityHalted;
    public String playerJoinFirespreadDisabled;
    public String playerChatDeny;
    public String playerInfiniteStackRemoved;
    public String playerSplashDeny;
    public String playerPotionsDeny;
    public String playerMoveDragonEgg;
    public String playerInfiniteStackRemovedSlot;
    public String playerWandCanYouBuild;
    public String playerWandCanBuildYes;
    public String playerWandCanBuildNo;
    public String playerWandRegionList;
    public String playerWandNoRegion;
    public String playerPlaceStep;
    public String playerPlaceBed;
    public String playerPlaceDoor;
    public String playerTryFire;
    public String playerPlaceEnderpearl;
    public String playerUseBonemeal;
    public String playerUseCocoaBeans;
    public String playerPlacePlantFlowerPot;
    public String playerUseBed;
    public String playerOpenContainer;
    public String playerUseEntityBlock;
    public String playerUseRedstoneMechanism;
    public String playerUseCake;
    public String playerPlaceMinecart;
    public String playerPlaceBoat;
    public String playerOpenProtectedChest;
    public String playerDropItem;
    public String playerBucketFill;
    public String playerBucketEmpty;
    public String playerBedEnter;
    public String playerTeleport;
    public String playerBlockedCmd;
    public String playerNoOp;
    public String vehicleDestroy;
    public String commandExceptionPermissions;
    public String commandExceptionNumberFormat;
    public String commandExceptionOther;
    public String commandExceptionPlayerExpected;
    public String commandExceptionNoSuchPlayer;
    public String commandExceptionInvalidGroup;
    public String commandExceptionTooManyPlayers;
    public String commandExceptionNoNormalWorld;
    public String commandExceptionNoNetherWorld;
    public String commandExceptionPlayerArgumentExcepted;
    public String commandExceptionInvalidIdentifier;
    public String commandExceptionNoSuchWorld;
    public String commandExceptionWENotFound;
    public String commandExceptionWEDetectionFailed;
    public String commandGodSelf;
    public String commandGodOthers;
    public String commandGodOthersNotification;
    public String commandUngodSelf;
    public String commandUngodOthers;
    public String commandUngodOthersNotification;
    public String commandHealSelf;
    public String commandHealOthers;
    public String commandHealOthersNotification;
    public String commandSlaySelf;
    public String commandSlayOthers;
    public String commandSlayOthersNotification;
    public String commandLocateSelf;
    public String commandLocateOthers;
    public String commandStackSelf;
    public String regionNotStanding;
    public String regionDbListSaving;
    public String regionDbListLoading;
    public String regionDbSaving;    
    public String regionDbSaved;
    public String regionDbLoading;    
    public String regionDbLoaded;
    public String regionDbMigrated;
    public String regionSelectedCuboid;
    public String regionSelectedPolygon;
    public String regionDefineHeightWarn;
    public String regionDefineHint;
    public String regionDefineTell;
    public String regionRedefine;
    public String regionClaimTooLarge;
    public String regionClaimTooLargeHint;
    public String regionList;
    public String regionFlagUnknown;
    public String regionFlagList;
    public String regionFlagSet;
    public String regionFlagRemoved;
    public String regionGroupFlagSet;
    public String regionGroupFlagRemoved;
    public String regionSetPriority;
    public String regionRemove;
    public String regionTeleport;
    public String regionUpdated;
    public String commandStopFireBroadcast;
    public String commandStopFireAlreadyDisabled;
    public String commandAllowFireBroadcast;
    public String commandAllowFireAlreadyEnabled;
    public String commandStopLagActivityHalted;
    public String commandStopLagActivityHaltedBroadcast;
    public String commandStopLagEntitiesRemoved;
    public String commandStopLagActivityEnabled;
    public String commandStopLagActivityEnabledBroadcast;
    public String commandReloadSuccess;
    public String commandReloadError;    
    public String commandReportWritten;
    public String commandReportUploading;
    public String commandReportUploadSuccess;
    public String commandReportUploadError;
    public String commandFlushStatesSelf;
    public String commandFlushStatesOthers;
    public String commandExceptionReportError;
    public String commandExceptionInvalidFlagFormat;
    public String commandExceptionNoWorldSpecified;
    public String commandExceptionNoSuchRegion;
    public String commandExceptionFailedWriteRegions;
    public String commandExceptionOwnMaxRegions;
    public String commandExceptionRemoveWithoutName;
    public String commandExceptionInvalidRegionId;
    public String commandExceptionNoGlobal;
    public String commandExceptionNoSuchRegionId;
    public String commandExceptionNotStanding;
    public String commandExceptionStandingSeveral;
    public String commandExceptionNoSelection;
    public String commandExceptionInvalidSelection;
    public String commandExceptionRegionSaving;
    public String commandExceptionRegionLoading;    
    public String commandExceptionGlobalSelection;
    public String commandExceptionUnknownRegionType;
    public String commandExceptionRegionAlreadyDefined;
    public String commandExceptionExistingRegion;
    public String commandExceptionClaimTooManyRegions;
    public String commandExceptionClaimExistingRegion;
    public String commandExceptionClaimOverlap;
    public String commandExceptionClaimInsideExistingRegion;
    public String commandExceptionNoRegionSpecified;
    public String commandExceptionNoRegionGroupFlag;
    public String commandExceptionNoRegionManager;
    public String commandExceptionMigrateCommon;
    public String commandExceptionNoMigratorFound;
    public String commandExceptionMigrateWarn;
    public String commandExceptionMigrateError;
    public String commandExceptionNoRegionSpawn;
    public String commandExceptionNoRegionTeleport;

    /**
     * Construct the object.
     *
     * @param plugin The plugin instance
     */
    public Locale(WorldGuardPlugin plugin, String language) {
        this.plugin = plugin;
        this.language = language;
    }

    /**
     * Load the configuration.
     */
    public void load() {
        String localeFileName = "locale_" + language + ".yml";

        // Create the default locale file
        plugin.createDefaultConfiguration(
                new File(plugin.getDataFolder(), "locale_en.yml"), "locale_en.yml");

        locale = new YAMLProcessor(new File(plugin.getDataFolder(), localeFileName), true, YAMLFormat.EXTENDED);
        try {
            locale.load();
        } catch (IOException e) {
            plugin.getLogger().severe("Error reading locale file: " + localeFileName + ". Trying to load locale_en.yml...");
            locale = new YAMLProcessor(new File(plugin.getDataFolder(), "locale_en.yml"), true, YAMLFormat.EXTENDED);
            try {
                locale.load();
            } catch (IOException e2) {
                e2.printStackTrace();            
                plugin.getServer().shutdown();
            }
        }

        // com.sk89q.worldguard.bukkit.WorldGuadBlockListener
        blockBreak = locale.getString("block.break", "&RYou don't have permission for this area.");
        blockBreakChest = locale.getString("block.break_chest", "&RThe chest is protected.");
        blockPlace = locale.getString("block.place", "&RYou don't have permission for this area.");
        blockPlaceChest = locale.getString("block.place_chest", "&RThis spot is for a chest that you don't have permission for.");
        signChangeChest = locale.getString("sign_change.chest", "&RYou do not own the adjacent chest.");
        signChangeNotPost = locale.getString("sign_change.not_post", "&rThe [Lock] sign must be a sign post, not a wall sign.");
        signChangeInvalidSign = locale.getString("sign_change.invalid_sign", "&rThe first owner line must be your name.");
        signChangeUnsafeBlock = locale.getString("sign_change.unsafe_block", "&rThat is not a safe block that you're putting this sign on.");
        signChangeChestProtected = locale.getString("sign_change.chest_protected", "&yA chest or double chest above is now protected.");
        signChangeNoChestProtection = locale.getString("sign_change.no_chest_protection", "&rWorldGuard's sign chest protection is disabled.");
        signChangeCantBuild = locale.getString("sign_change.cant_build", "&RYou don't have permission for this area.");

        // com.sk89q.worldguard.bukkit.WorldGuardEntityListener
        entityCancelPVP = locale.getString("entity.cancel_pvp", "&RYou are in a no-PvP area.");
        entityCancelPVPOthers = locale.getString("entity.cancel_pvp_others", "&RThat player is in a no-PvP area.");
        entityItemFrameDestroy = locale.getString("entity.item_frame_destroy", "&RYou don't have permission for this area.");

        // com.sk89q.worldguard.bukkit.WorldGuardHangingListener
        hangingBreak = locale.getString("hanging.break", "&RYou don't have permission for this area.");
        hangingPlace = locale.getString("hanging.place", "&RYou don't have permission for this area.");
        hangingEntityInteract = locale.getString("hanging.entity_interact", "&RYou don't have permission for this area.");

        // com.sk89q.worldguard.bukkit.WorldGuardPlayerListener
        playerRegionCantEnter = locale.getString("player.region_cant_enter", "&RYou are not permitted to enter this area.");
        playerRegionCantLeave = locale.getString("player.region_cant_leave", "&RYou are not permitted to leave this area.");
        playerRegionNotifyEnter = locale.getString("player.region_notify_enter", "&2WG: &p%playerName%&Y entered NOTIFY region: &w%regionList%");
        playerRegionNotifyLeave = locale.getString("player.region_notify_leave", "&2WG: &p%playerName%&Y left NOTIFY region");
        playerJoinActivityHalted = locale.getString("player.join_activity_halted", "&yIntensive server activity has been HALTED.");
        playerJoinFirespreadDisabled = locale.getString("player.join_firespread_disabled", "&yFire spread is currently globally disabled for this world.");
        playerChatDeny = locale.getString("player.chat_deny", "&rYou don't have permission to chat in this region!");
        playerInfiniteStackRemoved = locale.getString("player.infinite_stack_removed", "&rInfinite stack removed.");
        playerSplashDeny = locale.getString("player.splash_deny", "&rSorry, potions with %potionEffect% can't be thrown, even if you have a permission to bypass it, due to limitations (and because overly-reliable potion blocking is on).");
        playerPotionsDeny = locale.getString("player.potions_deny", "&rSorry, potions with %potionEffect% are presently disabled.");
        playerMoveDragonEgg = locale.getString("player.move_dragon_egg", "&RYou're not allowed to move dragon eggs here!");
        playerInfiniteStackRemovedSlot = locale.getString("player.infinite_stack_removed_slot", "&rInfinite stack in slot #%slotNumber% removed.");
        playerWandCanYouBuild = locale.getString("player.wand_can_you_build", "&yCan you build?");
        playerWandCanBuildYes = locale.getString("player.wand_can_build_yes", "&yYes");
        playerWandCanBuildNo = locale.getString("player.wand_can_build_no", "&yNo");
        playerWandRegionList = locale.getString("player.wand_region_list", "&yApplicable regions: %regionList%");
        playerWandNoRegion = locale.getString("player.wand_no_region", "&yWorldGuard: No defined regions here!");
        playerPlaceStep = locale.getString("player.place_step", "&RYou don't have permission for this area.");
        playerPlaceBed = locale.getString("player.place_bed", "&RYou don't have permission for this area.");
        playerPlaceDoor = locale.getString("player.place_door", "&RYou don't have permission for this area.");
        playerTryFire = locale.getString("player.try_fire", "&RYou're not allowed to use that here.");
        playerPlaceEnderpearl = locale.getString("player.place_enderpearl", "&RYou're not allowed to use that here.");
        playerUseBonemeal = locale.getString("player.use_bonemeal", "&RYou're not allowed to use that here.");
        playerUseCocoaBeans = locale.getString("player.use_cocoa_beans", "&RYou're not allowed to plant that here.");        
        playerPlacePlantFlowerPot = locale.getString("player.place_plant_flowerpot", "&RYou're not allowed to plant that here.");
        playerUseBed = locale.getString("player.use_bed", "&RYou're not allowed to use that bed.");
        playerOpenContainer = locale.getString("player.open_container", "&RYou don't have permission to open that in this area.");
        playerUseEntityBlock = locale.getString("player.use_entity_block", "&RYou don't have permission to use that in this area.");
        playerUseRedstoneMechanism = locale.getString("player.use_redstone_mechanism", "&RYou don't have permission to use that in this area.");
        playerUseCake = locale.getString("player.use_cake", "&RYou're not invited to this tea party!");
        playerPlaceMinecart = locale.getString("player.place_minecart", "&RYou don't have permission to place vehicles here.");
        playerPlaceBoat = locale.getString("player.place_boat", "&RYou don't have permission to place vehicles here.");
        playerOpenProtectedChest = locale.getString("player.open_protected_chest", "&RThe chest is protected.");
        playerDropItem = locale.getString("player.drop_item", "&rYou don't have permission to do that in this area.");
        playerBucketFill = locale.getString("player.bucket_fill", "&RYou don't have permission for this area.");
        playerBucketEmpty = locale.getString("player.bucket_empty", "&RYou don't have permission for this area.");
        playerBedEnter = locale.getString("player.bed_enter", "&RThis bed doesn't belong to you!");
        playerTeleport = locale.getString("player.teleport", "&RYou're not allowed to go there.");
        playerBlockedCmd = locale.getString("player.blocked_cmd", "&r%cmd% is not allowed in this area.");
        playerNoOp = locale.getString("player.no_op", "&r/op can only be used in console (as set by a WG setting).");
        
        // com.sk89q.worldguard.bukkit.WorldGuardVehicleListener
        vehicleDestroy = locale.getString("vehicle.destroy", "&RYou don't have permission to destroy vehicles here.");

        // com.sk89q.worldguard.bukkit.WorldGuardPlugin
        commandExceptionPermissions = locale.getString("command.exception.permissions", "&rYou don't have permission.");
        commandExceptionNumberFormat = locale.getString("command.exception.number_format", "&rNumber expected, string received instead.");
        commandExceptionOther = locale.getString("command.exception.other", "&rAn error has occurred. See console.");
        commandExceptionPlayerExpected = locale.getString("command.exception.player_excepted", "&rA player is expected.");
        commandExceptionNoSuchPlayer = locale.getString("command.exception.no_such_player", "&rNo players matched query.");
        commandExceptionInvalidGroup = locale.getString("command.exception.invalid_group", "&rInvalid group '%groupFilter%'.");
        commandExceptionTooManyPlayers = locale.getString("command.exception.too_many_players", "&rMore than one player found! Use @<name> for exact matching.");
        commandExceptionNoNormalWorld = locale.getString("command.exception.no_normal_world", "&rNo normal world found.");
        commandExceptionNoNetherWorld = locale.getString("command.exception.no_nether_world", "&rNo nether world found.");
        commandExceptionPlayerArgumentExcepted = locale.getString("command.exception.player_argument_expected", "&rArgument expected for #player.");
        commandExceptionInvalidIdentifier = locale.getString("command.exception.invalid_identifier", "&rInvalid identifier '%playerFilter%'.");
        commandExceptionNoSuchWorld = locale.getString("command.exception.no_such_world", "&rNo world by that exact name found.");
        commandExceptionWENotFound = locale.getString("command.exception.worldedit_not_found", "&rWorldEdit does not appear to be installed.");
        commandExceptionWEDetectionFailed = locale.getString("command.exception.worldedit_detection_failed", "&rWorldEdit detection failed (report error).");

        // com.sk89q.worldguard.bukkit.commands.GeneralCommands
        commandGodSelf = locale.getString("command.god.self", "&yGod mode enabled! Use /ungod to disable.");
        commandGodOthers = locale.getString("command.god.others", "&yGod enabled by %playerName%.");
        commandGodOthersNotification = locale.getString("command.god.others_notification", "&yPlayers now have god mode.");
        commandUngodSelf = locale.getString("command.ungod.self", "&yGod mode disabled!");
        commandUngodOthers = locale.getString("command.ungod.others", "&yGod disabled by %playerName%.");
        commandUngodOthersNotification = locale.getString("command.ungod.others_notification", "&yPlayers no longer have god mode.");
        commandHealSelf = locale.getString("command.heal.self", "&yHealed!");
        commandHealOthers = locale.getString("command.heal.others", "&yHealed by %playerName%.");
        commandHealOthersNotification = locale.getString("command.heal.others_notification", "&yPlayers healed.");
        commandSlaySelf = locale.getString("command.slay.self", "&ySlain!");
        commandSlayOthers = locale.getString("command.slay.others", "&ySlain by %playerName%.");
        commandSlayOthersNotification = locale.getString("command.slay.others_notification", "&yPlayers slain.");
        commandLocateSelf = locale.getString("command.locate.self", "&yCompass reset to spawn.");
        commandLocateOthers = locale.getString("command.locate.others", "&yCompass repointed.");
        commandStackSelf = locale.getString("command.stack.self", "&yItems compacted into stacks!");

        // com.sk89q.worldguard.bukkit.commands.ToggleCommands
        commandStopFireBroadcast = locale.getString("command.stopfire.broadcast", "&yFire spread has been globally disabled for '%worldName%' by %playerName%.");
        commandStopFireAlreadyDisabled = locale.getString("command.stopfire.already_disabled", "&yFire spread was already globally disabled.");
        commandAllowFireBroadcast = locale.getString("command.allowfire.broadcast", "&yFire spread has been globally for '%worldName%' re-enabled by %playerName%.");
        commandAllowFireAlreadyEnabled = locale.getString("command.allowfire.already_enabled", "&yFire spread was already globally enabled.");
        commandStopLagActivityHalted = locale.getString("command.stoplag.activity_halted", "&yALL intensive server activity halted.");
        commandStopLagActivityHaltedBroadcast = locale.getString("command.stoplag.activity_halted_broadcast", "&yALL intensive server activity halted by %playerName%.");
        commandStopLagEntitiesRemoved = locale.getString("command.stoplag.entities_removed", "%entitiesNumber% entities (>10) auto-removed from %worldName%");
        commandStopLagActivityEnabled = locale.getString("command.stoplag.activity_enabled", "ALL intensive server activity no longer halted.");
        commandStopLagActivityEnabledBroadcast = locale.getString("command.stoplag.activity_enabled_broadcast", "&yALL intensive server activity is now allowed.");

        // com.sk89q.worldguard.bukkit.commands.WorldGuardCommands
        commandReloadSuccess = locale.getString("command.reload.success", "WorldGuard configuration reloaded.");
        commandReloadError = locale.getString("command.reload.error", "Error while reloading: %errorMessage%");
        commandReportWritten = locale.getString("command.report.report_written", "&yWorldGuard report written to %path%");
        commandReportUploading = locale.getString("command.report.report_uploading", "&yNow uploading to Pastebin...");
        commandReportUploadSuccess = locale.getString("command.report.report_upload_success", "&yWorldGuard report (1 hour): %url%");
        commandReportUploadError = locale.getString("command.report.report_upload_error", "&yWorldGuard report pastebin error: %errorMessage%");
        commandFlushStatesSelf = locale.getString("command.flushstates.self", "Cleared all states.");
        commandFlushStatesOthers = locale.getString("command.flushstates.others", "Cleared states for player %playerName%.");
        commandExceptionReportError = locale.getString("command.exception.report_error", "&rFailed to write report: %errorMessage%");

        // com.sk89q.worldguard.protection.flags.LocationFlag
        // com.sk89q.worldguard.protection.flags.VectorFlag        
        commandExceptionInvalidFlagFormat = locale.getString("command.exception.invalid_flag_format", "&rExpected 'here' or x,y,z.");
        
        // com.sk89q.worldguard.bukkit.commands.RegionCommands
        regionNotStanding = locale.getString("command.region.not_standing", "&2You're not standing in any regions. Using the global region for this world instead.");
        regionDbListSaving = locale.getString("command.region.region_db_list_saving", "&2Now saving region list to disk... (Taking too long? We're fixing it)");
        regionDbListLoading = locale.getString("command.region.region_db_list_loading", "&2Now loading region list from disk... (Taking too long? We're fixing it)");
        regionDbSaving = locale.getString("command.region.region_db_saving", "&ySaving all region databases... This might take a bit.");
        regionDbSaved = locale.getString("command.region.region_db_saved", "&yRegion databases saved.");
        regionDbLoading = locale.getString("command.region.region_db_loading", "&yLoading all region databases... This might take a bit.");
        regionDbLoaded = locale.getString("command.region.region_db_loaded", "&yRegion databases loaded.");
        regionDbMigrated = locale.getString("command.region.region_db_migrated", "&yRegions have been migrated successfully.\\nIf you wish to use the destination format as your new backend, please update your config and reload WorldGuard.");
        regionSelectedCuboid = locale.getString("command.region.selected_cuboid", "&yRegion selected as a cuboid.");
        regionSelectedPolygon = locale.getString("command.region.selected_polygon", "&yRegion selected as a polygon.");
        regionDefineHeightWarn = locale.getString("command.region.define.height_warn", "&Y(Warning: The height of the region was %regionHeight% block(s).)");
        regionDefineHint = locale.getString("command.region.define.hint", "&2(This region is NOW PROTECTED from modification from others. Don't want that? Use &c/rg flag %regionId% &2passthrough allow&2)");
        regionDefineTell = locale.getString("command.region.define.tell", "&yA new region has been made named '%regionId%'.");
        regionRedefine = locale.getString("command.region.redefine", "&yRegion '%regionId%' updated with new area.");
        regionClaimTooLarge = locale.getString("command.region.claim.too_large", "&rThis region is too large to claim.");
        regionClaimTooLargeHint = locale.getString("command.region.claim.too_large_hint", "&rMax. volume: %maxClaimVolume%, your volume: %regionVolume%");
        regionList = locale.getString("command.region.list", "&rRegions %regionOwner% (page %pageNumber% of %pageCount%):");
        regionFlagUnknown = locale.getString("command.region.flag.unknown", "&rUnknown flag specified: %flagName%");
        regionFlagList = locale.getString("command.region.flag.list", "&rAvailable flags: %flagList%");
        regionFlagSet = locale.getString("command.region.flag.flag_set", "&yRegion flag %flagName% set on '%regionId%' to '%flagValue%'.");
        regionFlagRemoved = locale.getString("command.region.flag.flag_removed", "&yRegion flag %flagName% removed from '%regionId%'. (Any -g(roups) were also removed.)");
        regionGroupFlagSet = locale.getString("command.region.flag.group_flag_set", "&yRegion group flag for '%flagName%' set.");
        regionGroupFlagRemoved = locale.getString("command.region.flag.group_flag_removed", "&yRegion group flag for '%flagName%' reset to default.");
        regionSetPriority = locale.getString("command.region.setpriority", "&yPriority of '%regionId%' set to %priority% (higher numbers override).");
        regionRemove = locale.getString("command.region.remove", "&yRegion '%regionId%' removed.");
        regionTeleport = locale.getString("command.region.teleport", "&yTeleported you to the region '%regionId%'.");
        commandExceptionInvalidRegionId = locale.getString("command.exception.invalid_region_id", "&rThe region name of '%regionId%' contains characters that are not allowed.");
        commandExceptionNoGlobal = locale.getString("command.exception.no_global", "&rSorry, you can't use __global__ here.");
        commandExceptionNoSuchRegionId = locale.getString("command.exception.no_such_region_id", "&rNo region could be found with the name of '%regionId%'.");
        commandExceptionNotStanding = locale.getString("command.exception.region_not_standing", "&rYou're not standing in a region. Specify an ID if you want to select a specific region.");
        commandExceptionStandingSeveral = locale.getString("command.exception.region_standing_several", "&rYou're standing in several regions, and WorldGuard is not sure what you want.\\nYou're in: %regionList%");
        commandExceptionNoSelection = locale.getString("command.exception.no_selection", "&rPlease select an area first. Use WorldEdit to make a selection! (wiki: http://wiki.sk89q.com/wiki/WorldEdit).");
        commandExceptionInvalidSelection = locale.getString("command.exception.invalid_selection", "&rSorry, you can only use cuboids and polygons for WorldGuard regions.");
        commandExceptionRegionSaving = locale.getString("command.exception.region_saving", "&rUh oh, regions did not save: %errorMessage%");
        commandExceptionRegionLoading = locale.getString("command.exception.region_loading", "&rUh oh, regions did not load: %errorMessage%");        
        commandExceptionGlobalSelection = locale.getString("command.exception.global_selection", "&rCan't select global regions! That would cover the entire world.");
        commandExceptionUnknownRegionType = locale.getString("command.exception.region_unknown_type", "&rUnknown region type: %regionType%");
        commandExceptionRegionAlreadyDefined = locale.getString("command.exception.region_already_defined", "&rThat region is already defined. To change the shape, use /region redefine %regionId%");
        commandExceptionExistingRegion = locale.getString("command.exception.region_existing", "&rThat region already exists. Please choose a different name.");
        commandExceptionClaimTooManyRegions = locale.getString("command.exception.claim_too_many_regions", "&rYou own too many regions, delete one first to claim a new one.");
        commandExceptionClaimExistingRegion = locale.getString("command.exception.claim_existing_region", "&rThis region already exists and you don't own it.");
        commandExceptionClaimOverlap = locale.getString("command.exception.claim_overlap_region", "&rThis region overlaps with someone else's region.");
        commandExceptionClaimInsideExistingRegion = locale.getString("command.exception.claim_inside_existing_region", "&rYou may only claim regions inside existing regions that you or your group own.");
        commandExceptionNoRegionSpecified = locale.getString("command.exception.no_region_specified", "&rPlease specify the region with /region info -w world_name region_name.");
        commandExceptionNoRegionGroupFlag = locale.getString("command.exception.no_region_groupflag", "&rRegion flag '%flagName%' does not have a group flag!");
        commandExceptionNoRegionManager = locale.getString("command.exception.no_region_manager", "&rNo region manager exists for world '%worldName%'.");
        commandExceptionMigrateCommon = locale.getString("command.exception.migrate_common", "&rWill not migrate with common source and target.");
        commandExceptionNoMigratorFound = locale.getString("command.exception.no_migrator_found", "&rNo migrator found for that combination and direction.");
        commandExceptionMigrateWarn = locale.getString("command.exception.migrate_warn", "&rThis command is potentially dangerous.\\nPlease ensure you have made a backup of your data, and then re-enter the command exactly to procede.");
        commandExceptionMigrateError = locale.getString("command.exception.migrate_error", "&rError migrating database: %errorMessage%");
        commandExceptionNoRegionSpawn = locale.getString("command.exception.no_region_spawn_point", "&rThe region has no spawn point associated.");
        commandExceptionNoRegionTeleport = locale.getString("command.exception.no_region_teleport_point", "&rThe region has no teleport point associated.");

        // com.sk89q.worldguard.bukkit.commands.RegionMemberCommands
        regionUpdated = locale.getString("command.region.region_updated", "&yRegion '%regionId%' updated.");
        commandExceptionNoWorldSpecified = locale.getString("command.exception.no_world_specified", "&rNo world specified. Use -w <worldname>.");
        commandExceptionNoSuchRegion = locale.getString("command.exception.no_such_region", "&rCould not find a region by that ID.");
        commandExceptionFailedWriteRegions = locale.getString("command.exception.failed_write_regions", "&rFailed to write regions: %errorMessage%");
        commandExceptionOwnMaxRegions = locale.getString("command.exception.own_max_regions", "&rYou already own the maximum allowed amount of regions.");
        commandExceptionRemoveWithoutName = locale.getString("command.exception.remove_without_name", "&rList some names to remove, or use -a to remove all.");

        plugin.getLogger().info("Loaded locale");

        locale.setHeader(LOCALE_HEADER);

        if (!locale.save()) {
            plugin.getLogger().severe("Error saving locale!");
        }
    }
}
