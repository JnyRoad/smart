package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.MsgPersonDTO;
import com.tce.smart.platform.core.entity.SmtMsgTempPerson;

import java.util.List;

/**
 * @author Li.JiaJun
 * @since 2022/9/26 14:31
 */
public interface SmtMsgTempPersonService extends IService<SmtMsgTempPerson> {

	/**
	 * 新增/更新模板对应人员
	 *
	 * @param tempId
	 * @param personList
	 * @return
	 */
	Boolean save(Integer tempId, List<MsgPersonDTO> personList);

	/**
	 * 人员列表
	 *
	 * @param tempId
	 * @return
	 */
	List<MsgPersonDTO> getList(Integer tempId);

	/**
	 * 通过模板ID查询
	 *
	 * @param tempId
	 * @return
	 */
	List<String> getByTempId(Integer tempId);
}
