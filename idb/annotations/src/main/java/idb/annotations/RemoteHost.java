package idb.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Mirko Köhler
 */
@Retention( RetentionPolicy.RUNTIME )
public @interface RemoteHost {
    public String description();
}
