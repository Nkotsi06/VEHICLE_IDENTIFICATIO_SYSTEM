package utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import dao.RolePermissionDAO;

/**
 * Utility class for managing user permissions and role-based access control.
 * Implements caching for performance and supports automatic cache refresh.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class PermissionUtil {

    private static final Logger LOGGER = Logger.getLogger(PermissionUtil.class.getName());

    // Cache configuration
    private static final Map<String, Map<String, Boolean>> permissionCache = new HashMap<>();
    private static long lastCacheRefresh = 0;
    private static final long CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(5); // 5 minutes

    // RolePermissionDAO instance (lazy initialization)
    private static RolePermissionDAO permissionDAO;

    // Admin role has all permissions
    private static final String ADMIN_ROLE = "ADMIN";

    private PermissionUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Checks if a role has a specific permission.
     * Admin role automatically has all permissions.
     *
     * @param role           the user's role
     * @param permissionKey  the permission key to check
     * @return true if the role has the permission, false otherwise
     */
    public static boolean hasPermission(String role, String permissionKey) {
        // Validate inputs
        if (role == null || role.trim().isEmpty()) {
            LOGGER.warning("Role is null or empty when checking permission: " + permissionKey);
            return false;
        }

        if (permissionKey == null || permissionKey.trim().isEmpty()) {
            LOGGER.warning("Permission key is null or empty when checking for role: " + role);
            return false;
        }

        // Admin has all permissions
        if (ADMIN_ROLE.equalsIgnoreCase(role)) {
            return true;
        }

        // Refresh cache if needed
        refreshCacheIfNeeded();

        // Get permissions for the role
        Map<String, Boolean> rolePermissions = permissionCache.get(role);
        if (rolePermissions == null) {
            loadPermissionsForRole(role);
            rolePermissions = permissionCache.get(role);
        }

        // Check if permission exists and is granted
        return rolePermissions != null &&
                rolePermissions.getOrDefault(permissionKey, false);
    }

    /**
     * Refreshes the permission cache if it has expired.
     */
    private static void refreshCacheIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCacheRefresh > CACHE_TTL_MS) {
            permissionCache.clear();
            lastCacheRefresh = now;
            LOGGER.fine("Permission cache refreshed (TTL expired)");
        }
    }

    /**
     * Loads permissions for a specific role from the database.
     *
     * @param role the role to load permissions for
     */
    private static void loadPermissionsForRole(String role) {
        Map<String, Boolean> permissions = new HashMap<>();

        try {
            ensurePermissionDAO();
            var perms = permissionDAO.findByRole(role);

            if (perms != null) {
                for (var perm : perms) {
                    if (perm != null && perm.getPermissionKey() != null) {
                        permissions.put(perm.getPermissionKey(), perm.hasPermission());
                    }
                }
            }

            permissionCache.put(role, permissions);
            LOGGER.fine("Loaded " + permissions.size() + " permissions for role: " + role);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load permissions for role: " + role, e);
            ErrorHandler.handleSQLException(e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error loading permissions for role: " + role, e);
            ErrorHandler.handleException(e);
        }
    }

    /**
     * Ensures the permissionDAO is initialized.
     */
    private static void ensurePermissionDAO() {
        if (permissionDAO == null) {
            permissionDAO = new RolePermissionDAO();
        }
    }

    /**
     * Manually refreshes the entire permission cache.
     * Call this after updating permissions in the database.
     */
    public static void refreshCache() {
        permissionCache.clear();
        lastCacheRefresh = System.currentTimeMillis();
        LOGGER.info("Permission cache manually refreshed");
    }

    /**
     * Refreshes cache for a specific role.
     *
     * @param role the role to refresh
     */
    public static void refreshRoleCache(String role) {
        if (role != null && !role.trim().isEmpty()) {
            permissionCache.remove(role);
            LOGGER.fine("Permission cache refreshed for role: " + role);
        }
    }

    /**
     * Gets all permissions for a role (cached).
     *
     * @param role the role to get permissions for
     * @return map of permission keys to boolean values, or empty map if error
     */
    public static Map<String, Boolean> getRolePermissions(String role) {
        if (role == null || role.trim().isEmpty()) {
            return new HashMap<>();
        }

        // Admin has all permissions - return a special marker
        if (ADMIN_ROLE.equalsIgnoreCase(role)) {
            Map<String, Boolean> adminPermissions = new HashMap<>();
            adminPermissions.put("*", true); // Wildcard for all permissions
            return adminPermissions;
        }

        refreshCacheIfNeeded();

        Map<String, Boolean> rolePermissions = permissionCache.get(role);
        if (rolePermissions == null) {
            loadPermissionsForRole(role);
            rolePermissions = permissionCache.get(role);
        }

        return rolePermissions != null ? new HashMap<>(rolePermissions) : new HashMap<>();
    }

    /**
     * Checks if a user can perform multiple permissions (AND logic).
     *
     * @param role             the user's role
     * @param permissionKeys   the permissions to check
     * @return true if the role has ALL specified permissions
     */
    public static boolean hasAllPermissions(String role, String... permissionKeys) {
        if (permissionKeys == null || permissionKeys.length == 0) {
            return true;
        }

        for (String key : permissionKeys) {
            if (!hasPermission(role, key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a user can perform any of multiple permissions (OR logic).
     *
     * @param role             the user's role
     * @param permissionKeys   the permissions to check
     * @return true if the role has ANY of the specified permissions
     */
    public static boolean hasAnyPermission(String role, String... permissionKeys) {
        if (permissionKeys == null || permissionKeys.length == 0) {
            return false;
        }

        for (String key : permissionKeys) {
            if (hasPermission(role, key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the cache status for debugging.
     *
     * @return map containing cache statistics
     */
    public static Map<String, Object> getCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("cacheSize", permissionCache.size());
        status.put("lastRefreshTime", lastCacheRefresh);
        status.put("timeSinceLastRefreshMs", System.currentTimeMillis() - lastCacheRefresh);
        status.put("cachedRoles", new ArrayList<>(permissionCache.keySet()));
        return status;
    }
}