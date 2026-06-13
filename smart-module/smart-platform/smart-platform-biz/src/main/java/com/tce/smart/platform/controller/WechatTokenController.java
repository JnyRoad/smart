package com.tce.smart.platform.controller;

import com.tce.smart.common.core.wrapper.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * 显示信息
 */
@Slf4j
@RestController
@RequestMapping("/wechat")
public class WechatTokenController extends BaseController {

	@Value("${wechat.token:}")
	private String token;

    @GetMapping("/token")
    public String token(@RequestParam String signature,@RequestParam String timestamp,@RequestParam String nonce,@RequestParam String echostr){

	log.debug("微信token认证,{}",echostr);
		if (token == null || token.trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "wechat token is not configured");
		}
		if (!checkSignature(signature, timestamp, nonce)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid wechat signature");
		}
        return echostr;
    }

	@GetMapping("/openid")
	public String getOpenid(HttpServletRequest request){
		log.debug("wechat openid callback received");
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		try
		{
			br = request.getReader();
			String str;
			while ((str = br.readLine()) != null)
			{
				sb.append(str);
			}
			br.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (null != br)
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		log.debug("wechat openid callback body read");
		return sb.toString();
	}

	private boolean checkSignature(String signature, String timestamp, String nonce) {
		String[] values = new String[]{token, timestamp, nonce};
		Arrays.sort(values);
		return sha1(values[0] + values[1] + values[2]).equalsIgnoreCase(signature);
	}

	private String sha1(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder builder = new StringBuilder(bytes.length * 2);
			for (byte item : bytes) {
				String hex = Integer.toHexString(item & 0xff);
				if (hex.length() == 1) {
					builder.append('0');
				}
				builder.append(hex);
			}
			return builder.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "SHA-1 is not available", e);
		}
	}
}
