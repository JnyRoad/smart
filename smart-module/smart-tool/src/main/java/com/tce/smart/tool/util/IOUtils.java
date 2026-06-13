package com.tce.smart.tool.util;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.tce.smart.common.core.constant.enums.ExceptionEnum;
import com.tce.smart.common.core.exception.SmartException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 导入导出工具类
 * @author fushiping
 * @date 2020/7/10 16:49
 **/
@Slf4j
public class IOUtils {

	/**
	 * EasyPoi excel导出
	 *
	 * @param clazz       实体类型
	 * @param contentList excel实体
	 * @return excel工作薄
	 */
	public static <T> Workbook exportExcel(Class<T> clazz, List<T> contentList) {
		return ExcelExportUtil.exportExcel(new ExportParams(), clazz, contentList);
	}

	/**
	 * 设置Excel下载头信息
	 *
	 * @param fileName 名称名称
	 * @param workbook excle工作薄
	 * @return 文件流
	 */
	public static ResponseEntity<byte[]> getExcelResp(String fileName, Workbook workbook) {
		ResponseEntity<byte[]> responseEntity;
		try (ByteArrayOutputStream outByteStream = new ByteArrayOutputStream()) {
			workbook.write(outByteStream);
			responseEntity = buildResponseEntity(fileName, outByteStream);
		} catch (IOException e) {
			log.error("下载异常", e);
			throw new SmartException(ExceptionEnum.UNKNOWN.getCode(), "下载异常");
		}
		return responseEntity;
	}

	public static ResponseEntity<byte[]> buildResponseEntity(String fileName, ByteArrayOutputStream outByteStream) throws UnsupportedEncodingException {
		ResponseEntity<byte[]> responseEntity;
		String attachmentName = URLEncoder.encode(fileName, "UTF-8");
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentDispositionFormData("attachment", attachmentName);
		httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		responseEntity = new ResponseEntity<>(outByteStream.toByteArray(), httpHeaders, HttpStatus.OK);
		return responseEntity;
	}


	public static ResponseEntity<byte[]> getExcelResponse(String fileName, Workbook workbook) {
		ResponseEntity<byte[]> responseEntity;
		try (ByteArrayOutputStream outByteStream = new ByteArrayOutputStream()) {
			workbook.write(outByteStream);
			String attachmentName = URLEncoder.encode(fileName, "UTF-8");
			final HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentDisposition(ContentDisposition.builder("attachment")
					.filename(attachmentName).build());
			httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			responseEntity = new ResponseEntity<>(outByteStream.toByteArray(), httpHeaders, HttpStatus.OK);
		} catch (IOException e) {
			log.error("下载异常", e);
			throw new SmartException(ExceptionEnum.UNKNOWN.getCode(), "下载异常");
		}
		return responseEntity;
	}
}
