package com.tce.smart.platform.client.supplier;

import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.client.identity.ClientPersonnelDirectory;
import com.tce.smart.platform.core.client.supplier.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.Clock;
import java.util.*;

/** 认证先于资料读取；HTTP返回显式投影，证件摘要及内部资格对象不离开服务端。 */
public class SupplierAccessService {
    private final SupplierAccessProperties properties;
    private final SupplierAdmissionSource source;
    private final SupplierAccessRepository repository;
    private final Clock clock;
    private final SupplierAdmissionLock admissionLock;
    private final ClientPersonnelDirectory personnel;

    public SupplierAccessService(SupplierAccessProperties properties, SupplierAdmissionSource source,
            SupplierAccessRepository repository, Clock clock, SupplierAdmissionLock admissionLock) {
        this(properties, source, repository, clock, admissionLock, null);
    }

    /** 旧纯HTTP测试保持五参构造；生产配置必须传入人员目录以每次复核在职状态。 */
    public SupplierAccessService(SupplierAccessProperties properties, SupplierAdmissionSource source,
            SupplierAccessRepository repository, Clock clock, SupplierAdmissionLock admissionLock,
            ClientPersonnelDirectory personnel) {
        this.properties = properties; this.source = source; this.repository = repository; this.clock = clock;
        this.admissionLock = admissionLock; this.personnel = personnel;
    }

    public Map<String, Object> verify(String credentialCode, String postId) {
        SupplierOperator actor = authorize("supplier:execute");
        SupplierAccessProperties.Post post = requirePost(actor, postId);
        return admissionLock.withQualification(source, credentialCode, post, clock, qualification -> {
            SupplierVerification result = repository.verifyOrInitialize(qualification, actor, mapping(post), clock, UUID.randomUUID().toString());
            return verificationResponse(result, post);
        });
    }

    public Map<String, Object> record(String verificationId, String postId, String direction, String idempotencyKey) {
        SupplierOperator actor = authorize("supplier:execute");
        SupplierAccessProperties.Post post = requirePost(actor, postId);
        if (!SupplierAccessProperties.identifier(verificationId) || idempotencyKey == null
                || !idempotencyKey.matches("[A-Za-z0-9][A-Za-z0-9._:-]{0,127}")) throw new SupplierAccessHttpException(400);
        SupplierDirection movement;
        if ("enter".equals(direction)) movement = SupplierDirection.ENTER;
        else if ("leave".equals(direction)) movement = SupplierDirection.LEAVE;
        else throw new SupplierAccessHttpException(400);
        SupplierVerification verified = repository.findVerification(verificationId);
        if (verified == null) throw new SupplierAccessHttpException(404);
        if (!actor.getOperatorId().equals(verified.getOperatorId()) || !postId.equals(verified.getPostId())
                || !post.getAreaId().equals(verified.getAreaId())) throw new SupplierAccessHttpException(403);
        // 使用已存核验人员主键持锁重读资格，锁一直保留到通行仓储提交，客户端不能替换人员或申请。
        return admissionLock.withQualification(source, verified.getQualificationSnapshot().getBadgeId(), post, clock, current -> {
            SupplierPassageResult result = repository.record("client008:supplier-access", idempotencyKey, verificationId,
                    current, actor, mapping(post), movement, clock, UUID.randomUUID().toString());
            return eventResponse(result.getEvent());
        });
    }

    public List<Map<String, Object>> listEvents() {
        SupplierOperator actor = authorize("supplier:read");
        List<Map<String, Object>> results = new ArrayList<>();
        if (actor.getAuthorizedPostIds().isEmpty()) return results;
        for (SupplierPassageEvent event : repository.listEvents(actor.getAuthorizedPostIds(), 100)) {
            if (!actor.isAuthorizedForPost(event.getPostId()) || results.size() >= 100) throw new SupplierAccessHttpException(503);
            results.add(eventResponse(event));
        }
        return results;
    }

    private SupplierOperator authorize(String permission) {
        if (!properties.isEnabled()) throw new SupplierAccessHttpException(503);
        properties.validate();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof SmartUser)) throw new SupplierAccessHttpException(401);
        SmartUser user = (SmartUser) auth.getPrincipal();
        if (!user.isEnabled() || !user.isAccountNonExpired() || !user.isAccountNonLocked()
                || !user.isCredentialsNonExpired() || user.getId() == null || user.getId() <= 0) throw new SupplierAccessHttpException(401);
        // 令牌有效不代表人员仍可办理；生产路径始终按当前员工/外包/派遣主数据复核。
        if (personnel != null) personnel.require(user.getUsername());
        Set<String> permissions = new LinkedHashSet<>();
        for (GrantedAuthority authority : auth.getAuthorities()) permissions.add(authority.getAuthority());
        if (!permissions.contains(permission)) throw new SupplierAccessHttpException(403);
        Set<String> posts = new LinkedHashSet<>();
        for (SupplierAccessProperties.Post post : properties.getPosts()) {
            if (permissions.contains("supplier:post:" + post.getId()) && user.getParkIdList() != null
                    && user.getParkIdList().contains(post.getParkId())) posts.add(post.getId());
        }
        return SupplierOperator.authenticated(user.getUsername(), permissions, posts);
    }

    private SupplierAccessProperties.Post requirePost(SupplierOperator actor, String postId) {
        if (!SupplierAccessProperties.identifier(postId)) throw new SupplierAccessHttpException(400);
        SupplierAccessProperties.Post post = properties.post(postId);
        if (post == null || !actor.isAuthorizedForPost(postId)) throw new SupplierAccessHttpException(403);
        return post;
    }
    private SupplierPostAreaMapping mapping(SupplierAccessProperties.Post post) {
        return SupplierPostAreaMapping.fromTrustedDirectory(post.getId(), post.getAreaId());
    }

    private Map<String, Object> verificationResponse(SupplierVerification v, SupplierAccessProperties.Post post) {
        SupplierQualificationSnapshot q = v.getQualificationSnapshot();
        Map<String, Object> result = publicIdentity(q);
        result.put("id", v.getVerificationId()); result.put("postId", v.getPostId()); result.put("areaName", post.getAreaName());
        result.put("photoUrl", q.getPhotoUrl()); result.put("visitorPhone", q.getPersonPhone());
        result.put("hostName", q.getHostName()); result.put("hostPhone", q.getHostPhone());
        Set<String> areaNames = new LinkedHashSet<>();
        for (SupplierAccessProperties.Post p : properties.getPosts()) if (q.getAuthorizedAreaIds().contains(p.getAreaId())) areaNames.add(p.getAreaName());
        result.put("authorizedAreas", new ArrayList<>(areaNames));
        result.put("validFrom", q.getValidFrom().toString()); result.put("validUntil", q.getValidUntil().toString());
        result.put("expiresAt", v.getExpiresAt().toString()); result.put("allowed", true); result.put("reason", "");
        result.put("presence", v.getPresence().name().toLowerCase(Locale.ROOT));
        result.put("allowedDirections", v.getPresence() == SupplierPresence.UNKNOWN ? Arrays.asList("enter", "leave")
                : Collections.singletonList(v.getPresence() == SupplierPresence.INSIDE ? "leave" : "enter"));
        return result;
    }
    private Map<String, Object> eventResponse(SupplierPassageEvent event) {
        Map<String, Object> result = publicIdentity(event.getQualificationSnapshot());
        result.put("id", event.getEventId()); result.put("verificationId", event.getVerificationId());
        result.put("postId", event.getPostId());
        SupplierAccessProperties.Post post = properties.post(event.getPostId());
        result.put("areaName", post != null && event.getAreaId().equals(post.getAreaId()) ? post.getAreaName() : event.getAreaId());
        result.put("direction", event.getDirection().name().toLowerCase(Locale.ROOT));
        result.put("operatorName", operatorDisplayName(event.getOperatorId())); result.put("occurredAt", event.getOccurredAt().toString());
        return result;
    }

    private String operatorDisplayName(String staffNo) {
        if (personnel == null) return "安检人员";
        String displayName = personnel.displayNameOrStaffNo(staffNo);
        return displayName == null || displayName.trim().isEmpty() || staffNo.equals(displayName) ? "安检人员" : displayName;
    }
    /** 框架日志只显示类型说明；Jackson仍逐项序列化允许公开的字段。 */
    private static final class PublicResponse extends LinkedHashMap<String, Object> {
        private static final long serialVersionUID = 1L;
        @Override public String toString() { return "供应商通行响应（人员资料已隐藏）"; }
    }
    private Map<String, Object> publicIdentity(SupplierQualificationSnapshot q) {
        Map<String, Object> result = new PublicResponse();
        result.put("badgeId", q.getBadgeId()); result.put("visitorName", q.getPersonName());
        result.put("supplierName", q.getCompanyName()); result.put("admissionId", q.getAdmissionId()); return result;
    }
}
