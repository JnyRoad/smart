package com.tce.smart.schedule.task;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.SexType;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.data.api.dto.dhrview.resp.YutoDhrPsndoDTO;
import com.tce.smart.data.api.enums.DHREmpStatusEnum;
import com.tce.smart.data.api.feign.dhrview.RemoteYutoDhrYsService;
import com.tce.smart.ehrview.core.entity.EvwEmphrYs;
import com.tce.smart.ehrview.core.service.IEvwEmphrYsService;
import com.tce.smart.platform.api.dto.SmtEhrToStaffSettingDTO;
import com.tce.smart.platform.api.dto.req.EmpHrReqDTO;
import com.tce.smart.platform.api.feign.RemoteEhrToStaffSettingService;
import com.tce.smart.platform.api.feign.RemoteStaffService;
import com.tce.smart.schedule.config.TaskJob;
import com.tce.smart.schedule.service.comm.ISwitchService;
import com.tce.smart.schedule.service.platform.SmtStaffTaskService;
import com.tce.smart.tool.enums.EmpTypeEnum;
import com.tce.smart.tool.enums.EvwHortationsAllJchenEnum;
import com.tce.smart.tool.enums.StaffStatusEnum;
import com.tce.smart.tool.enums.TimerTaskEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * description: EhrToStaffTaskService <br>
 * date: 2020/1/15 15:34 <br>
 * author: qipei <br>
 * version: 1.0 <br>
 * ehr同步员工的任务
 */
@Component
@EnableScheduling
@Slf4j
public class EhrToStaffTaskService {

	/**
	 * 裕同员工库男性-编码
	 */
	private final static Integer YUTO_SEX_MALE = 1;
	/**
	 * 裕同员工库-女性编码
	 */
	private final static Integer YUTO_SEX_FEMALE = 2;
	/**
	 * 裕同员工预计总数
	 */
	private final static Integer STAFF_TOTAL = 250000;

	@Autowired
	private RemoteEhrToStaffSettingService service;
	@Autowired
	private IEvwEmphrYsService iEvwEmphrYsService;
	@Autowired
	private RemoteStaffService remoteStaffService;
	@Autowired
	private ISwitchService iSwitchService;
	@Autowired
	private RemoteYutoDhrYsService remoteYutoDhrYsService;
	@Autowired
	private SmtStaffTaskService smtStaffTaskService;
	@Autowired
	private TaskJob taskJob;

	/**
	 * 同步dhr人脸照片 60分钟一次
	 */
	@Scheduled(fixedDelayString  = "${smart.schedule.sync.task.syncDhrFaceImg}")
	public void syncDhrFaceImg() {
		if (taskJob.getSyncDhrFaceImg() && iSwitchService.process(TimerTaskEnum.DHR_IMG_TYPE)) {
			log.info("发起DHR员工人脸数据同步任务");
			remoteStaffService.syncStaffImg(SecurityConstants.FROM_IN);
			log.info("结束DHR员工人脸数据同步任务");
		}
	}

	/**
	 * 定时同步EHR员工数据
	 * 每10分钟同步一次
	 */
	@Scheduled(fixedDelay = 1000 * 60 * 10)
	public void syncStaffTask() {
		if (taskJob.getSyncStaff() && iSwitchService.process(TimerTaskEnum.EMPHR_TYPE)) {
			try {
				log.info("发起员工数据同步任务");
				//获取员工同步数据配置
				Result<List<SmtEhrToStaffSettingDTO>> list = service.getEhrList(SecurityConstants.FROM_IN);
				log.info("remoteDictService.findByType result{}", list);
				if (!list.isSuccess()) {
					log.error("未找到BU配置信息，终止同步");
					return;
				}
				List<Integer> compId = new ArrayList<>();
				if (CollectionUtils.isNotEmpty(list.getData())) {
					for (SmtEhrToStaffSettingDTO dto : list.getData()) {
						if (!compId.contains(Integer.parseInt(dto.getCompId()))) {
							compId.add(Integer.parseInt(dto.getCompId()));
						}
					}
				}
				// 任务逻辑
				//page查1000条 每页的条数可调整
				Page<EvwEmphrYs> page = new Page<>(1, 1000);
				//所有的ehr员工数据
				List<EvwEmphrYs> evwEmpHrYs = new ArrayList<>(STAFF_TOTAL);
				//EHR中所有员工的md5信息 预设25W
				Map<String, String> md5EhrMap = new HashMap<>(STAFF_TOTAL);
				//先查询出所有的ehr员工数据
				log.info("开始查询ehr数据:{}", DateUtil.date());
				boolean isNext;
				do {
					//查询一页数据
					if (page.getTotal() == page.getCurrent()) {
						log.info("ehr最后一页：" + page.getTotal());
						long lastPageCount = page.getTotal() % 1000;
						log.info("ehr最后一页剩余条数：" + lastPageCount);
						page.setSize(lastPageCount);
					}
					IPage<EvwEmphrYs> records = iEvwEmphrYsService.getPage(page, compId);
					List<EvwEmphrYs> evwEmpHrYsList = records.getRecords();
					//获取数据的MD5
					//在此处进行了人脸变更同步查询
					evwEmpHrYsList.forEach(item -> md5EhrMap.put(item.getBadge(), getMd5(getEmpHrReqDTO(item))));
					evwEmpHrYs.addAll(evwEmpHrYsList);
					if (page.hasNext()) {
						//下一页
						page.setCurrent(page.getCurrent() + 1);
						isNext = true;
					} else {
						isNext = false;
					}
				} while (isNext);
				log.info("查询ehr数据完成:{}", DateUtil.date());
				//再查询platform平台中的所有员工信息 每页查1000条
				Page page2 = new Page(1, 1000);
				//platform平台中的员工md5信息
				log.info("开始查询platform数据:{}", DateUtil.date());
				Map<String, String> md5PlatMap = new HashMap<>(STAFF_TOTAL);
				do {
					//判断如果是最后一页，最后的条数应该是剩余条数
					if (page2.getTotal() == page2.getCurrent()) {
						log.info("staff最后一页：" + page2.getTotal());
						long lastPageCount = page2.getTotal() % 1000;
						log.info("staff最后一页剩余条数：" + lastPageCount);
						page2.setSize(lastPageCount);
					}
					//查询一页数据
					Result<Page<EmpHrReqDTO>> result = remoteStaffService
							.getStaffList(page2.getCurrent(), page2.getSize(), SecurityConstants.FROM_IN);
					if (!result.isSuccess()) {
						//查询员工数据异常
						log.error("查询员工数据异常");
						return;
					}
					page2 = result.getData();
					List<EmpHrReqDTO> staffList = result.getData().getRecords();
					//获取数据MD5
					staffList.forEach(item -> md5PlatMap.put(item.getBadge(), getMd5(item)));
					if (page2.hasNext()) {
						//下一页
						page2.setCurrent(page2.getCurrent() + 1);
						isNext = true;
					} else {
						isNext = false;
					}
				} while (isNext);
				log.info("查询platform数据完成:{}", DateUtil.date());
				//比较两个map的差异
				//记录差异的员工
				List<EvwEmphrYs> diffList = new ArrayList<>();
				for (Map.Entry<String, String> ehrEntry : md5EhrMap.entrySet()) {
					if (!md5PlatMap.containsKey(ehrEntry.getKey())
							|| !md5PlatMap.get(ehrEntry.getKey()).equalsIgnoreCase(ehrEntry.getValue())) {
						//如果在platform中不存在或者加密的value值不相等,并且不是已经离职员工, 则需要同步数据
						diffList.addAll(evwEmpHrYs.stream()
								.filter(item -> item.getBadge().equals(ehrEntry.getKey())
										&& !item.getStatus().equals(StaffStatusEnum.STAFF_STATUS_QUIT.getCode()))
								.collect(Collectors.toList()));
					}
				}
				log.info("本次员工同步差异数据 ({}) 条", diffList.size());
				//同步数据
				diffList.forEach(this::saveEvwEmpHrYs);
				log.info("本次员工同步任务结束: {}", DateUtil.date());
			} catch (Exception e) {
				log.error("同步员工数据异常", e);
			}

		}
	}

	/**
	 * 定时同步DHR员工数据
	 * 每10分钟同步一次
	 */
	@Scheduled(fixedDelayString = "${smart.schedule.sync.task.syncDhrStaffTask}")
	public void syncDhrStaffTask() {
		LocalDateTime now = LocalDateTime.now();
		if ((now.getHour() == 4 && now.getMinute() > 30) || (now.getHour() == 5 && now.getMinute() < 30)) {
			// 凌晨4点半-5点半，由于DHR系统数据异常，因此该时间段不同步
			return;
		}
		if (taskJob.getSyncDhrStaff() && iSwitchService.process(TimerTaskEnum.EMPDHR_TYPE)) {
			try {
				log.info("发起DHR员工数据同步任务");
				//中心  bu-中心-部门-
				//获取员工同步数据BU配置
				Result<List<SmtEhrToStaffSettingDTO>> list = service.getDhrList(SecurityConstants.FROM_IN);
				log.info("DHR员工数据同步，查询配置BU响应结果 {}", list);
				if (!list.isSuccess()) {
					log.error("DHR员工数据同步,未找到BU配置信息，终止同步");
					return;
				}
				if (CollectionUtils.isNotEmpty(list.getData())) {
					List<Integer> compIds = new ArrayList<>();
					for (SmtEhrToStaffSettingDTO dto : list.getData()) {
						int compId = Integer.parseInt(dto.getCompId());
						if (!compIds.contains(compId)) {
							compIds.add(compId);
						}
					}
					// 任务逻辑
					//page查1000条 每页的条数可调整
					Page<YutoDhrPsndoDTO> page = new Page<>(1, 1000);
					//所有的dhr员工原始数据
					List<YutoDhrPsndoDTO> dhrEmpYsAll = new ArrayList<>(STAFF_TOTAL);
					//所有的dhr员工数据
					List<EvwEmphrYs> evwEmpHrYs = new ArrayList<>(STAFF_TOTAL);
					//DHR中所有员工的md5信息 预设25W
					Map<String, String> md5DhrMap = new HashMap<>(STAFF_TOTAL);
					//先查询出所有的dhr员工数据
					log.info("开始查询dhr数据:{}", DateUtil.date());
					boolean isNext;
					do {
						//查询一页数据
						Result<Page<YutoDhrPsndoDTO>> pageResult = remoteYutoDhrYsService
								.page(page.getCurrent(), page.getSize(), compIds, SecurityConstants.FROM_IN);
						log.info("远程查询DHR员工信息结果:{}", pageResult.isSuccess());
						if (!pageResult.isSuccess()) {
							log.error("远程查询DHR员工信息结果失败");
							return;
						}
						page = pageResult.getData();
						List<YutoDhrPsndoDTO> dhrEmpYsList = pageResult.getData().getRecords();
						dhrEmpYsAll.addAll(dhrEmpYsList);
						List<EvwEmphrYs> evwEmpHrYsList = convertData(dhrEmpYsList);
						//获取数据的MD5
						evwEmpHrYsList.forEach(item -> md5DhrMap.put(item.getBadge(), getMd5(getEmpHrReqDTO(item))));
						evwEmpHrYs.addAll(evwEmpHrYsList);
						if (page.hasNext()) {
							//下一页
							page.setCurrent(page.getCurrent() + 1);
							isNext = true;
						} else {
							isNext = false;
						}
					} while (isNext);
					log.info("查询dhr数据完成:{}", DateUtil.date());
					//再查询platform平台中的所有员工信息 每页查1000条
					Page page2 = new Page(1, 1000);
					//platform平台中的员工md5信息
					log.info("DHR同步开始查询platform数据:{}", DateUtil.date());
					Map<String, String> md5PlatMap = new HashMap<>(STAFF_TOTAL);
					List<EmpHrReqDTO> staffAllList = new ArrayList<>(STAFF_TOTAL);
					do {
						//查询一页数据
						Result<Page<EmpHrReqDTO>> result = remoteStaffService
								.getStaffList(page2.getCurrent(), page2.getSize(), SecurityConstants.FROM_IN);
						if (!result.isSuccess()) {
							//查询员工数据异常
							log.error("DHR同步查询员工数据异常,{}",result.getMessage());
							return;
						}
						page2 = result.getData();
						List<EmpHrReqDTO> staffList = result.getData().getRecords();
						staffAllList.addAll(staffList);
						//获取数据MD5
						staffList.forEach(item -> md5PlatMap.put(item.getBadge(), getMd5(item)));
						if (page2.hasNext()) {
							//下一页
							page2.setCurrent(page2.getCurrent() + 1);
							isNext = true;
						} else {
							isNext = false;
						}
					} while (isNext);
					log.info("DHR同步查询platform数据完成:{}", DateUtil.date());
					//比较两个map的差异
					//记录差异的员工
					int needUpdateCount = 0;
					for (Map.Entry<String, String> ehrEntry : md5DhrMap.entrySet()) {
						//平台不包含dhr数据或者平台MD5不等于dhr的MD5
						if (null != ehrEntry.getKey() && !md5PlatMap.containsKey(ehrEntry.getKey())
								|| !md5PlatMap.get(ehrEntry.getKey()).equalsIgnoreCase(ehrEntry.getValue())) {
							//如果在platform中不存在或者加密的value值不相等,并且不是已经离职员工, 则需要同步数据
							EvwEmphrYs empHrYs = evwEmpHrYs.stream()
									.filter(item -> ehrEntry.getKey().equals(item.getBadge()))
									.findFirst()
									.orElse(null);
							if (Objects.nonNull(empHrYs)) {
								saveEvwEmpHrYs(empHrYs);
								needUpdateCount++;
							}
						}
					}
					log.info("DHR同步本次员工同步差异数据 ({}) 条", needUpdateCount);
					log.info("DHR同步本次员工同步任务结束: {}", DateUtil.date());
				}
			} catch (Exception e) {
				log.error("DHR同步员工数据异常", e);
			}
		}
	}

	/**
	 * DHR数据转换为EHR数据结构
	 *
	 * @param records
	 * @return
	 */
	private List<EvwEmphrYs> convertData(List<YutoDhrPsndoDTO> records) {
		if (CollectionUtil.isEmpty(records)) {
			return null;
		}
		List<EvwEmphrYs> evwEmpHrYsList = new ArrayList<>();
		records.forEach(item -> {
			EvwEmphrYs evwEmphrYs = convertDhrDataToEhrData(item);
			evwEmpHrYsList.add(evwEmphrYs);
		});
		return evwEmpHrYsList;
	}

	private EvwEmphrYs convertDhrDataToEhrData(YutoDhrPsndoDTO item) {
		EvwEmphrYs evwEmphrYs = new EvwEmphrYs();
		evwEmphrYs.setBadge(item.getCode());
		evwEmphrYs.setName(item.getName());
		if (StringUtils.isNotEmpty(item.getEnddate())) {
			evwEmphrYs.setLeaDate(DateUtil.parseDate(item.getEnddate()));
		}
		evwEmphrYs.setLeaType(item.getLeatype());
		evwEmphrYs.setCompID(item.getPkOrg());
		evwEmphrYs.setCompname(item.getBuName());
		evwEmphrYs.setDepid(item.getPkDept());
		evwEmphrYs.setDepname(item.getDeptwe());
		evwEmphrYs.setJobid(item.getPkPost());
		evwEmphrYs.setJobname(item.getPostName());
		evwEmphrYs.setReportTo(null == item.getJobglbdef18() ? "" : item.getJobglbdef18());
		evwEmphrYs.setStatus(item.getJobglbdef1());
		evwEmphrYs.setStatusName(DHREmpStatusEnum.desc(item.getJobglbdef1()));
		evwEmphrYs.setEmpType(EmpTypeEnum.codeByDHR(item.getPsntype()));
		evwEmphrYs.setEmptypeName(item.getPsntype());
		if (null == item.getJchen()) {
			evwEmphrYs.setJchenID(EvwHortationsAllJchenEnum.STAFF.getCode());
		} else {
			evwEmphrYs.setJchenID(EvwHortationsAllJchenEnum.code(item.getJchen()
					.replace("层", "").replace("课长", "科长")));
		}
		evwEmphrYs.setJchenName(item.getJchen());
		evwEmphrYs.setFlcj(item.getJobglbdef21());
		evwEmphrYs.setJoindate(item.getGlbdef7());
		evwEmphrYs.setCertno(item.getGlbdef2());
		evwEmphrYs.setGender(item.getSex());
		evwEmphrYs.setMobile(null == item.getMobile() ? "" : item.getMobile());
		evwEmphrYs.setEmail(null == item.getEmail() ? "" : item.getEmail());
		evwEmphrYs.setDepAbbr(null == item.getDepone() ? "" : item.getDepone());
		evwEmphrYs.setNation(item.getGender());
		evwEmphrYs.setResidentaddress(null == item.getCensusaddr() ? "" : item.getCensusaddr());
		evwEmphrYs.setPqcompany(null == item.getPqcompany() ? "" : item.getPqcompany());
		return evwEmphrYs;
	}

	/**
	 * 对EHR员工数据进行MD5加密 注意字段拼接顺序
	 *
	 * @param empHrReqDTO
	 * @return
	 */
	private String getMd5(EmpHrReqDTO empHrReqDTO) {
		MD5 md5 = new MD5();
		return md5.digestHex(joinStr(empHrReqDTO));
	}

	private String joinStr(EmpHrReqDTO empHrReqDTO) {
		if (Objects.isNull(empHrReqDTO)) {
			return "";
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(empHrReqDTO.getFlcj());
		stringBuilder.append(empHrReqDTO.getCertno());
		stringBuilder.append(empHrReqDTO.getGender());

		if (StringUtils.isNotEmpty(empHrReqDTO.getPhone())) {
			stringBuilder.append(empHrReqDTO.getPhone().trim());
		}

		if (StringUtils.isNotEmpty(empHrReqDTO.getEmail())) {
			stringBuilder.append(empHrReqDTO.getEmail());
		}

		if (StringUtils.isNotEmpty(empHrReqDTO.getNation())) {
			stringBuilder.append(empHrReqDTO.getNation());
		}
		if (Objects.nonNull(empHrReqDTO.getLeaDate())) {
			stringBuilder.append(dateFormat.format(empHrReqDTO.getLeaDate()));
		}
		if (StringUtils.isNotEmpty(empHrReqDTO.getResidentaddress())) {
			stringBuilder.append(StringEscapeUtils.unescapeHtml(empHrReqDTO.getResidentaddress()));
		}
		if (StringUtils.isNotEmpty(empHrReqDTO.getDepAbbr())) {
			stringBuilder.append(empHrReqDTO.getDepAbbr());
		}
		stringBuilder.append(empHrReqDTO.getBadge());
		stringBuilder.append(empHrReqDTO.getName());
		stringBuilder.append(empHrReqDTO.getCompID());
		stringBuilder.append(empHrReqDTO.getCompname());
		stringBuilder.append(empHrReqDTO.getDepid());
		if (StringUtils.isNotEmpty(empHrReqDTO.getDepname())) {
			stringBuilder.append(StringEscapeUtils.unescapeHtml(empHrReqDTO.getDepname()));
		}
		stringBuilder.append(empHrReqDTO.getJobid());
		stringBuilder.append(empHrReqDTO.getJobname());
		if (StringUtils.isNotEmpty(empHrReqDTO.getReportTo())) {
			stringBuilder.append(empHrReqDTO.getReportTo());
		}
		stringBuilder.append(empHrReqDTO.getStatus());
		stringBuilder.append(empHrReqDTO.getEmpType());
		stringBuilder.append(empHrReqDTO.getJchenID());
		stringBuilder.append(empHrReqDTO.getJchenName());
		stringBuilder.append(empHrReqDTO.getJoindate() == null ? "" : dateFormat.format(empHrReqDTO.getJoindate()));
		stringBuilder.append(empHrReqDTO.getLeaType() == null ? "" : empHrReqDTO.getLeaType());
		if (StringUtils.isNotEmpty(empHrReqDTO.getPqcompany())) {
			stringBuilder.append(empHrReqDTO.getPqcompany());
		}
		return stringBuilder.toString();
	}

	private EmpHrReqDTO getEmpHrReqDTO(EvwEmphrYs d) {
		EmpHrReqDTO empHrReqDTO = new EmpHrReqDTO();
		empHrReqDTO.setFlcj(d.getFlcj());
		empHrReqDTO.setCertno(d.getCertno());
		empHrReqDTO.setGender(d.getGender());
		if (YUTO_SEX_MALE.equals(d.getGender())) {
			empHrReqDTO.setGender(SexType.MAN.getCode());
		} else if (YUTO_SEX_FEMALE.equals(d.getGender())) {
			empHrReqDTO.setGender(SexType.WOMAN.getCode());
		} else {
			empHrReqDTO.setGender(SexType.UNKNOWN.getCode());
		}
		if (Objects.nonNull(d.getLeaDate())) {
			empHrReqDTO.setLeaDate(d.getLeaDate());
		}
		empHrReqDTO.setNation(d.getNation());
		empHrReqDTO.setDepAbbr(d.getDepAbbr());
		empHrReqDTO.setResidentaddress(d.getResidentaddress());
		empHrReqDTO.setGenderName(d.getGenderName());
		empHrReqDTO.setAge(d.getAge());
		empHrReqDTO.setBirthDay(d.getBirthDay());
		empHrReqDTO.setPhone(d.getMobile());
		empHrReqDTO.setEmail(d.getEmail());
		empHrReqDTO.setBadge(d.getBadge());
		empHrReqDTO.setName(d.getName());
		empHrReqDTO.setCompID(d.getCompID());
		empHrReqDTO.setCompname(d.getCompname());
		empHrReqDTO.setDepid(d.getDepid());
		empHrReqDTO.setDepname(d.getDepname());
		empHrReqDTO.setJobid(d.getJobid());
		empHrReqDTO.setJobname(d.getJobname());
		empHrReqDTO.setReportTo(d.getReportTo());
		if (null == d.getStatus()) {
			empHrReqDTO.setStatus(StaffStatusEnum.STAFF_STATUS_IN.getCode());
			empHrReqDTO.setStatus(1);
		} else {
			empHrReqDTO.setStatus(StaffStatusEnum.changeStaffStatus(d.getStatus()));
		}
		empHrReqDTO.setStatusName(d.getStatusName());
		empHrReqDTO.setEmpType(d.getEmpType());
		empHrReqDTO.setEmptypeName(d.getEmptypeName());
		empHrReqDTO.setJchenID(d.getJchenID());
		empHrReqDTO.setJchenName(d.getJchenName());
		empHrReqDTO.setJoindate(d.getJoindate());
		empHrReqDTO.setEid(d.getEID());
		empHrReqDTO.setPzid(d.getPZID());
		empHrReqDTO.setLeaType(d.getLeaType());
		empHrReqDTO.setPqcompany(d.getPqcompany());
		return empHrReqDTO;
	}

	private void saveEvwEmpHrYs(EvwEmphrYs evwEmphrYs) {
		log.info("更新数据:{}", JSONUtil.toJsonStr(evwEmphrYs));
		try {
			EmpHrReqDTO empHrReqDTO = getEmpHrReqDTO(evwEmphrYs);
			Result result = remoteStaffService.syncStaff(empHrReqDTO, SecurityConstants.FROM_IN);
			log.info(result.getMsg());
			Thread.sleep(100L);
		} catch (Exception e) {
			log.error(e.toString());
		}
	}

	/**
	 * 同步许昌dhr人脸照片 20分钟一次
	 */
	@Scheduled(fixedDelayString  = "${smart.schedule.sync.task.syncXcDhrFaceImg}", initialDelay = 1000 * 60)
	public void syncXcDhrFaceImg() {
		if (taskJob.getSyncXcDhrFaceImg() && iSwitchService.process(TimerTaskEnum.DHR_IMG_XC)) {
			log.info("发起许昌DHR员工人脸数据同步任务");
			smtStaffTaskService.syncXCStaffPhoto();
			log.info("结束许昌DHR员工人脸数据同步任务");
		}
	}

	/**
	 * 许昌员工人脸照片补偿同步任务 30分钟一次
	 */
	@Scheduled(fixedDelay = 1000 * 60 * 30, initialDelay = 1000 * 60 * 2)
	public void staffNoPhotoSyncXc() {
		if (taskJob.getStaffNoPhotoSyncXc() && iSwitchService.process(TimerTaskEnum.STAFF_NO_PHOTO_XC)) {
			log.info("发起许昌员工人脸照片补偿同步任务");
			smtStaffTaskService.syncStaffNoPhoto(1);
			log.info("结束许昌员工人脸照片补偿同步任务");
		}
	}

	/**
	 * 员工人脸照片补偿同步任务 30分钟一次
	 */
	@Scheduled(fixedDelay = 1000 * 60 * 30, initialDelay = 1000 * 60 * 3)
	public void staffNoPhotoSync() {
		if (taskJob.getStaffNoPhotoSync() && iSwitchService.process(TimerTaskEnum.STAFF_NO_PHOTO)) {
			log.info("发起员工人脸照片补偿同步任务");
			smtStaffTaskService.syncStaffNoPhoto(2);
			log.info("结束员工人脸照片补偿同步任务");
		}
	}
}
