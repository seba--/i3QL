package saere.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to specify which goals should be measured. The corresponding goals must not
 * have any arguments.
 * 
 * @author Michael Eichberg
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface Benchmark {

	String goal() default "benchmark";

}
