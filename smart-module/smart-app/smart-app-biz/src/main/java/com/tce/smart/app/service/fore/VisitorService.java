package com.tce.smart.app.service.fore;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tce.smart.app.ao.fore.ApproveVisitorAo;
import com.tce.smart.app.ao.fore.VisitorAo;
import com.tce.smart.app.ao.fore.VisitorIdAo;
import com.tce.smart.app.ao.wechat.AddVisitMemberAo;
import com.tce.smart.app.ao.wechat.AddVisitorAo;
import com.tce.smart.app.ao.wechat.CheckFaceAo;
import com.tce.smart.app.ao.wechat.CheckHostAo;
import com.tce.smart.app.api.dto.WechatVisitorRecordDetailReqDTO;
import com.tce.smart.app.api.dto.WechatVisitorRecordReqDTO;
import com.tce.smart.app.vo.fore.MemberVo;
import com.tce.smart.app.vo.fore.VisitorCountVo;
import com.tce.smart.app.vo.fore.VisitorDetailVo;
import com.tce.smart.app.vo.fore.VisitorTypeVo;
import com.tce.smart.app.vo.wechat.AddVisitorVo;
import com.tce.smart.app.vo.wechat.CheckHostVo;
import com.tce.smart.app.vo.wechat.PhotoBaseVisitorVo;
import com.tce.smart.app.vo.wechat.PhotoVisitorVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SmtParkDTO;
import com.tce.smart.platform.api.dto.resp.VisitorListRespDTO;

import java.util.List;
import java.util.Map;

/**
 * 访客信息服务接口
 *
 * @author ly
 * @date 2019-05-10 16:16:08
 */
public interface VisitorService {

	/**
	 * 获取访客的审核列表
	 * @param params
	 * @param visitorAo
	 * @return
	 */
	IPage<?> getVisitorList(Map<String, Object> params, VisitorAo visitorAo);

	/**
	 * 获取访客的详情
	 * @param visitorId
	 * @return
	 */
	VisitorDetailVo getVisitorListDeatil(VisitorIdAo visitorId);

	/** App 已登录用户查看本人关联的预约详情。 */
	VisitorDetailVo getAppVisitorListDetail(VisitorIdAo visitorId);

	/**
	 * 获取随行人员的详情
	 * @param visitorId
	 * @return
	 */
	MemberVo getMemberListDeatil(VisitorIdAo visitorId);

	/** App 已登录用户查看本人关联预约的随行人员。 */
	MemberVo getAppMemberListDetail(VisitorIdAo visitorId);

	/**
	 * 添加访客信息
	 *
	 * @param addVisitorAo addVisitorAo
	 * @return
	 */
	AddVisitorVo addVisitor(AddVisitorAo addVisitorAo);

	/**
	 * 从微信公众号添加访客信息
	 *
	 * @param addVisitorAo addVisitorAo
	 * @return
	 */
	AddVisitorVo addVisitorFromWechat(AddVisitorAo addVisitorAo);
	/**
	 * 添加随行人员
	 * @param addVisitMemberAo
	 */
	void addFellowVisitor(AddVisitMemberAo addVisitMemberAo);

	/**
	 * 公众号添加随行人员
	 * @param addVisitMemberAo
	 */
	void addFellowVisitorWechat(AddVisitMemberAo addVisitMemberAo);

	/**
	 * 获取访客的来访类型
	 * @return
	 */
	VisitorTypeVo getVisitorType();

	/**
	 * 被访对象信息查询
	 *
	 * @param checkHostAo checkHostAo
	 * @return CheckHostVo
	 */
	CheckHostVo checkhost(CheckHostAo checkHostAo);

	/**
	 * 访客审核
	 * @param approveVisitorAo
	 */
	Boolean approveVisitorByVisitId(ApproveVisitorAo approveVisitorAo);

	/**
	 * 查询未审批的个数
	 * @return
	 */
	VisitorCountVo getToApprovalCount();

	/**
	 * 判断人脸
	 * @param checkFaceAo
	 * @return
	 */
	PhotoVisitorVo checkFace(CheckFaceAo checkFaceAo, String capability, String draftId);

	/**
	 * 获取图片base64
	 * @param photoVisitorVo
	 * @return
	 */
	PhotoBaseVisitorVo getFace(PhotoVisitorVo photoVisitorVo);

	Boolean addCheck(AddVisitorAo addVisitorAo);


	Result<?> checkBlackVisitor(AddVisitorAo addVisitorAo, String capability, String draftId);

	Result getVisitorRefuseType();

	List<SmtParkDTO> getPark();

	Result<?> getParks(String staffBadge);

	Result<?> checkBlackVehicle(AddVisitorAo addVisitorAo);

	/**
	 * 微信公众号查询预约记录
	 * @param wechatVisitorRecordReqDTO
	 * @return
	 */
	IPage<VisitorListRespDTO> getVisitRecord(WechatVisitorRecordReqDTO wechatVisitorRecordReqDTO);

	/**
	 * 微信公众号查询预约记录详情
	 * @param wechatVisitorRecordDetailReqDTO
	 * @return
	 */
	VisitorDetailVo getVisitRecordDetail(WechatVisitorRecordDetailReqDTO wechatVisitorRecordDetailReqDTO);

	/**
	 * 通过预约记录ID查询预约详情
	 * @param id
	 * @return
	 */
	VisitorDetailVo getVisitRecordDetailById(Long id);

	/**
	 * 微信公众号预约记录 再约一次
	 * @param wechatVisitorRecordDetailReqDTO
	 * @return
	 */
	Boolean wechatVisitorAgain(WechatVisitorRecordDetailReqDTO wechatVisitorRecordDetailReqDTO);

}
