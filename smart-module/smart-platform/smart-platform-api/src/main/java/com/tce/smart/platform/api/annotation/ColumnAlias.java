package com.tce.smart.platform.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description: TODO
 * @ProjectName smart
 * @ClassName: s
 * @Author jinbo
 * @Date 2019/10/16
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ColumnAlias {

	String value() default "";
}