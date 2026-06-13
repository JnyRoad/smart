package com.tce.smart.platform.service.remoteLock;

import com.tce.smart.platform.api.dto.req.remoteLock.LockDormitoryStaffDTO;
import com.tce.smart.platform.api.dto.resp.remoteLock.DeviceTypeInfoDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryStaff;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author fushiping
 * @date 2021/5/25 0025 18:28
 **/
public interface ConnectLockService {

	/**
	 * 新增锁卡片
	 */
	void sendSave(LockDormitoryStaffDTO staffDTO);

	/**
	 * 发送锁平台请求
	 * @param dormitoryStaff
	 * @param type
	 * @param oldRoom
	 * @return
	 */
	Boolean sendLockData(SmtDormitoryStaff dormitoryStaff, Integer type, Integer oldRoom);


	/**
	 * 修改锁平台工号与密码
	 * @param personNum 旧工号
	 * @param personName 姓名
	 * @param newPersonNum 新工号
	 * @param newPersonPhone 新电话
	 */
	void updateLockPerson(String personNum, String personName, String newPersonNum, String newPersonPhone);

	/**
	 * 根据工号获得动态码
	 * @param badge
	 * @return
	 */
	String getPwdByBadge(String badge);

	/**
	 * 根据工号生成动态码
	 * @param badge
	 * @return
	 */
	String generatePwdByBadge(String badge);


	/**
	 * 根据工号修改新密码
	 * @param badge
	 * @param newPwd
	 * @return
	 */
	String updatePwdByBadge(String badge,String newPwd);

	/**
	 * 根据工号判断是否已录入指纹
	 * @param badge
	 * @return
	 */
	Boolean existFingerByBadge(String badge);

	/**
	 * 根据房间id获得门锁支持的钥匙
	 * @param roomId
	 * @return
	 */
	DeviceTypeInfoDTO getLockType(Integer roomId);

}
