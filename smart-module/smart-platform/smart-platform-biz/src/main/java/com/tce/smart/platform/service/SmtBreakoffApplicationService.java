package com.tce.smart.platform.service;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.AddBreakOffApplicationDTO;
import com.tce.smart.platform.core.dto.SearchBreakOffDTO;
import com.tce.smart.platform.core.dto.SearchPatchDTO;
import com.tce.smart.platform.core.entity.SmtBreakoffApplication;
import com.tce.smart.platform.core.model.SearchBreakoffApplicationDetail;
import com.tce.smart.platform.core.vo.SearchBreakOffTypeVO;
import com.tce.smart.platform.core.vo.SearchBreakoffApplicationVO;

/**
 * 调休申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:30:08
 */
public interface SmtBreakoffApplicationService extends IService<SmtBreakoffApplication> {

	void saveBreakoffApplication(AddBreakOffApplicationDTO addBreakoffApplicationDTO);

	Page<SearchBreakoffApplicationVO> getSmtBreakoffApplicationPage(Page page, SmtBreakoffApplication smtBreakoffApplication);

	SearchBreakoffApplicationDetail getBreakoffApplicationById(Integer id);

	List<SearchBreakOffTypeVO> getBreakOffTypeList();

	List<SmtBreakoffApplication> getRestCountList(SearchPatchDTO searchPatchDTO);

	Page<SearchBreakoffApplicationVO> getSmtBreakoffApplicationPageList(Page page, SearchBreakOffDTO searchBreakOffDTO);

}
