package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SDStatementFeeDetailDTO;
import com.tce.smart.platform.core.dto.SmtStaffStatementDTO;
import com.tce.smart.platform.core.dto.SmtStaffStatementDetailDTO;
import com.tce.smart.platform.core.entity.SmtStaffStatementDetail;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @description: SmtStaffStatementDetailMapper
 * @date: 2020-07-16 15:48
 * @author: wuling
 * @version: 1.0
 */
public interface SmtStaffStatementDetailMapper extends BaseMapper<SmtStaffStatementDetail> {

	IPage<SmtStaffStatementDTO> getStaffSDStatementDetail(Page page, @Param("query") SmtStaffStatementDTO smtStaffStatementDTO, @Param("park") List<Integer> parkIdList);

	IPage<SmtStaffStatementDTO> getStaffSDStatementDetailNew(Page page, @Param("query") SmtStaffStatementDTO smtStaffStatementDTO, @Param("park") List<Integer> parkIdList, @Param("roomIdList") List<List<Integer>> roomIdList);

	IPage<SmtStaffStatementDTO> getStaffSDStatementDetailNewDaily(Page page, @Param("query") SmtStaffStatementDTO smtStaffStatementDTO, @Param("park") List<Integer> parkIdList, @Param("roomIdList") List<List<Integer>> roomIdList);

	List<SDStatementFeeDetailDTO> getStaffSDMeterRecord(@Param("staffBadge") String staffBadge, @Param("timeList") List<Date> timeList);

	List<SmtStaffStatementDTO> getStaffSDStatementDetailWithDor(@Param("dorIds") List<Integer> dorIds, @Param("meterMonth") Date meterMonth, @Param("park") List<Integer> parkIdList);

	List<SmtStaffStatementDetailDTO> getStaffSDStatementDetailExport(@Param("query") SmtStaffStatementDTO smtStaffStatementDTO, @Param("park") List<Integer> parkIdList);

}
