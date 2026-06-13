package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.commonsd.StaffSDRuleRespDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryFloor;
import com.tce.smart.platform.core.entity.SmtSdTemplates;
import com.tce.smart.platform.core.vo.FloorVO;
import com.tce.smart.platform.core.vo.SmtSDTemplateVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: SmtSDTemplatesMapper
 * @date: 2020-07-01 15:19
 * @author: wuling
 * @version: 1.0
 */
public interface SmtSDTemplatesMapper extends BaseMapper<SmtSdTemplates> {
	IPage<List<SmtSDTemplateVO>> getSmtDormitorySDTemplatePage(Page page, @Param("query") SmtSdTemplates smtSdTemplates,@Param("park") List<Integer> parkIdList);

	List<StaffSDRuleRespDTO> getStaffSDRule(@Param("staffBadgeList") List<String> staffBadgeList,@Param("categoryId") Integer categoryId, @Param("monthNum") Integer monthNum);

	StaffSDRuleRespDTO getSDRuleById(@Param("tempId") Long tempId,@Param("categoryId") Integer categoryId,@Param("monthNum")Integer monthNum);
}
