package com.sk89q.worldguard.domains.registry;

public class CustomDomain {
    // Im not sure how to make this. It could be a simple "String" but maybe we need some other stuff like default
    // behavior
    private String name;
    private String description;
    private String registeredPlugin;

    public CustomDomain(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public CustomDomain(String customDomainId) {
        this(customDomainId, null);
    }

    public String getName() {
        return name;
    }
}
