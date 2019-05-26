/**
 * @annotation CHANGE
 * @author Andrei
 * @version 1.0	
 * @date 2019-03-21
 * @contact https://github.com/andrei-coelho
 * 
 * @descr
 * 
 * 
 */
package br.com.hodoor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CHANGE {
	String paramSet() default "DFAULT"; 
	String valueSet() default "DFAULT";
	String paramGet() default "DFAULT"; 
	String valueGet() default "DFAULT";
}
