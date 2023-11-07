package com.sk89q.worldguard.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldguard.WorldGuard;

public class GeneralCompatibleCommands {
    private final GeneralCommands generalCommands;

    public GeneralCompatibleCommands(WorldGuard worldGuard) {
        this.generalCommands = new GeneralCommands(worldGuard);
    }

    @Command(aliases = {"wggod"}, usage = "[player]",
            desc = "Enable godmode on a player", flags = "s", max = 1)
    public void god(CommandContext args, Actor sender) throws CommandException, AuthorizationException {
        generalCommands.god(args, sender);
    }

    @Command(aliases = {"wgungod"}, usage = "[player]",
            desc = "Disable godmode on a player", flags = "s", max = 1)
    public void ungod(CommandContext args, Actor sender) throws CommandException, AuthorizationException {
        generalCommands.ungod(args, sender);
    }

    @Command(aliases = {"wgheal"}, usage = "[player]", desc = "Heal a player", flags = "s", max = 1)
    public void heal(CommandContext args, Actor sender) throws CommandException, AuthorizationException {
        generalCommands.heal(args, sender);
    }

    @Command(aliases = {"wgslay"}, usage = "[player]", desc = "Slay a player", flags = "s", max = 1)
    public void slay(CommandContext args, Actor sender) throws CommandException, AuthorizationException {
        generalCommands.slay(args, sender);
    }

    @Command(aliases = {"wglocate"}, usage = "[player]", desc = "Locate a player", max = 1)
    @CommandPermissions({"worldguard.locate"})
    public void locate(CommandContext args, Actor sender) throws CommandException {
        generalCommands.locate(args, sender);
    }

    @Command(aliases = {"wgstack", ";"}, usage = "", desc = "Stack items", max = 0)
    @CommandPermissions({"worldguard.stack"})
    public void stack(CommandContext args, Actor sender) throws CommandException {
        generalCommands.stack(args, sender);
    }
}
