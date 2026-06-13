package com.tce.smart.bridge.annotation;

import java.lang.annotation.*;

/**
 * @Description: TODO
 * @ProjectName smart-hbase-core
 * @ClassName: Family
 * @Author jinbo
 * @Date 2019/10/16
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface Key {

}