package mg.framework.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ModelView {
    private String view;
    private HashMap<String, Object> attributes = new HashMap<>();
    
    private HashMap<String, Object> sessionAttributes = new HashMap<>();
    private Set<String> sessionAttributesToRemove = new HashSet<>();
    private boolean invalidateSession = false;
    private boolean redirect = false;
    private String redirectUrl = null;
    private boolean loginSuccess = false;
    private String defaultRedirectAfterLogin = null;

    public ModelView() {
    }

    public ModelView(String view) {
        this.view = view;
    }
 
    public static ModelView redirect(String url) {
        ModelView mv = new ModelView();
        mv.redirect = true;
        mv.redirectUrl = url;
        return mv;
    }
    
    public boolean isRedirect() {
        return redirect;
    }
    
    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }
    
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
        this.redirect = true;
    }
 
    public static ModelView loginSuccess(String defaultRedirect) {
        ModelView mv = new ModelView();
        mv.loginSuccess = true;
        mv.defaultRedirectAfterLogin = defaultRedirect;
        return mv;
    }

    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    public String getDefaultRedirectAfterLogin() {
        return defaultRedirectAfterLogin;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public HashMap<String, Object> getAttributes() {
        return attributes;
    }
    public void setAttributes(HashMap<String, Object> attributesHashMap) {
        this.attributes = attributesHashMap;
    }

    public void addAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

 
    public void addSessionAttribute(String key, Object value) {
        this.sessionAttributes.put(key, value);
        this.sessionAttributesToRemove.remove(key);
    }

    public void removeSessionAttribute(String key) {
        this.sessionAttributesToRemove.add(key);
        this.sessionAttributes.remove(key);
    }

   
    public void invalidateSession() {
        this.invalidateSession = true;
    }


    public HashMap<String, Object> getSessionAttributes() {
        return sessionAttributes;
    }

    public Set<String> getSessionAttributesToRemove() {
        return sessionAttributesToRemove;
    }

    public boolean isInvalidateSession() {
        return invalidateSession;
    }
}