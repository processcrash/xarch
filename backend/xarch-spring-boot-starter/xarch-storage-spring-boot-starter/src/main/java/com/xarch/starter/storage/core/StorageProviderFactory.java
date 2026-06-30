package com.xarch.starter.storage.core;

import com.xarch.starter.storage.StorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the appropriate {@link StorageProvider} for a given
 * {@link StorageType}.
 * <p>
 * All {@link StorageProvider} beans contributed by the various auto-config
 * classes are registered here, indexed by their type. Lookups fall back to
 * the default configured backend if the requested type is not available.
 * </p>
 */
public class StorageProviderFactory {

    private static final Logger log = LoggerFactory.getLogger(StorageProviderFactory.class);

    private final Map<StorageType, StorageProvider> providers = new EnumMap<>(StorageType.class);
    private final StorageProperties properties;

    /**
     * Create a new factory.
     *
     * @param providers all available storage providers in the context
     * @param properties the storage configuration properties
     */
    public StorageProviderFactory(List<StorageProvider> providers, StorageProperties properties) {
        this.properties = properties;
        for (StorageProvider provider : providers) {
            this.providers.put(provider.getType(), provider);
        }
        log.info("Initialised StorageProviderFactory with providers: {}", this.providers.keySet());
    }

    /**
     * Return the provider for the given type, or the default provider if the
     * requested type is not registered.
     *
     * @param type the requested storage type
     * @return a non-null {@link StorageProvider}
     * @throws StorageException if neither the requested type nor the default
     *     backend has a registered provider
     */
    public StorageProvider getProvider(StorageType type) {
        StorageProvider provider = providers.get(type);
        if (provider != null) {
            return provider;
        }
        StorageType fallback = properties.resolvedDefaultType();
        provider = providers.get(fallback);
        if (provider == null) {
            throw new StorageException("No storage provider registered for type " + type
                    + " (fallback " + fallback + " is also missing)");
        }
        log.warn("Requested storage type {} is not available, falling back to {}", type, fallback);
        return provider;
    }

    /**
     * Return the default provider configured via
     * {@link StorageProperties#getDefaultType()}.
     *
     * @return a non-null {@link StorageProvider}
     */
    public StorageProvider getDefaultProvider() {
        return getProvider(properties.resolvedDefaultType());
    }

    /**
     * Convenience wrapper around {@link #getProvider(StorageType)} that
     * accepts a string code.
     *
     * @param code the storage type code (case-insensitive)
     * @return a non-null {@link StorageProvider}
     */
    public StorageProvider getProvider(String code) {
        return getProvider(StorageType.fromCode(code));
    }

    /**
     * Return true if a provider for the given type is registered.
     *
     * @param type the storage type
     * @return true if a provider is available
     */
    public boolean hasProvider(StorageType type) {
        return providers.containsKey(type);
    }

    /**
     * Return an immutable snapshot of all registered providers.
     *
     * @return map of type to provider
     */
    public Map<StorageType, StorageProvider> getAllProviders() {
        return Map.copyOf(providers);
    }
}
