package com.tce.smart.platform.service.badge.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.enums.ExceptionEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.badge.QueryLossInfoReqDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeApplyExcelRespDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeLossExcelRespDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.badge.SmtBadgeLoss;
import com.tce.smart.platform.core.mapper.SmtBadgeLossMapper;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.badge.SmtBadgeLossService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.ExportTypeEnum;
import com.tce.smart.tool.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 厂牌挂失
 *
 * @author fushiping
 * @date 2020-07-07 11:47:43
 */
@Service
@Slf4j
public class SmtBadgeLossServiceImpl extends ServiceImpl<SmtBadgeLossMapper, SmtBadgeLoss> implements SmtBadgeLossService {

	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtParkService smtParkService;

	@Override
	public IPage<SmtBadgeLoss> getPage(Page page, QueryLossInfoReqDTO reqDTO) {
		List<Integer> parkList = SecurityUtils.getUser().getParkIdList();
		return this.page(page, Wrappers.<SmtBadgeLoss>query().lambda()
				.eq(Objects.nonNull(reqDTO.getBadge()), SmtBadgeLoss::getBadge, reqDTO.getBadge())
				.eq(Objects.nonNull(reqDTO.getCompId()), SmtBadgeLoss::getCompId, reqDTO.getCompId())
				.eq(Objects.nonNull(reqDTO.getDepId()), SmtBadgeLoss::getDepId, reqDTO.getDepId())
				.eq(Objects.nonNull(reqDTO.getParkId()), SmtBadgeLoss::getParkId, reqDTO.getParkId())
				.ge(Objects.nonNull(reqDTO.getStartTime()), SmtBadgeLoss::getCreateTime, reqDTO.getStartTime())
				.le(Objects.nonNull(reqDTO.getEndTime()), SmtBadgeLoss::getCreateTime, reqDTO.getEndTime())
				.in(CollectionUtils.isNotEmpty(parkList), SmtBadgeLoss::getParkId, parkList)
				.orderByDesc(SmtBadgeLoss::getCreateTime));
	}

	@Override
	public Boolean saveBadgeLoss(Integer parkId, String staffNo) {
		SmtBadgeLoss badgeLoss = new SmtBadgeLoss();
		//获得员工信息
		SmtStaff smtStaff = smtStaffService.getSimpleSttaffByBadge(staffNo);
		//获得园区信息
		SmtPark smtPark = smtParkService.getById(parkId);
		if(Objects.isNull(smtStaff)) {
			throw new SmartException("员工信息关联失败");
		}
		badgeLoss.setBadge(staffNo);
		badgeLoss.setCompName(smtStaff.getCompName());
		badgeLoss.setDepName(smtStaff.getDepName());
		badgeLoss.setName(smtStaff.getName());
		badgeLoss.setParkId(parkId);
		badgeLoss.setCreateTime(LocalDateTime.now());
		if(Objects.nonNull(smtPark)) {
			badgeLoss.setParkName(smtPark.getParkName());
		}
		return this.save(badgeLoss);
	}

	@Override
	public ResponseEntity<byte[]> downLoadExcel(SmtBadgeLoss smtBadgeLoss) {
		//获得导出数据
		List<SmtBadgeLoss> list = this.list(Wrappers.query(smtBadgeLoss));
		if(CollectionUtils.isEmpty(list)) {
			throw new SmartException("暂无挂失记录");
		}
		List<BadgeLossExcelRespDTO> data = BeanUtils.batchTransform(BadgeLossExcelRespDTO.class, list);
		ResponseEntity<byte[]> responseEntity;
		String fileName = ExportTypeEnum.BADGE_LOSS.getDesc() + SymbolConstants.FULL_POINT + ExportTypeEnum.BADGE_LOSS.getFileSuffix();
		try (Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(), BadgeLossExcelRespDTO.class, data)){
			responseEntity = IOUtils.getExcelResp(fileName, workbook);
		}catch (IOException e){
			log.error("excel导出异常", e);
			throw new SmartException(ExceptionEnum.UNKNOWN.getCode(), "excel导出异常");
		}
		return responseEntity;
	}
}
