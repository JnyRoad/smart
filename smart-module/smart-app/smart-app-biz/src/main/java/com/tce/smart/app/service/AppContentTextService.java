package com.tce.smart.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.ao.AddAppSubjectAo;
import com.tce.smart.app.dto.AppQuestionDto;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.common.core.model.Result;
import org.springframework.beans.BeanUtils;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

/**
 * 文本内容
 *
 * @author fushiping
 * @date 2019-04-25 09:49:29
 */
public interface AppContentTextService extends IService<AppContentText> {

	/**
	 * 修改文本内容
	 * @param addAppSubjectAo
	 * @return
	 */
	void updateTextContent(AddAppSubjectAo addAppSubjectAo);


	/**
	 * 添加文本内容
	 * @param appSubjectAo
	 * @return
	 */
	Integer insertTextContent(AddAppSubjectAo appSubjectAo);

	/**
	 * 删除文本内容
	 * @param id
	 */
	void deleteTextContent(Integer id);
}
