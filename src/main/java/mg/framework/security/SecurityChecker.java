package mg.framework.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.framework.annotations.Authorized;
import mg.framework.annotations.Role;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SecurityChecker {
    
    public static SecurityResult checkSecurity(Method method, HttpServletRequest request) {
        
        SecurityConfig config = SecurityConfig.getInstance();
        HttpSession session = request.getSession(false);
        
        boolean hasAuthorized = method.isAnnotationPresent(Authorized.class);
        Role roleAnnotation = method.getAnnotation(Role.class);
        
        boolean requiresAuth = hasAuthorized || roleAnnotation != null;
        
        if (requiresAuth) {
            Object user = null;
            if (session != null) {
                user = session.getAttribute(config.getUserSessionName());
            }
            
            if (user == null) {
                return SecurityResult.unauthorized(
                    config.getLoginUrl(),
                    "Authentification requise pour accéder à cette ressource"
                );
            }
            
            if (roleAnnotation != null) {
                String[] requiredRoles = roleAnnotation.value();
                Set<String> userRoles = getUserRoles(session, config.getRoleSessionName());
                
                if (!hasAnyRole(userRoles, requiredRoles)) {
                    return SecurityResult.forbidden(
                        config.getForbiddenUrl(),
                        "Accès refusé. Rôles requis: " + Arrays.toString(requiredRoles),
                        requiredRoles
                    );
                }
            }
        }
        
        return SecurityResult.ok();
    }
    

    @SuppressWarnings("unchecked")
    private static Set<String> getUserRoles(HttpSession session, String roleSessionName) {
        Set<String> roles = new HashSet<>();
        
        if (session == null) {
            return roles;
        }
        
        Object rolesObj = session.getAttribute(roleSessionName);
        
        if (rolesObj == null) {
            return roles;
        }
        
        // Support différents types de stockage des rôles
        if (rolesObj instanceof String[]) {
            roles.addAll(Arrays.asList((String[]) rolesObj));
        } else if (rolesObj instanceof Collection) {
            for (Object role : (Collection<?>) rolesObj) {
                if (role != null) {
                    roles.add(role.toString());
                }
            }
        } else if (rolesObj instanceof String) {
            // Support d'un seul rôle comme String
            String roleStr = (String) rolesObj;
            // Support "admin,manager" format
            if (roleStr.contains(",")) {
                for (String r : roleStr.split(",")) {
                    roles.add(r.trim());
                }
            } else {
                roles.add(roleStr);
            }
        }
        
        return roles;
    }
    
    /**
     * Vérifie si l'utilisateur a au moins un des rôles requis.
     */
    private static boolean hasAnyRole(Set<String> userRoles, String[] requiredRoles) {
        for (String required : requiredRoles) {
            if (userRoles.contains(required)) {
                return true;
            }
        }
        return false;
    }
}
