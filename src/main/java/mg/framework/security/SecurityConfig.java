package mg.framework.security;

import java.io.InputStream;
import java.util.Properties;

public class SecurityConfig {
    private static final String CONFIG_FILE = "application.properties";
    
    private static final String DEFAULT_USER_SESSION = "user";
    private static final String DEFAULT_ROLE_SESSION = "roles";
    private static final String DEFAULT_LOGIN_URL = "/login";
    private static final String DEFAULT_FORBIDDEN_URL = "/forbidden";
    
    private String userSessionName;
    private String roleSessionName;
    private String loginUrl;
    private String forbiddenUrl;
    
    private static SecurityConfig instance;
    
    private SecurityConfig() {
        loadConfig();
    }
    
    public static synchronized SecurityConfig getInstance() {
        if (instance == null) {
            instance = new SecurityConfig();
        }
        return instance;
    }
    
   
    public static void reload() {
        instance = new SecurityConfig();
    }
    
    private void loadConfig() {
        Properties props = new Properties();
        
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
        }
        
        userSessionName = props.getProperty("user_session", DEFAULT_USER_SESSION).trim();
        roleSessionName = props.getProperty("role_session", DEFAULT_ROLE_SESSION).trim();
        loginUrl = props.getProperty("login_url", DEFAULT_LOGIN_URL).trim();
        forbiddenUrl = props.getProperty("forbidden_url", DEFAULT_FORBIDDEN_URL).trim();
        
        userSessionName = removeQuotes(userSessionName);
        roleSessionName = removeQuotes(roleSessionName);
        loginUrl = removeQuotes(loginUrl);
        forbiddenUrl = removeQuotes(forbiddenUrl);
    }
    
    private String removeQuotes(String value) {
        if (value != null && value.length() >= 2) {
            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'"))) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
    
    public String getUserSessionName() {
        return userSessionName;
    }
    
    public String getRoleSessionName() {
        return roleSessionName;
    }
    
    public String getLoginUrl() {
        return loginUrl;
    }
    
    public String getForbiddenUrl() {
        return forbiddenUrl;
    }
}
