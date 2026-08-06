package com.tce.smart.common.core.security;

import java.util.Arrays;

/**
 * 仅面向 IP literal 的 CIDR 匹配器。拒绝 DNS 名称，避免运行期 DNS 解析改变
 * Gateway 调用方白名单的安全边界。
 */
public final class LegacyCompatibilityCidr {

	private final byte[] network;
	private final int prefixLength;

	private LegacyCompatibilityCidr(byte[] network, int prefixLength) {
		this.network = network;
		this.prefixLength = prefixLength;
	}

	public static LegacyCompatibilityCidr parse(String value) {
		if (value == null) {
			throw new IllegalArgumentException("CIDR 不能为空");
		}
		String normalized = value.trim();
		int separator = normalized.lastIndexOf('/');
		if (separator <= 0 || separator != normalized.indexOf('/') || separator == normalized.length() - 1) {
			throw new IllegalArgumentException("CIDR 格式非法");
		}
		byte[] address = parseLiteral(normalized.substring(0, separator));
		int prefixLength;
		try {
			prefixLength = Integer.parseInt(normalized.substring(separator + 1));
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException("CIDR 前缀非法", exception);
		}
		if (prefixLength < 0 || prefixLength > address.length * Byte.SIZE) {
			throw new IllegalArgumentException("CIDR 前缀超出地址范围");
		}
		return new LegacyCompatibilityCidr(mask(address, prefixLength), prefixLength);
	}

	public boolean matches(String sourceIp) {
		try {
			byte[] candidate = parseLiteral(sourceIp);
			return candidate.length == network.length && Arrays.equals(network, mask(candidate, prefixLength));
		} catch (IllegalArgumentException exception) {
			// 调用方可控的来源字符串非法时，白名单必须失败关闭而不是抛出 500。
			return false;
		}
	}

	/** 仅当 CIDR 精确到单个 IPv4/IPv6 主机时返回 true。 */
	public boolean isHostCidr() {
		return prefixLength == network.length * Byte.SIZE;
	}

	private static byte[] parseLiteral(String value) {
		if (value == null || value.isEmpty() || !value.equals(value.trim())) {
			throw new IllegalArgumentException("仅支持 IP literal");
		}
		if (value.indexOf(':') >= 0) {
			return parseIpv6(value);
		}
		return parseIpv4(value);
	}

	/**
	 * 仅接受四段十进制 IPv4。拒绝 Java InetAddress 会宽松解释的缩写、八进制样式
	 * 和主机名，避免配置白名单被 DNS 或地址格式差异改变。
	 */
	private static byte[] parseIpv4(String value) {
		String[] parts = value.split("\\.", -1);
		if (parts.length != 4) {
			throw new IllegalArgumentException("IPv4 literal 非法");
		}
		byte[] output = new byte[4];
		for (int index = 0; index < parts.length; index++) {
			String part = parts[index];
			if (part.isEmpty() || part.length() > 3 || (part.length() > 1 && part.charAt(0) == '0')) {
				throw new IllegalArgumentException("IPv4 literal 非法");
			}
			int number = 0;
			for (int characterIndex = 0; characterIndex < part.length(); characterIndex++) {
				char character = part.charAt(characterIndex);
				if (character < '0' || character > '9') {
					throw new IllegalArgumentException("IPv4 literal 非法");
				}
				number = number * 10 + character - '0';
			}
			if (number > 255) {
				throw new IllegalArgumentException("IPv4 literal 非法");
			}
			output[index] = (byte) number;
		}
		return output;
	}

	/**
	 * 仅接受 RFC 4291 的十六进制 IPv6 literal。为消除 IPv4/IPv6 家族混淆，拒绝
	 * 内嵌 IPv4 形式（例如 ::ffff:10.0.0.1）；Gateway 对该请求应按未授权处理。
	 */
	private static byte[] parseIpv6(String value) {
		if (value.indexOf('.') >= 0 || value.indexOf('%') >= 0) {
			throw new IllegalArgumentException("IPv6 literal 非法");
		}
		int compressionIndex = value.indexOf("::");
		if (compressionIndex >= 0 && value.indexOf("::", compressionIndex + 2) >= 0) {
			throw new IllegalArgumentException("IPv6 literal 非法");
		}
		int[] words = new int[8];
		if (compressionIndex < 0) {
			int[] parsed = parseIpv6Words(value);
			if (parsed.length != words.length) {
				throw new IllegalArgumentException("IPv6 literal 非法");
			}
			System.arraycopy(parsed, 0, words, 0, words.length);
		} else {
			String[] sides = value.split("::", -1);
			if (sides.length != 2) {
				throw new IllegalArgumentException("IPv6 literal 非法");
			}
			int[] left = parseIpv6Words(sides[0]);
			int[] right = parseIpv6Words(sides[1]);
			if (left.length + right.length >= words.length) {
				throw new IllegalArgumentException("IPv6 literal 非法");
			}
			System.arraycopy(left, 0, words, 0, left.length);
			System.arraycopy(right, 0, words, words.length - right.length, right.length);
		}
		byte[] output = new byte[16];
		for (int index = 0; index < words.length; index++) {
			output[index * 2] = (byte) (words[index] >>> 8);
			output[index * 2 + 1] = (byte) words[index];
		}
		return output;
	}

	private static int[] parseIpv6Words(String value) {
		if (value.isEmpty()) {
			return new int[0];
		}
		String[] parts = value.split(":", -1);
		int[] words = new int[parts.length];
		for (int index = 0; index < parts.length; index++) {
			String part = parts[index];
			if (part.isEmpty() || part.length() > 4) {
				throw new IllegalArgumentException("IPv6 literal 非法");
			}
			int word = 0;
			for (int characterIndex = 0; characterIndex < part.length(); characterIndex++) {
				int digit = hexDigit(part.charAt(characterIndex));
				if (digit < 0) {
					throw new IllegalArgumentException("IPv6 literal 非法");
				}
				word = word * 16 + digit;
			}
			words[index] = word;
		}
		return words;
	}

	private static int hexDigit(char character) {
		if (character >= '0' && character <= '9') {
			return character - '0';
		}
		if (character >= 'a' && character <= 'f') {
			return character - 'a' + 10;
		}
		if (character >= 'A' && character <= 'F') {
			return character - 'A' + 10;
		}
		return -1;
	}

	private static byte[] mask(byte[] address, int prefixLength) {
		byte[] masked = Arrays.copyOf(address, address.length);
		int fullBytes = prefixLength / Byte.SIZE;
		int remainder = prefixLength % Byte.SIZE;
		if (fullBytes < masked.length && remainder > 0) {
			masked[fullBytes] = (byte) (masked[fullBytes] & (0xFF << (Byte.SIZE - remainder)));
			fullBytes++;
		}
		for (int index = fullBytes; index < masked.length; index++) {
			masked[index] = 0;
		}
		return masked;
	}
}
