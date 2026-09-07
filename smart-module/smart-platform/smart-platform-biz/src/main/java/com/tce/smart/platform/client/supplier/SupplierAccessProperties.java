package com.tce.smart.platform.client.supplier;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.*;

/** 默认关闭且无预置岗位；开启时必须由服务端配置完整园区及入厂区域映射。 */
@Getter
@Setter
@ConfigurationProperties(prefix = "smart.client.supplier")
public class SupplierAccessProperties {
    private boolean enabled;
    private String businessTimezone = "Asia/Shanghai";
    private List<Post> posts = new ArrayList<>();

    /** 仅描述该接口可用的可信岗位，不授予用户岗位权限。 */
    @Getter
    @Setter
    public static class Post {
        private String id;
        private String name;
        private Integer parkId;
        private String parkName;
        private String areaId;
        private String areaName;
        private String admittanceAreaTypeCode;
    }

    public void validate() {
        if (!enabled) return;
        if (!"Asia/Shanghai".equals(businessTimezone) || posts == null || posts.isEmpty()) invalid();
        Set<String> ids = new HashSet<>();
        Map<String, String> areaMappings = new HashMap<>();
        for (Post p : posts) {
            if (p == null || !identifier(p.id) || !identifier(p.areaId) || blank(p.name)
                    || p.parkId == null || p.parkId <= 0 || blank(p.parkName) || blank(p.areaName)
                    || p.admittanceAreaTypeCode == null || !p.admittanceAreaTypeCode.matches("0|[1-9][0-9]{0,8}")
                    || !ids.add(p.id)) invalid();
            String mapping = p.parkId + ":" + p.admittanceAreaTypeCode;
            String previous = areaMappings.put(p.areaId, mapping);
            if (previous != null && !previous.equals(mapping)) invalid();
        }
    }

    public Post post(String id) {
        if (posts != null) for (Post p : posts) if (p != null && Objects.equals(p.id, id)) return p;
        return null;
    }
    static boolean blank(String value) { return value == null || value.trim().isEmpty(); }
    static boolean identifier(String value) { return value != null && value.matches("[A-Za-z0-9][A-Za-z0-9._:-]{0,95}"); }
    private static void invalid() { throw new IllegalArgumentException("供应商通行岗位配置无效"); }
}
