package com.tce.smart.platform.service.settlement;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.SmtStaffStatementReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.StaffStatementWithDorReqDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.StaffStayModifyRespDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.StaffStayRespDTO;
import com.tce.smart.platform.api.dto.resp.sdstatement.SDStatementDetailRespDTO;
import com.tce.smart.platform.core.dto.SmtStaffStatementDTO;
import com.tce.smart.platform.core.dto.SmtStaffStatementDetailDTO;
import com.tce.smart.platform.core.entity.SmtStaffStatementDetail;
import com.tce.smart.platform.core.entity.SmtStaffStatementDetailDaily;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @description: SmtStaffStatementDetailService
 * @date: 2020-07-16 15:46
 * @author: wuling
 * @version: 1.0
 */
public interface SmtStaffStatementDetailDailyService extends IService<SmtStaffStatementDetailDaily> {

}
