package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SDStatementFeeDetailDTO;
import com.tce.smart.platform.core.dto.SmtStaffStatementDTO;
import com.tce.smart.platform.core.dto.SmtStaffStatementDetailDTO;
import com.tce.smart.platform.core.entity.SmtStaffStatementDetail;
import com.tce.smart.platform.core.entity.SmtStaffStatementDetailDaily;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @description: SmtStaffStatementDetailMapper
 * @date: 2020-07-16 15:48
 * @author: wuling
 * @version: 1.0
 */
public interface SmtStaffStatementDetailDailyMapper extends BaseMapper<SmtStaffStatementDetailDaily> {

}
