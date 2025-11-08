package com.sk89q.worldguard.util.localization;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class Localization {

    private static final Localization EMPTY = new Localization(Collections.emptyMap());

    private final Map<String, String> messages;

    public Localization(Map<String, String> messages) {
        Objects.requireNonNull(messages, "messages");
        this.messages = Collections.unmodifiableMap(Map.copyOf(messages));
    }

    public static Localization empty() {
        return EMPTY;
    }

    public String get(String key) {
        Objects.requireNonNull(key, "key");
        return messages.getOrDefault(key, key);
    }

    public String format(String key, Object... arguments) {
        String template = get(key);
        if (arguments == null || arguments.length == 0) {
            return template;
        }
        return String.format(Locale.ROOT, template, arguments);
    }
}


