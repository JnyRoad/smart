package com.tce.smart.platform.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.tce.smart.platform.api.annotation.ColumnAlias;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang.ArrayUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * @author Li.JiaJun
 * @since 2022/3/29 16:13
 */
@UtilityClass
public class ExcelUtils {

	/**
	 * 水电表错误表格导出实现
	 */
	public static <T, K> void export(HttpServletRequest request, HttpServletResponse response, String fileName,
									 String sheetName1, List<T> data1, String sheetName2, List<K> data2) {
		header(request, response, fileName);
		// 通过工具类创建writer
		// 注意 ExcelUtil.getWriter()默认创建xls格式的Excel，
		// 因此写出到客户端也需要自定义文件名为XXX.xls，否则会出现文件损坏的提示。
		// 若想生成xlsx格式，则使用ExcelUtil.getWriter(true)创建
		ExcelWriter writer;
		if (fileName.endsWith(".xlsx")) {
			writer = ExcelUtil.getWriter(true);
		} else {
			writer = ExcelUtil.getWriter();
		}
		writer.renameSheet(sheetName1);
		if (CollectionUtil.isNotEmpty(data1)) {
			T t = data1.get(0);
			if (Objects.nonNull(t)) {
				columnAlias(t.getClass(), writer);
			}
		}
		// 一次性写出内容，使用默认样式，强制输出标题
		writer.write(data1, true);
		writer.setSheet(sheetName2);
		if (CollectionUtil.isNotEmpty(data2)) {
			K t = data2.get(0);
			if (Objects.nonNull(t)) {
				columnAlias(t.getClass(), writer);
			}
		}
		// 一次性写出内容，使用默认样式，强制输出标题
		writer.write(data2, true);
		ServletOutputStream out;
		try {
			out = response.getOutputStream();
			writer.flush(out);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 关闭writer，释放内存
			writer.close();
		}
	}

	/**
	 * fileName 包含后缀名
	 *
	 * @param request
	 * @param response
	 * @param fileName
	 */
	private static void header(HttpServletRequest request, HttpServletResponse response, String fileName) {
		try {
			response.setCharacterEncoding("UTF-8");
			response.setHeader("Cache-control", "private");
			response.setHeader("Cache-Control", "maxAge=3600");
			response.setHeader("Pragma", "public");
			response.setHeader("Accept-Ranges", "bytes");
			//获得浏览器信息并转换为大写
			String agent = request.getHeader("User-Agent").toUpperCase();
			//IE浏览器和Edge浏览器
			if (agent.indexOf("MSIE") > 0 || (agent.indexOf("GECKO") > 0 && agent.indexOf("RV:11") > 0)) {
				fileName = URLEncoder.encode(fileName, "UTF-8");
			} else {
				//其他浏览器
				fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
			}
			response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
			if (fileName.endsWith(".xlsx")) {
				response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			}
			if (fileName.endsWith(".xls")) {
				response.setContentType("application/vnd.ms-excel");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private static <T> void columnAlias(Class<T> data, ExcelWriter writer) {
		Field[] fields = data.getDeclaredFields();
        for (Field field : fields) {
			ColumnAlias[] aliases = field.getAnnotationsByType(ColumnAlias.class);
			for (ColumnAlias alias : aliases) {
				String name = alias.value();
				if (StrUtil.isNotEmpty(name)) {
					writer.addHeaderAlias(field.getName(), name);
				}
			}
		}
	}

	/**
	 * 读取excel表格内容返回List<Bean>
	 *
	 * @return
	 */
	public static <T> List<T> importExcel(ExcelReader reader, String[] head, String[] headerAlias,
										  String sheetName, Class<T> bean, Integer headRowIndex, Integer startRowIndex) {
		//切换sheet
		reader.setSheet(sheetName);
		List<Object> header = reader.readRow(headRowIndex);
		//替换表头关键字
		if (ArrayUtils.isEmpty(head) || ArrayUtils.isEmpty(headerAlias) || head.length != headerAlias.length) {
			return null;
		} else {
			for (int i = 0; i < head.length; i++) {
				if (head[i].equals(header.get(i))) {
					reader.addHeaderAlias(head[i], headerAlias[i]);
				} else {
					return null;
				}
			}
		}
		return reader.read(headRowIndex, startRowIndex, bean);
	}
}
