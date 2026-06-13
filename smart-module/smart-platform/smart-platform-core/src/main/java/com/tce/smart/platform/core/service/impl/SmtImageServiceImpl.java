package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.platform.core.entity.SmtImage;
import com.tce.smart.platform.core.mapper.ImageMapper;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.tool.constant.VehicleConstants;
import com.tce.smart.tool.enums.SmtImageEnum;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 图片服务接口
 *
 * @author mckaywu
 * @date 2019-05-30 11:46:36
 */
@Service
@Slf4j
public class SmtImageServiceImpl extends ServiceImpl<ImageMapper, SmtImage> implements SmtImageService {
//
//	@Value("${spring.image.base-url}")
//	private String imageUrl;

	@Override
	public String saveImage(Integer parkId, String base64String, Integer imageType) {
		return saveImage(parkId,UUID.fastUUID().toString(),base64String,null,imageType);
	}

	@Override
	public String saveImage(Integer parkId,String imgCode, String base64String, String smallImageBase64String, Integer imageType) {
		//参数检查
		if (StringUtils.isEmpty(base64String)) {
			throw new TCEException("图片为空");
		}

		//把前缀去掉
		base64String = base64String.replaceFirst(VehicleConstants.BASE64_PREFIX,"")
				.replaceFirst(VehicleConstants.BASE64_PDF,"")
				.replaceFirst(VehicleConstants.BASE64_REGEX_PNG,"");



		//图片类型，默认'未知'
		Integer imageTypeTemp = Objects.nonNull(imageType) ? imageType : SmtImageEnum.TYPE_UNKNOWN.getCode();

		SmtImage smtImage = new SmtImage();
		smtImage.setParkId(parkId);
		//图片业务类型
		smtImage.setImageType(imageTypeTemp);
		//备注
		smtImage.setRemark(SmtImageEnum.desc(imageTypeTemp));
		//图片编码
		smtImage.setImageCode(imgCode);
		//将base64字符串转正二进制
		smtImage.setImage(ImageUtils.base64StrToByte(base64String));
		if(StringUtils.isNotBlank(smallImageBase64String)) {
			//缩略图
			smtImage.setImageSmall(ImageUtils.base64StrToByte(smallImageBase64String));
		} else
		{
			//制作缩略图
			smtImage.setImageSmall(this.image2Small(base64String, ImageUtils.SMALL_FIXED_SIZE_W, ImageUtils.SMALL_FIXED_SIZE_H));
		}
		smtImage.setCreateTime(LocalDateTime.now());

		//执行添加
		smtImage.insert();
//
//		if(null != smtImage.getImage()){
//			//保存图片的大小
//			log.info("保存图片{},原图大小{}",imgCode,smtImage.getImage().length / 1024);
//		}
//
//		if(null != smtImage.getImageSmall()){
//			//保存图片的大小
//			log.info("保存图片{},缩略图大小{}",imgCode,smtImage.getImageSmall().length / 1024);
//		}

		return smtImage.getImageCode();
	}

	@Override
	public Boolean updateByCode(String imageCode, String base64String) {
		//参数检查
		if (StringUtils.isEmpty(base64String)) {
			throw new TCEException("图片为空");
		}

		if (Objects.isNull(imageCode)) {
			throw new TCEException("图片编号为空");
		}

		//将base64字符串转正二进制
		byte[] imageBinary = ImageUtils.base64StrToByte(base64String);
		//处理缩略图
		byte[] smallImageBinary = this.image2Small(base64String, ImageUtils.SMALL_FIXED_SIZE_W, ImageUtils.SMALL_FIXED_SIZE_H);
		return this.update(Wrappers.<SmtImage>update()
				.lambda()
				.eq(SmtImage::getImageCode, imageCode)
				.set(SmtImage::getImage, imageBinary)
				.set(SmtImage::getImageSmall, smallImageBinary)
				.set(SmtImage::getUpdateTime, LocalDateTime.now()));
	}

	@Override
	public Boolean updateByCode(String imageCode, String base64String, String smallBase64String) {
		//参数检查
		if (StringUtils.isEmpty(base64String)) {
			throw new TCEException("图片为空");
		}

		if (Objects.isNull(imageCode)) {
			throw new TCEException("图片编号为空");
		}

		//将base64字符串转正二进制
		byte[] imageBinary = ImageUtils.base64StrToByte(base64String);

		byte[] smallImaseBinary = null;
		if (!StringUtils.isEmpty(smallBase64String)) {
			//将base64字符串转正二进制
			smallImaseBinary = ImageUtils.base64StrToByte(smallBase64String);

		}

		UpdateWrapper<SmtImage> updateWrapper = new UpdateWrapper<>();
		updateWrapper.lambda()
				.eq(SmtImage::getImageCode, imageCode)
				.set(SmtImage::getImage, imageBinary)

				.set(SmtImage::getUpdateTime, LocalDateTime.now());

		//更新缩略图
		if (ArrayUtil.isNotEmpty(smallImaseBinary)) {
			updateWrapper.lambda().set(SmtImage::getImageSmall, smallImaseBinary);
		}

		return this.update(updateWrapper);
	}

	@Override
	public Boolean deleteByCode(String imageCode) {
		// Oracle数据库兼容性修复：检查imageCode是否为null
		if (StringUtils.isEmpty(imageCode)) {
			log.warn("删除图片失败：图片编码为空");
			return Boolean.FALSE;
		}

		return this.remove(Wrappers.<SmtImage>query().lambda().eq(SmtImage::getImageCode, imageCode));
	}

	@Override
	public SmtImage getByCode(String imageCode) {
		// Oracle数据库兼容性修复：检查imageCode是否为null
		if (StringUtils.isEmpty(imageCode)) {
			log.warn("获取图片信息失败：图片编码为空");
			return null;
		}

		return this.list(Wrappers.<SmtImage>query()
				.lambda()
				.eq(SmtImage::getImageCode, imageCode)).stream().findFirst().orElse(null);
	}

	@Override
	public byte[] getImageBinaryByCode(String imageCode) {
		// Oracle数据库兼容性修复：检查imageCode是否为null
		if (StringUtils.isEmpty(imageCode)) {
			log.warn("获取图片二进制数据失败：图片编码为空");
			return null;
		}

		List<SmtImage> smtImage = this.list(Wrappers.<SmtImage>query()
				.lambda()
				.eq(SmtImage::getImageCode, imageCode));
		if(CollectionUtils.isNotEmpty(smtImage)) {
			SmtImage image = smtImage.get(0);
			return image.getImage();
		}
		return null;
	}

	@Override
	public String getImageBase64ByCode(String imageCode) {
		return ImageUtils.encodeImage(this.getImageBinaryByCode(imageCode));
	}

	@Override
	public byte[] getSmallBinaryByCode(String imageCode) {
		// Oracle数据库兼容性修复：检查imageCode是否为null
		if (StringUtils.isEmpty(imageCode)) {
			log.warn("获取缩略图二进制数据失败：图片编码为空");
			return null;
		}

		SmtImage smtImage = this.getOne(Wrappers.<SmtImage>query()
				.lambda()
				.eq(SmtImage::getImageCode, imageCode));

		return Objects.nonNull(smtImage) ? smtImage.getImageSmall() : null;
	}

	@Override
	public String getSmallBase64ByCode(String imageCode) {
		return ImageUtils.encodeImage(this.getSmallBinaryByCode(imageCode));
	}

	@Override
	public String buildImageUrl(String baseImageUrl,final String imageId) {
		String realImageUrl = "";
		if (StringUtils.isNotBlank(baseImageUrl) && baseImageUrl.contains("{image_id}") && StringUtils.isNotBlank(imageId)) {
			realImageUrl = baseImageUrl.replace("{image_id}", imageId);
		}

		return realImageUrl;
	}

	@Override
	public String buildImageUrl(String baseImageUrl, Integer parkId, String imageId) {
		String realImageUrl = "";
		if (StringUtils.isNotBlank(baseImageUrl)
				&& baseImageUrl.contains("{image_id}")
				&& StringUtils.isNotBlank(imageId)
				&& baseImageUrl.contains("{park_id}")
		) {
			realImageUrl = baseImageUrl.replace("{image_id}", imageId).replace("{park_id}",parkId.toString());
		}

		return realImageUrl;
	}

	@Override
	public ResponseEntity<byte[]> download(String code,String name) throws UnsupportedEncodingException {

		byte[] enclosure = getImageBinaryByCode(code);

		String fileName = new String(name.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentDispositionFormData("attachment", fileName);
		httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<>(enclosure, httpHeaders, HttpStatus.OK);
	}

	/**
	 * 处理缩略图
	 *
	 * @param imgBase64 图片base64字符串
	 * @param width     压缩宽度
	 * @param height    压缩高度
	 * @return 压缩后的图片二进制
	 */
	private byte[] image2Small(String imgBase64, int width, int height) {
		byte[] b_small = null;
		byte[] imageByte = ImageUtils.base64StrToByte(imgBase64);
		try {
			b_small = ImageUtils.fixedSizeByte(imageByte, width, height);
		} catch (Exception e) {
			log.warn("将图片转换为缩略图出错");
		}

		return b_small;
	}
}
