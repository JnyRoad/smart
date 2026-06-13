package com.tce.smart.platform.service.settlement.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.RoomMeterQueryDTO;
import com.tce.smart.platform.api.dto.req.SmtStaffStatementReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.StaffStatementWithDorReqDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.DormitorySDMeterreadNewRespDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.StaffStayModifyRespDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.StaffStayRespDTO;
import com.tce.smart.platform.api.dto.resp.sdstatement.SDStatementDetailRespDTO;
import com.tce.smart.platform.core.dto.SDStatementFeeDetailDTO;
import com.tce.smart.platform.core.dto.SmtStaffStatementDTO;
import com.tce.smart.platform.core.dto.SmtStaffStatementDetailDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.*;
import com.tce.smart.platform.service.SmtDormitoryRoomService;
import com.tce.smart.platform.service.SmtDormitoryService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.settlement.SmtSdMeterreadService;
import com.tce.smart.platform.service.settlement.SmtStaffSDMHistoryService;
import com.tce.smart.platform.service.settlement.SmtStaffStatementDetailDailyService;
import com.tce.smart.platform.service.settlement.SmtStaffStatementDetailService;
import com.tce.smart.tool.enums.MeterTypeEnum;
import com.tce.smart.tool.enums.SDCategoryEnum;
import com.tce.smart.tool.enums.SdStatementStatusEnum;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.ToolUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: SmtStaffStatementDetailServiceImpl
 * @date: 2020-07-16 15:47
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class SmtStaffStatementDetailDailyServiceImpl extends ServiceImpl<SmtStaffStatementDetailDailyMapper, SmtStaffStatementDetailDaily> implements SmtStaffStatementDetailDailyService {

}
