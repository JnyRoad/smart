package com.tce.smart.platform.service.remoteLock.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.platform.api.dto.req.remoteLock.LockDormitoryStaffDTO;
import com.tce.smart.platform.api.dto.req.remoteLock.PersonUpdateDTO;
import com.tce.smart.platform.api.dto.resp.remoteLock.DeviceTypeInfoDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryStaff;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.enums.ISCDeviceTaskEnum;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.service.remoteLock.ConnectLockService;
import com.tce.smart.tool.enums.DormitoryHisotryTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author fushiping
 * @date 2021/5/25 0025 18:28
 **/
@Slf4j
@Service
public class ConnectLockServiceImpl implements ConnectLockService {


	@Value("${spring.lock.base.url}")
	private String lockBaseUrl;

	/**
	 * 发送锁平台请求
	 */
	private final String saveUrl = "/person/dormitory";

	/**
	 * 修改锁平台用户数据
	 */
	private final String updateUrl = "/person/update/by-num-name";

	/**
	 * 获得门锁支持的开门方式
	 */
	private final String supportKeyUrl = "/api/device/support/key/";

	/**
	 * 判断是否已录入指纹
	 */
	private final String fingerGetUrl = "/api/person/exist/finger/";

	/**
	 * 获得门锁动态码
	 */
	private final String passwordGetUrl = "/api/person/password/";

	/**
	 * 生成动态码
	 */
	private final String generatePwdUrl = "/api/person/generate/password/";

	/**
	 * 修改开锁密码
	 */
	private final String updatePwdUrl = "/api/person/update/password/";

	@Autowired
	private SmtStaffMapper smtStaffMapper;


	@Override
	@Async
	public Boolean sendLockData(SmtDormitoryStaff dormitoryStaff, Integer type, Integer oldRoom) {
		if (DormitoryHisotryTypeEnum.CHANGE_DORMITORY.getCode().equals(type)
				&& dormitoryStaff.getRoomId().equals(oldRoom)) {
			log.info("同房间换床位");
			return Boolean.TRUE;
		}
		LockDormitoryStaffDTO lockStaff = new LockDormitoryStaffDTO();
		if (StringUtils.isNotEmpty(dormitoryStaff.getStaffBadge())) {
			SmtStaff staff = smtStaffMapper.selectOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, dormitoryStaff.getStaffBadge()));
			if (Objects.nonNull(staff)) {
				lockStaff.setPhone(staff.getPhone());
			}
		}
		lockStaff.setCreateTime(LocalDateTime.now());
		lockStaff.setDormitoryId(dormitoryStaff.getDormitoryId());
		lockStaff.setName(dormitoryStaff.getStaffName());
		lockStaff.setParkId(dormitoryStaff.getParkId());
		lockStaff.setBadge(dormitoryStaff.getStaffBadge());
		lockStaff.setFloorId(dormitoryStaff.getFloorId());
		lockStaff.setOldRoomId(oldRoom);
		lockStaff.setIsStaff(dormitoryStaff.getIsStaff());
		lockStaff.setRoomId(dormitoryStaff.getRoomId());
		switch (DormitoryHisotryTypeEnum.eventTypeAuthority(type)) {
			case IN_DORMITORY:
				lockStaff.setAction("A");
				break;
			case CHANGE_DORMITORY:
				lockStaff.setAction("C");
				break;
			case QUTI_DORMITORY:
			case OUT_DORMITORY:
			case OUT_SELF:
			case CHECK_OUT_DORMITORY:
				lockStaff.setAction("O");
				break;
		}
		this.sendSave(lockStaff);
		return Boolean.TRUE;
	}

	@Override
	@Async
	public void updateLockPerson(String personNum, String personName, String newPersonNum, String newPersonPhone) {
		PersonUpdateDTO updateDTO = PersonUpdateDTO.builder()
				.newPersonNum(newPersonNum)
				.personName(personName)
				.personNum(personNum)
				.newPersonPhone(newPersonPhone).build();
		ResponseEntity<String> response = new RestTemplate().postForEntity(lockBaseUrl + updateUrl, updateDTO, String.class);
		if (response.getStatusCode() != HttpStatus.OK) {
			log.info("请求处理锁平台人员修改请求{},resp={}", response);
			throw new SmartException("请求处理锁平台人员修改请求失败");
		}
	}

	@Override
	public void sendSave(LockDormitoryStaffDTO staffDTO) {
		ResponseEntity<String> response = new RestTemplate().postForEntity(lockBaseUrl + saveUrl, staffDTO, String.class);
		if (response.getStatusCode() != HttpStatus.OK) {
			log.info("请求处理锁卡片,resp={}", response);
			throw new SmartException("处理锁卡片请求失败");
		}
	}

	@Override
	public DeviceTypeInfoDTO getLockType(Integer roomId) {
		Result result = this.getTypeReq(lockBaseUrl + supportKeyUrl + roomId);
		if (Objects.nonNull(result.getData()) && result.getCode().equals(ISCDeviceTaskEnum.DEVICE_OK.getCode())) {
			return JSONUtil.toBean(JSONUtil.parseObj(result.getData()), DeviceTypeInfoDTO.class);
		}
		return null;
	}

	@Override
	public Boolean existFingerByBadge(String badge) {
		Result result = this.getTypeReq(lockBaseUrl + fingerGetUrl + badge);
		if (Objects.nonNull(result.getData())) {
			return JSONUtil.toBean(JSONUtil.parseObj(result.getData()), Boolean.class);
		}
		return null;
	}

	@Override
	public String getPwdByBadge(String badge) {
		Result result = this.getTypeReq(lockBaseUrl + passwordGetUrl + badge);
		if (Objects.nonNull(result.getData())) {
			return result.getData().toString();
		}
		return null;
	}

	@Override
	public String generatePwdByBadge(String badge) {
		Result result = this.getTypeReq(lockBaseUrl + generatePwdUrl + badge);
		if (Objects.nonNull(result.getData())) {
			return result.getData().toString();
		}
		return null;
	}
	@Override
	public String updatePwdByBadge(String badge,String newPwd) {
		Result result = this.getTypeReq(lockBaseUrl + updatePwdUrl + badge + "/" + newPwd);
		if (Objects.nonNull(result.getData())) {
			return result.getData().toString();
		}
		return null;
	}

	private Result getTypeReq(String url) {
		ResponseEntity<String> response = new RestTemplate().getForEntity(url, String.class);
		if (response.getStatusCode() != HttpStatus.OK) {
			log.info("锁平台接口请失败,resp={}", response);
			throw new SmartException("锁平台接口请失败");
		}
		return JSONUtil.toBean(response.getBody(), Result.class);
	}
}
