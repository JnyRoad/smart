package com.tce.smart.platform.core.service.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.enums.ExceptionEnum;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.platform.api.dto.req.DeviceAuthPersonReqDTO;
import com.tce.smart.platform.api.dto.req.DeviceAuthVehicleReqDTO;
import com.tce.smart.platform.api.dto.resp.DeviceAuthPersonRespDTO;
import com.tce.smart.platform.api.dto.resp.DeviceAuthVehicleRespDTO;
import com.tce.smart.platform.core.dto.DeviceDataDTO;
import com.tce.smart.platform.core.dto.DeviceVehicleDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtDeviceVehicle;
import com.tce.smart.platform.core.mapper.SmtDeviceTaskMapper;
import com.tce.smart.platform.core.mapper.SmtDeviceVehicleMapper;
import com.tce.smart.platform.core.service.SmtDeviceVehicleService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.util.IOUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 设备车辆关联
 *
 * @author 王艳勇
 * @date 2019-04-16 16:06:14
 */
@Slf4j
@Service
@AllArgsConstructor
public class SmtDeviceVehicleServiceImpl extends ServiceImpl<SmtDeviceVehicleMapper, SmtDeviceVehicle> implements SmtDeviceVehicleService {

	private final SmtDeviceTaskMapper smtDeviceTaskMapper;

	@Override
	public IPage<DeviceVehicleDTO> getDeviceVehicle(Page page, DeviceDataDTO deviceDataDTO) {
		return this.baseMapper.getDeviceVehicle(page, deviceDataDTO);
	}

	@Override
	public IPage<DeviceAuthVehicleRespDTO> getDeviceAuthVehicle(Page page, DeviceAuthVehicleReqDTO reqDTO) {
		return this.baseMapper.getDeviceAuthVehiclePage(page, reqDTO);
	}

	@Override
	public ResponseEntity<byte[]> exportAuthPerson() {
		long current = 0;
		long size = 100;
		Page page = new Page(current, size);
		DeviceAuthVehicleReqDTO reqDTO = new DeviceAuthVehicleReqDTO();
		List<DeviceAuthVehicleRespDTO> authVehicleRespDTOS = new ArrayList<>();
		do {
			current++;
			page.setCurrent(current);
			IPage<DeviceAuthVehicleRespDTO> authVehiclePage = getDeviceAuthVehicle(page, reqDTO);
			if (CollectionUtil.isEmpty(authVehiclePage.getRecords())) {
				break;
			}
			authVehicleRespDTOS.addAll(authVehiclePage.getRecords());
		} while (page.hasNext());

		if (CollectionUtil.isEmpty(authVehicleRespDTOS)) {
			throw new TCEException(CommonConstants.SUCCESS, "查询无数据");
		}

		ResponseEntity<byte[]> responseEntity;
		try (Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(), DeviceAuthVehicleRespDTO.class, authVehicleRespDTOS)) {
			String fileName = "授权车辆导出";
			responseEntity = IOUtils.getExcelResponse(fileName, workbook);
		} catch (IOException e) {
			log.error("excel导出异常", e);
			throw new TCEException(ExceptionEnum.UNKNOWN.getCode(), "excel导出异常");
		}
		return responseEntity;
	}

	@Override
	public List<SmtDeviceTask> listSmtDeviceTask(SmtDeviceTask smtDeviceTask, List<Integer> parkIds) {
		smtDeviceTask.setStatus(DeviceTaskConstants.DOWN_SUCCESS);
		smtDeviceTask.setDeviceType(DeviceTaskConstants.CAR);
		return smtDeviceTaskMapper.listSmtDeviceTask(smtDeviceTask, parkIds);
	}
}
