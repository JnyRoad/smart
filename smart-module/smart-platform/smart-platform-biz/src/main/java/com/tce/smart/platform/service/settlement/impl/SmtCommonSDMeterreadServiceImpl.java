package com.tce.smart.platform.service.settlement.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.RoomSdRuleDTO;
import com.tce.smart.platform.api.dto.req.sddto.SaveCommonSDMeterreadReqDTO;
import com.tce.smart.platform.api.dto.req.sddto.SearchCommonSDMeterreadReqDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.CommonSDMeterreadRespDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.SDCategoryDTO;
import com.tce.smart.platform.core.dto.commonsd.CommonSDMeterreadDTO;
import com.tce.smart.platform.core.dto.commonsd.CommonSDRecordDTO;
import com.tce.smart.platform.core.dto.meter.MeterReadConfigDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtCommonSDMapper;
import com.tce.smart.platform.core.mapper.SmtCommonSDMeterreadMapper;
import com.tce.smart.platform.core.mapper.SmtDormitoryRoomMapper;
import com.tce.smart.platform.service.settlement.*;
import com.tce.smart.tool.enums.MeterTypeEnum;
import com.tce.smart.tool.enums.SdStatementReviseEnum;
import com.tce.smart.tool.enums.SdStatementStatusEnum;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.ToolUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: SmtCommonSDMeterreadServiceImpl
 * @date: 2020/10/9 15:38
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
public class SmtCommonSDMeterreadServiceImpl extends ServiceImpl<SmtCommonSDMeterreadMapper, SmtCommonSDMeterread> implements SmtCommonSDMeterreadService {

	@Resource
	private SmtCommonSDService smtCommonSDService;

	@Resource
	private SmtSdMeterreadService smtSdMeterreadService;

	@Resource
	private SmtMeterreadCnfigService smtMeterreadCnfigService;

	@Resource
	private SmtStaffStatementDetailService smtStaffStatementDetailService;

	@Resource
	private SmtCommonSDMapper smtCommonSDMapper;

	@Resource
	private SmtDormitoryRoomMapper smtDormitoryRoomMapper;

	@Resource
	private SmtSDPreMHistoryService smtSDPreMHistoryService;

	@Resource
	private SmtTemplatesRuleService smtTemplatesRuleService;

	@Override
	public Page<CommonSDMeterreadRespDTO> getCommonSDMeterreadHisByCate(SearchCommonSDMeterreadReqDTO searchCommonSDMeterreadReqDTO,SmtCommonSDService smtCommonSDService) {
		Page resPage = new Page(searchCommonSDMeterreadReqDTO.getCurrent(),searchCommonSDMeterreadReqDTO.getSize(),0);

		Page searchPage = new Page(searchCommonSDMeterreadReqDTO.getCurrent(),searchCommonSDMeterreadReqDTO.getSize());

		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		//按收费项目查询所有的公摊水电记录
		List<CommonSDRecordDTO> commonSDS = smtCommonSDMapper.getAllCommonSDCategoryRecord(searchCommonSDMeterreadReqDTO.getParkId()
				,searchCommonSDMeterreadReqDTO.getDormitoryId()
				,searchCommonSDMeterreadReqDTO.getCategoryId()
				,searchCommonSDMeterreadReqDTO.getMeterMonth()
				,parkIdList);

		if(CollectionUtil.isEmpty(commonSDS)){
			return resPage;
		}


		List<Long> comIdList = commonSDS.stream().map(CommonSDRecordDTO::getId).collect(Collectors.toList());
		Map<Long, List<CommonSDRecordDTO>> comCollMap = commonSDS.stream().collect(Collectors.groupingBy(CommonSDRecordDTO::getId));
		//分页查询历史记录
		IPage<SmtCommonSDMeterread> commonPage = this.page(searchPage, new LambdaQueryWrapper<SmtCommonSDMeterread>()
				.in(SmtCommonSDMeterread::getCommonId, comIdList)
				.orderByDesc(SmtCommonSDMeterread::getId)
		);
		List<CommonSDMeterreadRespDTO> commonSDMeterreadRespDTOS = new ArrayList<>();
		if(null != commonPage.getRecords()){
			commonPage.getRecords().forEach(item -> {
				CommonSDMeterreadRespDTO commonSDMeterreadRespDTO = new CommonSDMeterreadRespDTO();
				CommonSDRecordDTO commonSDRecordDTO = comCollMap.get(item.getCommonId()).get(0);

				//查询房间
				List<String> strings = Arrays.asList(commonSDRecordDTO.getRoomIdList().split(","));
				List<Integer> roomIds = strings.stream().map(Integer::parseInt).collect(Collectors.toList());
				List<SmtDormitoryRoom> roomList = smtDormitoryRoomMapper.selectList(new LambdaQueryWrapper<SmtDormitoryRoom>().in(SmtDormitoryRoom::getId, roomIds));
				List<Integer> roomNames = roomList.stream().map(SmtDormitoryRoom::getRoomName).collect(Collectors.toList());


				commonSDMeterreadRespDTO.setSdName(commonSDRecordDTO.getSdName());
				commonSDMeterreadRespDTO.setDormitoryName(commonSDRecordDTO.getDormitoryName());
				commonSDMeterreadRespDTO.setParkName(commonSDRecordDTO.getParkName());
				commonSDMeterreadRespDTO.setRoomList(StringUtils.join(roomNames,","));
				commonSDMeterreadRespDTO.setSdId(item.getCommonId());
				commonSDMeterreadRespDTO.setCategoryId(comCollMap.get(item.getCommonId()).get(0).getCategoryId());
				commonSDMeterreadRespDTO.setMeterMonth(item.getMeterMonth());
				SDCategoryDTO sdCategoryDTO = new SDCategoryDTO();
				sdCategoryDTO.setMrId(item.getId());
				if(null != item.getRevPreMonthNum()){
					//上月止度修正过
					sdCategoryDTO.setPreMonthNum(item.getRevPreMonthNum());
					commonSDMeterreadRespDTO.setIsRevise(item.getIsRevise());
					CommonSDMeterreadRespDTO.ReviseInfo reviseInfo = new CommonSDMeterreadRespDTO.ReviseInfo();
					reviseInfo.setPreMonthNum(item.getPreMonthNum());
					reviseInfo.setRevPreMonthNum(item.getRevPreMonthNum());
					reviseInfo.setCategoryId(searchCommonSDMeterreadReqDTO.getCategoryId());
					reviseInfo.setMeterUser(item.getMeterUser());
					reviseInfo.setMeterMonth(item.getMeterMonth());
					reviseInfo.setCreateTime(item.getCreateTime());
					List<CommonSDMeterreadRespDTO.ReviseInfo> reviseInfos = new ArrayList<>();
					reviseInfos.add(reviseInfo);
					commonSDMeterreadRespDTO.setReviseInfo(reviseInfos);
				} else {
					sdCategoryDTO.setPreMonthNum(item.getPreMonthNum());
				}

				sdCategoryDTO.setCurMonthNum(item.getCurMonthNum());
				sdCategoryDTO.setIsRevise(item.getIsRevise());
				BigDecimal avgNum = BigDecimal.ZERO;
				if(null != item.getTotalStayDays() && !item.getTotalStayDays().equals(0)){
					avgNum = BigDecimal.valueOf(sdCategoryDTO.getCurMonthNum() - sdCategoryDTO.getPreMonthNum()).divide(new BigDecimal(item.getTotalStayDays()),2, RoundingMode.HALF_UP);
				}
				commonSDMeterreadRespDTO.setSdCategory(sdCategoryDTO);
				commonSDMeterreadRespDTO.setAvgNum(avgNum.setScale(2).doubleValue());

				commonSDMeterreadRespDTOS.add(commonSDMeterreadRespDTO);
			});
		}
		resPage.setTotal(commonPage.getTotal());
		resPage.setRecords(commonSDMeterreadRespDTOS);
		return resPage;
	}

	@Override
	public Page<CommonSDMeterreadRespDTO> getCommonSDMeterreadHis(Page page, Long commId) {
		SmtCommonSD smtCommonSD = smtCommonSDService.getById(commId);
		if(null == smtCommonSD){
			//公摊水电表不存在
			throw new TCEException("公摊水电表不存在");
		}
		IPage<SmtCommonSDMeterread> commonPage = this.page(page, new LambdaQueryWrapper<SmtCommonSDMeterread>().eq(SmtCommonSDMeterread::getCommonId, commId));
		List<CommonSDMeterreadRespDTO> commonSDMeterreadRespDTOS = new ArrayList<>();
		if(null != commonPage.getRecords()){
			commonPage.getRecords().forEach(item -> {
				CommonSDMeterreadRespDTO commonSDMeterreadRespDTO = new CommonSDMeterreadRespDTO();
				commonSDMeterreadRespDTO.setSdId(item.getCommonId());
				commonSDMeterreadRespDTO.setCategoryId(smtCommonSD.getCategoryId());
				commonSDMeterreadRespDTO.setMeterMonth(item.getMeterMonth());
				SDCategoryDTO sdCategoryDTO = new SDCategoryDTO();
				sdCategoryDTO.setMrId(item.getId());
				if(null != item.getRevPreMonthNum()){
					//上月止度修正过
					sdCategoryDTO.setPreMonthNum(item.getRevPreMonthNum());
				} else {
					sdCategoryDTO.setPreMonthNum(item.getPreMonthNum());
				}

				sdCategoryDTO.setCurMonthNum(item.getCurMonthNum());
				sdCategoryDTO.setIsRevise(item.getIsRevise());
				BigDecimal avgNum = BigDecimal.ZERO;
				if(null != item.getTotalStayDays() && !item.getTotalStayDays().equals(0)){
					avgNum = BigDecimal.valueOf(sdCategoryDTO.getCurMonthNum() - sdCategoryDTO.getPreMonthNum()).divide(new BigDecimal(item.getTotalStayDays()),2, RoundingMode.HALF_UP);
				}
				commonSDMeterreadRespDTO.setSdCategory(sdCategoryDTO);
				commonSDMeterreadRespDTO.setAvgNum(avgNum.setScale(2).doubleValue());

				commonSDMeterreadRespDTOS.add(commonSDMeterreadRespDTO);
			});
		}
		Page resPage = new Page(commonPage.getCurrent(),commonPage.getSize(),commonPage.getTotal());
		resPage.setRecords(commonSDMeterreadRespDTOS);
		return resPage;
	}

	@Override
	public CommonSDMeterreadRespDTO getCommonSDMeterread(SearchCommonSDMeterreadReqDTO searchCommonSDMeterreadReqDTO) {
		SmtCommonSD smtCommonSD = smtCommonSDService.getById(searchCommonSDMeterreadReqDTO.getId());
		if(null == smtCommonSD){
			//公摊水电表不存在
			throw new TCEException("公摊水电表不存在");
		}

		//这里的数据应该只有一条 即一个水电表一个月只有一条抄表数据
		List<CommonSDMeterreadDTO> commonSDMeterread = this.baseMapper.getCommonSDMeterread(searchCommonSDMeterreadReqDTO.getId(), searchCommonSDMeterreadReqDTO.getMeterMonth(),smtCommonSD.getCategoryId());
		CommonSDMeterreadRespDTO commonSDMeterreadRespDTO = new CommonSDMeterreadRespDTO();
		commonSDMeterreadRespDTO.setSdId(searchCommonSDMeterreadReqDTO.getId());
		commonSDMeterreadRespDTO.setMeterMonth(searchCommonSDMeterreadReqDTO.getMeterMonth());
		commonSDMeterreadRespDTO.setCategoryId(smtCommonSD.getCategoryId());
		commonSDMeterreadRespDTO.setStatementStatus(SdStatementStatusEnum.NON_STATEMENT.getCode());
		if(CollectionUtils.isNotEmpty(commonSDMeterread)){
			SDCategoryDTO sdCategoryDTO = SDCategoryDTO.builder()
					.categoryId(commonSDMeterread.get(0).getCategoryId())
					.preMonthNum(commonSDMeterread.get(0).getPreMonthNum())
					.curMonthNum(commonSDMeterread.get(0).getCurMonthNum())
					.isRevise(commonSDMeterread.get(0).getIsRevise())
					.build();
			commonSDMeterreadRespDTO.setStatementStatus(commonSDMeterread.get(0).getStatementStatus());
			if(null != commonSDMeterread.get(0).getRevPreMonthNum()){
				sdCategoryDTO.setPreMonthNum(commonSDMeterread.get(0).getRevPreMonthNum());
			}
			commonSDMeterreadRespDTO.setIsRevise(commonSDMeterread.get(0).getIsRevise());
			if(null != commonSDMeterreadRespDTO.getIsRevise()){
				List<CommonSDMeterreadRespDTO.ReviseInfo> reviseInfos = new ArrayList<>();
				CommonSDMeterreadRespDTO.ReviseInfo reviseInfo = new CommonSDMeterreadRespDTO.ReviseInfo();
				reviseInfo.setCategoryId(commonSDMeterreadRespDTO.getCategoryId());
				reviseInfo.setMeterUser(commonSDMeterread.get(0).getMeterUser());
				reviseInfo.setMeterMonth(commonSDMeterread.get(0).getMeterMonth());
				reviseInfo.setPreMonthNum(commonSDMeterread.get(0).getPreMonthNum());
				reviseInfo.setRevPreMonthNum(commonSDMeterread.get(0).getRevPreMonthNum());
				reviseInfo.setCreateTime(commonSDMeterread.get(0).getCreateTime());
				reviseInfos.add(reviseInfo);
				commonSDMeterreadRespDTO.setReviseInfo(reviseInfos);
			}
			commonSDMeterreadRespDTO.setSdCategory(sdCategoryDTO);
		} else {
			//查询上月已结算的数据
			List<CommonSDMeterreadDTO> preCommonSDMeterread = this.baseMapper.getCommonSDMeterread(searchCommonSDMeterreadReqDTO.getId(), ToolUtils.getCalDate(searchCommonSDMeterreadReqDTO.getMeterMonth(),Calendar.MONTH,-1),smtCommonSD.getCategoryId());
			if(CollectionUtil.isNotEmpty(preCommonSDMeterread)){
				SDCategoryDTO sdCategoryDTO = SDCategoryDTO.builder()
						.categoryId(preCommonSDMeterread.get(0).getCategoryId())
						.preMonthNum(preCommonSDMeterread.get(0).getCurMonthNum())
						.build();
				commonSDMeterreadRespDTO.setSdCategory(sdCategoryDTO);
			}
		}
		return commonSDMeterreadRespDTO;
	}

	@Transactional
	@Override
	public Boolean saveCommonSDMeterread(SaveCommonSDMeterreadReqDTO saveCommonSDMeterreadReqDTO) {

		SmtCommonSD smtCommonSD = smtCommonSDService.getById(saveCommonSDMeterreadReqDTO.getCommonId());
		if(null == smtCommonSD){
			//公摊表不存在
			log.error("公摊表记录{}不存在",saveCommonSDMeterreadReqDTO.getCommonId());
			throw new TCEException("公摊表不存在");
		}

		String username = SecurityUtils.getUser().getUsername();

		//查询上月已结算的止度
		SmtCommonSDMeterread perSDMeterread = this.getOne(new LambdaQueryWrapper<SmtCommonSDMeterread>()
				.eq(SmtCommonSDMeterread::getCommonId,saveCommonSDMeterreadReqDTO.getCommonId())
				.eq(SmtCommonSDMeterread::getMeterMonth, ToolUtils.getCalDate(saveCommonSDMeterreadReqDTO.getMeterMonth(),Calendar.MONTH,-1))
				.eq(SmtCommonSDMeterread::getStatus, SdStatementStatusEnum.STATEMENT.getCode())
		);
		MeterReadConfigDTO config = smtMeterreadCnfigService.calcDate(saveCommonSDMeterreadReqDTO.getMeterMonth(), smtCommonSD.getParkId());
		Date startDate = config.getStartDate();
		Date endDate = config.getEndDate();

		//上月止度
		Double calPreMonthNum = null;
		if(null != perSDMeterread){
			calPreMonthNum = perSDMeterread.getCurMonthNum();
//			//如果存在上月的抄表记录 则当月的抄表开始时间为上月的抄表结束时间
//			startDate = perSDMeterread.getEndTime();
		}

		//查询公摊房间的总入住人天
		List<Integer> roomIdList = Arrays.asList(smtCommonSD.getRoomList().split(",")).stream().map(s -> Integer.parseInt(s.trim())).collect(Collectors.toList());
		Map<String, List<SmtStaffStatementDetail>> roomStayData = smtSdMeterreadService.getRoomStayData(roomIdList, startDate,endDate);

		int monthNum = DateUtil.month(saveCommonSDMeterreadReqDTO.getMeterMonth()) + 1;
		//查询房间的配置规则
		List<SmtDormitoryRoom> roomList = smtDormitoryRoomMapper.selectList(new LambdaQueryWrapper<SmtDormitoryRoom>()
				.in(SmtDormitoryRoom::getId, roomIdList)
		);
		List<Long> tempIds = roomList.stream().map(a -> a.getSdTemplateId()).collect(Collectors.toList());
		List<SmtTemplatesRule> ruleList = smtTemplatesRuleService.list(new LambdaQueryWrapper<SmtTemplatesRule>()
				.in(SmtTemplatesRule::getTempId, tempIds)
				.eq(SmtTemplatesRule::getMonthNum, monthNum)
				.eq(SmtTemplatesRule::getCategoryId,smtCommonSD.getCategoryId())
		);
		Map<Long, List<SmtTemplatesRule>> ruleMap = ruleList.stream().collect(Collectors.groupingBy(a -> a.getTempId()));
		List<RoomSdRuleDTO> ruleInfo = new ArrayList<>();
		for(SmtDormitoryRoom room : roomList){
			SmtTemplatesRule smtTemplatesRule = ruleMap.get(room.getSdTemplateId()).get(0);
			ruleInfo.add(RoomSdRuleDTO.builder()
					.roomId(room.getId())
					.categoryId(smtTemplatesRule.getCategoryId())
					.curQty(smtTemplatesRule.getStandardQty())
					.overFee(smtTemplatesRule.getOverFee())
					.build());
		}

		int sumDay = 0;
		for(var stay : roomStayData.entrySet()){
			long days = stay.getValue().stream().collect(Collectors.summarizingInt(SmtStaffStatementDetail::getStayDays)).getSum();
			sumDay += days;
		}

		if(sumDay == 0){
			log.error("{}房间列表({})没有入住人天",saveCommonSDMeterreadReqDTO.getMeterMonth(),smtCommonSD.getRoomList());
			throw new TCEException("入住记录不存在");
		}

		Long commonMrDetailId = 0L;
		SmtCommonSDMeterread build;
		//上次抄表人
		String preUserName = "";
		//上次填写的上月止度
		Double writePreMonthNum = -1.0;

		//查询本月抄表数据
		SmtCommonSDMeterread sDMeterread = this.getOne(new LambdaQueryWrapper<SmtCommonSDMeterread>()
				.eq(SmtCommonSDMeterread::getCommonId,saveCommonSDMeterreadReqDTO.getCommonId())
				.eq(SmtCommonSDMeterread::getMeterMonth, saveCommonSDMeterreadReqDTO.getMeterMonth())
		);

		if(null != sDMeterread){

			if(SdStatementStatusEnum.STATEMENT.getCode().equals(sDMeterread.getStatus())){
				//已结算 不能再修改
				throw new TCEException("已结算，不能修改");
			}

			//更新
			commonMrDetailId = sDMeterread.getId();
			//查询抄表记录
			SmtCommonSDMeterread commonSDMeterread = this.getById(commonMrDetailId);
			preUserName = commonSDMeterread.getMeterUser();
			writePreMonthNum = commonSDMeterread.getRevPreMonthNum() != null ? commonSDMeterread.getRevPreMonthNum() : commonSDMeterread.getPreMonthNum();
			build = SmtCommonSDMeterread.builder()
					.id(commonMrDetailId)
					.preMonthNum(saveCommonSDMeterreadReqDTO.getPreMonthNum())
					.curMonthNum(saveCommonSDMeterreadReqDTO.getCurMonthNum())
					.totalStayDays(sumDay)
					.startTime(startDate)
					.endTime(endDate)
					.roomCount(smtCommonSD.getRoomList().split(",").length)
					.meterUser(username)
					.roomRuleInfo(JSONUtil.toJsonStr(ruleInfo))
					.build();
			if(!saveCommonSDMeterreadReqDTO.getPreMonthNum().equals(calPreMonthNum)){
				//传入的上月数据和计算的上月数据不一致时  记录手动调整过
				build.setPreMonthNum(null != calPreMonthNum ? calPreMonthNum : -1);
				build.setRevPreMonthNum(saveCommonSDMeterreadReqDTO.getPreMonthNum());
				build.setIsRevise(SdStatementReviseEnum.REVISE.getCode());
			} else {
				build.setRevPreMonthNum(null);
				build.setIsRevise(SdStatementReviseEnum.NON_REVISE.getCode());
			}
			this.updateById(build);
		} else {
			//新增
			build = SmtCommonSDMeterread.builder()
					.commonId(saveCommonSDMeterreadReqDTO.getCommonId())
					.meterMonth(saveCommonSDMeterreadReqDTO.getMeterMonth())
					.preMonthNum(saveCommonSDMeterreadReqDTO.getPreMonthNum())
					.curMonthNum(saveCommonSDMeterreadReqDTO.getCurMonthNum())
					.status(SdStatementStatusEnum.NON_STATEMENT.getCode())
					.totalStayDays(sumDay)
					.meterUser(username)
					.roomCount(smtCommonSD.getRoomList().split(",").length)
					.startTime(startDate)
					.endTime(endDate)
					.createTime(new Date())
					.roomRuleInfo(JSONUtil.toJsonStr(ruleInfo))
					.build();
			if(!saveCommonSDMeterreadReqDTO.getPreMonthNum().equals(calPreMonthNum)){
				//传入的上月数据和计算的上月数据不一致时  记录手动调整过
				build.setPreMonthNum(null != calPreMonthNum ? calPreMonthNum : -1);
				build.setRevPreMonthNum(saveCommonSDMeterreadReqDTO.getPreMonthNum());
				build.setIsRevise(SdStatementReviseEnum.REVISE.getCode());
			}

			this.save(build);
			commonMrDetailId = build.getId();
		}

		if(SdStatementReviseEnum.REVISE.getCode().equals(build.getIsRevise())) {
			//记录修正数据
			smtSDPreMHistoryService.save(SmtSDPreMHistory.builder()
					.mrId(smtCommonSD.getId())
					.mrdetailId(commonMrDetailId)
					.categoryId(smtCommonSD.getCategoryId())
					.preUserName(preUserName)
					.userName(username)
					.meterType(MeterTypeEnum.COMMON_METER.getCode())
					.meterMonth(saveCommonSDMeterreadReqDTO.getMeterMonth())
					.oldNum(writePreMonthNum)
					.newNum(build.getRevPreMonthNum())
					.remark("")
					.createTime(new Date())
					.build());
		}

		//房间数量
		int roomNum = smtCommonSD.getRoomList().split(",").length;
		//每个房间的平均用量
		BigDecimal roomAvgUse = BigDecimal.valueOf(saveCommonSDMeterreadReqDTO.getCurMonthNum()).subtract(BigDecimal.valueOf(saveCommonSDMeterreadReqDTO.getPreMonthNum())).divide(new BigDecimal(roomNum),4, RoundingMode.HALF_UP);
		//记录员工的水电使用数据
		for(var item : roomStayData.entrySet()){
			smtStaffStatementDetailService.saveStaffSdRecord(item.getValue(),smtCommonSD.getId(),commonMrDetailId,smtCommonSD.getCategoryId(),saveCommonSDMeterreadReqDTO.getMeterMonth(),roomAvgUse);
		}
		return true;
	}

	@Transactional
	@Override
	public Boolean delCommonSDMeterread(Long id) {
		//查询抄表记录是否存在
		SmtCommonSDMeterread commonSDMeterread = this.getById(id);
		if(null == commonSDMeterread){
			//公摊抄表抄表记录不存在
			log.error("公摊抄表抄表记录不存在:{}",id);
			throw new TCEException("抄表记录不存在");
		}

		if(commonSDMeterread.getStatus().equals(SdStatementStatusEnum.STATEMENT.getCode())){
			//抄表记录已结算
			log.error("抄表记录已结算,不能删除:{}",id);
			throw new TCEException("抄表记录已结算，不能删除");
		}
		//删除员工抄表数据
		smtStaffStatementDetailService.remove(new LambdaQueryWrapper<SmtStaffStatementDetail>()
				.eq(SmtStaffStatementDetail::getMrdetailId,id)
				.eq(SmtStaffStatementDetail::getMeterType, MeterTypeEnum.COMMON_METER.getCode())
		);
		//抄表数据执行物理删除
		return this.removeById(id);
	}
}
