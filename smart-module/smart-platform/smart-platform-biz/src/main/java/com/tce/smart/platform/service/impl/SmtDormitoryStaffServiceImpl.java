package com.tce.smart.platform.service.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.algorithm.api.dto.req.CompareDTO;
import com.tce.smart.algorithm.api.dto.req.CompareImageDTO;
import com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum;
import com.tce.smart.algorithm.api.enums.FaceTypeEnum;
import com.tce.smart.algorithm.api.feign.RemoteAlgorithmService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.ExceptionEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.ehrview.OvwYsCallOwanceDetailsDTO;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYsCallOwanceDetailsService;
import com.tce.smart.platform.api.dto.DormitoryStaffExcelDTO;
import com.tce.smart.platform.api.dto.req.DorStaffPerfectDTO;
import com.tce.smart.platform.api.dto.req.LockPwdUpdateDTO;
import com.tce.smart.platform.api.dto.req.remoteLock.LockDormitoryStaffDTO;
import com.tce.smart.platform.api.dto.resp.DormitoryRoomDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.DormitoryStaffRespDTO;
import com.tce.smart.platform.api.dto.resp.dormitorymange.DormitoryLockInfoRespDTO;
import com.tce.smart.platform.api.dto.resp.remoteLock.DeviceTypeInfoDTO;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.entity.ext.DormitoryRoomExt;
import com.tce.smart.platform.core.mapper.*;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.vo.InDormitoryVO;
import com.tce.smart.platform.core.vo.StaffInDormitoryVO;
import com.tce.smart.platform.core.vo.StaffInfoVO;
import com.tce.smart.platform.emun.RoomSexEnum;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryPersonService;
import com.tce.smart.platform.service.remoteLock.ConnectLockService;
import com.tce.smart.tool.constant.RedisKeyConstants;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.util.IOUtils;
import com.tce.smart.tool.util.RegexUtils;
import com.tce.smart.tool.util.ToolUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 员工宿舍信息表
 *
 * @author 齐佩
 * @date 2019-04-18 14:32:40
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmtDormitoryStaffServiceImpl extends ServiceImpl<SmtDormitoryStaffMapper, SmtDormitoryStaff> implements SmtDormitoryStaffService {

	private final SmtDormitoryStaffMapper mapper;

	private final SmtDormitoryMapper dormitoryMapper;

	private final SmtDormitoryOutRemarkService smtDormitoryOutRemarkService;

	private final SmtDormitoryFloorMapper floorMapper;

	private final SmtDormitoryRoomMapper roomMapper;

	private final SmtDormitoryBedMapper bedMapper;

	private final SmtParkService parkService;

	private final SmtDormitoryTypeMapper dormitoryTypeMapper;

	private final ConnectLockService connectLockService;

	private final SmtStaffService staffService;

	private final SmtDormitoryLevelService levelService;

	private final SmtDormitoryStaffHistoryService hisService;

	private final SmtDormitoryStaffHistoryMapper smtDormitoryStaffHistoryMapper;

	private final RemoteOvwYsCallOwanceDetailsService ovwYsCallOwanceDetailsService;

	private final SmtDormitoryPersonService smtDormitoryPersonService;

	private final SmtImageService smtImageService;

	private final RemoteAlgorithmService remoteAlgorithmService;

	private final StringRedisTemplate redisTemplate;

	private final SmtParkBuService smtParkBuService;

	/**
	 * 人脸登陆比对阀值
	 */
	@Value("${spring.face.login-compare-value}")
	private String loginCompareValue;
	@Value("${smart.xc-park-id:0}")
	private Integer xcParkId;

	@Value("${smart.lock.password.limit:5}")
	private Integer lockPasswordLimit;

	/**
	 * 查询内宿员工
	 */
	@Override
	public IPage<StaffInDormitoryVO> getSmtDormitoryStaff(Page page, StaffInDormitoryDTO staffInDormitoryDTO) {
		String userName = SecurityUtils.getUser().getUsername();
		List<Integer> parkIdList = smtDormitoryPersonService.getParkId(userName);
		if (CollUtil.isEmpty(parkIdList)) {
			parkIdList = SecurityUtils.getUser().getParkIdList();
		}
		List<Integer> dormitoryIds = smtDormitoryPersonService.getDormitoryId(userName, null);
		if (CollUtil.isNotEmpty(dormitoryIds)) {
			staffInDormitoryDTO.setDormitoryIds(dormitoryIds);
		}
		return mapper.getSmtDormitoryStaff(page, staffInDormitoryDTO, parkIdList);
	}

	@Override
	public Boolean deleteNotRegister(List<Integer> ids) {
		if (CollUtil.isEmpty(ids)) {
			return Boolean.FALSE;
		}
		ids.forEach(id -> {
			SmtDormitoryStaff selectById = this.getById(id);
			SmtStaff staff = staffService.getStaffByBadgeAll(selectById.getStaffBadge());
			//修改入职状态
			staff.setStatus(StaffStatusEnum.STAFF_STATUS_QUIT.getCode());
			//修改住宿状态
			staff.setDormitoryStatus(DormitoryStatusEnum.IS_INIT.getCode());
			//计算是否参与水电分摊，住宿时间大于3天分摊
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.DAY_OF_MONTH, -3);
			Integer flag = c.getTime().compareTo(selectById.getCreateTime());
			DormitoryStatisFlagnum enums = (flag < 0) ? DormitoryStatisFlagnum.NO_STATIS : DormitoryStatisFlagnum.STATIS;
			if (this.removeById(id)) {
				//修改员工状态
				staffService.updateById(staff);
				//将退宿记录插入历史表
				addDormitoryHistory(selectById, DormitoryHisotryTypeEnum.OUT_DORMITORY.getCode(), null, enums);
			}
		});
		return Boolean.TRUE;
	}

	@Override
	public List<LockDormitoryStaffDTO> getSmtDormitoryStaffToLock(Integer parkId, String createTime) {
		return this.mapper.getToLock(createTime, parkId);
	}

	@Override
	public Boolean editSimpleRemark(Integer id, String remark) {
		SmtDormitoryStaff dStaff = this.getById(id);
		dStaff.setSimpleRemark(remark);
		return this.updateById(dStaff);
	}

	@Override
	public SmtDormitoryStaff getDormitoryStaff(Integer dormitoryId, Integer floorId, Integer roomId, Integer bedId) {
		return this.getBaseMapper().selectOne(Wrappers.<SmtDormitoryStaff>query().lambda()
				.eq(SmtDormitoryStaff::getDormitoryId, dormitoryId)
				.eq(SmtDormitoryStaff::getFloorId, floorId)
				.eq(SmtDormitoryStaff::getRoomId, roomId)
				.eq(SmtDormitoryStaff::getBedId, bedId));
	}


	@Override
	public Result addInDormitory(InDormitoryDTO inDormitory) {
		// TODO Auto-generated method stub
		//根据员工的性别	和职层来查询所有的空床位
		if (inDormitory == null) {
			return new Result<>(Boolean.FALSE, "申请内宿信息为空，申请失败");
		}
		if (StringUtils.isEmpty(inDormitory.getStaffBadge())) {
			return new Result<>(Boolean.FALSE, "员工号获取失败");
		}
		//SmtOutDormitoryStaff one = smtOutDormitoryStaffService.getOne(Wrappers.<SmtOutDormitoryStaff> query().lambda().eq(SmtOutDormitoryStaff::getStaffBadge, inDormitory.getStaffBadge()));
		SmtStaff selectOne = staffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, inDormitory.getStaffBadge()));

		List<SmtOutDormitoryStaff> smtOutDormitoryStaff = mapper.getOutDormitoryStaff(inDormitory.getStaffBadge(), "外宿补贴");

		if (Objects.isNull(selectOne)) {
			return new Result<>(Boolean.FALSE, "未找到员工信息");
		}

		if (selectOne.getStatus().equals(StaffStatusEnum.STAFF_STATUS_QUIT.getCode())) {
			return new Result<>(Boolean.FALSE, "所属员工号的人员已离职，申请失败");
		}

		if (Objects.nonNull(selectOne.getDormitoryStatus()) && selectOne.getDormitoryStatus().equals(DormitoryStatusEnum.NOT_OUTER.getCode())) {
			return new Result<>(Boolean.FALSE, "您已申请外宿补贴，请在嘉阳系统取消补贴再申请内宿");
		}

		if (smtOutDormitoryStaff.size() > 0 && smtOutDormitoryStaff.get(0).getIsDelete().equals(0)) {
			return new Result<>(Boolean.FALSE, "您申请的外宿正在审批中，暂不能进行申请内宿");
		}

		Result<OvwYsCallOwanceDetailsDTO> callOwanceDetails = ovwYsCallOwanceDetailsService.getInfo(inDormitory.getStaffBadge(), 11);
		if (ObjectUtil.isNotNull(callOwanceDetails) && ObjectUtil.isNotNull(callOwanceDetails.getData())) {
			return new Result<>(Boolean.FALSE, "您申请的外宿补贴还未取消，请在嘉阳系统取消后再申请内宿");
		}

//		if(smtOutDormitoryStaff.size()>0 && smtOutDormitoryStaff.get(0).getIsDelete().equals(1))
//		{
//
//			//List<SmtCallowanceCancelRecord> list = callowanceCancelRecordService.list(Wrappers.<SmtCallowanceCancelRecord> query().lambda().eq(SmtCallowanceCancelRecord::getBadge, inDormitory.getStaffBadge()).orderByDesc(SmtCallowanceCancelRecord::getCreateTime));
//			List<SmtCallowanceCancelRecord> list =this.baseMapper.selectCallowanceCancelRecord(inDormitory.getStaffBadge());
//			if(list.size()>0)
//			{
//				List<SmtProcessRecord> selectList = smtProcessRecordService.list(Wrappers.<SmtProcessRecord> query().lambda().eq(SmtProcessRecord::getProcessId, list.get(0).getProcessId()).orderByDesc(SmtProcessRecord::getRecordDate));
//				if(selectList.size()>0) {
//					//查询流程的最新的状态数据
//					if(selectList.get(0).getStatementStatus().equals(ApplicationEnum.RECORD_STATUS_e.getCode()) || selectList.get(0).getStatementStatus().equals(ApplicationEnum.RECORD_STATUS_0.getCode())) {
//						Date creatTime = selectList.get(0).getCreatTime(); //审批时间
//						//判断当前时间是否再审批时间之后一个月得时间，否则提示不能申请内宿
//						Date tody=DateUtil.date();
//						long day=(tody.getTime()- creatTime.getTime())/(24*60*60*1000);
//						DateTime offsetDay = DateUtil.offsetDay(creatTime, 30);
//						if(day<30)
//						{
//							SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//							String format = DateUtils.format(offsetDay,formatter);
//							throw new TCEException("外宿补贴取消申请审批通过，"+format+"才能申请");
//						}
//
//					}
//				}
//
//			}
//
//		}
//

		Integer selectCount = this.count(Wrappers.<SmtDormitoryStaff>query().lambda()
				.eq(SmtDormitoryStaff::getStaffBadge, selectOne.getBadge())
				.eq(SmtDormitoryStaff::getParkId, inDormitory.getParkId()));
		if (selectCount > 0) {
			return new Result<>(Boolean.FALSE, "所属员工号的人员已分配宿舍，无需再次申请");
		}
		List<SmtDormitoryLevel> selectLevels = levelService.list(Wrappers.<SmtDormitoryLevel>query().lambda()
				.eq(SmtDormitoryLevel::getJcheId, selectOne.getJcheId()));
		if (Objects.isNull(selectLevels)) {
			return new Result<>(Boolean.FALSE, "所属职层没有宿舍");
		}
		List<SmtDormitoryBed> emptyBedList = new ArrayList<>();
		for (SmtDormitoryLevel selectLevel : selectLevels) {
			inDormitory.setSex(selectOne.getSex());
			inDormitory.setRoomType(selectLevel.getDormitoryTypeId());
			//查询空余床位
			emptyBedList = mapper.getEmptyBed(inDormitory);
			if (emptyBedList.size() > 0) {
				break;
			}
		}

		if (emptyBedList.size() == 0) {
			return new Result<>(Boolean.FALSE, "该职层的宿舍已没有空余床位");
		}

		Random random = new Random();
		int n = random.nextInt(emptyBedList.size());
		//获取到随机床位
		SmtDormitoryBed smtDormitoryBed = emptyBedList.get(n);
		//根据床位查询出关联的信息
		SmtDormitoryStaff dormitoryStaff = mapper.getBedInfo(smtDormitoryBed.getId());
		dormitoryStaff.setStaffId(selectOne.getId());
		dormitoryStaff.setStaffName(selectOne.getName());
		dormitoryStaff.setStaffBadge(selectOne.getBadge());
		dormitoryStaff.setCreateTime(DateUtil.date());
		dormitoryStaff.insert();
		//将入住记录放入历史记录表
		addDormitoryHistory(dormitoryStaff, DormitoryHisotryTypeEnum.IN_DORMITORY.getCode(), null);
		InDormitoryVO vo = new InDormitoryVO();
		vo.setBuildingName(dormitoryStaff.getDormitoryName());
		vo.setRoom(dormitoryStaff.getRoomName().toString());
		vo.setFloor(dormitoryStaff.getFloorName().toString());
		if (dormitoryStaff.getBedNumber() % 2 == 0) {
			vo.setBedName(dormitoryStaff.getBedNumber() + "号床");
		} else if (dormitoryStaff.getBedNumber() % 2 == 1) {
			vo.setBedName(dormitoryStaff.getBedNumber() + "号床");
		}
		selectOne.setDormitoryStatus(1); //1-内宿
		staffService.updateById(selectOne);
		return new Result<>(vo);
	}

	@Override
	public IPage<DormitoryStaffRespDTO> queryTodayIn(Page page) {
		IPage dormitoryStaffs = this.page(page, new LambdaQueryWrapper<SmtDormitoryStaff>()
				.between(SmtDormitoryStaff::getCreateTime, ToolUtils.getTodayStartTime(), ToolUtils.getTodayEndTime())
				.orderByAsc(SmtDormitoryStaff::getId)
		);
		List<DormitoryStaffRespDTO> dormitoryRespDTOList = new ArrayList<>();
		List records = dormitoryStaffs.getRecords();
		for (Object obj : records) {
			SmtDormitoryStaff smtDormitoryStaff = (SmtDormitoryStaff) obj;
			var dto = new DormitoryStaffRespDTO();
			BeanUtils.copyProperties(smtDormitoryStaff, dto);
			dto.setRoomId(smtDormitoryStaff.getRoomId());
			dto.setSex(smtDormitoryStaff.getStaffSex());
			dto.setBedNum(smtDormitoryStaff.getBedNumber());
			dormitoryRespDTOList.add(dto);
		}
		dormitoryStaffs.setRecords(dormitoryRespDTOList);

		return dormitoryStaffs;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public Boolean changeBed(String staffBadge, Integer bedId, Integer oldBedId) {

		//查询床位是否入住
		int count = this.count(new LambdaQueryWrapper<SmtDormitoryStaff>().eq(SmtDormitoryStaff::getBedId, bedId));
		if (count > 0) {
			//当前床位已入住
			log.error("床位{}当前已入住", bedId);
			throw new TCEException("当前床位已入住");
		}
		//查询该员工的入住记录
		List<SmtDormitoryStaff> smtDormitoryStaffs = this.list(new LambdaQueryWrapper<SmtDormitoryStaff>()
				.eq(SmtDormitoryStaff::getStaffBadge, staffBadge)
				.eq(SmtDormitoryStaff::getBedId, oldBedId)
		);
		if (CollectionUtil.isEmpty(smtDormitoryStaffs)) {
			//没有入住记录 不能换宿
			log.error("员工{}没有入住记录", staffBadge);
			throw new TCEException("该员工没有入住记录");
		}
		SmtDormitoryStaff smtDormitoryStaff = smtDormitoryStaffs.get(0);
		if (smtDormitoryStaff.getBedId().equals(bedId)) {
			//当前就是该床位
			return true;
		}
		Integer oldRoom = smtDormitoryStaff.getRoomId();
		DormitoryStatisFlagnum statisFlagnum = DormitoryStatisFlagnum.STATIS;
		//修改入住记录
		SmtDormitoryStaff tempDormitory = mapper.getBedInfo(bedId);
		tempDormitory.setId(smtDormitoryStaff.getId());
		tempDormitory.setStaffId(smtDormitoryStaff.getStaffId());
		tempDormitory.setStaffBadge(smtDormitoryStaff.getStaffBadge());
		tempDormitory.setStaffName(smtDormitoryStaff.getStaffName());
		tempDormitory.setStaffSex(smtDormitoryStaff.getStaffSex());
		tempDormitory.setCompName(smtDormitoryStaff.getCompName());
		tempDormitory.setDepName(smtDormitoryStaff.getDepName());
		tempDormitory.setJobName(smtDormitoryStaff.getJobName());
		tempDormitory.setIsStaff(smtDormitoryStaff.getIsStaff());
		tempDormitory.setOptUser(SecurityUtils.getUser().getUsername());
		if (oldRoom.equals(tempDormitory.getRoomId())) {
			//同一个房间 入住日期不变
			tempDormitory.setCreateTime(smtDormitoryStaff.getCreateTime());
			//同房间换宿 产生的换宿记录不参与水电统计 因为新的入住记录的开始入住时间是旧入住记录的入住时间
			statisFlagnum = DormitoryStatisFlagnum.NO_STATIS;
		} else {
			tempDormitory.setCreateTime(new Date());
		}

		//删除原来的入住记录
		this.removeById(smtDormitoryStaff.getId());
		//生成新的入住记录
		this.save(tempDormitory);
		//备注转移
		smtDormitoryOutRemarkService.transferRemark(smtDormitoryStaff.getId(), tempDormitory.getId());
		//生成一条换宿历史记录
		addDormitoryHistory(smtDormitoryStaff, DormitoryHisotryTypeEnum.CHANGE_DORMITORY.getCode(), null, statisFlagnum);
		//生成一条新的入住历史记录
		addDormitoryHistory(tempDormitory, DormitoryHisotryTypeEnum.IN_DORMITORY.getCode(), oldRoom);
		return true;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public Boolean addDormitoryStaff(String staffBadge, Integer bedId) {
		if (StringUtils.isEmpty(staffBadge)) {
			throw new TCEException("员工工号不存在");
		}
		SmtStaff staff = staffService.getOne(new LambdaQueryWrapper<SmtStaff>().eq(SmtStaff::getBadge, staffBadge));
		if (null == staff) {
			throw new TCEException("员工数据不存在");
		}
		if (StringUtils.isEmpty(bedId)) {
			throw new TCEException("床位ID不存在");
		}

		SmtDormitoryStaff dormitoryStaff = mapper.getBedInfo(bedId);
		dormitoryStaff.setStaffId(staff.getId());
		dormitoryStaff.setStaffName(staff.getName());
		dormitoryStaff.setStaffBadge(staff.getBadge());
		dormitoryStaff.setCompName(staff.getCompName());
		dormitoryStaff.setDepName(staff.getDepName());
		dormitoryStaff.setJobName(staff.getJobName());
		dormitoryStaff.setCreateTime(DateUtil.date());
		dormitoryStaff.setStaffSex(staff.getSex());
		//同时更新员工表的住宿状态
		staff.setDormitoryStatus(DormitoryStatusEnum.NOT_INNER.getCode()); //1-内宿
		dormitoryStaff.setIsStaff(staff.getStatus());
		staffService.updateById(staff);
		dormitoryStaff.insert();

		//查询该床位的当前入住情况
		List<SmtDormitoryStaff> dormitoryStaffs = this.list(new LambdaQueryWrapper<SmtDormitoryStaff>().eq(SmtDormitoryStaff::getBedId, bedId));
		if (dormitoryStaffs.size() > 1) {
			//床位重复入住
			throw new TCEException("床位已被占用");
		}

		//添加历史记录
		addDormitoryHistory(dormitoryStaff, DormitoryHisotryTypeEnum.IN_DORMITORY.getCode(), null);
		return true;
	}

	private void addDormitoryHistory(SmtDormitoryStaff dormitoryStaff, Integer type, Integer oldRoom) {
		addDormitoryHistory(dormitoryStaff, type, oldRoom, DormitoryStatisFlagnum.STATIS);
	}

	private void addDormitoryHistory(SmtDormitoryStaff dormitoryStaff, Integer type, Integer oldRoom, DormitoryStatisFlagnum dormitoryStatisFlagnum) {
		Date cTime = DateUtils.date();
		SmtDormitoryStaffHistory history = new SmtDormitoryStaffHistory();
		history.setBedId(dormitoryStaff.getBedId());
		history.setBedNumber(dormitoryStaff.getBedNumber());
		history.setTime(cTime);
		history.setCreateTime(cTime);
		history.setDormitoryId(dormitoryStaff.getDormitoryId());
		history.setDormitoryName(dormitoryStaff.getDormitoryName());
		history.setDormitoryTypeId(dormitoryStaff.getDormitoryTypeId());
		history.setDormitoryTypeName(dormitoryStaff.getDormitoryTypeName());
		history.setFloorId(dormitoryStaff.getFloorId());
		history.setFloorName(dormitoryStaff.getFloorName());
		history.setInTime(dormitoryStaff.getCreateTime());
		history.setParkId(dormitoryStaff.getParkId());
		history.setParkName(dormitoryStaff.getParkName());
		history.setRoomId(dormitoryStaff.getRoomId());
		history.setRoomName(dormitoryStaff.getRoomName());
		history.setStaffBadge(dormitoryStaff.getStaffBadge());
		history.setStaffSex(dormitoryStaff.getStaffSex());
		history.setStaffId(dormitoryStaff.getStaffId());
		history.setStaffName(dormitoryStaff.getStaffName());
		history.setType(type);
		history.setIsStaff(dormitoryStaff.getIsStaff());
		history.setDfId(dormitoryStaff.getId());
		history.setIsStaff(dormitoryStaff.getIsStaff());
		history.setCompName(dormitoryStaff.getCompName());
		history.setDepName(dormitoryStaff.getDepName());
		history.setJobName(dormitoryStaff.getJobName());
		if (DormitoryHisotryTypeEnum.CHANGE_DORMITORY.getCode().equals(type) && DormitoryStatisFlagnum.STATIS == dormitoryStatisFlagnum) {
			//换宿情况下 计算本次住宿天数是否大于3天 不大于3天则不参与水电计算
			int inDays = (int) ((cTime.getTime() - dormitoryStaff.getCreateTime().getTime()) / (1000 * 3600 * 24));
			if (inDays <= 3) {
				dormitoryStatisFlagnum = DormitoryStatisFlagnum.NO_STATIS;
			}
		}
		history.setStatisFlag(dormitoryStatisFlagnum.getCode());
		SmartUser user = SecurityUtils.getUser();
		if(Objects.nonNull(user)) {
			history.setOptUser(user.getUsername());
		}
		if(DormitoryHisotryTypeEnum.IN_DORMITORY.getCode().equals(type)){
			//如果是入住
			history.setInCreateTime(cTime);
			history.setInOptUser(dormitoryStaff.getOptUser());
		} else {
			// 非入住 取对应的入住记录历史记录
			List<SmtDormitoryStaffHistory> dormitoryStaffHistorys = smtDormitoryStaffHistoryMapper.selectList(new LambdaQueryWrapper<SmtDormitoryStaffHistory>()
					.eq(SmtDormitoryStaffHistory::getDfId, dormitoryStaff.getId())
					.eq(SmtDormitoryStaffHistory::getType, DormitoryHisotryTypeEnum.IN_DORMITORY.getCode())
			);
			if(CollectionUtil.isNotEmpty(dormitoryStaffHistorys)){
				history.setInCreateTime(dormitoryStaffHistorys.get(0).getCreateTime());
				history.setInOptUser(dormitoryStaffHistorys.get(0).getOptUser());
			}
		}
		history.insert();
		if (!DormitoryHisotryTypeEnum.IN_DORMITORY.getCode().equals(type)) {
			smtDormitoryOutRemarkService.updateDorStaffId(dormitoryStaff.getId(), history.getId());
		}
		try {
			if (DormitoryHisotryTypeEnum.IN_DORMITORY.getCode().equals(type) && Objects.nonNull(oldRoom)) {
				connectLockService.sendLockData(dormitoryStaff, DormitoryHisotryTypeEnum.CHANGE_DORMITORY.getCode(), oldRoom);
				return;
			}
			if(DormitoryHisotryTypeEnum.CHANGE_DORMITORY.getCode().equals(type) && Objects.isNull(oldRoom)) {
				return;
			}
			connectLockService.sendLockData(dormitoryStaff, type, oldRoom);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * 根据员工id，床位id,添加入住信息
	 */
	@Override
	public Boolean addDormitoryStaff(DormitoryStaffDTO smtDormitoryStaff) {
		return addDormitoryStaff(smtDormitoryStaff,StaffStatusEnum.STAFF_STATUS_IN.getCode());
	}

	@Transactional
    @Override
    public Boolean addDormitoryStaff(DormitoryStaffDTO smtDormitoryStaff, Integer isStaff) {
		Long staffId = smtDormitoryStaff.getStaffId();
		if (StringUtils.isEmpty(staffId)) {
			throw new TCEException("请选择入住的员工");
		}
		Integer bedId = smtDormitoryStaff.getBedId();
		if (StringUtils.isEmpty(bedId)) {
			throw new TCEException("请选择入住的床位");
		}



		List<SmtDormitoryStaffHistory> histories = smtDormitoryStaffHistoryMapper.selectList(Wrappers.<SmtDormitoryStaffHistory>query().lambda()
				.eq(SmtDormitoryStaffHistory::getStaffBadge, smtDormitoryStaff.getBadge())
				.orderByDesc(SmtDormitoryStaffHistory::getCreateTime));
		if (CollUtil.isNotEmpty(histories)) {
			if (DateUtil.parse(DateUtils.convert(LocalDateTime.now())).isBefore(histories.get(0).getCreateTime())) {
				throw new TCEException("入住时间小于最近一次退宿时间");
			}
		}
		log.info("bedId:{},staffId:{}", bedId, staffId);
		SmtStaff selectById = staffService.getById(staffId);


		// 许昌园区判断是否已入住
		List<SmtDormitoryStaff> dormitoryStaffList = this.list(new LambdaQueryWrapper<SmtDormitoryStaff>()
				.eq(SmtDormitoryStaff::getStaffId, staffId)
				.eq(SmtDormitoryStaff::getParkId, xcParkId)
		);
		if(CollectionUtil.isNotEmpty(dormitoryStaffList)){
			// 员工已经在许昌园区入住
			SmtDormitoryStaff smtDormitoryStaff1 = dormitoryStaffList.get(0);
			throw new SmartException("已在"+smtDormitoryStaff1.getDormitoryName()+"-"+smtDormitoryStaff1.getRoomName()+"入住，无法重复入住");
		}

		SmtDormitoryStaff dormitoryStaff = mapper.getBedInfo(bedId);
		dormitoryStaff.setStaffId(selectById.getId());
		dormitoryStaff.setStaffName(selectById.getName());
		dormitoryStaff.setStaffBadge(selectById.getBadge());
		dormitoryStaff.setCompName(selectById.getCompName());
		dormitoryStaff.setDepName(selectById.getDepName());
		dormitoryStaff.setJobName(selectById.getJobName());
		if (Objects.nonNull(smtDormitoryStaff.getCreateTime())) {
			dormitoryStaff.setCreateTime(DateUtil.parse(smtDormitoryStaff.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		} else {
			dormitoryStaff.setCreateTime(DateUtil.date());
		}
		dormitoryStaff.setStaffSex(selectById.getSex());
		//同时更新员工表的住宿状态
		selectById.setDormitoryStatus(DormitoryStatusEnum.NOT_INNER.getCode()); //1-内宿
		dormitoryStaff.setIsStaff(isStaff);
		dormitoryStaff.setOptUser(SecurityUtils.getUser().getUsername());
		dormitoryStaff.setSimpleRemark(smtDormitoryStaff.getSimpleRemark());
		//更新员工入住状态
		staffService.updateById(selectById);
		//添加入住记录
		dormitoryStaff.insert();

		//查询该床位的当前入住情况
		List<SmtDormitoryStaff> dormitoryStaffs = this.list(new LambdaQueryWrapper<SmtDormitoryStaff>().eq(SmtDormitoryStaff::getBedId, bedId));
		if (dormitoryStaffs.size() > 1) {
			//床位重复入住
			throw new TCEException("床位已被占用");
		}
		//添加历史记录
		addDormitoryHistory(dormitoryStaff, DormitoryHisotryTypeEnum.IN_DORMITORY.getCode(), null);
		return true;
    }

    @Transactional
	@Override
	public Boolean addDormitoryStaffTemp(DormitoryStaffDTO smtDormitoryStaff) {
		//添加入住记录
		SmtDormitoryStaff dormitoryStaff = mapper.getBedInfo(smtDormitoryStaff.getBedId());
		dormitoryStaff.setCreateTime(DateUtil.date());
		dormitoryStaff.setStaffSex(smtDormitoryStaff.getSex());
		dormitoryStaff.setIsStaff(StaffStatusEnum.UNKNOWN.getCode());            //未知人员
		dormitoryStaff.setStaffId(smtDormitoryStaff.getStaffId());
		dormitoryStaff.setStaffBadge(smtDormitoryStaff.getBadge());
		dormitoryStaff.setStaffName(smtDormitoryStaff.getName());
		this.save(dormitoryStaff);
		//查询该床位的当前入住情况
		List<SmtDormitoryStaff> dormitoryStaffs = this.list(new LambdaQueryWrapper<SmtDormitoryStaff>().eq(SmtDormitoryStaff::getBedId, smtDormitoryStaff.getBedId()));
		if (dormitoryStaffs.size() > 1) {
			//床位重复入住
			throw new TCEException("床位已被占用");
		}
		//添加历史入住记录
		addDormitoryHistory(dormitoryStaff, DormitoryHisotryTypeEnum.IN_DORMITORY.getCode(), null);
		return true;
	}

	@Transactional
	@Override
	public Boolean updateDormitoryStaffTemp(SmtStaff smtStaff) {
		//查询是否存在临时入住的数据
		List<SmtDormitoryStaff> smtDormitoryStaffs = this.list(new LambdaQueryWrapper<SmtDormitoryStaff>()
				.eq(SmtDormitoryStaff::getStaffBadge, smtStaff.getCertno())
				.eq(SmtDormitoryStaff::getIsStaff, StaffStatusEnum.UNKNOWN.getCode())
		);
		if (CollectionUtil.isEmpty(smtDormitoryStaffs)) {
			return true;
		}
		//修改入住信息和入住历史信息
		SmtDormitoryStaff smtDormitoryStaff = smtDormitoryStaffs.get(0);
		smtDormitoryStaff.setCompName(smtStaff.getCompName());
		smtDormitoryStaff.setDepName(smtStaff.getDepName());
		smtDormitoryStaff.setJobName(smtStaff.getJobName());
		smtDormitoryStaff.setStaffBadge(smtStaff.getBadge());
		smtDormitoryStaff.setStaffId(smtStaff.getId());
		smtDormitoryStaff.setIsStaff(smtStaff.getStatus());

		//注意这里 历史入住记录可能有多条的情况 比如临时人员入住又马上发起换宿操作
		List<SmtDormitoryStaffHistory> smtDormitoryStaffHistory = hisService.list(new LambdaQueryWrapper<SmtDormitoryStaffHistory>().eq(SmtDormitoryStaffHistory::getDfId, smtDormitoryStaff.getId()));
		for (SmtDormitoryStaffHistory history : smtDormitoryStaffHistory) {
			history.setCompName(smtStaff.getCompName());
			history.setDepName(smtStaff.getDepName());
			history.setJobName(smtStaff.getJobName());
			history.setStaffBadge(smtStaff.getBadge());
			history.setStaffId(smtStaff.getId());
			history.setIsStaff(smtStaff.getStatus());
			hisService.updateById(history);
		}
		this.updateById(smtDormitoryStaff);
		return true;
	}

	@Override
	public Result removeBedById(Integer id) {
		// TODO Auto-generated method stub
		SmtDormitoryStaff selectById = this.getById(id);
		//退宿后修改员工住宿状态
		Long staffId = selectById.getStaffId();
		SmtStaff selectById2 = staffService.getById(staffId);
		selectById2.setDormitoryStatus(DormitoryStatusEnum.IS_INIT.getCode()); //未住宿状态
		boolean removeById = this.removeById(id);
		if (removeById) {
			//修改员工状态
			staffService.updateById(selectById2);
			//将退宿记录插入历史表
			addDormitoryHistory(selectById, DormitoryHisotryTypeEnum.OUT_DORMITORY.getCode(), null);
		}
		return new Result<>(true);
	}

	@Override
	public Result updateById(UpdateDormitoryStaffDTO smtDormitoryStaff) {
		// TODO Auto-generated method stub
		String createTime = smtDormitoryStaff.getCreateTime();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date parse = null;
		try {
			parse = formatter.parse(createTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SmtDormitoryStaff st = new SmtDormitoryStaff();
		st.setId(smtDormitoryStaff.getId());
		st.setCreateTime(parse);
		boolean updateById = st.updateById();
		if (updateById) {
			//修改历史表中的入住时间
			List<SmtDormitoryStaffHistory> historyList = hisService.list(new LambdaQueryWrapper<SmtDormitoryStaffHistory>()
					.eq(SmtDormitoryStaffHistory::getDfId, smtDormitoryStaff.getId()));
			for (SmtDormitoryStaffHistory history : historyList) {
				history.setInTime(parse);
			}
			hisService.updateBatchById(historyList);
		}
		return new Result<>(true);
	}

	@Transactional
	@Override
	public Result changeDormitory(UpdateDormitoryStaffDTO smtDormitoryStaff) {
		SmtDormitoryStaff dormitoryStaff = this.getById(smtDormitoryStaff.getId());
		if (null == dormitoryStaff) {
			//入住记录不存在
			log.error("入住记录{}不存在", smtDormitoryStaff.getId());
			throw new TCEException("入住记录不存在");
		}
		//删除入住记录
		boolean removeById = this.removeById(smtDormitoryStaff.getId());
		//退宿后修改员工住宿状态
		if (removeById) {
			SmtStaff smtStaff = null;
			if (null != dormitoryStaff.getStaffId()) {
				smtStaff = staffService.getById(dormitoryStaff.getStaffId());
			} else {
				smtStaff = staffService.getOne(new LambdaQueryWrapper<SmtStaff>().eq(SmtStaff::getBadge, dormitoryStaff.getStaffBadge()));
			}
			if (null != smtStaff) {
				smtStaff.setDormitoryStatus(DormitoryStatusEnum.IS_INIT.getCode()); //未住宿状态
				//修改员工状态
				staffService.updateById(smtStaff);
			}
			dormitoryStaff.setOptUser(SecurityUtils.getUser().getUsername());

			//将退宿记录插入历史表
			if (smtDormitoryStaff.getType().equals(DormitoryHisotryTypeEnum.CHANGE_DORMITORY.getCode())) {
				//换宿
				addDormitoryHistory(dormitoryStaff, smtDormitoryStaff.getType(), dormitoryStaff.getRoomId());
			} else {
				//退宿
				addDormitoryHistory(dormitoryStaff, smtDormitoryStaff.getType(), null);
			}

		}
		return new Result<>(true);
	}

	@Transactional
	@Override
	public Boolean checkOutDormitory(Integer inId, Integer type) {
		SmtDormitoryStaff dormitoryStaff = this.getById(inId);
		if (null == dormitoryStaff) {
			//入住记录不存在
			throw new TCEException("入住记录不存在");
		}
		//删除入住记录
		boolean removeById = this.removeById(inId);
		//退宿后修改员工住宿状态
		if (removeById) {
			SmtStaff smtStaff = staffService.getStaffByBadgeAll(dormitoryStaff.getStaffBadge());
			if (Objects.nonNull(smtStaff)) {
				smtStaff.setDormitoryStatus(DormitoryStatusEnum.IS_INIT.getCode()); //未住宿状态
				//修改员工状态
				staffService.updateById(smtStaff);
			}
			//将退宿记录插入历史表
			if (Objects.isNull(type)) {
				type = DormitoryHisotryTypeEnum.CHECK_OUT_DORMITORY.getCode();
			}
			addDormitoryHistory(dormitoryStaff, type, null);
		}
		return true;
	}

	@Override
	public DormitoryRoomDetailRespDTO getStaffRoomInfo(String staffBadge) {
		SmtDormitoryStaff smtDormitoryStaff = this.getOne(new LambdaQueryWrapper<SmtDormitoryStaff>().eq(SmtDormitoryStaff::getStaffBadge, staffBadge));
		if (Objects.nonNull(smtDormitoryStaff)) {
			SmtDormitoryRoom smtDormitoryRoom = roomMapper.selectById(smtDormitoryStaff.getRoomId());
			SmtDormitoryFloor smtDormitoryFloor = floorMapper.selectById(smtDormitoryStaff.getFloorId());
			return DormitoryRoomDetailRespDTO.builder()
					.parkName(smtDormitoryStaff.getParkName())
					.dormitoryName(smtDormitoryStaff.getDormitoryName())
					.staffBadge(smtDormitoryStaff.getStaffBadge())
					.parkId(smtDormitoryStaff.getParkId())
					.floorName(StringUtils.isEmpty(smtDormitoryFloor.getAliasName()) ? smtDormitoryFloor.getFloorName().toString() : smtDormitoryFloor.getAliasName())
					.roomName(StringUtils.isEmpty(smtDormitoryRoom.getAliasName()) ? smtDormitoryRoom.getRoomName().toString() : smtDormitoryRoom.getAliasName())
					.dormitoryId(smtDormitoryStaff.getDormitoryId())
					.floorId(smtDormitoryStaff.getFloorId())
					.id(smtDormitoryStaff.getBedId())
					.bedNumber(smtDormitoryStaff.getBedNumber().toString())
					.roomId(smtDormitoryStaff.getRoomId())
					.build();
		}
		return null;
	}

	@Override
	public List<DormitoryRoomDetailRespDTO> getSimpleStaffRoomList(String staffBadge) {
		List<SmtDormitoryStaff> smtDormitoryStaffs = this.list(new LambdaQueryWrapper<SmtDormitoryStaff>().eq(SmtDormitoryStaff::getStaffBadge, staffBadge));
		if (CollUtil.isNotEmpty(smtDormitoryStaffs)) {
			List<Integer> roomIdList = smtDormitoryStaffs.stream().map(SmtDormitoryStaff::getRoomId).collect(Collectors.toList());
			List<Integer> floorIdList = smtDormitoryStaffs.stream().map(SmtDormitoryStaff::getFloorId).collect(Collectors.toList());
			List<SmtDormitoryRoom> smtDormitoryRooms = roomMapper.selectBatchIds(roomIdList);
			List<SmtDormitoryFloor> smtDormitoryFloors = floorMapper.selectBatchIds(floorIdList);
			Map<Integer, List<SmtDormitoryRoom>> roomCollect = smtDormitoryRooms.stream().collect(Collectors.groupingBy(SmtDormitoryRoom::getId));
			Map<Integer, List<SmtDormitoryFloor>> floorCollect = smtDormitoryFloors.stream().collect(Collectors.groupingBy(SmtDormitoryFloor::getId));
			List<DormitoryRoomDetailRespDTO> dormitoryRoomDetailRespDTOS = new ArrayList<>();
			smtDormitoryStaffs.forEach(smtDormitoryStaff -> {
				SmtDormitoryRoom room = roomCollect.get(smtDormitoryStaff.getRoomId()).get(0);
				SmtDormitoryFloor floor = floorCollect.get(smtDormitoryStaff.getFloorId()).get(0);
				dormitoryRoomDetailRespDTOS.add(DormitoryRoomDetailRespDTO.builder()
						.parkName(smtDormitoryStaff.getParkName())
						.dormitoryName(smtDormitoryStaff.getDormitoryName())
						.staffBadge(smtDormitoryStaff.getStaffBadge())
						.parkId(smtDormitoryStaff.getParkId())
						.floorName(StringUtils.isEmpty(floor.getAliasName()) ? floor.getFloorName().toString() : floor.getAliasName())
						.roomName(StringUtils.isEmpty(room.getAliasName()) ? room.getRoomName().toString() : room.getAliasName())
						.dormitoryId(smtDormitoryStaff.getDormitoryId())
						.floorId(smtDormitoryStaff.getFloorId())
						.id(smtDormitoryStaff.getBedId())
						.bedNumber(smtDormitoryStaff.getBedNumber().toString())
						.roomId(smtDormitoryStaff.getRoomId())
						.build());
			});
			return dormitoryRoomDetailRespDTOS;
		}
		return null;
	}

	@Override
	public List<DormitoryRoomDetailRespDTO> getStaffRoomInfoList(String staffBadge) {
		List<SmtDormitoryStaff> smtDormitoryStaffs = this.list(new LambdaQueryWrapper<SmtDormitoryStaff>().eq(SmtDormitoryStaff::getStaffBadge, staffBadge));
		if (CollUtil.isNotEmpty(smtDormitoryStaffs)) {
			List<Integer> roomIdList = smtDormitoryStaffs.stream().map(SmtDormitoryStaff::getRoomId).collect(Collectors.toList());
			List<Integer> floorIdList = smtDormitoryStaffs.stream().map(SmtDormitoryStaff::getFloorId).collect(Collectors.toList());
			List<SmtDormitoryRoom> smtDormitoryRooms = roomMapper.selectBatchIds(roomIdList);
			List<SmtDormitoryFloor> smtDormitoryFloors = floorMapper.selectBatchIds(floorIdList);
			Map<Integer, List<SmtDormitoryRoom>> roomCollect = smtDormitoryRooms.stream().collect(Collectors.groupingBy(SmtDormitoryRoom::getId));
			Map<Integer, List<SmtDormitoryFloor>> floorCollect = smtDormitoryFloors.stream().collect(Collectors.groupingBy(SmtDormitoryFloor::getId));
			List<DormitoryRoomDetailRespDTO> dormitoryRoomDetailRespDTOS = new ArrayList<>();
			smtDormitoryStaffs.forEach(smtDormitoryStaff -> {
				SmtDormitoryRoom room = roomCollect.get(smtDormitoryStaff.getRoomId()).get(0);
				SmtDormitoryFloor floor = floorCollect.get(smtDormitoryStaff.getFloorId()).get(0);
                String sb = smtDormitoryStaff.getParkName() + SymbolConstants.MINUS +
                        smtDormitoryStaff.getDormitoryName() + SymbolConstants.MINUS +
                        smtDormitoryStaff.getRoomName();
				dormitoryRoomDetailRespDTOS.add(DormitoryRoomDetailRespDTO.builder()
						.parkName(smtDormitoryStaff.getParkName())
						.dormitoryName(smtDormitoryStaff.getDormitoryName())
						.staffBadge(smtDormitoryStaff.getStaffBadge())
						.parkId(smtDormitoryStaff.getParkId())
						.floorName(StringUtils.isEmpty(floor.getAliasName()) ? floor.getFloorName().toString() : floor.getAliasName())
						.roomName(StringUtils.isEmpty(room.getAliasName()) ? room.getRoomName().toString() : room.getAliasName())
						.dormitoryId(smtDormitoryStaff.getDormitoryId())
						.floorId(smtDormitoryStaff.getFloorId())
						.id(smtDormitoryStaff.getBedId())
						.bedNumber(smtDormitoryStaff.getBedNumber().toString())
						.roomId(smtDormitoryStaff.getRoomId())
						.detailStr(sb)
						.lockPwd(this.getLockStatus(smtDormitoryStaff.getRoomId(), smtDormitoryStaff.getStaffBadge()))
						.build());
			});
			return dormitoryRoomDetailRespDTOS;
		}
		return new ArrayList<>();
	}

	@Override
	public DormitoryRoomDetailRespDTO getStaffRoomInfoByPhone(String phone, String name) {
		StaffInfoVO vo = staffService.getSmtStaffInfoByPhone(phone, name);
		if (Objects.nonNull(vo.getSmtStaff())) {
			List<SmtDormitoryStaff> smtDormitoryStaffs = this.list(new LambdaQueryWrapper<SmtDormitoryStaff>().eq(SmtDormitoryStaff::getStaffBadge, vo.getSmtStaff().getBadge())
					.orderByDesc(SmtDormitoryStaff::getCreateTime));
			if (CollUtil.isNotEmpty(smtDormitoryStaffs)) {
				SmtDormitoryStaff smtDormitoryStaff = smtDormitoryStaffs.get(0);
				SmtDormitoryRoom smtDormitoryRoom = roomMapper.selectById(smtDormitoryStaff.getRoomId());
				SmtDormitoryFloor smtDormitoryFloor = floorMapper.selectById(smtDormitoryStaff.getFloorId());
				return DormitoryRoomDetailRespDTO.builder()
						.parkName(smtDormitoryStaff.getParkName())
						.dormitoryName(smtDormitoryStaff.getDormitoryName())
						.staffBadge(smtDormitoryStaff.getStaffBadge())
						.floorName(StringUtils.isEmpty(smtDormitoryFloor.getAliasName()) ? smtDormitoryFloor.getFloorName().toString() : smtDormitoryFloor.getAliasName())
						.roomName(StringUtils.isEmpty(smtDormitoryRoom.getAliasName()) ? smtDormitoryRoom.getRoomName().toString() : smtDormitoryRoom.getAliasName())
						.dormitoryId(smtDormitoryStaff.getDormitoryId())
						.floorId(smtDormitoryStaff.getFloorId())
						.id(smtDormitoryStaff.getBedId())
						.bedNumber(smtDormitoryStaff.getBedNumber().toString())
						.roomId(smtDormitoryStaff.getRoomId())
						.build();
			}
		}
		return null;
	}

	@Transactional
	@Override
	public Result addDormitory(DormitoryStaffDTO dormitoryStaffDTO) {
		// TODO Auto-generated method stub
		Integer bedId = dormitoryStaffDTO.getBedId();
		if (StringUtils.isEmpty(bedId)) {
			return new Result<>(Boolean.FALSE, "该选择入住的床位");
		}
		String badge = dormitoryStaffDTO.getBadge().replace(" ", "");
		if (StringUtils.isEmpty(badge)) {
			return new Result<>(Boolean.FALSE, "工号不能为空");
		} else if (!RegexUtils.matchBadge(badge)) {
			return new Result<>(Boolean.FALSE, "工号仅允许字母和数字");
		}
		if (StringUtils.isEmpty(dormitoryStaffDTO.getName())) {
			return new Result<>(Boolean.FALSE, "姓名不能为空");
		}
		SmtDormitoryStaff dormitoryStaff = mapper.getBedInfo(bedId);
		if (xcParkId.equals(dormitoryStaff.getParkId())) {
			int count = smtDormitoryStaffHistoryMapper.selectCount(Wrappers.<SmtDormitoryStaffHistory>lambdaQuery()
					.eq(SmtDormitoryStaffHistory::getStaffBadge, dormitoryStaffDTO.getBadge())
					.eq(SmtDormitoryStaffHistory::getParkId, xcParkId)
			);
			if (count > 0) {
				return new Result<>(Boolean.FALSE, "工号已存在");
			}
		}
		dormitoryStaff.setStaffName(dormitoryStaffDTO.getName().replace(" ", ""));
		dormitoryStaff.setStaffBadge(badge);
		dormitoryStaff.setIsStaff(0);
		dormitoryStaff.setCompName(dormitoryStaffDTO.getCompName());
		dormitoryStaff.setDepName(dormitoryStaffDTO.getDepName());
		dormitoryStaff.setJobName(dormitoryStaffDTO.getJobName());
		dormitoryStaff.setOptUser(SecurityUtils.getUser().getUsername());
		if (Objects.nonNull(dormitoryStaffDTO.getCreateTime())) {
			dormitoryStaff.setCreateTime(DateUtil.parse(dormitoryStaffDTO.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		} else {
			dormitoryStaff.setCreateTime(DateUtil.date());
		}
		dormitoryStaff.setSimpleRemark(dormitoryStaffDTO.getSimpleRemark());
		dormitoryStaff.setStaffSex(dormitoryStaffDTO.getSex());
		boolean insert = dormitoryStaff.insert();
		if (insert) {
			addDormitoryHistory(dormitoryStaff, DormitoryHisotryTypeEnum.IN_DORMITORY.getCode(), null);
		}
		return new Result<>(true);
	}

	@Override
	public List<DormitoryRoomExt> getRoomBedUse(List<Integer> roomIds) {
		return this.mapper.getRoomBedUse(roomIds);
	}


	@Override
	public List<DormitoryStaffReqDTO> batchAddDormitoryStaff(List<DormitoryStaffReqDTO> dormitoryStaffReqDTOList) {
		if (CollectionUtil.isEmpty(dormitoryStaffReqDTOList)) {
			throw new TCEException("员工入住列表不能为空");
		}
		for (DormitoryStaffReqDTO staffReqDTO : dormitoryStaffReqDTOList) {
			addDormitoryStaffOne(staffReqDTO);
		}
		return dormitoryStaffReqDTOList;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean addDormitoryStaffOne(DormitoryStaffReqDTO staffReqDTO) {
		log.info("staff_data: {}", staffReqDTO);
		if (!checkParam(staffReqDTO)) {
			return false;
		}
		// 查询员工信息
		SmtStaff staff = staffService.getOne(Wrappers.<SmtStaff>query().lambda()
				.eq(SmtStaff::getBadge, staffReqDTO.getStaffBadge()), false);
		if (ObjectUtil.isNull(staff)) {
			staffReqDTO.setMark("员工信息不存在");
			return false;
		}
		// 查询该员工是否存在入住记录
		SmtDormitoryStaff dormitoryStaff = this.getOne(new LambdaQueryWrapper<SmtDormitoryStaff>()
				.eq(SmtDormitoryStaff::getStaffBadge, staff.getBadge()), false);
		if (ObjectUtil.isNotNull(dormitoryStaff)) {
			//已存在入住记录
			log.info("{}已存在入住记录", staffReqDTO.getStaffBadge());
			String mark = String.format("该员工已存在入住记录,宿舍楼：%s, 房号：%s, 床号：%s",
					dormitoryStaff.getDormitoryName(),
					dormitoryStaff.getRoomName(),
					dormitoryStaff.getBedNumber());
			staffReqDTO.setMark(mark);
			return false;
		}

		SmtPark park = parkService.getOne(new LambdaQueryWrapper<SmtPark>()
				.eq(SmtPark::getParkName, staffReqDTO.getParkName().trim()), false);
		if (ObjectUtil.isNull(park)) {
			staffReqDTO.setMark("园区信息不存在");
			return false;
		}

		List<SmtDormitory> dormitoryList = dormitoryMapper.selectList(new LambdaQueryWrapper<SmtDormitory>()
				.eq(SmtDormitory::getDormitoryName, staffReqDTO.getDormitoryName().trim())
				.eq(SmtDormitory::getParkId, park.getId()));
		if (CollectionUtil.isEmpty(dormitoryList)) {
			staffReqDTO.setMark("宿舍楼栋信息不存在");
			return false;
		}
		SmtDormitory dormitory = dormitoryList.get(0);

		List<SmtDormitoryFloor> floorList = floorMapper.selectList(new LambdaQueryWrapper<SmtDormitoryFloor>()
				.eq(SmtDormitoryFloor::getDormitoryId, dormitory.getId())
				.eq(SmtDormitoryFloor::getFloorName, staffReqDTO.getFloorName()));
		if (CollectionUtil.isEmpty(floorList)) {
			staffReqDTO.setMark("楼层信息不存在");
			return false;
		}
		SmtDormitoryFloor floor = floorList.get(0);

		List<SmtDormitoryRoom> roomList = roomMapper.selectList(new LambdaQueryWrapper<SmtDormitoryRoom>()
				.eq(SmtDormitoryRoom::getFloorId, floor.getId())
				.eq(SmtDormitoryRoom::getRoomName, staffReqDTO.getRoomName()));
		if (CollectionUtil.isEmpty(roomList)) {
			staffReqDTO.setMark("房间信息不存在");
			return false;
		}
		SmtDormitoryRoom room = roomList.get(0);
		if (!RoomSexEnum.desc(room.getRoomSex()).equals(staffReqDTO.getStaffSex())) {
			staffReqDTO.setMark("房间性别与员工性别不符");
			return false;
		}
		List<SmtDormitoryBed> bedList = bedMapper.selectList(new LambdaQueryWrapper<SmtDormitoryBed>()
				.eq(SmtDormitoryBed::getRoomId, room.getId())
				.eq(SmtDormitoryBed::getBedNumber, staffReqDTO.getBedNumber()));
		if (CollectionUtil.isEmpty(bedList)) {
			staffReqDTO.setMark("床位号不存在");
			return false;
		}
		SmtDormitoryBed bed = bedList.get(0);
		// 查询该床位是否已被占用
		SmtDormitoryStaff smtDormitoryStaff = this.getOne(new LambdaQueryWrapper<SmtDormitoryStaff>()
				.eq(SmtDormitoryStaff::getBedId, bed.getId()), false);
		if (ObjectUtil.isNotNull(smtDormitoryStaff)) {
			staffReqDTO.setMark("该床位已被占用");
			return false;
		}
		// 查询园区信息
		SmtPark smtPark = parkService.getOne(new LambdaQueryWrapper<SmtPark>()
				.eq(SmtPark::getId, dormitory.getParkId()), false);
		// 查询宿舍类型信息
		List<SmtDormitoryType> typeList = dormitoryTypeMapper.selectList(new LambdaQueryWrapper<SmtDormitoryType>()
				.eq(SmtDormitoryType::getTypeName, staffReqDTO.getRoomType()));
		if (CollectionUtil.isEmpty(typeList)) {
			staffReqDTO.setMark("房间类型不存在");
			return false;
		}
		SmtDormitoryType smtDormitoryType = typeList.get(0);
		// 构建宿舍员工入住对象
		smtDormitoryStaff = new SmtDormitoryStaff();
		smtDormitoryStaff.setStaffId(staff.getId());
		smtDormitoryStaff.setStaffName(staff.getName());
		smtDormitoryStaff.setStaffBadge(staff.getBadge());
		smtDormitoryStaff.setParkId(smtPark.getId());
		smtDormitoryStaff.setParkName(smtPark.getParkName());
		smtDormitoryStaff.setDormitoryId(dormitory.getId());
		smtDormitoryStaff.setDormitoryName(dormitory.getDormitoryName());
		smtDormitoryStaff.setFloorId(floor.getId());
		smtDormitoryStaff.setFloorName(floor.getFloorName());
		smtDormitoryStaff.setRoomId(room.getId());
		smtDormitoryStaff.setRoomName(room.getRoomName());
		smtDormitoryStaff.setBedId(bed.getId());
		smtDormitoryStaff.setBedNumber(bed.getBedNumber());
		smtDormitoryStaff.setDormitoryTypeId(smtDormitoryType.getId());
		smtDormitoryStaff.setDormitoryTypeName(smtDormitoryType.getTypeName());
		smtDormitoryStaff.setCreateTime(staffReqDTO.getCreateTime());
		smtDormitoryStaff.setStaffSex(staff.getSex());
		smtDormitoryStaff.setCompName(staff.getCompName());
		smtDormitoryStaff.setDepName(staff.getDepName());
		smtDormitoryStaff.setJobName(staff.getJobName());
		smtDormitoryStaff.setIsStaff(staff.getStatus());
		this.baseMapper.insert(smtDormitoryStaff);

		SmtDormitoryStaffHistory dormitoryStaffHistory = new SmtDormitoryStaffHistory();
		BeanUtils.copyProperties(smtDormitoryStaff, dormitoryStaffHistory);
		dormitoryStaffHistory.setId(null);
		dormitoryStaffHistory.setType(0);
		dormitoryStaffHistory.setInTime(staffReqDTO.getCreateTime());
		dormitoryStaffHistory.setTime(staffReqDTO.getCreateTime());
		dormitoryStaffHistory.setDfId(smtDormitoryStaff.getId());
		hisService.save(dormitoryStaffHistory);

		return true;
	}

	public boolean checkParam(DormitoryStaffReqDTO staffReqDTO) {
		if (StrUtil.isBlank(staffReqDTO.getParkName())) {
			staffReqDTO.setMark("园区名称不能为空");
			return false;
		}
		if (StrUtil.isBlank(staffReqDTO.getStaffBadge())) {
			staffReqDTO.setMark("员工号不能为空");
			return false;
		}
		if (StrUtil.isBlank(staffReqDTO.getDormitoryName())) {
			staffReqDTO.setMark("楼栋名称不能为空");
			return false;
		}
		if (ObjectUtil.isNull(staffReqDTO.getFloorName())) {
			staffReqDTO.setMark("楼层名称不能为空");
			return false;
		}
		if (StrUtil.isBlank(staffReqDTO.getRoomName())) {
			staffReqDTO.setMark("房间号不能为空");
			return false;
		}
		if (ObjectUtil.isNull(staffReqDTO.getBedNumber())) {
			staffReqDTO.setMark("床位号不能为空");
			return false;
		}
		if (StrUtil.isBlank(staffReqDTO.getRoomType())) {
			staffReqDTO.setMark("房间类型不能为空");
			return false;
		}
		if (ObjectUtil.isNull(staffReqDTO.getCreateTime())) {
			staffReqDTO.setMark("入住时间不能为空");
			return false;
		}
		return true;
	}

	@Override
	public ResponseEntity<byte[]> batchImportPersons(Integer dormId, MultipartFile multipartFile) {
		if (multipartFile == null) {
			log.info("上传文件对象为空");
			throw new TCEException("上传文件对象为空");
		}
		if(multipartFile.getSize() > 5242880) {
			log.info("上传文件对象大小大于5M");
			throw new TCEException("上传文件对象大小大于5M");
		}
		SmtDormitory dormitory = dormitoryMapper.selectById(dormId);
		if (ObjectUtil.isNull(dormitory)) {
			log.info("楼栋信息查询为空：{}", dormId);
			throw new TCEException("楼栋信息查询为空");
		}
		SmtPark park = parkService.getById(dormitory.getParkId());
		if (ObjectUtil.isNull(park)) {
			log.info("园区信息查询为空：{}", dormitory.getParkId());
			throw new TCEException("园区信息查询为空");
		}
		List<DormitoryStaffExcelDTO> excelDTOList = null;
		try (InputStream inputStream = multipartFile.getInputStream()) {
			ExcelReader excelReader = ExcelUtil.getReader(inputStream);

			Map<String, String> headerAlias = new HashMap<>(14);
			headerAlias.put("序号", "bedNum");
			headerAlias.put("工号", "badge");
			headerAlias.put("姓名", "staffName");
			headerAlias.put("部门", "dept");
			headerAlias.put("职务", "jobName");
			headerAlias.put("原因", "reason");
			headerAlias.put("入住日期", "inTime");
			headerAlias.put("退宿日期", "outTime");
			headerAlias.put("房间", "roomName");
			headerAlias.put("已住人数", "usedBed");
			headerAlias.put("空床位", "leftBed");
			headerAlias.put("S4", "four");
			headerAlias.put("集团", "company");
			headerAlias.put("备注", "remark");
			excelReader.setHeaderAlias(headerAlias);

			excelDTOList = excelReader.read(1, 2, DormitoryStaffExcelDTO.class);
			if (CollectionUtil.isEmpty(excelDTOList) || excelDTOList.size() < 1) {
				log.info("Excel内容为空");
				throw new TCEException("上传的Excel内容为空");
			}
			for (DormitoryStaffExcelDTO row : excelDTOList) {
				log.info("excel数据：{}", row);
				if (!checkParam(row)) {
					continue;
				}
				try {
					if ("离职".equals(row.getReason()) || "自离".equals(row.getReason())) {
						handleDormitoryStaff(row, dormitory, DormitoryHisotryTypeEnum.OUT_DORMITORY.getCode());
					} else if ("外宿".equals(row.getReason())) {
						handleDormitoryStaff(row, dormitory, DormitoryHisotryTypeEnum.QUTI_DORMITORY.getCode());
					} else if ("内宿".equals(row.getReason())) {
						handleDormitoryStaff(row, dormitory, DormitoryHisotryTypeEnum.IN_DORMITORY.getCode());
					}
				} catch (Exception ex) {
					row.setRespRemark("数据处理异常");
					log.error("数据处理异常", ex);
				}
			}
		} catch (IOException e) {
			log.error("读取excel失败,{}", e.getMessage());
			throw new TCEException("读取excel失败");
		}
		// 过滤出失败的记录
		excelDTOList = excelDTOList.stream().filter(dto -> StrUtil.isNotBlank(dto.getRespRemark())).collect(Collectors.toList());

		if (CollectionUtil.isEmpty(excelDTOList)) {
			throw new TCEException(0, "导入成功");
		}

		ResponseEntity<byte[]> responseEntity;
		log.info("excel_name:{}", multipartFile.getOriginalFilename());
		try (Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(), DormitoryStaffExcelDTO.class, excelDTOList)) {
			String fileName = multipartFile.getOriginalFilename();
			responseEntity = IOUtils.getExcelResponse(fileName, workbook);
		} catch (IOException e) {
			log.error("excel导出异常", e);
			throw new TCEException(ExceptionEnum.UNKNOWN.getCode(), "excel导出异常");
		}
		return responseEntity;
	}

	@Override
	public List<SmtDormitoryStaff> getRoommate(List<Integer> roomId, String badge) {
		return this.list(Wrappers.<SmtDormitoryStaff>query().lambda()
				.in(SmtDormitoryStaff::getRoomId, roomId).ne(SmtDormitoryStaff::getStaffBadge, badge));
	}

	private boolean checkParam(DormitoryStaffExcelDTO row) {
		if (ObjectUtil.isNull(row.getBedNum())) {
			row.setRespRemark("床位号不能为空");
			return false;
		} else if (StrUtil.isBlank(row.getRoomName())) {
			row.setRespRemark("房间号不能为空");
			return false;
		} else if (StrUtil.isBlank(row.getBadge())) {
			row.setRespRemark("工号不能为空");
			return false;
		} else if (StrUtil.isBlank(row.getStaffName())) {
			row.setRespRemark("姓名不能为空");
			return false;
		}
		return true;
	}

	private boolean handleDormitoryStaff(DormitoryStaffExcelDTO row, SmtDormitory dormitory, Integer type) {
		if (DormitoryHisotryTypeEnum.IN_DORMITORY.getCode().equals(type)) {
			if (StrUtil.isBlank(row.getInTime())) {
				row.setRespRemark("入住时间不能为空");
				return false;
			}
		} else {
			if (StrUtil.isBlank(row.getOutTime())) {
				row.setRespRemark("退宿时间不能为空");
				return false;
			}
		}
		SmtStaff staff = staffService.getOne(new LambdaQueryWrapper<SmtStaff>()
				.eq(SmtStaff::getBadge, row.getBadge()), false);

		String roomName = row.getRoomName().trim().substring(1);
		if (StrUtil.isBlank(roomName) || roomName.length() < 1) {
			row.setRespRemark("房间号数据格式不正确");
			return false;
		}
		List<SmtDormitoryFloor> floorList = floorMapper.selectList(new LambdaQueryWrapper<SmtDormitoryFloor>()
				.eq(SmtDormitoryFloor::getDormitoryId, dormitory.getId())
				.eq(SmtDormitoryFloor::getFloorName, roomName.length() > 3 ? Integer.parseInt(roomName.substring(0, 2)) : Integer.parseInt(roomName.substring(0, 1))));
		if (CollectionUtil.isEmpty(floorList)) {
			log.info("楼层查询失败：{},{}", dormitory.getId(), roomName);
			row.setRespRemark("楼层查询失败");
			return false;
		}
		SmtDormitoryFloor floor = floorList.get(0);
		List<SmtDormitoryRoom> roomList = roomMapper.selectList(new LambdaQueryWrapper<SmtDormitoryRoom>()
				.eq(SmtDormitoryRoom::getFloorId, floor.getId())
				.eq(SmtDormitoryRoom::getRoomName, Integer.parseInt(roomName)));
		if (CollectionUtil.isEmpty(roomList)) {
			log.info("房间查询失败：{},{}", floor.getId(), roomName);
			row.setRespRemark("房间查询失败");
			return false;
		}
		SmtDormitoryRoom room = roomList.get(0);

		SmtDormitoryStaff dormitoryStaff = this.getOne(new LambdaQueryWrapper<SmtDormitoryStaff>()
				.eq(SmtDormitoryStaff::getDormitoryId, dormitory.getId())
				.eq(SmtDormitoryStaff::getFloorId, floor.getId())
				.eq(SmtDormitoryStaff::getRoomId, room.getId())
				.eq(SmtDormitoryStaff::getBedNumber, row.getBedNum()), false);
		SmtPark park = this.parkService.getById(dormitory.getParkId());
		if (ObjectUtil.isNull(park)) {
			log.info("园区不存在：{}", dormitory.getParkId());
			row.setRespRemark("园区不存在");
			return false;
		}

		// 内宿
		if (DormitoryHisotryTypeEnum.IN_DORMITORY.getCode().equals(type)) {
			if (ObjectUtil.isNotNull(dormitoryStaff)) {
				log.info("该床位已被占用：roomId:{},bedNum:{},oldName:{},newName:{}", room.getId(), row.getBedNum(),
						dormitoryStaff.getStaffName(), row.getStaffName());
				if (dormitoryStaff.getStaffName().equals(row.getStaffName())) {
					row.setRespRemark("该人员已入住该床位");
					return false;
				}
				// 表格数据中姓名和已入住姓名不一致，则退宿再入住
				if (!outDormitory(dormitoryStaff, staff, DormitoryHisotryTypeEnum.OUT_DORMITORY.getCode(), DateUtils.now())) {
					log.info("退宿失败");
					row.setRespRemark("退宿失败");
					return false;
				}
			}
			log.info("床位查询信息：bedNum:{},roomId:{},floorId:{},dormId:{}", row.getBedNum(), room.getId(),
					floor.getId(), dormitory.getId());
			List<SmtDormitoryBed> beds = this.bedMapper.selectList(new LambdaQueryWrapper<SmtDormitoryBed>()
					.eq(SmtDormitoryBed::getBedNumber, row.getBedNum())
					.eq(SmtDormitoryBed::getRoomId, room.getId())
					.eq(SmtDormitoryBed::getFloorId, floor.getId())
					.eq(SmtDormitoryBed::getDormitoryId, dormitory.getId())
					.eq(SmtDormitoryBed::getDelFlag, 0));
			if (CollectionUtil.isEmpty(beds)) {
				log.info("无此床位信息");
				row.setRespRemark("无此床位信息");
				return false;
			}
			SmtDormitoryBed bed = beds.get(0);
			SmtDormitoryType smtDormitoryType = this.dormitoryTypeMapper.selectById(room.getRoomType());
			dormitoryStaff = new SmtDormitoryStaff();
			dormitoryStaff.setParkId(park.getId());
			dormitoryStaff.setParkName(park.getParkName());
			dormitoryStaff.setDormitoryId(dormitory.getId());
			dormitoryStaff.setDormitoryName(dormitory.getDormitoryName());
			dormitoryStaff.setFloorId(floor.getId());
			dormitoryStaff.setFloorName(floor.getFloorName());
			dormitoryStaff.setRoomId(room.getId());
			dormitoryStaff.setRoomName(room.getRoomName());
			dormitoryStaff.setBedId(bed.getId());
			dormitoryStaff.setBedNumber(row.getBedNum());
			dormitoryStaff.setDormitoryTypeId(smtDormitoryType.getId());
			dormitoryStaff.setDormitoryTypeName(smtDormitoryType.getTypeName());
			dormitoryStaff.setCreateTime(DateUtils.parseDate(row.getInTime()));
			log.info("入住员工信息: {}", staff);
			if (ObjectUtil.isNotNull(staff)) {
				// 正式员工入住
				dormitoryStaff.setIsStaff(1);
				dormitoryStaff.setStaffBadge(staff.getBadge());
				dormitoryStaff.setStaffName(staff.getName());
				dormitoryStaff.setCompName(staff.getCompName());
				dormitoryStaff.setDepName(staff.getDepName());
				dormitoryStaff.setJobName(staff.getJobName());
				dormitoryStaff.setStaffSex(staff.getSex());
			} else {
				// 非正式员工入住
				dormitoryStaff.setStaffBadge(row.getBadge());
				dormitoryStaff.setStaffName(row.getStaffName());
				dormitoryStaff.setJobName(row.getJobName());
				dormitoryStaff.setIsStaff(0);
			}
			dormitoryStaff.insert();
			return addDormitoryStaffHistory(dormitoryStaff, row.getInTime(),
					DormitoryHisotryTypeEnum.IN_DORMITORY.getCode(), null);
		}
		if (ObjectUtil.isNull(dormitoryStaff)) {
			log.info("该床位还未入住人员：{},{}", room.getId(), row.getBedNum());
			row.setRespRemark("该床位还未入住人员");
			return false;
		}
		if (!dormitoryStaff.getStaffBadge().equals(row.getBadge())) {
			log.info("该房间入住员工工号与导入数据不一致：{},{}", dormitoryStaff.getStaffBadge(), row.getBadge());
			row.setRespRemark("该房间入住员工工号与导入数据不一致，请确认数据");
			return false;
		}
		return outDormitory(dormitoryStaff, staff, type, row.getOutTime());
	}

	/**
	 * 退宿
	 *
	 * @param dormitoryStaff
	 * @param staff
	 * @param type
	 * @return
	 */
	private boolean outDormitory(SmtDormitoryStaff dormitoryStaff, SmtStaff staff, Integer type, String outTime) {
		if (this.removeById(dormitoryStaff.getId())) {
			if (ObjectUtil.isNotNull(staff)) {
				staff.setDormitoryStatus(DormitoryStatusEnum.IS_INIT.getCode());
				this.staffService.updateById(staff);
				dormitoryStaff.setCompName(staff.getCompName());
				dormitoryStaff.setDepName(staff.getDepName());
				dormitoryStaff.setJobName(staff.getJobName());
			}
			return addDormitoryStaffHistory(dormitoryStaff, outTime, type, null);
		}
		return true;
	}

	private boolean addDormitoryStaffHistory(SmtDormitoryStaff dormitoryStaff, String time, Integer type, Integer oldRoom) {
		return addDormitoryStaffHistory(dormitoryStaff, time, type, oldRoom, DormitoryStatisFlagnum.STATIS);
	}

	private boolean addDormitoryStaffHistory(SmtDormitoryStaff dormitoryStaff, String time, Integer type, Integer oldRoom, DormitoryStatisFlagnum flagnum) {
		SmtDormitoryStaffHistory history = new SmtDormitoryStaffHistory();
		history.setBedId(dormitoryStaff.getBedId());
		history.setBedNumber(dormitoryStaff.getBedNumber());
		history.setTime(DateUtils.parseDate(time));
		history.setCreateTime(DateUtils.date());
		history.setStatisFlag(flagnum.getCode());
		history.setDormitoryId(dormitoryStaff.getDormitoryId());
		history.setDormitoryName(dormitoryStaff.getDormitoryName());
		history.setDormitoryTypeId(dormitoryStaff.getDormitoryTypeId());
		history.setDormitoryTypeName(dormitoryStaff.getDormitoryTypeName());
		history.setFloorId(dormitoryStaff.getFloorId());
		history.setFloorName(dormitoryStaff.getFloorName());
		history.setInTime(dormitoryStaff.getCreateTime());
		history.setParkId(dormitoryStaff.getParkId());
		history.setParkName(dormitoryStaff.getParkName());
		history.setRoomId(dormitoryStaff.getRoomId());
		history.setRoomName(dormitoryStaff.getRoomName());
		history.setStaffBadge(dormitoryStaff.getStaffBadge());
		history.setStaffSex(dormitoryStaff.getStaffSex());
		history.setStaffId(dormitoryStaff.getStaffId());
		history.setStaffName(dormitoryStaff.getStaffName());
		history.setType(type);
		history.setIsStaff(dormitoryStaff.getIsStaff());
		history.setDfId(dormitoryStaff.getId());
		history.setIsStaff(dormitoryStaff.getIsStaff());
		history.setCompName(dormitoryStaff.getCompName());
		history.setDepName(dormitoryStaff.getDepName());
		history.setJobName(dormitoryStaff.getJobName());
		try {
			history.setOptUser(SecurityUtils.getUser().getUsername());
		}catch (Exception e){}
		history.setInOptUser(dormitoryStaff.getOptUser());
		history.setInCreateTime(dormitoryStaff.getCreateTime());
		history.insert();
		if (!DormitoryHisotryTypeEnum.IN_DORMITORY.getCode().equals(type)) {
			smtDormitoryOutRemarkService.updateDorStaffId(dormitoryStaff.getId(), history.getId());
		}
		try {
			connectLockService.sendLockData(dormitoryStaff, type, oldRoom);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return Boolean.TRUE;
	}


	@Override
	public DormitoryLockInfoRespDTO getLockStatus(Integer roomId, String badge) {
		DormitoryLockInfoRespDTO respDTO = new DormitoryLockInfoRespDTO();
		DeviceTypeInfoDTO typeInfoDTO = connectLockService.getLockType(roomId);
		log.info("门锁返回1{}", typeInfoDTO);
		if(Objects.isNull(typeInfoDTO)) {
			return null;
		}
		respDTO.setFingerprintCode(UnLockTypeEnum.CAUSE_2.getCode());
		respDTO.setFingerprintDesc(UnLockTypeEnum.CAUSE_2.getDesc());
		if (SymbolConstants.ONE_INTEGER.equals(typeInfoDTO.getApplyFinger())) {
			Boolean hasFinger = connectLockService.existFingerByBadge(badge);
			log.info("门锁返回2{}", hasFinger);
			if (hasFinger) {
				respDTO.setFingerprintCode(UnLockTypeEnum.CAUSE_1.getCode());
				respDTO.setFingerprintDesc(UnLockTypeEnum.CAUSE_1.getDesc());
			}
		} else {
			respDTO.setFingerprintCode(UnLockTypeEnum.CAUSE_0.getCode());
			respDTO.setFingerprintDesc(UnLockTypeEnum.CAUSE_0.getDesc());
		}
		if (SymbolConstants.ONE_INTEGER.equals(typeInfoDTO.getApplyPwd())) {
			String pwd = connectLockService.getPwdByBadge(badge);
			if (StrUtil.isNotBlank(pwd)) {
				respDTO.setDynamicCode(UnLockTypeEnum.CAUSE_3.getCode());
				respDTO.setDynamicDesc(pwd);
			} else {
				respDTO.setDynamicCode(UnLockTypeEnum.CAUSE_4.getCode());
				respDTO.setDynamicDesc(UnLockTypeEnum.CAUSE_4.getDesc());
			}
		} else {
			respDTO.setDynamicCode(UnLockTypeEnum.CAUSE_0.getCode());
			respDTO.setDynamicDesc(UnLockTypeEnum.CAUSE_0.getDesc());
		}
		return respDTO;
	}

	@Override
	public String faceCompare(DorStaffPerfectDTO perfectDTO) {
		if(this.compareFace(perfectDTO)) {
			return this.getPwdByBadge(perfectDTO.getBadge());
		}
		return null;
	}

	private Boolean compareFace(DorStaffPerfectDTO perfectDTO) {
		String badge = perfectDTO.getBadge();
		SmtStaff staff = staffService.getSimpleSttaffByBadge(badge);
		String getImageBase64Rs = smtImageService.getImageBase64ByCode(staff.getFacePicId());
		if(StrUtil.isEmpty(getImageBase64Rs)) {
			throw new SmartException("员工头像图片为空");
		}
		CompareDTO compareDTO = new CompareDTO();
		CompareImageDTO compareImageDTO1 = new CompareImageDTO();
		compareImageDTO1.setImageBase64(perfectDTO.getFacePic());
		compareImageDTO1.setFaceType(FaceTypeEnum.LIVE.getType());

		CompareImageDTO compareImageDTO2 = new CompareImageDTO();
		compareImageDTO2.setImageBase64(getImageBase64Rs);
		compareImageDTO2.setFaceType(FaceTypeEnum.LIVE.getType());

		compareDTO.setCompareImageA(compareImageDTO1);
		compareDTO.setCompareImageB(compareImageDTO2);
		try {
			com.tce.smart.algorithm.api.dto.resp.CompareDTO imageComareRs = remoteAlgorithmService.compare(IdUtil.fastSimpleUUID().toUpperCase(), AlgorithmTypeEnum.COMPARE_FACEALL.getType(), compareDTO, SecurityConstants.FROM_IN).data();
			log.info("人像比对: 员工号:{} 相似度：{}", staff.getBadge(), imageComareRs.getSimilarity());
			//小于阀值则认为不是本人
			if (-1 == (new BigDecimal(String.valueOf(imageComareRs.getSimilarity()))
					.compareTo(new BigDecimal(loginCompareValue)))) {
				throw new TCEException("人脸识别验证未通过,相识度[" + imageComareRs.getSimilarity() + "]");
			}
		} catch (TCEException tce) {
			throw tce;
		} catch (Exception e) {
			log.error("人脸识别验证未通过", e);
			throw new TCEException("人脸识别验证未通过");
		}
		return Boolean.TRUE;
	}

	@Override
	public String getPwdByBadge(String badge) {
		String pwd = "";
		try {
			pwd = connectLockService.getPwdByBadge(badge);
		} catch (Exception e) {
			throw new TCEException("获取动态码失败");
		}
		return pwd;
	}

	@Override
	public String updatePwdByBadge(DorStaffPerfectDTO perfectDTO) {
		if(!this.compareFace(perfectDTO)) {
			throw new SmartException("人脸比对失败");
		}
		String pwd = "";
		//生成动态码
		try {
			pwd = connectLockService.generatePwdByBadge(perfectDTO.getBadge());
		} catch (Exception e) {
			throw new TCEException("生成动态码失败");
		}
		return pwd;
	}

	@Override
	public String updateLockPwdByBadge(LockPwdUpdateDTO lockPwdUpdateDTO) {
		//限制一个工号每天修改次数
		ValueOperations<String, String> value = redisTemplate.opsForValue();
		String redisKey = RedisKeyConstants.SMAT_LOCK_UPDATE_PASSWORD_LIMIT + lockPwdUpdateDTO.getBadge();
		String val = value.get(redisKey);
		int currCount = StringUtils.hasLength(val) ? Integer.parseInt(val) : 0;
		if(currCount < lockPasswordLimit){
			//可以继续修改
			String pwd = connectLockService.updatePwdByBadge(lockPwdUpdateDTO.getBadge(), lockPwdUpdateDTO.getNewPwd());
			//修改次数加1
			currCount++;
			//过期时间为今天
			Duration between = Duration.between(LocalDateTime.now(), LocalDateTime.of(LocalDate.now(), LocalTime.MAX));
			value.set(redisKey,String.valueOf(currCount),between.getSeconds(),TimeUnit.SECONDS);
			return pwd;
		}
		throw new TCEException("今天修改次数已达到上限");
	}

	@Override
	public Boolean updateSimpleRemark(Long id, String remark) {
		return this.update(Wrappers.<SmtDormitoryStaff>lambdaUpdate().set(SmtDormitoryStaff::getSimpleRemark, remark)
				.eq(SmtDormitoryStaff::getId, id));
	}

}
