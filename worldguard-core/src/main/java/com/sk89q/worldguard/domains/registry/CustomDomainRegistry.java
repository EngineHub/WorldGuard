package com.sk89q.worldguard.domains.registry;

import com.sk89q.worldguard.protection.flags.Flag;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CustomDomainRegistry extends Iterable<CustomDomain> {

    /**
     * Register a new custom Domain.
     *
     * <p>There may be an appropriate time to register this. If domains are
     * registered outside this time, then an exception may be thrown.</p>
     *
     * @param customDomain the domain
     * @throws CustomDomainConflictException Thrown when an existing domain exists with the same name
     * @throws IllegalStateException If it is not the right time to register new domains
     */
    void register(CustomDomain customDomain) throws CustomDomainConflictException;

    /**
     * Register a collection of domains.
     *
     * <p>There may be an appropriate time to register domains. If domains are
     * registered outside this time, then an exception may be thrown.</p>
     *
     * <p>If there is a domain conflict, then an error will be logged but
     * no exception will be thrown.</p>
     *
     * @param customDomains a collection of flags
     * @throws IllegalStateException If it is not the right time to register new flags
     */
    void registerAll(Collection<CustomDomain> customDomains);

    /**
     * Get a domain by its name.
     *
     * @param name The name
     * @return The domain, if it has been registered
     */
    @Nullable
    CustomDomain get(String name);

    /**
     * Get all domains
     *
     * @return All domains
     */
    List<CustomDomain> getAll();

    /*
     * Unmarshal a raw map of values into a map of flags with their
     * unmarshalled values.
     *
     * @param rawValues The raw values map
     * @param createUnknown Whether "just in time" flags should be created for unknown flags
     * @return The unmarshalled flag values map
     */
//    Map<Flag<?>, Object> unmarshal(Map<String, Object> rawValues, boolean createUnknown);

    /**
     * Get the number of registered flags.
     *
     * @return The number of registered flags
     */
    int size();
}
