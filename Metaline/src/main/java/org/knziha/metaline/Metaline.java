package org.knziha.metaline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.SOURCE)
public @interface Metaline {
	boolean trim() default true;
	
	int flagPos() default 0;
	int flagSize() default 1;
	int shift() default 0;
	int elevation() default 0;
	int max() default 0;
	int debug() default -1;
	
	String file() default "";
	String to() default "";
	String charset() default "";
	
	boolean compile() default false;
	
	int log() default 0;
}
