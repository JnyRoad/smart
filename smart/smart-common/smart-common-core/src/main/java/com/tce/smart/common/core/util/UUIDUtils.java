package com.tce.smart.common.core.util;

import java.util.UUID;

/**
 * @Description: TODO
 * @ProjectName smart-yunan
 * @ClassName: UUIDUtils
 * @Author jinbo
 * @Date 2019/7/29
 */
public class UUIDUtils {

	public static String create(){
		return UUID.randomUUID().toString().replaceAll("-", StringUtils.EMPTY).toUpperCase();
	}
}
