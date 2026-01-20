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
        
        Authorized authorizedAnnotation = method.getAnnotation(Authorized.class);
        Role roleAnnotation = method.getAnnotation(Role.class);
        
        boolean hasAuthorized = authorizedAnnotation != null;
        boolean requiresAuth = hasAuthorized || roleAnnotation != null;
        
        if (requiresAuth) {
            Object user = null;
            if (session != null) {
                user = session.getAttribute(config.getUserSessionName());
            }
            
            if (user == null) {
                String fallbackUrl = config.getLoginUrl();
                String message = "Authentification requise pour accéder à cette ressource";
                
                if (hasAuthorized) {
                    String annotationFallback = authorizedAnnotation.fallback();
                    String annotationMessageKey = authorizedAnnotation.messageKey();
                    
                    if (annotationFallback != null && !annotationFallback.isEmpty()) {
                        fallbackUrl = annotationFallback;
                    }
                    if (annotationMessageKey != null && !annotationMessageKey.isEmpty()) {
                        message = annotationMessageKey;
                    }
                } else if (roleAnnotation != null) {
                    String annotationFallback = roleAnnotation.fallback();
                    String annotationMessageKey = roleAnnotation.messageKey();
                    
                    if (annotationFallback != null && !annotationFallback.isEmpty()) {
                        fallbackUrl = annotationFallback;
                    }
                    if (annotationMessageKey != null && !annotationMessageKey.isEmpty()) {
                        message = annotationMessageKey;
                    }
                }
                
                return SecurityResult.unauthorized(fallbackUrl, message);
            }
            
            if (roleAnnotation != null) {
                String[] requiredRoles = roleAnnotation.value();
                Set<String> userRoles = getUserRoles(session, config.getRoleSessionName());
                
                if (!hasAnyRole(userRoles, requiredRoles)) {
                    // Déterminer l'URL de fallback pour forbidden (priorité: annotation > config globale)
                    String fallbackUrl = config.getForbiddenUrl();
                    String message = "Accès refusé. Rôles requis: " + Arrays.toString(requiredRoles);
                    
                    String annotationFallback = roleAnnotation.fallback();
                    String annotationMessageKey = roleAnnotation.messageKey();
                    
                    if (annotationFallback != null && !annotationFallback.isEmpty()) {
                        fallbackUrl = annotationFallback;
                    }
                    if (annotationMessageKey != null && !annotationMessageKey.isEmpty()) {
                        message = annotationMessageKey;
                    }
                    
                    return SecurityResult.forbidden(fallbackUrl, message, requiredRoles);
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
        
        if (rolesObj instanceof String[]) {
            roles.addAll(Arrays.asList((String[]) rolesObj));
        } else if (rolesObj instanceof Collection) {
            for (Object role : (Collection<?>) rolesObj) {
                if (role != null) {
                    roles.add(role.toString());
                }
            }
        } else if (rolesObj instanceof String) {
            String roleStr = (String) rolesObj;
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
