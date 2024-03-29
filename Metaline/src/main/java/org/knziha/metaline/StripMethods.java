package org.knziha.metaline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface StripMethods {
	boolean strip() default true;
	String[] keys() default {};
	
	boolean stripMethod() default false;
	
	int log() default 0;
	//String key() default "";
}
