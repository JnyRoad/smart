package com.tce.smart.platform.client.identity;

import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.client.release.ReleaseAccessProperties;
import com.tce.smart.platform.client.supplier.SupplierAccessProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 把 OAuth 主体、人员主数据和服务器岗位目录收敛为 App 可消费的最小身份契约。 */
@Service
public class ClientIdentityService {
	private static final Set<String> EXPOSED_PERMISSIONS = new LinkedHashSet<>(Arrays.asList(
			"item-pass:apply", "item-pass:approve", "item-pass:execute", "item-pass:read",
			"supplier:execute", "supplier:read"));
	/**
	 * 入口是服务器白名单，而非前端根据权限自行猜测。App 仍用本地注册表解析路由，
	 * 这里绝不下发 URL 或可执行脚本，后续加模块只需添加一个受审查的定义。
	 */
	private static final List<ClientAppDefinition> APP_DEFINITIONS = Arrays.asList(
			new ClientAppDefinition("item-pass-apply", "物品放行", "物品放行申请", "提交保密物品放行申请", "item-pass:apply", 10),
			new ClientAppDefinition("item-pass-approve", "物品放行", "物品放行审批", "处理物品放行审批待办", "item-pass:approve", 20),
			new ClientAppDefinition("item-pass-execute", "物品放行", "物品现场执行", "执行物品放行出发与到达核验", "item-pass:execute", 30),
			new ClientAppDefinition("item-pass-records", "物品放行", "物品放行记录", "查询物品放行申请记录", "item-pass:read", 40),
			new ClientAppDefinition("visitor-badge-check", "保密区通行", "厂牌扫码核验", "扫描已打印厂牌并登记进入或离开保密区", "supplier:execute", 50),
			new ClientAppDefinition("visitor-passage-records", "保密区通行", "通行记录", "查询供应商保密区通行记录", "supplier:read", 60));
	private final ClientPersonnelDirectory personnel;
	private final ReleaseAccessProperties releases;
	private final SupplierAccessProperties suppliers;

	public ClientIdentityService(ClientPersonnelDirectory personnel, ReleaseAccessProperties releases,
			SupplierAccessProperties suppliers) {
		this.personnel = personnel; this.releases = releases; this.suppliers = suppliers;
	}

	public ClientAuthenticatedPrincipal current() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| !(authentication.getPrincipal() instanceof SmartUser)) throw new ClientApiException(401);
		SmartUser user = (SmartUser) authentication.getPrincipal();
		if (!user.isEnabled() || !user.isAccountNonExpired() || !user.isAccountNonLocked()
				|| !user.isCredentialsNonExpired() || user.getId() == null || user.getId().intValue() <= 0) throw new ClientApiException(401);
		Set<String> permissions = new LinkedHashSet<>();
		for (GrantedAuthority authority : authentication.getAuthorities()) if (authority != null && authority.getAuthority() != null)
			permissions.add(authority.getAuthority());
		return new ClientAuthenticatedPrincipal(personnel.require(user.getUsername()), permissions, user.getParkIdList());
	}

	public Map<String, Object> response() {
		ClientAuthenticatedPrincipal subject = current();
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("subjectId", subject.getPerson().getStaffNo());
		result.put("staffNo", subject.getPerson().getStaffNo());
		result.put("displayName", subject.getPerson().getDisplayName());
		result.put("employmentType", subject.getPerson().getEmploymentType());
		result.put("organization", subject.getPerson().getOrganization());
		List<String> permissions = new ArrayList<>();
		for (String permission : EXPOSED_PERMISSIONS) if (subject.has(permission)) permissions.add(permission);
		result.put("permissions", permissions);
		result.put("posts", posts(subject));
		return result;
	}

	/** 返回当前主体有权使用的已注册模块，不包含路由、前端实现或未授权模块。 */
	public List<Map<String, Object>> apps() {
		ClientAuthenticatedPrincipal subject = current();
		List<Map<String, Object>> result = new ArrayList<>();
		for (ClientAppDefinition definition : APP_DEFINITIONS) {
			if (!subject.has(definition.permission)) continue;
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("id", definition.id);
			item.put("category", definition.category);
			item.put("title", definition.title);
			item.put("description", definition.description);
			item.put("permission", definition.permission);
			item.put("sort", definition.sort);
			result.add(item);
		}
		return result;
	}

	private List<Map<String, Object>> posts(ClientAuthenticatedPrincipal subject) {
		Map<String, Map<String, Object>> unique = new LinkedHashMap<>();
		if (releases.isEnabled()) for (ReleaseAccessProperties.Post post : releases.getPosts()) {
			if (post != null && subject.has("item-pass:post:" + post.getId()) && subject.getParkIds().contains(post.getParkId()))
				unique.put(post.getId(), post(post.getId(), post.getName(), post.getParkId(), post.getParkName()));
		}
		if (suppliers.isEnabled()) for (SupplierAccessProperties.Post post : suppliers.getPosts()) {
			if (post != null && subject.has("supplier:post:" + post.getId()) && subject.getParkIds().contains(post.getParkId())) {
				Map<String, Object> existing = unique.get(post.getId());
				if (existing != null && (!post.getName().equals(existing.get("name"))
						|| !String.valueOf(post.getParkId()).equals(existing.get("parkId"))))
					throw new ClientApiException(503);
				unique.put(post.getId(), post(post.getId(), post.getName(), post.getParkId(), post.getParkName()));
			}
		}
		return new ArrayList<>(unique.values());
	}

	private Map<String, Object> post(String id, String name, Integer parkId, String parkName) {
		Map<String, Object> value = new LinkedHashMap<>();
		value.put("id", id); value.put("name", name); value.put("parkId", String.valueOf(parkId)); value.put("parkName", parkName);
		return value;
	}

	private static final class ClientAppDefinition {
		private final String id, category, title, description, permission;
		private final int sort;
		private ClientAppDefinition(String id, String category, String title, String description, String permission, int sort) {
			this.id = id; this.category = category; this.title = title; this.description = description; this.permission = permission; this.sort = sort;
		}
	}
}
