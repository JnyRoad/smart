package com.tce.smart.app.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author sunfujian
 * @since 2021/10/11 18:08
 */
public class SignHelper {
	/**
	 * 按参数名排序后依次拼接参数名称与数值，之后对该字符串使用 HmacSHA256 加签，加签结果进行 base 64 返回
	 * @param params 请求参数 map
	 * @param suiteSecret 套件密钥，用作 mac key
	 * @return 签名
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 * @throws InvalidKeyException
	 */
	public static String sign(Map<String, String> params, String suiteSecret) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
		// use tree map to sort params by name
		Map<String, String> treeMap;
		if (params instanceof TreeMap) {
			treeMap = params;
		} else {
			treeMap = new TreeMap<>(params);
		}

		StringBuilder stringBuilder = new StringBuilder();
		for (Map.Entry<String, String> entry : treeMap.entrySet()) {
			stringBuilder.append(entry.getKey()).append(entry.getValue());
		}

		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(suiteSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		byte[] signData = mac.doFinal(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
		String base64String = Base64.getEncoder().encodeToString(signData);
		return URLEncoder.encode(base64String, "UTF-8");
	}
}
