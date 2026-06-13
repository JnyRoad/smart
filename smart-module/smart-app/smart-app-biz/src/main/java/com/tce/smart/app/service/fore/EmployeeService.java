package com.tce.smart.app.service.fore;

import com.tce.smart.app.ao.fore.EmployeeUpdateAo;
import com.tce.smart.app.ao.fore.PerfectInfoAo;
import com.tce.smart.app.ao.fore.RoomApplyAo;
import com.tce.smart.app.vo.fore.EmployeeInfoVo;
import com.tce.smart.app.vo.fore.EmployeeSalayType;
import com.tce.smart.app.vo.fore.EmployeeVo;
import com.tce.smart.app.vo.fore.RoomDetailVo;
import com.tce.smart.app.vo.wechat.RelationTypeVO;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SmtOutDormitoryStaffDTO;

import java.util.List;
import java.util.Map;
/**
 * 员工信息接口
 * @author qipei
 *
 */
public interface EmployeeService {

	/**
	 * 获取员工基本信息
	 *
	 * @param badge 员工号
	 * @return
	 */
	EmployeeVo getBaseinfo(String badge);

	EmployeeInfoVo getFullinfo(String badge);

	Result relationUpdate(EmployeeUpdateAo employeeUpdateAo);

	Result roomApply(RoomApplyAo roomApplyVo);

	RoomDetailVo roomDetail();

	Result qrcode();

	List<RelationTypeVO> relationList();

	Result outRoomApply(SmtOutDormitoryStaffDTO outDormitory);

	Result getAllowance(String staffBadge, Integer type);

	Result outRoomDetaol(String staffBadge, Integer type);

	/**
	 * 更新员工人脸信息
	 *
	 * @param perfectInfoAo 人脸信息
	 * @return true-成功|false-失败
	 */
	Boolean updateFacePhoto(PerfectInfoAo perfectInfoAo);

	/**
	 * 同步员工照片信息到裕同C6、EHR
	 * @return true-成功，false-失败
	 */
	Boolean syncPhotoToYuto();

	/**
	 * 获取员工薪资结算类型
	 * @param badge
	 * @return
	 */
	EmployeeSalayType getSalayType(String badge);

	/**
	 * 查看员工申请外宿记录详情
	 * @param staffBadge
	 * @return
	 */
	Result outRoomApplyDetail(Integer id);

	/**
	 * 外宿补贴撤销申请
	 * @param staffBadge
	 * @param backDate
	 * @return
	 */
	Result addCallowanceCancel(String staffBadge, String backDate, Integer type);

	/**
	 * 查询员工外宿补贴撤销列表
	 * @param staffBadge
	 * @return
	 */
	Result callowanceCancelList(String staffBadge);

	/**
	 * 查询员工外宿补贴撤销详情
	 * @param id
	 * @return
	 */
	Result callowanceCancelDetail(Integer id);

	/**
	 * 员工及外宿补贴信息
	 * @param staffBadge
	 * @return
	 */
	Result callowanceUInfo(String staffBadge, Integer type);

	/**
	 * 查询员工补贴详情
	 * @param staffBadge
	 * @return
	 */
	Result callowanceDetail(String staffBadge, Integer type);

	/**
	 * 查询空余创维
	 * @param staffBadge
	 * @return
	 */
	Result getFreeBed(Integer parkId, String staffBadge);

	/**
	 * 获取外宿补贴时间的开始时间的设置
	 * @return
	 */
	Result<Integer> getDormitorySet();

	/**
	 * 查询外宿列表
	 * @param staffBadge
	 * @return
	 */
	Result outRoomList(Map<String, Object> params,String staffBadge);

	/**
	 * 查询外宿详情
	 * @param id
	 * @return
	 */
	Result outRoomListDetail(Integer id);

	/**
	 * 根据外宿id查询外宿详情
	 * @param recordId
	 * @return
	 */
	Result outRoomDetailById(String recordId);

}
