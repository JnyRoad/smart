package com.tce.smart.platform.service.news;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.news.NewsInfoFileReqDTO;
import com.tce.smart.platform.core.entity.news.SmtNewsInfoFile;
import com.tce.smart.platform.core.entity.news.SmtNewsInfoImage;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 *
 * @author fushiping
 * @date 2022-02-16 18:00:09
 */
public interface SmtNewsInfoFileService extends IService<SmtNewsInfoFile> {

	/**
	 * 文件上传
	 * @param fileReqDTO
	 * @return
	 */
	String upload(NewsInfoFileReqDTO fileReqDTO, MultipartFile data) throws IOException;

	/**
	 * 文件下载
	 * @param id
	 * @return
	 */
	byte[] download(String id);

	/**
	 * 文件流下载
	 * @param id
	 * @return
	 */
	void getStream(HttpServletRequest request, HttpServletResponse response, String id);

	/**
	 * 启用文件
	 * @param id
	 * @return
	 */
	Boolean enableFile(String id);

	/**
	 * 设置文件url
	 * @param fileId
	 * @return
	 */
	String buildFileUrl(String fileId);
}
