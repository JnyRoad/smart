package com.tce.smart.platform.service.news.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.platform.api.dto.req.news.NewsInfoFileReqDTO;
import com.tce.smart.platform.api.dto.resp.news.NewsFileRespDTO;
import com.tce.smart.platform.core.entity.news.SmtNewsInfoFile;
import com.tce.smart.platform.core.mapper.news.SmtNewsInfoFileMapper;
import com.tce.smart.platform.service.news.SmtNewsInfoFileService;
import com.tce.smart.tool.enums.NewsPublicTypeEnum;
import com.tce.smart.tool.enums.OneOrZeroEnum;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * @author fushiping
 * @date 2022-02-16 18:00:09
 */
@Service
public class SmtNewsInfoFileServiceImpl extends ServiceImpl<SmtNewsInfoFileMapper, SmtNewsInfoFile> implements SmtNewsInfoFileService {

	@Value("${spring.file.base-url}")
	private String baseUrl;


	@Override
	public String upload(NewsInfoFileReqDTO fileReqDTO, MultipartFile data) throws IOException {
		String md5 = DigestUtils.md5Hex(data.getBytes());
		SmtNewsInfoFile reFile = this.getByMd5(md5);
		if (Objects.isNull(reFile)) {
			SmtNewsInfoFile file = SmtNewsInfoFile.builder()
					.fileMd5(md5).isUse(OneOrZeroEnum.ZERO.getCode())
					.data(data.getBytes())
					.fileName(fileReqDTO.getFileName())
					.fileSize(fileReqDTO.getFileSize())
					.fileSuffix(fileReqDTO.getFileSuffix()).build();
			file.insert();
			return file.getId().toString();
		}
		return reFile.getId().toString();
	}

	@Override
	public byte[] download(String id) {
		SmtNewsInfoFile file = this.getById(Long.parseLong(id));
		if (Objects.nonNull(file)) {
			return file.getData();
		}
		return null;
	}

	@Override
	public void getStream(HttpServletRequest request, HttpServletResponse response, String id) {
		response.reset();
		try {
			OutputStream outputStream = response.getOutputStream();
			SmtNewsInfoFile file = this.getById(Long.parseLong(id));
			if (Objects.isNull(file)) {
				throw new SmartException("文件为空");
			}
			InputStream inputStream = new ByteArrayInputStream(file.getData());
			response.setContentType("video/mp4");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getFileName() + "\"");
			//跨域问题处理
			response.setHeader("Access-Control-Allow-Origin", "*");
			if(NewsPublicTypeEnum.PPT.getDesc().toLowerCase().equals(file.getFileSuffix())) {
				response.setContentType("application/vnd.ms-powerpoint");
			}
			byte[] buffer = new byte[4096];
			// 读取长度
			int len;
			while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
				outputStream.write(buffer, 0, len);
			}
//			byte[] data = new byte[inputStream.available()];
//			inputStream.read(data);
//			outputStream.write(data);
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String buildFileUrl(String fileId) {
		String realImageUrl = "";
		if (StringUtils.isNotBlank(baseUrl) && baseUrl.contains("{id}") && StringUtils.isNotBlank(fileId)) {
			realImageUrl = baseUrl.replace("{id}", fileId);
		}
		return realImageUrl;
	}

	@Override
	public Boolean enableFile(String id) {
		SmtNewsInfoFile fileBean = SmtNewsInfoFile.builder().id(Long.parseLong(id)).isUse(OneOrZeroEnum.ONE.getCode()).build();
		return this.updateById(fileBean);
	}

	public SmtNewsInfoFile getByMd5(String md5) {
		return getOne(Wrappers.<SmtNewsInfoFile>lambdaQuery().eq(SmtNewsInfoFile::getFileMd5, md5));
	}

}
