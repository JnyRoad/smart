package com.tce.smart.bridge.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @Description: TODO
 * @ProjectName smart-hbase-core
 * @ClassName: Table
 * @Author jinbo
 * @Date 2019/10/16
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

	String name();
}