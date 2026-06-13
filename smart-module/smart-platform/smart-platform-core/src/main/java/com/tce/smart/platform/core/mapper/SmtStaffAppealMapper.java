package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SmtSecurityAreaSupplierDTO;
import com.tce.smart.platform.core.dto.StaffAppealSearchDTO;
import com.tce.smart.platform.core.entity.SmtStaffAppeal;
import com.tce.smart.platform.core.vo.SmtStaffAppealVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: SmtStaffAppealMapper
 * @date: 2020-07-23 14:03
 * @author: wuling
 * @version: 1.0
 */
public interface SmtStaffAppealMapper extends BaseMapper<SmtStaffAppeal> {

	IPage<SmtStaffAppealVO> getStaffAppealPage(Page page, @Param("query") StaffAppealSearchDTO staffAppealSearchDTO, @Param("park") List<Integer> parkIdList);

	SmtStaffAppealVO getStaffAppealDetail(@Param("id") Long id);

	IPage<SmtStaffAppealVO> getStaffAppealListPage(Page page, @Param("query") StaffAppealSearchDTO staffAppealSearchDTO, @Param("park") List<Integer> parkIdList);
}
