package com.tce.smart.platform.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.DorStaffPerfectDTO;
import com.tce.smart.platform.api.dto.req.LockPwdUpdateDTO;
import com.tce.smart.platform.api.dto.req.SelfLockPwdRefreshReqDTO;
import com.tce.smart.platform.api.dto.req.remoteLock.LockDormitoryStaffDTO;
import com.tce.smart.platform.api.dto.resp.DormitoryRoomDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.DormitoryStaffRespDTO;
import com.tce.smart.platform.api.dto.resp.dormitorymange.DormitoryLockInfoRespDTO;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.SmtDormitoryStaff;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.ext.DormitoryRoomExt;
import com.tce.smart.platform.core.vo.StaffInDormitoryVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 员工宿舍信息表
 *
 * @author 齐佩
 * @date 2019-04-18 14:32:40
 */
public interface SmtDormitoryStaffService extends IService<SmtDormitoryStaff> {

	/**
	 * 编辑入住备注
	 * @param id
	 * @param remark
	 * @return
	 */
	Boolean editSimpleRemark(Integer id, String remark);

	SmtDormitoryStaff getDormitoryStaff(Integer dormitoryId, Integer floorId, Integer roomId, Integer bedId);

	IPage<StaffInDormitoryVO> getSmtDormitoryStaff(Page page, StaffInDormitoryDTO staffInDormitoryDTO);

	/**
	 * 删除已入住未报道员工
	 * @param ids
	 * @return
	 */
	Boolean deleteNotRegister(List<Integer> ids);

	Result addInDormitory(InDormitoryDTO inDormitory);

	/**
	 * 查询当天入住人员至锁平台
	 * @param parkId
	 * @param createTime
	 * @return
	 */
	List<LockDormitoryStaffDTO> getSmtDormitoryStaffToLock(Integer parkId, String createTime);

	/**
	 * 查询今天入住的数据
	 * @return
	 */
	IPage<DormitoryStaffRespDTO> queryTodayIn(Page page);

	/**
	 * 换宿操作
	 * @param staffBadge 工号 如果是临时入住则是身份证号
	 * @param bedId
	 * @return
	 */
	Boolean changeBed(String staffBadge,Integer bedId,Integer oldBedId);

	/**
	 * 添加宿舍入住记录
	 * @param staffBadge
	 * @param bedId
	 * @return
	 */
	Boolean addDormitoryStaff(String staffBadge, Integer bedId);



	Boolean addDormitoryStaff(DormitoryStaffDTO smtDormitoryStaff);

	Boolean addDormitoryStaff(DormitoryStaffDTO smtDormitoryStaff,Integer isStaff);

	/**
	 * 添加临时人员入住记录
	 * @param smtDormitoryStaff
	 * @return
	 */
	Boolean addDormitoryStaffTemp(DormitoryStaffDTO smtDormitoryStaff);

	/**
	 * 更新临时人员入住信息
	 * @param smtStaff
	 * @return
	 */
	Boolean updateDormitoryStaffTemp(SmtStaff smtStaff);

	Result removeBedById(Integer id);

	Result updateById(UpdateDormitoryStaffDTO smtDormitoryStaff);

	Result changeDormitory(UpdateDormitoryStaffDTO smtDormitoryStaff);

	/**
	 * 退宿操作
	 * @param inId
	 * @return
	 */
	Boolean checkOutDormitory(Integer inId, Integer type);

	/**
	 * 根据员工工号查询当前入住信息
	 * @return
	 */
	DormitoryRoomDetailRespDTO getStaffRoomInfo(String staffBadge);
	/**
	 * 根据员工工号查询当前入住信息列表
	 * @return
	 */
	List<DormitoryRoomDetailRespDTO> getStaffRoomInfoList(String staffBadge);

	DormitoryRoomDetailRespDTO getStaffRoomInfoByPhone(String phone,String name);

	Result addDormitory(DormitoryStaffDTO dormitoryStaffDTO);

	/**
	 * 查询房间床位使用数量
	 * @param roomIds
	 * @return
	 */
	List<DormitoryRoomExt> getRoomBedUse(List<Integer> roomIds);

	/**
	 * 批量新增员工入住记录信息
	 * @param dormitoryStaffReqDTOList
	 * @return
	 */
	List<DormitoryStaffReqDTO> batchAddDormitoryStaff(List<DormitoryStaffReqDTO> dormitoryStaffReqDTOList);

	Boolean addDormitoryStaffOne(DormitoryStaffReqDTO staffReqDTO);

	/**
	 * 批量更新员工入住及退宿信息
	 * @param dormId
	 * @param multipartFile
	 * @return
	 */
	ResponseEntity<byte[]> batchImportPersons(Integer dormId, MultipartFile multipartFile);

	/**
	 * 查询室友
	 * @param roomId
	 * @return
	 */
	List<SmtDormitoryStaff> getRoommate(List<Integer> roomId, String badge);

	/**
	 * 获得智能锁状态
	 * @param roomId
	 * @param badge
	 * @return
	 */
	DormitoryLockInfoRespDTO getLockStatus(Integer roomId, String badge);

	List<DormitoryRoomDetailRespDTO> getSimpleStaffRoomList(String staffBadge);

	/**
	 * 人脸比对获取动态码
	 * @param perfectDTO
	 * @return
	 */
	String faceCompare(DorStaffPerfectDTO perfectDTO);

	/**
	 * 根据工号获取动态码
	 * @param badge
	 * @return
	 */
	String getPwdByBadge(String badge);

	/**
	 * 更新动态码
	 * @param perfectDTO
	 * @return
	 */
	String updatePwdByBadge(DorStaffPerfectDTO perfectDTO);

	/**
	 * 修改门锁密码
	 * @param lockPwdUpdateDTO
	 * @return
	 */
	String updateLockPwdByBadge(LockPwdUpdateDTO lockPwdUpdateDTO);

	/**
	 * 获取当前认证员工的门锁动态码。
	 */
	String getPwdForAuthenticatedStaff(String badge);

	/**
	 * 修改当前认证员工的门锁动态码。
	 */
	String updateLockPwdForAuthenticatedStaff(String badge, String newPwd);

	/**
	 * 通过人脸核验刷新当前认证员工的门锁动态码。
	 */
	String refreshPwdForAuthenticatedStaff(String badge, SelfLockPwdRefreshReqDTO request);

	/**
	 * 当前认证员工通过人脸核验读取本人门锁动态码。
	 */
	String faceCompareForAuthenticatedStaff(String badge, SelfLockPwdRefreshReqDTO request);

	/**
	 * 修改备注
	 * @param remark
	 * @return
	 */
	Boolean updateSimpleRemark(Long id, String remark);
}
