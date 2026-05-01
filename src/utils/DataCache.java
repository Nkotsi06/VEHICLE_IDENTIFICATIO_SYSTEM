package utils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataCache {

    private static DataCache instance;
    private Map<String, CacheEntry> cache;

    private DataCache() {
        cache = new ConcurrentHashMap<>();
    }

    public static synchronized DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }
        return instance;
    }

    public void put(String key, Object value, long ttlSeconds) {
        CacheEntry entry = new CacheEntry(value, LocalDateTime.now().plusSeconds(ttlSeconds));
        cache.put(key, entry);
    }

    public Object get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && entry.expiryTime.isAfter(LocalDateTime.now())) {
            return entry.value;
        }
        cache.remove(key);
        return null;
    }

    public void remove(String key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }

    public boolean containsKey(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && entry.expiryTime.isAfter(LocalDateTime.now())) {
            return true;
        }
        cache.remove(key);
        return false;
    }

    public void evictExpired() {
        cache.entrySet().removeIf(entry -> entry.getValue().expiryTime.isBefore(LocalDateTime.now()));
    }

    private static class CacheEntry {
        Object value;
        LocalDateTime expiryTime;

        CacheEntry(Object value, LocalDateTime expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }
    }
}