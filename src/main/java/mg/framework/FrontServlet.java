package mg.framework;

import jakarta.servlet.ServletException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import mg.framework.annotations.RequestParam;
import mg.framework.model.ModelView;


@WebServlet(name = "FrontServlet", urlPatterns = {"/"}, loadOnStartup = 1)
public class FrontServlet extends HttpServlet {
    private mg.framework.registry.ControllerRegistry registry;

    @Override
    public void init() throws ServletException {
        super.init();
        Object attr = getServletContext().getAttribute(mg.framework.init.FrameworkInitializer.REGISTRY_ATTR);
        if (attr instanceof mg.framework.registry.ControllerRegistry) {
            this.registry = (mg.framework.registry.ControllerRegistry) attr;
            getServletContext().log("FrontServlet: registry loaded with " + this.registry.getExactRoutesSnapshot().size() + " exact routes");
        } else {
            getServletContext().log("FrontServlet: no ControllerRegistry found in ServletContext");
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        service(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        service(request, response);
    }
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();       
        String resourcePath = requestURI.substring(contextPath.length());

        if (registry != null) {
            java.util.List<mg.framework.registry.HandlerMethod> handlers = registry.findMatching(resourcePath, request.getMethod());
            if (handlers != null && !handlers.isEmpty()) {
                for (mg.framework.registry.HandlerMethod h : handlers) {
                    try {
                        Object controllerInstance = h.getControllerClass().getDeclaredConstructor().newInstance();
                        String handlerPath = h.getPath();
                        Object[] args = new Object[0];
                        java.lang.reflect.Parameter[] params = h.getMethod().getParameters();
                        if (handlerPath.contains("{")) {
                            List<String> vars = extractVarNames(handlerPath);
                            Pattern compiled = registry.getCompiledPattern(handlerPath);
                            if (compiled != null) {
                                Matcher matcher = compiled.matcher(resourcePath);
                                args = new Object[params.length];

                                if (matcher.matches()) {
                                    for (int i = 0; i < params.length; i++) {
                                        String paramName = params[i].getName();
                                        int varIndex = vars.indexOf(paramName);
                                        if (varIndex != -1) {
                                            String value = matcher.group(varIndex + 1);
                                            args[i] = convertValue(value, params[i].getType());
                                        } else {
                                            RequestParam requestParam = params[i].getAnnotation(RequestParam.class);
                                            if (requestParam != null) {
                                                String reqParamName = requestParam.value().isEmpty() ? paramName : requestParam.value();
                                                int pathVarIndex = vars.indexOf(reqParamName);
                                                if (pathVarIndex != -1) {
                                                    String value = matcher.group(pathVarIndex + 1);
                                                    args[i] = convertValue(value, params[i].getType());
                                                } else {
                                                    String value = request.getParameter(reqParamName);
                                                    if (value != null) {
                                                        args[i] = convertValue(value, params[i].getType());
                                                    } else if (params[i].getType().isPrimitive()) {
                                                        args[i] = getDefaultValue(params[i].getType());
                                                    } else if (isMapStringObject(params[i])) {
                                                        args[i] = request.getParameterMap();
                                                    }
                                                }
                                            } else if (params[i].getType().isPrimitive()) {
                                                args[i] = getDefaultValue(params[i].getType());
                                            } else if (isMapStringObject(params[i])) {
                                                args[i] = request.getParameterMap();
                                            }
                                        }
                                    }
                                    // Binder les objets personnalisés
                                    Map<String, Object> customObjects = bindCustomObjects(params, request);
                                    for (int i = 0; i < params.length; i++) {
                                        if (args[i] == null && isCustomObject(params[i].getType())) {
                                            args[i] = customObjects.get(params[i].getName());
                                        }
                                    }
                                } 
                            }
                        } else {
                            args = new Object[params.length];
                            for (int i = 0; i < params.length; i++) {
                                String sourceName = params[i].getName();
                                RequestParam requestParam = params[i].getAnnotation(RequestParam.class);
                                if (requestParam != null && !requestParam.value().isEmpty()) {
                                    sourceName = requestParam.value();
                                }
                                String value = request.getParameter(sourceName);
                                if (value != null) {
                                    args[i] = convertValue(value, params[i].getType());
                                } else if (params[i].getType().isPrimitive()) {
                                    args[i] = getDefaultValue(params[i].getType());
                                } else if (isMapStringObject(params[i])) {
                                    args[i] = request.getParameterMap();
                                }
                            }
                            // Binder les objets personnalisés
                            Map<String, Object> customObjects = bindCustomObjects(params, request);
                            for (int i = 0; i < params.length; i++) {
                                if (args[i] == null && isCustomObject(params[i].getType())) {
                                    args[i] = customObjects.get(params[i].getName());
                                }
                            }
                        }
                        Object result = h.getMethod().invoke(controllerInstance, args);
                        if (result instanceof String) {
                            response.getWriter().println((String) result);
                        } else if (result instanceof ModelView) {
                            ModelView mv = (ModelView) result;
                            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/" + mv.getView());
                            if (dispatcher != null) {
                                for (java.util.Map.Entry<String, Object> entry : mv.getAttributes().entrySet()) {
                                    request.setAttribute(entry.getKey(), entry.getValue());
                                }
                                dispatcher.forward(request, response);
                            } else {
                                response.getWriter().println("View not found: " + mv.getView());
                            }
                        } else {
                            response.getWriter().println("Unsupported return type: " + result.getClass().getName());
                        }
                    } catch (Exception e) {
                        response.getWriter().println("Error invoking method: " + e.getMessage());
                    }
                }
                return;
            }
        }

        try {
            java.net.URL resource = getServletContext().getResource(resourcePath);
            if (resource != null) {
                if (resourcePath.endsWith(".jsp")) {
                    RequestDispatcher jspDispatcher = getServletContext().getRequestDispatcher(resourcePath);
                    if (jspDispatcher != null) {
                        jspDispatcher.forward(request, response);
                        return;
                    }
                }
                RequestDispatcher defaultServlet = getServletContext().getNamedDispatcher("default");
                if (defaultServlet != null) {
                    defaultServlet.forward(request, response);
                    return;
                }
            }
        } catch (Exception e) {
            throw new ServletException("Erreur lors de la vérification de la ressource: " + resourcePath, e);
        }

        response.getWriter().println("Ressource non trouvée: " + resourcePath);

    }

    private List<String> extractVarNames(String pattern) {
        List<String> vars = new ArrayList<>();
        Pattern p = Pattern.compile("\\{([^}]+)\\}");
        Matcher m = p.matcher(pattern);
        while (m.find()) {
            vars.add(m.group(1));
        }
        return vars;
    }

    private Object convertValue(String value, Class<?> type) {
        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(value);
        } else if (type == char.class || type == Character.class) {
            return value.length() > 0 ? value.charAt(0) : '\0';
        } else if (type == byte.class || type == Byte.class) {
            return Byte.parseByte(value);
        } else if (type == short.class || type == Short.class) {
            return Short.parseShort(value);
        } else if (type == String.class) {
            return value;
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    private Map<String, Object> bindCustomObjects(java.lang.reflect.Parameter[] methodParams, HttpServletRequest request) {
        Map<String, Object> boundObjects = new java.util.HashMap<>();
        Map<String, String[]> paramMap = request.getParameterMap();

        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String paramName = entry.getKey();
            if (paramName.contains(".")) {
                String[] parts = paramName.split("\\.", 2);
                if (parts.length == 2) {
                    String objectName = parts[0];
                    String propertyName = parts[1];
                    String[] values = entry.getValue();
                    String value = values.length > 0 ? values[0] : null;

                    // Trouver le paramètre correspondant
                    for (java.lang.reflect.Parameter param : methodParams) {
                        if (param.getName().equals(objectName) && isCustomObject(param.getType())) {
                            Object instance = boundObjects.get(objectName);
                            if (instance == null) {
                                try {
                                    instance = param.getType().getDeclaredConstructor().newInstance();
                                    boundObjects.put(objectName, instance);
                                } catch (Exception e) {
                                    // Ignore si pas de constructeur par défaut
                                    continue;
                                }
                            }
                            // Assigner la propriété
                            setProperty(instance, propertyName, value);
                        }
                    }
                }
            }
        }
        return boundObjects;
    }

    private boolean isCustomObject(Class<?> type) {
        return !type.isPrimitive() && !type.equals(String.class) && !type.equals(Map.class) && !type.getName().startsWith("java.");
    }

    private void setProperty(Object instance, String propertyName, String value) {
        try {
            // Find field type if exists
            Class<?> clazz = instance.getClass();
            java.lang.reflect.Field field = null;
            try {
                field = clazz.getDeclaredField(propertyName);
            } catch (NoSuchFieldException nsf) {
                // It's okay if field not found; we can still try setter
            }

            // Essayer le setter (match by name and parameter count 1)
            String setterName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
            Method setter = null;
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(setterName) && m.getParameterCount() == 1) {
                    setter = m;
                    break;
                }
            }
            if (setter != null) {
                Class<?> paramType = setter.getParameterTypes()[0];
                Object converted = value == null ? null : convertValue(value, paramType);
                setter.invoke(instance, converted);
                return;
            }

            // Essayer le champ
            if (field != null) {
                field.setAccessible(true);
                if (value == null) {
                    field.set(instance, null);
                } else {
                    field.set(instance, convertValue(value, field.getType()));
                }
            }
        } catch (Exception e) {
            // Ignore si pas de setter ou champ ou conversion fail
        }
    }

    private Object getDefaultValue(Class<?> type) {
        if (type == int.class || type == Integer.class) {
            return 0;
        } else if (type == double.class || type == Double.class) {
            return 0.0;
        } else if (type == boolean.class || type == Boolean.class) {
            return false;
        } else if (type == long.class || type == Long.class) {
            return 0L;
        } else if (type == float.class || type == Float.class) {
            return 0.0f;
        } else if (type == char.class || type == Character.class) {
            return '\0';
        } else if (type == byte.class || type == Byte.class) {
            return (byte) 0;
        } else if (type == short.class || type == Short.class) {
            return (short) 0;
        }
        return null;
    }

    private boolean isMapStringObject(java.lang.reflect.Parameter parameter) {
        Type type = parameter.getParameterizedType();
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            if (pt.getRawType().equals(Map.class)) {
                Type[] args = pt.getActualTypeArguments();
                return args.length == 2 && args[0].equals(String.class) && args[1].equals(Object.class);
            }
        }
        return false;
    }
}