package mg.framework.security;

public class SecurityResult {
    
    public enum Status {
        OK,             
        UNAUTHORIZED,   
        FORBIDDEN        
    }
    
    private final Status status;
    private final String redirectUrl;
    private final String message;
    private final String[] requiredRoles;
    
    private SecurityResult(Status status, String redirectUrl, String message, String[] requiredRoles) {
        this.status = status;
        this.redirectUrl = redirectUrl;
        this.message = message;
        this.requiredRoles = requiredRoles;
    }
 
    public static SecurityResult ok() {
        return new SecurityResult(Status.OK, null, null, null);
    }
    
    public static SecurityResult unauthorized(String redirectUrl, String message) {
        return new SecurityResult(Status.UNAUTHORIZED, redirectUrl, message, null);
    }
    
    public static SecurityResult forbidden(String redirectUrl, String message, String[] requiredRoles) {
        return new SecurityResult(Status.FORBIDDEN, redirectUrl, message, requiredRoles);
    }
    
    public boolean isAllowed() {
        return status == Status.OK;
    }
    
    public boolean isUnauthorized() {
        return status == Status.UNAUTHORIZED;
    }
    
    public boolean isForbidden() {
        return status == Status.FORBIDDEN;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String[] getRequiredRoles() {
        return requiredRoles;
    }
}
