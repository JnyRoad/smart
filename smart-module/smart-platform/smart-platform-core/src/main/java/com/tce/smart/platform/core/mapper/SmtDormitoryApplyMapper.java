package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.GenerateStatementDTO;
import com.tce.smart.platform.core.dto.SmtSdMeterreadDTO;
import com.tce.smart.platform.core.dto.commonsd.DormitorySDMeterreadDTO;
import com.tce.smart.platform.core.dto.dormitorymanage.DormitoryApplyDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryApply;
import com.tce.smart.platform.core.entity.SmtSdMeterread;
import com.tce.smart.platform.core.vo.SmtSdMeterreadVO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @description: SmtDormitoryApplyMapper
 * @date: 2020-12-29
 * @author: wuling
 * @version: 1.0
 */
public interface SmtDormitoryApplyMapper extends BaseMapper<SmtDormitoryApply> {
	IPage<DormitoryApplyDTO> getApplyRecord(@Param("page") Page page, @Param("query") DormitoryApplyDTO applyDTO);
}
