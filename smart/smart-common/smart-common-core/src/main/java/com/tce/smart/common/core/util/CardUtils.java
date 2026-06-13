package com.tce.smart.common.core.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Objects;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @ClassName CardUtils.java
 * @Author mingkai.wu
 * @Date 2019-05-08 09:40
 * @Description
 */
@Slf4j
@UtilityClass
public class CardUtils {

	/**
	 * 接收识别参数返回 String 的字符串用于后续加密等操作
	 */
	public static String setRecgnPlainParam(String imgBase64, String type, String option, String password) {
		return getOneLine(imgBase64) + "==##" + type + "==##" + option + "==##" + password;
	}

	private static String getOneLine(String withr) {
		StringReader sr = null;
		BufferedReader br = null;
		String line = null;
		StringBuilder temp = new StringBuilder();
		try {
			sr = new StringReader(withr);
			br = new BufferedReader(sr);
			while ((line = br.readLine()) != null) {
				temp.append(line);
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			//e.printStackTrace();
		} finally {
			if (Objects.nonNull(br)) {
				try {
					br.close();
				} catch (IOException e) {
					log.error(e.getMessage(),e);
					//e.printStackTrace();
				}
			}
			sr.close();
		}

		return temp.toString();
	}

	public static byte[] file2Byte(String filePath) {
		byte[] buffer = null;
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;
		try {
			File file = new File(filePath);
			fis = new FileInputStream(file);
			bos = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			buffer = bos.toByteArray();
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			//e.printStackTrace();
		} finally {
			if (Objects.nonNull(fis)) {
				try {
					fis.close();
				} catch (IOException e) {
					log.error(e.getMessage(),e);
					//e.printStackTrace();
				}
			}

			if (Objects.nonNull(bos)) {
				try {
					bos.close();
				} catch (IOException e) {
					log.error(e.getMessage(),e);
					//e.printStackTrace();
				}
			}
		}

		return buffer;
	}

	public static void byte2File(byte[] buf, String filePath, String fileName) {
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		File file = null;
		try {
			File dir = new File(filePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			file = new File(filePath + File.separator + fileName);
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(buf);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			//e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					log.error(e.getMessage(),e);
					//e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					log.error(e.getMessage(),e);
					// e.printStackTrace();
				}
			}
		}
	}
}
