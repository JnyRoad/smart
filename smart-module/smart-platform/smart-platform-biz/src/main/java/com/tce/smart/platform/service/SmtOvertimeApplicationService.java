package com.tce.smart.platform.service;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.AddOverTimeApplicationDTO;
import com.tce.smart.platform.core.dto.SearchOverTimeDTO;
import com.tce.smart.platform.core.entity.SmtOvertimeApplication;
import com.tce.smart.platform.core.vo.SearchOverClassTimeTypeVO;
import com.tce.smart.platform.core.vo.SearchOverTimeApplicationDetailVO;
import com.tce.smart.platform.core.vo.SearchOverTimeApplicationVO;
import com.tce.smart.platform.core.vo.SearchOverTimeTypeVO;

/**
 * 加班申请表

 *
 * @author 梁圆
 * @date 2019-04-13 18:20:11
 */
public interface SmtOvertimeApplicationService extends IService<SmtOvertimeApplication> {

	List<SearchOverClassTimeTypeVO> getOverClassTypeList();
	List<SearchOverTimeTypeVO> getOverTypeList();

	void save(AddOverTimeApplicationDTO addOverApplicationDTO);

	Page<SearchOverTimeApplicationVO> getOvertimeApplicationPage(Page page, SmtOvertimeApplication smtOvertimeApplication);

	SearchOverTimeApplicationDetailVO getOverTimeById(Integer id);

	Page<SearchOverTimeApplicationVO> getOvertimeApplicationPageList(Page page, SearchOverTimeDTO searchLeaveDTO);

	SearchOverTimeApplicationDetailVO getOverTimeByListId(Integer id);

}
