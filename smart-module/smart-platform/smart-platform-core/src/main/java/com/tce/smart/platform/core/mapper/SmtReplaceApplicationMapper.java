package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tce.smart.platform.core.dto.PatchStatisticsDTO;
import com.tce.smart.platform.core.vo.PatchStatisticsVo;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SearchReplaceDTO;
import com.tce.smart.platform.core.entity.SmtReplaceApplication;
import com.tce.smart.platform.core.vo.SearchReplaceApplicationVO;

/**
 * 职工补卡申请表

 *
 * @author 梁圆
 * @date 2019-04-13 18:19:37
 */
public interface SmtReplaceApplicationMapper extends BaseMapper<SmtReplaceApplication> {

	Page<SearchReplaceApplicationVO> getSmtReplaceApplicationPage(Page page, @Param("query")SmtReplaceApplication smtReplaceApplication);

	Page<SearchReplaceApplicationVO> getSmtReplaceApplicationPageList(Page page,@Param("query") SearchReplaceDTO searchReplaceDTO);

	/**
	 * 获取每种补卡原因统计
	 * @param page
	 * @param query
	 * @return
	 */
	IPage<PatchStatisticsVo> patchStatistics(Page page, @Param("query") PatchStatisticsDTO query);
}
