package utils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import dao.RolePermissionDAO;

public class PermissionUtil {

    private static Map<String, Map<String, Boolean>> permissionCache = new HashMap<>();
    private static RolePermissionDAO permissionDAO = new RolePermissionDAO();
    private static long lastCacheRefresh = 0;
    private static final long CACHE_TTL = 300000;

    public static boolean hasPermission(String role, String permissionKey) {
        if (role == null || permissionKey == null) return false;

        if ("ADMIN".equals(role)) return true;

        refreshCacheIfNeeded();

        Map<String, Boolean> rolePermissions = permissionCache.get(role);
        if (rolePermissions == null) {
            loadPermissionsForRole(role);
            rolePermissions = permissionCache.get(role);
        }

        return rolePermissions != null && rolePermissions.getOrDefault(permissionKey, false);
    }

    private static void refreshCacheIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCacheRefresh > CACHE_TTL) {
            permissionCache.clear();
            lastCacheRefresh = now;
        }
    }

    private static void loadPermissionsForRole(String role) {
        try {
            Map<String, Boolean> permissions = new HashMap<>();
            var perms = permissionDAO.findByRole(role);
            for (var perm : perms) {
                permissions.put(perm.getPermissionKey(), perm.hasPermission());
            }
            permissionCache.put(role, permissions);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void refreshCache() {
        permissionCache.clear();
        lastCacheRefresh = System.currentTimeMillis();
    }
}