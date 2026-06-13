package com.tce.smart.tool.util;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.util.ArrayUtil;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Base64Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * 图片工具类
 *
 * @author mckaywu
 * @date 2019-05-29 13:33:19
 */
@Slf4j
public class ImageUtils {

	/**
	 * png图片
	 */
	public final static String IMAGE_TYPE_PNG = "data:image/png;base64,";

	/**
	 * jpeg图片
	 */
	public final static String IMAGE_TYPE_JPEG = "data:image/jpeg;base64,";

	/**
	 * gif图片
	 */
	public final static String IMAGE_TYPE_GIF = "data:image/gif;base64,";

	/**
	 * jpg图片
	 */
	public final static String IMAGE_TYPE_JPG = "data:image/jpg;base64,";

	/**
	 * 缩略图宽度
	 */
	public final static int SMALL_FIXED_SIZE_W = 160;

	/**
	 * 缩略图高度
	 */
	public final static int SMALL_FIXED_SIZE_H = 160;

	/**
	 * 拼接完整的图片base64字符串
	 *
	 * @param imageBase64Str base64字符串
	 * @param imageTypeSrc   图片类型
	 * @return
	 */
	public static String changeFullBase64(String imageBase64Str, String imageTypeSrc) {
		String fullImage = imageBase64Str;
		if (StringUtils.isBlank(imageBase64Str)) {
			return fullImage;
		}

		String imageType = null;
		if (StringUtils.isNotBlank(imageTypeSrc)) {
			imageType = imageTypeSrc;
		} else {
			imageType = IMAGE_TYPE_JPG;
		}

		int index = imageBase64Str.indexOf(",");
		if (index > 0) {
			String iamgeBase64Prefix = imageBase64Str.substring(0, index);
			if (!(StringUtils.containsIgnoreCase(IMAGE_TYPE_PNG, iamgeBase64Prefix)
					|| StringUtils.containsIgnoreCase(IMAGE_TYPE_JPEG, iamgeBase64Prefix)
					|| StringUtils.containsIgnoreCase(IMAGE_TYPE_GIF, iamgeBase64Prefix)
					|| StringUtils.containsIgnoreCase(IMAGE_TYPE_JPG, iamgeBase64Prefix))) {

				fullImage = imageType + imageBase64Str;
			}
		} else {
			fullImage = imageType + imageBase64Str;
		}

		return fullImage;
	}

	/**
	 * 拼接完整的图片base64字符串
	 *
	 * @param imageBase64Str
	 * @return
	 */
	public static String changeFullBase64(String imageBase64Str) {

		return changeFullBase64(imageBase64Str, IMAGE_TYPE_JPG);

	}

	/**
	 * 拼接完整的图片base64字符串
	 *
	 * @param imageByte 图片二进制
	 * @return
	 */
	public static String changeFullBase64(byte[] imageByte) {
		if (ArrayUtils.isEmpty(imageByte)) {
			return null;
		}
		return changeFullBase64(new String(imageByte), IMAGE_TYPE_JPG);

	}

	/**
	 * 拼接完整的图片base64字符串
	 *
	 * @param imageByte    图片二进制
	 * @param imageTypeSrc 图片类型
	 * @return
	 */
	public static String changeFullBase64(byte[] imageByte, String imageTypeSrc) {
		if (ArrayUtils.isEmpty(imageByte)) {
			return null;
		}

		return changeFullBase64(new String(imageByte), imageTypeSrc);

	}

	/**
	 * 转换换图片二进制
	 *
	 * @param imgBase64 图片base64字符串
	 * @return png图片二进制
	 */
	public static byte[] base64StrToByte(String imgBase64) {
		if (StringUtils.isBlank(imgBase64)) {
			return null;
		}
//
//		byte[] bytes = decodeImage(imgBase64);
//
//		ByteArrayOutputStream baos = null;
//		try {
//			baos = new ByteArrayOutputStream();
//			BufferedImage bi = null;
//			bi = ImageIO.read(new ByteArrayInputStream(bytes));
//			ImageIO.write(bi, "jpg", baos);
//			bytes = baos.toByteArray();
//		} catch (Exception e) {
//			log.error("图片解析失败", e);
//		} finally {
//			if (Objects.nonNull(baos)) {
//				try {
//					baos.close();
//				} catch (IOException e) {
//					baos = null;
//				}
//			}
//		}
//		return bytes;

		return Base64Utils.decodeFromString(imgBase64);
	}

	/**
	 * 解码base64字符串
	 *
	 * @param imgBase64 base64字符串
	 * @return byte
	 */
	public static byte[] decodeImage(String imgBase64) {
		byte[] bytes = null;
		try {
			// Base64解码
			bytes = Base64Decoder.decode(imgBase64);
			for (int i = 0; i < bytes.length; ++i) {
				if (bytes[i] < 0) {// 调整异常数据
					bytes[i] += 256;
				}
			}
		} catch (Exception e) {
			log.error("图片base64字符串解码失败", e);
		}
		return bytes;
	}

	/**
	 * 将二进制图片转换成Base64字符串
	 *
	 * @param imageBinary 图片二进制
	 * @return 图片base64字符串
	 */
	public static String encodeImage(byte[] imageBinary) {
		return ArrayUtil.isNotEmpty(imageBinary) ? Base64Encoder.encode(imageBinary) : null;
	}

	/**
	 * 移除图片base64字符串头
	 *
	 * @param imageBase64Str 图片base64字符串
	 * @return 移除头后的base64字符串
	 */
	public static String removeHeader(String imageBase64Str) {
		String realBase64 = imageBase64Str;
		if (StringUtils.isBlank(imageBase64Str)) {
			return realBase64;
		}

		int index = imageBase64Str.indexOf(",");
		if (index > 0) {
			realBase64 = imageBase64Str.substring(index + 1);
		}

		return realBase64;
	}


	/**
	 * 图片压缩
	 *
	 * @param imageBase64 图片base64字符串，不带前缀
	 * @param desFileSize 压制大小
	 * @param accuracy    压缩率
	 * @return 压缩后的base64字符串
	 * @throws IOException
	 */
	public static String commpressPicCycle(String imageBase64, long desFileSize, double accuracy) throws IOException {
		byte[] srcImageByte = ImageUtils.decodeImage(imageBase64);
		// 2、判断大小，如果小于指定大小(单位：KB)，不压缩；如果大于等于指定大小(单位：KB)，压缩
		if (srcImageByte.length <= desFileSize * 1024) {
			return Base64Encoder.encode(srcImageByte);
		}

		// 计算宽高
		BufferedImage bim = ImageIO.read(new ByteArrayInputStream(srcImageByte));
		int desWidth = new BigDecimal(bim.getWidth()).multiply(new BigDecimal(accuracy)).intValue();
		int desHeight = new BigDecimal(bim.getHeight()).multiply(new BigDecimal(accuracy)).intValue();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Thumbnails.of(bim).size(desWidth, desHeight).outputQuality(accuracy).outputFormat("png").toOutputStream(output);
		return commpressPicCycle(Base64Encoder.encode(output.toByteArray()), desFileSize, accuracy);
	}

	/**
	 * 将二进制图片做压缩
	 *
	 * @param imageByte
	 * @param width
	 * @param height
	 * @return
	 * @throws Exception
	 */
	public static byte[] fixedSizeByte(byte[] imageByte, int width, int height) throws Exception {
		byte[] fixedSizeImgByte = null;
		ByteArrayInputStream bais = new ByteArrayInputStream(imageByte);
		BufferedImage image = ImageIO.read(bais);
		BufferedImage fixedSizeImg = fixedSizeBufferedImage(image, width, height);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(fixedSizeImg, "jpg", baos);
		fixedSizeImgByte = baos.toByteArray();
		return fixedSizeImgByte;
	}

	/**
	 * 将BufferedImage 做压缩返回压缩后的BufferedImage
	 *
	 * @param image
	 * @param width
	 * @param height
	 * @return
	 * @throws Exception
	 */
	public static BufferedImage fixedSizeBufferedImage(BufferedImage image, int width, int height) throws Exception {
		BufferedImage fixedSizeImg = Thumbnails.of(image).size(width, height).asBufferedImage();
		return fixedSizeImg;
	}
}
