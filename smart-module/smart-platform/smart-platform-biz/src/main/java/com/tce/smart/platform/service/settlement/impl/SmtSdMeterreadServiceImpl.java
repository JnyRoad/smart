package com.tce.smart.platform.service.settlement.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.enums.ExceptionEnum;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.RoomSdRuleDTO;
import com.tce.smart.platform.api.dto.StatementDetailDTO;
import com.tce.smart.platform.api.dto.req.*;
import com.tce.smart.platform.api.dto.req.sddto.RoomSDMeterreadReqDTO;
import com.tce.smart.platform.api.dto.req.sddto.SdMeterreadDetailReqDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.DormitorySDMeterreadNewRespDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.DormitorySDMeterreadRespDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.RoomSDMeterreadRespDTO;
import com.tce.smart.platform.core.dto.GenerateStatementDTO;
import com.tce.smart.platform.core.dto.SmtSdMeterreadDTO;
import com.tce.smart.platform.core.dto.StaffInRoomNumDTO;
import com.tce.smart.platform.core.dto.commonsd.DormitorySDMeterreadDTO;
import com.tce.smart.platform.core.dto.commonsd.StaffSDRuleRespDTO;
import com.tce.smart.platform.core.dto.meter.MeterReadConfigDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.*;
import com.tce.smart.platform.core.vo.SmtSdMeterreadVO;
import com.tce.smart.platform.helper.SdChangeHelper;
import com.tce.smart.platform.service.SmtDormitoryFloorService;
import com.tce.smart.platform.service.SmtDormitoryOutRemarkService;
import com.tce.smart.platform.service.SmtDormitoryRoomService;
import com.tce.smart.platform.service.SmtDormitoryService;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryPersonService;
import com.tce.smart.platform.service.settlement.*;
import com.tce.smart.platform.utils.NumberUtils;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.IOUtils;
import com.tce.smart.tool.util.ToolUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: SmtSdMeterreadServiceImpl
 * @date: 2020-07-10 9:46
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
public class SmtSdMeterreadServiceImpl extends ServiceImpl<SmtSdMeterreadMapper, SmtSdMeterread> implements SmtSdMeterreadService {

	@Autowired
	private SmtSdMeterreadMapper smtSdMeterreadMapper;

	@Autowired
	private SmtDormitoryRoomService smtDormitoryRoomService;

	@Autowired
	private SmtDormitoryService smtDormitoryService;

	@Autowired
	private SmtDormitoryFloorService smtDormitoryFloorService;

	@Autowired
	private SmtSdMeterreadDetailService smtSdMeterreadDetailService;

	@Autowired
	private SmtDormitoryStaffMapper smtDormitoryStaffMapper;

	@Autowired
	private SmtDormitoryStaffHistoryMapper smtDormitoryStaffHistoryMapper;

	@Autowired
	private SmtStaffStatementDetailService smtStaffStatementDetailService;
	@Autowired
	private SmtMeterreadCnfigService smtMeterreadCnfigService;
	@Autowired
	private SmtCommonSDService smtCommonSDService;
	@Autowired
	private SmtSDTemplatesMapper smtSDTemplatesMapper;
	@Autowired
	private SmtCommonSDMeterreadMapper smtCommonSDMeterreadMapper;
	@Autowired
	private SmtCommonSDRoomService smtCommonSDRoomService;
	@Autowired
	private SmtStaffSDMHistoryService smtStaffSDMHistoryService;
	@Autowired
	private SmtTemplatesRuleService smtTemplatesRuleService;
	@Autowired
	private SmtDormitoryOutRemarkService smtDormitoryOutRemarkService;
	@Autowired
	private SmtDormitoryPersonService smtDormitoryPersonService;
	@Autowired
	private SdChangeHelper sdChangeHelper;

	@Value("${smart.xc-park-id:0}")
	private Integer xcParkId;

	@Override
	public IPage<SmtSdMeterreadVO> getSDMeterreadPage(Page page, SmtSdMeterreadReqDTO smtSdMeterreadReqDTO) {
		List<Integer> parkIdList = smtDormitoryPersonService.getParkId(SecurityUtils.getUser().getUsername());
		if (CollUtil.isEmpty(parkIdList)) {
			parkIdList = SecurityUtils.getUser().getParkIdList();
		}
		SmtSdMeterreadDTO smtSdMeterreadDTO = new SmtSdMeterreadDTO();
		BeanUtil.copyProperties(smtSdMeterreadReqDTO, smtSdMeterreadDTO);
		IPage<SmtSdMeterreadVO> sdMeterreadPage = smtSdMeterreadMapper.getSDMeterreadPage(page, smtSdMeterreadDTO, parkIdList);
		List<SmtSdMeterreadVO> records = sdMeterreadPage.getRecords();
		if (CollectionUtil.isNotEmpty(records)) {
			List<Long> mrIdList = records.stream().map(SmtSdMeterreadVO::getId).collect(Collectors.toList());
			List<SmtSdMeterreadDetail> meterreadDetails = smtSdMeterreadDetailService.list(new LambdaQueryWrapper<SmtSdMeterreadDetail>().in(SmtSdMeterreadDetail::getMrId, mrIdList));
			Map<Long, List<SmtSdMeterreadDetail>> collect = meterreadDetails.stream().collect(Collectors.groupingBy(SmtSdMeterreadDetail::getMrId));
			sdMeterreadPage.getRecords().forEach(item -> {
				if (collect.containsKey(item.getId())) {
					List<SmtSdMeterreadDetail> tempMeterreadDetails = collect.get(item.getId());

					List<SmtSdMeterreadVO.ReviseInfo> reviseInfos = new ArrayList<>();
					Integer isRevise = SdStatementReviseEnum.NON_REVISE.getCode();
					//热水
					List<SmtSdMeterreadDetail> hotWatercollect = tempMeterreadDetails.stream()
							.filter(cat -> cat.getCategoryId().equals(SDCategoryEnum.HOT_WATER.getCode()))
							.collect(Collectors.toList());
					if (CollectionUtil.isNotEmpty(hotWatercollect)) {
						SmtSdMeterreadDetail hotWaterDetail = hotWatercollect.get(0);
						Double preWaterNum = (hotWaterDetail.getRevPreMonthNum() != null ? hotWaterDetail.getRevPreMonthNum() : hotWaterDetail.getPreMonthNum());
						Double hotWater = BigDecimal.valueOf(hotWaterDetail.getCurMonthNum()).subtract(new BigDecimal(preWaterNum)).setScale(2, RoundingMode.HALF_UP).doubleValue();

						item.setHotWater(hotWater);
						SmtSdMeterreadVO.ReviseInfo reviseInfo = getReviseInfo(hotWaterDetail, item.getMeterMonth());
						if (null != reviseInfo) {
							//上月止度修正过
							isRevise = SdStatementReviseEnum.REVISE.getCode();
							reviseInfos.add(reviseInfo);
						}
					}
					//冷水
					List<SmtSdMeterreadDetail> coldWatercollect = tempMeterreadDetails.stream()
							.filter(cat -> cat.getCategoryId().equals(SDCategoryEnum.COLD_WATER.getCode()))
							.collect(Collectors.toList());
					if (CollectionUtil.isNotEmpty(coldWatercollect)) {
						SmtSdMeterreadDetail coldWaterDetail = coldWatercollect.get(0);
						Double preColdWater = (coldWaterDetail.getRevPreMonthNum() != null ? coldWaterDetail.getRevPreMonthNum() : coldWaterDetail.getPreMonthNum());
						Double coldWater = BigDecimal.valueOf(coldWaterDetail.getCurMonthNum()).subtract(new BigDecimal(preColdWater)).setScale(2, RoundingMode.HALF_UP).doubleValue();
						item.setColdWater(coldWater);
						SmtSdMeterreadVO.ReviseInfo reviseInfo = getReviseInfo(coldWaterDetail, item.getMeterMonth());
						if (null != reviseInfo) {
							//上月止度修正过
							isRevise = SdStatementReviseEnum.REVISE.getCode();
							reviseInfos.add(reviseInfo);
						}
					}
					//电
					List<SmtSdMeterreadDetail> electricCollect = tempMeterreadDetails.stream()
							.filter(cat -> cat.getCategoryId().equals(SDCategoryEnum.ELECTRIC.getCode()))
							.collect(Collectors.toList());
					if (CollectionUtil.isNotEmpty(electricCollect)) {
						SmtSdMeterreadDetail electricDetail = electricCollect.get(0);
						Double preElectric = (electricDetail.getRevPreMonthNum() != null ? electricDetail.getRevPreMonthNum() : electricDetail.getPreMonthNum());
						Double electric = BigDecimal.valueOf(electricDetail.getCurMonthNum()).subtract(new BigDecimal(preElectric)).setScale(2, RoundingMode.HALF_UP).doubleValue();
						item.setElectric(electric);

						SmtSdMeterreadVO.ReviseInfo reviseInfo = getReviseInfo(electricDetail, item.getMeterMonth());
						if (null != reviseInfo) {
							//上月止度修正过
							isRevise = SdStatementReviseEnum.REVISE.getCode();
							reviseInfos.add(reviseInfo);
						}
					}
					item.setIsRevise(isRevise);
					item.setReviseInfo(reviseInfos);
				}
			});
		}
		return sdMeterreadPage;
	}

	@Override
	public List<SmtSdMeterreadVO> getRoomSDMeterreadInfo(SmtSdMeterreadReqDTO smtSdMeterreadReqDTO) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		SmtSdMeterreadDTO smtSdMeterreadDTO = new SmtSdMeterreadDTO();
		BeanUtil.copyProperties(smtSdMeterreadReqDTO, smtSdMeterreadDTO);
		List<SmtSdMeterreadVO> sdMeterreadList = smtSdMeterreadMapper.getRoomSDMeterread(smtSdMeterreadDTO, parkIdList);
		if (CollectionUtil.isNotEmpty(sdMeterreadList)) {
			List<Integer> roomId = sdMeterreadList.stream().map(SmtSdMeterreadVO::getRoomId).collect(Collectors.toList());
			List<DormitorySDMeterreadDTO> meterreadDetails = this.baseMapper.getRoomsSDMeterread(roomId, smtSdMeterreadReqDTO.getMeterMonth());
			Map<Integer, List<DormitorySDMeterreadDTO>> collect = meterreadDetails.stream().collect(Collectors.groupingBy(DormitorySDMeterreadDTO::getRoomId));
			sdMeterreadList.forEach(item -> {
				if (collect.containsKey(item.getRoomId())) {
					List<DormitorySDMeterreadDTO> dormitorySDMeterreadDTOs = collect.get(item.getRoomId());

					List<SmtSdMeterreadVO.ReviseInfo> reviseInfos = new ArrayList<>();
					Integer isRevise = SdStatementReviseEnum.NON_REVISE.getCode();
					//热水
					List<DormitorySDMeterreadDTO> hotWatercollect = dormitorySDMeterreadDTOs.stream()
							.filter(cat -> cat.getCategoryId().equals(SDCategoryEnum.HOT_WATER.getCode()))
							.collect(Collectors.toList());
					if (CollectionUtil.isNotEmpty(hotWatercollect)) {
						DormitorySDMeterreadDTO hotWaterDetail = hotWatercollect.get(0);
						Double preWaterNum = (hotWaterDetail.getRevPreMonthNum() != null ? hotWaterDetail.getRevPreMonthNum() : hotWaterDetail.getPreMonthNum());
						Double hotWater = BigDecimal.valueOf(hotWaterDetail.getCurMonthNum()).subtract(new BigDecimal(preWaterNum)).setScale(2, RoundingMode.HALF_UP).doubleValue();

						item.setHotWater(hotWater);
						SmtSdMeterreadVO.ReviseInfo reviseInfo = getReviseInfo(hotWaterDetail, item.getMeterMonth());
						if (null != reviseInfo) {
							//上月止度修正过
							isRevise = SdStatementReviseEnum.REVISE.getCode();
							reviseInfos.add(reviseInfo);
						}
					}
					//冷水
					List<DormitorySDMeterreadDTO> coldWatercollect = dormitorySDMeterreadDTOs.stream()
							.filter(cat -> cat.getCategoryId().equals(SDCategoryEnum.COLD_WATER.getCode()))
							.collect(Collectors.toList());
					if (CollectionUtil.isNotEmpty(coldWatercollect)) {
						DormitorySDMeterreadDTO coldWaterDetail = coldWatercollect.get(0);
						Double preColdWater = (coldWaterDetail.getRevPreMonthNum() != null ? coldWaterDetail.getRevPreMonthNum() : coldWaterDetail.getPreMonthNum());
						Double coldWater = BigDecimal.valueOf(coldWaterDetail.getCurMonthNum()).subtract(new BigDecimal(preColdWater)).setScale(2, RoundingMode.HALF_UP).doubleValue();
						item.setColdWater(coldWater);
						SmtSdMeterreadVO.ReviseInfo reviseInfo = getReviseInfo(coldWaterDetail, item.getMeterMonth());
						if (null != reviseInfo) {
							//上月止度修正过
							isRevise = SdStatementReviseEnum.REVISE.getCode();
							reviseInfos.add(reviseInfo);
						}
					}
					//电
					List<DormitorySDMeterreadDTO> electricCollect = dormitorySDMeterreadDTOs.stream()
							.filter(cat -> cat.getCategoryId().equals(SDCategoryEnum.ELECTRIC.getCode()))
							.collect(Collectors.toList());
					if (CollectionUtil.isNotEmpty(electricCollect)) {
						DormitorySDMeterreadDTO electricDetail = electricCollect.get(0);
						Double preElectric = (electricDetail.getRevPreMonthNum() != null ? electricDetail.getRevPreMonthNum() : electricDetail.getPreMonthNum());
						Double electric = BigDecimal.valueOf(electricDetail.getCurMonthNum()).subtract(new BigDecimal(preElectric)).setScale(2, RoundingMode.HALF_UP).doubleValue();
						item.setElectric(electric);

						SmtSdMeterreadVO.ReviseInfo reviseInfo = getReviseInfo(electricDetail, item.getMeterMonth());
						if (null != reviseInfo) {
							//上月止度修正过
							isRevise = SdStatementReviseEnum.REVISE.getCode();
							reviseInfos.add(reviseInfo);
						}
					}
					item.setStatus(meterreadDetails.get(0).getStatus());
					item.setStatementStatus(meterreadDetails.get(0).getStatementStatus());
					item.setIsRevise(isRevise);
					item.setReviseInfo(reviseInfos);
				}
			});
		}
		return sdMeterreadList;
	}

	/**
	 * 查询上月止度修改数据
	 *
	 * @param meterreadDetail
	 * @param meterMonth
	 * @return
	 */
	private SmtSdMeterreadVO.ReviseInfo getReviseInfo(SmtSdMeterreadDetail meterreadDetail, Date meterMonth) {
		if (null != meterreadDetail.getIsRevise() && meterreadDetail.getIsRevise().equals(SdStatementReviseEnum.REVISE.getCode())) {
			//上月止度修正过
			SmtSdMeterreadVO.ReviseInfo reviseInfo = new SmtSdMeterreadVO.ReviseInfo();
			reviseInfo.setMeterUser(meterreadDetail.getMeterUser());
			reviseInfo.setCategoryId(meterreadDetail.getCategoryId());
			reviseInfo.setMeterMonth(meterMonth);
			reviseInfo.setCreateTime(meterreadDetail.getCreateTime());
			reviseInfo.setPreMonthNum(meterreadDetail.getPreMonthNum());
			reviseInfo.setRevPreMonthNum(meterreadDetail.getRevPreMonthNum());
			return reviseInfo;
		}
		return null;
	}

	private SmtSdMeterreadVO.ReviseInfo getReviseInfo(DormitorySDMeterreadDTO meterreadDetail, Date meterMonth) {
		if (null != meterreadDetail.getIsRevise() && meterreadDetail.getIsRevise().equals(SdStatementReviseEnum.REVISE.getCode())) {
			//上月止度修正过
			SmtSdMeterreadVO.ReviseInfo reviseInfo = new SmtSdMeterreadVO.ReviseInfo();
			reviseInfo.setMeterUser(meterreadDetail.getMeterUser());
			reviseInfo.setCategoryId(meterreadDetail.getCategoryId());
			reviseInfo.setMeterMonth(meterMonth);
			reviseInfo.setCreateTime(meterreadDetail.getCreateTime());
			reviseInfo.setPreMonthNum(meterreadDetail.getPreMonthNum());
			reviseInfo.setRevPreMonthNum(meterreadDetail.getRevPreMonthNum());
			return reviseInfo;
		}
		return null;
	}

	@Override
	public DormitorySDMeterreadRespDTO getRoomSDMeterread(Integer roomId, Date meterMonth, SmtCommonSDMeterreadService smtCommonSDMeterreadService) {
		//查询房间的抄表数据
		List<DormitorySDMeterreadDTO> dormitorySDMeterreadDTOS = this.baseMapper.getDormitorySDMeterread(roomId, meterMonth);
		DormitorySDMeterreadRespDTO dormitorySDMeterreadRespDTO = new DormitorySDMeterreadRespDTO();
		dormitorySDMeterreadRespDTO.setRoomId(roomId);
		dormitorySDMeterreadRespDTO.setMeterMonth(meterMonth);
		dormitorySDMeterreadRespDTO.setStatus(SdMeterreadStatusEnum.NON_METER_READ.getCode());
		dormitorySDMeterreadRespDTO.setStatementStatus(SdStatementStatusEnum.NON_STATEMENT.getCode());

		//如果本月已超过表
		if (CollectionUtil.isNotEmpty(dormitorySDMeterreadDTOS)) {
			List<DormitorySDMeterreadRespDTO.Cate> cateList = new ArrayList<>();
			dormitorySDMeterreadRespDTO.setStatus(dormitorySDMeterreadDTOS.get(0).getStatus());
			dormitorySDMeterreadRespDTO.setStatementStatus(dormitorySDMeterreadDTOS.get(0).getStatementStatus());
			dormitorySDMeterreadDTOS.forEach(item -> {
				DormitorySDMeterreadRespDTO.Cate cate = new DormitorySDMeterreadRespDTO.Cate();
				cate.setCategoryId(item.getCategoryId());
				if (null != item.getRevPreMonthNum()) {
					cate.setPreMonthNum(item.getRevPreMonthNum());
				} else {
					cate.setPreMonthNum(item.getPreMonthNum());
				}
				cate.setIsRevise(item.getIsRevise());
				cate.setCurMonthNum(item.getCurMonthNum());
				//人均每天用量
				double doubleValue = 0.0;
				if (null != item.getTotalInStay() && !item.getTotalInStay().equals(0)) {
					doubleValue = BigDecimal.valueOf(cate.getCurMonthNum() - cate.getPreMonthNum()).divide(new BigDecimal(item.getTotalInStay()), 2, RoundingMode.HALF_UP).setScale(2).doubleValue();
				}
				cate.setAvgNum(doubleValue);

				cateList.add(cate);
			});
			dormitorySDMeterreadRespDTO.setDormitoryCates(cateList);
		}

		//查询上月已结算的抄表数据
		Date preMonth = ToolUtils.getCalDate(meterMonth, Calendar.MONTH, -1);
		List<DormitorySDMeterreadDTO> preMeterreadDTOS = this.baseMapper.getDormitorySDMeterread(roomId, preMonth);
		if (CollectionUtil.isNotEmpty(preMeterreadDTOS)) {
			List<DormitorySDMeterreadRespDTO.Cate> cateList = dormitorySDMeterreadRespDTO.getDormitoryCates();
			if (CollectionUtil.isEmpty(cateList)) {
				cateList = new ArrayList<>();
				dormitorySDMeterreadRespDTO.setDormitoryCates(cateList);
			}
			List<Integer> categoryIds = cateList.stream().map(a -> a.getCategoryId()).collect(Collectors.toList());
			for (DormitorySDMeterreadDTO item : preMeterreadDTOS) {
				if (!categoryIds.contains(item.getCategoryId())) {
					DormitorySDMeterreadRespDTO.Cate cate = new DormitorySDMeterreadRespDTO.Cate();
					cate.setCategoryId(item.getCategoryId());
					//上月止度
					cate.setPreMonthNum(item.getCurMonthNum());
					cateList.add(cate);
				}
			}
		}

		//查询公摊水电表记录
		List<DormitorySDMeterreadRespDTO.CommonCate> commonCates = queryRoomCommonSDInfo(roomId, meterMonth, smtCommonSDMeterreadService);
		dormitorySDMeterreadRespDTO.setCommonCates(commonCates);
		return dormitorySDMeterreadRespDTO;
	}

	/**
	 * 查询房间指定月份的公摊水电数据
	 *
	 * @param roomId
	 * @param meterMonth
	 * @return
	 */
	@Override
	public List<DormitorySDMeterreadRespDTO.CommonCate> queryRoomCommonSDInfo(Integer roomId, Date meterMonth, SmtCommonSDMeterreadService smtCommonSDMeterreadService) {

		List<SmtCommonSD> commonSDList = smtCommonSDService.list();
		List<SmtCommonSD> existCommonSDList = new ArrayList<>();
		commonSDList.forEach(item -> {
			List<String> strings = Arrays.asList(item.getRoomList().split(","));
			if (strings.contains(roomId.toString())) {
				existCommonSDList.add(item);
			}
		});

		List<DormitorySDMeterreadRespDTO.CommonCate> commonCates = new ArrayList<>();

		if (CollectionUtil.isEmpty(existCommonSDList)) {
			return commonCates;
		}

		List<Long> collect = existCommonSDList.stream().map(SmtCommonSD::getId).collect(Collectors.toList());
		List<SmtCommonSDMeterread> commonSDMeterreads = smtCommonSDMeterreadService.list(new LambdaQueryWrapper<SmtCommonSDMeterread>()
				.in(SmtCommonSDMeterread::getCommonId, collect)
				.eq(SmtCommonSDMeterread::getMeterMonth, meterMonth)
		);
		if (CollectionUtil.isNotEmpty(commonSDMeterreads)) {
			Map<Long, List<SmtCommonSDMeterread>> listMap = commonSDMeterreads.stream().collect(Collectors.groupingBy(SmtCommonSDMeterread::getCommonId));
			existCommonSDList.forEach(item -> {
				DormitorySDMeterreadRespDTO.CommonCate commonCate = new DormitorySDMeterreadRespDTO.CommonCate();
				commonCate.setCategoryId(item.getCategoryId());
				SmtCommonSDMeterread sdMeterread = listMap.get(item.getId()).get(0);
				Double realPreNum = (null != sdMeterread.getRevPreMonthNum() ? sdMeterread.getRevPreMonthNum() : sdMeterread.getPreMonthNum());
				double doubleValue = new BigDecimal(sdMeterread.getCurMonthNum() - realPreNum).divide(new BigDecimal(sdMeterread.getTotalStayDays()), 2, RoundingMode.HALF_UP).setScale(2).doubleValue();
				commonCate.setAvgNum(doubleValue);

				commonCates.add(commonCate);
			});
		}
		return commonCates;
	}

	@Override
	public List<DormitorySDMeterreadRespDTO> getFloorSDMeterread(Integer floorId, Date meterMonth) {
		List<DormitorySDMeterreadDTO> floorSDMeterread = this.baseMapper.getFloorSDMeterread(floorId, meterMonth);
		List<DormitorySDMeterreadRespDTO> dtoList = new ArrayList<>();
		for (DormitorySDMeterreadDTO dto : floorSDMeterread) {
			DormitorySDMeterreadRespDTO respDTO = new DormitorySDMeterreadRespDTO();
			BeanUtils.copyProperties(dto, respDTO);
			respDTO.setMeterMonth(meterMonth);
			List<DormitorySDMeterreadRespDTO.Cate> cateList = new ArrayList<>();
			DormitorySDMeterreadRespDTO.Cate cate = new DormitorySDMeterreadRespDTO.Cate();
			cate.setCategoryId(dto.getCategoryId());
			if (null == dto.getId()) {
				//查询上月已结算的抄表数据
				Date preMonth = ToolUtils.getCalDate(meterMonth, Calendar.MONTH, -1);
				List<DormitorySDMeterreadDTO> preMeterreadDTOS = this.baseMapper.getDormitorySDMeterread(dto.getRoomId(), preMonth);
				if (CollectionUtil.isNotEmpty(preMeterreadDTOS)) {
					Map<Integer, List<DormitorySDMeterreadDTO>> sdMeterMap = preMeterreadDTOS.stream().collect(Collectors.groupingBy(a -> a.getCategoryId()));
					List<DormitorySDMeterreadDTO> dormitorySDMeterreadDTOS = sdMeterMap.get(SDCategoryEnum.ELECTRIC.getCode());
					if (CollectionUtil.isNotEmpty(dormitorySDMeterreadDTOS)) {
						//设置为上月止度
						cate.setCategoryId(SDCategoryEnum.ELECTRIC.getCode());
						cate.setPreMonthNum(dormitorySDMeterreadDTOS.get(0).getCurMonthNum());
					}
				}
			} else {
				cate.setPreMonthNum(dto.getPreMonthNum());
				if (dto.getRevPreMonthNum() != null) {
					cate.setPreMonthNum(dto.getRevPreMonthNum());
				} else if (dto.getPreMonthNum() != null && dto.getPreMonthNum().doubleValue() == -1) {
					cate.setPreMonthNum(null);
				}
				cate.setCurMonthNum(dto.getCurMonthNum());
			}

			cateList.add(cate);
			respDTO.setDormitoryCates(cateList);
			dtoList.add(respDTO);
		}
		return dtoList;
	}

	@Override
	public List<DormitorySDMeterreadNewRespDTO> getFloorSDMeterreadNew(RoomMeterQueryDTO roomMeterQueryDTO) {
		List<DormitorySDMeterreadDTO> floorSDMeterread = this.baseMapper.getFloorSDMeterreadNew(roomMeterQueryDTO.getDormitoryId(), roomMeterQueryDTO.getFloorId(),
				roomMeterQueryDTO.getRoomId(), roomMeterQueryDTO.getMeterMonth(), roomMeterQueryDTO.getDormitoryIds());
		List<DormitorySDMeterreadNewRespDTO> respDTOList = new ArrayList<>();

		if (CollectionUtil.isEmpty(floorSDMeterread)) {
			return respDTOList;
		}

		Map<Integer, SmtDormitory> dormitoryMap = new HashMap<>();

		Map<Integer, List<DormitorySDMeterreadDTO>> collect = floorSDMeterread.stream().collect(Collectors.groupingBy(DormitorySDMeterreadDTO::getRoomId));
		List<Integer> roomIdList = floorSDMeterread.stream().map(DormitorySDMeterreadDTO::getRoomId).distinct().collect(Collectors.toList());
		ArrayList<SmtDormitoryRoom> smtDormitoryRooms = new ArrayList<>();
		int count = roomIdList.size() / 1000 + (roomIdList.size() % 1000 > 0 ? 1 : 0);
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				if (i == count - 1) {
					smtDormitoryRooms.addAll(smtDormitoryRoomService.listByIds(roomIdList.subList(i * 1000, roomIdList.size())));
				} else {
					smtDormitoryRooms.addAll(smtDormitoryRoomService.listByIds(roomIdList.subList(i * 1000, (i + 1) * 1000)));
				}
			}
		} else {
			smtDormitoryRooms.addAll(smtDormitoryRoomService.listByIds(roomIdList));
		}
		Map<Integer, List<SmtDormitoryRoom>> roomCollect = smtDormitoryRooms.stream().collect(Collectors.groupingBy(SmtDormitoryRoom::getId));
		for (Integer roomId : collect.keySet()) {
			SmtDormitoryRoom room = roomCollect.get(roomId).get(0);
			DormitorySDMeterreadNewRespDTO respDTO = new DormitorySDMeterreadNewRespDTO();
			List<DormitorySDMeterreadDTO> dormitorySDMeterreadDTOS = collect.get(roomId);
			DormitorySDMeterreadDTO item = dormitorySDMeterreadDTOS.get(0);

			if (!dormitoryMap.containsKey(item.getDormitoryId())) {
				SmtDormitory dormitory = smtDormitoryService.getById(item.getDormitoryId());
				dormitoryMap.put(item.getDormitoryId(), dormitory);
			}

			respDTO.setMrId(item.getMrId());
			respDTO.setDormitoryId(item.getDormitoryId());
			respDTO.setDormitoryName(dormitoryMap.get(item.getDormitoryId()).getDormitoryName());
			respDTO.setFloorId(item.getFloorId());
			respDTO.setRoomId(item.getRoomId());
			respDTO.setRoomName(StringUtils.isNotEmpty(room.getAliasName()) ? room.getAliasName() : room.getRoomName().toString());
			respDTO.setRoomSex(item.getRoomSex());
			respDTO.setTempId(item.getSdTemplateId());
			respDTO.setMeterMonth(roomMeterQueryDTO.getMeterMonth());
			respDTO.setStatementStatus(item.getStatementStatus());
			respDTO.setStatus(item.getStatus());
			respDTO.setInDays(item.getTotalInStay());

			if (null == item.getId()) {
				//没有抄表ID 表示房间没有抄表
				handleNonMeter(respDTO, roomMeterQueryDTO.getMeterMonth());
			} else if (SdStatementStatusEnum.STATEMENT.getCode().equals(respDTO.getStatementStatus())) {
				//已结算的数据
				handleAlreadyStatement(respDTO, dormitorySDMeterreadDTOS);
			} else if (SdMeterreadStatusEnum.ALL_METER_READ.getCode().equals(respDTO.getStatus())) {
				//已抄表未结算的数据
				handleAlreadyMeter(respDTO, dormitorySDMeterreadDTOS, roomMeterQueryDTO.getMeterMonth());
			}

			respDTOList.add(respDTO);
		}

		respDTOList = respDTOList.stream().sorted(Comparator.comparing(DormitorySDMeterreadNewRespDTO::getRoomName)).collect(Collectors.toList());
		NumberUtils.formatDormitorySdMeterRead(respDTOList);
		sdChangeHelper.readSdChange(respDTOList);
		return respDTOList;
	}

	@Override
	public List<DormitorySDMeterreadNewRespDTO> getDormitorySDMeterread(DormitoryMeterQueryDTO dormitoryMeterQueryDTO) {
		List<DormitorySDMeterreadNewRespDTO> dormitorySDMeterreadNewRespDTOS = new ArrayList<>();
		if (StringUtils.isNotEmpty(dormitoryMeterQueryDTO.getDormitoryIds())) {
			List<Integer> dorIds = Arrays.asList(dormitoryMeterQueryDTO.getDormitoryIds().split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
			for (Integer dorId : dorIds) {
				RoomMeterQueryDTO queryDTO = new RoomMeterQueryDTO();
				queryDTO.setDormitoryId(dorId);
				queryDTO.setMeterMonth(dormitoryMeterQueryDTO.getMeterMonth());
				List<DormitorySDMeterreadNewRespDTO> floorSDMeterreadNew = getFloorSDMeterreadNew(queryDTO);
				dormitorySDMeterreadNewRespDTOS.addAll(floorSDMeterreadNew);
			}
		}
		dormitorySDMeterreadNewRespDTOS = dormitorySDMeterreadNewRespDTOS.stream()
				.sorted(Comparator.comparing(DormitorySDMeterreadNewRespDTO::getDormitoryName).thenComparing(DormitorySDMeterreadNewRespDTO::getRoomName))
				.collect(Collectors.toList());
		return dormitorySDMeterreadNewRespDTOS;
	}

	/**
	 * 处理已结算的数据
	 *
	 * @param respDTO
	 * @param dormitorySDMeterreadDTOS
	 */
	private void handleAlreadyStatement(DormitorySDMeterreadNewRespDTO respDTO, List<DormitorySDMeterreadDTO> dormitorySDMeterreadDTOS) {
		//房间总金额
		BigDecimal totalAmount = BigDecimal.ZERO;

		for (DormitorySDMeterreadDTO detl : dormitorySDMeterreadDTOS) {
			Double preMonthNum = detl.getRevPreMonthNum() == null ? detl.getPreMonthNum() : detl.getRevPreMonthNum();
			Double curMonthNum = detl.getCurMonthNum();
			Double useNum = curMonthNum - preMonthNum;


			Double curStdQty = detl.getCurStdQty();
			Double curOverFee = detl.getCurOverFee();
			Double payUseNum = useNum - curStdQty;
			payUseNum = new BigDecimal(payUseNum).setScale(2, RoundingMode.HALF_UP).doubleValue();

			if (SDCategoryEnum.HOT_WATER.getCode().equals(detl.getCategoryId())) {
				respDTO.setHotPreMonthNum(preMonthNum);
				respDTO.setHotCurMonthNum(curMonthNum);
				respDTO.setColdUse(useNum + (respDTO.getColdUse() != null ? respDTO.getColdUse() : 0));
//				respDTO.setHotUse(0.0);
//				respDTO.setHotOverUse(0.0);
//				respDTO.setHotQty(curStdQty);
//				respDTO.setHotOverFee(curOverFee);
				//直接加上热水的用量
				respDTO.setColdOverUse(useNum + (respDTO.getColdOverUse() != null ? respDTO.getColdOverUse() : 0));
			} else if (SDCategoryEnum.COLD_WATER.getCode().equals(detl.getCategoryId())) {
				respDTO.setColdPreMonthNum(preMonthNum);
				respDTO.setColdCurMonthNum(curMonthNum);
				respDTO.setColdUse(useNum + (respDTO.getHotUse() != null ? respDTO.getHotUse() : 0));
				respDTO.setColdQty(curStdQty);
				respDTO.setColdOverFee(detl.getCurOverFee());
				respDTO.setColdOverUse(payUseNum + (respDTO.getColdOverUse() != null ? respDTO.getColdOverUse() : 0));
			} else if (SDCategoryEnum.ELECTRIC.getCode().equals(detl.getCategoryId())) {
				respDTO.setElePreMonthNum(preMonthNum);
				respDTO.setEleCurMonthNum(curMonthNum);
				respDTO.setEleUse(useNum);
				respDTO.setEleQty(curStdQty);
				respDTO.setEleOverFee(detl.getCurOverFee());
				respDTO.setEleOverUse(payUseNum > 0 ? payUseNum : 0.0);
			}
		}

		if (respDTO.getColdOverUse() < 0) {
			respDTO.setColdOverUse(0.0);
		}

		//已结算的数据 计算冷水的总金额
		if (null != respDTO.getColdQty() && respDTO.getColdOverUse() > 0) {
			BigDecimal currAmount = BigDecimal.valueOf(respDTO.getColdOverUse()).multiply(BigDecimal.valueOf(respDTO.getColdOverFee())).setScale(4, RoundingMode.HALF_UP);
			totalAmount = totalAmount.add(currAmount);
		}

		//已结算的数据 计算电的总金额
		if (null != respDTO.getEleQty() && respDTO.getEleOverUse() > 0) {
			BigDecimal currAmount = BigDecimal.valueOf(respDTO.getEleOverUse()).multiply(BigDecimal.valueOf(respDTO.getEleOverFee())).setScale(4, RoundingMode.HALF_UP);
			totalAmount = totalAmount.add(currAmount);
		}

		respDTO.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP).doubleValue());
		respDTO.setAvgAmount(0.0);
		if (respDTO.getInDays() > 0) {
			//房间平均金额= 房间总金额 / 房间总入住天数
			respDTO.setAvgAmount(totalAmount.divide(new BigDecimal(respDTO.getInDays()), 2, RoundingMode.HALF_UP).doubleValue());
		}
	}

	/**
	 * 处理已抄表未结算的数据
	 *
	 * @param respDTO
	 * @param dormitorySDMeterreadDTOS
	 */
	private void handleAlreadyMeter(DormitorySDMeterreadNewRespDTO respDTO, List<DormitorySDMeterreadDTO> dormitorySDMeterreadDTOS, Date meterMonth) {
		//房间总金额
		BigDecimal totalAmount = BigDecimal.ZERO;

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(meterMonth);
		int curMonth = calendar.get(Calendar.MONTH) + 1;
		//查询水电模板规则
		List<SmtTemplatesRule> smtSdTemplates = smtTemplatesRuleService.list(new LambdaQueryWrapper<SmtTemplatesRule>()
				.eq(SmtTemplatesRule::getTempId, respDTO.getTempId())
				.eq(SmtTemplatesRule::getMonthNum, curMonth)
		);
		if (CollUtil.isEmpty(smtSdTemplates)) {
			return;
		}

		for (DormitorySDMeterreadDTO detl : dormitorySDMeterreadDTOS) {
			Double preMonthNum = detl.getRevPreMonthNum() == null ? detl.getPreMonthNum() : detl.getRevPreMonthNum();
			Double curMonthNum = detl.getCurMonthNum();
			Double useNum = curMonthNum - preMonthNum;

			SmtTemplatesRule smtTemplatesRule = null;
			Double curStdQty = 0.0;
			Double curOverFee = 0.0;

			if (SDCategoryEnum.HOT_WATER.getCode().equals(detl.getCategoryId()) || SDCategoryEnum.COLD_WATER.getCode().equals(detl.getCategoryId())) {
				smtTemplatesRule = smtSdTemplates.stream().filter(s -> s.getCategoryId().equals(SDCategoryEnum.COLD_WATER.getCode())).findFirst().get();
				curStdQty = smtTemplatesRule.getStandardQty();
				curOverFee = smtTemplatesRule.getOverFee().setScale(2, RoundingMode.HALF_UP).doubleValue();

				SmtDormitoryRoom room = smtDormitoryRoomService.getById(respDTO.getRoomId());
				MeterReadConfigDTO config = smtMeterreadCnfigService.calcDate(meterMonth, room.getParkId());
				Date startTime = config.getStartDate();
				Date endTime = config.getEndDate();
				List<Integer> roomIds = new ArrayList<>();
				roomIds.add(respDTO.getRoomId());
				Map<String, List<SmtStaffStatementDetail>> roomStayData = getRoomStayData(roomIds, startTime, endTime);
				if (CollectionUtil.isNotEmpty(roomStayData) && CollectionUtil.isNotEmpty(roomStayData.get(String.valueOf(respDTO.getRoomId())))) {
					curStdQty = curStdQty * roomStayData.get(String.valueOf(respDTO.getRoomId())).size();
				}
			} else {
				smtTemplatesRule = smtSdTemplates.stream().filter(s -> s.getCategoryId().equals(SDCategoryEnum.ELECTRIC.getCode())).findFirst().get();
				curStdQty = smtTemplatesRule.getStandardQty();
				curOverFee = smtTemplatesRule.getOverFee().setScale(2, RoundingMode.HALF_UP).doubleValue();
			}


			Double payUseNum = useNum - curStdQty;
			payUseNum = new BigDecimal(payUseNum).setScale(2, RoundingMode.HALF_UP).doubleValue();

			if (SDCategoryEnum.HOT_WATER.getCode().equals(detl.getCategoryId())) {
				respDTO.setHotPreMonthNum(preMonthNum);
				respDTO.setHotCurMonthNum(curMonthNum);
				respDTO.setColdUse(useNum + (respDTO.getColdUse() != null ? respDTO.getColdUse() : 0));
//				respDTO.setHotUse(0.0);
//				respDTO.setHotQty(0.0);
//				respDTO.setHotOverFee(0.0);
//				respDTO.setHotOverUse(0.0);
				//直接加上热水的用量
				respDTO.setColdOverUse(useNum + (respDTO.getColdOverUse() != null ? respDTO.getColdOverUse() : 0));
			} else if (SDCategoryEnum.COLD_WATER.getCode().equals(detl.getCategoryId())) {
				respDTO.setColdPreMonthNum(preMonthNum);
				respDTO.setColdCurMonthNum(curMonthNum);
				respDTO.setColdUse(useNum + (respDTO.getColdUse() != null ? respDTO.getColdUse() : 0));
				respDTO.setColdQty(curStdQty);
				respDTO.setColdOverFee(curOverFee);
				respDTO.setColdOverUse(payUseNum + (respDTO.getColdOverUse() != null ? respDTO.getColdOverUse() : 0));
			} else if (SDCategoryEnum.ELECTRIC.getCode().equals(detl.getCategoryId())) {
				respDTO.setElePreMonthNum(preMonthNum);
				respDTO.setEleCurMonthNum(curMonthNum);
				respDTO.setEleUse(useNum);
				respDTO.setEleQty(curStdQty);
				respDTO.setEleOverFee(curOverFee);
				respDTO.setEleOverUse(payUseNum > 0 ? payUseNum : 0.0);
			}
		}

		//计算冷水的总金额
		if (respDTO.getColdOverUse() > 0) {
			BigDecimal currAmount = BigDecimal.valueOf(respDTO.getColdOverUse()).multiply(BigDecimal.valueOf(respDTO.getColdOverFee())).setScale(4, RoundingMode.HALF_UP);
			totalAmount = totalAmount.add(currAmount);
		} else {
			respDTO.setColdOverUse(0.0);
		}

		//计算电的总金额
		if (respDTO.getEleOverUse() > 0) {
			BigDecimal currAmount = BigDecimal.valueOf(respDTO.getEleOverUse()).multiply(BigDecimal.valueOf(respDTO.getEleOverFee())).setScale(4, RoundingMode.HALF_UP);
			totalAmount = totalAmount.add(currAmount);
		}

		respDTO.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP).doubleValue());
		respDTO.setAvgAmount(0.0);
		if (respDTO.getInDays() > 0) {
			//房间平均金额= 房间总金额 / 房间总入住天数
			respDTO.setAvgAmount(totalAmount.divide(new BigDecimal(respDTO.getInDays()), 2, RoundingMode.HALF_UP).doubleValue());
		}
	}

	/**
	 * 处理未抄表的数据 只计算电配额
	 *
	 * @param respDTO
	 */
	private void handleNonMeter(DormitorySDMeterreadNewRespDTO respDTO, Date meterMonth) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(meterMonth);
		int curMonth = calendar.get(Calendar.MONTH) + 1;

		if (null == respDTO.getTempId()) {
			return;
		}

		//查询电模板规则
		SmtTemplatesRule smtSdTemplate = smtTemplatesRuleService.getOne(new LambdaQueryWrapper<SmtTemplatesRule>()
				.eq(SmtTemplatesRule::getTempId, respDTO.getTempId())
				.eq(SmtTemplatesRule::getCategoryId, SDCategoryEnum.ELECTRIC.getCode())
				.eq(SmtTemplatesRule::getMonthNum, curMonth)
		);

		SmtDormitoryRoom smtDormitoryRoom = smtDormitoryRoomService.getById(respDTO.getRoomId());

		//查询上月的已结算的数据
		Date preMonth = ToolUtils.getCalDate(meterMonth, Calendar.MONTH, -1);
		List<DormitorySDMeterreadDTO> preMeterreadDTOS = this.baseMapper.getDormitorySDMeterread(respDTO.getRoomId(), preMonth);
		if (null == respDTO.getColdPreMonthNum()) {
			//冷水未抄表
			List<DormitorySDMeterreadDTO> coldCollect = preMeterreadDTOS.stream().filter(x -> SDCategoryEnum.COLD_WATER.getCode().equals(x.getCategoryId())).collect(Collectors.toList());
			if (CollectionUtil.isNotEmpty(coldCollect)) {
				//上月已结算的数据
				if(xcParkId.equals(smtDormitoryRoom.getParkId())){
					// 如果是许昌园区 不判断是否结算
					respDTO.setColdPreMonthNum(coldCollect.get(0).getCurMonthNum());
				} else if(SdStatementStatusEnum.STATEMENT.getCode().equals(coldCollect.get(0).getStatementStatus())){
					respDTO.setColdPreMonthNum(coldCollect.get(0).getCurMonthNum());
				}
			}
		}
		if (null == respDTO.getHotCurMonthNum()) {
			//热水未抄表
			List<DormitorySDMeterreadDTO> hotCollect = preMeterreadDTOS.stream().filter(x -> SDCategoryEnum.HOT_WATER.getCode().equals(x.getCategoryId())).collect(Collectors.toList());
			if (CollectionUtil.isNotEmpty(hotCollect)) {
				//上月已结算的数据
				if(xcParkId.equals(smtDormitoryRoom.getParkId())){
					// 如果是许昌园区 不判断是否结算
					respDTO.setHotPreMonthNum(hotCollect.get(0).getCurMonthNum());
				} else if(SdStatementStatusEnum.STATEMENT.getCode().equals(hotCollect.get(0).getStatementStatus())){
					respDTO.setHotPreMonthNum(hotCollect.get(0).getCurMonthNum());
				}
			}
		}
		if (null == respDTO.getEleCurMonthNum()) {
			//电未抄表
			List<DormitorySDMeterreadDTO> eleCollect = preMeterreadDTOS.stream().filter(x -> SDCategoryEnum.ELECTRIC.getCode().equals(x.getCategoryId())).collect(Collectors.toList());
			if (CollectionUtil.isNotEmpty(eleCollect)) {
				//上月已结算的数据
				if(xcParkId.equals(smtDormitoryRoom.getParkId())){
					// 如果是许昌园区 不判断是否结算
					respDTO.setElePreMonthNum(eleCollect.get(0).getCurMonthNum());
				} else if(SdStatementStatusEnum.STATEMENT.getCode().equals(eleCollect.get(0).getStatementStatus())){
					respDTO.setElePreMonthNum(eleCollect.get(0).getCurMonthNum());
				}
			}
		}

		respDTO.setEleQty(smtSdTemplate.getStandardQty());
		respDTO.setEleOverFee(smtSdTemplate.getOverFee().setScale(2, RoundingMode.HALF_UP).doubleValue());

		respDTO.setTotalAmount(0.0);
		respDTO.setAvgAmount(0.0);
	}

	@Transactional
	@Override
	public Boolean saveBatchSDMeterread(List<SdMeterreadDetailReqDTO> detailReqDTOS) {
		if (null != detailReqDTOS) {
			detailReqDTOS.forEach(item -> {
				if (CollectionUtil.isEmpty(item.getMeterReadDetailList())) {
					//这里return不会退出方法 只结束本次循环
					return;
				}
				boolean isSave = false;
				//收费类型 上月止度 本月止度 都存在 才记录数据
				for (var readDetail : item.getMeterReadDetailList()) {
					if (null != readDetail.getCategoryId() && null != readDetail.getPreMonthNum() && null != readDetail.getCurMonthNum()) {
						isSave = true;
						break;
					}
				}
				if (!isSave) {
					//该房间没有抄表 跳过本次循环
					return;
				}
				//保存抄表明细
				smtSdMeterreadDetailService.saveMeterReadDetail(item, this);

			});
			return true;
		}
		return false;
	}

	@Override
	public Boolean addSDMeterreadRecord(SmtSdMeterreadDTO smtSdMeterreadDTO) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		if (smtSdMeterreadDTO.getRoomId() != null) {
			//判断房间号是否存在 且当前登录用户属于该园区
			SmtDormitoryRoom smtDormitoryRoom = smtDormitoryRoomService.getById(smtSdMeterreadDTO.getRoomId());
			if (smtDormitoryRoom == null || !parkIdList.contains(smtDormitoryRoom.getParkId())) {
				throw new TCEException("房间号不存在");
			}
			//判断该月的数据是否已经生成
			QueryWrapper<SmtSdMeterread> queryWrapper = new QueryWrapper<>();
			queryWrapper.lambda()
					.eq(SmtSdMeterread::getRoomId, smtSdMeterreadDTO.getRoomId())
					.eq(SmtSdMeterread::getMeterMonth, smtSdMeterreadDTO.getMeterMonth());
			SmtSdMeterread smtSdMeterread = this.getOne(queryWrapper);
			Assert.isNull(smtSdMeterread, "本月记录已生成");

			//生成记录
			SmtSdMeterread addSmtSdMeterread = SmtSdMeterread.builder()
					.roomId(smtSdMeterreadDTO.getRoomId())
					.meterMonth(smtSdMeterreadDTO.getMeterMonth())
					.status(SdMeterreadStatusEnum.NON_METER_READ.getCode())
					.statementStatus(SdStatementStatusEnum.NON_STATEMENT.getCode())
					.createTime(new Date())
					.build();
			return this.save(addSmtSdMeterread);
		} else {
			//如果没有指定房间号 则生成一层楼的记录
			//先判断楼层是否存在
			SmtDormitoryFloor smtDormitoryFloor = smtDormitoryFloorService.getById(smtSdMeterreadDTO.getFloorId());
			if (smtDormitoryFloor == null || !parkIdList.contains(smtDormitoryFloor.getParkId())) {
				throw new TCEException("楼层不存在");
			}
			//查询该层楼该月已生成的记录
			List<SmtSdMeterreadVO> floorMeterRecord = smtSdMeterreadMapper.getFloorMeterRecord(smtSdMeterreadDTO);
			//批量生成未添加记录
			List<SmtSdMeterread> smtSdMeterreads = new ArrayList<>();
			for (SmtSdMeterreadVO smtSdMeterreadVO : floorMeterRecord) {
				if (smtSdMeterreadVO.getId() == null) {
					smtSdMeterreads.add(SmtSdMeterread.builder()
							.roomId(smtSdMeterreadVO.getRoomId())
							.meterMonth(smtSdMeterreadDTO.getMeterMonth())
							.status(SdMeterreadStatusEnum.NON_METER_READ.getCode())
							.statementStatus(SdStatementStatusEnum.NON_STATEMENT.getCode())
							.createTime(new Date())
							.build());
				}
			}

			return this.saveBatch(smtSdMeterreads);
		}
	}

	/**
	 * 计算指定时间到当月底的剩余天数
	 *
	 * @param date
	 * @return
	 */
	private Integer calEndDays(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int totalDays = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		int nDays = c.get(Calendar.DAY_OF_MONTH);
		return totalDays - nDays;
	}

	/**
	 * 计算两个时间之间的间隔天数
	 *
	 * @param date1
	 * @param date2
	 * @return
	 */
	private Integer calDiffDays(Date date1, Date date2) {
		int days = (int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24));
		return days;
	}

	/**
	 * 计算指定时间当月的开始时间
	 *
	 * @param date
	 * @return
	 */
	private Date calStartDays(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		//设置为1号,当前日期既为本月第一天
		c.set(Calendar.DAY_OF_MONTH, 1);
		//将小时至0
		c.set(Calendar.HOUR_OF_DAY, 0);
		//将分钟至0
		c.set(Calendar.MINUTE, 0);
		//将秒至0
		c.set(Calendar.SECOND, 0);
		//将毫秒至0
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	/**
	 * SmtDormitoryStaff对象转SmtStaffStatementDetail对象
	 *
	 * @param dormitoryStaff 入住记录
	 * @param firstDay       指定月第一天
	 * @param endDay         指定月最后一天
	 * @param mMonth         指定月的月份
	 * @param currM          当前月的月份
	 * @return
	 */
	private SmtStaffStatementDetail smtDtToStaffSmd(SmtDormitoryStaff dormitoryStaff, Date firstDay, Date endDay, int mMonth, int currM) {
		SmtStaffStatementDetail smtStaffStatementDetail = SmtStaffStatementDetail.builder()
				.staffBadge(dormitoryStaff.getStaffBadge())
				.staffName(dormitoryStaff.getStaffName())
				.parkId(dormitoryStaff.getParkId())
				.roomId(dormitoryStaff.getRoomId())
				.roomName(dormitoryStaff.getRoomName())
				.bedId(dormitoryStaff.getBedId())
				.bedName(dormitoryStaff.getBedNumber())
				.inTime(dormitoryStaff.getCreateTime())
				.build();
		log.info("计算备注天数，入住记录：{}, 开始日期：{}，结束日期：{}, mMonth：{}, currM：{}", dormitoryStaff, firstDay, endDay, mMonth, currM);
		//计算备注天数
		Integer remarkDays = smtDormitoryOutRemarkService.getRemarkDate(dormitoryStaff.getId(),
				null, firstDay, endDay);
		if (dormitoryStaff.getCreateTime().getTime() < firstDay.getTime()) {
			//指定月前入住的 入住天数=本月第一天到目前的天数
			if (mMonth == currM) {
				remarkDays = smtDormitoryOutRemarkService.getRemarkDate(dormitoryStaff.getId(),
						null, firstDay, new Date());
				//本月 统计本月第一天到当前的天数
				smtStaffStatementDetail.setStayDays(calDiffDays(firstDay, new Date()) + 1 - remarkDays);
			} else if (mMonth < currM) {
				remarkDays = smtDormitoryOutRemarkService.getRemarkDate(dormitoryStaff.getId(),
						null, firstDay, endDay);
				//本月之前的月份 统计指定月份第一天到指定月份月末的天数
				smtStaffStatementDetail.setStayDays(calDiffDays(firstDay, endDay) + 1 - remarkDays);
			}

		} else if (dormitoryStaff.getCreateTime().getTime() >= firstDay.getTime()
				&& dormitoryStaff.getCreateTime().getTime() <= endDay.getTime()) {
			//指定月入住的
			if (mMonth == currM) {
				//本月 统计入住时间到目前的天数
				remarkDays = smtDormitoryOutRemarkService.getRemarkDate(dormitoryStaff.getId(),
						null, dormitoryStaff.getCreateTime(), new Date());
				smtStaffStatementDetail.setStayDays(calDiffDays(dormitoryStaff.getCreateTime(), new Date()) + 1 - remarkDays);
			} else if (mMonth < currM) {
				remarkDays = smtDormitoryOutRemarkService.getRemarkDate(dormitoryStaff.getId(),
						null, dormitoryStaff.getCreateTime(), endDay);
				//本月之前的月份 统计入住时间到指定月末的天数
				smtStaffStatementDetail.setStayDays(calDiffDays(dormitoryStaff.getCreateTime(), endDay) + 1 - remarkDays);
			}
		}
		smtStaffStatementDetail.setRemarkDays(remarkDays);
		return smtStaffStatementDetail;
	}

	/**
	 * SmtDormitoryStaff对象转SmtStaffStatementDetail对象
	 *
	 * @param dormitoryStaffHistory 入住历史记录
	 * @return
	 */
	private SmtStaffStatementDetail smtDtHistoryToStaffSmd(SmtDormitoryStaffHistory dormitoryStaffHistory, Date startCalTime, Date endCalTime) {
		SmtStaffStatementDetail smtStaffStatementDetail = SmtStaffStatementDetail.builder()
				.staffBadge(dormitoryStaffHistory.getStaffBadge())
				.staffName(dormitoryStaffHistory.getStaffName())
				.parkId(dormitoryStaffHistory.getParkId())
				.roomId(dormitoryStaffHistory.getRoomId())
				.roomName(dormitoryStaffHistory.getRoomName())
				.bedId(dormitoryStaffHistory.getBedId())
				.bedName(dormitoryStaffHistory.getBedNumber())
				.inTime(dormitoryStaffHistory.getInTime())
				.build();
		//计算备注天数
		Integer remarkDays = smtDormitoryOutRemarkService.getRemarkDate(dormitoryStaffHistory.getId(),
				null, startCalTime, endCalTime);
		smtStaffStatementDetail.setRemarkDays(remarkDays);
		//入住天数为计算开始时间和计算结束时间的差值再加1
		smtStaffStatementDetail.setStayDays(calDiffDays(startCalTime, endCalTime) + 1 - remarkDays);
		return smtStaffStatementDetail;
	}

	/**
	 * @param roomIds   房间号列表
	 * @param startDate 开始时间
	 * @param endDate   结束时间
	 * @return
	 */
	@Override
	public Map<String, List<SmtStaffStatementDetail>> getRoomStayData(List<Integer> roomIds, Date startDate, Date endDate) {
		Calendar calendar = Calendar.getInstance();
		//startDate 为上个月26号
		calendar.setTime(startDate);

		//指定的时间月份
		int meterM = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH) + 1;                    //获取月份

		//当前的时间月份
		Calendar currCal = Calendar.getInstance();
		currCal.setTime(new Date());
		int currM = currCal.get(Calendar.YEAR) * 12 + currCal.get(Calendar.MONTH) + 1;

		//入住数据
		Map<String, List<SmtStaffStatementDetail>> roomStayMaps = new HashMap<>();


		log.info("计算房间住宿情况,roomIds={},meterM={},currM={}", roomIds, meterM, currM);

		if (meterM > currM) {
			//本月后的月份 不统计
			return roomStayMaps;
		}

		//查询目前在住记录 时间为小于指定结束时间
		List<SmtDormitoryStaff> smtDormitoryStaffs = smtDormitoryStaffMapper.selectList(
				new QueryWrapper<SmtDormitoryStaff>().lambda()
						.in(SmtDormitoryStaff::getRoomId, roomIds)
						.le(SmtDormitoryStaff::getCreateTime, endDate)
		);
		//根据房间标识分组
		Map<Integer, List<SmtDormitoryStaff>> roomCollect = smtDormitoryStaffs.stream().collect(Collectors.groupingBy(SmtDormitoryStaff::getRoomId));
		for (Map.Entry<Integer, List<SmtDormitoryStaff>> entry : roomCollect.entrySet()) {

			if (!roomStayMaps.containsKey(entry.getKey().toString())) {
				//key不存在的情况
				List<SmtStaffStatementDetail> smtStaffStatementDetails = new ArrayList<>();
				entry.getValue().forEach(item -> {
					SmtStaffStatementDetail smtStaffStatementDetail = smtDtToStaffSmd(item, startDate, endDate, meterM, currM);
					smtStaffStatementDetails.add(smtStaffStatementDetail);
				});
				roomStayMaps.put(entry.getKey().toString(), smtStaffStatementDetails);
			} else {
				//key存在的情况
				entry.getValue().forEach(item -> {
					SmtStaffStatementDetail smtStaffStatementDetail = smtDtToStaffSmd(item, startDate, endDate, meterM, currM);
					roomStayMaps.get(entry.getKey().toString()).add(smtStaffStatementDetail);
				});
			}
		}


		//查询当月的退宿历史记录
		List<SmtDormitoryStaffHistory> smtDormitoryStaffHistories = smtDormitoryStaffHistoryMapper.getStatffHistory(roomIds, startDate, endDate);
		//根据房间标识分组
		Map<Integer, List<SmtDormitoryStaffHistory>> roomHistoryCollect = smtDormitoryStaffHistories.stream().collect(Collectors.groupingBy(SmtDormitoryStaffHistory::getRoomId));
		for (Map.Entry<Integer, List<SmtDormitoryStaffHistory>> entry : roomHistoryCollect.entrySet()) {
			entry.getValue().forEach(item -> {
				Date startCalTime = startDate;
				if (startCalTime.getTime() < item.getInTime().getTime()) {
					//计算开始时间小于入住时间
					startCalTime = item.getInTime();
				}
				Date endCalTime = endDate;
				if (endCalTime.getTime() > item.getTime().getTime()) {
					//计算结束时间大于退宿时间时
					endCalTime = item.getTime();
				}
				SmtStaffStatementDetail smtStaffStatementDetail = smtDtHistoryToStaffSmd(item, startCalTime, endCalTime);
				if (!roomStayMaps.containsKey(entry.getKey().toString())) {
					//key不存在的情况
					List<SmtStaffStatementDetail> smtStaffStatementDetails = new ArrayList<>();
					smtStaffStatementDetails.add(smtStaffStatementDetail);
					roomStayMaps.put(item.getRoomId().toString(), smtStaffStatementDetails);
				} else {
					roomStayMaps.get(entry.getKey().toString()).add(smtStaffStatementDetail);
				}
			});
		}
		return roomStayMaps;
	}

	@Override
	public ResponseEntity<byte[]> getSdImportTemplate(String meterMonth, List<Integer> dormitoryIds) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date meterMonthDate = null;
		try {
			meterMonthDate = dateFormat.parse(meterMonth);
		} catch (Exception e) {
			throw new TCEException("抄表月份时间格式错误");
		}

		//查询楼栋信息
		List<SmtDormitory> dormitoryList = (List<SmtDormitory>) smtDormitoryService.listByIds(dormitoryIds);
		if (CollectionUtil.isEmpty(dormitoryList)) {
			throw new TCEException("楼栋不存在");
		}
		List<Integer> dorIds = dormitoryList.stream().map(SmtDormitory::getId).collect(Collectors.toList());

		//查询楼栋下的所有楼层
		List<SmtDormitoryFloor> floorList = smtDormitoryFloorService.list(new LambdaQueryWrapper<SmtDormitoryFloor>()
				.in(SmtDormitoryFloor::getDormitoryId, dorIds)
				.orderByAsc(SmtDormitoryFloor::getFloorName)
		);
		if (CollectionUtil.isEmpty(floorList)) {
			throw new TCEException("楼层不存在");
		}

		List<Integer> floorIds = floorList.stream().map(SmtDormitoryFloor::getId).collect(Collectors.toList());
		//查询楼栋下的所有房间
		List<SmtDormitoryRoom> roomList = smtDormitoryRoomService.list(new LambdaQueryWrapper<SmtDormitoryRoom>()
				.in(SmtDormitoryRoom::getFloorId, floorIds)
				.orderByAsc(SmtDormitoryRoom::getRoomName)
		);
		if (CollectionUtil.isEmpty(roomList)) {
			throw new TCEException("房间不存在");
		}

		//查询房间上月结算数据
		List<Integer> roomIds = roomList.stream().map(SmtDormitoryRoom::getId).collect(Collectors.toList());

		Calendar instance = Calendar.getInstance();
		instance.setTime(meterMonthDate);
		instance.add(Calendar.MONTH, -1);
		Date preMonthTime = instance.getTime();

		List<SmtSdMeterread> sdMeterreadList = this.list(new LambdaQueryWrapper<SmtSdMeterread>()
				.in(SmtSdMeterread::getRoomId, roomIds)
				.eq(SmtSdMeterread::getMeterMonth, preMonthTime)
				.eq(SmtSdMeterread::getStatementStatus, SdStatementStatusEnum.STATEMENT.getCode())
		);


		List<DormitorySdTemplateDTO> templateDTOList = new ArrayList<>();

		for (SmtDormitoryRoom room : roomList) {
			DormitorySdTemplateDTO templateDTO = new DormitorySdTemplateDTO();
			SmtDormitory smtDormitory = dormitoryList.stream().filter(s -> s.getId().equals(room.getDormitoryId())).findFirst().get();
			SmtDormitoryFloor floor = floorList.stream().filter(s -> s.getId().equals(room.getFloorId())).findFirst().get();
			templateDTO.setDorName(smtDormitory.getDormitoryName());
			templateDTO.setFloorName(StringUtils.isNotEmpty(floor.getAliasName()) ? floor.getAliasName() : String.valueOf(floor.getFloorName()));
			templateDTO.setRoomName(StringUtils.isNotEmpty(room.getAliasName()) ? room.getAliasName() : String.valueOf(room.getRoomName()));

			SmtSdMeterread preSdMeterread = sdMeterreadList.stream().filter(s -> s.getRoomId().equals(room.getId())).findFirst().orElse(null);
			if (Objects.nonNull(preSdMeterread)) {
				//上月有结算数据
				List<SmtSdMeterreadDetail> preMeterreadDetails = smtSdMeterreadDetailService.list(new LambdaQueryWrapper<SmtSdMeterreadDetail>()
						.eq(SmtSdMeterreadDetail::getMrId, preSdMeterread.getId())
				);
				Double elePreNum = preMeterreadDetails.stream().filter(s -> s.getCategoryId().equals(SDCategoryEnum.ELECTRIC.getCode())).findFirst().get().getCurMonthNum();
				Double hotPreNum = preMeterreadDetails.stream().filter(s -> s.getCategoryId().equals(SDCategoryEnum.HOT_WATER.getCode())).findFirst().get().getCurMonthNum();
				Double coldPreNum = preMeterreadDetails.stream().filter(s -> s.getCategoryId().equals(SDCategoryEnum.COLD_WATER.getCode())).findFirst().get().getCurMonthNum();
				templateDTO.setElePreNum(String.valueOf(elePreNum));
				templateDTO.setHotPreNum(String.valueOf(hotPreNum));
				templateDTO.setColdPreNum(String.valueOf(coldPreNum));
			}

			templateDTOList.add(templateDTO);
		}

		//按楼栋和房间排序
		templateDTOList = templateDTOList.stream().sorted(Comparator.comparing(DormitorySdTemplateDTO::getDorName).thenComparing(DormitorySdTemplateDTO::getRoomName)).collect(Collectors.toList());

		ResponseEntity<byte[]> responseEntity;
		try (Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(), DormitorySdTemplateDTO.class, templateDTOList)) {
			String fileName = "水电导入模板";
			responseEntity = IOUtils.getExcelResponse(fileName, workbook);
		} catch (IOException e) {
			log.error("水电导入模板下载异常", e);
			throw new TCEException("水电导入模板下载异常");
		}
		return responseEntity;
	}

	@Transactional
	@Override
	public ResponseEntity<byte[]> importDormitorySd(String meterMonth, MultipartFile multipartFile, HttpServletResponse httpServletResponse) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date meterMonthDate = null;
		try {
			meterMonthDate = dateFormat.parse(meterMonth);
		} catch (Exception e) {
			throw new TCEException("抄表月份时间格式错误");
		}
		List<DormitorySdImportReqDTO> excelDTOList = null;
		List<DormitorySdImportReqDTO> failExcelDTOList = new ArrayList<>();
		try (InputStream inputStream = multipartFile.getInputStream()) {
			ExcelReader excelReader = ExcelUtil.getReader(inputStream);

			Map<String, String> headerAlias = new HashMap<>(9);
			headerAlias.put("楼栋名称", "dorName");
			headerAlias.put("楼层名称", "floorName");
			headerAlias.put("房间名称", "roomName");
			headerAlias.put("电上月止数", "elePreNum");
			headerAlias.put("电本月止数", "eleCurNum");
			headerAlias.put("冷水上月止数", "coldPreNum");
			headerAlias.put("冷水本月止数", "coldCurNum");
			headerAlias.put("热水上月止数", "hotPreNum");
			headerAlias.put("热水本月止数", "hotCurNum");
			excelReader.setHeaderAlias(headerAlias);

			excelDTOList = excelReader.read(0, 1, DormitorySdImportReqDTO.class);
			if (CollectionUtil.isEmpty(excelDTOList) || excelDTOList.size() < 1) {
				log.info("Excel内容为空");
				throw new TCEException("上传的Excel内容为空");
			}

			Map<String, SmtDormitory> dormitoryMap = new HashMap<>();
			Map<String, SmtDormitoryFloor> dormitoryFloorMap = new HashMap<>();
			Map<String, SmtDormitoryRoom> dormitoryRoomMap = new HashMap<>();

			for (DormitorySdImportReqDTO row : excelDTOList) {
				log.info("excel数据：{}", row);
				try {
					handleSdImport(dormitoryMap, dormitoryFloorMap, row, failExcelDTOList, meterMonthDate);
				} catch (Exception ex) {
					row.setRemark("数据处理异常");
					log.error("数据处理异常", ex);
					failExcelDTOList.add(row);
				}
			}
		} catch (IOException e) {
			log.error("读取excel失败,{}", e.getMessage());
			throw new TCEException("读取excel失败");
		}

		if (CollectionUtil.isEmpty(failExcelDTOList)) {
			JSONObject obj = new JSONObject();
			obj.put("code", 0);
			obj.put("msg", "导入成功");
			obj.put("message", "导入成功");
			obj.put("data", null);
			try {
				httpServletResponse.getWriter().print(obj);
				return null;
			} catch (Exception e) {

			}
		}

		ResponseEntity<byte[]> responseEntity;
		log.info("excel_name:{}", multipartFile.getOriginalFilename());
		try (Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(), DormitorySdImportReqDTO.class, failExcelDTOList)) {
			String fileName = multipartFile.getOriginalFilename();
			responseEntity = IOUtils.getExcelResponse(fileName, workbook);
		} catch (IOException e) {
			log.error("excel导出异常", e);
			throw new TCEException(ExceptionEnum.UNKNOWN.getCode(), "excel导出异常");
		}
		return responseEntity;
	}

	@Override
	public Integer getInRoomNum(String badge, Date meterMonth) {
		Date startime = ToolUtils.getDateMonthStartime(meterMonth);
		Date endTime = ToolUtils.getDateMonthEndTime(meterMonth);
		return this.baseMapper.getInRoomNum(badge, startime, endTime);
	}

	/** Oracle 单次 IN 子句最多支持 1000 个参数，批量查询按此分批 */
	private static final int IN_CLAUSE_BATCH_SIZE = 1000;

	@Override
	public Map<String, Integer> getInRoomNumBatch(List<String> badges, Date meterMonth) {
		if (CollectionUtil.isEmpty(badges)) {
			return Collections.emptyMap();
		}
		List<String> distinctBadges = badges.stream().distinct().collect(Collectors.toList());
		Date startime = ToolUtils.getDateMonthStartime(meterMonth);
		Date endTime = ToolUtils.getDateMonthEndTime(meterMonth);

		Map<String, Integer> result = new HashMap<>();
		int batchCount = distinctBadges.size() / IN_CLAUSE_BATCH_SIZE + (distinctBadges.size() % IN_CLAUSE_BATCH_SIZE > 0 ? 1 : 0);
		for (int i = 0; i < batchCount; i++) {
			int fromIndex = i * IN_CLAUSE_BATCH_SIZE;
			int toIndex = Math.min(fromIndex + IN_CLAUSE_BATCH_SIZE, distinctBadges.size());
			List<StaffInRoomNumDTO> batchResult = this.baseMapper.getInRoomNumBatch(distinctBadges.subList(fromIndex, toIndex), startime, endTime);
			batchResult.forEach(dto -> result.put(dto.getBadge(), dto.getInRoomNum()));
		}
		return result;
	}

	@Transactional
	public void handleSdImport(Map<String, SmtDormitory> dormitoryMap, Map<String, SmtDormitoryFloor> dormitoryFloorMap, DormitorySdImportReqDTO row, List<DormitorySdImportReqDTO> failExcelDTOList, Date meterMonthDate) {

		if (StringUtils.isEmpty(row.getColdCurNum())) {
			log.info("异常 冷水本月数据为空,{}", row.getDorName());
			row.setRemark("冷水本月数据为空");
			failExcelDTOList.add(row);
			return;
		}

		if (StringUtils.isEmpty(row.getHotCurNum())) {
			log.info("异常 热水本月数据为空,{}", row.getDorName());
			row.setRemark("热水本月数据为空");
			failExcelDTOList.add(row);
			return;
		}

		if (StringUtils.isEmpty(row.getEleCurNum())) {
			log.info("异常 电本月数据为空,{}", row.getDorName());
			row.setRemark("电本月数据为空");
			failExcelDTOList.add(row);
			return;
		}

		//查询楼栋
		if (!dormitoryMap.containsKey(row.getDorName())) {
			SmtDormitory dormitory = smtDormitoryService.getOne(new LambdaQueryWrapper<SmtDormitory>().eq(SmtDormitory::getDormitoryName, row.getDorName()));
			dormitoryMap.put(row.getDorName(), dormitory);
		}

		SmtDormitory smtDormitory = dormitoryMap.get(row.getDorName());
		if (Objects.isNull(smtDormitory)) {
			log.info("异常 楼栋不存在,{}", row.getDorName());
			row.setRemark("楼栋不存在");
			failExcelDTOList.add(row);
			return;
		}

		//查询楼层
		String dfName = row.getDorName() + "_" + row.getFloorName();
		if (!dormitoryFloorMap.containsKey(dfName)) {
			LambdaQueryWrapper<SmtDormitoryFloor> queryWrapper = new LambdaQueryWrapper<SmtDormitoryFloor>()
					.eq(SmtDormitoryFloor::getDormitoryId, smtDormitory.getId());

			if (NumberUtil.isNumber(row.getFloorName())) {
				queryWrapper.and(warp -> warp.eq(SmtDormitoryFloor::getFloorName, row.getFloorName()).or().eq(SmtDormitoryFloor::getAliasName, row.getFloorName()));
			} else {
				//名称非数字 只能是别名
				queryWrapper.eq(SmtDormitoryFloor::getAliasName, row.getFloorName());
			}

			SmtDormitoryFloor dormitoryFloor = smtDormitoryFloorService.getOne(queryWrapper);
			dormitoryFloorMap.put(dfName, dormitoryFloor);
		}
		SmtDormitoryFloor smtDormitoryFloor = dormitoryFloorMap.get(dfName);
		if (Objects.isNull(smtDormitoryFloor)) {
			log.info("异常 楼层不存在,{}", dfName);
			row.setRemark("楼层不存在");
			failExcelDTOList.add(row);
			return;
		}

		//查询房间
		LambdaQueryWrapper<SmtDormitoryRoom> queryWrapper = new LambdaQueryWrapper<SmtDormitoryRoom>()
				.eq(SmtDormitoryRoom::getDormitoryId, smtDormitory.getId())
				.eq(SmtDormitoryRoom::getFloorId, smtDormitoryFloor.getId());

		if (NumberUtil.isNumber(row.getRoomName())) {
			queryWrapper.and(wrap -> wrap.eq(SmtDormitoryRoom::getRoomName, row.getRoomName()).or().eq(SmtDormitoryRoom::getAliasName, row.getRoomName()));
		} else {
			//名称非数字 只能是别名
			queryWrapper.eq(SmtDormitoryRoom::getAliasName, row.getRoomName());
		}

		SmtDormitoryRoom dormitoryRoom = smtDormitoryRoomService.getOne(queryWrapper);
		if (Objects.isNull(dormitoryRoom)) {
			log.info("异常 房间不存在,{}_{}", dfName, row.getRoomName());
			row.setRemark("房间不存在");
			failExcelDTOList.add(row);
			return;
		}

		Double elePre = Double.parseDouble(row.getElePreNum());
		Double eleCur = Double.parseDouble(row.getEleCurNum());
		if (eleCur.doubleValue() < elePre.doubleValue()) {
			log.info("异常 电本月止度小于上月止度,{} < {}", eleCur, eleCur);
			row.setRemark("电本月止度小于上月止度");
			failExcelDTOList.add(row);
			return;
		}

		Double hotPre = Double.parseDouble(row.getHotPreNum());
		Double hotCur = Double.parseDouble(row.getHotCurNum());
		if (hotCur.doubleValue() < hotPre.doubleValue()) {
			log.info("异常 热水本月止度小于上月止度,{} < {}", hotCur, hotPre);
			row.setRemark("热水本月止度小于上月止度");
			failExcelDTOList.add(row);
			return;
		}

		Double coldPre = Double.parseDouble(row.getColdPreNum());
		Double coldCur = Double.parseDouble(row.getColdCurNum());
		if (coldCur.doubleValue() < coldPre.doubleValue()) {
			log.info("异常 冷水本月止度小于上月止度,{} < {}", coldCur, coldPre);
			row.setRemark("冷水本月止度小于上月止度");
			failExcelDTOList.add(row);
			return;
		}

		if (null == dormitoryRoom.getSdTemplateId()) {
			log.info("异常 房间没有配置水电模板,{}_{}", dfName, row.getRoomName());
			row.setRemark("房间没有配置水电模板");
			failExcelDTOList.add(row);
			return;
		}

		//查询房间抄表记录
		SmtSdMeterread sdMeterread = this.getOne(new LambdaQueryWrapper<SmtSdMeterread>()
				.eq(SmtSdMeterread::getRoomId, dormitoryRoom.getId())
				.eq(SmtSdMeterread::getMeterMonth, meterMonthDate)
		);
		if (Objects.isNull(sdMeterread)) {
			sdMeterread = new SmtSdMeterread();
			sdMeterread.setRoomId(dormitoryRoom.getId());
			sdMeterread.setMeterMonth(meterMonthDate);
			sdMeterread.setStatus(2);
			sdMeterread.setStatementStatus(0);
			sdMeterread.setCreateTime(new Date());
			this.save(sdMeterread);
		} else {
			if (SdStatementStatusEnum.STATEMENT.getCode().equals(sdMeterread.getStatementStatus())) {
				log.info("房间水电已结算,{}_{}", dfName, row.getRoomName());
				row.setRemark("房间水电已结算");
				failExcelDTOList.add(row);
				return;
			}
			sdMeterread.setRoomId(dormitoryRoom.getId());
			sdMeterread.setMeterMonth(meterMonthDate);
			sdMeterread.setStatus(2);
			sdMeterread.setStatementStatus(0);
			sdMeterread.setCreateTime(new Date());
			this.updateById(sdMeterread);
		}
		Calendar instance = Calendar.getInstance();
		instance.setTime(meterMonthDate);
		int year = instance.get(Calendar.YEAR);
		int month = instance.get(Calendar.MONTH) + 1;
		//查询房间水电模板配置
		List<SmtTemplatesRule> templatesRules = smtTemplatesRuleService.list(new LambdaQueryWrapper<SmtTemplatesRule>()
				.eq(SmtTemplatesRule::getTempId, dormitoryRoom.getSdTemplateId())
				.eq(SmtTemplatesRule::getMonthNum, month)
		);

		if (CollectionUtil.isEmpty(templatesRules) || templatesRules.size() < 3) {
			log.info("异常 水电模板没有配置完整,{}_{}", dfName, row.getRoomName());
			row.setRemark("水电模板没有配置完整");
			failExcelDTOList.add(row);
			return;
		}

		SdMeterreadDetailReqDTO sdMeterreadDetailReqDTO = new SdMeterreadDetailReqDTO();
		sdMeterreadDetailReqDTO.setRoomId(dormitoryRoom.getId());
		sdMeterreadDetailReqDTO.setMeterMonth(meterMonthDate);

		List<SdMeterreadDetailReqDTO.MeterReadDetail> meterReadDetailList = new ArrayList<>();
		SdMeterreadDetailReqDTO.MeterReadDetail coldMeterReadDetail = new SdMeterreadDetailReqDTO.MeterReadDetail();
		coldMeterReadDetail.setCategoryId(SDCategoryEnum.COLD_WATER.getCode());
		coldMeterReadDetail.setPreMonthNum(coldPre);
		coldMeterReadDetail.setCurMonthNum(coldCur);
		meterReadDetailList.add(coldMeterReadDetail);

		SdMeterreadDetailReqDTO.MeterReadDetail hotMeterReadDetail = new SdMeterreadDetailReqDTO.MeterReadDetail();
		hotMeterReadDetail.setCategoryId(SDCategoryEnum.HOT_WATER.getCode());
		hotMeterReadDetail.setPreMonthNum(hotPre);
		hotMeterReadDetail.setCurMonthNum(hotCur);
		meterReadDetailList.add(hotMeterReadDetail);

		SdMeterreadDetailReqDTO.MeterReadDetail eleMeterReadDetail = new SdMeterreadDetailReqDTO.MeterReadDetail();
		eleMeterReadDetail.setCategoryId(SDCategoryEnum.ELECTRIC.getCode());
		eleMeterReadDetail.setPreMonthNum(elePre);
		eleMeterReadDetail.setCurMonthNum(eleCur);
		meterReadDetailList.add(eleMeterReadDetail);

		sdMeterreadDetailReqDTO.setMeterReadDetailList(meterReadDetailList);
		smtSdMeterreadDetailService.saveMeterReadDetail(sdMeterreadDetailReqDTO, this);
	}

	@Transactional
	@Override
	public String generateSDStatementDetail(Integer dormitoryId) {
		List<Integer> parkIdList;
		try {
			parkIdList = SecurityUtils.getUser().getParkIdList();
		} catch (Exception e) {
			log.error("未登录，默认许昌园区");
			parkIdList = CollUtil.newArrayList(xcParkId);
		}
		//查询待结算的数据 这些数据是多个房间多个月份的已抄表完成且待结算的数据
		List<GenerateStatementDTO> needStatementRecord = smtSdMeterreadMapper.getNeedStatementRecord(dormitoryId, parkIdList);
		StringBuilder failReson = new StringBuilder();
		int succCount = 0;
		if (CollectionUtil.isNotEmpty(needStatementRecord)) {
			//查询所有的房间抄表明细
			List<Long> mrIdList = needStatementRecord.stream().map(GenerateStatementDTO::getId).collect(Collectors.toList());

			// 分批查询，避免Oracle IN子句超过1000个参数的限制
			List<SmtSdMeterreadDetail> meterreadDetails = new ArrayList<>();
			int batchSize = 900; // 设置为900，留有余量
			for (int i = 0; i < mrIdList.size(); i += batchSize) {
				int endIndex = Math.min(i + batchSize, mrIdList.size());
				List<Long> batchIds = mrIdList.subList(i, endIndex);

				List<SmtSdMeterreadDetail> batchDetails = smtSdMeterreadDetailService
						.list(new LambdaQueryWrapper<SmtSdMeterreadDetail>().in(SmtSdMeterreadDetail::getMrId, batchIds));
				meterreadDetails.addAll(batchDetails);
			}

			Map<Long, List<SmtSdMeterreadDetail>> meterreadDetailMap = meterreadDetails.stream()
					.collect(Collectors.groupingBy(SmtSdMeterreadDetail::getMrId));

			//按房间遍历
			for (GenerateStatementDTO generateStatementDTO : needStatementRecord) {
				SmtDormitoryRoom dormitoryRoom = smtDormitoryRoomService.getById(generateStatementDTO.getRoomId());
				//其中一个房间水电抄表详情
				List<SmtSdMeterreadDetail> roomSdMeterreadDetails = meterreadDetailMap.get(generateStatementDTO.getId());

				//查询需要结算的员工房间抄表数据
				List<SmtStaffStatementDetail> staffStatementDetails = smtStaffStatementDetailService
						.list(new LambdaQueryWrapper<SmtStaffStatementDetail>()
								.eq(SmtStaffStatementDetail::getMrId, generateStatementDTO.getId())
						);
				if (CollectionUtil.isEmpty(staffStatementDetails)) {
					//没有入住记录
					failReson.append("房间 " + dormitoryRoom.getRoomName() + " 没有入住记录").append(System.getProperty("line.separator"));
					continue;
				}
				//按收费分类分组
				Map<Integer, List<SmtStaffStatementDetail>> staffStatementDetailMap = staffStatementDetails
						.stream().collect(Collectors.groupingBy(SmtStaffStatementDetail::getCategoryId));

				//还未配置模板
				if (ObjectUtil.isNull(generateStatementDTO.getTempId())) {
					failReson.append("房间 " + dormitoryRoom.getRoomName() + " 还未设置水电模板").append(System.getProperty("line.separator"));
					continue;
				}
				//查询房间配置的电模板
				StaffSDRuleRespDTO elcRule = smtSDTemplatesMapper.getSDRuleById(generateStatementDTO.getTempId(), SDCategoryEnum.ELECTRIC.getCode(), generateStatementDTO.getMonthNum());

				if (null == elcRule) {
					//房间电模板没有配置
					failReson.append("房间 " + dormitoryRoom.getRoomName() + " 没有配置电模板").append(System.getProperty("line.separator"));
					continue;
				}

				//房间电抄表数据
				List<SmtSdMeterreadDetail> elcMeterDetails = roomSdMeterreadDetails.stream().filter(s ->
						s.getCategoryId().equals(SDCategoryEnum.ELECTRIC.getCode())).collect(Collectors.toList());
				//员工的电抄表数据
				List<SmtStaffStatementDetail> staffStatementDetailElc = staffStatementDetailMap.get(SDCategoryEnum.ELECTRIC.getCode());
				if (null == staffStatementDetailElc) {
					//员工电抄表数据不存在
					continue;
				}
				//房间电结算
				roomElcStatement(staffStatementDetailElc, elcMeterDetails, elcRule);

				//结算房间热水抄表
				//查询房间对应的水收费规则
				StaffSDRuleRespDTO hotRule = smtSDTemplatesMapper.getSDRuleById(generateStatementDTO.getTempId(),
						SDCategoryEnum.HOT_WATER.getCode(), generateStatementDTO.getMonthNum());
				if (null == hotRule) {
					//房间热水模板没有配置
					failReson.append("房间 " + dormitoryRoom.getRoomName() + " 没有配置热水模板").append(System.getProperty("line.separator"));
					continue;
				}

				//员工的热水抄表数据
				List<SmtStaffStatementDetail> staffStatementDetailHot = staffStatementDetailMap.get(SDCategoryEnum.HOT_WATER.getCode());
				//房间热水抄表数据
				SmtSdMeterreadDetail hotMeterDetail = roomSdMeterreadDetails.stream().filter(s ->
						s.getCategoryId().equals(SDCategoryEnum.HOT_WATER.getCode())).collect(Collectors.toList()).get(0);
				//房间热水结算
				roomWaterStatement(staffStatementDetailHot, hotRule, hotMeterDetail, dormitoryRoom, null);


				//结算房间冷水抄表
				StaffSDRuleRespDTO coldRule = smtSDTemplatesMapper.getSDRuleById(generateStatementDTO.getTempId(), SDCategoryEnum.COLD_WATER.getCode(), generateStatementDTO.getMonthNum());
				if (null == coldRule) {
					//员工冷水模板没有配置
					failReson.append("房间 " + dormitoryRoom.getRoomName() + " 没有配置冷水模板").append(System.getProperty("line.separator"));
					continue;
				}
				//员工的冷水抄表数据
				List<SmtStaffStatementDetail> staffStatementDetailCold = staffStatementDetailMap.get(SDCategoryEnum.COLD_WATER.getCode());
				//房间冷水抄表数据
				SmtSdMeterreadDetail coldMeterDetail = roomSdMeterreadDetails.stream().filter(s -> s.getCategoryId().equals(SDCategoryEnum.COLD_WATER.getCode())).collect(Collectors.toList()).get(0);
				//房间冷水结算
				roomWaterStatement(staffStatementDetailCold, coldRule, coldMeterDetail, dormitoryRoom, hotMeterDetail);


				//批量更新员工房间水电费用
				//设置结算时间
				staffStatementDetails.forEach(item -> {
					item.setStatementStatus(SdStatementStatusEnum.STATEMENT.getCode());
					item.setStatementDate(new Date());
				});
				smtStaffStatementDetailService.updateBatchById(staffStatementDetails);

				//房间公摊结算
				commonSDStatement(generateStatementDTO.getRoomId(), generateStatementDTO.getMeterMonth(), elcRule, hotRule, coldRule);

				//更新房间水电结算状态
				SmtSdMeterread smtSdMeterread = new SmtSdMeterread();
				smtSdMeterread.setId(generateStatementDTO.getId());
				smtSdMeterread.setStatementStatus(SdStatementStatusEnum.STATEMENT.getCode());
				this.updateById(smtSdMeterread);

				//修改本月的全部公摊抄表数据为已结算
				smtCommonSDMeterreadMapper.update(SmtCommonSDMeterread.builder()
								.status(SdStatementStatusEnum.STATEMENT.getCode())
								.build(),
						new LambdaUpdateWrapper<SmtCommonSDMeterread>().eq(SmtCommonSDMeterread::getMeterMonth, generateStatementDTO.getMeterMonth())
				);
				succCount++;
			}
		}
		StringBuilder resultStr = new StringBuilder();
		resultStr.append("本次结算结果:").append(System.getProperty("line.separator"));
		resultStr.append("结算房间:" + (needStatementRecord.size())).append(System.getProperty("line.separator"));
		resultStr.append("成功数:" + (succCount)).append(System.getProperty("line.separator"));
		resultStr.append("失败数:" + ((needStatementRecord.size() - succCount))).append(System.getProperty("line.separator"));
		if (succCount < needStatementRecord.size()) {
			resultStr.append(failReson);
		}
		return resultStr.toString();
	}

	/**
	 * 房间关联的公摊水电结算
	 *
	 * @param roomId
	 * @param meterMonth
	 * @param elcRule
	 * @param hotRule
	 * @param coldRule
	 */
	public void commonSDStatement(Integer roomId, Date meterMonth, StaffSDRuleRespDTO elcRule,
								  StaffSDRuleRespDTO hotRule,
								  StaffSDRuleRespDTO coldRule) {
		//房间公摊水电结算
		//查询该房间关联的员工的公摊抄表数据
		List<SmtStaffStatementDetail> commonStaffStatementDetails = smtStaffStatementDetailService.list(new LambdaQueryWrapper<SmtStaffStatementDetail>()
				.eq(SmtStaffStatementDetail::getRoomId, roomId)
				.eq(SmtStaffStatementDetail::getMeterMonth, meterMonth)
				.eq(SmtStaffStatementDetail::getMeterType, MeterTypeEnum.COMMON_METER.getCode())
		);

		if (CollectionUtil.isNotEmpty(commonStaffStatementDetails)) {
			//公摊数据没有基础配额 使用多少就按多少结算
			//存在公摊抄表数据
			Map<Integer, List<SmtStaffStatementDetail>> commonStaffStatementMap = commonStaffStatementDetails.stream().collect(Collectors.groupingBy(SmtStaffStatementDetail::getCategoryId));
			//公摊热水
			List<SmtStaffStatementDetail> commonStaffStatementHot = commonStaffStatementMap.get(SDCategoryEnum.HOT_WATER.getCode());
			if (CollectionUtil.isNotEmpty(commonStaffStatementHot)) {
				commonStaffStatementHot.forEach(item -> {
					//设置员工公摊热水金额
					item.setFee(BigDecimal.valueOf(item.getUsage()).multiply(hotRule.getOverFee()).setScale(2, RoundingMode.HALF_UP));
				});
			}

			//公摊冷水
			List<SmtStaffStatementDetail> commonStaffStatementClod = commonStaffStatementMap.get(SDCategoryEnum.COLD_WATER.getCode());
			if (CollectionUtil.isNotEmpty(commonStaffStatementClod)) {
				commonStaffStatementClod.forEach(item -> {
					//设置员工公摊冷水费用
					item.setFee(BigDecimal.valueOf(item.getUsage()).multiply(coldRule.getOverFee()).setScale(2, RoundingMode.HALF_UP));
				});
			}

			//公摊电
			List<SmtStaffStatementDetail> commonStaffStatementElc = commonStaffStatementMap.get(SDCategoryEnum.ELECTRIC.getCode());
			if (CollectionUtil.isNotEmpty(commonStaffStatementElc)) {
				commonStaffStatementElc.forEach(item -> {
					//设置员工公摊电费用
					item.setFee(BigDecimal.valueOf(item.getUsage()).multiply(elcRule.getOverFee()).setScale(2, RoundingMode.HALF_UP));
				});
			}
			commonStaffStatementDetails.forEach(item -> {
				item.setStatementDate(new Date());
				item.setStatementStatus(SdStatementStatusEnum.STATEMENT.getCode());
			});
			//批量更新员工公摊水电费用
			smtStaffStatementDetailService.updateBatchById(commonStaffStatementDetails);
		}
	}

	/**
	 * 房间电结算
	 *
	 * @param staffStatementDetailElc 员工的房间电抄表
	 * @param elcMeterDetails         房间的电抄表数据
	 * @param elcRule                 房间配置的电模板
	 */
	public void roomElcStatement(List<SmtStaffStatementDetail> staffStatementDetailElc,
								 List<SmtSdMeterreadDetail> elcMeterDetails, StaffSDRuleRespDTO elcRule) {
		//电用量
		SmtSdMeterreadDetail elcMeterDetail = elcMeterDetails.get(0);
		Double preUse = (elcMeterDetail.getRevPreMonthNum() != null ? elcMeterDetail.getRevPreMonthNum() : elcMeterDetail.getPreMonthNum());
		BigDecimal elcUse = BigDecimal.valueOf(elcMeterDetail.getCurMonthNum()).subtract(new BigDecimal(preUse));

		//设置标准用量和超出费用单价
		elcMeterDetail.setCurStdQty(elcRule.getStandardQty());
		elcMeterDetail.setCurOverFee(elcRule.getOverFee());
		smtSdMeterreadDetailService.updateById(elcMeterDetail);

		BigDecimal stdQty = BigDecimal.valueOf(elcRule.getStandardQty());

		if (stdQty.compareTo(elcUse) >= 0) {
			//没有超出标准用量 房间所有人的电费用为0
			staffStatementDetailElc.forEach(item -> {
				item.setFee(BigDecimal.ZERO);
			});
		} else {
			//超出用量
			BigDecimal subElcUse = elcUse.subtract(stdQty);
			//总费用
			BigDecimal totalFee = subElcUse.multiply(elcRule.getOverFee()).setScale(2, RoundingMode.HALF_UP);
			//总入住天数
			int sumStayDay = staffStatementDetailElc.stream().mapToInt(SmtStaffStatementDetail::getStayDays).sum();
			//平均每人天费用
			BigDecimal avgFee = totalFee.divide(new BigDecimal(sumStayDay), 4, RoundingMode.HALF_UP);
			//设置每人的电费用
			staffStatementDetailElc.forEach(item -> {
				item.setFee(new BigDecimal(item.getStayDays()).multiply(avgFee));
			});
		}
	}

	/**
	 * 房间水结算
	 *
	 * @param staffStatementDetailWater 员工水抄表
	 * @param staffWaterRule            房间水配置规则
	 * @param waterMeterDetail          房间水抄表记录
	 */
	public void roomWaterStatement(List<SmtStaffStatementDetail> staffStatementDetailWater, StaffSDRuleRespDTO staffWaterRule, SmtSdMeterreadDetail waterMeterDetail, SmtDormitoryRoom room, SmtSdMeterreadDetail hotWaterDetail) {

		//如果是热水 所有人的费用为0
		if (SDCategoryEnum.HOT_WATER.getCode().equals(staffWaterRule.getCategoryId())) {
			for (SmtStaffStatementDetail statementDetail : staffStatementDetailWater) {
				statementDetail.setFee(BigDecimal.ZERO);
			}
			waterMeterDetail.setCurStdQty(0.0);
			waterMeterDetail.setCurOverFee(BigDecimal.ZERO);
			smtSdMeterreadDetailService.updateById(waterMeterDetail);
			return;
		}

		//热水和冷水的标准=配置标准*本月房间的入住总人数
		Double oldRule = staffWaterRule.getStandardQty();
		Double curStdQty = oldRule * staffStatementDetailWater.size();

		//房间本月使用量
		Double preNum = (waterMeterDetail.getRevPreMonthNum() != null ? waterMeterDetail.getRevPreMonthNum() : waterMeterDetail.getPreMonthNum());
		BigDecimal roomUse = BigDecimal.valueOf(waterMeterDetail.getCurMonthNum()).subtract(new BigDecimal(preNum));
		//冷水的超出用量要加上热水的使用量
		Double hotPreNum = (hotWaterDetail.getRevPreMonthNum() != null ? hotWaterDetail.getRevPreMonthNum() : hotWaterDetail.getPreMonthNum());
		BigDecimal roomHotUse = BigDecimal.valueOf(hotWaterDetail.getCurMonthNum()).subtract(new BigDecimal(hotPreNum));
		BigDecimal subUse = roomUse.add(roomHotUse).subtract(new BigDecimal(curStdQty));

		//设置标准用量和超出费用单价
		waterMeterDetail.setCurStdQty(curStdQty);
		waterMeterDetail.setCurOverFee(staffWaterRule.getOverFee());
		smtSdMeterreadDetailService.updateById(waterMeterDetail);

		if (subUse.compareTo(BigDecimal.ZERO) > 0) {
			//使用超量 每个员工都需计算金额
			//总费用
			BigDecimal totalFee = subUse.multiply(staffWaterRule.getOverFee()).setScale(4, RoundingMode.HALF_UP);
			//总入住天数
			int sumStayDay = staffStatementDetailWater.stream().mapToInt(SmtStaffStatementDetail::getStayDays).sum();
			//人天平均费用
			BigDecimal avgFee = BigDecimal.ZERO;
			if (sumStayDay > 0) {
				avgFee = totalFee.divide(new BigDecimal(sumStayDay), 4, RoundingMode.HALF_UP);
			}
			//设置每人的费用
			for (SmtStaffStatementDetail statementDetail : staffStatementDetailWater) {
				statementDetail.setFee(new BigDecimal(statementDetail.getStayDays()).multiply(avgFee));
			}
		} else {
			//使用未超量 每个员工的费用为0
			staffStatementDetailWater.forEach(item -> {
				item.setFee(BigDecimal.ZERO);
			});
		}
	}

	@Override
	public StatementDetailDTO queryRoomStatementDetail(Long mrId) {

		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		//查询宿舍抄表记录
		SmtSdMeterreadVO roomMeterRecord = smtSdMeterreadMapper.getRoomMeterRecord(mrId);
		//判断该抄表记录是否存在
		if (roomMeterRecord == null || !parkIdList.contains(roomMeterRecord.getParkId())) {
			throw new TCEException("房间抄表记录不存在");
		}

		if (!roomMeterRecord.getStatementStatus().equals(SdStatementStatusEnum.STATEMENT.getCode())) {
			//未结算
			throw new TCEException("抄表记录未结算");
		}
		StatementDetailDTO statementDetailDTO = new StatementDetailDTO();
		List<StatementDetailDTO.CategoryData> categoryDataList = new ArrayList<>();

		//查询员工结算详细数据
		List<SmtStaffStatementDetail> smtStaffStatementDetails = smtStaffStatementDetailService.list(new QueryWrapper<SmtStaffStatementDetail>().lambda()
				.eq(SmtStaffStatementDetail::getRoomId, roomMeterRecord.getRoomId())
				.eq(SmtStaffStatementDetail::getMeterMonth, roomMeterRecord.getMeterMonth())
				.orderByAsc(SmtStaffStatementDetail::getMeterType)
				.orderByAsc(SmtStaffStatementDetail::getCategoryId)
		);

		Map<Integer, List<SmtStaffStatementDetail>> staffStatementMap = smtStaffStatementDetails.stream()
				.collect(Collectors.groupingBy(SmtStaffStatementDetail::getCategoryId));

		//通过抄表记录查询房间水电抄表详情
		List<SmtSdMeterreadDetail> smtSdMeterreadDetails = smtSdMeterreadDetailService.list(new LambdaQueryWrapper<SmtSdMeterreadDetail>()
				.eq(SmtSdMeterreadDetail::getMrId, mrId));
		Map<Long, List<SmtSdMeterreadDetail>> sdMeterreadMap = smtSdMeterreadDetails.stream().collect(Collectors.groupingBy(SmtSdMeterreadDetail::getId));
		if (CollectionUtil.isNotEmpty(smtSdMeterreadDetails)) {
			smtSdMeterreadDetails.forEach(item -> {
				List<SmtStaffStatementDetail> staffStatementDetails = staffStatementMap.get(item.getCategoryId());
				//房间结算总金额
				BigDecimal totalFee = BigDecimal.ZERO;
				//房间总配额
				BigDecimal totalQty = BigDecimal.ZERO;
				//其中一个收费项目 统计房间抄表的金额
				for (SmtStaffStatementDetail detail : staffStatementDetails) {
					if (detail.getMeterType().equals(MeterTypeEnum.ROOM_METER.getCode())) {
						totalFee = totalFee.add(detail.getFee());
						if (item.getCategoryId().equals(SDCategoryEnum.ELECTRIC.getCode())) {
							//房间电的总配额为水电模板配置的额度
							totalQty = BigDecimal.valueOf(item.getCurStdQty());
						} else {
							//水的配额为所有入住人员的总配额
							//每人的配额=房间配额 / 30 * 入住天数
							totalQty = totalQty.add(BigDecimal.valueOf(item.getCurStdQty()).divide(new BigDecimal(30), 4, RoundingMode.HALF_UP)
									.multiply(new BigDecimal(detail.getStayDays())));
						}
					}
				}
				Double preNum = (item.getRevPreMonthNum() == null ? item.getPreMonthNum() : item.getRevPreMonthNum());
				Double actualNum = BigDecimal.valueOf(item.getCurMonthNum()).subtract(new BigDecimal(preNum)).setScale(2, RoundingMode.HALF_UP).doubleValue();
				categoryDataList.add(StatementDetailDTO.CategoryData.builder()
						.meterType(MeterTypeEnum.ROOM_METER.getCode())
						.actualQty(actualNum)
						.overFee(item.getCurOverFee())
						.meterStartTime(item.getStartTime())
						.meterEndTime(item.getEndTime())
						.categoryId(item.getCategoryId())
						.totalFee(totalFee)
						.preMonthNum(item.getRevPreMonthNum() == null ? item.getPreMonthNum() : item.getRevPreMonthNum())
						.curMonthNum(item.getCurMonthNum())
						.totalQty(totalQty.setScale(2, RoundingMode.HALF_UP).doubleValue())
						.overQty(new BigDecimal(actualNum).subtract(totalQty).setScale(2, RoundingMode.HALF_UP).doubleValue())
						.build()
				);
			});
		}
		//查询房间关联的公摊表
		List<SmtCommonSDRoom> commonSDRooms = smtCommonSDRoomService.list(new LambdaQueryWrapper<SmtCommonSDRoom>().eq(SmtCommonSDRoom::getRoomId, roomMeterRecord.getRoomId()));
		Map<Long, List<SmtCommonSDMeterread>> commonSDDetailMap = new HashMap<>();
		if (CollectionUtil.isNotEmpty(commonSDRooms)) {
			//查询公摊表记录
			List<Long> commonIds = commonSDRooms.stream().map(item -> item.getCommonId()).collect(Collectors.toList());
			List<SmtCommonSD> commonSDS = smtCommonSDService.list(new LambdaQueryWrapper<SmtCommonSD>().in(SmtCommonSD::getId, commonIds));
			Map<Long, List<SmtCommonSD>> commonSDMap = commonSDS.stream().collect(Collectors.groupingBy(a -> a.getId()));

			//房间抄表记录 通过房间抄表记录查询房间的配置单价
			Map<Integer, List<SmtSdMeterreadDetail>> meterDetailMap = smtSdMeterreadDetails.stream().collect(Collectors.groupingBy(a -> a.getCategoryId()));

			//查询房间本月关联的公摊抄表记录
			List<SmtCommonSDMeterread> smtCommonSDMeterreads = smtCommonSDMeterreadMapper.selectList(new LambdaQueryWrapper<SmtCommonSDMeterread>()
					.in(SmtCommonSDMeterread::getCommonId, commonIds)
					.eq(SmtCommonSDMeterread::getMeterMonth, roomMeterRecord.getMeterMonth())
			);
			commonSDDetailMap = smtCommonSDMeterreads.stream().collect(Collectors.groupingBy(SmtCommonSDMeterread::getId));
			smtCommonSDMeterreads.forEach(item -> {
				//查询公摊抄表关联的所有员工抄表数据
				List<SmtStaffStatementDetail> staffStatementDetails = smtStaffStatementDetailService.list(new LambdaQueryWrapper<SmtStaffStatementDetail>()
						.eq(SmtStaffStatementDetail::getMrdetailId, item.getId())
						.eq(SmtStaffStatementDetail::getMeterType, MeterTypeEnum.COMMON_METER.getCode())
				);
				//房间水电配置规则
				List<RoomSdRuleDTO> ruleDTOs = JSONUtil.toList(JSONUtil.parseArray(item.getRoomRuleInfo()), RoomSdRuleDTO.class);
				Map<Integer, List<RoomSdRuleDTO>> ruleMap = ruleDTOs.stream().collect(Collectors.groupingBy(a -> a.getRoomId()));
				//公摊表数据
				SmtCommonSD commonSD = commonSDMap.get(item.getCommonId()).get(0);
				BigDecimal totalFee = BigDecimal.ZERO;
				Double preNum = item.getRevPreMonthNum() != null ? item.getRevPreMonthNum() : item.getPreMonthNum();
				BigDecimal avgUse = BigDecimal.valueOf(item.getCurMonthNum()).subtract(new BigDecimal(preNum)).divide(new BigDecimal(item.getRoomCount()), 4, RoundingMode.HALF_UP);
				//统计公摊抄表的金额
				for (String roomIdStr : commonSD.getRoomList().split(",")) {
					Integer roomId = Integer.parseInt(roomIdStr);
					RoomSdRuleDTO ruleDTO = ruleMap.get(roomId).get(0);
					totalFee = totalFee.add(avgUse.multiply(ruleDTO.getOverFee()));
				}
				//统计房间均摊的费用 为该房间所有员工费用的总和
				Map<Integer, List<SmtStaffStatementDetail>> roomStaffStatementMap = staffStatementDetails.stream().collect(Collectors.groupingBy(a -> a.getRoomId()));
				List<SmtStaffStatementDetail> roomStaffStatementDetails = roomStaffStatementMap.get(roomMeterRecord.getRoomId());
				BigDecimal roomTotalFee = BigDecimal.ZERO;
				if (CollectionUtil.isEmpty(roomStaffStatementDetails)) {
					return;
				}
				for (SmtStaffStatementDetail detail : roomStaffStatementDetails) {
					roomTotalFee = roomTotalFee.add(detail.getFee());
				}

				Double actualNum = BigDecimal.valueOf(item.getCurMonthNum()).subtract(new BigDecimal(preNum)).setScale(2, RoundingMode.HALF_UP).doubleValue();
				categoryDataList.add(StatementDetailDTO.CategoryData.builder()
						.meterType(MeterTypeEnum.COMMON_METER.getCode())
						.actualQty(actualNum)
						.overFee(meterDetailMap.get(commonSD.getCategoryId()).get(0).getCurOverFee())
						.meterStartTime(item.getStartTime())
						.meterEndTime(item.getEndTime())
						.categoryId(commonSD.getCategoryId())
						.totalFee(totalFee.setScale(2, RoundingMode.HALF_UP))
						.preMonthNum(item.getRevPreMonthNum() != null ? item.getRevPreMonthNum() : item.getPreMonthNum())
						.curMonthNum(item.getCurMonthNum())
						.roomNum(commonSD.getRoomList().split(",").length)
						.roomAvgFee(roomTotalFee)
						.build()
				);
			});
		}

		//设置返回员工数据
		List<StatementDetailDTO.StaffStatmentData> staffStatmentDataList = new ArrayList<>();
		for (SmtStaffStatementDetail smtStaffStatementDetail : smtStaffStatementDetails) {
			Date startTime = null;
			Date endTime = null;
			if (smtStaffStatementDetail.getMeterType().equals(MeterTypeEnum.ROOM_METER.getCode())) {
				//房间抄表
				SmtSdMeterreadDetail meterreadDetail = sdMeterreadMap.get(smtStaffStatementDetail.getMrdetailId()).get(0);
				startTime = meterreadDetail.getStartTime();
				endTime = meterreadDetail.getEndTime();
			} else {
				//公摊抄表
				SmtCommonSDMeterread commonSDMeterread = commonSDDetailMap.get(smtStaffStatementDetail.getMrdetailId()).get(0);
				startTime = commonSDMeterread.getStartTime();
				endTime = commonSDMeterread.getEndTime();
			}
			//开始统计时间为入住时间如果入住时间为上月25号之前  则开始入住时间为上月25号
			Date statiStartTime = smtStaffStatementDetail.getInTime();
			SmtDormitoryRoom room = smtDormitoryRoomService.getById(smtStaffStatementDetail.getRoomId());
			MeterReadConfigDTO config = smtMeterreadCnfigService.calcDate(smtStaffStatementDetail.getMeterMonth(), room.getParkId());
			Date preMonth = config.getStartDate();
			if (statiStartTime.getTime() < preMonth.getTime()) {
				statiStartTime = preMonth;
			}

			Integer inTotalDays = smtStaffStatementDetail.getStayDays();
			Integer reviseDays = 0;

			//查询入住天数修改记录
			List<SmtStaffSDMHistory> historyList = smtStaffSDMHistoryService.list(new LambdaQueryWrapper<SmtStaffSDMHistory>()
					.eq(SmtStaffSDMHistory::getStaffBadge, smtStaffStatementDetail.getStaffBadge())
					.eq(SmtStaffSDMHistory::getMeterMonth, smtStaffStatementDetail.getMeterMonth())
					.orderByAsc(SmtStaffSDMHistory::getCreateTime)
			);
			if (CollectionUtil.isNotEmpty(historyList)) {
				SmtStaffSDMHistory firstHistory = historyList.get(0);
				SmtStaffSDMHistory lastHistory = historyList.get(historyList.size() - 1);
				inTotalDays = firstHistory.getOldDays();
				reviseDays = Math.abs(firstHistory.getOldDays() - lastHistory.getNewDays());
			}


			StatementDetailDTO.StaffCategoryInfo categoryInfo = StatementDetailDTO.StaffCategoryInfo.builder()
					.meterType(smtStaffStatementDetail.getMeterType())
					.categoryId(smtStaffStatementDetail.getCategoryId())
					.statiStartTime(statiStartTime)
					.statiEndTime(endTime)
					.inTotalDays(inTotalDays)
					.reviseDays(reviseDays)
					.statementDays(smtStaffStatementDetail.getStayDays())
					.statementFee(smtStaffStatementDetail.getFee())
					.avgFee(smtStaffStatementDetail.getFee().divide(new BigDecimal(smtStaffStatementDetail.getStayDays()), 2, RoundingMode.HALF_UP))
					.build();
			if (staffStatmentDataList.stream().anyMatch(a -> a.getStaffBadge().equals(smtStaffStatementDetail.getStaffBadge()))) {
				//已添加
				StatementDetailDTO.StaffStatmentData staffStatmentData = staffStatmentDataList.stream().filter(a -> a.getStaffBadge().equals(smtStaffStatementDetail.getStaffBadge())).findFirst().get();
				staffStatmentData.setFee(staffStatmentData.getFee().add(smtStaffStatementDetail.getFee()));
				staffStatmentData.getStaffCategoryInfo().add(categoryInfo);

			} else {
				//未添加
				//收费项目明细
				List<StatementDetailDTO.StaffCategoryInfo> categoryInfos = new ArrayList<>();
				categoryInfos.add(categoryInfo);
				staffStatmentDataList.add(StatementDetailDTO.StaffStatmentData.builder()
						.staffBadge(smtStaffStatementDetail.getStaffBadge())
						.inTime(smtStaffStatementDetail.getInTime())
						.staffCategoryInfo(categoryInfos)
						.staffName(smtStaffStatementDetail.getStaffName())
						.roomName(smtStaffStatementDetail.getRoomName())
						.bedNumber(smtStaffStatementDetail.getBedName())
						.fee(smtStaffStatementDetail.getFee())
						.build());
			}
		}

		//设置返回数据
		statementDetailDTO.setStatementMonth(roomMeterRecord.getMeterMonth());
		categoryDataList.sort(Comparator.comparing(StatementDetailDTO.CategoryData::getCategoryId));
		categoryDataList.sort(Comparator.comparing(StatementDetailDTO.CategoryData::getMeterType));
		statementDetailDTO.setCategoryDataList(categoryDataList);
		statementDetailDTO.setStaffStatmentDataList(staffStatmentDataList);

		return statementDetailDTO;
	}

	@Override
	public RoomSDMeterreadRespDTO queryRoomMeterStatus(RoomSDMeterreadReqDTO roomSDMeterreadReqDTO) {
		//查询房间抄表数据
		List<SmtSdMeterread> smtSdMeterreads = this.list(new LambdaQueryWrapper<SmtSdMeterread>()
				.eq(SmtSdMeterread::getMeterMonth, roomSDMeterreadReqDTO.getMeterMonth())
				.in(SmtSdMeterread::getRoomId, roomSDMeterreadReqDTO.getRoomIds())
		);
		RoomSDMeterreadRespDTO roomSDMeterreadRespDTO = new RoomSDMeterreadRespDTO();
		roomSDMeterreadRespDTO.setMeterMonth(roomSDMeterreadReqDTO.getMeterMonth());
		List<RoomSDMeterreadRespDTO.RoomMeterStatus> roomMeterStatuses = new ArrayList<>();
		//初始化为未抄表
		roomSDMeterreadReqDTO.getRoomIds().forEach(item -> {
			RoomSDMeterreadRespDTO.RoomMeterStatus roomMeterStatus = new RoomSDMeterreadRespDTO.RoomMeterStatus();
			roomMeterStatus.setRoomId(item);
			roomMeterStatus.setStatus(SdMeterreadStatusEnum.NON_METER_READ.getCode());
			roomMeterStatuses.add(roomMeterStatus);
		});
		//当前抄表状态
		if (CollectionUtil.isNotEmpty(smtSdMeterreads)) {
			smtSdMeterreads.forEach(item -> {
				RoomSDMeterreadRespDTO.RoomMeterStatus roomMeterStatus = new RoomSDMeterreadRespDTO.RoomMeterStatus();
				roomMeterStatus.setRoomId(item.getRoomId());
				roomMeterStatus.setStatus(item.getStatus());
				roomMeterStatuses.add(roomMeterStatus);
			});
		}
		roomSDMeterreadRespDTO.setRoomMeterStatuses(roomMeterStatuses);
		return roomSDMeterreadRespDTO;
	}
}
