package com.tce.smart.data.util;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @Title: BaiduDwz
 * @Descripition: 百度短链接工具类
 * @Auther: guohongtai
 * @Version: 1.0
 * @Date: 2020-11-02 17:02
 */
@Slf4j
public class BaiduDwz {
	final static String CREATE_API = "https://dwz.cn/admin/v2/create";

	private static final String TOKEN_PROPERTY = "spring.baidu.dwz.token";

	private static final String TOKEN_ENV = "SPRING_BAIDU_DWZ_TOKEN";

	class UrlResponse {
		@SerializedName("Code")
		private int code;

		@SerializedName("ErrMsg")
		private String errMsg;

		@SerializedName("LongUrl")
		private String longUrl;

		@SerializedName("ShortUrl")
		private String shortUrl;

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getErrMsg() {
			return errMsg;
		}

		public void setErrMsg(String errMsg) {
			this.errMsg = errMsg;
		}

		public String getLongUrl() {
			return longUrl;
		}

		public void setLongUrl(String longUrl) {
			this.longUrl = longUrl;
		}

		public String getShortUrl() {
			return shortUrl;
		}

		public void setShortUrl(String shortUrl) {
			this.shortUrl = shortUrl;
		}
	}
	/**
	 * 创建短网址
	 *
	 * @param longUrl
	 *            长网址：即原网址
	 *        termOfValidity
	 *            有效期：默认值为long-term
	 * @return  成功：短网址
	 *          失败：返回空字符串
	 */
	public static String createShortUrl(String longUrl) {
		return createShortUrl(longUrl, getToken());
	}

	public static String createShortUrl(String longUrl, String token) {
		if (token == null || token.trim().isEmpty()) {
			throw new IllegalStateException("baidu dwz token is not configured");
		}
		String params = "{\"Url\":\""+ longUrl + "\",\"TermOfValidity\":\"1-year\"}";
		BufferedReader reader = null;
		try {
			// 创建连接
			URL url = new URL(CREATE_API);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestMethod("POST"); // 设置请求方式
			connection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
			connection.setRequestProperty("Token", token); // 设置发送数据的格式");

			// 发起请求
			connection.connect();
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8); // utf-8编码
			out.append(params);
			out.flush();
			out.close();

			// 读取响应
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
			String line;
			String res = "";
			while ((line = reader.readLine()) != null) {
				res += line;
			}
			reader.close();

			// 抽取生成短网址
			UrlResponse urlResponse = new Gson().fromJson(res, UrlResponse.class);
			if (urlResponse.getCode() == 0) {
				return urlResponse.getShortUrl();
			} else {
				log.error("Dwz======================" + urlResponse.getErrMsg() + "=====================" + urlResponse.getCode());
			}

			return ""; // TODO：自定义错误信息
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
		return ""; // TODO：自定义错误信息
	}

	private static String getToken() {
		String value = System.getProperty(TOKEN_PROPERTY);
		if (value != null && !value.trim().isEmpty()) {
			return value;
		}
		value = System.getenv(TOKEN_ENV);
		return value != null && !value.trim().isEmpty() ? value : null;
	}

	//public static void main(String[] args) {
	//	String res = createShortUrl("https://tech.szyuto.com/#/mobile/visitorCode?id=1326111231834365954");
	//	System.out.println(res);
	//}
}
