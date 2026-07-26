package com.tce.smart.platform.controller;

import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.AddFellowVisitorReqDTO;
import com.tce.smart.platform.api.dto.resp.AppVisitorFellowRespDTO;
import com.tce.smart.platform.api.dto.resp.AppVisitorSelfDetailRespDTO;
import com.tce.smart.platform.core.dto.AddFellowVisitorDTO;
import com.tce.smart.platform.core.dto.SaveFellowVisitorDTO;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.core.vo.GetSmtFellowVisitorVO;
import com.tce.smart.platform.core.vo.SearchAppVisitorDetailVO;
import com.tce.smart.platform.service.SmtVisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Smart App 访客记录的最小内部入口。
 *
 * 浏览器传入的 visitorId 只是定位键，不能作为授权依据；App 从登录态取得 actorBadge 后，
 * Platform 必须再以预约发起人或被访人归属验证。这样即使 App 网关层出现参数替换，
 * 也不会让一个员工读取或修改另一个员工关联的访客资料。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/app-visitor")
public class InternalAppVisitorController extends BaseController {
	private static final String PURPOSE = "app-visitor-self";

	private final SmtVisitorService smtVisitorService;
	private final OpenApiAuthenticationAdapter authenticationAdapter;

	/** 配置为空即拒绝，避免把任意 server scope 服务当作 Smart App。 */
	@Value("${security.inner.visitor-app.app-client-id:}")
	private String appServiceClientId;

	@Inner
	@OpenApi("server")
	@GetMapping("/detail/{visitorId}")
	public Result<AppVisitorSelfDetailRespDTO> detail(@PathVariable("visitorId") Long visitorId,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader(value = "X-Smart-Actor-Park-Ids", required = false) String actorParkIds,
			@RequestHeader(value = SecurityConstants.FROM, required = false) String from,
			@RequestHeader(value = "X-Smart-Internal-Purpose", required = false) String purpose) {
		assertAppCaller(from, purpose);
		assertActorOwnsVisitor(visitorId, actorBadge, actorParkIds);
		SearchAppVisitorDetailVO detail = smtVisitorService.searchAppVisitorDetailById(visitorId);
		if (detail == null) {
			throw new AccessDeniedException("访客记录不存在或无权访问");
		}
		return success(toSelfDetail(detail));
	}

	@Inner
	@OpenApi("server")
	@PostMapping("/fellow")
	public Result addFellow(@RequestBody AddFellowVisitorReqDTO request,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader(value = "X-Smart-Actor-Park-Ids", required = false) String actorParkIds,
			@RequestHeader(value = SecurityConstants.FROM, required = false) String from,
			@RequestHeader(value = "X-Smart-Internal-Purpose", required = false) String purpose) {
		assertAppCaller(from, purpose);
		if (request == null) {
			throw new AccessDeniedException("随行人员请求不完整");
		}
		assertActorOwnsVisitor(request.getVisitId(), actorBadge, actorParkIds);
		smtVisitorService.addFellowVisitor(toFellowCommand(request));
		return success();
	}

	/** API 契约与领域命令分层转换，防止内部 DTO 直接穿透至业务层。 */
	private AddFellowVisitorDTO toFellowCommand(AddFellowVisitorReqDTO request) {
		AddFellowVisitorDTO command = new AddFellowVisitorDTO();
		command.setVisitId(request.getVisitId());
		if (request.getFollowList() == null) {
			command.setFollowList(null);
			return command;
		}
		List<SaveFellowVisitorDTO> followers = new ArrayList<>();
		for (com.tce.smart.platform.api.dto.req.SaveFellowVisitorReqDTO source : request.getFollowList()) {
			if (source == null) {
				continue;
			}
			SaveFellowVisitorDTO target = new SaveFellowVisitorDTO();
			BeanUtils.copyProperties(source, target);
			followers.add(target);
		}
		command.setFollowList(followers);
		return command;
	}

	/** 记录不存在、工号为空及跨主体均统一拒绝，避免把记录存在性暴露给调用方。 */
	private void assertActorOwnsVisitor(Long visitorId, String actorBadge, String actorParkIds) {
		if (visitorId == null || StrUtil.isBlank(actorBadge)) {
			throw new AccessDeniedException("访客记录不存在或无权访问");
		}
		SmtVisitor visitor = smtVisitorService.getById(visitorId);
		if (visitor == null || (!actorBadge.equals(visitor.getPromoterBadge())
				&& !actorBadge.equals(visitor.getReceptionistBadge()))
				|| !actorParksContain(actorParkIds, visitor.getParkId())) {
			throw new AccessDeniedException("访客记录不存在或无权访问");
		}
	}

	/** App 只能转发其认证会话中已解析出的园区集合；缺失、非法或跨园区均拒绝。 */
	private boolean actorParksContain(String actorParkIds, Integer visitorParkId) {
		if (visitorParkId == null || StrUtil.isBlank(actorParkIds)) {
			return false;
		}
		Set<Integer> parks = new HashSet<>();
		for (String value : actorParkIds.split(",")) {
			try {
				parks.add(Integer.valueOf(value.trim()));
			} catch (RuntimeException ignored) {
				return false;
			}
		}
		return parks.contains(visitorParkId);
	}

	/** 仅显式映射允许展示的字段，禁止将底层详情对象直接复制至浏览器响应。 */
	private AppVisitorSelfDetailRespDTO toSelfDetail(SearchAppVisitorDetailVO source) {
		AppVisitorSelfDetailRespDTO target = new AppVisitorSelfDetailRespDTO();
		target.setParkId(source.getParkId());
		target.setParkName(source.getParkName());
		target.setVisitorId(source.getVisitorId());
		target.setVisitorName(source.getVisitorName());
		target.setVisitorPhoto(source.getVisitorPhoto());
		target.setVisitorPhone(maskPhone(source.getVisitorPhone()));
		target.setVehiclePlate(maskPlate(source.getVehiclePlate()));
		target.setCompany(source.getCompany());
		target.setCause(source.getCause());
		target.setCauseDesc(source.getCauseDesc());
		target.setStatus(source.getStatus());
		target.setStatusDesc(source.getStatusDesc());
		target.setStartTime(source.getStartTime());
		target.setEndTime(source.getEndTime());
		target.setReceptionistName(source.getReceptionistName());
		target.setReceptionistPhone(maskPhone(source.getReceptionistPhone()));
		target.setCarryThing(source.getCarryThing());
		target.setCarryThingDesc(source.getCarryThingDesc());
		if (source.getFellowVisitorList() != null) {
			List<AppVisitorFellowRespDTO> followers = new ArrayList<>();
			for (GetSmtFellowVisitorVO fellow : source.getFellowVisitorList()) {
				if (fellow == null) {
					continue;
				}
				AppVisitorFellowRespDTO follower = new AppVisitorFellowRespDTO();
				follower.setId(fellow.getId());
				follower.setFellowName(fellow.getFellowName());
				follower.setFellowPhoto(fellow.getFellowPhoto());
				followers.add(follower);
			}
			target.setFellowVisitorList(followers);
		}
		return target;
	}

	private String maskPhone(String phone) {
		if (StrUtil.isBlank(phone) || phone.length() < 7) {
			return "****";
		}
		return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
	}

	private String maskPlate(String plate) {
		if (StrUtil.isBlank(plate) || plate.length() < 3) {
			return "****";
		}
		return plate.substring(0, 2) + "****" + plate.substring(plate.length() - 1);
	}

	/** server scope 还必须同时满足精确 OAuth client、内部来源和固定用途。 */
	private void assertAppCaller(String from, String purpose) {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (!SecurityConstants.FROM_IN.equals(from) || !PURPOSE.equals(purpose)
				|| StrUtil.isBlank(appServiceClientId) || authentication == null
				|| !authenticationAdapter.isClientOnly(authentication)
				|| !appServiceClientId.equals(authenticationAdapter.clientId(authentication))) {
			throw new AccessDeniedException("App 访客内部调用未获授权");
		}
	}
}
