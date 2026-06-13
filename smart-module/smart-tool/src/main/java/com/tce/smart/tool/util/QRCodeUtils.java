package com.tce.smart.tool.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName: QRCodeUtils
 * @Package com.tce.operator.jsiot.phone.util
 * @Description:
 * @Author wuxinjian
 * @Date 2019/4/1 16:46
 * @Version V1.0
 */
public class QRCodeUtils {


    /**
     * 图形交换格式
     */
    public static final String IMAGE_TYPE_GIF = "gif";

    /**
     * 联合照片专家组
     */
    public static final String IMAGE_TYPE_JPG = "jpg";

    /**
     * 联合照片专家组
     */
    public static final String IMAGE_TYPE_JPEG = "jpeg";
    /**
     * 英文Bitmap（位图）的简写，它是Windows操作系统中的标准图像文件格式
     */
    public static final String IMAGE_TYPE_BMP = "bmp";
    /**
     * 可移植网络图形
     */
    public static final String IMAGE_TYPE_PNG = "png";
    /**
     * Photoshop的专用格式Photoshop
     */
    public static final String IMAGE_TYPE_PSD = "psd";
    /**
     * 二维码宽度
     */
    private static final int QR_CODE_WIDTH = 200;

    /**
     * 二维码高度
     */
    private static final int QR_CODE_HEIGHT = 200;

    private static final Map<EncodeHintType, Object> HINTS = new HashMap<>(3);

    static {
        HINTS.put(EncodeHintType.CHARACTER_SET, "utf-8");
        HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        HINTS.put(EncodeHintType.MARGIN, 2);
    }


    public static String wordsCreateQRCode(String content) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //生成二维码
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT, HINTS);
        MatrixToImageWriter.writeToStream(bitMatrix, "png", out);
        return Base64Utils.encodeToString(out.toByteArray());
    }


    public static String wordsCreateQRCodeWithHead(String content) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //生成二维码
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT, HINTS);
        MatrixToImageWriter.writeToStream(bitMatrix, "png", out);
        return imageBase64(out.toByteArray());
    }

    public static String imgType(byte[] imageData) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
             MemoryCacheImageInputStream is = new MemoryCacheImageInputStream(bais)) {
            Iterator<ImageReader> it = ImageIO.getImageReaders(is);

            if (!it.hasNext()) {
                throw new IOException("非图片文件");
            }
            ImageReader reader = it.next();
            return reader.getFormatName();
        }
    }

    public static String imageBase64(byte[] data) throws IOException {
        if (Objects.nonNull(data)) {
            String type = imgType(data);
            if (StringUtils.isEmpty(type)) {
                return "";
            }
            String prefix;
            switch (type.toLowerCase()) {
                case IMAGE_TYPE_PNG:
                    prefix = "data:image/png;base64,";
                    break;
                case IMAGE_TYPE_JPEG:
                    prefix = "data:image/jpeg;base64,";
                    break;
                case IMAGE_TYPE_GIF:
                    prefix = "data:image/gif;base64,";
                    break;
                case IMAGE_TYPE_JPG:
                    prefix = "data:image/jpg;base64,";
                    break;
                default:
                    return "";
            }
            return prefix + Base64Utils.encodeToString(data);
        }
        return "";
    }


    public static void main(String[] args) throws Exception {
        String base64 = wordsCreateQRCode("Y23");
        System.out.println(base64);
    }
}
