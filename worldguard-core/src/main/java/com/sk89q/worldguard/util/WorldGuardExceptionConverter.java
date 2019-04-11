/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.command.parametric.ExceptionConverterHelper;
import com.sk89q.worldedit.util.command.parametric.ExceptionMatch;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.util.UnresolvedNamesException;

import java.util.concurrent.CancellationException;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorldGuardExceptionConverter extends ExceptionConverterHelper {

    private static final Pattern numberFormat = Pattern.compile("^Для входной строки: \"(.*)\"$");
    private final WorldGuard worldGuard;

    public WorldGuardExceptionConverter(WorldGuard worldGuard) {
        checkNotNull(worldGuard);
        this.worldGuard = worldGuard;
    }

    @ExceptionMatch
    public void convert(NumberFormatException e) throws CommandException {
        final Matcher matcher = numberFormat.matcher(e.getMessage());

        if (matcher.matches()) {
            throw new CommandException("Ожидаемое количество; строка \"" + matcher.group(1)
                    + "\" получено.");
        } else {
            throw new CommandException("Ожидаемое количество; заданная строка.");
        }
    }

    @ExceptionMatch
    public void convert(StorageException e) throws CommandException {
        WorldGuard.logger.log(Level.WARNING, "Ошибка загрузки/сохранения регионов", e);
        throw new CommandException("Данные региона не могут быть загружены/сохранены: " + e.getMessage());
    }

    @ExceptionMatch
    public void convert(RejectedExecutionException e) throws CommandException {
        throw new CommandException("В настоящее время в очереди слишком много задач, чтобы добавить ваши. Используйте /wg running, чтобы перечислить поставленные в очередь и выполняющиеся задачи.", e);
    }

    @ExceptionMatch
    public void convert(CancellationException e) throws CommandException {
        throw new CommandException("WorldGuard: Задача была отменена.", e);
    }

    @ExceptionMatch
    public void convert(InterruptedException e) throws CommandException {
        throw new CommandException("WorldGuard: Задача была прервана.", e);
    }

    @ExceptionMatch
    public void convert(WorldEditException e) throws CommandException {
        throw new CommandException(e.getMessage(), e);
    }

    @ExceptionMatch
    public void convert(UnresolvedNamesException e) throws CommandException {
        throw new CommandException(e.getMessage(), e);
    }

    @ExceptionMatch
    public void convert(AuthorizationException e) throws CommandException {
        throw new CommandException("У вас нет разрешения на это.", e);
    }
}
