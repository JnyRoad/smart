package com.tce.smart.platform.core.service.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.enums.ExceptionEnum;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.platform.api.dto.req.DeviceAuthPersonReqDTO;
import com.tce.smart.platform.api.dto.resp.DeviceAuthPersonRespDTO;
import com.tce.smart.platform.core.dto.DeviceDataDTO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtDevicePerson;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtTaskDownRecord;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.platform.core.mapper.SmtDevicePersonMapper;
import com.tce.smart.platform.core.mapper.SmtDeviceTaskMapper;
import com.tce.smart.platform.core.mapper.SmtTaskDownRecordMapper;
import com.tce.smart.platform.core.service.SmtDevicePersonService;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.vo.DeviceTaskPersonVO;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 设备人员关联
 *
 * @author 王艳勇
 * @date 2019-04-15 15:15:38
 */
@Slf4j
@Service
public class SmtDevicePersonServiceImpl extends ServiceImpl<SmtDevicePersonMapper, SmtDevicePerson> implements SmtDevicePersonService {

	@Resource
	private SmtDeviceTaskMapper smtDeviceTaskMapper;

	@Resource
	private SmtTaskDownRecordMapper taskDownRecordMapper;

	@Resource
	private SmtDeviceMapper smtDeviceMapper;

	@Resource
	private SmtImageService smtImageService;

	@Value("${spring.image.base-url:}")
	private String imageUrl;

	@Value("${smart.xc-park-id}")
	private Integer xcParkId;

	@Override
	public IPage getDevicePerson(Page page, DeviceDataDTO devicePersonDTO) {
		SmtDevice device = smtDeviceMapper.selectById(devicePersonDTO.getDeviceId());
		if (device != null && device.getIsSync() != null && device.getIsSync() == 1) {
			return this.baseMapper.getISCDevicePerson(page, devicePersonDTO);
		}
		return this.baseMapper.getDevicePerson(page, devicePersonDTO);
	}

	@Override
	public ResponseEntity<byte[]> exportAuthPerson() {
		long current = 0;
		long size = 100;
		Page page = new Page(current, size);
		DeviceAuthPersonReqDTO reqDTO = new DeviceAuthPersonReqDTO();
		List<DeviceAuthPersonRespDTO> authPersonDTOS = new ArrayList<>();
		do {
			current++;
			page.setCurrent(current);
			IPage<DeviceAuthPersonRespDTO> authPersonPage = getDeviceAuthPerson(page, reqDTO);
			if (CollectionUtil.isEmpty(authPersonPage.getRecords())) {
				break;
			}
			authPersonDTOS.addAll(authPersonPage.getRecords());
		} while (page.hasNext());

		if (CollectionUtil.isEmpty(authPersonDTOS)) {
			throw new TCEException(CommonConstants.SUCCESS, "查询无数据");
		}

		ResponseEntity<byte[]> responseEntity;
		try (Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(), DeviceAuthPersonRespDTO.class, authPersonDTOS)) {
			String fileName = "授权人员导出";
			responseEntity = IOUtils.getExcelResponse(fileName, workbook);
		} catch (IOException e) {
			log.error("excel导出异常", e);
			throw new TCEException(ExceptionEnum.UNKNOWN.getCode(), "excel导出异常");
		}
		return responseEntity;
	}

	@Override
	public IPage<DeviceAuthPersonRespDTO> getDeviceAuthPerson(Page page, DeviceAuthPersonReqDTO authPersonReqDTO) {
		SmtDevice device = null;
		if (StrUtil.isNotBlank(authPersonReqDTO.getDeviceId())) {
			device = smtDeviceMapper.selectById(authPersonReqDTO.getDeviceId());
		}
		if (device != null && device.getIsSync() != null && device.getIsSync() == 1) {
			return parseData(this.baseMapper.getISCDeviceAuthPerson(page, authPersonReqDTO));
		}
		if (Objects.nonNull(authPersonReqDTO.getParkId()) && authPersonReqDTO.getParkId().equals(xcParkId)) {
			return parseData(this.baseMapper.getISCDeviceAuthPerson(page, authPersonReqDTO));
		}
		return parseData(this.baseMapper.getDeviceAuthPerson(page, authPersonReqDTO));
	}

	private IPage<DeviceAuthPersonRespDTO> parseData(IPage<DeviceAuthPersonRespDTO> dataPage) {
		List<DeviceAuthPersonRespDTO> records = dataPage.getRecords();
		if (CollUtil.isEmpty(records)) {
			return dataPage;
		}
		records.forEach(item -> {
			String general = item.getGeneral();
			if (StrUtil.isNotBlank(general)) {
				String[] info = general.split("-");
				if (info.length == 1) {
					item.setStaffName(info[0]);
				} else if (info.length == 2) {
					item.setBadge(info[0]);
					item.setStaffName(info[1]);
				}
			}
		});
		return dataPage;
	}

	@Override
	public List<SmtDeviceTask> listSmtDeviceTask(SmtDeviceTask smtDeviceTask, List<Integer> parkIds) {
		smtDeviceTask.setStatus(DeviceTaskConstants.DOWN_SUCCESS);
		smtDeviceTask.setDeviceType(DeviceTaskConstants.CARD);
		return smtDeviceTaskMapper.listSmtDeviceTask(smtDeviceTask, parkIds);
	}

	@Override
	public List<DeviceTaskPersonVO> listDownRecord(SmtDeviceTask smtDeviceTask,List<Integer> parkIds) {
		List<SmtTaskDownRecord> taskDownRecords = taskDownRecordMapper.selectList(new LambdaQueryWrapper<SmtTaskDownRecord>()
				.like(SmtTaskDownRecord::getGeneral, smtDeviceTask.getGeneral())
				.in(SmtTaskDownRecord::getParkId, parkIds)
				.orderByAsc(SmtTaskDownRecord::getCreateTime)
		);
		List<DeviceTaskPersonVO> taskPersonVOS = new ArrayList<>();
		if(CollectionUtil.isNotEmpty(taskDownRecords)){
			Map<String, List<SmtTaskDownRecord>> listMap = taskDownRecords.stream().collect(Collectors.groupingBy(SmtTaskDownRecord::getCardNo));
			for(String cardNo : listMap.keySet()){
				DeviceTaskPersonVO personVO = new DeviceTaskPersonVO();
				personVO.setCardNo(cardNo);
				personVO.setName(listMap.get(cardNo).get(0).getGeneral());
				String imageUrl = smtImageService.buildImageUrl(this.imageUrl, listMap.get(cardNo).get(0).getImageId());
				personVO.setFaceImage(imageUrl);
				taskPersonVOS.add(personVO);
			}
		}
		return taskPersonVOS;
	}
}
