package com.tce.smart.platform.client.supplier;

import com.tce.smart.platform.core.client.supplier.SupplierQualificationSnapshot;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;
import com.tce.smart.platform.core.mapper.SmtAdmittanceApplyMapper;
import com.tce.smart.platform.core.mapper.SmtAdmittanceFellowMapper;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.tool.enums.AdmittancePersonCertTypeEnum;
import org.apache.ibatis.session.SqlSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

/** 只按人员主键关联现有入厂申请；不查询预约码或长期供应商台账。 */
public class SupplierAdmissionSource {
    private final SmtAdmittanceFellowMapper fellows;
    private final SmtAdmittanceApplyMapper applies;
    private final ImageService images;
    private final SupplierAccessProperties properties;

    public SupplierAdmissionSource(SmtAdmittanceFellowMapper fellows, SmtAdmittanceApplyMapper applies,
            ImageService images, SupplierAccessProperties properties) {
        this.fellows = fellows; this.applies = applies; this.images = images; this.properties = properties;
    }

    public SupplierQualificationSnapshot load(String credentialCode, SupplierAccessProperties.Post post, Instant now) {
        Long badge = parseBadge(credentialCode);
        SmtAdmittanceFellow fellow = fellows.selectById(badge);
        if (fellow == null || !badge.equals(fellow.getId()) || fellow.getVisitorId() == null || fellow.getVisitorId() <= 0) missing();
        SmtAdmittanceApply apply = applies.selectById(fellow.getVisitorId());
        if (apply == null || !fellow.getVisitorId().equals(apply.getId())) missing();
        if (!Integer.valueOf(1).equals(apply.getApplyType())) denied();
        if (post == null || !Objects.equals(post.getParkId(), apply.getParkId())) denied();
        if (!Integer.valueOf(0).equals(apply.getStatus()) && !Integer.valueOf(3).equals(apply.getStatus())) denied();
        if (SupplierAccessProperties.blank(fellow.getFellowName()) || SupplierAccessProperties.blank(apply.getCompany())
                || SupplierAccessProperties.blank(fellow.getCertNo())) denied();
        if (apply.getStartTime() == null || apply.getEndTime() == null) denied();
        ZoneId zone = ZoneId.of(properties.getBusinessTimezone());
        Instant from = apply.getStartTime().atZone(zone).toInstant();
        Instant until = apply.getEndTime().atZone(zone).toInstant();
        if (!until.isAfter(from) || now.isBefore(from) || !now.isBefore(until)) denied();
        Set<String> allowedCodes = areaCodes(apply.getAreaType());
        if (!allowedCodes.contains(post.getAdmittanceAreaTypeCode())) denied();
        Set<String> areaIds = new LinkedHashSet<>();
        for (SupplierAccessProperties.Post candidate : properties.getPosts()) {
            if (Objects.equals(candidate.getParkId(), apply.getParkId()) && allowedCodes.contains(candidate.getAdmittanceAreaTypeCode())) areaIds.add(candidate.getAreaId());
        }
        String normalizedCert = normalizedCertificate(fellow.getCertNo());
        if (normalizedCert.isEmpty()) denied();
        int certificateType = certificateType(fellow.getCertType(), normalizedCert);
        String personId = "admittance-person:" + digest(certificateType + ":" + normalizedCert);
        // 公司和人员没有独立启停字段；这里的active仅表示来源记录完整且申请已通过。
        String companyId = "admittance-company:" + digest(normalizedText(apply.getCompany()));
        boolean mainMatches = Integer.valueOf(1).equals(fellow.getIsMain())
                && normalizedCert.equals(normalizedCertificate(apply.getCertNo()))
                && normalizedText(fellow.getFellowName()).equals(normalizedText(apply.getVisitorName()));
        String photo = SupplierAccessProperties.blank(fellow.getFellowPhotoId()) ? "" : text(images.buildImageUrl(apply.getParkId(), fellow.getFellowPhotoId()));
        return SupplierQualificationSnapshot.fromTrustedSource(credentialCode, personId, companyId, apply.getId().toString(),
                true, true, true, true, true, from, until, areaIds,
                fellow.getFellowName(), apply.getCompany(), photo, mainMatches ? text(apply.getVisitorPhone()) : "",
                text(apply.getReceptionistName()), text(apply.getReceptionistPhone()));
    }

    /** 使用持有资格行锁的同一连接及既有Mapper；关闭会话由锁执行器负责。 */
    public SupplierQualificationSnapshot loadUsingSession(SqlSession session, String credentialCode,
            SupplierAccessProperties.Post post, Instant now) {
        return new SupplierAdmissionSource(session.getMapper(SmtAdmittanceFellowMapper.class),
                session.getMapper(SmtAdmittanceApplyMapper.class), images, properties).load(credentialCode, post, now);
    }

    /** 历史H5主/陪同页只收18位身份证并校验ISO7064，OA同步明确使用zjlx=0。
     * 仅满足相同规则的历史空类型解释为身份证；已有0与历史null必须使用同一人员摘要。
     * 其他明确类型以现有0..5枚举为准，不能把未知类型或任意空类型当身份证。
     */
    private static int certificateType(Integer declared, String normalized) {
        if (declared != null) {
            if (AdmittancePersonCertTypeEnum.getEnmu(declared) == null) denied();
            return declared;
        }
        if (!normalized.matches("[0-9]{17}[0-9X]")) denied();
        int[] weights = {7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2};
        int sum = 0;
        for (int i = 0; i < weights.length; i++) sum += (normalized.charAt(i) - '0') * weights[i];
        if (normalized.charAt(17) != "10X98765432".charAt(sum % 11)) denied();
        return AdmittancePersonCertTypeEnum.ID_CARD.getCode();
    }

    static Long parseBadge(String value) {
        if (value == null || !value.matches("[1-9][0-9]{0,18}")) throw new SupplierAccessHttpException(400);
        try { return Long.valueOf(value); } catch (NumberFormatException invalid) { throw new SupplierAccessHttpException(400); }
    }
    private Set<String> areaCodes(String raw) {
        if (raw == null || raw.length() > 2048) denied();
        Set<String> result = new LinkedHashSet<>();
        for (String token : raw.split(",", -1)) {
            String code = token.trim();
            if (!code.matches("0|[1-9][0-9]{0,8}")) denied();
            result.add(code);
        }
        return result;
    }
    private static String normalizedCertificate(String value) {
        return normalizedText(value).toUpperCase(Locale.ROOT);
    }
    private static String normalizedText(String value) {
        return value == null ? "" : Normalizer.normalize(value, Normalizer.Form.NFKC).trim();
    }
    static String text(String value) { return value == null ? "" : value; }
    private static String digest(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte b : bytes) hex.append(String.format(Locale.ROOT, "%02x", b & 0xff));
            return hex.toString();
        } catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException("摘要服务不可用"); }
    }
    private static void missing() { throw new SupplierAccessHttpException(404); }
    private static void denied() { throw new SupplierAccessHttpException(403); }
}
