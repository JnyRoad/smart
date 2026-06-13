package com.tce.smart.platform.service.settlement.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.ResetSdDetailReqDTO;
import com.tce.smart.platform.api.dto.req.sddto.SdMeterreadDetailReqDTO;
import com.tce.smart.platform.api.dto.resp.SmtSdMeterreadDetailRespDTO;
import com.tce.smart.platform.core.dto.meter.MeterReadConfigDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtSdMeterreadDetailMapper;
import com.tce.smart.platform.core.mapper.SmtSdMeterreadMapper;
import com.tce.smart.platform.service.SmtDormitoryRoomService;
import com.tce.smart.platform.service.settlement.*;
import com.tce.smart.tool.enums.MeterTypeEnum;
import com.tce.smart.tool.enums.SdMeterreadStatusEnum;
import com.tce.smart.tool.enums.SdStatementReviseEnum;
import com.tce.smart.tool.enums.SdStatementStatusEnum;
import com.tce.smart.tool.util.ToolUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: SmtSdMeterreadDetailServiceImpl
 * @date: 2020-07-13 15:50
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
public class SmtSdMeterreadDetailServiceImpl extends ServiceImpl<SmtSdMeterreadDetailMapper, SmtSdMeterreadDetail> implements SmtSdMeterreadDetailService {

	@Resource
	private SmtSdMeterreadMapper smtSdMeterreadMapper;

	@Resource
	private SmtStaffStatementDetailService smtStaffStatementDetailService;

	@Resource
	private SmtSDPreMHistoryService smtSDPreMHistoryService;

	@Resource
	private SmtDormitoryRoomService smtDormitoryRoomService;


	@Resource
	private SmtMeterreadCnfigService smtMeterreadCnfigService;

	@Value("${smart.xc-park-id}")
	private Integer xcParkId;

	@Transactional
	@Override
	public Boolean saveMeterReadDetail(SdMeterreadDetailReqDTO sdMeterreadDetailReqDTO, SmtSdMeterreadService smtSdMeterreadService) {
		String username;
		try {
			username = SecurityUtils.getUser().getUsername();
		} catch (Exception e) {
			log.error("未登录，默认采用platform用户");
			username = "platform";
		}
		//String username = "platform";
		//1. 检查抄表月是否生成抄表数据
		SmtSdMeterread sdMeterread = smtSdMeterreadMapper.selectOne(new LambdaQueryWrapper<SmtSdMeterread>()
				.eq(SmtSdMeterread::getRoomId, sdMeterreadDetailReqDTO.getRoomId())
				.eq(SmtSdMeterread::getMeterMonth, sdMeterreadDetailReqDTO.getMeterMonth())
		);

		if(null == sdMeterread){
			//生成抄表记录
			sdMeterread = SmtSdMeterread.builder()
					.roomId(sdMeterreadDetailReqDTO.getRoomId())
					.meterMonth(sdMeterreadDetailReqDTO.getMeterMonth())
					.status(SdMeterreadStatusEnum.NON_METER_READ.getCode())
					.statementStatus(SdStatementStatusEnum.NON_STATEMENT.getCode())
					.createTime(new Date())
					.build();
			smtSdMeterreadService.save(sdMeterread);
		} else if(sdMeterread.getStatementStatus().equals(SdStatementStatusEnum.STATEMENT.getCode())){
			//房间水电已结算 按照新逻辑 可以继续修改抄表并结算
			sdMeterread.setStatementStatus(SdStatementStatusEnum.NON_STATEMENT.getCode());
			smtSdMeterreadService.updateById(sdMeterread);

			//清除水电详情表中的标准数据
			this.baseMapper.updateMeterDetailQty(sdMeterread.getId());
		}

		//查询房间住宿数据
		List<Integer> roomIds = new ArrayList<>();
		roomIds.add(sdMeterreadDetailReqDTO.getRoomId());

		//查询房间上月的结算数据
		SmtSdMeterread preSdMeterread = smtSdMeterreadService.getOne(new LambdaQueryWrapper<SmtSdMeterread>()
				.eq(SmtSdMeterread::getRoomId, sdMeterreadDetailReqDTO.getRoomId())
				.eq(SmtSdMeterread::getMeterMonth, ToolUtils.getCalDate(sdMeterreadDetailReqDTO.getMeterMonth(), Calendar.MONTH, -1))
				.eq(SmtSdMeterread::getStatementStatus, SdStatementStatusEnum.STATEMENT.getCode())
		);

		if(CollectionUtil.isNotEmpty(sdMeterreadDetailReqDTO.getMeterReadDetailList())) {
			//房间抄表标识
			final long mrId = sdMeterread.getId();
			//查询房间抄表记录
			SmtSdMeterreadDetailRespDTO meterReadDetail = this.getMeterReadDetail(sdMeterread.getId());
			List<SmtSdMeterreadDetailRespDTO.MeterReadDetail> meterReadDetailList = meterReadDetail.getMeterReadDetailList();
			//按收费项目分组
			Map<Integer, List<SmtSdMeterreadDetailRespDTO.MeterReadDetail>> collect = meterReadDetailList.stream().collect(Collectors.groupingBy(SmtSdMeterreadDetailRespDTO.MeterReadDetail::getCategoryId));

			//查询上月的抄表详情
			SmtSdMeterreadDetailRespDTO preMeterReadDetail = null;
			Map<Integer, SmtSdMeterreadDetailRespDTO.MeterReadDetail> preCollect = new HashMap<>();
			if (null != preSdMeterread) {
				preMeterReadDetail = this.getMeterReadDetail(preSdMeterread.getId());
				//上月抄表
				preCollect = preMeterReadDetail.getMeterReadDetailList().stream().collect(Collectors.toMap(SmtSdMeterreadDetailRespDTO.MeterReadDetail::getCategoryId, detail -> detail));
			}
			//本月时间
			SmtDormitoryRoom room = smtDormitoryRoomService.getById(sdMeterreadDetailReqDTO.getRoomId());
			MeterReadConfigDTO config = smtMeterreadCnfigService.calcDate(sdMeterreadDetailReqDTO.getMeterMonth(), room.getParkId());
			Date startDate = config.getStartDate();
			Date endDate = config.getEndDate();
			//按房间号和月份查询房间的入住情况  这里房间号只有一个 所以开始时间和结算时间就是当前房间的时间
			Map<String, List<SmtStaffStatementDetail>> roomStayData = smtSdMeterreadService.getRoomStayData(roomIds, startDate,endDate);
			log.info("查询房间列表某月的入住详情: {}", roomStayData);
			int size = roomStayData.get(sdMeterreadDetailReqDTO.getRoomId().toString()) != null ? roomStayData.get(sdMeterreadDetailReqDTO.getRoomId().toString()).size() : 0;

			log.info("查询房间住宿数据:roomId={},size={}",sdMeterreadDetailReqDTO.getRoomId(),size);


			//2. 生成或更新抄表详情记录
			for (SdMeterreadDetailReqDTO.MeterReadDetail detail : sdMeterreadDetailReqDTO.getMeterReadDetailList()) {
				//数据不全不处理
				if (null == detail.getCategoryId() || null == detail.getPreMonthNum() || null == detail.getCurMonthNum()) {
					continue;
				}

				//本次抄表入住总天数
				Integer sumStay = 0;

				//当前房间的入住情况
				List<SmtStaffStatementDetail> staffStatementDetails = new ArrayList<>();
				if(CollectionUtil.isNotEmpty(roomStayData)){
					staffStatementDetails = roomStayData.get(sdMeterreadDetailReqDTO.getRoomId().toString());
					sumStay = staffStatementDetails.stream().mapToInt(SmtStaffStatementDetail::getStayDays).sum();
				}

				//上月计算的止度
				Double calPreNum = -1.0;
				//上次抄表人
				String preUserName = "";
				//上次填写的上月止度
				Double writePreMonthNum = -1.0;
				if (preCollect.containsKey(detail.getCategoryId())) {
					calPreNum = preCollect.get(detail.getCategoryId()).getCurMonthNum();
				}

				SmtSdMeterreadDetail build = SmtSdMeterreadDetail.builder()
						.mrId(mrId)
						.preMonthNum(detail.getPreMonthNum())
						.curMonthNum(detail.getCurMonthNum())
						.categoryId(detail.getCategoryId())
						.totalStayDays(sumStay)
						.meterUser(username)
						.startTime(startDate)
						.endTime(endDate)
						.createTime(new Date())
						.build();
				if (!calPreNum.equals(detail.getPreMonthNum())) {
					//上月止度被修改过
					build.setPreMonthNum(calPreNum);
					build.setRevPreMonthNum(detail.getPreMonthNum());
					build.setIsRevise(SdStatementReviseEnum.REVISE.getCode());
				} else {
					//未修正
					build.setPreMonthNum(detail.getPreMonthNum());
					build.setRevPreMonthNum(null);
					build.setIsRevise(SdStatementReviseEnum.NON_REVISE.getCode());
				}
				//本月该收费项目是否已存在记录
				if (collect.containsKey(detail.getCategoryId())) {
					SmtSdMeterreadDetailRespDTO.MeterReadDetail meterReadDetail1 = collect.get(detail.getCategoryId()).get(0);
					preUserName = meterReadDetail1.getMeterUser();
					writePreMonthNum = meterReadDetail1.getRevPreMonthNum() != null ? meterReadDetail1.getRevPreMonthNum() : meterReadDetail1.getPreMonthNum();

					if(SdStatementReviseEnum.NON_REVISE.getCode().equals(build.getIsRevise())){
						//清除修正信息
						this.baseMapper.updateMeterDetailRevById(meterReadDetail1.getId());
					}
					//更新
					build.setId(meterReadDetail1.getId());
					this.updateById(build);

				} else {
					//添加
					this.save(build);
				}

				//删除当前房间人员结算详细
				smtStaffStatementDetailService.remove(new LambdaQueryWrapper<SmtStaffStatementDetail>().eq(SmtStaffStatementDetail::getMrId,mrId)
						.eq(SmtStaffStatementDetail::getCategoryId,build.getCategoryId()));

				//添加员工抄表住宿信息
				addStaffMeterInfo(sdMeterread,build,staffStatementDetails);

				if(!detail.getPreMonthNum().equals(writePreMonthNum)){
					//上月止度被修改过
					SmtSDPreMHistory preMHistory = SmtSDPreMHistory.builder()
							.mrId(mrId)
							.mrdetailId(build.getId())
							.categoryId(build.getCategoryId())
							.userName(username)
							.preUserName(preUserName)
							.meterType(MeterTypeEnum.ROOM_METER.getCode())
							.meterMonth(sdMeterreadDetailReqDTO.getMeterMonth())
							.oldNum(writePreMonthNum)
							.newNum(detail.getPreMonthNum())
							.remark("")
							.createTime(new Date())
							.build();
					smtSDPreMHistoryService.save(preMHistory);
				}
			}
		}

		//查询当前房间的抄表数据是否抄完
		int count = this.count(new LambdaQueryWrapper<SmtSdMeterreadDetail>().eq(SmtSdMeterreadDetail::getMrId, sdMeterread.getId()));
		//更新抄表状态如果有3条记录 表示已经抄完
		SmtSdMeterread smtSdMeterread = new SmtSdMeterread();
		smtSdMeterread.setId(sdMeterread.getId());
		if(count == 3){
			smtSdMeterread.setStatus(SdMeterreadStatusEnum.ALL_METER_READ.getCode());
		} else if (count == 0){
			smtSdMeterread.setStatus(SdMeterreadStatusEnum.NON_METER_READ.getCode());
		} else {
			smtSdMeterread.setStatus(SdMeterreadStatusEnum.HALF_METER_READ.getCode());
		}
		smtSdMeterreadService.updateById(smtSdMeterread);

		return true;

	}

	/**
	 * 添加员工抄表住宿信息
	 * @param smtSdMeterread
	 * @param sdMeterreadDetail
	 * @param staffStatementDetails
	 */
	private void addStaffMeterInfo(SmtSdMeterread smtSdMeterread,SmtSdMeterreadDetail sdMeterreadDetail,List<SmtStaffStatementDetail> staffStatementDetails){
		if(CollectionUtil.isEmpty(staffStatementDetails)){
			return;
		}
		log.info("待添加的员工抄表数据：{}", staffStatementDetails);
		//待添加的员工抄表数据
		List<SmtStaffStatementDetail> addList = new ArrayList<>();
		staffStatementDetails.forEach(det -> {
			if(det.getStayDays().intValue() == 0){
				return;
			}
			det.setId(null);
			det.setMrId(smtSdMeterread.getId());
			det.setMeterMonth(smtSdMeterread.getMeterMonth());
			det.setMrdetailId(sdMeterreadDetail.getId());
			det.setCategoryId(sdMeterreadDetail.getCategoryId());
			Double realPreNum = (null != sdMeterreadDetail.getRevPreMonthNum() ? sdMeterreadDetail.getRevPreMonthNum() : sdMeterreadDetail.getPreMonthNum());
			det.setUsage(0.0);
			if(null != sdMeterreadDetail.getTotalStayDays() && sdMeterreadDetail.getTotalStayDays() > 0) {
				BigDecimal multiply = new BigDecimal(sdMeterreadDetail.getCurMonthNum() - realPreNum).divide(new BigDecimal(sdMeterreadDetail.getTotalStayDays()), 4, RoundingMode.HALF_UP)
						.multiply(new BigDecimal(det.getStayDays())).setScale(2, RoundingMode.HALF_UP);
				det.setUsage(multiply.doubleValue());
			}
			det.setMeterType(MeterTypeEnum.ROOM_METER.getCode());
			addList.add(det);
		});

		if(CollectionUtil.isNotEmpty(addList)){
			//smtStaffStatementDetailService.saveBatch(addList);
			for(SmtStaffStatementDetail detail : addList){
				try{
					smtStaffStatementDetailService.save(detail);
				}catch (Exception e){
					log.info("添加数据:{}", JSONUtil.toJsonStr(detail));
					log.error("导入水电异常:",e);
				}
			}
		}
	}

	@Override
	public SmtSdMeterreadDetailRespDTO getMeterReadDetail(Long mrId) {

		//查询房间抄表记录
		SmtSdMeterread roomMeterRecord = smtSdMeterreadMapper.selectById(mrId);

		//查询已填写的记录详细
		List<SmtSdMeterreadDetail> smtSdMeterreadDetails = this.list(new LambdaQueryWrapper<SmtSdMeterreadDetail>().eq(SmtSdMeterreadDetail::getMrId,mrId));

		//组装响应数据
		SmtSdMeterreadDetailRespDTO smtSdMeterreadDetailRespDTO = new SmtSdMeterreadDetailRespDTO();
		smtSdMeterreadDetailRespDTO.setMrId(mrId);
		smtSdMeterreadDetailRespDTO.setMeterMonth(roomMeterRecord.getMeterMonth());
		List<SmtSdMeterreadDetailRespDTO.MeterReadDetail> meterReadDetails = new ArrayList<>();
		for(SmtSdMeterreadDetail smtSdMeterreadDetail : smtSdMeterreadDetails){
			SmtSdMeterreadDetailRespDTO.MeterReadDetail detail = new SmtSdMeterreadDetailRespDTO.MeterReadDetail();
			detail.setId(smtSdMeterreadDetail.getId());
			detail.setCategoryId(smtSdMeterreadDetail.getCategoryId());
			detail.setTotalStayDays(smtSdMeterreadDetail.getTotalStayDays());
			detail.setPreMonthNum(smtSdMeterreadDetail.getPreMonthNum());
			detail.setCurMonthNum(smtSdMeterreadDetail.getCurMonthNum());
			detail.setRevPreMonthNum(smtSdMeterreadDetail.getRevPreMonthNum());
			detail.setIsRevise(smtSdMeterreadDetail.getIsRevise());
			detail.setMeterUser(smtSdMeterreadDetail.getMeterUser());
			detail.setStartTime(smtSdMeterreadDetail.getStartTime());
			detail.setEndTime(smtSdMeterreadDetail.getEndTime());
			meterReadDetails.add(detail);
		}

		smtSdMeterreadDetailRespDTO.setMeterReadDetailList(meterReadDetails);

		return smtSdMeterreadDetailRespDTO;
	}

    @Override
    public SmtSdMeterreadDetailRespDTO getPreMonthDetail(Integer roomId, Date meterMonth) {
		//计算上月时间
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(meterMonth);
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
		Date preMonth = calendar.getTime();

		SmtDormitoryRoom dormitoryRoom = smtDormitoryRoomService.getById(roomId);

		LambdaQueryWrapper<SmtSdMeterread> queryWrapper = new LambdaQueryWrapper<SmtSdMeterread>()
				.eq(SmtSdMeterread::getRoomId, roomId)
				.eq(SmtSdMeterread::getStatus, SdMeterreadStatusEnum.ALL_METER_READ.getCode())
				.eq(SmtSdMeterread::getMeterMonth, preMonth);

		if(!xcParkId.equals(dormitoryRoom.getParkId())){
			//不是许昌园区 查询已结算的数据
			queryWrapper.eq(SmtSdMeterread::getStatementStatus, SdStatementStatusEnum.STATEMENT.getCode());
		}

		//查询抄表记录
		SmtSdMeterread roomMeterRecord = smtSdMeterreadMapper.selectOne(queryWrapper);

		if(roomMeterRecord == null){
			//没有上月的结算数据
			return null;
		}

		return getMeterReadDetail(roomMeterRecord.getId());
    }

    @Transactional
	@Override
	public Boolean resetSdMeterDetail(ResetSdDetailReqDTO resetSdDetailReqDTO,SmtSdMeterreadService smtSdMeterreadService) {
		if(Objects.isNull(resetSdDetailReqDTO.getDormitoryId()) && Objects.isNull(resetSdDetailReqDTO.getFloorId()) && Objects.isNull(resetSdDetailReqDTO.getRoomId())){
			new SmartException("请选择楼栋、楼层或房间");
		}
		List<SmtDormitoryRoom> dormitoryRooms = smtDormitoryRoomService.list(new LambdaQueryWrapper<SmtDormitoryRoom>()
				.eq(Objects.nonNull(resetSdDetailReqDTO.getDormitoryId()), SmtDormitoryRoom::getDormitoryId, resetSdDetailReqDTO.getDormitoryId())
				.eq(Objects.nonNull(resetSdDetailReqDTO.getFloorId()), SmtDormitoryRoom::getFloorId, resetSdDetailReqDTO.getFloorId())
				.eq(Objects.nonNull(resetSdDetailReqDTO.getRoomId()), SmtDormitoryRoom::getId, resetSdDetailReqDTO.getRoomId())
		);
		List<Integer> roomIds = dormitoryRooms.stream().map(SmtDormitoryRoom::getId).collect(Collectors.toList());
		List<SmtSdMeterread> smtSdMeterreads = smtSdMeterreadMapper.selectList(new LambdaQueryWrapper<SmtSdMeterread>()
				.in(SmtSdMeterread::getRoomId, roomIds)
				.eq(SmtSdMeterread::getMeterMonth,resetSdDetailReqDTO.getMeterMonth())
		);

		smtSdMeterreads.forEach(item -> {
			List<SmtSdMeterreadDetail> smtSdMeterreadDetails = this.list(new LambdaQueryWrapper<SmtSdMeterreadDetail>().eq(SmtSdMeterreadDetail::getMrId, item.getId()));
			SdMeterreadDetailReqDTO sdMeterreadDetailReqDTO = new SdMeterreadDetailReqDTO();
			sdMeterreadDetailReqDTO.setMrId(item.getId());
			sdMeterreadDetailReqDTO.setRoomId(item.getRoomId());
			sdMeterreadDetailReqDTO.setMeterMonth(resetSdDetailReqDTO.getMeterMonth());

			List<SdMeterreadDetailReqDTO.MeterReadDetail> meterReadDetailList = new ArrayList<>();
			smtSdMeterreadDetails.forEach(detail -> {
				SdMeterreadDetailReqDTO.MeterReadDetail meterReadDetail = new SdMeterreadDetailReqDTO.MeterReadDetail();
				meterReadDetail.setPreMonthNum(SdStatementReviseEnum.REVISE.getCode().equals(detail.getIsRevise()) ? detail.getRevPreMonthNum() : detail.getPreMonthNum());
				meterReadDetail.setCurMonthNum(detail.getCurMonthNum());
				meterReadDetail.setCategoryId(detail.getCategoryId());
				meterReadDetailList.add(meterReadDetail);
			});

			sdMeterreadDetailReqDTO.setMeterReadDetailList(meterReadDetailList);

			saveMeterReadDetail(sdMeterreadDetailReqDTO,smtSdMeterreadService);
		});

		return true;
	}
}
