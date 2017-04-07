package com.denghb.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表
 * 
 * @author denghb
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
	/**
	 * 表名
	 */
	String name();

	/**
	 * 数据库
	 */
	String database() default "";
}
