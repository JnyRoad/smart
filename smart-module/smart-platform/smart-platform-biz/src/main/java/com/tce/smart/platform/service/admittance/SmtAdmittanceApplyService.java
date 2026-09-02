package com.tce.smart.platform.service.admittance;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.admittance.SaveAdmittanceApplyReqDTO;
import com.tce.smart.platform.api.dto.req.admittance.SaveAdmittanceCarApplyReqDTO;
import com.tce.smart.platform.api.dto.resp.SearchVisitorDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.VisitorListRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorWechatIdentityRespDTO;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.SearchTadayVisitorDetail;
import com.tce.smart.platform.core.entity.SearchTodayVisitor;
import com.tce.smart.platform.core.entity.SearchVisitorDetail;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.vo.*;
import com.tce.smart.platform.service.SmtSnapVehicleService;

import java.util.List;

/**
 * 入厂申请预约表
 *
 * @author fushiping
 * @date 2021-08-17 17:45:45
 */
public interface SmtAdmittanceApplyService extends IService<SmtAdmittanceApply> {


	/**
	 * 新增入厂申请
	 * @param saveAdmittanceApply
	 * @return
	 */
	SmtAdmittanceApply saveAdmittanceApply(SaveAdmittanceApplyReqDTO saveAdmittanceApply);

	/**
	 * 新增货车申请预约
	 * @param saveSmtVisitor
	 * @return
	 */
	SmtAdmittanceApply saveAdmittanceCarApply(SaveAdmittanceCarApplyReqDTO saveSmtVisitor);

	Boolean visitorEqualCheck(SaveAdmittanceApplyReqDTO saveSmtVisitor);

	VisitorWechatIdentityRespDTO getOpenId(String code);

	/**
	 * 根据流程ID获得入厂申请
	 * @param processId
	 * @return
	 */
	SmtAdmittanceApply getByProcessId(String processId);

	/**
	 * 修改OA审批状态
	 * @param admittanceApply
	 */
	void updateStatus(SmtAdmittanceApply admittanceApply);

	/**
	 * claim 式落 OA 终态：以 CAS（status=待审核 → finalStatus）抢占本条申请的终态处理权，
	 * 抢占成功后执行审批后置处理（权限下发/短信等，即 updateStatus），后置失败仅标记 deviceStatus=FAIL 交补偿任务重试。
	 * OA 回调链路与拉取对账（updateOaStatusTask）共用该入口，保证同一申请只有一方执行后置副作用。
	 *
	 * @param admittanceApply 仍处于待审核态的申请（需含 id/processId/endTime）
	 * @param finalStatus     OA 终态（Status_0 通过 / Status_1 拒绝）
	 * @return true=本次抢占成功并已执行（或已补偿登记）后置处理；false=已被其他链路处理，调用方应跳过
	 */
	boolean claimAndApplyOaFinalStatus(SmtAdmittanceApply admittanceApply, Integer finalStatus);

	/**
	 * 抓拍车辆如果是访客的则补全车辆记录信息，否则不处理
	 * @param entity 抓拍车辆信息
	 * @return
	 */
	void admittanceSnapVehicleHandle(AddSnapVehicleDTO entity);

	/**
	 * 判断被访人是否存在
	 * @param apply
	 * @return
	 */
	SmtAdmittanceApply searchReceptionist(SmtAdmittanceApply apply);

	/**
	 * 根据id获得访客信息
	 * @param id
	 * @return
	 */
	SmtAdmittanceApply searchDetailById(Long id);

	/**
	 * 分页查询
	 * @param page
	 * @param searchSmtVisitorDTO
	 * @return
	 */
	IPage<SearchSmtVisitorVO> getSmtVisitorPage(Page page, SearchSmtVisitorDTO searchSmtVisitorDTO);

	/**
	 * 获取访问者预约记录
	 */
	IPage<VisitorListRespDTO> getVisitRecord(Page page, String visitorPhone);

	/**
	 * 重新下发访客设备权限
	 * @param id
	 * @return
	 */
	Boolean repeatVisitorDeviceAuth(Long id);

	/**
	 * 获得访客凭条
	 * @param code
	 * @return
	 */
	SmtAdmittanceApply searchVisitorByCode(String code);

	/**
	 * 根据身份证获得访客预约
	 * @param idCard
	 * @return
	 */
	List<SmtAdmittanceApply> getByIdCard(String idCard);

	/**
	 * 根据身份证获得最新访客预约
	 * @param idCard
	 * @return
	 */
	SmtAdmittanceApply getLastByIdCard(String idCard);

	Boolean delDeviceAuth(Long id);

	/**
	 * 作废入厂申请，并提交该申请关联人员、车辆的权限回收任务。
	 * 调用方必须在调用前完成申请单归属校验。
	 *
	 * @param apply 已完成归属校验的申请单
	 * @return 是否作废成功
	 */
	Boolean revokeApply(SmtAdmittanceApply apply);

	/**
	 * 访客超时提示
	 */
	void visitorOverTime();

	/**
	 * 访客已到达状态删除设备权限
	 */
	void visitorComeOnTime();

	/**
	 * 访客未到达时发送提示
	 */
	void visitorRemind();

	/**
	 * 超时未离开的访客提示
	 */
	void visitorOverTimeNoLeave();

	/**
	 * @param id 文件上传服务器
	 * @return
	 */
	Boolean smbPutPhoto(Long id);
	/**
	 * 获得图片下载地址
	 * @return
	 */
	String getRemoteUrl();

	/**
	 * 定时更新OA审批状态
	 */
	void updateOaStatusTask();
}
