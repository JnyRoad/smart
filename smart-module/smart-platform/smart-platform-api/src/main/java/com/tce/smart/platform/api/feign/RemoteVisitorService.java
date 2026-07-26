package com.tce.smart.platform.api.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SaveSmtVisitorDTO;
import com.tce.smart.platform.api.dto.SmtVisitorDTO;
import com.tce.smart.platform.api.dto.req.AddFellowVisitorReqDTO;
import com.tce.smart.platform.api.dto.req.AddWechatFellowVisitorReqDTO;
import com.tce.smart.platform.api.dto.req.SaveWechatSmtVisitorReqDTO;
import com.tce.smart.platform.api.dto.req.VisitorAgainReqDTO;
import com.tce.smart.platform.api.dto.req.admittance.VisitorActionCapabilityConsumeReqDTO;
import com.tce.smart.platform.api.dto.resp.SearchAppSmtVisitorRespDTO;
import com.tce.smart.platform.api.dto.resp.AppVisitorSelfDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.VisitorListRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 访客
 * @author 梁圆
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteVisitorService {

	/**
	 * 添加随行访客的信息
	 * @param addFellowVisitorDTO
	 * @return
	 */
	@PostMapping("/visitor/addFellowVisitor")
	Result addFellowVisitor(@RequestBody AddFellowVisitorReqDTO addFellowVisitorDTO, @RequestHeader(SecurityConstants.FROM) String from);
	/**
	 * 公众号添加随行访客的信息
	 * @param addWechatFellowVisitorDTO
	 * @return
	 */
	@PostMapping("/visitor/addWechatFellowVisitor")
	Result addWechatFellowVisitor(@RequestBody AddWechatFellowVisitorReqDTO addWechatFellowVisitorDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 公众号添加访客的信息
	 * @param saveWechatSmtVisitorReqDTO saveWechatSmtVisitorReqDTO
	 * @param from from
	 * @return
	 */
	@PostMapping("/visitor/addWechatVisitor")
	Result<SmtVisitorDTO> addWechatVisitor(@RequestBody SaveWechatSmtVisitorReqDTO saveWechatSmtVisitorReqDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 微信公众号预约 再约一次
	 * @param id
	 * @param from from/visitor/addWechatVisitor
	 * @return
	 */
	@PostMapping("/visitor/wechatVisitorAgain")
	Result<Boolean> wechatVisitorAgain(@RequestBody VisitorAgainReqDTO visitorAgainReqDTO, @RequestHeader(SecurityConstants.FROM) String from);



	/**
	 * 添加访客的信息
	 * @param saveSmtVisitorDTO saveSmtVisitorDTO
	 * @param from from
	 * @return
	 */
	@PostMapping("/visitor/addVisitor")
	Result<SmtVisitorDTO> addSmtVisitor(@RequestBody SaveSmtVisitorDTO saveSmtVisitorDTO, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 查询访客的列表信息
	 * @param current,size,staffBadge
	 * @return
	 */
	@GetMapping("/visitor/app/searchAppVisitorPage")
	Result<Page<SearchAppSmtVisitorRespDTO>> searchAppVisitorPage(@RequestParam("current") final long current, @RequestParam("size") final long size, @RequestParam("staffBadge") final String staffBadge, @RequestParam("visitListType") final Integer visitListType, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 查询员工未审批的
	 * @param staffBadge
	 * @return
	 */
	@GetMapping("/visitor/app/searchAppVisitorCount")
	Result searchAppVisitorCount(@RequestParam("staffBadge") final String staffBadge,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * Smart App 按当前登录员工读取其关联的访客详情。
	 * actorBadge 只能由 App 服务端从已认证会话派生，Platform 会再次核验记录归属。
	 */
	@GetMapping("/internal/app-visitor/detail/{visitorId}")
	Result<AppVisitorSelfDetailRespDTO> getAppVisitorDetailForActor(@PathVariable("visitorId") Long visitorId,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader("X-Smart-Actor-Park-Ids") String actorParkIds,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

	/** App 仅能为本人发起或接待的预约补充随行人员。 */
	@PostMapping("/internal/app-visitor/fellow")
	Result addAppFellowForActor(@RequestBody AddFellowVisitorReqDTO request,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader("X-Smart-Actor-Park-Ids") String actorParkIds,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

	/**
	 *修改
	 *
	 * @param smtVisitor
	 * @return
	 */
	@PostMapping("/visitor/app/updateVisitorStatus")
	Result updateVisitorStatus(@RequestBody SmtVisitorDTO smtVisitor, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 *查询被访人的信息
	 *
	 * @param smtVisitor
	 * @return
	 */
	@PostMapping("/visitor/app/searchReceptionist")
	Result<SmtVisitorDTO> SearchReceptionist(@RequestBody SmtVisitorDTO smtVisitor,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 *查询被访人的信息
	 *
	 * @param smtVisitor
	 * @return
	 */
	@PostMapping("/visitor/wechat/searchReceptionist")
	Result<SmtVisitorDTO> SearchReceptionistForWechat(@RequestBody SmtVisitorDTO smtVisitor,@RequestHeader(SecurityConstants.FROM) String from);

	/** 访客身份证黑名单校验仅供 Smart App 服务端调用；结果只能是是否允许预约。 */
	@PostMapping("/internal/visitor-blacklist/visitor")
	Result<Boolean> checkVisitorBlacklist(@RequestBody SmtVisitorDTO smtVisitor,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

	/** 访客车牌黑名单校验仅供 Smart App 服务端调用；结果只能是是否允许预约。 */
	@PostMapping("/internal/visitor-blacklist/vehicle")
	Result<Boolean> checkVehicleBlacklist(@RequestBody SmtVisitorDTO smtVisitor,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

	/**
	 * App 在执行匿名访客上传或黑名单校验前原子消费 capability。
	 * 服务令牌标记会让 Feign 拦截器改用 App 专属 client_credentials，不能透传浏览器令牌。
	 */
	@PostMapping("/admittance/visitor-action/internal/consume")
	Result<Boolean> consumeVisitorActionCapability(@RequestBody VisitorActionCapabilityConsumeReqDTO request,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 查询拒绝访客的原因
	 * @return
	 */
	@GetMapping("/visitor/refuseType")
	Result getVisitorRefuseType();


	/**
	 * 微信公众号查询预约记录
	 * @param current,size,visitorPhone
	 * @return
	 */
	@GetMapping("/visitor/record/list")
	Result<Page<VisitorListRespDTO>> wechatGetVisitRecord(@RequestParam("current") Integer current, @RequestParam("size") Integer size, @RequestParam("visitorPhone") String visitorPhone, @RequestHeader(SecurityConstants.FROM) String from);


}
