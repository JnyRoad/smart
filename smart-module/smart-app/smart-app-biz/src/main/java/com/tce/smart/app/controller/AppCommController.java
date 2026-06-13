package com.tce.smart.app.controller;

import cn.hutool.http.HttpUtil;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.common.core.wrapper.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * description: App公共Controller <br>
 * date: 2019/11/13 9:41 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@RestController
@RequestMapping("/common")
public class AppCommController extends BaseController {

	/**
	 * 图片id
	 */
	public static final String NORMAL_IAMGE_ID = "{imageId}";

	/**
	 * 远程文件服务器存储-图片访问URL后缀
	 */
	public static final String NORMAL_IAMGE = "/image/view/"+NORMAL_IAMGE_ID;

	/**
	 * 本地App数据库存储-图片方式访问URL后缀-内容图片
	 */
	public static final String NEWS_IAMGE = "/image/view/ct/"+NORMAL_IAMGE_ID;

	/**
	 * 本地App数据库存储-图片方式访问URL后缀-图片内容表
	 */
	public static final String CP_IAMGE = "/image/view/cp/"+NORMAL_IAMGE_ID;

	/**
	 * 本地App数据库存储-图片方式访问URL后缀-模块图片
	 */
	public static final String MODULE_IAMGE = "/image/view/md/"+NORMAL_IAMGE_ID;

	/**
	 * 本地App数据库存储-图片方式访问URL后缀-广告图片
	 */
	public static final String ADVER_IAMGE = "/image/view/adver/"+NORMAL_IAMGE_ID;

	@Autowired
	private AppCommService appCommService;

	/**
	 * 访问图片图片
	 * @param imageId 图片存储ID
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = NORMAL_IAMGE, produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<byte[]> viewImage(@PathVariable("imageId") String imageId) throws Exception {
		byte[] imageContent = appCommService.getHqImageByte(imageId);

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_PNG);
		return new ResponseEntity<>(imageContent, headers, HttpStatus.OK);
	}

	/**
	 * 访问文本内容图片
	 * @param contentTextId 文本内容ID
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = NEWS_IAMGE, produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<byte[]> viewCtImage(@PathVariable("imageId") String contentTextId) {
		byte[] imageContent = appCommService.getContentTextImageByte(contentTextId);

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_PNG);
		return new ResponseEntity<>(imageContent, headers, HttpStatus.OK);
	}

	/**
	 * 访问图片内容表
	 * @param contentPicId 图片内容ID
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = CP_IAMGE, produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<byte[]> viewCpImage(@PathVariable("imageId") String contentPicId) {
		byte[] imageContent = appCommService.getContentPicImageByte(contentPicId);

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_PNG);
		return new ResponseEntity<>(imageContent, headers, HttpStatus.OK);
	}

	/**
	 * 访问模块图片
	 * @param moduleId 模块ID
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = MODULE_IAMGE, produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<byte[]> viewModuleImage(@PathVariable("imageId") String moduleId) throws Exception {
		byte[] imageContent = appCommService.getModuleImageByte(moduleId);

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_PNG);
		return new ResponseEntity<>(imageContent, headers, HttpStatus.OK);
	}

	/**
	 * 访问广告图片
	 * @param moduleId 模块ID
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = ADVER_IAMGE, produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<byte[]> viewAdverImage(@PathVariable("imageId") String moduleId) throws Exception {
		byte[] imageContent = appCommService.getAdverImageByte(moduleId);

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_PNG);
		return new ResponseEntity<>(imageContent, headers, HttpStatus.OK);
	}

	/**
	 * 下载PDF文件
	 *
	 * @param subjectId 主题ID
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/download/{id}", produces = "application/octet-stream;charset=UTF-8", method = RequestMethod.GET)
	public ResponseEntity<byte[]> pdfDownload(@PathVariable("id") Integer subjectId) throws IOException {
		return appCommService.downloadFdf(subjectId);
	}

	@GetMapping("/weather")
	public String getWeather(@RequestParam("city") String city) {
		// 解决浏览器中https页面不兼容http请求的问题
		return HttpUtil.get("http://wthrcdn.etouch.cn/weather_mini?city="+city);
	}
}
