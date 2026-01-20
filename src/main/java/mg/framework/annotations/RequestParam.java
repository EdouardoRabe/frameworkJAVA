package mg.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestParam {
    String value() default "";
    
    /**
     * Indique si le paramètre est requis.
     * Si false et que le paramètre n'est pas fourni, null sera injecté.
     */
    boolean required() default true;
}