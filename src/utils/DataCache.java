package utils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Simple in-memory cache with TTL (Time-To-Live) support.
 * Automatically evicts expired entries.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class DataCache {

    private static DataCache instance;
    private Map<String, CacheEntry> cache;
    private ScheduledExecutorService cleanupExecutor;

    // Default TTL values
    public static final long DEFAULT_TTL_SECONDS = 300; // 5 minutes
    public static final long SHORT_TTL_SECONDS = 60;    // 1 minute
    public static final long LONG_TTL_SECONDS = 3600;   // 1 hour

    /**
     * Private constructor for singleton pattern.
     * Initializes the cache and starts a cleanup thread.
     */
    private DataCache() {
        cache = new ConcurrentHashMap<>();
        startCleanupScheduler();
    }

    /**
     * Gets the singleton instance of the cache.
     *
     * @return the cache instance
     */
    public static synchronized DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }
        return instance;
    }

    /**
     * Starts a scheduled task to clean expired entries every minute.
     */
    private void startCleanupScheduler() {
        cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "CacheCleanup");
            t.setDaemon(true);
            return t;
        });
        cleanupExecutor.scheduleAtFixedRate(this::evictExpired, 60, 60, TimeUnit.SECONDS);
    }

    /**
     * Stores a value in the cache with a specified TTL.
     *
     * @param key        the cache key
     * @param value      the value to store
     * @param ttlSeconds time-to-live in seconds
     */
    public void put(String key, Object value, long ttlSeconds) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }
        if (value == null) {
            remove(key);
            return;
        }

        LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(ttlSeconds);
        cache.put(key, new CacheEntry(value, expiryTime));
    }

    /**
     * Stores a value with the default TTL.
     *
     * @param key   the cache key
     * @param value the value to store
     */
    public void put(String key, Object value) {
        put(key, value, DEFAULT_TTL_SECONDS);
    }

    /**
     * Retrieves a value from the cache.
     * Returns null if the key doesn't exist or has expired.
     *
     * @param key the cache key
     * @return the cached value, or null if not found or expired
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry entry = cache.get(key);

        if (entry != null && !entry.isExpired()) {
            return (T) entry.getValue();
        }

        if (entry != null) {
            // Entry exists but is expired
            cache.remove(key);
        }

        return null;
    }

    /**
     * Removes a value from the cache.
     *
     * @param key the cache key
     */
    public void remove(String key) {
        cache.remove(key);
    }

    /**
     * Clears all entries from the cache.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Checks if a key exists in the cache and is not expired.
     *
     * @param key the cache key
     * @return true if the key exists and is valid, false otherwise
     */
    public boolean containsKey(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return true;
        }
        if (entry != null) {
            cache.remove(key);
        }
        return false;
    }

    /**
     * Gets the number of entries in the cache.
     *
     * @return the cache size
     */
    public int size() {
        return cache.size();
    }

    /**
     * Gets the TTL remaining for a key in seconds.
     *
     * @param key the cache key
     * @return seconds remaining, or -1 if key doesn't exist
     */
    public long getRemainingTTL(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return -1;
        }
        return entry.getRemainingSeconds();
    }

    /**
     * Removes all expired entries from the cache.
     */
    public void evictExpired() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * Shuts down the cache cleanup scheduler.
     * Call this when the application is closing.
     */
    public void shutdown() {
        if (cleanupExecutor != null && !cleanupExecutor.isShutdown()) {
            cleanupExecutor.shutdown();
            try {
                if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    cleanupExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                cleanupExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // ============================================
    // INNER CLASS - Cache Entry
    // ============================================

    /**
     * Represents a single cache entry with value and expiry time.
     */
    private static class CacheEntry {
        private final Object value;
        private final LocalDateTime expiryTime;

        CacheEntry(Object value, LocalDateTime expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }

        Object getValue() {
            return value;
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }

        long getRemainingSeconds() {
            if (isExpired()) return 0;
            return java.time.Duration.between(LocalDateTime.now(), expiryTime).getSeconds();
        }
    }
}