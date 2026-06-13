package com.tce.smart.common.core.util;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: TODO
 * @ProjectName smart
 * @ClassName: HttpUtils
 * @Author jinbo
 * @Date 2019/5/9
 */
@Slf4j
public class HttpUtils extends HttpUtil{
	public static <T> T parse(HttpResponse response, Class<T> responseType) {
		String body = response.body();
		if(response.isOk()){
			return JSONUtil.toBean(body, responseType);
		}
		log.warn("响应异常：{}  {}", response.getStatus(), body);
		return null;
	}
}
