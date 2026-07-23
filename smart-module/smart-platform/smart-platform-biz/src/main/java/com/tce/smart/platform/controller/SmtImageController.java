package com.tce.smart.platform.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.dispatcher.api.dto.req.ImageDTO;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.core.dto.SaveImageDto;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.tool.constant.VehicleConstants;
import com.tce.smart.tool.util.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Objects;

/**
 * 图片访问控制器
 *
 * @author mckaywu
 * @date 2019-05-29 18:27:36
 */
@RestController
@RequestMapping("/image")
public class SmtImageController extends BaseController {

	@Autowired
	private SmtImageService smtImageService;

	@Resource
	private RemoteDispatcherService remoteDispatcherService;

	/**
	 * 保存图片
	 *
	 * @param saveImageDto 图片信息
	 * @return 图片编码
	 */
	@PostMapping("/save")
	public Result<String> save(@RequestBody SaveImageDto saveImageDto) {
		if (Objects.isNull(saveImageDto) || Objects.isNull(saveImageDto.getImageType()) || Objects.isNull(saveImageDto.getBase64String())) {
			return fail("参数不全");
		}

		return success(smtImageService.saveImage(saveImageDto.getParkId(), saveImageDto.getBase64String(), saveImageDto.getImageType()));
	}

	/**
	 * 根据图片编码更新
	 *
	 * @param saveImageDto 图片信息
	 * @return 成功-true，失败-false
	 */
	@PostMapping("/update/code")
	public Result<Boolean> updateByImageCode(@RequestBody SaveImageDto saveImageDto) {
		if (Objects.isNull(saveImageDto) || Objects.isNull(saveImageDto.getImageCode()) || Objects.isNull(saveImageDto.getBase64String())) {
			return fail("参数不全");
		}

		return success(smtImageService.updateByCode(saveImageDto.getImageCode(), saveImageDto.getBase64String()));
	}

	/**
	 * 根据图片编码删除
	 *
	 * @param imageCode 图片编号
	 * @return 成功-true，失败-false
	 */
	@PostMapping("/delete/code/{imageCode}")
	public Result<Boolean> deleteByCode(@PathVariable("imageCode") String imageCode) {
		return success(smtImageService.deleteByCode(imageCode));
	}

	/**
	 * 根据图片编码获取Base64原图
	 *
	 * @param imageCode 图片编号
	 * @return 图片base64字符串
	 */
	@GetMapping("/get/base64/code/{imageCode}")
	public Result<String> getImageBase64ByCode(@PathVariable("imageCode") String imageCode) {
		return success(smtImageService.getImageBase64ByCode(imageCode));
	}

	/**
	 *  查看原图
	 *
	 * @param imageId 图片编号
	 * @return
	 */
	@RequestMapping(value = "/view/{imageId}", produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<byte[]> viewImage(@PathVariable("imageId") String imageId){
		byte[] imageContent = smtImageService.getImageBinaryByCode(imageId);
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_PNG);
		return new ResponseEntity<>(imageContent, headers, HttpStatus.OK);
	}

	/**
	 *  查看原图
	 *
	 * @param imageId 图片编号
	 * @return
	 */
	@RequestMapping(value = "/view/{parkId}/{imageId}", produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<byte[]> viewImage(@PathVariable("parkId") Integer parkId,@PathVariable("imageId") String imageId){
		byte[] imageContent = smtImageService.getImageBinaryByCode(imageId);
		if(null == imageContent || imageContent.length <= 0){
			try {
				//从hbase中查询图片内容
				ImageDTO imageDTO = new ImageDTO();
				imageDTO.setParkId(parkId);
				imageDTO.setId(imageId);
				Result<String> imageRs = remoteDispatcherService.getImage(imageDTO, SecurityConstants.FROM_IN,
						SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);

				imageContent = ImageUtils.base64StrToByte(imageRs.getData().replaceFirst(VehicleConstants.BASE64_PREFIX, ""));
			}catch (Exception e){}
		}

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_PNG);
		return new ResponseEntity<>(imageContent, headers, HttpStatus.OK);
	}

	/**
	 * 查看缩略图
	 *
	 * @param imageId 图片编号
	 * @return
	 */
	@RequestMapping(value = "/view/small/{imageId}", produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<byte[]> viewSmallImage(@PathVariable("imageId") String imageId){
		byte[] imageContent = smtImageService.getSmallBinaryByCode(imageId);

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_PNG);
		return new ResponseEntity<>(imageContent, headers, HttpStatus.OK);
	}

	/**
	 * 下载
	 * @param code
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/download/{code}/{name}", produces = "application/octet-stream;charset=UTF-8", method = RequestMethod.GET)
	public ResponseEntity<byte[]> download(@PathVariable("code") String code,@PathVariable("name") String name) throws IOException {
		return smtImageService.download(code,name);
	}


}
