package com.tce.smart.bridge.isc.service.impl;

import com.tce.smart.bridge.isc.entity.File;
import com.tce.smart.bridge.isc.service.HBaseFileService;
import com.tce.smart.bridge.isc.service.HBaseService;
import com.tce.smart.common.core.util.UUIDUtils;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

/**
 * @Description: TODO
 * @ProjectName smart-file
 * @ClassName: HBaseFileServiceImpl
 * @Author jinbo
 * @Date 2019/12/17
 */
@Slf4j
@Service
public class HBaseFileServiceImpl implements HBaseFileService {
	@Autowired
	private HBaseService hBaseService;
	@Override
	public byte[] getById(String id) {
		File file = new File();
		file.setKey(id);
		file = hBaseService.query(file);
		return Objects.isNull(file) ? null : file.get();
	}

	@Override
	public byte[] getThumbnailById(String id) {
		byte[] file = getById(id);
		if(Objects.nonNull(file)){
			return image2Small(file);
		}
		return new byte[0];
	}

	@Override
	public String save(byte[] data, Integer photoType) {
		String id = UUIDUtils.create();
		boolean save = save(id, data, photoType);
		return save ? id : null;
	}

	@Override
	public boolean save(String id, byte[] data, Integer photoType) {
		File file = new File();
		file.setKey(id);
		file.put(data);
		file.setFileType(photoType);
		return hBaseService.put(file);
	}

	@Override
	public boolean update(String id, byte[] data) {
		File file = new File();
		file.setKey(id);
		file.put(data);
		return hBaseService.put(file);
	}

	@Override
	public boolean delete(String id) {
		File file = new File();
		file.setKey(id);
		return hBaseService.delete(file);
	}


	private final int SMALL_FIXED_SIZE_W = 160;  //缩略图宽度
	private final int SMALL_FIXED_SIZE_H = 160;  //缩略图高度

	private byte[] image2Small(byte[] imageByte){
		try {
			return fixedSizeByte(imageByte, SMALL_FIXED_SIZE_W, SMALL_FIXED_SIZE_H);
		} catch (Exception e) {
			log.warn("将图片转换为缩略图出错");
		}
		return null;
	}

	private byte[] fixedSizeByte(byte[] imageByte,int width,int height) throws Exception{
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageByte);
		BufferedImage image = ImageIO.read(byteArrayInputStream);
		BufferedImage fixedSizeImg = Thumbnails.of(image).size(width, height).asBufferedImage();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(fixedSizeImg,"jpg", byteArrayOutputStream);
		return byteArrayOutputStream.toByteArray();
	}
}
