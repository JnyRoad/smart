package com.tce.smart.bridge.isc.util;

import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.Method;

/**
 * @Description: TODO
 * @ProjectName smart
 * @ClassName: ReflectUtils
 * @Author jinbo
 * @Date 2019/10/17
 */
public class ReflectUtils extends ReflectUtil {
	/**
	 * 根据属性，获取get方法
	 * @param t 对象
	 * @param name 属性名
	 * @return
	 * @throws Exception
	 */
	public static <T>Object getGetMethod(T t, String name)throws Exception{
		Method[] m = t.getClass().getMethods();
		for (Method method : m) {
			if (("get" + name).equalsIgnoreCase(method.getName())) {
				return method.invoke(t);
			}
		}
		return null;
	}
}
