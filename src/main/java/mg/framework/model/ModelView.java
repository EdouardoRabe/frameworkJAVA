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

    public ModelView() {
    }

    public ModelView(String view) {
        this.view = view;
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