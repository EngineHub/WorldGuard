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

package com.sk89q.worldguard.commands.region;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.Registry;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.component.PaginationBox;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import com.sk89q.worldedit.util.formatting.text.serializer.legacy.LegacyComponentSerializer;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.LocationFlag;
import com.sk89q.worldguard.protection.flags.NumberFlag;
import com.sk89q.worldguard.protection.flags.RegistryFlag;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.UnknownFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

class FlagHelperBox extends PaginationBox {

    private static final List<Flag<?>> FLAGS = WorldGuard.getInstance().getFlagRegistry().getAll().stream()
            .sorted((f1, f2) -> {
                if (f1 == f2) return 0;
                int idx1 = Flags.INBUILT_FLAGS.indexOf(f1.getName());
                int idx2 = Flags.INBUILT_FLAGS.indexOf(f2.getName());
                if (idx1 < 0 && idx2 >= 0) return 1;
                if (idx2 < 0 && idx1 >= 0) return -1;
                if (idx1 < 0) return f1.getName().compareTo(f2.getName());
                return idx1 < idx2 ? -1 : 1;
            })
            .collect(Collectors.toList());
    private static final int SIZE = FLAGS.size() == Flags.INBUILT_FLAGS.size() ? FLAGS.size() : FLAGS.size() + 1;
    private static final int PAD_PX_SIZE = 180;
    static final Set<Flag<?>> DANGER_ZONE = ImmutableSet.of(Flags.BUILD, Flags.PASSTHROUGH, Flags.BLOCK_PLACE, Flags.BLOCK_BREAK);

    private final World world;
    private final ProtectedRegion region;
    private final RegionPermissionModel perms;
    private boolean monoSpace;

    FlagHelperBox(World world, ProtectedRegion region, RegionPermissionModel perms) {
        super("Flags for " + region.getId(), "/rg flags -w \"" + world.getName() + "\" -p %page% " + region.getId());
        this.world = world;
        this.region = region;
        this.perms = perms;
    }

    @Override
    public Component getComponent(int number) {
        if (number == Flags.INBUILT_FLAGS.size()) {
            return centerAndBorder(TextComponent.of("Third-Party Flags", TextColor.AQUA));
        } else if (number > Flags.INBUILT_FLAGS.size()) {
            number -= 1;
        }
        Flag<?> flag = FLAGS.get(number);
        return createLine(flag, number >= Flags.INBUILT_FLAGS.size());
    }

    @Override
    public int getComponentsSize() {
        return SIZE;
    }

    private Component createLine(Flag<?> flag, boolean thirdParty) {
        final TextComponent.Builder builder = TextComponent.builder("");

        appendFlagName(builder, flag, flag instanceof UnknownFlag ? TextColor.GRAY : thirdParty ? TextColor.LIGHT_PURPLE : TextColor.GOLD);
        appendFlagValue(builder, flag);
        return builder.build();
    }

    private void appendFlagName(TextComponent.Builder builder, Flag<?> flag, TextColor color) {
        final String name = flag.getName();
        int length = monoSpace ? name.length() : FlagFontInfo.getPxLength(name);
        builder.append(TextComponent.of(name, color));
        if (flag.usesMembershipAsDefault()) {
            builder.append(TextComponent.empty().append(TextComponent.of("*", TextColor.AQUA))
                    .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT,
                            TextComponent.of("This is a special flag which defaults to allow for members, and deny for non-members"))));
            length += monoSpace ? 1 : FlagFontInfo.getPxLength('*');
        }
        if (flag == Flags.PASSTHROUGH) {
            builder.append(TextComponent.empty().append(TextComponent.of("*", TextColor.AQUA))
                    .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT,
                            TextComponent.of("This is a special flag which overrides build checks. (Not movement related!)"))));
            length += monoSpace ? 1 : FlagFontInfo.getPxLength('*');
        }
        int leftover = (monoSpace ? PAD_PX_SIZE / 3 : PAD_PX_SIZE) - length;
        builder.append(TextComponent.space());
        leftover -= 4;
        if (leftover > 0) {
            builder.append(TextComponent.of(Strings.repeat(".", monoSpace ? leftover : leftover / 2), TextColor.DARK_GRAY));
        }
    }

    private void appendFlagValue(TextComponent.Builder builder, Flag<?> flag) {
        if (flag instanceof StateFlag) {
            appendStateFlagValue(builder, (StateFlag) flag);
        } else if (flag instanceof BooleanFlag) {
            appendBoolFlagValue(builder, ((BooleanFlag) flag));
        } else if (flag instanceof SetFlag) {
            appendSetFlagValue(builder, ((SetFlag<?>) flag));
        } else if (flag instanceof RegistryFlag) {
            appendRegistryFlagValue(builder, ((RegistryFlag<?>) flag));
        } else if (flag instanceof StringFlag) {
            appendStringFlagValue(builder, ((StringFlag) flag));
        } else if (flag instanceof LocationFlag) {
            appendLocationFlagValue(builder, ((LocationFlag) flag));
        } else if (flag instanceof NumberFlag) {
            appendNumericFlagValue(builder, (NumberFlag<? extends Number>) flag);
        } else {
            String display = String.valueOf(region.getFlag(flag));
            if (display.length() > 23) {
                display = display.substring(0, 20) + "...";
            }
            appendValueText(builder, flag, display, null);
        }
    }

    private <T> T getInheritedValue(ProtectedRegion region, Flag<T> flag) {
        ProtectedRegion parent = region.getParent();
        T val;
        while (parent != null) {
            val = parent.getFlag(flag);
            if (val != null) {
                return val;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private <V> void appendValueChoices(TextComponent.Builder builder, Flag<V> flag, Iterator<V> choices, @Nullable String suggestChoice) {
        V defVal = flag.getDefault();
        V currVal = region.getFlag(flag);
        boolean inherited = false;
        if (currVal == null) {
            currVal = getInheritedValue(region, flag);
            if (currVal != null) {
                inherited = true;
            }
        }
        while (choices.hasNext()) {
            V choice = choices.next();
            boolean isExplicitSet = currVal == choice && !inherited;

            boolean maySet = perms.maySetFlag(region, flag, isExplicitSet ? null : String.valueOf(choice));

            TextColor col = isExplicitSet ? TextColor.WHITE : inherited && currVal == choice ? TextColor.GRAY : TextColor.DARK_GRAY;
            Set<TextDecoration> styles = choice == defVal ? ImmutableSet.of(TextDecoration.UNDERLINED) : Collections.emptySet();

            Component choiceComponent = TextComponent.empty().append(TextComponent.of(capitalize(String.valueOf(choice)), col, styles));

            List<Component> hoverTexts = new ArrayList<>();
            if (maySet) {
                if (isExplicitSet) {
                    hoverTexts.add(TextComponent.of("Click to unset", TextColor.GOLD));
                } else if (DANGER_ZONE.contains(flag) && !(ProtectedRegion.GLOBAL_REGION.equals(region.getId()) && flag == Flags.PASSTHROUGH)) {
                    hoverTexts.add(TextComponent.of("Setting this flag may have unintended consequences.", TextColor.RED)
                            .append(TextComponent.newline())
                            .append(TextComponent.of("Please read the documentation and set this flag manually if you really intend to.")
                            .append(TextComponent.newline())
                            .append(TextComponent.of("(Hint: You do not need to set this to protect the region!)"))));
                } else {
                    hoverTexts.add(TextComponent.of("Click to set", TextColor.GOLD));
                }
            }
            Component valType = getToolTipHint(defVal, choice, inherited);
            if (valType != null) {
                hoverTexts.add(valType);
            }

            if (!hoverTexts.isEmpty()) {
                TextComponent.Builder hoverBuilder = TextComponent.builder("");
                for (Iterator<Component> hovIt = hoverTexts.iterator(); hovIt.hasNext(); ) {
                    hoverBuilder.append(hovIt.next());
                    if (hovIt.hasNext()) {
                        hoverBuilder.append(TextComponent.newline());
                    }
                }
                choiceComponent = choiceComponent.hoverEvent(
                        HoverEvent.of(HoverEvent.Action.SHOW_TEXT, hoverBuilder.build()));
            }

            if (maySet && (isExplicitSet || !DANGER_ZONE.contains(flag))) {
                builder.append(choiceComponent.clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND,
                        makeCommand(flag, isExplicitSet ? "" : choice))));
            } else {
                builder.append(choiceComponent);
            }
            builder.append(TextComponent.space());
        }
        if (suggestChoice != null && perms.maySetFlag(region, flag)) {
            builder.append(TextComponent.of(suggestChoice, TextColor.DARK_GRAY)
                    .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT,
                            TextComponent.of("Click to set custom value", TextColor.GOLD)))
                    .clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND, makeCommand(flag, ""))));
        }
    }

    private <V> void appendValueText(TextComponent.Builder builder, Flag<V> flag, String display, @Nullable Component hover) {
        V defVal = flag.getDefault();
        V currVal = region.getFlag(flag);
        boolean inherited = false;
        if (currVal == null) {
            currVal = getInheritedValue(region, flag);
            if (currVal != null) {
                inherited = true;
            }
        }

        boolean isExplicitSet = currVal != null && !inherited;

        boolean maySet = !(flag instanceof UnknownFlag) && perms.maySetFlag(region, flag);

        TextColor col = isExplicitSet ? TextColor.WHITE : inherited ? TextColor.GRAY : TextColor.DARK_GRAY;
        Set<TextDecoration> styles = currVal == defVal ? ImmutableSet.of(TextDecoration.UNDERLINED) : Collections.emptySet();

        Component displayComponent = TextComponent.empty().append(TextComponent.of(display, col, styles));

        List<Component> hoverTexts = new ArrayList<>();
        if (maySet) {
            if (isExplicitSet) {
                hoverTexts.add(TextComponent.of("Click to change", TextColor.GOLD));
            } else {
                hoverTexts.add(TextComponent.of("Click to set", TextColor.GOLD));
            }
        }
        Component valType = getToolTipHint(defVal, currVal, inherited);
        if (valType != null) {
            hoverTexts.add(valType);
        }

        if (!hoverTexts.isEmpty()) {
            TextComponent.Builder hoverBuilder = TextComponent.builder("");
            for (Iterator<Component> hovIt = hoverTexts.iterator(); hovIt.hasNext(); ) {
                hoverBuilder.append(hovIt.next());
                if (hovIt.hasNext()) {
                    hoverBuilder.append(TextComponent.newline());
                }
            }
            if (hover != null) {
                hoverBuilder.append(TextComponent.newline());
                hoverBuilder.append(hover);
            }
            displayComponent = displayComponent.hoverEvent(
                    HoverEvent.of(HoverEvent.Action.SHOW_TEXT, hoverBuilder.build()));
        }

        if (maySet) {
            builder.append(displayComponent.clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND,
                    makeCommand(flag, ""))));
        } else {
            builder.append(displayComponent);
        }
        builder.append(TextComponent.space());
    }

    private String makeCommand(Flag<?> flag, Object choice) {
        return "/rg flag -w \"" + world.getName() + "\" -h " + getCurrentPage()
                + " " + region.getId() + " " + flag.getName() + " " + choice;
    }

    private String capitalize(String value) {
        if (value.isEmpty()) return value;
        value = value.toLowerCase();
        return value.length() > 1
                ? Character.toUpperCase(value.charAt(0)) + value.substring(1)
                : String.valueOf(Character.toUpperCase(value.charAt(0)));
    }

    @Nullable
    private <V> Component getToolTipHint(V defVal, V currVal, boolean inherited) {
        Component valType;
        if (inherited) {
            if (currVal == defVal) {
                valType = TextComponent.of("Inherited & ")
                        .append(TextComponent.of("default")
                                .decoration(TextDecoration.UNDERLINED, true))
                        .append(TextComponent.of(" value"));
            } else {
                valType = TextComponent.of("Inherited value");
            }
        } else {
            if (currVal == defVal) {
                valType = TextComponent.empty()
                        .append(TextComponent.of("Default")
                                .decoration(TextDecoration.UNDERLINED, true))
                        .append(TextComponent.of(" value"));
            } else {
                valType = null;
            }
        }
        return valType;
    }

    private void appendStateFlagValue(TextComponent.Builder builder, StateFlag flag) {
        final Iterator<StateFlag.State> choices = Iterators.forArray(StateFlag.State.values());
        appendValueChoices(builder, flag, choices, null);
    }

    private void appendBoolFlagValue(TextComponent.Builder builder, BooleanFlag flag) {
        final Iterator<Boolean> choices = Iterators.forArray(Boolean.TRUE, Boolean.FALSE);
        appendValueChoices(builder, flag, choices, null);
    }

    private <V> void appendSetFlagValue(TextComponent.Builder builder, SetFlag<V> flag) {
        Flag<V> subType = flag.getType();
        Class<?> clazz = subType.getClass();
        String subName;
        subName = clazz.isAssignableFrom(RegistryFlag.class)
                ? ((RegistryFlag<?>) subType).getRegistry().getName()
                : subType.getClass().getSimpleName().replace("Flag", "");
        Set<V> currVal = region.getFlag(flag);
        if (currVal == null) {
            currVal = getInheritedValue(region, flag);
        }
        String display = (currVal == null ? "" : currVal.size() + "x ") + subName;
        final String stringValue = currVal == null ? ""
                : currVal.stream().map(String::valueOf).collect(Collectors.joining(","));
        TextComponent hoverComp = TextComponent.of("");
        if (currVal != null) {
            hoverComp = hoverComp.append(TextComponent.of("Current values:"))
                    .append(TextComponent.newline()).append(TextComponent.of(stringValue));
        }
        appendValueText(builder, flag, display, hoverComp);
    }

    private void appendRegistryFlagValue(TextComponent.Builder builder, RegistryFlag<?> flag) {
        final Registry<?> registry = flag.getRegistry();
        String regName = registry.getName();
        Keyed currVal = region.getFlag(flag);
        if (currVal == null) {
            currVal = getInheritedValue(region, flag);
        }
        String display = currVal == null ? regName : currVal.id();
        appendValueText(builder, flag, display, null);
    }

    private void appendLocationFlagValue(TextComponent.Builder builder, LocationFlag flag) {
        Location currVal = region.getFlag(flag);
        if (currVal == null) {
            currVal = getInheritedValue(region, flag);
        }
        if (currVal == null) {
            final Location defVal = flag.getDefault();
            if (defVal == null) {
                appendValueText(builder, flag, "unset location", null);
            } else {
                appendValueText(builder, flag, defVal.toString(), TextComponent.of("Default value:")
                        .append(TextComponent.newline()).append(TextComponent.of(defVal.toString())));
            }
        } else {
            appendValueText(builder, flag, currVal.toString(), TextComponent.of("Current value:")
                    .append(TextComponent.newline()).append(TextComponent.of(currVal.toString())));
        }
    }

    private <V extends Number> void appendNumericFlagValue(TextComponent.Builder builder, NumberFlag<V> flag) {
        Number currVal = region.getFlag(flag);
        if (currVal == null) {
            currVal = getInheritedValue(region, flag);
        }
        Number defVal = flag.getDefault();
        Number[] suggested = flag.getSuggestedValues();
        SortedSet<Number> choices = new TreeSet<>(Comparator.comparing(Number::doubleValue));
        if (currVal != null) {
            choices.add(currVal);
        }
        if (defVal != null) {
            choices.add(defVal);
        }
        if (suggested.length > 0) {
            choices.addAll(Arrays.asList(suggested));
        }
        //noinspection unchecked
        appendValueChoices(builder, flag, (Iterator<V>) choices.iterator(), choices.isEmpty() ? "unset number" : "[custom]");
    }

    private void appendStringFlagValue(TextComponent.Builder builder, StringFlag flag) {
        String currVal = region.getFlag(flag);
        if (currVal == null) {
            currVal = getInheritedValue(region, flag);
        }
        if (currVal == null) {
            final String defVal = flag.getDefault();
            if (defVal == null) {
                appendValueText(builder, flag, "unset string", null);
            } else {
                final TextComponent defComp = LegacyComponentSerializer.INSTANCE.deserialize(defVal);
                String display = reduceToText(defComp);
                display = display.replace("\n", "\\n");
                if (display.length() > 23) {
                    display = display.substring(0, 20) + "...";
                }
                appendValueText(builder, flag, display, TextComponent.of("Default value:")
                        .append(TextComponent.newline()).append(defComp));
            }
        } else {
            TextComponent currComp = LegacyComponentSerializer.INSTANCE.deserialize(currVal);
            String display = reduceToText(currComp);
            display = display.replace("\n", "\\n");
            if (display.length() > 23) {
                display = display.substring(0, 20) + "...";
            }
            appendValueText(builder, flag, display, TextComponent.of("Current value:")
                    .append(TextComponent.newline()).append(currComp));
        }
    }

    private static String reduceToText(Component component) {
        StringBuilder text = new StringBuilder();
        appendTextTo(text, component);
        return text.toString();
    }

    private static void appendTextTo(StringBuilder builder, Component component) {
        if (component instanceof TextComponent) {
            builder.append(((TextComponent) component).content());
        } else if (component instanceof TranslatableComponent) {
            builder.append(((TranslatableComponent) component).key());
        }
        for (Component child : component.children()) {
            appendTextTo(builder, child);
        }
    }

    void tryMonoSpacing() {
        this.monoSpace = true;
    }

    private static final class FlagFontInfo {
        static int getPxLength(char c) {
            switch (c) {
                case 'i':
                case ':':
                    return 1;
                case 'l':
                    return 2;
                case '*':
                case 't':
                    return 3;
                case 'f':
                case 'k':
                    return 4;
                default:
                    return 5;
            }
        }

        static int getPxLength(String string) {
            return string.chars().reduce(0, (p, i) -> p + getPxLength((char) i) + 1);
        }
    }
}